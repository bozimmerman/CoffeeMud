package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

/*
	Modified by Xalan of Sancara
	www.Sancara.co.uk
	Sancara.servegame.com Port 5555
	Respect to all who work on Coffee, keep the tradition going!
*/

public class ThievesCant extends StdLanguage
{
	@Override
	public String ID()
	{
		return "ThievesCant";
	}

	private final static String localizedName = CMLib.lang().L("Thieves Cant");

	@Override
	public String name()
	{
		return localizedName;
	}

	public static List<String[]> wordLists=null;
	private static boolean mapped=false;
	public ThievesCant()
	{
		super();
		if (!mapped)
		{
			mapped = true;
			CMLib.ableMapper().addCharAbilityMapping("Bard", 10, ID(), false);
		}
	}

	@Override
	public List<String[]> translationVector(String language)
	{
		if(wordLists==null)
		{
			final String[] one={"a","i"};
			final String[] two={"to","do","it","at","on"};
			final String[] three={"tip","dab","fat","ken","leg","rum","sly","dub"};
			final String[] four={"arch","buck","bulk","adam","door","back","bear","beef","bell","cove","cull","hank","gull","jack","lily","mort","nask","prig","bite","fine","gelt","stag","stam","stow","wink"};
			final String[] five={"nasty","abram","rogue","royal","blood","bluff","break","teeth","blade","fitch","purse","burnt","chink","chive","chife","clear","drunk","court","cramp","flash","glaze","sharp","ketch","merry","stool","peery","board","queer","boots","smear","smoke","snapt","flash","unrig","whack"};
			final String[] six={"baggage","sodomite","banging","battle","garden","beater","beggar","bobbed","bracket","brother","poxed","canters","cousins","money","clanker","damber","fencing","gentry","glimflashy","lavender","jemmy","nubbing","penance","pothooks","rigging","trumps","weeping"};
			wordLists=new Vector<String[]>();
			wordLists.add(one);
			wordLists.add(two);
			wordLists.add(three);
			wordLists.add(four);
			wordLists.add(five);
			wordLists.add(six);
		}
		return wordLists;
	}

	private static final Hashtable<String,String> hashwords=new Hashtable<String,String>();

