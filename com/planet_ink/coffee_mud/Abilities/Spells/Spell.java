package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell extends StdAbility
{
	public String ID() { return "Spell"; }
	public String name(){ return "a Spell";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){ return INDIFFERENT;}
	private static final String[] triggerStrings = {"CAST","CA","C"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SPELL;}
	protected boolean exemptFromArmorReq(){return false;}

	protected int affectType(boolean auto){
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
		return affectType;
	}

	public Environmental newInstance(){	return new Spell();}
	public boolean maliciousAffect(MOB mob,
								   Environmental target,
								   int tickAdjustmentFromStandard,
								   int additionAffectCheckCode)
	{
		boolean truefalse=super.maliciousAffect(mob,target,tickAdjustmentFromStandard,additionAffectCheckCode);
		if(truefalse
		&&(target!=null)
		&&(target instanceof MOB)
		&&(mob!=target)
		&&(!((MOB)target).isMonster())
		&&(Dice.rollPercentage()==1)
		&&(((MOB)target).charStats().getCurrentClass().baseClass().equals("Mage")))
		{
			MOB tmob=(MOB)target;
			int num=0;
			for(int i=0;i<tmob.numEffects();i++)
			{
				Ability A=tmob.fetchEffect(i);
				if((A!=null)
				&&(A instanceof Spell)
				&&(A.quality()==Ability.MALICIOUS))
				{
					num++;
					if(num>5)
					{
						Ability A2=CMClass.getAbility("Disease_Magepox");
						if((A2!=null)&&(target.fetchEffect(A2.ID())==null))
							A2.invoke(mob,target,true);
						break;
					}
				}
			}
		}
		return truefalse;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)
		&&(!exemptFromArmorReq())
		&&(CMAble.getQualifyingLevel(mob.charStats().getCurrentClass().ID(),ID())<0)
		&&(!CoffeeUtensils.armorCheck(mob,CharClass.ARMOR_CLOTH))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(Dice.rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
			return false;
		}
		return true;
	}
}
