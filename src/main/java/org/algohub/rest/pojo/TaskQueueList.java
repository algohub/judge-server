package org.algohub.rest.pojo;

import org.springframework.data.redis.support.collections.RedisList;


public class TaskQueueList {
  RedisList<String> list;

  public TaskQueueList(RedisList<String> list) {
    this.list = list;
  }

  public RedisList<String> getList() {
    return list;
  }

  public void setList(RedisList<String> list) {
    this.list = list;
  }
}
