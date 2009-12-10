package com.planet_ink.coffee_mud.Abilities.Common;
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

@SuppressWarnings("unchecked")
public class Engraving extends CommonSkill
{
	public String ID() { return "Engraving"; }
	public String name(){ return "Engraving";}
	private static final String[] triggerStrings = {"ENGRAVE","ENGRAVING"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_CALLIGRAPHY; }

	protected Item found=null;
	protected String writing="";
	public Engraving()
	{
		super();
		displayText="You are engraving...";
		verb="engraving";
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping))
			{
				MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonTell(mob,"You mess up your engraving.");
				else
				{
					String desc=found.description();
					int x=desc.indexOf(" Engraved on it are the words `");
					int y=desc.lastIndexOf("`");
					if((x>=0)&&(y>x))
						desc=desc.substring(0,x);
					found.setDescription(desc+" Engraved on it are the words `"+writing+"`.");
				}
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			commonTell(mob,"You must specify what you want to engrave onto, and what words to engrave on it.");
			return false;
		}
		Item target=mob.fetchCarried(null,(String)commands.firstElement());
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTell(mob,"You don't seem to have a '"+((String)commands.firstElement())+"'.");
			return false;
		}
		commands.remove(commands.firstElement());

		Ability write=mob.fetchAbility("Skill_Write");
		if(write==null)
		{
			commonTell(mob,"You must know how to write to engrave.");
			return false;
		}
		
		if((((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GLASS)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ROCK)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PLASTIC)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL))
		||(!target.isGeneric()))
		{
			commonTell(mob,"You can't engrave onto that material.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		writing=CMParms.combine(commands,0);
		verb="engraving on "+target.name();
		displayText="You are "+verb;
		found=target;
		if((!proficiencyCheck(mob,0,auto))||(!write.proficiencyCheck(mob,0,auto)))
			writing="";
		int duration=getDuration(30,mob,1,3);
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_HANDS,"<S-NAME> start(s) engraving on <T-NAME>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
