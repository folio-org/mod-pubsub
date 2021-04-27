package org.folio.config.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.vertx.core.json.JsonObject;

@Component
public class PubSubUserConfig {
  private String name;
  private String password;

  public PubSubUserConfig(@Value("${SYSTEM_USER_NAME:pub-sub}") String name,
    @Value("${SYSTEM_USER_PASSWORD:pubsub}") String password) {

    this.name = name;
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public JsonObject getUserCredentialsJson() {
    return new JsonObject()
      .put("username", name)
      .put("password", password);
  }
}
