package com.planet_ink.coffee_mud.interfaces;

public interface Container extends Item
{
	public boolean isLocked();
	public boolean hasALock();
	public boolean isOpen();
	public boolean hasALid();
	public void setLidsNLocks(boolean newHasALid, boolean newIsOpen, boolean newHasALock, boolean newIsLocked);
	public String keyName();
	public void setKeyName(String newKeyName);
}
