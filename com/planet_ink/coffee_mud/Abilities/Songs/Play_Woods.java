package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Woods extends Play_Instrument
{
	public String ID() { return "Play_Woods"; }
	public String name(){ return "Wood Clappers";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_WOODS;}
	public String mimicSpell(){return "Spell_Shield";}
	private static Ability theSpell=null;
	protected Ability getSpell()
	{
		if(theSpell!=null) return theSpell;
		if(mimicSpell().length()==0) return null;
		theSpell=CMClass.getAbility(mimicSpell());
		return theSpell;
	}

}
