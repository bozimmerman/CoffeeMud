package com.planet_ink.coffee_mud.Abilities.Common;
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

public class Mining extends GatheringSkill
{
	public String ID() { return "Mining"; }
	public String name(){ return "Mining";}
	private static final String[] triggerStrings = {"MINE","MINING"};
	public String[] triggerStrings(){return triggerStrings;}
	public long flags(){return FLAG_GATHERING;}
	protected boolean allowedWhileMounted(){return false;}
	protected String supportedResourceString(){return "GLASS|PRECIOUS|SAND|ROCK|METAL|MITHRIL";}

	private Item found=null;
	private String foundShortName="";
	public Mining()
	{
		super();
		displayText="You are mining...";
		verb="mining";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(found!=null)
				{
					commonTell(mob,"You have found a vein of "+foundShortName+"!");
					displayText="You are mining "+foundShortName;
					verb="mining "+foundShortName;
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find anything worth mining here.\n\r");
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((found!=null)&&(!aborted))
				{
					int amount=Dice.roll(1,10,0);
					if(((found.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_ROCK)
					&&(found.material()!=EnvResource.RESOURCE_COAL))
						amount=Dice.roll(1,85,0);
					amount=amount*(abilityCode());
					String s="s";
					if(amount==1) s="";
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to mine "+amount+" pound"+s+" of "+foundShortName+".");
					for(int i=0;i<amount;i++)
					{
						Item newFound=(Item)found.copyOf();
						mob.location().addItemRefuse(newFound,Item.REFUSE_RESOURCE);
						//CommonMsgs.get(mob,null,newFound,true);
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
		{
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
			    return super.bundle(mob,commands);
			else
			    return false;
		}
		verb="mining";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int resourceType=mob.location().myResource();
		if((profficiencyCheck(mob,0,auto))
		   &&((resourceType&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PRECIOUS)
		   &&((resourceType&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_GLASS)
		   &&(resourceType!=EnvResource.RESOURCE_SAND)
		   &&(((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_ROCK)
		   ||((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
		   ||((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL)))
		{
			found=(Item)CoffeeUtensils.makeResource(resourceType,mob.location().domainType(),false);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=50-mob.envStats().level();
		if(duration<15) duration=15;
		FullMsg msg=new FullMsg(mob,found,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) mining.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
