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

public class Fighter_CoverDefence extends StdAbility
{
	public int hits=0;
	public String ID() { return "Fighter_CoverDefence"; }
	public String name(){ return "Cover Defence";}
	public String displayText(){return "";};
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){ return Ability.SKILL; }

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		   &&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(msg.source().rangeToTarget()>0)
		   &&(mob.envStats().height()<84)
		   &&(msg.tool()!=null)
		   &&(msg.tool() instanceof Weapon)
		   &&((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)
			  ||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN))
		   &&(profficiencyCheck(null,mob.charStats().getStat(CharStats.DEXTERITY)-90,false))
		   &&(msg.source().getVictim()==mob))
		{
			FullMsg msg2=new FullMsg(msg.source(),mob,null,CMMsg.MSG_QUIETMOVEMENT,"<T-NAME> take(s) cover from <S-YOUPOSS> attack!");
			if(mob.location().okMessage(mob,msg2))
			{
				mob.location().send(mob,msg2);
				helpProfficiency(mob);
				return false;
			}
		}
		return true;
	}
}