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
public class Chant_Reabsorb extends Chant
{
	public String ID() { return "Chant_Reabsorb"; }
	public String name(){return "Reabsorb";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
	protected int canTargetCode(){return CAN_ITEMS;}
	protected int canAffectCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(!(target instanceof Item))
                return Ability.QUALITY_INDIFFERENT;
            Room R=mob.location();
            if(R!=null)
            {
                int type=R.domainType();
                if((type==Room.DOMAIN_INDOORS_STONE)
                ||(type==Room.DOMAIN_INDOORS_WOOD)
                ||(type==Room.DOMAIN_INDOORS_MAGIC)
                ||(type==Room.DOMAIN_INDOORS_UNDERWATER)
                ||(type==Room.DOMAIN_INDOORS_WATERSURFACE)
                ||(type==Room.DOMAIN_OUTDOORS_AIR)
                ||(type==Room.DOMAIN_OUTDOORS_CITY)
                ||(type==Room.DOMAIN_OUTDOORS_SPACEPORT)
                ||(type==Room.DOMAIN_OUTDOORS_UNDERWATER)
                ||(type==Room.DOMAIN_OUTDOORS_WATERSURFACE))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=this.getTarget(mob,mob.location(),givenTarget,null,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;
		
        Vector V=CMLib.utensils().getDeadBodies(target);
        for(int v=0;v<V.size();v++)
        {
        	DeadBody D=(DeadBody)V.elementAt(v);
			if((D!=null)
			&&(D.playerCorpse())
			&&(!D.mobName().equals(mob.Name())))
			{
				mob.tell("You are not allowed to reabsorb a player corpse.");
				return false;
			}
        }
		if(!(target.owner() instanceof Room))
		{
			mob.tell("You need to put "+target.name()+" on the ground first.");
			return false;
		}
		int type=mob.location().domainType();
		if((type==Room.DOMAIN_INDOORS_STONE)
		    ||(type==Room.DOMAIN_INDOORS_WOOD)
		    ||(type==Room.DOMAIN_INDOORS_MAGIC)
		    ||(type==Room.DOMAIN_INDOORS_UNDERWATER)
		    ||(type==Room.DOMAIN_INDOORS_WATERSURFACE)
		    ||(type==Room.DOMAIN_OUTDOORS_AIR)
			||(type==Room.DOMAIN_OUTDOORS_CITY)
		    ||(type==Room.DOMAIN_OUTDOORS_SPACEPORT)
			||(type==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(type==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("That magic won't work here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> starts vibrating!":"^S<S-NAME> chant(s), causing <T-NAMESELF> to decay!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The ground reabsorbs "+target.name()+".");
					target.destroy();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAME>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
