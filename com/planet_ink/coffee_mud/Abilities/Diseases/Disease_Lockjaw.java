package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Lockjaw extends Disease
{
	public String ID() { return "Disease_Lockjaw"; }
	public String name(){ return "Lockjaw";}
	public String displayText(){ return "(Lockjaw)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Lockjaw();}

	protected int DISEASE_TICKS(){return 9999999;}
	protected int DISEASE_DELAY(){return new Long(Host.TICKS_PER_MUDDAY).intValue();}
	protected String DISEASE_DONE(){return "Your lockjaw is cured.";}
	protected String DISEASE_START(){return "^G<S-NAME> get(s) lockjaw!^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			if(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_DISEASE))
			{
				unInvoke();
				return false;
			}
			return true;
		}
		return true;
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
		&&((msg.sourceMinor()==CMMsg.TYP_EAT)||(msg.sourceMinor()==CMMsg.TYP_DRINK)))
		{
			mob.tell("You can't open your mouth!");
			return false;
		}

		return super.okMessage(myHost,msg);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
	}
}
