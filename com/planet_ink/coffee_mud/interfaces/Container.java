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
	public boolean canContain(Environmental E);
	public long containTypes();
	public void setContainTypes(long containTypes);
	public void emptyPlease();
	
	public static final int CONTAIN_ANYTHING=0;
	public static final int CONTAIN_LIQUID=1;
	public static final int CONTAIN_COINS=2;
	public static final int CONTAIN_SWORDS=4;
	public static final int CONTAIN_DAGGERS=8;
	public static final int CONTAIN_OTHERWEAPONS=16;
	public static final int CONTAIN_ONEHANDWEAPONS=32;
	public static final int CONTAIN_BODIES=64;
	public static final int CONTAIN_READABLES=128;
	public static final int CONTAIN_SCROLLS=256;
	public static final int CONTAIN_CAGED=512;
	public static final int CONTAIN_KEYS=1024;
	public static final String[] CONTAIN_DESCS={"ANYTHING",
												"LIQUID",
												"COINS",
												"SWORDS",
												"DAGGERS",
												"OTHER WEAPONS",
												"ONE-HANDED WEAPONS",
												"BODIES",
												"READABLES",
												"SCROLLS",
												"CAGED ANIMALS",
												"KEYS"};
}
