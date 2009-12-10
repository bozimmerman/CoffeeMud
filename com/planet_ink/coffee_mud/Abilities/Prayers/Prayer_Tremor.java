package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_Tremor extends Prayer
{
	public String ID() { return "Prayer_Tremor"; }
	public String name(){ return "Tremor";}
	public String displayText(){return "(Tremor)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int maxRange(){return adjustedMaxInvokerRange(3);}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected boolean oncePerRd=false;

	public boolean tick(Tickable ticking, int tickID)
	{ oncePerRd=false; return super.tick(ticking,tickID);}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);
		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.sourceMinor()==CMMsg.TYP_STAND)
		&&(mob.location()!=null))
		{
			if(!oncePerRd)
			{
				oncePerRd=true;
				mob.location().show(mob,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> attempt(s) to stand up, and falls back down!");
			}
			return false;
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
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> regain(s) <S-HIS-HER> feet as the ground stops shaking.");
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					CMLib.commands().postStand(mob,true);
				}
			}
			else
				mob.tell("The movement under your feet stops.");
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth shaking up.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),(auto?"":"^S<S-NAME> "+prayWord(mob)+" thunderously.^?")+CMProps.msp("earthquake.wav",40)))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
				if(CMLib.flags().isInFlight(target))
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) unaffected.");
				else
				if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						if(target.charStats().getBodyPart(Race.BODY_LEG)>0)
						{
							success=maliciousAffect(mob,target,asLevel,2,-1);
							if(success)
							{
								if(target.location()==mob.location())
									CMLib.combat().postDamage(mob,target,this,10,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,-1,"The ground underneath <T-NAME> shakes as <T-NAME> fall(s) to the ground!!");
							}
						}
						else
							mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) unaffected by the quake.");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" thunderously, but nothing happens.");


		// return whether it worked
		return success;
	}
}
