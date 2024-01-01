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
   Copyright 2002-2024 Bo Zimmerman

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
public class Orcish extends StdLanguage
{
	@Override
	public String ID()
	{
		return "Orcish";
	}

	private final static String localizedName = CMLib.lang().L("Orcish");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String getTranslationVerb()
	{
		return "grunt(s)";
	}

	public static List<String[]> wordLists=null;
	public Orcish()
	{
		super();
	}

	@Override
	public List<String[]> translationLists(final String language)
	{
		if(wordLists==null)
		{
			final String[] one={"a"};
			final String[] two={"uk","ik","og","eg","ak","ag"};
			final String[] three={"uko","ugg","ick","ehk","akh","oog"};
			final String[] four={"blec","mugo","guck","gook","kill","dead","twak","kwat","klug"};
			final String[] five={"bleko","thwak","klarg","gluck","kulgo","mucka","splat","kwath","garth","blark"};
			final String[] six={"kalarg","murder","bleeke","kwargh","guttle","thungo"};
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

		exactWords.put("ABOVE","mub");
		exactWords.put("ABYSS","pafund");
		exactWords.put("ACID","tharb");
		exactWords.put("ACID","tharm");
		exactWords.put("ACROSS","ti");
		exactWords.put("ADMINISTRATION","mubarshtaum");
		exactWords.put("ADOLESCENT","dajal");
		exactWords.put("ADULT","moshar");
		exactWords.put("ADVISOR","kishaulus");
		exactWords.put("ADZE","skopar");
		exactWords.put("AFAR","larg");
		exactWords.put("AFTER","mabas");
		exactWords.put("AGGRESSIVE","sulmus");
		exactWords.put("AGONY","dhaub");
		exactWords.put("AGRICULTURE","bujukasi");
		exactWords.put("AIR","ia");
		exactWords.put("ALARM","kishtraum");
		exactWords.put("ALL","gith");
		exactWords.put("ALL","uk");
		exactWords.put("ALLIANCE","bashkaum");
		exactWords.put("ALLOR","laudh");
		exactWords.put("ALLY","bosnauk");
		exactWords.put("ALMIGHTY","tanfuksham");
		exactWords.put("ALONE","nalt");
		exactWords.put("ALSO","do");
		exactWords.put("ALTAR","thoror");
		exactWords.put("AMBUSH","prauta");
		exactWords.put("AMER","kolaubar");
		exactWords.put("AMONG","nadar");
		exactWords.put("ANCESTOR","stargush");
		exactWords.put("ANCIENT","motsham");
		exactWords.put("AND","agh");
		exactWords.put("ANGER","zemaraum");
		exactWords.put("ANIMAL","dyr");
		exactWords.put("ARCH","kular");
		exactWords.put("ARCHER","kalus");
		exactWords.put("ARCHERY","kalaum");
		exactWords.put("ARENA","lam");
		exactWords.put("ARM","krah");
		exactWords.put("ARMOR","kalkan");
		exactWords.put("ARMY","ushtar");
		exactWords.put("ARMY","ushtarak");
		exactWords.put("ARROW","shaugit");
		exactWords.put("ARTILLERY","topa");
		exactWords.put("ARTISAN","zogtar");
		exactWords.put("ASH","hi");
		exactWords.put("ASSASSIN","vras");
		exactWords.put("AT","na");
		exactWords.put("ATTACK","inras");
		exactWords.put("ATTACK","sulmog");
		exactWords.put("AUTUMN","visht");
		exactWords.put("AVALANCHE","rung(al)");
		exactWords.put("AWE","poni");
		exactWords.put("AXE","sapat");
		exactWords.put("BABY","foshnu");
		exactWords.put("BACK","kurrauz");
		exactWords.put("BAD HABIT","ovani");
		exactWords.put("BAG","thos");
		exactWords.put("BAKE","pik");
		exactWords.put("BALD","paflok");
		exactWords.put("BAND","rup");
		exactWords.put("LEATHER","rup");
		exactWords.put("BANK","ana");
		exactWords.put("RIVER","ana");
		exactWords.put("BAR","loz");
		exactWords.put("BAR","shul");
		exactWords.put("BARB","mosh");
		exactWords.put("BARK","lavozagh");
		exactWords.put("TREE","lavozagh");
		exactWords.put("BARLEY","olb");
		exactWords.put("BARRACKS","kazorm");
		exactWords.put("BARREL","bukol");
		exactWords.put("BARROW","kaup-du");
		exactWords.put("BARROW","suk");
		exactWords.put("BASIN","logon");
		exactWords.put("BASKET","kosh");
		exactWords.put("BASKET","skort");
		exactWords.put("BASTINADO","raf");
		exactWords.put("BATTERING-RAM","kridash");
		exactWords.put("BATTLE","lutaum");
		exactWords.put("BATTLEFIELD","fushjalut");
		exactWords.put("BE-AHEAD","pros kokan");
		exactWords.put("BEACON","paustar");
		exactWords.put("BEAK","skop");
		exactWords.put("BEAK","skup");
		exactWords.put("BEAKER","kolk");
		exactWords.put("BEAM","tra");
		exactWords.put("TIMBER","tra");
		exactWords.put("BEAR","arau");
		exactWords.put("BEARD","mikar");
		exactWords.put("BEAST","staz");
		exactWords.put("BEG","lup");
		exactWords.put("BEHIND","prap");
		exactWords.put("BELL","kumbon");
		exactWords.put("BELLOWS","kakok");
		exactWords.put("BELOW","nen");
		exactWords.put("BELT","broz");
		exactWords.put("BENCH","tog");
		exactWords.put("BENT","parkulun");
		exactWords.put("BERRY","kokar");
		exactWords.put("BESIDE","pran");
		exactWords.put("BESIEGE","rothos");
		exactWords.put("BEYOND","ti");
		exactWords.put("BIG","madh");
		exactWords.put("BILLHOOK","kiz");
		exactWords.put("BIND","doturog");
		exactWords.put("BIND","krimp");
		exactWords.put("BIRCH","blotaz");
		exactWords.put("BIRD","shapend");
		exactWords.put("BIRD","zog");
		exactWords.put("BITE","kafsog");
		exactWords.put("BLACK","zau");
		exactWords.put("BLACK","zi");
		exactWords.put("BLACKEN","nixi");
		exactWords.put("BLADE","pros");
		exactWords.put("BLEAK","fatoft");
		exactWords.put("BLIND","vorbat");
		exactWords.put("BLIZZARD","pogalm");
		exactWords.put("BLOOD","blog");
		exactWords.put("BLOOD","gijak");
		exactWords.put("BLOODSHED","gijakudob");
		exactWords.put("BLOODSTAINED","pargijakun");
		exactWords.put("BLOODTHIRSTY","gijakpis");
		exactWords.put("BLOW","frib");
		exactWords.put("BLUE","kartart");
		exactWords.put("BOAR","dorr");
		exactWords.put("BOAR","goltur");
		exactWords.put("BOAT","lundar");
		exactWords.put("BODY","trup");
		exactWords.put("BODYGUARD","rog-votak");
		exactWords.put("BOG","mosal");
		exactWords.put("BOIL","zau");
		exactWords.put("BOLT","lloz");
		exactWords.put("DOOR","lloz");
		exactWords.put("BONE","asht");
		exactWords.put("BOOT","kapuk");
		exactWords.put("BORDER","kufi");
		exactWords.put("BOULDER","curr");
		exactWords.put("BOW","bogi");
		exactWords.put("BOW","lak");
		exactWords.put("BRAIN","tru");
		exactWords.put("BRANCH","dob");
		exactWords.put("BRAWL","bartas");
		exactWords.put("BREAD","buk");
		exactWords.put("BREAK","molva");
		exactWords.put("BREAK","thu");
		exactWords.put("BREED","rraus");
		exactWords.put("BRESS","praunk");
		exactWords.put("BREW","zim");
		exactWords.put("BRICK","korpaus");
		exactWords.put("MUD","korpaus");
		exactWords.put("BRIDGE","ur");
		exactWords.put("BRIGHT","shendrautsham");
		exactWords.put("BRILLIANT","skalkisham");
		exactWords.put("BRING","thrak");
		exactWords.put("BROAD","gogan");
		exactWords.put("BROADSWORD","hanksar");
		exactWords.put("BRONZE","zoshk");
		exactWords.put("BROTH","lang-maush");
		exactWords.put("BROWN","zoshkat");
		exactWords.put("BRUTE","shataz");
		exactWords.put("BUILD","nudertog");
		exactWords.put("BULGE","bungo");
		exactWords.put("BULL","dom");
		exactWords.put("BULL","mazat");
		exactWords.put("BURIAL-GROUND","vorroz");
		exactWords.put("BURN","dig");
		exactWords.put("BURROW","zagavarr");
		exactWords.put("BURY","jargza");
		exactWords.put("BURY","vorrog");
		exactWords.put("BUSH","druth");
		exactWords.put("BUTTER","talun");
		exactWords.put("BUTTOCK","hom");
		exactWords.put("BY","afar");
		exactWords.put("CAGE","kafaz");
		exactWords.put("CALL","bugd");
		exactWords.put("CAMP","fushaum");
		exactWords.put("CANDLE","kaur");
		exactWords.put("CAP","kasul");
		exactWords.put("CAPE","bruk");
		exactWords.put("CAPTAIN","kritar");
		exactWords.put("CARRION","marsh");
		exactWords.put("CARRION CROW","starkok");
		exactWords.put("CART","korn");
		exactWords.put("CARVE","gaduhend");
		exactWords.put("CASTE","faus");
		exactWords.put("CASTLE","kala");
		exactWords.put("CASTRATION","trod");
		exactWords.put("CAULDRON","kazan");
		exactWords.put("CAVALRY","kalors");
		exactWords.put("CAVE","shapol");
		exactWords.put("CAVE","shatroful");
		exactWords.put("CELL","kolauz");
		exactWords.put("CENTER","mos");
		exactWords.put("CESSPOOL","bagronk");
		exactWords.put("CHAIN","varg");
		exactWords.put("CHALK","shakumbas");
		exactWords.put("CHAMBER","od");
		exactWords.put("CHANNEL","vi");
		exactWords.put("CHARM","fal");
		exactWords.put("CHEST","kista");
		exactWords.put("CHIEF","kri-krisur");
		exactWords.put("CHIEF","krual");
		exactWords.put("CHIMNEY","oxhak");
		exactWords.put("CIRCLE","roth");
		exactWords.put("CITADEL","kutotaz");
		exactWords.put("CITY","goi");
		exactWords.put("CITY","kitot");
		exactWords.put("CLAN","bajrak");
		exactWords.put("CLAN","fos");
		exactWords.put("CLASP","mubarthok");
		exactWords.put("CLAW","bukra");
		exactWords.put("CLAW","kathotar");
		exactWords.put("CLAY","bot");
		exactWords.put("CLEAVE","kag");
		exactWords.put("CLEFT","kam");
		exactWords.put("CLEFT","kar");
		exactWords.put("CLIFF","shakamb");
		exactWords.put("CLOAK","gun");
		exactWords.put("CLOAK","potak");
		exactWords.put("CLOSE","mubull");
		exactWords.put("CLOTH","palhur");
		exactWords.put("CLOUD","ro");
		exactWords.put("CLOUDED/CLOUDY","varanat");
		exactWords.put("CLUB","kopak");
		exactWords.put("COAL","kumur");
		exactWords.put("COCK","gajol");
		exactWords.put("COLD","fatoft");
		exactWords.put("COLOR","nagir");
		exactWords.put("COMMAND","urdanog");
		exactWords.put("COMMON","pa-shi");
		exactWords.put("CONCLAVE","kashaul");
		exactWords.put("CONDITION","um");
		exactWords.put("CONQUER","sundog");
		exactWords.put("CONQUEST","sundaum");
		exactWords.put("CONTEST","mund");
		exactWords.put("COOK","pik");
		exactWords.put("COPPER","tem");
		exactWords.put("CORNER","kand");
		exactWords.put("COUNTRY","de");
		exactWords.put("COUNTRY","krahaun");
		exactWords.put("COUNTRY","tok");
		exactWords.put("COUNTRY","trov");
		exactWords.put("COURT","oborr");
		exactWords.put("COURT","gikator");
		exactWords.put("LAW","gikator");
		exactWords.put("COWARD","fraukanak");
		exactWords.put("COWARD","ragur");
		exactWords.put("COWARD","skraefa");
		exactWords.put("COWARD","tutas");
		exactWords.put("COWARD","zemarpak");
		exactWords.put("CRACK","plas");
		exactWords.put("CRAFT","shakathsi");
		exactWords.put("CRAFT","zog");
		exactWords.put("CRAFTSMAN","zogtar");
		exactWords.put("CRAG","thop");
		exactWords.put("CRAGGY","thopausan");
		exactWords.put("CRAWL","ok");
		exactWords.put("CRAWL","zovarr");
		exactWords.put("CRAZY","galin");
		exactWords.put("CREEP","skrigz");
		exactWords.put("CREVASSE","plaskom");
		exactWords.put("CRIPPLE","ulog");
		exactWords.put("CROSSROAD","udakruk");
		exactWords.put("CROW","gal");
		exactWords.put("CROW","sorr");
		exactWords.put("CRUCIFY","kruksog");
		exactWords.put("CRUEL","mauzur");
		exactWords.put("CRUSH","marzgi");
		exactWords.put("CRUSH","shatup");
		exactWords.put("CULT","bos");
		exactWords.put("CULT","fe");
		exactWords.put("CUP","zan");
		exactWords.put("CURE","mikog");
		exactWords.put("CURSE","bolvag");
		exactWords.put("CUT","plag");
		exactWords.put("CUT","pros");
		exactWords.put("DAGGER","kam");
		exactWords.put("DAGGER","kurtil");
		exactWords.put("DAGGER","thauk");
		exactWords.put("DARK","burz");
		exactWords.put("DARK","mubullat");
		exactWords.put("DARK","orrat");
		exactWords.put("DARKNESS","burzum");
		exactWords.put("DART","bauz");
		exactWords.put("DART","shatauz");
		exactWords.put("DASH","flak");
		exactWords.put("DAWN","agon");
		exactWords.put("DAWN","daga");
		exactWords.put("DAY","dautas");
		exactWords.put("DAYLIGHT","draut");
		exactWords.put("DEAD","vadokan");
		exactWords.put("DEADLY","vadokiprus");
		exactWords.put("DEAF","shadur");
		exactWords.put("DEATH","gurz");
		exactWords.put("DEATH","vadok");
		exactWords.put("DECAY","prasog");
		exactWords.put("DEEP","tholl");
		exactWords.put("DEER","nadro");
		exactWords.put("DEFEAT","ugl");
		exactWords.put("DEFECATE","dahautom");
		exactWords.put("DEFECATION","dahaut");
		exactWords.put("DEFENSE","muprogit");
		exactWords.put("DELICACY","moz");
		exactWords.put("DELVE","garmog");
		exactWords.put("DEMOLISH","rifa");
		exactWords.put("DEMON","dagul");
		exactWords.put("DEMON","drok");
		exactWords.put("DEMON","pauzul");
		exactWords.put("DEMONIAC","dagalur");
		exactWords.put("DEN","shatrofuk");
		exactWords.put("DEN","shatroful");
		exactWords.put("DEPENDENT","haz");
		exactWords.put("DEPUTY","zavandas");
		exactWords.put("DESCENT","zabrat");
		exactWords.put("DESTROY","egur");
		exactWords.put("DESTROY","zaduk");
		exactWords.put("DIE","mat");
		exactWords.put("DIG","garmog");
		exactWords.put("DIG","grafa");
		exactWords.put("DIKE","hondok");
		exactWords.put("DIM","mugat");
		exactWords.put("DIN","zurm");
		exactWords.put("DIRE","laug");
		exactWords.put("DIRE","ugurz");
		exactWords.put("DIRT","flauk");
		exactWords.put("DIRTY","onreinn");
		exactWords.put("DISASTER","bolb");
		exactWords.put("DISASTER","dam");
		exactWords.put("DISEASE","langat");
		exactWords.put("DISEASE","samund");
		exactWords.put("DISEMBOWEL","dakog");
		exactWords.put("DISGUISE","shatragtaum");
		exactWords.put("DISH","logon");
		exactWords.put("DISH","pajat");
		exactWords.put("DISMEMBER","koptog");
		exactWords.put("DISTANT","largat");
		exactWords.put("DISTIL","shakulog");
		exactWords.put("DOG","hundur");
		exactWords.put("DOG","kon");
		exactWords.put("DOME","kub");
		exactWords.put("DOOM","duump");
		exactWords.put("DOOM","fund");
		exactWords.put("DOOR","dar");
		exactWords.put("DOORWAY","dolap");
		exactWords.put("DOWN","poshat");
		exactWords.put("DOWNHILL","taposhat");
		exactWords.put("DRAGON","kulkodar");
		exactWords.put("DRAKE","rosak");
		exactWords.put("DRAUGHT","galtaum");
		exactWords.put("DREAD","dru");
		exactWords.put("DREADFUL","timorsham");
		exactWords.put("DREAR","murg");
		exactWords.put("DRINK","pau");
		exactWords.put("DRIVE","magas");
		exactWords.put("DRIVE AWAY","dabog");
		exactWords.put("DROWN","mabus");
		exactWords.put("DRUG","shushatus");
		exactWords.put("DRUM","daul");
		exactWords.put("DRUM","lodar");
		exactWords.put("DRY","thag");
		exactWords.put("DULL","mubulat");
		exactWords.put("DUMB","pa-gog");
		exactWords.put("DUNG","bagal");
		exactWords.put("DUNGEON","bauruk");
		exactWords.put("DUSK","mug");
		exactWords.put("DUST","pluhun");
		exactWords.put("DUST","ryk");
		exactWords.put("DWARF","shakutarbik");
		exactWords.put("DWARF","vok");
		exactWords.put("DWELLING","banam");
		exactWords.put("DWELLING","banos");
		exactWords.put("EAGLE","shakab");
		exactWords.put("EAGLE","shakapon");
		exactWords.put("EAR","vosh");
		exactWords.put("EARTH","tok");
		exactWords.put("EAST","lind");
		exactWords.put("EAT","ha");
		exactWords.put("EDGE","ana");
		exactWords.put("EDGE","skag");
		exactWords.put("EDIBLE","nagransham");
		exactWords.put("EGG","vo");
		exactWords.put("EGG","voz");
		exactWords.put("ELDER","ma-plak");
		exactWords.put("ELF","golog");
		exactWords.put("ELITE","ta-parat");
		exactWords.put("EMISSARY","dargum");
		exactWords.put("EMPIRE","porandor");
		exactWords.put("EMPTY","zabraz");
		exactWords.put("ENCHANT","namat");
		exactWords.put("ENCLOSURE","thark");
		exactWords.put("ENCLOSURE","thur");
		exactWords.put("END","fund");
		exactWords.put("END","mubaram");
		exactWords.put("ENEMY","armauk");
		exactWords.put("ENGINEER","zongot");
		exactWords.put("ENTER","hu-na");
		exactWords.put("ENTRY","ta-hum");
		exactWords.put("EVER","parhor");
		exactWords.put("EVIL","illska");
		exactWords.put("EVIL","laug(shat)");
		exactWords.put("EVIL SPIRIT","ari");
		exactWords.put("EVISCERATE","nixir");
		exactWords.put("EVISCERATE","zorrat");
		exactWords.put("EXECUTE","vadoksog");
		exactWords.put("EXECUTION","vadoksam");
		exactWords.put("EXILE","margim");
		exactWords.put("EXPEDITION","fushat");
		exactWords.put("EXPLODE","palkas");
		exactWords.put("EXPLOSION","karkat");
		exactWords.put("EXPLOSION","plasi");
		exactWords.put("EXPLOSIVE","karkitas");
		exactWords.put("EXPLOSIVE","plasas");
		exactWords.put("EYE","auga");
		return exactWords;
	}
}
