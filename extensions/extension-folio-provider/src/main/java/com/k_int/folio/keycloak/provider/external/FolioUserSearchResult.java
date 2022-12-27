package com.k_int.folio.keycloak.provider.external;

import lombok.Data;
import java.util.List;

@lombok.Data
public class FolioUserSearchResult {
  private List<FolioUser> users;
  private long totalRecords;
  // Lets see if we can ignore resultInfo { totalRecords:n, diagnostics:[] }
}

