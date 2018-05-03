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
   Copyright 2014-2018 Bo Zimmerman

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

public class AnimalSpeak extends StdLanguage
{
	@Override
	public String ID()
	{
		return "AnimalSpeak";
	}

	private final static String	localizedName	= CMLib.lang().L("Animal Speak");

	@Override
	public String name()
	{
		return localizedName;
	}

	public static List<String[]>	wordLists	= null;
	private static boolean			mapped		= false;
	
	public AnimalSpeak()
	{
		super();
		if(!mapped){mapped=true;
					CMLib.ableMapper().addCharAbilityMapping("Archon",1,ID(),false);}
	}
	
	private final static String[] animalSounds={
		"hiss","grunt","roar","quak","snort","growl","screech","growl","groan","moan","roar","hum","buzz","drone","click","chirrup","chirp",
		"twitter","tweet","sing","whistle","boom","whistle","chirp","squeal","screech","shriek","squeak","hoot","bleat","grunt","chirp",
		"chatter","trill","mew","purr","meow","miaow","hiss","yowl","screech","caterwaul","ow","cluck","cackle","bock","chirp","crow",
		"screech","peep","cockadoodledoo","pant-hoot","grunt","scream","chatter","screech","bark","squeak","chirp","moo","low","bawl",
		"bellow","bark","yelp","cry","snarl","howl","chirp","creak","caw","cah","coo","cuckoo","pipe[10]","bell","bark","cry","bark",
		"woof","arf","bay","bow-wow","howl","yap","click","bray","hee-haw","coo-coo","quack","scream","trumpet","roar","moan","rumble",
		"drum","chant","dook","buzz","hum","bark","yelp","simper","croak","ribbit","gribbit","cackle","gobble","hiss","honk","quack",
		"whoop","chirp","screech","wail","bleat","bleat","baa","hoot","bark","grunt","whine","pock","pant","chirp","squeak","squeak",
		"squeak","cackle","cluck","chirp","bellow","rumble","roar","growl","grunt","snort","neigh","snort","whinny","nicker","sputter",
		"whisper","hum","whistle","cry","scream","sing","talk","moan","laugh","sputter","coo","hum","twitter",
		"laugh","scream","whoop","gecker","howl","chatter","screech","chortle","scream","bellow","wail","growl","snarl","hiss","bleat",
		"baa","sing","warble","chuckle","roar","growl","maw","chatter","squeak","squeal","chatter","gecker","gibber","whoop","screech",
		"bellow","whine","squeal","pipe","sing","warble","cough","bellow","groan","grunt","smooch","wheeze","chirp","squeal","sputter",
		"chirp","bark","hiss","hummmmm","hoot","scream","screech","shriek","bellow","low","screech","squawk",
		"scream","scream","snort","grunt","squeal","oink","coo","whistle","click","bark","chirp","chatter","squeak","drum","growl",
		"trill","squeak","eek","brux","croak","bellow","chirp","caw","crow","scream","squawk","mew","bark","bleat","baa","hiss","chirp",
		"twitter","squeak","chatter","click","bellow","twitter","squeal","cry","whistle","squeak","growl","roar","snarl","whistle","sing",
		"croak","gobble","chirp","chatter","grunt","bark","coo","sputter","scream","groan","sing","bark","howl","cry","yell","yelp","trill",
		"warble","moan","whinny","whoop"
	};
	
	protected String[] getSounds() 
	{
		return animalSounds;
	}

	@Override
	public List<String[]> translationVector(String language)
	{
		return wordLists;
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats stats)
	{
		super.affectPhyStats(affected, stats);
		if(this.beingSpoken(ID()))
			stats.setSensesMask(stats.sensesMask()|PhyStats.CAN_GRUNT_WHEN_STUPID);
	}

	@Override
	public String translate(String language, String word)
	{
		final String newWord=getSounds()[CMLib.dice().roll(1, getSounds().length, -1)];
		switch(CMLib.dice().roll(1, 5, -1))
		{
		case 0:
			return newWord.toLowerCase();
		case 1:
			return newWord.toLowerCase();
		case 2:
			return CMStrings.capitalizeAndLower(newWord);
		case 3:
			return newWord.toUpperCase();
		case 5:
			return newWord.toUpperCase() + "!";
		default:
			return newWord;
		}
	}
}
