package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Harmonicas extends Play_Instrument
{
	public String ID() { return "Play_Harmonicas"; }
	public String name(){ return "Harmonicas";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_HARMONICAS;}
	public Environmental newInstance(){	return new Play_Harmonicas();}
	public String mimicSpell(){return "Spell_Awe";}
	private static Ability theSpell=null;
	protected Ability getSpell()
	{
		if(theSpell!=null) return theSpell;
		if(mimicSpell().length()==0) return null;
		theSpell=CMClass.getAbility(mimicSpell());
		return theSpell;
	}

}
