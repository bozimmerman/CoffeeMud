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

public class Fighter_CoupDeGrace extends StdAbility
{
	public String ID() { return "Fighter_CoupDeGrace"; }
	public String name(){ return "Coup de Grace";}
	private static final String[] triggerStrings = {"COUP","COUPDEGRACE"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int maxRange(){return 0;}
	public int classificationCode(){ return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to do this!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to try that!");
			return false;
		}
		Item w=mob.fetchWieldedItem();
		Weapon ww=null;
		if(!auto)
		{
			if((w==null)||(!(w instanceof Weapon)))
			{
				mob.tell("You cannot coup de grace without a weapon!");
				return false;
			}
			ww=(Weapon)w;
			if((ww.weaponType()!=Weapon.TYPE_SLASHING)
			&&(ww.weaponType()!=Weapon.TYPE_PIERCING))
			{
				mob.tell("You cannot coup de grace with a "+ww.name()+"!");
				return false;
			}
			if(mob.curState().getMovement()<150)
			{
				mob.tell("You don't have the energy to try it.");
				return false;
			}
			if(!Sense.isSleeping(mob.getVictim()))
			{
				mob.tell(mob.getVictim().charStats().HeShe()+" is not prone!");
				return false;
			}
		}

		MOB target=mob.getVictim();
		int dmg=target.curState().getHitPoints();
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-adjustedLevel(mob,asLevel);
		if(levelDiff>0)
			levelDiff=levelDiff*3;
		else
			levelDiff=0;
		mob.curState().adjMovement(-150,mob.maxState());
		int chance=(-levelDiff)+(-(target.charStats().getStat(CharStats.CONSTITUTION)*2));
		boolean hit=(auto)||(Dice.normalizeAndRollLess(mob.adjustedAttackBonus(mob.getVictim())+mob.getVictim().adjustedArmor()));
		boolean success=profficiencyCheck(mob,chance,auto)&&(hit);
		if((success)&&((dmg<50)||(dmg<(target.maxState().getHitPoints()/4))))
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.curState().setHitPoints(0);
				MUDFight.postDamage(mob,target,ww,dmg,CMMsg.MSG_WEAPONATTACK,ww.weaponClassification(),auto?"":"^F^<FIGHT^><S-NAME> rear(s) back and Coup de Graces <T-NAME>!^</FIGHT^>^?"+CommonStrings.msp("decap.wav",30));
				mob.location().recoverRoomStats();
			}
		}
		else
		{
			String str=auto?"":"<S-NAME> attempt(s) a Coup de Grace and fail(s)!";
			FullMsg msg=new FullMsg(mob,target,null,CMMsg.MASK_MALICIOUS|CMMsg.MSG_OK_ACTION,str);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		return success;
	}

}
