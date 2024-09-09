package sut;

import dasniko.testcontainers.keycloak.ExtendableKeycloakContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import java.time.Duration;

public class KeycloakCustomContainer extends ExtendableKeycloakContainer<KeycloakCustomContainer> {

    public static final String DEFAULT_DOCKER_IMAGE_NAME = "docker-registry.inventage.com:10094/com.inventage.keycloak.custom.container:latest";

    private static final String DEFAULT_WAIT_LOG_REGEX = ".*KEYCLOAK SETUP FINISHED.*";
    private static final long DEFAULT_STARTUP_TIMEOUT = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakCustomContainer.class);

    private final WaitStrategy waitStrategy;
    private final long startupTimeout;


    public KeycloakCustomContainer() {
        this(DEFAULT_DOCKER_IMAGE_NAME);
    }

    public KeycloakCustomContainer(String dockerImageName){
        super(dockerImageName);
        this.startupTimeout = DEFAULT_STARTUP_TIMEOUT;
        this.waitStrategy = new LogMessageWaitStrategy()
                .withRegEx(DEFAULT_WAIT_LOG_REGEX)
                .withTimes(1)
                .withStartupTimeout(Duration.ofMinutes(this.startupTimeout));
        this.withLogConsumer(new Slf4jLogConsumer(LOGGER));
    }

    @Override
    protected void configure() {
        super.configure();
        setWaitStrategy(this.waitStrategy);
    }

}
