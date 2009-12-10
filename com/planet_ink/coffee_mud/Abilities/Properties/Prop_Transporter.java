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
public class Prop_Transporter extends Property
{
	public String ID() { return "Prop_Transporter"; }
	public String name(){ return "Room entering adjuster";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	int transCode=-1;

	public String accountForYourself()
	{ return "Zap them elsewhere";	}

	public int transCode()
	{
		if(transCode>=0) return transCode;
		if(affected==null) return -1;
		if(affected instanceof Drink)
			transCode= CMMsg.TYP_DRINK;
		else
		if(affected instanceof Food)
			transCode= CMMsg.TYP_EAT;
		else
		if(affected instanceof Rideable)
		{
			transCode= CMMsg.TYP_MOUNT;
			switch(((Rideable)affected).rideBasis())
			{
			case Rideable.RIDEABLE_ENTERIN:
				transCode= CMMsg.TYP_ENTER; break;
			case Rideable.RIDEABLE_SIT:
			case Rideable.RIDEABLE_TABLE:
				transCode= CMMsg.TYP_SIT; break;
			case Rideable.RIDEABLE_SLEEP:
				transCode= CMMsg.TYP_SLEEP; break;
			}
		}
		else
		if(affected instanceof MOB)
			transCode= CMMsg.TYP_SPEAK;
		else
		if(affected instanceof Weapon)
			transCode= CMMsg.TYP_WEAPONATTACK;
		else
		if(affected instanceof Armor)
			transCode= CMMsg.TYP_WEAR;
		else
		if(affected instanceof Item)
			transCode= CMMsg.TYP_GET;
		else
		if(affected instanceof Room)
			transCode= CMMsg.TYP_ENTER;
		else
		if(affected instanceof Area)
			transCode= CMMsg.TYP_ENTER;
		else
		if(affected instanceof Exit)
			transCode= CMMsg.TYP_ENTER;
		return transCode;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((transCode()>=0)
		   &&((msg.targetMinor()==transCode())||(msg.sourceMinor()==transCode()))
		   &&(msg.amITarget(affected)||(msg.tool()==affected))
		   &&(text().length()>0))
		{
			Room prevRoom=msg.source().location();
			Room otherRoom=CMLib.map().getRoom(text());
			if(otherRoom==null)
				msg.source().tell("You are whisked nowhere at all, since '"+text()+"' is nowhere to be found.");
			else
			if(prevRoom!=otherRoom)
			{
				otherRoom.bringMobHere(msg.source(),true);
				CMLib.commands().postLook(msg.source(),true);
				if(affected instanceof Rideable)
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,CMMsg.TYP_DISMOUNT,null));
				if((affected instanceof Item)&&(prevRoom.isContent((Item)affected)))
					prevRoom.delItem((Item)affected);
			}

		}
		super.executeMsg(myHost,msg);
	}
}
