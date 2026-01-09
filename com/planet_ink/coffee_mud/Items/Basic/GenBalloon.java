package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.Items.Basic.StdPortal;
import com.planet_ink.coffee_mud.Items.Basic.StdNavigableBoardable.NavigatingCommand;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.interfaces.Rideable.Basis;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Int;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;

/*
   Copyright 2025-2026 Bo Zimmerman

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
public class GenBalloon extends GenAirShip
{
	@Override
	public String ID()
	{
		return "GenBalloon";
	}

	public GenBalloon()
	{
		super();
		setName("the balloon called [NEWNAME]");
		setDisplayText("the balloon [NEWNAME] is here.");
		this.verb_sail = L("float");
		this.verb_sailing = L("floating");
		this.noun_word = L("balloon");
		this.anchor_name= L("rope");
		this.anchor_verbed = L("secured");
		this.head_offTheDeck = L("^HOff the side you see: ^N");
		setMaterial(RawMaterial.RESOURCE_OAK);
		basePhyStats().setAbility(1);
		basePhyStats().setArmor(-50);
		this.recoverPhyStats();
		this.ticksPerTurn=1;
		this.ticksPerMoves=3;
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return L("a balloon");
	}

	@Override
	protected boolean preNavigateCheck(final Room thisRoom, final int direction, final Room destRoom)
	{
		if((direction==Directions.UP)||(direction==Directions.DOWN))
			return super.preNavigateCheck(thisRoom, direction, destRoom);
		if ((destRoom.domainType() != Room.DOMAIN_OUTDOORS_AIR) && (destRoom.domainType() != Room.DOMAIN_INDOORS_AIR))
		{
			announceToAllAboard(L("<S-NAME> scrape(s) along the ground, going nowhere.", CMLib.directions().getInDirectionName(direction), verb_sail));
			courseDirections.clear();
			return false;
		}
		return true;
	}

	private final static Map<String, NavigatingCommand> navCommandWords = new Hashtable<String, NavigatingCommand>();

	@Override
	protected Pair<NavigatingCommand, Integer> findNavCommand(final String word, final String secondWord)
	{
		if(word == null)
			return null;
		if(navCommandWords.size()==0)
		{
			for(final NavigatingCommand N : NavigatingCommand.values())
			{
				switch(N)
				{
				case NAVIGATE:
					navCommandWords.put("FLOAT", N);
					break;
				case RAISE_ANCHOR:
					navCommandWords.put("RELEASE_TIEDOWNS", N);
					navCommandWords.put("RELEASE_ROPES", N);
					navCommandWords.put("RELEASE_ROPE", N);
					navCommandWords.put("RELEASE_TIEDOWN", N);
					navCommandWords.put("RELEASE_TIES", N);
					break;
				case LOWER_ANCHOR:
					navCommandWords.put("TIEDOWN", N);
					navCommandWords.put("TIE_DOWN", N);
					break;
				default:
					navCommandWords.put(N.name().toUpperCase().trim(), N);
				}
			}
		}

		if((secondWord!=null)&&(secondWord.length()>0)&&(navCommandWords.containsKey((word+"_"+secondWord).toUpperCase().trim())))
			return new Pair<NavigatingCommand, Integer>(navCommandWords.get((word+"_"+secondWord).toUpperCase().trim()),Integer.valueOf(2));
		if (navCommandWords.containsKey(word.toUpperCase().trim()))
			return new Pair<NavigatingCommand, Integer>(navCommandWords.get(word.toUpperCase().trim()), Integer.valueOf(1));
		return null;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if (!super.tick(ticking, tickID))
			return false;
		if((tickID == Tickable.TICKID_AREA)
		&&(!amInTacticalMode())
		&& (area != null)
		&& (CMLib.dice().rollPercentage()==1))
		{
			final Room thisRoom=CMLib.map().roomLocation(this);
			if((thisRoom != null)
			&&(thisRoom.getRoomInDir(Directions.DOWN)!=null))
			{
				boolean noaboard=true;
				for(final Enumeration<Room> r=area.getFilledCompleteMap();r.hasMoreElements();)
				{
					final Room R = r.nextElement();
					noaboard = noaboard && (R!=null) && (R.numInhabitants()==0);
					if (!noaboard)
						break;
				}
				if(noaboard)
					this.navMove(Directions.DOWN);
			}
		}

		return true;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenBalloon))
			return false;
		return super.sameAs(E);
	}

}
