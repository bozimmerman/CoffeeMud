package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class MasterArmorsmithing extends Armorsmithing
{
	public String ID() { return "MasterArmorsmithing"; }
	public String name(){ return "Master Armorsmithing";}
	private static final String[] triggerStrings = {"MARMORSMITH","MASTERARMORSMITHING"};
	public String[] triggerStrings(){return triggerStrings;}

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("MASTERARMORSMITHING RECIPES");
		if(V==null)
		{
			StringBuffer
			str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"masterarmorsmith.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("MasterArmorsmithing","Recipes not found!");
			Resources.submitResource("MASTERARMORSMITHING RECIPES",V);
		}
		return V;
	}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Armorsmithing")==null)
		{
			teacher.tell(student.name()+" has not yet learned armorsmithing.");
			student.tell("You need to learn armorsmithing before you can learn "+name()+".");
			return false;
		}

		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget,boolean auto)
	{
		int autoGenerate=0;

			if((auto)&&(givenTarget==this)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			autoGenerate=((Integer)commands.firstElement()).intValue();
			commands.removeElementAt(0);
			givenTarget=null;
		}
		randomRecipeFix(mob,loadRecipes(),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"marmorsmith list\" for a list,\"marmorsmith scan\", or \"marmorsmith mend <item>\".");
			return false;
		}
		if(autoGenerate>0)
			commands.insertElementAt(new Integer(autoGenerate),0);
		return super.invoke(mob,commands,givenTarget,auto);
	}
}
