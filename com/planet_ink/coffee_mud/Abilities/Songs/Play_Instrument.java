package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Instrument extends Play
{
	public String ID() { return "Play_Instrument"; }
	public String name(){ return "Instruments";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_WOODS;}
	public Environmental newInstance(){	return new Play_Instrument();}
	public String mimicSpell(){return "";}
	
	protected void inpersistantAffect(MOB mob)
	{
		if(getSpell()!=null)
		{
			Vector chcommands=new Vector();
			chcommands.addElement(mob.name());
			((Ability)getSpell().copyOf()).invoke(invoker(),chcommands,null,true);
		}
	}
	
		
	protected String songOf()
	{
		if(instrument!=null)
			return instrument.name();
		else
			return name();
	}
	private static Ability theSpell=null;
	protected Ability getSpell()
	{
		if(theSpell!=null) return theSpell;
		if(mimicSpell().length()==0) return null;
		theSpell=CMClass.getAbility(mimicSpell());
		return theSpell;
	}
	public int quality()
	{
		if(getSpell()!=null) return getSpell().quality();
		return BENEFICIAL_OTHERS;
	}
	protected boolean persistantSong(){return false;}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
}
