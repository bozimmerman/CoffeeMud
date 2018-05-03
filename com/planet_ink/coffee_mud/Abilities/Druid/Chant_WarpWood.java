package com.planet_ink.coffee_mud.Abilities.Druid;
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

public class Chant_WarpWood extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_WarpWood";
	}

	private final static String localizedName = CMLib.lang().L("Warp Wood");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS;
	}

	public Item getPossibility(MOB mobTarget)
	{
		if(mobTarget!=null)
		{
			final Vector<Item> goodPossibilities=new Vector<Item>();
			final Vector<Item> possibilities=new Vector<Item>();
			for(int i=0;i<mobTarget.numItems();i++)
			{
				final Item item=mobTarget.getItem(i);
				if((item!=null)
				   &&((item.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
				   &&(item.subjectToWearAndTear()))
				{
					if(item.amWearingAt(Wearable.IN_INVENTORY))
						possibilities.addElement(item);
					else
						goodPossibilities.addElement(item);
				}
				if(goodPossibilities.size()>0)
					return goodPossibilities.elementAt(CMLib.dice().roll(1,goodPossibilities.size(),-1));
				else
				if(possibilities.size()>0)
					return possibilities.elementAt(CMLib.dice().roll(1,possibilities.size(),-1));
			}
		}
		return null;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				final Item targetItem=getPossibility((MOB)target);
				if(targetItem==null)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB mobTarget=getTarget(mob,commands,givenTarget,true,false);
		Item target=getPossibility(mobTarget);
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;
		if(((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
		||(!target.subjectToWearAndTear()))
		{
			mob.tell(L("That can't be warped."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> starts warping!"):L("^S<S-NAME> chant(s) at <T-NAMESELF>.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,mobTarget,this,verbalCastCode(mob,mobTarget,auto),null);
			if((mob.location().okMessage(mob,msg))&&((mobTarget==null)||(mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(mobTarget!=null)
					mob.location().send(mob,msg2);
				if(msg.value()<=0)
				{
					int damage=100+(mob.phyStats().level()+(2*getXLEVELLevel(mob)))-target.phyStats().level();
					if(CMLib.flags().isABonusItems(target))
						damage=(int)Math.round(CMath.div(damage,2.0));
					target.setUsesRemaining(target.usesRemaining()-damage);
					if(mobTarget==null)
						mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> begin(s) to twist and warp!"));
					else
						mob.location().show(mobTarget,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME>, possessed by <S-NAME>, twists and warps!"));
					if(target.usesRemaining()>0)
						target.recoverPhyStats();
					else
					{
						target.setUsesRemaining(100);
						mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> is destroyed!"));
						target.unWear();
						target.destroy();
						mob.location().recoverRoomStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> chant(s), but nothing happens."));

		// return whether it worked
		return success;
	}
}
