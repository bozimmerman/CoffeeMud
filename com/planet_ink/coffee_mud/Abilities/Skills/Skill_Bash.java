package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Bash extends StdAbility
{
	public Skill_Bash()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Bash";
		displayText="(Dented)";
		miscText="";

		triggerStrings.addElement("BASH");

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(4);

		addQualifyingClass("Fighter",4);
		addQualifyingClass("Ranger",4);
		addQualifyingClass("Paladin",4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Bash();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;


		Item thisSheild=mob.fetchWornItem(Item.HELD);
		if((thisSheild==null)||((thisSheild!=null)&&(!(thisSheild instanceof Shield))))
		{
			mob.tell("You must have a sheild to perform a bash.");
			return false;
		}

		if((Sense.isSitting(target)||Sense.isSleeping(target)))
		{
			mob.tell(target.name()+" must stand up first!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		String str=null;
		if(success)
		{
			str=auto?"<T-NAME> is bashed!":"<S-NAME> bash(es) <T-NAMESELF>!";
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.ACT_GENERAL:0),str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Weapon w=CMClass.getWeapon("ShieldWeapon");
				if((w!=null)&&(thisSheild!=null))
				{
					w.setName(thisSheild.name());
					w.setDisplayText(thisSheild.displayText());
					w.setDescription(thisSheild.description());
					w.baseEnvStats().setDamage(thisSheild.envStats().level()+10);
					ExternalPlay.doAttack(mob,target,w);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to bash <T-NAMESELF>, but end(s) up looking silly.");

		return success;
	}

}
