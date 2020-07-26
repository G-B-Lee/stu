package com.cy.Testocp;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.text.StyleContext.SmallAttributeSet;
//线程单例
class ThreadLocalUtil{
//方案1:
//	static SimpleDateFormat sm = new SimpleDateFormat("yyyy/MM/dd");
//	public static String format(Date date) {
//		return sm.format(date);
//	}
	
//方案2:
	//将simpleDateFormat对象设为私有静态与ThreadLocal绑定 
	private static ThreadLocal<SimpleDateFormat> tl = new ThreadLocal<SimpleDateFormat>();
	public static String format(Date date) {
		//ThreadLocal中取simpleDateFormat对象
		SimpleDateFormat sm = tl.get();
		//如果没有则创建
		if(sm==null) {
			System.out.println("---cread----");
			sm = new SimpleDateFormat("yyyy/MM/dd");
			tl.set(sm);
		}
		return sm.format(date);
		
		
	}
}

public class ThreadLocalTest {

	
	public static void main(String[] args) {
		
		for(int i = 0 ; i < 5 ; i++) {
			new Thread() {
				public void run() {
					System.out.println(ThreadLocalUtil.format(new Date()));
					System.out.println(ThreadLocalUtil.format(new Date()));
				};
			}.start();
		}
	}
}
