package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Amnesia extends Disease
{
	public String ID() { return "Disease_Amnesia"; }
	public String name(){ return "Amnesia";}
	public String displayText(){ return "(Amnesia)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Amnesia();}
	public int classificationCode(){return Ability.SKILL;}

	protected int DISEASE_TICKS(){return 34;}
	protected int DISEASE_DELAY(){return 5;}
	protected int lastHP=Integer.MAX_VALUE;
	protected String DISEASE_DONE(){return "Your memory returns.";}
	protected String DISEASE_START(){return "^G<S-NAME> feel(s) like <S-HE-SHE> <S-HAS-HAVE> forgotten something.^?";}
	protected String DISEASE_AFFECT(){return "";}
	protected boolean DISEASE_STD(){return false;}
	protected boolean DISEASE_TOUCHSPREAD(){return false;}
	
	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((affect.amISource(mob))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(mob.fetchAbility(affect.tool().ID())==affect.tool())
		&&(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_MIND)+10)))
		{
			mob.tell("You can't remember "+affect.tool().name()+"!");
			return false;
		}

		return super.okAffect(affect);
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))	return false;
		if((affected==null)||(invoker==null)) return false;
		return true;
	}
}
