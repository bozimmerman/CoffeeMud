package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Malaria extends Disease
{
	public String ID() { return "Disease_Malaria"; }
	public String name(){ return "Malaria";}
	public String displayText(){ return "(Malaria)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Malaria();}

	protected int DISEASE_TICKS(){return 9*Host.TICKS_PER_DAY;}
	protected int DISEASE_DELAY(){return 5;}
	protected String DISEASE_DONE(){return "Your malaria clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with malaria.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> ache(s) and sneeze(s). AAAAAAAAAAAAAACHOOO!!!!";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION|DiseaseAffect.SPREAD_PROXIMITY|DiseaseAffect.SPREAD_CONTACT|DiseaseAffect.SPREAD_STD;}
	private int conDown=0;
	private int tickUp=0;
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		MOB diseaser=invoker;
		if(diseaser==null) diseaser=mob;
		if((++tickUp)==Host.TICKS_PER_DAY)
		{
			tickUp=0;
			conDown++;
			if((Dice.rollPercentage()<20)
			&&(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_DISEASE)))
			{
				unInvoke();
				return false;
			}
		}
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,Affect.MSG_NOISE,DISEASE_AFFECT());
			int damage=Dice.roll(2,diseaser.envStats().level()+1,1);
			ExternalPlay.postDamage(diseaser,mob,this,damage,Affect.MASK_GENERAL|Affect.TYP_DISEASE,-1,null);
			catchIt(mob);
			if(Dice.rollPercentage()==1)
			{
				Ability A=CMClass.getAbility("Disease_Fever");
				if(A!=null) A.invoke(diseaser,mob,true);
			}
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)-5);
		if(affectableStats.getStat(CharStats.STRENGTH)<=0)
			affectableStats.setStat(CharStats.STRENGTH,1);
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-(5+conDown));
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
		{
			conDown=-1;
			MOB diseaser=invoker;
			if(diseaser==null) diseaser=affected;
			ExternalPlay.postDeath(diseaser,affected,null);
		}
	}
	
	public void affectCharState(MOB affected, CharState affectableState)
	{
		if(affected==null) return;
		affectableState.setMovement(affectableState.getMovement()/2);
		affectableState.setMana(affectableState.getMana()-(affectableState.getMana()/3));
		affectableState.setHitPoints(affectableState.getHitPoints()-affected.envStats().level());
	}
}
