package utils;

import dasniko.testcontainers.keycloak.ExtendableKeycloakContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import java.time.Duration;

public class KeycloakCustomContainer extends ExtendableKeycloakContainer<KeycloakCustomContainer> {

    private final WaitStrategy waitStrategy;
    private final long startupTimeout;
    private static final String DEFAULT_WAIT_LOG_REGEX = ".*KEYCLOAK SETUP FINISHED.*";
    private static final long DEFAULT_STARTUP_TIMEOUT = 3;

    public KeycloakCustomContainer(String dockerImageName, WaitStrategy waitStrategy, long startupTimeout) {
        super(dockerImageName);
        this.waitStrategy = waitStrategy;
        this.startupTimeout = startupTimeout;
    }

    public KeycloakCustomContainer(String dockerImageName){
        super(dockerImageName);
        this.startupTimeout = DEFAULT_STARTUP_TIMEOUT;
        this.waitStrategy = new LogMessageWaitStrategy()
                .withRegEx(DEFAULT_WAIT_LOG_REGEX)
                .withTimes(1)
                .withStartupTimeout(Duration.ofMinutes(this.startupTimeout));
    }
    @Override
    protected void configure() {
        super.configure();
        setWaitStrategy(this.waitStrategy);
    }
}
