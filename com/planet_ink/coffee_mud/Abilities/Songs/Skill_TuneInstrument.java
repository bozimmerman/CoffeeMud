package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2000-2006 Bo Zimmerman

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
public class Skill_TuneInstrument extends BardSkill
{
	public String ID() { return "Skill_TuneInstrument"; }
	public String name(){ return "Tune Instrument";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"TUNEINSTRUMENT","TUNE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
    private static final int EXPERTISE_STAGES=10;
    private static final String[] EXPERTISE={"TUNING"};
    private static final String[] EXPERTISE_NAME={"Tuning"};
    public void initializeClass()
    {
        super.initializeClass();
        if(CMLib.expertises().getDefinition(EXPERTISE[0]+EXPERTISE_STAGES)==null)
            for(int i=1;i<=EXPERTISE_STAGES;i++)
                CMLib.expertises().addDefinition(EXPERTISE[0]+i,EXPERTISE_NAME[0]+" "+CMath.convertToRoman(i),
                        "","+CHA "+(16+i)+" -LEVEL +>="+(27+(5*i)),0,1,0,0,0);
        registerExpertiseUsage(EXPERTISE,EXPERTISE_STAGES,false,null);
    }
    protected int getXLevel(MOB mob){ return getExpertiseLevel(mob,EXPERTISE[0]);}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setAbility(affectableStats.ability()+2+getXLevel(invoker));
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=Play.getInstrument(mob,-1,true);
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target.name()+" is already tuned.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,"<S-NAME> tune(s) <T-NAMESELF>.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to tune <T-NAMESELF>, but mess(es) up.");


		// return whether it worked
		return success;
	}
}
