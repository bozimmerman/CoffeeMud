package com.planet_ink.coffee_mud.Abilities.Songs;

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
public class Play_Retreat extends Play
{
	public String ID() { return "Play_Retreat"; }
	public String name(){ return "Retreat";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return 0;}
	protected boolean persistantSong(){return false;}
	protected String songOf(){return "a "+name();}
	int directionCode=-1;

	protected void inpersistantAffect(MOB mob)
	{
		if(directionCode<0)
		{
			mob.tell(CommonStrings.getScr("Movement","fleeerr2"));
			return;
		}
		mob.makePeace();
		MUDTracker.move(mob,directionCode,true,false);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		directionCode=-1;
		String where=Util.combine(commands,0);
		if(!where.equals("NOWHERE"))
		{
			if(where.length()==0)
			{
				Vector directions=new Vector();
				for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
				{
					Exit thisExit=mob.location().getExitInDir(i);
					Room thisRoom=mob.location().getRoomInDir(i);
					if((thisRoom!=null)&&(thisExit!=null)&&(thisExit.isOpen()))
						directions.addElement(new Integer(i));
				}
				// up is last resort
				if(directions.size()>1)
					directions.removeElement(new Integer(Directions.UP));
				if(directions.size()>0)
				{
					directionCode=((Integer)directions.elementAt(Dice.roll(1,directions.size(),-1))).intValue();
					where=Directions.getDirectionName(directionCode);
				}
			}
			else
				directionCode=Directions.getGoodDirectionCode(where);
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		return true;
	}
}
