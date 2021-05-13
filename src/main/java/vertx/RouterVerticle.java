package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;

public class RouterVerticle extends AbstractVerticle {
  //第一步 声明router
  Router router;
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    //第二步 初始化router
    router = Router.router(vertx);
    //第四步 配置Router解析url
    router.get("/vico").handler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello router");
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
