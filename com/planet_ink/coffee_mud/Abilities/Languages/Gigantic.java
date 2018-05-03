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
   Copyright 2004-2018 Bo Zimmerman

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

	public static List<String[]> wordLists=null;
	public Gigantic()
	{
		super();
	}

	@Override
	public List<String[]> translationVector(String language)
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

	public String tup(String msg)
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

	private static final Hashtable<String,String> hashwords=new Hashtable<String,String>();

	@Override
	public Map<String, String> translationHash(String language)
	{
		if((hashwords!=null)&&(hashwords.size()>0))
			return hashwords;
		hashwords.put("0","nola");
		hashwords.put("1","jedan");
		hashwords.put("2","dva");
		hashwords.put("3","tre");
		hashwords.put("4","stare");
		hashwords.put("5","pet");
		hashwords.put("6","sest");
		hashwords.put("7","sedam");
		hashwords.put("8","osam");
		hashwords.put("9","devet");
		hashwords.put("10","deset");
		hashwords.put("100","sto");
		hashwords.put("1000","tesac");
		hashwords.put("1000000","meljen");
		hashwords.put("1000000000","meljard");
		hashwords.put("1000000000000","heljen");
		hashwords.put("1000000000000000","treljen");
		hashwords.put("AND","e");
		hashwords.put("BAD","spatno");
		hashwords.put("BADLY","spatnoje");
		hashwords.put("BE","ta jast");
		hashwords.put("BEAUTY","kresno");
		hashwords.put("BEAUTIFUL","kresnoje");
		hashwords.put("BEAUTIFULLY","nakresnoje");
		hashwords.put("BREAD","vodanet");
		hashwords.put("BUT","ola");
		hashwords.put("COME","kralestvo");
		hashwords.put("DAY","kezdanon");
		hashwords.put("DEBTS","vinarat");
		hashwords.put("DELIVER","zebav");
		hashwords.put("DONE","vola");
		hashwords.put("EARTH","nevar");
		hashwords.put("EIGHT","osmon");
		hashwords.put("EVER","vaker");
		hashwords.put("EVIL","zilonis");
		hashwords.put("FATHER","atece");
		hashwords.put("FINE","dobro");
		hashwords.put("FIVE","peton");
		hashwords.put("FOR","na");
		hashwords.put("FORGIVE","adpast");
		hashwords.put("FOUR","staron");
		hashwords.put("GIVE","helabet");
		hashwords.put("GLORY","slavat");
		hashwords.put("GOOD","dobroje");
		hashwords.put("HALLOWED","fasveston");
		hashwords.put("HEAVEN","nevaror");
		hashwords.put("HIGH","vesako");
		hashwords.put("HIGHEST","navesakoje");
		hashwords.put("HIGHLY","vesakoje");
		hashwords.put("HUNDRED","ston");
		hashwords.put("IN","na");
		hashwords.put("INTO","vo");
		hashwords.put("IS","jesi");
		hashwords.put("KINGDOM","prijoda");
		hashwords.put("LEAD","neprived");
		hashwords.put("MILLION","meljanon");
		hashwords.put("BILLION","meljardon");
		hashwords.put("NAME","namet");
		hashwords.put("NEW","navo");
		hashwords.put("NEWEST","nanavoje");
		hashwords.put("NEWLY","navoje");
		hashwords.put("NICE","hezako");
		hashwords.put("NICELY","hezakoje");
		hashwords.put("NICEST","nahezakoje");
		hashwords.put("NINE","devton");
		hashwords.put("NOT","nas");
		hashwords.put("OK","dobro");
		hashwords.put("ONE","nolten");
		hashwords.put("OUR","nar");
		hashwords.put("POWER","mocet");
		hashwords.put("QUADRILLION","treljanon");
		hashwords.put("SEVEN","sedmon");
		hashwords.put("SIX","seston");
		hashwords.put("TEMPTATION","farsykonot");
		hashwords.put("TEN","deston");
		hashwords.put("THE","ta");
		hashwords.put("THIS","daj");
		hashwords.put("THOUSAND","tesacon");
		hashwords.put("THREE","treton");
		hashwords.put("TRILLION","heljanon");
		hashwords.put("TWO","parvon");
		hashwords.put("US","nar");
		hashwords.put("WELL","nadobroje");
		hashwords.put("WHO","ketri");
		hashwords.put("WILL","so-stada");
		hashwords.put("WORSE","naspatnoje");
		hashwords.put("YOUR","ar");
		hashwords.put("YOURS","tar");
		return hashwords;
	}
}
