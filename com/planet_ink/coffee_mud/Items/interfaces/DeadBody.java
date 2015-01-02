package com.planet_ink.coffee_mud.Items.interfaces;
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
/*
   Copyright 2001-2015 Bo Zimmerman

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
public interface DeadBody extends Container
{
	public CharStats charStats();
	public void setCharStats(CharStats newStats);
	public String getMobName();
	public void setMobName(String newName);
	public int getMobHash();
	public void setMobHash(int newHash);
	public String geteMobDescription();
	public void setMobDescription(String newDescription);
	public String getKillerName();
	public void setKillerName(String newName);
	public boolean isKillerPlayer();
	public void setIsKillerPlayer(boolean trueFalse);
	public String getLastMessage();
	public void setLastMessage(String lastMsg);
	public Environmental getKillerTool();
	public void setKillerTool(Environmental tool);
	public boolean isDestroyedAfterLooting();
	public void setIsDestroyAfterLooting(boolean truefalse);
	public boolean isPlayerCorpse();
	public void setIsPlayerCorpse(boolean truefalse);
	public boolean getMobPKFlag();
	public void setMobPKFlag(boolean truefalse);
	public long getTimeOfDeath();
	public void setTimeOfDeath(long time);
	public void setSavedMOB(MOB mob);
	public MOB getSavedMOB();
}

