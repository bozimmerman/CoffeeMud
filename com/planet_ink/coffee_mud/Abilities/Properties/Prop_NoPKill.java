package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NoPKill extends Property
{
	public String ID() { return "Prop_NoPKill"; }
	public String name(){ return "No Player Killing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}
	public Environmental newInstance(){	Prop_NoPKill newOne=new Prop_NoPKill();	newOne.setMiscText(text());	return newOne;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(((Util.bset(affect.sourceCode(),affect.MASK_MALICIOUS))
		||(Util.bset(affect.targetCode(),affect.MASK_MALICIOUS))
		||(Util.bset(affect.othersCode(),affect.MASK_MALICIOUS)))
			&&(affect.target()!=null)
			&&(affect.target() instanceof MOB)
		    &&(!((MOB)affect.target()).isMonster())
		    &&(!affect.source().isMonster()))
		{
			if(Util.s_int(text())==0)
			{
				affect.source().tell("Player killing is forbidden here.");
				affect.source().setVictim(null);
				return false;
			}
			int levelDiff=affect.source().envStats().level()-((MOB)affect.target()).envStats().level();
			if(levelDiff<0) levelDiff=levelDiff*-1;
			if(levelDiff>Util.s_int(text()))
			{
				affect.source().tell("Player killing is forbidden for characters whose level difference is greater than "+Util.s_int(text())+".");
				affect.source().setVictim(null);
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}
}