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
	public Environmental newInstance(){	return new Play_Organs();}
	public String mimicSpell(){return "Spell_Lightning";}
	protected int canAffectCode(){return 0;}

}
