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
public class Elvish extends StdLanguage
{
	@Override
	public String ID()
	{
		return "Elvish";
	}

	private final static String localizedName = CMLib.lang().L("Elvish");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String getTranslationVerb()
	{
		return "trill(s)";
	}

	public static List<String[]> wordLists=null;
	public Elvish()
	{
		super();
	}

	@Override
	public List<String[]> translationLists(final String language)
	{
		if(wordLists==null)
		{
			final String[] one={"a","e","i","o","á","é","í","ó"};
			final String[] two={"os","vi","ne","vo","li","eh","no","ai","by","et","ce","un","il"};
			final String[] three={"ána","cil","sar","tan","hel","loa","sir","hep","yur","nol","hol","qua","éth"};
			final String[] four={"séya","qual","quel","lara","uqua","sana","yava","masse","yanna","quettaparma","manna","manan","merme","carma","harno","harne","varno","essar","saira","cilta","veuma","norta","turme","saita"};
			final String[] five={"cuiva","cuina","nonwa","imire","nauta","cilta","entuc","norta","latin","lòtea","veuya","veuro","apama","hampa","nurta","firta","saira","holle","herwa","uquen","arcoa","calte","cemma","hanta","tanen"};
			final String[] six={"mahtale","porisalque","hairie","tararan","ambarwa","latina","olòtie","amawil","apacen","yavinqua","apalume","linquilea","menelwa","alassea","nurmea","parmasse","ceniril","heldasse","imirin","earina","calatengew","lapselunga","rianna","eneques"};
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
		exactWords.put("ABANDON","avarta");
		exactWords.put("ABLE","pol");
		exactWords.put("ACCOMMODATE","camta");
		exactWords.put("ACT","car");
		exactWords.put("ADAPT","camta");
		exactWords.put("ADDRESS","tengessë");
		exactWords.put("AFFECT","peresta");
		exactWords.put("AFFECTED","persana");
		exactWords.put("AFFECTION","persanië");
		exactWords.put("AFFECTIONATE","méla");
		exactWords.put("AFTER","nó");
		exactWords.put("AGAINST","ara");
		exactWords.put("AGED","yeniquanta");
		exactWords.put("AGO","luina");
		exactWords.put("AID","resta");
		exactWords.put("ALIKE","óvëa");
		exactWords.put("ALONE","erinqua");
		exactWords.put("ALREADY","epello");
		exactWords.put("ALSO","yando");
		exactWords.put("ALTER","presto");
		exactWords.put("ALWAYS","illumë");
		exactWords.put("AMEN","násië");
		exactWords.put("AMONG","imíca");
		exactWords.put("AND","ar");
		exactWords.put("ANGLE","vennassë");
		exactWords.put("ANNIVERSARY","atyenárë");
		exactWords.put("ANSWER","dangweth");
		exactWords.put("APPARITION","ausa");
		exactWords.put("APPLIANCE","yungwa");
		exactWords.put("APPROACH","analendë");
		exactWords.put("APPROPRIATE","sati");
		exactWords.put("ARTICLE","maca");
		exactWords.put("AS","ier");
		exactWords.put("ASSEMBLE","yocar");
		exactWords.put("ATTACK","nalanta");
		exactWords.put("BEFORE","epë");
		exactWords.put("BEGETTER","ammë");
		exactWords.put("BEGIN","yesta");
		exactWords.put("BEGINNING","yessë");
		exactWords.put("BELT","quilta");
		exactWords.put("BESIDE","apa");
		exactWords.put("BETRAY","varta");
		exactWords.put("BETRAYER","varto");
		exactWords.put("BE","nauva");
		exactWords.put("BEYOND","lá");
		exactWords.put("BLADE","maica");
		exactWords.put("BLEND","ostimë");
		exactWords.put("BLESSED","manaquenta");
		exactWords.put("BLOOM","etelotya");
		exactWords.put("BLOSSOM","olótë");
		exactWords.put("BOOT","saipo");
		exactWords.put("BOUND","lanya");
		exactWords.put("BOUNDS","lanwa");
		exactWords.put("BOUNDARY","taica");
		exactWords.put("BROTH","salpa");
		exactWords.put("BURDEN","cólo");
		exactWords.put("BUT","ono");
		exactWords.put("CAN","ista");
		exactWords.put("CAT","miura");
		exactWords.put("CATCH","raita");
		exactWords.put("CENTRE","entë");
		exactWords.put("CENTER","entë");
		exactWords.put("CENTRAL","entya");
		exactWords.put("CERTAINLY","tancavë");
		exactWords.put("CHAIN","angwenda");
		exactWords.put("CHARACTER","indómë");
		exactWords.put("CHEERS","almien");
		exactWords.put("CHESS","arantyalmë");
		exactWords.put("CHESSBOARD","artapano");
		exactWords.put("CHOOSE","cil");
		exactWords.put("CHRIST","Elpino");
		exactWords.put("CIGARETTE","uscillë");
		exactWords.put("CITY","minassë");
		exactWords.put("CLEANSE","poita");
		exactWords.put("CLOAK","fanta");
		exactWords.put("CLOCK","lúma");
		exactWords.put("CLOSED","avalatya");
		exactWords.put("CLOSENESS","aquapahtië");
		exactWords.put("CLOTHE","hap");
		exactWords.put("CLOTHING","hampë");
		exactWords.put("COMING","tulessë");
		exactWords.put("COMPASSION","ófelmë");
		exactWords.put("COMPOSE","yocar");
		exactWords.put("COMPRESS","sanga");
		exactWords.put("CONCERN","apa");
		exactWords.put("CONCERNING","pá");
		exactWords.put("CONFIRM","tancata");
		exactWords.put("CONFRONTING","nimba");
		exactWords.put("CONSONANT","náva-tengwë");
		exactWords.put("CONSTRUCT","yocar");
		exactWords.put("CONVERSE","artaquet");
		exactWords.put("COPPER","calarus");
		exactWords.put("COURSE","ratta");
		exactWords.put("COVERING","vailë");
		exactWords.put("CURSE","racco");
		exactWords.put("DAILY","ilyarëa");
		exactWords.put("DANGER","raxalë");
		exactWords.put("DEBATE","artaquet");
		exactWords.put("DEBT","luhta");
		exactWords.put("DEBTOR","lucando");
		exactWords.put("DEEM","ndab");
		exactWords.put("DEER","arassë");
		exactWords.put("DEFENCE","ortírië");
		exactWords.put("DEFINED","lanwa");
		exactWords.put("DELIVER","eterúna");
		exactWords.put("DEMON","úmaia");
		exactWords.put("DENSE","sangwa");
		exactWords.put("DEPRIVED","racinë");
		exactWords.put("DESCRIBE","úna");
		exactWords.put("DESIRE","námië");
		exactWords.put("DESPISE","nattira");
		exactWords.put("DESTITUTE","úna");
		exactWords.put("DEVIL","úmaia");
		exactWords.put("DIFFICULTY","taryassë");
		exactWords.put("DIPHTHONG","ohlon");
		exactWords.put("DISTANCE","hayassë");
		exactWords.put("DISTANCING","hailë");
		exactWords.put("DISTURB","peresta");
		exactWords.put("DIVIDE","sati");
		exactWords.put("DIVINE","eruva");
		exactWords.put("DOOR","fendassë");
		exactWords.put("DOORWAY","fendassë");
		exactWords.put("DOWN","nat");
		exactWords.put("DRAIN","suhta");
		exactWords.put("DRINK","yulmë");
		exactWords.put("DRINKER","yulmo");
		exactWords.put("DRUM","rambillë");
		exactWords.put("DYNASTY","hilyalë");
		exactWords.put("ECCLESIASTIC","hostalya");
		exactWords.put("EDGE","lanca");
		exactWords.put("EIGHTH","toldëa");
		exactWords.put("ELDER","anyáro");
		exactWords.put("EMINENT","minya");
		exactWords.put("EMOTION","felmë");
		exactWords.put("EMPLOY","yuhta");
		exactWords.put("ENHANCE","han");
		exactWords.put("ENCLOSE","lanya");
		exactWords.put("END","lancassë");
		exactWords.put("ENDURE","larta");
		exactWords.put("ENFOLD","hampë");
		exactWords.put("ENLACED","raina");
		exactWords.put("ENQUIRY","minasurië");
		exactWords.put("ENTANGLED","rembina");
		exactWords.put("ENTER","mitta");
		exactWords.put("ENTRAP","remi");
		exactWords.put("ENTREATY","arcandë");
		exactWords.put("ERROR","mista");
		exactWords.put("ESTABLISH","tancata");
		exactWords.put("ETERNITY","oirë");
		exactWords.put("EVIL","ulka");
		exactWords.put("EXCAVATE","rosta");
		exactWords.put("EXCLUDED","satya");
		exactWords.put("EXILE","etelerro");
		exactWords.put("EXILED","etelenda");
		exactWords.put("EXTENSION","taima");
		exactWords.put("FACE","nívë");
		exactWords.put("FACING","nimba");
		exactWords.put("FASHION","eccat");
		exactWords.put("FASTENING","tanca");
		exactWords.put("FEEL","tenya");
		exactWords.put("FEELING","tendilë");
		exactWords.put("FENCE","ettelë");
		exactWords.put("FIFTH","lempëa");
		exactWords.put("FINGER","lepsë");
		exactWords.put("FINITE","lanwa");
		exactWords.put("FIRM","talya");
		exactWords.put("FIT","camta");
		exactWords.put("FLOOD","oloiya");
		exactWords.put("FLOWER","lotsë");
		exactWords.put("FLY","ramya");
		exactWords.put("FOLLOW","veuya");
		exactWords.put("FOLLOWER","veuro");
		exactWords.put("FOLLOWING","hilmë");
		exactWords.put("FOOD","sulpa");
		exactWords.put("FOOTSTOOL","sarassë");
		exactWords.put("FOR","rá");
		exactWords.put("FORCE","sahtië");
		exactWords.put("FOREBODE","apaquet");
		exactWords.put("FORESTER","apaquet");
		exactWords.put("FORGIVE","apsene");
		exactWords.put("FORLORN","úna");
		exactWords.put("FORSAKE","avarta");
		exactWords.put("FORT","minassë");
		exactWords.put("FORTRESS","arta");
		exactWords.put("FOUNTAIN","ehtelë");
		exactWords.put("FOURTH","cantëa");
		exactWords.put("FOXY","ruscu");
		exactWords.put("FROG","carpo");
		exactWords.put("FRONT","nívë");
		exactWords.put("FULL","penquanta");
		exactWords.put("FULLNESS","quantassë");
		exactWords.put("FUR","helet");
		exactWords.put("FUTURE","apalúmë");
		exactWords.put("GARMENT","hampë");
		exactWords.put("GATEWAY","fendassë");
		exactWords.put("GATHERING","hostalë");
		exactWords.put("GET","net");
		exactWords.put("GLOOM","nimbë");
		exactWords.put("GLORIFY","alcarya");
		exactWords.put("GNAW","nyanda");
		exactWords.put("GORGE","capië");
		exactWords.put("GRACE","erulisse");
		exactWords.put("GRACIOUS","raina");
		exactWords.put("GRAVE","sarca");
		exactWords.put("GREET","suila");
		exactWords.put("GREETING","suilië");
		exactWords.put("GROW","lauya");
		exactWords.put("HALLOW","airita");
		exactWords.put("HARASS","tarasta");
		exactWords.put("HARD","sarda");
		exactWords.put("HARP","tanta");
		exactWords.put("HARVEST","cermië");
		exactWords.put("HASSOCK","sarassë");
		exactWords.put("HAVE","inyen");
		exactWords.put("HAVEN","ciryapanda");
		exactWords.put("HE","së");
		exactWords.put("HEAVE","solto");
		exactWords.put("HEAVEN","eruman");
		exactWords.put("HEM","lanë");
		exactWords.put("HERO","salyon");
		exactWords.put("HOLD","hep");
		exactWords.put("HOMELESS","avamarwa");
		exactWords.put("HONOUR","han");
		exactWords.put("HOP","lapa");
		exactWords.put("HOPE","amatírë");
		exactWords.put("HORN","ramna");
		exactWords.put("HUNGRY","maita");
		exactWords.put("HUNT","fara");
		exactWords.put("HURL","hat");
		exactWords.put("HYMN","airelinna");
		exactWords.put("IF","ai");
		exactWords.put("IMPEDED","tapta");
		exactWords.put("IMPEL","or");
		exactWords.put("IMPELLED","horna");
		exactWords.put("IMPETUOUS","ascara");
		exactWords.put("IMPULSE","hroafelmë");
		exactWords.put("IN","mina");
		exactWords.put("INACTION","lacarë");
		exactWords.put("INADEQUATE","penya");
		exactWords.put("INCREASE","han");
		exactWords.put("INDICATE","tëa");
		exactWords.put("INDUCE","sahta");
		exactWords.put("INEVITABILITY","sangië");
		exactWords.put("INSPIRE","mihwesta");
		exactWords.put("INSULT","ehta");
		exactWords.put("INTEND","elya");
		exactWords.put("INTO","mitta");
		exactWords.put("INUNDATE","oloiya");
		exactWords.put("INWARDS","mitta");
		exactWords.put("ISLANDER","tolloquen");
		exactWords.put("JESUS","yésus");
		exactWords.put("JUDGE","nem");
		exactWords.put("JUDGEMENT","námië");
		exactWords.put("JUMP","lapa");
		exactWords.put("JOURNEY","lendë");
		exactWords.put("KEEP","hep");
		exactWords.put("KINDLE","calta");
		exactWords.put("KINGDOM","turindië");
		exactWords.put("KITCHEN","mastasan");
		exactWords.put("KNEAD","mascata");
		exactWords.put("LABOUR","arassë");
		exactWords.put("LACE","raiwë");
		exactWords.put("LACKING","penya");
		exactWords.put("LAMENTING","nainala");
		exactWords.put("LAST","larta");
		exactWords.put("LEAD","tulya");
		exactWords.put("LEAN","linya");
		exactWords.put("LEAP","cap");
		exactWords.put("LEARN","nolya");
		exactWords.put("LENITION","persanië");
		exactWords.put("LENITED","persana");
		exactWords.put("LIBRARY","parmassë");
		exactWords.put("LIBRARIAN","parmasson");
		exactWords.put("LIBRARIANS","parmassondi");
		exactWords.put("LIFE","cuivië");
		exactWords.put("LIKEHOOD","óvëassë");
		exactWords.put("LIMIT","taica");
		exactWords.put("LIMITED","lanya");
		exactWords.put("LIP","pé");
		exactWords.put("LIPS","peu");
		exactWords.put("LOFTY","ancassëa");
		exactWords.put("LONG-LIVED","yeniquanta");
		exactWords.put("LOVING","méla");
		exactWords.put("MANTLE","fanta");
		exactWords.put("MARIA","maría");
		exactWords.put("MARK","talca");
		exactWords.put("MAYBE","cé");
		exactWords.put("MEAGRE","linya");
		exactWords.put("MEAN","selya");
		exactWords.put("MEET","ovanta");
		exactWords.put("MIGHTY","taura");
		exactWords.put("MIND","sáma");
		exactWords.put("MIDDLE","entya");
		exactWords.put("MOTHER","amillë");
		exactWords.put("MOUTH","náva");
		exactWords.put("NECESSITY","sangië");
		exactWords.put("NET","raima");
		exactWords.put("NETTED","raina");
		exactWords.put("NETWORK","raimë");
		exactWords.put("NEWS","sinyar");
		exactWords.put("NINTH","nertëa");
		exactWords.put("NUMERAL","notessë");
		exactWords.put("OBSTINATE","taryalanca");
		exactWords.put("OIL","LIB");
		exactWords.put("OMNIFICENT","ilucara");
		exactWords.put("OMNIPOTENT","ilúvala");
		exactWords.put("OMNISCIENT","iluisa");
		exactWords.put("ON","apa");
		exactWords.put("ONE","mo");
		exactWords.put("OPEN","láta");
		exactWords.put("OPENING","latya");
		exactWords.put("OPENNESS","látië");
		exactWords.put("OPPOSED","ara");
		exactWords.put("OPPOSITE","ara");
		exactWords.put("OPPRESSION","sangarë");
		exactWords.put("OR","var");
		exactWords.put("OVER","terwa");
		exactWords.put("PASTURE","narassë");
		exactWords.put("PATH","rata");
		exactWords.put("PATHWAY","ratta");
		exactWords.put("PATRONAGE","ortírië");
		exactWords.put("PEACE","sívë");
		exactWords.put("PERHAPS","quíta");
		exactWords.put("PERMISSION","lávë");
		exactWords.put("PETITION","arcandë");
		exactWords.put("PHONETIC","lambelë");
		exactWords.put("PHONETICS","lambelë");
		exactWords.put("PICK","lepta");
		exactWords.put("PINETREE","sondë");
		exactWords.put("PINETREES","sondi");
		exactWords.put("PLEASE","iquista");
		exactWords.put("POET","lairemo");
		exactWords.put("POST","talca");
		exactWords.put("POUT","penga");
		exactWords.put("PRAY","arca");
		exactWords.put("PRAYER","arcandë");
		exactWords.put("PRESS","nir");
		exactWords.put("PRESSURE","sahtië");
		exactWords.put("PRICK","ehta");
		exactWords.put("PRIVACY","aquapahtië");
		exactWords.put("PRIVATE","satya");
		exactWords.put("PROMINENT","eteminya");
		exactWords.put("PROTECTION","ortírië");
		exactWords.put("PURPOSE","selya");
		exactWords.put("PURSUE","sac");
		exactWords.put("QUADRANGLE","cantil");
		exactWords.put("QUADRANGLES","cantildi");
		exactWords.put("QUADRANGULAR","cantilya");
		exactWords.put("QUESTION","maquetta");
		exactWords.put("QUESTIONS","maquetta");
		exactWords.put("RANSOM","nanwenda");
		exactWords.put("RAVINE","rissë");
		exactWords.put("RAVISH","amapta");
		exactWords.put("READ","cenda");
		exactWords.put("REAP","cer");
		exactWords.put("REAPING","cermë");
		exactWords.put("REASON","tyarwë");
		exactWords.put("REDEEMER","runando");
		exactWords.put("RED-HAIRED","russa");
		exactWords.put("REGARDS","pá");
		exactWords.put("REGULATIONS","namnasta");
		exactWords.put("RELEASE","lerya");
		exactWords.put("REMOVAL","hailë");
		exactWords.put("RESEMBLANCE","óvëassë ");
		exactWords.put("RESOLVE","indo");
		exactWords.put("RIDE","norta");
		exactWords.put("RIDGEPOLE","orpano");
		exactWords.put("RISE","tyulya");
		exactWords.put("RIVER-BED ","ratta");
		exactWords.put("RIVET","tanca");
		exactWords.put("ROBBERY","maptalë");
		exactWords.put("ROBE","vaimata");
		exactWords.put("RUSHING","ascara");
		exactWords.put("SAD","lemba");
		exactWords.put("SADNESS","nimbë");
		exactWords.put("SAIL","ramya");
		exactWords.put("SAINT","aimo");
		exactWords.put("SAPLING","nessornë");
		exactWords.put("SCRATCH","rimpë");
		exactWords.put("SCRIBE","tecindo");
		exactWords.put("SEARCH","sac");
		exactWords.put("SEAT","hamba");
		exactWords.put("SECOND","attëa");
		exactWords.put("SEEKING","surië");
		exactWords.put("SEEM","séya");
		exactWords.put("SEND","menta");
		exactWords.put("SENTIMENT","tendilë");
		exactWords.put("SEPARATE","satya");
		exactWords.put("SERVE","hilya");
		exactWords.put("SEVENTH","otsëa");
		exactWords.put("SHARPEN","laiceta");
		exactWords.put("SHE","së");
		exactWords.put("SHUT","pahta");
		exactWords.put("SIN","naiquë");
		exactWords.put("SINGLE","erya");
		exactWords.put("SIXTH","enquëa");
		exactWords.put("SMILING","raina");
		exactWords.put("SNARE","remma");
		exactWords.put("SO","sië");
		exactWords.put("SOFT","mussë");
		exactWords.put("SOMEBODY","mo");
		exactWords.put("SOUP","salpa");
		exactWords.put("SPRING","celwë");
		exactWords.put("SPLINTER","sacillë");
		exactWords.put("SPY","ettirno");
		exactWords.put("STAB","ehta");
		exactWords.put("STAFF","vandil");
		exactWords.put("STALWART","talya");
		exactWords.put("STAND","tyulya");
		exactWords.put("STARTLE","capta");
		exactWords.put("STATE","indo");
		exactWords.put("STAY","norta");
		exactWords.put("STEADY","tulunca");
		exactWords.put("STIFFNESS","taryassë");
		exactWords.put("STRAY","mistana");
		exactWords.put("STREET","ratta");
		exactWords.put("STRENGTHEN","antorya");
		exactWords.put("STRENGTHENING","antoryamë");
		exactWords.put("STRIDE","telconta");
		exactWords.put("STRIPPED","racinë");
		exactWords.put("STUDY","cenya");
		exactWords.put("SUCCESSION","hilyalë");
		exactWords.put("SUIT","camta");
		exactWords.put("SUPERIOR","orohalla");
		exactWords.put("SURGE","solto");
		exactWords.put("SURVIVE","vor");
		exactWords.put("SWORD","paswa");
		exactWords.put("SYMPATHY","ófelmë");
		exactWords.put("TABLE","paluhta");
		exactWords.put("TALL","ancassëa");
		exactWords.put("TAMBOURINE","rambil");
		exactWords.put("TASK","tarassë");
		exactWords.put("TEACH","saita");
		exactWords.put("TEMPTATION","úsahtië");
		exactWords.put("TENTH","quainëa");
		exactWords.put("THAT","sa");
		exactWords.put("THICK","sangwa");
		exactWords.put("THIN","linya");
		exactWords.put("THING","engwë");
		exactWords.put("THINK","sana");
		exactWords.put("THIRD","neldëa");
		exactWords.put("THIRSTY","soica");
		exactWords.put("THITHER","tanna");
		exactWords.put("THOUGHT","sanwë");
		exactWords.put("THREAD","lanya");
		exactWords.put("THRUST","nir");
		exactWords.put("THUS","sië");
		exactWords.put("TIDE","sóla");
		exactWords.put("TIGHT","sangwa");
		exactWords.put("TODAY","sinaurë");
		exactWords.put("TOMB","sarca");
		exactWords.put("TOMORROW","entaurë");
		exactWords.put("TOUCH","appa");
		exactWords.put("TOUCHING","apa");
		exactWords.put("TOUGH","sangwa");
		exactWords.put("TOUGHNESS","taryassë");
		exactWords.put("TRACK","vata");
		exactWords.put("TRAMPLE","vatta");
		exactWords.put("TRAP","remba");
		exactWords.put("TRAVERSE","tervanta");
		exactWords.put("TRESPASS","naicë");
		exactWords.put("TRESSURE","carrëa");
		exactWords.put("TRINITY","neldië");
		exactWords.put("TROLL","torco");
		exactWords.put("TROUBLE","tarasta");
		exactWords.put("THROW","hat");
		exactWords.put("TURRET","mindë");
		exactWords.put("TWELFTH","yunquë");
		exactWords.put("UNCOUNTED","únótëa");
		exactWords.put("UNICORN","eretildo");
		exactWords.put("UNITE","erta");
		exactWords.put("UNTIL","mennai");
		exactWords.put("UNWILL","avanir");
		exactWords.put("UNWISE","alasaila");
		exactWords.put("URGE","or");
		exactWords.put("URGENCY","sangië");
		exactWords.put("USE","yuhta");
		exactWords.put("USEFULLNESS","yungwë");
		exactWords.put("USED","yunca");
		exactWords.put("VALOUR","astal");
		exactWords.put("VASSAL","neuro");
		exactWords.put("VAST","taura");
		exactWords.put("VEIL","vasar");
		exactWords.put("VENGEANCE","atacarmë");
		exactWords.put("VERDIGRIS","lairus");
		exactWords.put("VICTORY","nangwë");
		exactWords.put("VICTOR","nacil");
		exactWords.put("VIOLENT","naraca");
		exactWords.put("VOWEL","óma-tengwë");
		exactWords.put("WAIN","lunca");
		exactWords.put("WAIT","larta");
		exactWords.put("WAKE","eccuita");
		exactWords.put("WANDER","ramya");
		exactWords.put("WANDERER","ranyar");
		exactWords.put("WANDERING","ranya");
		exactWords.put("WARE","maca");
		exactWords.put("WARM","lauta");
		exactWords.put("WAS","engë");
		exactWords.put("WATERFALL","lantasírë");
		exactWords.put("WATCH","cenda");
		exactWords.put("WAVE","solmë");
		exactWords.put("WHENCE","yallo");
		exactWords.put("WHERE","massë");
		exactWords.put("WHEREBY","yanen");
		exactWords.put("WHERETO","yanna");
		exactWords.put("WHITHER","manna");
		exactWords.put("WHOSE","yava");
		exactWords.put("WHY","manan");
		exactWords.put("WILL","mendë");
		exactWords.put("WINDOW","henet");
		exactWords.put("WINDOWS","henetsi");
		exactWords.put("WINE","miruva");
		exactWords.put("WINY","míruva");
		exactWords.put("WING","ramna");
		exactWords.put("WISE","saila");
		exactWords.put("WITH","as");
		exactWords.put("WITHOUT","ú");
		exactWords.put("WOMB","móna");
		exactWords.put("WOOD","toa");
		exactWords.put("WRITER","tecindo");
		exactWords.put("WRITING","sarmë");
		exactWords.put("WRONG","raicë");
		exactWords.put("YES","yé");
		exactWords.put("YESTERDAY","tellaurë");
		return exactWords;
		}
	}
