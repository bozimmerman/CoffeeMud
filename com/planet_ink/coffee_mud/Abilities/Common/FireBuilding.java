package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class FireBuilding extends CommonSkill
{
	public String ID() { return "FireBuilding"; }
	public String name(){ return "Fire Building";}
	private static final String[] triggerStrings = {"LIGHT","FIREBUILD","FIREBUILDING"};
	public String[] triggerStrings(){return triggerStrings;}

	public Item lighting=null;
	private int durationOfBurn=0;
	private boolean failed=false;
	private static boolean mapped=false;
	public FireBuilding()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance()	{return new FireBuilding();	}


	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted))
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
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Light what?  Try light fire, or light torch...");
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
				if(lighting.isGettable())
					commonTell(mob,"Just hold this item to light it.");
				else
				{
					l.light(true);
					mob.location().show(mob,lighting,CMMsg.TYP_HANDS,"<S-NAME> light(s) <T-NAMESELF>.");
					return true;
				}
				return false;
			}
			switch(lighting.material()&EnvResource.MATERIAL_MASK)
			{
			case EnvResource.MATERIAL_LEATHER:
				durationOfBurn=20+lighting.envStats().weight();
				break;
			case EnvResource.MATERIAL_CLOTH:
			case EnvResource.MATERIAL_PAPER:
			case EnvResource.MATERIAL_PLASTIC:
				durationOfBurn=5+lighting.envStats().weight();
				break;
			case EnvResource.MATERIAL_WOODEN:
				completion=25-mob.envStats().level();
				durationOfBurn=150+(lighting.envStats().weight()*5);
				break;
			case EnvResource.MATERIAL_VEGETATION:
			case EnvResource.MATERIAL_FLESH:
				commonTell(mob,"You need to cook that, if you can.");
				return false;
			case EnvResource.MATERIAL_UNKNOWN:
			case EnvResource.MATERIAL_GLASS:
			case EnvResource.MATERIAL_LIQUID:
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_ENERGY:
			case EnvResource.MATERIAL_MITHRIL:
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_PRECIOUS:
				commonTell(mob,"That won't burn.");
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

		durationOfBurn=durationOfBurn*abilityCode();
		if(completion<4) completion=4;

		FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) building a fire.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
