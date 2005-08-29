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

public class Chopping extends GatheringSkill
{
	public String ID() { return "Chopping"; }
	public String name(){ return "Wood Chopping";}
	private static final String[] triggerStrings = {"CHOP","CHOPPING"};
	public String[] triggerStrings(){return triggerStrings;}
	public long flags(){return FLAG_GATHERING;}
	protected boolean allowedWhileMounted(){return false;}
	public String supportedResourceString(){return "WOODEN";}

	private Item found=null;
	private String foundShortName="";
	public Chopping()
	{
		super();
		displayText="You are looking for a good tree...";
		verb="looking";
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
					commonTell(mob,"You have a good tree for "+foundShortName+".");
					displayText="You are chopping up "+foundShortName;
					verb="chopping "+foundShortName;
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find any trees worth cutting around here.\n\r");
					int d=lookingFor(EnvResource.MATERIAL_WOODEN,mob.location());
					if(d<0)
						str.append("You might try elsewhere.");
					else
						str.append("You might try "+Directions.getInDirectionName(d)+".");
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
					int amount=Dice.roll(1,20,10)*(abilityCode());
					String s="s";
					if(amount==1) s="";
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to chop up "+amount+" pound"+s+" of "+foundShortName+".");
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
        bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
		{
            bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
			    return super.bundle(mob,commands);
		    return false;
		}
		
		verb="chopping";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int resourceType=mob.location().myResource();
		if((profficiencyCheck(mob,0,auto))
		   &&((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN))
		{
			found=(Item)CoffeeUtensils.makeResource(resourceType,mob.location().domainType(),false);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=40-mob.envStats().level();
		if(duration<15) duration=15;
		FullMsg msg=new FullMsg(mob,found,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) looking for a good tree to chop.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
