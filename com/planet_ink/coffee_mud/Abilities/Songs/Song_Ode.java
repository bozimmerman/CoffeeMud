package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Ode extends Song
{
	public String ID() { return "Song_Ode"; }
	public String name(){ return "Ode";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Song_Ode();	}
	public MOB whom=null;
	protected String songOf(){ return "Ode"+((whom==null)?"":" to "+whom.name())+"";}
	
	public Hashtable getSongs()
	{
		Hashtable H=new Hashtable();
		return H;
	}
	
}
