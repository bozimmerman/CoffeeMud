package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Knowledge extends Song
{
	public String ID() { return "Song_Knowledge"; }
	public String name(){ return "Knowledge";}
	public String displayText(){ return "(Song of Knowledge)";}
	public int quality(){ return OK_OTHERS;}
	public Environmental newInstance(){	return new Song_Knowledge();}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		affectableStats.setStat(CharStats.WISDOM,(int)Math.round(affectableStats.getStat(CharStats.WISDOM)+2));
		affectableStats.setStat(CharStats.INTELLIGENCE,(int)Math.round(affectableStats.getStat(CharStats.INTELLIGENCE)+2));
	}
}
