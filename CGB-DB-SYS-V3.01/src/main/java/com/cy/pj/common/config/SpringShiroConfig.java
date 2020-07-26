package com.cy.pj.common.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionContext;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringShiroConfig {

//-----------------------------------------------------------------------------------------
	//此对象是shiro框架的核心，由此对象完成认证、授权等功能
	//缓存管理器注入
	//Realm：
	@Bean
	public SecurityManager securityManager(Realm realm,
			CacheManager cacheManager,
			RememberMeManager rememberMeManager,
			SessionManager sessionManager
			) {
		DefaultWebSecurityManager sManager=new DefaultWebSecurityManager();
		sManager.setRealm(realm);
		sManager.setCacheManager(cacheManager);
		sManager.setRememberMeManager(rememberMeManager);
		sManager.setSessionManager(sessionManager);
		return sManager;
	
	}
//-----------------------------------------------------------------------------------------
	
	//初始化过滤器工厂的bean对象（底层完成对过滤器工厂的创建，然后通过工厂创建过滤器）
	@Bean
    public ShiroFilterFactoryBean shiroFilterFactory(
    		SecurityManager securityManager) {//如何查找实参(方法上默认有@Autowired注解)
    	//1.构建工厂bean对象(FactoryBean规范由spring定义，规范的实现在当前模块由shiro框架实现，例如ShiroFilterFactoryBean)
    	ShiroFilterFactoryBean fBean=new ShiroFilterFactoryBean();
    	//2.为fBean对象注入SecurityManager对象
    	fBean.setSecurityManager(securityManager);
    	//3.设置登录url(没有经过认证的请求，需要跳转到这个路径对应的页面)
    	fBean.setLoginUrl("/doLoginUI");
    	//4.设置请求过滤规则?(例如哪些请求要进行认证检测，哪些请求不需要)
    	Map<String,String> filterChainDefinitionMap=new LinkedHashMap<>();
    	 //静态资源允许匿名访问:"anon"(Shiro框架指定这些常亮值)//官网http://shiro.apache.org/web.html
    	filterChainDefinitionMap.put("/bower_components/**","anon");
    	filterChainDefinitionMap.put("/build/**","anon");
    	filterChainDefinitionMap.put("/dist/**","anon");
    	filterChainDefinitionMap.put("/plugins/**","anon");
    	filterChainDefinitionMap.put("/user/doLogin","anon");
    	filterChainDefinitionMap.put("/doLogout","logout");//触发此url时系统地层会清session，然后跳转到LoginUrl
		 //除了匿名访问的资源,其它都要认证("authc")后访问，这里要记住，需认证访问的资源，要写在下面
    	//filterChainDefinitionMap.put("/**","authc");
    	filterChainDefinitionMap.put("/**","user");//记住我功能实现时，认证规则需要修改。
    	fBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
		return fBean;
	}
//-----------------------------------------------------------------------------------------
	//配置授权属性资源管理器，配置完还需将对象注入给securityManager对象
	//配置advisor对象,shiro框架底层会通过此对象的matchs方法返回值(类似切入点)决定是否创建代理对象,进行权限控制。
	@Bean
	public AuthorizationAttributeSourceAdvisor newAuthorizationAttributeSourceAdvisor(
			SecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
			advisor.setSecurityManager(securityManager);
		return advisor;
	}
	
//-----------------------------------------------------------------------------------------
	//1.配置shiro中的缓存管理器，来对授权信息缓存
	//2.配置完还需将对象注入给securityManager对象
	@Bean
	public CacheManager shiroCacheManager() {
		MemoryConstrainedCacheManager cManager = new MemoryConstrainedCacheManager();
		return cManager;
	}
//-----------------------------------------------------------------------------------------	
	//记住我功能：cookieManager配置,配置完还需将对象注入给securityManager对象
	@Bean
	public RememberMeManager rememberMeManager() {
		CookieRememberMeManager rememberMeManager=new CookieRememberMeManager();
		SimpleCookie cookie=new SimpleCookie("rememberMe");
		cookie.setMaxAge(7*24*60*60);
		rememberMeManager.setCookie(cookie);
		return rememberMeManager;
	}
//-----------------------------------------------------------------------------------------	
	//会话管理器，设置session超时为1小时，配置完还需将对象注入给securityManager对象
	@Bean	
	public SessionManager sessionManager() {
			DefaultWebSessionManager sManager = new DefaultWebSessionManager();
			sManager.setGlobalSessionTimeout(60*60*1000);//1个小时
			return sManager;
		}
//-----------------------------------------------------------------------------------------	
}

























