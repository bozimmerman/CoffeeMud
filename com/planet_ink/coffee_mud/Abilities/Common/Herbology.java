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
public class Herbology extends CommonSkill
{
	public String ID() { return "Herbology"; }
	public String name(){ return "Herbology";}
	private static final String[] triggerStrings = {"HERBOLOGY"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_NATURELORE; }
    public String parametersFormat(){ return "HERB_NAME";}

	protected Item found=null;
	protected boolean messedUp=false;

	public Herbology()
	{
		super();
		displayText="You are evaluating...";
		verb="evaluating";
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping))
			{
				MOB mob=(MOB)affected;
				if(messedUp)
					commonTell(mob,"You lose your concentration on "+found.name()+".");
				else
				{
				    Vector herbList=Resources.getFileLineVector(Resources.getFileResource("skills/herbology.txt",true));
					String herb=null;
                    while((herbList.size()>2)&&((herb==null)||(herb.trim().length()==0)))
                        herb=((String)herbList.elementAt(CMLib.dice().roll(1,herbList.size(),-1))).trim().toLowerCase();
					
					if(found.rawSecretIdentity().length()>0)
					{	
						herb=found.rawSecretIdentity();
						found.setSecretIdentity("");
					}
					
					commonTell(mob,found.name()+" appears to be "+herb+".");
					String name=found.Name();
					name=name.substring(0,name.length()-5).trim();
					if(name.length()>0)
						found.setName(name+" "+herb);
					else
						found.setName("some "+herb);
					found.setDisplayText(found.Name()+" is here");
					found.setDescription("");
					found.text();
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			commonTell(mob,"You must specify what herb you want to identify.");
			return false;
		}
		Item target=mob.fetchCarried(null,CMParms.combine(commands,0));
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTell(mob,"You don't seem to have a '"+((String)commands.firstElement())+"'.");
			return false;
		}
		commands.remove(commands.firstElement());

		if((target.material()!=RawMaterial.RESOURCE_HERBS)
		||((!target.Name().toUpperCase().endsWith(" HERBS"))
		   &&(!target.Name().equalsIgnoreCase("herbs")))
		||(!(target instanceof RawMaterial))
		||(!target.isGeneric()))
		{
			commonTell(mob,"You can only identify unknown herbs.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		verb="studying "+target.name();
		displayText="You are "+verb;
		found=target;
		messedUp=false;
		if(!proficiencyCheck(mob,0,auto)) messedUp=true;
		int duration=getDuration(15,mob,1,2);
		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> stud(ys) "+target.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
