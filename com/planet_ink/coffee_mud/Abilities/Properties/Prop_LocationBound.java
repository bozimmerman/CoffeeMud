package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;

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
public class Prop_LocationBound extends Property
{
	public String ID() { return "Prop_LocationBound"; }
	public String name(){ return "Leave the specified area, or room";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.sourceMinor()!=CMMsg.TYP_ENTER)
		&&(msg.target() instanceof Room)
		&&((msg.source()==affected)
	        ||((affected instanceof Item)&&(msg.source()==((Item)affected).owner()))))
		{
		    Room whereTo=(Room)msg.target();
	        Room R=CoffeeUtensils.roomLocation(affected);
	        if((whereTo==null)||(R==null))
	            return true;
	        
		    if(text().length()==0)
		    {
		        if(affected instanceof MOB)
			        msg.source().tell("You are not allowed to leave this place.");
		        else
			        msg.source().tell(affected.name()+" prevents you from taking it that way.");
		        return false;
		    }
		    else
		    if(text().equalsIgnoreCase("ROOM"))
		    {
		        if(whereTo!=R)
		        {
			        if(affected instanceof MOB)
				        msg.source().tell("You are not allowed to leave this place.");
			        else
				        msg.source().tell(affected.name()+" prevents you from taking it that way.");
			        return false;
		        }
		    }
		    else
		    if(text().equalsIgnoreCase("AREA"))
		    {
		        if(whereTo.getArea()!=R.getArea())
		        {
			        if(affected instanceof MOB)
				        msg.source().tell("You are not allowed to leave this place.");
			        else
				        msg.source().tell(affected.name()+" prevents you from taking it that way.");
			        return false;
		        }
		    }
		    else
		    {
		        Room tR=CMMap.getRoom(text());
		        if((tR!=null)&&(whereTo!=tR))
		        {
			        if(R!=tR)
			        {
				        if(affected instanceof MOB)
				        {
					        msg.source().tell("You are whisked back home!");
					        tR.bringMobHere((MOB)affected,false);
				        }
				        else
				        {
					        msg.source().tell(affected.name()+" is whisked from you and back to its home.");
					        tR.bringItemHere((Item)affected,0);
				        }
			        }
			        else
			        {
				        if(affected instanceof MOB)
					        msg.source().tell("You are not allowed to leave this place.");
				        else
					        msg.source().tell(affected.name()+" prevents you from taking it that way.");
			        }
			        return false;
		        }
		        else
		        {
		            Area A=CMMap.getArea(text());
		            if((A!=null)&&(!A.inMetroArea(whereTo.getArea())))
		            {
				        if(!A.inMetroArea(R.getArea()))
				        {
					        if(affected instanceof MOB)
					        {
						        msg.source().tell("You are whisked back home!");
						        A.getRandomMetroRoom().bringMobHere((MOB)affected,false);
					        }
					        else
					        {
						        msg.source().tell(affected.name()+" is whisked from you and back to its home.");
						        A.getRandomMetroRoom().bringItemHere((Item)affected,0);
					        }
				        }
				        else
				        {
					        if(affected instanceof MOB)
						        msg.source().tell("You are not allowed to leave this place.");
					        else
						        msg.source().tell(affected.name()+" prevents you from taking it that way.");
				        }
				        return false;
		            }
		        }
		    }
		}
		return true;
	}
}
