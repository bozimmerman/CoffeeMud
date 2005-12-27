package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2006 Bo Zimmerman

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
public class GenFoodResource extends GenFood implements RawMaterial, Food
{
	public String ID(){	return "GenFoodResource";}
	protected boolean readyToSet=false;
	
	public GenFoodResource()
	{
		super();
		setName("an edible resource");
		setDisplayText("a pile of edible resource sits here.");
		setDescription("");
		material=RawMaterial.RESOURCE_BERRIES;
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
        super.executeMsg(host,msg);
        if((msg.target()==this)||((msg.target()==container())&&(container()!=null)))
        {
    	    if(((msg.tool() instanceof ShopKeeper)&&(msg.targetMinor()==CMMsg.TYP_GET))
            ||(msg.targetMinor()==CMMsg.TYP_ROOMRESET))
    	    {
    	        Ability A=fetchEffect("Poison_Rotten");
    	        if(A!=null) delEffect(A);
    	        decayTime=0;
    	        readyToSet=true;
    	    }
        }
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
    	if((readyToSet)
        &&(fetchEffect("Poison_Rotten")==null)
        &&(owner!=null))
    	{
    	    readyToSet=false;
	        decayTime=0;
		    switch(material&RawMaterial.MATERIAL_MASK)
		    {
		    case RawMaterial.MATERIAL_FLESH:
			    {
		        decayTime=System.currentTimeMillis()+(
		            	   Tickable.TIME_TICK
					        *CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)
					      );
		    	break;
			    }
		    case RawMaterial.MATERIAL_VEGETATION:
			    {
		        decayTime=System.currentTimeMillis()+(
		                Tickable.TIME_TICK
				        *CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)
				        *5);
			    break;
			    }
		    }
		    switch(material)
		    {
		    case RawMaterial.RESOURCE_HERBS:
		    case RawMaterial.RESOURCE_WAX:
		    case RawMaterial.RESOURCE_COFFEEBEANS:
		    case RawMaterial.RESOURCE_SEAWEED:
		    case RawMaterial.RESOURCE_SUGAR:
		    case RawMaterial.RESOURCE_COCOA:
		    case RawMaterial.RESOURCE_MUSHROOMS:
		    case RawMaterial.RESOURCE_VINE:
		    case RawMaterial.RESOURCE_FLOWERS:
		    case RawMaterial.RESOURCE_NUTS:
		    case RawMaterial.RESOURCE_CRACKER:
		    case RawMaterial.RESOURCE_PIPEWEED:
		    case RawMaterial.RESOURCE_GARLIC:
		    case RawMaterial.RESOURCE_SOAP:
		    case RawMaterial.RESOURCE_ASH:
		        decayTime=0;
		    	break;
		    }
    	}
	    if((decayTime>0)
        &&(!CMLib.flags().isABonusItems(this))
        &&(System.currentTimeMillis()>decayTime))
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
	protected int domainSource=-1;
	public int domainSource(){return domainSource;}
	public void setDomainSource(int src){domainSource=src;}
}
