package com.planet_ink.coffee_mud.Exits;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class GenExit extends StdExit
{
	protected String keyName="";
	public GenExit()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic exit";
		description="An ordinary looking way from here to there.";
		displayText="an open exit";
		closedText="a closed exit";
		doorName="exit";
		openName="open";
		closeName="close";
		keyName="";
		hasADoor=false;
		isOpen=true;
		hasALock=false;
		isLocked=false;
		doorDefaultsClosed=false;
		doorDefaultsLocked=false;
		
		openDelayTicks=15;
	}
	
	public Environmental newInstance()
	{
		return new GenExit();
	}
	public String text()
	{
		return Generic.getPropertiesStr(this);
	}
	
	public void setMiscText(String newText)
	{
		miscText="";
		Generic.setPropertiesStr(this,newText);
		recoverEnvStats();
		isOpen=!doorDefaultsClosed;
		isLocked=doorDefaultsLocked;
	}
	
	public String readableText(){ return (isReadable?keyName:"");}
	public void setReadable(boolean isTrue){isReadable=isTrue;}
	public void setReadableText(String text) { keyName=text; }
	
	public String classRestrictedName(){ return (classRestricted?keyName:"");}
	public void setClassRestricted(boolean isTrue){classRestricted=isTrue;}
	public void setClassRestrictedName(String className) { keyName=className; }
	
	public String keyName()	{ return keyName; }
	public void setKeyName(String newKeyName){keyName=newKeyName;}

}
