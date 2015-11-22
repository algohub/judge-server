package org.algohub.rest.service;

import org.algohub.engine.pojo.JudgeResult;
import org.algohub.engine.pojo.Question;
import org.algohub.rest.pojo.Submission;


public interface RedisService {
  void addQuestion(final String id, final String json); // Only for testing
  Question getQuestion(String id);
  void pushTask(final Submission submission);
  String popTask();

  void setSubmissionResult(long submissionId, final JudgeResult judgeResult);
  long incrementAndGetSubmissionId();
  JudgeResult getSubmissionResult(long id);
  void clear();
}
