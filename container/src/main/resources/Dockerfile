FROM quay.io/keycloak/keycloak:${keycloak.version} as builder

# build stage configuration is located in keycloak.conf
COPY ./keycloak/conf/keycloak.conf /opt/keycloak/conf/
# providers
COPY ./keycloak/providers/*.jar /opt/keycloak/providers/
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:${keycloak.version}
COPY --from=builder /opt/keycloak/lib/quarkus/ /opt/keycloak/lib/quarkus/
COPY ./keycloak/ /opt/keycloak/
COPY ./keycloak/providers/*.jar /opt/keycloak/providers/
WORKDIR /opt/keycloak

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start"]