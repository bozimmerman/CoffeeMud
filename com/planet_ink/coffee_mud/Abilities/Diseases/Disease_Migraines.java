package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Migraines extends Disease
{
	public String ID() { return "Disease_Migraines"; }
	public String name(){ return "Migraine Headaches";}
	public String displayText(){ return "(Migraine Headaches)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Migraines();}

	protected int DISEASE_TICKS(){return 99999;}
	protected int DISEASE_DELAY(){return 50;}
	protected String DISEASE_DONE(){return "Your headaches stop.";}
	protected String DISEASE_START(){return "^G<S-NAME> get(s) terrible headaches.^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int spreadCode(){return 0;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okAffect(myHost,affect);

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((affect.amISource(mob))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(mob.fetchAbility(affect.tool().ID())==affect.tool())
		&&(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_MIND)+25)))
		{
			mob.tell("Your headaches make you forget "+affect.tool().name()+"!");
			return false;
		}

		return super.okAffect(myHost,affect);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if((affected==null)||(invoker==null)) return false;
		return true;
	}
}
