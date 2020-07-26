package com.cy.pj.sys.service.impl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.tomcat.util.digester.Digester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.cy.pj.common.aspect.ReCache;
import com.cy.pj.common.aspect.RequiredLog;
import com.cy.pj.common.config.PaginationProperties;
import com.cy.pj.common.util.Assert;
import com.cy.pj.common.vo.PageObject;
import com.cy.pj.sys.dao.SysUserDao;
import com.cy.pj.sys.dao.SysUserRoleDao;
import com.cy.pj.sys.entity.SysUser;
import com.cy.pj.sys.service.SysUserService;
import com.cy.pj.sys.vo.SysUserDeptVo;

import io.micrometer.core.instrument.util.StringUtils;
//timeout 事务超时时间, Isolation.READ_COMMITTED 事务的隔离级别,rollbackFor异常回滚
@Transactional(timeout = 30,isolation = Isolation.READ_COMMITTED,rollbackFor = Throwable.class)
@Service
public final class SysUserServiceImpl implements SysUserService {
	@Autowired
	private SysUserDao sysUserDao;
	
	@Autowired
	private SysUserRoleDao sysUserRoleDao;
	@Autowired
	private PaginationProperties paginationProperties;
	
	//修改密码
	@Override
	public void doUpdatPassword(String pwd, String newPwd, String cfgPwd) {
		SysUser user = (SysUser)SecurityUtils.getSubject().getPrincipal();
		Assert.isNull(pwd, "原密码不能为空");
		SimpleHash sh=new SimpleHash(
				"MD5",//algorithmName 加密算法
				pwd,//source 需要加密的密码
				user.getSalt(), //salt 加密盐
				1);//hashIterations 加密次数
		Assert.isArgumentValid(!user.getPassword().equals(sh.toHex()), "原密码不正确");
		Assert.isArgumentValid(!newPwd.equals(cfgPwd), "两次输入的密码不正确");
		Assert.isArgumentValid(newPwd.equals(pwd), "新密码不能与旧密码相同");
		Assert.isArgumentValid(
				!newPwd.toString().matches(
						"^[A-Za-z]+$"),
				"验证由26个英文字母组成的字符串");
		SimpleHash nPwd = new SimpleHash("MD5", newPwd.toString(), user.getSalt(), 1);
		int rows = sysUserDao.updataPassword(user.getUsername(),nPwd.toHex());
		
			
		
	}
	
	
	@Override
	@ReCache
	@RequiresPermissions("sys:user:update")
	@Transactional(readOnly = false)//readOnly = true 表示只读事务
	@Cacheable(value = "userCache")
	public Map<String, Object> findObjectById(Integer userId) {
		//1.参数校验
		Assert.isArgumentValid(userId==null||userId<1,"id值不正确" );
		//2.查询用户以及用户对应的部门信息
		SysUserDeptVo user=sysUserDao.findObjectById(userId);
		Assert.isNull(user, "记录可能已经不存在");
		//3.查询用户对应的角色id
		List<Integer> roleIds=sysUserRoleDao.findRoleIdsByUserId(userId);
		//4.对查询结果进行封装
		Map<String,Object> map=new HashMap<>();
		map.put("user", user);
		map.put("roleIds", roleIds);
		return map;
	}
	
