package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FakeWeapon extends Spell
{
	private Item myItem=null;
	public Spell_FakeWeapon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Fake Weapon";

		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=Ability.CAN_ITEMS;
		canTargetCode=0;
		
		baseEnvStats().setLevel(13);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_FakeWeapon();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ILLUSION;
	}

	public void unInvoke()
	{
		if(myItem==null) return;
		super.unInvoke();
		Item item=myItem;
		myItem=null;
		item.destroyThis();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		&&(affected instanceof Item)
		&&(affect.tool()==affected)
		&&((affect.targetCode()&Affect.MASK_HURT)>0))
		{
			affect.modify(affect.source(),
						  affect.target(),
						  affect.tool(),
						  affect.sourceCode(),
						  affect.sourceMessage(),
						  Affect.MASK_HURT,
						  affect.targetMessage(),
						  affect.othersCode(),
						  affect.othersMessage());
		}
		return super.okAffect(affect);
	
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

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> wave(s) <S-HIS-HER> arms around dramatically.");
			if(mob.location().okAffect(msg))
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
				weapon.recoverEnvStats();
				mob.addInventory(weapon);
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly, <S-NAME> own(s) "+weapon.name());
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