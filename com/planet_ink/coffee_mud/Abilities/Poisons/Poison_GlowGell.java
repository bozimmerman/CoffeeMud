package com.planet_ink.coffee_mud.Abilities.Poisons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Poison_Glowgell extends Poison
{
	public String ID() { return "Poison_Glowgell"; }
	public String name(){ return "Glowgell";}
	protected int canAffectCode(){return Ability.CAN_MOBS
										 |Ability.CAN_ITEMS
										 |Ability.CAN_EXITS;}
	public Environmental newInstance(){	return new Poison_Glowgell();}
	protected int POISON_DAMAGE(){return 0;}
	protected String POISON_DONE(){return "";}
	protected String POISON_START(){return "^G<S-NAME> start(s) glowing!^?";}
	protected String POISON_AFFECT(){return "";}
	protected String POISON_CAST(){return "^F<S-NAME> attempt(s) to smear something on <T-NAMESELF>!^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to smear something on <T-NAMESELF>, but fail(s).";}

	protected boolean catchIt(MOB mob, Environmental target)
	{
		return false;
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GLOWING);
	}
}