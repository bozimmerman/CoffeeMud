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
public class Play_Ballad extends Play
{
	public String ID() { return "Play_Ballad"; }
	public String name(){ return "Ballad";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String songOf(){return "a "+name();}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		// the sex rules
		if(!(affected instanceof MOB)) return;

		MOB myChar=(MOB)affected;
		if((msg.target()!=null)&&(msg.target() instanceof MOB))
		{
			MOB mate=(MOB)msg.target();
			if((msg.amISource(myChar))
			&&(msg.tool()!=null)
			&&(msg.tool().ID().equals("Social"))
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>"))
			&&(myChar.charStats().getStat(CharStats.GENDER)!=mate.charStats().getStat(CharStats.GENDER))
			&&((mate.charStats().getStat(CharStats.GENDER)==('M'))
			   ||(mate.charStats().getStat(CharStats.GENDER)==('F')))
			&&((myChar.charStats().getStat(CharStats.GENDER)==('M'))
			   ||(myChar.charStats().getStat(CharStats.GENDER)==('F')))
			&&((myChar.charStats().getMyRace().ID().equals("Human"))
			   ||(mate.charStats().getMyRace().ID().equals("Human"))
			   ||(mate.charStats().getMyRace().ID().equals(ID())))
			&&(myChar.charStats().getMyRace().fertile())
			&&(mate.charStats().getMyRace().fertile())
			&&(myChar.location()==mate.location())
			&&(myChar.numWearingHere(Item.ON_LEGS)==0)
			&&(mate.numWearingHere(Item.ON_LEGS)==0)
			&&(myChar.numWearingHere(Item.ON_WAIST)==0)
			&&(mate.numWearingHere(Item.ON_WAIST)==0)
			&&((mate.charStats().getStat(CharStats.AGE)==0)
			        ||((mate.charStats().ageCategory()>Race.AGE_CHILD)
			                &&(mate.charStats().ageCategory()<Race.AGE_OLD)))
			&&((myChar.charStats().getStat(CharStats.AGE)==0)
			        ||((myChar.charStats().ageCategory()>Race.AGE_CHILD)
			                &&(myChar.charStats().ageCategory()<Race.AGE_OLD))))
			{
				MOB female=myChar;
				MOB male=mate;
				if((mate.charStats().getStat(CharStats.GENDER)==('F')))
				{
					female=mate;
					male=myChar;
				}
				Ability A=CMClass.getAbility("Pregnancy");
				if((A!=null)
				&&(female.fetchAbility(A.ID())==null)
				&&(female.fetchEffect(A.ID())==null))
				{
					A.invoke(male,female,true,0);
					unInvoke();
				}
			}
		}
	}
	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		if(invoker()!=null)
			stats.setStat(CharStats.SAVE_MIND,stats.getStat(CharStats.SAVE_MIND)+(invokerLevel()*1));
	}
	public void affectEnvStats(Environmental mob, EnvStats stats)
	{
		super.affectEnvStats(mob,stats);
		if(invoker()!=null)
			stats.setAttackAdjustment(stats.attackAdjustment()+invoker().charStats().getStat(CharStats.CHARISMA)+(invokerLevel()/2));
	}
}
