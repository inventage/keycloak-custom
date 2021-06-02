FROM ${docker.pull.registry}${jboss.base.image}
MAINTAINER Edwin Steiner <esteiner@inventage.com>

# Enables signals getting passed from startup script to JVM
# ensuring clean shutdown when container is stopped.
ENV LAUNCH_JBOSS_IN_BACKGROUND 1
ENV PROXY_ADDRESS_FORWARDING false
ENV JBOSS_HOME /opt/jboss/keycloak
ENV KEYCLOAK_HOME $JBOSS_HOME

USER root
RUN cp /usr/share/zoneinfo/Europe/Zurich /etc/localtime && echo "Europe/Zurich" > /etc/timezone
RUN yum update -y && yum install -y epel-release git && yum install -y jq openssl which && yum clean all

USER jboss
ADD ${artifactId}-${project.version}-all.tar $JBOSS_HOME/
ADD entrypoint.sh $JBOSS_HOME/
ADD wait-for-it.sh $JBOSS_HOME/bin/

# https://docs.openshift.com/container-platform/3.11/creating_images/guidelines.html
USER root
RUN chown -R jboss:root $JBOSS_HOME
RUN chmod -R 755 $JBOSS_HOME/bin
RUN chmod +x $JBOSS_HOME/entrypoint.sh
RUN chmod -R g=u $JBOSS_HOME

USER jboss
WORKDIR $JBOSS_HOME/bin

EXPOSE 8080
EXPOSE 8443
ENTRYPOINT [ "/opt/jboss/keycloak/entrypoint.sh" ]
