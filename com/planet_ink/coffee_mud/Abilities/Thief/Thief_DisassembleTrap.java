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
   Copyright 2008-2018 Bo Zimmerman

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
public class Thief_DisassembleTrap extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_DisassembleTrap";
	}

	private final static String	localizedName	= CMLib.lang().L("Disassemble Traps");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS | Ability.CAN_EXITS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "DISTRAP", "DISASSEMBLETRAPS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_DETRAP;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Thief_RemoveTraps trapRemover=(Thief_RemoveTraps)mob.fetchAbility("Thief_RemoveTraps");
		final Hashtable<String,Trap> traps=new Hashtable<String,Trap>();
		if(trapRemover==null)
		{
			mob.tell(L("You don't know how to remove traps."));
			return false;
		}

		final Vector<String> cmds=new XVector<String>(commands);
		final Vector<Trap> trapList = new XVector<Trap>();
		final CharState oldState=(CharState)mob.curState().copyOf();
		final boolean worked=trapRemover.invoke(mob,cmds,givenTarget,auto,asLevel,true,trapList);
		oldState.copyInto(mob.curState());
		if(!worked)
			return false;
		for(int c=0;c<trapList.size();c++)
		{
			final Trap T=trapList.elementAt(c);
			if(!traps.containsKey(T.ID()))
				traps.put(T.ID(),T);
		}
		if(traps.size()==0)
		{
			mob.tell(L("Your attempt was unsuccessful."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final Trap T=traps.elements().nextElement();
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,T,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_DELICATE_HANDS_ACT,
													 CMMsg.MSG_DELICATE_HANDS_ACT,
													 CMMsg.MSG_OK_ACTION,
												auto?L("@x1 begins to glow.",T.name()):
													L("<S-NAME> attempt(s) to safely dissassemble the @x1 trap.",T.name()));
			final Room R=mob.location();
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final List<Item> components=T.getTrapComponents();
				if(components.size()==0)
				{
					mob.tell(L("You don't end up with any usable components."));
				}
				else
				{
					for(int i=0;i<components.size();i++)
					{
						final Item I=components.get(i);
						I.text();
						I.recoverPhyStats();
						R.addItem(I,ItemPossessor.Expire.Resource);
					}
					R.recoverRoomStats();
					for(int i=0;i<components.size();i++)
					{
						final Item I=components.get(i);
						if(R.isContent(I))
						{
							if(!CMLib.commands().postGet(mob,null,I,false))
								break;
						}
					}
					R.recoverRoomStats();
				}
			}
		}
		else
			beneficialVisualFizzle(mob,T,L("<S-NAME> attempt(s) to disassemble the <T-NAME> trap, but fail(s)."));

		return success;
	}
}
