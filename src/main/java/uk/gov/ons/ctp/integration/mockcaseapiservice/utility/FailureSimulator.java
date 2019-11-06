package uk.gov.ons.ctp.integration.mockcaseapiservice.utility;

import java.util.HashMap;
import java.util.Map;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.CTPException.Fault;

public class FailureSimulator {

    private static final Map<Integer, Fault> faultCodes = new HashMap<>();

    static {
        faultCodes.put(400, Fault.BAD_REQUEST);
        faultCodes.put(401, Fault.ACCESS_DENIED);
        faultCodes.put(404, Fault.RESOURCE_NOT_FOUND);
        faultCodes.put(500, Fault.SYSTEM_ERROR);
    }

    public static void optionallyTriggerFailure(String key, Integer... failureCodes)
            throws CTPException {
        for (int failureCode : failureCodes) {
            String failureCodeAsString = Integer.toString(failureCode);
            if (key.startsWith(failureCodeAsString)) {
                throw new CTPException(faultCodes.get(failureCode));
            }
        }
    }
}
