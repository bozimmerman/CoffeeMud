package com.planet_ink.coffee_mud.interfaces;

public interface Exit extends Environmental
{
	public boolean isOpen();
	public void setOpen(boolean isTrue);
	public boolean isLocked();
	public void setLocked(boolean isTrue);
	public boolean hasADoor();
	public void setHasDoor(boolean isTrue);
	public boolean hasALock();
	public void setHasLock(boolean isTrue);
	public String keyName();
	public void setKeyName(String keyName);
	public boolean defaultsLocked();
	public void setDefaultsLocked(boolean isTrue);
	public boolean defaultsClosed();
	public void setDefaultsClosed(boolean isTrue);
	
	public boolean isTrapped();
	public void setTrapped(boolean isTrue);
	
	public String readableText();
	public boolean isReadable();
	public void setReadable(boolean isTrue);
	public void setReadableText(String text);
	
	public boolean levelRestricted();
	public void setLevelRestricted(boolean isTrue);
	
	public String classRestrictedName();
	public boolean classRestricted();
	public void setClassRestricted(boolean isTrue);
	public void setClassRestrictedName(String className);
	
	public String doorName();
	public String closeWord();
	public String openWord();
	public String closedText();
	public void setExitParams(String newDoorName,
							  String newCloseWord,
							  String newOpenWord,
							  String newClosedText);

	
	public int openDelayTicks();
	public void setOpenDelayTicks(int numTicks);
}
