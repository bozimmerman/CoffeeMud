package com.planet_ink.coffee_mud.Abilities.Skills;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2024 Bo Zimmerman

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
public class Skill_TradeCharting extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_TradeCharting";
	}

	private final static String	localizedName	= CMLib.lang().L("Trade Charting");

	@Override
	public String name()
	{
		return localizedName;
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

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "TRADECHARTING", "TRADECHART" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_TRAVEL;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if(commands.size()==0)
		{
			mob.tell(L("You did not specify whether you wanted to ADD your current location, LIST existing locations, DISTANCE [x] to a chart point, or REMOVE [X} an old chart point.  Try adding ADD, REMOVE X, or LIST."));
			return false;
		}
		final String cmd=commands.get(0).toString().toUpperCase().trim();
		if(!cmd.equals("LIST"))
		{
			if(R.getArea() instanceof Boardable)
			{
			}
			else
			if((mob.riding() !=null)
			&&((mob.riding().rideBasis() == Rideable.Basis.LAND_BASED)
				||(mob.riding().rideBasis() == Rideable.Basis.WAGON)))
			{
			}
			else
			{
				mob.tell(L("This skill only works on board a caravan."));
				return false;
			}
		}

		if((!cmd.equals("LIST"))
		&&(!cmd.equals("REMOVE"))
		&&(!cmd.equals("DISTANCE"))
		&&(!cmd.equals("ADD")))
		{
			mob.tell(L("'@x1' is not a valid argument.  Try ADD, LIST, DISTANCE X, or REMOVE X (where X is a number).",cmd));
			return false;
		}

		final List<String> rooms=CMParms.parseAny(text(),';',true);
		int chartPointIndex=-1;
		if(cmd.equals("REMOVE"))
		{
			if(rooms.size()==0)
			{
				mob.tell(L("There are no chart points to remove.  Try LIST."));
				return false;
			}
			if(commands.size()<2)
			{
				mob.tell(L("You must specify which chart point to remove.  Try LIST."));
				return false;
			}
			if(!CMath.isInteger(commands.get(1)))
			{
				mob.tell(L("'@x1' is not a valid chart point number to remove.   Try LIST.",commands.get(1)));
				return false;
			}
			chartPointIndex=CMath.s_int(commands.get(1));
			if((chartPointIndex<1)||(chartPointIndex>rooms.size()))
			{
				mob.tell(L("'@x1' is not a valid chart point number to remove.   Try LIST.",commands.get(1)));
				return false;
			}
			chartPointIndex--;
		}

		Room currentR=null;
		if(R.getArea() instanceof Boardable)
		{
			currentR=CMLib.map().roomLocation(((Boardable)R.getArea()).getBoardableItem());
		}
		else
		if((mob.riding() !=null)
		&&((mob.riding().rideBasis() == Rideable.Basis.LAND_BASED)
			||(mob.riding().rideBasis() == Rideable.Basis.WAGON)))
		{
			if(CMLib.flags().isDrivableRoom(mob.location()))
				currentR=mob.location();
		}

		if(cmd.equals("DISTANCE"))
		{
			if(rooms.size()==0)
			{
				mob.tell(L("There are no chart points yet.  Try LIST."));
				return false;
			}
			if(currentR==null)
			{
				mob.tell(L("You can't seem to figure out how to get there from here."));
				return false;
			}
			if(commands.size()<2)
			{
				mob.tell(L("You must specify which chart point to get the distance to.  Try LIST."));
				return false;
			}
			if(!CMath.isInteger(commands.get(1)))
			{
				mob.tell(L("'@x1' is not a valid chart point number to get the distance to.   Try LIST.",commands.get(1)));
				return false;
			}
			chartPointIndex=CMath.s_int(commands.get(1));
			if((chartPointIndex<1)||(chartPointIndex>rooms.size()))
			{
				mob.tell(L("'@x1' is not a valid chart point number to get the distance to.   Try LIST.",commands.get(1)));
				return false;
			}
			chartPointIndex--;
		}

		if(cmd.equalsIgnoreCase("LIST"))
		{
			if(rooms.size()==0)
				mob.tell(L("You have not added any chart points."));
			else
				mob.tell(L("^HYour charted points:^?"));
			for(int i=0;i<rooms.size();i++)
			{
				final Room room=CMLib.map().getRoom(rooms.get(i));
				if(room == null)
				{
					rooms.remove(i);
					i--;
				}
				else
				{
					mob.tell((i+1)+") "+CMStrings.padRight(CMStrings.ellipse(room.displayText(mob),40),40)+" ^H("+rooms.get(i)+")^N");
				}
			}
			return true;
		}

		final String addStr;
		if(cmd.equalsIgnoreCase("ADD"))
		{
			if(currentR!=null)
			{
				if(!CMLib.flags().isDrivableRoom(currentR))
				{
					mob.tell(L("This place cannot be charted."));
					return false;
				}
				addStr=CMLib.map().getExtendedRoomID(currentR);
			}
			else
			{
				mob.tell(L("This place cannot be charted."));
				return false;
			}

			if(addStr.length()==0)
			{
				mob.tell(L("This place cannot be charted."));
				return false;
			}
			if(rooms.contains(addStr))
			{
				mob.tell(L("You have already charted this room, check #@x1",""+(rooms.indexOf(addStr)+1)));
				return false;
			}
		}
		else
			addStr="";

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final String str;
			if(cmd.equalsIgnoreCase("ADD"))
				str=L("<S-NAME> make(s) a mark on <S-HIS-HER> trade chart for this location.");
			else
			if(cmd.equalsIgnoreCase("REMOVE"))
				str=L("<S-NAME> erase(s) a mark on <S-HIS-HER> trade chart.!");
			else
			if(cmd.equalsIgnoreCase("DISTANCE"))
				str=L("<S-NAME> calculate(s) a distance between points on <S-HIS-HER> trade chart.!");
			else
				str="?!?!?!";
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_DELICATE_HANDS_ACT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				if(cmd.equalsIgnoreCase("ADD"))
				{
					if(text().length()==0)
						setMiscText(addStr);
					else
						setMiscText(text()+";"+addStr);
				}
				else
				if(cmd.equalsIgnoreCase("REMOVE"))
				{
					rooms.remove(chartPointIndex);
					setMiscText(CMParms.combineWith(rooms,';'));
				}
				else
				if(cmd.equalsIgnoreCase("DISTANCE"))
				{
					final String roomID = rooms.get(chartPointIndex);
					final Room room=CMLib.map().getRoom(roomID);
					if((room == null)||(currentR==null))
					{
						mob.tell(L("You can't get there from here."));
						return false;
					}
					if(currentR == room)
					{
						mob.tell(L("You are already there."));
						return false;
					}

					final List<Room> destRooms=new XVector<Room>(room);
					final TrackingFlags flags=CMLib.tracking().newFlags()
												.plus(TrackingFlag.PASSABLE)
												.plus(TrackingFlag.DRIVEABLEONLY);
					final List<Room> trail=CMLib.tracking().findTrailToAnyRoom(currentR, destRooms, flags, 100);
					if((trail==null)
					||(trail.size()==0)
					||(trail.get(trail.size()-1)!=currentR)
					||(trail.get(0)!=room))
					{
						mob.tell(L("You really can't get there from here."));
						return false;
					}
					mob.tell(L("The distance from your current location to that chart point is @x1.",""+trail.size()));
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> can't seem to figure out where <S-HE-SHE> <S-IS-ARE> on <S-HIS-HER> trade charts."));

		return success;
	}

}
