package org.algohub.rest.service;


import org.algohub.engine.pojo.Question;

public interface QuestionService {
  void addQuestion(final String id, final String json);

  Question getQuestionById(String id);
}
