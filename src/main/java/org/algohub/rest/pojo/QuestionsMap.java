package org.algohub.rest.pojo;

import org.algohub.engine.pojo.Question;
import org.springframework.data.redis.support.collections.RedisMap;

import java.util.Map;

public class QuestionsMap {
  private RedisMap<String, String> map;

  public QuestionsMap(RedisMap<String, String> map) {
    this.map = map;
  }

  public RedisMap<String, String> getMap() {
    return map;
  }

  public void setMap(RedisMap<String, String> map) {
    this.map = map;
  }
}
