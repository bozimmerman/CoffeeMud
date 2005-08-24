package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.Basic.StdRideable;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class StdShipConsole extends StdRideable 
	implements Electronics, ShipComponent, ShipComponent.ShipPanel
{
	public String ID(){	return "StdShipConsole";}
	public StdShipConsole()
	{
		super();
		setName("a computer console");
		baseEnvStats.setWeight(20);
		setDescription("");
		baseGoldValue=5;
		containType=Container.CONTAIN_SSCOMPONENTS;
		rideBasis=Rideable.RIDEABLE_TABLE;
		riderCapacity=1;
		setLidsNLocks(true,true,false,false);
		capacity=500;
		material=EnvResource.RESOURCE_STEEL;
		recoverEnvStats();
	}

	public int fuelType(){return EnvResource.RESOURCE_ENERGY;}
	public void setFuelType(int resource){}
	public long powerCapacity(){return 1;}
	public void setPowerCapacity(long capacity){}
	public long powerRemaining(){return 1;}
	public void setPowerRemaining(long remaining){}
	protected boolean activated=false;
	public boolean activated(){return activated;}
	public void activate(boolean truefalse){activated=truefalse;}
	
	private int panelType=ShipComponent.ShipPanel.COMPONENT_PANEL_COMPUTER;
	public int panelType(){return panelType;}
	public void setPanelType(int type){panelType=type;}
	
	public boolean canContain(Environmental E)
	{
		return E instanceof Software;
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
	        case CMMsg.TYP_READ:
		    {
		        if(!activated())
		        {
		            msg.source().tell("The screen is blank.  Try ACTIVATEing it first.");
		        }
		        else
		        {
		        }
		        return;
		    }
	        case CMMsg.TYP_ACTIVATE:
	            if(!activated())
	            {
	                activate(true);
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
	        }
	    }
	    super.executeMsg(host,msg);
	}
}
