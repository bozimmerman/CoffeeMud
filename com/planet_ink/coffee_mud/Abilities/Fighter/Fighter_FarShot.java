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

public class Fighter_FarShot extends StdAbility
{
	public String ID() { return "Fighter_FarShot"; }
	public String name(){ return "Far Shot";}
	public String displayText(){ return "";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.SKILL;}
	public int checkDown=4;

	protected Vector qualifiedWeapons=new Vector();

	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.source()==affected)
		&&(msg.target() instanceof Weapon)
		&&(((Weapon)msg.target()).weaponClassification()==Weapon.CLASS_RANGED)
		&&(((Weapon)msg.target()).ammunitionType().length()>0))
		{
			if(((msg.targetMinor()==CMMsg.TYP_WEAR)
			   ||(msg.targetMinor()==CMMsg.TYP_WIELD)
			   ||(msg.targetMinor()==CMMsg.TYP_HOLD))
			&&(!qualifiedWeapons.contains(msg.target()))
			&&((msg.source().fetchAbility(ID())==null)||profficiencyCheck(null,0,false)))
			{
				qualifiedWeapons.addElement(msg.target());
				Ability A=(Ability)this.copyOf();
				A.setBorrowed(msg.target(),true);
				msg.target().addEffect(A);
			}
			else
			if(((msg.targetMinor()==CMMsg.TYP_REMOVE)
				||(msg.targetMinor()==CMMsg.TYP_DROP))
			&&(qualifiedWeapons.contains(msg.target())))
			{
				qualifiedWeapons.removeElement(msg.target());
				msg.target().delEffect(msg.target().fetchEffect(ID()));
			}
		}
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof Item)
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.SENSE_ITEMNOMAXRANGE);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(--checkDown<=0)
		{
			checkDown=5;
			Item w=mob.fetchWieldedItem();
			if((w!=null)
			&&(w instanceof Weapon)
			&&(((Weapon)w).weaponClassification()==Weapon.CLASS_RANGED)
			&&(((Weapon)w).ammunitionType().length()>0)
			&&((mob.fetchAbility(ID())==null)||profficiencyCheck(null,0,false)))
			{
				if((Dice.rollPercentage()==1)&&(Dice.rollPercentage()<10))
					helpProfficiency(mob);
				if(!qualifiedWeapons.contains(w))
				{
					qualifiedWeapons.addElement(w);
					Ability A=(Ability)this.copyOf();
					A.setBorrowed(w,true);
					w.addEffect(A);
				}
			}
			for(int i=qualifiedWeapons.size()-1;i>=0;i--)
			{
				Item I=(Item)qualifiedWeapons.elementAt(i);
				if((I.amWearingAt(Item.INVENTORY))
				||(I.owner()!=affected))
				{
					qualifiedWeapons.removeElement(I);
					I.delEffect(I.fetchEffect(ID()));
				}
			}
		}
		return true;
	}
}
