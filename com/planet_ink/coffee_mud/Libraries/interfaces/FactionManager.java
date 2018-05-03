package com.planet_ink.coffee_mud.Libraries.interfaces;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

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
import com.planet_ink.coffee_mud.Libraries.MUDZapper;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
/*
   Copyright 2005-2018 Bo Zimmerman

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
public interface FactionManager extends CMLibrary, Tickable
{
	public boolean addFaction(Faction F);
	public Enumeration<Faction> factions();
	public int numFactions();
	public void clearFactions();
	public void reloadFactions(String factionList);
	public boolean isRangeCodeName(String key);
	public boolean isFactionedThisWay(MOB mob, Faction.FRange rangeCode);
	public String rangeDescription(Faction.FRange FR, String andOr);
	public Faction getFaction(String factionID);
	public Faction getFactionByRangeCodeName(String rangeCodeName);
	public Faction.FRange getFactionRangeByCodeName(String rangeCodeName);
	public Faction getFactionByName(String factionNamed);
	public Faction getFactionByNumber(int index);
	public String makeFactionFilename(String factionID);
	public boolean removeFaction(String factionID);
	public String listFactions();
	public String getName(String factionID);
	public int getMinimum(String factionID);
	public int getMaximum(String factionID);
	public int getPercent(String factionID, int faction);
	public int getPercentFromAvg(String factionID, int faction);
	public Faction.FRange getRange(String factionID, int faction);
	public Enumeration<Faction.FRange> getRanges(String factionID);
	public double getRangePercent(String factionID, int faction);
	public int getTotal(String factionID);
	public int getRandom(String factionID);
	public void updatePlayerFactions(MOB mob, Room R, boolean forceAutoCheck);
	public String AlignID();
	public void setAlignment(MOB mob, Faction.Align newAlignment);
	public void setAlignmentOldRange(MOB mob, int oldRange);
	public int getAlignPurity(int faction, Faction.Align eq);
	public int getAlignMedianFacValue(Faction.Align eq);
	public int isFactionTag(String tag);
	public Faction.Align getAlignEnum(String str);
	public void modifyFaction(MOB mob, Faction me) throws IOException;
	public boolean postChangeAllFactions(MOB mob, MOB victim, int amount, boolean quiet);
	public boolean postFactionChange(MOB mob, Environmental tool,String factionID, int amount);
	public int getAbilityFlagType(String strflag);
	public String resaveFaction(Faction F);
}
