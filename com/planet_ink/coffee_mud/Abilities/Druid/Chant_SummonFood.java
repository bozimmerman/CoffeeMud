package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Chant_SummonFood extends Chant
{
	public String ID() { return "Chant_SummonFood"; }
	public String name(){ return "Summon Food";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors to try this.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Food newItem=null;
				int berryType=EnvResource.BERRIES[Dice.roll(1,EnvResource.BERRIES.length,-1)];
				for(int i=0;i<((adjustedLevel(mob)/4)+1);i++)
				{
					newItem=(Food)CMClass.getStdItem("GenFoodResource");
					newItem.setName("some "+EnvResource.RESOURCE_DESCS[berryType&EnvResource.RESOURCE_MASK].toLowerCase());
					newItem.setDisplayText(Util.capitalize(newItem.name())+" are growing here.");
					newItem.setDescription("These little berries look juicy and good.");
					newItem.setMaterial(berryType);
					newItem.setNourishment(150);
					newItem.setBaseValue(1);
					newItem.setMiscText(newItem.text());
					mob.location().addItemRefuse(newItem,Item.REFUSE_RESOURCE);
				}
				if(newItem!=null)
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,Util.capitalize(newItem.name())+" quickly begin to grow here.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the ground, but nothing happens.");

		// return whether it worked
		return success;
	}
}
