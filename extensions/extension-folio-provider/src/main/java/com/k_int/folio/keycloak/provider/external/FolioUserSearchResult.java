package com.k_int.folio.keycloak.provider.external;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FolioUserSearchResult {
  private List<FolioUser> users;
  private long totalRecords;
  // Lets see if we can ignore resultInfo { totalRecords:n, diagnostics:[] }
}

