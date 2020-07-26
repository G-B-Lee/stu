package com.cy.pj.sys.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class controller {

	@RequestMapping("1111")
	public String doho() {
		return "hello";
	}
}
