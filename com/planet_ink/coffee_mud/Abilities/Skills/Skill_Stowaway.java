package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Thief.Thief_Articles;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.StdBehavior;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.RFilter;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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
public class Skill_Stowaway extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Stowaway";
	}

	private final static String	localizedName	= CMLib.lang().L("Stowaway");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "STOWAWAY"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_DECEPTIVE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA|USAGE_MOVEMENT;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Stowaway)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	protected Room destR = null;
	protected Room boxR = null;
	protected int abilityCode = 0;
	protected int tickUp=0;

	@Override
	public int abilityCode()
	{
		return abilityCode;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		this.abilityCode = newCode;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&((msg.sourceMinor()==CMMsg.TYP_DEATH)
			||(msg.sourceMinor()==CMMsg.TYP_QUIT)
			||(msg.sourceMinor()==CMMsg.TYP_RECALL)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(destR==null)
			||(msg.source().location()!=boxR)))
		{
			destR=null;
			unInvoke();
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void unInvoke()
	{
		final Physical affected=this.affected;
		final Room R=this.destR;
		final Room boxR=this.boxR;
		super.unInvoke();
		if((affected instanceof MOB)&&(R!=null))
		{
			final MOB mob=(MOB)affected;
			if(mob.location()==boxR)
			{
				mob.tell(L("You feel yourself being unloaded from the ship."));
				final CMMsg leaveMsg=CMClass.getMsg(mob,R,this,CMMsg.MSG_LEAVE|CMMsg.MASK_ALWAYS,L("<S-NAME> slip(s) out of a cargo box just unloaded from one of the ships."));
				if(R.okMessage(mob,leaveMsg))
					R.send(mob,leaveMsg);
				if(!R.isInhabitant(mob))
					R.bringMobHere(mob, false);
				CMLib.commands().postStand(mob, true);
			}
		}
		if(boxR!=null)
		{
			if(R==null)
				CMLib.map().emptyRoom(boxR, CMLib.map().getStartRoom(affected), true);
			else
				CMLib.map().emptyRoom(boxR, R, true);
			boxR.destroy();
		}
		this.destR=null;
		this.boxR=null;
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		
		if((affected instanceof MOB)
		&&(((MOB)affected).location()==boxR)
		&&(destR!=null))
		{
			final MOB mob=(MOB)affected;
			if(++tickUp==2)
			{
				mob.tell(L("^xYou feel yourself being picked up and loaded onto a ship.^.^?"));
			}
			else
			if(tickUp>2)
			{
				switch(CMLib.dice().roll(1,50,0))
				{
				case 1:
					mob.tell(L("You feel the ship rocking gently in the waves."));
					break;
				case 2:
					mob.tell(L("You hear rats crawling around on your crate."));
					break;
				case 3:
					mob.tell(L("You hear the sounds of some crewmen talking outside."));
					break;
				case 4:
				case 5:
					mob.tell(L("You are extremely bored."));
					break;
				case 6:
				case 7:
					mob.tell(L("It is dark and cramped in here."));
					break;
				}
			}
		}
		else
		{
			destR=null;
			unInvoke();
		}
		return true;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;

		final RFilter SHORE_ONLY=TrackingFlag.SHOREONLY.myFilter;
		if(SHORE_ONLY.isFilteredOut(R, R, null, 0))
		{
			mob.tell(L("You must be on a shore to stow-away."));
			return false;
		}
		
		final Ability seaChartA=mob.fetchAbility("Skill_SeaCharting");
		Room destR=null;
		if(commands.size()>0)
		{
			if(seaChartA!=null)
			{
				List<String> chartRooms=CMParms.parseAny(seaChartA.text(),';',true);
				String name=CMParms.combine(commands,0);
				if(CMath.isInteger(name))
				{
					int x=CMath.s_int(name);
					if((x<1)||(x>chartRooms.size()))
					{
						mob.tell(L("@x1 is not a valid number.  Check your sea charts!"));
						return false;
					}
					destR=CMLib.map().getRoom(chartRooms.get(x-1));
				}
				else
				{
					for(String roomID : chartRooms)
					{
						final Room R2=CMLib.map().getRoom(roomID);
						if((R2!=null)&&(CMLib.english().containsString(R2.displayText(mob), name)))
						{
							destR=R2;
							break;
						}
					}
					if(destR==null)
					{
						for(String roomID : chartRooms)
						{
							final Room R2=CMLib.map().getRoom(roomID);
							if((R2!=null)&&(CMLib.english().containsString(R2.description(mob), name)))
							{
								destR=R2;
								break;
							}
						}
					}
					if(destR==null)
					{
						mob.tell(L("You have not charted a room called '@x1'.",name));
						return false;
					}
				}
			}
			else
			{
				mob.tell(L("You cannot specify a destination unless you have Sea Charting."));
				return false;
			}
		}
		
		TrackingFlags flags=CMLib.tracking().newFlags().plus(TrackingFlag.WATERSURFACEORSHOREONLY);
		int radius=50 + (10*(super.getXLEVELLevel(mob)+super.getXMAXRANGELevel(mob)));
		List<Room> rooms=CMLib.tracking().getRadiantRooms(R, flags, radius);
		boolean success=proficiencyCheck(mob,0,auto);
		if((destR==null)||(!rooms.contains(destR))||(!success))
		{
			for(Iterator<Room> i=rooms.iterator();i.hasNext();)
			{
				final Room R2=i.next();
				if((R2 == R)
				||(CMLib.map().getRoomDir(R, R2)>=0)
				||(SHORE_ONLY.isFilteredOut(R2, R2, null, 0)))
					i.remove();
			}
			if(rooms.size()==0)
			{
				mob.tell(L("There isn't enough shipping traffic here."));
				return false;
			}
			destR=rooms.get(CMLib.dice().roll(1, rooms.size(), -1));
		}
		
		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;
		
		final CMMsg leaveMsg=CMClass.getMsg(mob,R,this,CMMsg.MSG_LEAVE|(auto?CMMsg.MASK_ALWAYS:0),auto?L("<S-NAME> stow(s) away!"):L("<S-NAME> slip(s) into a cargo box!"));
		if(R.okMessage(mob,leaveMsg))
		{
			R.send(mob,leaveMsg);
			Room boxR=CMClass.getLocale("WoodRoom");
			boxR.setDisplayText(L("You are squeezed into a dark cramped shipping box."));
			boxR.addNonUninvokableEffect(CMClass.getAbility("Prop_Crawlspace"));
			boxR.addNonUninvokableEffect(CMClass.getAbility("Prop_RoomDark"));
			final Ability consA=CMClass.getAbility("Prop_ReqCapacity");
			consA.setMiscText("people=1");
			boxR.addNonUninvokableEffect(consA);
			boxR.bringMobHere(mob, false);
			CMLib.commands().forceStandardCommand(mob, "Sit",new XVector<String>(""));
			Skill_Stowaway stow=(Skill_Stowaway)super.beneficialAffect(mob, mob, asLevel, (int)((4 * 60 * 1000) / CMProps.getTickMillis())  / (1+super.getXTIMELevel(mob)));
			stow.destR=destR;
			stow.boxR=boxR;
			stow.tickUp=0;
		}
		return true;
	}
}
