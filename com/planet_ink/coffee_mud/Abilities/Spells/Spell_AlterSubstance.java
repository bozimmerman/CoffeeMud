package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_AlterSubstance extends Spell
{
	public String ID() { return "Spell_AlterSubstance"; }
	public String name(){return "Alter Substance";}
	protected int canTargetCode(){return CAN_ITEMS;}
	protected int canAffectCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_AlterSubstance();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ALTERATION;}
	public String newName="";
	public int oldMaterial=0;
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(newName.length()>0)
			affectableStats.setReplacementName(newName);
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof Item))
		{
			Item I=(Item)affected;
			I.setMaterial(oldMaterial);
			if(I.owner() instanceof Room)
				((Room)I.owner()).showHappens(Affect.MSG_OK_VISUAL,I.name()+" reverts to its natural form.");
			else
			if(I.owner() instanceof MOB)
				((MOB)I.owner()).tell(I.name()+" reverts to its natural form.");
		}
		super.unInvoke();
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String material="";
		if(commands.size()>0)
		{
			material=(String)commands.lastElement();
			commands.removeElement(material);
		}
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		int newMaterial=-1;
		for(int m=0;m<EnvResource.MATERIAL_DESCS.length;m++)
		{
			if(CoffeeUtensils.containsString(EnvResource.MATERIAL_DESCS[m],material))
			{
				for(int r=0;r<EnvResource.RESOURCE_DESCS.length;r++)
				{
					int code=EnvResource.RESOURCE_DATA[r][0];
					if((code&EnvResource.MATERIAL_MASK)==(m<<8))
					{
						newMaterial=code;
						material=EnvResource.RESOURCE_DESCS[r];
						break;
					}
				}
				if(newMaterial<0)
				{
					newMaterial=(m<<8);
					material=EnvResource.MATERIAL_DESCS[m];
				}
				break;
			}
		}
		if(newMaterial<0)
		for(int r=0;r<EnvResource.RESOURCE_DESCS.length;r++)
		{
			if(CoffeeUtensils.containsString(EnvResource.RESOURCE_DESCS[r],material))
			{
				newMaterial=EnvResource.RESOURCE_DATA[r][0];
				material=EnvResource.RESOURCE_DESCS[r];
				break;
			}
		}
		if(newMaterial<0)
		{
			mob.tell("'"+material+"' is not a known substance!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				material=Util.capitalize(material);
				mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> change(s) into "+material+"!");
				oldMaterial=target.material();
				target.setMaterial(newMaterial);
				String oldResourceName=EnvResource.RESOURCE_DESCS[oldMaterial&EnvResource.RESOURCE_MASK];
				String oldMaterialName=EnvResource.MATERIAL_DESCS[(oldMaterial&EnvResource.MATERIAL_MASK)>>8];
				String oldName=target.name().toUpperCase();
System.out.println(oldName+"/"+oldResourceName+"/"+oldMaterialName);
				oldName=oldName.replaceAll(oldResourceName,material);
				oldName=oldName.replaceAll(oldMaterialName,material);
				newName=Util.capitalize(oldName);
System.out.println(newName+"/"+oldResourceName+"/"+oldMaterialName);
				beneficialAffect(mob,target,100);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting but nothing happens.");


		// return whether it worked
		return success;
	}
}