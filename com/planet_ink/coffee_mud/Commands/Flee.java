package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
		if(commands.size()>1) direction=CMParms.combine(commands,1);
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
        
        boolean XPloss=true;
        if(mob.getVictim()!=null)
        {
            HashSet H=CMLib.combat().allCombatants(mob);
            for(Iterator i=H.iterator();i.hasNext();)
            {
                MOB M=(MOB)i.next();
                if(CMLib.flags().aliveAwakeMobileUnbound(M,true))
                {
                    XPloss=true;
                    break;
                }
                XPloss=false;
            }
        }
        
        if((!XPloss)&&(direction.length()==0))
        {
            mob.tell(getScr("Movement","fleestop"));
            direction="NOWHERE";
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
					directionCode=((Integer)directions.elementAt(CMLib.dice().roll(1,directions.size(),-1))).intValue();
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
		if(XPloss&&(mob.getVictim()!=null))
		{
			MOB victim=mob.getVictim();
			String whatToDo=CMProps.getVar(CMProps.SYSTEM_PLAYERFLEE);
			if(whatToDo==null) return false;
			if(whatToDo.startsWith("UNL"))
			{
				Vector V=CMParms.parse(whatToDo);
				int times=1;
				if((V.size()>1)&&(CMath.s_int((String)V.lastElement())>1))
					times=CMath.s_int((String)V.lastElement());
				for(int t=0;t<times;t++)
					mob.charStats().getCurrentClass().unLevel(mob);
			}
			else
			if(whatToDo.startsWith("PUR"))
			{
				MOB deadMOB=CMLib.map().getLoadPlayer(mob.Name());
				if(deadMOB!=null)
				{
					CMLib.utensils().obliteratePlayer(deadMOB,false);
					return false;
				}
			}
			else
            if(whatToDo.startsWith("LOSESK"))
            {
                if(mob.numLearnedAbilities()>0)
                {
                    Ability A=mob.fetchAbility(CMLib.dice().roll(1,mob.numLearnedAbilities(),-1));
                    if(A!=null)
                    {
                        mob.tell(getScr("Movement","loseskill",A.Name()));
                        mob.delAbility(A);
                        if(A.isAutoInvoked())
                        {
                            Ability A2=mob.fetchEffect(A.ID());
                            A2.unInvoke();
                            mob.delEffect(A2);
                        }
                    }
                }
            }
            else
			if((whatToDo.trim().equals("0"))||(CMath.s_int(whatToDo)>0))
				lostExperience=CMath.s_int(whatToDo);
			else
			{
				lostExperience=10+((mob.envStats().level()-victim.envStats().level()))*5;
				if(lostExperience<10) lostExperience=10;
			}
		}
		if((direction.equals("NOWHERE"))||((directionCode>=0)&&(move(mob,directionCode,true,false,false))))
		{
			mob.makePeace();
			if(XPloss&&(lostExperience>0))
			{
				mob.tell(getScr("Movement","fleeexp",""+lostExperience));
				CMLib.combat().postExperience(mob,null,null,-lostExperience,false);
			}
		}
		return false;
	}
	public double actionsCost(){return 0.0;}
	public boolean canBeOrdered(){return true;}

	
}
