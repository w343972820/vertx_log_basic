package vertx;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Log4JLoggerFactory;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

import java.util.ArrayList;

public class FuturePromiseVerticle extends AbstractVerticle {
  // 创建连接
  MySQLPool client;
  InternalLogger logger = Log4JLoggerFactory.getInstance(FuturePromiseVerticle.class);

  @Override
  public void start() throws Exception {
    this.getConfig().onSuccess(sqlJson -> {
      logger.error("读取配置成功");
    //配置连接参数
      MySQLConnectOptions connectOptions = new MySQLConnectOptions()
        .setPort(Integer.parseInt(sqlJson.getString("port")))
        .setHost(sqlJson.getString("host"))
        .setDatabase(sqlJson.getString("database"))
        .setUser(sqlJson.getString("user"))
        .setPassword(sqlJson.getString("password"));
      // 配置连接池
      PoolOptions poolOptions = new PoolOptions()
        .setMaxSize(5);
      // 创建连接
      client = MySQLPool.pool(vertx, connectOptions, poolOptions);
    })
    .onFailure(throwable -> {
      logger.error("读取文件异常...."+throwable.toString());
    });

    // 创建HttpServer
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    //经典模式:http://locallhost:8888/vico?page=1&age=10
    router.route("/vico").handler(req -> {
      Integer offset = Integer.parseInt(req.request().getParam("page"));
      //链式调用，直接拿到查询的数据库结果来运用
      this.getCon()
        .compose(con -> this.getRows(con, offset))
        .onSuccess(rows -> {
          ArrayList<JsonObject> resultsList = new ArrayList<>();
          rows.forEach(item -> {
            JsonObject json = new JsonObject();
            json.put("username", item.getValue("username"));
            json.put("password", item.getValue("password"));
            resultsList.add(json);
          });
          req.response()
            .putHeader("content-type", "application/json;charset=UTF-8")
            .end("Hello router：" + resultsList.toString());
        })
      .onFailure(throwable -> {
          //链式总异常
        req.response()
          .putHeader("content-type", "application/json;charset=UTF-8")
          .end(new JsonObject().put("10001","请求超时").toString());
        logger.error("出问题了 - > "+throwable.toString());
      });
    });
    router.route("/").handler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello vico");

    });
    // 把请求交给路由处理--------------------(1)
    server.requestHandler(router::accept);
    server.listen(8888);
  }

  //第一步 获取数据库连接
  private Future<SqlConnection> getCon() {
    Promise<SqlConnection> promise = Promise.promise();
    client.getConnection(ar1 -> {
      if (ar1.succeeded()) {
        System.out.println("连接成功..");
        SqlConnection conn = ar1.result();
        //这一点非常关键，固定写法
        promise.complete(conn);
      } else {
        logger.error("连接失败.."+ar1.cause());
        promise.fail(ar1.cause());
      }
    });
    //非常关键，固定用法
    return promise.future();
  }

  //第二步 用获取的数据库查询数据
  private Future<RowSet<Row>> getRows(SqlConnection conn, Integer offset) {
    Promise<RowSet<Row>> promise = Promise.promise();
    conn
      .preparedQuery("SELECT username,password FROM role_user limit 5 offset ?")   //带参查询
      //多个问号可传多个参数
      .execute(Tuple.of(offset), ar2 -> {
        //.execute(ar2 -> {
        conn.close();   //执行完之后关毕
        if (ar2.succeeded()) {
          RowSet<Row> row = ar2.result();
          promise.complete(row);

        } else {
          logger.error("执行失败..."+ar2.cause());
          //这里相当于继续向上抛出异常，用Promise来向上抛出异常
          promise.fail(ar2.cause());
        }
      });
    return promise.future();
  }

  //获取配置数据
  private Future<JsonObject> getConfig() {
    Promise<JsonObject> promise = Promise.promise();
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
    retriever.getConfig(ar -> {
      if (ar.succeeded()) {
        JsonObject json = ar.result();
        promise.complete(json);
      } else {
        promise.fail(ar.cause());
      }
    });
    return promise.future();
  }
}
