package com.planet_ink.coffee_mud.Items.Basic;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class GenFoodResource extends GenFood implements EnvResource, Food
{
	public String ID(){	return "GenFoodResource";}
	public GenFoodResource()
	{
		super();
		setName("an edible resource");
		setDisplayText("a pile of edible resource sits here.");
		setDescription("");
		setMaterial(EnvResource.RESOURCE_BERRIES);
		setNourishment(200);
		baseEnvStats().setWeight(0);
		recoverEnvStats();
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
	    if((decayTime>0)&&(System.currentTimeMillis()>decayTime))
	    {
            if(fetchEffect("Poison_Rotten")==null)
            {
                Ability A=CMClass.getAbility("Poison_Rotten");
                if(A!=null) this.addNonUninvokableEffect(A);
            }
            setNourishment(0);
	        decayTime=0;
	    }
	    return super.okMessage(host,msg);
	}

	public void setMaterial(int newValue)
	{
	    super.setMaterial(newValue);
    	if(fetchEffect("Poison_Rotten")==null)
    	{
	        decayTime=0;
		    switch(newValue&EnvResource.MATERIAL_MASK)
		    {
		    case EnvResource.MATERIAL_FLESH:
			    {
		        decayTime=System.currentTimeMillis()+(
		            	   MudHost.TICK_TIME
		        		  *CommonStrings.SYSTEMI_TICKSPERMUDDAY);
		    	break;
			    }
		    case EnvResource.MATERIAL_VEGETATION:
			    {
		        decayTime=System.currentTimeMillis()+(
		                MudHost.TICK_TIME
				        *CommonStrings.SYSTEMI_TICKSPERMUDDAY
				        *DefaultTimeClock.globalClock.getDaysInWeek());
			    break;
			    }
		    }
		    switch(newValue)
		    {
		    case EnvResource.RESOURCE_HERBS:
		    case EnvResource.RESOURCE_WAX:
		    case EnvResource.RESOURCE_COFFEEBEANS:
		    case EnvResource.RESOURCE_SEAWEED:
		    case EnvResource.RESOURCE_SUGAR:
		    case EnvResource.RESOURCE_COCOA:
		    case EnvResource.RESOURCE_MUSHROOMS:
		    case EnvResource.RESOURCE_VINE:
		    case EnvResource.RESOURCE_FLOWERS:
		    case EnvResource.RESOURCE_NUTS:
		    case EnvResource.RESOURCE_CRACKER:
		    case EnvResource.RESOURCE_PIPEWEED:
		    case EnvResource.RESOURCE_GARLIC:
		    case EnvResource.RESOURCE_SOAP:
		    case EnvResource.RESOURCE_ASH:
		        decayTime=0;
		    	break;
		    }
    	}
	}
	private int domainSource=-1;
	public int domainSource(){return domainSource;}
	public void setDomainSource(int src){domainSource=src;}
}
