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

public class FireBuilding extends CommonSkill
{
	public String ID() { return "FireBuilding"; }
	public String name(){ return "Fire Building";}
	private static final String[] triggerStrings = {"LIGHT","FIREBUILD","FIREBUILDING"};
	public String[] triggerStrings(){return triggerStrings;}
    protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_FIRE;}

	public Item lighting=null;
	private int durationOfBurn=0;
	private boolean failed=false;

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping))
			{
				MOB mob=(MOB)affected;
				if(failed)
					commonTell(mob,"You failed to get the fire started.");
				else
				{
					if(lighting==null)
					{
						Item I=CMClass.getItem("GenItem");
						I.baseEnvStats().setWeight(50);
						I.setName("a roaring campfire");
						I.setDisplayText("A roaring campfire has been built here.");
						I.setDescription("It consists of dry wood, burning.");
						I.recoverEnvStats();
						I.setMaterial(EnvResource.RESOURCE_WOOD);
						mob.location().addItem(I);
						lighting=I;
					}
					Ability B=CMClass.getAbility("Burning");
					B.setProfficiency(durationOfBurn);
					B.invoke(mob,lighting,true,0);
				}
				lighting=null;
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.isMonster()
		&&(!Sense.isAnimalIntelligence(mob)))
		&&(commands.size()==0))
			commands.addElement("fire");

		if(commands.size()==0)
		{
			commonTell(mob,"Light what?  Try light fire, or light torch...");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		String name=Util.combine(commands,0);
		int profficiencyAdjustment=0;
		int completion=6;
		if(name.equalsIgnoreCase("fire"))
		{
			lighting=null;
			if((mob.location().domainType()&Room.INDOORS)>0)
			{
				commonTell(mob,"You can't seem to find any deadwood around here.");
				return false;
			}
			switch(mob.location().domainType())
			{
			case Room.DOMAIN_OUTDOORS_HILLS:
			case Room.DOMAIN_OUTDOORS_JUNGLE:
			case Room.DOMAIN_OUTDOORS_MOUNTAINS:
			case Room.DOMAIN_OUTDOORS_PLAINS:
			case Room.DOMAIN_OUTDOORS_WOODS:
				break;
			default:
				commonTell(mob,"You can't seem to find any dry deadwood around here.");
				return false;
			}
			completion=25-mob.envStats().level();
			durationOfBurn=150+(mob.envStats().level()*5);
			verb="building a fire";
			displayText="You are building a fire.";
		}
		else
		{
			lighting=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
			if(lighting==null) return false;
			if(lighting.displayText().length()==0)
			{
				commonTell(mob,"For some reason, "+lighting.name()+" just won't catch.");
				return false;
			}
			if(lighting instanceof Light)
			{
				Light l=(Light)lighting;
				if(l.isLit())
				{
					commonTell(mob,l.name()+" is already lit!");
					return false;
				}
				if(Sense.isGettable(lighting))
					commonTell(mob,"Just hold this item to light it.");
				else
				{
					l.light(true);
					mob.location().show(mob,lighting,CMMsg.TYP_HANDS,"<S-NAME> light(s) <T-NAMESELF>.");
					return true;
				}
				return false;
			}
			if(!(lighting instanceof EnvResource))
			{
				LandTitle t=CoffeeUtensils.getLandTitle(mob.location());
				if((t!=null)&&(!CoffeeUtensils.doesHavePriviledgesHere(mob,mob.location())))
				{
					mob.tell("You are not allowed to burn anything here.");
					return false;
				}
			}
			durationOfBurn=Sense.burnStatus(lighting);
			if(durationOfBurn<0)
			{
				commonTell(mob,"You need to cook that, if you can.");
				return false;
			}
			else
			if(durationOfBurn==0)
			{
				commonTell(mob,"That won't burn.");
				return false;
			}
			if((lighting.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
				completion=25-mob.envStats().level();
			verb="lighting "+lighting.name();
			displayText="You are lighting "+lighting.name()+".";
		}

		switch(mob.location().getArea().getClimateObj().weatherType(mob.location()))
		{
		case Climate.WEATHER_BLIZZARD:
		case Climate.WEATHER_SNOW:
		case Climate.WEATHER_THUNDERSTORM:
			profficiencyAdjustment=-80;
			break;
		case Climate.WEATHER_DROUGHT:
			profficiencyAdjustment=50;
			break;
		case Climate.WEATHER_DUSTSTORM:
		case Climate.WEATHER_WINDY:
			profficiencyAdjustment=-10;
			break;
		case Climate.WEATHER_HEAT_WAVE:
			profficiencyAdjustment=10;
			break;
		case Climate.WEATHER_RAIN:
		case Climate.WEATHER_SLEET:
		case Climate.WEATHER_HAIL:
			profficiencyAdjustment=-50;
			break;
		}
		failed=!profficiencyCheck(mob,profficiencyAdjustment,auto);

		durationOfBurn=durationOfBurn*abilityCode();
		if(completion<4) completion=4;

		FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> start(s) building a fire.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,completion);
		}
		return true;
	}
}
