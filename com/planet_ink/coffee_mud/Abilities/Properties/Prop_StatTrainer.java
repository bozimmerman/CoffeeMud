package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
public class Prop_StatTrainer extends Property
{
	public String ID() { return "Prop_StatTrainer"; }
	public String name(){ return "Good training MOB";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	private static final int[] all25={25,25,25,25,25,25};
	private int[] stats=all25;
	private boolean noteach=false;

	public String accountForYourself()
	{ return "Stats Trainer";	}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		if((!noteach)&&(Util.bset(affectedMOB.getBitmap(),MOB.ATT_NOTEACH)))
			affectedMOB.setBitmap(Util.unsetb(affectedMOB.getBitmap(),MOB.ATT_NOTEACH));
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
			stats[CharStats.STRENGTH]=Util.getParmInt(newMiscText,"STR",25);
			stats[CharStats.INTELLIGENCE]=Util.getParmInt(newMiscText,"INT",25);
			stats[CharStats.WISDOM]=Util.getParmInt(newMiscText,"WIS",25);
			stats[CharStats.CONSTITUTION]=Util.getParmInt(newMiscText,"CON",25);
			stats[CharStats.CHARISMA]=Util.getParmInt(newMiscText,"CHA",25);
			stats[CharStats.DEXTERITY]=Util.getParmInt(newMiscText,"DEX",25);
		}
	}

}
