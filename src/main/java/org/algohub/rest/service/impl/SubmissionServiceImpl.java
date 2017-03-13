package org.algohub.rest.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.algohub.engine.JudgeEngine;
import org.algohub.engine.judge.StatusCode;
import org.algohub.engine.pojo.JudgeResult;
import org.algohub.engine.util.ObjectMapperInstance;
import org.algohub.rest.pojo.Submission;
import org.algohub.rest.pojo.TaskQueueList;
import org.algohub.rest.service.ProblemService;
import org.algohub.rest.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SubmissionServiceImpl implements SubmissionService {
  public static final String GLOBAL_KEY_PREIFIX = "algohub_judge:";
  private static final String MAX_SUBMISSION_ID_KEY = "max_submission_id";
  private static final String TASK_QUEUE = "task_queue";
  private static final String SUBMISSION_REDULT_KEY = "submission_result:";

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Autowired
  private RedisAtomicLong maxSubmissionId;

  @Autowired
  private TaskQueueList taskQueue;

  @Bean
  RedisAtomicLong createAtomicLong(RedisConnectionFactory connectionFactory) {
    return new RedisAtomicLong(GLOBAL_KEY_PREIFIX + MAX_SUBMISSION_ID_KEY, connectionFactory);
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
        new PatternTopic(GLOBAL_KEY_PREIFIX + TASK_QUEUE));

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

  public void pushTask(final Submission submission) {
    try {
      final String jsonStr = ObjectMapperInstance.INSTANCE.writeValueAsString(submission);
      taskQueue.getList().offerLast(jsonStr);
      redisTemplate.convertAndSend(GLOBAL_KEY_PREIFIX + TASK_QUEUE,
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
      final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
      valueOps.set(GLOBAL_KEY_PREIFIX + SUBMISSION_REDULT_KEY + String.valueOf(submissionId), jsonStr);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  public JudgeResult getSubmissionResult(long id) {
    final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
    final String jsonStr = valueOps.get(GLOBAL_KEY_PREIFIX + SUBMISSION_REDULT_KEY + String.valueOf(id));
    try {
      return ObjectMapperInstance.INSTANCE.readValue(jsonStr, JudgeResult.class);
    } catch (IOException e) {
      return null;
    }
  }

  public void clear() {
    redisTemplate.delete(GLOBAL_KEY_PREIFIX + MAX_SUBMISSION_ID_KEY);
    redisTemplate.delete(GLOBAL_KEY_PREIFIX + TASK_QUEUE);
  }

  static class Receiver {
    private final JudgeEngine judgeEngine = new JudgeEngine();

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private ProblemService problemService;

    public void receiveMessage(String message) {
      try {
        final String jsonStr = submissionService.popTask();
        if (jsonStr != null) {
          final Submission submission = ObjectMapperInstance.INSTANCE.readValue(jsonStr,
              Submission.class);
          submissionService.setSubmissionResult(submission.getId(), new JudgeResult(StatusCode.RUNNING));
          final JudgeResult judgeResult = judgeEngine.judge(problemService.getProblemById(
              submission.getProblemId()), submission.getCode(), submission.getLanguage());
          submissionService.setSubmissionResult(submission.getId(), judgeResult);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
