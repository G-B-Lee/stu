package com.cy.pj.common.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.cy.pj.common.aspect.RequiredIp;

/**
 * 获取ip的工具类
 */
@Service
    public class IpUtils {
        public static String getIpAddr(HttpServletRequest request) {
            String ipAddress = null;
            try {
                ipAddress = request.getHeader("x-forwarded-for");
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("Proxy-Client-IP");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getRemoteAddr();
                    if (ipAddress.equals("127.0.0.1")) {
                        // 根据网卡取本机配置的IP
                        InetAddress inet = null;
                        try {
                            inet = InetAddress.getLocalHost();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        ipAddress = inet.getHostAddress();
                    }
                }
                // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
                if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                                                                    // = 15
                    if (ipAddress.indexOf(",") > 0) {
                        ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                    }
                }
            } catch (Exception e) {
                ipAddress="";
            }
            return ipAddress;
        }
        /** 
         * @return 获取本机IP 
         * @throws SocketException 
         */  
        @RequiredIp
        public static String getRealIp() throws SocketException {  
            String localip = null;// 本地IP，如果没有配置外网IP则返回它  
            String netip = null;// 外网IP  
        
            Enumeration<NetworkInterface> netInterfaces =   
                NetworkInterface.getNetworkInterfaces();  
            InetAddress ip = null;  
            boolean finded = false;// 是否找到外网IP  
            while (netInterfaces.hasMoreElements() && !finded) {  
                NetworkInterface ni = netInterfaces.nextElement();  
                Enumeration<InetAddress> address = ni.getInetAddresses();  
                while (address.hasMoreElements()) {  
                    ip = address.nextElement();  
                    if (!ip.isSiteLocalAddress()   
                            && !ip.isLoopbackAddress()   
                            && ip.getHostAddress().indexOf(":") == -1) {// 外网IP  
                        netip = ip.getHostAddress();  
                        finded = true;  
                        break;  
                    } else if (ip.isSiteLocalAddress()   
                            && !ip.isLoopbackAddress()   
                            && ip.getHostAddress().indexOf(":") == -1) {// 内网IP  
                        localip = ip.getHostAddress();  
                    }  
                }  
            }  
            
            if (netip != null && !"".equals(netip)) {  
                return netip;  
            } else {  
                return localip;  
            }  
        }  
    }
