package org.algohub.rest;

import org.algohub.engine.JudgeEngine;
import org.algohub.engine.judge.StatusCode;
import org.algohub.engine.pojo.JudgeResult;
import org.algohub.engine.util.ObjectMapperInstance;
import org.algohub.rest.pojo.Submission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;


public class Receiver {

  @Autowired
  @Qualifier("judgeEngine")
  private JudgeEngine judgeEngine;

  @Autowired
  private RedisDao redisDao;

  public void receiveMessage(String message) {
    try {
      final Submission submission = ObjectMapperInstance.INSTANCE.readValue(message,
          Submission.class);
      redisDao.setSubmissionResult(submission.getId(), new JudgeResult(StatusCode.RUNNING));
      final JudgeResult judgeResult = judgeEngine.judge(redisDao.getQuestion(
              submission.getQuestionId()), submission.getCode(), submission.getLanguage());
      redisDao.setSubmissionResult(submission.getId(), judgeResult);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
