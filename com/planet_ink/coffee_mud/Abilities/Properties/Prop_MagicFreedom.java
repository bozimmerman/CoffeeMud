package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_MagicFreedom extends Property
{
	public String ID() { return "Prop_MagicFreedom"; }
	public String name(){ return "Magic Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}
	public Environmental newInstance(){	return new Prop_MagicFreedom();}

	public String accountForYourself()
	{ return "Anti-Magic Field";	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((Util.bset(affect.sourceCode(),affect.MASK_MAGIC))
		||(Util.bset(affect.targetCode(),affect.MASK_MAGIC))
		||(Util.bset(affect.othersCode(),affect.MASK_MAGIC)))
		{
			Room room=null;
			if((affect.target()!=null)
			&&(affect.target() instanceof MOB)
			&&(((MOB)affect.target()).location()!=null))
				room=((MOB)affect.target()).location();
			else
			if((affect.source()!=null)
			&&(affect.source().location()!=null))
				room=affect.source().location();
			if(room!=null)
				room.showHappens(Affect.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}
}
