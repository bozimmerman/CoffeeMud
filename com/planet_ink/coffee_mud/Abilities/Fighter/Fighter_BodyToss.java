package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Fighter_BodyToss extends StdAbility
{
	public String ID() { return "Fighter_BodyToss"; }
	public String name(){ return "Body Toss";}
	private static final String[] triggerStrings = {"BODYTOSS"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean anyWeapons(MOB mob)
	{
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			   &&((I.amWearingAt(Item.WIELD))
			      ||(I.amWearingAt(Item.HELD))))
				return true;
		}
		return false;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob.getVictim();
		if(target==null)
		{
			mob.tell("You can only do this in combat!");
			return false;
		}
		if(anyWeapons(mob))
		{
			mob.tell("You must be unarmed to use this skill.");
			return false;
		}
		if(mob.rangeToTarget()>0)
		{
			mob.tell("You must get closer to "+target.charStats().himher()+" first!");
			return false;
		}
		if(Sense.isSitting(mob))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if(mob.charStats().getBodyPart(Race.BODY_ARM)<=1)
		{
			mob.tell("You need arms to do this.");
			return false;
		}
		if(target.baseEnvStats().weight()>(mob.baseEnvStats().weight()*2))
		{
			mob.tell(target.name()+" is too big for you to toss!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),"<S-NAME> pick(s) up <T-NAMESELF> and toss(es) <T-HIM-HER> into the air!");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int dist=2;
				if(mob.location().maxRange()<2) dist=mob.location().maxRange();
				mob.setAtRange(dist);
				target.setAtRange(dist);
				MUDFight.postDamage(mob,target,this,Dice.roll(1,12,0),CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,Weapon.TYPE_BASHING,"The hard landing <DAMAGE> <T-NAME>!");
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to pick up <T-NAMESELF>, but fail(s).");

		// return whether it worked
		return success;
	}
}
