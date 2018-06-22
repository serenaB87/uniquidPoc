package com.uniquid.core.impl;

import org.junit.Assert;
import org.junit.Test;

import com.uniquid.core.provider.impl.ApplicationContext;

public class UniquidSimplifierTest {
	
	@Test
	public void testConstructor() {
		
		ApplicationContext applicationContext = new ApplicationContext();
		
		Assert.assertNotNull(applicationContext);
		
	}
	
	@Test
	public void testServerInfo() {
		
		ApplicationContext applicationContext = new ApplicationContext();
		
		Assert.assertEquals("Uniquid Library", applicationContext.getServerInfo());
		
	}
	
	@Test
	public void testAttributeNames() {
		
		ApplicationContext applicationContext = new ApplicationContext();
		
		Assert.assertNotNull(applicationContext.getAttributeNames());
		
	}

}
