package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.interfaces.Environmental;
public class Acidbreath extends Dragonbreath
{
	public String ID() { return "Acidbreath"; }
	public String name(){ return "Acidbreath";}
	public String text(){return "acid";}
	public void setMiscText(String newText){super.setMiscText(text());}
}
