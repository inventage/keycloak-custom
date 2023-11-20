## base image is keycloak, which is based on ubi8/ubi-minimal
## see https://www.redhat.com/en/blog/introducing-red-hat-universal-base-image
#FROM quay.io/keycloak/keycloak:${keycloak.version} as builder
#
## build stage configuration is located in keycloak.conf
#COPY ./keycloak/conf/keycloak.conf /opt/keycloak/conf/
## providers
#COPY ./keycloak/providers/*.jar /opt/keycloak/providers/
#
#RUN /opt/keycloak/bin/kc.sh build

FROM registry.access.redhat.com/ubi9 AS ubi-micro-build
RUN mkdir -p /mnt/rootfs
RUN dnf install --installroot /mnt/rootfs jq tzdata --releasever 9 --setopt install_weak_deps=false --nodocs -y; dnf --installroot /mnt/rootfs clean all

FROM quay.io/keycloak/keycloak:${keycloak.version}
COPY --from=ubi-micro-build /mnt/rootfs /

# copy from previous stage
#COPY --from=builder /opt/keycloak/lib/quarkus/ /opt/keycloak/lib/quarkus/
# copy folders: bin, conf, providers, setup
COPY ./keycloak/ /opt/keycloak/
RUN /opt/keycloak/bin/kc.sh build

USER root
# allow timezone configuration, see also https://access.redhat.com/solutions/5616681
RUN ln -sf /usr/share/zoneinfo/Europe/Zurich /etc/localtime
RUN chmod -R g+rwx /opt/keycloak

USER 1000
WORKDIR /opt/keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc-with-setup.sh"]
CMD start
