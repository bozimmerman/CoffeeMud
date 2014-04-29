package com.planet_ink.coffee_mud.Abilities.Thief;
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
@SuppressWarnings("rawtypes")
public class Thief_MarkerSpying extends ThiefSkill
{
	@Override public String ID() { return "Thief_MarkerSpying"; }
	@Override public String name(){ return "Marker Spying";}
		// can NOT have a display text since the ability instance
		// is shared between the invoker and the target
	@Override public String displayText(){return "";}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return CAN_MOBS;}
	@Override public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	private static final String[] triggerStrings = {"MARKERSPYING","MARKSPY"};
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;
	@Override public int abilityCode(){return code;}
	@Override public void setAbilityCode(int newCode){code=newCode;}

	public MOB getMark(MOB mob)
	{
		if(mob!=null)
		{
			final Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
			if(A!=null)
				return A.mark;
		}
		return null;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(super.canBeUninvoked())
		{
			final MOB mark=getMark(invoker());
			if(mark!=affected)
			{
				final MOB invoker=invoker();
				unInvoke();
				if((mark!=null)&&(mark.fetchEffect(ID())==null)&&(invoker!=null))
					beneficialAffect(invoker,mark,0,Ability.TICKS_FOREVER);
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.targetMinor()==CMMsg.TYP_READ)
		&&(msg.source()==affected)
		&&(msg.target()!=null)
		&&(invoker()!=null)
		&&(CMLib.flags().isInTheGame(invoker(),true))
		&&(getMark(invoker())==msg.source()))
		{
			final CMMsg msg2=(CMMsg)msg.copyOf();
			msg2.modify(invoker(),msg.target(),msg.tool(),msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),msg.targetMessage(),CMMsg.NO_EFFECT,null);
			invoker().tell("You remember something else from "+msg.source().Name()+"'s papers:");
			msg.target().executeMsg(invoker(),msg2);
		}
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((invoker!=null)&&(affected!=null))
				invoker.tell("You are no longer spying on "+affected.name()+".");
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getMark(mob);
		if(target==null)
		{
			mob.tell("You'll need to mark someone first.");
			return false;
		}
		final Ability A=target.fetchEffect(ID());
		if(A!=null)
		{
			if(A.invoker()==mob)
				A.unInvoke();
			else
			{
				mob.tell(mob,target,null,"It is too crowded to spy on <T-NAME>.");
				return false;
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_OK_VISUAL,auto?"":"Your attempt to spy on <T-NAMESELF> fails; <T-NAME> spots you!",CMMsg.MSG_OK_VISUAL,auto?"":"You spot <S-NAME> trying to spy on you.",CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_THIEF_ACT,"You are now spying on <T-NAME>.  Enter 'spy <targetname>' again to disengage.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,Ability.TICKS_FOREVER);
			}
		}
		return success;
	}
}
