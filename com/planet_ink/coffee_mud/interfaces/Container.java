package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
public interface Container extends Item
{
	public boolean isLocked();
	public boolean hasALock();
	public boolean isOpen();
	public boolean hasALid();
	public void setLidsNLocks(boolean newHasALid, boolean newIsOpen, boolean newHasALock, boolean newIsLocked);
	public String keyName();
	public void setKeyName(String newKeyName);
	public Vector getContents();
	public int capacity();
	public void setCapacity(int newValue);
}
