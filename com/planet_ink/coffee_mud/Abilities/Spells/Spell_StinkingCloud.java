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
public class Spell_StinkingCloud extends Spell
{
	public String ID() { return "Spell_StinkingCloud"; }
	public String name(){return "Stinking Cloud";}
	public String displayText(){return "(In the Stinking Cloud)";}
	public int maxRange(){return adjustedMaxInvokerRange(3);}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}

	Room castingLocation=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			MOB M=(MOB)affected;
			if((M.location()!=castingLocation)||(M.amDead()))
				unInvoke();
			else
			if((!M.amDead())
            &&(M.location()!=null)
            &&(CMLib.flags().canSmell(M)))
			{
				if((M.curState().getHunger()<=0))
					CMLib.combat().postDamage(invoker,M,this,M.envStats().level(),CMMsg.MASK_ALWAYS|CMMsg.TYP_GAS,-1,"<T-NAME> heave(s) in the stinking cloud.");
				else
				{
					CMLib.combat().postDamage(invoker,M,this,M.envStats().level(),CMMsg.MASK_ALWAYS|CMMsg.TYP_GAS,-1,"<T-NAME> heave(s) all over the place!");
					M.curState().adjHunger(-500,M.maxState().maxHunger(M.baseWeight()));
				}
                if((!M.isInCombat())&&(M!=invoker)&&(M.location()!=null)&&(M.location().isInhabitant(invoker))&&(CMLib.flags().canBeSeenBy(invoker,M)))
                    CMLib.combat().postAttack(M,invoker,M.fetchWieldedItem());
			}
			else
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		   &&(affected instanceof MOB)
		   &&(msg.amISource((MOB)affected)))
		{
			MOB mob=(MOB)affected;
			if(CMLib.flags().canSmell(mob))
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_ADVANCE:
					if(CMLib.dice().rollPercentage()>(mob.charStats().getSave(CharStats.STAT_SAVE_GAS)))
					{
						mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> double(s) over from the sickening gas.");
						return false;
					}
					break;
				}
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_ADVANCE:
				unInvoke();
				break;
			}
		}
		else
		if((msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(CMLib.flags().canSmell(msg.source())))
			msg.source().tell(msg.source(),affected,null,"<T-NAME> smell(s) nauseatingly stinky!");
		super.executeMsg(myHost,msg);
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
			if((!mob.amDead())&&(mob.location()!=null))
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to escape the stinking cloud!");
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=null;
		if(givenTarget!=null)
		{
			h=new HashSet();
			h.add(givenTarget);
		}
		else
			h=CMLib.combat().properTargets(this,mob,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth casting this on.");
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
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> incant(s) and wave(s) <S-HIS-HER> arms around.  A horrendous cloud of green and orange gas appears!^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
				CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_GAS|(auto?CMMsg.MASK_ALWAYS:0),null);
				if((mob.location().okMessage(mob,msg))
				   &&(mob.location().okMessage(mob,msg2))
				   &&(target.fetchEffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					if((msg.value()<=0)&&(msg2.value()<=0)&&(target.location()==mob.location()))
					{
						castingLocation=mob.location();
						success=maliciousAffect(mob,target,asLevel,(adjustedLevel(mob,asLevel)*10),-1);
						target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> become(s) enveloped in the stinking cloud!");
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
