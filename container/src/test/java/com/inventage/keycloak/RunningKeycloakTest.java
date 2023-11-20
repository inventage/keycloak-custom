package com.inventage.keycloak;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@Testcontainers
public class RunningKeycloakTest {

//    @Container
//    private KeycloakContainer keycloak;
//
//    public RunningKeycloakTest() {
//        keycloak = new KeycloakContainer("docker-registry.inventage.com:10094/com.inventage.keycloak.custom.container:latest");
//        keycloak.setEnv(List.of("KC_DB=dev-mem"));
//        keycloak.setCommand("start");
//    }
//
//    @Test
//    public void testKC(){
//        Keycloak keycloakAdminClient = KeycloakBuilder.builder()
//                .serverUrl(keycloak.getAuthServerUrl())
//                .realm("master")
//                .clientId("admin-cli")
//                .username(keycloak.getAdminUsername())
//                .password(keycloak.getAdminPassword())
//                .build();
//
//        List<RealmRepresentation> all = keycloakAdminClient.realms().findAll();
//        keycloakAdminClient.close();
//    }
}
