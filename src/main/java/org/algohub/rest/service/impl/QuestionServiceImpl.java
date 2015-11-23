package org.algohub.rest.service.impl;

import org.algohub.engine.pojo.Question;
import org.algohub.engine.util.ObjectMapperInstance;
import org.algohub.rest.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class QuestionServiceImpl implements QuestionService {
  @Autowired
  private StringRedisTemplate redisTemplate;

  public void addQuestion(final String id, final String json) {
    final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
    valueOps.set("question:" + id, json);
  }

  public Question getQuestionById(String id) {
    final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
    final String jsonStr = valueOps.get("question:" + id);
    if (jsonStr == null) {
      return null;
    }

    try {
      return ObjectMapperInstance.INSTANCE.readValue(jsonStr, Question.class);
    } catch (IOException e) {
      return null;
    }
  }
}
