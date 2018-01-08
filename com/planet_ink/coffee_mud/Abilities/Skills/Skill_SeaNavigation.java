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

public class Skill_SeaNavigation extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_SeaNavigation";
	}

	private final static String	localizedName	= CMLib.lang().L("Sea Navigation");

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

	private static final String[]	triggerStrings	= I(new String[] { "SEANAVIGATION","SEANAVIGATE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_SEATRAVEL;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	@Override
	public boolean invoke(final MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		Room currentR=null;
		if(R.getArea() instanceof BoardableShip)
		{
			currentR=CMLib.map().roomLocation(((BoardableShip)R.getArea()).getShipItem());
		}
		else
		if((mob.riding() !=null) && (mob.riding().rideBasis() == Rideable.RIDEABLE_WATER))
		{
			if(CMLib.flags().isWaterySurfaceRoom(mob.location()))
				currentR=mob.location();
		}
		else
		{
			mob.tell(L("This skill only works on board a ship or boat."));
			return false;
		}

		boolean fullNav = text().trim().toUpperCase().equals("FULL");
		Ability seaChartA=mob.fetchAbility("Skill_SeaCharting");
		if(commands.size()==0)
		{
			if(fullNav)
			{
				if(seaChartA==null)
					mob.tell(L("You must specify either the number of a Sea Charted point, or the name of an area on the sea to navigate to."));
				else
					mob.tell(L("You must specify the name of an area on the sea to navigate to."));
			}
			else
			if(seaChartA==null)
				mob.tell(L("You must learn Sea Charting to use this skill."));
			else
				mob.tell(L("You must specify the number of a Sea Charted point.  Use SEACHART LIST for a list of valid numbers."));
			return false;
		}
		
		List<String> rooms;
		if(seaChartA!=null)
			rooms=CMParms.parseAny(seaChartA.text(),';',true);
		else
			rooms=new Vector<String>(1);

		final String parm=CMParms.combine(commands).trim();
		
		if(currentR==null)
		{
			mob.tell(L("You can't seem to figure out how to get there from here."));
			return false;
		}
		
		Room targetR = null;
		List<Room> trail = null;
		if(CMath.isInteger(parm))
		{
			if(seaChartA==null)
			{
				mob.tell(L("You must learn Sea Charting to use this skill this way."));
				return false;
			}
			if(rooms.size()==0)
			{
				mob.tell(L("There are no chart points yet.  Try SEACHART LIST."));
				return false;
			}
			int chartPointIndex=CMath.s_int(parm);
			if((chartPointIndex<1)||(chartPointIndex>rooms.size()))
			{
				mob.tell(L("'@x1' is not a valid chart point number to get the distance to.   Try LIST.",""+chartPointIndex));
				return false;
			}
			chartPointIndex--;
			TrackingFlags flags=CMLib.tracking().newFlags().plus(TrackingFlag.NOAIR)
															.plus(TrackingFlag.WATERSURFACEORSHOREONLY);
			targetR=CMLib.map().getRoom(rooms.get(chartPointIndex));
			if(targetR!=null)
				trail = CMLib.tracking().findTrailToRoom(currentR, targetR, flags, 100);
		}
		else
		if(fullNav)
		{
			TrackingFlags flags=CMLib.tracking().newFlags().plus(TrackingFlag.NOAIR)
					.plus(TrackingFlag.NOHOMES);
			TrackingLibrary.RFilter destFilter = new TrackingLibrary.RFilter()
			{
				@Override
				public boolean isFilteredOut(Room hostR, Room R, Exit E, int dir)
				{
					if (R == null)
						return false;
					switch (R.domainType())
					{
					case Room.DOMAIN_INDOORS_UNDERWATER:
					case Room.DOMAIN_INDOORS_WATERSURFACE:
					case Room.DOMAIN_OUTDOORS_UNDERWATER:
					case Room.DOMAIN_OUTDOORS_WATERSURFACE:
						return true;
					}
					if(CMLib.english().containsString(R.displayText(mob), parm))
						return false;
					final Area A=R.getArea();
					if((A!=null)&&(CMLib.english().containsString(A.Name(), parm)))
						return false;
					return true;
				}
			};
			trail = CMLib.tracking().findTrailToAnyRoom(currentR, destFilter, flags, 100);
			if((trail!=null)&&(trail.size()>0))
				targetR=trail.get(0);
		}
		else
		{
			if(seaChartA==null)
				mob.tell(L("You must learn Sea Charting to use this skill this way."));
			else
				mob.tell(L("'@x1' is not a valid chart point number to get the distance to. Try SEACHART LIST.",parm));
			return false;
		}
		
		if((targetR==null)||(trail==null)||(trail.size()==0))
		{
			mob.tell(L("You don't know how to get there from here."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final String str=L("<S-NAME> consult(s) <S-HIS-HER> sea charts.");
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_DELICATE_HANDS_ACT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				StringBuilder dirs=new StringBuilder("");
				StringBuilder courseStr=new StringBuilder("");
				Room room=trail.get(trail.size()-1);
				for(int i=trail.size()-2;i>=0;i--)
				{
					Room nextRoom=trail.get(i);
					int dir=CMLib.map().getRoomDir(room, nextRoom);
					if(dir >= 0)
					{
						dirs.append(CMLib.directions().getDirectionName(dir));
						courseStr.append(CMLib.directions().getDirectionName(dir));
						if(i>0)
						{
							dirs.append(", ");
							courseStr.append(" ");
						}
					}
					room=nextRoom;
				}
				final String msgStr=L("Your charts say the way there is: @x1",dirs.toString());
				if(R.getArea() instanceof BoardableShip)
				{
					String courseMsgStr="COURSE "+courseStr.toString();
					final CMMsg huhMsg=CMClass.getMsg(mob,null,null,CMMsg.MSG_HUH,msgStr,courseMsgStr,null);
					if(R.okMessage(mob,huhMsg))
						R.send(mob,huhMsg);
				}
				else
				if((mob.riding() !=null) && (mob.riding().rideBasis() == Rideable.RIDEABLE_WATER))
				{
					mob.tell(msgStr);
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> can't seem to figure out where <S-HE-SHE> <S-IS-ARE>."));

		return success;
	}

}
