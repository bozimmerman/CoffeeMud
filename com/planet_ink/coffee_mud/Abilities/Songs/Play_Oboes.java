package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Oboes extends Play_Instrument
{
	public String ID() { return "Play_Oboes"; }
	public String name(){ return "Oboes";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_OBOES;}
	public String mimicSpell(){return "Spell_AcidArrow";}
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
