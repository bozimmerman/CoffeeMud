package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_PeaceMaker extends Property
{
	public String ID() { return "Prop_PeaceMaker"; }
	public String name(){ return "Strike Neuralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_PeaceMaker();}
	public String accountForYourself()
	{ return "Peace Maker";	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((Util.bset(affect.sourceCode(),affect.MASK_MALICIOUS))
		||(Util.bset(affect.targetCode(),affect.MASK_MALICIOUS))
		||(Util.bset(affect.othersCode(),affect.MASK_MALICIOUS)))
		{
			if((affect.source()!=null)
			&&(affect.target()!=null)
			&&(affect.source()!=affected)
			&&(affect.source()!=affect.target()))
			{
				if(affected instanceof MOB)
				{
					MOB mob=(MOB)affected;
					if((Sense.aliveAwakeMobile(mob,true))
					&&(!mob.isInCombat()))
					{
						String t="No fighting!";
						if(text().length()>0)
						{
							Vector V=Util.parseSemicolons(text(),true);
							t=(String)V.elementAt(Dice.roll(1,V.size(),-1));
						}
						ExternalPlay.quickSay(mob,affect.source(),t,false,false);
					}
					else
						return super.okAffect(myHost,affect);
				}
				else
				{
					String t="You feel too peaceful here.";
					if(text().length()>0)
					{
						Vector V=Util.parseSemicolons(text(),true);
						t=(String)V.elementAt(Dice.roll(1,V.size(),-1));
					}
					affect.source().tell(t);
				}
				if(affect.source().getVictim()!=null)
					affect.source().getVictim().makePeace();
				affect.source().makePeace();
				affect.modify(affect.source(),affect.target(),affect.tool(),Affect.NO_EFFECT,"",Affect.NO_EFFECT,"",Affect.NO_EFFECT,"");
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}
}
