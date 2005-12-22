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
public class Skill_Convert extends StdSkill
{
	public String ID() { return "Skill_Convert"; }
	public String name(){ return "Convert";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"CONVERT"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
    protected static DVector convertStack=new DVector(2);

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			mob.tell("You must specify either a deity to convert yourself to, or a player to convert to your religeon.");
            if(mob.isMonster())
                CMLib.commands().postSay(mob,null,"I am unable to convert.",false,false);
			return false;
		}

		MOB target=mob;
		Deity D=CMLib.map().getDeity(CMParms.combine(commands,0));
		if(D==null)
		{
			D=mob.getMyDeity();
			target=getTarget(mob,commands,givenTarget);
			if(target==null)
			{
				mob.tell("You've also never heard of a deity called '"+CMParms.combine(commands,0)+"'.");
                if(mob.isMonster())
                    CMLib.commands().postSay(mob,target,"I've never heard of '"+CMParms.combine(commands,0)+"'.",false,false);
				return false;
			}
			if(D==null)
			{
				mob.tell("A faithless one cannot convert "+target.name()+".");
                if(mob.isMonster())
                    CMLib.commands().postSay(mob,target,"I am faithless, and can not convert you.",false,false);
				return false;
			}
		}
		if(target.isMonster())
		{
			mob.tell("You can't convert "+target.name()+".");
            if(mob.isMonster())
                CMLib.commands().postSay(mob,target,"I can not convert you.",false,false);
			return false;
		}
		if(!auto)
		{
			if(convertStack.contains(target))
			{
				Long L=(Long)convertStack.elementAt(convertStack.getIndex(target),2);
				if((System.currentTimeMillis()-L.longValue())>MudHost.TIME_MILIS_PER_MUDHOUR*5)
					convertStack.removeElement(target);
			}
			if(convertStack.contains(target))
			{
				mob.tell(target.name()+" must wait to be converted again.");
                if(mob.isMonster())
                    CMLib.commands().postSay(mob,target,"You must wait to be converted again.",false,false);
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			if(target!=mob)
			{
				if(target.getMyDeity()!=null)
				{
					mob.tell(target.name()+" is worshiping "+target.getMyDeity().name()+".  "+target.charStats().HeShe()+" must REBUKE "+target.getMyDeity().charStats().himher()+" first.");
                    if(mob.isMonster())
                        CMLib.commands().postSay(mob,target,"You already worship "+target.getMyDeity().Name()+".",false,false);
					return false;
				}
				if(target.getMyDeity()==D)
				{
					mob.tell(target.name()+" already worships "+D.name()+".");
                    if(mob.isMonster())
                        CMLib.commands().postSay(mob,target,"You already worship "+D.Name()+".",false,false);
					return false;
				}
				try
				{
					if(!target.session().confirm(mob.name()+" is trying to convert you to the worship of "+D.name()+".  Is this what you want (N/y)?","N"))
					{
						mob.location().show(mob,target,CMMsg.MSG_SPEAK,"<S-YOUPOSS> attempt to convert <T-NAME> to the worship of "+D.name()+" is rejected.");
						return false;
					}
				}
				catch(Exception e)
				{
					return false;
				}
			}
			Room dRoom=D.location();
			if(dRoom==mob.location()) dRoom=null;
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,auto?"<T-NAME> <T-IS-ARE> converted!":"<S-NAME> convert(s) <T-NAMESELF> to the worship of "+D.name()+".");
			CMMsg msg2=CMClass.getMsg(target,D,this,CMMsg.MSG_SERVE,null);
			if((mob.location().okMessage(mob,msg))
			   &&(mob.location().okMessage(mob,msg2))
			   &&((dRoom==null)||(dRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(target,msg2);
				if(dRoom!=null)
					dRoom.send(target,msg2);
				convertStack.addElement(target,new Long(System.currentTimeMillis()));
				if(mob!=target)
					CMLib.combat().postExperience(mob,null,null,200,false);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to convert <T-NAMESELF>, but <S-IS-ARE> unconvincing.");

		// return whether it worked
		return success;
	}
}
