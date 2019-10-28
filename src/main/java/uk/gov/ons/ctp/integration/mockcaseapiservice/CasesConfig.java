package uk.gov.ons.ctp.integration.mockcaseapiservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.ons.ctp.integration.caseapiclient.caseservice.model.CaseContainerDTO;
import java.io.IOException;
import java.util.*;

@Configuration
@EnableConfigurationProperties
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:cases.yml")
@ConfigurationProperties("casedata")
public class CasesConfig {

    private String cases;
    private Map<String, CaseContainerDTO> caseUUIDMap = new HashMap<>();
    private Map<String, CaseContainerDTO> caseRefMap = new HashMap<>();
    private Map<String, List<CaseContainerDTO>> caseUprnMap = new HashMap<>();

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

    public CaseContainerDTO getCaseByUUID(final String key) {
        return caseUUIDMap.getOrDefault(key, null);
    }

    public CaseContainerDTO getCaseByRef(final String key) {
        return caseRefMap.getOrDefault(key, null);
    }

    public List<CaseContainerDTO> getCaseByUprn(final String key) {
        return caseUprnMap.getOrDefault(key, null);
    }

    @Override
    public String toString() {
        return "{" + getCases() + "}";
    }
}
