package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Gonorrhea extends Disease
{
	public String ID() { return "Disease_Gonorrhea"; }
	public String name(){ return "Gonorrhea";}
	public String displayText(){ return "(Gonorrhea)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Gonorrhea();}

	protected int DISEASE_TICKS(){return 99999;}
	protected int DISEASE_DELAY(){return new Long(Host.TICKS_PER_DAY).intValue();}
	protected String DISEASE_DONE(){return "Your gonorrhea clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> squeeze(s) <S-HIS-HER> privates uncomfortably.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> squeeze(s) <S-HIS-HER> privates uncomfortably.";}
	public int abilityCode(){return DiseaseAffect.SPREAD_STD;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((Dice.rollPercentage()==1)
		&&(Dice.rollPercentage()>mob.charStats().getSave(CharStats.SAVE_COLD))
		&&(Dice.rollPercentage()<25-mob.charStats().getStat(CharStats.CONSTITUTION)))
		{
			MOB diseaser=invoker;
			if(diseaser==null) diseaser=mob;
			Ability A=CMClass.getAbility("Disease_Arthritis");
			A.invoke(diseaser,mob,true);
		}
		else
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,Affect.MSG_NOISE,DISEASE_AFFECT());
			return true;
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-5);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(affected==null) return super.okAffect(myHost,affect);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(((affect.amITarget(mob))||(affect.amISource(mob)))
			&&(affect.tool()!=null)
			&&(affect.tool().ID().equals("Social"))
			&&(affect.tool().Name().equals("MATE <T-NAME>")
			||affect.tool().Name().equals("SEX <T-NAME>")))
			{
				affect.source().tell(mob,null,null,"<S-NAME> really do(es)n't feel like it.");
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}
}
