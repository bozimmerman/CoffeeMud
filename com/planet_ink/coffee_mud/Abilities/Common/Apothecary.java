package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Apothecary extends Cooking
{
	public String ID() { return "Apothecary"; }
	public String name(){ return "Apothecary";}
	private static final String[] triggerStrings = {"APOTHECARY","MIX"};
	public String[] triggerStrings(){return triggerStrings;}
	public String cookWordShort(){return "mix";};
	public String cookWord(){return "mixing";};
	public Environmental newInstance(){	return new Apothecary();}
	public boolean honorHerbs(){return false;}

	
	protected synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("APOTHECARY RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"poisons.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Apothecary","Recipes not found!");
			Resources.submitResource("APOTHECARY RECIPES",V);
		}
		return V;
	}
}
