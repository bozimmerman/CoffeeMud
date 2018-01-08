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
   Copyright 2005-2018 Bo Zimmerman

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

public class GatheringSkill extends CommonSkill
{
	@Override
	public String ID()
	{
		return "GatheringSkill";
	}

	private final static String	localizedName	= CMLib.lang().L("GatheringSkill");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "FLETCH", "FLETCHING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "";
	}

	protected static final Map<String, List<Integer>>	supportedResources	= new Hashtable<String, List<Integer>>();

	public GatheringSkill()
	{
		super();
	}

	@Override
	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TRACK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	protected static int fixResourceRequirement(int resource, int amt)
	{
		if(amt<=0)
			return amt;
		switch(resource)
		{
		case RawMaterial.RESOURCE_MITHRIL:
			amt=amt/2;
			break;
		case RawMaterial.RESOURCE_ADAMANTITE:
			amt=amt/3;
			break;
		case RawMaterial.RESOURCE_BALSA:
			amt=amt/2;
			break;
		case RawMaterial.RESOURCE_IRONWOOD:
			amt=amt*2;
			break;
		}
		if(amt<=0)
			amt=1;
		return amt;
	}

	public List<Integer> myResources()
	{
		if(supportedResources.containsKey(ID()))
			return supportedResources.get(ID());
		String mask=supportedResourceString();
		final List<Integer> maskV=new Vector<Integer>();
		String str=mask;
		while(mask.length()>0)
		{
			str=mask;
			final int x=mask.indexOf('|');
			if(x>=0)
			{
				str=mask.substring(0,x);
				mask=mask.substring(x+1);
			}
			else
				mask="";
			if(str.length()>0)
			{
				boolean found=false;
				if(str.startsWith("_"))
				{
					final int rsc=RawMaterial.CODES.FIND_IgnoreCase(str.substring(1));
					if(rsc>=0)
					{
						maskV.add(Integer.valueOf(rsc));
						found=true;
					}
				}
				if(!found)
				{
					final List<Integer> notResources=new ArrayList<Integer>();
					final int y=str.indexOf('-');
					if(y>0)
					{
						final List<String> restV=CMParms.parseAny(str.substring(y+1),"-",true);
						str=str.substring(0,y);
						for(final String sv : restV)
						{
							final int code=RawMaterial.CODES.FIND_CaseSensitive(sv);
							if(code >= 0)
								notResources.add(Integer.valueOf(code));
						}
					}
					final RawMaterial.Material m=RawMaterial.Material.findIgnoreCase(str);
					if(m!=null)
					{
						final List<Integer> rscs=new XVector<Integer>(RawMaterial.CODES.COMPOSE_RESOURCES(m.mask()));
						maskV.addAll(rscs);
						maskV.removeAll(notResources);
						found=rscs.size()>0;
					}
				}
				if(!found)
				{
					final int rsc=RawMaterial.CODES.FIND_IgnoreCase(str);
					if(rsc>=0)
						maskV.add(Integer.valueOf(rsc));
				}
			}
		}
		supportedResources.put(ID(),maskV);
		return maskV;
	}

	public boolean bundle(MOB mob, List<String> what)
	{
		if((what.size()<3)
		||((!CMath.isNumber(what.get(1)))&&(!what.get(1).equalsIgnoreCase("ALL"))))
		{
			commonTell(mob,L("You must specify an amount to bundle, followed by what resource to bundle."));
			return false;
		}
		int amount=CMath.s_int(what.get(1));
		if(what.get(1).equalsIgnoreCase("ALL"))
			amount=Integer.MAX_VALUE;
		if(amount<=0)
		{
			commonTell(mob,L("@x1 is not an appropriate amount.",""+amount));
			return false;
		}
		int numHere=0;
		final Room R=mob.location();
		if(R==null)
			return false;
		final String name=CMParms.combine(what,2);
		int foundResource=-1;
		Item foundAnyway=null;
		final List<Integer> maskV=myResources();
		final Hashtable<String,Ability> foundAblesH=new Hashtable<String,Ability>();
		Ability A=null;
		long lowestNonZeroFoodNumber=Long.MAX_VALUE;
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if(CMLib.english().containsString(I.Name(),name))
			{
				if(foundAnyway==null)
					foundAnyway=I;
				if((I instanceof RawMaterial)
				&&(!CMLib.flags().isOnFire(I))
				&&(!CMLib.flags().isEnchanted(I))
				&&(I.container()==null)
				&&((I.material()==foundResource)
					||((foundResource<0)&&maskV.contains(Integer.valueOf(I.material())))))
				{
					if((I instanceof Decayable)
					&&(((Decayable)I).decayTime()>0)
					&&(((Decayable)I).decayTime()<lowestNonZeroFoodNumber))
						lowestNonZeroFoodNumber=((Decayable)I).decayTime();
					for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
					{
						A=a.nextElement();
						if((A!=null)
						&&(!A.canBeUninvoked())
						&&(!foundAblesH.containsKey(A.ID())))
							foundAblesH.put(A.ID(),A);
					}
					foundResource=I.material();
					numHere+=I.phyStats().weight();
				}
			}
		}
		if((numHere==0)||(foundResource<0))
		{
			if(foundAnyway!=null)
				commonTell(mob,L("You can't bundle @x1 with this skill.",foundAnyway.name()));
			else
				commonTell(mob,L("You don't see any @x1 on the ground here.",name));
			return false;
		}
		if(amount==Integer.MAX_VALUE)
			amount=numHere;
		if(numHere<amount)
		{
			commonTell(mob,L("You only see @x1 pounds of @x2 on the ground here.",""+numHere,name));
			return false;
		}
		if(lowestNonZeroFoodNumber==Long.MAX_VALUE)
			lowestNonZeroFoodNumber=0;
		final Item I=(Item)CMLib.materials().makeResource(foundResource,Integer.toString(mob.location().domainType()),true,null);
		if(I==null)
		{
			commonTell(mob,L("You could not bundle @x1 due to @x2 being an invalid resource code.  Bug it!",name,""+foundResource));
			return false;
		}
		I.basePhyStats().setWeight(amount);
		CMLib.materials().adjustResourceName(I);
		I.setDisplayText(L("@x1 is here.",I.name()));
		if(R.show(mob,null,I,getActivityMessageType(),L("<S-NAME> create(s) <O-NAME>.")))
		{
			final int lostValue=CMLib.materials().destroyResourcesValue(R,amount,foundResource,-1,I);
			I.setBaseValue(lostValue);
			if(I instanceof Food)
				((Food)I).setNourishment(((Food)I).nourishment()*amount);
			if(I instanceof Drink)
				((Drink)I).setLiquidHeld(((Drink)I).liquidHeld()*amount);
			if((!I.amDestroyed())&&(!R.isContent(I)))
				R.addItem(I,ItemPossessor.Expire.Player_Drop);
		}
		if(I instanceof Decayable)
			((Decayable)I).setDecayTime(lowestNonZeroFoodNumber);
		for(final Enumeration<String> e=foundAblesH.keys();e.hasMoreElements();)
			I.addNonUninvokableEffect((Ability)((Environmental)foundAblesH.get(e.nextElement())).copyOf());
		R.recoverRoomStats();
		return true;
	}
}
