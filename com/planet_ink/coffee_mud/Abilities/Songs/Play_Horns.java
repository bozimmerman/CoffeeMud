package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Horns extends Play_Instrument
{
	public String ID() { return "Play_Horns"; }
	public String name(){ return "Horns";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_HORNS;}
	public Environmental newInstance(){	return new Play_Horns();}
	public String mimicSpell(){return "Spell_FaerieFire";}
}
