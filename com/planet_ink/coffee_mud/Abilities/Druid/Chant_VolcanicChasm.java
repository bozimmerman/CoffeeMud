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

public class Chant_VolcanicChasm extends Chant
{
	public String ID() { return "Chant_VolcanicChasm"; }
	public String name(){ return "Volcanic Chasm";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			Room R=(Room)affected;
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if((M!=null)&&(Dice.rollPercentage()>M.charStats().getSave(CharStats.SAVE_FIRE)))
                {
					MUDFight.postDamage(invoker(),M,this,Dice.roll(1,M.envStats().level(),1),CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_MELTING,"The extreme heat <DAMAGE> <T-NAME>!");
                    if((!M.isInCombat())&&(M!=invoker)&&(M.location().isInhabitant(invoker))&&(Sense.canBeSeenBy(invoker,M)))
                        MUDFight.postAttack(M,invoker,M.fetchWieldedItem());
                }
			}
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)&&(!Sense.isOnFire(I)))
				{
					Ability A=CMClass.getAbility("Burning");
					if(A!=null)	A.invoke(invoker(),I,true,0);
				}
			}
		}
		return true;
	}

	protected boolean checked=false;
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((!checked)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(affected instanceof Room))
		{
			checked=true;
			if(!CMClass.ThreadEngine().isTicking(this,-1))
				CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_SPELL_AFFECT,1);
		}
		super.executeMsg(host,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE))
		{
			mob.tell("This chant only works in caves.");
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
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the walls.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					for(int i=0;i<target.numInhabitants();i++)
					{
						MOB M=target.fetchInhabitant(i);
						if((M!=null)&&(mob!=M))
							mob.location().show(mob,M,CMMsg.MASK_MALICIOUS|CMMsg.TYP_OK_VISUAL,null);
					}
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Flames and sulfurous steam leap from cracks opening around you!");
					maliciousAffect(mob,target,asLevel,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) the walls, but the magic fades.");
		// return whether it worked
		return success;
	}
}
