package org.algohub.rest.controller;

import org.algohub.engine.JudgeEngine;
import org.algohub.engine.pojo.JudgeResult;
import org.algohub.engine.type.LanguageType;
import org.algohub.rest.pojo.Answer;
import org.algohub.rest.pojo.Submission;
import org.algohub.rest.pojo.SubmissionId;
import org.algohub.rest.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class JudgeServerController {

  @Autowired
  @Qualifier("judgeEngine")
  private JudgeEngine judgeEngine;

  @Autowired
  private RedisService redisService;


  @RequestMapping(method = RequestMethod.POST, value = "/question/judge/java/{id}")
  public SubmissionId judgeJava(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = redisService.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.JAVA, userCode);
    redisService.pushTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/question/judge/cpp/{id}")
  public SubmissionId judgeCpp(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = redisService.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.CPLUSPLUS, userCode);
    redisService.pushTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/question/judge/python/{id}")
  public SubmissionId judgePython(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = redisService.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.PYTHON, userCode);
    redisService.pushTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/question/judge/ruby/{id}")
  public SubmissionId judgeRuby(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = redisService.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.RUBY, userCode);
    redisService.pushTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/question/judge")
  public SubmissionId judge(@RequestBody final Answer answer) {
    final long submissionId = redisService.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, answer.getId(),
        answer.getLanguage(), answer.getCode());
    redisService.pushTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/submission/check/{id}")
  public JudgeResult check(@PathVariable("id") long id) {
    return redisService.getSubmissionResult(id);
  }

  @Bean
  @Qualifier("judgeEngine")
  public JudgeEngine createJudgeEngine() {
    return new JudgeEngine();
  }
}
