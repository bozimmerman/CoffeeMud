package com.planet_ink.coffee_mud.Abilities.Properties;

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
public class Prop_ReqTattoo extends Property
{
	public String ID() { return "Prop_ReqTattoo"; }
	public String name(){ return "Tattoo Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS|Ability.CAN_ITEMS;}

	public boolean passesMuster(MOB mob)
	{
		if(mob==null) return false;
		if(Sense.isATrackingMonster(mob))
			return true;
		if(Sense.isSneaking(mob)&&(text().toUpperCase().indexOf("NOSNEAK")<0))
			return true;
		int x=text().toUpperCase().indexOf("ALL");
		Vector V=new Vector();
		for(int m=0;m<mob.numTattoos();m++)
			V.addElement(mob.fetchTattoo(m));
		if(V.size()==0) V.addElement("NONE");
		for(int v=0;v<V.size();v++)
		{
			String tattoo=(String)V.elementAt(v);
			if((tattoo.length()>0)
			&&(Character.isDigit(tattoo.charAt(0)))
			&&(tattoo.indexOf(" ")>0)
			&&(Util.isNumber(tattoo.substring(0,tattoo.indexOf(" ")))))
			   tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
			int y=text().toUpperCase().indexOf(tattoo);
			if(((x>0)
				&&(text().charAt(x-1)=='-')
				&&((y<=0)
				   ||((y>0)&&(text().charAt(y-1)!='+'))))
			 ||((y>0)&&(text().charAt(y-1)=='-')))
				return false;
		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(msg.target()!=null)
		&&(!Sense.isFalling(msg.source()))
		&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
		{
			if(((msg.target() instanceof Room)&&(msg.targetMinor()==CMMsg.TYP_ENTER))
			||((msg.target() instanceof Item)&&((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_SIT))))
			{
				HashSet H=new HashSet();
				if(text().toUpperCase().indexOf("NOFOL")>=0)
					H.add(msg.source());
				else
				{
					msg.source().getGroupMembers(H);
					for(Iterator e=H.iterator();e.hasNext();)
						((MOB)e.next()).getRideBuddies(H);
				}
				for(Iterator e=H.iterator();e.hasNext();)
				{
				    Environmental E=(Environmental)e.next();
				    if((E instanceof MOB)
					&&(passesMuster((MOB)E)))
						return super.okMessage(myHost,msg);
				}
				if(msg.target() instanceof Room)
					msg.source().tell("You have not been granted authorization to go that way.");
				else
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,affected,CMMsg.MSG_OK_ACTION,"<O-NAME> flashes and flies out of <S-HIS-HER> hands!");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
