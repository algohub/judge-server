package org.algohub.rest;


import org.algohub.engine.JudgeEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Beans {

  @Bean
  @Qualifier("judgeEngine")
  public JudgeEngine createJudgeEngine() {
    return new JudgeEngine();
  }
}
