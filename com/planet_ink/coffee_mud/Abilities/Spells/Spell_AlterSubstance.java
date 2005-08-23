package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_AlterSubstance extends Spell
{
	public String ID() { return "Spell_AlterSubstance"; }
	public String name(){return "Alter Substance";}
	protected int canTargetCode(){return CAN_ITEMS;}
	protected int canAffectCode(){return CAN_ITEMS;}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ALTERATION;}
	public String newName="";
	public int oldMaterial=0;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(newName.length()>0)
			affectableStats.setName(newName);
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof Item))
		{
			Item I=(Item)affected;
			I.setMaterial(oldMaterial);
			if(I.owner() instanceof Room)
				((Room)I.owner()).showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" reverts to its natural form.");
			else
			if(I.owner() instanceof MOB)
				((MOB)I.owner()).tell(I.name()+" reverts to its natural form.");
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
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
			if(EnglishParser.containsString(EnvResource.MATERIAL_DESCS[m],material))
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
			if(EnglishParser.containsString(EnvResource.RESOURCE_DESCS[r],material))
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

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				material=Util.capitalizeAndLower(material);
				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> change(s) into "+material+"!");
				oldMaterial=target.material();
				target.setMaterial(newMaterial);
				String oldResourceName=EnvResource.RESOURCE_DESCS[oldMaterial&EnvResource.RESOURCE_MASK];
				String oldMaterialName=EnvResource.MATERIAL_DESCS[(oldMaterial&EnvResource.MATERIAL_MASK)>>8];
				String oldName=target.name().toUpperCase();
				oldName=Util.replaceAll(oldName,oldResourceName,material);
				oldName=Util.replaceAll(oldName,oldMaterialName,material);
				if(oldName.indexOf(material)<0)
				{
					int x=oldName.lastIndexOf(" ");
					if(x<0)
						oldName=material+" "+oldName;
					else
						oldName=oldName.substring(0,x)+" "+material+oldName.substring(x);
				}
				newName=Util.capitalizeAndLower(oldName);
				beneficialAffect(mob,target,asLevel,100);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting but nothing happens.");


		// return whether it worked
		return success;
	}
}
