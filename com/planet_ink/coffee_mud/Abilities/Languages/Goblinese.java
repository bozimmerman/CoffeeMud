package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Goblinese extends Language
{
	public String ID() { return "Goblinese"; }
	public String name(){ return "Goblinese";}
	public static Vector wordLists=null;
	private static boolean mapped=false;
	public Goblinese()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Vector translationVector()
	{
		if(wordLists==null)
		{
			String[] one={"i","klpt","ih","g"};
			String[] two={"te","il","ag","go"};
			String[] three={"nik","rem","tit","nip","pop","pon","ipi","wip","pec"};
			String[]
four={"perp","merp","nerp","pein","noog","gobo","koer","werp","terp","tert","grlt","Jrl","gran","kert"};
			String[]
five={"whamb","thwam","nipgo","pungo","upoin","krepe","tungo","pongo","twang","hrgap","splt","krnch","baam","poww"};
			String[]
six={"tawthak","krsplt","palpep","poopoo","dungdung","owwie","greepnak","tengak","grnoc","pisspiss","phlyyytt","plllb","hrangnok","ticktick","nurang"};
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
