package com.cy.Testocp;

import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.omg.CORBA.PRIVATE_MEMBER;

interface SendService{
	void send(String msg);
}
final class SendServiceImpl implements SendService{

	@Override
	public void send(String msg) {
		System.out.println("send:"+msg);
	}
	
}

interface Interceptor1{
	void doBefore();
	void doAfter();
}

class LogInterceptor1 implements Interceptor1{
	@Override
	public void doBefore() {
		System.out.println("start:"+System.currentTimeMillis());
	}
	@Override
	public void doAfter() {
		System.out.println("end:"+System.currentTimeMillis());
	}
}

class PermissionInterceptor1 implements Interceptor1{
	@Override
	public void doBefore() {
		System.out.println("Permission-->权限检查!!!");
	}
	public void doAfter() {
	}
}

class ComposeSendServiceImpl implements SendService{

	private SendService sendService;
	private List<Interceptor1> interceptors;
	public ComposeSendServiceImpl(SendService sendService,List<Interceptor1> interceptors) {
		this.sendService = sendService;
		this.interceptors = interceptors;
	}
	@Override
	public void send(String msg) {
		for (int i = 0; i < interceptors.size(); i++) {
			interceptors.get(i).doBefore();
		}
		sendService.send(msg);
		for (int i = interceptors.size()-1; i>=0; i--) {
			interceptors.get(i).doAfter();
		}
	}
}
public class TestSend {

	public static void main(String[] args) {
		List<Interceptor1> interceptors = new ArrayList<Interceptor1>();
		interceptors.add(new PermissionInterceptor1());
		interceptors.add(new LogInterceptor1());
		SendService send = new ComposeSendServiceImpl(new SendServiceImpl(), interceptors);
		send.send("五一劳动节!");
	}
	
}






