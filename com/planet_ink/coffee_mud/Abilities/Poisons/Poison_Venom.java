package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Poison_Venom extends Poison
{
	public String ID() { return "Poison_Venom"; }
	public String name(){ return "Venom";}
	private static final String[] triggerStrings = {"POISONBITE"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Poison_Venom();}
	
	protected int POISON_TICKS(){return 10;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 2;}
	protected String POISON_DONE(){return "The venom runs its course.";}
	protected String POISON_START(){return "^G<S-NAME> turn(s) green.^?";}
	protected String POISON_AFFECT(){return "<S-NAME> cringe(s) as the venom courses through <S-HIS-HER> blood.";}
	protected String POISON_CAST(){return "^F<S-NAME> bite(s) <T-NAMESELF>!^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to bite <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return (invoker!=null)?Dice.roll(1,19,1):0;}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-7);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)-3);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.CONSTITUTION,1);
		if(affectableStats.getStat(CharStats.STRENGTH)<=0)
			affectableStats.setStat(CharStats.STRENGTH,1);
	}
}
