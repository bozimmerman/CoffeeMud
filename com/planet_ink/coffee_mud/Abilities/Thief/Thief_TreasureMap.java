package com.planet_ink.coffee_mud.Abilities.Thief;
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

public class Thief_TreasureMap extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_TreasureMap";
	}

	private final static String	localizedName	= CMLib.lang().L("Draw Treasure Map");

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
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "DRAWTREASUREMAP","TREASUREMAP" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_CALLIGRAPHY;
	}

	@Override
	public int overrideMana()
	{
		return 0;
	}
	
	protected void prefixAll(List<StringBuilder> theMap,int num)
	{
		for(StringBuilder str : theMap)
			str.insert(0,CMStrings.SPACES.subSequence(0, num));
	}

	protected void postfixAll(List<StringBuilder> theMap,int num)
	{
		for(StringBuilder str : theMap)
			str.append(CMStrings.SPACES.subSequence(0, num));
	}

	protected void insertLink(int[] coord, int lastWidth, int dir, String nextName, int trailCt, List<StringBuilder> theMap)
	{
		if(dir == Directions.UP)
			dir=Directions.NORTH;
		else
		if(dir == Directions.DOWN)
			dir=Directions.SOUTH;
		nextName="["+nextName+"]";
		int width = theMap.get(0).length();
		switch(dir)
		{
		case Directions.NORTH:
		{
			coord[1]--;
			if(coord[0]<0)
			{
				theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '|');
				coord[1]--;
			}
			else
			while(coord[1]>=0)
			{
				theMap.get(coord[1]).setCharAt(coord[0], '|');
				coord[1]--;
			}
			coord[1]=0;
			for(int i=0;i<(trailCt);i++)
			{
				theMap.add(0,new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '.');
			}
			theMap.add(0,new StringBuilder(CMStrings.repeat(' ',width)));
			theMap.get(coord[1]).setCharAt(coord[0], '^');
			theMap.add(0,new StringBuilder(CMStrings.repeat(' ',width)));
			int stDiff=coord[0]-(nextName.length()/2);
			if(stDiff<0)
				stDiff=0;
			for(int i=0;i<nextName.length();i++)
				theMap.get(coord[1]).setCharAt(stDiff+i, nextName.charAt(i));
			break;
		}
		case Directions.NORTHEAST:
		{
			coord[1]--;
			if(coord[0]<0)
			{
				theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '/');
				coord[1]--;
				coord[0]++;
				width++;
			}
			else
			while(coord[1]>=0)
			{
				theMap.get(coord[1]).setCharAt(coord[0], '/');
				coord[1]--;
				coord[0]++;
				if(coord[0]==theMap.get(coord[1]).length())
				{
					postfixAll(theMap,1);
					width++;
				}
			}
			coord[1]=0;
			for(int i=0;i<(trailCt);i++)
			{
				theMap.add(0,new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '.');
				coord[0]++;
				if(coord[0]==theMap.get(coord[1]).length())
				{
					postfixAll(theMap,1);
					width++;
				}
			}
			theMap.add(0,new StringBuilder(CMStrings.repeat(' ',width)));
			theMap.get(coord[1]).setCharAt(coord[0], '^');
			theMap.add(0,new StringBuilder(CMStrings.repeat(' ',width)));
			int stDiff=coord[0]-(nextName.length()/2);
			if(stDiff<0)
				stDiff=0;
			if(stDiff+nextName.length()>=theMap.get(coord[1]).length())
				postfixAll(theMap,((stDiff+nextName.length())-theMap.get(coord[1]).length()));
			for(int i=0;i<nextName.length();i++)
				theMap.get(coord[1]).setCharAt(stDiff+i, nextName.charAt(i));
			break;
		}
		case Directions.NORTHWEST:
		{
			coord[1]--;
			if(coord[0]<0)
			{
				theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '\\');
				coord[1]--;
			}
			else
			while(coord[1]>=0)
			{
				theMap.get(coord[1]).setCharAt(coord[0], '\\');
				coord[1]--;
				coord[0]--;
				if(coord[0]<0)
				{
					coord[0]=0;
					prefixAll(theMap,1);
					width++;
				}
			}
			coord[1]=0;
			for(int i=0;i<(trailCt);i++)
			{
				theMap.add(0,new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '.');
				coord[0]--;
				if(coord[0]<0)
				{
					coord[0]=0;
					prefixAll(theMap,1);
					width++;
				}
			}
			theMap.add(0,new StringBuilder(CMStrings.repeat(' ',width)));
			theMap.get(coord[1]).setCharAt(coord[0], '^');
			theMap.add(0,new StringBuilder(CMStrings.repeat(' ',width)));
			int stDiff=coord[0]-(nextName.length()/2);
			if(stDiff<0)
			{
				prefixAll(theMap,-stDiff);
				width+= -stDiff;
				stDiff=0;
			}
			for(int i=0;i<nextName.length();i++)
				theMap.get(coord[1]).setCharAt(stDiff+i, nextName.charAt(i));
			break;
		}
		case Directions.SOUTH:
		{
			coord[1]++;
			if(coord[1]>=theMap.size())
			{
				theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '|');
				coord[1]++;
			}
			else
			while(coord[1]<theMap.size())
			{
				theMap.get(coord[1]).setCharAt(coord[0], '|');
				coord[1]++;
			}
			for(int i=0;i<(trailCt);i++)
			{
				theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '.');
				coord[1]++;
			}
			theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
			theMap.get(coord[1]).setCharAt(coord[0], 'v');
			coord[1]++;
			theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
			int stDiff=coord[0]-(nextName.length()/2);
			if(stDiff<0)
				stDiff=0;
			for(int i=0;i<nextName.length();i++)
				theMap.get(coord[1]).setCharAt(stDiff+i, nextName.charAt(i));
			break;
		}
		case Directions.SOUTHWEST:
		{
			coord[1]++;
			if(coord[1]>=theMap.size())
			{
				theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '/');
				coord[1]++;
				coord[0]--;
			}
			else
			while(coord[1]<theMap.size())
			{
				theMap.get(coord[1]).setCharAt(coord[0], '/');
				coord[1]++;
				coord[0]--;
				if(coord[0]<0)
				{
					coord[0]=0;
					prefixAll(theMap,1);
					width++;
				}
			}
			for(int i=0;i<(trailCt);i++)
			{
				theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '.');
				coord[1]++;
				coord[0]--;
				if(coord[0]<0)
				{
					coord[0]=0;
					prefixAll(theMap,1);
					width++;
				}
			}
			theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
			theMap.get(coord[1]).setCharAt(coord[0], 'v');
			coord[1]++;
			theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
			int stDiff=coord[0]-(nextName.length()/2);
			if(stDiff<0)
			{
				prefixAll(theMap,-stDiff);
				width+= -stDiff;
				stDiff=0;
			}
			for(int i=0;i<nextName.length();i++)
				theMap.get(coord[1]).setCharAt(stDiff+i, nextName.charAt(i));
			break;
		}
		case Directions.SOUTHEAST:
		{
			coord[1]++;
			if(coord[1]>=theMap.size())
			{
				theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '\\');
				coord[1]++;
				coord[0]++;
				width++;
			}
			else
			while(coord[1]<theMap.size())
			{
				theMap.get(coord[1]).setCharAt(coord[0], '\\');
				coord[1]++;
				coord[0]++;
				if(coord[0]==theMap.get(coord[1]).length())
				{
					postfixAll(theMap,1);
					width++;
				}
			}
			for(int i=0;i<(trailCt);i++)
			{
				theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
				theMap.get(coord[1]).setCharAt(coord[0], '.');
				coord[1]++;
				coord[0]++;
				if(coord[0]==theMap.get(coord[1]).length())
				{
					postfixAll(theMap,1);
					width++;
				}
			}
			theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
			theMap.get(coord[1]).setCharAt(coord[0], 'v');
			coord[1]++;
			theMap.add(new StringBuilder(CMStrings.repeat(' ',width)));
			int stDiff=coord[0]-(nextName.length()/2);
			if(stDiff<0)
				stDiff=0;
			if(stDiff+nextName.length()>=theMap.get(coord[1]).length())
				postfixAll(theMap,((stDiff+nextName.length())-theMap.get(coord[1]).length()));
			for(int i=0;i<nextName.length();i++)
				theMap.get(coord[1]).setCharAt(stDiff+i, nextName.charAt(i));
			break;
		}
		case Directions.WEST:
			coord[0]=coord[0]-(lastWidth/2);
			if(coord[0]<0)
			{
				prefixAll(theMap, -coord[0]);
				width+= -coord[0];
			}
			if((coord[0]>=0)
			&&(theMap.get(coord[1]).charAt(coord[0])!=' '))
				coord[0]--;
			if(coord[0]<0)
			{
				coord[0]=0;
				prefixAll(theMap, 1);
				width++;
				theMap.get(coord[1]).setCharAt(coord[0], '-');
			}
			else
			while(coord[0]>=0)
			{
				theMap.get(coord[1]).setCharAt(coord[0], '-');
				coord[0]--;
			}
			coord[0]=0;
			for(int i=0;i<(trailCt);i++)
			{
				this.prefixAll(theMap, 1);
				width++;
				theMap.get(coord[1]).setCharAt(coord[0], '.');
			}
			prefixAll(theMap, 1);
			width++;
			theMap.get(coord[1]).setCharAt(coord[0], '<');
			prefixAll(theMap, nextName.length());
			width++;
			for(int i=0;i<nextName.length();i++)
				theMap.get(coord[1]).setCharAt(i, nextName.charAt(i));
			coord[0]=nextName.length()/2;
			break;
		case Directions.EAST:
			coord[0]=coord[0]+(lastWidth/2);
			if((coord[0]<theMap.get(coord[1]).length())
			&&(theMap.get(coord[1]).charAt(coord[0])!=' '))
				coord[0]++;
			if(coord[0]>=theMap.get(coord[1]).length())
			{
				postfixAll(theMap, 1);
				theMap.get(coord[1]).setCharAt(coord[0], '-');
				coord[0]++;
				width++;
			}
			else
			while(coord[0]<theMap.get(coord[1]).length())
			{
				theMap.get(coord[1]).setCharAt(coord[0], '-');
				coord[0]++;
			}
			coord[0]=theMap.get(coord[1]).length();
			for(int i=0;i<(trailCt);i++)
			{
				postfixAll(theMap, 1);
				theMap.get(coord[1]).setCharAt(coord[0], '.');
				coord[0]++;
				width++;
			}
			postfixAll(theMap, 1);
			theMap.get(coord[1]).setCharAt(coord[0], '>');
			coord[0]++;
			width++;
			postfixAll(theMap, nextName.length());
			width++;
			if(coord[0]+nextName.length()>=theMap.get(coord[1]).length())
				postfixAll(theMap,((coord[0]+nextName.length())-theMap.get(coord[1]).length()));
			for(int i=0;i<nextName.length();i++)
				theMap.get(coord[1]).setCharAt(coord[0]+i, nextName.charAt(i));
			coord[0]+=nextName.length()/2;
			break;
		}
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<2)
		{
			mob.tell(L("You are too stupid to actually draw anything."));
			return false;
		}
		if(commands.size()<1)
		{
			mob.tell(L("What would you like to put your map on?"));
			return false;
		}
		String rest = CMParms.combine(commands);
		Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,rest);
		if(target==null)
		{
			target=mob.location().findItem(null,rest);
			if((target!=null)&&(CMLib.flags().isGettable(target)))
			{
				mob.tell(L("You don't have that."));
				return false;
			}
		}
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",(rest)));
			return false;
		}

		final Item item=target;
		if(((item.material()!=RawMaterial.RESOURCE_PAPER)
		   &&(item.material()!=RawMaterial.RESOURCE_SILK)
		   &&(item.material()!=RawMaterial.RESOURCE_HIDE)
		   &&(item.material()!=RawMaterial.RESOURCE_HEMP))
		||(!item.isReadable()))
		{
			mob.tell(L("You can't draw a map on that."));
			return false;
		}

		if(item instanceof Scroll)
		{
			mob.tell(L("You can't draw a map on a scroll."));
			return false;
		}

		int range = 50 + (10*super.getXLEVELLevel(mob));
		final TrackingFlags flags=CMLib.tracking().newFlags()
				.plus(TrackingLibrary.TrackingFlag.AREAONLY)
				.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
		final List<Room> rooms=CMLib.tracking().getRadiantRooms(mob.location(), flags, range);
		final Set<String> mapsDone=new TreeSet<String>();
		for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if(I!=null)
			{
				Ability A=I.fetchEffect(ID());
				if((A!=null)&&(A.text().length()>0))
					mapsDone.add(A.text());
			}
		}
		Room targetRoom = null;
		Room nearestRoom = null;
		for(final Room room : rooms)
		{
			if(room != null)
			{
				for(int i=0;i<room.numItems();i++)
				{
					Item I=room.getItem(i);
					if((I!=null)&&(I.ID().equals("HoleInTheGround")))
					{
						Ability A=I.fetchEffect("Thief_BuriedTreasure");
						if((A!=null)&&(A.text().equals(mob.Name())))
						{
							nearestRoom = room;
							if(!mapsDone.contains(CMLib.map().getExtendedRoomID(room)))
								targetRoom=room;
						}
					}
				}
			}
		}
		if(targetRoom == null)
			targetRoom = nearestRoom;
		if(targetRoom == null)
		{
			mob.tell(L("You have no buried treasure in this area for which you do not already have a map handy."));
			return false;
		}
		List<Room> dest=new XVector<Room>(targetRoom);
		List<Room> track=CMLib.tracking().findTrailToAnyRoom(R, dest, flags, range);
		
		List<StringBuilder> theMap=new LinkedList<StringBuilder>();
		String firstRoomStr="["+CMStrings.ellipse(CMStrings.removeColors(R.displayText(mob)),20)+"]";
		int lastWidth=firstRoomStr.length();
		theMap.add(new StringBuilder(CMStrings.SPACES.substring(0,lastWidth)));
		theMap.add(new StringBuilder(firstRoomStr));
		theMap.add(new StringBuilder(CMStrings.SPACES.substring(0,lastWidth)));
		StringBuilder desc=new StringBuilder("");
		desc.append(L("^HStart at @x1^N, then go ",R.displayText(mob)));
		int[] coord=new int[]{lastWidth/2,1};
		int trailCt=0;
		Room room=R;
		for(int t=track.size()-2;t>=0;t--)
		{
			final Room nextR=track.get(t);
			final int dir=CMLib.map().getRoomDir(room, nextR);
			if(dir<0)
			{
				mob.tell(L("You can't seem to recall the way from here!"));
				return false;
			}
			if((t>0)&&(CMLib.map().getRoomDir(nextR, track.get(t-1))==dir))
			{
				trailCt++;
				room=nextR;
				continue;
			}
			if(trailCt>0)
				desc.append(L("@x1 @x2 times until you get to @x3, then ",CMLib.directions().getDirectionName(dir).toLowerCase(),""+trailCt,nextR.name()));
			else
				desc.append(L("@x1 to @x2, then ",CMLib.directions().getDirectionName(dir).toLowerCase(),nextR.name()));
			
			insertLink(coord,lastWidth,dir,nextR.name(),trailCt,theMap);
			R.send(mob, CMClass.getMsg(mob, item, this, CMMsg.MSG_WROTE, null, CMMsg.MSG_WROTE, CMLib.map().getExtendedRoomID(nextR),-1,null));
			lastWidth=nextR.name().length()+2;
			room=nextR;
			trailCt=0;
		}
		desc.append(L("at @x1, X will mark the spot.",room.displayText(mob)));
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			StringBuilder buf=new StringBuilder("");
			buf.append(L("@x1's treasure map of @x2\n\r",mob.Name(),room.getArea().Name()));
			for(StringBuilder str : theMap)
				buf.append(CMStrings.rtrim(str.toString())).append("\n\r");
			buf.append("\n\r");
			buf.append(desc).append("\n\r");
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_WRITE,L("<S-NAME> draw(s) a treasure map on <T-NAMESELF>."),CMMsg.MSG_WRITE,buf.toString(),CMMsg.MSG_WRITE,L("<S-NAME> draw(s) a treasure map on <T-NAMESELF>."));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
			mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<S-NAME> attempt(s) to draw a treasure map  on <T-NAMESELF>, but mess(es) up."));
		return success;
	}

}
