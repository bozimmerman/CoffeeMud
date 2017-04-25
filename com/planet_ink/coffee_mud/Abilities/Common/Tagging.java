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
import java.util.concurrent.atomic.AtomicInteger;

/*
   Copyright 2002-2017 Bo Zimmerman

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
public class Tagging extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Tagging";
	}

	private final static String	localizedName	= CMLib.lang().L("Tagging");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "TAGGING"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_CALLIGRAPHY;
	}

	protected Item		found		  = null;
	protected String	writing		  = "";
	
	protected static final Map<String,String>	tagPrefixes		= new TreeMap<String,String>();

	protected final Map<String,AtomicInteger>	nextTagNumbers	= new TreeMap<String,AtomicInteger>();
	
	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public Tagging()
	{
		super();
		displayText=L("You are tagging...");
		verb=L("tagging");
	}

	protected String getTagLabel()
	{
		return L(" Tag #");
	}
	
	@Override
	public String text()
	{
		return CMParms.combineEQParms(nextTagNumbers, ' ');
	}
	
	@Override
	public void setMiscText(final String text)
	{
		nextTagNumbers.clear();
		if(text.length()>0)
		{
			Map<String,String> map = CMParms.parseEQParms(text);
			for(String key : map.keySet())
				nextTagNumbers.put(key, new AtomicInteger(CMath.s_int(map.get(key))));
		}
	}

	public String getTag(Item itemI)
	{
		final String type = CMClass.getObjectType(itemI).name();
		final String itemKey = type+"/"+itemI.ID();
		if(!tagPrefixes.containsKey(itemKey))
		{
			synchronized(tagPrefixes)
			{
				if(!tagPrefixes.containsKey(itemKey))
				{
					tagPrefixes.clear();
					@SuppressWarnings("unchecked")
					MultiEnumeration<Item> i=new MultiEnumeration<Item>(new Enumeration[] {
						CMClass.basicItems(),
						CMClass.armor(),
						CMClass.clanItems(),
						CMClass.miscMagic(),
						CMClass.weapons()
					});
					final HashSet<String> usedShortKeys=new HashSet<String>();
					for(;i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						final String iType = CMClass.getObjectType(I).name();
						final String iKey = iType+"/"+I.ID();
						final StringBuilder id=new StringBuilder("");
						for(char c : I.ID().toCharArray())
						{
							if(Character.isUpperCase(c))
								id.append(c);
						}
						int num=0;
						while(usedShortKeys.contains(String.format("%1$02d", Integer.valueOf(num))))
							num++;
						final String finalPrefixKey=String.format("%1$02d", Integer.valueOf(num));
						usedShortKeys.add(finalPrefixKey);
						id.append(finalPrefixKey);
						tagPrefixes.put(iKey, id.toString());
					}
				}
			}
		}
		if(!nextTagNumbers.containsKey(itemKey))
			nextTagNumbers.put(itemKey, new AtomicInteger(0));
		return tagPrefixes.get(itemKey)+String.format("%1$04d", Long.valueOf(nextTagNumbers.get(itemKey).addAndGet(1)));
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
					commonTell(mob,L("You mess up your tagging."));
				else
				{
					String desc=found.description();
					final int x=desc.indexOf(getTagLabel());
					if(x>=0)
						desc=desc.substring(0,x);
					else
					if(!writing.equals("REMOVE"))
						found.setDescription(desc+getTagLabel()+writing);
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
		boolean remove=commands.get(commands.size()-1).toString().equalsIgnoreCase("remove");
		if(remove)
			commands.remove(commands.size()-1);
		if(commands.size()<1)
		{
			commonTell(mob,L("You must specify what you want to tag.  Add the word remove to remove a tag."));
			return false;
		}
		String what=CMParms.combine(commands);
		Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,what);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			target=mob.location().findItem(null, what);
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
					commonTell(mob,L("You aren't allowed to work on '@x1'.",what));
					return false;
				}
			}
		}
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",what));
			return false;
		}

		final Ability write=mob.fetchAbility("Skill_Write");
		if(write==null)
		{
			commonTell(mob,L("You must know how to write to tag."));
			return false;
		}

		if(!target.isGeneric())
		{
			commonTell(mob,L("You can't tag that."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(remove)
		{
			writing="REMOVE";
			verb=L("tagging @x1",target.name());
		}
		else
		{
			writing=this.getTag(target);
			verb=L("tagging @x1",target.name());
		}
		displayText=L("You are @x1",verb);
		found=target;
		if((!proficiencyCheck(mob,0,auto))||(!write.proficiencyCheck(mob,0,auto)))
			writing="";
		final int duration=getDuration(30,mob,1,3);
		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),remove?L("<S-NAME> start(s) untagging <T-NAME>."):L("<S-NAME> start(s) tagging <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
