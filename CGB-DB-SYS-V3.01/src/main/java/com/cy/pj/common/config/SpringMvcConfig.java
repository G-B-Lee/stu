package com.cy.pj.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cy.pj.common.web.TimeAccessInterceptor;

@Configuration
public class SpringMvcConfig implements WebMvcConfigurer{

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(
				new TimeAccessInterceptor()).addPathPatterns("/user/doLogin");
	}
}
