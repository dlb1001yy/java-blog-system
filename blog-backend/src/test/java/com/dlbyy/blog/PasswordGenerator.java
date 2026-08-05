	package com.dlbyy.blog;
	import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
	public class PasswordGenerator {
	    public static void main(String[] args) {
	        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	        // 明文密码
	        String rawPassword = "admin123";
	        // 生成哈希
	        String encodedPassword = encoder.encode(rawPassword);
	        System.out.println("==================================================");
	        System.out.println("您的哈希密码是: " + encodedPassword);
	        System.out.println("==================================================");
	    }
	}