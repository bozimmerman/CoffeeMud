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

public class Fighter_DeflectProjectile extends StdAbility
{
	public String ID() { return "Fighter_DeflectProjectile"; }
	public String name(){ return "Deflect Projectile";}
	public String displayText(){ return "";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){ return Ability.SKILL;}
	public boolean doneThisRound=false;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(!doneThisRound)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)
		   ||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN))
		&&(!(msg.tool() instanceof Electronics))
		&&(mob.rangeToTarget()>0)
		&&(mob.fetchEffect("Fighter_CatchProjectile")==null)
		&&(mob.fetchEffect("Fighter_ReturnProjectile")==null)
		&&(mob.charStats().getBodyPart(Race.BODY_ARM)>0)
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(null,-85+mob.charStats().getStat(CharStats.DEXTERITY),false))
		&&(mob.freeWearPositions(Item.HELD)>0))
		{
			Item w=(Item)msg.tool();
			if((((Weapon)w).weaponClassification()==Weapon.CLASS_THROWN)
			&&(msg.source().isMine(w)))
			{
				if(!w.amWearingAt(Item.INVENTORY))
					CommonMsgs.remove(msg.source(),w,true);
				CommonMsgs.drop(msg.source(),w,true,false);
				if(!mob.location().isContent(w))
					return true;
			}
			FullMsg msg2=new FullMsg(mob,w,msg.source(),CMMsg.MSG_GET,"<S-NAME> deflect(s) the <T-NAME> shot by <O-NAME>!");
			if(mob.location().okMessage(mob,msg2))
			{
				mob.location().send(mob,msg2);
				doneThisRound=true;
				helpProfficiency(mob);
				return false;
			}
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}
}