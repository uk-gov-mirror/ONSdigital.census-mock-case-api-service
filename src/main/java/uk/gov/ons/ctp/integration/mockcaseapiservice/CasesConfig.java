package uk.gov.ons.ctp.integration.mockcaseapiservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.CTPException.Fault;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.CaseContainerDTO;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.EventDTO;

@Configuration
@EnableConfigurationProperties
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:cases.yml")
@ConfigurationProperties("casedata")
public class CasesConfig {

  private LuhnCheckDigit luhnChecker = new LuhnCheckDigit();
  private String cases;
  private final Map<String, CaseContainerDTO> caseUUIDMap =
      Collections.synchronizedMap(new HashMap<>());
  private final Map<String, CaseContainerDTO> caseRefMap =
      Collections.synchronizedMap(new HashMap<>());
  private final Map<String, List<CaseContainerDTO>> caseUprnMap =
      Collections.synchronizedMap(new HashMap<>());
  private final Map<String, List<EventDTO>> eventMap = Collections.synchronizedMap(new HashMap<>());

  public String getCases() {
    return cases;
  }

  /**
   * Sets cases from the JSON into a list of Case container DTOs
   *
   * @param cases - JSON String
   * @throws IOException - Thrown when Object Mapper errors
   */
  public void setCases(final String cases) throws IOException, CTPException {
    this.cases = cases;
    final ObjectMapper objectMapper = new ObjectMapper();
    final List<CaseContainerDTO> caseList =
        objectMapper.readValue(cases, new TypeReference<List<CaseContainerDTO>>() {});
    addOrReplaceData(caseList);
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

  /**
   * add or replace data in the case maps from a list of Cases
   *
   * @param caseList - list of cases
   */
  public void addOrReplaceData(final List<CaseContainerDTO> caseList) throws CTPException {
    for (CaseContainerDTO caseDetails : caseList) {
      if (!luhnChecker.isValid(caseDetails.getCaseRef())) {
        throw new CTPException(Fault.BAD_REQUEST, "Invalid Case Reference");
      }
      updateMaps(caseDetails);
    }
  }

  /**
   * Reset the data maps back to the original JSON
   *
   * @throws IOException - thrown
   */
  public synchronized void resetData() throws IOException, CTPException {
    synchronized (caseUUIDMap) {
      caseUUIDMap.clear();
    }
    synchronized (caseUprnMap) {
      caseUprnMap.clear();
    }
    synchronized (eventMap) {
      eventMap.clear();
    }
    synchronized (caseRefMap) {
      caseRefMap.clear();
      setCases(cases);
    }
  }

  /**
   * Update maps from a case
   *
   * @param caseDetails - a case
   */
  private synchronized void updateMaps(final CaseContainerDTO caseDetails) {

    synchronized (caseUUIDMap) {
      caseUUIDMap.put(caseDetails.getId().toString(), caseDetails);
    }
    synchronized (caseRefMap) {
      caseRefMap.put(caseDetails.getCaseRef(), caseDetails);
    }
    synchronized (caseUprnMap) {
      if (!caseUprnMap.containsKey(caseDetails.getUprn())) {
        caseUprnMap.put(caseDetails.getUprn(), new ArrayList<>());
      }

      List<CaseContainerDTO> oldCasesForUprn = caseUprnMap.get(caseDetails.getUprn());
      List<CaseContainerDTO> newCasesForUprn = new ArrayList<>();
      for (CaseContainerDTO caze : oldCasesForUprn) {
        if (!caze.getId().equals(caseDetails.getId())) {
          newCasesForUprn.add(caze);
        }
      }
      caseUprnMap.put(caseDetails.getUprn(), newCasesForUprn);
      caseUprnMap.get(caseDetails.getUprn()).add(caseDetails);
    }
    synchronized (eventMap) {
      if (!caseDetails.getCaseEvents().isEmpty()) {
        if (!eventMap.containsKey(caseDetails.getId().toString())) {
          eventMap.put(caseDetails.getId().toString(), new ArrayList<>());
        }
        caseDetails
            .getCaseEvents()
            .forEach(ev -> eventMap.get(caseDetails.getId().toString()).add(ev));
        caseDetails.getCaseEvents().clear();
      }
    }
  }

  @Override
  public String toString() {
    return "{" + getCases() + "}";
  }
}
