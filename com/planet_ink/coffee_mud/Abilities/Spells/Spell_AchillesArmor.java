package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_AchillesArmor extends Spell
{
	public String ID() { return "Spell_AchillesArmor"; }
	public String name(){return "Achilles Armor";}
	public String displayText(){return "(Achilles Armor)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int overrideMana(){return 100;}
	public Environmental newInstance(){	return new Spell_AchillesArmor();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ABJURATION;}
	private int vulnerability=0;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your Achilles Armor fades.");

		super.unInvoke();

	}


	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okAffect(myHost,affect);

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(affect.source()!=affect.target())
		&&(mob.location()!=null)
		&&(mob.location().isInhabitant(affect.source()))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&((affect.targetCode()-Affect.MASK_HURT)>0)
		&&(!mob.amDead()))
		{
			int weaponType=-1;
			if((affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			   weaponType=((Weapon)affect.tool()).weaponType();
			else
			switch(affect.sourceMinor())
			{
			case Affect.TYP_FIRE:
				weaponType=Weapon.TYPE_BURNING;
				break;
			case Affect.TYP_WATER:
				weaponType=Weapon.TYPE_FROSTING;
				break;
			case Affect.TYP_ACID:
				weaponType=Weapon.TYPE_MELTING;
				break;
			case Affect.TYP_COLD:
				weaponType=Weapon.TYPE_FROSTING;
				break;
			case Affect.TYP_GAS:
				weaponType=Weapon.TYPE_GASSING;
				break;
			case Affect.TYP_ELECTRIC:
				weaponType=Weapon.TYPE_STRIKING;
				break;
			case Affect.TYP_DISEASE:
			case Affect.TYP_POISON:
			case Affect.TYP_UNDEAD:
			case Affect.TYP_CAST_SPELL:
				weaponType=Weapon.TYPE_BURSTING;
				break;
			}
			if(weaponType<0)
				return super.okAffect(myHost,affect);

			if(weaponType!=vulnerability)
			{
				String name=null;
				if(affect.tool()==null)
					name="the attack";
				else
				if(affect.tool() instanceof Weapon)
					name=affect.tool().displayName();
				else
					name="the "+affect.tool().displayName();
				mob.location().show(mob,affect.source(),Affect.MSG_OK_VISUAL,"The armor around <S-NAME> blocks "+name+" attack from <T-NAME>!");
				return false;
			}
			else
				ExternalPlay.postDeath(affect.source(),mob,affect);
		}
		return super.okAffect(myHost,affect);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> attain(s) Achilles Armor!":"^S<S-NAME> invoke(s) Achilles Armor around <T-NAMESELF>!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				vulnerability=Dice.roll(1,Weapon.typeDescription.length,-1);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke Achilles Armor, but fail(s).");

		return success;
	}
}
