package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin_PoisonImmunity extends Paladin
{
	public String ID() { return "Paladin_PoisonImmunity"; }
	public String name(){ return "Poison Immunity";}
	public Environmental newInstance(){	return new Paladin_PoisonImmunity();}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(affect.targetMinor()==Affect.TYP_POISON)
		&&(!mob.amDead())
		&&(mob.getAlignment()>650)
		&&(profficiencyCheck(0,false)))
			return false;
		return super.okAffect(myHost,affect);
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((affected!=null)&&(affected.getAlignment()>650))
			affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+50+profficiency());
	}
}
