package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class FireBuilding extends CommonSkill
{
	public Item lighting=null;
	private int durationOfBurn=0;
	private boolean failed=false;
	public FireBuilding()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Fire Building";

		displayText="You building a fire";
		verb="building a fire";
		miscText="";
		triggerStrings.addElement("LIGHT");
		triggerStrings.addElement("FIREBUILD");
		triggerStrings.addElement("FIREBUILDING");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}
	
	public Environmental newInstance()
	{
		return new FireBuilding();
	}
	
	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(failed)
				mob.tell("You failed to get the fire started.");
			else
			{
				if(lighting==null)
				{
					Item I=CMClass.getItem("GenItem");
					I.baseEnvStats().setWeight(50);
					I.setName("a roaring campire");
					I.setDisplayText("A roaring campire has been built here.");
					I.setDescription("It consists of dry wood, burning.");
					I.recoverEnvStats();
					I.setMaterial(EnvResource.RESOURCE_WOOD);
					mob.location().addItem(I);
					lighting=I;
				}
				Ability B=CMClass.getAbility("Burning");
				B.setProfficiency(durationOfBurn);
				B.invoke(mob,lighting,true);
			}
			lighting=null;
		}
		super.unInvoke();
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			mob.tell("Light what?  Try light fire, or light torch...");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		
		String name=Util.combine(commands,0);
		int profficiencyAdjustment=0;
		int completion=6;
		if(name.equalsIgnoreCase("fire"))
		{
			lighting=null;
			if((mob.location().domainType()&Room.INDOORS)>0)
			{
				mob.tell("You can't seem to find any deadwood around here.");
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
				mob.tell("You can't seem to find any dry deadwood around here.");
				return false;
			}
			completion=25-mob.envStats().level();
			durationOfBurn=50+mob.envStats().level();
			verb="building a fire";
		}
		else
		{
			lighting=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
			if(lighting==null) return false;
			if((!lighting.isGettable())||(lighting.displayText().length()==0))
			{
				mob.tell("For some reason, "+lighting.name()+" just won't catch.");
				return false;
			}
			if(lighting instanceof Light)
			{
				Light l=(Light)lighting;
				if(l.isLit())
				{
					mob.tell(l.name()+" is already lit!");
					return false;
				}
				mob.tell("Just hold this item to light it.");
				return false;
			}
			switch(lighting.material()&EnvResource.MATERIAL_MASK)
			{
			case EnvResource.MATERIAL_LEATHER:
				durationOfBurn=20+lighting.envStats().weight();
				break;
			case EnvResource.MATERIAL_CLOTH:
			case EnvResource.MATERIAL_PAPER:
				durationOfBurn=5+lighting.envStats().weight();
				break;
			case EnvResource.MATERIAL_WOODEN:
				completion=25-mob.envStats().level();
				durationOfBurn=40+lighting.envStats().weight();
				break;
			case EnvResource.MATERIAL_VEGETATION:
			case EnvResource.MATERIAL_FLESH:
				mob.tell("You need to cook that, if you can.");
				return false;
			case EnvResource.MATERIAL_UNKNOWN:
			case EnvResource.MATERIAL_GLASS:
			case EnvResource.MATERIAL_LIQUID:
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL:
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_PRECIOUS:
				mob.tell("That won't burn.");
				return false;
			}
			verb="lighting "+lighting.name();
		}

		switch(mob.location().getArea().weatherType(mob.location()))
		{
		case Area.WEATHER_BLIZZARD:
		case Area.WEATHER_SNOW:
		case Area.WEATHER_THUNDERSTORM:
			profficiencyAdjustment=-80;
			break;
		case Area.WEATHER_DROUGHT:
			profficiencyAdjustment=50;
			break;
		case Area.WEATHER_DUSTSTORM:
		case Area.WEATHER_WINDY:
			profficiencyAdjustment=-10;
			break;
		case Area.WEATHER_HEAT_WAVE:
			profficiencyAdjustment=10;
			break;
		case Area.WEATHER_RAIN:
		case Area.WEATHER_SLEET:
		case Area.WEATHER_HAIL:
			profficiencyAdjustment=-50;
			break;
		}
		failed=!profficiencyCheck(profficiencyAdjustment,auto);
		   
		if(completion<4) completion=4;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) building a fire.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
