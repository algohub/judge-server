package org.algohub.rest.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.algohub.engine.JudgeEngine;
import org.algohub.engine.judge.StatusCode;
import org.algohub.engine.pojo.JudgeResult;
import org.algohub.engine.pojo.Question;
import org.algohub.engine.util.ObjectMapperInstance;
import org.algohub.rest.pojo.QuestionsMap;
import org.algohub.rest.pojo.Submission;
import org.algohub.rest.pojo.SubmissionResultMap;
import org.algohub.rest.pojo.TaskQueueList;
import org.algohub.rest.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {
  private static final String GLOBAL_KEY_PREIFIX = "judge:";
  private static final String MAX_SUBMISSION_ID_KEY = "max_submission_id";
  private static final String NEW_TASK_NOTIFICATION_KEY = "task_queue";
  private static final String TASK_QUEUE = "task_queue";
  private static final String SUBMISSION_REDULT_MAP_KEY = "submission-result-map";
  private static final String QUESTION_MAP_KEY = "question-map";

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Autowired
  private RedisAtomicLong maxSubmissionId;

  @Autowired
  private SubmissionResultMap submissionResultMap;
  @Autowired
  private QuestionsMap questionsMap;

  @Autowired
  private TaskQueueList taskQueue;

  @Bean
  RedisAtomicLong createAtomicLong(RedisConnectionFactory connectionFactory) {
    return new RedisAtomicLong(GLOBAL_KEY_PREIFIX + MAX_SUBMISSION_ID_KEY, connectionFactory);
  }

  @Bean
  SubmissionResultMap createSubmissionResultMap(StringRedisTemplate redisTemplate) {
    final RedisMap<String, String> redisMap = new DefaultRedisMap<>(
        GLOBAL_KEY_PREIFIX + SUBMISSION_REDULT_MAP_KEY, redisTemplate);
    return new SubmissionResultMap(redisMap);
  }

  @Bean
  QuestionsMap createQuestionMap(StringRedisTemplate redisTemplate) {
    final RedisMap<String, String> redisMap = new DefaultRedisMap<>(
        GLOBAL_KEY_PREIFIX + QUESTION_MAP_KEY, redisTemplate);
    return new QuestionsMap(redisMap);
  }

  @Bean TaskQueueList createTaskQueue(StringRedisTemplate redisTemplate) {
    final RedisList<String> list = new DefaultRedisList<>(
        GLOBAL_KEY_PREIFIX + TASK_QUEUE, redisTemplate);
    return new TaskQueueList(list);
  }

  @Bean RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
      MessageListenerAdapter listenerAdapter) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(listenerAdapter,
        new PatternTopic(GLOBAL_KEY_PREIFIX + NEW_TASK_NOTIFICATION_KEY));

    return container;
  }

  @Bean
  MessageListenerAdapter listenerAdapter(Receiver receiver) {
    return new MessageListenerAdapter(receiver, "receiveMessage");
  }

  @Bean
  Receiver receiver() {
    return new Receiver();
  }

  public long incrementAndGetSubmissionId() {
    return maxSubmissionId.incrementAndGet();
  }

  public void addQuestion(final String id, final String json) {
    questionsMap.getMap().put(id, json);
  }

  public Question getQuestion(String id) {
    final String jsonStr = questionsMap.getMap().get(id);
    if (jsonStr == null) {
      return null;
    }

    try {
      return ObjectMapperInstance.INSTANCE.readValue(jsonStr, Question.class);
    } catch (IOException e) {
      return null;
    }
  }

  public void pushTask(final Submission submission) {
    try {
      final String jsonStr = ObjectMapperInstance.INSTANCE.writeValueAsString(submission);
      taskQueue.getList().offerLast(jsonStr);
      redisTemplate.convertAndSend(GLOBAL_KEY_PREIFIX + NEW_TASK_NOTIFICATION_KEY,
          String.valueOf(submission.getId()));
      setSubmissionResult(submission.getId(), new JudgeResult(StatusCode.PENDING));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  public String popTask() {
      return taskQueue.getList().pollFirst();
  }

  public void setSubmissionResult(long submissionId, final JudgeResult judgeResult) {
    try {
      final String jsonStr = ObjectMapperInstance.INSTANCE.writeValueAsString(judgeResult);
      submissionResultMap.getMap().put(String.valueOf(submissionId), jsonStr);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  public JudgeResult getSubmissionResult(long id) {
    final String jsonStr = submissionResultMap.getMap().get(String.valueOf(id));
    try {
      return ObjectMapperInstance.INSTANCE.readValue(jsonStr, JudgeResult.class);
    } catch (IOException e) {
      return null;
    }
  }

  public void clear() {
    redisTemplate.delete(GLOBAL_KEY_PREIFIX + "max_submission_id");
    redisTemplate.delete(GLOBAL_KEY_PREIFIX + NEW_TASK_NOTIFICATION_KEY);
    redisTemplate.delete(GLOBAL_KEY_PREIFIX + QUESTION_MAP_KEY);
    redisTemplate.delete(GLOBAL_KEY_PREIFIX + SUBMISSION_REDULT_MAP_KEY);
  }

  static class Receiver {
    @Autowired
    @Qualifier("judgeEngine")
    private JudgeEngine judgeEngine;

    @Autowired
    private RedisService redisService;

    public void receiveMessage(String message) {
      try {
        final String jsonStr = redisService.popTask();
        if (jsonStr != null) {
          final Submission submission = ObjectMapperInstance.INSTANCE.readValue(jsonStr,
              Submission.class);
          redisService.setSubmissionResult(submission.getId(), new JudgeResult(StatusCode.RUNNING));
          final JudgeResult judgeResult = judgeEngine.judge(redisService.getQuestion(
              submission.getQuestionId()), submission.getCode(), submission.getLanguage());
          redisService.setSubmissionResult(submission.getId(), judgeResult);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
