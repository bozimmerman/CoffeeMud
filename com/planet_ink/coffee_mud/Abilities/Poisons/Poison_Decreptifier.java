package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Poison_Decreptifier extends Poison
{
	public String ID() { return "Poison_Decreptifier"; }
	public String name(){ return "Decreptifier";}
	private static final String[] triggerStrings = {"POISONDECREPT"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Poison_Decreptifier();}
	
	protected int POISON_TICKS(){return 25;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 5;}
	protected String POISON_DONE(){return "The poison runs its course.";}
	protected String POISON_START(){return "^G<S-NAME> seem(s) weakened!^?";}
	protected String POISON_AFFECT(){return "^G<S-NAME> shiver(s) weakly.";}
	protected String POISON_CAST(){return "^F<S-NAME> poison(s) <T-NAMESELF>!^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return (invoker!=null)?Dice.roll(1,3,1):0;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-10);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.CONSTITUTION,1);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)-10);
		if(affectableStats.getStat(CharStats.STRENGTH)<=0)
			affectableStats.setStat(CharStats.STRENGTH,1);
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)-10);
		if(affectableStats.getStat(CharStats.DEXTERITY)<=0)
			affectableStats.setStat(CharStats.DEXTERITY,1);
	}
}
