package com.planet_ink.coffee_mud.Common;
import java.util.Vector;

import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompConnector;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompLocation;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompType;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;

/*
   Copyright 2009-2018 Bo Zimmerman

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
	private CompConnector	connector		= CompConnector.AND;
	private CompLocation	location		= CompLocation.INVENTORY;
	private boolean			isConsumed		= true;
	private int				amount			= 1;
	private CompType		type			= CompType.STRING;
	private long			compTypeMatRsc	= 0;
	private String			compTypeStr		= "";
	private String			maskStr			= "";
	private CompiledZMask	compiledMask	= null;

	@Override
	public String ID()
	{
		return "DefaultAbilityComponent";
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultAbilityComponent();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final Object O=this.clone();
			return (CMObject)O;
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultAbilityComponent();
		}
	}

	@Override
	public CompConnector getConnector()
	{
		return connector;
	}

	@Override
	public void setConnector(CompConnector connector)
	{
		this.connector = connector;
	}

	@Override
	public CompLocation getLocation()
	{
		return location;
	}

	@Override
	public void setLocation(CompLocation location)
	{
		this.location = location;
	}

	@Override
	public boolean isConsumed()
	{
		return isConsumed;
	}

	@Override
	public void setConsumed(boolean isConsumed)
	{
		this.isConsumed = isConsumed;
	}

	@Override
	public int getAmount()
	{
		return amount;
	}

	@Override
	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	@Override
	public MaskingLibrary.CompiledZMask getCompiledMask()
	{
		return compiledMask;
	}

	@Override
	public String getMaskStr()
	{
		return maskStr;
	}

	@Override
	public void setMask(String maskStr)
	{

		this.maskStr = maskStr.trim();
		this.compiledMask = null;
		if (this.maskStr.length() > 0)
			CMLib.masking().maskCompile(this.maskStr);
	}

	@Override
	public CompType getType()
	{
		return type;
	}

	@Override
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

	@Override
	public long getLongType()
	{
		return compTypeMatRsc;
	}

	@Override
	public String getStringType()
	{
		return compTypeStr;
	}
}
