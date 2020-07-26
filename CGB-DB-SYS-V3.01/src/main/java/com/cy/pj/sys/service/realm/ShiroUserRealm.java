package com.cy.pj.sys.service.realm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cy.pj.sys.dao.SysMenuDao;
import com.cy.pj.sys.dao.SysRoleMenuDao;
import com.cy.pj.sys.dao.SysUserDao;
import com.cy.pj.sys.dao.SysUserRoleDao;
import com.cy.pj.sys.entity.SysUser;

import io.micrometer.core.instrument.util.StringUtils;

@Service
public class ShiroUserRealm extends AuthorizingRealm{

	@Autowired
	private SysUserDao sysUserDao;
	@Autowired
	private SysUserRoleDao sysUserRoleDao;
	@Autowired
	private SysRoleMenuDao sysRoleMenuDao;
	@Autowired
	private SysMenuDao sysMenuDao;
	
//--------------------------------------------------------------------------------------	
	
//第二步：指定加密方法	
	@Override
	public void setCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
		
		//1、设置加密算法
		HashedCredentialsMatcher cMatcher = new HashedCredentialsMatcher("MD5");
		//2、设置加密次数
		cMatcher.setHashIterations(1);
		super.setCredentialsMatcher(cMatcher);
	}
	
//--------------------------------------------------------------------------------------
//第一步：获取用户信息
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		//把token转成usernamePasswordToken的具体类型
		UsernamePasswordToken uToken = (UsernamePasswordToken)token;
		//获取到username
		String username = uToken.getUsername();
		//通过username查询到user对象
		SysUser user = sysUserDao.findUserByUserName(username);
		//判断用户名是否存在及用户名是否被禁用
		
		if (user==null)
			throw new UnknownAccountException();
		if (user.getValid()==0) {
			throw new LockedAccountException();
		}	
		
		//盐值需要指定对象进行转换
		ByteSource credentialsSalt = ByteSource.Util.bytes(user.getSalt());
		//对查询到的用户信息进行封装返回
		SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(
				user, //用户身份，结合业务自身情况赋予具体对象
				user.getPassword(),//已加密的密码
				credentialsSalt,//加密时使用的盐值
				getName());//命名：可以是类名
		
		return info;
	}
//--------------------------------------------------------------------------------------
	//基于此方法进行授权信息的获取和封装，然后将封装好的信息传递给securityManager对象，由此对象进行权限检测与授权
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		//获取登录用户的信息，例如用户id
		SysUser user = (SysUser)principals.getPrimaryPrincipal();
		//基于用户id查找角色id
		List<Integer> roleIds = sysUserRoleDao.findRoleIdsByUserId(user.getId());
		if (roleIds==null || roleIds.size()==0) {
			throw new AuthorizationException();
		}
		//List转数组
		Integer[] array = {};
		//基于角色id查找菜单id
		List<Integer> menuIds = sysRoleMenuDao.findMenuIdsByRoleIds(roleIds.toArray(array));
		if (menuIds==null || menuIds.size()==0) {
			throw new AuthorizationException();
		}
		//基于菜单id查找对应的权限并将添加到set
		List<String> findPermission = sysMenuDao.findPermission(menuIds.toArray(array));
		Set<String> Permissions = new HashSet<>();
		for (String per : findPermission) {
			if (!StringUtils.isEmpty(per)) {
				Permissions.add(per);
			}
		}
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		info.setStringPermissions(Permissions);
		return info;//返回给授权管理器
	}

	
}
