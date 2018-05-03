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
public class Chant_Goodberry extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Goodberry";
	}

	private final static String	localizedName	= CMLib.lang().L("Goodberry");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_PLANTCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	public boolean checkDo(Item newTarget, Item originaltarget, Environmental owner)
	{
		if((newTarget!=null)
		&&(newTarget instanceof Food)
		&&(!(newTarget instanceof Pill))
		&&(isBerry(newTarget))
		&&(newTarget.container()==originaltarget.container())
		&&(newTarget.name().equals(originaltarget.name())))
		{
			final Pill newItem=(Pill)CMClass.getItem("GenPill");
			newItem.setName(newTarget.name());
			newItem.setDisplayText(newTarget.displayText());
			newItem.setDescription(newTarget.description());
			newItem.setMaterial(RawMaterial.RESOURCE_BERRIES);
			newItem.setNourishment(0);
			newItem.basePhyStats().setDisposition(PhyStats.IS_GLOWING);
			newItem.setSpellList(";Prayer_CureLight;");
			newItem.recoverPhyStats();
			newItem.setMiscText(newItem.text());
			final Container location=newTarget.container();
			newTarget.destroy();
			if(owner instanceof MOB)
				((MOB)owner).addItem(newItem);
			else
			if(owner instanceof Room)
				((Room)owner).addItem(newItem,ItemPossessor.Expire.Player_Drop);
			newItem.setContainer(location);
			return true;
		}
		return false;
	}

	public boolean isBerry(Item I)
	{
		return CMParms.contains(RawMaterial.CODES.BERRIES(), I.material());
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		final Environmental owner=target.owner();
		if(owner==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if((!(target instanceof Food))
		||(!isBerry(target)))
		{
			mob.tell(L("This magic will not work on @x1.",target.name(mob)));
			return false;
		}

		if(success)
		{
			int numAffected=CMLib.dice().roll(1,adjustedLevel(mob,asLevel)/7,1);
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> begin to glow!"));
				if(owner instanceof MOB)
				{
					for(int i=0;i<((MOB)owner).numItems();i++)
					{
						final Item newTarget=((MOB)owner).getItem(i);
						if((newTarget!=null)&&(checkDo(newTarget,target,owner)))
						{
							if((--numAffected)==0)
								break;
							i=-1;
						}
					}
				}
				if(owner instanceof Room)
				{
					for(int i=0;i<((Room)owner).numItems();i++)
					{
						final Item newTarget=((Room)owner).getItem(i);
						if((newTarget!=null)&&(checkDo(newTarget,target,owner)))
						{
							if((--numAffected)==0)
								break;
							i=-1;
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
