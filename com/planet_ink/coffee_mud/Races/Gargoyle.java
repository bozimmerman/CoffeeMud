package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Gargoyle extends StoneGolem
{
	public String ID(){	return "Gargoyle"; }
	public String name(){ return "Gargoyle"; }
	protected String[] racialAbilityNames={"WingedFlying"};
	protected int[] racialAbilityLevels={1};
	protected int[] racialAbilityProfficiencies={100};
	protected boolean[] racialAbilityQuals={false};
	
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,2 };
	public int[] bodyMask(){return parts;}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}
}
