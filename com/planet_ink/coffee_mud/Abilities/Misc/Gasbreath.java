package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.interfaces.Environmental;
public class Gasbreath extends Dragonbreath
{
	public String ID() { return "Gasbreath"; }
	public String name(){ return "Gasbreath";}
	public String text(){return "gas";}
	public void setMiscText(String newText){super.setMiscText(text());}
	public Environmental newInstance(){	return new Gasbreath();}
}
