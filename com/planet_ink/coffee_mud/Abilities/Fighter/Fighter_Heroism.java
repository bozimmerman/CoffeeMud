package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Heroism extends StdAbility
{
	public String ID() { return "Fighter_Heroism"; }
	public String name(){ return "Heroism";}
	public String displayText(){ return "";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_Heroism();}
	public int classificationCode(){ return Ability.SKILL;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		MOB mob=(MOB)affected;

		if((!Sense.isSitting(mob))
		&&(Sense.isSleeping(mob))
		&&(mob.isInCombat())
		&&(Dice.rollPercentage()==1)
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(0,false))
		&&(tickID==MudHost.TICK_MOB))
			helpProfficiency(mob);
		return super.tick(ticking,tickID);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.SAVE_JUSTICE,
								(affectableStats.getStat(CharStats.CHARISMA)/4)
								+(affectableStats.getStat(CharStats.STRENGTH)/4)
								+(adjustedLevel(affected)/2));
	}
}