package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Chant_Goodberry extends Chant
{
	public String ID() { return "Chant_Goodberry"; }
	public String name(){ return "Goodberry";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_ITEMS;}

	public boolean checkDo(Item newTarget, Item originaltarget, Environmental owner)
	{
		if((newTarget!=null)
		&&(newTarget instanceof Food)
		&&(!(newTarget instanceof Pill))
		&&(isBerry(newTarget))
		&&(newTarget.container()==originaltarget.container())
		&&(newTarget.name().equals(originaltarget.name())))
		{
			Pill newItem=(Pill)CMClass.getItem("GenPill");
			newItem.setName(newTarget.name());
			newItem.setDisplayText(newTarget.displayText());
			newItem.setDescription(newTarget.description());
			newItem.setMaterial(RawMaterial.RESOURCE_BERRIES);
			newItem.baseEnvStats().setDisposition(EnvStats.IS_GLOWING);
			newItem.setSpellList(";Prayer_CureLight;");
			newItem.recoverEnvStats();
			newItem.setMiscText(newItem.text());
			Item location=newTarget.container();
			newTarget.destroy();
			if(owner instanceof MOB)
				((MOB)owner).addInventory(newItem);
			else
			if(owner instanceof Room)
				((Room)owner).addItemRefuse(newItem,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
			newItem.setContainer(location);
			return true;
		}
		return false;
	}

	public boolean isBerry(Item I) { return CMParms.contains(RawMaterial.CODES.BERRIES(),I.material()); }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;

		Environmental owner=target.owner();
		if(owner==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if((!(target instanceof Food))
		||(!isBerry(target)))
		{
			mob.tell("This magic will not work on "+target.name()+".");
			return false;
		}

		if(success)
		{
			int numAffected=CMLib.dice().roll(1,adjustedLevel(mob,asLevel)/7,1);
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> begin to glow!");
				if(owner instanceof MOB)
					for(int i=0;i<((MOB)owner).inventorySize();i++)
					{
						Item newTarget=((MOB)owner).fetchInventory(i);
						if((newTarget!=null)&&(checkDo(newTarget,target,owner)))
						{
							if((--numAffected)==0)
								break;
							i=-1;
						}
					}
				if(owner instanceof Room)
					for(int i=0;i<((Room)owner).numItems();i++)
					{
						Item newTarget=((Room)owner).fetchItem(i);
						if((newTarget!=null)&&(checkDo(newTarget,target,owner)))
						{
							if((--numAffected)==0)
								break;
							i=-1;
						}
					}
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
