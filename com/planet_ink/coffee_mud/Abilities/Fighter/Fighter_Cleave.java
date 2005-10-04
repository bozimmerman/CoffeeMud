package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Fighter_Cleave extends StdAbility
{
	public String ID() { return "Fighter_Cleave"; }
	public String name(){ return "Cleave";}
	public String displayText(){ return "";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	private MOB thisTarget=null;
	private MOB nextTarget=null;
	public int classificationCode(){ return Ability.SKILL;	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if((thisTarget!=null)
		&&(nextTarget!=null)
		&&(thisTarget.amDead())
		&&(!nextTarget.amDead())
		&&(nextTarget.location()==mob.location())
		&&(mob.location().isInhabitant(nextTarget)))
		{
			Item w=mob.fetchWieldedItem();
			if(w==null) w=mob.myNaturalWeapon();
            FullMsg msg=new FullMsg(mob,nextTarget,this,CMMsg.MSG_NOISYMOVEMENT,"^F^<FIGHT^><S-NAME> CLEAVE(S) INTO <T-NAME>!!^</FIGHT^>^?");
            CMColor.fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
                mob.location().send(mob,msg);
				MUDFight.postAttack(mob,nextTarget,w);
				helpProfficiency(mob);
			}
		}
		thisTarget=null;
		nextTarget=null;
		return true;
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(mob.getVictim()!=null)
		&&(msg.amITarget(mob.getVictim()))
		&&(!msg.amITarget(mob))
		&&(!mob.getVictim().amDead())
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon))
		{
			MOB victim=mob.getVictim();
			Weapon w=(Weapon)msg.tool();
			int damAmount=msg.value();

			if((damAmount>victim.curState().getHitPoints())
			&&(w.weaponType()==Weapon.TYPE_SLASHING)
			&&(w.weaponClassification()!=Weapon.CLASS_NATURAL)
			&&(Sense.aliveAwakeMobile(mob,true))
			&&((mob.fetchAbility(ID())==null)||profficiencyCheck(mob,0,false)))
			{
				nextTarget=null;
				thisTarget=null;
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB vic=mob.location().fetchInhabitant(i);
					if((vic!=null)
					&&(vic.getVictim()==mob)
					&&(vic!=mob)
					&&(vic!=victim)
					&&(!vic.amDead())
					&&(vic.rangeToTarget()==0))
					{
						nextTarget=vic;
						break;
					}
				}
				if(nextTarget!=null)
					thisTarget=victim;
			}
		}
		return true;
	}

}
