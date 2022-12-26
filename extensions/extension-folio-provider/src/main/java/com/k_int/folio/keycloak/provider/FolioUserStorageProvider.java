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

// interface for talking to FOLIO for user details, and a Simple HTTP implementation
import com.k_int.folio.keycloak.provider.external.FolioUser;
import com.k_int.folio.keycloak.provider.external.FolioClient;
import com.k_int.folio.keycloak.provider.external.FolioClientSimpleHttp;

import org.keycloak.connections.httpclient.HttpClientProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;

import org.jboss.logging.Logger;

/**
 * inspired by https://github.com/dasniko/keycloak-extensions-demo/blob/main/user-provider/src/main/java/dasniko/keycloak/user/PeanutsUserProvider.java
 */
public class FolioUserStorageProvider implements UserStorageProvider, 
                                                 UserLookupProvider,
                                                 CredentialInputValidator {

  // implement in future: CredentialInputUpdater, UserRegistrationProvider, UserQueryProvider,

  private static final Logger log = Logger.getLogger(FolioUserStorageProvider.class);
  private final KeycloakSession session;
  private final ComponentModel model;
  private final FolioClient client;

  public FolioUserStorageProvider(KeycloakSession session, ComponentModel model) {
    log.info("FolioUserStorageProvider::FolioUserStorageProvider(...)");
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
    log.debug(String.format("supportsCredentialType(%s)",credentialType));
    return PasswordCredentialModel.TYPE.equals(credentialType);
  }

  @Override
  public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
    log.debug(String.format("isConfiguredFor(realm,user,%s)",credentialType));
    return supportsCredentialType(credentialType);
  }

  @Override
  public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {

    log.debug(String.format("isValid(...%s)",input.toString()));

    if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
      return false;
    }

    UserCredentialModel ucm = (UserCredentialModel) input;
    String password = ucm.getChallengeResponse();
    String username = user.getUsername();

    try {
      int response = attemptFolioLogin(username, password);
    }
    catch ( Exception e ) {
      log.error("Exception talking to FOLIO/OKAPI",e);
    }

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

  private int attemptFolioLogin(String username, String password) throws Exception {

      String user_pass_json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
      StringEntity entity = new StringEntity(user_pass_json);

      int responseCode = 400;
      CloseableHttpClient client = session.getProvider(HttpClientProvider.class).getHttpClient();
      try {
        String cfg_baseUrl = model.get(FolioProviderConstants.BASE_URL);
        String cfg_tenant = model.get(FolioProviderConstants.TENANT);
        String cfg_basicUsername = model.get(FolioProviderConstants.AUTH_USERNAME);
        String cfg_basicPassword = model.get(FolioProviderConstants.AUTH_PASSWORD);
  
        log.debug(String.format("Attempting FOLIO to %s(%s)login with %s",cfg_baseUrl, cfg_tenant, user_pass_json));
  
        // get okapi token first
        log.info("/authn/login");
        String token_url = cfg_baseUrl + "/authn/login";
        HttpPost httpPost = new HttpPost(token_url);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("X-Okapi-Tenant", cfg_tenant);
        CloseableHttpResponse response = client.execute(httpPost);

        if ( response != null ) {
          log.debugf("Got okapi response %s",response.toString());
          String token = response.getFirstHeader("X-Okapi-Token").getValue().toString();
  
          // call '/bl-users/login' with token (expected 201)
          log.info("/bl-users/login");
          if (token != null){
             String blusers_url = cfg_baseUrl + "/bl-users/login";
             HttpPost postRequest = new HttpPost(blusers_url);
             postRequest.setEntity(entity);
             postRequest.setHeader("Content-type", "application/json");
             postRequest.setHeader("X-Okapi-Token", token);
             CloseableHttpResponse httpResponse = client.execute(postRequest);
             responseCode = httpResponse.getStatusLine().getStatusCode();
          } else { 
            throw new Exception("Invaid token, check credentials."); 
          }
        }
        else {
          log.warn("NULL response from OKAPI");
        }
      }
      finally {
        if ( client != null ) 
          client.close();
      }

      return responseCode;
  }


  @Override
  public UserModel getUserByEmail(RealmModel realm, String email) {
    log.debug("getUserByEmail");
    FolioUser folio_user = new FolioUser();
    folio_user.setFolioUUID("1234");
    folio_user.setUsername("mockuser");
    folio_user.setFirstName("mockuserfirst");
    folio_user.setLastName("mockuserlast");
    folio_user.setEmail("mockemail");
    folio_user.setBarcode("mockbarcode");
    return new FolioUserAdapter(session, realm, model, folio_user);
  }


  @Override
  public UserModel getUserByUsername(RealmModel realm, String username) {
    log.debugf("getUserByUsername: %s", username);
    FolioUser folio_user = new FolioUser();
    folio_user.setFolioUUID("1234");
    folio_user.setUsername("mockuser");
    folio_user.setFirstName("mockuserfirst");
    folio_user.setLastName("mockuserlast");
    folio_user.setEmail("mockemail");
    folio_user.setBarcode("mockbarcode");
    return new FolioUserAdapter(session, realm, model, folio_user);
  }


  @Override
  public UserModel getUserById(RealmModel realm, String id) {
    log.debugf("getUserById: %s", id);
    FolioUser folio_user = new FolioUser();
    folio_user.setFolioUUID("1234");
    folio_user.setUsername("mockuser");
    folio_user.setFirstName("mockuserfirst");
    folio_user.setLastName("mockuserlast");
    folio_user.setEmail("mockemail");
    folio_user.setBarcode("mockbarcode");
    return new FolioUserAdapter(session, realm, model, folio_user);
  }
}
