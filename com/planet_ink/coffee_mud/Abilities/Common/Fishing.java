package com.planet_ink.coffee_mud.Abilities.Common;
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

public class Fishing extends CommonSkill
{
	public String ID() { return "Fishing"; }
	public String name(){ return "Fishing";}
	private static final String[] triggerStrings = {"FISH"};
	public String[] triggerStrings(){return triggerStrings;}
	public long flags(){return FLAG_GATHERING;}

	private Item found=null;
	private String foundShortName="";
	public Fishing()
	{
		super();
		displayText="You are fishing...";
		verb="fishing";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(found!=null)
					commonTell(mob,"You got a tug on the line!");
				else
				{
					StringBuffer str=new StringBuffer("Nothing is biting around here.\n\r");
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
				if((found!=null)&&(!aborted)&&(!helping))
				{
					int amount=Dice.roll(1,5,0)*(abilityCode());
					String s="s";
					if(amount==1) s="";
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to catch "+amount+" pound"+s+" of "+foundShortName+".");
					for(int i=0;i<amount;i++)
					{
						Item newFound=(Item)found.copyOf();
						mob.location().addItemRefuse(newFound,Item.REFUSE_PLAYER_DROP);
						if((mob.riding()!=null)&&(mob.riding() instanceof Container))
							newFound.setContainer((Container)mob.riding());
						CommonMsgs.get(mob,null,newFound,true);
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		int foundFish=-1;
		boolean maybeFish=false;
		if(mob.location()!=null)
		{
			for(int i=0;i<EnvResource.FISHES.length;i++)
				if(mob.location().myResource()==EnvResource.FISHES[i])
				{
					foundFish=EnvResource.FISHES[i];
					maybeFish=true;
				}
				else
				if((mob.location().resourceChoices()!=null)
				&&(mob.location().resourceChoices().contains(new Integer(EnvResource.FISHES[i]))))
					maybeFish=true;
		}
		if(!maybeFish)
		{
			commonTell(mob,"This fishing doesn't look too good around here.");
			return false;
		}
		verb="fishing";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		if((profficiencyCheck(mob,0,auto))
		   &&(foundFish>0))
		{
			found=(Item)CoffeeUtensils.makeResource(foundFish,mob.location().domainType(),false);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=35-mob.envStats().level();
		if(duration<10) duration=10;
		FullMsg msg=new FullMsg(mob,found,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) fishing.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}