package com.planet_ink.coffee_mud.Commands;
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
public class Flee extends Go
{
	public Flee(){}

	private String[] access={"FLEE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String direction="";
		if(commands.size()>1) direction=Util.combine(commands,1);
		if(mob==null) return false;
		Room R=mob.location();
		if((!mob.isMonster())||(mob.amFollowing()!=null))
		{
			if((R==null)||(!mob.isInCombat()))
			{
				mob.tell(getScr("Movement","fleeerr1"));
				return false;
			}
		}

		int directionCode=-1;
		if(!direction.equals("NOWHERE"))
		{
			if(direction.length()==0)
			{
				Vector directions=new Vector();
				for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
				{
					Exit thisExit=R.getExitInDir(i);
					Room thisRoom=R.getRoomInDir(i);
					if((thisRoom!=null)&&(thisExit!=null)&&(thisExit.isOpen()))
						directions.addElement(new Integer(i));
				}
				// up is last resort
				if(directions.size()>1)
					directions.removeElement(new Integer(Directions.UP));
				if(directions.size()>0)
				{
					directionCode=((Integer)directions.elementAt(Dice.roll(1,directions.size(),-1))).intValue();
					direction=Directions.getDirectionName(directionCode);
				}
			}
			else
				directionCode=Directions.getGoodDirectionCode(direction);
			if(directionCode<0)
			{
				mob.tell(getScr("Movement","fleeerr2"));
				return false;
			}
		}
		int lostExperience=10;
		if(mob.getVictim()!=null)
		{
			MOB victim=mob.getVictim();
			String whatToDo=CommonStrings.getVar(CommonStrings.SYSTEM_PLAYERFLEE);
			if(whatToDo==null) return false;
			if(whatToDo.startsWith("UNL"))
			{
				Vector V=Util.parse(whatToDo);
				int times=1;
				if((V.size()>1)&&(Util.s_int((String)V.lastElement())>1))
					times=Util.s_int((String)V.lastElement());
				for(int t=0;t<times;t++)
					mob.charStats().getCurrentClass().unLevel(mob);
			}
			else
			if(whatToDo.startsWith("PUR"))
			{
				MOB deadMOB=CMMap.getLoadPlayer(mob.Name());
				if(deadMOB!=null)
				{
					CoffeeUtensils.obliteratePlayer(deadMOB,false);
					return false;
				}
			}
			else
			if((whatToDo.trim().equals("0"))||(Util.s_int(whatToDo)>0))
				lostExperience=Util.s_int(whatToDo);
			else
			{
				lostExperience=10+((mob.envStats().level()-victim.envStats().level()))*5;
				if(lostExperience<10) lostExperience=10;
			}
		}
		if((direction.equals("NOWHERE"))||((directionCode>=0)&&(move(mob,directionCode,true,false,false))))
		{
			mob.makePeace();
			if(lostExperience>0)
			{
				mob.tell(getScr("Movement","fleeexp",""+lostExperience));
				MUDFight.postExperience(mob,null,null,-lostExperience,false);
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
