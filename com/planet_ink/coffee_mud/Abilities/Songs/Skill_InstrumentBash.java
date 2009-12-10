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
public class Skill_InstrumentBash extends BardSkill
{
	public String ID() { return "Skill_InstrumentBash"; }
	public String name(){ return "Instrument Bash";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"INSTRUMENTBASH","IBASH"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;}
	public int usageType(){return USAGE_MOVEMENT;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            Item instrument=Play.getInstrument(mob,-1,true);
            if(instrument==null)
                return Ability.QUALITY_INDIFFERENT;
            if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		Item instrument=Play.getInstrument(mob,-1,true);
		if(instrument==null) return false;

		if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
		{
			mob.tell(target.name()+" must stand up first!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		String str=null;
		if(success)
		{
			str=auto?"<T-NAME> is bashed!":"^F^<FIGHT^><S-NAME> bash(es) <T-NAMESELF> with "+instrument.name()+"!^</FIGHT^>^?";
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),str);
            CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Weapon w=CMClass.getWeapon("ShieldWeapon");
				if(w!=null)
				{
					w.setName(instrument.name());
					w.setDisplayText(instrument.displayText());
					w.setDescription(instrument.description());
					w.baseEnvStats().setDamage(instrument.envStats().level()+5+(2*getXLEVELLevel(mob)));
					CMLib.combat().postAttack(mob,target,w);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to bash <T-NAMESELF> with "+instrument.name()+", but end(s) up looking silly.");

		return success;
	}

}
