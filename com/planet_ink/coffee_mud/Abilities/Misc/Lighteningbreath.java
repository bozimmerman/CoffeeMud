package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.interfaces.Environmental;
public class Lighteningbreath extends Dragonbreath
{
	public String ID() { return "Lighteningbreath"; }
	public String name(){ return "Lighteningbreath";}
	public String text(){return "lightning";}
	public Environmental newInstance(){	return new Lighteningbreath();}
}
