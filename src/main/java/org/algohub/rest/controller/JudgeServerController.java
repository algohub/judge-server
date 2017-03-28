package org.algohub.rest.controller;

import org.algohub.engine.JudgeEngine;
import org.algohub.engine.pojo.JudgeResult;
import org.algohub.engine.pojo.Problem;
import org.algohub.rest.bo.DirectSubmission;
import org.algohub.rest.pojo.Submission;
import org.algohub.rest.pojo.SubmissionId;
import org.algohub.rest.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class JudgeServerController {
  private final JudgeEngine judgeEngine = new JudgeEngine();
  @Autowired
  private SubmissionService submissionService;

  @RequestMapping(method = RequestMethod.POST, value = "/problems/{id}/judge")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public SubmissionId judge(@PathVariable("id") String problemId, @RequestBody final Submission submission) {
    final long submissionId = submissionService.incrementAndGetSubmissionId();
    submission.setId(submissionId);
    submission.setProblemId(problemId);
    submissionService.pushTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/validate")
  public JudgeResult judge(@RequestBody final DirectSubmission submission) {
    return judgeEngine.judge(submission.getProblem(), submission.getCode(),
        submission.getLang());
  }
}
