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
public class Thief_CarefulStep extends ThiefSkill
{
	public String ID() { return "Thief_CarefulStep"; }
	public String name(){ return "Careful Step";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
    public double castingTime(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFABLETIME),200.0);}
    public double combatCastingTime(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMABLETIME),200.0);}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"CARESTEP","CAREFULSTEP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT;}
    public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_ACROBATIC; }

    public boolean preInvoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
    {
        if(secondsElapsed==0)
        {
            String dir=CMParms.combine(commands,0);
            if(commands.size()>0) dir=(String)commands.lastElement();
            int dirCode=Directions.getGoodDirectionCode(dir);
            if(dirCode<0)
            {
                mob.tell("Step where?");
                return false;
            }
            if(mob.isInCombat())
            {
                mob.tell("Not while you are fighting!");
                return false;
            }
    
            if((mob.location().getRoomInDir(dirCode)==null)||(mob.location().getExitInDir(dirCode)==null))
            {
                mob.tell("Step where?");
                return false;
            }
            CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_DELICATE_HANDS_ACT,"<S-NAME> start(s) walking carefully "+Directions.getDirectionName(dirCode)+".");
            if(mob.location().okMessage(mob,msg))
                mob.location().send(mob,msg);
            else
                return false;
        }
        return true;
    }
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String dir=CMParms.combine(commands,0);
		if(commands.size()>0) dir=(String)commands.lastElement();
		int dirCode=Directions.getGoodDirectionCode(dir);
        if(!preInvoke(mob,commands,givenTarget,auto,asLevel,0,0.0))
            return false;

        MOB highestMOB=getHighestLevelMOB(mob,null);
		int levelDiff=mob.envStats().level()+(2*super.getXLEVELLevel(mob))-getMOBLevel(highestMOB);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=false;
		CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_DELICATE_HANDS_ACT,"<S-NAME> walk(s) carefully "+Directions.getDirectionName(dirCode)+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(levelDiff<0)
				levelDiff=levelDiff*8;
			else
				levelDiff=levelDiff*10;
			success=proficiencyCheck(mob,levelDiff,auto);
			int oldDex=mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY);
			if(success)
				mob.baseCharStats().setStat(CharStats.STAT_DEXTERITY,oldDex+100);
			mob.recoverCharStats();
			CMLib.tracking().move(mob,dirCode,false,false);
			if(oldDex!=mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY))
				mob.baseCharStats().setStat(CharStats.STAT_DEXTERITY,oldDex);
			mob.recoverCharStats();
		}
		return success;
	}

}
