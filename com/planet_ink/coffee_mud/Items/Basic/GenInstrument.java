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
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2020 Bo Zimmerman

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
public class GenInstrument extends GenItem implements MusicalInstrument
{
	@Override
	public String ID()
	{
		return "GenInstrument";
	}

	private InstrumentType type = InstrumentType.OTHER_INSTRUMENT_TYPE;

	public GenInstrument()
	{
		super();
		setName("a generic musical instrument");
		basePhyStats.setWeight(12);
		setDisplayText("a generic musical instrument sits here.");
		setDescription("");
		baseGoldValue = 15;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_OAK);
	}

	@Override
	public void recoverPhyStats()
	{
		CMLib.flags().setReadable(this, false);
		super.recoverPhyStats();
	}

	@Override
	public InstrumentType getInstrumentType()
	{
		return type;
	}

	@Override
	public String getInstrumentTypeName()
	{
		return type.name();
	}

	@Override
	public void setReadableText(final String text)
	{
		super.setReadableText(text);
		if(CMath.isInteger(text))
		{
			setInstrumentType(CMath.s_int(text));
			readableText="";
		}
	}

	@Override
	public void setInstrumentType(final int typeOrdinal)
	{
		if(typeOrdinal < InstrumentType.values().length)
			type = InstrumentType.values()[typeOrdinal];
	}

	@Override
	public void setInstrumentType(final InstrumentType newType)
	{
		if(newType != null)
			type = newType;
	}

	@Override
	public void setInstrumentType(final String newType)
	{
		if(newType != null)
		{
			final InstrumentType typeEnum = (InstrumentType)CMath.s_valueOf(InstrumentType.class, newType.toUpperCase().trim());
			if(typeEnum != null)
				type = typeEnum;
		}
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if (!super.okMessage(host, msg))
			return false;
		if(amWearingAt(Wearable.WORN_WIELD)
		&&(msg.source()==owner())
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(msg.source().location()!=null)
		&&((msg.tool()==null)
			||(msg.tool()==this)
			||(!(msg.tool() instanceof Weapon))
			||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)))
		{
			msg.source().location().show(msg.source(), null, this, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> play(s) <O-NAME>."));
			return false;
		}
		return true;
	}

	private final static String[] MYCODES={"INSTTYPE"};

	@Override
	public String getStat(final String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0:
			return this.getInstrumentTypeName();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0:
		{
			this.setInstrumentType(val);
			break;
		}
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
		final String[] MYCODES=CMProps.getStatCodesList(GenInstrument.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(GenericBuilder.GenItemCode.values());
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
		if(!(E instanceof GenInstrument))
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
