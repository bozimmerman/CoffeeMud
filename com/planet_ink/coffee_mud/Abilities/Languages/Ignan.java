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
Copyright 2018-2022 Bo Zimmerman

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
	public List<String[]> translationLists(final String language)
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

	private static final Map<String,String> exactWords=new TreeMap<String,String>();

	@Override
	public Map<String, String> translationHash(final String language)
	{
		if((exactWords!=null)&&(exactWords.size()>0))
			return exactWords;
		exactWords.put("ABODE","okah");
		exactWords.put("ABOVE","adhi");
		exactWords.put("ACID","amla");
		exactWords.put("ACCOMPLISH","daks");
		exactWords.put("AIM","artha");
		exactWords.put("ALSO","api");
		exactWords.put("APPROVAL","ati");
		exactWords.put("ARM","bahu");
		exactWords.put("ARMY","jurnih");
		exactWords.put("AROUND","pari");
		exactWords.put("ATMOSPHERE","krntatram");
		exactWords.put("AUNT","attai");
		exactWords.put("AWAY","ava");
		exactWords.put("AXLE","aksa");
		exactWords.put("BAD","agha");
		exactWords.put("BANK","rodhas");
		exactWords.put("BATTLE","khala");
		exactWords.put("BEARD","s'ru");
		exactWords.put("BELOW","adhah");
		exactWords.put("BEND","ac");
		exactWords.put("BEYOND","ati");
		exactWords.put("BIND","and");
		exactWords.put("BLIND","andha");
		exactWords.put("BRAND","alata");
		exactWords.put("BREAKING","vi");
		exactWords.put("BREAST","vaksas");
		exactWords.put("BREATHE","an");
		exactWords.put("BRIDE","vadhu");
		exactWords.put("BROTHER","bhrata");
		exactWords.put("CLEAR","accha");
		exactWords.put("CLOTHES","vastram");
		exactWords.put("CLOUD","sasnim");
		exactWords.put("COAL","angara");
		exactWords.put("COMBINATION","sam");
		exactWords.put("CONTACT","api");
		exactWords.put("COVER","vala");
		exactWords.put("COW","usriya");
		exactWords.put("CROW","ka'ka");
		exactWords.put("CURL","alaka");
		exactWords.put("DAY","ghramsa");
		exactWords.put("DAYS","svasarani");
		exactWords.put("DELIGHTFUL","ramsu");
		exactWords.put("DESERT","arana");
		exactWords.put("DESPICABLE","drati");
		exactWords.put("DEW","avas'ya");
		exactWords.put("DICE","aksa");
		exactWords.put("DIFFERENT","antara");
		exactWords.put("DISAPPROVAL","nir");
		exactWords.put("DISPLEASURE","hi");
		exactWords.put("DISTANT","duram");
		exactWords.put("DOOR","dvarah");
		exactWords.put("DOWN","ava");
		exactWords.put("DOWNWARDS","ni");
		exactWords.put("DRIVE","aj");
		exactWords.put("DROP","prsatah");
		exactWords.put("EAT","ad");
		exactWords.put("EDGE","anika");
		exactWords.put("END","anta");
		exactWords.put("ENTRAILS","antra");
		exactWords.put("ENTRUSTED","arpya");
		exactWords.put("EVEN","api");
		exactWords.put("EYE","aksih");
		exactWords.put("FACE","anika");
		exactWords.put("FATHER","tatah");
		exactWords.put("FINE","anu");
		exactWords.put("FINGER","anguli");
		exactWords.put("FINGERS","angulayah");
		exactWords.put("FIRE","agni");
		exactWords.put("FIST","kas'i");
		exactWords.put("FIXED","arpya");
		exactWords.put("FLASH","arka");
		exactWords.put("FLOOD","vah");
		exactWords.put("FLYING","dayamanah");
		exactWords.put("FOOD","sinam");
		exactWords.put("FOOL","ja'mi");
		exactWords.put("FORWARD","pra");
		exactWords.put("FRAGMENT","khanda");
		exactWords.put("FRONT","pragram");
		exactWords.put("FROM","prati");
		exactWords.put("GAMBLER","s'vaghni");
		exactWords.put("GIFT","tujah");
		exactWords.put("GIVE","s'is'iti");
		exactWords.put("GOAT","aja");
		exactWords.put("GO","amb");
		exactWords.put("GOING","aya");
		exactWords.put("GOLD","hiranyam");
		exactWords.put("GRAIN","saktuh");
		exactWords.put("GREAT","aminah");
		exactWords.put("HALF","ardha");
		exactWords.put("HAND","panh");
		exactWords.put("HEEL","adi");
		exactWords.put("HERBS","virudhah");
		exactWords.put("HIGH","atta");
		exactWords.put("HOME","ama");
		exactWords.put("HOMES","grhah");
		exactWords.put("HOOK","ankus'a");
		exactWords.put("HORNY","nissapi");
		exactWords.put("HORSE","dadhikra");
		exactWords.put("HOUSE","agara");
		exactWords.put("HURT","ard");
		exactWords.put("INACTIVE","alasa");
		exactWords.put("INJURED","arta");
		exactWords.put("INSIDE","antar");
		exactWords.put("INTIMATE","aryamya");
		exactWords.put("IRON","ayas");
		exactWords.put("JOINED","u");
		exactWords.put("KILL","kutsa");
		exactWords.put("KNOWLEDGE","kila");
		exactWords.put("LIGHT","kus'ika");
		exactWords.put("LIKE","iva");
		exactWords.put("LIMB","anga");
		exactWords.put("LOWER","avara");
		exactWords.put("MAIDEN","kanya");
		exactWords.put("MAN","maryah");
		exactWords.put("MEN","nara");
		exactWords.put("MERCHANT","pani");
		exactWords.put("MESSENGER","dutah");
		exactWords.put("METAL","ayas");
		exactWords.put("MILK","dhena");
		exactWords.put("MIND","manas");
		exactWords.put("MINUTE","anu");
		exactWords.put("MONKEY","harih");
		exactWords.put("MOTHER","atta");
		exactWords.put("MUCH","bahu");
		exactWords.put("MUSHROOM","ksumpam");
		exactWords.put("NEAR","anti");
		exactWords.put("NEARER","avara");
		exactWords.put("NECK","griva");
		exactWords.put("NET","nidha");
		exactWords.put("NOURISHES","posati");
		exactWords.put("OFFSPRING","apatya");
		exactWords.put("ON","adhi");
		exactWords.put("OPPOSITION","aha");
		exactWords.put("OUT","apa");
		exactWords.put("OVER","ati");
		exactWords.put("PAIL","kos'a");
		exactWords.put("PART","ams'a");
		exactWords.put("PERFECT","aditi");
		exactWords.put("PIT","avata");
		exactWords.put("PLACE","ardha");
		exactWords.put("PLAYER","devara");
		exactWords.put("POINT","anika");
		exactWords.put("POISONOUS","alakta");
		exactWords.put("POLE","dhuh");
		exactWords.put("POSTERIOR","apara");
		exactWords.put("PRESS","amh");
		exactWords.put("PRIEST","rtwik");
		exactWords.put("PROHIBITION","khalu");
		exactWords.put("PROTECTION","utih");
		exactWords.put("PROXIMITY","anta");
		exactWords.put("PUNISH","danda");
		exactWords.put("PURIFY","pavitram");
		exactWords.put("QUARTERS","kastha");
		exactWords.put("RAIN","abhra");
		exactWords.put("RAM","mesah");
		exactWords.put("REACH","as");
		exactWords.put("REASON","nu");
		exactWords.put("RESPECT","cid");
		exactWords.put("RICE","atta");
		exactWords.put("RIVER","nadyah");
		exactWords.put("ROAD","adhvan");
		exactWords.put("ROW","s'reni");
		exactWords.put("SCREEN","ada");
		exactWords.put("SCORPION","alin");
		exactWords.put("SEA","arnava");
		exactWords.put("SEER","nadah");
		exactWords.put("SENSE","kham");
		exactWords.put("SHEEP","avi");
		exactWords.put("SHINE","arc");
		exactWords.put("SHINING","tvisitah");
		exactWords.put("SHOULDER","skandha");
		exactWords.put("SIEVE","tita");
		exactWords.put("SINGER","nodhas");
		exactWords.put("SISTER","jamih");
		exactWords.put("SIX","sat");
		exactWords.put("SLAY","vanusyati");
		exactWords.put("SMALL","hrasva");
		exactWords.put("SON","vahni");
		exactWords.put("SOUL","atma");
		exactWords.put("SOUND","kunarum");
		exactWords.put("SOUR","amla");
		exactWords.put("SPOKE","ara");
		exactWords.put("SPREAD","tan");
		exactWords.put("STAFF","dandin");
		exactWords.put("STARS","strahih");
		exactWords.put("STING","ala");
		exactWords.put("STINKY","kepuyah");
		exactWords.put("STOMACH","jatharam");
		exactWords.put("STREAM","visruhah");
		exactWords.put("STRONGLY","barhana");
		exactWords.put("SUN","aru");
		exactWords.put("SUPPLE","srprah");
		exactWords.put("SUPREME","adhi");
		exactWords.put("SWANS","hamsah");
		exactWords.put("TEAR","as'ru");
		exactWords.put("THIEF","stena");
		exactWords.put("THIS","ayam");
		exactWords.put("THRONE","garta");
		exactWords.put("THUNDER","vajrah");
		exactWords.put("THUMB","angustha");
		exactWords.put("TOE","anguli");
		exactWords.put("TOP","agra");
		exactWords.put("TORCH","li");
		exactWords.put("TOWARDS","abhi");
		exactWords.put("TRANSPARENT","accha");
		exactWords.put("TREASURE","s'evadhi");
		exactWords.put("TREE","vrksa");
		exactWords.put("TRUTH","satya");
		exactWords.put("TURTLE","kacchapa");
		exactWords.put("UNCERTAIN","nunam");
		exactWords.put("UNCERTAINTY","s'as'vat");
		exactWords.put("UNDER","adhah");
		exactWords.put("UNLIKE","na");
		exactWords.put("UNKNOWN","josavakam");
		exactWords.put("UPWARDS","ud");
		exactWords.put("WAGON","anas");
		exactWords.put("WANDER","at");
		exactWords.put("WANTON","ahana");
		exactWords.put("WATER","udakam");
		exactWords.put("WAVE","arnava");
		exactWords.put("WEALTH","rekna");
		exactWords.put("WELL","kupa");
		exactWords.put("WHEEL","cakram");
		exactWords.put("WISE","medhavi");
		exactWords.put("WOMAN","yosa");
		exactWords.put("WOOD","daru");
		exactWords.put("WORK","damsayah");
		exactWords.put("WORSHIP","yajna");
		exactWords.put("WORSHIPPING","arcana");
		return exactWords;
		}
	}

