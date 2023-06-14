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
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import java.util.Collections;
import java.util.Map;


/**
 * Implement the sierra api.
 * Here be dragons. Keycloak demands lowercase usernames. Sierra patron validation is case sensitve. fortunately
 * sierra user lookup is case insensitve so we can look up a user record by a lower case uniqueId (The field kc-towers atleast seems
 * to use for username) and the returned record contains the uniqueId in the uniqueIds field. We can then use that when
 * we submit to the validate endpoint. So the flow is
 *
 * Keycloak.login(Lowercase username)
 * Provider - lookup sierra user using that username
 * Provider - validate using the uniqueId we got back from the user lookup rather than the username field.
 */
public class SierraClientSimpleHttp implements SierraClient {

  private static final Logger log = Logger.getLogger(SierraClientSimpleHttp.class);

  private final CloseableHttpClient httpClient;
  private final String baseUrl;
  private final String client_key;
  private final String secret;
  private final String localSystemCode;
  private final String authMode;

  // private final Map<String, SierraUser> userLookupCache = new java.util.HashMap<String, SierraUser>();
  // private final LRUMap<String, SierraUser> userLookupCache = new LRUMap<String, SierraUser>(200);
  // Wrap a LRU map with the passive expiry decorator
  private static final long CACHE_TTL_MS = 1000 * 60 * 60; // Max cache TTL = 1h
  private final Map<String, SierraUser> userLookupCache = Collections.synchronizedMap(new PassiveExpiringMap<String,SierraUser>(CACHE_TTL_MS,new LRUMap<String, SierraUser>(200)));

  // Suggestions for a better way to do this are MOST welcome
  private static final String REQUIRED_USER_FIELDS = "id,updatedDate,createdDate,expirationDate,names,barcodes,patronType,patronCodes,homeLibraryCode,emails,message,blockInfo,autoBlockInfo,uniqueIds";

        // Not final - we expect that the jwt may become invalidated at some point and will need to be refreshed. TBC
  private String cached_okapi_api_session_jwt;


  public SierraClientSimpleHttp(KeycloakSession session, ComponentModel model) {

    this.httpClient = session.getProvider(HttpClientProvider.class).getHttpClient();

    this.baseUrl = model.get(SierraProviderConstants.BASE_URL);
    this.client_key = model.get(SierraProviderConstants.CLIENT_KEY);
    this.secret = model.get(SierraProviderConstants.SECRET);
    this.localSystemCode = model.get(SierraProviderConstants.LOCAL_SYSTEM_CODE);
    this.authMode = model.get(SierraProviderConstants.AUTH_MODE);

    log.debug(String.format("%s = %s",SierraProviderConstants.BASE_URL,this.baseUrl));
    log.debug(String.format("%s = %s",SierraProviderConstants.CLIENT_KEY,this.client_key));
    log.debug(String.format("%s = %s",SierraProviderConstants.SECRET,this.secret));
    log.debug(String.format("%s = %s",SierraProviderConstants.LOCAL_SYSTEM_CODE,this.localSystemCode));
    log.debug(String.format("%s = %s",SierraProviderConstants.AUTH_MODE,this.authMode));
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
                                             .header("Accept", "application/json" )
                                             .header("Authorization", "Basic "+encoded_auth)
                                             .json(new StringEntity(""))
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

    if ( userLookupCache.containsKey(id) ) {
      log.debug("Cache hit");
      return userLookupCache.get(id);
    }

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

        // Stash this user in the cache so we don't hit sierra more than we need to
        userLookupCache.put(id, usr);
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
    return getSierraUserByX(username,"u");
  }

  @Override
  @SneakyThrows
  public SierraUser getSierraUserByBarcode(String username) {
    return getSierraUserByX(username,"b");
  }

