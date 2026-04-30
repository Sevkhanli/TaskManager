package az.edu.itbrains;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})
public class TaskManagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(TaskManagerApplication.class, args);
	}
}

