package com.planet_ink.coffee_mud.Items.BasicTech;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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
public class GenTickerShield extends GenElecItem
{

	public String ID(){	return "GenTickerShield";}
	
	public GenTickerShield()
	{
		super();
		setName("a personal field generator");
		basePhyStats.setWeight(2);
		setDisplayText("a personal field generator sits here.");
		setDescription("");
		baseGoldValue=2500;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		super.activate(true);
		super.setRawProperLocationBitmap(Wearable.WORN_ABOUT_BODY);
		super.setPowerCapacity(100);
		super.setPowerRemaining(100);
	}
	
	protected String fieldOnStr(MOB viewerM) { return "A field surrounds <O-NAME>."; }
	
	protected String fieldDeadStr(MOB viewerM) { return "The around <S-NAME> flickers and dies out as <S-HE-SHE> fade(s) back into view."; }
	
	@Override public TechType getTechType() { return TechType.PERSONAL_SHIELD; }

	@Override
	public void setOwner(ItemPossessor owner)
	{
		final ItemPossessor prevOwner=super.owner;
		super.setOwner(owner);
		if((prevOwner != owner)&&(owner!=null))
		{
			if(!CMLib.threads().isTicking(this, Tickable.TICKID_ELECTRONICS))
				CMLib.threads().startTickDown(this, Tickable.TICKID_ELECTRONICS, 1);
		}
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(activated() && (tickID==Tickable.TICKID_ELECTRONICS))
		{
			if(!amWearingAt(Item.IN_INVENTORY))
			setPowerRemaining(powerRemaining()-1);
			if(powerRemaining()<=0)
			{
				setPowerRemaining(0);
				if(owner() instanceof MOB)
				{
					MOB mob=(MOB)owner();
					CMMsg msg=CMClass.getMsg(mob, this, null,CMMsg.MSG_OK_VISUAL,CMMsg.TYP_DEACTIVATE|CMMsg.MASK_ALWAYS,CMMsg.MSG_OK_VISUAL,fieldDeadStr(mob));
					if(mob.location()!=null)
						mob.location().send(mob, msg);
				}
				else
					activate(false);
			}
		}
		return !amDestroyed();
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.amITarget(owner()) && (owner() instanceof MOB))
		{
			MOB mob=(MOB)owner();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_REMOVE:
			case CMMsg.TYP_DROP:
				if(activated())
					msg.addTrailerMsg(CMClass.getMsg(mob, this, null,CMMsg.MSG_OK_VISUAL,CMMsg.TYP_DEACTIVATE|CMMsg.MASK_ALWAYS,CMMsg.MSG_OK_VISUAL,fieldDeadStr(msg.source())));
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
					msg.source().location().show(msg.source(), this, owner(), CMMsg.MSG_OK_VISUAL, fieldOnStr(null));
				this.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, owner(), CMMsg.MSG_OK_VISUAL, fieldDeadStr(null));
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
					msg.source().tell(msg.source(),this,owner(),fieldOnStr(msg.source()));
				return;
			}
		}
		super.executeMsg(host, msg);
	}
}
