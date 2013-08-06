package com.planet_ink.coffee_mud.Items.ShipTech;
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
import com.planet_ink.coffee_mud.Items.BasicTech.StdElecContainer;
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
public class StdElecCompContainer extends StdElecContainer implements ShipComponent
{
	public String ID(){	return "StdElecCompContainer";}
	
	protected float installedFactor = 1.0f;
	
	public StdElecCompContainer()
	{
		super();
		setName("a component container");
		basePhyStats.setWeight(500);
		setDisplayText("a component container sits here.");
		setDescription("");
		baseGoldValue=500000;
		setUsesRemaining(100);
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
	}
	
	@Override public float getInstalledFactor() { return installedFactor; }
	@Override public void setInstalledFactor(float pct) { installedFactor=pct; }
	
	@Override
	public boolean subjectToWearAndTear()
	{
		return true;
	}
	
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdElecCompContainer)) return false;
		return super.sameAs(E);
	}
	
	protected volatile String circuitKey=null;
	
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(affected instanceof Room)
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_ROOMCIRCUITED);
	}
	
	public void destroy()
	{
		if((!destroyed)&&(circuitKey!=null))
		{
			CMLib.tech().unregisterElectronics(this,circuitKey);
			circuitKey=null;
		}
		super.destroy();
	}
	
	public void setOwner(ItemPossessor newOwner)
	{
		final ItemPossessor prevOwner=super.owner;
		super.setOwner(newOwner);
		if(prevOwner != newOwner)
		{
			if(newOwner instanceof Room)
				circuitKey=CMLib.tech().registerElectrics(this,circuitKey);
			else
			{
				CMLib.tech().unregisterElectronics(this,circuitKey);
				circuitKey=null;
			}
		}
	}
	
	protected static final boolean isThisPanelActivated(Electronics.ElecPanel E)
	{
		if(!E.activated())
			return false;
		if(E.container() instanceof Electronics.ElecPanel)
			return isThisPanelActivated((Electronics.ElecPanel)E.container());
		return true;
	}
	
	public static final boolean isAllWiringConnected(Electronics E)
	{
		if(E instanceof Electronics.ElecPanel)
			return isThisPanelActivated((Electronics.ElecPanel)E);
		if(E.container() instanceof Electronics.ElecPanel)
			return isThisPanelActivated((Electronics.ElecPanel)E.container());
		return true;
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if(!isAllWiringConnected(this))
				{
					if(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG))
						msg.source().tell("The panel containing "+name()+" is not activated or connected.");
					return false;
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				break;
			case CMMsg.TYP_LOOK:
				break;
			case CMMsg.TYP_POWERCURRENT:
				break;
			}
		}
		return super.okMessage(host, msg);
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, "<S-NAME> activate(s) <T-NAME>.");
				this.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, "<S-NAME> deactivate(s) <T-NAME>.");
				this.activate(false);
				break;
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
					msg.source().tell(name()+" is currently "+(activated()?"connected.\n\r":"deactivated/disconnected.\n\r"));
				return;
			}
		}
		super.executeMsg(host, msg);
	}
}
