package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Resistance extends Property
{
	public Prop_Resistance()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Stuff Neutralizing";
	}
	public Environmental newInstance()
	{
		Prop_Resistance BOB=new Prop_Resistance();
		BOB.setMiscText(text());
		return BOB;
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if((affect.amITarget(mob))&&(!affect.wasModified())&&(mob.location()!=null))
		{
			if(!Prop_HaveResister.isOk(affect,this,mob))
				return false;
			Prop_HaveResister.resistAffect(affect,mob,this);
		}
		return super.okAffect(affect);
	}
}