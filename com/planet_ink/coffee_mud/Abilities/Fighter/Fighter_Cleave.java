package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Cleave extends StdAbility
{
	public String ID() { return "Fighter_Cleave"; }
	public String name(){ return "Cleave";}
	public String displayText(){ return "";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	
	private MOB thisTarget=null;
	private MOB nextTarget=null;
	public Environmental newInstance(){	return new Fighter_Cleave();}
	public int classificationCode(){ return Ability.SKILL;	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		
		if((thisTarget!=null)
		&&(nextTarget!=null)
		&&(thisTarget.amDead())
		&&(!nextTarget.amDead())
		&&(nextTarget.location()==mob.location())
		&&(mob.location().isInhabitant(nextTarget)))
		{
			Item w=mob.fetchWieldedItem();
			if(w==null) w=mob.myNaturalWeapon();
			mob.location().show(mob,nextTarget,Affect.MSG_NOISYMOVEMENT,"<S-NAME> CLEAVE(S) INTO <T-NAME>!!");
			ExternalPlay.postAttack(mob,nextTarget,w);
			helpProfficiency(mob);
		}
		thisTarget=null;
		nextTarget=null;
		return true;
	}
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amISource(mob))
		&&(mob.getVictim()!=null)
		&&(affect.amITarget(mob.getVictim()))
		&&(!affect.amITarget(mob))
		&&(!mob.getVictim().amDead())
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon))
		{
			MOB victim=mob.getVictim();
			Weapon w=(Weapon)affect.tool();
			int damAmount=affect.targetCode()-Affect.MASK_HURT;
			
			if((damAmount>victim.curState().getHitPoints())
			&&(profficiencyCheck(0,false))
			&&(w.weaponType()==Weapon.TYPE_SLASHING)
			&&(w.weaponClassification()!=Weapon.CLASS_NATURAL)
			&&(Sense.aliveAwakeMobile(mob,true)))
			{
				nextTarget=null;
				thisTarget=null;
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB vic=mob.location().fetchInhabitant(i);
					if((vic!=null)
					&&(vic.getVictim()==mob)
					&&(vic!=mob)
					&&(vic!=victim)
					&&(!vic.amDead())
					&&(vic.rangeToTarget()==0))
					{
						nextTarget=vic;
						break;
					}
				}
				if(nextTarget!=null)
					thisTarget=victim;
			}
		}
		return true;
	}
	
}
