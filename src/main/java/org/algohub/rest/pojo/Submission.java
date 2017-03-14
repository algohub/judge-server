package org.algohub.rest.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.algohub.engine.type.LanguageType;


public class Submission {
  private long id;
  @JsonProperty("problem_id") private String problemId;
  private LanguageType language;
  private String code;

  @JsonCreator public Submission(@JsonProperty("id") long id,
      @JsonProperty("problem_id") String problemId,
      @JsonProperty("language") LanguageType language,
      @JsonProperty("code") String code) {
    this.id = id;
    this.problemId = problemId;
    this.language = language;
    this.code = code;
  }

  public Submission(LanguageType language, String code) {
    this.language = language;
    this.code = code;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getProblemId() {
    return problemId;
  }

  public void setProblemId(String problemId) {
    this.problemId = problemId;
  }

  public LanguageType getLanguage() {
    return language;
  }

  public void setLanguage(LanguageType language) {
    this.language = language;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
