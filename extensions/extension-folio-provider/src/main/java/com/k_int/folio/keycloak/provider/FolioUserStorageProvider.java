package com.k_int.folio.keycloak.provider;


import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// interface for talking to FOLIO for user details, and a Simple HTTP implementation
import com.k_int.folio.keycloak.provider.external.FolioClient;
import com.k_int.folio.keycloak.provider.external.FolioClientSimpleHttp;

/**
 * inspired by https://github.com/dasniko/keycloak-extensions-demo/blob/main/user-provider/src/main/java/dasniko/keycloak/user/PeanutsUserProvider.java
 */
public class FolioUserStorageProvider implements UserStorageProvider, CredentialInputValidator {

  // implement in future: UserLookupProvider, UserQueryProvider, CredentialInputUpdater, UserRegistrationProvider {

  private static Logger log = LoggerFactory.getLogger(FolioUserStorageProvider.class);
  private final KeycloakSession session;
  private final ComponentModel model;
  private final FolioClient client;

  public FolioUserStorageProvider(KeycloakSession session, ComponentModel model) {
    this.session = session;
    this.model = model;
    this.client = new FolioClientSimpleHttp(session, model);
  }

  @Override
  public void close() {
    log.debug("close");
  }


  // CredentialInputProvider
  // https://www.keycloak.org/docs-api/20.0.2/javadocs/org/keycloak/credential/CredentialInputValidator.html
  @Override
  public boolean supportsCredentialType(String credentialType) {
    log.debug("supportsCredentialType({})",credentialType);
    return PasswordCredentialModel.TYPE.equals(credentialType);
  }

  @Override
  public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
    return supportsCredentialType(credentialType);
  }

  @Override
  public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
    if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
      return false;
    }

    return false;
  }

  // These are from other interfaces
  /*
  */

  @Override
  public void preRemove(RealmModel realm) {
  }

  @Override
  public void preRemove(RealmModel realm, GroupModel group) {
  }

  @Override
  public void preRemove(RealmModel realm, RoleModel role) {
  }
}
