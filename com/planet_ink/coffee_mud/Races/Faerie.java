package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;

public class Faerie extends SmallElfKin
{
	public String ID(){	return "Faerie"; }
	public String name(){ return "Faerie"; }
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,2 };
	public int[] bodyMask(){return parts;}
	protected String[] racialAbilityNames={"WingedFlying"};
	protected int[] racialAbilityLevels={1};
	protected int[] racialAbilityProfficiencies={100};
	protected boolean[] racialAbilityQuals={false};
	
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}
}
