package com.blog.services;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.blog.services.**.mapper")
public class BlogMainServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogMainServicesApplication.class, args);
	}

}
