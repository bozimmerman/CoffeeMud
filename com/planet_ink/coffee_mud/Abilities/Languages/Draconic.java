package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Draconic extends Language
{
	public String ID() { return "Draconic"; }
	public String name(){ return "Draconic";}
	public static Vector wordLists=null;	
	private static boolean mapped=false;
	public Draconic()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Draconic();}
	
	public Vector translationVector()
	{ 
		if(wordLists==null)
		{
			String[] one={"y"};
			String[] two={"ve","ov","iv","si","es","se"};
			String[] three={"see","sev","ave","ces","ven","sod"};
			String[] four={"nirg","avet","sav`e","choc","sess","sens","vent","vens","sven","yans","vays"};
			String[] five={"splut","svets","fruite","dwagg","vrers","verrs","srens","swath","senys","varen"};
			String[] six={"choccie","svenren","yorens","vyrues","whyrie","vrysenso","forin","sinnes","sessis","uroven","xorers","nosees"};
			wordLists=new Vector();
			wordLists.addElement(one);
			wordLists.addElement(two);
			wordLists.addElement(three);
			wordLists.addElement(four);
			wordLists.addElement(five);
			wordLists.addElement(six);
		}
		return wordLists; 
	}
}
