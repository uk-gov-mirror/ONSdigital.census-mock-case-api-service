package uk.gov.ons.ctp.integration.mockcaseapiservice.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.CaseContainerDTO;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.EventDTO;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.QuestionnaireIdDTO;
import uk.gov.ons.ctp.integration.contactcentresvc.representation.CaseRequestDTO;
import uk.gov.ons.ctp.integration.contactcentresvc.representation.model.UniquePropertyReferenceNumber;
import uk.gov.ons.ctp.integration.mockcaseapiservice.CasesConfig;
import uk.gov.ons.ctp.integration.mockcaseapiservice.QuestionnairesConfig;
import uk.gov.ons.ctp.integration.mockcaseapiservice.utility.FailureSimulator;

/** Provides mock endpoints for the case service. */
@RestController
@RequestMapping(value = "/cases", produces = "application/json")
public final class CaseServiceMockStub implements CTPEndpoint {
  private static final Logger log = LoggerFactory.getLogger(CaseServiceMockStub.class);

  @Autowired private CasesConfig casesConfig; // can allow field injection here in a mock service.
  @Autowired private QuestionnairesConfig questionnairesConfig;

  @RequestMapping(value = "/info", method = RequestMethod.GET)
  public ResponseEntity<String> info() {
    return ResponseEntity.ok("CENSUS MOCK CASE SERVICE");
  }

  @RequestMapping(value = "/examples", method = RequestMethod.GET)
  public ResponseEntity<String> examples() {
    return ResponseEntity.ok(
        "CASES-- "
            + casesConfig.getCases()
            + " -- QUESTIONNAIRES-- "
            + questionnairesConfig.getQuestionnaires());
  }

  /**
   * the GET endpoint to find a Case by UUID
   *
   * @param caseId to find by
   * @param includeCaseEvents flag used to return or not CaseEvents
   * @return the case found
   */
  @RequestMapping(value = "/{caseId}", method = RequestMethod.GET)
  public ResponseEntity<CaseContainerDTO> findCaseById(
      @PathVariable("caseId") final UUID caseId,
      @RequestParam(value = "caseEvents", required = false) boolean includeCaseEvents)
      throws CTPException {
    log.with("case_id", caseId).debug("Entering findCaseById");

    FailureSimulator.optionallyTriggerFailure(caseId.toString(), 400, 401, 404, 500);
    CaseContainerDTO caseDetails = casesConfig.getCaseByUUID(caseId.toString());
    nullTestThrowsException(caseDetails);
    caseDetails.setCaseEvents(getCaseEvents(includeCaseEvents, caseDetails.getCreatedDateTime()));
    return ResponseEntity.ok(caseDetails);
  }

  /**
   * the GET endpoint to find a Questionnaire Id by Case ID
   *
   * @param caseId to find by
   * @return the questionnaire id found
   * @throws CTPException something went wrong
   */
  @RequestMapping(value = "/ccs/{caseId}/qid", method = RequestMethod.GET)
  public ResponseEntity<QuestionnaireIdDTO> findQuestionnaireIdByCaseId(
      @PathVariable("caseId") final UUID caseId) throws CTPException {
    log.with("case_id", caseId).debug("Entering findQuestionnaireIdByCaseId");

    FailureSimulator.optionallyTriggerFailure(caseId.toString(), 400, 401, 404, 500);
    QuestionnaireIdDTO questionnaireId = questionnairesConfig.getQuestionnaire(caseId.toString());
    nullTestThrowsException(questionnaireId);
    return ResponseEntity.ok(questionnaireId);
  }

  @RequestMapping(value = "/uprn/{uprn}", method = RequestMethod.GET)
  public ResponseEntity<List<CaseContainerDTO>> findCaseByUPRN(
      @PathVariable(value = "uprn") final UniquePropertyReferenceNumber uprn) throws CTPException {
    log.with("uprn", uprn).debug("Entering findCaseByUPRN");

    FailureSimulator.optionallyTriggerFailure(Long.toString(uprn.getValue()), 400, 401, 404, 500);
    List<CaseContainerDTO> cases = casesConfig.getCaseByUprn(Long.toString(uprn.getValue()));
    nullTestThrowsException(cases);
    return ResponseEntity.ok(cases);
  }

  @RequestMapping(value = "/ref/{ref}", method = RequestMethod.GET)
  public ResponseEntity<CaseContainerDTO> findCaseByCaseReference(
      @PathVariable(value = "ref") final long ref, @Valid CaseRequestDTO requestParamsDTO)
      throws CTPException {
    log.with("ref", ref)
        .with("caseEvents", requestParamsDTO.getCaseEvents())
        .info("Entering GET getCaseByCaseReference");

    FailureSimulator.optionallyTriggerFailure(Long.toString(ref), 400, 401, 404, 500);

    CaseContainerDTO caseDetails = casesConfig.getCaseByRef(Long.toString(ref));
    nullTestThrowsException(caseDetails);
    caseDetails.setCaseEvents(
        getCaseEvents(requestParamsDTO.getCaseEvents(), caseDetails.getCreatedDateTime()));
    return ResponseEntity.ok(caseDetails);
  }

  private void nullTestThrowsException(Object response) throws CTPException {
    if (response == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND);
    }
  }

  private List<EventDTO> getCaseEvents(final boolean includeCaseEvents, final Date createdDate) {
    final List<EventDTO> caseEvents = new ArrayList<>();
    if (!includeCaseEvents) {
      return caseEvents;
    }

    final EventDTO e1 = new EventDTO();
    e1.setId("101");
    e1.setDescription("Initial creation of case");
    e1.setEventType("CASE_CREATED");
    e1.setCreatedDateTime(createdDate);

    final EventDTO e2 = new EventDTO();
    e2.setId("102");
    e2.setDescription("Create Household Visit");
    e1.setEventType("CASE_UPDATED");
    e2.setCreatedDateTime(createdDate);

    caseEvents.add(e1);
    caseEvents.add(e2);
    return caseEvents;
  }
}
