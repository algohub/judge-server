package org.algohub.rest.service.impl;

import org.algohub.engine.pojo.Problem;
import org.algohub.engine.util.ObjectMapperInstance;
import org.algohub.rest.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ProblemServiceImpl implements ProblemService {
  @Autowired
  private StringRedisTemplate redisTemplate;

  public void addProblem(String json) {
    try {
       final Problem problem = ObjectMapperInstance.INSTANCE.readValue(json, Problem.class);
       final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
       valueOps.set(SubmissionServiceImpl.GLOBAL_KEY_PREIFIX + "problem:" + problem.getId(), json);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Problem getProblemById(String id) {
    final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
    final String jsonStr = valueOps.get(SubmissionServiceImpl.GLOBAL_KEY_PREIFIX + "problem:" + id);
    if (jsonStr == null) {
      return null;
    }

    try {
      return ObjectMapperInstance.INSTANCE.readValue(jsonStr, Problem.class);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
