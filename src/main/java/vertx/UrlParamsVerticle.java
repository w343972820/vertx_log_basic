package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;

public class UrlParamsVerticle extends AbstractVerticle {
  //第一步 声明router
  Router router;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    //第二步 初始化router
    router = Router.router(vertx);

    //经典模式:http://locallhost:8888/vico?page=1&age=10
    router.post("/vico").handler(req -> {
      String page = req.request().getParam("page"); //vert.x获取url参数就这一句
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello router -> " + page);
    });

    //传统模式:http://locallhost:8888/vico/1/10
    router.post("/vico/:page").handler(req -> {
      String page = req.request().getParam("page"); //vert.x获取url参数就这一句
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello router - > "+page);
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
