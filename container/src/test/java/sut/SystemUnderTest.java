package sut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.HashMap;
import java.util.Map;

public class SystemUnderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemUnderTest.class);
    private static final String DOCKER_IMAGE_NAME_POSTGRES = "postgres:16-alpine";
    private static final String NETWORK_ALIAS_POSTGRES = "postgres";
    private static final String DATABASE_NAME_POSTGRES = "postgres";
    private String runningKeycloakBaseUrl;

    private Network network;

    public PostgreSQLContainer postgres;
    public KeycloakCustomContainer keycloak;

    public SystemUnderTest(String runningKeycloakBaseUrl) {
        this.runningKeycloakBaseUrl = runningKeycloakBaseUrl;
    }

    private SystemUnderTest(Network network) {
        this.network = network;
    }

    public String getBaseUrl() {
        return runningKeycloakBaseUrl != null ? runningKeycloakBaseUrl : keycloak.getAuthServerUrl();
    }

    public static SystemUnderTest start() {
        final SystemUnderTest sut = new SystemUnderTest(Network.newNetwork());
        sut.startComponents();
        return sut;
    }

    private void startComponents() {
        startPostgres(network);
        startKeycloak(postgres);
    }

    private PostgreSQLContainer startPostgres(Network network) {
        postgres = new PostgreSQLContainer<>(DOCKER_IMAGE_NAME_POSTGRES)
                .withLogConsumer(new Slf4jLogConsumer(LOGGER))
                .withNetwork(network)
                .withNetworkAliases(NETWORK_ALIAS_POSTGRES)
                .withDatabaseName(DATABASE_NAME_POSTGRES)
                .withUsername("postgres")
                .withPassword("postgres");
        try {
            postgres.start();
            return postgres;
        } catch (Exception e) {
            System.err.println(postgres.getLogs());
            throw e;
        }
    }

    private KeycloakCustomContainer startKeycloak(PostgreSQLContainer postgres) {
        keycloak = new KeycloakCustomContainer()
                .withNetwork(network)
                .withEnv(getKeycloakEnvs());
        try {
            keycloak.start();
            return keycloak;
        } catch (Exception e) {
            System.err.println(keycloak.getLogs());
            throw e;
        }
    }

    private Map<String, String> getKeycloakEnvs() {
        HashMap<String, String> envs = new HashMap<>();
        envs.put("PRINT_ENV", "true");
        envs.put("KC_HTTP_ENABLED", "true");
        envs.put("KC_HOSTNAME_STRICT", "false");
        envs.put("KC_HOSTNAME_STRICT_HTTPS", "false");
        envs.put("REALM_MASTER_SSL_REQUIRED", "none");
        envs.put("KC_BOOTSTRAP_ADMIN_CLIENT_ID", "temp-admin");
        envs.put("KC_BOOTSTRAP_ADMIN_CLIENT_SECRET", "admin");
        envs.put("KEYCLOAK_CONFIG_CLI_CLIENT_ID", "keycloak-config-cli");
        envs.put("KEYCLOAK_CONFIG_CLI_CLIENT_SECRET", "keycloak-config-cli");
        envs.put("KEYCLOAK_GRANTTYPE", "client_credentials");
        envs.put("KEYCLOAK_CLIENTID", "${KC_BOOTSTRAP_ADMIN_CLIENT_ID}");
        envs.put("KEYCLOAK_CLIENTSECRET", "${KC_BOOTSTRAP_ADMIN_CLIENT_SECRET}");
        envs.put("KC_DB_USERNAME", postgres.getUsername());
        envs.put("KC_DB_PASSWORD", postgres.getPassword());
        envs.put("KC_DB_URL", String.format("jdbc:postgresql://%s:5432/%s?loggerLevel=OFF", NETWORK_ALIAS_POSTGRES, DATABASE_NAME_POSTGRES));
        envs.put("KC_LOG_LEVEL", "info");
        return envs;
    }

    public void stop() {
        stopComponents();
    }

    private void stopComponents() {
        stopKeycloak();
        stopPostgres();
    }

    private void stopKeycloak() {
        keycloak.stop();
    }

    private void stopPostgres() {
        postgres.stop();
    }

}