	//清除userCache缓存对象,allEntries = true清楚所有,beforeInvocation = false更新之后清除
	@CacheEvict(value = "userCache",allEntries = true,beforeInvocation = false)
	@RequiredLog(operation = "添加用户")
	@Override
	@RequiresPermissions("sys:user:update")
	public int saveObject(SysUser entity, Integer[] roleIds) {
		//1.参数校验
		Assert.isNull(entity, "保存对象不能为空");
		Assert.isEmpty(entity.getUsername(), "用户名不能为空");
		Assert.isEmpty(entity.getPassword(), "密码不能为空");
		//..
		Assert.isArgumentValid(roleIds==null||roleIds.length==0, "必须为用户分配权限");
		//2.对密码进行加密
		String password=entity.getPassword();
		String salt=UUID.randomUUID().toString();
		//String hashPassword=DigestUtils.md5DigestAsHex((password+salt).getBytes());//spring
		SimpleHash sh=new SimpleHash(
				"MD5",//algorithmName 加密算法
				password,//source 需要加密的密码
				salt, //salt 加密盐
				1);//hashIterations 加密次数
		entity.setPassword(sh.toHex());//将密码加密结果转换为16进制并存储到entity对象
		entity.setSalt(salt);
		//3.保存用户自身信息
		int rows=sysUserDao.insertObject(entity);
		//4.保存用户与角色的关系数据
		sysUserRoleDao.insertObjects(entity.getId(), roleIds);
		//5.返回业务结果
		return rows;
	}
	
	
	@RequiresPermissions("sys:user:update")
	@RequiredLog(operation = "修改用户")
	//清除userCache缓存对象,allEntries = true清楚所有,beforeInvocation = false更新之后清除
	@CacheEvict(value = "userCache",allEntries = true,beforeInvocation = false)
	@Override
	public int updateObject(SysUser entity, Integer[] roleIds) {
		//1.参数校验
		Assert.isNull(entity, "保存对象不能为空");
		Assert.isEmpty(entity.getUsername(), "用户名不能为空");
		//..
		Assert.isArgumentValid(roleIds==null||roleIds.length==0, "必须为用户分配权限");
		//3.更新用户自身信息
		int rows=sysUserDao.updateObject(entity);
		//4.更新用户与角色的关系数据
		sysUserRoleDao.deleteObjectsByUserId(entity.getId());
		sysUserRoleDao.insertObjects(entity.getId(), roleIds);
		//5.返回业务结果
		return rows;
	}
	@RequiresPermissions("sys:user:update")
	@Transactional
	@RequiredLog(operation = "禁用启用")
	@Override
	//清除userCache缓存对象,allEntries = true清楚所有,beforeInvocation = false更新之后清除
	@CacheEvict(value = "userCache",allEntries = true,beforeInvocation = false)
	public int validById(Integer id, Integer valid) {
		//1.参数校验
		Assert.isArgumentValid(id==null||id<1, "id值不正确");
		Assert.isArgumentValid(valid!=1&&valid!=0,"状态值不正确");
		//2.执行更新并校验
		int rows=sysUserDao.validById(id, valid, "admin");//admin后续是登录用户
		Assert.isServiceValid(rows<0, "记录可能已经不存在");
		//3.返回结果
		return rows;
	}
	/**
	 * readOnly = true 表示只读事务,但在这个方法中会报错,因为查询和写入日志是同一个事务,写日志有写的动作,
	 * 写日志的默认事务配置会让写日志的事务顺用查询的事务,从而导致因为readOnly事务特性而报错.
	 * 
	 * 事务默认配置:当method1调用method2,method1上没有事务,会沿用method2上的事务,method1上有事务,
	 * method2顺用method1的事务.@Transactional(propagation = Propagation.REQUIRED).
	 * 
		方案1:在写日志saveObject方法上加@Transactional(propagation = Propagation.REQUIRES_NEW),
		让他始终运行在一个新事务中,不让他的业务影响其他的事务特性.
		方案2:提高写日志的事务等级.
	 */
	@Transactional(readOnly = true)
	@RequiredLog(operation = "用户分页查询")
	@Override
	@Cacheable(value = "userCache")//添加缓存userCache对象
	@RequiresPermissions("sys:user:update")
	public PageObject<SysUserDeptVo> findPageObjects(String username, Integer pageCurrent) {
		System.out.println("在数据库中取----->");
		System.out.println("user.find.thread"+Thread.currentThread());
		//1.参数校验
		Assert.isArgumentValid(pageCurrent==null||pageCurrent<1, "当前页码值不正确");
		//2.查询总记录数并校验
		int rowCount=sysUserDao.getRowCount(username);
		Assert.isServiceValid(rowCount<=0, "没有对应的记录");
		//3.查询当前页用户记录信息
		Integer pageSize=paginationProperties.getPageSize();
		Integer startIndex=paginationProperties.getStartIndex(pageCurrent);
		List<SysUserDeptVo> records=
		sysUserDao.findPageObjects(username, startIndex, pageSize);
		//4.封装结果并返回
		return new PageObject<>(rowCount, records, pageSize, pageCurrent);
	}



	

}








