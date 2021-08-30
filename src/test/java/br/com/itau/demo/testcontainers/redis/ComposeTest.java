package br.com.itau.demo.testcontainers.redis;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

public class ComposeTest {

    static final int REDIS_PORT = 6379;
    static final int ELASTICSEARCH_PORT = 9200;

    @ClassRule
    public static DockerComposeContainer environment =
            new DockerComposeContainer(new File("src/test/resources/compose-test.yml"))
                    .withExposedService("redis_1", REDIS_PORT)
                    .withExposedService("elasticsearch_1", ELASTICSEARCH_PORT);

    @BeforeClass
    public static void setup() {
    }

    @Test
    public void isRunning() {
        Assertions.assertThat(environment.getContainerByServiceName("redis_1").isPresent()).isTrue();
        Assertions.assertThat(environment.getContainerByServiceName("elasticsearch_1").isPresent()).isTrue();
    }

    @Test
    public void assertRedisUrl() {
        String redisUrl = environment.getServiceHost("redis_1", REDIS_PORT)
                + ":" +
                environment.getServicePort("redis_1", REDIS_PORT);

        Assertions.assertThat(redisUrl).isNotEmpty();
    }

    @Test
    public void assertElasticsearchUrl() {
        String elasticUrl = environment.getServiceHost("elasticsearch_1", ELASTICSEARCH_PORT)
                + ":" +
                environment.getServicePort("elasticsearch_1", ELASTICSEARCH_PORT);

        Assertions.assertThat(elasticUrl).isNotEmpty();
    }
}
