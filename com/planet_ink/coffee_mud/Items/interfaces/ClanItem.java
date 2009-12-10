package com.planet_ink.coffee_mud.Items.interfaces;
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
   Copyright 2000-2010 Bo Zimmerman

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
public interface ClanItem extends Item
{
	public final static int CI_FLAG=0;
	public final static int CI_BANNER=1;
	public final static int CI_GAVEL=2;
	public final static int CI_PROPAGANDA=3;
	public final static int CI_GATHERITEM=4;
	public final static int CI_CRAFTITEM=5;
	public final static int CI_SPECIALSCALES=6;
	public final static int CI_SPECIALSCAVENGER=7;
	public final static int CI_SPECIALOTHER=8;
	public final static int CI_SPECIALTAXER=9;
    public final static int CI_DONATEJOURNAL=10;
    public final static int CI_ANTIPROPAGANDA=11;
    public final static int CI_SPECIALAPRON=12;
    public final static int CI_LEGALBADGE=13;
	
	public final static String[] CI_DESC={
		"FLAG",
		"BANNER",
		"GAVEL",
		"PROPAGANDA",
		"GATHERITEM",
		"CRAFTITEM",
		"SPECIALSCALES",
		"SPECIALSCAVENGER",
		"SPECIALOTHER",
		"SPECIALTAXER",
        "DONATIONJOURNAL",
        "ANTI-PROPAGANDA",
        "SPECIALAPRON",
        "LEGALBADGE"
	};
	
	public String clanID();
	public void setClanID(String ID);
	
	public int ciType();
	public void setCIType(int type);
    
    public Environmental rightfulOwner();
    public void setRightfulOwner(Environmental E);
}
