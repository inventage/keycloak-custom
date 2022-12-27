package com.k_int.folio.keycloak.provider.external;


import com.fasterxml.jackson.core.type.TypeReference;
import com.k_int.folio.keycloak.provider.FolioProviderConstants;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.Header;
import org.jboss.logging.Logger;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;

public class FolioClientSimpleHttp implements FolioClient {

  private static final Logger log = Logger.getLogger(FolioClientSimpleHttp.class);

  private final CloseableHttpClient httpClient;
  private final String baseUrl;
  private final String tenant;
  private final String basicUsername;
  private final String basicPassword;

        // Not final - we expect that the jwt may become invalidated at some point and will need to be refreshed. TBC
  private String cached_okapi_api_session_jwt;


  public FolioClientSimpleHttp(KeycloakSession session, ComponentModel model) {

    this.httpClient = session.getProvider(HttpClientProvider.class).getHttpClient();

    this.baseUrl = model.get(FolioProviderConstants.BASE_URL);
    log.debug(String.format("%s = %s ",FolioProviderConstants.BASE_URL,this.baseUrl));

    this.tenant = model.get(FolioProviderConstants.TENANT);
    log.debug(String.format("%s = %s",FolioProviderConstants.TENANT,this.tenant));

    this.basicUsername = model.get(FolioProviderConstants.AUTH_USERNAME);
    log.debug(String.format("%s = %s",FolioProviderConstants.AUTH_USERNAME,this.basicUsername));

    this.basicPassword = model.get(FolioProviderConstants.AUTH_PASSWORD);
  }


  @Override
  @SneakyThrows
  public FolioUser getFolioUserById(String id) {

    log.debug(String.format("getFolioUserById(%s)",id));

    String url = String.format("%s/%s", baseUrl, id);
    // SimpleHttp.Response response = SimpleHttp.doGet(url, httpClient).authBasic(basicUsername, basicPassword).asResponse();
    // if (response.getStatus() == 404) {
    //   throw new WebApplicationException(response.getStatus());
    // }
    // return response.asJson(Peanut.class);
          
                FolioUser mock_user = new FolioUser();
          mock_user.setFolioUUID("1234");
          mock_user.setUsername("mockuser");
          mock_user.setFirstName("mockuserfirst");
          mock_user.setLastName("mockuserlast");
          mock_user.setEmail("mockemail");
          mock_user.setBarcode("mockbarcode");
                return mock_user;
  }

  @Override
  @SneakyThrows
  public FolioUser getFolioUserByUsername(String username) {
    log.debug(String.format("getFolioUserByUsername(%s)",username));
  
    String api_session_token = getValidOKAPISession();

    if ( api_session_token != null ) {

      // We lookup users by calling /users with query parameters limit, query, sortBy, etc

      String user_query_url = String.format("%s/users?query=%s",baseUrl,"username%3d"+username);
      log.debugf("attempting user lookup %s",user_query_url);
      SimpleHttp.Response response = SimpleHttp.doGet(user_query_url, httpClient).header("X-Okapi-Token", api_session_token).asResponse();

      if (response.getStatus() == 404) {
        throw new WebApplicationException(response.getStatus());
      }

      FolioUserSearchResult fusr = response.asJson(FolioUserSearchResult.class);
      if ( ( fusr != null ) && 
           ( fusr.getTotalRecords() == 1 ) )
        return fusr.getUsers().get(0);

       return null;
      // FolioUser mock_user = new FolioUser();
      // mock_user.setFolioUUID("1234");
      // mock_user.setUsername("mockuser");
      // mock_user.setFirstName("mockuserfirst");
      // mock_user.setLastName("mockuserlast");
      // mock_user.setEmail("mockemail");
      // mock_user.setBarcode("mockbarcode");
      // return mock_user;
    }
    else { 
    }

    return null;
  }     

        /**
         * Get an okapi session capable of looking up user details.
         * use the URL, tenant, username and password configured in the keycloak provider screen. This gives us a
         * JWT we can use to perform user lookup operations generally
         * We cache the session jwt so we don't need to spam the login endpoint
         * @return jwt we can use in X-Okapi-Token when looking up users or performing other API tasks
         */
  private String getValidOKAPISession() {
    if ( cached_okapi_api_session_jwt == null ) {  // ToDo: Or it has expired

      log.debug("Attempting to get new session token for OKAPI API");

      try {
        String blusers_url = baseUrl + "/bl-users/login";
        String user_pass_json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", basicUsername, basicPassword);
        log.debugf("Attempt bl-users login at %s with %s",blusers_url,user_pass_json);

        StringEntity entity = new StringEntity(user_pass_json);
        HttpPost postRequest = new HttpPost(blusers_url);
        postRequest.setEntity(entity);
        postRequest.setHeader("Content-type", "application/json");
        CloseableHttpResponse httpResponse = httpClient.execute(postRequest);
        log.debugf("Okapi API login: %d",httpResponse.getStatusLine().getStatusCode());
        // The httpResponse contains a number of headers, which should include an X-Okapi-Token if the login succeeded
        Header okapi_token_header = httpResponse.getFirstHeader("X-Okapi-Token");

        if ( okapi_token_header != null )
          cached_okapi_api_session_jwt = okapi_token_header.getValue().toString();
        else
          log.warn("Okapi API Login Response did not carry an X-Okapi-Token - likely invalid API user");
      }
      catch ( Exception e ) {
        log.error("Exception obtaining API session with OKAPI");
      }
    }
    return cached_okapi_api_session_jwt;
  }
}
