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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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
public class Chant_AnimalSpy extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_AnimalSpy";
	}

	private final static String	localizedName	= CMLib.lang().L("Animal Spy");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Animal Spy)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ANIMALAFFINITY;
	}

	protected MOB		spy		= null;
	protected boolean	disable	= false;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&(affected==spy))
		{
			if(spy.amDead()
			||(spy.amFollowing()!=invoker)
			||(!CMLib.flags().isInTheGame(spy,false)))
				unInvoke();
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		if(canBeUninvoked())
		{
			if(invoker!=null)
			{
				final Ability A=invoker.fetchEffect(this.ID());
				if(A!=null)
					invoker.delEffect(A);
				invoker.tell(L("Your connection with '@x1' fades.",spy.name()));
			}
		}
		super.unInvoke();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		try
		{
			super.executeMsg(myHost,msg);
			if(spy==null)
				return;
			if(invoker==null)
				return;

			if((msg.amISource(spy))
			&&((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE))
			&&(msg.target()!=null)
			&&((invoker.location()!=spy.location())||(!(msg.target() instanceof Room))))
			{
				disable=true;
				final CMMsg newAffect=CMClass.getMsg(invoker,msg.target(),msg.sourceMinor(),null);
				msg.target().executeMsg(invoker,newAffect);
			}
			else
			if((!msg.amISource(invoker))
			&&(invoker.location()!=spy.location())
			&&(msg.source().location()==spy.location())
			&&(msg.othersCode()!=CMMsg.NO_EFFECT)
			&&(msg.othersMessage()!=null)
			&&(!disable))
			{
				disable=true;
				invoker.executeMsg(invoker,msg);
			}
			else
			if(msg.amISource(invoker)
			&&(!disable)
			&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(msg.sourceMessage()!=null)
			&&((msg.sourceMajor()&CMMsg.MASK_MAGIC)==0))
			{
				final String msg2=CMStrings.getSayFromMessage(msg.sourceMessage());
				if((msg2!=null)&&(msg2.length()>0))
					spy.enqueCommand(CMParms.parse(msg2.trim()),MUDCmdProcessor.METAFLAG_FORCED,0);
			}
		}
		finally
		{
			disable=false;
			if((spy!=null)&&((spy.amFollowing()!=invoker)
							||(spy.amDead())
							||(!CMLib.flags().isInTheGame(spy,false))
							||(!CMLib.flags().isInTheGame(invoker,true))))
				unInvoke();
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("Chant to whom?"));
			return false;
		}
		final String mobName=CMParms.combine(commands,0).trim().toUpperCase();
		final MOB target=getTarget(mob,commands,givenTarget);

		Room newRoom=mob.location();
		if(target!=null)
		{
			newRoom=target.location();
			if((!CMLib.flags().isAnimalIntelligence(target))
			||(target.amFollowing()!=mob))
			{
				mob.tell(L("You have no animal follower named '@x1' here.",mobName));
				return false;
			}
		}
		else
		{
			mob.tell(L("You have no animal follower named '@x1' here.",mobName));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>, invoking the a mystical connection.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
			if((mob.location().okMessage(mob,msg))&&((newRoom==mob.location())||(newRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location())
					newRoom.send(target,msg2);
				spy=target;
				beneficialAffect(mob,spy,asLevel,0);
				final Ability A=spy.fetchEffect(ID());
				if(A!=null)
				{
					mob.addNonUninvokableEffect((Ability)A.copyOf());
					A.setAffectedOne(spy);
				}
			}

		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));

		// return whether it worked
		return success;
	}
}
