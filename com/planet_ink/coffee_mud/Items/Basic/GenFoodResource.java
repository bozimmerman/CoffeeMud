package com.planet_ink.coffee_mud.Items.Basic;


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
public class GenFoodResource extends GenFood implements EnvResource, Food
{
	public String ID(){	return "GenFoodResource";}
	private boolean readyToSet=false;
	
	public GenFoodResource()
	{
		super();
		setName("an edible resource");
		setDisplayText("a pile of edible resource sits here.");
		setDescription("");
		material=EnvResource.RESOURCE_BERRIES;
		setNourishment(200);
		baseEnvStats().setWeight(0);
		recoverEnvStats();
		decayTime=0;
	}
	
	public void setMaterial(int newValue)
	{
	    super.setMaterial(newValue);
	    readyToSet=true;
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
	    if((msg.tool() instanceof ShopKeeper)
	    &&(msg.targetMinor()==CMMsg.TYP_GET)
	    &&(msg.target()!=null)
	    &&((msg.target()==this)||(msg.target()==container())))
	    {
	        Ability A=fetchEffect("Poison_Rotten");
	        if(A!=null) delEffect(A);
	        decayTime=0;
	        readyToSet=true;
	    }
	    super.executeMsg(host,msg);
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
    	if((readyToSet)
        &&(fetchEffect("Poison_Rotten")==null)
        &&(owner!=null))
    	{
    	    readyToSet=false;
	        decayTime=0;
		    switch(material&EnvResource.MATERIAL_MASK)
		    {
		    case EnvResource.MATERIAL_FLESH:
			    {
		        decayTime=System.currentTimeMillis()+(
		            	   MudHost.TICK_TIME
					        *CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)
					      );
		    	break;
			    }
		    case EnvResource.MATERIAL_VEGETATION:
			    {
		        decayTime=System.currentTimeMillis()+(
		                MudHost.TICK_TIME
				        *CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)
				        *5);
			    break;
			    }
		    }
		    switch(material)
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
	private int domainSource=-1;
	public int domainSource(){return domainSource;}
	public void setDomainSource(int src){domainSource=src;}
}
