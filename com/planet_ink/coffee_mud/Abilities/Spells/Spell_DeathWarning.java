package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_DeathWarning extends Spell
{
	@Override public String ID() { return "Spell_DeathWarning"; }
	@Override public String name(){return "Death Warning";}
	@Override public String displayText(){return "(Death Warning)";}
	@Override public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}
	protected Vector commands=new XVector("FLEE");

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.tell(mob,null,null,_("<S-YOUPOSS> death warning magic fades."));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
		{
			final MOB mob=(MOB)affected;
			final int hitPoints=mob.curState().getHitPoints();
			mob.curState().setHitPoints(1);
			final Room room=mob.location();
			mob.tell(_("^SYou receive a warning of your impending death!!^N"));
			mob.doCommand(commands,0);
			if(mob.location()!=room)
			{
				mob.makePeace();
				return false;
			}
			else
				mob.curState().setHitPoints(hitPoints);
			unInvoke();
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,_("<S-NAME> already <S-HAS-HAVE> a death's warning."));
			return false;
		}

		if(commands.size()==0)
		{
			if(mob.isMonster())
				commands.add("FLEE");
			else
			{
				mob.tell(_("You need to specify what you want to do should the warning arrives!"));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> begin(s) listening for a death's warning!":"^S<S-NAME> incant(s) coldly, and begin(s) listening for death's warning!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				final Spell_DeathWarning A=(Spell_DeathWarning)target.fetchEffect(ID());
				if(A!=null) A.commands=commands;
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) coldly and listen(s), but the spell fizzles.");

		return success;
	}
}
