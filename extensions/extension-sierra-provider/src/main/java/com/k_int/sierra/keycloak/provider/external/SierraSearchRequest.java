package com.k_int.sierra.keycloak.provider.external;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This is what the users endpoint returns for a query
 *
 * id,updatedDate,createdDate,expirationDate,names,barcodes,patronType,patronCodes,homeLibraryCode,emails,uniqueIds
 *
 * think we should add @lombok.Builder here but waiting for it all to work the simple way first
 */

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SierraSearchRequest {
	private SierraSearchTarget target;
	private SierraSearchExpr expr;

	public SierraSearchRequest(String resourceType, String tag, String singleValue) {
		this.target = new SierraSearchTarget(resourceType,tag);
		this.expr = new SierraSearchExpr(singleValue);
	}
       
}

