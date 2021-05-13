package vertx;


import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Log4JLoggerFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;


public class TemplateVerticle  extends AbstractVerticle {
  Router router;
  //第一步声明
  ThymeleafTemplateEngine thymeleafTemplateEngine;
  InternalLogger logger = Log4JLoggerFactory.getInstance(TemplateVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    System.out.println("fdsggygtggg.......");
    logger.error("look...");
    router = Router.router(vertx);
    //第二步初始化
    thymeleafTemplateEngine = ThymeleafTemplateEngine.create(vertx);
    router.route("/").handler(req -> {
      //直接用模版rander,rander的第一个参数可往前端传数据
      JsonObject json=new JsonObject();
      logger.error("vvvvkkkkkk..");
      json.put("name","hello vico..dddd.");
      thymeleafTemplateEngine.render(json,"templates/index.html",bufferAsyncResult -> {
        if (bufferAsyncResult.succeeded()){
          req.response()
            .putHeader("content-type", "text/html")
            .end(bufferAsyncResult.result());
        }else{

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
