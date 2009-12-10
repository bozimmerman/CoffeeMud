package com.planet_ink.coffee_mud.Abilities.Fighter;
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
public class Fighter_Rescue extends FighterSkill
{
	public String ID() { return "Fighter_Rescue"; }
	public String name(){ return "Rescue";}
	private static final String[] triggerStrings = {"RESCUE","RES"};
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob==null) return false;
		MOB imfighting=mob.getVictim();
		MOB target=null;

		if((commands.size()==0)
		&&(imfighting!=null)
		&&(imfighting!=mob)
		&&(imfighting.getVictim()!=null)
		&&(imfighting.getVictim()!=mob))
			target=imfighting.getVictim();

		if(target==null)
			target=getTarget(mob,commands,givenTarget);

		if(target==null) return false;
		MOB monster=target.getVictim();

		if((target.amDead())||(monster==null)||(monster.amDead()))
		{
			mob.tell(target.charStats().HeShe()+" isn't fighting anyone!");
			return false;
		}

		if(monster.getVictim()==mob)
		{
			mob.tell("You are already taking the blows from "+monster.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		String str=null;
		if(success)
		{
			str="^F^<FIGHT^><S-NAME> rescue(s) <T-NAMESELF>!^</FIGHT^>^?";
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,str);
            CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				monster.setVictim(mob);
			}
		}
		else
		{
			str="<S-NAME> attempt(s) to rescue <T-NAMESELF>, but fail(s).";
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,str);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}

		return success;
	}

}
