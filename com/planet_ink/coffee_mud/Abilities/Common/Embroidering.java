package com.planet_ink.coffee_mud.Abilities.Common;
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

public class Embroidering extends CommonSkill
{
	public String ID() { return "Embroidering"; }
	public String name(){ return "Embroidering";}
	private static final String[] triggerStrings = {"EMBROIDER","EMBROIDERING"};
	public String[] triggerStrings(){return triggerStrings;}

	private Item found=null;
	private String writing="";
	public Embroidering()
	{
		super();
		displayText="You are embroidering...";
		verb="embroidering";
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping))
			{
				MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonTell(mob,"You mess up your embroidery.");
				else
				{
					String desc=found.description();
					int x=desc.indexOf(" Embroidered on it are the words `");
					int y=desc.lastIndexOf("`");
					if((x>=0)&&(y>x))
						desc=desc.substring(0,x);
					found.setDescription(desc+" Embroidered on it are the words `"+writing+"`.");
				}
			}
		}
		super.unInvoke();
	}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Skill_Write")==null)
		{
			teacher.tell(student.name()+" has not yet learned how to write.");
			student.tell("You need to learn how to write before you can learn "+name()+".");
			return false;
		}

		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			commonTell(mob,"You must specify what you want to embroider onto, and what words to embroider on it.");
			return false;
		}
		Item target=mob.fetchCarried(null,(String)commands.firstElement());
		if((target==null)||(!Sense.canBeSeenBy(target,mob)))
		{
			commonTell(mob,"You don't seem to have a '"+((String)commands.firstElement())+"'.");
			return false;
		}
		else
			commands.remove(commands.firstElement());
		
		Ability write=mob.fetchAbility("Skill_Write");
		if(write==null)
		{
			commonTell(mob,"You must know how to write to embroider.");
			return false;
		}

		if((((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_CLOTH)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_LEATHER))
		||(!target.isGeneric()))
		{
			commonTell(mob,"You can't embroider onto that material.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		writing=Util.combine(commands,0);
		verb="embroidering on "+target.name();
		displayText="You are "+verb;
		found=target;
		if((!profficiencyCheck(mob,0,auto))||(!write.profficiencyCheck(mob,0,auto)))
			writing="";
		int duration=30-mob.envStats().level();
		if(duration<6) duration=6;
		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_HANDS,"<S-NAME> start(s) embroidering on <T-NAME>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}