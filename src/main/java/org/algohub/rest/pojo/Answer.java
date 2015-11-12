package org.algohub.rest.pojo;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.algohub.engine.type.LanguageType;


public class Answer {
  private String id;
  private LanguageType language;
  private String code;

  @JsonCreator public Answer(@JsonProperty("id") String id,
      @JsonProperty("language") LanguageType language,
      @JsonProperty("code") String code) {
    this.id = id;
    this.language = language;
    this.code = code;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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
