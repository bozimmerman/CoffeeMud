package com.planet_ink.coffee_mud.Races;

public class Alligator extends GreatLizard
{
	public String ID(){	return "Alligator"; }
	public String name(){ return "Alligator"; }
	private String[]racialAbilityNames={"Skill_Swim"};
	private int[]racialAbilityLevels={1};
	private int[]racialAbilityProfficiencies={100};
	private boolean[]racialAbilityQuals={false};
	protected String[] racialAbilityNames(){return racialAbilityNames;}
	protected int[] racialAbilityLevels(){return racialAbilityLevels;}
	protected int[] racialAbilityProfficiencies(){return racialAbilityProfficiencies;}
	protected boolean[] racialAbilityQuals(){return racialAbilityQuals;}
}
