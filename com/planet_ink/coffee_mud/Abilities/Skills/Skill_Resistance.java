package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_Resistance extends StdSkill
{
	public String ID() { return "Skill_Resistance"; }
	public String name(){ return "Resistance";}
	protected String displayText="";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int resistanceCode=0;

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		resistanceCode=0;
		for(int i : CharStats.CODES.SAVING_THROWS())
			if(newText.equalsIgnoreCase(CharStats.CODES.NAME(i))||newText.equalsIgnoreCase(CharStats.CODES.DESC(i)))
				resistanceCode=i;
		if(resistanceCode>0)
			displayText="(Resistance to "+newText.trim().toLowerCase()+")";
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		int amount=(int)Math.round(CMath.mul(CMath.div(proficiency(),100.0),affected.envStats().level()));
		if(resistanceCode>0)
			affectableStats.setStat(resistanceCode,affectableStats.getStat(resistanceCode)+amount);
		else
		for(int i : CharStats.CODES.SAVING_THROWS())
			affectableStats.setStat(i,affectableStats.getStat(i)+amount);
	}
}
