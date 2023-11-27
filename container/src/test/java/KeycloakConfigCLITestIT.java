import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import utils.KeycloakCustomContainer;

import java.util.Map;
import java.util.Optional;

@Testcontainers
class KeycloakConfigCLITestIT {

    private static final String DOCKER_IMAGE_NAME = "docker-registry.inventage.com:10094/com.inventage.keycloak.custom.container:latest";
    private final Map<String, String> envMap = Map.of("KC_DB", "dev-mem", "KC_LOG_LEVEL", "info");
    @Container
    private final KeycloakCustomContainer keycloak = new KeycloakCustomContainer(DOCKER_IMAGE_NAME)
            .withEnv(envMap);
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
