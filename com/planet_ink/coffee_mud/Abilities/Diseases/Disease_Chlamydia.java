package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Chlamydia extends Disease
{
	public String ID() { return "Disease_Chlamydia"; }
	public String name(){ return "Chlamydia";}
	public String displayText(){ return "(Chlamydia)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Chlamydia();}

	protected int DISEASE_TICKS(){return Host.TICKS_PER_DAY*10;}
	protected int DISEASE_DELAY(){return Host.TICKS_PER_DAY;}
	protected String DISEASE_DONE(){return "Your chlamydia clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> scratch(es) <S-HIS-HER> privates.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> scratch(es) <S-HIS-HER> privates.";}
	public int abilityCode(){return DiseaseAffect.SPREAD_STD;}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,Affect.MSG_NOISE,DISEASE_AFFECT());
			return true;
		}
		return true;
	}
	
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		affectableState.setMovement(affectableState.getMovement()/2);
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
			&&(affect.tool().name().equals("MATE <T-NAME>")
			||affect.tool().name().equals("SEX <T-NAME>")))
			{
				affect.source().tell(mob,null,null,"<S-NAME> really do(es)n't feel like it.");
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}
}
