package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class BodyVerticle extends AbstractVerticle {
  //第一步 声明router
  Router router;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    //第二步 初始化router
    router = Router.router(vertx);

    //post获取body参数必需先加入该行
    router.route().handler(BodyHandler.create());


    //form-data格式
    router.post("/vico/form").handler(req -> {
      String page = req.request().getFormAttribute("page");
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello router -> " + page);
    });
    //json格式
    router.post("/vico/json").handler(req -> {
      //vert.x获取json格式body参数就这么一句req.getBodyAsJson()
      JsonObject page = req.getBodyAsJson();
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello router -> " + page.toString());
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
