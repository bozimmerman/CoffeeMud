package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Dwarven extends Language
{
	public String ID() { return "Dwarven"; }
	public String name(){ return "Dwarven";}
	public static Vector wordLists=null;	
	private static boolean mapped=false;
	public Dwarven()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Dwarven();}
	
	public Vector translationVector()
	{ 
		if(wordLists==null)
		{
			String[] one={"o"};
			String[] two={"`ai","`oi","`ul"};
			String[] three={"aya","dum","mim","oyo","tum"};
			String[] four={"menu","bund","ibun","khim","nala","rukhs","dumu","zirik","gunud","gabil","gamil"};
			String[] five={"kibil","celeb","mahal","narag","zaram","sigin","tarag","uzbad","zigil","zirak","aglab","baraz","baruk","bizar","felak"};
			String[] six={"azanul","bundushathur","morthond","felagund","gabilan","ganthol","khazad","kheled","khuzud","mazarbul","khuzdul"};
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
