package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.Common.interfaces.CharStats;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

public class Duergar extends Dwarf
{
	public String ID(){	return "Duergar"; }
	public String name(){ return "Duergar"; }
	private String[]culturalAbilityNames={"Dwarven","Mining","Undercommon","Spell_Invisibility","Spell_Grow"};
	private int[]culturalAbilityProficiencies={100,50,25,25,25};
	public String[] culturalAbilityNames(){return culturalAbilityNames;}
	public int[] culturalAbilityProficiencies(){return culturalAbilityProficiencies;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)-2);
		affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,affectableStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)-2);
		affectableStats.setStat(CharStats.STAT_SAVE_FIRE,affectableStats.getStat(CharStats.STAT_SAVE_FIRE)+15);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)+10);
	}
}
