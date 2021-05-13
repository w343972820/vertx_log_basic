package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;

public class MysqlVerticle extends AbstractVerticle {
  //第一步 声明router
  Router router;
  //配置连接参数
  MySQLConnectOptions connectOptions = new MySQLConnectOptions()
    .setPort(3307)
    .setHost("192.168.1.69")
    .setDatabase("test")
    .setUser("root")
    .setPassword("DEbZYnu8?KaCtZCZ");

  // 配置连接池
  PoolOptions poolOptions = new PoolOptions()
    .setMaxSize(5);

  // 创建连接
  MySQLPool client;
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    //第二步 初始化router
    router = Router.router(vertx);
    System.out.println("进来一次。。。");
    // 创建连接
    client = MySQLPool.pool(vertx,connectOptions, poolOptions);

    //收到网页请求时查询数据库
    router.route("/vico").handler(req -> {
      Integer offset = Integer.parseInt(req.request().getParam("page"));
      System.out.println(offset);
      client.getConnection(ar1 ->{
        if (ar1.succeeded()){
          System.out.println("连接成功..");
          SqlConnection conn = ar1.result();
          conn
            //.query("SELECT username,password FROM role_user")普通查询
            .preparedQuery("SELECT username,password FROM role_user limit 5 offset ?")   //带参查询
            //多个问号可传多个参数
            .execute(Tuple.of(offset),ar2 -> {
            //.execute(ar2 -> {
              conn.close();   //执行完之后关毕
              if (ar2.succeeded()){
                ArrayList<JsonObject> resultsList=new ArrayList<>();
                ar2.result().forEach(item -> {
                  JsonObject json=new JsonObject();
                  json.put("username",item.getValue("username"));
                  json.put("password",item.getValue("password"));
                  resultsList.add(json);
                });
                req.response()
                  .putHeader("content-type", "application/json;charset=UTF-8")
                  .end("Hello router："+resultsList.toString());
              }else{
                req.response()
                  .putHeader("content-type", "application/json;charset=UTF-8")
                  .end("Hello router："+ar2.cause().toString());
              }
            });
        }else{
          System.out.println("Something went wrong " + ar1.cause().getMessage());
        }
      });
    });


    //第三步 将Router与vertx HttpServer绑定
    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
