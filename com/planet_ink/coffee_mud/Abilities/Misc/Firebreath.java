package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.interfaces.Environmental;
public class Firebreath extends Dragonbreath
{
	public String ID() { return "Firebreath"; }
	public String name(){ return "Firebreath";}
	public String text(){return "fire";}
	public void setMiscText(String newText){super.setMiscText(text());}
	public Environmental newInstance(){	return new Firebreath();}
}
