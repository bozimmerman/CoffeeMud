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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class Skill_RegionalAwareness extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_RegionalAwareness";
	}

	private final static String	localizedName	= CMLib.lang().L("Regional Awareness");

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

	private static final String[]	triggerStrings	= I(new String[] { "REGION", "REGIONALAWARENESS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_NATURELORE;
	}

	@Override
	public int overrideMana()
	{
		return 0;
	}

	public char roomColor(Room room)
	{
		if(room==null)
			return ' ';
		if(CMath.bset(room.phyStats().sensesMask(),PhyStats.SENSE_ROOMUNMAPPABLE))
			return 'w';
		switch(room.domainType())
		{
		case Room.DOMAIN_OUTDOORS_CITY:
			return 'w';
		case Room.DOMAIN_OUTDOORS_WOODS:
			return 'G';
		case Room.DOMAIN_OUTDOORS_ROCKS:
			return 'W';
		case Room.DOMAIN_OUTDOORS_PLAINS:
			return 'Y';
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
			return 'B';
		case Room.DOMAIN_OUTDOORS_AIR:
			return ' ';
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			return 'b';
		case Room.DOMAIN_OUTDOORS_JUNGLE:
			return 'R';
		case Room.DOMAIN_OUTDOORS_SEAPORT:
			return 'y';
		case Room.DOMAIN_OUTDOORS_SWAMP:
			return 'r';
		case Room.DOMAIN_OUTDOORS_DESERT:
			return 'y';
		case Room.DOMAIN_OUTDOORS_HILLS:
			return 'g';
		case Room.DOMAIN_OUTDOORS_MOUNTAINS:
			return 'p';
		case Room.DOMAIN_OUTDOORS_SPACEPORT:
			return 'P';
		case Room.DOMAIN_INDOORS_STONE:
			return 'W';
		case Room.DOMAIN_INDOORS_WOOD:
			return 'y';
		case Room.DOMAIN_INDOORS_CAVE:
			return 'w';
		case Room.DOMAIN_INDOORS_MAGIC:
			return 'r';
		case Room.DOMAIN_INDOORS_UNDERWATER:
			return 'B';
		case Room.DOMAIN_INDOORS_AIR:
			return ' ';
		case Room.DOMAIN_INDOORS_WATERSURFACE:
			return 'b';
		case Room.DOMAIN_INDOORS_METAL:
			return 'P';
		default:
			return 'k';
		}
	}
	
	public char roomChar(Room room, boolean amOutdoors)
	{
		if(room==null)
			return ' ';
		if(CMath.bset(room.phyStats().sensesMask(),PhyStats.SENSE_ROOMUNMAPPABLE))
			return ' ';
		switch(room.domainType())
		{
		case Room.DOMAIN_OUTDOORS_CITY:
			return '=';
		case Room.DOMAIN_OUTDOORS_WOODS:
			return 'T';
		case Room.DOMAIN_OUTDOORS_ROCKS:
			return ':';
		case Room.DOMAIN_OUTDOORS_PLAINS:
			return '_';
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
			return '~';
		case Room.DOMAIN_OUTDOORS_AIR:
			return ' ';
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			return '~';
		case Room.DOMAIN_OUTDOORS_JUNGLE:
			return 'J';
		case Room.DOMAIN_OUTDOORS_SEAPORT:
			return 'P';
		case Room.DOMAIN_OUTDOORS_SWAMP:
			return 'x';
		case Room.DOMAIN_OUTDOORS_DESERT:
			return '.';
		case Room.DOMAIN_OUTDOORS_HILLS:
			return 'h';
		case Room.DOMAIN_OUTDOORS_MOUNTAINS:
			return 'M';
		case Room.DOMAIN_OUTDOORS_SPACEPORT:
			return '@';
		case Room.DOMAIN_INDOORS_UNDERWATER:
			return '~';
		case Room.DOMAIN_INDOORS_AIR:
			return ' ';
		case Room.DOMAIN_INDOORS_WATERSURFACE:
			return '~';
		case Room.DOMAIN_INDOORS_STONE:
		case Room.DOMAIN_INDOORS_WOOD:
		case Room.DOMAIN_INDOORS_CAVE:
		case Room.DOMAIN_INDOORS_MAGIC:
		case Room.DOMAIN_INDOORS_METAL:
			return '#';
		default:
			return '?';
		}
	}

	public String[] getMiniMap(Room room, final int diameter, boolean openOnly)
	{
		final char[][] map=new char[diameter][diameter];
		for(int i=0;i<diameter;i++)
		{
			for(int i2=0;i2<diameter;i2++)
				map[i][i2]=' ';
		}
		final boolean amIndoors=((room.domainType()&Room.INDOORS)==Room.INDOORS);
		final Room[][] rmap=new Room[diameter][diameter];
		final Vector<Room> rooms=new Vector<Room>();
		final HashSet<Room> closedPaths=new HashSet<Room>();
		TrackingLibrary.TrackingFlags flags;
		flags = CMLib.tracking().newFlags()
					.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
					.plus(TrackingLibrary.TrackingFlag.NOAIR);
		if(openOnly)
			flags = flags.plus(TrackingLibrary.TrackingFlag.OPENONLY);

		CMLib.tracking().getRadiantRooms(room,rooms,flags,null,diameter,null);
		rmap[diameter/2][diameter/2]=room;
		map[diameter/2][diameter/2]='*';
		for(int i=0;i<rooms.size();i++)
		{
			final Room R=rooms.elementAt(i);
			if((closedPaths.contains(R))
			||(R==room))
				continue;
			Room parentR=null;
			int parentDir=-1;
			int[] xy=null;
			for(int i2=0;(i2<diameter)&&(parentR==null);i2++)
			{
				for(int i3=0;(i3<diameter)&&(parentR==null);i3++)
				{
					final Room R2=rmap[i2][i3];
					if(R2!=null)
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						if((R2.getRoomInDir(d)==R)
						&&(!closedPaths.contains(R2))
						&&(R2.getExitInDir(d)!=null))
						{
							parentR=R2;
							parentDir=d;
							xy=Directions.adjustXYByDirections(i3,i2,d);
							break;
						}
					}
				}
			}
			if(xy!=null)
			{
				if((parentDir<0)
				||(xy[0]<0)||(xy[0]>=diameter)||(xy[1]<0)||(xy[1]>=diameter)
				||(map[xy[1]][xy[0]]!=' '))
					closedPaths.add(R);
				else
				{
					map[xy[1]][xy[0]]=roomChar(R,!amIndoors);
					rmap[xy[1]][xy[0]]=R;

					if((R.domainType()&Room.INDOORS)==Room.INDOORS)
						closedPaths.add(R);
				}
			}
		}
		final String[] miniMap=new String[diameter];
		final StringBuffer str=new StringBuffer("");
		char r=' ';
		char c=' ';
		for(int i2=0;i2<diameter;i2++)
		{
			str.setLength(0);
			for(int i3=0;i3<diameter;i3++)
			{
				r=map[i2][i3];
				c=roomColor(rmap[i2][i3]);
				if(c!=' ')
					str.append("^"+c+""+r);
				else
					str.append(r);
			}
			miniMap[i2]=str.toString();
		}
		return miniMap;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Session sess = mob.session();
		if(auto && (givenTarget instanceof Room) && (asLevel>0))
		{
			final String[] miniMap=getMiniMap((Room)givenTarget, asLevel, false);
			if(commands!=null)
			{
				for(final String s : miniMap)
					commands.add(s);
			}
			else
			if(sess != null)
			{
				for(final String s : miniMap)
				{
					sess.setIdleTimers();
					sess.colorOnlyPrintln(s);
				}
			}
			return true;
		}

		if((!auto)&&((mob.location().domainType()&Room.INDOORS)==Room.INDOORS))
		{
			mob.tell(L("This only works outdoors."));
			return false;
		}

		if((!auto)
		&&(!CMLib.flags().canBeSeenBy(mob.location(),mob)))
		{
			mob.tell(L("You need to be able to see your surroundings to do that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_LOOK,auto?"":L("<S-NAME> peer(s) at the horizon with a distant expression."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final String[] miniMap=getMiniMap(mob.location(), 2+(adjustedLevel(mob,asLevel)/10), true);
				if(sess != null)
				{
					for(final String s : miniMap)
					{
						sess.setIdleTimers(); // prevents spam block
						sess.colorOnlyPrintln(s+"\n\r");
					}
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> peer(s) around distantly, looking frustrated."));
		return success;
	}
}
