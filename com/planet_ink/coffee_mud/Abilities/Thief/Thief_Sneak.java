package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_Sneak extends ThiefSkill
{
	public String ID() { return "Thief_Sneak"; }
	public String name(){ return "Sneak";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALTHY;}
	private static final String[] triggerStrings = {"SNEAK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String dir=CMParms.combine(commands,0);
		if(commands.size()>0) dir=(String)commands.lastElement();
		int dirCode=Directions.getGoodDirectionCode(dir);
		if(dirCode<0)
		{
			mob.tell("Sneak where?");
			return false;
		}

		if((mob.location().getRoomInDir(dirCode)==null)||(mob.location().getExitInDir(dirCode)==null))
		{
			mob.tell("Sneak where?");
			return false;
		}

        MOB highestMOB=getHighestLevelMOB(mob,null);
		int levelDiff=(mob.envStats().level()+(super.getXLEVELLevel(mob)*2))-getMOBLevel(highestMOB);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=false;
		CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_DELICATE_HANDS_ACT,"You quietly sneak "+Directions.getDirectionName(dirCode)+".",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(levelDiff<0)
				levelDiff=levelDiff*8;
			else
				levelDiff=levelDiff*10;
			success=proficiencyCheck(mob,levelDiff,auto);
			if(success)
			{
				mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()|EnvStats.IS_SNEAKING);
				mob.recoverEnvStats();
			}
			CMLib.tracking().move(mob,dirCode,false,false);
			if(success)
			{

				int disposition=mob.baseEnvStats().disposition();
				if((disposition&EnvStats.IS_SNEAKING)>0)
				{
					mob.baseEnvStats().setDisposition(disposition-EnvStats.IS_SNEAKING);
					mob.recoverEnvStats();
				}
				Ability toHide=mob.fetchAbility("Thief_Hide");
				if(toHide==null) toHide=mob.fetchAbility("Ranger_Hide");
				if(toHide!=null)
					toHide.invoke(mob,new Vector(),null,false,asLevel);
			}
			if(CMLib.flags().isSneaking(mob))
				mob.envStats().setDisposition(mob.envStats().disposition()-EnvStats.IS_SNEAKING);
		}
		return success;
	}

}
