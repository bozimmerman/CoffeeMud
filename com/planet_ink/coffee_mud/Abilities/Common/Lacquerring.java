package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Lacquerring extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Lacquerring";
	}

	private final static String	localizedName	= CMLib.lang().L("Lacquering");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "LACQUERING", "LACQUER" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ARTISTIC;
	}

	protected Item		found	= null;
	protected String	writing	= "";

	public Lacquerring()
	{
		super();
		displayText=L("You are lacquering...");
		verb=L("lacquering");
	}

	protected String fixColor(String name, char color, String colorWord)
	{
		final int end=name.indexOf("^?");
		if((end>0)&&(end<=name.length()-3))
		{
			final int start=name.substring(0,end).indexOf('^');
			if((start>=0)&&(start<(end-3)))
				name=name.substring(0,start)+name.substring(end+3);
		}
		colorWord="^"+color+colorWord+"^?";
		final Vector<String> V=CMParms.parse(name);
		for(int v=0;v<V.size();v++)
		{
			final String word=V.elementAt(v);
			if((word.equalsIgnoreCase("an")) || (word.equalsIgnoreCase("a")))
			{
				final String properPrefix=CMLib.english().properIndefiniteArticle(colorWord);
				V.insertElementAt(colorWord,v+1);
				if(word.toLowerCase().equals(word))
					V.set(v,properPrefix.toLowerCase());
				else
					V.set(v,CMStrings.capitalizeAndLower(properPrefix));
				return CMParms.combine(V,0);
			}
			else
			if((word.equalsIgnoreCase("of"))
			||(word.equalsIgnoreCase("some"))
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

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonEmote(mob,L("<S-NAME> mess(es) up the lacquering."));
				else
				{
					final StringBuffer desc=new StringBuffer(found.description());
					for(int x=0;x<(desc.length()-1);x++)
					{
						if((desc.charAt(x)=='^')
						&&(desc.charAt(x+1)!='?')
						&&(desc.charAt(x+1)!='_')
						&&(desc.charAt(x+1)!='#'))
							desc.setCharAt(x+1,writing.charAt(0));
					}
					final String d=desc.toString();
					if(!d.endsWith("^?"))
						desc.append("^?");
					if(!d.startsWith("^"+writing.charAt(0)))
						desc.insert(0,"^"+writing.charAt(0));
					found.setDescription(desc.toString());
					final String writingName=Character.isUpperCase(writing.charAt(0))?"dark "+writing.toLowerCase():writing.toLowerCase();
					found.setName(fixColor(found.Name(),writing.charAt(0),writingName));
					found.setDisplayText(fixColor(found.displayText(),writing.charAt(0),writingName));
					found.text();
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()<2)
		{
			commonTell(mob,L("You must specify what you want to lacquer, and the color to lacquer it in."));
			return false;
		}
		Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,commands.get(0));
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
			target=mob.location().findItem(null, commands.get(0));
		if((target!=null)&&(CMLib.flags().canBeSeenBy(target,mob)))
		{
			final Set<MOB> followers=mob.getGroupMembers(new TreeSet<MOB>());
			boolean ok=false;
			for(final MOB M : followers)
			{
				if(target.secretIdentity().indexOf(getBrand(M))>=0)
					ok=true;
			}
			if(!ok)
			{
				commonTell(mob,L("You aren't allowed to work on '@x1'.",(commands.get(0))));
				return false;
			}
		}
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",(commands.get(0))));
			return false;
		}
		commands.remove(commands.get(0));

		if((((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GLASS)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_SYNTHETIC)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ROCK)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN))
		||(!target.isGeneric()))
		{
			commonTell(mob,L("You can't lacquer that material."));
			return false;
		}

		writing=CMParms.combine(commands,0).toLowerCase();
		boolean darkFlag=false;
		if(writing.startsWith("dark "))
		{
			darkFlag=true;
			writing=writing.substring(5).trim();
		}
		if((" white green blue red yellow cyan purple ".indexOf(" "+writing.trim()+" ")<0)||(writing.trim().indexOf(' ')>=0))
		{
			commonTell(mob,L("You can't lacquer anything '@x1'.  Try white, green, blue, red, yellow, cyan, or purple. You can also prefix the colors with the word 'dark'.",writing));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		verb=L("lacquering @x1 @x2",target.name(),(darkFlag?"dark ":"")+writing);
		displayText=L("You are @x1",verb);
		found=target;
		if(darkFlag)
			writing=CMStrings.capitalizeAndLower(writing);
		if(!proficiencyCheck(mob,0,auto))
			writing="";
		final int duration=getDuration(60,mob,1,12);
		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),L("<S-NAME> start(s) lacquering <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
