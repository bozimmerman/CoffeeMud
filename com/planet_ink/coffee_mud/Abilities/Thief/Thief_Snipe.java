package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_Snipe extends ThiefSkill
{
	public String ID() { return "Thief_Snipe"; }
	public String name(){ return "Snipe";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"SNIPE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int overrideMana(){return 100;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell("Not while in combat!");
			return false;
		}
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!Sense.aliveAwakeMobile(mob,true))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if(Sense.canBeSeenBy(mob,target))
		{
			mob.tell(target.name()+" is watching you too closely.");
			return false;
		}
		Item w=mob.fetchWieldedItem();
		if((w==null)
		||(!(w instanceof Weapon)))
		{
			mob.tell("You need a weapon to snipe.");
			return false;
		}
		Weapon ww=(Weapon)w;
		if(((ww.weaponClassification()!=Weapon.CLASS_RANGED)&&(ww.weaponClassification()!=Weapon.CLASS_THROWN))
		||(w.maxRange()<=0))
		{
			mob.tell("You need a ranged weapon to snipe.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		int code=CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT;
		String str=auto?"":"<S-NAME> strike(s) <T-NAMESELF> from the shadows!";
		int otherCode=success?code:CMMsg.NO_EFFECT;
		if(Util.bset(otherCode,CMMsg.MASK_SOUND))
		    otherCode=Util.unsetb(otherCode,CMMsg.MASK_SOUND);
		String otherStr=success?str:null;
		FullMsg msg=new FullMsg(mob,target,this,code,str,otherCode,otherStr,otherCode,otherStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			MUDFight.postAttack(mob,target,w);
			if(success)
			{
				MOB oldVictim=target.getVictim();
				MOB oldVictim2=mob.getVictim();
				if(oldVictim==mob) target.makePeace();
				if(oldVictim2==target) mob.makePeace();
				if(mob.fetchEffect("Thief_Hide")==null)
				{
					Ability hide=mob.fetchAbility("Thief_Hide");
					if(hide!=null) hide.invoke(mob,null,false,asLevel);

					mob.location().recoverRoomStats();
					if(Sense.canBeSeenBy(mob,target))
					{
						target.setVictim(oldVictim);
						mob.setVictim(oldVictim2);
					}
				}
			}
		}
		return success;
	}
}
