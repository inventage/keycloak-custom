package com.k_int.sierra.keycloak.provider.external;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This is what the users endpoint returns for a query
 *
 * id,updatedDate,createdDate,expirationDate,names,barcodes,patronType,patronCodes,homeLibraryCode,emails,uniqueIds
 */

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SierraSearchExpr {
	private String op;
	private List<String> operands;

        // This should really be a static helper method I feel.. will revisit as soon as it works end to end
	public SierraSearchExpr(String single_value) {
		this.op = "equals";
		this.operands = new java.util.ArrayList<String>();
		this.operands.add(single_value);
	}
}

