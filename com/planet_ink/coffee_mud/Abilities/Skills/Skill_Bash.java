package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Bash extends StdAbility
{
	public String ID() { return "Skill_Bash"; }
	public String name(){ return "Bash";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"BASH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Bash();}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;


		Item thisSheild=null;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)&&(I instanceof Shield)&&(!I.amWearingAt(Item.INVENTORY)))
			{ thisSheild=I; break;}
		}
		if(thisSheild==null)
		{
			mob.tell("You must have a shield to perform a bash.");
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
			str=auto?"<T-NAME> is bashed!":"^F<S-NAME> bash(es) <T-NAMESELF>!^?";
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Weapon w=CMClass.getWeapon("ShieldWeapon");
				if((w!=null)&&(thisSheild!=null))
				{
					w.setName(thisSheild.name());
					w.setDisplayText(thisSheild.displayText());
					w.setDescription(thisSheild.description());
					w.baseEnvStats().setDamage(thisSheild.envStats().level()+5);
					MUDFight.postAttack(mob,target,w);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to bash <T-NAMESELF>, but end(s) up looking silly.");

		return success;
	}

}