	@Override
	public Map<String, String> translationHash(String language)
	{
		if((hashwords!=null)&&(hashwords.size()>0))
			return hashwords;
		hashwords.put("ABANDONED PERSON","lurch");
		hashwords.put("ABANDONED","blasted");
		hashwords.put("ACCOMPLICE","hired help");
		hashwords.put("ARRESTED","boned");
		hashwords.put("ASSASSIN","hit man");
		hashwords.put("ASSASSINATE","shoulder tap");
		hashwords.put("BACKSTAB","shoulder tap");
		hashwords.put("BEER","bowse");
		hashwords.put("BEGGER","abram man");
		hashwords.put("BEGGARS","abram men");
		hashwords.put("BEGGING","angling for coppers");
		hashwords.put("BOAT","ark");
		hashwords.put("BODY","quarromes");
		hashwords.put("GRAVE ROBBERS","ressurection men");
		hashwords.put("BOOTY","swag");
		hashwords.put("BRANDY","bingo");
		hashwords.put("BRIBE","expense money");
		hashwords.put("BROTHEL","academy");
		hashwords.put("BURGLAR","rounder");
		hashwords.put("BURGLARY","visitin' the neighbors");
		hashwords.put("CALF","blater");
		hashwords.put("CALL FOR HELP","cry beef");
		hashwords.put("CART","gaoler's coach");
		hashwords.put("CLIMB","taking a big step");
		hashwords.put("CLOAK","calle");
		hashwords.put("CLUB","oaken towel");
		hashwords.put("COACH MAN","Rattling cove");
		hashwords.put("FAKE COIN","button");
		hashwords.put("FAKE COINS","buttons");
		hashwords.put("COMMITING CRIMES","rooton'");
		hashwords.put("CON","hoodwink");
		hashwords.put("CONNED","hoodwinked");
		hashwords.put("CON MAN","jack in the box");
		hashwords.put("VICTIM","pigeon");
		hashwords.put("CON ARTIST","pigeon plucker");
		hashwords.put("COUNTERFEITER","figure dancer");
		hashwords.put("COUNTERFEITED","confect");
		hashwords.put("COUNTERFEITING","drawing the kings picture");
		hashwords.put("COURTHOUSE","nubbing ken");
		hashwords.put("BEING TRANSPORTED","barrow man");
		hashwords.put("IN THE STOCKS","babes in the wood");
		hashwords.put("WITH BURNED HANDS","badge");
		hashwords.put("CROWBAR","ginny");
		hashwords.put("DAGGER","pig poker");
		hashwords.put("DANGER","lay");
		hashwords.put("DEATH SENTENCE","cramp word");
		hashwords.put("DAY","lightmans");
		hashwords.put("DIE","goin' legit");
		hashwords.put("DOG","bufe");
		hashwords.put("DOOR","lid");
		hashwords.put("DRUNK","clear");
		hashwords.put("ESCAPE","hike");
		hashwords.put("FAKE SICK","sham abram");
		hashwords.put("FENCE","uncle");
		hashwords.put("FLOGGING","getting stripes");
		hashwords.put("FOLLOW","drag");
		hashwords.put("FOOL","gudgeon");
		hashwords.put("GALLOWS","three legged stool");
		hashwords.put("GANG","birds of a feather");
		hashwords.put("GYPSIES","cattle");
		hashwords.put("GLOVES","farms");
		hashwords.put("GLOVE","farm");
		hashwords.put("GOOD","rum");
		hashwords.put("GUARD","pig");
		hashwords.put("HEAD OF THE GUARD","papa poker");
		hashwords.put("GUILD ELDER","made-guy");
		hashwords.put("HANGED","frummagemmed");
		hashwords.put("HEALER","tinker");
		hashwords.put("HELLO","knock-knock");
		hashwords.put("HIDE","shadow dance");
		hashwords.put("HIDEOUT","dive");
		hashwords.put("HIGHWAY","pad");
		hashwords.put("HIGHWAYMAN","knight of the road");
		hashwords.put("HIGHWAY ROBBERY","recruiting service");
		hashwords.put("HIT","click");
		hashwords.put("HONEST MAN","cull");
		hashwords.put("HOUSE","panny");
		hashwords.put("HOUSEBREAKER","rushers");
		hashwords.put("ILLEGAL GOODS","contraband");
		hashwords.put("INFORM","cackle");
		hashwords.put("INFORMTANT","a hole in need of plugging");
		hashwords.put("INJURY","scratch");
		hashwords.put("WOUND","beef");
		hashwords.put("INNOCENT","dodgies");
		hashwords.put("INJURE","scratched");
		hashwords.put("JAIL","guesthouse");
		hashwords.put("PRISON","guesthouse");
		hashwords.put("JAIL HOUSE","guesthouse");
		hashwords.put("JAIL BREAK","goin' home");
		hashwords.put("JUDGE","fortune teller");
		hashwords.put("KILL","have a chat with");
		hashwords.put("KILLED","had a chat with");
		hashwords.put("KNOCKED OUT","tucked into bed");
		hashwords.put("LANTERN","darkee");
		hashwords.put("LARCENY","racket");
		hashwords.put("LAWYER","cursitor");
		hashwords.put("LEAD","moss");
		hashwords.put("LICENSE","jukrum");
		hashwords.put("LIE","amuse");
		hashwords.put("LIED","amused");
		hashwords.put("LOCK","joke");
		hashwords.put("LOCKPICK","punchline");
		hashwords.put("LOCKED","joked");
		hashwords.put("LOOKOUT","pair of eyes");
		hashwords.put("LOCKPICKER","gilt");
		hashwords.put("LOCKPICKING","black art");
		hashwords.put("LOOT","score");
		hashwords.put("LOSE","dropping");
		hashwords.put("LOST","dropped");
		hashwords.put("HAND","glove");
		hashwords.put("MAGIC","flash");
		hashwords.put("MAGIC ITEM","bagged flash");
		hashwords.put("MAN","cove");
		hashwords.put("MISTRESS","nypper");
		hashwords.put("NIGHT WATCHMAN","moon pig");
		hashwords.put("NIGHT GUARD","moon pig");
		hashwords.put("OPEN","tell a joke");
		hashwords.put("PICKPOCKET","purse collector");
		hashwords.put("PLAN","signals");
		hashwords.put("PLANNING","flag waving");
		hashwords.put("PRISONER","hizzoner's guest");
		hashwords.put("QUIET","smooth");
		hashwords.put("RICH","oak");
		hashwords.put("RING","onion");
		hashwords.put("RIOT","hubbub");
		hashwords.put("RISK","boredom");
		hashwords.put("ROPE","danglestuff");
		hashwords.put("SECURE","rug");
		hashwords.put("SELL","switch");
		hashwords.put("SHACKLES","king's plate");
		hashwords.put("SHOPLIFTER","bob");
		hashwords.put("STAFF","jordain");
		hashwords.put("STEAL","borrow");
		hashwords.put("STOLE","borrowed");
		hashwords.put("STOCKS","parenthesis");
		hashwords.put("STOLEN","made");
		hashwords.put("SWAP","ring the changes");
		hashwords.put("TARGET","mark");
		hashwords.put("TAVERN","beggar maker");
		hashwords.put("BAR","beggar maker");
		hashwords.put("PUB","beggar maker");
		hashwords.put("THEFT","game");
		hashwords.put("TIE UP","dress up");
		hashwords.put("THEIF","gentleman");
		hashwords.put("THIEVES","gentlemen");
		hashwords.put("GUILD MASTER","upright man");
		hashwords.put("SAFE CRACKER","box-man");
		hashwords.put("LONER","wolf");
		hashwords.put("THIEVES CANT","gibberish");
		hashwords.put("TORTURE","have tea with the pigs");
		hashwords.put("TORTURED","had tea with the pigs");
		hashwords.put("TRAP","bite");
		hashwords.put("TRIAL","show");
		hashwords.put("VICTIM","content");
		hashwords.put("WALL","tilted floor");
		hashwords.put("WHIPPING","cly the jerk");
		hashwords.put("WHIPPING POST","jigger");
		hashwords.put("WIDOW","ace of spades");
		hashwords.put("LIAR","knight of the post");
		hashwords.put("WOMAN","mort");
		hashwords.put("COPPER","rust");
		hashwords.put("GOLD","yellow tin");
		hashwords.put("PLATINUM","shiny tin");
		hashwords.put("SILVER","tin");
		hashwords.put("DIAMOND","chunk o' gin");
		hashwords.put("DIAMONDS","chunks o' gin");
		hashwords.put("EMERALD","green beer");
		hashwords.put("EMERALDS","green beers");
		hashwords.put("SAPPHIRE","berry wine");
		hashwords.put("SAPPHIRES","berry wines");
		hashwords.put("PEARL","milk");
		hashwords.put("PEARLS","milk");
		hashwords.put("RUBY","chunk o' brandy");
		hashwords.put("RUBYS","chunks o' brandy");
		hashwords.put("EARRINGS","lobe latches");
		hashwords.put("EARRING","lobe latch");
		hashwords.put("JEWEL","booze");
		hashwords.put("JEWELS","booze");
		hashwords.put("JEWELRY","fancy booze");
		hashwords.put("LOOT","stuffing");
		hashwords.put("MONEY","bits");
		hashwords.put("NECKLACE","noose");
		hashwords.put("RING","finger joint");
		hashwords.put("RINGS","finger joints");
		hashwords.put("BRACELET","shackle");
		hashwords.put("BRACELETS","shackles");
		hashwords.put("BIG","tall man");
		hashwords.put("SMALL","short man");
		hashwords.put("HEAVY","fat lady");
		hashwords.put("LIGHT","thin woman");
		return hashwords;
	}
}
