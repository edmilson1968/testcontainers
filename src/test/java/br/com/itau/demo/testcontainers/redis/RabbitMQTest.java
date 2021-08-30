package br.com.itau.demo.testcontainers.redis;

import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.RabbitMQContainer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class RabbitMQTest {

    @ClassRule
    public static RabbitMQContainer rabbitMQContainer =
            new RabbitMQContainer("rabbitmq:3.9.5-alpine")
                    .withExposedPorts(5672);

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getFirstMappedPort);
        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private Queue helloWorldQueue;

    CountDownLatch latch = new CountDownLatch(1);

    String message;

    @Test
    public void isRunning() {
        Assertions.assertThat(rabbitMQContainer.isRunning()).isTrue();
    }

    @Test
    public void sendAndReceive() throws InterruptedException {
        message = "";
        rabbitTemplate.convertAndSend(helloWorldQueue.getName(), "Hello, world!");
        latch.await(3, TimeUnit.SECONDS);
        Assertions.assertThat(latch.getCount()).isEqualTo(0);
        Assertions.assertThat(message).isEqualTo("Hello, world!");
    }

    @RabbitListener(queues = "myQueue")
    public void listen(String in) {
        message = in;
        latch.countDown();
    }
}

@Configuration
class RabbitMQConfig {

    @Bean
    public Queue myQueue() {
        return new Queue("myQueue", false);
    }
}