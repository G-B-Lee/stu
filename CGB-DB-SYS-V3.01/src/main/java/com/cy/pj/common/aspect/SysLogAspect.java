package com.cy.pj.common.aspect;

import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Date;


import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cy.pj.common.util.IpUtils;
import com.cy.pj.sys.entity.SysLog;
import com.cy.pj.sys.entity.SysUser;
import com.cy.pj.sys.service.SysLogService;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class SysLogAspect {
	@Pointcut("bean(sysUserServiceImpl)")
	public void logPointCut() {}
	
	@Around("logPointCut()")
	public Object around(ProceedingJoinPoint jp) throws Throwable {
		try {
			Long start = System.currentTimeMillis();
			log.info("start:"+start);
			Object result = jp.proceed();
			Long end = System.currentTimeMillis();
			log.info("end:"+end);
			saveObject(jp,end-start);
			return result;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	@Autowired
	private SysLogService sysLogService;
	
	@Autowired
	private IpUtils ipUtils;
	  
	private void saveObject(ProceedingJoinPoint jp,Long time) throws SocketException, Exception {
		//1.获取用户行为日志
		Class<? extends Object> targetClass = jp.getTarget().getClass();
		String className = targetClass.getName();
		//1.1获取用户操作(获取注解上的名称)
		//获取目标方法
		MethodSignature ms = (MethodSignature) jp.getSignature();
		
		//获取目标方法
		Method targetClassMethod = targetClass.getMethod(ms.getName(), ms.getParameterTypes());
		//获取方法上的注解
		RequiredLog requiredLog = targetClassMethod.getAnnotation(RequiredLog.class);
		String operation = "operation";
		if(requiredLog!=null) {
			operation = requiredLog.operation();
		}
		
		
		SysUser user = (SysUser)SecurityUtils.getSubject().getPrincipal();
		//2.封装日志
		SysLog entity = new SysLog();
		entity.setUsername(user.getUsername())
		.setIp(ipUtils.getRealIp())
		.setMethod(className+ms.getName())
		.setOperation(operation)
		//获取目标实际方法的参数
		.setParams(Arrays.toString(jp.getArgs()))
		.setTime(time)
		.setCreatedTime(new Date());
		
//		new Thread() {
//			@Override
//			public void run() {
//				
//				sysLogService.insertObject(entity);
//			}
//		}.start();
		sysLogService.insertObject(entity);
	}
	
}
