package uk.gov.ons.ctp.integration.fakecaseservice.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionnaireIdDTO {

  private String questionnaireId;
}
