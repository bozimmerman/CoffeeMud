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

public class Drilling extends CommonSkill
{
	public String ID() { return "Drilling"; }
	public String name(){ return "Drilling";}
	private static final String[] triggerStrings = {"DRILL","DRILLING"};
	public String[] triggerStrings(){return triggerStrings;}
	public long flags(){return FLAG_GATHERING;}
	protected boolean allowedWhileMounted(){return false;}
	protected String supportedResourceString(){return "LIQUID";}

	private Item found=null;
	private Drink container=null;
	private String foundShortName="";
	public Drilling()
	{
		super();
		displayText="You are drilling...";
		verb="drilling";
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
					commonTell(mob,"You have found some "+foundShortName+"!");
					displayText="You are drilling out some "+foundShortName;
					verb="drilling out some "+foundShortName;
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find anything worth drilling around here.\n\r");
					int d=lookingFor(EnvResource.MATERIAL_LIQUID,mob.location());
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
					int amount=((found.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_CLOTH)?
							   (Dice.roll(1,30,0)*(abilityCode())):
							   (Dice.roll(1,5,0)*(abilityCode()));
					String s="s";
					if(amount==1) s="";
					if(amount>(container.liquidHeld()-container.liquidRemaining()))
						amount=(container.liquidHeld()-container.liquidRemaining());
					if(amount>((Container)container).capacity())
						amount=((Container)container).capacity();
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to drill out "+amount+" pound"+s+" of "+foundShortName+".");
					for(int i=0;i<amount;i++)
					{
						Item newFound=(Item)found.copyOf();
						mob.location().addItemRefuse(newFound,Item.REFUSE_PLAYER_DROP);
						if((container!=null)
						&&(mob.isMine(container))
						&&(container instanceof Container))
						{
							CommonMsgs.get(mob,null,newFound,true);
							if(mob.isMine(newFound))
								newFound.setContainer((Container)container);
						}
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item I=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(I==null) return false;
		if((!(I instanceof Container))||(((Container)I).capacity()<=((Container)I).envStats().weight()))
		{
			commonTell(mob,I.name()+" doesn't look like it can hold anything.");
			return false;
		}
		Vector V=((Container)I).getContents();
		for(int v=0;v<V.size();v++)
		{
			Item I2=(Item)V.elementAt(v);
			if((I2.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
			{
				commonTell(mob,I.name()+" needs to have the "+I2.name()+" removed first.");
				return false;
			}
		}
		if((!(I instanceof Drink))||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID))
		{
			commonTell(mob,I.name()+" doesn't look like it can hold a liquid.");
			return false;
		}
		if(((Drink)I).containsDrink())
		{
			commonTell(mob,"You need to empty "+I.name()+" first.");
			return false;
		}

		verb="drilling";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int resourceType=mob.location().myResource();
		if((profficiencyCheck(mob,0,auto))
		   &&(((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)))
		{
			found=(Item)CoffeeUtensils.makeResource(resourceType,mob.location().domainType(),false);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=35-mob.envStats().level();
		if(duration<10) duration=10;
		FullMsg msg=new FullMsg(mob,found,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) drilling.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			container=(Drink)I;
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
