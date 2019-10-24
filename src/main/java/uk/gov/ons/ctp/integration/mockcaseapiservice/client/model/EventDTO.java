package uk.gov.ons.ctp.integration.mockcaseapiservice.client.model;

import java.util.Date;
import lombok.Data;

@Data
public class EventDTO {

  private String id;

  private String category;

  private String description;

  private Date createdDateTime;

  private String eventType;
}
