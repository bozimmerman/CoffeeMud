package com.planet_ink.coffee_mud.Abilities.Ranger;
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
public class Ranger_AnimalFrenzy extends StdAbility
{
	public String ID() { return "Ranger_AnimalFrenzy"; }
	public String name(){ return "Animal Frenzy";}
	public String displayText(){return "";}
	public int quality(){return Ability.OK_OTHERS;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	protected Vector rangersGroup=null;
	public int classificationCode(){ return Ability.SKILL;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return false;
		if(invoker==null)
		{
			if(Sense.isAnimalIntelligence((MOB)affected)
			&&(((MOB)affected).isMonster()))
				return true;
			invoker=(MOB)affected;
		}
		if(invoker!=affected) return true;
		if(rangersGroup==null)
			rangersGroup=new Vector();

		if(rangersGroup!=null)
		{
			HashSet H=invoker.getGroupMembers(new HashSet());
			for(Iterator e=H.iterator();e.hasNext();)
			{
				MOB mob=(MOB)e.next();
				if((!rangersGroup.contains(mob))
				&&(mob!=invoker)
				&&(mob.location()==invoker.location())
				&&(Sense.isAnimalIntelligence(mob)))
				{
					rangersGroup.addElement(mob);
					mob.addNonUninvokableEffect((Ability)this.copyOf());
				}
			}
			for(int i=rangersGroup.size()-1;i>=0;i--)
			{
				try
				{
					MOB mob=(MOB)rangersGroup.elementAt(i);
					if((!H.contains(mob))
					||(mob.location()!=invoker.location()))
					{
						Ability A=mob.fetchEffect(this.ID());
						if((A!=null)&&(A.invoker()==invoker))
							mob.delEffect(A);
						rangersGroup.removeElement(mob);
					}
				}
				catch(java.lang.ArrayIndexOutOfBoundsException e)
				{
				}
			}
			if((Dice.rollPercentage()==1)
			   &&(invoker.isInCombat())
			   &&(rangersGroup.size()>0))
				helpProfficiency(invoker);
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((invoker!=null)&&(affected!=invoker)&&(invoker.isInCombat()))
		{
			int invoAtt=(int)Math.round(Util.mul(Util.div(profficiency(),100.0),invoker.envStats().attackAdjustment()));
			int damBonus=(int)Math.round(Util.mul(affectableStats.damage(),(Util.div(profficiency(),100.0)*4.0)));
			affectableStats.setDamage(affectableStats.damage()+damBonus);
			if(affectableStats.attackAdjustment()<invoAtt)
				affectableStats.setAttackAdjustment(invoAtt);
		}
	}
}
