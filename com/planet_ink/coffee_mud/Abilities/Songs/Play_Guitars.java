package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Guitars extends Play_Instrument
{
	public String ID() { return "Play_Guitars"; }
	public String name(){ return "Guitars";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_GUITARS;}
	public Environmental newInstance(){	return new Play_Guitars();}
	public String mimicSpell(){return "Spell_Fireball";}
	protected int canAffectCode(){return 0;}

}
