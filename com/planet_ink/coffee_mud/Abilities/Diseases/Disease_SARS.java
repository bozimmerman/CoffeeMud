package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_SARS extends Disease
{
	public String ID() { return "Disease_SARS"; }
	public String name(){ return "SARS";}
	public String displayText(){ return "(SARS Virus)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_SARS();}

	protected int DISEASE_TICKS(){return 30;}
	protected int DISEASE_DELAY(){return 5;}
	protected String DISEASE_DONE(){return "Your sars clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with sars.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> wheeze(s).";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION|DiseaseAffect.SPREAD_PROXIMITY|DiseaseAffect.SPREAD_CONTACT|DiseaseAffect.SPREAD_STD;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		MOB diseaser=invoker;
		if(diseaser==null) diseaser=mob;
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_NOISE,DISEASE_AFFECT());
			int damage=Dice.roll(2,diseaser.envStats().level()+1,1);
			if(Dice.rollPercentage()==1)
				damage+=Dice.roll(10,10,1);
			MUDFight.postDamage(diseaser,mob,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_DISEASE,-1,null);
			catchIt(mob);
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-4);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)-5);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.CONSTITUTION,1);
		if(affectableStats.getStat(CharStats.STRENGTH)<=0)
			affectableStats.setStat(CharStats.STRENGTH,1);
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		if(affected==null) return;
		affectableState.setMovement(affectableState.getMovement()/2);
		affectableState.setMana(affectableState.getMana()-(affectableState.getMana()/3));
		affectableState.setHitPoints(affectableState.getHitPoints()-affected.envStats().level());
	}
}
