package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Elvish extends Language
{
	public String ID() { return "Elvish"; }
	public String name(){ return "Elvish";}
	public static Vector wordLists=null;	
	private static boolean mapped=false;
	public Elvish()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Elvish();}
	public Vector translationVector()
	{ 
		if(wordLists==null)
		{
			String[] one={"i"};
			String[] two={"os","vi","ne","ye","vo"};
			String[] three={"apa","`ana","cil","sar","tan","hel","loa","sir","hep","yur","`nol","hol","qua"};
			String[] four={"s`eya","qual","quel","lara","uqua","sana","yava","masse","yanna","quettaparma","manna","manan","merme","carma","harno","harne","varno","essar","saira","cilta","veuma","norta","turme","saita"};
			String[] five={"cuiva","cuina","nonwa","imire","nauta","cilta","entuc","norta","latin","l`otea","veuya","veuro","apama","hampa","nurta","firta","saira","holle","herwa","uquen","arcoa","calte","cemma","hanta","tanen"};
			String[] six={"mahtale","porisalque","hairie","tararan","ambarwa","latina","ol`otie","amawil","apacen","yavinqua","apalume","linquilea","menelwa","alassea","nurmea","parmasse","ceniril","heldasse","imirin","earina","calatengew","lapselunga","rianna","eneques"};
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
