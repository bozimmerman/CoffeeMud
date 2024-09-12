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
   Copyright 2024-2024 Bo Zimmerman

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
public class Skill_CaravanTravel extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_CaravanTravel";
	}

	private final static String	localizedName	= CMLib.lang().L("Caravan Travel");

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

	private static final String[]	triggerStrings	= I(new String[] { "CARAVANTRAVEL","CNAVIGATE" });

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
		else
		{
			mob.tell(L("This skill only works on board a caravan."));
			return false;
		}

		final boolean fullNav = text().trim().toUpperCase().equals("FULL");
		final Ability tradeChartA=mob.fetchAbility("Skill_TradeCharting");
		if(commands.size()==0)
		{
			if(fullNav)
			{
				if(tradeChartA==null)
					mob.tell(L("You must specify either the number of a Trade Charted point, or the name of an area on the roads to navigate to."));
				else
					mob.tell(L("You must specify the name of an area on the roads to navigate to."));
			}
			else
			if(tradeChartA==null)
				mob.tell(L("You must learn Trade Charting to use this skill."));
			else
				mob.tell(L("You must specify the number of a Trade Charted point.  Use TRADECHART LIST for a list of valid numbers."));
			return false;
		}

		List<String> rooms;
		if(tradeChartA!=null)
			rooms=CMParms.parseAny(tradeChartA.text(),';',true);
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
			if(tradeChartA==null)
			{
				mob.tell(L("You must learn Trade Charting to use this skill this way."));
				return false;
			}
			if(rooms.size()==0)
			{
				mob.tell(L("There are no chart points yet.  Try TRADECHART LIST."));
				return false;
			}
			int chartPointIndex=CMath.s_int(parm);
			if((chartPointIndex<1)||(chartPointIndex>rooms.size()))
			{
				mob.tell(L("'@x1' is not a valid chart point number to get the distance to.   Try LIST.",""+chartPointIndex));
				return false;
			}
			chartPointIndex--;
			final TrackingFlags flags=CMLib.tracking().newFlags().plus(TrackingFlag.NOAIR)
															.plus(TrackingFlag.PASSABLE)
															.plus(TrackingFlag.DRIVEABLEONLY);
			targetR=CMLib.map().getRoom(rooms.get(chartPointIndex));
			if(targetR!=null)
				trail = CMLib.tracking().findTrailToRoom(currentR, targetR, flags, 100);
		}
		else
		if(fullNav)
		{
			final TrackingFlags flags=CMLib.tracking().newFlags()
					.plus(TrackingFlag.NOAIR)
					.plus(TrackingFlag.PASSABLE)
					.plus(TrackingFlag.NOHOMES);
			final TrackingLibrary.RFilter destFilter = new TrackingLibrary.RFilter()
			{
				@Override
				public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
				{
					if (R == null)
						return false;
					if(CMLib.flags().isDrivableRoom(R))
						return true;
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
			if(tradeChartA==null)
				mob.tell(L("You must learn Trade Charting to use this skill this way."));
			else
				mob.tell(L("'@x1' is not a valid chart point number to get the distance to. Try TRADECHART LIST.",parm));
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
			final String str=L("<S-NAME> consult(s) <S-HIS-HER> trade charts.");
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_DELICATE_HANDS_ACT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final StringBuilder dirs=new StringBuilder("");
				final StringBuilder courseStr=new StringBuilder("");
				Room room=trail.get(trail.size()-1);
				for(int i=trail.size()-2;i>=0;i--)
				{
					final Room nextRoom=trail.get(i);
					final int dir=CMLib.map().getRoomDir(room, nextRoom);
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
				if(R.getArea() instanceof Boardable)
				{
					final String courseMsgStr="COURSE "+courseStr.toString();
					final CMMsg huhMsg=CMClass.getMsg(mob,null,null,CMMsg.MSG_HUH,msgStr,courseMsgStr,null);
					if(R.okMessage(mob,huhMsg))
						R.send(mob,huhMsg);
				}
				else
				if((mob.riding() !=null)
				&&((mob.riding().rideBasis() == Rideable.Basis.LAND_BASED)
					||(mob.riding().rideBasis() == Rideable.Basis.WAGON)))
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
