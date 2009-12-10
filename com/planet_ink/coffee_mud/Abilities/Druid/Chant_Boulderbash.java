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
public class Chant_Boulderbash extends Chant
{
	public String ID() { return "Chant_Boulderbash"; }
	public String name(){ return "Boulderbash";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ROCKCONTROL;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int maxRange(){return adjustedMaxInvokerRange(2);}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(!(target instanceof MOB))
                return Ability.QUALITY_INDIFFERENT;
            Room R=mob.location();
            if(R!=null)
            {
                if((R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
                &&(R.domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
                &&(R.domainType()!=Room.DOMAIN_OUTDOORS_ROCKS))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_ROCKS))
		{
			mob.tell("This magic only works in caves, mountainous, or rocky regions, where the rocks will answer to your chant.");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"A boulder flies through the air!":"^S<S-NAME> chant(s) to <T-NAMESELF>.  Suddenly a huge rock flies at <T-HIM-HER>!^?"));
			CMMsg msg2=CMClass.getMsg(mob,target,this,verbalCastMask(mob,target,auto)|CMMsg.TYP_JUSTICE,null);
			if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				int maxDie =  (adjustedLevel( mob, asLevel )+(2*super.getX1Level(mob))) / 2;
				int damage = CMLib.dice().roll(maxDie,6,5+(maxDie/6));
				if((msg.value()>0)||(msg2.value()>0))
					damage = (int)Math.round(CMath.div(damage,1.5));
				if(target.location()==mob.location())
					CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BASHING,"The boulder <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}
