package uk.gov.ons.ctp.integration.mockcaseapiservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.QuestionnaireIdDTO;

@Configuration
@EnableConfigurationProperties
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:questionnaires.yml")
@ConfigurationProperties("questionnairedata")
public class QuestionnairesConfig {

  private String questionnaires;
  private Map<String, QuestionnaireIdDTO> questionnaireMap = new HashMap<>();

  public String getQuestionnaires() {
    return questionnaires;
  }

  public void setQuestionnaires(String questionnaires) throws IOException {
    this.questionnaires = questionnaires;
    final ObjectMapper objectMapper = new ObjectMapper();
    final List<QuestionnaireIdDTO> questionnaireIdDTOList =
        objectMapper.readValue(questionnaires, new TypeReference<List<QuestionnaireIdDTO>>() {});
    questionnaireIdDTOList.forEach(q -> questionnaireMap.put(q.getQuestionnaireId(), q));
  }

  public QuestionnaireIdDTO getQuestionnaire(final String key) {
    return questionnaireMap.getOrDefault(key, null);
  }

  @Override
  public String toString() {
    return "{" + getQuestionnaires() + "}";
  }
}
