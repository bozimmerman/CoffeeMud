package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_PeaceMaker extends Property
{
	public String ID() { return "Prop_PeaceMaker"; }
	public String name(){ return "Strike Neuralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}
	public Environmental newInstance(){	return new Prop_PeaceMaker();}
	public String accountForYourself()
	{ return "Peace Maker";	}

	public boolean okAffect(Affect affect)
	{
		if((Util.bset(affect.sourceCode(),affect.MASK_MALICIOUS))
		||(Util.bset(affect.targetCode(),affect.MASK_MALICIOUS))
		||(Util.bset(affect.othersCode(),affect.MASK_MALICIOUS)))
		{
			if(affect.source()!=null)
			{
				affect.source().tell("Nah, you feel too peaceful here.");
				if(affect.source().getVictim()!=null)
					affect.source().getVictim().makePeace();
				affect.source().makePeace();
			}
			affect.modify(affect.source(),affect.target(),affect.tool(),Affect.NO_EFFECT,"",Affect.NO_EFFECT,"",Affect.NO_EFFECT,"");
			return false;
		}
		return super.okAffect(affect);
	}
}
