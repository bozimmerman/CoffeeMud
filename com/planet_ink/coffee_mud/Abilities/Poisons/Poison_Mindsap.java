package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Poison_Mindsap extends Poison
{
	public String ID() { return "Poison_Mindsap"; }
	public String name(){ return "Mindsap";}
	private static final String[] triggerStrings = {"POISONSAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Poison_Mindsap();}
	
	protected int POISON_TICKS(){return 50;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 1;}
	protected String POISON_DONE(){return "Your thoughts clear up.";}
	protected String POISON_START(){return "^G<S-NAME> seem(s) confused!^?";}
	protected String POISON_AFFECT(){return "";}
	protected String POISON_CAST(){return "^F<S-NAME> poison(s) <T-NAMESELF>!^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return 0;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-1);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.CONSTITUTION,1);
		affectableStats.setStat(CharStats.INTELLIGENCE,affectableStats.getStat(CharStats.INTELLIGENCE)-10);
		if(affectableStats.getStat(CharStats.INTELLIGENCE)<=0)
			affectableStats.setStat(CharStats.INTELLIGENCE,1);
	}
}
