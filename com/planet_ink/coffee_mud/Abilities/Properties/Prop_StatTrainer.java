package com.planet_ink.coffee_mud.Abilities.Properties;
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


import java.util.*;

/* 
   Copyright 2000-2009 Bo Zimmerman

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
public class Prop_StatTrainer extends Property
{
	public String ID() { return "Prop_StatTrainer"; }
	public String name(){ return "Good training MOB";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
    protected static final int[] all25={25,25,25,25,25,25};
    protected int[] stats=all25;
    protected boolean noteach=false;

	public String accountForYourself()
	{ return "Stats Trainer";	}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		if((!noteach)&&(CMath.bset(affectedMOB.getBitmap(),MOB.ATT_NOTEACH)))
			affectedMOB.setBitmap(CMath.unsetb(affectedMOB.getBitmap(),MOB.ATT_NOTEACH));
		for(int i=0;i<stats.length;i++)
			affectableStats.setStat(i,stats[i]);
	}
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
		{
			if(newMiscText.toUpperCase().indexOf("NOTEACH")>=0)
				noteach=true;
			stats=new int[CharStats.NUM_BASE_STATS];
			stats[CharStats.STAT_STRENGTH]=CMParms.getParmInt(newMiscText,"STR",25);
			stats[CharStats.STAT_INTELLIGENCE]=CMParms.getParmInt(newMiscText,"INT",25);
			stats[CharStats.STAT_WISDOM]=CMParms.getParmInt(newMiscText,"WIS",25);
			stats[CharStats.STAT_CONSTITUTION]=CMParms.getParmInt(newMiscText,"CON",25);
			stats[CharStats.STAT_CHARISMA]=CMParms.getParmInt(newMiscText,"CHA",25);
			stats[CharStats.STAT_DEXTERITY]=CMParms.getParmInt(newMiscText,"DEX",25);
		}
	}

}
