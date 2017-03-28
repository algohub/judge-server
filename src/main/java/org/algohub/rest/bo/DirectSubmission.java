package org.algohub.rest.bo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.algohub.engine.pojo.Problem;
import org.algohub.engine.type.LanguageType;

public class DirectSubmission {
  private Problem problem;
  private LanguageType lang;
  private String code;

  @JsonCreator DirectSubmission(@JsonProperty(value = "problem", required = true) Problem problem,
      @JsonProperty(value = "lang", required = true) LanguageType lang,
      @JsonProperty(value = "code", required = true) String code) {
    this.problem = problem;
    this.lang = lang;
    this.code = code;
  }

  public Problem getProblem() {
    return problem;
  }

  public LanguageType getLang() {
    return lang;
  }

  public String getCode() {
    return code;
  }
}
