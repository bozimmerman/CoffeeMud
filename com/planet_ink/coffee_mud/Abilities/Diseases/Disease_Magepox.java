package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Magepox extends Disease
{
	public String ID() { return "Disease_Magepox"; }
	public String name(){ return "Magepox";}
	public String displayText(){ return "(Magepox)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Magepox();}

	protected int DISEASE_TICKS(){return new Long(Host.TICKS_PER_MUDDAY).intValue();}
	protected int DISEASE_DELAY(){return 15;}
	protected String DISEASE_DONE(){return "Your magepox clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with the Magepox.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> watch(es) new mystical sores appear on <S-HIS-HER> body.";}
	public int abilityCode(){return DiseaseAffect.SPREAD_PROXIMITY;}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,DISEASE_AFFECT());
			catchIt(mob);
			return true;
		}
		return true;
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		if(affected==null) return;
		int hitsLost=affected.maxState().getHitPoints()-affected.curState().getHitPoints();
		if(hitsLost<0) hitsLost=0;
		int movesLost=(affected.maxState().getMovement()-affected.curState().getMovement());
		if(movesLost<0) movesLost=0;
		int lostMana=hitsLost+movesLost;
		affectableState.setMana(affectableState.getMana()-lostMana);
		if(affectableState.getMana()<0)
			affectableState.setMana(0);
		if(affected.curState().getMana()>affectableState.getMana())
			affected.curState().setMana(affectableState.getMana());
			
	}
}
