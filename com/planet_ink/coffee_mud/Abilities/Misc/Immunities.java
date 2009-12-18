package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Immunities extends StdAbility
{
	public String ID() { return "Immunities"; }
	public String name(){ return "Immunities";}
	protected String displayText="";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int resistanceCode=0;


	public static Object[][] immunityTypes={
		{Integer.valueOf(CMMsg.TYP_ACID), "ACID"},
		{Integer.valueOf(CMMsg.TYP_WATER), "WATER"},
		{Integer.valueOf(CMMsg.TYP_COLD), "COLD"},
		{Integer.valueOf(CMMsg.TYP_DISEASE), "DISEASE"},
		{Integer.valueOf(CMMsg.TYP_ELECTRIC), "ELECTRIC"},
		{Integer.valueOf(CMMsg.TYP_FIRE), "FIRE"},
		{Integer.valueOf(CMMsg.TYP_GAS), "GAS"},
		{Integer.valueOf(CMMsg.TYP_JUSTICE), "JUSTICE"},
		{Integer.valueOf(CMMsg.TYP_MIND), "MIND"},
		{Integer.valueOf(CMMsg.TYP_PARALYZE), "PARALYZE"},
		{Integer.valueOf(CMMsg.TYP_POISON), "POISON"},
		{Integer.valueOf(CMMsg.TYP_UNDEAD), "UNDEAD"},
	};

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS)||(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		&&(!mob.amDead()))
		{
			for(int i=0;i<immunityTypes.length;i++)
				if(((msg.targetMinor()==((Integer)immunityTypes[i][0]).intValue())||(msg.sourceMinor()==((Integer)immunityTypes[i][0]).intValue()))
				&&((text().toUpperCase().indexOf((String)immunityTypes[i][1])>=0)||(text().toUpperCase().equals("ALL"))))
			{
				String immunityName="certain";
				if(msg.tool()!=null)
					immunityName=msg.tool().name();
				if(mob!=msg.source())
					mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) immune to "+immunityName+" attacks from <T-NAME>.");
				else
					mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) immune to "+immunityName+".");
				return false;
			}
		}
		return true;
	}
}

