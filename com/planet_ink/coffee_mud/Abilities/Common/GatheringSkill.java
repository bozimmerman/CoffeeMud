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
   Copyright 2005-2020 Bo Zimmerman

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

	protected static TreeMap<Room,Quad<Room,Integer,short[],Long>> roomSpamCounter = new TreeMap<Room,Quad<Room,Integer,short[],Long>>();

	public GatheringSkill()
	{
		super();
	}

	protected double getRoomSpamDropRate()
	{
		return 0.25;
	}

	protected boolean checkIfAnyYield(Room R)
	{
		R=CMLib.map().getRoom(R);
		if(R==null)
			return false;
		synchronized(roomSpamCounter)
		{
			if(roomSpamCounter.containsKey(R))
			{
				final Quad<Room,Integer,short[],Long> oldRecord = roomSpamCounter.get(R);
				if((oldRecord!=null)
				&&((System.currentTimeMillis() > oldRecord.fourth.longValue())
				  ||(oldRecord.second.intValue() != R.myResource())))
				{
					roomSpamCounter.remove(R);
					return true;
				}
			}
			final Quad<Room,Integer,short[],Long> curRecord = roomSpamCounter.get(R);
			if(curRecord == null)
				return true;
			final double pctDrop = this.getRoomSpamDropRate() * curRecord.third[0];
			final int finalYield = (int)Math.round(10 - CMath.mul(10, pctDrop));
			if(finalYield >= 1)
				return true;
			return false;
		}
	}

	protected int adjustYieldBasedOnRoomSpam(final int initialYield, final Room R)
	{
		if((R==null)
		||(initialYield==0))
			return 0;
		synchronized(roomSpamCounter)
		{
			final long now=System.currentTimeMillis();
			if(roomSpamCounter.containsKey(R))
			{
				final Quad<Room,Integer,short[],Long> oldRecord = roomSpamCounter.get(R);
				if((oldRecord!=null)
				&&((now > oldRecord.fourth.longValue())
				  ||(oldRecord.second.intValue() != R.myResource())))
					roomSpamCounter.remove(R);
			}
			for(final Iterator<Room> i=roomSpamCounter.keySet().iterator();i.hasNext();)
			{
				final Quad<Room,Integer,short[],Long> rec=roomSpamCounter.get(i.next());
				if(now> rec.fourth.longValue())
					i.remove();
			}
			final Quad<Room,Integer,short[],Long> curRecord = roomSpamCounter.get(R);
			if(curRecord == null)
			{
				final Long expirationTime = new Long(now + (30 * 60 * 1000)); // intentional
				final short[] first = new short[] {1};
				final Quad<Room,Integer,short[],Long> record = new Quad<Room,Integer,short[],Long>(R,Integer.valueOf(R.myResource()),first,expirationTime);
				roomSpamCounter.put(R, record);
				return initialYield;
			}
			else
			{
				final double pctDrop = this.getRoomSpamDropRate() * curRecord.third[0];
				curRecord.third[0]++;
				final int finalYield = (int)Math.round(initialYield - CMath.mul(initialYield, pctDrop));
				if(finalYield <= 0)
					return 0;
				return finalYield;
			}
		}
	}

	@Override
	public void affectPhyStats(final Physical affectedEnv, final PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TRACK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	protected static int fixResourceRequirement(final int resource, int amt)
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

	public boolean bundle(final MOB mob, final List<String> what)
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
		String name=CMParms.combine(what,2);
		int foundResource=-1;
		String foundSubType=null;
		String foundSecret=null;
		Item foundAnyway=null;
		final List<RawMaterial> allFound=new ArrayList<RawMaterial>();
		final List<Integer> maskV=myResources();
		final Hashtable<String,Ability> foundAblesH=new Hashtable<String,Ability>();
		Ability A=null;
		long lowestNonZeroFoodNumber=Long.MAX_VALUE;
		int count=name.lastIndexOf('.');
		if(count > 0)
		{
			final int x=count;
			count=CMath.s_int(name.substring(count+1))-1;
			if(count>=0)
				name=name.substring(0, x);
		}
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if(CMLib.english().containsString(I.Name(),name)
			&&(count--<=0))
			{
				if(foundAnyway==null)
					foundAnyway=I;
				if((I instanceof RawMaterial)
				&&(!CMLib.flags().isOnFire(I))
				&&(!CMLib.flags().isEnchanted(I))
				&&(I.container()==null)
				&&((foundSubType==null)
					||(((RawMaterial)I).getSubType().equals(foundSubType)))
				&&((foundSecret==null)
					||(((RawMaterial)I).rawSecretIdentity().equals(foundSecret)))
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
					foundSubType=((RawMaterial)I).getSubType();
					foundSecret=I.rawSecretIdentity();
					allFound.add((RawMaterial)I);
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
		if(allFound.size()==1)
		{
			commonTell(mob,L("It appears that @x1 is already bundled as much as it can be.",allFound.get(0).Name()));
			return false;
		}
		if(lowestNonZeroFoodNumber==Long.MAX_VALUE)
			lowestNonZeroFoodNumber=0;
		final Item I=(Item)CMLib.materials().makeResource(foundResource,Integer.toString(mob.location().domainType()),true,foundSecret,foundSubType);
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
			int amountToGo=amount;
			for(final RawMaterial I2 : allFound)
			{
				if(I2.basePhyStats().weight()<=amountToGo)
				{
					amountToGo-= I2.basePhyStats().weight();
					I2.destroy();
				}
				else
				{
					I2.basePhyStats().setWeight(I2.basePhyStats().weight()-amountToGo);
					I2.recoverPhyStats();
					amountToGo=0;
					CMLib.materials().adjustResourceName(I2);
					break;
				}
			}
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
