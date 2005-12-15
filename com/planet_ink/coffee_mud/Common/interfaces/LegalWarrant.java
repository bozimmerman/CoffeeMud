package com.planet_ink.coffee_mud.Common.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public interface LegalWarrant extends CMObject
{
	public void setArrestingOfficer(Area legalArea, MOB mob);
	public MOB criminal();
	public MOB victim();
	public MOB witness();
	public MOB arrestingOfficer();
	public Room jail();
	public Room releaseRoom();
	public String crime();
	public int actionCode();
    public String getActionParm(int code);
    public void addActionParm(int code, String parm);
	public int jailTime();
	public int state();
	public int offenses();
	public long lastOffense();
	public long travelAttemptTime();
	public String warnMsg();
	public void setCriminal(MOB mob);
	public void setVictim(MOB mob);
	public void setWitness(MOB mob);
	public void setJail(Room R);
	public void setReleaseRoom(Room R);
	public void setCrime(String crime);
	public void setActionCode(int code);
	public void setJailTime(int time);
	public void setState(int state);
	public void setOffenses(int num);
	public void setLastOffense(long last);
	public void setTravelAttemptTime(long time);
	public void setWarnMsg(String msg);
}
