package com.edulink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.edulink.mapper")
public class EduLinkApplication {
	public static void main(String[] args) {
		SpringApplication.run(EduLinkApplication.class, args);
		System.out.println("========================================");
		System.out.println("  家校协同系统启动成功！");
		System.out.println("  后端地址: http://localhost:8080/api");
		System.out.println("========================================");
	}
}