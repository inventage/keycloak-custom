package com.k_int.folio.keycloak.provider.external;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This is what the users endpoint returns for a query
 *
 * {
 *   "users": [
 * {"username":"diku_admin","id":"30ed55d7-59b1-591e-9a41-2c553bda3164","active":true,"patronGroup":"3684a786-6671-4268-8ed0-9db82ebca60b","departments":[],"proxyFor":[],"personal":{"lastName":"ADMINISTRATOR","firstName":"DIKU","email":"admin@diku.example.org","addresses":[]},"createdDate":"2022-12-27T15:17:08.325+00:00","updatedDate":"2022-12-27T15:17:08.325+00:00","metadata":{"createdDate":"2022-12-27T15:13:46.869+00:00","updatedDate":"2022-12-27T15:17:08.321+00:00","updatedByUserId":"30ed55d7-59b1-591e-9a41-2c553bda3164"}}],
 *   "totalRecords": 1,
 *   "resultInfo": {"totalRecords":1,"facets":[],"diagnostics":[]}
 * }
 * 
 */

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FolioUser {
	private String id;
	private String username;
        private Boolean active;
        private String barcode;
        private String type;
        private String patronGroup;
        private UserPersonalData personal;
        private String localSystemCode;
        private String homeLibraryCode;
	private List<String> groups;
	private List<String> roles;
}

