package spring.ajax.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages="spring.ajax.demo")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
