package com.k_int.sierra.keycloak.provider.external;


import com.fasterxml.jackson.core.type.TypeReference;
import com.k_int.sierra.keycloak.provider.SierraProviderConstants;
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
import java.util.Base64;

public class SierraClientSimpleHttp implements SierraClient {

  private static final Logger log = Logger.getLogger(SierraClientSimpleHttp.class);

  private final CloseableHttpClient httpClient;
  private final String baseUrl;
  private final String client_key;
  private final String secret;
  private final String localSystemCode;

        // Not final - we expect that the jwt may become invalidated at some point and will need to be refreshed. TBC
  private String cached_okapi_api_session_jwt;


  public SierraClientSimpleHttp(KeycloakSession session, ComponentModel model) {

    this.httpClient = session.getProvider(HttpClientProvider.class).getHttpClient();

    this.baseUrl = model.get(SierraProviderConstants.BASE_URL);
    this.client_key = model.get(SierraProviderConstants.CLIENT_KEY);
    this.secret = model.get(SierraProviderConstants.SECRET);
    this.localSystemCode = model.get(SierraProviderConstants.LOCAL_SYSTEM_CODE);

    log.debug(String.format("%s = %s ",SierraProviderConstants.BASE_URL,this.baseUrl));
    log.debug(String.format("%s = %s",SierraProviderConstants.CLIENT_KEY,this.client_key));
    log.debug(String.format("%s = %s",SierraProviderConstants.SECRET,this.secret));
    log.debug(String.format("%s = %s",SierraProviderConstants.LOCAL_SYSTEM_CODE,this.localSystemCode));
  }

  private String getSierraSession() {
    if ( cached_okapi_api_session_jwt == null ) {
      try {
        String login_url = baseUrl + "/iii/sierra-api/v6/token";
        String encoded_auth = Base64.getEncoder().encodeToString((client_key+':'+secret).getBytes());

        SimpleHttp.Response response = SimpleHttp.doGet(login_url, httpClient)
                                                 .header("Authorization", "Basic "+encoded_auth)
                                                 .asResponse();

        SierraTokenResponse token_response = response.asJson(SierraTokenResponse.class);
        if ( token_response != null )
          cached_okapi_api_session_jwt = token_response.getToken();
      }
      catch ( Exception e ) {
        log.error("Exception obtaining API session with SIERRA");
        cached_okapi_api_session_jwt = null;
      }
    }
    return cached_okapi_api_session_jwt;
  }


  @Override
  @SneakyThrows
  public SierraUser getSierraUserById(String id) {

    log.debug(String.format("getSierraUserById(%s)",id));

    String url = String.format("%s/%s", baseUrl, id);
    String api_session_token = getSierraSession();
    if ( api_session_token != null ) {
      String get_user_url = String.format("%s/users/%s",baseUrl,id);
      SimpleHttp.Response response = SimpleHttp.doGet(get_user_url, httpClient)
                                                   .header("X-Okapi-Token", api_session_token)
                                                   .asResponse();
      SierraUser fu = response.asJson(SierraUser.class);
      return fu;
    }
    else {
      log.warn("No session api token");
    }

    return null;
  }

  @Override
  @SneakyThrows
  public SierraUser getSierraUserByUsername(String username) {
    log.debug(String.format("getSierraUserByUsername(%s)",username));
  
    String api_session_token = getSierraSession();

    if ( api_session_token != null ) {

      // We lookup users by calling /users with query parameters limit, query, sortBy, etc

      String user_query_url = String.format("%s/users?query=%s",baseUrl,"username%3d"+username);
      log.debugf("attempting user lookup %s",user_query_url);
      SimpleHttp.Response response = SimpleHttp.doGet(user_query_url, httpClient).header("X-Okapi-Token", api_session_token).asResponse();

      if (response.getStatus() == 404) {
        throw new WebApplicationException(response.getStatus());
      }

      SierraUserSearchResult fusr = response.asJson(SierraUserSearchResult.class);
      if ( ( fusr != null ) && 
           ( fusr.getTotalRecords() == 1 ) )
        return fusr.getUsers().get(0);

       return null;
    }
    else { 
      log.warn("No session api token");
    }

    return null;
  }     

  /**
   *  
   */
  @Override
  public boolean isValid(String barcode, String pin) throws java.io.UnsupportedEncodingException, java.io.IOException {

    boolean result = false;

    log.debugf("isValid(..%s,%s)",barcode,pin);
    String api_session_token = getSierraSession();
    String login_url = this.baseUrl + "/iii/sierra-api/v6/patrons/validate";

    // SimpleHttp simpleHttp = SimpleHttp.doPost(httpAuthenticationChannelUri, session)
    //                             .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
    //                             .json(channelRequest)
    //                             .auth(createBearerToken(request, client));

    String user_pin_json = String.format("{\"barcode\":\"%s\",\"pin\":\"%s\"}", barcode, pin);
    StringEntity entity = new StringEntity(user_pin_json);

    SimpleHttp.Response response = SimpleHttp.doPost(login_url, httpClient)
                                                 .header("Authorization", "Basic "+api_session_token)
                                                 .header("Accept", "application/json" )
                                                 .header("Content-Type", "application/json" )
                                                 .json(entity)
                                                 .asResponse();

    if ( response.getStatus() == 204 ) {
      result = true;
    }

    log.debugf("isValid(%s,...) returning "+result,barcode);

    return result;
  }


}
