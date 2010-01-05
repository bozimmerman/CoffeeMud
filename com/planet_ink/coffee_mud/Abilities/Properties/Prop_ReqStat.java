package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Prop_ReqStat extends Property
{
	public String ID() { return "Prop_ReqStat"; }
	public String name(){ return "Require stat values";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS|Ability.CAN_ITEMS;}
	private boolean noSneak=false;
	
	public void setMiscText(String txt)
	{
		noSneak=false;
		Vector parms=CMParms.parse(txt.toUpperCase());
		String s;
		for(Enumeration p=parms.elements();p.hasMoreElements();)
		{
			s=(String)p.nextElement();
			if(s.startsWith("NOSNEAK"))
				noSneak=true;
		}
		super.setMiscText(txt);
	}
	

	public String accountForYourself()
	{
		return "Entry restricted as follows: "+CMLib.masking().maskDesc(miscText);
	}
	
	public boolean passesMuster(MOB mob, String msg)
	{
		if(mob==null) return false;
		if(CMLib.flags().isATrackingMonster(mob))
			return true;
		if(CMLib.flags().isSneaking(mob)&&(!noSneak))
			return true;
		int[] comp=null;
		for(int c : CharStats.CODES.ALL())
		{
			comp=CMParms.getParmCompare(text(),CharStats.CODES.NAME(c),mob.charStats().getStat(c));
			if(comp[1]<0)
			{
				switch(comp[0])
				{
				case '=':
				case '!':
					mob.tell("You aren't the right "+CMStrings.capitalizeAndLower(CharStats.CODES.NAME(c))+" to "+msg+".");
					break;
				case '<':
					mob.tell("You are too "+CMStrings.capitalizeAndLower(CharStats.CODES.ATTDESC(c))+" to "+msg+".");
					break;
				case '>':
					mob.tell("You are not "+CMStrings.capitalizeAndLower(CharStats.CODES.ATTDESC(c))+" enough to "+msg+".");
					break;
				}
				return false;
			}
		}
		return true;
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected!=null)
		{
			if((msg.target()==affected)
			&&(affected instanceof Exit)
			&&(((Exit)affected).hasADoor()))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_OPEN:
					if(passesMuster(msg.source(),((Exit)affected).openWord()+" that"))
						return super.okMessage(myHost,msg);
					return false;
				case CMMsg.TYP_CLOSE:
					if(passesMuster(msg.source(),((Exit)affected).closeWord()+" that"))
						return super.okMessage(myHost,msg);
					return false;
				}
			}
			else
			if((msg.target()==affected)
			&&(affected instanceof Container)
			&&(((Container)affected).hasALid()))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_OPEN:
					if(passesMuster(msg.source(),"open that"))
						return super.okMessage(myHost,msg);
					return false;
				case CMMsg.TYP_CLOSE:
					if(passesMuster(msg.source(),"close that"))
						return super.okMessage(myHost,msg);
					return false;
				}
			}
			else
			if((msg.target()!=null)
		    &&(((msg.target() instanceof Room)&&(msg.targetMinor()==CMMsg.TYP_ENTER))
			  ||((msg.target() instanceof Rideable)&&(msg.targetMinor()==CMMsg.TYP_SIT)))
		    &&(!CMLib.flags().isFalling(msg.source()))
		    &&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
			{
				if(passesMuster(msg.source(),"go there"))
					return super.okMessage(myHost,msg);
				return false;
			}
			else
			if((affected instanceof Item)
			&&(((Item)affected).owner() instanceof MOB))
			{
				Item myItem=(Item)affected;
				if(msg.amISource((MOB)myItem.owner()))
					switch(msg.sourceMinor())
					{
					case CMMsg.TYP_FILL:
						if((myItem instanceof Drink)
						&&(msg.tool()!=myItem)
						&&(msg.amITarget(myItem)))
						{
							if(passesMuster(msg.source(),"fill that"))
								return super.okMessage(myHost,msg);
							return false;
						}
						break;
					case CMMsg.TYP_WEAR:
						if((myItem instanceof Armor)
					    &&(msg.amITarget(myItem)))
						{
							if(passesMuster(msg.source(),"wear that"))
								return super.okMessage(myHost,msg);
							return false;
						}
						break;
					case CMMsg.TYP_PUT:
						if((myItem instanceof Container)
						&&(msg.amITarget(myItem)))
						{
							if(passesMuster(msg.source(),"put that in there"))
								return super.okMessage(myHost,msg);
							return false;
						}
						break;
					case CMMsg.TYP_WIELD:
					case CMMsg.TYP_HOLD:
						if((!(myItem instanceof Drink))
					    &&(!(myItem instanceof Armor))
					    &&(!(myItem instanceof Container))
					    &&(msg.amITarget(myItem)))
						{
							if(passesMuster(msg.source(),"hold that"))
								return super.okMessage(myHost,msg);
							return false;
						}
						break;
					}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
