package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Poison_Bloodboil extends Poison
{
	public String ID() { return "Poison_Bloodboil"; }
	public String name(){ return "Blood Boil";}
	private static final String[] triggerStrings = {"POISONBURN"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Poison_Bloodboil();}
	
	protected int POISON_TICKS(){return 20;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 2;}
	protected String POISON_DONE(){return "Your blood stops burning.";}
	protected String POISON_START(){return "^R<S-NAME> turn(s) red.^?";}
	protected String POISON_AFFECT(){return "<S-NAME> cringe(s) as <S-HIS-HER> blood burns.";}
	protected String POISON_CAST(){return "^F<S-NAME> sting(s) <T-NAMESELF>!^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to sting <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return (invoker!=null)?Dice.roll(1,2,0):0;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof MOB)
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-20);
	}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-1);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)-5);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.CONSTITUTION,1);
		if(affectableStats.getStat(CharStats.STRENGTH)<=0)
			affectableStats.setStat(CharStats.STRENGTH,1);
	}
}
