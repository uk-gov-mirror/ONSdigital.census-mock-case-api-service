package uk.gov.ons.ctp.integration.fakecaseservice.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.integration.contactcentresvc.representation.CaseRequestDTO;
import uk.gov.ons.ctp.integration.contactcentresvc.representation.model.UniquePropertyReferenceNumber;
import uk.gov.ons.ctp.integration.fakecaseservice.client.model.CaseContainerDTO;
import uk.gov.ons.ctp.integration.fakecaseservice.client.model.EventDTO;
import uk.gov.ons.ctp.integration.fakecaseservice.utility.FailureSimulator;

/** Provides fake endpoints for the case service. */
@RestController
@RequestMapping(value = "/cases", produces = "application/json")
public final class CaseServiceFakeStub implements CTPEndpoint {
  private static final Logger log = LoggerFactory.getLogger(CaseServiceFakeStub.class);

  @RequestMapping(value = "/info", method = RequestMethod.GET)
  public ResponseEntity<String> info() throws CTPException {

    return ResponseEntity.ok("FAKE CASE SERVICE");
  }

  /**
   * the GET endpoint to find a Case by UUID
   *
   * @param caseId to find by
   * @param caseevents flag used to return or not CaseEvents
   * @return the case found
   * @throws CTPException something went wrong
   * @throws ParseException x
   */
  @RequestMapping(value = "/{caseId}", method = RequestMethod.GET)
  public ResponseEntity<CaseContainerDTO> findCaseById(
      @PathVariable("caseId") final UUID caseId,
      @RequestParam(value = "caseEvents", required = false) boolean caseevents)
      throws CTPException, ParseException {
    log.with("case_id", caseId).debug("Entering findCaseById");

    FailureSimulator.optionallyTriggerFailure(caseId.toString(), 400, 401, 404, 500);

    CaseContainerDTO caseDetails = createFakeCase(caseId, "11 Park Street", "123123", caseevents);

    return ResponseEntity.ok(caseDetails);
  }

  @RequestMapping(value = "/uprn/{uprn}", method = RequestMethod.GET)
  public ResponseEntity<List<CaseContainerDTO>> findCaseByUPRN(
      @PathVariable(value = "uprn") final UniquePropertyReferenceNumber uprn)
      throws CTPException, ParseException {
    log.with("uprn", uprn).debug("Entering findCaseByUPRN");

    FailureSimulator.optionallyTriggerFailure(Long.toString(uprn.getValue()), 400, 401, 404, 500);

    // Final digit of uprn controls how many cases to return
    List<CaseContainerDTO> cases = new ArrayList<>();
    long numCases = uprn.getValue() % 10;
    for (int i = 0; i < numCases; i++) {
      cases.add(createFakeCase(UUID.randomUUID(), (i + " Park Street"), "123123", true));
    }

    return ResponseEntity.ok(cases);
  }

  @RequestMapping(value = "/ref/{ref}", method = RequestMethod.GET)
  public ResponseEntity<CaseContainerDTO> findCaseByCaseReference(
      @PathVariable(value = "ref") final long ref, @Valid CaseRequestDTO requestParamsDTO)
      throws CTPException, ParseException {
    log.with("ref", ref)
        .with("caseEvents", requestParamsDTO.getCaseEvents())
        .info("Entering GET getCaseByCaseReference");

    FailureSimulator.optionallyTriggerFailure(Long.toString(ref), 400, 401, 404, 500);

    CaseContainerDTO caseData =
        createFakeCase(
            UUID.randomUUID(), "88 Harbour Street", "123123", requestParamsDTO.getCaseEvents());
    return ResponseEntity.ok(caseData);
  }

