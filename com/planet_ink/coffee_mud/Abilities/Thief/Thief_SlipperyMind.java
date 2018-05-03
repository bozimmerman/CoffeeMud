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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class Thief_SlipperyMind extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_SlipperyMind";
	}

	private final static String	localizedName	= CMLib.lang().L("Slippery Mind");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Slippery Mind)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SLIPPERYMIND" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected volatile LinkedList<Pair<Faction, Integer>>	oldFactions	= null;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_EVASIVE;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(unInvoked)
			return false;
		if((affected!=null)&&(affected instanceof MOB)&&(ticking instanceof MOB))
		{
			if(!super.tick(ticking,tickID))
				return false;
			final MOB mob=(MOB)affected;
			Faction F=null;
			if(oldFactions==null)
			{
				oldFactions=new LinkedList<Pair<Faction,Integer>>();
				for(final Enumeration<String> e=mob.factions();e.hasMoreElements();)
				{
					F=CMLib.factions().getFaction(e.nextElement());
					if(F!=null)
					{
						oldFactions.add(new Pair<Faction,Integer>(F,Integer.valueOf(mob.fetchFaction(F.factionID()))));
						mob.addFaction(F.factionID(),F.middle());
					}
				}
			}
			else
			for(final Pair<Faction,Integer> p : oldFactions)
			{
				F=p.first;
				if(mob.fetchFaction(F.factionID())!=F.middle())
					mob.addFaction(F.factionID(),F.middle());
			}
		}
		return true;
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(super.canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(msg.amISource((MOB)affected))
			&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
				unInvoke();
			else
			if(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
				unInvoke();
		}
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(((msg.sourceMinor()==CMMsg.TYP_QUIT)
			||(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||(msg.sourceMinor()==CMMsg.TYP_DEATH) // yes, intentional
			||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET)))
		{
			unInvoke();
		}
		return super.okMessage(host,msg);
	}

	@Override
	public void unInvoke()
	{
		final Environmental E=affected;
		super.unInvoke();
		if((E instanceof MOB)&&(oldFactions!=null)&&(super.canBeUninvoked()))
		{
			if(!((MOB)E).amDead())
				((MOB)E).tell(L("You've lost your slippery mind concentration."));
			for(final Pair<Faction,Integer> p : oldFactions)
				((MOB)E).addFaction(p.first.factionID(),p.second.intValue());
			oldFactions=null;
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> already <S-HAS-HAVE> a slippery mind."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,auto?L("<T-NAME> gain(s) a slippery mind."):L("<S-NAME> wink(s) and nod(s)."));
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":L("<S-NAME> wink(s) and nod(s), but <S-IS-ARE>n't fooling anyone."));
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			oldFactions=null;
			beneficialAffect(mob,target,asLevel,0);
			final Ability A=target.fetchEffect(ID());
			if(A!=null)
				A.tick(target,Tickable.TICKID_MOB);
		}
		return success;
	}
}
