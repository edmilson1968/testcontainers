package br.com.itau.demo.testcontainers.redis;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public class LocalstackTest {

    DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:0.12.17");

    @Rule
    public LocalStackContainer localstack =
            new LocalStackContainer(localstackImage)
                    .withServices(S3);

    @Test
    public void shouldUseS3() {
        Assertions.assertThat(localstack.isRunning()).isTrue();
    }

    @Test
    public void shouldPutAndReadOnS3() {
        // AWS SDK v1
        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(localstack.getEndpointConfiguration(S3))
                .withCredentials(localstack.getDefaultCredentialsProvider())
                .build();

        s3.createBucket("foo");
        s3.putObject("foo", "bar", "baz");

        final String s3object = s3.getObjectAsString("foo", "bar");
        Assertions.assertThat(s3object).isNotEmpty();
        Assertions.assertThat(s3object).isEqualTo("baz");
    }
}

//host.testcontainers.internal