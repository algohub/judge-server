package org.algohub.rest.controller;

import org.algohub.engine.pojo.JudgeResult;
import org.algohub.rest.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(method = RequestMethod.GET, value = "/submissions")
public class SubmissionController {

  @Autowired
  private SubmissionService submissionService;

  @RequestMapping(value = "/{id}")
  public JudgeResult check(@PathVariable("id") long id) {
    return submissionService.getSubmissionResult(id);
  }
}
