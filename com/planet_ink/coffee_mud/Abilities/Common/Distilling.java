package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Distilling extends Cooking
{
	public String ID() { return "Distilling"; }
	public String name(){ return "Distilling";}
	private static final String[] triggerStrings = {"DISTILLING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String cookWordShort(){return "distill";};
	public String cookWord(){return "distilling";};
	public Environmental newInstance(){	return new Distilling();}
	public boolean honorHerbs(){return false;}
	private static boolean myMapped=false;

	public Distilling()
	{
		super();
		if(ID().equals("Distilling")&&(!myMapped))
		{myMapped=true; CMAble.addCharAbilityMapping("All",10,ID(),false);}
	}
	
	protected synchronized static Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("DISTILLING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"liquors.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Distilling","Recipes not found!");
			Resources.submitResource("DISTILLING RECIPES",V);
		}
		return V;
	}
}
