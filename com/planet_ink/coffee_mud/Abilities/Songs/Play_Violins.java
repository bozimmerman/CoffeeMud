package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Violins extends Play_Instrument
{
	public String ID() { return "Play_Violins"; }
	public String name(){ return "Violins";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_VIOLINS;}
	public String mimicSpell(){return "Spell_Frost";}
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
