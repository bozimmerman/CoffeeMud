package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenBoat extends Boat
{
	private String readableText = "";
	public GenBoat()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}

	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public String keyName()
	{
		return readableText;
	}
	public void setKeyName(String newKeyName)
	{
		readableText=newKeyName;
	}
	public Environmental newInstance()
	{
		return new GenBoat();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		return Generic.getPropertiesStr(this,false);
	}

	public void setMiscText(String newText)
	{
		miscText="";
		Generic.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}
}
