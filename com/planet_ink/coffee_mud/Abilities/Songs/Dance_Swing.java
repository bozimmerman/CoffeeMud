package com.planet_ink.coffee_mud.Abilities.Songs;

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
public class Dance_Swing extends Dance
{
	public String ID() { return "Dance_Swing"; }
	public String name(){ return "Swing";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	private boolean doneThisRound=false;
	protected String danceOf(){return name()+" Dancing";}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		   &&(!doneThisRound)
		   &&(mob.rangeToTarget()==0))
		{
			if((msg.tool()!=null)&&(msg.tool() instanceof Item))
			{
				Item attackerWeapon=(Item)msg.tool();
				if((attackerWeapon!=null)
				&&(attackerWeapon instanceof Weapon)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_FLAILED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_RANGED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_THROWN))
				{
					FullMsg msg2=new FullMsg(mob,msg.source(),null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> parr(ys) "+attackerWeapon.name()+" attack from <T-NAME>!");
					if((profficiencyCheck(null,mob.charStats().getStat(CharStats.DEXTERITY)-70,false))
					&&(mob.location().okMessage(mob,msg2)))
					{
						doneThisRound=true;
						mob.location().send(mob,msg2);
						return false;
					}
				}
			}
		}
		return true;
	}

}
