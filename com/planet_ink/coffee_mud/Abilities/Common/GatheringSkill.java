package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.File;

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

public class GatheringSkill extends CommonSkill
{
	public String ID() { return "GatheringSkill"; }
	public String name(){ return "GatheringSkill";}
	private static final String[] triggerStrings = {"FLETCH","FLETCHING"};
	public String[] triggerStrings(){return triggerStrings;}
	protected String supportedResourceString(){return "";}
	protected static final Hashtable supportedResources=new Hashtable();
	
	public Vector myresources()
	{
	    if(supportedResources.containsKey(ID()))
	        return (Vector)supportedResources.get(ID());
	    String mask=supportedResourceString();
	    Vector maskV=new Vector();
	    String str=mask;
	    while(mask.length()>0)
	    {
	        str=mask;
	        int x=mask.indexOf("|");
	        if(x>=0)
	        {
	            str=mask.substring(0,x);
	            mask=mask.substring(x+1);
	        }
	        else
	            mask="";
	        if(str.length()>0)
	        {
	            boolean found=false;
        		for(int i=0;i<EnvResource.MATERIAL_DESCS.length;i++)
        			if(EnvResource.MATERIAL_DESCS[i].equalsIgnoreCase(str))
        			{
        			    for(int ii=0;ii<EnvResource.RESOURCE_DATA.length;ii++)
        			        if((EnvResource.RESOURCE_DATA[ii][0]&EnvResource.MATERIAL_MASK)==(i<<8))
			                { 
        			            found=true; 
        			            maskV.addElement(new Integer(EnvResource.RESOURCE_DATA[ii][0]));
        			        }
        			    break;
        			}
	            if(!found)
	            for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
	                if(EnvResource.RESOURCE_DESCS[i].equalsIgnoreCase(str))
	                { 
		                maskV.addElement(new Integer(EnvResource.RESOURCE_DATA[i][0]));
		                break;
		            }
	        }
	    }
	    supportedResources.put(ID(),maskV);
	    return maskV;
	}
	
	public boolean bundle(MOB mob, Vector what)
	{
	    if((what.size()<3)
	    ||(!Util.isNumber((String)what.elementAt(1))))
	    {
	        commonTell(mob,"You must specify an amount to bundle, followed by what resource to bundle.");
	        return false;
	    }
	    int amount=Util.s_int((String)what.elementAt(1));
	    if(amount<=0)
	    {
	        commonTell(mob,amount+" is not an appropriate amount.");
	        return false;
	    }
	    int numHere=0;
	    Room R=mob.location();
	    if(R==null) return false;
	    String name=Util.combine(what,2);
	    int foundResource=-1;
	    Item foundAnyway=null;
	    Vector maskV=myresources();
	    for(int i=0;i<R.numItems();i++)
	    {
	        Item I=R.fetchItem(i);
			if(EnglishParser.containsString(I.Name(),name))
			{
			    foundAnyway=I;
				if((I instanceof EnvResource)
				&&(!Sense.isOnFire(I))
				&&(!Sense.enchanted(I))
				&&(I.container()==null)
				&&((I.material()==foundResource)||(maskV.contains(new Integer(I.material())))))
				{
				    foundResource=I.material();
				    numHere+=I.envStats().weight();
				}
			}
	    }
	    if(numHere==0)
	    {
	        if(foundAnyway!=null)
		        commonTell(mob,"You can't bundle "+foundAnyway.name()+" with this skill.");
	        else
		        commonTell(mob,"You don't see any "+name+" on the ground here.");
	        return false;
	    }
	    if(numHere<amount)
	    {
	        commonTell(mob,"You only see "+numHere+" pounds of "+name+" on the ground here.");
	        return false;
	    }
		Item I=(Item)CoffeeUtensils.makeResource(foundResource,mob.location().domainType(),true);
		I.setName("a "+amount+"# "+EnvResource.RESOURCE_DESCS[foundResource&EnvResource.RESOURCE_MASK].toLowerCase()+" bundle");
		I.setDisplayText(I.name()+" is here.");
		I.baseEnvStats().setWeight(amount);
		if(R.show(mob,null,I,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> create(s) <O-NAME>."))
		{
		    int lostValue=destroyResources(R,amount,foundResource,-1,null,0);
			I.setBaseValue(lostValue);
			if(I instanceof Food)
			    ((Food)I).setNourishment(((Food)I).nourishment()*amount);
			if(I instanceof Drink)
			    ((Drink)I).setLiquidHeld(((Drink)I).liquidHeld()*amount);
			R.addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
			R.recoverRoomStats();
		}
	    return true;
	}
	
	
}
