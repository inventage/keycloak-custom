import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import utils.KeycloakCustomContainer;

import java.util.Map;
import java.util.Optional;

@Testcontainers
class KeycloakConfigCLITestIT {

    private static final String DOCKER_IMAGE_NAME = "docker-registry.inventage.com:10094/com.inventage.keycloak.custom.container:latest";
    private static final String POSTGRES_DOCKER_IMAGE_NAME = "postgres:13-alpine";
    private static final String POSTGRES_NETWORK_ALIAS = "postgres";
    private static KeycloakCustomContainer keycloak;
    private static PostgreSQLContainer<?> postgres;

    @BeforeAll
    static void beforeAll() {
        Network network = Network.newNetwork();
        final String jdbcUrl = String.format("jdbc:postgresql://%s:5432/test?loggerLevel=OFF", POSTGRES_NETWORK_ALIAS);

        postgres = new PostgreSQLContainer<>(POSTGRES_DOCKER_IMAGE_NAME)
                .withNetwork(network).withNetworkAliases(POSTGRES_NETWORK_ALIAS);
        postgres.start();

        keycloak = new KeycloakCustomContainer(DOCKER_IMAGE_NAME)
                .withNetwork(network)
                .withEnv(Map.of("KC_DB", "postgres",
                        "KC_DB_USERNAME" , postgres.getUsername(),
                        "KC_DB_PASSWORD", postgres.getPassword(),
                        "KC_DB_URL", jdbcUrl,
                        "KC_LOG_LEVEL", "info"));
        keycloak.start();
    }
    @AfterAll
    static void afterAll() {
        keycloak.stop();
        postgres.close();
    }
    @Test
    void test_startup() {
        Assertions.assertTrue(keycloak.isRunning());
    }
    @Test
    void test_import_realm() {
        Keycloak keycloakAdminClient = KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(keycloak.getAdminUsername())
                .password(keycloak.getAdminPassword())
                .build();

        Optional<RealmRepresentation> testRealm = keycloakAdminClient.realms().findAll().stream().filter(realmRepresentation -> realmRepresentation.getRealm().equals("example1")).findFirst();
        Assertions.assertTrue(testRealm.isPresent(), "Realm `testcontainer` should exist. Realm import via keycloak-config-cli failed.");
    }
}
