package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class MasterWeaponsmithing extends Weaponsmithing
{
	public String ID() { return "MasterWeaponsmithing"; }
	public Environmental newInstance(){	return new MasterWeaponsmithing();}
	public String name(){ return "Master Weaponsmithing";}
	private static final String[] triggerStrings = {"MWEAPONSMITH","MASTERWEAPONSMITHING"};
	public String[] triggerStrings(){return triggerStrings;}
	private static boolean mapped2=false;
	
	public MasterWeaponsmithing()
	{
		if(!mapped2){mapped2=true;
					CMAble.addCharAbilityMapping("All",30,ID(),false);}
	}

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("MASTERWEAPONSMITHING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"masterweaponsmith.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("MasterWeaponsmithing","Recipes not found!");
			Resources.submitResource("MASTERWEAPONSMITHING RECIPES",V);
		}
		return V;
	}
	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Weaponsmithing")==null)
		{
			teacher.tell(student.name()+" has not yet learned weaponsmithing.");
			student.tell("You need to learn weaponsmithing before you can learn "+name()+".");
			return false;
		}

		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
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
			commonTell(mob,"Make what? Enter \"mweaponsmith list\" for a list, \"mweaponsmith scan\", or \"mweaponsmith mend <item>\".");
			return false;
		}
		if(autoGenerate>0)
			commands.insertElementAt(new Integer(autoGenerate),0);
		return super.invoke(mob,commands,givenTarget,auto);
	}
	
}