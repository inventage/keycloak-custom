package com.k_int.folio.keycloak.provider.external;

import lombok.Data;
import java.util.List;

@lombok.Data
public class UserPersonalData {
	private String lastName;
	private String firstName;
        private String email;
        // Addresses = List
}

