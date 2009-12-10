package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Spell_Youth extends Spell
{
	public String ID() { return "Spell_Youth"; }
	public String name(){return "Youth";}
	public int overrideMana(){return Integer.MAX_VALUE;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;}
    public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		// it worked, so build a copy of this ability,
		// and add it to the affects list of the
		// affected MOB.  Then tell everyone else
		// what happened.
		CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms around <T-NAMESELF>, drawing forth <T-HIS-HER> youthful self.^?");
		if(success)
		{
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((target.baseCharStats().getStat(CharStats.STAT_AGE)<=0)
			        ||(target.baseCharStats().ageCategory()<=Race.AGE_YOUNGADULT))
				{
				    mob.tell(mob,target,null,"The magic appears to have had no effect upon <T-NAME>.");
				    success=false;
				}
				else
				{
					int[] chart=target.baseCharStats().getMyRace().getAgingChart();
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> grow(s) younger!");
					int cat=target.baseCharStats().ageCategory();
					int age=target.baseCharStats().getStat(CharStats.STAT_AGE);
					if(cat>=Race.AGE_ANCIENT)
					{
						int diff=chart[Race.AGE_ANCIENT]-chart[Race.AGE_VENERABLE];
						age=age-chart[Race.AGE_ANCIENT];
						int num=(diff>0)?(int)Math.abs(Math.floor(CMath.div(age,diff))):0;
						if(num<=0)
						    age=(int)Math.round(CMath.div(chart[cat]+chart[cat-1],2.0));
						else
						    age=target.baseCharStats().getStat(CharStats.STAT_AGE)-diff;
					}
					else
					    age=(int)Math.round(CMath.div(chart[cat]+chart[cat-1],2.0));
					if(target.playerStats()!=null)
					{
					    TimeClock C=CMLib.time().globalClock();
					    target.playerStats().getBirthday()[2]=C.getYear()-age;
					    int day=C.getDayOfMonth();
					    int month=C.getMonth();
					    int bday=mob.playerStats().getBirthday()[0];
					    int bmonth=mob.playerStats().getBirthday()[1];
					    if((month<bmonth)||((month==bmonth)&&(day<bday)))
					        age--;
						target.baseCharStats().setStat(CharStats.STAT_AGE,age);
					}
					else
						target.baseCharStats().setStat(CharStats.STAT_AGE,age);
					target.recoverCharStats();
					target.recoverEnvStats();
				}
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> arms around <T-NAMESELF>, but the spell fizzles.");


		// return whether it worked
		return success;
	}

}
