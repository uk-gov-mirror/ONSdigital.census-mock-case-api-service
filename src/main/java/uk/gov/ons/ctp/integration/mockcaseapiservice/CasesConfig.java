package uk.gov.ons.ctp.integration.mockcaseapiservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.QuestionnaireIdDTO;
import uk.gov.ons.ctp.integration.mockcaseapiservice.client.model.CaseContainerDTO;

import java.io.IOException;
import java.util.*;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("examples")
public class CasesConfig {

    private String cases;
    private String questionnaires;
    private Map<String, CaseContainerDTO> caseUUIDMap = new HashMap<>();
    private Map<String, CaseContainerDTO> caseRefMap = new HashMap<>();
    private Map<String, List<CaseContainerDTO>> caseUprnMap = new HashMap<>();
    private Map<String, QuestionnaireIdDTO> questionnaireMap = new HashMap<>();

    public String getCases() {
        return cases;
    }

    public void setCases(final String cases) throws IOException {
        this.cases = cases;
        final ObjectMapper objectMapper = new ObjectMapper();
        final List<CaseContainerDTO> caseList  = objectMapper.readValue(cases, new TypeReference<List<CaseContainerDTO>>(){});
        caseList.forEach( c-> {
           caseUUIDMap.put(c.getId().toString(), c);
           caseRefMap.put(c.getCaseRef(), c);
           if (!caseUprnMap.containsKey(c.getUprn())) {
               caseUprnMap.put(c.getUprn(), new ArrayList<>());
           }
           caseUprnMap.get(c.getUprn()).add(c);
        });
    }

    public String getQuestionnaires() {
        return questionnaires;
    }

    public void setQuestionnaires(String questionnaires) throws IOException {
        this.questionnaires = questionnaires;
        final ObjectMapper objectMapper = new ObjectMapper();
        final List<QuestionnaireIdDTO> questionnaireIdDTOList  = objectMapper.readValue(questionnaires, new TypeReference<List<QuestionnaireIdDTO>>(){});
        questionnaireIdDTOList.forEach( q-> {
            questionnaireMap.put(q.getQuestionnaireId(), q);
        });
    }

    public CaseContainerDTO getCaseByUUID(final String key) {
        return caseUUIDMap.getOrDefault(key, null);
    }

    public CaseContainerDTO getCaseByRef(final String key) {
        return caseRefMap.getOrDefault(key, null);
    }

    public List<CaseContainerDTO> getCaseByUprn(final String key) {
        return caseUprnMap.getOrDefault(key, null);
    }

    public QuestionnaireIdDTO getQuestionnaire(final String key) {
        return questionnaireMap.getOrDefault(key, null);
    }

    @Override
    public String toString() {
        return "{" + getCases() + "}";
    }
}