  private CaseContainerDTO createFakeCase(
      UUID caseId, String address, String caseRef, boolean includeCaseEvents)
      throws ParseException {
    SimpleDateFormat dateParser = new SimpleDateFormat(DateTimeUtil.DATE_FORMAT_IN_JSON);

    List<EventDTO> caseEvents = new ArrayList<>();

    if (includeCaseEvents) {
      EventDTO e1 = new EventDTO();
      e1.setId("101");
      e1.setDescription("Initial creation of case");
      e1.setCategory("CASE_CREATED");
      e1.setCreatedDateTime(dateParser.parse("2019-04-01T07:12:26.626Z"));

      EventDTO e2 = new EventDTO();
      e2.setId("102");
      e2.setDescription("Create Household Visit");
      e2.setCategory("CASE_UPDATED");
      e2.setCreatedDateTime(dateParser.parse("2019-12-14T12:45:26.751Z"));

      caseEvents.add(e1);
      caseEvents.add(e2);
    }

    CaseContainerDTO caseDetails = new CaseContainerDTO();
    caseDetails.setId(caseId);
    caseDetails.setArid("2344266233");
    caseDetails.setEstabArid("AABBCC");
    caseDetails.setEstabType("ET");
    caseDetails.setUprn("1347459999");
    caseDetails.setCaseRef(caseRef);
    caseDetails.setCaseType("HH");
    caseDetails.setCreatedDateTime(dateParser.parse("2019-04-14T12:45:26.564Z"));
    caseDetails.setAddressLine1("Napier House");
    caseDetails.setAddressLine2(address);
    caseDetails.setAddressLine3("Parkhead");
    caseDetails.setTownName("Glasgow");
    caseDetails.setPostcode("G1 2AA");
    caseDetails.setOrganisationName("ON");
    caseDetails.setAddressLevel("E");
    caseDetails.setAbpCode("AACC");
    caseDetails.setRegion("E");
    caseDetails.setLatitude("41.40338");
    caseDetails.setLongitude("2.17403");
    caseDetails.setOa("EE22");
    caseDetails.setLsoa("x1");
    caseDetails.setMsoa("x2");
    caseDetails.setLad("H1");
    caseDetails.setCaseEvents(caseEvents);

    return caseDetails;
  }

  //  private String createCaseString() {
  //    String caseDetails =
  //        "{\n"
  //            + "  \"id\": \"b7565b5e-1396-4965-91a2-918c0d3642ed\",\n"
  //            + "  \"arid\": \"2344266233\",\n"
  //            + "  \"estabArid\": \"AABBCC\",\n"
  //            + "  \"estabType\": \"ET\",\n"
  //            + "  \"uprn\": \"1235532324343434\",\n"
  //            + "  \"caseRef\": \"1000000000000001\",\n"
  //            + "  \"caseType\": \"HH\",\n"
  //            + "  \"createdDateTime\": \"2019-05-14T16:11:41.343+01:00\",\n"
  //            + "  \"addressLine1\": \"Napier House\",\n"
  //            + "  \"addressLine2\": \"11 Park Street\",\n"
  //            + "  \"addressLine3\": \"Parkhead\",\n"
  //            + "  \"townName\": \"Glasgow\",\n"
  //            + "  \"postcode\": \"G1 2AA\",\n"
  //            + "  \"organisationName\": \"ON\",\n"
  //            + "  \"addressLevel\": \"E\",\n"
  //            + "  \"abpCode\": \"AACC\",\n"
  //            + "  \"region\": \"E\",\n"
  //            + "  \"latitude\": \"41.40338\",\n"
  //            + "  \"longitude\": \"2.17403\",\n"
  //            + "  \"oa\": \"EE22\",\n"
  //            + "  \"lsoa\": \"x1\",\n"
  //            + "  \"msoa\": \"x2\",\n"
  //            + "  \"lad\": \"H1\",\n"
  //            + "  \"caseEvents\": [\n"
  //            + "    {\n"
  //            + "      \"id\": \"101\",\n"
  //            + "      \"category\": \"CASE_CREATED\",\n"
  //            + "      \"description\": \"Initial creation of case\",\n"
  //            + "      \"createdDateTime\": \"2019-05-14T16:11:41\"\n"
  //            + "    },\n"
  //            + "    {\n"
  //            + "      \"id\": \"102\",\n"
  //            + "      \"category\": \"CASE_UPDATED\",\n"
  //            + "      \"description\": \"Create Household Visit\",\n"
  //            + "      \"createdDateTime\": \"2019-05-16T12:12:12.343Z\"\n"
  //            + "    }\n"
  //            + "  ]\n"
  //            + "}";
  //
  //    return caseDetails;
  //  }
}
