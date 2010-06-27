package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.Common.interfaces.CharStats;
import com.planet_ink.coffee_mud.Common.interfaces.PhyStats;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.interfaces.Physical;

public class Duergar extends Dwarf
{
	public String ID(){	return "Duergar"; }
	public String name(){ return "Duergar"; }
	private String[]culturalAbilityNames={"Dwarven","Mining","Undercommon","Spell_Invisibility","Spell_Grow"};
	private int[]culturalAbilityProficiencies={100,50,25,25,25};
	public String[] culturalAbilityNames(){return culturalAbilityNames;}
	public int[] culturalAbilityProficiencies(){return culturalAbilityProficiencies;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		int senses=affectableStats.sensesMask();
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(senses|PhyStats.CAN_SEE_DARK);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)-3);
		affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,affectableStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)-3);
		affectableStats.setStat(CharStats.STAT_SAVE_FIRE,affectableStats.getStat(CharStats.STAT_SAVE_FIRE)+15);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)+5);
		affectableStats.setStat(CharStats.STAT_SAVE_COLD,affectableStats.getStat(CharStats.STAT_SAVE_COLD)-15);
		affectableStats.setStat(CharStats.STAT_SAVE_WATER,affectableStats.getStat(CharStats.STAT_SAVE_WATER)-10);
		affectableStats.setStat(CharStats.STAT_SAVE_DISEASE,affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)-10);
	}
}
