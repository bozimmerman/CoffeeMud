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
	public Environmental newInstance(){	return new Play_Woods();}
	public String mimicSpell(){return "Spell_Shield";}

}
