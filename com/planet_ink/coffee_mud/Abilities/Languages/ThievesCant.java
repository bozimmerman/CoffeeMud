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
   Copyright 2002-2025 Bo Zimmerman

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

	@Override
	public String getTranslationVerb()
	{
		return "insinuate(s)";
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
	public List<String[]> translationLists(final String language)
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

	private static final Map<String,String> exactWords=new TreeMap<String,String>();

	@Override
	public Map<String, String> translationHash(final String language)
	{
		if((exactWords!=null)&&(exactWords.size()>0))
			return exactWords;

		exactWords.put("ARREST","sanilin");
		exactWords.put("ARRIVE","isimin");
		exactWords.put("ASK","esun");
		exactWords.put("ATTACK","kalan");
		exactWords.put("BACKSTAB","uneklin");
		exactWords.put("BE","mon");
		exactWords.put("BEGIN","fasren");
		exactWords.put("BELIEVE","esoan");
		exactWords.put("BETRAY","rolkon");
		exactWords.put("BLAME","hisuin");
		exactWords.put("BORROW","baylan");
		exactWords.put("BREATH","mafitan");
		exactWords.put("BRIBE","madlin");
		exactWords.put("BURGLARIZE","kalintan");
		exactWords.put("BUY","kun");
		exactWords.put("CALL","laryn");
		exactWords.put("CHOOSE","uhan");
		exactWords.put("CLIMB","san");
		exactWords.put("CLOSE","heryan");
		exactWords.put("COME","tun");
		exactWords.put("ARSON","hinuntan");
		exactWords.put("COUNTERFEIT","kartan");
		exactWords.put("COVER","kafun");
		exactWords.put("CRAWL","nusen");
		exactWords.put("CURSE","totakalan");
		exactWords.put("CUT","syan");
		exactWords.put("DECIDE","tisan");
		exactWords.put("DEFEAT","kokan");
		exactWords.put("DETECT","rabalan");
		exactWords.put("DIE","hunaran");
		exactWords.put("DISGUISE","bakren");
		exactWords.put("DISTRIBUTE","sinkin");
		exactWords.put("DIVERT","lakan");
		exactWords.put("DIVIDE","brokan");
		exactWords.put("DO","salan");
		exactWords.put("DRESS","taron");
		exactWords.put("DRUG","titanin");
		exactWords.put("EAT","safen");
		exactWords.put("ENTER","tyhin");
		exactWords.put("ESCAPE","untan");
		exactWords.put("FALL","rakin");
		exactWords.put("FIGHT","kolen");
		exactWords.put("FIND","tafen");
		exactWords.put("FINISH","unen");
		exactWords.put("FRAME","kaluan");
		exactWords.put("FREE","boroban");
		exactWords.put("GET","feun");
		exactWords.put("GIVE","lyn");
		exactWords.put("HAND","falabin");
		exactWords.put("HANDLE","tankan");
		exactWords.put("HAVE","tyn");
		exactWords.put("HEAT","takan");
		exactWords.put("HELP","nafen");
		exactWords.put("HIDE","turen");
		exactWords.put("HIJACK","kandon");
		exactWords.put("HIT","klun");
		exactWords.put("HOLD","kontoran");
		exactWords.put("INSPECT","lanin");
		exactWords.put("INTEREST","hyutan");
		exactWords.put("KIDNAP","naman");
		exactWords.put("KILL","intan");
		exactWords.put("KNOW","solen");
		exactWords.put("LEAVE","tern");
		exactWords.put("LEND","fyton");
		exactWords.put("LIGHT","utan");
		exactWords.put("LISTEN","tin");
		exactWords.put("LIVE","harn");
		exactWords.put("LOCK","ihilin");
		exactWords.put("LOOK","sarhon");
		exactWords.put("LOOT","kilikon");
		exactWords.put("LOSE","obfiken");
		exactWords.put("MAKE","forlin");
		exactWords.put("MOVE","otan");
		exactWords.put("OBSERVE","tulon");
		exactWords.put("PAY","hinlan");
		exactWords.put("PICK","trun");
		exactWords.put("PICKPOCKET","mun");
		exactWords.put("PILFER","kenon");
		exactWords.put("PLAN","ituan");
		exactWords.put("PLUNDER","kruteln");
		exactWords.put("POACH","ikiban");
		exactWords.put("PRACTICE","suon");
		exactWords.put("PREPARE","sun");
		exactWords.put("PULL","sanatan");
		exactWords.put("PUNISH","erminan");
		exactWords.put("PUSH","tambon");
		exactWords.put("PUT","kunin");
		exactWords.put("RAIN","rison");
		exactWords.put("RANSOM","haten");
		exactWords.put("READ","fehin");
		exactWords.put("RELEASE","obamon");
		exactWords.put("REPEAT","nabasin");
		exactWords.put("REST","hintyn");
		exactWords.put("RETREAT","kurnan");
		exactWords.put("RETURN","myban");
		exactWords.put("ROB","foren");
		exactWords.put("RUN","sen");
		exactWords.put("SAVE","toran");
		exactWords.put("SEE","tryn");
		exactWords.put("SELL","lobun");
		exactWords.put("SHOOT","tlin");
		exactWords.put("SHOW","lebon");
		exactWords.put("SNITCH","nilinin");
		exactWords.put("SNOW","mublin");
		exactWords.put("STAB","klin");
		exactWords.put("STAY","buron");
		exactWords.put("STEAL","ken");
		exactWords.put("STEP","kaltan");
		exactWords.put("STOP","ryn");
		exactWords.put("STRANGLE","ronen");
		exactWords.put("SWEAR","tekenon");
		exactWords.put("SWIM","usen");
		exactWords.put("TAKE","olon");
		exactWords.put("TALK","fen");
		exactWords.put("TEACH","kuron");
		exactWords.put("TELL","lynin");
		exactWords.put("THINK","brun");
		exactWords.put("THROW","haron");
		exactWords.put("TIE","seyen");
		exactWords.put("TRAIL","beon");
		exactWords.put("TRAP","esten");
		exactWords.put("USE","bun");
		exactWords.put("VISIT","maban");
		exactWords.put("WAIT","myn");
		exactWords.put("WAKE","ylink");
		exactWords.put("WALK","bosen");
		exactWords.put("WANT","uon");
		exactWords.put("WASH","orea");
		exactWords.put("WEAR","ymasan");
		exactWords.put("WIN","firen");
		exactWords.put("WORK","haman");
		exactWords.put("WRITE","sabnolan");
		exactWords.put("ABOUT","syhi");
		exactWords.put("ABOVE","fu");
		exactWords.put("OVER","fu");
		exactWords.put("UP","fu");
		exactWords.put("ACROSS","ne");
		exactWords.put("AFTER","bira");
		exactWords.put("AFTERNOON","salane");
		exactWords.put("AGAINST","bus");
		exactWords.put("ALCHEMIST","simikir");
		exactWords.put("ALCHEMY","simik");
		exactWords.put("ALE","ymus");
		exactWords.put("AMETHYST","balolot");
		exactWords.put("AMULET","binto");
		exactWords.put("ANIMAL","raoka");
		exactWords.put("ANY","at");
		exactWords.put("ANYTHING","arbura");
		exactWords.put("ANYTIME","atnau");
		exactWords.put("APPROACH","beslin");
		exactWords.put("ARCH","ahak");
		exactWords.put("ARM","nakao");
		exactWords.put("ARMOR","rokato");
		exactWords.put("ARMORER","rokatin");
		exactWords.put("ARMORY","kosohin");
		exactWords.put("AROUND","lema");
		exactWords.put("ARREST","sanili");
		exactWords.put("ARROW","tot");
		exactWords.put("ARSON","hinunta");
		exactWords.put("ARTERY","fanil");
		exactWords.put("AS","bina");
		exactWords.put("LIKE","bina");
		exactWords.put("ASSASSIN","bitama");
		exactWords.put("AT","na");
		exactWords.put("UNCLE","toa");
		exactWords.put("AUNT","toa");
		exactWords.put("BACK","sakam");
		exactWords.put("BACKPACK","sakambar");
		exactWords.put("BADNESS","frun");
		exactWords.put("BANDIT","ykana");
		exactWords.put("BANK","falsa");
		exactWords.put("BANKER","falsat");
		exactWords.put("BARDING","burfe");
		exactWords.put("BARGE","falaka");
		exactWords.put("BARKEEP","kalakar");
		exactWords.put("BARN","kiset");
		exactWords.put("BARRACKS","kauhin");
		exactWords.put("BARREL","sahas");
		exactWords.put("BASIN","huso");
		exactWords.put("BAT","ibit");
		exactWords.put("BATH","liko");
		exactWords.put("BATTLE","tabo");
		exactWords.put("BEARD","inat");
		exactWords.put("BECAUSE","ibli");
		exactWords.put("BECAUSE OF","libi");
		exactWords.put("BED","nako");
		exactWords.put("BEDROOM","nakohin");
		exactWords.put("BEFORE","moly");
		exactWords.put("BEGINNING","bone");
		exactWords.put("BEHIND","obul");
		exactWords.put("BELOW","nu");
		exactWords.put("BENEATH","nu");
		exactWords.put("UNDER","nu");
		exactWords.put("DOWN","nu");
		exactWords.put("BELT","sintur");
		exactWords.put("BENCH","syili");
		exactWords.put("BESIDE","sa");
		exactWords.put("BETWEEN","flo");
		exactWords.put("BEVERAGE","asefa");
		exactWords.put("DRINK","asefa");
		exactWords.put("BIGNESS","sio");
		exactWords.put("BIRD","syt");
		exactWords.put("BIRTH","hibar");
		exactWords.put("BLACK","koret");
		exactWords.put("BLACKMAIL","barsan");
		exactWords.put("BLANKET","tomuk");
		exactWords.put("BLOOD","fanbo");
		exactWords.put("BLUE","nerb");
		exactWords.put("BOAT","barbo");
		exactWords.put("BODY","salarka");
		exactWords.put("BONE","botu");
		exactWords.put("BOOK","lakat");
		exactWords.put("BOOT","telu");
		exactWords.put("BOOTY","relbat");
		exactWords.put("BOSS","ukal");
		exactWords.put("CHIEF","ukal");
		exactWords.put("MASTER","ukal");
		exactWords.put("BOW","rafak");
		exactWords.put("BOX","kal");
		exactWords.put("BRACELET","omban");
		exactWords.put("BRAIN","sankot");
		exactWords.put("BREATH","mafit");
		exactWords.put("BRIBE","madli");
		exactWords.put("BRICK","kilu");
		exactWords.put("BRIDGE","hili");
		exactWords.put("BROACH","toka");
		exactWords.put("BROWN","noru");
		exactWords.put("BUCKET","kubak");
		exactWords.put("BUILDING","kusal");
		exactWords.put("BURGLAR","kalinta");
		exactWords.put("BURIAL","maleto");
		exactWords.put("BURN","balsan");
		exactWords.put("BUT","moky");
		exactWords.put("BY","ky");
		exactWords.put("CAN","beben");
		exactWords.put("CANDLE","ylo");
		exactWords.put("CARAVAN","lusok");
		exactWords.put("CART","kest");
		exactWords.put("CASE","mibar");
		exactWords.put("CASKET","hunakal");
		exactWords.put("CASTLE","ornek");
		exactWords.put("CAT","aram");
		exactWords.put("CAVE","kuena");
		exactWords.put("CEILING","filu");
		exactWords.put("CELL","myo");
		exactWords.put("CENTER","het");
		exactWords.put("CHAIN","sambil");
		exactWords.put("CHAIR","yli");
		exactWords.put("CHARISMA","lisema");
		exactWords.put("CHEST","kanab");
		exactWords.put("CHILD","berkin");
		exactWords.put("CHIN","banal");
		exactWords.put("CHOICE","uha");
		exactWords.put("CITY","holyat");
		exactWords.put("CLERIC","kawabi");
		exactWords.put("CLIENT","hinylot");
		exactWords.put("CLOAK","loab");
		exactWords.put("CLOTHING","mys");
		exactWords.put("CLOUD","kalok");
		exactWords.put("CLUB","tufa");
		exactWords.put("COLD","arot");
		exactWords.put("COLOR","lok");
		exactWords.put("CONSCIOUSNESS","fora");
		exactWords.put("CONSTITUTION","mofit");
		exactWords.put("CONTAINER","sahal");
		exactWords.put("COPPER","krin");
		exactWords.put("CORRECTNESS","bersity");
		exactWords.put("COUNTRY","esim");
		exactWords.put("COURT","nio");
		exactWords.put("CROWD","arkar");
		exactWords.put("CROWN","fuib");
		exactWords.put("CRYPT","hunahin");
		exactWords.put("CUP","bebol");
		exactWords.put("CUPBOARD","osalos");
		exactWords.put("CURSE","takali");
		exactWords.put("CURTAIN","aoro");
		exactWords.put("CUSTOM","ibim");
		exactWords.put("CUT","sya");
		exactWords.put("DAGGER","ret");
		exactWords.put("DANGER","bylis");
		exactWords.put("DARKNESS","ekob");
		exactWords.put("DART","tur");
		exactWords.put("DATE","syf");
		exactWords.put("DAY","ine");
		exactWords.put("DEAL","yhy");
		exactWords.put("DEATH","hunar");
		exactWords.put("DEFEAT","koka");
		exactWords.put("DEMON","asin");
		exactWords.put("DESERT","akbun");
		exactWords.put("DESK","risin");
		exactWords.put("DESTROY","bahelan");
		exactWords.put("DEXTERITY","tura");
		exactWords.put("DIAMON","urtel");
		exactWords.put("DISCOVERY","fibil");
		exactWords.put("DISGUISE","bakre");
		exactWords.put("DISHONESTY","oberabas");
		exactWords.put("DIVERSION","lakana");
		exactWords.put("DIVISION","broka");
		exactWords.put("DOG","simar");
		exactWords.put("DOOR","uhob");
		exactWords.put("DRAGON","fybkes");
		exactWords.put("DRAWER","tiran");
		exactWords.put("DRINK","asefan");
		exactWords.put("DRUG","titani");
		exactWords.put("DRUID","kaslil");
		exactWords.put("DUNGEON","ror");
		exactWords.put("DURING","norb");
		exactWords.put("DWARF","teko");
		exactWords.put("EAR","tyna");
		exactWords.put("EARLINESS","naltos");
		exactWords.put("EARRING","matybal");
		exactWords.put("EARTH","nusfi");
		exactWords.put("EAST","salif");
		exactWords.put("EIGHT","tulo");
		exactWords.put("ELECTRUM","bintal");
		exactWords.put("ELF","ulim");
		exactWords.put("EMERALD","akoy");
		exactWords.put("END","une");
		exactWords.put("ENDURANCE","nabi");
		exactWords.put("ENGINEER","inher");
		exactWords.put("ENTRANCE","mahan");
		exactWords.put("EQUIPMENT","sala");
		exactWords.put("ESCAPE","unta");
		exactWords.put("EVERY TIME","heken");
		exactWords.put("EVERYTHING","faburan");
		exactWords.put("EVERYWHERE","kontonken");
		exactWords.put("EVIDENCE","turyas");
		exactWords.put("EXCEPT","tik");
		exactWords.put("EXPENSE","onbom");
		exactWords.put("EXPERIENCE","syto");
		exactWords.put("EYE","atami");
		exactWords.put("FAINT","barbun");
		exactWords.put("FALL","norsalik");
		exactWords.put("FARM","narak");
		exactWords.put("FIGHTER","kol");
		exactWords.put("FINGER","baral");
		exactWords.put("FIRE","bal");
		exactWords.put("FIREPLACE","kanabal");
		exactWords.put("FIRST","bink");
		exactWords.put("FIVE","lim");
		exactWords.put("FLESH","kitki");
		exactWords.put("FLINT","bybi");
		exactWords.put("FLOOR","ybel");
		exactWords.put("FOOD","safe");
		exactWords.put("FOOT","uket");
		exactWords.put("FOR","lit");
		exactWords.put("FOREHEAD","kuta");
		exactWords.put("FOREST","suso");
		exactWords.put("FORK","rimnok");
		exactWords.put("FOUNTAIN","enit");
		exactWords.put("FOUR","lo");
		exactWords.put("FREEDOM","borob");
		exactWords.put("FROM","bu");
		exactWords.put("FUTURE","kar");
		exactWords.put("GALLERY","bio");
		exactWords.put("GALLEY","teltan");
		exactWords.put("GARDEN","amas");
		exactWords.put("GARLIC","os");
		exactWords.put("GATE","sany");
		exactWords.put("GEM","isala");
		exactWords.put("GIFT","lyr");
		exactWords.put("GLASS","sulan");
		exactWords.put("GNOME","kurbe");
		exactWords.put("GO","ban");
		exactWords.put("GOBLET","siril");
		exactWords.put("GOD","akfur");
		exactWords.put("GOLD","hymur");
		exactWords.put("GOODBYE","syetken");
		exactWords.put("GOODNESS","syet");
		exactWords.put("GRAY","solet");
		exactWords.put("GREEN","resen");
		exactWords.put("GROUP","tek");
		exactWords.put("GUARDROOM","sarhin");
		exactWords.put("HAIR","kuho");
		exactWords.put("HALF","obla");
		exactWords.put("HALF-ELF","ulimobla");
		exactWords.put("HALF-ORC","takobla");
		exactWords.put("HALFLING","mistis");
		exactWords.put("HALL","sanla");
		exactWords.put("HAND","sakel");
		exactWords.put("HARNESS","biby");
		exactWords.put("HE","ti");
		exactWords.put("HIM","ti");
		exactWords.put("SHE","i");
		exactWords.put("HER","i");
		exactWords.put("IT","i");
		exactWords.put("HEAD","biak");
		exactWords.put("HEART","likiha");
		exactWords.put("HEARTBEAT","likihyran");
		exactWords.put("HEAT","ortob");
		exactWords.put("HEAVINESS","obtylo");
		exactWords.put("HELLO","syetonta");
		exactWords.put("HELMET","tif");
		exactWords.put("HIGHNESS","bora");
		exactWords.put("HIGHWAYMAN","lekel");
		exactWords.put("HILL","kunsal");
		exactWords.put("HOLDUP","kontora");
		exactWords.put("HOLINESS","mina");
		exactWords.put("HOME","iro");
		exactWords.put("HOUSE","iro");
		exactWords.put("HONESTY","terabas");
		exactWords.put("HONOR","totak");
		exactWords.put("HORSE","aby");
		exactWords.put("HOSTAGE","amon");
		exactWords.put("HOUR","lane");
		exactWords.put("HOW","bihe");
		exactWords.put("HOWEVER","kymo");
		exactWords.put("HUMAN","rylo");
		exactWords.put("HUNDRED","sur");
		exactWords.put("I","to");
		exactWords.put("ME","o");
		exactWords.put("IDOL","natuk");
		exactWords.put("IF","beti");
		exactWords.put("ILLUSION","kulat");
		exactWords.put("ILLUSIONIST","kulamak");
		exactWords.put("IN","ly");
		exactWords.put("IN FRONT OF","ul");
		exactWords.put("INCORRECT","obersity");
		exactWords.put("WRONGNESS","obersity");
		exactWords.put("INSANITY","sabit");
		exactWords.put("INSPECTION","lani");
		exactWords.put("INSTEAD OF","ab");
		exactWords.put("INTELLIGENCE","sunin");
		exactWords.put("JAIL","asorit");
		exactWords.put("JAW","tara");
		exactWords.put("JEWEL","kem");
		exactWords.put("JUG","satek");
		exactWords.put("KEG","rytena");
		exactWords.put("KENNEL","simarhin");
		exactWords.put("KETTLE","sikla");
		exactWords.put("KEY","tirib");
		exactWords.put("KICK","okob");
		exactWords.put("KID","ime");
		exactWords.put("KITCHEN","nirahin");
		exactWords.put("KNOCK OUT","belon");
		exactWords.put("KNOW","altan");
		exactWords.put("KOBOLD","hamoki");
		exactWords.put("LADDER","fasan");
		exactWords.put("LAKE","utho");
		exactWords.put("LANGUAGE","olarhe");
		exactWords.put("LANTERN","tatobal");
		exactWords.put("LATENESS","nobal");
		exactWords.put("LAW","biho");
		exactWords.put("LAWLESSNESS","ankat");
		exactWords.put("LEATHER","falat");
		exactWords.put("LEAVE","aban");
		exactWords.put("LEFTNESS","resa");
		exactWords.put("LEG","akte");
		exactWords.put("LENGTH","haba");
		exactWords.put("LESSON","kutal");
		exactWords.put("LETTER","kargel");
		exactWords.put("LIBRARY","lahin");
		exactWords.put("LIFE","har");
		exactWords.put("LIGHT","uta");
		exactWords.put("LIGHTNESS","tylo");
		exactWords.put("LITTLENESS","tan");
		exactWords.put("LOCK","ihil");
		exactWords.put("LOCK PICK","tru");
		exactWords.put("LOCKET","fafet");
		exactWords.put("LOOT","kilik");
		exactWords.put("LOUDNESS","obykar");
		exactWords.put("LOWNESS","obora");
		exactWords.put("MAGIC","ker");
		exactWords.put("MAGIC-USER","mankero");
		exactWords.put("MALLET","ekmel");
		exactWords.put("MAN","ark");
		exactWords.put("MAP","faki");
		exactWords.put("MARSH","ratik");
		exactWords.put("MATTRESS","kanoho");
		exactWords.put("MAY","sib");
		exactWords.put("MAYOR","holok");
		exactWords.put("MEAD","kural");
		exactWords.put("MEAL","baka");
		exactWords.put("MEDALLION","kolnok");
		exactWords.put("MEDICINE","tlinkit");
		exactWords.put("MERCHANT","forkel");
		exactWords.put("MIDDLE","albir");
		exactWords.put("MIDNIGHT","hetobne");
		exactWords.put("MIGHT","kutin");
		exactWords.put("MILLION","kisak");
		exactWords.put("MINUTE","nebi");
		exactWords.put("MIRROR","lamin");
		exactWords.put("MOMENT","orib");
		exactWords.put("MONEY","hyman");
		exactWords.put("MONK","rosati");
		exactWords.put("MONSTER","arkoba");
		exactWords.put("MORNING","nibe");
		exactWords.put("MOUND","iloka");
		exactWords.put("PILE","iloka");
		exactWords.put("MOUNTAIN","usal");
		exactWords.put("MOUSTACHE","tanina");
		exactWords.put("MOUTH","bibik");
		exactWords.put("MOUTH","likano");
		exactWords.put("SNEAK","bin");
		exactWords.put("MUSCLE","fonbak");
		exactWords.put("MUSEUM","sihyres");
		exactWords.put("MUSIC","siterke");
		exactWords.put("MUST","miban");
		exactWords.put("NAIL","konfa");
		exactWords.put("NAME","lary");
		exactWords.put("NEAR","ry");
		exactWords.put("NECESSITY","tanol");
		exactWords.put("NECK","liki");
		exactWords.put("PILLAR","liki");
		exactWords.put("NECKLACE","likob");
		exactWords.put("NEWNESS","hobuli");
		exactWords.put("NIGHT","obine");
		exactWords.put("NINE","am");
		exactWords.put("NO","hibni");
		exactWords.put("NOT","hibni");
		exactWords.put("NOISE","yran");
		exactWords.put("NOON","hetne");
		exactWords.put("NORTH","bisfi");
		exactWords.put("NOSE","lonek");
		exactWords.put("NOTHING","hibmun");
		exactWords.put("NOW","lyrib");
		exactWords.put("NUMBER","taka");
		exactWords.put("OF","ro");
		exactWords.put("OFF","al");
		exactWords.put("OFFERING","fakab");
		exactWords.put("OFFICE","teltin");
		exactWords.put("OIL","fyal");
		exactWords.put("OLDNESS","huli");
		exactWords.put("ON","li");
		exactWords.put("ONE","bi");
		exactWords.put("OPAL","samun");
		exactWords.put("OPEN","binson");
		exactWords.put("OPPOSITE","feku");
		exactWords.put("OR","fy");
		exactWords.put("ORANGE","hali");
		exactWords.put("ORB","hari");
		exactWords.put("ORC","tak");
		exactWords.put("OUT","baker");
		exactWords.put("PAINTING","rarkan");
		exactWords.put("PALADIN","filta");
		exactWords.put("PARAPET","nekio");
		exactWords.put("PARK","herel");
		exactWords.put("PARTY","bislis");
		exactWords.put("PASS","behan");
		exactWords.put("PASSAGE","bak");
		exactWords.put("PAST","bir");
		exactWords.put("PAYMENT","lyny");
		exactWords.put("PEARL","boblin");
		exactWords.put("PEOPLE","banasko");
		exactWords.put("PILLAGE","baron");
		exactWords.put("PILLOW","sakana");
		exactWords.put("PIN","katam");
		exactWords.put("PLACE","alub");
		exactWords.put("PLAINS","lyket");
		exactWords.put("PLAN","itu");
		exactWords.put("PLATINUM","sublin");
		exactWords.put("POCKET","tolata");
		exactWords.put("POISON","mala");
		exactWords.put("POLE","anukit");
		exactWords.put("POTION","isili");
		exactWords.put("POUCH","byb");
		exactWords.put("POUND","sorba");
		exactWords.put("PRICE","rub");
		exactWords.put("PRINCE","arhobol");
		exactWords.put("PRINCESS","obarhobol");
		exactWords.put("PRISON","basihin");
		exactWords.put("PUNISHMENT","ermina");
		exactWords.put("QUESTION","tanob");
		exactWords.put("QUILT","buk");
		exactWords.put("QUIVER","tilya");
		exactWords.put("RAIN","riso");
		exactWords.put("RAP","babon");
		exactWords.put("RATIONS","hira");
		exactWords.put("RECTANGLE","harata");
		exactWords.put("RED","blon");
		exactWords.put("REMOVE","berosin");
		exactWords.put("RIGHTNESS","kolta");
		exactWords.put("RING","mabal");
		exactWords.put("ROAD","bayn");
		exactWords.put("ROAD","byn");
		exactWords.put("ROBE","balab");
		exactWords.put("ROOM","hin");
		exactWords.put("ROPE","timon");
		exactWords.put("RUBY","kumba");
		exactWords.put("RUG","bamat");
		exactWords.put("RUINS","raiga");
		exactWords.put("SACK","baim");
		exactWords.put("SADDLE","silkaba");
		exactWords.put("SAFETY","tonta");
		exactWords.put("SAGE","balub");
		exactWords.put("SALT","luset");
		exactWords.put("SAND","betita");
		exactWords.put("SANTUARY","tonlub");
		exactWords.put("SAPPHIRE","be");
		exactWords.put("SCABBARD","kamkanab");
		exactWords.put("SCHOLAR","roal");
		exactWords.put("SCONCE","ybila");
		exactWords.put("SCREEN","kufer");
		exactWords.put("SCROLL","karel");
		exactWords.put("SEA","falati");
		exactWords.put("SECOND","lank");
		exactWords.put("SECRET","huna");
		exactWords.put("SEVEN","ula");
		exactWords.put("SHADOW","eski");
		exactWords.put("SHEET","sihal");
		exactWords.put("SHELF","mitaf");
		exactWords.put("SHIELD","hantak");
		exactWords.put("SHIP","lulani");
		exactWords.put("SHORTNESS","yta");
		exactWords.put("SHOULDER","nakaoka");
		exactWords.put("SHRINE","fysos");
		exactWords.put("SIGN","fikisik");
		exactWords.put("SILENCE","robal");
		exactWords.put("SILVER","rubel");
		exactWords.put("SINCE","okam");
		exactWords.put("SIX","sito");
		exactWords.put("SIZE","rakomo");
		exactWords.put("SKELETON","akbotark");
		exactWords.put("SKIN","funar");
		exactWords.put("SKULL","akbot");
		exactWords.put("SKY","morukek");
		exactWords.put("SLING","haro");
		exactWords.put("SLOWNESS","obok");
		exactWords.put("SNAKE","hiblokni");
		exactWords.put("SNOW","mubli");
		exactWords.put("SOFTNESS","ykar");
		exactWords.put("SOLDIER","kaual");
		exactWords.put("QUIETNESS","ykar");
		exactWords.put("WARRIOR","kaual");
		exactWords.put("SOMETHING","burakan");
		exactWords.put("SOUTH","lafe");
		exactWords.put("SPEED","koma");
		exactWords.put("SPELL","lam");
		exactWords.put("SPIDER","tulokto");
		exactWords.put("SPIKE","helu");
		exactWords.put("SPRING","loksalik");
		exactWords.put("SPY","limenet");
		exactWords.put("SQUARE","ashi");
		exactWords.put("STABLE","abyhin");
		exactWords.put("STAIRWAY","sosan");
		exactWords.put("STAND","bilin");
		exactWords.put("STAND","lofon");
		exactWords.put("STATUE","emna");
		exactWords.put("STEEL","ulof");
		exactWords.put("STOMACH","hanfi");
		exactWords.put("STONE","tato");
		exactWords.put("STOOL","fokon");
		exactWords.put("STORAGE","bikit");
		exactWords.put("STORE","foryhab");
		exactWords.put("STRENGTH","fatas");
		exactWords.put("STRING","kentis");
		exactWords.put("STRONGHOLD","tontyha");
		exactWords.put("SUMMER","resalik");
		exactWords.put("SUN","tobalon");
		exactWords.put("SWORD","kam");
		exactWords.put("SYMBOL","biny");
		exactWords.put("TABLE","ruba");
		exactWords.put("TAPESTRY","uhasi");
		exactWords.put("TARGET","tili");
		exactWords.put("TEACHER","kura");
		exactWords.put("TEMPLE","kauta");
		exactWords.put("TEN","imbo");
		exactWords.put("THAT","sab");
		exactWords.put("WHICH","om");
		exactWords.put("THEN","nosa");
		exactWords.put("THEY","ii");
		exactWords.put("THEM","ii");
		exactWords.put("THIEF","basim");
		exactWords.put("CANT","basifen");
		exactWords.put("TOOLS","basisala");
		exactWords.put("THING","bura");
		exactWords.put("THIRD","salonk");
		exactWords.put("THOUSAND","bakat");
		exactWords.put("THREE","salo");
		exactWords.put("THRONE","ruka");
		exactWords.put("THROUGH","ily");
		exactWords.put("TIME","hek");
		exactWords.put("TINDER","uhys");
		exactWords.put("TO","sy");
		exactWords.put("TODAY","lyrne");
		exactWords.put("TOE","kuly");
		exactWords.put("TOMORROW","sirine");
		exactWords.put("TONGUE","lina");
		exactWords.put("TOOTH","iti");
		exactWords.put("TORCH","uro");
		exactWords.put("TORSO","lak");
		exactWords.put("TOWARD","so");
		exactWords.put("TOWER","fysy");
		exactWords.put("TOWN","mibet");
		exactWords.put("TRAIL","beo");
		exactWords.put("TRAITOR","rolko");
		exactWords.put("TRAP","itak");
		exactWords.put("TREASURE","kyam");
		exactWords.put("TRIANGLE","kolibis");
		exactWords.put("TRIP","kult");
		exactWords.put("TROLL","bart");
		exactWords.put("TWO","la");
		exactWords.put("UNCONSCIOUSNESS","obfora");
		exactWords.put("UNHOLINESS","obina");
		exactWords.put("UNLIKE","obinta");
		exactWords.put("UNTIL","si");
		exactWords.put("VALUABLES","konba");
		exactWords.put("VASE","hliki");
		exactWords.put("VAULT","narko");
		exactWords.put("VIOLET","rybun");
		exactWords.put("WAGON","enbra");
		exactWords.put("WALL","alun");
		exactWords.put("WAND","naki");
		exactWords.put("WANDER","aron");
		exactWords.put("WATER","ros");
		exactWords.put("WATERSKIN","roskanab");
		exactWords.put("WAY","ba");
		exactWords.put("WE","too");
		exactWords.put("US","oo");
		exactWords.put("WEAPON","kosol");
		exactWords.put("WEAPONER","kosolit");
		exactWords.put("WEATHER","hos");
		exactWords.put("WEEK","lino");
		exactWords.put("WELL","limi");
		exactWords.put("WEST","limok");
		exactWords.put("WHAT","laf");
		exactWords.put("WHEN","nau");
		exactWords.put("WHERE","foram");
		exactWords.put("WHETHER","bo");
		exactWords.put("WHILE","uale");
		exactWords.put("WHO","tehel");
		exactWords.put("WHY","sarum");
		exactWords.put("WIND","ferel");
		exactWords.put("WINE","kalaf");
		exactWords.put("WIRE","kenil");
		exactWords.put("WITCH","manka");
		exactWords.put("WITH","ki");
		exactWords.put("WITHOUT","ryki");
		exactWords.put("WOMAN","obark");
		exactWords.put("WOOD","kaby");
		exactWords.put("WORD","hal");
		exactWords.put("WORK","hama");
		exactWords.put("WORKSHOP","hamhin");
		exactWords.put("WOULD","takin");
		exactWords.put("WRIST","lator");
		exactWords.put("YEAR","fal");
		exactWords.put("YELLOW","nolt");
		exactWords.put("YES","an");
		exactWords.put("YESTERDAY","labne");
		exactWords.put("YALL","toe");
		exactWords.put("Y`ALL","toe");
		exactWords.put("YOU","e");
		exactWords.put("ZERO","orni");
		exactWords.put("ABANDONED PERSON","lurch");
		exactWords.put("ABANDONED","blasted");
		exactWords.put("ACCOMPLICE","hired help");
		exactWords.put("ARRESTED","boned");
		exactWords.put("ASSASSIN","hit man");
		exactWords.put("ASSASSINATE","shoulder tap");
		exactWords.put("BACKSTAB","shoulder tap");
		exactWords.put("BEER","bowse");
		exactWords.put("BEGGER","abram man");
		exactWords.put("BEGGARS","abram men");
		exactWords.put("BEGGING","angling for coppers");
		exactWords.put("BOAT","ark");
		exactWords.put("BODY","quarromes");
		exactWords.put("GRAVE ROBBERS","ressurection men");
		exactWords.put("BOOTY","swag");
		exactWords.put("BRANDY","bingo");
		exactWords.put("BRIBE","expense money");
		exactWords.put("BROTHEL","academy");
		exactWords.put("BURGLAR","rounder");
		exactWords.put("BURGLARY","visitin' the neighbors");
		exactWords.put("CALF","blater");
		exactWords.put("CALL FOR HELP","cry beef");
		exactWords.put("CART","gaoler's coach");
		exactWords.put("CLIMB","taking a big step");
		exactWords.put("CLOAK","calle");
		exactWords.put("CLUB","oaken towel");
		exactWords.put("COACH MAN","Rattling cove");
		exactWords.put("FAKE COIN","button");
		exactWords.put("FAKE COINS","buttons");
		exactWords.put("COMMITING CRIMES","rooton'");
		exactWords.put("CON","hoodwink");
		exactWords.put("CONNED","hoodwinked");
		exactWords.put("CON MAN","jack in the box");
		exactWords.put("VICTIM","pigeon");
		exactWords.put("CON ARTIST","pigeon plucker");
		exactWords.put("COUNTERFEITER","figure dancer");
		exactWords.put("COUNTERFEITED","confect");
		exactWords.put("COUNTERFEITING","drawing the kings picture");
		exactWords.put("COURTHOUSE","nubbing ken");
		exactWords.put("BEING TRANSPORTED","barrow man");
		exactWords.put("IN THE STOCKS","babes in the wood");
		exactWords.put("WITH BURNED HANDS","badge");
		exactWords.put("CROWBAR","ginny");
		exactWords.put("DAGGER","pig poker");
		exactWords.put("DANGER","lay");
		exactWords.put("DEATH SENTENCE","cramp word");
		exactWords.put("DAY","lightmans");
		exactWords.put("DIE","goin' legit");
		exactWords.put("DOG","bufe");
		exactWords.put("DOOR","lid");
		exactWords.put("DRUNK","clear");
		exactWords.put("ESCAPE","hike");
		exactWords.put("FAKE SICK","sham abram");
		exactWords.put("FENCE","uncle");
		exactWords.put("FLOGGING","getting stripes");
		exactWords.put("FOLLOW","drag");
		exactWords.put("FOOL","gudgeon");
		exactWords.put("GALLOWS","three legged stool");
		exactWords.put("GANG","birds of a feather");
		exactWords.put("GYPSIES","cattle");
		exactWords.put("GLOVES","farms");
		exactWords.put("GLOVE","farm");
		exactWords.put("GOOD","rum");
		exactWords.put("GUARD","pig");
		exactWords.put("HEAD OF THE GUARD","papa poker");
		exactWords.put("GUILD ELDER","made-guy");
		exactWords.put("HANGED","frummagemmed");
		exactWords.put("HEALER","tinker");
		exactWords.put("HELLO","knock-knock");
		exactWords.put("HIDE","shadow dance");
		exactWords.put("HIDEOUT","dive");
		exactWords.put("HIGHWAY","pad");
		exactWords.put("HIGHWAYMAN","knight of the road");
		exactWords.put("HIGHWAY ROBBERY","recruiting service");
		exactWords.put("HIT","click");
		exactWords.put("HONEST MAN","cull");
		exactWords.put("HOUSE","panny");
		exactWords.put("HOUSEBREAKER","rushers");
		exactWords.put("ILLEGAL GOODS","contraband");
		exactWords.put("INFORM","cackle");
		exactWords.put("INFORMTANT","a hole in need of plugging");
		exactWords.put("INJURY","scratch");
		exactWords.put("WOUND","beef");
		exactWords.put("INNOCENT","dodgies");
		exactWords.put("INJURE","scratched");
		exactWords.put("JAIL","guesthouse");
		exactWords.put("PRISON","guesthouse");
		exactWords.put("JAIL HOUSE","guesthouse");
		exactWords.put("JAIL BREAK","goin' home");
		exactWords.put("JUDGE","fortune teller");
		exactWords.put("KILL","have a chat with");
		exactWords.put("KILLED","had a chat with");
		exactWords.put("KNOCKED OUT","tucked into bed");
		exactWords.put("LANTERN","darkee");
		exactWords.put("LARCENY","racket");
		exactWords.put("LAWYER","cursitor");
		exactWords.put("LEAD","moss");
		exactWords.put("LICENSE","jukrum");
		exactWords.put("LIE","amuse");
		exactWords.put("LIED","amused");
		exactWords.put("LOCK","joke");
		exactWords.put("LOCKPICK","punchline");
		exactWords.put("LOCKED","joked");
		exactWords.put("LOOKOUT","pair of eyes");
		exactWords.put("LOCKPICKER","gilt");
		exactWords.put("LOCKPICKING","black art");
		exactWords.put("LOOT","score");
		exactWords.put("LOSE","dropping");
		exactWords.put("LOST","dropped");
		exactWords.put("HAND","glove");
		exactWords.put("MAGIC","flash");
		exactWords.put("MAGIC ITEM","bagged flash");
		exactWords.put("MAN","cove");
		exactWords.put("MISTRESS","nypper");
		exactWords.put("NIGHT WATCHMAN","moon pig");
		exactWords.put("NIGHT GUARD","moon pig");
		exactWords.put("OPEN","tell a joke");
		exactWords.put("PICKPOCKET","purse collector");
		exactWords.put("PLAN","signals");
		exactWords.put("PLANNING","flag waving");
		exactWords.put("PRISONER","hizzoner's guest");
		exactWords.put("QUIET","smooth");
		exactWords.put("RICH","oak");
		exactWords.put("RING","onion");
		exactWords.put("RIOT","hubbub");
		exactWords.put("RISK","boredom");
		exactWords.put("ROPE","danglestuff");
		exactWords.put("SECURE","rug");
		exactWords.put("SELL","switch");
		exactWords.put("SHACKLES","king's plate");
		exactWords.put("SHOPLIFTER","bob");
		exactWords.put("STAFF","jordain");
		exactWords.put("STEAL","borrow");
		exactWords.put("STOLE","borrowed");
		exactWords.put("STOCKS","parenthesis");
		exactWords.put("STOLEN","made");
		exactWords.put("SWAP","ring the changes");
		exactWords.put("TARGET","mark");
		exactWords.put("TAVERN","beggar maker");
		exactWords.put("BAR","beggar maker");
		exactWords.put("PUB","beggar maker");
		exactWords.put("THEFT","game");
		exactWords.put("TIE UP","dress up");
		exactWords.put("THEIF","gentleman");
		exactWords.put("THIEVES","gentlemen");
		exactWords.put("GUILD MASTER","upright man");
		exactWords.put("SAFE CRACKER","box-man");
		exactWords.put("LONER","wolf");
		exactWords.put("THIEVES CANT","gibberish");
		exactWords.put("TORTURE","have tea with the pigs");
		exactWords.put("TORTURED","had tea with the pigs");
		exactWords.put("TRAP","bite");
		exactWords.put("TRIAL","show");
		exactWords.put("VICTIM","content");
		exactWords.put("WALL","tilted floor");
		exactWords.put("WHIPPING","cly the jerk");
		exactWords.put("WHIPPING POST","jigger");
		exactWords.put("WIDOW","ace of spades");
		exactWords.put("LIAR","knight of the post");
		exactWords.put("WOMAN","mort");
		exactWords.put("COPPER","rust");
		exactWords.put("GOLD","yellow tin");
		exactWords.put("PLATINUM","shiny tin");
		exactWords.put("SILVER","tin");
		exactWords.put("DIAMOND","chunk o' gin");
		exactWords.put("DIAMONDS","chunks o' gin");
		exactWords.put("EMERALD","green beer");
		exactWords.put("EMERALDS","green beers");
		exactWords.put("SAPPHIRE","berry wine");
		exactWords.put("SAPPHIRES","berry wines");
		exactWords.put("PEARL","milk");
		exactWords.put("PEARLS","milk");
		exactWords.put("RUBY","chunk o' brandy");
		exactWords.put("RUBYS","chunks o' brandy");
		exactWords.put("EARRINGS","lobe latches");
		exactWords.put("EARRING","lobe latch");
		exactWords.put("JEWEL","booze");
		exactWords.put("JEWELS","booze");
		exactWords.put("JEWELRY","fancy booze");
		exactWords.put("LOOT","stuffing");
		exactWords.put("MONEY","bits");
		exactWords.put("NECKLACE","noose");
		exactWords.put("RING","finger joint");
		exactWords.put("RINGS","finger joints");
		exactWords.put("BRACELET","shackle");
		exactWords.put("BRACELETS","shackles");
		exactWords.put("BIG","tall man");
		exactWords.put("SMALL","short man");
		exactWords.put("HEAVY","fat lady");
		exactWords.put("LIGHT","thin woman");
		return exactWords;
	}
}
