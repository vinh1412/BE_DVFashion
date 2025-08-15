package vn.edu.iuh.fit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enable scheduling for periodic tasks
public class DvFashionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DvFashionApplication.class, args);
    }

}
