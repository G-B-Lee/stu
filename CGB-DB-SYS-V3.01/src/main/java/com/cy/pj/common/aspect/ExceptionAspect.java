package com.cy.pj.common.aspect;



import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import com.cy.pj.common.mail.MailService;

import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class ExceptionAspect {
	
	@Value("${email}")
	private String email;
	
	@Autowired
	private MailService mailService;
	
	@AfterThrowing(pointcut = "bean(sysLogServiceImpl)",throwing = "e")
	public void doHandleException(JoinPoint jp,Exception e) {
		MethodSignature ms = (MethodSignature) jp.getSignature();
		log.info("{} method exception is {}",ms.getName(),e.getMessage());
		//mailService.sendSimpleMail(email, ms.getName()+e.getMessage(),e.getMessage() );
	}
}
















