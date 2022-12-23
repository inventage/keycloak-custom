package org.olf.folio.adaptor;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolioAuthenticator implements ConditionalAuthenticator {

   private final String OKAPI = "https://folio-snapshot-okapi.dev.folio.org/";
   private final String tenant = "diku";
   private final String UN_PW_json = "{\"username\":\"diku_admin\",\"password\":\"admin\"}";
   private int defaultResponseCode = 400;

   private static Logger log = LoggerFactory.getLogger(FolioAuthenticator.class);


   @Override
   public boolean matchCondition(AuthenticationFlowContext authenticationFlowContext) {

      try {
         defaultResponseCode = getfolioCode();
      } catch (ClientProtocolException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }

      if(defaultResponseCode == 201){
         log.info("Successful login to folio!");
         return true;
      } else {
         System.out.println("Folio response code was not 201");
         log.info("Folio login was unsuccessful!");
         return false;
      }
   }

   public int getfolioCode() throws Exception {
      StringEntity entity = new StringEntity(UN_PW_json);

      // get okapi token first
      log.info("/authn/login");
      CloseableHttpClient client = HttpClients.createDefault();
      String token_url = OKAPI + "/authn/login";
      HttpPost httpPost = new HttpPost(token_url);
      httpPost.setEntity(entity);
      httpPost.setHeader("Accept", "application/json");
      httpPost.setHeader("Content-type", "application/json");
      httpPost.setHeader("X-Okapi-Tenant", tenant);
      CloseableHttpResponse response = client.execute(httpPost);
      String token = response.getFirstHeader("X-Okapi-Token").getValue().toString();
      client.close();

      // call '/bl-users/login' with token (expected 201)
      log.info("/bl-users/login");
      if (token != null){
         CloseableHttpClient httpClient = HttpClients.createDefault();
         String blusers_url = OKAPI + "/bl-users/login";
         HttpPost postRequest = new HttpPost(blusers_url);
         postRequest.setEntity(entity);
         postRequest.setHeader("Content-type", "application/json");
         postRequest.setHeader("X-Okapi-Token", token);
         CloseableHttpResponse httpResponse = httpClient.execute(postRequest);
         defaultResponseCode = httpResponse.getStatusLine().getStatusCode();
         httpClient.close();

      } else { throw new Exception("Invaid token, check credentials."); }

      return defaultResponseCode;
   }

   @Override
   public void action(AuthenticationFlowContext authenticationFlowContext) {

   }

   @Override
   public boolean requiresUser() {
      return false;
   }

   @Override
   public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

   }

   @Override
   public void close() {

   }
}
