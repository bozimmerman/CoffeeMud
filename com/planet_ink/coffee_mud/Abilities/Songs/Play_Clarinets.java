package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Clarinets extends Play_Instrument
{
	public String ID() { return "Play_Clarinets"; }
	public String name(){ return "Clarinets";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_CLARINETS;}
	public Environmental newInstance(){	return new Play_Clarinets();}
	public String mimicSpell(){return "Spell_ShockingGrasp";}
	protected int canAffectCode(){return 0;}

}
