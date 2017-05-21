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

public class Ignan extends StdLanguage
{
	@Override
	public String ID()
	{
		return "Ignan";
	}

	private final static String localizedName = CMLib.lang().L("Ignan");

	@Override
	public String name()
	{
		return localizedName;
	}

	public static List<String[]> wordLists=null;
	public Ignan()
	{
		super();
	}

	@Override
	public CMObject newInstance()
	{
		return new Ignan();
	}

	@Override
	public List<String[]> translationVector(String language)
	{
		if(wordLists==null)
		{
			final String[] one={"am","sa","pu","ra","an","aj","na","dh"};
			final String[] two={"pul","sak","dhu","ka","rej","ya","no","la","iy","da","vra","ni","ik"};
			final String[] three={"nya","rik","iwa","tya","mat","kaj","raj","lak","vak","dra","vik","sat","nav"};
			final String[] four={"nahi","ty'aj","tras","dams","dagh","jatu","chik","jhak","gruk","k'sip","kuha","ucca","inkh","asya","abhi","arya","isti","id'va","mala","mrga","s'ami","sira","skus","stup"};
			final String[] five={"stigh","stubh","stoka","me'das","mraks","ghana","buk'ni","narka","pams'u","niska","netrya","ni'jh","dhu'li","dhraj","taran","tarus","jungu","chupna","jattu","chirnu","cukvu","jhakut","jhaur","udaya","ilih"};
			final String[] six={"masu'ra","marttika","bhai'sa","bollai","vila'sini","barhis't'ha","barkro","parada","nikauriya","nird'har","dhanik","ja'n'o","tanakti","chikka'ra","kullada","uccaya","utsava","istaka","i'rsya","inkhuksa","akha'ra","adhuna","avas'dyak"};
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
		hashwords.put("ABODE","okah");
		hashwords.put("ABOVE","adhi");
		hashwords.put("ACID","amla");
		hashwords.put("ACCOMPLISH","daks");
		hashwords.put("AIM","artha");
		hashwords.put("ALSO","api");
		hashwords.put("APPROVAL","ati");
		hashwords.put("ARM","bahu");
		hashwords.put("ARMY","jurnih");
		hashwords.put("AROUND","pari");
		hashwords.put("ATMOSPHERE","krntatram");
		hashwords.put("AUNT","attai");
		hashwords.put("AWAY","ava");
		hashwords.put("AXLE","aksa");
		hashwords.put("BAD","agha");
		hashwords.put("BANK","rodhas");
		hashwords.put("BATTLE","khala");
		hashwords.put("BEARD","s'ru");
		hashwords.put("BELOW","adhah");
		hashwords.put("BEND","ac");
		hashwords.put("BEYOND","ati");
		hashwords.put("BIND","and");
		hashwords.put("BLIND","andha");
		hashwords.put("BRAND","alata");
		hashwords.put("BREAKING","vi");
		hashwords.put("BREAST","vaksas");
		hashwords.put("BREATHE","an");
		hashwords.put("BRIDE","vadhu");
		hashwords.put("BROTHER","bhrata");
		hashwords.put("CLEAR","accha");
		hashwords.put("CLOTHES","vastram");
		hashwords.put("CLOUD","sasnim");
		hashwords.put("COAL","angara");
		hashwords.put("COMBINATION","sam");
		hashwords.put("CONTACT","api");
		hashwords.put("COVER","vala");
		hashwords.put("COW","usriya");
		hashwords.put("CROW","ka'ka");
		hashwords.put("CURL","alaka");
		hashwords.put("DAY","ghramsa");
		hashwords.put("DAYS","svasarani");
		hashwords.put("DELIGHTFUL","ramsu");
		hashwords.put("DESERT","arana");
		hashwords.put("DESPICABLE","drati");
		hashwords.put("DEW","avas'ya");
		hashwords.put("DICE","aksa");
		hashwords.put("DIFFERENT","antara");
		hashwords.put("DISAPPROVAL","nir");
		hashwords.put("DISPLEASURE","hi");
		hashwords.put("DISTANT","duram");
		hashwords.put("DOOR","dvarah");
		hashwords.put("DOWN","ava");
		hashwords.put("DOWNWARDS","ni");
		hashwords.put("DRIVE","aj");
		hashwords.put("DROP","prsatah");
		hashwords.put("EAT","ad");
		hashwords.put("EDGE","anika");
		hashwords.put("END","anta");
		hashwords.put("ENTRAILS","antra");
		hashwords.put("ENTRUSTED","arpya");
		hashwords.put("EVEN","api");
		hashwords.put("EYE","aksih");
		hashwords.put("FACE","anika");
		hashwords.put("FATHER","tatah");
		hashwords.put("FINE","anu");
		hashwords.put("FINGER","anguli");
		hashwords.put("FINGERS","angulayah");
		hashwords.put("FIRE","agni");
		hashwords.put("FIST","kas'i");
		hashwords.put("FIXED","arpya");
		hashwords.put("FLASH","arka");
		hashwords.put("FLOOD","vah");
		hashwords.put("FLYING","dayamanah");
		hashwords.put("FOOD","sinam");
		hashwords.put("FOOL","ja'mi");
		hashwords.put("FORWARD","pra");
		hashwords.put("FRAGMENT","khanda");
		hashwords.put("FRONT","pragram");
		hashwords.put("FROM","prati");
		hashwords.put("GAMBLER","s'vaghni");
		hashwords.put("GIFT","tujah");
		hashwords.put("GIVE","s'is'iti");
		hashwords.put("GOAT","aja");
		hashwords.put("GO","amb");
		hashwords.put("GOING","aya");
		hashwords.put("GOLD","hiranyam");
		hashwords.put("GRAIN","saktuh");
		hashwords.put("GREAT","aminah");
		hashwords.put("HALF","ardha");
		hashwords.put("HAND","panh");
		hashwords.put("HEEL","adi");
		hashwords.put("HERBS","virudhah");
		hashwords.put("HIGH","atta");
		hashwords.put("HOME","ama");
		hashwords.put("HOMES","grhah");
		hashwords.put("HOOK","ankus'a");
		hashwords.put("HORNY","nissapi");
		hashwords.put("HORSE","dadhikra");
		hashwords.put("HOUSE","agara");
		hashwords.put("HURT","ard");
		hashwords.put("INACTIVE","alasa");
		hashwords.put("INJURED","arta");
		hashwords.put("INSIDE","antar");
		hashwords.put("INTIMATE","aryamya");
		hashwords.put("IRON","ayas");
		hashwords.put("JOINED","u");
		hashwords.put("KILL","kutsa");
		hashwords.put("KNOWLEDGE","kila");
		hashwords.put("LIGHT","kus'ika");
		hashwords.put("LIKE","iva");
		hashwords.put("LIMB","anga");
		hashwords.put("LOWER","avara");
		hashwords.put("MAIDEN","kanya");
		hashwords.put("MAN","maryah");
		hashwords.put("MEN","nara");
		hashwords.put("MERCHANT","pani");
		hashwords.put("MESSENGER","dutah");
		hashwords.put("METAL","ayas");
		hashwords.put("MILK","dhena");
		hashwords.put("MIND","manas");
		hashwords.put("MINUTE","anu");
		hashwords.put("MONKEY","harih");
		hashwords.put("MOTHER","atta");
		hashwords.put("MUCH","bahu");
		hashwords.put("MUSHROOM","ksumpam");
		hashwords.put("NEAR","anti");
		hashwords.put("NEARER","avara");
		hashwords.put("NECK","griva");
		hashwords.put("NET","nidha");
		hashwords.put("NOURISHES","posati");
		hashwords.put("OFFSPRING","apatya");
		hashwords.put("ON","adhi");
		hashwords.put("OPPOSITION","aha");
		hashwords.put("OUT","apa");
		hashwords.put("OVER","ati");
		hashwords.put("PAIL","kos'a");
		hashwords.put("PART","ams'a");
		hashwords.put("PERFECT","aditi");
		hashwords.put("PIT","avata");
		hashwords.put("PLACE","ardha");
		hashwords.put("PLAYER","devara");
		hashwords.put("POINT","anika");
		hashwords.put("POISONOUS","alakta");
		hashwords.put("POLE","dhuh");
		hashwords.put("POSTERIOR","apara");
		hashwords.put("PRESS","amh");
		hashwords.put("PRIEST","rtwik");
		hashwords.put("PROHIBITION","khalu");
		hashwords.put("PROTECTION","utih");
		hashwords.put("PROXIMITY","anta");
		hashwords.put("PUNISH","danda");
		hashwords.put("PURIFY","pavitram");
		hashwords.put("QUARTERS","kastha");
		hashwords.put("RAIN","abhra");
		hashwords.put("RAM","mesah");
		hashwords.put("REACH","as");
		hashwords.put("REASON","nu");
		hashwords.put("RESPECT","cid");
		hashwords.put("RICE","atta");
		hashwords.put("RIVER","nadyah");
		hashwords.put("ROAD","adhvan");
		hashwords.put("ROW","s'reni");
		hashwords.put("SCREEN","ada");
		hashwords.put("SCORPION","alin");
		hashwords.put("SEA","arnava");
		hashwords.put("SEER","nadah");
		hashwords.put("SENSE","kham");
		hashwords.put("SHEEP","avi");
		hashwords.put("SHINE","arc");
		hashwords.put("SHINING","tvisitah");
		hashwords.put("SHOULDER","skandha");
		hashwords.put("SIEVE","tita");
		hashwords.put("SINGER","nodhas");
		hashwords.put("SISTER","jamih");
		hashwords.put("SIX","sat");
		hashwords.put("SLAY","vanusyati");
		hashwords.put("SMALL","hrasva");
		hashwords.put("SON","vahni");
		hashwords.put("SOUL","atma");
		hashwords.put("SOUND","kunarum");
		hashwords.put("SOUR","amla");
		hashwords.put("SPOKE","ara");
		hashwords.put("SPREAD","tan");
		hashwords.put("STAFF","dandin");
		hashwords.put("STARS","strahih");
		hashwords.put("STING","ala");
		hashwords.put("STINKY","kepuyah");
		hashwords.put("STOMACH","jatharam");
		hashwords.put("STREAM","visruhah");
		hashwords.put("STRONGLY","barhana");
		hashwords.put("SUN","aru");
		hashwords.put("SUPPLE","srprah");
		hashwords.put("SUPREME","adhi");
		hashwords.put("SWANS","hamsah");
		hashwords.put("TEAR","as'ru");
		hashwords.put("THIEF","stena");
		hashwords.put("THIS","ayam");
		hashwords.put("THRONE","garta");
		hashwords.put("THUNDER","vajrah");
		hashwords.put("THUMB","angustha");
		hashwords.put("TOE","anguli");
		hashwords.put("TOP","agra");
		hashwords.put("TORCH","li");
		hashwords.put("TOWARDS","abhi");
		hashwords.put("TRANSPARENT","accha");
		hashwords.put("TREASURE","s'evadhi");
		hashwords.put("TREE","vrksa");
		hashwords.put("TRUTH","satya");
		hashwords.put("TURTLE","kacchapa");
		hashwords.put("UNCERTAIN","nunam");
		hashwords.put("UNCERTAINTY","s'as'vat");
		hashwords.put("UNDER","adhah");
		hashwords.put("UNLIKE","na");
		hashwords.put("UNKNOWN","josavakam");
		hashwords.put("UPWARDS","ud");
		hashwords.put("WAGON","anas");
		hashwords.put("WANDER","at");
		hashwords.put("WANTON","ahana");
		hashwords.put("WATER","udakam");
		hashwords.put("WAVE","arnava");
		hashwords.put("WEALTH","rekna");
		hashwords.put("WELL","kupa");
		hashwords.put("WHEEL","cakram");
		hashwords.put("WISE","medhavi");
		hashwords.put("WOMAN","yosa");
		hashwords.put("WOOD","daru");
		hashwords.put("WORK","damsayah");
		hashwords.put("WORSHIP","yajna");
		hashwords.put("WORSHIPPING","arcana");
		return hashwords;
		}
	}

