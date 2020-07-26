package com.cy.pj.sys.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cy.pj.common.mail.MailService;

@SpringBootTest
public class MailServiceTest {
	
	@Autowired
	private MailService mailService;
	@Test
	public void mailTest() {
		mailService.sendSimpleMail("119666152@qq.com", "主题:简单邮件", "内容:测试邮件!");
	}
	
}
