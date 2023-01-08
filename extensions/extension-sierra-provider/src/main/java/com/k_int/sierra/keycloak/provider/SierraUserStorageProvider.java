package com.k_int.sierra.keycloak.provider;


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

// interface for talking to SIERRA for user details, and a Simple HTTP implementation
import com.k_int.sierra.keycloak.provider.external.SierraUser;
import com.k_int.sierra.keycloak.provider.external.SierraClient;
import com.k_int.sierra.keycloak.provider.external.SierraClientSimpleHttp;

import org.keycloak.connections.httpclient.HttpClientProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.Header;
import java.util.Base64;

import org.jboss.logging.Logger;

/**
 * inspired by https://github.com/dasniko/keycloak-extensions-demo/blob/main/user-provider/src/main/java/dasniko/keycloak/user/PeanutsUserProvider.java
 */
public class SierraUserStorageProvider implements UserStorageProvider, 
                                                  UserLookupProvider,
                                                  CredentialInputValidator {

  // implement in future: CredentialInputUpdater, UserRegistrationProvider, UserQueryProvider,

  private static final Logger log = Logger.getLogger(SierraUserStorageProvider.class);
  private final KeycloakSession session;
  private final ComponentModel model;
  private final SierraClient client;

  public SierraUserStorageProvider(KeycloakSession session, ComponentModel model) {
    log.info("SierraUserStorageProvider::SierraUserStorageProvider(...)");
    this.session = session;
    this.model = model;
    this.client = new SierraClientSimpleHttp(session, model);
    log.info("SierraUserStorageProvider::SierraUserStorageProvider(...) COMPLETE");
  }

  @Override
  public void close() {
    log.debug("close");
  }


  // CredentialInputProvider
  // https://www.keycloak.org/docs-api/20.0.2/javadocs/org/keycloak/credential/CredentialInputValidator.html
  @Override
  public boolean supportsCredentialType(String credentialType) {
    log.debug(String.format("supportsCredentialType(%s)",credentialType));
    return PasswordCredentialModel.TYPE.equals(credentialType);
  }

  @Override
  public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
    log.debug(String.format("isConfiguredFor(realm,user,%s)",credentialType));
    return supportsCredentialType(credentialType);
  }


  /**
   *  
   */
  @Override
  public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {

    log.debug(String.format("isValid(..%s,%s)",user.toString(),input.toString()));

    if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
      return false;
    }

    UserCredentialModel ucm = (UserCredentialModel) input;
    String password = ucm.getChallengeResponse();
    String username = user.getUsername();

    try {
      if ( client.isValid(username,password) ) {
        log.debug("RETURNING isValid:: TRUE");
        return true;
      }
    }
    catch ( Exception e ) {
      log.error("Exception talking to SIERRA",e);
    }

    log.debug("RETURNING isValid:: FALSE");
    return false;
  }

  @Override
  public void preRemove(RealmModel realm) {
  }

  @Override
  public void preRemove(RealmModel realm, GroupModel group) {
  }

  @Override
  public void preRemove(RealmModel realm, RoleModel role) {
  }

  @Override
  public UserModel getUserByEmail(RealmModel realm, String email) {
    log.debug("getUserByEmail");
    SierraUser sierra_user = new SierraUser();
    // sierra_user.setSierraUUID("1234");
    sierra_user.setId("mockuser");
    // sierra_user.setFirstName("mockuserfirst");
    // sierra_user.setLastName("mockuserlast");
    // sierra_user.setEmail("mockemail");
    // sierra_user.setBarcode("mockbarcode");
    return new SierraUserAdapter(session, realm, model, sierra_user);
  }



  @Override
  public UserModel getUserByUsername(RealmModel realm, String username) {
    log.debugf("getUserByUsername: %s", username);
    SierraUser sierra_user = client.getSierraUserByUsername(username);

    // If we got a response, from our api object convert it into a keycloak user model and return it
    if ( sierra_user != null ) {
      log.debugf("Result of getUserByUsername(%s): %s",username,sierra_user.toString());
      return new SierraUserAdapter(session, realm, model, sierra_user);
    }
    else {
      log.warnf("Unable to locate user %s",username);
    }

    // Otherwise all bets are off
    return null;
  }


  @Override
  public UserModel getUserById(RealmModel realm, String id) {

    log.debugf("getUserById: %s (%s)", id, StorageId.externalId(id));

    // Whats going on here? in a user federation, keycloak constructs a user id as "f:uuid-of-provider:username" StorageId.externalId effectively
    // parses that ID out to just username, so although the function is getUserById we actually need to call 
    SierraUser sierra_user = client.getSierraUserByUsername(StorageId.externalId(id));

    // and not SierraUser sierra_user = client.getSierraUserById(StorageId.externalId(id));
    if ( sierra_user != null ) {
      log.debugf("client.getSierraUserByUsername returned %s",sierra_user.toString());
      UserModel user_model =  new SierraUserAdapter(session, realm, model, sierra_user);
      log.debugf("Converted user model is %s",user_model.toString());
      return user_model;
    }

    log.debug("getUserById did not return a user - returning null");
    return null;
  }
}
