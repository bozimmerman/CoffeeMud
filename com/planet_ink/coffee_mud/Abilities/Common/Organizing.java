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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2017-2025 Bo Zimmerman

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
public class Organizing extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Organizing";
	}

	private final static String	localizedName	= CMLib.lang().L("Organizing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "ORGANIZE", "ORGANIZING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_EDUCATIONLORE;
	}

	protected enum OrganizeBy
	{
		TAG,
		VALUE,
		CRAFTER,
		LEVEL,
		TYPE,
		NAME,
		WEIGHT
	}

	protected boolean		descending	= false;
	protected Physical		building	= null;
	protected OrganizeBy	orgaType	= null;
	protected boolean		messedUp	= false;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
						commonTelL(mob,"<S-NAME> mess(es) up organizing @x1.",building.name());
					else
					{
						final List<Item> items=new ArrayList<Item>();
						if(building instanceof Container)
						{
							items.addAll(((Container)building).getContents());
							for(final Item I : items)
							{
								if(((Container) building).owner() instanceof Room)
									((Room)((Container) building).owner()).delItem(I);
								else
								if(((Container) building).owner() instanceof MOB)
									((MOB)((Container) building).owner()).delItem(I);
							}
						}
						else
						if(building instanceof ItemPossessor)
						{
							for(final Enumeration<Item> i=((ItemPossessor)building).items();i.hasMoreElements();)
							{
								final Item I=i.nextElement();
								if((I!=null)&&(I.container()==null))
									items.add(I);
							}
							for(final Item I : items)
								((ItemPossessor)building).delItem(I);
						}
						final OrganizeBy orgaT=this.orgaType;
						final Organizing me = this;
						final Comparator<Item> compare=new Comparator<Item>()
						{
							@Override
							public int compare(final Item o1, final Item o2)
							{
								int result=0;
								switch(orgaT)
								{
								case CRAFTER:
									final String brand1=CMLib.ableParms().getCraftingBrand(o1);
									final String brand2=CMLib.ableParms().getCraftingBrand(o2);
									result = brand1.compareTo(brand2);
									break;
								case LEVEL:
									if(o1.phyStats().level()==o2.phyStats().level())
										result=0;
									else
									if(o1.phyStats().level()<o2.phyStats().level())
										result=-1;
									else
										result=1;
									break;
								case NAME:
									result=CMLib.english().removeArticleLead(o1.Name()).compareTo(CMLib.english().removeArticleLead(o2.Name()));
									break;
								case TAG:
									result=Labeling.getCurrentTag(o1).compareTo(Labeling.getCurrentTag(o2));
									break;
								case TYPE:
									result = CMClass.getObjectType(o1).name().compareTo(CMClass.getObjectType(o2).name());
									if(result == 0)
										result = o1.ID().compareTo(o2.ID());
									break;
								case VALUE:
									if(o1.baseGoldValue()==o2.baseGoldValue())
										result=0;
									else
									if(o1.baseGoldValue()<o2.baseGoldValue())
										result=-1;
									else
										result=1;
									break;
								case WEIGHT:
									if(o1.phyStats().weight()==o2.phyStats().weight())
										result=0;
									else
									if(o1.phyStats().weight()<o2.phyStats().weight())
										result=-1;
									else
										result=1;
									break;
								}
								if((result == 0)&&(orgaT != OrganizeBy.NAME))
									result=CMLib.english().removeArticleLead(o1.Name()).compareTo(CMLib.english().removeArticleLead(o2.Name()));
								if(me.descending)
									result = (result == 0) ? 0 : (result < 0) ? 1 : -1;
								return result;
							}
						};
						Collections.sort(items, compare);
						if(building instanceof Container)
						{
							for(final Item I : items)
							{
								if(((Container) building).owner() instanceof Room)
									((Room)((Container) building).owner()).addItem(I);
								else
								if(((Container) building).owner() instanceof MOB)
									((MOB)((Container) building).owner()).addItem(I);
							}
						}
						else
						{
							for(final Item I : items)
							{
								if(building instanceof ItemPossessor)
									((ItemPossessor)building).addItem(I);
							}
						}
						items.clear();
					}
				}
				building=null;
				orgaType=null;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(super.checkStop(mob, commands)||(R==null))
			return true;

		building=null;
		orgaType=null;
		messedUp=false;
		this.descending=false;

		if(commands.size()>2)
		{
			if("ASCENDING".startsWith(commands.get(commands.size()-1).toUpperCase()))
			{
				commands.remove(commands.size()-1);
			}
			else
			if("DESCENDING".startsWith(commands.get(commands.size()-1).toUpperCase()))
			{
				commands.remove(commands.size()-1);
				this.descending=true;
			}
		}

		if(commands.size()<2)
		{
			commonTelL(mob,"Organize what? Try ROOM or a container name, and also one of these: "
					+CMLib.english().toEnglishStringList(OrganizeBy.class,false));
			return false;
		}

		final String orgaTypeName = commands.remove(commands.size()-1).toUpperCase().trim();
		orgaType = (OrganizeBy)CMath.s_valueOf(OrganizeBy.class, orgaTypeName);
		if(orgaType == null)
		{
			commonTelL(mob,"'@x1' is invalid. Try one of these: "
					+CMLib.english().toEnglishStringList(OrganizeBy.class,false),orgaTypeName);
			return false;
		}



		final String str=CMParms.combine(commands);
		building=null;
		if(str.equalsIgnoreCase("room"))
		{
			building=mob.location();
			if((CMLib.law().getLandTitle(mob.location())!=null)
			&&(!CMLib.law().doesHavePriviledgesHere(mob,mob.location())))
			{
				commonFaiL(mob,commands,"You need the owners permission to organize stuff here.");
				return false;
			}
		}
		else
		{
			final Physical I=super.getAnyTarget(mob, commands, givenTarget, Wearable.FILTER_ANY, false);
			if((!(I instanceof ItemPossessor)) && (!(I instanceof Container)))
			{
				commonFaiL(mob,commands,"You cannot organize the contents of '@x1'.",str);
				return false;
			}
			if(I instanceof MOB)
			{
				if(!mob.getGroupMembers(new HashSet<MOB>()).contains(I))
				{
					commonFaiL(mob,commands,"You aren't allowed to organize stuff for @x1.",I.Name());
					return false;
				}
			}
			else
			if((I instanceof Item)&&(((Item)I).owner() instanceof MOB))
			{
				if((CMLib.law().getLandTitle(mob.location())!=null)
				&&(!CMLib.law().doesHavePriviledgesHere(mob,mob.location())))
				{
					commonFaiL(mob,commands,"You need the owners permission to organize stuff here.");
					return false;
				}
			}
			building=I;
		}

		int duration=15;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
		{
			building=null;
			return false;
		}

		final String startStr=L("<S-NAME> start(s) organizing @x1 by @x2.",building.name(),orgaType.name().toLowerCase());
		displayText=L("You are organizing @x1",building.name());
		verb=L("organizing @x1",building.name());
		building.recoverPhyStats();
		building.text();
		building.recoverPhyStats();

		messedUp=!proficiencyCheck(mob,0,auto);
		duration=getDuration(15,mob,1,2);

		final CMMsg msg=CMClass.getMsg(mob,building,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
