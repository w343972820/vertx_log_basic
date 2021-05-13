package vertx;


import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;


public class ConfigVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
    retriever.getConfig(ar ->{
      if (ar.succeeded()){
        JsonObject json=ar.result();
        String port = json.getString("port");
        System.out.println("port:"+port);
      }else{
        System.out.println(ar.cause().getMessage());
      }
    });


  }

  public static void main(String[] args) {
    //第一行
    Vertx.vertx().deployVerticle(new ConfigVerticle());
  }



}
