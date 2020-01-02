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
   Copyright 2002-2020 Bo Zimmerman

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

	private static final Hashtable<String,String> hashWords=new Hashtable<String,String>();

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
		if((hashWords!=null)&&(hashWords.size()>0))
			return hashWords;
		// *** Capitalized keys are below.
		final Map<String,String> hashwords = new TreeMap<String,String>();
		hashwords.put("are","be");
		hashwords.put("is","be");
		hashwords.put("am","be");
		hashwords.put("I'm","I be");
		hashwords.put("Im","I be");
		hashwords.put("you've","ye be");
		hashwords.put("youve","ye be");
		hashwords.put("we've","we be");
		hashwords.put("weve","we be");
		hashwords.put("you","ye");
		hashwords.put("your","yer");
		hashwords.put("my","me");
		hashwords.put("smart","witty");
		hashwords.put("steal","purloin");
		hashwords.put("take","plunder");
		hashwords.put("mercy","quarter");
		hashwords.put("give","giv'");
		hashwords.put("heaven","heav'n");
		hashwords.put("eating","eat'n");
		hashwords.put("chat","natter");
		hashwords.put("chatting","nattering");
		hashwords.put("stab","skewer");
		hashwords.put("hello","ahoy");
		hashwords.put("wow","avast");
		hashwords.put("neat","smart");
		hashwords.put("yes","aye");
		hashwords.put("coin","doubloon");
		hashwords.put("umm","arr");
		hashwords.put("ummm","arrr");
		hashwords.put("ummmm","arrrr");
		hashwords.put("uhm","arh");
		hashwords.put("uhmm","argh");
		hashwords.put("uhh","arr");
		hashwords.put("uhhh ","arrr");
		hashwords.put("cheat","hornswaggle");
		hashwords.put("cheating","hornswaggling");
		hashwords.put("rob","pillage");
		hashwords.put("citizen","landlubber");
		hashwords.put("idiot","landlubber");
		hashwords.put("ship","manowar");
		hashwords.put("perfect","shipshape");
		hashwords.put("shopkeeper","chandler");
		hashwords.put("shopkeep","chandler");
		hashwords.put("friend","hearty");
		hashwords.put("friends","hearties");
		hashwords.put("girl","lass");
		hashwords.put("girls","lassies");
		hashwords.put("boy","lad");
		hashwords.put("boys","laddies");
		hashwords.put("lady","wench");
		hashwords.put("woman","wench");
		hashwords.put("queen","grand strumpet");
		hashwords.put("stealing","thievin'");
		hashwords.put("needing","needin'");
		hashwords.put("taking","haulin'");
		hashwords.put("lying","lyin'");
		hashwords.put("eating","eat'n");
		hashwords.put("captain","cap'n");
		hashwords.put("reading","readin'");
		hashwords.put("writing","writin'");
		hashwords.put("rotting","festerin'");
		hashwords.put("stopping","stoppin'");
		hashwords.put("swimming","swimmin'");
		hashwords.put("with","wit'");
		hashwords.put("because","coz");
		hashwords.put("cuz","coz");
		hashwords.put("cousin","son of a biscuit eater");
		hashwords.put("quickly","smartly");
		hashwords.put("bastard","knave");
		hashwords.put("villain","scallywag");
		hashwords.put("toilet","head");
		hashwords.put("potty","head");
		hashwords.put("bathroom","jardin");
		hashwords.put("restroom","jardin");
		hashwords.put("them","'em");
		hashwords.put("him","'im");
		hashwords.put("her","'er");
		hashwords.put("there","thar");
		hashwords.put("criminal","scalawag");
		hashwords.put("thief","scallywag");
		hashwords.put("villain ","scalallalloololowag");
		hashwords.put("stomach","gizzard");
		hashwords.put("dumb","daft");
		hashwords.put("stupid","daft");
		hashwords.put("almost","nigh-on");
		hashwords.put("over ","o'er");
		hashwords.put("before","afore");
		hashwords.put("little","wee");
		hashwords.put("small","wee");
		hashwords.put("tiny","wee");
		hashwords.put("wee","wee");
		hashwords.put("myself","meself");
		hashwords.put("expect","'spect");
		hashwords.put("punish","keelhaul");
		hashwords.put("punishment","keelhauling");
		hashwords.put("drunk","three sheets to the wind");
		hashwords.put("ouch","shiver me timbers");
		hashwords.put("ow","Blow me down!");
		hashwords.put("oof","blimey!");
		hashwords.put("noose","hempen halter");
		hashwords.put("chest","coffer");
		hashwords.put("peaceful","becalmed");
		hashwords.put("recruit","crimp");
		hashwords.put("hell","Davy Jones' locker");
		hashwords.put("eyes","deadlights");
		hashwords.put("lean","list");
		hashwords.put("wake","show a leg");
		hashwords.put("damn","sink me!");
		hashwords.put("nap","caulk");
		hashwords.put("sleep","caulk");
		hashwords.put("coffin","dead men's chest");
		hashwords.put("food","grub");
		hashwords.put("coward","lily-liver");
		hashwords.put("cowardly","lily-livered");
		hashwords.put("rebellion","mutiny");
		hashwords.put("no","nay");
		hashwords.put("reward","bounty");
		hashwords.put("song","chantey");
		//hashwords.put("feet","fathoms");
		hashwords.put("stop","heave to");
		hashwords.put("understand","savvy");
		hashwords.put("telescope","spyglass");
		hashwords.put("binoculars","spyglasses");
		hashwords.put("tipsy","squiffy");
		hashwords.put("surrender","strike colors");
		hashwords.put("mop","swab");
		hashwords.put("ignore","belay");
		hashwords.put("tie","belay");
		hashwords.put("butt","dungbie");
		hashwords.put("ass","dungbie");
		//hashwords.put("become a pirate	go on account
		hashwords.put("backpack","duffle");
		hashwords.put("nerd","drivelswigger");
		hashwords.put("rascal","picaroon");
		hashwords.put("cask","hogshead");
		hashwords.put("afraid","afeard");
		hashwords.put("insane","addled");
		hashwords.put("eggs","cackle fruit");
		hashwords.put("ghost","duffy");
		hashwords.put("revenant","dredgie");
		hashwords.put("hey","ho");
		hashwords.put("excution","Jack Ketch");
		hashwords.put("executed","Jack Ketch-ed");
		hashwords.put("executing","Jack Ketch-ing");
		hashwords.put("child","nipper");
		hashwords.put("move", "step to");
		hashwords.put("nice","Aaaaaaaaaaaarh");
		hashwords.put("impressive","begad");
		hashwords.put("heaven","Fiddler's Green");
		hashwords.put("up","aloft");
		hashwords.put("above","aloft");
		hashwords.put("Ha","Harr");
		hashwords.put("Haha","har-har");
		hashwords.put("Sailor","Jack Tar");
		hashwords.put("attention","a weather eye open");
		hashwords.put("unprepared","under bare poles");
		hashwords.put("gossip","scuttlebutt");
		hashwords.put("coat","reefer");
		hashwords.put("lie","spin yarn");
		hashwords.put("overwhelmed","awash");
		hashwords.put("progress","headway");
		hashwords.put("assignment","berth");
		hashwords.put("lodging","quarters");
		hashwords.put("home","quarters");
		hashwords.put("property","quarters");
		hashwords.put("put","stow");
		hashwords.put("swamped","awash");
		hashwords.put("aftermath","wake");
		hashwords.put("lost","adrift");
		hashwords.put("everyone","all hands");
		hashwords.put("everybody","all hands");
		hashwords.put("weapons","armamament");
		hashwords.put("pull","bowse");
		hashwords.put("demote","disrate");
		hashwords.put("full speed","flank");
		hashwords.put("beat","flog");
		hashwords.put("bottom","foot");
		hashwords.put("kitchen","galley");
		hashwords.put("Steamship","Hand Bomber");
		hashwords.put("Steamboat","Hand Bomber");
		hashwords.put("soap","holystone");
		hashwords.put("police","jollies");
		hashwords.put("cityguard","bluejack");
		hashwords.put("constable","jollies");
		hashwords.put("cop","bluejack");
		hashwords.put("stair","ladder");
		hashwords.put("dining","mess");
		hashwords.put("kitchen","mess");
		hashwords.put("navigator","pilot");
		hashwords.put("group","crew");
		hashwords.put("wind","windage");
		hashwords.put("land","ashore");
		hashwords.put("cane","stonnacky");
		hashwords.put("whip","cat");
		hashwords.put("I","oi");
		hashwords.put("want","wants");
		hashwords.put("ya'all","you alls");
		hashwords.put("yall","you alls");
		//hashwords.put("cat o' nine tails	captain's daughter
		//hashwords.put("cat of nine tails	captain's duaghter
		hashwords.put("go","lay");
		hashwords.put("sit","lie");
		hashwords.put("crowded","no room to swing a cat");
		hashwords.put("quiet","pipe down");
		hashwords.put("manipulate","run a rig");
		hashwords.put("manipulating","running a rig");
		hashwords.put("manipulation","rig running");
		hashwords.put("go downwind","haul wind");
		for(final Iterator<String> i=hashwords.keySet().iterator();i.hasNext();)
		{
			final String key = i.next();
			final String value = hashwords.get(key);
			hashWords.put(key.toUpperCase().trim(), value.toLowerCase());
		}
		return hashWords;
	}
}
