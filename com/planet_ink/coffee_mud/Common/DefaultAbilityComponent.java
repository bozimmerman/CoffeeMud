package com.planet_ink.coffee_mud.Common;
import java.util.Vector;

import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompConnector;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompLocation;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompType;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;

/*
Copyright 2007-2010 Bo Zimmerman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
public class DefaultAbilityComponent implements AbilityComponent
{
	private CompConnector connector = CompConnector.AND;
	private CompLocation location = CompLocation.INVENTORY;
	private boolean isConsumed = true;
	private int amount = 1;
	private CompType type = CompType.STRING;
	private long compTypeMatRsc = 0;
	private String compTypeStr = "";
	private String maskStr = "";
	@SuppressWarnings("unchecked")
	private Vector compiledMask = null;
	
    public String ID(){return "DefaultAbilityComponent";}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultAbilityComponent();}}
    public void initializeClass(){}
    public CMObject copyOf()
    {
        try
        {
            Object O=this.clone();
            return (CMObject)O;
        }
        catch(CloneNotSupportedException e)
        {
            return new DefaultAbilityComponent();
        }
    }
    
	public CompConnector getConnector() 
	{
		return connector;
	}
	public void setConnector(CompConnector connector) 
	{
		this.connector = connector;
	}
	public CompLocation getLocation() 
	{
		return location;
	}
	public void setLocation(CompLocation location) 
	{
		this.location = location;
	}
	public boolean isConsumed() 
	{
		return isConsumed;
	}
	public void setConsumed(boolean isConsumed) 
	{
		this.isConsumed = isConsumed;
	}
	public int getAmount() 
	{
		return amount;
	}
	public void setAmount(int amount) 
	{
		this.amount = amount;
	}
	@SuppressWarnings("unchecked")
	public Vector getCompiledMask() 
	{
		return compiledMask;
	}
	public String getMaskStr()
	{
		return maskStr;
	}
	public void setMask(String maskStr) 
	{
		
		this.maskStr = maskStr.trim();
		this.compiledMask = null;
		if(this.maskStr.length()>0)
			CMLib.masking().maskCompile(this.maskStr);
	}
	public CompType getType() 
	{
		return type;
	}
	public void setType(CompType type, Object typeObj) 
	{
		this.type = type;
		if(typeObj == null)
		{
			compTypeStr="";
			compTypeMatRsc=0;
		}
		else
		if(type == CompType.STRING)
			compTypeStr = typeObj.toString();
		else
			compTypeMatRsc=CMath.s_long(typeObj.toString());
	}
	
	public long getLongType() 
	{ 
		return compTypeMatRsc;
	}
	
	public String getStringType() 
	{ 
		return compTypeStr;
	}
}
