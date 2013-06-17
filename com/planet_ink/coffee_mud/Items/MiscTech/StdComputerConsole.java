package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.Basic.StdRideable;
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
public class StdComputerConsole extends StdRideable 
	implements Electronics, ShipComponent, Electronics.ElecPanel
{
	public String ID(){	return "StdShipConsole";}
	public StdComputerConsole()
	{
		super();
		setName("a computer console");
		setDisplayText("a computer console is here");
		basePhyStats.setWeight(20);
		setDescription("");
		baseGoldValue=5;
		containType=Container.CONTAIN_SSCOMPONENTS;
		rideBasis=Rideable.RIDEABLE_TABLE;
		riderCapacity=1;
		basePhyStats.setSensesMask(basePhyStats.sensesMask()|PhyStats.SENSE_ITEMREADABLE);
		setLidsNLocks(true,true,false,false);
		capacity=500;
		material=RawMaterial.RESOURCE_STEEL;
		recoverPhyStats();
	}
	private volatile String circuitKey=null;

	public int fuelType(){return RawMaterial.RESOURCE_ENERGY;}
	public void setFuelType(int resource){}
	public long powerCapacity(){return 1;}
	public void setPowerCapacity(long capacity){}
	public long powerRemaining(){return 1;}
	public void setPowerRemaining(long remaining){}
	protected boolean activated=false;
	public boolean activated(){return activated;}
	public void activate(boolean truefalse){activated=truefalse;}
	
	protected ElecPanelType panelType=Electronics.ElecPanel.ElecPanelType.COMPUTER;
	public ElecPanelType panelType(){return panelType;}
	public void setPanelType(ElecPanelType type){panelType=type;}
	
	protected long nextSoftwareCheck=0;
	protected List<Software> software=null;
	
	protected String currentMenu="";
	
	public boolean canContain(Environmental E)
	{
		return E instanceof Software;
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(affected instanceof Room)
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_ROOMCIRCUITED);
	}

	protected List<Software> getSoftware()
	{
		if((software==null)||(nextSoftwareCheck==0)||(System.currentTimeMillis()>nextSoftwareCheck))
		{
			final List<Item> list=getContents();
			final LinkedList<Software> softwareList=new LinkedList<Software>();
			for(Item I : list)
				if(I instanceof Software)
					softwareList.add((Software)I);
			nextSoftwareCheck=System.currentTimeMillis()+(60*1000);
			software=softwareList;
		}
		return software;
	}
	
	public String readableText()
	{
		final StringBuilder str=new StringBuilder(super.readableText());
		if(str.length()>0) str.append("\n\r");
		if(!activated())
			str.append("The screen is blank.  Try ACTIVATEing it first.");
		else
		{
			final List<Software> software=getSoftware();
			for(final Software S : software)
				if(S.getInternalName().equals(currentMenu))
					str.append(S.readableText());
				else
				if(S.getParentMenu().equals(currentMenu))
					str.append(S.getActivationString()).append(": ").append(S.getActivationDescription()).append("\n\r");
		}
		
		return str.toString();
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_READ:
				return true;
			case CMMsg.TYP_ACTIVATE:
				if((msg.targetMessage()==null)&&(activated()))
				{
					msg.source().tell(name()+" is already booted up.");
					return false;
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.targetMessage()==null)&&(!activated()))
				{
					msg.source().tell(name()+" is already shut down.");
					return false;
				}
				break;
			}
		}
		return super.okMessage(host,msg);
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUT:
				nextSoftwareCheck=0;
				break;
			case CMMsg.TYP_ACTIVATE:
				if(!activated())
				{
					activate(true);
					currentMenu="";
					msg.source().location().show(msg.source(),this,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> boot(s) up <T-NAME>.");
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				if(activated())
				{
					activate(false);
					msg.source().location().show(msg.source(),this,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> shut(s) up <T-NAME>.");
				}
				break;
			case CMMsg.TYP_POWERCURRENT:
				if(activated())
				{
				}
				break;
			}
		}
		super.executeMsg(host,msg);
	}
	
	public void destroy()
	{
		if((!destroyed)&&(circuitKey!=null))
		{
			CMLib.tech().unregisterElectronics(this,circuitKey);
			circuitKey=null;
			CMLib.threads().deleteTick(this,Tickable.TICKID_ELECTRONICS);
		}
		super.destroy();
	}
	
	public void setOwner(ItemPossessor owner)
	{
		final ItemPossessor prevOwner=super.owner;
		super.setOwner(owner);
		if(prevOwner != owner)
		{
			if(owner instanceof Room)
			{
				if(!CMLib.threads().isTicking(this, Tickable.TICKID_ELECTRONICS))
					CMLib.threads().startTickDown(this, Tickable.TICKID_ELECTRONICS, 1);
				circuitKey=CMLib.tech().registerElectrics(this,circuitKey);
			}
			else
			{
				CMLib.threads().deleteTick(this,Tickable.TICKID_ELECTRONICS);
				CMLib.tech().unregisterElectronics(this,circuitKey);
				circuitKey=null;
			}
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(tickID==Tickable.TICKID_ELECTRONICS)
		{
			if(activated())
			{
			}
		}
		return true;
	}
}
