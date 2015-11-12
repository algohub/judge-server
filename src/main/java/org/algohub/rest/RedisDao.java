package org.algohub.rest;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.algohub.engine.judge.StatusCode;
import org.algohub.engine.pojo.JudgeResult;
import org.algohub.engine.pojo.Question;
import org.algohub.engine.util.ObjectMapperInstance;
import org.algohub.rest.pojo.QuestionsMap;
import org.algohub.rest.pojo.Submission;
import org.algohub.rest.pojo.SubmissionResultMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisMap;

import java.io.IOException;


@Configuration
public class RedisDao {
  private static final String GLOBAL_KEY_PREIFIX = "rest:";
  private static final String MAX_SUBMISSION_ID_KEY = "max_submission_id";
  private static final String TASK_QUEUE_KEY = "task_queue";
  private static final String SUBMISSION_REDULT_MAP_KEY = "submission-result-map";
  private static final String QUESTION_MAP_KEY = "question-map";

  @Value("${redis.host}")
  private String redisHost;

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Autowired
  private RedisAtomicLong maxSubmissionId;

  @Autowired
  private SubmissionResultMap submissionResultMap;
  @Autowired
  private QuestionsMap questionsMap;


  @Bean
  RedisAtomicLong createAtomicLong(RedisConnectionFactory connectionFactory) {
    return new RedisAtomicLong(GLOBAL_KEY_PREIFIX + "max_submission_id", connectionFactory);
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

  @Bean
  RedisConnectionFactory redisConnectionFactory() {
    JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory();
    redisConnectionFactory.setHostName(redisHost);
    redisConnectionFactory.setUsePool(true);
    return redisConnectionFactory;
  }

  // pub/sub
  @Bean
  StringRedisTemplate createStringRedisTemplate(RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }

  @Bean RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
      MessageListenerAdapter listenerAdapter) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(listenerAdapter,
        new PatternTopic(GLOBAL_KEY_PREIFIX + TASK_QUEUE_KEY));

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

  public void addTask(final Submission submission) {
    try {
      final String jsonStr = ObjectMapperInstance.INSTANCE.writeValueAsString(submission);
      redisTemplate.convertAndSend(GLOBAL_KEY_PREIFIX + TASK_QUEUE_KEY, jsonStr);
      setSubmissionResult(submission.getId(), new JudgeResult(StatusCode.PENDING));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
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
    redisTemplate.delete(GLOBAL_KEY_PREIFIX + TASK_QUEUE_KEY);
    redisTemplate.delete(GLOBAL_KEY_PREIFIX + QUESTION_MAP_KEY);
    redisTemplate.delete(GLOBAL_KEY_PREIFIX + SUBMISSION_REDULT_MAP_KEY);
  }
}
