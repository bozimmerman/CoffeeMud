package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Spell_FakeWeapon extends Spell
{
	public String ID() { return "Spell_FakeWeapon"; }
	public String name(){return "Fake Weapon";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	private Item myItem=null;
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public void unInvoke()
	{
		if(myItem==null) return;
		super.unInvoke();
		if(canBeUninvoked())
		{
			Item item=myItem;
			myItem=null;
			item.destroy();
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof Item)
		&&(msg.tool()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
			msg.setValue(0);
		return super.okMessage(myHost,msg);

	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String weaponName=Util.combine(commands,0);
		String[] choices={"sword","dagger","mace","staff","axe","hammer", "flail"};
		int choice=-1;
		for(int i=0;i<choices.length;i++)
		{
			if(choices[i].equalsIgnoreCase(weaponName))
				choice=i;
		}
		if(choice<0)
		{
			mob.tell("You must specify what kind of weapon to create: sword, dagger, mace, flail, staff, axe, or hammer.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms around dramatically.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Weapon weapon=(Weapon)CMClass.getItem("GenWeapon");
				weapon.baseEnvStats().setAttackAdjustment(100);
				weapon.baseEnvStats().setDamage(75);
				weapon.baseEnvStats().setDisposition(weapon.baseEnvStats().disposition()|EnvStats.IS_BONUS);
				weapon.setMaterial(EnvResource.RESOURCE_COTTON);
				switch(choice)
				{
				case 0:
					weapon.setName("a fancy sword");
					weapon.setDisplayText("a fancy sword sits here");
					weapon.setDescription("looks fit to cut something up!");
					weapon.setWeaponClassification(Weapon.CLASS_SWORD);
					weapon.setWeaponType(Weapon.TYPE_SLASHING);
					break;
				case 1:
					weapon.setName("a sharp dagger");
					weapon.setDisplayText("a sharp dagger sits here");
					weapon.setDescription("looks fit to cut something up!");
					weapon.setWeaponClassification(Weapon.CLASS_DAGGER);
					weapon.setWeaponType(Weapon.TYPE_PIERCING);
					break;
				case 2:
					weapon.setName("a large mace");
					weapon.setDisplayText("a large mace sits here");
					weapon.setDescription("looks fit to whomp on something with!");
					weapon.setWeaponClassification(Weapon.CLASS_BLUNT);
					weapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				case 3:
					weapon.setName("a quarterstaff");
					weapon.setDisplayText("a quarterstaff sits here");
					weapon.setDescription("looks like a reliable weapon");
					weapon.setWeaponClassification(Weapon.CLASS_STAFF);
					weapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				case 4:
					weapon.setName("a deadly axe");
					weapon.setDisplayText("a deadly axe sits here");
					weapon.setDescription("looks fit to shop something up!");
					weapon.setWeaponClassification(Weapon.CLASS_AXE);
					weapon.setWeaponType(Weapon.TYPE_SLASHING);
					break;
				case 5:
					weapon.setName("a large hammer");
					weapon.setDisplayText("a large hammer sits here");
					weapon.setDescription("looks fit to pound something into a pulp!");
					weapon.setWeaponClassification(Weapon.CLASS_HAMMER);
					weapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				case 6:
					weapon.setName("a large flail");
					weapon.setDisplayText("a large flail sits here");
					weapon.setDescription("looks fit to pound something into a pulp!");
					weapon.setWeaponClassification(Weapon.CLASS_FLAILED);
					weapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				}
				weapon.baseEnvStats().setWeight(0);
				weapon.setBaseValue(0);
				weapon.recoverEnvStats();
				mob.addInventory(weapon);
				mob.location().show(mob,null,weapon,CMMsg.MSG_OK_ACTION,"Suddenly, <S-NAME> own(s) <O-NAME>!");
				myItem=weapon;
				beneficialAffect(mob,weapon,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> dramatically wave(s) <S-HIS-HER> arms around, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
