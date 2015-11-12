package org.algohub.rest.controller;

import org.algohub.engine.JudgeEngine;
import org.algohub.engine.pojo.JudgeResult;
import org.algohub.engine.type.LanguageType;
import org.algohub.rest.RedisDao;
import org.algohub.rest.pojo.Answer;
import org.algohub.rest.pojo.Submission;
import org.algohub.rest.pojo.SubmissionId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
  private RedisDao redisDao;


  @RequestMapping(method = RequestMethod.POST, value = "/question/judge/java/{id}")
  public SubmissionId judgeJava(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = redisDao.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.JAVA, userCode);
    redisDao.addTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/question/judge/cpp/{id}")
  public SubmissionId judgeCpp(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = redisDao.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.CPLUSPLUS, userCode);
    redisDao.addTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/question/judge/python/{id}")
  public SubmissionId judgePython(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = redisDao.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.PYTHON, userCode);
    redisDao.addTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/question/judge/ruby/{id}")
  public SubmissionId judgeRuby(@PathVariable("id") String id, @RequestBody String userCode) {
    final long submissionId = redisDao.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, id, LanguageType.RUBY, userCode);
    redisDao.addTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/question/judge")
  public SubmissionId judge(@RequestBody final Answer answer) {
    final long submissionId = redisDao.incrementAndGetSubmissionId();
    final Submission submission = new Submission(submissionId, answer.getId(),
        answer.getLanguage(), answer.getCode());
    redisDao.addTask(submission);
    return new SubmissionId(submissionId);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/submission/check/{id}")
  public JudgeResult check(@PathVariable("id") long id) {
    return redisDao.getSubmissionResult(id);
  }
}
