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
public class SierraSearchTarget {
	private SierraSearchRecord record;
	private SierraSearchField field;

	public SierraSearchTarget(String resourceType, String tag) {
		this.record = new SierraSearchRecord();
                this.record.setType(resourceType);
		this.field = new SierraSearchField();
		this.field.setTag(tag);
	}
}

