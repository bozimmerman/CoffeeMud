package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Fleas extends Disease
{
	public String ID() { return "Disease_Fleas"; }
	public String name(){ return "Fleas";}
	public String displayText(){ return "(Fleas)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Fleas();}

	protected int DISEASE_TICKS(){return 35;}
	protected int DISEASE_DELAY(){return 5;}
	protected String DISEASE_DONE(){return "Your problem with fleas clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> <S-HAS-HAVE> fleas!^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> scratch(es) <S-HIM-HERSELF>.";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONTACT|DiseaseAffect.SPREAD_STD|Disease.SPREAD_PROXIMITY;}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,DISEASE_AFFECT());
			catchIt(mob);
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)-4);
		if(affectableStats.getStat(CharStats.DEXTERITY)<=0)
			affectableStats.setStat(CharStats.DEXTERITY,1);
	}
}
