package uk.gov.ons.ctp.integration.mockcaseapiservice.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.model.UniquePropertyReferenceNumber;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.CaseContainerDTO;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.EventDTO;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.NewQuestionnaireIdDTO;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.QuestionnaireIdDTO;
import uk.gov.ons.ctp.integration.contactcentresvc.representation.CaseRequestDTO;
import uk.gov.ons.ctp.integration.contactcentresvc.representation.ResponseDTO;
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
    caseDetails.setCaseEvents(getCaseEvents(caseDetails.getId().toString(), includeCaseEvents));
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

  /**
   * the GET endpoint to generate a new Questionnaire Id for a case.
   *
   * @param caseId to find by
   * @return the new questionnaire id
   * @throws CTPException something went wrong
   */
  @RequestMapping(value = "/{caseId}/qid", method = RequestMethod.GET)
  public ResponseEntity<NewQuestionnaireIdDTO> newQuestionnaireIdForCase(
      @PathVariable("caseId") final UUID caseId,
      @RequestParam(required = false) final boolean individual,
      @RequestParam(required = false) final UUID individualCaseId)
      throws CTPException {
    log.with("case_id", caseId)
        .with("individual", individual)
        .with("individualCaseId", individualCaseId)
        .debug("Entering newQuestionnaireIdForCase");

    FailureSimulator.optionallyTriggerFailure(caseId.toString(), 400, 401, 404, 500);

    CaseContainerDTO caseDetails = casesConfig.getCaseByUUID(caseId.toString());
    nullTestThrowsException(caseDetails);

    if (individual == false && individualCaseId != null) {
      throw new IllegalStateException("Can't supply individualCaseId if not for an individual");
    }

    if (caseDetails.getCaseType().contentEquals("CE") && individual == false) {
      log.info("Generating new questionnaire ID for CE");
    } else if (caseDetails.getCaseType().contentEquals("CE")
        && individual == true
        && individualCaseId == null) {
      log.info("Generating new questionnaire ID for individual in CE");
    } else if (caseDetails.getCaseType().contentEquals("HH") && individual == false) {
      log.info("Generating new questionnaire ID for HH");
    } else if (caseDetails.getCaseType().contentEquals("HH")
        && individual == true
        && individualCaseId != null) {
      log.info("Generating new questionaire ID for individual in CE");
    } else {
      throw new IllegalStateException(
          "Invalid combination of caseType, individual and individualCaseId");
    }

    NewQuestionnaireIdDTO newQuestionnaire = new NewQuestionnaireIdDTO();
    newQuestionnaire.setQuestionnaireId(
        String.format("%010d", new Random().nextInt(Integer.MAX_VALUE)));
    newQuestionnaire.setUac(UUID.randomUUID());

    return ResponseEntity.ok(newQuestionnaire);
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
        getCaseEvents(caseDetails.getId().toString(), requestParamsDTO.getCaseEvents()));
    return ResponseEntity.ok(caseDetails);
  }

  private void nullTestThrowsException(Object response) throws CTPException {
    if (response == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND);
    }
  }

  private List<EventDTO> getCaseEvents(final String caseID, final boolean includeCaseEvents) {
    final List<EventDTO> caseEvents = new ArrayList<>();
    if (!includeCaseEvents) {
      return caseEvents;
    }
    return casesConfig.getEventsByCaseID(caseID);
  }

  /**
   * Post a list of Cases in order to add to the case maps driving the responses here.
   *
   * @param requestBody - a list of cases
   * @return - response confirming post.
   */
  @RequestMapping(value = "/data/cases/add", method = RequestMethod.POST)
  @ResponseStatus(value = HttpStatus.OK)
  public ResponseEntity<ResponseDTO> addCaseData(@RequestBody List<CaseContainerDTO> requestBody)
      throws CTPException {

    log.with("requestBody", requestBody).info("Entering POST addCData");
    casesConfig.addData(requestBody);

    return ResponseEntity.ok(createResponseDTO("MockCaseAddService"));
  }

  /**
   * reset the application case data back to the original JSON
   *
   * @return - response confirming success.
   */
  @RequestMapping(value = "/data/cases/reset", method = RequestMethod.GET)
  @ResponseStatus(value = HttpStatus.OK)
  public ResponseEntity<ResponseDTO> resetCaseData() throws CTPException {
    try {
      casesConfig.resetData();
      return ResponseEntity.ok(createResponseDTO("MockCaseResetService"));
    } catch (IOException e) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST, "Unable to reset Case Data - IO Exception  reading JSON");
    }
  }

  /**
   * Post a list of Questionnaires in order to add to the questionnaire maps driving the responses
   * here.
   *
   * @param requestBody - a list of Questionnaires
   * @return - response confirming post.
   */
  @RequestMapping(value = "/data/questionnaires/add", method = RequestMethod.POST)
  @ResponseStatus(value = HttpStatus.OK)
  public ResponseEntity<ResponseDTO> addQuestionnaireData(
      @Valid @RequestBody List<QuestionnaireIdDTO> requestBody) throws CTPException {

    log.with("requestBody", requestBody).info("Entering POST addQData");
    questionnairesConfig.addData(requestBody);
    return ResponseEntity.ok(createResponseDTO("MockQuestionnaireAddService"));
  }

  /**
   * reset the application data back to the original JSON
   *
   * @return - response confirming post.
   */
  @RequestMapping(value = "/data/questionnaires/reset", method = RequestMethod.GET)
  @ResponseStatus(value = HttpStatus.OK)
  public ResponseEntity<ResponseDTO> resetQuestionnaireData() throws CTPException {
    try {
      questionnairesConfig.resetData();
      return ResponseEntity.ok(createResponseDTO("MockQuestionnaireResetService"));
    } catch (IOException e) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST,
          "Unable to reset Questionnaire Data - IO Exception  reading JSON");
    }
  }

  private ResponseDTO createResponseDTO(final String id) {
    final ResponseDTO responseDTO = new ResponseDTO();
    responseDTO.setId(id);
    responseDTO.setDateTime(new Date());
    return responseDTO;
  }
}
