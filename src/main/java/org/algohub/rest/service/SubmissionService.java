package org.algohub.rest.service;

import org.algohub.engine.pojo.JudgeResult;
import org.algohub.engine.pojo.Question;
import org.algohub.rest.pojo.Submission;


public interface SubmissionService {
  void pushTask(final Submission submission);
  String popTask();

  void setSubmissionResult(long submissionId, final JudgeResult judgeResult);
  long incrementAndGetSubmissionId();
  JudgeResult getSubmissionResult(long id);
  void clear();
}
