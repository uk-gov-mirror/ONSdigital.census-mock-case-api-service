package uk.gov.ons.ctp.integration.mockcaseapiservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.CaseContainerDTO;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.EventDTO;

@Configuration
@EnableConfigurationProperties
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:cases.yml")
@ConfigurationProperties("casedata")
public class CasesConfig {

  private String cases;
  private Map<String, CaseContainerDTO> caseUUIDMap = new HashMap<>();
  private Map<String, CaseContainerDTO> caseRefMap = new HashMap<>();
  private Map<String, List<CaseContainerDTO>> caseUprnMap = new HashMap<>();
  private Map<String, List<EventDTO>> eventMap = new HashMap<>();

  public String getCases() {
    return cases;
  }

  public void setCases(final String cases) throws IOException {
    this.cases = cases;
    final ObjectMapper objectMapper = new ObjectMapper();
    final List<CaseContainerDTO> caseList =
        objectMapper.readValue(cases, new TypeReference<List<CaseContainerDTO>>() {});
    caseList.forEach(
        c -> {
          caseUUIDMap.put(c.getId().toString(), c);
          caseRefMap.put(c.getCaseRef(), c);
          if (!caseUprnMap.containsKey(c.getUprn())) {
            caseUprnMap.put(c.getUprn(), new ArrayList<>());
          }
          caseUprnMap.get(c.getUprn()).add(c);
          if (!c.getCaseEvents().isEmpty()) {
            if (!eventMap.containsKey(c.getId().toString())) {
                eventMap.put(c.getId().toString(), new ArrayList<>());
            }
            c.getCaseEvents().forEach( ev -> {
                eventMap.get(c.getId().toString()).add(ev);
            });
            c.getCaseEvents().clear();
          }
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

  public List<EventDTO> getEventsByCaseID(final String key) {
      return eventMap.getOrDefault(key, new ArrayList<>());
  }

  @Override
  public String toString() {
    return "{" + getCases() + "}";
  }
}
