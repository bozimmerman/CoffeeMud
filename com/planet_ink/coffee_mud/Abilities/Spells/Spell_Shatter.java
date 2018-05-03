package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Spell_Shatter extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Shatter";
	}

	private final static String localizedName = CMLib.lang().L("Shatter");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS|CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;
	}

	public Item getItem(MOB mobTarget)
	{
		final Vector<Item> goodPossibilities=new Vector<Item>();
		final Vector<Item> possibilities=new Vector<Item>();
		for(int i=0;i<mobTarget.numItems();i++)
		{
			final Item item=mobTarget.getItem(i);
			if((item!=null)
			   &&(item.subjectToWearAndTear()))
			{
				if(item.amWearingAt(Wearable.IN_INVENTORY))
					possibilities.addElement(item);
				else
					goodPossibilities.addElement(item);
			}
		}
		if(goodPossibilities.size()>0)
			return goodPossibilities.elementAt(CMLib.dice().roll(1,goodPossibilities.size(),-1));
		else
		if(possibilities.size()>0)
			return possibilities.elementAt(CMLib.dice().roll(1,possibilities.size(),-1));
		return null;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((target instanceof MOB)&&(mob!=target))
			{
				final Item I=getItem((MOB)target);
				if(I==null)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB mobTarget=getTarget(mob,commands,givenTarget,true,false);
		Item target=null;
		if(mobTarget!=null)
		{
			target=getItem(mob);
			if(target==null)
				return maliciousFizzle(mob,mobTarget,L("<S-NAME> attempt(s) a shattering spell at <T-NAMESELF>, but nothing happens."));
		}

		if((target==null)&&(mobTarget!=null))
			target=getTarget(mobTarget,mobTarget.location(),givenTarget,commands,Wearable.FILTER_ANY);
		else
		if((target==null)&&(mobTarget==null))
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);

		if(target==null)
			return false;
		Room R=CMLib.map().roomLocation(target);
		if(R==null)
			R=mob.location();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> starts vibrating!"):L("^S<S-NAME> utter(s) a shattering spell, causing <T-NAMESELF> to vibrate and resonate.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,mobTarget,this,verbalCastCode(mob,target,auto),null);
			if((R.okMessage(mob,msg))&&((mobTarget==null)||(R.okMessage(mob,msg2))))
			{
				R.send(mob,msg);
				if(mobTarget!=null)
					R.send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					int damage=100+adjustedLevel(mob,asLevel)-target.phyStats().level();
					if(CMLib.flags().isABonusItems(target))
						damage=(int)Math.round(CMath.div(damage,2.0));
					switch(target.material()&RawMaterial.MATERIAL_MASK)
					{
					case RawMaterial.MATERIAL_PAPER:
					case RawMaterial.MATERIAL_CLOTH:
					case RawMaterial.MATERIAL_VEGETATION:
					case RawMaterial.MATERIAL_SYNTHETIC:
					case RawMaterial.MATERIAL_LEATHER:
					case RawMaterial.MATERIAL_FLESH:
						damage=(int)Math.round(CMath.div(damage,3.0));
						break;
					case RawMaterial.MATERIAL_WOODEN:
						damage=(int)Math.round(CMath.div(damage,1.5));
						break;
					case RawMaterial.MATERIAL_GLASS:
					case RawMaterial.MATERIAL_ROCK:
						damage=(int)Math.round(CMath.mul(damage,2.0));
						break;
					case RawMaterial.MATERIAL_PRECIOUS:
						break;
					case RawMaterial.MATERIAL_ENERGY:
					case RawMaterial.MATERIAL_GAS:
						damage=0;
						break;
					}
					if((damage>0)&&(target.subjectToWearAndTear()))
						target.setUsesRemaining(target.usesRemaining()-damage);
					else
					{
						R.show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> seems otherwise unaffected."));
						return true;
					}
					if(target.usesRemaining()>0)
						target.recoverPhyStats();
					else
					{
						target.setUsesRemaining(100);
						if(mobTarget==null)
							R.show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> is destroyed!"));
						else
							R.show(mobTarget,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME>, possessed by <S-NAME>, is destroyed!"));
						target.unWear();
						target.destroy();
						R.recoverRoomStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> attempt(s) a shattering spell, but nothing happens."));

		// return whether it worked
		return success;
	}
}

