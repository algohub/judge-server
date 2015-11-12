package org.algohub.rest.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by programmer on 10/27/15.
 */
public class SubmissionId {
  @JsonProperty("submission_id")
  private long submissionId;

  @JsonCreator public SubmissionId(@JsonProperty("submission_id") long submissionId) {
    this.submissionId = submissionId;
  }

  public long getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(long submissionId) {
    this.submissionId = submissionId;
  }
}
