package com.uniquid.node.impl;

import java.io.File;

import org.bitcoinj.core.NetworkParameters;
import org.junit.Assert;
import org.junit.Test;

import com.uniquid.params.UniquidRegTest;
import com.uniquid.node.impl.utils.DummyRegisterFactory;
import com.uniquid.node.impl.utils.DummyTransactionManager;
import com.uniquid.register.RegisterFactory;

public class UniquidWatchingNodeImplBuilderTest {
	
	@Test
	public void testConstructor() throws Exception {
		
		UniquidWatchingNodeImpl.WatchingNodeBuilder builder = new UniquidWatchingNodeImpl.WatchingNodeBuilder();
		
		Assert.assertNotNull(builder);
		
		Assert.assertNull(builder.getUniquidNodeConfiguration().getNetworkParameters());
		Assert.assertNull(builder.getUniquidNodeConfiguration().getProviderFile());
		Assert.assertNull(builder.getUniquidNodeConfiguration().getUserFile());
		Assert.assertNull(builder.getUniquidNodeConfiguration().getProviderChainFile());
		Assert.assertNull(builder.getUniquidNodeConfiguration().getUserChainFile());
		Assert.assertNull(builder.getUniquidNodeConfiguration().getRegisterFactory());
		Assert.assertNull(builder.getUniquidNodeConfiguration().getNodeName());
		
	}
	
	@Test
	public void testParams() {
		
		UniquidWatchingNodeImpl.WatchingNodeBuilder builder = new UniquidWatchingNodeImpl.WatchingNodeBuilder();
		
		Assert.assertNull(builder.getUniquidNodeConfiguration().getNetworkParameters());
		
		NetworkParameters parameters = UniquidRegTest.get();
		
		builder.setNetworkParameters(parameters);
		
		Assert.assertEquals(parameters, builder.getUniquidNodeConfiguration().getNetworkParameters());
		
	}
	
	@Test
	public void testProviderFile() throws Exception {
		
		UniquidWatchingNodeImpl.WatchingNodeBuilder builder = new UniquidWatchingNodeImpl.WatchingNodeBuilder();
		
		Assert.assertNull(builder.getUniquidNodeConfiguration().getProviderFile());
		
		File file = File.createTempFile("provider", ".wallet");
		
		builder.setProviderFile(file);
		
		Assert.assertEquals(file, builder.getUniquidNodeConfiguration().getProviderFile());
		
	}
	
	@Test
	public void testUserFile() throws Exception {
		
		UniquidWatchingNodeImpl.WatchingNodeBuilder builder = new UniquidWatchingNodeImpl.WatchingNodeBuilder();
		
		Assert.assertNull(builder.getUniquidNodeConfiguration().getUserFile());
		
		File file = File.createTempFile("user", ".wallet");
		
		builder.setUserFile(file);
		
		Assert.assertEquals(file, builder.getUniquidNodeConfiguration().getUserFile());
		
	}
	
	@Test
	public void testChainFile() throws Exception {
		
		UniquidWatchingNodeImpl.WatchingNodeBuilder builder = new UniquidWatchingNodeImpl.WatchingNodeBuilder();
		
		Assert.assertNull(builder.getUniquidNodeConfiguration().getProviderChainFile());
		
		File file = File.createTempFile("chain", ".chain");
		
		builder.setProviderChainFile(file);
		
		Assert.assertEquals(file, builder.getUniquidNodeConfiguration().getProviderChainFile());
		
	}
	
	@Test
	public void testUserChainFile() throws Exception {
		
		UniquidWatchingNodeImpl.WatchingNodeBuilder builder = new UniquidWatchingNodeImpl.WatchingNodeBuilder();
		
		Assert.assertNull(builder.getUniquidNodeConfiguration().getUserFile());
		
		File file = File.createTempFile("userchain", ".chain");
		
		builder.setUserChainFile(file);
		
		Assert.assertEquals(file, builder.getUniquidNodeConfiguration().getUserChainFile());
		
	}
	
	@Test
	public void testRegisterFactory() {
		
		UniquidWatchingNodeImpl.WatchingNodeBuilder builder = new UniquidWatchingNodeImpl.WatchingNodeBuilder();
		
		Assert.assertNull(builder.getUniquidNodeConfiguration().getRegisterFactory());
		
		RegisterFactory dummy = new DummyRegisterFactory(null, null, new DummyTransactionManager(), null);
		
		builder.setRegisterFactory(dummy);
		
		Assert.assertEquals(dummy, builder.getUniquidNodeConfiguration().getRegisterFactory());
		
	}
	
	@Test
	public void testMachineName() {
		
		UniquidWatchingNodeImpl.WatchingNodeBuilder builder = new UniquidWatchingNodeImpl.WatchingNodeBuilder();
		
		Assert.assertNull(builder.getUniquidNodeConfiguration().getNodeName());
		
		String machineName = "machineName";
		
		builder.setNodeName(machineName);
		
		Assert.assertEquals(machineName, builder.getUniquidNodeConfiguration().getNodeName());
		
	}
	
	@Test
	public void testBuild() throws Exception {
		
		UniquidWatchingNodeImpl.WatchingNodeBuilder builder = new UniquidWatchingNodeImpl.WatchingNodeBuilder();
		
		NetworkParameters parameters = UniquidRegTest.get();
		File providerFile = File.createTempFile("provider", ".wallet");
		File userFile = File.createTempFile("user", ".wallet");
		File chainFile = File.createTempFile("chain", ".chain");
		File userChainFile = File.createTempFile("userchain", ".chain");
		RegisterFactory dummyRegister = new DummyRegisterFactory(null, null, new DummyTransactionManager(), null);
		String machineName = "machineName";

		builder.setNetworkParameters(parameters);
		builder.setProviderFile(providerFile);
		builder.setUserFile(userFile);
		builder.setProviderChainFile(chainFile);
		builder.setUserChainFile(userChainFile);
		builder.setRegisterFactory(dummyRegister);
		builder.setNodeName(machineName);
		
		UniquidWatchingNodeImpl uniquidNode = builder.buildFromXpub("tpubDAnD549eCz2j2w21P6sx9NvXJrEoWzVevpbvXDpwQzKTC9xWsr8emiEdJ64h1qXbYE4SbDJNbZ7imotNPsGD8RvHQvh6xtgMJTczb8WW8X8", 123456789);
		
		Assert.assertNotNull(uniquidNode);
		
	}

}
