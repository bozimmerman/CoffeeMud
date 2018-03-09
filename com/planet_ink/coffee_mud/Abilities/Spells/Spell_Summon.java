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
   Copyright 2001-2018 Bo Zimmerman

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

public class Spell_Summon extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Summon";
	}

	private final static String	localizedName	= CMLib.lang().L("Summon");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Summoned)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_PCT + 50;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_CONJURATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING | Ability.FLAG_SUMMONING;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)&&(super.canBeUninvoked()))
		{
			final MOB mob=(MOB)affected;
			if((!mob.amDead())&&(mob.location()!=null))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> drawn back into the summoning swirl."));
				mob.getStartRoom().bringMobHere(mob,false);
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String areaName=CMParms.combine(commands,0).trim().toUpperCase();
		if((commands.size()<1)&&(!auto))
		{
			mob.tell(L("Summon whom?"));
			return false;
		}
		else
		if(auto)
		{
			for(int i=0;i<2000;i++)
			{
				final Room R=CMLib.map().getRandomRoom();
				if((CMLib.flags().canAccess(mob,R))&&(R.numInhabitants()>0))
				{
					final MOB M=R.fetchRandomInhabitant();
					if(M!=null)
					{
						areaName=M.Name().toUpperCase();
						break;
					}
				}
			}
		}

		if((mob.location().fetchInhabitant(areaName)!=null)&&(!auto))
		{
			mob.tell(L("Better look around first."));
			return false;
		}

		Room oldRoom=null;
		MOB target=null;
		try
		{
			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				if((S.mob()!=null)
				&&(CMLib.flags().canAccess(mob,S.mob().location()))
				&&(CMLib.english().containsString(S.mob().name(),areaName)))
				{
					oldRoom=S.mob().location();
					target=S.mob();
					break;
				}
			}
			if(oldRoom==null)
			{
				target=CMLib.map().findFirstInhabitant(CMLib.map().rooms(), mob,areaName,10);
				if(target != null)
					oldRoom=target.location();
			}
		}
		catch (final NoSuchElementException nse)
		{
		}

		if((oldRoom==null)||(target==null))
		{
			mob.tell(L("You can't seem to fixate on '@x1', perhaps they don't exist?",CMParms.combine(commands,0)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int adjustment=(target.phyStats().level()-(mob.phyStats().level()+(getXLEVELLevel(mob)+(2*getX1Level(mob)))))*3;
		boolean success=proficiencyCheck(mob,-adjustment,auto);

		if(success&&(!auto)&&(!mob.mayIFight(target))&&(!mob.getGroupMembers(new HashSet<MOB>()).contains(target)))
		{
			mob.tell(L("@x1 is a player, so you must be group members, or your playerkill flags must be on for this to work.",target.name(mob)));
			success=false;
		}

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> summon(s) <T-NAME> in a mighty cry!^?"));
			if((mob.location().okMessage(mob,msg))&&(oldRoom.okMessage(mob,msg)))
			{
				mob.location().send(mob,msg);

				final MOB follower=target;
				final Room newRoom=mob.location();
				final CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,("<S-NAME> appear(s) in a burst of light.")+CMLib.protocol().msp("appear.wav",10));
				final CMMsg leaveMsg=CMClass.getMsg(follower,oldRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,L("<S-NAME> disappear(s) in a great summoning swirl created by @x1.",mob.name()));
				if(oldRoom.okMessage(follower,leaveMsg))
				{
					if(newRoom.okMessage(follower,enterMsg))
					{
						follower.makePeace(true);
						oldRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,false);
						newRoom.send(follower,enterMsg);
						follower.tell(L("\n\r\n\r"));
						if(follower.isMonster()
						&&(follower.getStartRoom()!=null)
						&&(follower.getStartRoom().getArea().name().equals(oldRoom.getArea().name())))
							beneficialAffect(mob,follower,asLevel,0);
						CMLib.commands().postLook(follower,true);
					}
					else
						mob.tell(L("Some powerful magic stifles the spell."));
				}
				else
					mob.tell(L("Some powerful magic stifles the spell."));
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to summon '@x1', but fail(s).",areaName));
		// return whether it worked
		return success;
	}
}
