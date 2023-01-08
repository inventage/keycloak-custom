package com.k_int.sierra.keycloak.provider.external;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This is what the users endpoint returns for a query
 *
 * id,updatedDate,createdDate,expirationDate,names,barcodes,patronType,patronCodes,homeLibraryCode,emails,message,blockInfo,autoBlockInfo,uniqueIds,emails
 */

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SierraUser {
	private String id;
	private List<String> names;
	private List<String> barcodes;
	private List<String> emails;
	private List<String> uniqueIds;
	private Integer patronType;
        private String localSystemCode;
        private String homeLibraryCode;
	private List<String> groups;
	private List<String> roles;
}

