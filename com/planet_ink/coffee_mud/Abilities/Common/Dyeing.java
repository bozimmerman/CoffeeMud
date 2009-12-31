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
public class Dyeing extends CommonSkill
{
	public String ID() { return "Dyeing"; }
	public String name(){ return "Dyeing";}
	private static final String[] triggerStrings = {"DYE","DYEING"};
    public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ARTISTIC; }
	public String[] triggerStrings(){return triggerStrings;}

	protected Item found=null;
	protected String writing="";
	protected boolean brightFlag = false;
	protected boolean lightFlag = false;
	
	public Dyeing()
	{
		super();
		displayText="You are dyeing...";
		verb="dyeing";
	}

	protected String fixColor(String name, char colorChar, String colorWord)
	{
		int end=name.indexOf("^?");
		if((end>0)&&(end<=name.length()-3))
		{
			int start=name.substring(0,end).indexOf("^");
			if((start>=0)&&(start<(end-3)))
				name=name.substring(0,start)
					 +name.substring(end+3);
		}
		colorWord="^"+colorChar+colorWord+"^?";
		Vector V=CMParms.parse(name);
		for(int v=0;v<V.size();v++)
		{
			String word=(String)V.elementAt(v);
			if((word.equalsIgnoreCase("of"))
			||(word.equalsIgnoreCase("some"))
			||(word.equalsIgnoreCase("an"))
			||(word.equalsIgnoreCase("a"))
			||(word.equalsIgnoreCase("the"))
			   )
			{
				V.insertElementAt(colorWord,v+1);
				return CMParms.combine(V,0);
			}
		}
		V.insertElementAt(colorWord,0);
		return CMParms.combine(V,0);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping))
			{
				MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonEmote(mob,"<S-NAME> mess(es) up the dyeing.");
				else
				{
					StringBuffer desc=new StringBuffer(found.description());
					for(int x=0;x<(desc.length()-1);x++)
					{
						if((desc.charAt(x)=='^')
						&&(desc.charAt(x+1)!='?'))
							desc.setCharAt(x+1,writing.charAt(0));
					}
					String d=desc.toString();
					if(!d.endsWith("^?")) desc.append("^?");
					if(!d.startsWith("^"+writing.charAt(0))) desc.insert(0,"^"+writing.charAt(0));
					found.setDescription(desc.toString());
					String prefix="";
					if(brightFlag) prefix="bright ";
					if(lightFlag) prefix="light ";
					
					found.setName(fixColor(found.Name(),writing.charAt(0),prefix+writing));
					found.setDisplayText(fixColor(found.displayText(),writing.charAt(0),prefix+writing));
					found.text();
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			commonTell(mob,"You must specify what you want to dye, and color to dye it.");
			return false;
		}
		Item target=mob.fetchCarried(null,(String)commands.firstElement());
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTell(mob,"You don't seem to have a '"+((String)commands.firstElement())+"'.");
			return false;
		}
		commands.remove(commands.firstElement());

		if((((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_CLOTH)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LIQUID)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_VEGETATION)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER))
		||(!target.isGeneric()))
		{
			commonTell(mob,"You can't dye that material.");
			return false;
		}
		writing=CMParms.combine(commands,0).toLowerCase();
		boolean darkFlag=false;
		brightFlag=false;
		lightFlag=false;
		if(writing.startsWith("dark "))
		{
			darkFlag=true;
			writing=writing.substring(5).trim();
		}
		else
		if(writing.startsWith("bright "))
		{
			brightFlag=true;
			writing=writing.substring(7).trim();
		}
		else
		if(writing.startsWith("light "))
		{
			lightFlag=true;
			writing=writing.substring(6).trim();
		}
		if(" white green blue red yellow cyan purple ".indexOf(" "+writing.trim()+" ")<0)
		{
			commonTell(mob,"You can't dye anything '"+writing+"'.  Try white, green, blue, red, yellow, cyan, or purple. You can also prefix the colors with the word 'dark', 'light', or 'bright'.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		verb="dyeing "+target.name()+" "+(darkFlag?"dark ":brightFlag?"bright ":lightFlag?"light ":"")+writing;
		displayText="You are "+verb;
		found=target;
		if(darkFlag) writing=CMStrings.capitalizeAndLower(writing);
		if(!proficiencyCheck(mob,0,auto)) 
			writing="";
		int duration=30;
		if((target.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LEATHER)
			duration*=2;
		duration=getDuration(duration,mob,1,6);
		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) dyeing "+target.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
