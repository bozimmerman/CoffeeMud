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
public class Painting extends CommonSkill
{
	public String ID() { return "Painting"; }
	public String name(){ return "Painting";}
	private static final String[] triggerStrings = {"PAINT","PAINTING"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ARTISTIC; }

	protected Item building=null;
	protected boolean messedUp=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
						commonTell(mob,"<S-NAME> mess(es) up painting "+building.name()+".");
					else
						mob.location().addItemRefuse(building,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		try{
		if(commands.size()==0)
		{
			commonTell(mob,"Paint on what? Enter \"paint [canvas name]\" or paint \"wall\".");
			return false;
		}
		String str=CMParms.combine(commands,0);
		building=null;
		messedUp=false;
		Session S=mob.session();
		if((S==null)&&(mob.amFollowing()!=null))
			S=mob.amFollowing().session();
		if(S==null)
		{
			commonTell(mob,"I can't work! I need a player to follow!");
			return false;
		}

		Item I=null;
		if(str.equalsIgnoreCase("wall"))
		{
			if(!CMLib.law().doesOwnThisProperty(mob,mob.location()))
			{
				commonTell(mob,"You need the owners permission to paint the walls here.");
				return false;
			}
		}
		else
		{
			I=mob.location().fetchItem(null,str);
			if((I==null)||(!CMLib.flags().canBeSeenBy(I,mob)))
			{
				commonTell(mob,"You don't see any canvases called '"+str+"' sitting here.");
				return false;
			}
			if((I.material()!=RawMaterial.RESOURCE_COTTON)
			&&(I.material()!=RawMaterial.RESOURCE_SILK)
			&&(!I.Name().toUpperCase().endsWith("CANVAS"))
			&&(!I.Name().toUpperCase().endsWith("SILKSCREEN")))
			{
				commonTell(mob,"You cannot paint on '"+str+"'.");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int duration=25;
		if(str.equalsIgnoreCase("wall"))
		{
			String name=S.prompt("Enter the key words (not the description) for this work.\n\r:","");
			if(name.trim().length()==0) return false;
			Vector V=CMParms.parse(name.toUpperCase());
			for(int v=0;v<V.size();v++)
			{
				String vstr=" "+((String)V.elementAt(v))+" ";
				for(int i=0;i<mob.location().numItems();i++)
				{
					I=mob.location().fetchItem(i);
					if((I!=null)
					&&(I.displayText().length()==0)
					&&(!CMLib.flags().isGettable(I))
					&&((" "+I.name().toUpperCase()+" ").indexOf(vstr)>=0))
					{
						if(S.confirm("'"+I.name()+"' already shares one of these key words ('"+vstr.trim().toLowerCase()+"').  Would you like to destroy it (y/N)? ","N"))
						{
							I.destroy();
							return true;
						}
					}
				}
			}
			String desc=S.prompt("Enter a description for this.\n\r:");
			if(desc.trim().length()==0) return false;
			if(!S.confirm("Wall art key words: '"+name+"', description: '"+desc+"'.  Correct (Y/n)?","Y"))
				return false;
			building=CMClass.getItem("GenWallpaper");
			building.setName(name);
			building.setDescription(desc);
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
		}
		else
		if(I!=null)
		{
			String name=S.prompt("In brief, what is this a painting of?\n\r:");
			if(name.trim().length()==0) return false;
			String desc=S.prompt("Please describe this painting.\n\r:");
			if(desc.trim().length()==0) return false;
			building=CMClass.getItem("GenItem");
			building.setName("a painting of "+name);
			building.setDisplayText("a painting of "+name+" is here.");
			building.setDescription(desc);
			building.baseEnvStats().setWeight(I.baseEnvStats().weight());
			building.setBaseValue(I.baseGoldValue()*(CMLib.dice().roll(1,5,0)));
			building.setMaterial(I.material());
			building.baseEnvStats().setLevel(I.baseEnvStats().level());
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			I.destroy();
		}
		String startStr="<S-NAME> start(s) painting "+building.name()+".";
		displayText="You are painting "+building.name();
		verb="painting "+building.name();
		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();

		messedUp=!proficiencyCheck(mob,0,auto);
		duration=getDuration(25,mob,1,2);

		CMMsg msg=CMClass.getMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		}catch(java.io.IOException e){return false;}
		return true;
	}
}
