package sut;

import dasniko.testcontainers.keycloak.ExtendableKeycloakContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import java.time.Duration;

public class KeycloakCustomContainer extends ExtendableKeycloakContainer<KeycloakCustomContainer> {

    public static final String DEFAULT_DOCKER_IMAGE_NAME = "ghcr.io/inventage/keycloak-custom/com.inventage.keycloak.custom.container:latest";

    private static final String DEFAULT_WAIT_LOG_REGEX = ".*KEYCLOAK SETUP FINISHED.*";
    private static final long DEFAULT_STARTUP_TIMEOUT = 3;

    private final WaitStrategy waitStrategy;
    private final long startupTimeout;

    public KeycloakCustomContainer() {
        this(DEFAULT_DOCKER_IMAGE_NAME);
    }

    public KeycloakCustomContainer(String dockerImageName) {
        super(dockerImageName); // includes withLogConsumer(new Slf4jLogConsumer(...))
        this.startupTimeout = DEFAULT_STARTUP_TIMEOUT;
        this.waitStrategy = new LogMessageWaitStrategy()
                .withRegEx(DEFAULT_WAIT_LOG_REGEX)
                .withTimes(1)
                .withStartupTimeout(Duration.ofMinutes(this.startupTimeout));
        this.withProductionMode(); // kc.sh start instead of start-dev
        this.withOptimizedFlag(); // --optimized
    }

    @Override
    protected void configure() {
        super.configure();
        setWaitStrategy(this.waitStrategy);
    }

}
