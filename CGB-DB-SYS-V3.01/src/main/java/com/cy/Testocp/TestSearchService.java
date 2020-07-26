package com.cy.Testocp;

import java.util.ArrayList;

import java.util.List;
interface SearchService {

	Object Search(String key);
}

class SearchServiceImpl implements SearchService {

	@Override
	public Object Search(String key) {
		System.out.println("key--->"+key);
		return null;
	}
}

interface Interceptor{
	void doBefore();
	void doAfter();
}

class LogInterceptor implements Interceptor{
	@Override
	public void doBefore() {
		System.out.println("start:"+System.currentTimeMillis());
	}
	@Override
	public void doAfter() {
		System.out.println("end:"+System.currentTimeMillis());
	}
}

class PermissionInterceptor implements Interceptor{
	@Override
	public void doBefore() {
		System.out.println("权限检查---->");
	}

	@Override
	public void doAfter() {
		
	}
}

class ClildSearchServiceImpl extends SearchServiceImpl{
	
	 private List<Interceptor> interceptors;

	public ClildSearchServiceImpl(List<Interceptor> interceptors) {
		 this.interceptors = interceptors;
	}
	
	@Override
	public Object Search(String key) {
		for (int i = 0; i< interceptors.size(); i++) {
			interceptors.get(i).doBefore();
		}
		Object search = super.Search(key);
		
		for (int i = interceptors.size()-1; i>=0 ; i--) {
			interceptors.get(i).doAfter();
		}
		return search; 
	}
}
public class TestSearchService {

	public static void main(String[] args) {
		 List<Interceptor> interceptors = new ArrayList<Interceptor>();
		 interceptors.add(new PermissionInterceptor());
		 interceptors.add(new LogInterceptor());
		 SearchService css = new ClildSearchServiceImpl(interceptors);
		 css.Search("stream");
	}
}
