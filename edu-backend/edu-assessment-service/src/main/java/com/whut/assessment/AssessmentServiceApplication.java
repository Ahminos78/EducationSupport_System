package com.whut.assessment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.whut.assessment.mapper")
@SpringBootApplication(scanBasePackages = "com.whut")
public class AssessmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssessmentServiceApplication.class, args);
    }
}
