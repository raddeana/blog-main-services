package com.blog.services;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.blog.services.**.mapper")
public class BlogMainServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogMainServicesApplication.class, args);
	}

}
