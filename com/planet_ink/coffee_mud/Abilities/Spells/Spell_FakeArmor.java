package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FakeArmor extends Spell
{
	public String ID() { return "Spell_FakeArmor"; }
	public String name(){return "Fake Armor";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	private Item myItem=null;
	private static boolean notAgainThisRound=false;
	public Environmental newInstance(){	return new Spell_FakeArmor();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(notAgainThisRound) notAgainThisRound=false;
		return super.tick(ticking,tickID);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected!=null)
		&&(!notAgainThisRound)
		&&(affect.target()!=null)
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affected instanceof Item)
		&&(affect.amITarget(((Item)affected).owner()))
		&&(affect.target() instanceof MOB))
		{
			notAgainThisRound=true;
			affect.addTrailerMsg(new FullMsg((MOB)affect.target(),null,Affect.MSG_OK_VISUAL,affected.name()+" absorbs some of the damage done to <S-NAME>."));
			((Item)affected).unWear();
			((Item)affected).destroy();
		}
		return super.okAffect(myHost,affect);

	}
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String[] choices={"plate","chain","leather", "studded"};
		String[] choices2={"helmet","shirt","leggings", "sleeves","boots"};
		int choice=-1;
		int choice2=-1;
		if(commands.size()>1)
		{
			for(int i=0;i<choices.length;i++)
			{
				if(choices[i].equalsIgnoreCase((String)commands.elementAt(0)))
					choice=i;
			}
			for(int i=0;i<choices2.length;i++)
			{
				if(choices2[i].equalsIgnoreCase(Util.combine(commands,1)))
					choice2=i;
			}
		}
		if((choice<0)||(choice2<0))
		{
			mob.tell("You must specify what kind of armor to create: plate, chain, studded, or leather."
			+"You must also specify a armor type: helmet, shirt, leggings, sleeves, or boots");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms around dramatically.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				Armor armor=(Armor)CMClass.getItem("GenArmor");
				armor.baseEnvStats().setArmor(0);
				armor.baseEnvStats().setDisposition(armor.baseEnvStats().disposition()|EnvStats.IS_BONUS);
				armor.setMaterial(EnvResource.RESOURCE_COTTON);
				String materialName="cloth";
				switch(choice)
				{
				case 0: materialName="platemail"; break;
				case 1: materialName="chainmail"; break;
				case 3: materialName="studded leather"; break;
				case 2: materialName="leather"; break;
				}
				switch(choice2)
				{
				case 0:
					armor.setName("a "+materialName+" helmet");
					armor.setRawProperLocationBitmap(Item.ON_HEAD);
					break;
				case 1:
					armor.setName("a "+materialName+" shirt");
					armor.setRawProperLocationBitmap(Item.ON_HEAD);
					break;
				case 2:
					armor.setName("a pair of "+materialName+" leggings");
					armor.setRawProperLocationBitmap(Item.ON_LEGS);
					break;
				case 3:
					armor.setName("a pair of "+materialName+" sleeves");
					armor.setRawProperLocationBitmap(Item.ON_ARMS);
					break;
				case 4:
					armor.setName("a pair of "+materialName+" boots");
					armor.setRawProperLocationBitmap(Item.ON_FEET);
					break;
				}
				armor.setDisplayText(armor.name()+" sits here");
				armor.setDescription("looks like your size!");
				armor.baseEnvStats().setWeight(0);
				armor.recoverEnvStats();
				mob.addInventory(armor);
				mob.location().show(mob,null,armor,Affect.MSG_OK_ACTION,"Suddenly, <S-NAME> own(s) <O-NAME>!");
				myItem=armor;
				beneficialAffect(mob,armor,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> dramatically wave(s) <S-HIS-HER> arms around, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}