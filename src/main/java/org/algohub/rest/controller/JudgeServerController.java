package org.algohub.rest.controller;

import org.algohub.engine.type.LanguageType;
import org.algohub.rest.pojo.Answer;
import org.algohub.rest.pojo.Submission;
import org.algohub.rest.pojo.SubmissionId;
import org.algohub.rest.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(method = RequestMethod.POST, value = "/judge")
public class JudgeServerController {

  @Autowired
  private SubmissionService submissionService;


  @RequestMapping(value = "/java/{id}")
  public SubmissionId judgeJava(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = submissionService.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.JAVA, userCode);
    submissionService.pushTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(value = "/cpp/{id}")
  public SubmissionId judgeCpp(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = submissionService.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.CPLUSPLUS, userCode);
    submissionService.pushTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(value = "/python/{id}")
  public SubmissionId judgePython(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = submissionService.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.PYTHON, userCode);
    submissionService.pushTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(value = "/ruby/{id}")
  public SubmissionId judgeRuby(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = submissionService.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.RUBY, userCode);
    submissionService.pushTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping
  public SubmissionId judge(@RequestBody final Answer answer) {
    final long submissionId = submissionService.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, answer.getId(),
        answer.getLanguage(), answer.getCode());
    submissionService.pushTask(submission);
    return new SubmissionId(submissionId);
  }
}
