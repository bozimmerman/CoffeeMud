package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Syphilis extends Disease
{
	public String ID() { return "Disease_Syphilis"; }
	public String name(){ return "Syphilis";}
	public String displayText(){ return "(Syphilis)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Syphilis();}

	protected int DISEASE_TICKS(){return 99999;}
	protected int DISEASE_DELAY(){return new Long(MudHost.TICKS_PER_MUDDAY).intValue();}
	protected String DISEASE_DONE(){return "Your syphilis clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> get(s) some uncomfortable red sores on <S-HIS-HER> privates.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> scratch(es) <S-HIS-HER> privates.";}
	public int abilityCode(){return DiseaseAffect.SPREAD_STD;}
	protected int conDown=0;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_NOISE,DISEASE_AFFECT());
			if(Dice.rollPercentage()>50)
				conDown++;
			if(Dice.rollPercentage()<10)
			{
				Ability A=null;
				if(Dice.rollPercentage()>50)
					A=CMClass.getAbility("Disease_Cold");
				else
					A=CMClass.getAbility("Disease_Fever");
				if(A!=null)A.invoke(mob,mob,true);
			}
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		if(conDown<=0) return;
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-conDown);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
		{
			conDown=-1;
			MOB diseaser=invoker;
			if(diseaser==null) diseaser=affected;
			MUDFight.postDeath(diseaser,affected,null);
		}
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		if(affected==null) return;
		int down=2;
		if(conDown>down) down=conDown;
		affectableState.setMovement(affectableState.getMovement()/down);
		affectableState.setMana(affectableState.getMana()/down);
		affectableState.setHitPoints(affectableState.getHitPoints()/down);
	}
}
