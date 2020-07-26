package com.cy.pj.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class IpAspect {

	@Pointcut("bean(ipUtils)")
	public void doIps() {}
	
	@Around("doIps()")
	public Object around(ProceedingJoinPoint jp) throws Throwable {
		
			long start = System.currentTimeMillis();			
			Object ip = jp.proceed();
			long end = System.currentTimeMillis();
			System.out.println("ip的查询时间："+(end-start));
			return ip;
		
	
	
	
	}
}
