package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Organs extends Play_Instrument
{
	public String ID() { return "Play_Organs"; }
	public String name(){ return "Organs";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_ORGANS;}
	public String mimicSpell(){return "Prayer_ProtectHealth";}
	protected int canAffectCode(){return 0;}
	private static Ability theSpell=null;
	protected Ability getSpell()
	{
		if(theSpell!=null) return theSpell;
		if(mimicSpell().length()==0) return null;
		theSpell=CMClass.getAbility(mimicSpell());
		return theSpell;
	}

}
