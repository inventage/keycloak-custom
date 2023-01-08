package com.k_int.sierra.keycloak.provider.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 */

@lombok.Builder
@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SierraPatronAuthenticationRequest {
	private String barcode;
	private String pin;
}

