package com.uniquid.register.circle;

import com.uniquid.register.exception.RegisterException;

public interface CircleRegister {
	
	public void insertCircle(String masterId) throws RegisterException;
	
	public void deleteCircle(CircleChannel circle) throws RegisterException;
	
	public CircleChannel getCircle(String masterId) throws RegisterException;
	
	public Boolean checkExists(String masterId) throws RegisterException;
	
} 
