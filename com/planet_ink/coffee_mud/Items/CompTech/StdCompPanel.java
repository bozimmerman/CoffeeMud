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
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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
public class StdCompPanel extends StdElecCompContainer implements ElecPanel, TechComponent
{
	@Override
	public String ID()
	{
		return "StdCompPanel";
	}

	protected volatile int		powerNeeds	= 0;
	protected volatile String	circuitKey	= null;

	public StdCompPanel()
	{
		super();
		setName("an engineering panel");
		setDisplayText("an engineering panel is on the wall");
		setDescription("Usually seemless with the wall, these panels can be opened to install new equipment.");
		super.setDoorsNLocks(true, true, true,false, false,false);
		basePhyStats().setSensesMask(basePhyStats().sensesMask()|PhyStats.SENSE_ALWAYSCOMPRESSED|PhyStats.SENSE_INSIDEACCESSIBLE|PhyStats.SENSE_ITEMNOTGET);
		this.activated=true;
		this.openDelayTicks=0;
		this.recoverPhyStats();
	}

	@Override
	public TechType getTechType()
	{
		return TechType.SHIP_PANEL;
	}

	protected TechType	panelType	= TechType.ANY;

	@Override
	public TechType panelType()
	{
		return panelType;
	}

	@Override
	public void setPanelType(TechType type)
	{
		panelType = type;
	}

	@Override
	public int powerNeeds()
	{
		return powerNeeds;
	}

	@Override
	public boolean canContain(Item I)
	{
		if(!super.canContain(I))
			return false;
		if((I instanceof Technical)&&(panelType()==((Technical)I).getTechType()))
			return true;
		return false;
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
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_PUT:
				if(CMSecurity.isAllowed(msg.source(), msg.source().location(), CMSecurity.SecFlag.CMDITEMS))
					break; // let admins go ahead and put stuff
				else
				if(msg.tool() instanceof TechComponent)
				{
					if(msg.value()<=0)
					{
						final Ability installA=msg.source().fetchAbility("AstroEngineering");
						if(installA==null)
						{
							msg.source().tell(L("You don't know how to install @x1 into @x2.",((TechComponent)msg.tool()).name(msg.source()),name(msg.source())));
							return false;
						}
						else
						{
							installA.invoke(msg.source(),new XVector<String>("INSTALL",msg.tool().Name()), (Physical)msg.target(), false, 0);
							return false;
						}
					}
				}
				break;
			case CMMsg.TYP_INSTALL:
				if(msg.value()<=0)
				{
					msg.source().tell(L("You failed to install @x1 into @x2.",((TechComponent)msg.tool()).name(msg.source()),name(msg.source())));
					return false;
				}
				break;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_PUT:
				if(msg.tool() instanceof TechComponent)
					((TechComponent)msg.tool()).setInstalledFactor(CMSecurity.isAllowed(msg.source(), msg.source().location(), CMSecurity.SecFlag.CMDITEMS)?1.0f:0.0f);
				break;
			case CMMsg.TYP_INSTALL:
				if((msg.tool() instanceof TechComponent)&&(msg.value()>=0))
				{
					if(msg.value()<=0)
						((TechComponent)msg.tool()).setInstalledFactor((float)1.0);
					else
						((TechComponent)msg.tool()).setInstalledFactor((float)CMath.div(msg.value(), 100.0));
					CMMsg msg2=(CMMsg)msg.copyOf();
					msg2.setTargetCode(CMMsg.MSG_PUT);
					msg2.setSourceCode(CMMsg.MSG_PUT);
					msg2.setOthersCode(CMMsg.MSG_PUT);
					super.executeMsg(myHost, msg2);
				}
				break;
			case CMMsg.TYP_ACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, L("<S-NAME> connect(s) <T-NAME>."));
				this.activate(true);
				return; // don't let comp container do its thing
			case CMMsg.TYP_DEACTIVATE:
			{
				final Room locR=CMLib.map().roomLocation(this);
				final MOB M=CMLib.map().getFactoryMOB(locR);
				final CMMsg deactivateMsg = CMClass.getMsg(M, null, null, CMMsg.MASK_ALWAYS|CMMsg.MASK_CNTRLMSG|CMMsg.MSG_DEACTIVATE,null);
				for(final Item I : this.getDeepContents())
				{
					if(I instanceof Electronics)
					{
						deactivateMsg.setTarget(I);
						if(locR.okMessage(M, deactivateMsg))
							locR.send(M, deactivateMsg);
					}
				}
				return; // don't let comp container do its thing
			}
			case CMMsg.TYP_POWERCURRENT:
			{
				final Room R=CMLib.map().roomLocation(this);
				int powerRemaining=msg.value();
				final List<Item> contents=getDeepContents();
				final CMMsg powerMsg=CMClass.getMsg(msg.source(), CMMsg.MSG_POWERCURRENT, null);
				double totalPowerReq=0.0;
				for(int i=contents.size()-1;i>=0;i--)
				{
					final Item I=contents.get(i);
					if((I instanceof Electronics)&&(!(I instanceof PowerSource))&&(!(I instanceof PowerGenerator)))
						totalPowerReq+=((((Electronics)I).powerNeeds()<=0)?1.0:((Electronics)I).powerNeeds());
				}
				for(int i=contents.size()-1;i>=0;i--)
				{
					final Item I=contents.get(i);
					if((I instanceof Electronics)&&(!(I instanceof PowerSource))&&(!(I instanceof PowerGenerator)))
					{
						int powerToTake=0;
						if(powerRemaining>0)
						{
							final double pctToTake=CMath.div(((((Electronics)I).powerNeeds()<=0)?1:((Electronics)I).powerNeeds()),totalPowerReq);
							powerToTake=(int)Math.round(pctToTake * powerRemaining);
							if(powerToTake<1)
								powerToTake=1;
						}
						powerMsg.setValue(powerToTake);
						powerMsg.setTarget(I);
						if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
							R.send(powerMsg.source(), powerMsg);
						powerRemaining-=(powerMsg.value()<0)?powerToTake:(powerToTake-powerMsg.value());
					}
				}
				powerNeeds=(int)Math.round(totalPowerReq);
				CMClass.returnMsg(powerMsg);
				msg.setValue(powerRemaining);
				return; // don't let comp container do its thing
			}
			}
			super.executeMsg(myHost, msg);
		}
	}
	
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdCompPanel))
			return false;
		return super.sameAs(E);
	}
}
