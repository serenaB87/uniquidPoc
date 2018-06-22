package com.uniquid.register.guest;

import java.util.List;

import com.uniquid.register.exception.RegisterException;

public interface GuestsRegister {
	
	public List<GuestChannel> getGuestsByCircleName (String masterId) throws RegisterException;

	public void insertGuest(GuestChannel guest, String masterId) throws RegisterException;
	
}
