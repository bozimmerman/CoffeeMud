package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RestrictSpells extends Property
{
	public String ID() { return "Prop_RestrictSpells"; }
	public String name(){ return "Specific Spell Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	private CharStats adjCharStats=null;
	public Environmental newInstance(){	return new Prop_RestrictSpells();}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(text().toUpperCase().indexOf(affect.tool().ID().toUpperCase())>=0))
		{
			Room roomS=null;
			Room roomD=null;
			if((affect.target()!=null)&&(affect.target() instanceof MOB)&&(((MOB)affect.target()).location()!=null))
				roomD=((MOB)affect.target()).location();
			else
			if((affect.source()!=null)&&(affect.source().location()!=null))
				roomS=affect.source().location();
			else
			if((affect.target()!=null)&&(affect.target() instanceof Room))
				roomD=(Room)affect.target();

			if((roomS!=null)&&(roomD!=null)&&(roomS==roomD))
				roomD=null;

			if(roomS!=null)
				roomS.showHappens(Affect.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			if(roomD!=null)
				roomD.showHappens(Affect.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			if((affect.source()!=null)
			&&(affect.source().location()!=null)
			&&(affect.source().location()!=roomS)
			&&(affect.source().location()!=roomD))
				affect.source().location().showHappens(Affect.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}
}
