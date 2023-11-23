package utils;

import dasniko.testcontainers.keycloak.ExtendableKeycloakContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

public class KeycloakCustomContainer extends ExtendableKeycloakContainer<KeycloakCustomContainer> {

    private final WaitStrategy waitStrategy;
    private static final String DEFAULT_WAIT_LOG_REGEX = ".*KEYCLOAK SETUP FINISHED.*";
    public KeycloakCustomContainer(String dockerImageName, WaitStrategy waitStrategy) {
        super(dockerImageName);
        this.waitStrategy = waitStrategy;
    }

    public KeycloakCustomContainer(String dockerImageName){
        super(dockerImageName);
        this.waitStrategy = Wait.forLogMessage(DEFAULT_WAIT_LOG_REGEX, 1);
    }
    @Override
    protected void configure() {
        super.configure();
        setWaitStrategy(this.waitStrategy);
    }
}
