package com.cy.pj.sys.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class SysUserMenuVo implements Serializable{
	
	private static final long serialVersionUID = -7234863379941761458L;
	
	private Integer id;
	private String name;
	private String url;
	private List<SysUserMenuVo> childs;
}
