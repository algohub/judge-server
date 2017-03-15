package org.algohub.rest.service;

import org.algohub.engine.pojo.Problem;


public interface ProblemService {
  void addProblem(final String json);

  Problem getProblemById(String id);
}
