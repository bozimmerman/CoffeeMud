package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Ballad extends Play
{
	public String ID() { return "Play_Ballad"; }
	public String name(){ return "Ballad";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String songOf(){return "a "+name();}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		// the sex rules
		if(!(affected instanceof MOB)) return;

		MOB myChar=(MOB)affected;
		if((msg.target()!=null)&&(msg.target() instanceof MOB))
		{
			MOB mate=(MOB)msg.target();
			if((msg.amISource(myChar))
			&&(msg.tool()!=null)
			&&(msg.tool().ID().equals("Social"))
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>"))
			&&(myChar.charStats().getStat(CharStats.GENDER)!=mate.charStats().getStat(CharStats.GENDER))
			&&((mate.charStats().getStat(CharStats.GENDER)==((int)'M'))
			   ||(mate.charStats().getStat(CharStats.GENDER)==((int)'F')))
			&&((myChar.charStats().getStat(CharStats.GENDER)==((int)'M'))
			   ||(myChar.charStats().getStat(CharStats.GENDER)==((int)'F')))
			&&((myChar.charStats().getMyRace().ID().equals("Human"))
			   ||(mate.charStats().getMyRace().ID().equals("Human"))
			   ||(mate.charStats().getMyRace().ID().equals(ID())))
			&&(myChar.location()==mate.location())
			&&(myChar.numWearingHere(Item.ON_LEGS)==0)
			&&(mate.numWearingHere(Item.ON_LEGS)==0)
			&&(myChar.numWearingHere(Item.ON_WAIST)==0)
			&&(mate.numWearingHere(Item.ON_WAIST)==0))
			{
				MOB female=myChar;
				MOB male=mate;
				if((mate.charStats().getStat(CharStats.GENDER)==((int)'F')))
				{
					female=mate;
					male=myChar;
				}
				Ability A=CMClass.getAbility("Pregnancy");
				if((A!=null)
				&&(female.fetchAbility(A.ID())==null)
				&&(female.fetchEffect(A.ID())==null))
				{
					A.invoke(male,female,true);
					unInvoke();
				}
			}
		}
	}
	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		if(invoker()!=null)
			stats.setStat(CharStats.SAVE_MIND,stats.getStat(CharStats.SAVE_MIND)+(invokerLevel()*1));
	}
	public void affectEnvStats(Environmental mob, EnvStats stats)
	{
		super.affectEnvStats(mob,stats);
		if(invoker()!=null)
			stats.setAttackAdjustment(stats.attackAdjustment()+invoker().charStats().getStat(CharStats.CHARISMA)+(invokerLevel()/2));
	}
}
