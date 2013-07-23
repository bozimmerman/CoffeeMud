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
public class StdShipThruster extends StdElecContainer implements ShipComponent.ShipEngine
{
	public String ID(){	return "StdShipThruster";}
	public StdShipThruster()
	{
		super();
		setName("a thruster engine");
		basePhyStats.setWeight(500);
		setDisplayText("a thruster engine sits here.");
		setDescription("");
		baseGoldValue=500000;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		setFuelType(RawMaterial.RESOURCE_PLASMA);
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdShipThruster)) return false;
		return super.sameAs(E);
	}
	protected int maxThrust=1000;
	public int getMaxThrust(){return maxThrust;}
	public void setMaxThrust(int max){maxThrust=max;}
	protected int thrust=1000;
	public int getThrust(){return thrust;}
	public void setThrust(int current){thrust=current;}
	
	private volatile String circuitKey=null;
	
	protected int   generatedAmtPerTick = 1;
	protected int[] generatedFuelTypes  = new int[]{RawMaterial.RESOURCE_DEUTERIUM};
	protected int   ticksPerFuelConsume = 10;
	protected volatile int fuelTickDown	= 0;
	
	@Override
	public long containTypes(){return Container.CONTAIN_RAWMATERIALS;}
	@Override
	public void setContainTypes(long containTypes){containType=CONTAIN_RAWMATERIALS;}
	@Override
	public int getTicksPerFuelConsume() { return ticksPerFuelConsume; }
	@Override
	public void getTicksPerFuelConsume(int tick) { ticksPerFuelConsume=tick; }
	@Override
    public int[] getConsumedFuelTypes() { return generatedFuelTypes; }
	@Override
    public void setConsumedFuelType(int[] resources) { 
		generatedFuelTypes = resources;
    }
	
	@Override
	public boolean canContain(Environmental E)
	{
		if(!super.canContain(E)) return false;
		if(E instanceof RawMaterial)
			return CMParms.contains(this.getConsumedFuelTypes(), ((RawMaterial)E).material());
		return false;
	}
	
	public void destroy()
	{
		if((!destroyed)&&(circuitKey!=null))
		{
			CMLib.tech().unregisterElectronics(this,circuitKey);
			circuitKey=null;
		}
		CMLib.threads().deleteTick(this,Tickable.TICKID_ELECTRONICS);
		super.destroy();
	}
	
	public void setOwner(ItemPossessor newOwner)
	{
		final ItemPossessor prevOwner=super.owner;
		super.setOwner(newOwner);
		if(prevOwner != newOwner)
		{
			if(newOwner instanceof Room)
			{
				if(!CMLib.threads().isTicking(this, Tickable.TICKID_ELECTRONICS))
					CMLib.threads().startTickDown(this, Tickable.TICKID_ELECTRONICS, 1);
				circuitKey=CMLib.tech().registerElectrics(this,circuitKey);
			}
			else
			{
				CMLib.tech().unregisterElectronics(this,circuitKey);
				circuitKey=null;
				CMLib.threads().deleteTick(this,Tickable.TICKID_ELECTRONICS);
			}
		}
	}
	
	protected void engineShutdown()
	{
		MOB deity=CMLib.map().deity();
		CMMsg msg=CMClass.getMsg(CMLib.map().deity(), CMMsg.MSG_DEACTIVATE, "<T-NAME> sputters and shuts itself down.");
		Room R=CMLib.map().roomLocation(this);
		if((R!=null)&&(R.okMessage(deity, msg)))
			R.send(deity, msg);
	}

	protected volatile List<Item> fuelCache=null;
	protected synchronized List<Item> getFuel()
	{
		if(fuelCache==null)
		{
			fuelCache=getContents();
		}
		return fuelCache;
	}
	protected synchronized void clearFuelCache()
	{
		fuelCache=null;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(tickID==Tickable.TICKID_ELECTRONICS)
		{
			if(activated())
			{
				if(fuelTickDown <= 0)
				{
					fuelTickDown=getTicksPerFuelConsume();
					boolean consumedFuel=false;
					List<Item> fuel=getFuel();
					for(Item I : fuel)
					{
						if((I instanceof RawMaterial)
						&&(!I.amDestroyed())
						&&CMParms.contains(this.getConsumedFuelTypes(), ((RawMaterial)I).material()))
						{
							CMLib.materials().destroyResources(fuel, 1, ((RawMaterial)I).material(), -1, null, this);
							consumedFuel=true;
							break;
						}
					}
					if(!consumedFuel)
						engineShutdown();
				}
			}
		}
		return true;
	}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
				clearFuelCache();
				break;
			case CMMsg.TYP_PUT:
				clearFuelCache();
				break;
			case CMMsg.TYP_ACTIVATE:
				this.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				this.activate(false);
				break;
			case CMMsg.TYP_LOOK:
				super.executeMsg(myHost, msg);
				return;
			case CMMsg.TYP_POWERCURRENT:
				break;
			}
		}
	}
}
