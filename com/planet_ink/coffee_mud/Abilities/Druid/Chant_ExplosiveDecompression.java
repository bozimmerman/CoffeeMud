package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class Chant_ExplosiveDecompression extends Chant
{
	public String ID() { return "Chant_ExplosiveDecompression"; }
	public String name(){ return "Explosive Decompression";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public boolean bubbleAffect(){return true;}

	public void affectEnvStats(Environmental affecting, EnvStats stats)
	{
		super.affectEnvStats(affected,stats);
		if(affecting instanceof MOB)
			stats.setSensesMask(stats.sensesMask()|EnvStats.CAN_NOT_BREATHE);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;
		if((!auto)&&((target.domainType()&Room.INDOORS)==0))
		{
			mob.tell("This chant only works indoors.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) loudly.  A ball of fire forms around <S-NAME>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The ball of fire **EXPLODES**!");
					for(int i=0;i<target.numInhabitants();i++)
					{
						MOB M=target.fetchInhabitant(i);
						if((M!=null)&&(M!=mob))
						{
							FullMsg msg2=new FullMsg(mob,M,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_FIRE|(auto?CMMsg.MASK_GENERAL:0),null);
							if(mob.location().okMessage(mob,msg2))
							{
								mob.location().send(mob,msg2);
								invoker=mob;
							    int numDice = adjustedLevel(mob,asLevel);
								int damage = Dice.roll(numDice, 10, 50);
								if(msg2.value()>0)
									damage = (int)Math.round(Util.div(damage,2.0));
								MUDFight.postDamage(mob,M,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The flaming blast <DAMAGE> <T-NAME>!");
							}
							if((M.charStats().getBodyPart(Race.BODY_FOOT)>0)
							&&(!Sense.isFlying(M))&&(!Sense.isSitting(M))&&(!Sense.isSleeping(M)))
								mob.location().show(M,null,CMMsg.MASK_GENERAL|CMMsg.TYP_SIT,"<S-NAME> <S-IS-ARE> blown off <S-HIS-HER> feet!");
						}
					}
					maliciousAffect(mob,target,asLevel,20,-1);
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The fire burns off all the air here!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) loudly, but nothing happens.");
		// return whether it worked
		return success;
	}
}
