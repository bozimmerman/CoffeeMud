package com.planet_ink.coffee_mud.Abilities.Poisons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Poison_Firebreather extends Poison_Liquor
{
	public String ID() { return "Poison_Firebreather"; }
	public String name(){ return "Firebreather";}
	private static final String[] triggerStrings = {"LIQUORFIRE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int POISON_TICKS(){return 35;}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((Dice.rollPercentage()<10)&&(!((MOB)affected).isMonster()))
			{
				Ability A=CMClass.getAbility("Disease_Migraines");
				if((A!=null)&&(mob.fetchEffect(A.ID())==null))
					A.invoke(mob,mob,true);
			}
			CommonMsgs.stand(mob,true);
		}
		super.unInvoke();
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if((affected==null)||(invoker==null)) return false;
		if(!(affected instanceof MOB)) return super.tick(ticking,tickID);

		MOB mob=(MOB)affected;
		Room room=mob.location();
		if((Dice.rollPercentage()<15)&&(Sense.aliveAwakeMobile(mob,true))&&(room!=null))
		{
			if(Dice.rollPercentage()<40)
			{
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> belch(es) fire!"+CommonStrings.msp("fireball.wav",20));
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB target=room.fetchInhabitant(i);

					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_FIRE,null);
					if((mob!=target)&&(mob.mayPhysicallyAttack(target))&&(room.okMessage(mob,msg)))
					{
						room.send(mob,msg);
						invoker=mob;

						int damage = 0;
						int maxDie =  mob.envStats().level();
						if (maxDie > 10)
							maxDie = 10;
						damage += Dice.roll(maxDie,6,1);
						if(msg.value()>0)
							damage = (int)Math.round(Util.div(damage,2.0));
						MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.MASK_SOUND|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"^FThe fire <DAMAGE> <T-NAME>!^?");
					}
				}
			}
			else
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> belch(es) smoke!");
			disableHappiness=true;
		}
		return super.tick(ticking,tickID);
	}
}