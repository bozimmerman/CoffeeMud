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

public class Chant_ManicMoon extends Chant
{
	public String ID() { return "Chant_ManicMoon"; }
	public String name(){ return "Manic Moon";}
	public String displayText(){return "(Manic Moon)";}
	public int quality(){ return MALICIOUS;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public long flags(){return FLAG_MOONCHANGING;}

	public void unInvoke()
	{
		if(canBeUninvoked())
			if(affected instanceof Room)
				((Room)affected).showHappens(CMMsg.MSG_OK_VISUAL,"The manic moon sets.");

		super.unInvoke();

	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(affected instanceof Room)
		{
			Room room=(Room)affected;
			if(!Chant_BlueMoon.moonInSky(room,this))
				unInvoke();
			else
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=room.fetchInhabitant(i);
				if((M!=null)&&(M!=invoker))
				{
					if(M.isInCombat())
					{
						if(Dice.rollPercentage()<20)
							M.setVictim(null);
						else
						{
							MOB newvictim=M.location().fetchInhabitant(Dice.roll(1,M.location().numInhabitants(),-1));
							if(newvictim!=M) M.setVictim(newvictim);
						}
					}
					else
					if(Dice.rollPercentage()<20)
					{
						MOB newvictim=M.location().fetchInhabitant(Dice.roll(1,M.location().numInhabitants(),-1));
						if(newvictim!=M) M.setVictim(newvictim);
					}
				}
			}
			MOB M=room.fetchInhabitant(Dice.roll(1,room.numInhabitants(),-1));
			if((Dice.rollPercentage()<50)&&(M!=null)&&(M!=invoker))
				switch(Dice.roll(1,5,0))
				{
				case 1:
					room.show(M,null,CMMsg.MSG_NOISE,"<S-NAME> howl(s) at the moon!");
					break;
				case 2:
					room.show(M,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> wig(s) out!");
					break;
				case 3:
					room.show(M,null,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> get(s) confused!");
					break;
				case 4:
					room.show(M,null,CMMsg.MSG_NOISE,"<S-NAME> sing(s) randomly!");
					break;
				case 5:
					room.show(M,null,CMMsg.MSG_NOISE,"<S-NAME> go(es) nuts!");
					break;
				}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(!Chant_BlueMoon.moonInSky(mob.location(),null))
		{
			mob.tell("You must be able to see the moon for this magic to work.");
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already under the manic moon.");
			return false;
		}
		for(int a=0;a<target.numEffects();a++)
		{
			Ability A=target.fetchEffect(a);
			if((A!=null)
			&&(Util.bset(A.flags(),Ability.FLAG_MOONCHANGING)))
			{
				mob.tell("The moon is already under "+A.name()+", and can not be changed until this magic is gone.");
				return false;
			}
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the sky.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The Manic Moon Rises!");
					beneficialAffect(mob,target,asLevel,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the sky, but the magic fades.");
		// return whether it worked
		return success;
	}
}
