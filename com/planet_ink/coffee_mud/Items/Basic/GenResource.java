package com.planet_ink.coffee_mud.Items.Basic;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.GenCow;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2020 Bo Zimmerman

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
public class GenResource extends GenItem implements RawMaterial
{
	@Override
	public String ID()
	{
		return "GenResource";
	}

	public GenResource()
	{
		super();
		setName("a pile of resources");
		setDisplayText("a pile of resources sits here.");
		setDescription("");
		setMaterial(RawMaterial.RESOURCE_IRON);
		basePhyStats().setWeight(0);
		recoverPhyStats();
	}

	protected String	domainSource	= null;
	protected String	resourceSubType	= "";

	@Override
	public String domainSource()
	{
		return domainSource;
	}

	@Override
	public void setDomainSource(final String src)
	{
		domainSource = src;
	}

	@Override
	public void setSubType(final String subType)
	{
		resourceSubType = (subType == null)?"":subType;
	}

	@Override
	public String getSubType()
	{
		return resourceSubType;
	}

	@Override
	public boolean rebundle()
	{
		return CMLib.materials().rebundle(this);
	}

	@Override
	public void quickDestroy()
	{
		CMLib.materials().quickDestroy(this);
	}

	private final static String[] MYCODES={"DOMAINSRC","RSUBTYPE"};

	@Override
	public String getStat(final String code)
	{
		if(super.isStat(code))
			return super.getStat(code);
		else
		switch(getCodeNum(code))
		{
		case 0:
			return this.domainSource();
		case 1:
			return this.getSubType();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(super.isStat(code))
			super.setStat(code, val);
		else
		switch(getCodeNum(code))
		{
		case 0:
			setDomainSource(val);
			break;
		case 1:
			this.setSubType(val);
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	protected int getCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[]	codes	= null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenResource.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(super.getStatCodes());
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenResource))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
