package com.planet_ink.coffee_mud.Items.MiscMagic;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2001-2020 Bo Zimmerman

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
public class GenWand extends StdWand
{
	@Override
	public String ID()
	{
		return "GenWand";
	}

	protected String	readableText	= "";
	protected String	spellText		= "";
	protected int		maxUses			= Integer.MAX_VALUE;
	protected int		enchType		= -1;

	public GenWand()
	{
		super();

		setName("a wand");
		setDisplayText("a simple wand is here.");
		setDescription("A wand made out of wood.");
		secretIdentity=null;
		setUsesRemaining(0);
		baseGoldValue=20000;
		basePhyStats().setLevel(12);
		CMLib.flags().setReadable(this,false);
		material=RawMaterial.RESOURCE_OAK;
		recoverPhyStats();
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public void setSpell(final Ability theSpell)
	{
		readableText="";
		spellText="";
		if(theSpell!=null)
			spellText=theSpell.ID();
		secretWord=StdWand.getWandWord(spellText);
	}

	@Override
	public Ability getSpell()
	{
		if((spellText==null)||(spellText.length()==0))
			return null;
		return CMClass.getAbility(spellText);
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(final String text)
	{
		readableText = text;
		if(text.length()>0)
		{
			final Ability A=CMClass.getAbility(text);
			if(A!=null)
			{
				readableText="";
				setSpell(A);
			}
		}
	}

	@Override
	public int maxUses()
	{
		return maxUses;
	}

	@Override
	public void setMaxUses(final int newMaxUses)
	{
		maxUses = newMaxUses;
		if(newMaxUses > super.usesRemaining() && (newMaxUses > 0))
			super.setUsesRemaining(newMaxUses);
	}

	@Override
	public int getEnchantType()
	{
		return enchType;
	}

	@Override
	public void setEnchantType(final int enchType)
	{
		this.enchType = enchType;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	@Override
	public void setMiscText(final String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	private final static String[] MYCODES={"ENCHTYPE", "SPELL", "MAXUSES"};

	@Override
	public String getStat(final String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0:
			if((getEnchantType()<0)||(getEnchantType()>=Ability.ACODE_DESCS_.length))
				return "ANY";
			return Ability.ACODE_DESCS_[getEnchantType()];
		case 1:
		{
			final Ability A = getSpell();
			return (A!=null) ? A.ID() : "";
		}
		case 2:
			return ""+maxUses();
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
			setEnchantType(CMParms.indexOf(Ability.ACODE_DESCS_, val.toUpperCase().trim()));
			break;
		case 1:
		{
			final Ability A=CMClass.getAbility(val);
			if(A!=null)
				setSpell(A);
			break;
		}
		case 2:
		{
			if(CMath.isMathExpression(val))
				this.setMaxUses(CMath.parseIntExpression(val));
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
		final String[] MYCODES=CMProps.getStatCodesList(GenWand.MYCODES,this);
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
		if(!(E instanceof GenWand))
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
