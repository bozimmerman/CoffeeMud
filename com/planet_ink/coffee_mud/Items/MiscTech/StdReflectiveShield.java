package com.planet_ink.coffee_mud.Items.MiscTech;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class StdReflectiveShield extends StdElecItem
{
	public String ID(){	return "StdReflectiveShield";}

	public StdReflectiveShield()
	{
		super();
		setName("a reflective shield generator");
		basePhyStats.setWeight(2);
		setDisplayText("a reflective shield generator sits here.");
		setDescription("");
		baseGoldValue=500;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		super.activate(true);
		super.setRawProperLocationBitmap(Wearable.WORN_ABOUT_BODY);
		super.setPowerCapacity(1000);
		super.setPowerRemaining(1000);
	}
	
	protected String fieldOnStr(MOB viewerM) { return "A glossy transparent field of energy surrounds "+name(viewerM)+"."; }
	
	protected String fieldDeadStr(MOB viewerM) { return "The reflective field around <S-NAME> flickers and dies out."; }
	
	protected boolean doShield(MOB mob, CMMsg msg, double successFactor)
	{
		msg.addTrailerMsg(CMClass.getMsg(mob,msg.source(),null,CMMsg.MSG_OK_VISUAL,"The reflective field around <S-NAME> reflects the "+msg.tool().name()+" damage."));
		return false;
	}
	
	protected boolean doesShield(MOB mob, CMMsg msg, double successFactor)
	{
		return (Math.random() >= successFactor) && activated();
	}
	
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdReflectiveShield)) return false;
		return super.sameAs(E);
	}
	
	public void setMiscText(String newText)
	{
		if(CMath.isInteger(newText))
			this.setPowerCapacity(CMath.s_int(newText));
		super.setMiscText(newText);
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.amITarget(owner()) && (owner() instanceof MOB) && (!amWearingAt(Item.IN_INVENTORY)))
		{
			MOB mob=(MOB)owner();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DAMAGE: // remember 50% miss rate
				if((msg.tool() instanceof Electronics)&&(activated())&&(powerRemaining()>0))
				{
					double successFactor=0.5;
					final Manufacturer m=getFinalManufacturer();
					successFactor=m.getReliabilityPct()*successFactor;
					int weaponTech=((Electronics)msg.tool()).techLevel();
					int myTech=techLevel();
					int techDiff=Math.max(Math.min(myTech-weaponTech,10),-10);
					if(techDiff!= 0) successFactor+=(0.05)*techDiff;
					if(doesShield(mob, msg, successFactor))
					{
						long powerConsumed=Math.round(msg.value()*m.getEfficiencyPct());
						if(powerRemaining()>=powerConsumed)
						{
							setPowerRemaining(powerRemaining()-powerConsumed);
							if(!doShield(mob,msg, successFactor))
								return false;
						}
						else
						{
							setPowerRemaining(0);
							CMMsg msg2=CMClass.getMsg(mob, this, null,CMMsg.MSG_OK_VISUAL,CMMsg.TYP_DEACTIVATE|CMMsg.MASK_ALWAYS,CMMsg.MSG_OK_VISUAL,fieldDeadStr(msg.source()));
							if(mob.location()!=null)
								mob.location().send(mob, msg2);
						}
					}
				}
				break;
			}
		}
		return true;
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
				{
					msg.source().tell(name()+" is currently "+(activated()?"activated":"deactivated")
							+" and is at "+Math.round(CMath.div(powerRemaining(),powerCapacity())*100.0)+"% power.");
				}
				return;
			case CMMsg.TYP_ACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, fieldOnStr(null));
				this.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, fieldDeadStr(null));
				this.activate(false);
				break;
			}
		}
		else if(msg.amITarget(owner()) && (owner() instanceof MOB) && (!amWearingAt(Item.IN_INVENTORY)))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(owner(), msg.source()) &&(activated())&&(powerRemaining()>0))
					msg.source().tell(fieldOnStr(msg.source()));
				return;
			}
		}
		super.executeMsg(host, msg);
	}
}
