package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;


public class CageBuilding extends Wainwrighting
{
	public String ID() { return "CageBuilding"; }
	public String name(){ return "Cage Building";}
	private static final String[] triggerStrings = {"BUILDCAGE","CAGEBUILDING"};
	public String[] triggerStrings(){return triggerStrings;}
	public CageBuilding()
	{
	}
	public Environmental newInstance(){return new CageBuilding();}
	
	protected synchronized static Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("CAGEBUILD RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"cagebuilding.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("CageBuilding","Recipes not found!");
			Resources.submitResource("CAGEBUILD RECIPES",V);
		}
		return V;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Build what? Enter \"buildcage list\" for a list.");
			return false;
		}
		return super.invoke(mob,commands,givenTarget,auto);
	}
}
