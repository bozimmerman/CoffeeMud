package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Grass extends Dance
{
	public String ID() { return "Dance_Grass"; }
	public String name(){ return "Grass";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Dance_Grass();}
	public static Ability kick=null;
	protected String danceOf(){return name()+" Dance";}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
		affectableStats.setArmor(affectableStats.armor()-invoker().charStats().getStat(CharStats.CHARISMA));
	}

}
