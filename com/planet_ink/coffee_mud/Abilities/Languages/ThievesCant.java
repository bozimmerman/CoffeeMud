package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ThievesCant extends Language
{
	public static Vector wordLists=null;	
	public ThievesCant()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Thieves Cant";
		CMAble.addCharAbilityMapping("Thief",1,ID(),true);
		CMAble.addCharAbilityMapping("Bard",10,ID(),false);
	}
	public Environmental newInstance()
	{
		return new ThievesCant();
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