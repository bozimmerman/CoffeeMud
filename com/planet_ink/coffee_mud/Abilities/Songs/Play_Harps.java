package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Harps extends Play_Instrument
{
	public String ID() { return "Play_Harps"; }
	public String name(){ return "Harps";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_HARPS;}
	public Environmental newInstance(){	return new Play_Harps();}
	public String mimicSpell(){return "Spell_Friends";}
	private static Ability theSpell=null;
	protected Ability getSpell()
	{
		if(theSpell!=null) return theSpell;
		if(mimicSpell().length()==0) return null;
		theSpell=CMClass.getAbility(mimicSpell());
		return theSpell;
	}
}
