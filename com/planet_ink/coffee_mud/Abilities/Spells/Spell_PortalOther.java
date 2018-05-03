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

public class Spell_PortalOther extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_PortalOther";
	}

	private final static String localizedName = CMLib.lang().L("Portal Other");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_SUMMONING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	Room newRoom=null;

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(newRoom!=null)
			{
				newRoom.showHappens(CMMsg.MSG_OK_VISUAL,L("The swirling portal closes."));
				newRoom.rawDoors()[Directions.GATE]=null;
				newRoom.setRawExit(Directions.GATE,null);
			}
		}
		super.unInvoke();
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		newRoom=null;

		if((auto||mob.isMonster())&&((commands.size()<1)||((commands.get(0)).equals(mob.name()))))
		{
			commands.clear();
			if(text().length()>0)
				commands.add(text());
			else
			{
				MOB M=null;
				int tries=0;
				while(((++tries)<100)&&(M==null))
				{
					final Room R=CMLib.map().getRandomRoom();
					if(R.numInhabitants()>0)
						M=R.fetchRandomInhabitant();
					if((M!=null)&&(M.name().equals(mob.name())))
						M=null;
				}
				if(M!=null)
					commands.add(M.Name());
			}
		}
		if(commands.size()<1)
		{
			mob.tell(L("Create a portal at whom?"));
			return false;
		}
		final String targetName=CMParms.combine(commands,0).trim().toUpperCase();
		if(mob.location().fetchInhabitant(targetName)!=null)
		{
			mob.tell(L("Better look around first."));
			return false;
		}

		newRoom=null;

		List<MOB> candidates=new Vector<MOB>();
		MOB target=null;
		try
		{
			target=CMLib.players().findPlayerOnline(targetName, false);
			if(target != null)
				candidates.add(target);
			else
				candidates=CMLib.map().findInhabitantsFavorExact(CMLib.map().rooms(), mob, targetName, false, 10);
		}
		catch(final NoSuchElementException nse)
		{
		}
		Room newRoom=null;
		if(candidates.size()>0)
		{
			target=candidates.get(CMLib.dice().roll(1,candidates.size(),-1));
			newRoom=target.location();
		}

		if((newRoom==null) || (target == null))
		{
			mob.tell(L("You can't seem to fixate on '@x1', perhaps they don't exist?",targetName));
			return false;
		}

		int adjustment=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(target.isMonster())
			adjustment=adjustment*3;
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,-adjustment,auto);

		if((success)
		&&((newRoom.getRoomInDir(Directions.GATE)==null)
		&&(newRoom.getExitInDir(Directions.GATE)==null)))
		{
			final CMMsg msg=CMClass.getMsg(mob,mob.location(),this,verbalCastCode(mob,mob.location(),auto),L("^S<S-NAME> evoke(s) a blinding, swirling portal somewhere.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,newRoom,this,verbalCastCode(mob,newRoom,auto),L("A blinding, swirling portal appears here."));
			if((mob.location().okMessage(mob,msg))&&(newRoom.okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				newRoom=(Room)msg2.target();
				newRoom.sendOthers(mob,msg2);
				final Exit e2=CMClass.getExit("GenExit");
				e2.setDescription(L("A swirling portal to somewhere"));
				e2.setDisplayText(L("A swirling portal to somewhere"));
				e2.setDoorsNLocks(false,true,false,false,false,false);
				e2.setExitParams("portal","close","open","closed.");
				e2.setName(L("a swirling portal"));
				final Ability A2=CMClass.getAbility("Prop_RoomView");
				if(A2!=null)
				{
					A2.setMiscText(CMLib.map().getExtendedRoomID(mob.location()));
					e2.addNonUninvokableEffect(A2);
				}
				newRoom.rawDoors()[Directions.GATE]=mob.location();
				newRoom.setRawExit(Directions.GATE,e2);
				Spell_PortalOther A = (Spell_PortalOther)beneficialAffect(mob,e2,asLevel,15);
				if(A!=null)
					A.newRoom=newRoom;
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to evoke a portal somewhere, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
