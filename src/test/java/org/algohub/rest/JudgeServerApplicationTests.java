package org.algohub.rest;

import com.google.common.collect.ImmutableMap;

import org.algohub.engine.JudgeEngine;
import org.algohub.engine.judge.StatusCode;
import org.algohub.engine.pojo.JudgeResult;
import org.algohub.engine.pojo.Problem;
import org.algohub.engine.type.LanguageType;
import org.algohub.engine.util.ObjectMapperInstance;
import org.algohub.rest.pojo.Answer;
import org.algohub.rest.pojo.SubmissionId;
import org.algohub.rest.service.ProblemService;
import org.algohub.rest.service.SubmissionService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JudgeServerApplication.class)
@WebAppConfiguration
public class JudgeServerApplicationTests {

  private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(),
      Charset.forName("utf8"));

  private final MediaType contentTypeText = new MediaType(MediaType.TEXT_PLAIN.getType(),
      MediaType.TEXT_PLAIN.getSubtype(),
      Charset.forName("utf8"));

  private MockMvc mockMvc;

  private HttpMessageConverter mappingJackson2HttpMessageConverter;

  @Autowired
  private ProblemService problemService;
  @Autowired
  private SubmissionService submissionService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private static final ImmutableMap<LanguageType, String> LANGUAGE_TO_EXTENSION =
      ImmutableMap.<LanguageType, String>builder().put(LanguageType.JAVA, ".java")
          .put(LanguageType.JAVASCRIPT, ".js").put(LanguageType.CPLUSPLUS, ".cpp")
          .put(LanguageType.PYTHON, ".py").put(LanguageType.RUBY, ".rb").build();

  private static final JudgeEngine JUDGE_ENGINE = new JudgeEngine();

  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {

    this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
        hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

    Assert.assertNotNull("the JSON message converter must not be null",
        this.mappingJackson2HttpMessageConverter);
  }

  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();
    loadProblems();
  }

  @After
  public void clear() {
    submissionService.clear();
  }

  @Test public void judgeTest() {
    batchJudge(LanguageType.JAVA, this::judgeOne);
    batchJudge(LanguageType.CPLUSPLUS, this::judgeOne);
    batchJudge(LanguageType.PYTHON, this::judgeOne);
    batchJudge(LanguageType.RUBY, this::judgeOne);
  }

  @Test public void javaJudgeTest() {
    batchJudge(LanguageType.JAVA, this::judgeOneLanguage);
  }

  @Test public void cppJudgeTest() {
    batchJudge(LanguageType.CPLUSPLUS, this::judgeOneLanguage);
  }

  @Test public void pythonJudgeTest() {
    batchJudge(LanguageType.PYTHON, this::judgeOneLanguage);
  }

  @Test public void rubyJudgeTest() {
    batchJudge(LanguageType.RUBY, this::judgeOneLanguage);
  }

  private String json(Object o) throws IOException {
    MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
    this.mappingJackson2HttpMessageConverter.write(
        o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
    return mockHttpOutputMessage.getBodyAsString();
  }

  @FunctionalInterface
  private interface JudgeOneFunction {
    void apply(String id, String userCode, LanguageType languageType);
  }

  private static void batchJudge(final LanguageType languageType, JudgeOneFunction judgeOne) {
    final File rootDir = new File("src/test/resources/solutions/");
    final Pattern pattern = Pattern.compile("\\w+\\" + LANGUAGE_TO_EXTENSION.get(languageType));

    try {
      for (final File solutionDir : rootDir.listFiles()) {
        for (final File solutionFile : solutionDir.listFiles()) {
          final Matcher matcher = pattern.matcher(solutionFile.getName());
          if (!matcher.matches()) continue;

          final String userCode =
              new String(java.nio.file.Files.readAllBytes(solutionFile.toPath()), StandardCharsets.UTF_8);

          judgeOne.apply(solutionDir.getName(), userCode, languageType);
        }
      }
    } catch (IOException ex) {
      fail(ex.getMessage());
    }
  }

  private void judgeOne(final String id, final String userCode,
      LanguageType languageType) {
    try {
      String response = mockMvc.perform(
          post("/judge").content(this.json(new Answer(id, languageType, userCode)))
              .contentType(contentType)).andReturn().getResponse().getContentAsString();
      final SubmissionId submissionId = ObjectMapperInstance.INSTANCE.readValue(response,
          SubmissionId.class);

      JudgeResult judgeResult = new JudgeResult(StatusCode.PENDING);
      for (int i = 0; i < 10; ++i) {
        Thread.sleep(1000);
        response = mockMvc.perform(get("/submission/check/" + submissionId.getSubmissionId())).andReturn().getResponse()
            .getContentAsString();
        judgeResult = ObjectMapperInstance.INSTANCE.readValue(response, JudgeResult.class);
        if (judgeResult.getStatusCode() == StatusCode.ACCEPTED.toInt()) break;
      }
      if (judgeResult.getStatusCode() != StatusCode.ACCEPTED.toInt()) {
        System.err.println(judgeResult.getErrorMessage());
      }
      assertEquals(StatusCode.ACCEPTED.toInt(), judgeResult.getStatusCode());
    } catch (Exception ex) {
      fail(ex.getMessage());
    }
  }

  private void judgeOneLanguage(final String id, final String userCode,
      LanguageType languageType) {
    try {
      String response = mockMvc.perform(
          post("/judge/" + languageType.toValue() + '/' + id)
              .content(userCode)
              .contentType(contentTypeText)).andReturn().getResponse().getContentAsString();
      final SubmissionId submissionId = ObjectMapperInstance.INSTANCE.readValue(response,
          SubmissionId.class);

      JudgeResult judgeResult = new JudgeResult(StatusCode.PENDING);
      for (int i = 0; i < 10; ++i) {
        Thread.sleep(1000);
        response = mockMvc.perform(get("/submission/check/" + submissionId.getSubmissionId())).andReturn().getResponse()
            .getContentAsString();
        judgeResult = ObjectMapperInstance.INSTANCE.readValue(response, JudgeResult.class);
        if (judgeResult.getStatusCode() == StatusCode.ACCEPTED.toInt()) break;
      }
      if (judgeResult.getStatusCode() != StatusCode.ACCEPTED.toInt()) {
        System.err.println(judgeResult.getErrorMessage());
      }
      assertEquals(StatusCode.ACCEPTED.toInt(), judgeResult.getStatusCode());
    } catch (Exception ex) {
      fail(ex.getMessage());
    }
  }

  private void loadProblems() throws IOException {
    final File folder = new File("src/test/resources/problems");

    for (final File fileEntry : folder.listFiles()) {
      if (fileEntry.isFile() && fileEntry.getName().endsWith(".json")) {
        final String jsonStr = new String(Files.readAllBytes(fileEntry.toPath()),
            StandardCharsets.UTF_8);
        final Problem problem = ObjectMapperInstance.INSTANCE.readValue(jsonStr, Problem.class);
        problemService.addProblem(problem.getId(), jsonStr);
      }
    }
  }
}
