package com.k_int.folio.keycloak.provider.external;


import com.fasterxml.jackson.core.type.TypeReference;
import com.k_int.folio.keycloak.provider.FolioProviderConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolioClientSimpleHttp implements FolioClient {

	private static Logger log = LoggerFactory.getLogger(FolioClientSimpleHttp.class);

	private final CloseableHttpClient httpClient;
	private final String baseUrl;
	private final String tenant;
	private final String basicUsername;
	private final String basicPassword;


	public FolioClientSimpleHttp(KeycloakSession session, ComponentModel model) {
		this.httpClient = session.getProvider(HttpClientProvider.class).getHttpClient();
		this.baseUrl = model.get(FolioProviderConstants.BASE_URL);
		this.tenant = model.get(FolioProviderConstants.TENANT);
		this.basicUsername = model.get(FolioProviderConstants.AUTH_USERNAME);
		this.basicPassword = model.get(FolioProviderConstants.AUTH_PASSWORD);
		log.debug(String.format("new FolioClientSimpleHttp base url will be",this.baseUrl));
	}


	@Override
	@SneakyThrows
	public FolioUser getFolioUserById(String id) {

		log.debug(String.format("getFolioUserById(%s)",id));

		String url = String.format("%s/%s", baseUrl, id);
		// SimpleHttp.Response response = SimpleHttp.doGet(url, httpClient).authBasic(basicUsername, basicPassword).asResponse();
		// if (response.getStatus() == 404) {
		// 	throw new WebApplicationException(response.getStatus());
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
}
