package sut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

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
        final String jdbcUrl = String.format("jdbc:postgresql://%s:5432/%s?loggerLevel=OFF", NETWORK_ALIAS_POSTGRES, DATABASE_NAME_POSTGRES);
        keycloak = new KeycloakCustomContainer()
                .withLogConsumer(new Slf4jLogConsumer(LOGGER))
                .withNetwork(network)
                .withEnv(Map.of(
                        "KC_BOOTSTRAP_ADMIN_USERNAME", "admin",
                        "KC_BOOTSTRAP_ADMIN_PASSWORD", "admin",
                        "KC_DB", "postgres",
                        "KC_DB_USERNAME" , postgres.getUsername(),
                        "KC_DB_PASSWORD", postgres.getPassword(),
                        "KC_DB_URL", jdbcUrl,
                        "KC_LOG_LEVEL", "info"));
        try {
            keycloak.start();
            return keycloak;
        } catch (Exception e) {
            System.err.println(keycloak.getLogs());
            throw e;
        }
    }

    public void stop() {
        stopComponents();
    }

    private void stopComponents() {
        stopKeycloak();
        stopPostgres();
    }

    private void stopKeycloak() {
        keycloak.start();
    }

    private void stopPostgres() {
        postgres.stop();
    }

}
