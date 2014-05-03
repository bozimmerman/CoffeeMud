package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings({"unchecked","rawtypes"})
public class Chant_LoveMoon extends Chant
{
	@Override public String ID() { return "Chant_LoveMoon"; }
	@Override public String name(){ return "Love Moon";}
	@Override public String displayText(){return "(Love Moon)";}
	@Override public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_MOONALTERING;}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
		{
			if(affected instanceof Room)
				((Room)affected).showHappens(CMMsg.MSG_OK_VISUAL,"The love moon sets.");
			super.unInvoke();
			return;
		}

		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(_("You are no longer under the love moon."));

		super.unInvoke();

	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(mob.location().fetchEffect(ID())==null)
				unInvoke();
			else
			{
				final Vector choices=new Vector();
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					final MOB M=mob.location().fetchInhabitant(i);
					if((M!=null)
					&&(M!=mob)
					&&(CMLib.flags().canBeSeenBy(M,mob))
					&&(M.charStats().getStat(CharStats.STAT_GENDER)!=mob.charStats().getStat(CharStats.STAT_GENDER))
					&&(M.charStats().getStat(CharStats.STAT_GENDER)!='N')
					&&(M.charStats().getSave(CharStats.STAT_CHARISMA)>14))
						choices.addElement(M);
				}
				if(choices.size()>0)
				{
					final MOB M=(MOB)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
					if(CMLib.dice().rollPercentage()==1)
					{
						Item I=mob.fetchFirstWornItem(Wearable.WORN_WAIST);
						if(I!=null)	CMLib.commands().postRemove(mob,I,false);
						I=mob.fetchFirstWornItem(Wearable.WORN_LEGS);
						if(I!=null)	CMLib.commands().postRemove(mob,I,false);
						mob.doCommand(CMParms.parse("MATE "+M.Name()),Command.METAFLAG_FORCED);
					}
					else
					if(CMLib.dice().rollPercentage()>10)
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1:
							mob.tell("You feel strange urgings towards "+M.name(mob)+".");
							break;
						case 2:
							mob.tell("You have strong happy feelings towards "+M.name(mob)+".");
							break;
						case 3:
							mob.tell("You feel very appreciative of "+M.name(mob)+".");
							break;
						case 4:
							mob.tell("You feel very close to "+M.name(mob)+".");
							break;
						case 5:
							mob.tell("You feel lovingly towards "+M.name(mob)+".");
							break;
						}
				}
			}
		}
		else
		if(affected instanceof Room)
		{
			final Room room=(Room)affected;
			if(!room.getArea().getClimateObj().canSeeTheMoon(room,this))
				unInvoke();
			else
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB M=room.fetchInhabitant(i);
				if((M!=null)&&(M.fetchEffect(ID())==null))
				{
					final Ability A=(Ability)copyOf();
					M.addEffect(A);
					M.recoverCharStats();
				}
			}
		}
		return true;
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+6);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room target=mob.location();
		if(target==null) return false;
		if(!target.getArea().getClimateObj().canSeeTheMoon(target,null))
		{
			mob.tell(_("You must be able to see the moon for this magic to work."));
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(_("This place is already under the love moon."));
			return false;
		}
		for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_MOONALTERING))
			{
				mob.tell("The moon is already under "+A.name()+", and can not be changed until this magic is gone.");
				return false;
			}
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to the sky.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The Love Moon Rises!");
					beneficialAffect(mob,target,asLevel,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the sky, but the magic fades.");
		// return whether it worked
		return success;
	}
}
