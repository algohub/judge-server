package org.algohub.rest.controller;

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
@RequestMapping(method = RequestMethod.POST, value = "/problems")
public class JudgeServerController {

  @Autowired
  private SubmissionService submissionService;

  @RequestMapping(value = "/{id}/judge")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public SubmissionId judge(@PathVariable("id") String problemId, @RequestBody final Submission submission) {
    final long submissionId = submissionService.incrementAndGetSubmissionId();
    submission.setId(submissionId);
    submission.setProblemId(problemId);
    submissionService.pushTask(submission);
    return new SubmissionId(submissionId);
  }
}
