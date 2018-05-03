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
public class Farming extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Farming";
	}

	private final static String localizedName = CMLib.lang().L("Farming");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings = I(new String[] { "PLANT", "FARM", "FARMING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_GATHERINGSKILL;
	}

	@Override
	protected boolean allowedWhileMounted()
	{
		return false;
	}

	@Override
	public String supportedResourceString()
	{
		return "VEGETATION|COTTON|HEMP|WOODEN";
	}

	protected Item		found			= null;
	protected Room		room			= null;
	protected String	foundShortName	= "";

	public Farming()
	{
		super();
		displayText=L("You are planting...");
		verb=L("planting");
	}

	protected int getDuration(MOB mob, int level)
	{
		return getDuration(45,mob,level,15);
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			final MOB mob=invoker();
			if(tickUp==6)
			{
				if((found==null)
				||(mob==null)
				||(mob.location()==null))
				{
					commonTell(mob,L("Your @x1 crop has failed.\n\r",foundShortName));
					unInvoke();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	protected boolean isCompost(final Item I)
	{
		return ((I!=null) &&(I.rawSecretIdentity().equals("compost")));
	}
	
	protected int deCompost(final Item I, int doubleRemain)
	{
		int amount=0;
		if(I.phyStats().weight()<=doubleRemain)
		{
			amount+=I.phyStats().weight();
			doubleRemain-=I.phyStats().weight();
			I.destroy();
		}
		else
		{
			if(I.basePhyStats().weight()<=doubleRemain)
				I.destroy();
			else
				I.basePhyStats().setWeight(I.basePhyStats().weight()-doubleRemain);
			amount+=doubleRemain;
			doubleRemain=0;
		}
		return amount;
	}
	
	@Override
	public void unInvoke()
	{
		final boolean isaborted=aborted;
		final Environmental aff=affected;
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected==room))
			{
				if((found!=null)&&(!isaborted))
				{
					int amount=CMLib.dice().roll(1,7,0)*(baseYield()+abilityCode());
					int origAmount = amount; 
					int doubleRemain = amount * 10;
					for(Enumeration<Item> i=room.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if((I instanceof PackagedItems)
						&&(((PackagedItems)I).areAllItemsTheSame()))
						{
							Item I2=((PackagedItems)I).peekFirstItem();
							while(isCompost(I2))
							{
								int amt=deCompost(I2,doubleRemain);
								if(I2.amDestroyed())
									((PackagedItems)I).setNumberOfItemsInPackage(((PackagedItems)I).numberOfItemsInPackage()-1);
								if(amt != 0)
								{
									amount += amt * origAmount;
									doubleRemain -=amt;
								}
								I2.destroy();
								if(((PackagedItems)I).numberOfItemsInPackage()==0)
								{
									I.destroy();
									break;
								}
								I2=((PackagedItems)I).peekFirstItem();
							}
							I2.destroy();
						}
						else
						if(isCompost(I))
						{
							int amt=deCompost(I,doubleRemain);
							if(amt != 0)
							{
								amount += amt * origAmount;
								doubleRemain -=amt;
							}
						}
						if(doubleRemain <= 0)
							break;
					}
					String s="s";
					if(amount==1)
						s="";
					room.showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 pound@x2 of @x3 have grown here.",""+amount,s,foundShortName));
					for(int i=0;i<amount;i++)
					{
						final Item newFound=(Item)found.copyOf();
						if(!dropAWinner(null,room,newFound))
							break;
					}
				}
			}
		}
		super.unInvoke();
		if((canBeUninvoked)
		&&(aff!=null)
		&&(aff instanceof MOB)
		&&(aff!=room)
		&&(!isaborted)
		&&(room!=null))
		{
			final Farming F=((Farming)copyOf());
			F.unInvoked=false;
			F.tickUp=0;
			F.tickDown=50;
			F.startTickDown(invoker,room,50);
		}
	}

	public boolean isPotentialCrop(Room R, int code)
	{
		if(R==null)
			return false;
		if(R.resourceChoices()==null)
			return false;
		for(int i=0;i<R.resourceChoices().size();i++)
		{
			if(R.resourceChoices().get(i).intValue()==code)
				return true;
		}
		return false;
	}

	private boolean plantable(MOB mob, Item I2)
	{
		if((I2!=null)
		&&(I2 instanceof RawMaterial)
		&&(CMLib.flags().canBeSeenBy(I2,mob))
		&&(I2.container()==null)
		&&(((I2.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
			||(I2.material()==RawMaterial.RESOURCE_COTTON)
			||(I2.material()==RawMaterial.RESOURCE_HEMP)
			||((I2.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)))
			return true;
		return false;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&((commands.get(0)).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}

		verb=L("planting");
		if((!auto)&&((mob.location().domainType()&Room.INDOORS)>0))
		{
			commonTell(mob,L("You can't plant anything indoors!"));
			return false;
		}
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_HILLS)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_PLAINS)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_SWAMP))
		{
			commonTell(mob,L("The land is not suitable for farming here."));
			return false;
		}
		if((!auto)&&(mob.location().getArea().getClimateObj().weatherType(mob.location())==Climate.WEATHER_DROUGHT))
		{
			commonTell(mob,L("The current drought conditions make planting useless."));
			return false;
		}
		if(mob.location().fetchEffect(ID())!=null)
		{
			commonTell(mob,L("It looks like a crop is already growing here."));
			return false;
		}
		if(mob.isMonster()
		&&(!auto)
		&&(!CMLib.flags().isAnimalIntelligence(mob))
		&&(commands.size()==0))
		{
			Item mine=null;
			for(int i=0;i<mob.location().numItems();i++)
			{
				final Item I2=mob.location().getItem(i);
				if(plantable(mob,I2))
				{
					mine=I2;
					commands.add(RawMaterial.CODES.NAME(I2.material()));
					break;
				}
			}
			if(mine==null)
			{
				for(int i=0;i<mob.numItems();i++)
				{
					final Item I2=mob.getItem(i);
					if(plantable(mob,I2))
					{
						commands.add(RawMaterial.CODES.NAME(I2.material()));
						mine=(Item)I2.copyOf();
						if(mob.location().findItem(null,mob.location().getContextName(I2))==null)
							mob.location().addItem(mine,ItemPossessor.Expire.Resource);
						break;
					}
				}
			}
			if(mine==null)
			{
				commonTell(mob,L("You don't have anything you can plant."));
				return false;
			}
		}
		else
		if(commands.size()==0)
		{
			commonTell(mob,L("Grow what?"));
			return false;
		}
		int code=-1;
		final String what=CMParms.combine(commands,0).toUpperCase();
		final RawMaterial.CODES codes = RawMaterial.CODES.instance();
		for(final int cd : codes.all())
		{
			final String str=codes.name(cd).toUpperCase();
			if((str.equals(what))
			&&(((cd&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
			  ||(cd==RawMaterial.RESOURCE_COTTON)
			  ||(cd==RawMaterial.RESOURCE_HEMP)
			  ||((cd&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)))
			{
				code=cd;
				foundShortName=CMStrings.capitalizeAndLower(str);
				break;
			}
		}
		if(code<0)
		{
			for(final int cd : codes.all())
			{
				final String str=codes.name(cd).toUpperCase();
				if((str.toUpperCase().startsWith(what)||(what.startsWith(str)))
				&&(((cd&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
				  ||(cd==RawMaterial.RESOURCE_COTTON)
				  ||(cd==RawMaterial.RESOURCE_HEMP)
				  ||((cd&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)))
				{
					code=cd;
					foundShortName=CMStrings.capitalizeAndLower(str);
					break;
				}
			}
		}
		if(code<0)
		{
			commonTell(mob,L("You've never heard of '@x1'.",CMParms.combine(commands,0)));
			return false;
		}

		Item mine=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if(plantable(mob,I)
			&&(I.material()==code))
			{
				mine = I;
				break;
			}
		}
		if(mine==null)
		{
			commonTell(mob,L("You'll need to have some @x1 to seed from on the ground first.",foundShortName));
			return false;
		}
		final String mineName=mine.name();
		mine=(Item)CMLib.materials().unbundle(mine,-1,null);
		if(mine==null)
		{
			commonTell(mob,L("'@x1' is not suitable for use as a seed crop.",mineName));
			return false;
		}
		if(!(isPotentialCrop(mob.location(),code)))
		{
			commonTell(mob,L("'@x1' does not seem to be taking root here.",mineName));
			return false;
		}

		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((proficiencyCheck(mob,0,auto))
		&&(isPotentialCrop(mob.location(),code)))
		{
			found=(Item)CMLib.materials().makeResource(code,Integer.toString(mob.location().domainType()),false,null);
			if((found!=null)
			&&(found.material()==RawMaterial.RESOURCE_HERBS)
			&&(mine.material()==found.material()))
			{
				found.setName(mine.name());
				found.setDisplayText(mine.displayText());
				found.setDescription(mine.description());
				found.text();
			}
		}

		mine.destroy();
		final int duration=getDuration(mob,1);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) planting @x1.",foundShortName));
		verb=L("planting @x1",foundShortName);
		displayText=L("You are planting @x1",foundShortName);
		room=mob.location();
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
