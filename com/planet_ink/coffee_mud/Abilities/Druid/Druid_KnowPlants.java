package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druid_KnowPlants extends StdAbility
{
	public String ID() { return "Druid_KnowPlants"; }
	public String name(){ return "Know Plants";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	private static final String[] triggerStrings = {"KNOWPLANT"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Druid_KnowPlants();}
	public int classificationCode(){return Ability.SKILL;}

	public static boolean isPlant(Item I)
	{
		if((I!=null)&&(I.rawSecretIdentity().length()>0))
		{
			for(int a=0;a<I.numAffects();a++)
			{
				Ability A=I.fetchAffect(a);
				if((A!=null)&&(A.invoker()!=null)&&(A instanceof Chant_SummonPlants))
					return true;
			}
		}
		return false;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item I=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(I==null) return false;
		if(((I.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_VEGETATION)
		&&((I.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN))
		{
			mob.tell("Your plant knowledge can tell you nothing about "+I.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);

		if(!success)
			mob.tell("Your plant senses fail you.");
		else
		{
			FullMsg msg=new FullMsg(mob,I,null,Affect.MSG_DELICATE_HANDS_ACT|Affect.MASK_MAGIC,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer str=new StringBuffer("");
				str.append(I.name()+" is a kind of "+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK].toLowerCase()+".  ");
				if(isPlant(I))
					str.append("It was summoned by "+I.rawSecretIdentity()+".");
				else
					str.append("It is either processed by hand, or grown wild.");
				mob.tell(str.toString());
			}
		}
		return success;
	}
}

