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

  // Suggestions for a better way to do this are MOST welcome
  private static final String USER_LOOKUP_JSON_TEMPLATE = "{ \"target\": { \"record\": {\"type\": \"patron\"}, \"field\": {\"tag\":\"b\"} }, \"expr\": { \"op\": \"equals\", \"operands\": [\"%s\"] } }";
  private static final String REQUIRED_USER_FIELDS = "id,updatedDate,createdDate,names,barcodes,patronType,patronCodes,homeLibraryCode,uniqueIds";

        // Not final - we expect that the jwt may become invalidated at some point and will need to be refreshed. TBC
  private String cached_okapi_api_session_jwt;


  public SierraClientSimpleHttp(KeycloakSession session, ComponentModel model) {

    this.httpClient = session.getProvider(HttpClientProvider.class).getHttpClient();

    this.baseUrl = model.get(SierraProviderConstants.BASE_URL);
    this.client_key = model.get(SierraProviderConstants.CLIENT_KEY);
    this.secret = model.get(SierraProviderConstants.SECRET);
    this.localSystemCode = model.get(SierraProviderConstants.LOCAL_SYSTEM_CODE);

    log.debug(String.format("%s = %s",SierraProviderConstants.BASE_URL,this.baseUrl));
    log.debug(String.format("%s = %s",SierraProviderConstants.CLIENT_KEY,this.client_key));
    log.debug(String.format("%s = %s",SierraProviderConstants.SECRET,this.secret));
    log.debug(String.format("%s = %s",SierraProviderConstants.LOCAL_SYSTEM_CODE,this.localSystemCode));
  }

  private String getSierraSession() {
    log.debug("getSierraSession()");
    if ( cached_okapi_api_session_jwt == null ) {

      // Only for debugging - remove ASAP
      log.debugf("No cached session token - get one : %s %s %s",baseUrl,client_key,secret);
      try {
        String login_url = baseUrl + "/iii/sierra-api/v6/token";
        String encoded_auth = Base64.getEncoder().encodeToString((client_key+':'+secret).getBytes());
        log.debugf("Contact %s %s",login_url,encoded_auth);

        SimpleHttp.Response response = SimpleHttp.doPost(login_url, httpClient)
                                             .header("Authorization", "Basic "+encoded_auth)
                                             .asResponse();

        log.debugf("Result of get token: %d",response.getStatus());

        SierraAccessTokenResponse token_response = response.asJson(SierraAccessTokenResponse.class);
        log.debugf("Get token response: %s",token_response);

        if ( token_response != null )
          cached_okapi_api_session_jwt = token_response.getAccess_token();
        else
          log.warn("Failed to get response from server");
      }
      catch ( Exception e ) {
        log.error("Exception obtaining API session with SIERRA", e);
        cached_okapi_api_session_jwt = null;
      }
    }

    log.debugf("getSierraSession() result %s",cached_okapi_api_session_jwt);
    return cached_okapi_api_session_jwt;
  }


  @Override
  @SneakyThrows
  public SierraUser getSierraUserById(String id) {
    log.debug(String.format("getSierraUserById(%s)",id));
    String api_session_token = getSierraSession();

    if ( api_session_token != null ) {

      // We lookup users by calling /users with query parameters limit, query, sortBy, etc

      String get_user_url = String.format("%s/iii/sierra-api/v6/patrons/%s?fields=%s",baseUrl,id,REQUIRED_USER_FIELDS);
      log.debugf("attempting user lookup %s",get_user_url);
      SimpleHttp.Response response = SimpleHttp.doGet(get_user_url, httpClient)
                     .header("Authorization", "Bearer "+api_session_token)
                     .asResponse();

      if (response.getStatus() == 404) {
        throw new WebApplicationException(response.getStatus());
      }

      log.debugf("Response as string: %s",response.asString());

      SierraUser usr = response.asJson(SierraUser.class);
      if ( usr != null ) {
        // Inject the local system code
        usr.setLocalSystemCode(this.localSystemCode);
        return usr;
      }

      return null;
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

      // First we have to try and find the id of the user with the supplied username via /iii/sierra-api/v6/patrons/query

      String get_user_url = String.format("%s/iii/sierra-api/v6/patrons/query?offset=0&limit=10",baseUrl);

      String user_lookup_json = String.format(USER_LOOKUP_JSON_TEMPLATE, username);
      log.debugf("User lookup query : %s",user_lookup_json);
      StringEntity entity = new StringEntity(user_lookup_json);


      log.debugf("attempting user lookup %s",get_user_url);
      SimpleHttp.Response response = SimpleHttp.doPost(get_user_url, httpClient)
                     .header("Authorization", "Bearer "+api_session_token)
                     .header("Accept", "application/json" )
                     .header("Content-Type", "application/json" )
                     .json(entity)
                     .asResponse();

      if (response.getStatus() == 404) {
        throw new WebApplicationException(response.getStatus());
      }

      log.debugf("Response as string: %s",response.asString());

      SierraSearchResponse search_response = response.asJson(SierraSearchResponse.class);
      if ( ( search_response != null ) && ( search_response.getTotal() == 1 ) ) {
        // Inject the local system code
        SierraUser usr = SimpleHttp.doGet(search_response.getEntries().get(0).getLink()+"&fields="+REQUIRED_USER_FIELDS, httpClient)
                            .header("Authorization", "Bearer "+api_session_token)
                            .header("Accept", "application/json" )
                            .asResponse()
                            .asJson(SierraUser.class);
        usr.setLocalSystemCode(this.localSystemCode);
        return usr;
      }
      else {
        log.warnf("Unable to find user with barcode matching %s",username);
      }

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
                                                 .header("Authorization", "Bearer "+api_session_token)
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
