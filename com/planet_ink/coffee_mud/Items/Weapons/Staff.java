package com.planet_ink.coffee_mud.Items.Weapons;
import java.util.List;

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
   Copyright 2001-2018 Bo Zimmerman

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
public class Staff extends StdWeapon implements Wand
{
	@Override
	public String ID()
	{
		return "Staff";
	}

	protected String secretWord=CMProps.getAnyListFileValue(CMProps.ListFile.MAGIC_WORDS);

	public Staff()
	{
		super();

		setName("a wooden staff");
		setDisplayText("a wooden staff lies in the corner of the room.");
		setDescription("It`s long and wooden, just like a staff ought to be.");
		secretIdentity="";
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(0);
		basePhyStats.setWeight(4);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(4);
		baseGoldValue=1;
		recoverPhyStats();
		wornLogicalAnd=true;
		material=RawMaterial.RESOURCE_OAK;
		properWornBitmap=Wearable.WORN_HELD|Wearable.WORN_WIELD;
		weaponDamageType=TYPE_BASHING;
		weaponClassification=Weapon.CLASS_STAFF;
		setUsesRemaining(0);
	}

	@Override
	public int maxUses()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public void setMaxUses(int newMaxUses)
	{
	}

	@Override
	public String magicWord()
	{
		return secretWord;
	}

	@Override
	public void setSpell(Ability theSpell)
	{
		miscText="";
		if(theSpell!=null)
			miscText=theSpell.ID();
		secretWord=StdWand.getWandWord(miscText);
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord=StdWand.getWandWord(newText);
	}

	@Override
	public Ability getSpell()
	{
		if(text().length()==0)
			return null;
		return CMClass.getAbility(text());
	}

	@Override
	public int value()
	{
		if(usesRemaining()<=0)
			return 0;
		return super.value();
	}

	@Override
	public String secretIdentity()
	{
		String id=super.secretIdentity();
		final Ability A=getSpell();
		String uses;
		if(this.maxUses() < 999999)
			uses=""+usesRemaining()+"/"+maxUses();
		else
			uses = ""+usesRemaining();
		if(A!=null)
			id="'A staff of "+A.name()+"' Charges: "+uses+"\n\r"+id;
		return id+"\n\rSay the magic word :`"+secretWord+"` to the target.";
	}

	@Override
	public void waveIfAble(MOB mob, Physical afftarget, String message)
	{
		StdWand.waveIfAble(mob,afftarget,message,this);
	}

	@Override
	public boolean checkWave(MOB mob, String message)
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
				waveIfAble(mob,(Physical)msg.tool(),msg.targetMessage());
			break;
		case CMMsg.TYP_SPEAK:
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)&&(!amWearingAt(Wearable.IN_INVENTORY)))
			{
				boolean alreadyWanding=false;
				final List<CMMsg> trailers =msg.trailerMsgs();
				if(trailers!=null)
				{
					for(final CMMsg msg2 : trailers)
					{
						if((msg2.targetMinor()==CMMsg.TYP_WAND_USE)
						&&(msg2.target() == this))
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
}
