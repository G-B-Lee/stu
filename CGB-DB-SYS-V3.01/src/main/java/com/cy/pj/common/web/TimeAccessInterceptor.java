package com.cy.pj.common.web;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

import com.cy.pj.common.exception.ServiceException;


//设置系统的登录时间
public class TimeAccessInterceptor implements HandlerInterceptor{

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		System.out.println("=============================================");
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY,9);//小时
		c.set(Calendar.MINUTE, 0);//分
		c.set(Calendar.SECOND, 0);//秒
		long start = c.getTimeInMillis();
		c.set(Calendar.HOUR_OF_DAY, 24);
		long end = c.getTimeInMillis();
		long cTime = System.currentTimeMillis();
		if (cTime<start || cTime>end) {
			throw new ServiceException("请在9:00~22:00之间登录");
		}
		return true;
	}
}
