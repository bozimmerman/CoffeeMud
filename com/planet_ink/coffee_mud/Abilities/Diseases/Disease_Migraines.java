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
	public int abilityCode(){return 0;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(mob.fetchAbility(msg.tool().ID())==msg.tool())
		&&(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_MIND)+25)))
		{
			mob.tell("Your headaches make you forget "+msg.tool().name()+"!");
			return false;
		}

		return super.okMessage(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		return true;
	}
}
