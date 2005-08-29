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

public class Farming extends GatheringSkill
{
	public String ID() { return "Farming"; }
	public String name(){ return "Farming";}
	private static final String[] triggerStrings = {"PLANT","FARM","FARMING"};
	public String[] triggerStrings(){return triggerStrings;}
	public long flags(){return FLAG_GATHERING;}
	protected boolean allowedWhileMounted(){return false;}
	public String supportedResourceString(){return "VEGETATION|COTTON|HEMP|WOODEN";}

	private Item found=null;
	private Room room=null;
	private String foundShortName="";
	public Farming()
	{
		super();
		displayText="You are planting...";
		verb="planting";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			MOB mob=invoker();
			if(tickUp==6)
			{
				if(found==null)
				{
					commonTell(mob,"Your "+foundShortName+" crop has failed.\n\r");
					unInvoke();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		boolean isaborted=aborted;
		Environmental aff=affected;
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected==room))
			{
				if((found!=null)&&(!isaborted))
				{
					int amount=Dice.roll(1,20,0)*(abilityCode());
					String s="s";
					if(amount==1) s="";
					room.showHappens(CMMsg.MSG_OK_VISUAL,amount+" pound"+s+" of "+foundShortName+" have grown here.");
					for(int i=0;i<amount;i++)
					{
						Item newFound=(Item)found.copyOf();
						room.addItemRefuse(newFound,Item.REFUSE_PLAYER_DROP);
					}
				}
			}
		}
		super.unInvoke();
		if((canBeUninvoked)
		   &&(aff!=null)
		   &&(aff instanceof MOB)
		   &&(aff!=room)
		   &&(!isaborted)
		   &&(room!=null))
		{
			Farming F=((Farming)copyOf());
			F.unInvoked=false;
			F.tickUp=0;
			F.tickDown=50;
			F.startTickDown(invoker,room,50);
		}
	}

	public boolean isPotentialCrop(Room R, int code)
	{
		if(R==null) return false;
		if(R.resourceChoices()==null) return false;
		for(int i=0;i<R.resourceChoices().size();i++)
			if(((Integer)R.resourceChoices().elementAt(i)).intValue()==code)
				return true;
		return false;
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
		
		verb="planting";
		if((!auto)&&((mob.location().domainType()&Room.INDOORS)>0))
		{
			commonTell(mob,"You can't plant anything indoors!");
			return false;
		}
        if((!auto)&&(mob.location().getArea().getClimateObj().weatherType(mob.location())==Climate.WEATHER_DROUGHT))
        {
            commonTell(mob,"The current drought conditions make planting useless.");
            return false;
        }
		if(mob.location().fetchEffect("Farming")!=null)
		{
			commonTell(mob,"It looks like a crop is already growing here.");
			return false;
		}
		if(mob.isMonster()
        &&(!auto)
		&&(!Sense.isAnimalIntelligence(mob))
		&&(commands.size()==0))
		{
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I2=mob.fetchInventory(i);
				if((I2!=null)
				&&(I2 instanceof EnvResource)
				&&(Sense.canBeSeenBy(I2,mob))
				&&(I2.container()==null)
				&&(((I2.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION)
					||(I2.material()==EnvResource.RESOURCE_COTTON)
					||(I2.material()==EnvResource.RESOURCE_HEMP)
					||((I2.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)))
				{
					commands.addElement(EnvResource.RESOURCE_DESCS[I2.material()&EnvResource.RESOURCE_MASK]);
					break;
				}
				if(commands.size()==0)
				{
					commonTell(mob,"You don't have anything you can plant.");
					return false;
				}
			}
		}
		else
		if(commands.size()==0)
		{
			commonTell(mob,"Grow what?");
			return false;
		}
		int code=-1;
		for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
		{
			String str=EnvResource.RESOURCE_DESCS[i];
			if((str.toUpperCase().equalsIgnoreCase(Util.combine(commands,0)))
			&&(((EnvResource.RESOURCE_DATA[i][0]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION)
			  ||(EnvResource.RESOURCE_DATA[i][0]==EnvResource.RESOURCE_COTTON)
			  ||(EnvResource.RESOURCE_DATA[i][0]==EnvResource.RESOURCE_HEMP)
			  ||((EnvResource.RESOURCE_DATA[i][0]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)))
			{
				code=EnvResource.RESOURCE_DATA[i][0];
				foundShortName=Util.capitalizeAndLower(str);
				break;
			}
		}
		if(code<0)
		{
			commonTell(mob,"You don't know how to plant "+Util.combine(commands,0));
			return false;
		}

		Item mine=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)&&(I instanceof EnvResource)&&(I.material()==code))
			{ mine=I; break;}
		}
		if(mine==null)
		{
			commonTell(mob,"You'll need to have some "+foundShortName+" to seed from on the ground first.");
			return false;
		}
        mine=(Item)CoffeeUtensils.unbundle(mine);
        if(mine==null)
        {
            commonTell(mob,mine.name()+" is not suitable for use as a seed crop.");
            return false;
        }
        
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((profficiencyCheck(mob,0,auto))&&(isPotentialCrop(mob.location(),code)))
		{
			found=(Item)CoffeeUtensils.makeResource(code,mob.location().domainType(),false);
			if((found!=null)
			&&(found.material()==EnvResource.RESOURCE_HERBS)
			&&(mine!=null)
			&&(mine.material()==found.material()))
			{
				found.setName(mine.name());
				found.setDisplayText(mine.displayText());
				found.setDescription(mine.description());
				found.text();
			}
		}
        
		mine.destroy();
		int duration=45-mob.envStats().level();
		if(duration<25) duration=25;
		FullMsg msg=new FullMsg(mob,found,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) planting "+foundShortName+".");
		verb="planting "+foundShortName;
		displayText="You are planting "+foundShortName;
		room=mob.location();
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
