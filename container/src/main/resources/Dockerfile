FROM quay.io/keycloak/keycloak:${keycloak.version}

# copy folders: bin, conf, providers, setup
COPY ./keycloak/ /opt/keycloak/
#
RUN /opt/keycloak/bin/kc.sh build

USER root
# allow timezone configuration, see also https://access.redhat.com/solutions/5616681
RUN microdnf install jq tzdata && ln -sf /usr/share/zoneinfo/Europe/Zurich /etc/localtime && microdnf clean all
# make custom shell scrips executable
RUN chmod -R 755 /opt/keycloak/bin/*.sh

USER 1000
WORKDIR /opt/keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc-with-setup.sh", "start-dev"]