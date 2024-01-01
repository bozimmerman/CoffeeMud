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
   Copyright 2004-2024 Bo Zimmerman

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
public class Gigantic extends StdLanguage
{
	@Override
	public String ID()
	{
		return "Gigantic";
	}

	private final static String localizedName = CMLib.lang().L("Gigantic");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String getTranslationVerb()
	{
		return "BOOM(S)";
	}

	public static List<String[]> wordLists=null;
	public Gigantic()
	{
		super();
	}

	@Override
	public List<String[]> translationLists(final String language)
	{
		if(wordLists==null)
		{
			final String[] one={"o","est","e","am"};
			final String[] two={"on","dva","sa","is","id","et","bo","ja","te","me","za","ve"};
			final String[] three={"pet","set","tre","mal","maz","mat","ane","dom"};
			final String[] four={"nast","sest","osam","bedu","beda","mene","mame","maja","beli","nesi"};
			final String[] five={"sedam","devat","flanon","dvade","matke","trede","horat","jesam","taram","anaht","maram","nezme"};
			final String[] six={"jedanast","delalime","veralim","dvanast","bahone","zahedon","prasad","trenast","staronast","starde","delaja"};
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

	public String tup(final String msg)
	{
		if(msg==null)
			return msg;
		return msg.toUpperCase();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((beingSpoken(ID()))
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMessage()!=null)
		&&(msg.tool()==null)
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))))
		{
			msg.modify(msg.source(),msg.target(),msg.tool(),
					   msg.sourceCode(),tup(msg.sourceMessage()),
					   msg.targetCode(),tup(msg.targetMessage()),
					   msg.othersCode(),tup(msg.othersMessage()));
		}
		return super.okMessage(myHost,msg);
	}

	private static final Map<String,String> exactWords=new TreeMap<String,String>();

	@Override
	public Map<String, String> translationHash(final String language)
	{
		if((exactWords!=null)&&(exactWords.size()>0))
			return exactWords;
		exactWords.put("0","nola");
		exactWords.put("1","jedan");
		exactWords.put("2","dva");
		exactWords.put("3","tre");
		exactWords.put("4","stare");
		exactWords.put("5","pet");
		exactWords.put("6","sest");
		exactWords.put("7","sedam");
		exactWords.put("8","osam");
		exactWords.put("9","devet");
		exactWords.put("10","deset");
		exactWords.put("100","sto");
		exactWords.put("1000","tesac");
		exactWords.put("1000000","meljen");
		exactWords.put("1000000000","meljard");
		exactWords.put("1000000000000","heljen");
		exactWords.put("1000000000000000","treljen");
		exactWords.put("AND","e");
		exactWords.put("BAD","spatno");
		exactWords.put("BADLY","spatnoje");
		exactWords.put("BE","ta jast");
		exactWords.put("BEAUTY","kresno");
		exactWords.put("BEAUTIFUL","kresnoje");
		exactWords.put("BEAUTIFULLY","nakresnoje");
		exactWords.put("BREAD","vodanet");
		exactWords.put("BUT","ola");
		exactWords.put("COME","kralestvo");
		exactWords.put("DAY","kezdanon");
		exactWords.put("DEBTS","vinarat");
		exactWords.put("DELIVER","zebav");
		exactWords.put("DONE","vola");
		exactWords.put("EARTH","nevar");
		exactWords.put("EIGHT","osmon");
		exactWords.put("EVER","vaker");
		exactWords.put("EVIL","zilonis");
		exactWords.put("FATHER","atece");
		exactWords.put("FINE","dobro");
		exactWords.put("FIVE","peton");
		exactWords.put("FOR","na");
		exactWords.put("FORGIVE","adpast");
		exactWords.put("FOUR","staron");
		exactWords.put("GIVE","helabet");
		exactWords.put("GLORY","slavat");
		exactWords.put("GOOD","dobroje");
		exactWords.put("HALLOWED","fasveston");
		exactWords.put("HEAVEN","nevaror");
		exactWords.put("HIGH","vesako");
		exactWords.put("HIGHEST","navesakoje");
		exactWords.put("HIGHLY","vesakoje");
		exactWords.put("HUNDRED","ston");
		exactWords.put("IN","na");
		exactWords.put("INTO","vo");
		exactWords.put("IS","jesi");
		exactWords.put("KINGDOM","prijoda");
		exactWords.put("LEAD","neprived");
		exactWords.put("MILLION","meljanon");
		exactWords.put("BILLION","meljardon");
		exactWords.put("NAME","namet");
		exactWords.put("NEW","navo");
		exactWords.put("NEWEST","nanavoje");
		exactWords.put("NEWLY","navoje");
		exactWords.put("NICE","hezako");
		exactWords.put("NICELY","hezakoje");
		exactWords.put("NICEST","nahezakoje");
		exactWords.put("NINE","devton");
		exactWords.put("NOT","nas");
		exactWords.put("OK","dobro");
		exactWords.put("ONE","nolten");
		exactWords.put("OUR","nar");
		exactWords.put("POWER","mocet");
		exactWords.put("QUADRILLION","treljanon");
		exactWords.put("SEVEN","sedmon");
		exactWords.put("SIX","seston");
		exactWords.put("TEMPTATION","farsykonot");
		exactWords.put("TEN","deston");
		exactWords.put("THE","ta");
		exactWords.put("THIS","daj");
		exactWords.put("THOUSAND","tesacon");
		exactWords.put("THREE","treton");
		exactWords.put("TRILLION","heljanon");
		exactWords.put("TWO","parvon");
		exactWords.put("US","nar");
		exactWords.put("WELL","nadobroje");
		exactWords.put("WHO","ketri");
		exactWords.put("WILL","so-stada");
		exactWords.put("WORSE","naspatnoje");
		exactWords.put("YOUR","ar");
		exactWords.put("YOURS","tar");
		return exactWords;
	}
}
