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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2011-2018 Bo Zimmerman

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

public class Spell_SpyingStone extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_SpyingStone";
	}

	private final static String localizedName = CMLib.lang().L("Spying Stone");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Spying Stone)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected LinkedList<String> msgs=new LinkedList<String>();

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&((msg.source()==invoker())
			||((invoker()!=null) && msg.source().Name().equalsIgnoreCase(invoker().Name())))
		&&(msg.target()==affected)
		&&(msg.sourceMessage().toUpperCase().indexOf("SPEAK")>=0))
		{
			final Room room=CMLib.map().roomLocation(affected);
			if(room!=null)
			{
				final StringBuilder str=new StringBuilder("");
				for(final String m : msgs)
					str.append(m).append("\n\r");
				if(str.length()==0)
					str.append(L("Nothing!"));
				room.showHappens(CMMsg.MSG_SPEAK, affected,L("^S<S-NAME> grow(s) a mouth and say(s) '^N@x1^S'^N",str.toString()));
				msgs.clear();
			}
		}
		else
		if((msg.othersCode()!=CMMsg.NO_EFFECT)
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().length()>0))
			msgs.add(CMLib.coffeeFilter().fullOutFilter(null, null, msg.source(), msg.target(), msg.tool(), CMStrings.removeColors(msg.othersMessage()), false));
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(!(target instanceof Item))
		{
			mob.tell(L("You can't cast this spell on that."));
			return false;
		}

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(L("@x1 is already a spying stone!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> point(s) <S-HIS-HER> finger at <T-NAMESELF>, incanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> open(s) a pair of strange eyes, which become transluscent."));
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> point(s) at <T-NAMESELF>, incanting, but nothing happens."));

		// return whether it worked
		return success;
	}
}
