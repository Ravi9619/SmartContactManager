package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	//generating otp of certain digit
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "normal/forgot-email-form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email")String email,
							HttpSession session)
	{
		System.out.println("EMAIL "+email);
		
		int otp = random.nextInt(99999);
		System.out.println("OTP "+otp);
		
		//write code to send otp
		String subject = "OTP from Contact Manager";
		String message = "<h1> OTP = "+otp+" </h1>";
		String to = email;
		
		
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag)
		{
			return "normal/verify_otp";
		}else {
			session.setAttribute("message", "Check your email id");
			return "normal/forgot-email-form";
		}
		
		
	}
	
}
