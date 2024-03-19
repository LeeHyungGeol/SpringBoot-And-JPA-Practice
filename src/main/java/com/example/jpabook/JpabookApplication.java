package com.example.jpabook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JpabookApplication {

	public static void main(String[] args) {
		Hello hello = new Hello();
		hello.setData("hello");
		System.out.println("hello.getData() = " + hello.getData());

		SpringApplication.run(JpabookApplication.class, args);
	}

}
