package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.interfaces.*;

public class Ranger_Enemy4 extends Ranger_Enemy1
{
	public String ID() { return "Ranger_Enemy4"; }
	public String name(){ return "Favored Enemy 4";}
	public Environmental newInstance(){	Ranger_Enemy4 BOB=new Ranger_Enemy4();	BOB.setMiscText(text()); return BOB;}
}
