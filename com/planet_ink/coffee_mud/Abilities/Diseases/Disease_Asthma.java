package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Asthma extends Disease
{
	public String ID() { return "Disease_Asthma"; }
	public String name(){ return "Asthma";}
	public String displayText(){ return "(Asthma)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Asthma();}

	protected int DISEASE_TICKS(){return 99999;}
	protected int DISEASE_DELAY(){return 5;}
	protected String DISEASE_DONE(){return "Your asthma clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> start(s) wheezing.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> wheeze(s) loudly.";}
	public int abilityCode(){return 0;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			if(Dice.rollPercentage()==1)
			{
				int damage=mob.curState().getHitPoints()/2;
				MOB diseaser=invoker;
				if(diseaser==null) diseaser=mob;
				ExternalPlay.postDamage(diseaser,mob,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_DISEASE,-1,"<S-NAME> <S-HAS-HAVE> an asthma attack! It <DAMAGE> <S-NAME>!");
			}
			else
				mob.location().show(mob,null,CMMsg.MSG_NOISE,DISEASE_AFFECT());
			return true;
		}
		return true;
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		if(affected==null) return;
		affectableState.setMovement(affectableState.getMovement()/4);
	}
}
