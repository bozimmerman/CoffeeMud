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
	public Environmental newInstance(){	return new Prop_RestrictSpells();}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(text().toUpperCase().indexOf(msg.tool().ID().toUpperCase())>=0))
		{
			Room roomS=null;
			Room roomD=null;
			if((msg.target()!=null)&&(msg.target() instanceof MOB)&&(((MOB)msg.target()).location()!=null))
				roomD=((MOB)msg.target()).location();
			else
			if((msg.source()!=null)&&(msg.source().location()!=null))
				roomS=msg.source().location();
			else
			if((msg.target()!=null)&&(msg.target() instanceof Room))
				roomD=(Room)msg.target();

			if((roomS!=null)&&(roomD!=null)&&(roomS==roomD))
				roomD=null;

			if(roomS!=null)
				roomS.showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			if(roomD!=null)
				roomD.showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			if((msg.source()!=null)
			&&(msg.source().location()!=null)
			&&(msg.source().location()!=roomS)
			&&(msg.source().location()!=roomD))
				msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}
}
