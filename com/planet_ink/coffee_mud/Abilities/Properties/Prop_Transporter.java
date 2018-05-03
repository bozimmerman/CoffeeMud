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
   Copyright 2002-2018 Bo Zimmerman

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
public class Prop_Transporter extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_Transporter";
	}

	@Override
	public String name()
	{
		return "Room entering adjuster";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_EXITS|Ability.CAN_ROOMS;
	}

	protected int transCode=-1;
	protected String roomID = "START";
	protected boolean sendEnter = false; 

	@Override
	public void setMiscText(String newMiscText)
	{
		roomID = "START";
		sendEnter = false;
		super.setMiscText(newMiscText);
		int x=newMiscText.indexOf(';');
		if(x<0)
		{
			roomID=newMiscText;
		}
		else
		{
			roomID=newMiscText.substring(0,x);
			String vars=newMiscText.substring(x+1);
			sendEnter = CMParms.getParmBool(vars, "SENDENTER", false);
		}
	}
	
	@Override
	public String accountForYourself()
	{
		return "Zap them elsewhere";
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_DROP_PUTIN;
	}

	public int transCode()
	{
		if(transCode>=0)
			return transCode;
		if(affected==null)
			return -1;
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

	protected Exit getAnyExit(final Room R)
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			Exit E2=R.getExitInDir(d);
			if(E2 != null)
				return E2;
		}
		return null;
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((transCode()>=0)
		   &&((msg.targetMinor()==transCode())||(msg.sourceMinor()==transCode()))
		   &&(msg.amITarget(affected)||(msg.tool()==affected))
		   &&(roomID.length()>0))
		{
			final Room prevRoom=msg.source().location();
			final Room otherRoom=CMLib.map().getRoom(roomID);
			if(otherRoom==null)
				msg.source().tell(L("You are whisked nowhere at all, since '@x1' is nowhere to be found.",roomID));
			else
			if(prevRoom!=otherRoom)
			{
				otherRoom.bringMobHere(msg.source(),true);
				if(sendEnter && (CMLib.map().getRoom(msg.source().location()) == CMLib.map().getRoom(otherRoom)))
					otherRoom.send(msg.source(), CMClass.getMsg(msg.source(),otherRoom,getAnyExit(otherRoom),CMMsg.MSG_ENTER,null));
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
