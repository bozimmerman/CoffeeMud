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
   Copyright 2002-2023 Bo Zimmerman

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
public class PirateTalk extends StdLanguage
{
	@Override
	public String ID()
	{
		return "PirateTalk";
	}

	private final static String localizedName = CMLib.lang().L("Pirate");

	@Override
	public String name()
	{
		return localizedName;
	}

	public static List<String[]> wordLists=null;
	public PirateTalk()
	{
		super();
	}

	final private static Set<String> supp = new XHashSet<String>(new String[]{"PirateTalk", "", "Common"});

	@Override
	public Set<String> languagesSupported()
	{
		return supp;
	}

	private static final Map<String,String> exactWords=new TreeMap<String,String>();

	@Override
	public String translate(final String language, final String word)
	{
		final String res = super.translate(language, word);
		if(res.endsWith("ING") && (!res.equalsIgnoreCase("sing")))
			return res.substring(0,res.length()-3)+"IN'";
		if(res.endsWith("ing") && (!res.equalsIgnoreCase("sing")))
			return res.substring(0,res.length()-3)+"in'";
		return res;
	}

	@Override
	public Map<String, String> translationHash(final String language)
	{
		if((exactWords!=null)&&(exactWords.size()>0))
			return exactWords;
		// *** Capitalized keys are below.
		final Map<String,String> subExactWords = new TreeMap<String,String>();
		subExactWords.put("are","be");
		subExactWords.put("is","be");
		subExactWords.put("am","be");
		subExactWords.put("I'm","I be");
		subExactWords.put("Im","I be");
		subExactWords.put("you've","ye be");
		subExactWords.put("youve","ye be");
		subExactWords.put("we've","we be");
		subExactWords.put("weve","we be");
		subExactWords.put("you","ye");
		subExactWords.put("your","yer");
		subExactWords.put("my","me");
		subExactWords.put("smart","witty");
		subExactWords.put("steal","purloin");
		subExactWords.put("take","plunder");
		subExactWords.put("mercy","quarter");
		subExactWords.put("give","giv'");
		subExactWords.put("heaven","heav'n");
		subExactWords.put("eating","eat'n");
		subExactWords.put("chat","natter");
		subExactWords.put("chatting","nattering");
		subExactWords.put("stab","skewer");
		subExactWords.put("hello","ahoy");
		subExactWords.put("wow","avast");
		subExactWords.put("neat","smart");
		subExactWords.put("yes","aye");
		subExactWords.put("coin","doubloon");
		subExactWords.put("umm","arr");
		subExactWords.put("ummm","arrr");
		subExactWords.put("ummmm","arrrr");
		subExactWords.put("uhm","arh");
		subExactWords.put("uhmm","argh");
		subExactWords.put("uhh","arr");
		subExactWords.put("uhhh ","arrr");
		subExactWords.put("cheat","hornswaggle");
		subExactWords.put("cheating","hornswaggling");
		subExactWords.put("rob","pillage");
		subExactWords.put("citizen","landlubber");
		subExactWords.put("idiot","landlubber");
		subExactWords.put("ship","manowar");
		subExactWords.put("perfect","shipshape");
		subExactWords.put("shopkeeper","chandler");
		subExactWords.put("shopkeep","chandler");
		subExactWords.put("friend","hearty");
		subExactWords.put("friends","hearties");
		subExactWords.put("girl","lass");
		subExactWords.put("girls","lassies");
		subExactWords.put("boy","lad");
		subExactWords.put("boys","laddies");
		subExactWords.put("lady","wench");
		subExactWords.put("woman","wench");
		subExactWords.put("queen","grand strumpet");
		subExactWords.put("stealing","thievin'");
		subExactWords.put("needing","needin'");
		subExactWords.put("taking","haulin'");
		subExactWords.put("lying","lyin'");
		subExactWords.put("eating","eat'n");
		subExactWords.put("captain","cap'n");
		subExactWords.put("reading","readin'");
		subExactWords.put("writing","writin'");
		subExactWords.put("rotting","festerin'");
		subExactWords.put("stopping","stoppin'");
		subExactWords.put("swimming","swimmin'");
		subExactWords.put("with","wit'");
		subExactWords.put("because","coz");
		subExactWords.put("cuz","coz");
		subExactWords.put("cousin","son of a biscuit eater");
		subExactWords.put("quickly","smartly");
		subExactWords.put("bastard","knave");
		subExactWords.put("villain","scallywag");
		subExactWords.put("toilet","head");
		subExactWords.put("potty","head");
		subExactWords.put("bathroom","jardin");
		subExactWords.put("restroom","jardin");
		subExactWords.put("them","'em");
		subExactWords.put("him","'im");
		subExactWords.put("her","'er");
		subExactWords.put("there","thar");
		subExactWords.put("criminal","scalawag");
		subExactWords.put("thief","scallywag");
		subExactWords.put("villain ","scalallalloololowag");
		subExactWords.put("stomach","gizzard");
		subExactWords.put("dumb","daft");
		subExactWords.put("stupid","daft");
		subExactWords.put("almost","nigh-on");
		subExactWords.put("over ","o'er");
		subExactWords.put("before","afore");
		subExactWords.put("little","wee");
		subExactWords.put("small","wee");
		subExactWords.put("tiny","wee");
		subExactWords.put("wee","wee");
		subExactWords.put("myself","meself");
		subExactWords.put("expect","'spect");
		subExactWords.put("punish","keelhaul");
		subExactWords.put("punishment","keelhauling");
		subExactWords.put("drunk","three sheets to the wind");
		subExactWords.put("ouch","shiver me timbers");
		subExactWords.put("ow","Blow me down!");
		subExactWords.put("oof","blimey!");
		subExactWords.put("noose","hempen halter");
		subExactWords.put("chest","coffer");
		subExactWords.put("peaceful","becalmed");
		subExactWords.put("recruit","crimp");
		subExactWords.put("hell","Davy Jones' locker");
		subExactWords.put("eyes","deadlights");
		subExactWords.put("lean","list");
		subExactWords.put("wake","show a leg");
		subExactWords.put("damn","sink me!");
		subExactWords.put("nap","caulk");
		subExactWords.put("sleep","caulk");
		subExactWords.put("coffin","dead men's chest");
		subExactWords.put("food","grub");
		subExactWords.put("coward","lily-liver");
		subExactWords.put("cowardly","lily-livered");
		subExactWords.put("rebellion","mutiny");
		subExactWords.put("no","nay");
		subExactWords.put("reward","bounty");
		subExactWords.put("song","chantey");
		//exactWords.put("feet","fathoms");
		subExactWords.put("stop","heave to");
		subExactWords.put("understand","savvy");
		subExactWords.put("telescope","spyglass");
		subExactWords.put("binoculars","spyglasses");
		subExactWords.put("tipsy","squiffy");
		subExactWords.put("surrender","strike colors");
		subExactWords.put("mop","swab");
		subExactWords.put("ignore","belay");
		subExactWords.put("tie","belay");
		subExactWords.put("butt","dungbie");
		subExactWords.put("ass","dungbie");
		//exactWords.put("become a pirate	go on account
		subExactWords.put("backpack","duffle");
		subExactWords.put("nerd","drivelswigger");
		subExactWords.put("rascal","picaroon");
		subExactWords.put("cask","hogshead");
		subExactWords.put("afraid","afeard");
		subExactWords.put("insane","addled");
		subExactWords.put("eggs","cackle fruit");
		subExactWords.put("ghost","duffy");
		subExactWords.put("revenant","dredgie");
		subExactWords.put("hey","ho");
		subExactWords.put("excution","Jack Ketch");
		subExactWords.put("executed","Jack Ketch-ed");
		subExactWords.put("executing","Jack Ketch-ing");
		subExactWords.put("child","nipper");
		subExactWords.put("move", "step to");
		subExactWords.put("nice","Aaaaaaaaaaaarh");
		subExactWords.put("impressive","begad");
		subExactWords.put("heaven","Fiddler's Green");
		subExactWords.put("up","aloft");
		subExactWords.put("above","aloft");
		subExactWords.put("Ha","Harr");
		subExactWords.put("Haha","har-har");
		subExactWords.put("Sailor","Jack Tar");
		subExactWords.put("attention","a weather eye open");
		subExactWords.put("unprepared","under bare poles");
		subExactWords.put("gossip","scuttlebutt");
		subExactWords.put("coat","reefer");
		subExactWords.put("lie","spin yarn");
		subExactWords.put("overwhelmed","awash");
		subExactWords.put("progress","headway");
		subExactWords.put("assignment","berth");
		subExactWords.put("lodging","quarters");
		subExactWords.put("home","quarters");
		subExactWords.put("property","quarters");
		subExactWords.put("put","stow");
		subExactWords.put("swamped","awash");
		subExactWords.put("aftermath","wake");
		subExactWords.put("lost","adrift");
		subExactWords.put("everyone","all hands");
		subExactWords.put("everybody","all hands");
		subExactWords.put("weapons","armamament");
		subExactWords.put("pull","bowse");
		subExactWords.put("demote","disrate");
		subExactWords.put("full speed","flank");
		subExactWords.put("beat","flog");
		subExactWords.put("bottom","foot");
		subExactWords.put("kitchen","galley");
		subExactWords.put("Steamship","Hand Bomber");
		subExactWords.put("Steamboat","Hand Bomber");
		subExactWords.put("soap","holystone");
		subExactWords.put("police","jollies");
		subExactWords.put("cityguard","bluejack");
		subExactWords.put("constable","jollies");
		subExactWords.put("cop","bluejack");
		subExactWords.put("stair","ladder");
		subExactWords.put("dining","mess");
		subExactWords.put("kitchen","mess");
		subExactWords.put("navigator","pilot");
		subExactWords.put("group","crew");
		subExactWords.put("wind","windage");
		subExactWords.put("land","ashore");
		subExactWords.put("cane","stonnacky");
		subExactWords.put("whip","cat");
		subExactWords.put("I","oi");
		subExactWords.put("want","wants");
		subExactWords.put("ya'all","you alls");
		subExactWords.put("yall","you alls");
		//exactWords.put("cat o' nine tails	captain's daughter
		//exactWords.put("cat of nine tails	captain's duaghter
		subExactWords.put("go","lay");
		subExactWords.put("sit","lie");
		subExactWords.put("crowded","no room to swing a cat");
		subExactWords.put("quiet","pipe down");
		subExactWords.put("manipulate","run a rig");
		subExactWords.put("manipulating","running a rig");
		subExactWords.put("manipulation","rig running");
		subExactWords.put("go downwind","haul wind");
		subExactWords.put("castle","ksul");
		subExactWords.put("fore","fo`");
		subExactWords.put("forecastle","fo`ksul");
		subExactWords.put("haul","hal");
		for(final Iterator<String> i=subExactWords.keySet().iterator();i.hasNext();)
		{
			final String key = i.next();
			final String value = subExactWords.get(key);
			exactWords.put(key.toUpperCase().trim(), value.toLowerCase());
		}
		return exactWords;
	}
}
