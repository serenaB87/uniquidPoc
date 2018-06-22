package com.uniquid.node.impl.utils;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.circle.CircleRegister;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.guest.GuestsRegister;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.transaction.TransactionManager;
import com.uniquid.register.user.UserRegister;

public class DummyRegisterFactory implements RegisterFactory {
	
	private UserRegister userRegister;
	private ProviderRegister providerRegister;
	private TransactionManager transactionManager;
	private CircleRegister circleRegister;
	
	public DummyRegisterFactory(UserRegister userRegister, ProviderRegister providerRegister, TransactionManager transactionManager, CircleRegister circleRegister) {
		this.userRegister = userRegister;
		this.providerRegister = providerRegister;
		this.transactionManager = transactionManager;
		this.circleRegister = circleRegister;
	}
	
	@Override
	public UserRegister getUserRegister() throws RegisterException {
		return userRegister;
	}
	
	@Override
	public ProviderRegister getProviderRegister() throws RegisterException {
		return providerRegister;
	}
	
	@Override
	public TransactionManager getTransactionManager() throws RegisterException {
		return transactionManager;
	}

	@Override
	public CircleRegister getCircleRegister() throws RegisterException {
		// TODO Auto-generated method stub
		return circleRegister;
	}

	@Override
	public GuestsRegister getGuestsRegister() throws RegisterException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
