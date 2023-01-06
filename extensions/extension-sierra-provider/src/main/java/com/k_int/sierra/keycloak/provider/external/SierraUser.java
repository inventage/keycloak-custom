package com.k_int.sierra.keycloak.provider.external;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This is what the users endpoint returns for a query
 *
 */

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SierraUser {
	private String id;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
        private String localSystemCode;
        private String homeLibraryCode;
	private List<String> groups;
	private List<String> roles;
}

