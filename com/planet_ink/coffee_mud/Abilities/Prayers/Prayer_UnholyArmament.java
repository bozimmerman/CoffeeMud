package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_UnholyArmament extends Prayer
{
	public String ID() { return "Prayer_UnholyArmament"; }
	public String name(){ return "Unholy Armament";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return OK_SELF;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public Environmental newInstance(){	return new Prayer_UnholyArmament();}
	public static final long[] checkOrder={
		Item.WIELD,
		Item.ON_TORSO,
		Item.ON_LEGS,
		Item.ON_WAIST,
		Item.ON_HEAD,
		Item.ON_ARMS,
		Item.ON_FEET,
		Item.ON_HANDS,
		Item.ON_LEFT_WRIST,
		Item.ON_RIGHT_WRIST,
		Item.ABOUT_BODY,
	};

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		long pos=-1;
		for(int i=0;i<checkOrder.length;i++)
			if(mob.freeWearPositions(checkOrder[i])>0)
			{ pos=checkOrder[i]; break;}
		if(pos<0)
		{
			if(mob.getWorshipCharID().length()>0)
				mob.tell(mob.getWorshipCharID()+" can see that you are already completely armed.");
			else
				mob.tell("The gods can see that you are already armed.");
			return false;
		}
		Item I=null;
		if(pos==Item.WIELD)
		{
			I=CMClass.getWeapon("GenWeapon");
			I.setName("an unholy blade");
			I.setDisplayText("an wicked looking blade sits here.");
			((Weapon)I).setWeaponClassification(Weapon.CLASS_SWORD);
			((Weapon)I).setWeaponType(Weapon.TYPE_SLASHING);
			I.setDescription("Whatever made this sharp twisted thing couldn`t have been good..");
			I.baseEnvStats().setLevel(mob.envStats().level());
			I.baseEnvStats().setWeight(20);
			I.setMaterial(EnvResource.MATERIAL_MITHRIL);
			I.recoverEnvStats();
			Hashtable H=CoffeeMaker.timsItemAdjustments(I,mob.envStats().level(),I.material(),I.baseEnvStats().weight(),1,((Weapon)I).weaponClassification(),0,I.rawProperLocationBitmap());
			I.baseEnvStats().setDamage(Util.s_int((String)H.get("DAMAGE")));
			I.baseEnvStats().setAttackAdjustment(Util.s_int((String)H.get("ATTACK")));
			I.setBaseValue(Util.s_int((String)H.get("VALUE")));
		}
		else
		{
			I=CMClass.getArmor("GenArmor");
			I.setRawProperLocationBitmap(pos);
			I.baseEnvStats().setLevel(mob.envStats().level());
			if(pos==Item.ABOUT_BODY) 
				I.setMaterial(EnvResource.RESOURCE_COTTON);
			else
				I.setMaterial(EnvResource.MATERIAL_MITHRIL);
			I.recoverEnvStats();
			Hashtable H=CoffeeMaker.timsItemAdjustments(I,mob.envStats().level(),I.material(),I.baseEnvStats().weight(),1,0,0,I.rawProperLocationBitmap());
			I.baseEnvStats().setArmor(Util.s_int((String)H.get("ARMOR")));
			I.baseEnvStats().setWeight(Util.s_int((String)H.get("WEIGHT")));
			I.setBaseValue(Util.s_int((String)H.get("VALUE")));
			if(pos==Item.ON_TORSO)
			{
				I.setName("an unholy breast plate");
				I.setDisplayText("a wicked looking breast plate sits here.");
				I.setDescription("Whatever made this black spiked armor couldn`t have been good.");
			}
			if(pos==Item.ON_HEAD)
			{
				I.setName("an unholy helm");
				I.setDisplayText("a wicked looking helmet sits here.");
				I.setDescription("Whatever made this spiked helmet couldn`t have been good.");
			}
			if(pos==Item.ABOUT_BODY)
			{
				I.setName("an unholy cape");
				I.setDisplayText("a torn black cape sits here.");
				I.setDescription("Whatever made this cape couldn`t have been good.");
			}
			if(pos==Item.ON_ARMS)
			{
				I.setName("some unholy arm cannons");
				I.setDisplayText("a pair of wicked looking arm cannons sit here.");
				I.setDescription("Whatever made this couldn`t have been good.");
			}
			if((pos==Item.ON_LEFT_WRIST)
			||(pos==Item.ON_RIGHT_WRIST))
			{
				I.setName("an unholy vambrace");
				I.setDisplayText("a wicked looking spiked vambrace sit here.");
				I.setDescription("Whatever made this twisted black metal couldn`t have been good.");
			}
			if(pos==Item.ON_HANDS)
			{
				I.setName("a pair of unholy gauntlets");
				I.setDisplayText("some wicked looking gauntlets sit here.");
				I.setDescription("Whatever made this twisted black metal couldn`t have been good.");
			}
			if(pos==Item.ON_WAIST)
			{
				I.setName("an unholy girdle");
				I.setDisplayText("a wicked looking girdle sits here.");
				I.setDescription("Whatever made this twisted black metal couldn`t have been good.");
			}
			if(pos==Item.ON_LEGS)
			{
				I.setName("a pair of unholy leg cannons");
				I.setDisplayText("a wicked looking pair of leg cannons sits here.");
				I.setDescription("Whatever made this twisted and spiked black metal couldn`t have been good.");
			}
			if(pos==Item.ON_FEET)
			{
				I.setName("a pair of unholy boots");
				I.setDisplayText("a wicked looking pair of boots sits here.");
				I.setDescription("Whatever made this pair of twisted and spiked black metal boots couldn`t have been good.");
			}
		}
		Ability A=CMClass.getAbility("Prop_HaveZapper");
		if(A!=null)
		{
			A.setMiscText("-GOOD -NEUTRAL -NAMES \"+"+mob.Name()+"\"");
			I.addNonUninvokableEffect(A);
		}
		I.recoverEnvStats();
		if((mob.fetchInventory(null,I.name()+"$")!=null)
		||(mob.location().fetchItem(null,I.name()+"$")!=null))
			I=null;
		
		
		boolean success=profficiencyCheck(mob,0,auto);
		
		if((success)&&(I!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" to be provided armament!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().addItemRefuse(I,Item.REFUSE_MONSTER_EQ);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" materializes out of the ground.");
			}
		}
		else
			return beneficialWordsFizzle(mob, null,"<S-NAME> "+prayWord(mob)+" for armament, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
