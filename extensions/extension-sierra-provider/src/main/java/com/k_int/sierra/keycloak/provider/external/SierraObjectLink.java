package com.k_int.sierra.keycloak.provider.external;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * link is a hatheos style pointer to the returned object
 */

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SierraObjectLink {
	private String link;
}

