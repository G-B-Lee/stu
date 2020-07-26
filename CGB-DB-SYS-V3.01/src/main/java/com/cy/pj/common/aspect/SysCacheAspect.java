package com.cy.pj.common.aspect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@Component
public class SysCacheAspect {

	Map<String, Object> cache = new ConcurrentHashMap<>();
	
//	@Pointcut("bean(sysDeptServiceImpl)")
//	public void doCache() {}
	
	@Around("@annotation(com.cy.pj.common.aspect.ReCache)")
	public Object doCacheObject(ProceedingJoinPoint jp) throws Throwable {
		Object value = cache.get("deptCahe");
		if(value!=null) {
			System.out.println("输出缓存内容----->");
			return value;
		}
		Object obj = jp.proceed();
		cache.put("deptCahe", obj);
		System.out.println("输出数据库内容----->");
		return obj;
	}
	
	@AfterReturning("@annotation(com.cy.pj.common.aspect.UpDateCache)")
	public void upDateCache() {
		cache.clear();
	}
}






