package com.k_int.sierra.keycloak.provider.external;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SierraUserSearchResult {
  private List<SierraUser> users;
  private Long totalRecords;
  // Lets see if we can ignore resultInfo { totalRecords:n, diagnostics:[] }
}

