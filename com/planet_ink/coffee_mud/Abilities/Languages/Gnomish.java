package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Gnomish extends Language
{
	public static Vector wordLists=null;	
	public Gnomish()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Gnomish";
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}
	public Environmental newInstance()
	{
		return new Gnomish();
	}
	public Vector translationVector()
	{ 
		if(wordLists==null)
		{
			String[] one={"y"};
			String[] two={"te","it","at","to"};
			String[] three={"nep","tem","tit","nip","pop","pon","upo","wip","pin"};
			String[] four={"peep","meep","neep","pein","nopo","popo","woop","weep","teep","teet"};
			String[] five={"whemp","thwam","nippo","punno","upoon","teepe","tunno","ponno","twano","ywhap"};
			String[] six={"tawhag","ponsol","paleep","ponpopol","niptittle","minwap","tinmipmip","niptemtem","wipwippoo"};
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