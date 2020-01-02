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
   Copyright 2018-2020 Bo Zimmerman

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
public class Gardening extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Gardening";
	}

	private final static String localizedName = CMLib.lang().L("Gardening");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings = I(new String[] { "GPLANT", "GARDEN", "GARDENING" });

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
		return "FLOWERS|HERBS";
	}

	protected Item		found			= null;
	protected Room		room			= null;
	protected String	foundShortName	= "";
	protected int		goodticks		= 0;

	public Gardening()
	{
		super();
		displayText=L("You are gardening...");
		verb=L("gardening");
	}

	protected int getDuration(final MOB mob, final int level)
	{
		return getDuration(45,mob,level,15);
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			final Room R=(Room)affected;
			if(R!=null)
			{
				if(R.getArea().getClimateObj().canSeeTheSun(R))
					goodticks++;
				final MOB mob=invoker();
				if(tickUp==6)
				{
					if((found==null)
					||(mob==null)
					||(mob.location()==null))
					{
						commonTell(mob,L("Your @x1 garden has failed.\n\r",foundShortName));
						unInvoke();
					}
				}
				else
				if((tickUp > 10)&&(goodticks < (tickUp/2)))
				{
					found=null;
					commonTell(mob,L("Your @x1 garden has failed due to lack of sunlight.\n\r",foundShortName));
					unInvoke();
				}
			}
		}
		else
			this.goodticks=0;
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
					final int origAmount = amount;
					int doubleRemain = amount * 10;
					for(final Enumeration<Item> i=room.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if((I instanceof PackagedItems)
						&&(((PackagedItems)I).areAllItemsTheSame()))
						{
							Item I2=((PackagedItems)I).peekFirstItem();
							while(isCompost(I2))
							{
								final int amt=deCompost(I2,doubleRemain);
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
							final int amt=deCompost(I,doubleRemain);
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
					room.showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 @x3@x2 have grown here.",""+amount,s,foundShortName));
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
			final Gardening F=((Gardening)copyOf());
			F.unInvoked=false;
			F.tickUp=0;
			F.tickDown=50;
			F.startTickDown(invoker,room,50);
		}
	}

	public boolean isPotentialCrop(final Room R, final int code)
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
		if(R.myResource()==RawMaterial.RESOURCE_DIRT)
			return true;
		return false;
	}

	private boolean plantable(final MOB mob, final Item I2)
	{
		if((I2!=null)
		&&(I2 instanceof RawMaterial)
		&&(CMLib.flags().canBeSeenBy(I2,mob))
		&&(I2.container()==null)
		&&((I2.material()==RawMaterial.RESOURCE_FLOWERS)
		  ||(I2.material()==RawMaterial.RESOURCE_HERBS)
		  ||(I2.material()==RawMaterial.RESOURCE_GARLIC)))
			return true;
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
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
		if((!auto)&&(!R.getArea().getClimateObj().canSeeTheSun(R)))
		{
			commonTell(mob,L("You need clear sunlight to do your gardening.  Check the time and weather."));
			return false;
		}

		if((!auto)
		&&(R.domainType()!=Room.DOMAIN_OUTDOORS_HILLS)
		&&(R.domainType()!=Room.DOMAIN_OUTDOORS_PLAINS)
		&&(R.domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&(R.domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE)
		&&(R.domainType()!=Room.DOMAIN_OUTDOORS_SWAMP)
		&&(R.myResource()!=RawMaterial.RESOURCE_DIRT))
		{
			commonTell(mob,L("The land is not suitable for gardening here."));
			return false;
		}
		if((!auto)&&(R.getArea().getClimateObj().weatherType(R)==Climate.WEATHER_DROUGHT))
		{
			commonTell(mob,L("The current drought conditions make gardening useless."));
			return false;
		}
		if(R.fetchEffect(ID())!=null)
		{
			commonTell(mob,L("It looks like a garden is already growing here."));
			return false;
		}
		if(mob.isMonster()
		&&(!auto)
		&&(!CMLib.flags().isAnimalIntelligence(mob))
		&&(commands.size()==0))
		{
			Item mine=null;
			for(int i=0;i<R.numItems();i++)
			{
				final Item I2=R.getItem(i);
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
						if(R.findItem(null,R.getContextName(I2))==null)
							R.addItem(mine,ItemPossessor.Expire.Resource);
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
			&&((cd==RawMaterial.RESOURCE_FLOWERS)
			  ||(cd==RawMaterial.RESOURCE_HERBS)
			  ||(cd==RawMaterial.RESOURCE_GARLIC)))
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
				&&((cd==RawMaterial.RESOURCE_FLOWERS)
				  ||(cd==RawMaterial.RESOURCE_HERBS))
				  ||(cd==RawMaterial.RESOURCE_GARLIC))
				{
					code=cd;
					foundShortName=CMStrings.capitalizeAndLower(str);
					break;
				}
			}
		}
		Item mine=null;
		if(code<0)
		{
			final PhysicalAgent P=R.fetchFromRoomFavorItems(null, what);
			if((P instanceof Item)
			&&(plantable(mob,(Item)P)))
			{
				mine=(Item)P;
				code=mine.material();
				foundShortName=mine.Name();
			}
			else
				mine=null;
		}
		if(code<0)
		{
			commonTell(mob,L("You've never heard of an herb or flower called '@x1'.",CMParms.combine(commands,0)));
			return false;
		}

		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
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
			commonTell(mob,L("'@x1' is not suitable for use as seed.",mineName));
			return false;
		}
		if(!(isPotentialCrop(R,code)))
		{
			commonTell(mob,L("'@x1' does not seem to be taking root here.",mineName));
			return false;
		}

		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((proficiencyCheck(mob,0,auto))
		&&(isPotentialCrop(R,code)))
		{
			final String subType = (mine instanceof RawMaterial)?((RawMaterial)mine).getSubType():"";
			String makeSubType=subType;
			if(subType.equalsIgnoreCase(RawMaterial.ResourceSubType.SEED.name()))
				makeSubType="";
			found=(Item)CMLib.materials().makeResource(code,Integer.toString(R.domainType()),false,null, makeSubType);
			if((found!=null)
			&&(mine.material()==found.material()))
			{
				if(!subType.equalsIgnoreCase(RawMaterial.ResourceSubType.SEED.name()))
				{
					found.setName(mine.name());
					found.setDisplayText(mine.displayText());
					found.setDescription(mine.description());
				}
				found.text();
			}
		}

		mine.destroy();
		final int duration=getDuration(mob,1);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) planting @x1.",foundShortName));
		verb=L("planting @x1",foundShortName);
		displayText=L("You are planting @x1",foundShortName);
		room=R;
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
