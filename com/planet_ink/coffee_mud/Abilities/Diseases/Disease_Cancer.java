package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Cancer extends Disease
{
	public String ID() { return "Disease_Cancer"; }
	public String name(){ return "Cancer";}
	public String displayText(){ return "(Cancer)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Cancer();}

	protected int DISEASE_TICKS(){return 99999;}
	protected int DISEASE_DELAY(){return Host.TICKS_PER_DAY;}
	protected String DISEASE_DONE(){return "Your cancer is cured!";}
	protected String DISEASE_START(){return "^G<S-NAME> seem(s) ill.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> <S-IS-ARE> getting sicker...";}
	public int abilityCode(){return 0;}
	protected int conDown=1;
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if((affected==null)||(invoker==null)) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,Affect.MSG_NOISE,DISEASE_AFFECT());
			conDown++;
			return true;
		}
		return true;
	}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		if(conDown<0) return;
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-conDown);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
		{
			conDown=-1;
			ExternalPlay.postDeath(invoker(),affected,null);
		}
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		if(affected==null) return;
		affectableState.setMovement(affectableState.getMovement()/conDown);
		affectableState.setMana(affectableState.getMana()/conDown);
		affectableState.setHitPoints(affectableState.getHitPoints()/conDown);
	}
}