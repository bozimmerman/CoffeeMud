package com.planet_ink.coffee_mud.Items.Weapons;
import java.util.List;

import com.planet_ink.coffee_mud.Items.MiscMagic.GenWand;
import com.planet_ink.coffee_mud.Items.MiscMagic.StdWand;
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
public class GenStaff extends GenWeapon implements Wand
{
	@Override
	public String ID()
	{
		return "GenStaff";
	}

	protected String	secretWord	= CMProps.getAnyListFileValue(CMProps.ListFile.MAGIC_WORDS);
	protected String	spellText	= "";
	protected int		enchType	= -1;

	public GenStaff()
	{
		super();

		setName("a wooden staff");
		setDisplayText("a wooden staff lies in the corner of the room.");
		setDescription("");
		secretIdentity="";
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(0);
		basePhyStats.setWeight(4);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(4);
		setUsesRemaining(0);
		baseGoldValue=1;
		recoverPhyStats();
		wornLogicalAnd=true;
		material=RawMaterial.RESOURCE_OAK;
		properWornBitmap=Wearable.WORN_HELD|Wearable.WORN_WIELD;
		weaponDamageType=TYPE_BASHING;
		weaponClassification=Weapon.CLASS_STAFF;
		recoverPhyStats();
	}

	protected int maxUses=Integer.MAX_VALUE;

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
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public int value()
	{
		if((usesRemaining()<=0)
		&&(readableText.length()>0)
		&&(this.getSpell()!=null))
			return 0;
		return super.value();
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
	public String secretIdentity()
	{
		String id=super.secretIdentity();
		final Ability A=getSpell();
		final String uses;
		if(this.usesRemaining() < 999999)
		{
			if(this.maxUses() < 999999)
				uses=""+usesRemaining()+"/"+maxUses();
			else
				uses = ""+usesRemaining();
		}
		else
			uses="unlimited";
		if(A!=null)
			id="'A staff of "+A.name()+"' Charges: "+uses+"\n\r"+id;
		return id+"\n\rSay the magic word :`"+secretWord+"` to the target.";
	}

	@Override
	public String magicWord()
	{
		return secretWord;
	}

	@Override
	public void waveIfAble(final MOB mob, final Physical afftarget, final String message)
	{
		StdWand.waveIfAble(mob,afftarget,message,this);
	}

	@Override
	public boolean checkWave(final MOB mob, final String message)
	{
		return StdWand.checkWave(mob, message, this);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if(msg.amITarget(this)&&((msg.tool()==null)||(msg.tool() instanceof Physical)))
				StdWand.waveIfAble(mob,(Physical)msg.tool(),msg.targetMessage(),this);
			break;
		case CMMsg.TYP_SPEAK:
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&& amBeingWornProperly())
			{
				boolean alreadyWanding=false;
				final List<CMMsg> trailers =msg.trailerMsgs();
				if(trailers!=null)
				{
					for(final CMMsg msg2 : trailers)
					{
						if(msg2.targetMinor()==CMMsg.TYP_WAND_USE)
							alreadyWanding=true;
					}
				}
				final String said=CMStrings.getSayFromMessage(msg.sourceMessage());
				if((!alreadyWanding)&&(said!=null)&&(checkWave(mob,said)))
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,said,CMMsg.NO_EFFECT,null));
			}
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}

	// maxuses and secret word stats handled by genweapon, filled by readableText
	private final static String[] MYCODES={"ENCHTYPE", "SPELL", "MAXUSES"};

	@Override
	public String getStat(final String code)
	{
		if(GenWeapon.getGenWeaponCodeNum(code)>=0)
			return super.getStat(code);
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
		if(GenWeapon.getGenWeaponCodeNum(code)>=0)
			super.setStat(code, val);
		else
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			super.setStat(code, val);
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
		final String[] MYCODES=CMProps.getStatCodesList(GenStaff.MYCODES,this);
		final String[] superCodes=super.getStatCodes();
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
		if(!(E instanceof GenStaff))
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
