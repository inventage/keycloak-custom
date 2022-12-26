package com.k_int.folio.keycloak.provider.external;

import lombok.Data;
import java.util.List;

@lombok.Data
public class FolioUser {
	private String folioUUID;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private String barcode;
	private List<String> groups;
	private List<String> roles;
}

