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
public class Spell_Cloudkill extends Spell
{
	public String ID() { return "Spell_Cloudkill"; }
	public String name(){return "Cloudkill";}
	public String displayText(){return "(Cloudkill)";}
	public int maxRange(){return adjustedMaxInvokerRange(10);}
	public int minRange(){return 1;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		MOB invoker = this.invoker();

		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.tell("You feel less intoxicated.");
			CMLib.commands().postStand(mob,true);
			if((invoker!=null)
			&&(!mob.isInCombat())
			&&(mob.location()!=null)
		    &&(mob.location().isInhabitant(invoker))
		    &&(!mob.amDead()))
				CMLib.combat().postAttack(mob,invoker,mob.fetchWieldedItem());
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth clouding.");
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
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),auto?"A horrendous green cloud appears!":"^S<S-NAME> evoke(s) a horrendous green cloud.^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
				CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_GAS|(auto?CMMsg.MASK_ALWAYS:0),null);
				if(mob.location().okMessage(mob,msg)&&mob.location().okMessage(mob,msg2))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					if(target.fetchEffect(ID())==null)
					{
						invoker=mob;
	
						int damage = target.curState().getHitPoints();
	
						int midLevel=(int)Math.round(CMath.div(adjustedLevel(mob,asLevel),2.0));
						if(midLevel<target.envStats().level())
							damage=(int)Math.round(CMath.mul(damage,0.10));
	
						if((msg.value()>0)||(msg2.value()>0))
							damage = (int)Math.round(CMath.div(damage,2.0));
	
						if(damage<=0) damage=1;
						if(target.location()==mob.location())
						{
							String addOn = "";
							if((target.charStats().getBodyPart(Race.BODY_LEG)>0)
							&&(msg.value()<=0)
							&&(msg2.value()<=0))
							{
								maliciousAffect(mob,target,asLevel,2,-1);
								if(mob!=target)
								{
									mob.setVictim(target);
									target.setVictim(mob);
								}
								if(target.fetchEffect(ID())!=null)
									addOn =" <T-NAME> collapse(s).";
							}
							CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_GAS,Weapon.TYPE_GASSING,"The gas <DAMAGE> <T-NAME>!"+addOn);
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to evoke a green cloud, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}
