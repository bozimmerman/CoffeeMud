package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin_DiseaseImmunity extends Paladin
{
	public String ID() { return "Paladin_DiseaseImmunity"; }
	public String name(){ return "Disease Immunity";}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(msg.targetMinor()==CMMsg.TYP_DISEASE)
		&&(!mob.amDead())
		&&(mob.getAlignment()>650)
		&&((invoker==null)||(invoker.fetchAbility(ID())==null)||profficiencyCheck(null,0,false)))
			return false;
		return super.okMessage(myHost,msg);
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((affected!=null)&&(affected.getAlignment()>650))
			affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+50+profficiency());
	}
}
