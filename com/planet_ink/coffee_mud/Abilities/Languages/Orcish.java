package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Orcish extends Language
{
	public String ID() { return "Orcish"; }
	public String name(){ return "Orcish";}
	public static Vector wordLists=null;	
	private static boolean mapped=false;
	public Orcish()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Orcish();}
	public Vector translationVector()
	{ 
		if(wordLists==null)
		{
			String[] one={"a"};
			String[] two={"uk","ik","og","eg","ak","ag"};
			String[] three={"uko","ugg","ick","ehk","akh","oog"};
			String[] four={"blec","mugo","guck","gook","kill","dead","twak","kwat","klug"};
			String[] five={"bleko","thwak","klarg","gluck","kulgo","mucka","splat","kwath","garth","blark"};
			String[] six={"kalarg","murder","bleeke","kwargh","guttle","thungo"};
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
