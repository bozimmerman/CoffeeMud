package com.planet_ink.coffee_mud.interfaces;

public interface DiseaseAffect extends Ability
{
	public final static int SPREAD_STD=1;
	public final static int SPREAD_CONTACT=2;
	public final static int SPREAD_PROXIMITY=4;
	public final static int SPREAD_CONSUMPTION=8;
	public final static int SPREAD_DAMAGE=16;
	public int spreadCode();
}
