package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Skill_RegionalAwareness extends StdAbility
{
	public String ID() { return "Skill_RegionalAwareness"; }
	public String name(){ return "Regional Awareness";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"REGION","REGIONALAWARENESS"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public int overrideMana(){return 0;}
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONPRACCOST);}

	public char roomChar(Room room)
	{
		switch(room.domainType())
		{
		case Room.DOMAIN_OUTDOORS_CITY:return '=';
		case Room.DOMAIN_OUTDOORS_WOODS:return 'T';
		case Room.DOMAIN_OUTDOORS_ROCKS:return ':';
		case Room.DOMAIN_OUTDOORS_PLAINS:return '_';
		case Room.DOMAIN_OUTDOORS_UNDERWATER:return '~';
		case Room.DOMAIN_OUTDOORS_AIR:return ' ';
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:return '~';
		case Room.DOMAIN_OUTDOORS_JUNGLE:return 'J';
		case Room.DOMAIN_OUTDOORS_SWAMP:return 'x';
		case Room.DOMAIN_OUTDOORS_DESERT:return '.';
		case Room.DOMAIN_OUTDOORS_HILLS:return 'h';
		case Room.DOMAIN_OUTDOORS_MOUNTAINS:return 'M';
		case Room.DOMAIN_OUTDOORS_SPACEPORT:return '@';
		default: 
			return '#';
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.location().domainType()&Room.INDOORS)==Room.INDOORS)
		{
			mob.tell("This only works outdoors.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
		    return false;
		
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_EXAMINESOMETHING,"<S-NAME> peer(s) at the horizon with a distant expression.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				
				int diameter=2+(adjustedLevel(mob,asLevel)/10);
				char[][] map=new char[diameter][diameter];
				for(int i=0;i<diameter;i++)
					for(int i2=0;i2<diameter;i2++)
						map[i][i2]=' ';
				Room[][] rmap=new Room[diameter][diameter];
				Vector rooms=new Vector();
				HashSet closedPaths=new HashSet();
				MUDTracker.getRadiantRooms(mob.location(),rooms,true,false,true,true,false,null,diameter);
				rmap[diameter/2][diameter/2]=mob.location();
				map[diameter/2][diameter/2]='*';
				for(int i=0;i<rooms.size();i++)
				{
					Room R=(Room)rooms.elementAt(i);
					if((closedPaths.contains(R)) 
					||(R==mob.location()))
						continue;
					Room parentR=null;
					int parentDir=-1;
					int[] xy=null;
					for(int i2=0;(i2<diameter)&&(parentR==null);i2++)
						for(int i3=0;(i3<diameter)&&(parentR==null);i3++)
						{
							Room R2=rmap[i2][i3];
							if(R2!=null)
							for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
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
					if((parentDir<0)
					||(xy[0]<0)||(xy[0]>=diameter)||(xy[1]<0)||(xy[1]>=diameter)
					||(map[xy[1]][xy[0]]!=' '))
						closedPaths.add(R);
					else
					{
						map[xy[1]][xy[0]]=roomChar(R);
						rmap[xy[1]][xy[0]]=R;
					
						if((R.domainType()&Room.INDOORS)==Room.INDOORS)
							closedPaths.add(R);
					}
				}
				StringBuffer str=new StringBuffer("");
				for(int i2=0;i2<diameter;i2++)
				{
					for(int i3=0;i3<diameter;i3++)
						str.append(map[i2][i3]);
					str.append("\n\r");
				}
				if(mob.session()!=null) mob.session().rawPrintln(str.toString());
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> peer(s) around distantly, looking frustrated.");
		return success;
	}

}
