package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class Prop_ReqStat extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_ReqStat";
	}

	@Override
	public String name()
	{
		return "Require stat values";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS|Ability.CAN_ITEMS;
	}

	private boolean noSneak=false;

	@Override
	public long flags()
	{
		return Ability.FLAG_ZAPPER;
	}

	@Override
	public int triggerMask()
	{
		if((affected instanceof Room)||(affected instanceof Area)||(affected instanceof Exit))
			return TriggeredAffect.TRIGGER_ENTER;
		if((affected instanceof Armor)||(affected instanceof Weapon))
			return TriggeredAffect.TRIGGER_WEAR_WIELD;
		if((affected instanceof Drink)||(affected instanceof Food))
			return TriggeredAffect.TRIGGER_USE;
		if((affected instanceof Room)||(affected instanceof Area)||(affected instanceof Exit))
			return TriggeredAffect.TRIGGER_ENTER;
		if(affected instanceof Container)
			return TriggeredAffect.TRIGGER_DROP_PUTIN;
		return TriggeredAffect.TRIGGER_WEAR_WIELD;
	}

	@Override
	public void setMiscText(String txt)
	{
		noSneak=false;
		final Vector<String> parms=CMParms.parse(txt.toUpperCase());
		String s;
		for(final Enumeration<String> p=parms.elements();p.hasMoreElements();)
		{
			s=p.nextElement();
			if(s.startsWith("NOSNEAK"))
				noSneak=true;
		}
		super.setMiscText(txt);
	}

	@Override
	public String accountForYourself()
	{
		return "Entry restricted as follows: "+CMLib.masking().maskDesc(miscText);
	}

	public boolean passesMuster(MOB mob, String msg)
	{
		if(mob==null)
			return false;
		if(CMLib.flags().isATrackingMonster(mob))
			return true;
		if(CMLib.flags().isSneaking(mob)&&(!noSneak))
			return true;
		char[] comparator=new char[]{'\0'};
		for(final int c : CharStats.CODES.ALLCODES())
		{
			if(!CMParms.getParmCompare(text(),CharStats.CODES.NAME(c),mob.charStats().getStat(c),comparator))
			{
				switch(comparator[0])
				{
				case '=':
				case '!':
					mob.tell(L("You aren't the right @x1 to @x2.",CMStrings.capitalizeAndLower(CharStats.CODES.NAME(c)),msg));
					break;
				case '<':
					mob.tell(L("You are too @x1 to @x2.",CMStrings.capitalizeAndLower(CharStats.CODES.ATTDESC(c)),msg));
					break;
				case '>':
					mob.tell(L("You are not @x1 enough to @x2.",CMStrings.capitalizeAndLower(CharStats.CODES.ATTDESC(c)),msg));
					break;
				}
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
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
			&&(((Container)affected).hasADoor()))
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
			if((((msg.target() instanceof Room)&&(msg.targetMinor()==CMMsg.TYP_ENTER))
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
				final Item myItem=(Item)affected;
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
					case CMMsg.TYP_INSTALL:
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
