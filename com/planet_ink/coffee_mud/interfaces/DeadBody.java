package com.planet_ink.coffee_mud.interfaces;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	public String mobName();
	public void setMobName(String newName);
	public String mobDescription();
	public void setMobDescription(String newDescription);
	public String killerName();
	public void setKillerName(String newName);
	public boolean killerPlayer();
	public void setKillerPlayer(boolean trueFalse);
	public String lastMessage();
	public void setLastMessage(String lastMsg);
	public Environmental killingTool();
	public void setKillingTool(Environmental tool);
	public boolean destroyAfterLooting();
	public void setDestroyAfterLooting(boolean truefalse);
	public boolean playerCorpse();
	public void setPlayerCorpse(boolean truefalse);
	public boolean mobPKFlag();
	public void setMobPKFlag(boolean truefalse);
	public long timeOfDeath();
	public void setTimeOfDeath(long time);
	
	public void startTicker(Room thisRoom);
}
  