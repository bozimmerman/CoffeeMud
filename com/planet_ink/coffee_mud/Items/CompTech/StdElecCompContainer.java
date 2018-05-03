package com.planet_ink.coffee_mud.Items.CompTech;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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
public class StdElecCompContainer extends StdElecContainer implements TechComponent
{
	@Override
	public String ID()
	{
		return "StdElecCompContainer";
	}

	protected float				maxRechargePer	= 0;

	protected float				installedFactor	= 1.0f;
	protected volatile String	circuitKey		= null;

	public StdElecCompContainer()
	{
		super();
		setName("a component container");
		basePhyStats.setWeight(500);
		setDisplayText("a component container sits here.");
		setDescription("");
		baseGoldValue=500;
		setUsesRemaining(100);
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		this.setRechargeRate(this.powerCapacity());
	}

	@Override
	public float getInstalledFactor()
	{
		return installedFactor;
	}

	@Override
	public void setInstalledFactor(float pct)
	{
		if ((pct >= 0.0) && (pct <= 2.0))
			installedFactor = pct;
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return true;
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdElecCompContainer))
			return false;
		return super.sameAs(E);
	}

	@Override
	public void setRechargeRate(float pctCapPer)
	{
		this.maxRechargePer = pctCapPer;
	}

	@Override
	public float getRechargeRate()
	{
		return maxRechargePer;
	}
	
	@Override
	public int powerNeeds()
	{
		return (int)Math.min((powerCapacity - power), getRechargeRate());
	}

	@Override
	public void destroy()
	{
		if((!destroyed)&&(circuitKey!=null))
		{
			CMLib.tech().unregisterElectronics(this,circuitKey);
			circuitKey=null;
		}
		super.destroy();
	}

	@Override
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

	@Override
	protected double getComputedEfficiency()
	{
		return super.getComputedEfficiency() * this.getInstalledFactor();
	}
	
	protected static boolean reportError(final Electronics me, final Software controlI, final MOB mob, final String literalMessage, final String controlMessage)
	{
		if((mob!=null) && (mob.location()==CMLib.map().roomLocation(me)) && (literalMessage!=null))
			mob.tell(literalMessage);
		if(controlMessage!=null)
		{
			if(controlI!=null)
				controlI.addScreenMessage(controlMessage);
			else
			if((mob!=null)&&(me!=null))
				mob.tell(CMLib.lang().L("A panel on @x1 reports '@x2'.",me.name(mob),controlMessage));
		}
		return false;
	}

	protected static final boolean isThisPanelActivated(ElecPanel E)
	{
		if(!E.activated())
			return false;
		if(E.container() instanceof ElecPanel)
			return isThisPanelActivated((ElecPanel)E.container());
		return true;
	}

	public static final boolean isAllWiringHot(Electronics E)
	{
		if(E instanceof ElecPanel)
			return isThisPanelActivated((ElecPanel)E);
		if(E.container() instanceof ElecPanel)
			return isThisPanelActivated((ElecPanel)E.container());
		return false;
	}

	@Override
	public boolean isInstalled()
	{
		if(!CMLib.flags().isGettable(this))
			return true;
		if(this.container() instanceof ElecPanel)
			return (!CMLib.flags().isGettable(this.container()));
		if(this.container() instanceof TechComponent)
			return ((TechComponent)this.container()).isInstalled();
		return false;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if(!isInstalled())
				{
					if(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG))
						msg.source().tell(L("@x1 is not installed or connected.",name()));
					return false;
				}
				else
				if(!isAllWiringHot(this))
				{
					if(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG))
						msg.source().tell(L("The panel containing @x1 is not activated or connected.",name()));
					return false;
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				break;
			case CMMsg.TYP_LOOK:
				break;
			case CMMsg.TYP_POWERCURRENT:
				if((!(this instanceof FuelConsumer))
				&&(!(this instanceof PowerGenerator))
				&& activated() && (powerNeeds()>0) && (msg.value()>0))
				{
					double amtToTake=Math.min((double)powerNeeds(), (double)msg.value());
					msg.setValue(msg.value()-(int)Math.round(amtToTake));
					amtToTake *= getFinalManufacturer().getEfficiencyPct();
					if(subjectToWearAndTear() && (usesRemaining()<=200))
						amtToTake *= CMath.div(usesRemaining(), 100.0);
					setPowerRemaining(Math.min(powerCapacity(), Math.round(amtToTake) + powerRemaining()));
				}
				break;
			}
		}
		return super.okMessage(host, msg);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, L("<S-NAME> activate(s) <T-NAME>."));
				this.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, L("<S-NAME> deactivate(s) <T-NAME>."));
				this.activate(false);
				break;
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
					msg.source().tell(L("@x1 is currently @x2",name(),(activated()?"connected.\n\r":"deactivated/disconnected.\n\r")));
				return;
			case CMMsg.TYP_REPAIR:
				if(CMLib.dice().rollPercentage()<msg.value())
				{
					setUsesRemaining(usesRemaining()<100?100:usesRemaining());
					msg.source().tell(L("@x1 is now repaired.\n\r",name()));
				}
				else
				{
					final int repairRequired=100-usesRemaining();
					if(repairRequired>0)
					{
						int repairApplied=(int)Math.round(CMath.mul(repairRequired, CMath.div(msg.value(), 100)));
						if(repairApplied < 0)
							repairApplied=1;
						setUsesRemaining(usesRemaining()+repairApplied);
						msg.source().tell(L("@x1 is now @x2% repaired.\n\r",name(),""+usesRemaining()));
					}
				}
				break;
			case CMMsg.TYP_ENHANCE:
				if((CMLib.dice().rollPercentage()<msg.value())&&(CMLib.dice().rollPercentage()<50))
				{
					float addAmt=0.01f;
					if(getInstalledFactor() < 1.0)
					{
						addAmt=(float)(CMath.div(100.0, msg.value()) * 0.1);
						if(addAmt < 0.1f)
							addAmt=0.1f;
					}
					setInstalledFactor(this.getInstalledFactor()+addAmt);
					msg.source().tell(msg.source(),this,null,L("<T-NAME> is now enhanced.\n\r"));
				}
				else
				{
					msg.source().tell(msg.source(),this,null,L("Your attempt to enhance <T-NAME> has failed.\n\r"));
				}
				break;
			}
		}
		super.executeMsg(host, msg);
	}
	
}
