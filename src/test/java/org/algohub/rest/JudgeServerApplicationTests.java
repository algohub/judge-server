package org.algohub.rest;

import com.google.common.collect.ImmutableMap;

import java.nio.file.Paths;
import org.algohub.engine.JudgeEngine;
import org.algohub.engine.judge.StatusCode;
import org.algohub.engine.pojo.Code;
import org.algohub.engine.pojo.JudgeResult;
import org.algohub.engine.pojo.Problem;
import org.algohub.engine.type.LanguageType;
import org.algohub.engine.util.ObjectMapperInstance;
import org.algohub.rest.pojo.Submission;
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

  @Test public void javaJudgeTest() {
    judgeMulti(LanguageType.JAVA, this::judgeOne);
  }

  @Test public void cppJudgeTest() {
    judgeMulti(LanguageType.CPLUSPLUS, this::judgeOne);
  }

  @Test public void pythonJudgeTest() {
    judgeMulti(LanguageType.PYTHON, this::judgeOne);
  }

  @Test public void rubyJudgeTest() {
    judgeMulti(LanguageType.RUBY, this::judgeOne);
  }

  @Test public void judgeDirectly() {
    judgeDirectly(LanguageType.JAVA);
    judgeDirectly(LanguageType.CPLUSPLUS);
    judgeDirectly(LanguageType.PYTHON);
    judgeDirectly(LanguageType.RUBY);
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

  private static void judgeMulti(final LanguageType languageType, JudgeOneFunction judgeOne) {
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

  private void judgeOne(final String problemId, final String userCode,
      LanguageType languageType) {
    try {
      String response = mockMvc.perform(
          post("/problems/" + problemId + "/judge").content(this.json(new Submission(languageType, userCode)))
              .contentType(contentType)).andReturn().getResponse().getContentAsString();
      final SubmissionId submissionId = ObjectMapperInstance.INSTANCE.readValue(response,
          SubmissionId.class);

      JudgeResult judgeResult = new JudgeResult(StatusCode.PENDING);
      for (int i = 0; i < 10; ++i) {
        Thread.sleep(1000);
        response = mockMvc.perform(get("/submissions/" + submissionId.getSubmissionId())).andReturn().getResponse()
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

  private void judgeDirectly(final LanguageType languageType) {
    final File solutionsDir = new File("src/test/resources/solutions/");
    final File problemDir = new File("src/test/resources/problems/");
    final Pattern pattern = Pattern.compile("\\w+\\" + LANGUAGE_TO_EXTENSION.get(languageType));

    try {
      for (final File solutionDir : solutionsDir.listFiles()) {
        final String problemJson = new String(java.nio.file.Files.readAllBytes(
            Paths.get(problemDir.getAbsolutePath(), solutionDir.getName() + ".json")),
            StandardCharsets.UTF_8);
        for (final File solutionFile : solutionDir.listFiles()) {
          final Matcher matcher = pattern.matcher(solutionFile.getName());
          if (!matcher.matches()) continue;

          final String userCode =
              new String(java.nio.file.Files.readAllBytes(solutionFile.toPath()), StandardCharsets.UTF_8);
          final String jsonCode = ObjectMapperInstance.INSTANCE.writeValueAsString(new Code(languageType, userCode));
          final int lastCloseBrace = problemJson.lastIndexOf('}');
          final String problemJsonNew = problemJson.substring(0, lastCloseBrace) + "," +
              "\"solution\":" + jsonCode + "}";

          try {
            String response = mockMvc.perform(post("/judge").content(problemJsonNew).
                contentType(contentType)).andReturn().getResponse().getContentAsString();
            final JudgeResult judgeResult = ObjectMapperInstance.INSTANCE.readValue(response,
                JudgeResult.class);
            if (judgeResult.getStatusCode() != StatusCode.ACCEPTED.toInt()) {
              System.err.println(judgeResult.getErrorMessage());
            }
            assertEquals(StatusCode.ACCEPTED.toInt(), judgeResult.getStatusCode());
          } catch (Exception ex) {
            fail(ex.getMessage());
          }
        }
      }
    } catch (IOException ex) {
      fail(ex.getMessage());
    }
  }

  private void loadProblems() throws IOException {
    final File folder = new File("src/test/resources/problems");

    for (final File fileEntry : folder.listFiles()) {
      if (fileEntry.isFile() && fileEntry.getName().endsWith(".json")) {
        final String jsonStr = new String(Files.readAllBytes(fileEntry.toPath()),
            StandardCharsets.UTF_8);
        try {
          mockMvc.perform(post("/problems").content(jsonStr).contentType(contentType))
                  .andReturn().getResponse().getContentAsString();
        } catch (Exception ex) {
          fail(ex.getMessage());
        }
      }
    }
  }
}
