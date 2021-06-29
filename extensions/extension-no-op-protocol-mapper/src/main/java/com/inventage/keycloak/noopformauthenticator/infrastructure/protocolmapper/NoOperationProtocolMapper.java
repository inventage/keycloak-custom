package com.inventage.keycloak.noopformauthenticator.infrastructure.protocolmapper;

import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.List;

public class NoOperationProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final String PROVIDER_ID = "no-operation-protocol-mapper";

    private static final Logger LOG = Logger.getLogger(NoOperationProtocolMapper.class);

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(CONFIG_PROPERTIES, NoOperationProtocolMapper.class);
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "No Operation Protocol Mapper";
    }

    @Override
    public String getHelpText() {
        return "No Operation Protocol Mapper";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        LOG.debugf("setClaim:");
        token.getOtherClaims().put("claimName", "claimValue");
    }

}