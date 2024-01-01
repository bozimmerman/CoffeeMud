package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2009-2024 Bo Zimmerman

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
	private String			abilityID		= "";
	private CompConnector	connector		= CompConnector.AND;
	private CompLocation	location		= CompLocation.INVENTORY;
	private boolean			isConsumed		= true;
	private int				amount			= 1;
	private CompType		type			= CompType.STRING;
	private long			compTypeMatRsc	= 0;
	private String			compTypeStr		= "";
	private String			maskStr			= "";
	private String			compSubTypeStr	= "";
	private CompiledZMask	compiledMask	= null;
	private String			triggerDef		= "";

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
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().getDeclaredConstructor().newInstance();
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
	public String getAbilityID()
	{
		return abilityID;
	}

	@Override
	public void setAbilityID(final String ID)
	{
		abilityID=ID;
	}

	@Override
	public CompConnector getConnector()
	{
		return connector;
	}

	@Override
	public void setConnector(final CompConnector connector)
	{
		this.connector = connector;
	}

	@Override
	public CompLocation getLocation()
	{
		return location;
	}

	@Override
	public void setLocation(final CompLocation location)
	{
		this.location = location;
	}

	@Override
	public boolean isConsumed()
	{
		return isConsumed;
	}

	@Override
	public void setConsumed(final boolean isConsumed)
	{
		this.isConsumed = isConsumed;
	}

	@Override
	public int getAmount()
	{
		return amount;
	}

	@Override
	public void setAmount(final int amount)
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
	public void setMask(final String maskStr)
	{

		this.maskStr = maskStr.trim();
		this.compiledMask = null;
		if (maskStr.length() > 0)
			compiledMask = CMLib.masking().getPreCompiledMask(this.maskStr);
	}

	@Override
	public CompType getType()
	{
		return type;
	}

	@Override
	public void setType(final CompType type, final Object typeObj, final String subType)
	{
		this.type = type;
		compSubTypeStr = (subType == null)?"":subType.toUpperCase().trim();
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

	@Override
	public String getSubType()
	{
		return compSubTypeStr;
	}

	@Override
	public String getTriggererDef()
	{
		return triggerDef;
	}

	@Override
	public void setTriggererDef(String def)
	{
		if((def == null)||(def.trim().equalsIgnoreCase("null")))
			def="";
		triggerDef = def.trim();
	}
}
