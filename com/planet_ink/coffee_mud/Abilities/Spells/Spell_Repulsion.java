package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_Repulsion extends Spell
{
	public String ID() { return "Spell_Repulsion"; }
	public String name(){return "Repulsion";}
	public String displayText(){return "(Repulsion)";}
	public int maxRange(){return 3;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ABJURATION;}
	public long flags(){return Ability.FLAG_MOVING;}

	public int amountRemaining=0;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			{
				if(mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against the repulsion field."))
				{
					amountRemaining-=mob.charStats().getStat(CharStats.STRENGTH);
					if(amountRemaining<0)
						unInvoke();
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of the repulsion field.");
			CommonMsgs.stand(mob,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if((h==null)||(h.size()==0))
		{
			mob.tell("There doesn't appear to be anyone here worth repelling.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms and cast(s) a spell.^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						amountRemaining=130;
						if(target.location()==mob.location())
						{
							success=maliciousAffect(mob,target,(mob.envStats().level()*10),-1);
							int level=2;
							if((CMAble.qualifyingClassLevel(mob,this)>0)&&((adjustedLevel(mob)-CMAble.qualifyingClassLevel(mob,this))>10))
								level+=((adjustedLevel(mob)-CMAble.qualifyingClassLevel(mob,this))-10)/10;
							target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> become(s) repelled!");
							if((target.getVictim()!=null)&&(target.rangeToTarget()>0))
								target.setAtRange(target.rangeToTarget());
							else
							if(target.location().maxRange()<level)
								target.setAtRange(target.location().maxRange());
							else
								target.setAtRange(level);
							if(target.getVictim()!=null)
								target.getVictim().setAtRange(target.rangeToTarget());
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> incant(s), but the spell fizzles.");


		// return whether it worked
		return success;
	}
}
