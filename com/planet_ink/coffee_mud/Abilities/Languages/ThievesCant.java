package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ThievesCant extends Language
{
	public String ID() { return "ThievesCant"; }
	public String name(){ return "Thieves Cant";}
	public static Vector wordLists=null;
	private static boolean mapped=false;
	public ThievesCant()
	{
		super();
		if(!mapped){mapped=true;
				CMAble.addCharAbilityMapping("Bard",10,ID(),false);}
	}

	public Vector translationVector()
	{
		if(wordLists==null)
		{
			String[] one={"a","i"};
			String[] two={"to","do","it","at","on"};
			String[] three={"tip","dab","fat","ken","leg","rum","sly","dub"};
			String[] four={"arch","buck","bulk","adam","door","back","bear","beef","bell","cove","cull","hank","gull","jack","lily","mort","nask","prig","bite","fine","gelt","stag","stam","stow","wink"};
			String[] five={"nasty","abram","rogue","royal","blood","bluff","break","teeth","blade","fitch","purse","burnt","chink","chive","chife","clear","drunk","court","cramp","flash","glaze","sharp","ketch","merry","stool","peery","board","queer","boots","smear","smoke","snapt","flash","unrig","whack"};
			String[] six={"baggage","sodomite","banging","battle","garden","beater","beggar","bobbed","bracket","brother","poxed","canters","cousins","money","clanker","damber","fencing","gentry","glimflashy","lavender","jemmy","nubbing","penance","pothooks","rigging","trumps","weeping"};
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