  @SneakyThrows
  public SierraUser getSierraUserByX(String username, String idtype) {
    log.debug(String.format("getSierraUserByX(%s,%s)",username,idtype));
  
    String api_session_token = getSierraSession();

    if ( api_session_token != null ) {

      // First we have to try and find the id of the user with the supplied username via /iii/sierra-api/v6/patrons/query

      String get_user_url = String.format("%s/iii/sierra-api/v6/patrons/query?offset=0&limit=10",baseUrl);

      // SimpleHttp expects the request json to be in a Java object form it can marshal into JSON. /sigh.
      SierraSearchRequest patron_search_req = new SierraSearchRequest("patron",idtype,username);

      // log.debug(org.keycloak.util.JsonSerialization.writeValueAsString(patron_search_req));

      log.debugf("attempting user lookup %s",get_user_url);
      // BEWARE .param does not do what you think it should - sourcecode here: http://www.java2s.com/example/java-src/pkg/org/keycloak/broker/provider/util/simplehttp-16d96.html
      // if any params are set SimpleHttp will add the parameters and return a UrlEncodedFormEntity which is not what we want in this case.
      // So we fall back to encoding params in the URL above, and then using .json to encode a POJO as Json ()
      SimpleHttp.Response response = SimpleHttp.doPost(get_user_url, httpClient)
                     .connectionRequestTimeoutMillis(1000*30)
                     .connectTimeoutMillis(1000*30)
                     .header("Authorization", "Bearer "+api_session_token)
                     .header("Accept", "application/json" )
                     .header("Content-Type", "application/json" )
                     .json(patron_search_req)
                     .asResponse();

      if (response.getStatus() == 404) {
        throw new WebApplicationException(response.getStatus());
      }

      // log.debugf("Response as string: %s",response.asString());
      SierraSearchResponse search_response = response.asJson(SierraSearchResponse.class);
      log.debugf("Decoded search response %s",search_response.toString());

      if ( ( search_response != null ) && ( search_response.getTotal() == 1 ) ) {
        log.debugf("Search found a single record - attempting to retrieve %s",search_response.getEntries().get(0).getLink());

        SimpleHttp.Response rsp = SimpleHttp.doGet(search_response.getEntries().get(0).getLink()+"?fields="+REQUIRED_USER_FIELDS, httpClient)
                            .header("Authorization", "Bearer "+api_session_token)
                            .header("Accept", "application/json" )
                            .asResponse();

        // Inject the local system code
        SierraUser usr = rsp.asJson(SierraUser.class);
        usr.setLocalSystemCode(this.localSystemCode);

        log.debugf("Got user: %s",usr.toString());
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
    if ( ( this.authMode == null ) || ( this.authMode.equals("PIN") ) ) {
      log.debug("Authmode is PIN");
      return isValidByPin(barcode,pin);
    }
    else {
      log.debug("Authmode is NAME");
      return isValidByName(barcode,pin);
    }
  }

  public boolean isValidByPin(String barcode, String pin) throws java.io.UnsupportedEncodingException, java.io.IOException {

    boolean result = false;

    log.debugf("isValid(..%s,%s)",barcode,pin);
    String api_session_token = getSierraSession();
    String login_url = this.baseUrl + "/iii/sierra-api/v6/patrons/validate";

    SierraPatronAuthenticationRequest req = SierraPatronAuthenticationRequest.builder()
                                                  .barcode(barcode)
                                                  .pin(pin)
                                                  .build();

    SimpleHttp.Response response = SimpleHttp.doPost(login_url, httpClient)
                                                 .header("Authorization", "Bearer "+api_session_token)
                                                 .header("Accept", "application/json" )
                                                 .header("Content-Type", "application/json" )
                                                 .json(req)
                                                 .asResponse();

    if ( response.getStatus() == 204 ) {
      result = true;
    }

    log.debugf("isValid(%s,...) returning "+result,barcode);

    return result;
  }

  public boolean isValidByName(String barcode, String pin) throws java.io.UnsupportedEncodingException, java.io.IOException {

    boolean result = false;

    log.debugf("isValid(..%s,%s)",barcode,pin);
    String api_session_token = getSierraSession();
    String login_url = this.baseUrl + "/iii/sierra-api/v6/patrons/validate";

    SierraUser su = getSierraUserByBarcode(barcode);
    if ( su != null ) {
      if ( su.getNames() != null ) {
        log.debugf("Got user record... testing name against %s for %s",pin,su.getNames().toString());
        for ( String s : su.getNames() ) {
          log.debugf("Testing %s",s);
          if ( ( s.length() > 3 ) && ( s.toLowerCase().startsWith(pin.toLowerCase() ) ) ) {
            log.debugf("Pin %s matches name %s",pin,s);
            result=true;
          }
        }
      }
      else {
        log.debugf("Names array was null for user %s",barcode);
      }
    }
    else {
      log.warn("No user record for barcode "+barcode);
    }

    log.debugf("isValid(%s,...) returning "+result,barcode);

    return result;
  }


}
