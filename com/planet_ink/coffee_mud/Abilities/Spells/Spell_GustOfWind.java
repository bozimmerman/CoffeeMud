package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_GustOfWind extends Spell
{
	public String ID() { return "Spell_GustOfWind"; }
	public String name(){return "Gust of Wind";}
	public String displayText(){return "(Blown Down)";}
	public int maxRange(){return 4;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public boolean doneTicking=false;
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}
	public long flags(){return Ability.FLAG_MOVING;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((doneTicking)&&(msg.amISource(mob)))
			unInvoke();
		else
		if(msg.amISource(mob)&&(msg.sourceMinor()==CMMsg.TYP_STAND))
			return false;
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			doneTicking=true;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> regain(s) <S-HIS-HER> feet.");
				CommonMsgs.stand(mob,true);
			}
			else
				mob.tell("You regain your feet.");
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if((h==null)||(h.size()==0))
		{
			mob.tell("There doesn't appear to be anyone here worth blowing around.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),auto?"A horrendous wind gust blows through here.":"^S<S-NAME> blow(s) at <S-HIS-HER> enemies.^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"<T-NAME> get(s) blown back!");
				if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
				{
					if((msg.value()<=0)&&(target.location()==mob.location()))
					{
						MOB victim=target.getVictim();
						if((victim!=null)&&(target.rangeToTarget()>=0))
							target.setAtRange(target.rangeToTarget()+1+(mob.envStats().level()/10));
						if(target.rangeToTarget()>target.location().maxRange())
							target.setAtRange(target.location().maxRange());

						mob.location().send(mob,msg);
						if((!Sense.isInFlight(target))
						&&(Dice.rollPercentage()>((target.charStats().getStat(CharStats.DEXTERITY)*2)+target.envStats().level()))
						&&(target.charStats().getBodyPart(Race.BODY_LEG)>0))
						{
							mob.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fall(s) down!");
							doneTicking=false;
							success=maliciousAffect(mob,target,asLevel,2,-1);
						}
						victim=target.getVictim();
						if(victim!=null)
							victim.setAtRange(target.rangeToTarget());
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> blow(s), but find(s) <S-HE-SHE> is only full of hot air.");


		// return whether it worked
		return success;
	}
}
