package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Import extends StdCommand
{
	public Import(){}

	private String[] access={"IMPORT"};
	public String[] getAccessWords(){return access;}

	public static final String[][] objDescs={
		{"silver",""+EnvResource.RESOURCE_STEEL},
		{"gold",""+EnvResource.RESOURCE_GOLD},
		{"iron",""+EnvResource.RESOURCE_IRON},
		{"dragonscales",""+EnvResource.RESOURCE_SCALES},
		{"dragonscale",""+EnvResource.RESOURCE_SCALES},
		{"mithril",""+EnvResource.RESOURCE_MITHRIL},
		{"animal fur",""+EnvResource.RESOURCE_FUR},
		{"stone",""+EnvResource.RESOURCE_STONE},
		{"platinum",""+EnvResource.RESOURCE_PLATINUM},
		{"soft leather",""+EnvResource.RESOURCE_LEATHER},
		{"plant",""+EnvResource.RESOURCE_FLOWERS},
		{"kid leather",""+EnvResource.RESOURCE_HIDE},
		{"shell",""+EnvResource.RESOURCE_EGGS},
		{"tin",""+EnvResource.RESOURCE_TIN},
		{"bone",""+EnvResource.RESOURCE_BONE},
		{"plastic",""+EnvResource.RESOURCE_PLASTIC},
		{"kevlar",""+EnvResource.RESOURCE_PLASTIC},
		{"Dew",""+EnvResource.RESOURCE_FRESHWATER},
		{"dew",""+EnvResource.RESOURCE_FRESHWATER},
		{"adamantite",""+EnvResource.RESOURCE_ADAMANTITE},
		{"admantite",""+EnvResource.RESOURCE_ADAMANTITE},
		{"marble",""+EnvResource.RESOURCE_MARBLE},
		{"nothingness",""+EnvResource.RESOURCE_NOTHING},
		{"hard leather",""+EnvResource.RESOURCE_LEATHER},
		{"feathers",""+EnvResource.RESOURCE_FEATHERS},
		{"ash",""+EnvResource.RESOURCE_DUST},
		{"snakeskin",""+EnvResource.RESOURCE_SCALES},
		{"fire",""+EnvResource.RESOURCE_ENERGY},
		{"gut",""+EnvResource.RESOURCE_MEAT},
		{"food",""+EnvResource.RESOURCE_MEAT},
		{"unknown",""+EnvResource.RESOURCE_LEATHER},
		{"onyx",""+EnvResource.RESOURCE_STONE},
		{"A oldstyle",""+EnvResource.RESOURCE_WOOD},
		{"a oldstyle",""+EnvResource.RESOURCE_WOOD},
		{"Other",""+EnvResource.RESOURCE_WOOD},
		{"other",""+EnvResource.RESOURCE_WOOD},
		{"etherealness",""+EnvResource.RESOURCE_NOTHING},
		{"target",""+EnvResource.RESOURCE_LEATHER},
		{"pill",""+EnvResource.RESOURCE_CORN},
		{"potion",""+EnvResource.RESOURCE_GLASS},
		{"rubies",""+EnvResource.RESOURCE_GEM},
		{"magic",""+EnvResource.RESOURCE_ENERGY},
		{"alcohol",""+EnvResource.RESOURCE_GLASS},
		{"beer",""+EnvResource.RESOURCE_GLASS},
		{"sword",""+EnvResource.RESOURCE_IRON},
		{"glas",""+EnvResource.RESOURCE_GLASS},
		{"bamboo",""+EnvResource.RESOURCE_WOOD},
		{"linen",""+EnvResource.RESOURCE_COTTON},
		{"book",""+EnvResource.RESOURCE_PAPER},
		{"shadow",""+EnvResource.RESOURCE_NOTHING},
		{"light",""+EnvResource.RESOURCE_ENERGY},
		{"corundum",""+EnvResource.RESOURCE_STONE},
		{"skin",""+EnvResource.RESOURCE_HIDE},
		{"air",""+EnvResource.RESOURCE_NOTHING},
		{"hair",""+EnvResource.RESOURCE_FUR},
		{"parchment",""+EnvResource.RESOURCE_PAPER},
		{"grain",""+EnvResource.RESOURCE_WHEAT},
		{"steele",""+EnvResource.RESOURCE_STEEL},
		{"dragonskin",""+EnvResource.RESOURCE_SCALES},
		{"granite",""+EnvResource.RESOURCE_GRANITE},
		{"pottery",""+EnvResource.RESOURCE_CLAY},
		{"pottery",""+EnvResource.RESOURCE_CLAY},
		{"liqued",""+EnvResource.RESOURCE_FRESHWATER},
		{"wax",""+EnvResource.RESOURCE_WAX},
		{"drink",""+EnvResource.RESOURCE_FRESHWATER},
		{"steel",""+EnvResource.RESOURCE_STEEL},
		{"lead",""+EnvResource.RESOURCE_LEAD},
		{"bronze",""+EnvResource.RESOURCE_BRONZE},
		{"copper",""+EnvResource.RESOURCE_COPPER},
		{"brass",""+EnvResource.RESOURCE_BRASS},
		{"platinium",""+EnvResource.RESOURCE_PLATINUM},
		{"titanium",""+EnvResource.RESOURCE_STEEL},
		{"aliminum",""+EnvResource.RESOURCE_TIN},
		{"aluminum",""+EnvResource.RESOURCE_TIN},
		{"metal",""+EnvResource.RESOURCE_IRON},
		{"wood",""+EnvResource.RESOURCE_WOOD},
		{"ebony",""+EnvResource.RESOURCE_EBONY},
		{"ice",""+EnvResource.RESOURCE_CRYSTAL},
		{"energy",""+EnvResource.RESOURCE_ENERGY},
		{"hardwood",""+EnvResource.RESOURCE_BALSA},
		{"softwood",""+EnvResource.RESOURCE_OAK},
		{"flesh",""+EnvResource.RESOURCE_MEAT},
		{"silk",""+EnvResource.RESOURCE_SILK},
		{"wool",""+EnvResource.RESOURCE_WOOL},
		{"cloth",""+EnvResource.RESOURCE_COTTON},
		{"fur",""+EnvResource.RESOURCE_FUR},
		{"water",""+EnvResource.RESOURCE_FRESHWATER},
		{"oak",""+EnvResource.RESOURCE_OAK},
		{"ivory",""+EnvResource.RESOURCE_IVORY},
		{"diamond",""+EnvResource.RESOURCE_DIAMOND},
		{"pearl",""+EnvResource.RESOURCE_PEARL},
		{"gem",""+EnvResource.RESOURCE_GEM},
		{"ruby",""+EnvResource.RESOURCE_GEM},
		{"obsidian",""+EnvResource.RESOURCE_OBSIDIAN},
		{"lead",""+EnvResource.RESOURCE_LEAD},
		{"leather",""+EnvResource.RESOURCE_LEATHER},
		{"glass",""+EnvResource.RESOURCE_GLASS},
		{"vellum",""+EnvResource.RESOURCE_HIDE},
		{"crystal",""+EnvResource.RESOURCE_CRYSTAL},
		{"clay",""+EnvResource.RESOURCE_CLAY},
		{"china",""+EnvResource.RESOURCE_CHINA},
		{"paper",""+EnvResource.RESOURCE_PAPER},
		{"adamantium",""+EnvResource.RESOURCE_ADAMANTITE},
		{"amethyst",""+EnvResource.RESOURCE_AMETHYST},
		{"bearskin",""+EnvResource.RESOURCE_FUR},
		{"blood",""+EnvResource.RESOURCE_BLOOD},
		{"bread",""+EnvResource.RESOURCE_BREAD},
		{"burlap",""+EnvResource.RESOURCE_HEMP},
		{"canvas",""+EnvResource.RESOURCE_HEMP},
		{"cashmire",""+EnvResource.RESOURCE_WOOL},
		{"coal",""+EnvResource.RESOURCE_COAL},
		{"cookie",""+EnvResource.RESOURCE_CRACKER},
		{"coral",""+EnvResource.RESOURCE_SEAWEED},
		{"Daggwood",""+EnvResource.RESOURCE_WOOD},
		{"daggwood",""+EnvResource.RESOURCE_WOOD},
		{"ethereal",""+EnvResource.RESOURCE_NOTHING},
		{"felt",""+EnvResource.RESOURCE_WOOL},
		{"fish",""+EnvResource.RESOURCE_FISH},
		{"flame",""+EnvResource.RESOURCE_ENERGY},
		{"flower",""+EnvResource.RESOURCE_FLOWERS},
		{"fruit",""+EnvResource.RESOURCE_FRUIT},
		{"golden",""+EnvResource.RESOURCE_GOLD},
		{"ink",""+EnvResource.RESOURCE_BLOOD},
		{"iron-wood",""+EnvResource.RESOURCE_IRONWOOD},
		{"jade",""+EnvResource.RESOURCE_JADE},
		{"kalarit",""+EnvResource.RESOURCE_LEATHER},
		{"lapis",""+EnvResource.RESOURCE_GEM},
		{"malachite",""+EnvResource.RESOURCE_COPPER},
		{"meat",""+EnvResource.RESOURCE_MEAT},
		{"none",""+EnvResource.RESOURCE_NOTHING},
		{"oil",""+EnvResource.RESOURCE_LAMPOIL},
		{"parafin",""+EnvResource.RESOURCE_LAMPOIL},
		{"pewter",""+EnvResource.RESOURCE_TIN},
		{"pie",""+EnvResource.RESOURCE_CRACKER},
		{"plant_organism",""+EnvResource.RESOURCE_GREENS},
		{"porcelain",""+EnvResource.RESOURCE_CHINA},
		{"quartz",""+EnvResource.RESOURCE_CRYSTAL},
		{"ruby-silver-diamond",""+EnvResource.RESOURCE_GEM},
		{"sapphire",""+EnvResource.RESOURCE_GEM},
		{"shadows",""+EnvResource.RESOURCE_NOTHING},
		{"tatamite",""+EnvResource.RESOURCE_ADAMANTITE},
		{"velvet",""+EnvResource.RESOURCE_SILK},
		{"wood-iron",""+EnvResource.RESOURCE_IRONWOOD},
		{"soft leather",""+EnvResource.RESOURCE_LEATHER},
		{"balm",""+EnvResource.RESOURCE_WAX},
		{"elastic",""+EnvResource.RESOURCE_PLASTIC},
		{"hemp",""+EnvResource.RESOURCE_HEMP},
		{"cork",""+EnvResource.RESOURCE_WOOD},
		{"sponge",""+EnvResource.RESOURCE_WOOL},
		{"rubber",""+EnvResource.RESOURCE_RUBBER},
		{"unique",""+EnvResource.RESOURCE_WOOD},
		{"satin",""+EnvResource.RESOURCE_SILK},
		{"horn",""+EnvResource.RESOURCE_BONE},
		{"bark",""+EnvResource.RESOURCE_WOOD},
		{"Fabric",""+EnvResource.RESOURCE_COTTON},
		{"Other",""+EnvResource.RESOURCE_WOOD},
		{"Cord",""+EnvResource.RESOURCE_HEMP},
		{"velum",""+EnvResource.RESOURCE_PAPER},
		{"clothe",""+EnvResource.RESOURCE_COTTON},
		{"beef",""+EnvResource.RESOURCE_BEEF},
		{"dough",""+EnvResource.RESOURCE_BREAD},
		{"chicken",""+EnvResource.RESOURCE_POULTRY},
		{"bagel",""+EnvResource.RESOURCE_BREAD},
		{"fries",""+EnvResource.RESOURCE_POTATOES},
		{"chicken",""+EnvResource.RESOURCE_POULTRY},
		{"cheese",""+EnvResource.RESOURCE_MILK},
		{"chalk",""+EnvResource.RESOURCE_STONE},
		{"cotton",""+EnvResource.RESOURCE_COTTON},
		{"electrum",""+EnvResource.RESOURCE_SILVER},
		{"wooden",""+EnvResource.RESOURCE_WOOD},
		{"cardboard",""+EnvResource.RESOURCE_PAPER},
		{"petals",""+EnvResource.RESOURCE_FLOWERS},
		{"lace",""+EnvResource.RESOURCE_SILK},
		{"fabric",""+EnvResource.RESOURCE_COTTON},
		{"flannel",""+EnvResource.RESOURCE_WOOL},
		{"Lightning",""+EnvResource.RESOURCE_ENERGY},
		{"lightning",""+EnvResource.RESOURCE_ENERGY},
		{"rawhide",""+EnvResource.RESOURCE_LEATHER},
		{"pine",""+EnvResource.RESOURCE_PINE},
		{"cream",""+EnvResource.RESOURCE_MILK},
		{"cheeseskin",""+EnvResource.RESOURCE_PLASTIC},
		{"fiberglass",""+EnvResource.RESOURCE_COTTON},
		{"leopard",""+EnvResource.RESOURCE_HIDE},
		{"tobacco",""+EnvResource.RESOURCE_PIPEWEED},
		{"flowers",""+EnvResource.RESOURCE_FLOWERS},
		{"emerald",""+EnvResource.RESOURCE_GEM},
		{"coffee",""+EnvResource.RESOURCE_COFFEEBEANS},
		{"turnip",""+EnvResource.RESOURCE_GREENS},
		{"nickel",""+EnvResource.RESOURCE_SILVER},
		{"yew",""+EnvResource.RESOURCE_WOOD},
		{"mahogany",""+EnvResource.RESOURCE_WOOD},
		{"alligator",""+EnvResource.RESOURCE_SCALES},
		{"potatoes",""+EnvResource.RESOURCE_POTATOES},
		{"yeast",""+EnvResource.RESOURCE_BREAD},
		{"pork",""+EnvResource.RESOURCE_PORK},
		{"turkey",""+EnvResource.RESOURCE_POULTRY},
		{"stew",""+EnvResource.RESOURCE_MEAT},
		{"chili",""+EnvResource.RESOURCE_MEAT},
		{"wonton",""+EnvResource.RESOURCE_MEAT},
		{"sandstone",""+EnvResource.RESOURCE_STONE},
		{"vevet",""+EnvResource.RESOURCE_SILK},
		{"lether",""+EnvResource.RESOURCE_LEATHER},
		{"(null)",""+EnvResource.RESOURCE_NOTHING}
	};


	private static String getAreaName(Vector V)
	{
		V=(Vector)V.clone();
		// find area line first
		String areaName="";
		String firstLine=nextLine(V);
		if((firstLine.indexOf("~")>=0)&&(firstLine.indexOf("}")>=0))
		{
			String areaLine=firstLine;
			areaLine=areaLine.substring(0,areaLine.length()-1);
			int x=areaLine.indexOf("}");
			areaLine=areaLine.substring(x+1).trim();
			x=areaLine.indexOf("  ");
			if(x>0)
				areaLine=areaLine.substring(x+1).trim();
			areaName=areaLine;
		}
		else
		if((firstLine.indexOf("~")>=0)
		&&(firstLine.startsWith("#AREA "))
		&&(firstLine.toUpperCase().indexOf(".ARE")<0))
		{
			String areaLine=firstLine;
			areaLine=areaLine.substring(5).trim();
			if(areaLine.endsWith("~"))
				areaLine=areaLine.substring(0,areaLine.length()-1).trim();
			areaName=areaLine;
		}
		else
		if(V.size()>1)
		{
			String lineAfter=(String)V.elementAt(1);
			if(lineAfter.indexOf("~")<0)
				return "";
			lineAfter=lineAfter.substring(0,lineAfter.length()-1);
			if((lineAfter.indexOf(".are")>=0)&&(V.size()>2)&&(lineAfter.indexOf("@@")<0))
			{
				lineAfter=(String)V.elementAt(2);
				if(lineAfter.indexOf("~")<0)
					return "";
				lineAfter=lineAfter.substring(0,lineAfter.length()-1);
				areaName=lineAfter.trim();
			}
			else
				areaName=lineAfter.trim();
		}
		if(areaName.toUpperCase().startsWith("NAME "))
		{
			if((areaName.length()>=41)
			&&(areaName.charAt(12)=='{')
			&&(areaName.charAt(21)!=' ')
			&&(areaName.charAt(40)==' '))
				areaName=areaName.substring(21,41).trim();
			else
			if((areaName.length()>13)&&((areaName.charAt(12)=='{')||(areaName.charAt(13)=='{')))			
			{
				int x=areaName.indexOf("}",12);
				if(x>12)
					areaName=areaName.substring(x+1).trim();
				else
					areaName=areaName.substring(5).trim();
			}
			else
				areaName=areaName.substring(5).trim();
		}

		if((areaName.indexOf(""+((char)27))>=0)
		||(areaName.indexOf("&")>=0)
		||(areaName.indexOf("{")>=0)
		||(areaName.indexOf("@@")>=0))
		{
			for(int s1=0;s1<colors.length;s1++)
				if(areaName.indexOf(colors[s1][0])>=0)
					areaName=Util.replaceAll(areaName,colors[s1][0],colors[s1][1]);
		}
		return Util.removeColors(CoffeeFilter.safetyFilter(areaName));
	}
	
	private static String getAreaAuthor(Vector V)
	{
		V=(Vector)V.clone();
		for(int v=0;v<V.size();v++)
		{
			String s=((String)V.elementAt(v)).trim();
			if(s.toUpperCase().startsWith("#AUTHOR "))
			{
				s=s.substring(8).trim();
				if(s.endsWith("~"))
					s=s.substring(0,s.length()-1).trim();
				return s;
			}
			else
			if(s.toUpperCase().startsWith("BUILDERS "))
			{
				s=s.substring(9).trim();
				if(s.endsWith("~"))
					s=s.substring(0,s.length()-1).trim();
				if((s.length()==0)||(s.toUpperCase().equalsIgnoreCase("NONE")))
					continue;
				return s;
			}
			else
			if((s.toUpperCase().startsWith("NAME     "))
			&&(s.indexOf("{")>0))
			{
				s=s.substring(5).trim();
				if(s.endsWith("~"))
					s=s.substring(0,s.length()-1).trim();
				if(Util.parse(s).size()>0)
					s=(String)Util.parse(s).lastElement();
				if((s.length()==0)||(s.toUpperCase().equalsIgnoreCase("NONE")))
					continue;
				return s;
			}
			else
			if((s.toUpperCase().startsWith("#AREA "))
			&&(s.indexOf("{")>0))
			{
				s=s.substring(6).trim();
				if(s.trim().startsWith("{"))
				{
					int x=s.trim().indexOf("}");
					if(x>0) s=s.substring(x+1).trim();
					if(s.endsWith("~"))
						s=s.substring(0,s.length()-1).trim();
					x=s.indexOf(" ");
					if((x>1)
					&&(s.substring(0,x).trim().toUpperCase().equals("THE"))
					&&(Util.parse(s).size()>0))
						s=(String)Util.parse(s).lastElement();
					else
					if(x>1) 
						s=s.substring(0,x).trim();
					if((s.length()==0)||(s.toUpperCase().equalsIgnoreCase("NONE")))
						continue;
					return s;
				}
			}
			else
			if(s.trim().startsWith("O ")&&(s.trim().endsWith("~")))
			{
				s=s.substring(1).trim();
				if(s.endsWith("~"))
					s=s.substring(0,s.length()-1).trim();
				if((s.length()==0)||(s.toUpperCase().equalsIgnoreCase("NONE")))
					continue;
				return s;
			}
			else
			if(s.toUpperCase().startsWith("CREDITS "))
			{
				s=s.substring(8).trim();
				if(s.trim().startsWith("("))
					continue;
				if(s.trim().startsWith("{"))
				{
					int x=s.trim().indexOf("}");
					if(x>0) s=s.substring(x+1).trim();
				}
				else
				if((Util.parse(s).size()>2)
				&&(Util.isNumber((String)Util.parse(s).elementAt(0)))
				&&(Util.isNumber((String)Util.parse(s).elementAt(1))))
					s=(String)Util.parse(s).elementAt(2);
				if(s.endsWith("~"))
					s=s.substring(0,s.length()-1).trim();
				int x=s.indexOf(" ");
				if(x>1) s=s.substring(0,x).trim();
				if((s.length()==0)||(s.toUpperCase().equalsIgnoreCase("NONE")))
					continue;
				return s;
			}
			else
			if(s.trim().startsWith("{"))
			{
				int x=s.trim().indexOf("}");
				if(x>0) s=s.substring(x+1).trim();
				if(s.endsWith("~"))
					s=s.substring(0,s.length()-1).trim();
				x=s.indexOf(" ");
				if((x>1)
				&&(s.substring(0,x).trim().toUpperCase().equals("THE"))
				&&(Util.parse(s).size()>0))
					s=(String)Util.parse(s).lastElement();
				else
				if(x>1) 
					s=s.substring(0,x).trim();
				if((s.length()==0)||(s.toUpperCase().equalsIgnoreCase("NONE")))
					continue;
				return s;
			}
			else
			if(s.trim().startsWith("["))
			{
				int x=s.trim().indexOf("]");
				if(x>0) s=s.substring(x+1).trim();
				if(s.endsWith("~"))
					s=s.substring(0,s.length()-1).trim();
				x=s.indexOf(" ");
				if((x>1)
				&&(s.substring(0,x).trim().toUpperCase().equals("THE"))
				&&(Util.parse(s).size()>0))
					s=(String)Util.parse(s).lastElement();
				else
				if(x>1) 
					s=s.substring(0,x).trim();
				if((s.length()==0)||(s.toUpperCase().equalsIgnoreCase("NONE")))
					continue;
				return s;
			}
		}
		return "";
	}
	
	private static final String[][] colors={
		{((char)27)+"ash"+((char)27),"^c"},
		{((char)27)+"black"+((char)27),"^W"},
		{((char)27)+"blood"+((char)27),"^R"},
		{((char)27)+"blue"+((char)27),"^B"},
		{((char)27)+"brown"+((char)27),"^Y"},
		{((char)27)+"cyan"+((char)27),"^c"},
		{((char)27)+"green"+((char)27),"^g"},
		{((char)27)+"grey"+((char)27),"^W"},
		{((char)27)+"pea"+((char)27),"^G"},
		{((char)27)+"purple"+((char)27),"^P"},
		{((char)27)+"red"+((char)27),"^r"},
		{((char)27)+"smurf"+((char)27),"^b"},
		{((char)27)+"teal"+((char)27),"^C"},
		{((char)27)+"violet"+((char)27),"^p"},
		{((char)27)+"white"+((char)27),"^w"},
		{((char)27)+"yellow"+((char)27),"^y"},
		{((char)27)+"misc"+((char)27),"^N"},
		{((char)27)+"roomname"+((char)27),"^O"},
		{((char)27)+"roomdesc"+((char)27),"^L"},
		{((char)27)+"monster"+((char)27),"^M"},
		{"_whi ","^w"},
		{"_yel ","^y"},
		{"_blu ","^b"},
		{"_dbl ","^B"},
		{"_lbl ","^C"},
		{"_cya ","^c"},
		{"_gre ","^g"},
		{"_dgr ","^G"},
		{"_cha ","^W"},
		{"_dch ","^W"},
		{"_pur ","^P"},
		{"_red ","^r"},
		{"_ora ","^Y"},
		{"_bro ","^Y"},
		{"_pin ","^p"},
		{"_bla ",""},
		{"&x",""},
		{"&r","^R"},
		{"&g","^G"},
		{"&O","^Y"},
		{"&b","^B"},
		{"&p","^P"},
		{"&c","^C"},
		{"&w","^W"},
		{"&z","^W"},
		{"&R","^r"},
		{"&G","^g"},
		{"&Y","^y"},
		{"&B","^b"},
		{"&P","^p"},
		{"&C","^c"},
		{"&W","^w"},
		{"&[",""},
		{"&[","^?"},
		{"{T","^C"},
		{"{x","^?"},
		{"{X","^?"},
		{"{r","^R"},
		{"{g","^G"},
		{"{O","^Y"},
		{"{b","^B"},
		{"{p","^P"},
		{"{M","^P"},
		{"{m","^p"},
		{"{c","^C"},
		{"{w","^W"},
		{"{D","^W"},
		{"{z","^W"},
		{"{R","^r"},
		{"{G","^g"},
		{"{Y","^y"},
		{"{B","^b"},
		{"{P","^p"},
		{"{C","^c"},
		{"{W","^w"},
		{"{y","^Y"},
		{"@@k",""},
		{"@@R","^R"},
		{"@@G","^G"},
		{"@@b","^Y"},
		{"@@B","^B"},
		{"@@p","^P"},
		{"@@c","^C"},
		{"@@g","^W"},
		{"@@d","^W"},
		{"@@r","^r"},
		{"@@e","^g"},
		{"@@y","^y"},
		{"@@l","^b"},
		{"@@m","^p"},
		{"@@M","^p"},
		{"@@a","^c"},
		{"@@W","^w"},
		{"@@x","^!"},
		{"@@f","^*"},
		{"@@i",""},
		{"@@2","^R"},
		{"@@3","^G"},
		{"@@4","^Y"},
		{"@@1","^B"},
		{"@@5","^P"},
		{"@@6","^C"},
		{"@@0","^W"},
		{"@@7","^W"},
		{"@@N","^?"}
	};

	private static String nextLine(Vector V)
	{
		if(V.size()==0) return "";
		return (String)V.elementAt(0);
	}
	private static String eatLine(Vector V)
	{
		if(V.size()==0) return "";
		String s=(String)V.elementAt(0);
		V.removeElementAt(0);
		return s;
	}
	private static String eatNextLine(Vector V)
	{
		String s="";
		while((s.trim().length()==0)&&(V.size()>0))
			s=eatLine(V);
		return s;
	}

	private static Room changeRoomClass(Room R, String newClass)
	{
		Room R2=CMClass.getLocale(newClass);
		if(R2==null)
		{
			Log.errOut("Import","Cannot find room class "+newClass+".");
			return R;
		}
		R2.setRoomID(R.roomID());
		R2.setArea(R.getArea());
		R2.setDescription(R.description());
		R2.setDisplayText(R.displayText());
		R2.setName(R.name());
		R2.setBaseEnvStats(R.baseEnvStats());
		R2.setMiscText(R.text());
		return R2;
	}

	private static int getBitMask(String str, int which)
	{
		String s=Util.getCleanBit(str,which);
		if(s.length()==0)
			return 0;
		int x=s.indexOf("|");
		if((x<0)&&(s.length()>0)&&(Util.s_int(s)==0))
		{
			boolean otherStyle=true;
			int num=0;
			for(int z=0;z<s.length();z++)
				if(!Character.isLetter(s.charAt(z)))
				{
				   otherStyle=false;
				   break;
				}
				else
				if(Character.isUpperCase(s.charAt(z)))
					num=num|(1<<(s.charAt(z))-('A'));
				else
				if(Character.isLowerCase(s.charAt(z)))
					num=num|(1<<(26+((s.charAt(z))-('a'))));

			if(otherStyle)
				return num;
		}

		int num=0;
		while(x>0)
		{
			num=num|Util.s_int(s.substring(0,x));
			s=s.substring(x+1);
			x=s.indexOf("|");
		}

		return (num|Util.s_int(s));
	}

	private static String trimSpacesOnly(String s)
	{
		while(s.startsWith(" ")||s.startsWith("\t")||s.startsWith("\n")||s.startsWith("\r"))
			s=s.substring(1);
		while(s.endsWith(" ")||s.endsWith("\t")||s.endsWith("\n")||s.endsWith("\r"))
			s=s.substring(0,s.length()-1);
		return s;
	}

	private static String eatLineSquiggle(Vector V)
	{
		if(V.size()==0) return "";
		String s=eatLine(V);
		while(s.indexOf("~")<0)
		{
			String l=eatLine(V);
			if(l.startsWith(" "))
				s+="%0D"+l;
			else
			if(l.length()==0)
				s+="%0D";
			else
				s+=" "+l;
		}
		s=trimSpacesOnly(s);

		if(s.endsWith("~"))
			s=s.substring(0,s.length()-1);

		if(s.indexOf("^")>=0)	s=Util.replaceAll(s,"^","^^");

		if((s.indexOf(""+((char)27))>=0)
		||(s.indexOf("&")>=0)
		||(s.indexOf("{")>=0)
		||(s.indexOf("_")>=0)
		||(s.indexOf("@@")>=0))
		{
			for(int s1=0;s1<colors.length;s1++)
				if(s.indexOf(colors[s1][0])>=0)
					s=Util.replaceAll(s,colors[s1][0],colors[s1][1]);
		}

		int x=s.indexOf("@eng");
		if(x>=0)
		{
			int y=s.indexOf("@rus",x);
			if(y<x) y=s.indexOf("@ger",x);
			if(y<x) y=s.indexOf("@spa",x);
			if(y<x) y=s.length();
			s=s.substring(x+4,y);
		}
		return s.trim();
	}

	private static boolean hasReadableContent(String objectName)
	{
		objectName=objectName.toUpperCase();
		if((objectName.indexOf("SIGN")>=0)
			||(objectName.indexOf("PLAQUE")>=0)
		    ||(objectName.indexOf("NOTICE")>=0)
		    ||(objectName.indexOf("PAPER")>=0)
		    ||(objectName.indexOf("WRITING")>=0)
		    ||(objectName.indexOf("CARVING")>=0)
		    ||(objectName.indexOf("LETTER")>=0)
		    ||(objectName.indexOf("INSCRIPTION")>=0)
		    ||(objectName.indexOf("NOTE")>=0)
		    ||(objectName.indexOf("POST")>=0))
				return true;
		return false;
	}

	private static String fixReadableContent(String text)
	{
		while(text.startsWith("%0D"))
			text=text.substring(3);
		if((text.toUpperCase().trim().startsWith("IT SAYS `"))
		||(text.toUpperCase().trim().startsWith("IT SAYS '")))
		{
			text=text.trim().substring(9).trim();
			if((text.endsWith("'"))||(text.endsWith("`")))
				text=text.substring(0,text.length()-1);
		}
		if(text.toUpperCase().trim().startsWith("IT SAYS:"))
			text=text.trim().substring(8).trim();
		if(text.toUpperCase().trim().startsWith("IT SAYS"))
			text=text.trim().substring(7).trim();
		return text;
	}

	private static boolean returnAnError(MOB mob, String str)
	{
		Log.errOut("Import",str);
		mob.tell(str);
		return false;
	}

	private static String getSpell(String word, int i)
	{
		if((word.trim().length()>0)&&((Character.isLetter(word.trim().charAt(0)))||(word.trim().startsWith("'"))||(word.trim().startsWith("`"))))
		{
			word=word.toUpperCase().trim();
			if((word.startsWith("'"))||(word.startsWith("`")))
			   word=word.substring(1);
			if(word.length()<3)	return "";
			if(word.startsWith("NONE")) i=-1;
			else
			if(word.startsWith("COMPLETE HEAL")) i=209;
			else
			if(word.startsWith("RESTORE MANA")) i=234;
			else
			if(word.startsWith("ACID B")) i=70;
			else
			if(word.startsWith("ARMOR")) i=1;
			else
			if(word.startsWith("ENHANCED ARMOR")) i=212;
			else
			if(word.startsWith("ENHANCE ARMOR")) i=212;
			else
			if(word.startsWith("BLESS")) i=3;
			else
			if(word.startsWith("BLINDNE")) i=4;
			else
			if(word.startsWith("BURNING H")) i=5;
			else
			if(word.startsWith("HASTE")) i=84;
			else
			if(word.startsWith("CALL LIGH")) i=6;
			else
			if(word.startsWith("GENERAL PURPOSE")) i=-1;
			else
			if(word.startsWith("CANCELLA")) i=59;
			else
			if(word.startsWith("CAUSE CRI")) i=63;
			else
			if(word.startsWith("CAUSE LI")) i=62;
			else
			if(word.startsWith("CAUSE SE")) i=222;
			else
			if(word.startsWith("CHANGE SE")) i=82;
			else
			if(word.startsWith("CHARM PER")) i=7;
			else
			if(word.startsWith("CHILL TOU")) i=8;
			else
			if(word.startsWith("COLO")) i=10;
			else
			if(word.startsWith("RESERVED")) i=223;
			else
			if(word.startsWith("FIREBA")) i=26;
			else
			if(word.startsWith("FLAMESTR")) i=65;
			else
			if(word.startsWith("FLY")) i=56;
			else
			if(word.startsWith("GATE")) i=83;
			else
			if(word.startsWith("GIANT")) i=39;
			else
			if(word.startsWith("HOLY STRE")) i=39;
			else
			if(word.startsWith("ENHANCED STRE")) i=39;
			else
			if(word.startsWith("HARM")) i=27;
			else
			if(word.startsWith("HOLY FIRE")) i=27;
			else
			if(word.startsWith("HEAL")) i=28;
			else
			if(word.startsWith("IDENTIFY")) i=53;
			else
			if(word.startsWith("INFRAVISION")) i=77;
			else
			if(word.startsWith("INVIS")) i=29;
			else
			if(word.startsWith("KNOW")) i=58;
			else
			if(word.startsWith("LIGHTNING BOLT")) i=30;
			else
			if(word.startsWith("LIGHTENING BOLT")) i=30;
			else
			if(word.startsWith("LIGHTNINGBOLT")) i=30;
			else
			if(word.startsWith("LIGHTENINGBOLT")) i=30;
			else
			if(word.startsWith("LOCATE")) i=31;
			else
			if(word.startsWith("CANCELL")) i=57;
			else
			if(word.startsWith("CONTINU")) i=57;
			else
			if(word.startsWith("CONTROL")) i=11;
			else
			if(word.startsWith("CREATE FO")) i=12;
			else
			if(word.startsWith("CREATE SP")) i=80;
			else
			if(word.startsWith("CREATE WA")) i=13;
			else
			if(word.startsWith("CURE BLI")) i=14;
			else
			if(word.startsWith("CURE CRI")) i=15;
			else
			if(word.startsWith("CURE LI")) i=16;
			else
			if(word.startsWith("CURE PO")) i=43;
			else
			if(word.startsWith("CURE SE")) i=61;
			else
			if(word.startsWith("CURE D")) i=45;
			else
			if(word.startsWith("DETECT E")) i=18;
			else
			if(word.startsWith("DETECT HI")) i=44;
			else
			if(word.startsWith("DETECT I")) i=19;
			else
			if(word.startsWith("DETECT M")) i=20;
			else
			if(word.startsWith("DETECT P")) i=21;
			else
			if(word.startsWith("DISPEL E")) i=22;
			else
			if(word.startsWith("DISPEL M")) i=59;
			else
			if(word.startsWith("EARTHQ")) i=23;
			else
			if(word.startsWith("ENCHANT W")) i=24;
			else
			if(word.startsWith("ENERGY DRA")) i=25;
			else
			if(word.startsWith("FAERIE F")) i=72;
			else
			if(word.startsWith("MAGIC MI")) i=32;
			else
			if(word.startsWith("MASS INV")) i=69;
			else
			if(word.startsWith("PASS D")) i=74;
			else
			if(word.startsWith("POISON")) i=33;
			else
			if(word.startsWith("PROTECTION")) i=34;
			else
			if(word.startsWith("REFRESH")) i=81;
			else
			if(word.startsWith("REMOVE CU")) i=35;
			else
			if(word.startsWith("SANCTUARY")) i=36;
			else
			if(word.startsWith("SHIELD")) i=67;
			else
			if(word.startsWith("SHOCKING G")) i=37;
			else
			if(word.startsWith("SLEEP")) i=38;
			else
			if(word.startsWith("STONE SK")) i=66;
			else
			if(word.startsWith("SUMMON")) i=40;
			else
			if(word.startsWith("TELEPORT")) i=2;
			else
			if(word.startsWith("VENTRI")) i=41;
			else
			if(word.startsWith("WEAKEN")) i=68;
			else
			if(word.startsWith("WORD OF R")) i=42;
			else
			if(word.startsWith("ACID BR")) i=200;
			else
			if(word.startsWith("FIRE BR")) i=201;
			else
			if(word.startsWith("FROST BR")) i=202;
			else
			if(word.startsWith("GAS BR")) i=203;
			else
			if(word.startsWith("LIGHTNING BR")) i=204;
			else
			if(word.startsWith("LIGHTENING BR")) i=204;
			else
			if(word.startsWith("FRENZY")) i=205;
			else
			if(word.startsWith("DISPEL G")) i=206;
			else
			if(word.startsWith("CURSE")) i=17;
			else
			if(word.startsWith("ENCHANT W")) i=207;
			else
			if(word.startsWith("ENCHANT A")) i=208;
			else
			if(word.startsWith("REJUV")) i=209;
			else
			if(word.startsWith("PLAGUE")) i=213;
			else
			if(word.startsWith("HEAT M")) i=210;
			else
			if(word.startsWith("HIGH EXP")) i=26;
			else
			if(word.startsWith("FARSIGHT")) i=211;
			else
			if(word.startsWith("SLOW")) i=214;
			else
			if(word.startsWith("WEB")) i=215;
			else
			if(word.startsWith("CONFUSE")) i=216;
			else
			if(word.startsWith("FIRESHIELD")) i=232;
			else
			if(word.startsWith("ICESHIELD")) i=232;
			else
			if(word.startsWith("SHOCKSHIELD")) i=89;
			else
			if(word.startsWith("BLADE BARRIER")) i=233;
			else
			if(word.startsWith("SENSE LI")) i=217;
			else
			if(word.startsWith("MYSTERIOUS DR")) i=218;
			else
			if(word.startsWith("MIND LIGHT")) i=219;
			else
			if(word.startsWith("ACUTE VIS")) i=44;
			else
			if(word.startsWith("CALM")) i=220;
			else
			if(word.startsWith("DETECT G")) i=221;
			else
			if(word.startsWith("DEMONFIRE")) i=27;
			else
			if(word.startsWith("BARK SKIN")) i=224;
			else
			if(word.startsWith("STEEL SKIN")) i=224;
			else
			if(word.startsWith("LETHARGIC MIS")) i=214;
			else
			if(word.startsWith("BLACK DEATH")) i=213;
			else
			if(word.startsWith("DISGRACE")) i=225;
			else
			if(word.startsWith("POWER WORD ST")) i=226;
			else
			if(word.startsWith("MASTER HEAL")) i=227;
			else
			if(word.startsWith("SCREAM")) i=32;
			else
			if(word.startsWith("SEVERITY FO")) i=23;
			else
			if(word.startsWith("HOLY WORD")) i=228;
			else
			if(word.startsWith("MASS HEAL")) i=229;
			else
			if(word.startsWith("ACID RAIN")) i=230;
			else
			if(word.startsWith("ETHEREAL FORM")) i=231;
			else
			if(word.startsWith("MANA")) i=234;
			else
			if(word.startsWith("CHAOS FIELD")) i=235;
			else
			if(word.startsWith("COMBAT MIND")) i=236;
			else
			if(word.startsWith("DARK BLESSING")) i=17;
			else
			if(word.startsWith("FLESH ARMOR")) i=224;
			else
			if(word.startsWith("TRUE SIGHT")) i=237;
			else
			if(word.startsWith("INERTIAL")) i=36;
			else
			if(word.startsWith("PHASE SHIFT")) i=36;
			else
			if(word.startsWith("DISPLACEMENT")) i=244;
			else
			if(word.startsWith("THOUGHT SHIELD")) i=238;
			else
			if(word.startsWith("INTELLECT FORTRESS")) i=238;
			else
			if(word.startsWith("MENTAL BARRIER")) i=238;
			else
			if(word.startsWith("MENTAL BLOCK")) i=238;
			else
			if(word.startsWith("SHATTER")) i=239;
			else
			if(word.startsWith("ICESTORM")) i=240;
			else
			if(word.startsWith("DISINTEGRATE")) i=241;
			else
			if(word.startsWith("ANIMATE DEAD")) i=242;
			else
			if(word.startsWith("VIBRATE")) i=243;
			else
			if(word.startsWith("ULTRABLAST")) i=26;
			else
			if(word.startsWith("FIELD OF DECAY")) i=245;
			else
			if(word.startsWith("BIOFEEDBACK")) i=245;
			else
			if(word.startsWith("DANCING LIGHTS")) i=246;
			else
			if(word.startsWith("BIO-ACCELERATION")) i=227;
			else
			if(word.startsWith("CELL ADJUSTMENT")) i=227;
			else
			if(word.startsWith("AURA SIGHT")) i=20;
			else
			if(word.startsWith("ENERGY CONTAINMENT")) i=247;
			else
			{
				Log.sysOut("Unknown spell: "+word);
				return "";
			}
		}

		switch(i)
		{
		case -1: break;
		case 0: break;
		case 1: return "Spell_GraceOfTheCat"; // armor
		case 2: return "Spell_Teleport";
		case 3: return "Prayer_Bless";
		case 4: return "Spell_Blindness";
		case 5: return "Spell_BurningHands";
		case 6: return "Spell_Clog"; // call lightening, dumb
		case 7: return "Spell_Charm";
		case 8: return "Undead_ColdTouch"; // chill touch
		case 9: return "Spell_MirrorImage"; // clone
		case 10: return "Spell_Feeblemind"; // color spray
		case 11: return "Chant_CallRain";
		case 12: return "Prayer_CreateFood";
		case 13: return "Prayer_CreateWater";
		case 14: return "Prayer_CureBlindness";
		case 15: return "Prayer_CureCritical";
		case 16: return "Prayer_CureLight";
		case 17: return "Prayer_Curse";
		case 18: return "Prayer_SenseEvil";
		case 19: return "Spell_DetectInvisible";
		case 20: return "Spell_DetectMagic";
		case 21: return "Spell_DetectPoison";
		case 22: return "Prayer_DispelEvil";
		case 23: return "Spell_Earthquake";
		case 24: return "Spell_EnchantWeapon";
		case 25: return "Prayer_Drain";
		case 26: return "Spell_Fireball";
		case 27: return "Prayer_Harm";
		case 28: return "Prayer_Heal";
		case 29: return "Spell_Invisibility";
		case 30: return "Spell_Lightning";
		case 31: return "Spell_LocateObject";
		case 32: return "Spell_MagicMissile";
		case 33: return "Thief_Poison";
		case 34: return "Prayer_ProtEvil";
		case 35: return "Prayer_RemoveCurse";
		case 36: return "Prayer_Sanctuary";
		case 37: return "Spell_ShockingGrasp";
		case 38: return "Spell_Sleep";
		case 39: return "Spell_GiantStrength";
		case 40: return "Spell_Summon";
		case 41: return "Spell_Ventriloquate";
		case 42: return "Skill_Recall";
		case 43: return "Prayer_RemovePoison";
		case 44: return "Chant_Dragonsight";
		case 45: return "Prayer_CureDisease"; // not the real ###
		case 51: return "Spell_ShockingGrasp";
		case 53: return "Spell_IdentifyObject";
		case 54: return "Prayer_AnimateDead";
		case 55: return "Spell_Fear";
		case 56: return "Spell_Fly";
		case 57: return "Spell_Light";
		case 58: return "Spell_KnowAlignment";
		case 59: return "Spell_DispelMagic";
		case 60: return "Spell_FlamingHands";
		case 61: return "Prayer_CureSerious";
		case 62: return "Prayer_CauseLight";
		case 63: return "Spell_WaterBreathing"; // water of lifew
		case 64: return "Prayer_CauseSerious";
		case 65: return "Spell_Dragonfire"; // flamestrike
		case 66: return "Spell_Stoneskin";
		case 67: return "Spell_Shield";
		case 68: return "Spell_Weaken";
		case 69: return "Spell_MassInvisibility";
		case 70: return "Spell_AcidArrow"; // acid blast
		case 71: return "Spell_Portal"; // actually mass teleport
		case 72: return "Spell_FaerieFog";
		case 73: return "Spell_IceStorm";
		case 74: return "Spell_PassDoor";
		case 75: return "Spell_EnchantArmor";
		case 76: return "Spell_StoneFlesh"; // stone
		case 77: return "Spell_Infravision";
		case 78: return "Prayer_RaiseDead";
		case 80: return "Prayer_CreateWater";
		case 81: return "Prayer_Restoration"; // refresh
		case 82: return "Spell_ChangeSex";
		case 83: return "Spell_Gate";
		case 84: return "Spell_Haste";
		case 85: return "Chant_SummonFire";
		case 86: return "Spell_DetectTraps";
		case 87: return "Thief_RemoveTraps";
		case 88: return "Spell_Flameshield";
		case 89: return "Spell_Shockshield";
		case 90: return "Spell_PassDoor";
		case 91: return "Spell_Scry";
		case 93: return "Skill_Meditate";
		case 94: return "Skill_Meditate";
		case 97: return "Spell_Web";
		case 98: return "Spell_EnchantArmor";
		case 99: return "Spell_Teleport";
		case 101: return ""; // create symbol
		case 102: return ""; // alertness
		case 103: return "Spell_Fatigue"; // fatigue
		case 104: return ""; // grounding
		case 105: return ""; // charged beacon
		case 106: return ""; // resilience
		case 107: return "Spell_Feeblemind";
		case 108: return ""; // ill fortune
		case 109: return "Spell_Slow";
		case 110: return ""; // unravel defence
		case 111: return ""; // holy sanctity
		case 112: return ""; // divinity
		case 113: return "Prayer_Restoration";
		case 114: return "Undead_ColdTouch";
		case 115: return ""; // spiritual wrath
		case 120: return "Spell_Cloudkill";
		case 121: return ""; // quickening
		case 122: return "Chant_SummonElemental"; // summon elemental
		case 124: return ""; // uplift
		case 200: return "Acidbreath";
		case 201: return "Firebreath";
		case 202: return "Frostbreath";
		case 203: return "Gasbreath";
		case 204: return "Lighteningbreath";
		case 205: return "Spell_Frenzy";
		case 206: return "Prayer_DispelGood";
		case 207: return "Spell_EnchantWeapon";
		case 208: return "Spell_EnchantArmor";
		case 209: return "Prayer_Restoration";
		case 210: return "Spell_HeatMetal";
		case 211: return "Spell_Farsight";
		case 212: return "Spell_MageArmor";
		case 213: return "Prayer_Plague";
		case 214: return "Spell_Slow";
		case 215: return "Spell_Web";
		case 216: return "Spell_Confusion";
		case 217: return "Spell_DetectSentience";
		case 218: return "Spell_Nightmare";
		case 219: return "Spell_MindLight";
		case 220: return "Prayer_Calm";
		case 221: return "Prayer_SenseGood";
		case 222: return "Prayer_CauseSerious";
		case 223: return "";
		case 224: return "Chant_Barkskin";
		case 225: return "Spell_Tourettes";
		case 226: return "Spell_Misstep";
		case 227: return "Prayer_Restoration";
		case 228: return "Prayer_HolyWord";
		case 229: return "Prayer_MassHeal";
		case 230: return "Spell_AcidFog";
		case 231: return "Prayer_Etherealness";
		case 232: return "Spell_Flameshield";
		case 233: return "Prayer_BladeBarrier";
		case 234: return "Chant_RestoreMana";
		case 235: return "Spell_Frenzy";
		case 236: return "Spell_CombatPrecognition";
		case 237: return "Spell_TrueSight";
		case 238: return "Spell_MindBlock";
		case 239: return "Spell_Shatter";
		case 240: return "Spell_IceStorm";
		case 241: return "Spell_Disintegrate";
		case 242: return "Prayer_AnimateDead";
		case 243: return "Spell_Siphon";
		case 244: return "Spell_Blink";
		case 245: return "Prayer_Plague";
		case 246: return "Spell_Delirium";
		case 247: return "Chant_SpellWard";
		case 291: return "Chant_PlantPass";
		case 292: return "Spell_FeatherFall";
		case 294: return "Spell_Polymorph";
		case 295: return ""; // spiral blast
		case 296: return "Spell_BurningHands";
		case 299: return ""; // ice shard
		case 300: return ""; //Torrent
		case 301: return ""; //Black Hand
		case 302: return ""; //Acetum Primus
		case 303: return ""; //Black Lightning
		case 304: return ""; //Galvanic Whip
		case 305: return ""; //Disruption
		case 306: return ""; //Spectral Furor
		case 308: return ""; //Sulfurous Spray
		case 309: return ""; //Sonic Resonance
		case 310: return ""; //Black Fist
		case 311: return ""; //Magnetic Thrust
		case 313: return ""; //Caustic Fount
		case 314: return ""; //Quantum Spike
		case 315: return ""; //Energy Blast
		case 342: return ""; //Benefic Aura
		case 356: return ""; //Succor
		case 402: return "Skill_Explosive";
		case 500: return "Spell_ChainLightening";
		case 501: return "Chant_Goodberry";
		case 502: return "Prayer_FlameWeapon";
		case 503: return "Chant_GrowClub"; // spiritual hammer
		case 504: return "Spell_Frenzy";
		case 505: return "Spell_Fireball";
		case 506: return "Prayer_HolyWord";
		case 507: return ""; // vine whip
		case 508: return "Chant_Barkskin";
		case 509: return "Prayer_ProtectElements";
		case 510: return "Spell_Frenzy";
		case 511: return ""; // fletch
		case 512: return ""; // divine aid
		case 513: return "Spell_Frenzy"; // divine fury
		case 514: return "Prayer_Curse"; // lich curse
		case 515: return "Spell_GiantStrength";
		case 516: return "Prayer_Drain"; // withering hand
		case 517: return "Spell_GraceOfTheCat"; // chaos armor
		case 518: return ""; // soul scream
		case 519: return "Spell_StinkingCloud";
		case 520: return "Spell_Web";
		case 521: return "Chant_PlantSnare";
		case 522: return "Prayer_BladeBarrier";
		case 523: return "Spell_ResistFire";
		case 524: return ""; // heroes feast
		case 525: return "Prayer_RemoveParalysis";
		case 526: return "Druid_RecoverVoice";
		case 527: return "Spell_Silence";
		case 528: return "Spell_Hold";
		case 529: return "Prayer_HolyWord";
		case 530: return "Spell_PolymorphSelf";
		case 531: return "Spell_GustOfWind";
		case 532: return ""; //creepingdoom
		case 533: return "Chant_Sunray";
		case 534: return "Prayer_Calm";
		case 535: return ""; //fireseed
		case 536: return ""; //warmount
		case 537: return ""; //despair
		case 538: return "Spell_Forget";
		case 539: return "Skill_Recall";
		case 540: return "Spell_Portal";
		case 541: return ""; // mass armor
		case 542: return ""; // arcane perception
		case 543: return ""; // mass armor
		case 544: return "Spell_MassFeatherfall"; // mass featherfall
		case 545: return ""; // mass refresh
		case 546: return "Spell_MassFly";
		case 547: return ""; // minor track
		case 548: return ""; // major track
		case 550: return "Spell_AcidFog";
		case 551: return "Spell_ResistPoison";
		case 553: return "Prayer_Poison";
		case 554: return "Spell_AcidArrow";
		case 555: return "Spell_MeteorStorm";
		case 556: return "Spell_Frost";
		case 559: return "Spell_GustOfWind";
		case 560: return "Spell_StinkingCloud";
		case 561: return ""; // skeletal armor
		case 562: return "Prayer_AiryForm"; // wraithform
		case 563: return "Spell_ManaBurn";
		case 564: return "Undead_ColdTouch";
		case 565: return ""; // death chant
		case 566: return ""; // life surge
		case 567: return ""; // mana surge
		case 568: return ""; // death aura
		case 569: return ""; // dark ritual
		case 570: return "Prayer_Deathfinger";
		case 573: return ""; // crystal blades
		case 574: return "Spell_Dragonfire"; // fiery dragon
		case 575: return ""; // celestial thunder
		case 576: return ""; // celestial lightning
		case 577: return ""; // create mistletow
		case 578: return "Prayer_Plague"; // toxin shot
		case 579: return "Spell_Slow";
		case 580: return "Chant_Dragonsight";
		case 581: return "Spell_SummonSteed";
		case 582: return ""; // rejuvenate
		case 583: return "Spell_Haste";
		case 584: return ""; // cacophony
		case 585: return "Spell_Disintegrate";
		case 586: return ""; // beacon
		case 587: return "Prayer_Anger";
		case 588: return "Spell_MassWaterbreath";
		case 589: return "Spell_Gate";
		case 590: return "Spell_Portal";
		case 591: return ""; // tree transport
		case 592: return ""; // treespeak
		case 593: return ""; // bloodslash
		case 594: return "Spell_SpellTurning";
		case 595: return "Spell_ChainLightening";
		case 596: return ""; // harmonic aura
		case 597: return "Spell_Siphon";
		case 598: return "Undead_ColdTouch";
		case 599: return "Spell_ResistFire";
		case 600: return "Spell_ResistCold";
		case 601: return "Spell_ResistFire";
		case 602: return "Prayer_Curse";
		case 603: return "Spell_Knock";
		case 604: return "Prayer_Deathfinger";
		case 605: return "Spell_MinorGlobe";
		case 606: return "Spell_Mirage";
		case 607: return ""; // tarangreal
		case 608: return ""; // kassandra
		case 609: return ""; // sebat
		case 610: return ""; // metandra
		case 611: return ""; // vampiric blast
		case 612: return ""; // dragonskin
		case 613: return "Prayer_Heal";
		case 614: return "Spell_ResistElectricity";
		case 615: return ""; // shocking trap
		case 616: return "Spell_Feeblemind"; //insanity
		case 617: return ""; // holy shield
		case 618: return ""; // evil spirit
		case 619: return ""; // disgrace
		case 620: return ""; // summon shadow
		case 621: return ""; // dismantle
		case 622: return ""; // astral walk
		case 623: return "Prayer_SenseLife";
		case 624: return ""; // take revenge
		case 625: return "Spell_Hold";
		case 626: return ""; // improved detect
		case 627: return "Spell_ImprovedInvisibility";
		case 628: return "Spell_MassSleep";
		case 629: return ""; // desert heat
		case 630: return "Prayer_DivineLuck";
		case 631: return "Paralysis";
		case 632: return ""; // lighting stroke
		case 633: return "Spell_Repulsion";
		case 634: return "Spel_MassSlow";
		case 636: return "Prayer_ProtUndead";
		case 637: return "Prayer_BlessItem";
		case 638: return ""; // resiliance
		case 639: return "Spell_Polymorph";
		case 640: return "Prayer_Heal";
		case 641: return "Prayer_Restoration";
		case 643: return "Prayer_Restoration";
		case 644: return "Spell_AcidArrow";
		case 645: return ""; // etheral fist
		case 646: return ""; // spectral furor
		case 647: return "Undead_ColdTouch";
		case 649: return ""; // disruption
		case 650: return "Spell_Forget"; // mind wrack
		case 651: return "Spell_Feeblemind"; // mind wrack
		case 652: return ""; // sulferous spray
		case 653: return ""; // caustic front
		case 655: return ""; // galvan whip
		case 656: return ""; // magnetic trust
		case 657: return ""; // quantum spike
		case 658: return ""; // mist walk
		case 659: return ""; // solar flight
		case 660: return ""; // blue fire
		case 661: return ""; // hellenic flow
		case 662: return ""; // lesser golem
		case 663: return ""; // stone golem
		case 664: return ""; // iron golem
		case 665: return ""; // adamantite golem
		case 666: return "Prayer_ProtGood";
		case 667: return ""; // ruler aura
		case 668: return "Chant_Reincarnation";
		case 669: return "Skill_ControlUndead";
		case 670: return ""; // assist
		case 671: return ""; // corruption
		case 672: return "Chant_Tornado";//hurricane
		case 673: return ""; // sanctify lands
		case 674: return ""; // deadly worm
		case 675: return ""; // cursed lands
		case 676: return ""; // lethargic mist
		case 677: return ""; // black death
		case 678: return "Spell_Dream";
		case 679: return "Prayer_BladeBarrier";
		case 680: return ""; // aid
		case 681: return ""; // desert fist

		default:
			Log.sysOut("Unknown spell num: "+i);
			break;
		}
		return "";
	}

	private static int importNumber(String s)
	{
		StringBuffer str=new StringBuffer("");
		for(int i=0;i<s.length();i++)
			switch(s.charAt(i))
			{
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				str.append(s.charAt(i));
				break;
			case 'x':
			case 'X':
			case 'Z':
			case ' ':
				break;
			default:
				return 0;
			}
		return Util.s_int(str.toString());
	}

	private static void readBlocks(Vector buf,
						   Vector areaData,
						   Vector roomData,
						   Vector mobData,
						   Vector resetData,
						   Vector objectData,
						   Vector mobProgData,
						   Vector objProgData,
						   Vector shopData,
						   Vector specialData,
						   Vector socialData)
	{
		Vector helpsToEat=new Vector();

		Vector wasUsingThisOne=null;
		Vector useThisOne=null;
		while(buf.size()>0)
		{
			String s=((String)buf.elementAt(0)).toUpperCase().trim();
			if(s.startsWith("#")&&((String)buf.elementAt(0)).startsWith(" "))
				s=((String)buf.elementAt(0)).toUpperCase();
			boolean okString=true;
			if(s.startsWith("#"))
			{
				s=s.substring(1).trim();
				if((s.startsWith("AREA"))
				||(s.startsWith("AUTHOR")))
				{
					wasUsingThisOne=null;
					if((s.indexOf("~")>=0)
					&&(s.startsWith("AREA ")||s.startsWith("AUTHOR ")))
						okString=true;
					useThisOne=areaData;
				}
				else
				if((s.startsWith("MOBILE"))
				||(s.startsWith("MOBOLD")))
				{
					wasUsingThisOne=mobData;
					useThisOne=mobData;
				}
				else
				if((s.startsWith("OBJECT"))
				||(s.startsWith("OBJOLD")))
				{
					wasUsingThisOne=objectData;
					useThisOne=objectData;
				}
				else
				if((s.startsWith("OLIMITS"))
				||(s.startsWith("OMPROGS"))
				||(s.startsWith("ECONOMY"))
				||(s.startsWith("RANGES"))
				||(s.startsWith("CLIMATE"))
				||(s.startsWith("RESETMSG"))
				||(s.startsWith("VERSION"))
				||(s.startsWith("CONTINENT"))
				||(s.startsWith("COORDS"))
				||(s.startsWith("RESETFREQUENCY"))
				||(s.startsWith("VNUMS"))
				||(s.startsWith("FLAGS"))
				||(s.startsWith("REPAIRS"))
				||(s.startsWith("OBJFUNS"))
				||(s.startsWith("HELPS"))
				||(s.startsWith("PRACTICERS")))
				{
					wasUsingThisOne=null;
					useThisOne=helpsToEat;
				}
				else
				if(s.startsWith("MOBPROG"))
				{
					wasUsingThisOne=null;
					useThisOne=mobProgData;
				}
				else
				if(s.startsWith("ROOM"))
				{
					wasUsingThisOne=roomData;
					useThisOne=roomData;
				}
				else
				if(s.startsWith("RESETS"))
				{
					wasUsingThisOne=null;
					useThisOne=resetData;
				}
				else
				if(s.startsWith("SHOP"))
				{
					wasUsingThisOne=null;
					useThisOne=shopData;
				}
				else
				if(s.startsWith("SPECIALS"))
				{
					wasUsingThisOne=null;
					useThisOne=specialData;
				}
				else
				if(s.startsWith("SOCIALS"))
				{
					wasUsingThisOne=null;
					useThisOne=socialData;
				}
				else
				if((importNumber(s)>0)&&(wasUsingThisOne!=null))
				{
					Vector V=new Vector();
					wasUsingThisOne.addElement(V);
					useThisOne=V;
				}
				else
				if(s.equals("0")||s.equals("$")||s.equals("O"))
				{
					okString=false;
				}
				else
				if((s.equals("")||s.equals("~"))&&(useThisOne==socialData))
					okString=true;
				else
				if(useThisOne==mobProgData)
					okString=true;
				else
				{
					//useThisOne=null;
					Log.sysOut("Import","Suspect line: "+s);
				}
			}
			if(useThisOne!=null)
			{
				if(okString)
					useThisOne.addElement(CoffeeFilter.safetyFilter((String)buf.elementAt(0)));
				buf.removeElementAt(0);
			}
			else
			{
				Log.sysOut("Import","Just eating: "+s);
				buf.removeElementAt(0);
			}
		}
		if(helpsToEat.size()>0)
		{
			Log.sysOut("Import","Ate "+helpsToEat.size()+" unsupported lines.");
			helpsToEat.clear();
		}
	}

	private static void doWeapon(Weapon I, String name, int val1, String str1, int val2, int val3, int val4, String str4)
	{
		final String[][] weaponTypes={
			{ "exotic","0"},
			{ "sword","1"},
			{ "dagger","9"},
			{ "spear","3"},
			{ "staff","10"},
			{ "mace","4"},
			{ "axe","5"},
			{ "flail","6"},
			{ "whip","7"},
			{ "polearm","8"},
			{ "bow","0"},
			{ "arrow","3"},
			{ "lance","3"}
		};
		str1=str1.toLowerCase().trim();
		if(str1.startsWith("'")) str1=str1.substring(1);
		for(int wt=0;wt<weaponTypes.length;wt++)
			if(str1.startsWith(weaponTypes[wt][0]))
			{ val1=Util.s_int(weaponTypes[wt][1]); break;}
		if(val1==0)
			for(int wt=0;wt<weaponTypes.length;wt++)
				if(name.toLowerCase().indexOf(weaponTypes[wt][0])>=0)
				{ val1=Util.s_int(weaponTypes[wt][1]); break;}
		if((name.toUpperCase().endsWith("HAMMER"))&&(val1==4))
			val1=11;
		switch(val1)
		{
		case 0: I.setWeaponClassification(Weapon.CLASS_RANGED);
				if(name.toUpperCase().indexOf("BOW")>=0)
				{
					I.setAmmoCapacity(20);
					I.setAmmoRemaining(20);
					I.setAmmunitionType("arrows");
					I.setRanges(1,3);
					I.setRawLogicalAnd(true);
				}
				break;
		case 1: I.setWeaponClassification(Weapon.CLASS_SWORD); break;
		case 2: I.setWeaponClassification(Weapon.CLASS_EDGED); break;
		case 3: I.setWeaponClassification(Weapon.CLASS_POLEARM);
				I.setRanges(0,1);
				I.setRawLogicalAnd(true);
				break;
		case 4: I.setWeaponClassification(Weapon.CLASS_BLUNT); break;
		case 5: I.setWeaponClassification(Weapon.CLASS_AXE); break;
		case 6: I.setWeaponClassification(Weapon.CLASS_FLAILED);
				I.setRanges(0,1);
				break;
		case 7: I.setWeaponClassification(Weapon.CLASS_FLAILED);
				I.setRanges(0,1);
				break;
		case 8: I.setWeaponClassification(Weapon.CLASS_POLEARM);
				I.setRanges(0,1);
				I.setRawLogicalAnd(true);
				break;
		case 9: I.setWeaponClassification(Weapon.CLASS_DAGGER); break;
		case 10: I.setWeaponClassification(Weapon.CLASS_STAFF); break;
		case 11: I.setWeaponClassification(Weapon.CLASS_HAMMER); break;
		}
		if(val2>=1)
			I.baseEnvStats().setDamage(val2*val3);
		else
			I.baseEnvStats().setDamage(val3);
		if((str4.trim().length()>0)&&((Character.isLetter(str4.trim().charAt(0)))||(str4.trim().startsWith("'"))))
		{
			str4=str4.toUpperCase().trim();
			if(str4.startsWith("'"))
			   str4=str4.substring(1);
			if(str4.startsWith("POUND")) val4=7;
			else
			if(str4.startsWith("CRUSH")) val4=7;
			else
			if(str4.startsWith("SMASH")) val4=7;
			else
			if(str4.startsWith("FLAMI")) val4=57;
			else
			if(str4.startsWith("SCORC")) val4=57;
			else
			if(str4.startsWith("SEARI")) val4=57;
			else
			if(str4.startsWith("GOUT")) val4=57;
			else
			if(str4.startsWith("SCRATCH")) val4=22;
			else
			if(str4.startsWith("CLAW")) val4=22;
			else
			if(str4.startsWith("BITE")) val4=22;
			else
			if(str4.startsWith("PECK")) val4=22;
			else
			if(str4.startsWith("STING")) val4=22;
			else
			if(str4.startsWith("BEAT")) val4=22;
			else
			if(str4.startsWith("SLAP")) val4=22;
			else
			if(str4.startsWith("PUNC")) val4=22;
			else
			if(str4.startsWith("WHALL")) val4=22;
			else
			if(str4.startsWith("STAB")) val4=2;
			else
			if(str4.startsWith("PIERCE")) val4=2;
			else
			if(str4.startsWith("CHOP")) val4=25;
			else
			if(str4.startsWith("CLEA")) val4=25;
			else
			if(str4.startsWith("SLIC")) val4=25;
			else
			if(str4.startsWith("SLAS")) val4=25;
			else
			if(str4.startsWith("WHIP")) val4=25;
		}
		switch(val4)
		{
		case 7:
		case 8:
		case 27:
				I.setWeaponType(Weapon.TYPE_BASHING); break;
		case 29:
		case 55:
		case 56:
		case 57:
				I.setWeaponType(Weapon.TYPE_BURNING); break;
		case 22:
		case 5:
		case 10:
		case 23:
		case 26:
		case 32:
		case 13:
		case 16:
		case 17:
		case 24:
				I.setWeaponType(Weapon.TYPE_NATURAL); break;
		case 2:
		case 11:
				I.setWeaponType(Weapon.TYPE_PIERCING); break;
		case 25:
		case 21:
		case 4:
		case 3:
		case 1:
				I.setWeaponType(Weapon.TYPE_SLASHING); break;

		default: I.setWeaponType(Weapon.TYPE_BURSTING); break;
		}
	}

	public static int getDRoll(String str)
	{
		int i=str.indexOf("d");
		if(i<0) return 11;
		int roll=Util.s_int(str.substring(0,i).trim());
		str=str.substring(i+1).trim();

		i=str.indexOf("+");
		int dice=0;
		int plus=0;
		if(i<0)
		{
			i=str.indexOf("-");
			if(i<0)
				dice=Util.s_int(str.trim());
			else
			{
				dice=Util.s_int(str.substring(0,i).trim());
				plus=Util.s_int(str.substring(i));
			}
		}
		else
		{
			dice=Util.s_int(str.substring(0,i).trim());
			plus=Util.s_int(str.substring(i+1));
		}
		return (roll*dice)+plus;
	}

	private static MOB getMOB(String OfThisID,
					  Room putInRoom,
					  MOB mob,
					  Vector mobData,
					  Vector mobProgData,
					  Vector specialData,
					  Vector shopData,
					  Hashtable doneMOBS,
					  String areaFileName)
	{
		if(OfThisID.startsWith("#"))
		{
			if(doneMOBS.containsKey(OfThisID.substring(1)))
			{
				MOB M=(MOB)((MOB)doneMOBS.get(OfThisID.substring(1))).copyOf();
				M.setStartRoom(putInRoom);
				M.setLocation(putInRoom);
				return M;
			}
		}
		else
		{
			if(doneMOBS.containsKey(OfThisID))
			{
				MOB M=(MOB)((MOB)doneMOBS.get(OfThisID)).copyOf();
				M.setStartRoom(putInRoom);
				M.setLocation(putInRoom);
				return M;
			}
		}


		for(int m=0;m<mobData.size();m++)
		{
			Vector objV=null;
			if(mobData.elementAt(m) instanceof Vector)
				objV=(Vector)mobData.elementAt(m);
			else
			if(mobData.elementAt(m) instanceof String)
			{
				String s=(String)mobData.elementAt(m);
				if((!s.toUpperCase().trim().startsWith("#MOB"))&&(s.length()>0))
					returnAnError(mob,"Eating mob immaterial line: "+mobData.elementAt(m));
				continue;
			}
			else
				continue;

			String mobID=eatNextLine(objV);


			if(!mobID.equals(OfThisID))
				continue;

			String simpleName=CoffeeFilter.safetyFilter(eatLineSquiggle(objV));
			String mobName=CoffeeFilter.safetyFilter(eatLineSquiggle(objV));
			String mobDisplay=CoffeeFilter.safetyFilter(eatLineSquiggle(objV));
			String mobDescription=CoffeeFilter.safetyFilter(eatLineSquiggle(objV));
			Race R=null;
			boolean circleFormat=false;
			if(nextLine(objV).endsWith("~"))
			{
				String raceName=eatLineSquiggle(objV);
				R=CMClass.getRace(raceName);
				circleFormat=true;
			}
			if(R==null)
				R=CMClass.getRace("StdRace");

			String codeStr1=eatNextLine(objV);
			String codeStr2=eatNextLine(objV);
			String codeStr3=eatNextLine(objV);
			String codeStr4=eatNextLine(objV);
			String codeStr5="";
			if(circleFormat)
			{
				codeStr3=codeStr4;
				codeStr4=eatNextLine(objV);
				codeStr5=eatNextLine(objV);
			}


			if((!mobID.startsWith("#"))
			||((mobName.length()==0)
			&&((Util.numBits(codeStr1)<3)
			||(Util.numBits(codeStr1)>4)
			||(Util.numBits(codeStr2)<2)
			||(Util.numBits(codeStr3)<2))))
			{
				returnAnError(mob,"Malformed mob! Aborting this mob "+mobID+", display="+mobDisplay+", simple="+simpleName+", name="+mobName+", codeStr1="+codeStr1+", codeStr2="+codeStr2+", codeStr3="+codeStr3+"!");
				continue;
			}
			if(mobName.length()==0)
				mobName="Unknown";
			int actFlag=getBitMask(codeStr1,0);
			int affFlag=getBitMask(codeStr1,1);
			int aliFlag=Util.s_int(Util.getBit(codeStr1,2));
			MOB M=CMClass.getMOB("GenMob");
			String checkName=mobName.trim().toUpperCase();
			if(Util.isSet(actFlag,14)
			||(checkName.indexOf("GHOUL")>=0)
			||(checkName.indexOf("GHAST")>=0)
			||(checkName.indexOf("SKELETON")>=0)
			||(checkName.indexOf("ZOMBIE")>=0)
			||(checkName.indexOf("VAMPIRE")>=0)
			||(checkName.indexOf("LICH")>=0)
			||(checkName.indexOf("MUMMY")>=0)
			||(checkName.indexOf("GHOST")>=0)
			||(checkName.indexOf("GEIST")>=0))
				M=CMClass.getMOB("GenUndead");
			else
			if(simpleName.toUpperCase().indexOf("HORSE")>=0)
				M=CMClass.getMOB("GenRideable");
			for(int i=0;i<shopData.size();i++)
			{
				String s=((String)shopData.elementAt(i)).trim();
				if(("#"+s).startsWith(OfThisID+" ")||("#"+s).startsWith(OfThisID+"\t")||("#"+s).startsWith(OfThisID+"	"))
				{
					M=CMClass.getMOB("GenShopkeeper");
					int i1=Util.s_int(Util.getBit(s,1));
					int i2=Util.s_int(Util.getBit(s,2));
					int i3=Util.s_int(Util.getBit(s,3));
					//int i4=Util.s_int(Util.getBit(s,4));
					int whatIsell=ShopKeeper.DEAL_INVENTORYONLY;
					if((i1>4)&&(i1<8)&&(i2>4)&&(i2<8)&&(i3>4)&&(i3<8))
						whatIsell=ShopKeeper.DEAL_WEAPONS;
					else
					if((((i1>1)&&(i1<5))||(i1==10)||(i1==26))
					&&(((i2>1)&&(i2<5))||(i2==10)||(i2==26))
					&&(((i3>1)&&(i3<5))||(i3==10)||(i3==26)))
						whatIsell=ShopKeeper.DEAL_MAGIC;
					else
					if(((i1==9)||(i1==0))&&((i2==9)||(i2==0))&&((i3==9)||(i3==0)))
						whatIsell=ShopKeeper.DEAL_ARMOR;
					else
					if(mobName.toUpperCase().indexOf("LEATHER")>=0)
						whatIsell=ShopKeeper.DEAL_LEATHER;
					else
					if((mobName.toUpperCase().indexOf("PET ")>=0)||(mobName.toUpperCase().indexOf("PETS ")>=0))
						whatIsell=ShopKeeper.DEAL_PETS;
					((ShopKeeper)M).setWhatIsSold(whatIsell);
					break;
				}
			}
			M.setName(mobName);
			M.setDisplayText(mobDisplay);
			if(!mobDescription.trim().equalsIgnoreCase("OLDSTYLE"))
				M.setDescription(mobDescription);
			aliFlag=(int)Math.round(Util.div(aliFlag,2));
			M.setAlignment(500+aliFlag);
			M.setStartRoom(putInRoom);
			M.setLocation(putInRoom);
			M.baseCharStats().setMyRace(R);

			Behavior behavior=CMClass.getBehavior("Mobile");
			if(Util.isSet(actFlag,5))
				behavior=CMClass.getBehavior("MobileAggressive");
			//if(!Util.isSet(actFlag,6))
			//	behavior.setParms("WANDER");
			if(!Util.isSet(actFlag,1))
				M.addBehavior(behavior);
			if(Util.isSet(actFlag,2))
				M.addBehavior(CMClass.getBehavior("Scavenger"));
			if(Util.isSet(actFlag,4))
				M.addNonUninvokableEffect(CMClass.getAbility("Prop_Invisibility"));
			if(Util.isSet(actFlag,5)&&Util.isSet(actFlag,1))
				M.addBehavior(CMClass.getBehavior("Aggressive"));
			M.setWimpHitPoint(0);
			if(Util.isSet(actFlag,7)) // this needs to be adjusted further down!
				M.setWimpHitPoint(2);
			//if(Util.isSet(actFlag,8)) // not really supported properly
			//	M.addNonUninvokableEffect(CMClass.getAbility("Prop_SafePet"));

			if(Util.isSet(actFlag,9))
				M.addNonUninvokableEffect(CMClass.getAbility("Prop_StatTrainer"));
			if(Util.isSet(actFlag,10))
				M.addBehavior(CMClass.getBehavior("MOBTeacher"));

			if(Util.isSet(actFlag,11))
				M.addBehavior(CMClass.getBehavior("Fighterness"));
			if(Util.isSet(actFlag,12))
				M.addBehavior(CMClass.getBehavior("Mageness"));
			if(Util.isSet(actFlag,13))
				M.addBehavior(CMClass.getBehavior("Mageness"));

			if(Util.isSet(actFlag,14))
			{
				R=M.baseCharStats().getMyRace();
				if(R.ID().equals("Human")||R.ID().equals("StdRace"))
				{
					R=CMClass.getRace("Undead");
					M.baseCharStats().setMyRace(R);
				}
			}

			if(Util.isSet(actFlag,16))
				M.addBehavior(CMClass.getBehavior("Clericness"));
			if(Util.isSet(actFlag,17))
				M.addBehavior(CMClass.getBehavior("Mageness"));
			if(Util.isSet(actFlag,18))
				M.addBehavior(CMClass.getBehavior("Thiefness"));
			if(Util.isSet(actFlag,19))
				M.addBehavior(CMClass.getBehavior("Fighterness"));
			if(Util.isSet(actFlag,26))
				M.addBehavior(CMClass.getBehavior("Healer"));
			if(Util.isSet(actFlag,27))
				M.addBehavior(CMClass.getBehavior("MOBTeacher"));

			if(Util.isSet(affFlag,0))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_NOT_SEE);
			if(Util.isSet(affFlag,1))
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|EnvStats.IS_INVISIBLE);
			if(Util.isSet(affFlag,2))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_EVIL);
			if(Util.isSet(affFlag,3))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_INVISIBLE);
			if(Util.isSet(affFlag,4))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_BONUS);
			if(Util.isSet(affFlag,5))
			{
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_HIDDEN);
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_SNEAKERS);
			}
			if(Util.isSet(affFlag,6))
			{
				if(M.getAlignment()<350)
				   M.addNonUninvokableEffect(CMClass.getAbility("Prayer_UnholyWord"));
				else
				   M.addNonUninvokableEffect(CMClass.getAbility("Prayer_HolyWord"));
				M.addNonUninvokableEffect(CMClass.getAbility("Prayer_Sanctuary"));
			}
			else
			if(Util.isSet(affFlag,7))
				M.addNonUninvokableEffect(CMClass.getAbility("Prayer_Sanctuary"));

			if(Util.isSet(affFlag,8))
				M.addNonUninvokableEffect(CMClass.getAbility("Spell_FaerieFire"));
			if(Util.isSet(affFlag,9))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_INFRARED);
			if(Util.isSet(affFlag,10))
				M.addNonUninvokableEffect(CMClass.getAbility("Prayer_Curse"));
			if(Util.isSet(affFlag,11))
			{
				for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
				{
					Ability A=(Ability)a.nextElement();
					if(A.ID().startsWith("Specialization"))
						M.addNonUninvokableEffect((Ability)A.newInstance());
				}
			}

			//if(Util.isSet(affFlag,12)) really dumb
			//  M.addNonUninvokableEffect(new Poison());

			if(Util.isSet(affFlag,13))
				M.addNonUninvokableEffect(CMClass.getAbility("Prayer_ProtEvil"));

			if(Util.isSet(affFlag,14))
				M.addNonUninvokableEffect(CMClass.getAbility("Prayer_ProtGood"));

			if(Util.isSet(affFlag,15))
			{
				Ability A=(Ability)CMClass.getAbility("Thief_Sneak").copyOf();
				A.setProfficiency(100);
				M.addAbility(A);
			}

			if(Util.isSet(affFlag,16))
				M.addNonUninvokableEffect(CMClass.getAbility("Prop_Hidden"));

			if(Util.isSet(affFlag,17))
				M.addNonUninvokableEffect(CMClass.getAbility("Spell_Sleep"));

			if(Util.isSet(affFlag,18))
				M.addNonUninvokableEffect(CMClass.getAbility("Spell_Charm"));

			if(Util.isSet(affFlag,20))
				M.addNonUninvokableEffect(CMClass.getAbility("Spell_PassDoor"));

			if(Util.isSet(affFlag,21))
				M.addNonUninvokableEffect(CMClass.getAbility("Spell_Haste"));

			//if(Util.isSet(affFlag,22)) no effect anyway
			//	M.addNonUninvokableEffect(new Prayer_Calm());

			if(Util.isSet(affFlag,23))
				M.addNonUninvokableEffect(CMClass.getAbility("Prayer_Plague"));

			if(Util.isSet(affFlag,24))
				M.addNonUninvokableEffect(CMClass.getAbility("Spell_Weaken"));

			if(Util.isSet(affFlag,25))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);

			if(Util.isSet(affFlag,26))
				M.addNonUninvokableEffect(CMClass.getAbility("Fighter_Berzerk"));

			if(Util.isSet(affFlag,27))
				M.addAbility(CMClass.getAbility("Skill_Swim"));

			if(Util.isSet(affFlag,28))
				M.addNonUninvokableEffect(CMClass.getAbility("Regeneration"));

			if(Util.isSet(affFlag,29))
			{
				M.addNonUninvokableEffect(CMClass.getAbility("Regeneration"));
				M.addNonUninvokableEffect(CMClass.getAbility("Spell_Slow"));
			}

			// start ROM type
			int positionCode=8;
			int sexCode=1;
			if(Util.numBits(codeStr2)>=4)
			{
				M.baseEnvStats().setLevel(Util.s_int(Util.getCleanBit(codeStr2,0)));
				if(M.baseEnvStats().level()==0)
					M.baseEnvStats().setLevel(1);
				int baseHP=11;
				if(circleFormat)
					baseHP=getDRoll(Util.getCleanBit(codeStr2,2));
				else
					baseHP=getDRoll(Util.getCleanBit(codeStr2,3));
				baseHP=baseHP-10;
				baseHP=baseHP-((int)Math.round(Util.mul(M.baseEnvStats().level()*M.baseEnvStats().level(),0.85)));
				baseHP=baseHP/M.baseEnvStats().level();
				M.baseEnvStats().setAbility(baseHP);

				if(circleFormat)
				{
					if(Util.isNumber(Util.getCleanBit(codeStr4,2)))
						sexCode=Util.s_int(Util.getCleanBit(codeStr4,2));
					else
					if(Util.getCleanBit(codeStr4,2).toUpperCase().equals("MALE"))
						sexCode=1;
					else
					if(Util.getCleanBit(codeStr4,2).toUpperCase().equals("FEMALE"))
						sexCode=2;
					else
					if(Util.getCleanBit(codeStr4,2).toUpperCase().equals("EITHER"))
						sexCode=(Dice.rollPercentage()>50)?1:2;
					else
						sexCode=3;

					if(Util.isNumber(Util.getCleanBit(codeStr4,0)))
						positionCode=Util.s_int(Util.getCleanBit(codeStr4,2));
					else
					if(Util.getCleanBit(codeStr4,0).trim().startsWith("STAND"))
						positionCode=8;
					else
					if(Util.getCleanBit(codeStr4,0).trim().startsWith("SIT"))
						positionCode=5;
					else
					if(Util.getCleanBit(codeStr4,0).trim().startsWith("SLEEP"))
						positionCode=1;

				}
				else
				{
					positionCode=Util.s_int(Util.getCleanBit(codeStr4,0));
					sexCode=Util.s_int(Util.getCleanBit(codeStr4,2));
				}
				if(Dice.rollPercentage()>75)
					M.addBehavior(CMClass.getBehavior("MudChat"));
			}
			else
			{
				M.baseEnvStats().setAbility(11);
				int baseLevel=Util.s_int(Util.getCleanBit(codeStr2,0));
				while(baseLevel>25)
					baseLevel=(int)Math.round(Util.div(baseLevel,2.0));
			}

			if(M.baseEnvStats().level()==0)
				M.baseEnvStats().setLevel(1);
			if(M.getWimpHitPoint()==2)
				M.setWimpHitPoint(((int)Math.round(Util.div(M.baseEnvStats().level()*(11+M.baseEnvStats().ability()),8.0)))+1);

			M.baseEnvStats().setArmor(CMClass.getCharClass("StdCharClass").getLevelArmor(M));
			M.baseEnvStats().setAttackAdjustment(CMClass.getCharClass("StdCharClass").getLevelAttack(M));
			M.baseEnvStats().setDamage(CMClass.getCharClass("StdCharClass").getLevelDamage(M));
			if(circleFormat)
				M.setMoney(Util.s_int(Util.getCleanBit(codeStr4,3)));
			else
				M.setMoney(Dice.roll(1,M.baseEnvStats().level(),0)+Dice.roll(1,10,0));
			M.baseEnvStats().setWeight(50);

			switch(positionCode)
			{
			case 1:
			case 2:
			case 3:
			case 4:
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|EnvStats.IS_SLEEPING);
				break;
			case 5:
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|EnvStats.IS_SITTING);
				break;
			case 6:
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|EnvStats.IS_SITTING);
				break;
			}

			M.baseCharStats().setStat(CharStats.GENDER,'M');
			switch(sexCode)
			{
			case 2: M.baseCharStats().setStat(CharStats.GENDER,'F'); break;
			case 3: M.baseCharStats().setStat(CharStats.GENDER,'N'); break;
			}

			if(circleFormat)
			{
				int off=getBitMask(codeStr3,0);
				int imm=getBitMask(codeStr3,1);
				int res=getBitMask(codeStr3,2);
				int size=Util.s_int(Util.getCleanBit(codeStr5,2));
				switch(size)
				{
				case 0: M.baseEnvStats().setWeight(1); break;
				case 1: M.baseEnvStats().setWeight(15); break;
				case 2: M.baseEnvStats().setWeight(150); break;
				case 3: M.baseEnvStats().setWeight(350); break;
				case 4: M.baseEnvStats().setWeight(850); break;
				case 5: M.baseEnvStats().setWeight(2000); break;
				}
				// ignore the above, coffeemud does it better!
				int numAbiles=M.numLearnedAbilities();
				M.baseCharStats().getMyRace().startRacing(M,false);
				//if(Util.isSet(off,0)) // no area killers in coffeemud
				//if(Util.isSet(off,1)) // no circling in coffeemud

				if(Util.isSet(off,2)) // bash them off their feet?
					M.addAbility(CMClass.getAbility("Skill_Trip"));
				if(Util.isSet(off,3))
					M.addAbility(CMClass.getAbility("Fighter_Berzerk"));
				if(Util.isSet(off,4))
					M.addAbility(CMClass.getAbility("Skill_Disarm"));
				if(Util.isSet(off,5))
					M.addAbility(CMClass.getAbility("Skill_Dodge"));
				//if(Util.isSet(off,6)) is missing
				if(Util.isSet(off,7))
					M.baseEnvStats().setSpeed(M.baseEnvStats().speed()+1);
				if(Util.isSet(off,8))
					M.addAbility(CMClass.getAbility("Fighter_Kick"));
				if(Util.isSet(off,9))
					M.addAbility(CMClass.getAbility("Skill_Dirt"));
				if(Util.isSet(off,10))
					M.addAbility(CMClass.getAbility("Skill_Parry"));
				//if(Util.isSet(off,11)) rescue is irrelevant
				//if(Util.isSet(off,12)) is missing
				if(Util.isSet(off,13))
					M.addAbility(CMClass.getAbility("Skill_Trip"));
				if(Util.isSet(off,14))
					M.addAbility(CMClass.getAbility("Fighter_Whomp"));
				if(Util.isSet(off,15))
					M.addBehavior(CMClass.getBehavior("MOBHelper"));
				if(Util.isSet(off,16))
					M.addBehavior(CMClass.getBehavior("AlignHelper"));
				if(Util.isSet(off,17))
					M.addBehavior(CMClass.getBehavior("RaceHelper"));
				if(Util.isSet(off,18))
					M.addBehavior(CMClass.getBehavior("PlayerHelper"));
				if(Util.isSet(off,19))
				{
					Behavior guardian=CMClass.getBehavior("GoodGuardian");
					for(int b=M.numBehaviors()-1;b>=0;b--)
					{
						Behavior B=M.fetchBehavior(b);
						if((B!=null)&&(Util.bset(B.flags(),Behavior.FLAG_MOBILITY)))
						{
							if(guardian.ID().equals("GoodGuardian"))
								guardian=CMClass.getBehavior("MobileGoodGuardian");
							if(B.getParms().length()>0)
								guardian.setParms(B.getParms());
							M.delBehavior(B);
						}
					}
					M.addBehavior(guardian);
					M.addBehavior(CMClass.getBehavior("AntiVagrant"));
				}
				if(Util.isSet(off,20))
					M.addBehavior(CMClass.getBehavior("BrotherHelper"));
				//if(Util.isSet(off,21)) is missing
				if(Util.isSet(off,22))
					M.addAbility(CMClass.getAbility("Fighter_Sweep"));

				if((M.numAbilities())>numAbiles)
					M.addBehavior(CMClass.getBehavior("CombatAbilities"));

				Ability resist=CMClass.getAbility("Prop_Resistance");
				if((Util.isSet(res,0))||(Util.isSet(imm,0)))
					resist.setMiscText(resist.text()+" teleport");
				if((Util.isSet(res,1))||(Util.isSet(imm,1)))
					resist.setMiscText(resist.text()+" mind");
				if((Util.isSet(res,2))||(Util.isSet(imm,2)))
					resist.setMiscText(resist.text()+" magic");
				if((Util.isSet(res,3))||(Util.isSet(imm,3)))
					resist.setMiscText(resist.text()+" weapons");
				if((Util.isSet(res,4))||(Util.isSet(imm,4)))
					resist.setMiscText(resist.text()+" blunt");
				if((Util.isSet(res,5))||(Util.isSet(imm,5)))
					resist.setMiscText(resist.text()+" pierce");
				if((Util.isSet(res,6))||(Util.isSet(imm,6)))
					resist.setMiscText(resist.text()+" slash");
				if((Util.isSet(res,7))||(Util.isSet(imm,7)))
					resist.setMiscText(resist.text()+" fire");
				if((Util.isSet(res,8))||(Util.isSet(imm,8)))
					resist.setMiscText(resist.text()+" cold");
				if((Util.isSet(res,9))||(Util.isSet(imm,9)))
					resist.setMiscText(resist.text()+" elec");
				if((Util.isSet(res,10))||(Util.isSet(imm,10)))
					resist.setMiscText(resist.text()+" acid");
				if((Util.isSet(res,11))||(Util.isSet(imm,11)))
					resist.setMiscText(resist.text()+" poison");
				if((Util.isSet(res,12))||(Util.isSet(imm,12)))
					resist.setMiscText(resist.text()+" evil");
				if((Util.isSet(res,13))||(Util.isSet(imm,13)))
					resist.setMiscText(resist.text()+" holy");
				if(Util.isSet(res,14))
					M.addNonUninvokableEffect(CMClass.getAbility("Spell_ResistMagicMissiles"));
				if((Util.isSet(res,15))||(Util.isSet(imm,15)))
					resist.setMiscText(resist.text()+" mind");
				if((Util.isSet(res,16))||(Util.isSet(imm,16)))
					resist.setMiscText(resist.text()+" disease");
				if((Util.isSet(res,17))||(Util.isSet(imm,17)))
					resist.setMiscText(resist.text()+" water gas");
				//if(Util.isSet(res,18)) no light resistance
				//if(Util.isSet(res,18)) no sound resistance
				if(resist.text().length()>0)
				{
					resist.setMiscText(resist.text()+" "+(10+M.baseEnvStats().level())+"%");
					M.addNonUninvokableEffect(resist);
				}
			}

			String scriptStuff="";
			while(objV.size()>0)
			{
				String s=nextLine(objV);
				if(s.startsWith(">"))
				{
					s=eatLineSquiggle(objV);
					if(!s.substring(1).trim().toUpperCase().startsWith("IN_FILE_PROG"))
					{
						scriptStuff+=s.substring(1).trim()+";";
						s=nextLine(objV);
						while(s.indexOf("~")<0)
						{
							scriptStuff+=s.trim()+";";
							eatLine(objV);
							s=nextLine(objV);
						}
						s=eatLineSquiggle(objV).trim();
						scriptStuff+=s+"~";
					}
				}
				else
				if(s.startsWith("X "))
				{
					String codeLine=eatLineSquiggle(objV);
					Behavior B=M.fetchBehavior("Sounder");
					if(B==null)
					{
						B=CMClass.getBehavior("Sounder");
						if(B!=null) M.addBehavior(B);
					}
					if(B.getParms().length()==0)
						B.setParms(codeLine.substring(1).trim());
					else
						B.setParms(B.getParms()+";"+codeLine.substring(1).trim());
				}
				else
				if(s.startsWith("|"))
				{
					eatNextLine(objV);
					// just eat and go.. its an end of mob marker probably
				}
				else
					eatNextLine(objV);
			}
			for(int mp=0;mp<mobProgData.size();mp++)
			{
				String s=(String)mobProgData.elementAt(mp);
				String rest=null;
				if(s.startsWith("#")&&(s.length()>1)&&(Util.isNumber(""+s.charAt(1))))
				{
					s="M "+s.substring(1);
					rest="";
					while(s.indexOf("~")<=0)
					{
						mp++;
						if(mp<mobProgData.size())
						{
							rest+=(String)mobProgData.elementAt(mp);
							s=(String)mobProgData.elementAt(mp);
						}
						else
							break;
					}
				}
				if(s.startsWith("M "))
				{
					String MOBID=Util.getBit(s,1);
					if(!("#"+MOBID).equals(OfThisID))
						continue;

					String mobprg=Util.getBit(s,2).toUpperCase().trim();
					if(mobprg.equals("JANITOR.PRG"))
						M.addBehavior(CMClass.getBehavior("Scavenger"));
					else
					if(mobprg.equals("VAGABOND.PRG"))
						M.addBehavior(CMClass.getBehavior("Vagrant"));
					else
					if(mobprg.equals("DRUNK.PRG"))
					{
						if(M.fetchEffect("Inebriation")==null)
							M.addNonUninvokableEffect(CMClass.getAbility("Inebriation"));
					}
					else
					if(mobprg.equals("MID_CIT.PRG"))
					{
						//
					}
					else
					if(mobprg.equals("BEGGAR.PRG"))
						M.addBehavior(CMClass.getBehavior("Beggar"));
					else
					if(mobprg.equals("GATEGRD.PRG"))
						M.addBehavior(CMClass.getBehavior("GateGuard"));
					else
					if(mobprg.equals("GATEGRD2.PRG"))
						M.addBehavior(CMClass.getBehavior("GateGuard"));
					else
					if(mobprg.equals("CRIER.PRG"))
					{
					}
					else
					if(rest!=null)
						continue;
					else
					{
						try{
							File F2=new File(areaFileName);
							if((F2.exists())&&(!F2.isDirectory()))
							{
								int x=F2.getAbsolutePath().lastIndexOf(File.separatorChar);
								String path=F2.getAbsolutePath().substring(0,x)+File.separatorChar+mobprg;
								StringBuffer buf=Resources.getFile(path);
								if((buf==null)||(buf.length()==0))
									returnAnError(mob,"Unknown MobPrg: "+mobprg);
								else
								{
									Vector V=Resources.getFileLineVector(buf);
									while(V.size()>0)
									{
										s=nextLine(V);
										if(s.startsWith(">"))
										{
											s=eatLineSquiggle(V).substring(1).trim();
											scriptStuff+=s+";";
											s=nextLine(V);
											while(s.indexOf("~")<0)
											{
												scriptStuff+=s+";";
												eatLine(V);
												s=nextLine(V);
											}
											s=eatLineSquiggle(V).trim();
											scriptStuff+=s+"~";
										}
										else
											eatLine(V);
									}
								}
							}
						}catch(Exception e){
							returnAnError(mob,"Unknown MobPrg: "+mobprg);
						}
					}
				}
				else
				if(s.startsWith("O "))
				{
					// also unsupported
				}
				else
				if((s.startsWith("#M"))||(s.startsWith("S")))
				{
				}
				else
				if(s.trim().length()>0)
					returnAnError(mob,"MobPrg line: "+s);
			}
			if(scriptStuff.length()>0)
			{
				Behavior S=CMClass.getBehavior("Scriptable");
				S.setParms(scriptStuff);
				M.addBehavior(S);
			}

			for(int mp=0;mp<specialData.size();mp++)
			{
				String s=(String)specialData.elementAt(mp);
				if(s.startsWith("M "))
				{
					String MOBID=Util.getBit(s,1);
					if(!("#"+MOBID).equals(OfThisID))
						continue;

					String special=Util.getBit(s,2).toUpperCase().trim();
					if((special.equals("SPEC_CAST_MAGE"))
					||(special.equals("SPEC_WANDERER")))
						M.addBehavior(CMClass.getBehavior("Mageness"));
					else
					if(special.equals("SPEC_CAST_SENESCHAL"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Spell_Blindness"));
						M.addAbility(CMClass.getAbility("Spell_DispelMagic"));
						M.addAbility(CMClass.getAbility("Spell_Weaken"));
						M.addAbility(CMClass.getAbility("Spell_AcidArrow"));
						M.addAbility(CMClass.getAbility("Spell_Fireball"));
						M.addAbility(CMClass.getAbility("Spell_AcidFog"));
						M.addAbility(CMClass.getAbility("Spell_Lightning"));
						M.addAbility(CMClass.getAbility("Undead_WeakEnergyDrain"));
						M.addAbility(CMClass.getAbility("Prayer_Plague"));
					}
					else
					if(special.equals("SPEC_CAST_BEHOLDER"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Spell_Spook"));
						M.addAbility(CMClass.getAbility("Spell_Slow"));
						M.addAbility(CMClass.getAbility("Prayer_Harm"));
						M.addAbility(CMClass.getAbility("Prayer_CauseCritical"));
						M.addAbility(CMClass.getAbility("Prayer_CauseSerious"));
						M.addAbility(CMClass.getAbility("Spell_DispelMagic"));

					}
					else
					if(special.equals("SPEC_CAST_PSIONICIST"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Spell_Spook"));
						M.addAbility(CMClass.getAbility("Spell_Slow"));
						M.addAbility(CMClass.getAbility("Spell_DispelMagic"));
						M.addAbility(CMClass.getAbility("Undead_WeakEnergyDrain"));
						M.addAbility(CMClass.getAbility("WeakParalysis"));
					}
					else
					if((special.equals("SPEC_CAST_GHOST"))
					||(special.equals("SPEC_UNDEAD")))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Spell_Spook"));
						M.addAbility(CMClass.getAbility("Prayer_Curse"));
						M.addAbility(CMClass.getAbility("Prayer_Blindness"));
						M.addAbility(CMClass.getAbility("Prayer_Harm"));
						M.addAbility(CMClass.getAbility("Prayer_Poison"));
						M.addAbility(CMClass.getAbility("WeakParalysis"));
					}
					else
					if(special.equals("SPEC_THIEF"))
						M.addBehavior(CMClass.getBehavior("Thiefness"));
					else
					if(special.equals("SPEC_HEALER"))
						M.addBehavior(CMClass.getBehavior("Healer"));
					else
					if(special.equals("SPEC_REPAIRMAN"))
						M.addBehavior(CMClass.getBehavior("ItemMender"));
					else
					if((special.equals("SPEC_SUMMON_LIGHT"))
					||(special.equals("SPEC_SUMMON_DEMON")))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Prayer_SummonElemental"));
					}
					else
					if(special.equals("SPEC_EXECUTIONER"))
						M.addBehavior(CMClass.getBehavior("GoodExecutioner"));
					else
					if(special.startsWith("SPEC_ASSASSIN"))
					{
						Behavior B=M.fetchBehavior("Aggressive");
						if(B==null)B=M.fetchBehavior("MobileAggressive");
						if(B==null)B=M.fetchBehavior("VeryAggressive");
						if(B==null)B=CMClass.getBehavior("Aggressive");
						B.setParms(B.getParms()+" MOBKILLER ");
						M.addBehavior(B);
					}
					else
					if(special.equals("SPEC_CAST_ADEPT"))
						M.addBehavior(CMClass.getBehavior("Healer"));
					else
					if(special.equals("SPEC_CAST_CLERIC"))
						M.addBehavior(CMClass.getBehavior("Clericness"));
					else
					if(special.equals("SPEC_NASTY"))
						M.addBehavior(CMClass.getBehavior("FightFlee"));
					else
					if(special.equals("SPEC_DARK_MAGIC"))
					{
						M.addNonUninvokableEffect(CMClass.getAbility("Spell_SpellTurning"));
						M.addAbility(CMClass.getAbility("Prayer_Heal"));
					}
					else
					if(special.equals("SPEC_SMART"))
						M.addBehavior(CMClass.getBehavior("Scavenger"));
					else
					if(special.equals("SPEC_CAST_UNDEAD"))
					{
						M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|EnvStats.IS_GOLEM);
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Undead_ColdTouch"));
						M.addAbility(CMClass.getAbility("Undead_LifeDrain"));
						M.baseCharStats().setMyRace(CMClass.getRace("Undead"));
						M.baseCharStats().getMyRace().startRacing(M,false);
					}
					else
					if((special.equals("SPEC_GUARD"))
					||(special.equals("SPEC_POLICEMAN"))
					||(special.equals("SPEC_SPECIAL_GUARD")))
						M.addBehavior(CMClass.getBehavior("GoodGuardian"));
					else
					if(special.equals("SPEC_FIDO"))
						M.addBehavior(CMClass.getBehavior("CorpseEater"));
					else
					if((special.equals("SPEC_MAYOR"))
					||(special.equals("SPEC_CAPTAIN")))
						M.addBehavior(CMClass.getBehavior("MudChat"));
					else
					if(special.equals("SPEC_JANITOR"))
						M.addBehavior(CMClass.getBehavior("Scavenger"));
					else
					if(special.equals("SPEC_BREATH_ANY"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Dragonbreath"));
					}
					else
					if(special.equals("SPEC_CAST_CADAVER"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Prayer_Bury"));
					}
					else
					if((special.equals("SPEC_REWIELD"))
					||(special.equals("SPEC_WIZARDOFOZ"))
					||(special.equals("SPEC_VAMP_HUNTER"))
					||(special.equals("SPEC_MINO_GUARD")))
					{
						// who knows?
					}
					else
					if(special.equals("SPEC_TAX_MAN"))
					{
						M.addBehavior(CMClass.getBehavior("RandomTeleporter"));
						M.addBehavior(CMClass.getBehavior("Thiefness"));
					}
					else
					if(special.equals("SPEC_STEPHEN"))
					{
						M.addBehavior(CMClass.getBehavior("RandomTeleporter"));
						M.addBehavior(CMClass.getBehavior("Healer"));
					}
					else
					if(special.equals("SPEC_CAST_BIGTIME"))
					{
						// In CoffeeMud, they all cast bigtime
					}
					else
					if(special.equals("SPEC_BREATH_ACID"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Acidbreath"));
					}
					else
					if(special.equals("SPEC_CAST_JUDGE"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Skill_Explosive"));
					}
					else
					if(special.equals("SPEC_BREATH_FIRE"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Firebreath"));
					}
					else
					if(special.equals("SPEC_BREATH_FROST"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Frostbreath"));
					}
					else
					if(special.equals("SPEC_BREATH_GAS"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Gasbreath"));
					}
					else
					if(special.equals("SPEC_BREATH_LIGHTNING"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Lighteningbreath"));
					}
					else
					if(special.equals("SPEC_POISON"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Poison"));
					}
					else
					if(special.equals("SPEC_OGRE_MEMBER"))
					{
						Behavior B=CMClass.getBehavior("ROMGangMember");
						B.setParms("Ogre");
						M.addBehavior(B);
					}
					else
					if(special.equals("SPEC_TROLL_MEMBER"))
					{
						Behavior B=CMClass.getBehavior("ROMGangMember");
						B.setParms("Troll");
						M.addBehavior(B);
					}
					else
					if(special.equals("SPEC_PATROLMAN"))
						M.addBehavior(CMClass.getBehavior("ROMPatrolman"));
					else
						returnAnError(mob,"Unknown mob special: "+special);
				}
				else
				if((s.startsWith("#SPE"))||(s.startsWith("S"))||(s.startsWith("*")||(s.startsWith("#$"))))
				{
				}
				else
				if(s.trim().length()>0)
					returnAnError(mob,"Unknown mob special line: "+s);
			}
			for(int a=0;a<M.numLearnedAbilities();a++)
			{
				Ability A=M.fetchAbility(a);
				if(A!=null)
					A.autoInvocation(M);
			}
			long rejuv=MudHost.TICKS_PER_RLMIN+MudHost.TICKS_PER_RLMIN+(MudHost.TICKS_PER_RLMIN*M.baseEnvStats().level()/2);
			if(rejuv>(30*MudHost.TICKS_PER_RLMIN)) rejuv=(30*MudHost.TICKS_PER_RLMIN);
			M.baseEnvStats().setRejuv((int)rejuv);
			if(M.displayText().toUpperCase().indexOf("MONEY CHANGER")>=0)
				M.addBehavior(CMClass.getBehavior("MoneyChanger"));
			M.recoverCharStats();
			M.recoverEnvStats();
			M.recoverMaxState();
			M.resetToMaxState();
			M.text();
			if(OfThisID.startsWith("#"))
				doneMOBS.put(OfThisID.substring(1),M.copyOf());
			else
				doneMOBS.put(OfThisID,M.copyOf());
			return M;
		}
		return null;
	}

	private static Item getItem(String OfThisID,
						 MOB mob,
						 String areaName,
						 Vector objectData,
						 Vector objProgData,
						 Hashtable doneItems,
						 Hashtable doneRooms)
	{
		if(OfThisID.startsWith("#"))
		{
			if(doneItems.containsKey(OfThisID.substring(1)))
				return (Item)((Item)doneItems.get(OfThisID.substring(1))).copyOf();
		}
		else
		{
			if(doneItems.containsKey(OfThisID))
				return (Item)((Item)doneItems.get(OfThisID)).copyOf();
		}
		for(int o=0;o<objectData.size();o++)
		{
			Vector objV=null;
			if(objectData.elementAt(o) instanceof Vector)
				objV=(Vector)objectData.elementAt(o);
			else
			if(objectData.elementAt(o) instanceof String)
			{
				String s=(String)objectData.elementAt(o);
				if((!s.toUpperCase().trim().startsWith("#OBJ"))&&(s.length()>0))
					returnAnError(mob,"Eating immaterial line: "+objectData.elementAt(o)+", area="+areaName);
				continue;
			}
			else
				continue;
			String objectID=eatNextLine(objV);

			if(!objectID.equals(OfThisID))
				continue;

			String simpleName=CoffeeFilter.safetyFilter(eatLineSquiggle(objV));
			String objectName=CoffeeFilter.safetyFilter(eatLineSquiggle(objV));
			String objectDisplay=CoffeeFilter.safetyFilter(eatLineSquiggle(objV));
			String objectDescription="";
			if((nextLine(objV).indexOf("~")>=0)||((nextLine(objV).length()>0)&&(!Character.isDigit(nextLine(objV).charAt(0)))))
				objectDescription=CoffeeFilter.safetyFilter(eatLineSquiggle(objV));
			String codeStr1=eatNextLine(objV);
			String codeStr2=eatNextLine(objV);
			String codeStr3=eatNextLine(objV);

			if((!objectID.startsWith("#"))
			||((objectName.length()==0)
			&&((Util.numBits(codeStr1)<3)
			||(Util.numBits(codeStr1)>4)
			||(Util.numBits(codeStr2)<4)
			||(codeStr3.length()==0))))
			{
				returnAnError(mob,"Malformed object! Aborting this object "+objectID+", display="+objectDisplay+", simple="+simpleName+", name="+objectName+", codeStr1="+codeStr1+", codeStr2="+codeStr2+", codeStr3="+codeStr3+", area="+areaName);
				continue;
			}
			if(objectName.length()==0)
				objectName="Unknown";
			boolean circleForm=false;
			String obj=Util.getBit(codeStr1,0);
			if((obj.trim().length()>1)&&(Character.isLetter(obj.charAt(0))))
				circleForm=true;
			int objType=Util.s_int(obj);
			final String[][] itemTypes={
			{ "light","1"},
			{ "scroll","2"},
			{ "wand","3"},
			{ "staff","4"},
			{ "weapon","5"},
			{ "treasure","8"},
			{ "armor","9"},
			{ "potion","10"},
			{ "clothing","11"},
			{ "furniture","12"},
			{ "trash","13"},
			{ "container","15"},
			{ "drink","17"},
			{ "key","18"},
			{ "food","19"},
			{ "money","20"},
			{ "boat","22"},
			{ "npc_corpse","99"},
			{ "pc_corpse","99"},
			{ "fountain","25"},
			{ "pill","26"},
			{ "protect",""},
			{ "map","28"},
			{ "portal","97"},
			{ "warp_stone",""},
			{ "room_key","98"},
			{ "gem",""},
			{ "jewelry",""},
			{ "jukebox",""},
			{ "tattoo",""},
			{ "pipe","32"}
			};

			if(circleForm)
			{
				if(obj.equalsIgnoreCase("jukebox"))
					continue;// NO JUKE BOXES!
				for(int it=0;it<itemTypes.length;it++)
					if(obj.equalsIgnoreCase(itemTypes[it][0]))
					{
						objType=Util.s_int(itemTypes[it][1]);
						break;
					}
			}
			int extraFlag=getBitMask(codeStr1,1);
			int wearFlag=getBitMask(codeStr1,2);

			Ability adjuster=CMClass.getAbility("Prop_HaveAdjuster");
			switch(objType)
			{
			case 2:
			case 3:
			case 4:
			case 10:
				if((codeStr2.indexOf("`")<=0)
				&&(nextLine(objV).indexOf("`")>=0))
					codeStr2=eatNextLine(objV);
				break;
			default:
				break;
			}
			boolean forgiveZeroes=false;
			if((codeStr2.indexOf("~")>=0)&&(codeStr2.lastIndexOf("~")>codeStr2.indexOf("~")))
			{
				Vector V=Util.parseSquiggles(codeStr2);
				if(V.size()==4)
				{
					forgiveZeroes=true;
					codeStr2="'"+((String)V.elementAt(0))+"' "
						    +"'"+((String)V.elementAt(1))+"' "
						    +"'"+((String)V.elementAt(2))+"' "
						    +"'"+((String)V.elementAt(3))+"'";
				}
				else
					returnAnError(mob,"Invalid object codeStr2 line: "+codeStr2+", item not aborted, but stuff will be wrong!");

			}


			String str1=Util.getBit(codeStr2,0);
			String str2=Util.getBit(codeStr2,1);
			String str3=Util.getBit(codeStr2,2);
			String str4=Util.getBit(codeStr2,3);
			int val1=getBitMask(codeStr2,0);
			int val2=getBitMask(codeStr2,1);
			int val3=getBitMask(codeStr2,2);
			int val4=getBitMask(codeStr2,3);
			Item I=null;
			switch(objType)
			{
			case 1: if(objectName.toUpperCase().indexOf("LANTERN")>=0)
						I=CMClass.getStdItem("GenLantern");
					else
						I=CMClass.getStdItem("GenLightSource");
					((Light)I).setDuration(val3*80);
					break;
			case 2: I=CMClass.getMiscMagic("GenScroll");
					I.baseEnvStats().setLevel(val1);
					I.setUsesRemaining(3);
					((SpellHolder)I).setSpellList(getSpell(str2,val2)+";"+getSpell(str3,val3)+";"+getSpell(str4,val4));
					break;
			case 3: I=CMClass.getMiscMagic("GenWand");
					I.baseEnvStats().setLevel(val1);
					I.setUsesRemaining(val2);
					((Wand)I).setSpell(CMClass.getAbility(getSpell(str4,val4)));
					break;
			case 4: I=CMClass.getWeapon("GenStaff");
					I.baseEnvStats().setLevel(val1);
					I.setUsesRemaining(val2);
					((Wand)I).setSpell(CMClass.getAbility(getSpell(str4,val4)));
					adjuster=CMClass.getAbility("Prop_WearAdjuster");
					break;
			case 5: I=CMClass.getWeapon("GenWeapon");
					doWeapon((Weapon)I,objectName,val1,str1,val2,val3,val4,str4);
					adjuster=CMClass.getAbility("Prop_WearAdjuster");
					break;
			case 6: I=CMClass.getWeapon("GenWeapon");
					doWeapon((Weapon)I,objectName,val1,str1,val2,val3,val4,str4);
					adjuster=CMClass.getAbility("Prop_WearAdjuster");
					break;
			case 7: I=CMClass.getWeapon("GenWeapon");
					doWeapon((Weapon)I,objectName,val1,str1,val2,val3,val4,str4);
					adjuster=CMClass.getAbility("Prop_WearAdjuster");
					break;
			case 8: I=CMClass.getStdItem("GenItem");
					break;
			case 9: if(objectName.toUpperCase().indexOf("SHIELD")>=0)
						I=CMClass.getArmor("GenShield");
					else
						I=CMClass.getArmor("GenArmor");
					I.baseEnvStats().setArmor((int)Math.round(Util.div((val1+val2+val3+val4+1),4.0)+1));
					adjuster=CMClass.getAbility("Prop_WearAdjuster");
					break;
			case 10: I=CMClass.getMiscMagic("GenPotion");
					I.baseEnvStats().setLevel(val1);
					((Potion)I).setSpellList(getSpell(str2,val2)+";"+getSpell(str3,val3)+";"+getSpell(str4,val4));
					 break;
			case 11: I=CMClass.getArmor("GenArmor");
					 I.baseEnvStats().setArmor(0);
					 adjuster=CMClass.getAbility("Prop_WearAdjuster");
					 break;
			case 12: I=CMClass.getStdItem("GenItem");
					 if(hasReadableContent(objectName))
						I=CMClass.getStdItem("GenReadable");
					 break;
			case 13: I=CMClass.getStdItem("GenItem");
					 if(hasReadableContent(objectName))
						I=CMClass.getStdItem("GenReadable");
					 break;
			case 14: I=CMClass.getStdItem("GenItem"); break;
			case 15: if(EnglishParser.containsString(objectName,"belt")
					 ||EnglishParser.containsString(objectName,"bandolier")
					 ||EnglishParser.containsString(objectName,"sheath"))
						I=CMClass.getArmor("GenArmor");
					 else
						I=CMClass.getStdItem("GenContainer");
					 ((Container)I).setCapacity(val1);
					 boolean lid=false;
					 boolean open=true;
					 boolean lock=false;
					 boolean locked=false;
					 if((val2&1)==1)
						 lid=true;
					 if((val2&2)==2)
					 {
						 lock=true;
						 locked=true;
						 open=false;
						 lid=true;
						 I.baseEnvStats().setLevel(100);
					 }
					 if((val2&4)==4)
					 {
						 lid=true;
						 open=false;
					 }
					 if((val2&8)==8)
					 {
						 lock=true;
						 locked=true;
						 open=false;
						 lid=true;
					 }
					 ((Container)I).setLidsNLocks(lid,open,lock,locked);
					 if(((Container)I).hasALid()&&((Container)I).hasALock())
						 ((Container)I).setKeyName(areaName+"#"+val3);
					 break;
			case 16: I=CMClass.getStdItem("GenItem"); break;
			case 17: I=CMClass.getStdItem("GenDrink");
					 str3=str3.toUpperCase().trim();
					 if(((val3>0)&&(val3<6))
					 ||(str3.indexOf("BEER")>=0)
					 ||(str3.indexOf("ALE")>=0)
					 ||(str3.indexOf("BREW")>=0)
					 ||(str3.indexOf("WINE")>=0))
					 {
						((Drink)I).setLiquidType(EnvResource.RESOURCE_LIQUOR);
						I.addEffect(CMClass.getAbility("Poison_Beer"));
						((Drink)I).setLiquidHeld(val1*10);
						((Drink)I).setLiquidRemaining(val2);
					 }
					 else
					 if(str3.indexOf("FIREBREATHER")>=0)
					 {
						((Drink)I).setLiquidType(EnvResource.RESOURCE_LIQUOR);
						I.addEffect(CMClass.getAbility("Poison_Firebreather"));
						((Drink)I).setLiquidHeld(val1*10);
						((Drink)I).setLiquidRemaining(val2);
					 }
					 else
					 if(str3.indexOf("LOCAL SPECIALTY")>=0)
					 {
						((Drink)I).setLiquidType(EnvResource.RESOURCE_LIQUOR);
						I.addEffect(CMClass.getAbility("Poison_Liquor"));
						((Drink)I).setLiquidHeld(val1*10);
						((Drink)I).setLiquidRemaining(val2);
					 }
					 else
					 if(str3.indexOf("WHISKEY")>=0)
					 {
						((Drink)I).setLiquidType(EnvResource.RESOURCE_LIQUOR);
						I.addEffect(CMClass.getAbility("Poison_Liquor"));
						((Drink)I).setLiquidHeld(val1*10);
						((Drink)I).setLiquidRemaining(val2);
					 }
					 else
					 if((val4>0)||(str3.indexOf("POISON")>=0))
					 {
						((Drink)I).setLiquidType(EnvResource.RESOURCE_POISON);
						I.addEffect(CMClass.getAbility("Poison"));
						((Drink)I).setLiquidHeld(val1*10);
						((Drink)I).setLiquidRemaining(val2);
					 }
					 else
					 {
						((Drink)I).setLiquidHeld(val1*30);
						((Drink)I).setLiquidRemaining(val2*10);
					 }
					 break;
			case 18: I=CMClass.getStdItem("GenKey");
					 ((Key)I).setKey(areaName+objectID);
					 break;
			case 19: I=CMClass.getStdItem("GenFood");
					 if(val4>0)
					 {
						 I=CMClass.getMiscMagic("GenPill");
						 ((Pill)I).setSpellList("Poison"+";");
					 }
					 ((Food)I).setNourishment(20*val1);
					 break;
			case 20: I=CMClass.getStdItem("StdCoins");
					 ((Coins)I).setNumberOfCoins(val1);
					 ((Coins)I).setDenomination(1.0);
					 ((Coins)I).setCurrency("");
					 break;
			case 21: I=CMClass.getStdItem("GenItem"); break;
			case 22: I=CMClass.getStdItem("GenBoat");
					 break;
			case 23: I=CMClass.getStdItem("GenCorpse"); break;
			case 24: I=CMClass.getStdItem("GenCorpse"); break;
			case 25: I=CMClass.getStdItem("GenWater");
					 Sense.setGettable(I,false);
					 ((Drink)I).setLiquidHeld(Integer.MAX_VALUE-5000);
					 ((Drink)I).setLiquidRemaining(((Drink)I).liquidHeld());
					 break;
			case 26: I=CMClass.getMiscMagic("GenPill");
					I.baseEnvStats().setLevel(val1);
					((Pill)I).setSpellList(getSpell(str2,val2)+";"+getSpell(str3,val3)+";"+getSpell(str4,val4));
					 break;
			case 27: I=CMClass.getStdItem("GenItem"); break;
			case 28: I=CMClass.getStdItem("GenReadable"); // don't use GemMaps any more...
					 break;
			case 29: I=CMClass.getStdItem("GenItem"); break;
			case 97: I=CMClass.getStdItem("GenPortal");
					 if((str4.length()>0)&&(!str4.equals("0")))
					 {
						 Room R=findRoomSomewhere(str4,areaName,doneRooms);
						 if(R!=null) 
							 I.setReadableText(R.roomID());
						 else
						 for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
						 {
							 R=(Room)e.nextElement();
							 if(R.roomID().endsWith("#"+str4))
							 {
								I.setReadableText(R.roomID());
								break;
							 }
						 }
						if(I.readableText().length()==0)
							I.setReadableText("#"+str4);
					 }
					 break;
			case 98: I=CMClass.getStdItem("GenKey");
					 ((Key)I).setKey(areaName+objectID);
					 break;
			case 99: I=CMClass.getStdItem("GenCorpse"); break;
			case -1: I=CMClass.getStdItem("GenWallpaper"); break;
			default:
					I=CMClass.getStdItem("GenItem"); 
					break;
			}

			if(!Util.isSet(wearFlag,0))
				Sense.setGettable(I,false);
			if(Util.isSet(wearFlag,1))
				I.setRawProperLocationBitmap(Item.ON_LEFT_FINGER|Item.ON_RIGHT_FINGER|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,2))
				I.setRawProperLocationBitmap(Item.ON_NECK|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,3))
				I.setRawProperLocationBitmap(Item.ON_TORSO|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,4))
				I.setRawProperLocationBitmap(Item.ON_HEAD|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,5))
				I.setRawProperLocationBitmap(Item.ON_LEGS|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,6))
				I.setRawProperLocationBitmap(Item.ON_FEET|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,7))
				I.setRawProperLocationBitmap(Item.ON_HANDS|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,8))
				I.setRawProperLocationBitmap(Item.ON_ARMS|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,9))
				I.setRawProperLocationBitmap(Item.HELD|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,10))
				I.setRawProperLocationBitmap(Item.ABOUT_BODY|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,11))
				I.setRawProperLocationBitmap(Item.ON_WAIST|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,12))
				I.setRawProperLocationBitmap(Item.ON_LEFT_WRIST|Item.ON_RIGHT_WRIST|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,13))
				I.setRawProperLocationBitmap(Item.WIELD|Item.HELD|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,14))
				I.setRawProperLocationBitmap(Item.HELD|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,15))
				I.setRawLogicalAnd(true);
			if(Util.isSet(wearFlag,17))
				I.setRawProperLocationBitmap(Item.ON_EARS|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,18)) // ankles
				I.setRawProperLocationBitmap(Item.ON_FEET|I.rawProperLocationBitmap());

			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			// the relation of this stuff is vital!  must follow properlocation setting
			// and the getttable setting ONLY!!!
			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			if((adjuster.ID().equals("Prop_HaveAdjuster"))
			&&(I.rawProperLocationBitmap()>0)
			)
			{
				adjuster=CMClass.getAbility("Prop_WearAdjuster");
				if(I.ID().equals("GenItem"))
				{
					long wear=I.rawProperLocationBitmap();
					boolean bool=I.rawLogicalAnd();
					boolean gettable=Sense.isGettable(I);
					I=CMClass.getArmor("GenArmor");
					I.setRawProperLocationBitmap(wear);
					I.setRawLogicalAnd(bool);
					I.baseEnvStats().setArmor(0);
					Sense.setGettable(I,gettable);
				}
			}

			Ability resister=CMClass.getAbility("Prop_HaveResister");
			Ability caster=CMClass.getAbility("Prop_HaveSpellCast");
			if(adjuster.ID().equals("Prop_WearAdjuster"))
			{
				resister=CMClass.getAbility("Prop_WearResister");
				caster=CMClass.getAbility("Prop_WearSpellCast");
			}
			if(Util.numBits(codeStr3)>2)
			{
				I.baseEnvStats().setLevel(Util.s_int(Util.getCleanBit(codeStr3,0)));
				I.baseEnvStats().setWeight(Util.s_int(Util.getCleanBit(codeStr3,1)) / 10);
				if(I.baseEnvStats().weight()<1) I.baseEnvStats().setWeight(1);
				if(I instanceof Rideable)
					I.baseEnvStats().setWeight(Util.s_int(Util.getCleanBit(codeStr3,1)) * 10);
				I.setBaseValue(Util.s_int(Util.getCleanBit(codeStr3,2)));
			}
			else
			{
				I.baseEnvStats().setLevel(Util.s_int(codeStr3));
			}


			I.setName(objectName);
			I.setDisplayText(objectDisplay);
			if(!objectDescription.trim().equalsIgnoreCase("OLDSTYLE"))
				I.setDescription(objectDescription);
			if((I instanceof Weapon)
			&&((objectName.toUpperCase().indexOf("TWO-HANDED")>=0)
			||(objectName.toUpperCase().indexOf("TWO HANDED")>=0)))
			{
				I.setRawLogicalAnd(true);
				I.setRawProperLocationBitmap(Item.HELD|I.rawProperLocationBitmap());
			}

			boolean materialchange=false;
			for(int ot=0;ot<objDescs.length;ot++)
				if(objectDescription.equalsIgnoreCase(objDescs[ot][0]))
				{
					I.setMaterial(Util.s_int(objDescs[ot][1]));
					materialchange=true;
					break;
				}

			// correction for certain rings
			if((((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_CLOTH)
				||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_PAPER))
			   &&(I.fitsOn(Armor.ON_LEFT_FINGER)))
			{
				I.setMaterial(EnvResource.RESOURCE_SILVER);
				materialchange=true;
			}

			if(materialchange)
			    I.setDescription("");

			if((I instanceof Armor)&&(((Armor)I).containTypes()==Container.CONTAIN_ANYTHING))
			{
				if(EnglishParser.containsString(objectName,"belt")
				||EnglishParser.containsString(objectName,"bandolier")
				||EnglishParser.containsString(objectName,"sheath"))
				{
					((Armor)I).setContainTypes(Container.CONTAIN_ONEHANDWEAPONS);
					if(((Armor)I).capacity()-I.baseEnvStats().weight()<30)
						((Armor)I).setCapacity(I.baseEnvStats().weight()+30);
				}
				else
				if(EnglishParser.containsString(objectName,"boot")
				||EnglishParser.containsString(objectName,"bracer")
				||EnglishParser.containsString(objectName,"sheath"))
				{
					((Armor)I).setContainTypes(Container.CONTAIN_DAGGERS);
					if(((Armor)I).capacity()-I.baseEnvStats().weight()<10)
						((Armor)I).setCapacity(I.baseEnvStats().weight()+10);
				}
			}
			
			if(Util.isSet(extraFlag,0))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_GLOWING);

			//if((extraFlag&2)==2) coffeemud has no hummers
			if(Util.isSet(extraFlag,2))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_DARK);

			if(Util.isSet(extraFlag,4))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_EVIL);

			if(Util.isSet(extraFlag,5))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_INVISIBLE);

			if(Util.isSet(extraFlag,6))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_BONUS);

			if(Util.isSet(extraFlag,7))
				Sense.setDroppable(I,false);

			if(Util.isSet(extraFlag,8))
				I.addNonUninvokableEffect(CMClass.getAbility("Prayer_Bless"));

			Ability prop_WearZapper = CMClass.getAbility("Prop_WearZapper");

			if(Util.isSet(extraFlag,9))
				prop_WearZapper.setMiscText(prop_WearZapper.text()+" -good");

			if(Util.isSet(extraFlag,10))
				prop_WearZapper.setMiscText(prop_WearZapper.text()+" -evil");

			if(Util.isSet(extraFlag,11))
				prop_WearZapper.setMiscText(prop_WearZapper.text()+" -neutral");

			if(prop_WearZapper.text().length()>0)
				I.addNonUninvokableEffect(prop_WearZapper);

			if(Util.isSet(extraFlag,12))
				Sense.setRemovable(I,false);

			//if(extraFlag&4096)==4096) coffeemud doesn't support rotting cargo

			if(Util.isSet(extraFlag,14))
				Sense.setGettable(I,false);

			//if(extraFlag&16384)==16384) coffeemud doesn't support rotting cargo

			if(Util.isSet(extraFlag,16))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_INVISIBLE);

			if(Util.isSet(extraFlag,17))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_GOOD);

			if(Util.isSet(extraFlag,18))
				if((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
					I.setMaterial(EnvResource.RESOURCE_GLASS);

			if(Util.isSet(extraFlag,20))
				I.baseEnvStats().setSensesMask(I.baseEnvStats().sensesMask()|EnvStats.SENSE_UNLOCATABLE);

			//if(Util.isSet(extraFlag,22))
			//nothing is unidentifiable

			// now all those funny tags
			while(objV.size()>0)
			{
				String codeLine=nextLine(objV).trim().toUpperCase();
				if(codeLine.equals("E"))
				{
					eatNextLine(objV);
					if((CMClass.getItem(I.ID())!=null)
					&&(I.description().equals(CMClass.getItem(I.ID()).description())))
					   I.setDescription("");
					else
					if(I.description().length()>0)
						I.setDescription(I.description()+"%0D");
					eatLineSquiggle(objV);
					boolean squiggleFound=false;
					for(int y=0;y<objV.size();y++)
					{
						String ts=(String)objV.elementAt(y);
						if(ts.indexOf("~")>=0)
						{
							squiggleFound=true;
							break;
						}
						if(ts.equals("A")
						   ||ts.equals("E")
						   ||ts.equals("L")
						   ||ts.equals("F"))
						{
							objV.insertElementAt("~",y);
							squiggleFound=true;
							break;
						}
					}
					if(!squiggleFound)
						objV.addElement("~");
					String desc=CoffeeFilter.safetyFilter(eatLineSquiggle(objV));
					I.setDescription(I.description()+desc);
					if(I.ID().equals("GenReadable"))
						I.setReadableText(fixReadableContent(I.description()));
				}
				else
				if(codeLine.equals("L"))
				{
					eatNextLine(objV);
					// need to figure this one out.
					eatLine(objV);
				}
				else
				if(codeLine.startsWith("X "))
				{
					codeLine=eatLineSquiggle(objV);
					Behavior B=I.fetchBehavior("Sounder");
					if(B==null)
					{
						B=CMClass.getBehavior("Sounder");
						if(B!=null) I.addBehavior(B);
					}
					if(B.getParms().length()==0)
						B.setParms(codeLine.substring(1).trim());
					else
						B.setParms(B.getParms()+";"+codeLine.substring(1).trim());
				}
				else
				if(codeLine.equals("A"))
				{
					eatNextLine(objV);
					String codesLine=eatNextLine(objV);
					if(Util.numBits(codesLine)!=2)
						returnAnError(mob,"Malformed 'A' code for item "+objectID+", "+I.Name()+": "+codesLine+", area="+areaName);
					else
					{
						int num=Util.s_int(Util.getCleanBit(codesLine,0));
						int val=Util.s_int(Util.getCleanBit(codesLine,1));
						switch(num)
						{
						case 1:
							adjuster.setMiscText(adjuster.text()+" str"+((val>=0)?("+"+val):(""+val)));
							break;
						case 2:
							adjuster.setMiscText(adjuster.text()+" dex"+((val>=0)?("+"+val):(""+val)));
							break;
						case 3:
							adjuster.setMiscText(adjuster.text()+" int"+((val>=0)?("+"+val):(""+val)));
							break;
						case 4:
							adjuster.setMiscText(adjuster.text()+" wis"+((val>=0)?("+"+val):(""+val)));
							break;
						case 5:
							adjuster.setMiscText(adjuster.text()+" con"+((val>=0)?("+"+val):(""+val)));
							break;
						case 6:
							// coffeemud don't play with sex
							break;
						case 7:
							adjuster.setMiscText(adjuster.text()+" cha"+((val>=0)?("+"+val):(""+val)));
							break;
						case 8: 	break;
						case 9: 	break;
						case 10: 	break;
						case 11: 	break;
						case 12:
							adjuster.setMiscText(adjuster.text()+" mana"+((val>=0)?("+"+val):(""+val)));
							break;
						case 13:
							adjuster.setMiscText(adjuster.text()+" hit"+((val>=0)?("+"+val):(""+val)));
							break;
						case 14:
							adjuster.setMiscText(adjuster.text()+" move"+((val>=0)?("+"+val):(""+val)));
							break;
						case 15: 	break;
						case 16: 	break;
						case 17:
							if((val>0)&&(I instanceof Armor))
								I.baseEnvStats().setArmor(I.baseEnvStats().armor()+(val*5));
							else
								adjuster.setMiscText(adjuster.text()+" armor"+((val>=0)?("+"+(val*5)):(""+(val*5))));
							break;
						case 18:
							if((val>0)&&(I instanceof Weapon))
								I.baseEnvStats().setAttackAdjustment(I.baseEnvStats().attackAdjustment()+(val*5));
							else
								adjuster.setMiscText(adjuster.text()+" attack"+((val>=0)?("+"+(val*5)):(""+(val*5))));
							break;
						case 19:
							if((val>0)&&(I instanceof Weapon))
								I.baseEnvStats().setDamage(I.baseEnvStats().damage()+val);
							else
								adjuster.setMiscText(adjuster.text()+" damage"+((val>=0)?("+"+(val)):(""+(val))));
							break;
						case 20: // spells, but with a numeric value.. ?!?!
							break;
						case 21: 	break;
						case 22: 	break;
						case 23: 	break;
						case 24:
							resister.setMiscText(resister.text()+" magic "+((-val)*2)+"%");
							break;
						case 25:
							// i have no idea what a power up is
							break;
						case 30:
							switch(val)
							{
							case 6:
								caster.setMiscText(caster.text()+("Prayer_Curse")+";");
								break;
							case 9:
								caster.setMiscText(caster.text()+("Poison")+";");
								break;
							case 10:
								caster.setMiscText(caster.text()+("Prayer_Plague")+";");
								break;
							case 11:
								caster.setMiscText(caster.text()+("Spell_Blindness")+";");
								break;
							}
							break;
						}
					}
				}
				else
				if(codeLine.equals("F"))
				{
					eatNextLine(objV);
					String codesLine=eatNextLine(objV);
					if(Util.numBits(codesLine)!=4)
						returnAnError(mob,"Malformed 'F' code for item "+objectID+", "+I.Name()+": "+codesLine+", area="+areaName);
					else
					{
						String codeType=Util.getBit(codesLine,0);
						if(codeType.equals("V"))
						{
							int res=getBitMask(codesLine,3);
							int imm=getBitMask(codesLine,3);
							String[] resistances={
								" teleport",
								" mind",
								" magic",
								" weapons",
								" blunt",
								" pierce",
								" slash",
								" fire",
								" cold",
								" elec",
								" acid",
								" poison",
								" evil",
								" holy",
								"",
								" mind",
								" disease",
								" gas"};
							for(int rei=0;rei<resistances.length;rei++)
							{
								if((Util.isSet(res,rei))&&(resistances[rei].length()>0))
									resister.setMiscText(resister.text()+resistances[rei]+" -25%");
								else
								if((Util.isSet(imm,rei))&&(resistances[rei].length()>0))
									resister.setMiscText(resister.text()+resistances[rei]+" -100%");
							}

						}
						else
						if(codeType.equals("A"))
						{
							int dis=0;
							int sense=0;
							int codeBits=getBitMask(codesLine,3);
							if(Util.isSet(codeBits,0))
								sense=sense|EnvStats.CAN_NOT_SEE;
							if(Util.isSet(codeBits,1))
								dis=dis|EnvStats.IS_INVISIBLE;
							if(Util.isSet(codeBits,2))
								sense=sense|EnvStats.CAN_SEE_EVIL;
							if(Util.isSet(codeBits,3))
								sense=sense|EnvStats.CAN_SEE_INVISIBLE;
							if(Util.isSet(codeBits,4))
								sense=sense|EnvStats.CAN_SEE_BONUS;
							if(Util.isSet(codeBits,5))
								sense=sense|EnvStats.CAN_SEE_HIDDEN|EnvStats.CAN_SEE_SNEAKERS;
							if(Util.isSet(codeBits,6))
								caster.setMiscText(caster.text()+("Spell_IronGrip")+";");
							if(Util.isSet(codeBits,7))
								caster.setMiscText(caster.text()+("Prayer_Sanctuary")+";");
							if(Util.isSet(codeBits,8))
								caster.setMiscText(caster.text()+("Spell_FaerieFire")+";");
							if(Util.isSet(codeBits,9))
								sense=sense|EnvStats.CAN_SEE_INFRARED;
							if(Util.isSet(codeBits,10))
								caster.setMiscText(caster.text()+("Prayer_Curse")+";");
							if(Util.isSet(codeBits,11))
								caster.setMiscText(caster.text()+"Specialization_Weapon;");
							if(Util.isSet(codeBits,12))
								caster.setMiscText(caster.text()+("Poison")+";");
							if(Util.isSet(codeBits,13))
								caster.setMiscText(caster.text()+("Prayer_ProtEvil")+";");
							if(Util.isSet(codeBits,14))
								caster.setMiscText(caster.text()+("Prayer_ProtGood")+";");
							if(Util.isSet(codeBits,15))
								dis=dis|EnvStats.IS_SNEAKING;
							if(Util.isSet(codeBits,16))
								caster.setMiscText(caster.text()+("Skill_Hide")+";");
							if(Util.isSet(codeBits,17))
							{
								dis=dis|EnvStats.IS_SLEEPING;
								caster.setMiscText(caster.text()+("Spell_Sleep")+";");
							}
							//if(Util.isSet(codeBits,18)) item cannot charm you
							//	caster.setMiscText(caster.text()+(new Poison().ID())+";");
							if(Util.isSet(codeBits,19))
								dis=dis|EnvStats.IS_FLYING;
							if(Util.isSet(codeBits,20))
								caster.setMiscText(caster.text()+("Spell_PassDoor")+";");
							if(Util.isSet(codeBits,21))
								caster.setMiscText(caster.text()+("Spell_Haste")+";");
							if(Util.isSet(codeBits,22))
								caster.setMiscText(caster.text()+("Prayer_Calm")+";");
							if(Util.isSet(codeBits,23))
								caster.setMiscText(caster.text()+("Prayer_Plague")+";");
							if(Util.isSet(codeBits,24))
								caster.setMiscText(caster.text()+("Spell_Awe")+";");
							if(Util.isSet(codeBits,25))
								sense=sense|EnvStats.CAN_SEE_DARK;
							if(Util.isSet(codeBits,26))
								caster.setMiscText(caster.text()+("Fighter_Berzerk")+";");
							if(Util.isSet(codeBits,27))
								caster.setMiscText(caster.text()+("Regeneration")+";");
							if(Util.isSet(codeBits,28))
								sense=sense|EnvStats.CAN_SEE_GOOD;
							if(Util.isSet(codeBits,29))
								caster.setMiscText(caster.text()+("Spell_Slow")+";");
							if(sense>0)
								adjuster.setMiscText(adjuster.text()+" sen+"+sense);
							if(dis>0)
								adjuster.setMiscText(adjuster.text()+" dis+"+dis);
						}
						else
						{
							int res=getBitMask(codesLine,3);
							int imm=getBitMask(codesLine,3);
							String[] resistances={
								" teleport",
								" mind",
								" magic",
								" weapons",
								" blunt",
								" pierce",
								" slash",
								" fire",
								" cold",
								" elec",
								" acid",
								" poison",
								" evil",
								" holy",
								"",
								" mind",
								" disease",
								" gas"};
							for(int rei=0;rei<resistances.length;rei++)
							{
								if((Util.isSet(res,rei))&&(resistances[rei].length()>0))
									resister.setMiscText(resister.text()+resistances[rei]+" 25%");
								else
								if((Util.isSet(imm,rei))&&(resistances[rei].length()>0))
									resister.setMiscText(resister.text()+resistances[rei]+" 100%");
							}

							if(Util.isSet(res,14))
								caster.setMiscText(caster.text()+"Spell_ResistMagicMissiles;");
							//if(Util.isSet(res,18)) no light resistance
							//if(Util.isSet(res,18)) no sound resistance
						}


					}
				}
				else
				if(codeLine.startsWith(">"))
				{
					codeLine=eatLineSquiggle(objV);
					String scriptStuff="";
					if(!codeLine.substring(1).trim().toUpperCase().startsWith("IN_FILE_PROG"))
					{
						scriptStuff+=codeLine.substring(1).trim()+";";
						codeLine=nextLine(objV);
						while(codeLine.indexOf("~")<0)
						{
							scriptStuff+=codeLine.trim()+";";
							eatLine(objV);
							codeLine=nextLine(objV);
						}
						codeLine=eatLineSquiggle(objV).trim();
						scriptStuff+=codeLine+"~";
					}
					// nothing done with the script. :(
				}
				else
				if(codeLine.equals("|"))
				{
					eatNextLine(objV);
					// have no idea, but we skip it
				}
				else
				if((forgiveZeroes)
				&&(codeLine.length()>0)
				&&(Util.isNumber(""+codeLine.charAt(0)))
				&&(Util.numBits(codeLine)>1))
				{
					eatNextLine(objV);
					// eos support
				}
				else
				if((forgiveZeroes)
				&&(codeLine.endsWith("~")))
				{
					eatNextLine(objV);
					// eos support
				}
				else
				if((codeLine.startsWith("#"))||(codeLine.length()==0))
				{
					eatNextLine(objV);
				}
				else
				{
					eatNextLine(objV);
					returnAnError(mob,"Unknown code for item "+objectID+", "+I.Name()+": "+codeLine+", area="+areaName);
				}
			}
			if(adjuster.text().length()>0)
				I.addNonUninvokableEffect(adjuster);
			if(caster.text().length()>0)
				I.addNonUninvokableEffect(caster);
			if(resister.text().length()>0)
				I.addNonUninvokableEffect(resister);
			I.recoverEnvStats();
			I.text();
			I.recoverEnvStats();
			if(OfThisID.startsWith("#"))
				doneItems.put(OfThisID.substring(1),I);
			else
				doneItems.put(OfThisID,I);
			return I;
		}
		return null;
	}

	public static String socialFix(String str)
	{

		str=Util.replaceAll(str,"$n","<S-NAME>");
		str=Util.replaceAll(str,"$N","<T-NAMESELF>");
		str=Util.replaceAll(str,"$m","<S-HIM-HER>");
		str=Util.replaceAll(str,"$M","<T-HIM-HER>");
		str=Util.replaceAll(str,"$s","<S-HIS-HER>");
		str=Util.replaceAll(str,"$S","<T-HIS-HER>");
		str=Util.replaceAll(str,"$e","<S-HE-SHE>");
		str=Util.replaceAll(str,"$E","<T-HE-SHE>");
		str=Util.replaceAll(str,"`","\'");
		if(str.equals("$")) return "";
		return str.trim();
	}

	public static Room findRoomSomewhere(String roomID, String areaName, Hashtable doneRooms)
	{
		if(roomID.startsWith("#"))
		{
			if(doneRooms.containsKey(roomID.substring(1)))
				return (Room)doneRooms.get(roomID.substring(1));
		}
		else
		{
			if(doneRooms.containsKey(roomID))
				return (Room)doneRooms.get(roomID);
		}
		Room R=CMMap.getRoom(roomID);
		if(R!=null) return R;
		return CMMap.getRoom(areaName+"#"+roomID);
	}

	public static void importCustomFiles(MOB mob, Hashtable files, HashSet customBother, boolean noPrompt)
		throws IOException
	{
		if(files.size()==0) return;
		if((!noPrompt)&&(mob.session()==null)) return;
		for(Enumeration e=files.keys();e.hasMoreElements();)
		{
		    String filename=(String)e.nextElement();
		    String data=(String)files.get(filename);
			if(customBother.contains(filename))
			   continue;

			if(Resources.getFileResource(filename)!=null)
			{
				if(!noPrompt)
					if(!mob.session().confirm("External resource '"+filename+"' found, import (Y/n)?","Y"))
						continue;
			}
		    Resources.submitResource(filename,new StringBuffer(data));
		    Resources.saveFileResource(filename);
		}
	}

	public static void importCustomObjects(MOB mob, Vector custom, HashSet customBother, boolean noPrompt)
	throws IOException
	{
		if(custom.size()==0) return;
		if((!noPrompt)&&(mob.session()==null)) return;
		for(int c=0;c<custom.size();c++)
		{
			if(custom.elementAt(c) instanceof Race)
			{
				Race R=(Race)custom.elementAt(c);
				if(customBother.contains(R.ID()))
				   continue;
	
				Race R2=CMClass.getRace(R.ID());
				if(R2==null)
				{
					if(!noPrompt)
						if(!mob.session().confirm("Custom Race '"+R.ID()+"' found, import (Y/n)?","Y"))
							continue;
					CMClass.addRace(R);
					CMClass.DBEngine().DBCreateRace(R.ID(),R.racialParms());
				}
				else
				if(!R2.isGeneric())
				{
					if(!noPrompt)
						if(!mob.session().confirm("Custom Race '"+R.ID()+"' found which would override your standard race.  Import anyway (Y/n)?","Y"))
							continue;
					CMClass.addRace(R);
					CMClass.DBEngine().DBCreateRace(R.ID(),R.racialParms());
				}
			}
			else
			if(custom.elementAt(c) instanceof CharClass)
			{
				CharClass C=(CharClass)custom.elementAt(c);
				if(customBother.contains(C.ID()))
				   continue;
	
				CharClass C2=CMClass.getCharClass(C.ID());
				if(C2==null)
				{
					if(!noPrompt)
						if(!mob.session().confirm("Custom Char Class '"+C.ID()+"' found, import (Y/n)?","Y"))
							continue;
					CMClass.addCharClass(C);
					CMClass.DBEngine().DBCreateClass(C.ID(),C.classParms());
				}
				else
				if(!C2.isGeneric())
				{
					if(!noPrompt)
						if(!mob.session().confirm("Custom Char Class '"+C.ID()+"' found which would override your standard class.  Import anyway (Y/n)?","Y"))
							continue;
					CMClass.addCharClass(C);
					CMClass.DBEngine().DBCreateClass(C.ID(),C.classParms());
				}
			}
		}
	}
	public static boolean localDeleteArea(MOB mob, Vector reLinkTable, String areaName)
	{
		if(mob.location().getArea().Name().equalsIgnoreCase(areaName))
		{
			mob.tell("You dip!  You are IN that area!  Leave it first...");
			return false;
		}
		else
		{
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(!R.getArea().Name().equalsIgnoreCase(areaName))
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					{
						Room dirR=R.rawDoors()[d];
						if((dirR!=null)&&(dirR.getArea().Name().equalsIgnoreCase(areaName)))
							reLinkTable.addElement(R.roomID()+"/"+d+"/"+dirR.roomID());
					}
			}
			while(true)
			{
				Room foundOne=null;
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(R.getArea().Name().equalsIgnoreCase(areaName))
					{
						foundOne=R;
						break;
					}
				}
				if(foundOne==null)
					break;
				else
					CoffeeUtensils.obliterateRoom(foundOne);
			}
		}
		Area A1=CMMap.getArea(areaName);
		if(A1!=null)
		{
			CMClass.DBEngine().DBDeleteArea(A1);
			CMMap.delArea(A1);
		}
		return true;
	}


	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean prompt=true;
		Hashtable doneItems=new Hashtable();
		Hashtable doneRooms=new Hashtable();
		Hashtable doneMOBS=new Hashtable();
		Vector nextResetData=new Vector();
		Hashtable laterLinks=new Hashtable();
		boolean multiArea=false;
		Vector custom=new Vector();
		Hashtable externalFiles=new Hashtable();
		HashSet customBotherChecker=new HashSet();

		commands.removeElementAt(0);

		// verify that the process can start
		if(commands.size()<1)
		{
			mob.tell("Import what?  Specify the path/filename!");
			return false;
		}
		if(mob.isMonster())
		{
			mob.tell("No can do.");
			return false;
		}

		if(((String)commands.elementAt(0)).equalsIgnoreCase("noprompt"))
		{
			commands.removeElementAt(0);
			prompt=false;
		}

		// continue pre-processing
		for(int areaFile=commands.size()-1;areaFile>=0;areaFile--)
		{
			String areaFileName=(String)commands.elementAt(areaFile);
			File F=new File(areaFileName);
			File[] FF=F.listFiles();
			if((FF!=null)&&(FF.length>0))
			{
				for(int f=0;f<FF.length;f++)
					commands.addElement(FF[f].getAbsolutePath());
				commands.removeElementAt(areaFile);
			}
		}

		Vector mobData=new Vector();
		Vector objectData=new Vector();

		multiArea=commands.size()>1;
		for(int areaFile=0;areaFile<commands.size();areaFile++)
		{
		Vector areaData=new Vector();
		Vector roomData=new Vector();
		Vector resetData=new Vector();
		Vector mobProgData=new Vector();
		Vector objProgData=new Vector();
		Vector shopData=new Vector();
		Vector specialData=new Vector();
		Vector newRooms=new Vector();
		Vector socialData=new Vector();
		Vector reLinkTable=null;

		String areaFileName=(String)commands.elementAt(areaFile);
		// read in the .are file
		StringBuffer buf=Resources.getFile(areaFileName);
		if((buf==null)||((buf!=null)&&(buf.length()==0)))
		{
			mob.tell("File not found at: '"+areaFileName+"'!");
			return false;
		}
		try
		{
			if(areaFileName.toUpperCase().trim().endsWith(".LST"))
			{
				mob.tell("Unpacking areas lists from file : '"+areaFileName+"'...");
				String filePrefix="";
				int c=areaFileName.lastIndexOf(File.separator);
				if(c>=0) filePrefix=areaFileName.substring(0,c+1);
				c=0;
				String fn="";
				while((buf.length()>0)&&(c<buf.length()))
				{
					switch(buf.charAt(c))
					{
					case '\n':
					case '\r':
						if((fn.length()>0)&&(!fn.startsWith("#"))&&(!fn.startsWith("$")))
							commands.addElement(filePrefix+fn);
						buf.delete(0,c+1);
						c=0;
						fn="";
						break;
					default:
						fn+=buf.charAt(c);
						c++;
						break;
					}
				}
				if((fn.length()>0)&&(!fn.startsWith("#"))&&(!fn.startsWith("$")))
					commands.addElement(filePrefix+fn);
				continue;
			}
			if((buf.length()>20)&&(buf.substring(0,20).indexOf("<AREAS>")>=0))
			{
				if(!CMSecurity.isAllowedEverywhere(mob,"IMPORTROOMS"))
				{
					mob.tell("You are not allowed to import areas in '"+areaFileName+"'.");
					continue;
				}
				
				Vector areas=new Vector();
				if(mob.session()!=null)
					mob.session().rawPrint("Unpacking area(s) from file: '"+areaFileName+"'...");
				String error=CoffeeMaker.fillAreasVectorFromXML(buf.toString(),areas,custom,externalFiles);
				if(error.length()==0) Import.importCustomObjects(mob,custom,customBotherChecker,!prompt);
				if(error.length()==0) Import.importCustomFiles(mob,externalFiles,customBotherChecker,!prompt);
				if(error.length()>0) return false;
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
				mob.tell("Found "+areas.size()+" areas.");
				int num=areas.size();
				int a=0;
				while(areas.size()>0)
				{
					if(mob.session()!=null)
						mob.session().rawPrint("Unpacking area #"+(a+1)+"/"+num+"...");
					Vector area=(Vector)areas.firstElement();
					error=CoffeeMaker.unpackAreaFromXML(area,mob.session(),true);
					if(mob.session()!=null)
						mob.session().rawPrintln("!");
					if(error.startsWith("Area Exists: "))
					{
						String areaName=error.substring(13).trim();
						if((!prompt)
						||(mob.session().confirm("Area: \""+areaName+"\" exists, obliterate first?","N")))
						{
							if(reLinkTable==null) reLinkTable=new Vector();
							if(mob.location().getArea().Name().equalsIgnoreCase(areaName))
								for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
								{
									Room R=(Room)r.nextElement();
									if((R!=null)&&(!R.getArea().Name().equalsIgnoreCase(areaName)))
									{
										R.bringMobHere(mob,true);
										break;
									}
								}
							if(!localDeleteArea(mob,reLinkTable,areaName))
								return false;
						}
						else
							return false;
					}
					else
					if(error.length()>0)
					{
						mob.tell("An error occurred on import: "+error);
						mob.tell("Please correct the problem and try the import again.");
						return false;
					}
					else
					{
						areas.removeElement(area);
						a++;
					}
				}
				Log.sysOut("Import",mob.Name()+" imported "+areaFileName);
				mob.tell("Area(s) successfully imported!");
				continue;
			}
			else
			if((buf.length()>20)&&(buf.substring(0,20).indexOf("<AREA>")>=0))
			{
				if(!CMSecurity.isAllowedEverywhere(mob,"IMPORTROOMS"))
				{
					mob.tell("You are not allowed to import area in '"+areaFileName+"'.");
					continue;
				}
				if(mob.session()!=null)
					mob.session().rawPrint("Unpacking area from file: '"+areaFileName+"'...");
				Vector areaD=new Vector();
				String error=CoffeeMaker.fillAreaAndCustomVectorFromXML(buf.toString(),areaD,custom,externalFiles);
				if(error.length()==0) Import.importCustomObjects(mob,custom,customBotherChecker,!prompt);
				if(error.length()==0) Import.importCustomFiles(mob,externalFiles,customBotherChecker,!prompt);
				if(error.length()==0)
					error=CoffeeMaker.unpackAreaFromXML(areaD,mob.session(),true);
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
				if(error.startsWith("Area Exists: "))
				{
					String areaName=error.substring(13).trim();
					if((!prompt)
					||(mob.session().confirm("Area: \""+areaName+"\" exists, obliterate first?","N")))
					{
						reLinkTable=new Vector();
						if(!localDeleteArea(mob,reLinkTable,areaName))
							return false;
					}
					else
						return false;
					if(mob.session()!=null)
						mob.session().rawPrint("Unpacking area from file: '"+areaFileName+"'...");
					error=CoffeeMaker.unpackAreaFromXML(buf.toString(),mob.session(),true);
					if(mob.session()!=null)
						mob.session().rawPrintln("!");
				}
				if(error.length()>0)
				{
					mob.tell("An error occurred on import: "+error);
					mob.tell("Please correct the problem and try the import again.");
					return false;
				}
				else
				{
					Log.sysOut("Import",mob.Name()+" imported "+areaFileName);
					mob.tell("Area successfully imported!");
					continue;
				}
			}
			else
			if((buf.length()>20)&&(buf.substring(0,20).indexOf("<AROOM>")>=0))
			{
				if(!CMSecurity.isAllowedEverywhere(mob,"IMPORTROOMS"))
				{
					mob.tell("You are not allowed to import room in '"+areaFileName+"'.");
					continue;
				}
				mob.tell("Unpacking room from file: '"+areaFileName+"'...");
				String error=CoffeeMaker.fillCustomVectorFromXML(buf.toString(),custom,externalFiles);
				if(error.length()==0) importCustomObjects(mob,custom,customBotherChecker,!prompt);
				if(error.length()==0) Import.importCustomFiles(mob,externalFiles,customBotherChecker,!prompt);
				if(error.length()==0)
					error=CoffeeMaker.unpackRoomFromXML(buf.toString(),true);
				if(error.startsWith("Room Exists: "))
				{
					Room R=CMMap.getRoom(error.substring(13).trim());
					if(R!=null)
					{
						reLinkTable=new Vector();
						for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
						{
							Room R2=(Room)r.nextElement();
							if(R2!=R)
							for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
							{
								Room dirR=R2.rawDoors()[d];
								if((dirR!=null)&&(dirR==R))
									reLinkTable.addElement(R2.roomID()+"/"+d+"/"+dirR.roomID());
							}
						}
						CoffeeUtensils.obliterateRoom(R);
					}
					error=CoffeeMaker.unpackRoomFromXML(buf.toString(),true);
				}
				if(error.length()>0)
				{
					mob.tell("An error occurred on import: "+error);
					mob.tell("Please correct the problem and try the import again.");
					return false;
				}
				else
				{
					Log.sysOut("Import",mob.Name()+" imported "+areaFileName);
					mob.tell("Room successfully imported!");
					continue;
				}
			}
			else
			if((buf.length()>20)&&(buf.substring(0,20).indexOf("<MOBS>")>=0))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"IMPORTMOBS"))
				{
					mob.tell("You are not allowed to import mobs in '"+areaFileName+"' here.");
					continue;
				}
				if(mob.session()!=null)
					mob.session().rawPrint("Unpacking mobs from file: '"+areaFileName+"'...");
				Vector mobs=new Vector();
				String error=CoffeeMaker.fillCustomVectorFromXML(buf.toString(),custom,externalFiles);
				if(error.length()==0) Import.importCustomObjects(mob,custom,customBotherChecker,!prompt);
				if(error.length()==0) Import.importCustomFiles(mob,externalFiles,customBotherChecker,!prompt);
				if(error.length()==0)
					error=CoffeeMaker.addMOBsFromXML(buf.toString(),mobs,mob.session());
				if(mob.session()!=null)	mob.session().rawPrintln("!");
				if(error.length()>0)
				{
					mob.tell("An error occurred on import: "+error);
					mob.tell("Please correct the problem and try the import again.");
					return false;
				}
				else
				{
					for(int m=0;m<mobs.size();m++)
					{
						MOB M=(MOB)mobs.elementAt(m);
						M.setStartRoom(mob.location());
						M.setLocation(mob.location());
						M.bringToLife(mob.location(),true);
					}
					mob.location().recoverRoomStats();
					Log.sysOut("Import",mob.Name()+" imported "+areaFileName);
					mob.tell("MOB(s) successfully imported!");
					continue;
				}
			}
			else
			if((buf.length()>20)&&(buf.substring(0,20).indexOf("<PLAYERS>")>=0))
			{
				if(!CMSecurity.isAllowedEverywhere(mob,"IMPORTPLAYERS"))
				{
					mob.tell("You are not allowed to import players in '"+areaFileName+"' here.");
					continue;
				}
				if(mob.session()!=null)
					mob.session().rawPrint("Unpacking players from file: '"+areaFileName+"'...");
				Vector mobs=new Vector();
				String error=CoffeeMaker.fillCustomVectorFromXML(buf.toString(),custom,externalFiles);
				if(error.length()==0) Import.importCustomObjects(mob,custom,customBotherChecker,!prompt);
				if(error.length()==0) Import.importCustomFiles(mob,externalFiles,customBotherChecker,!prompt);
				if(error.length()==0)
					error=CoffeeMaker.addPLAYERsFromXML(buf.toString(),mobs,mob.session());
				if(mob.session()!=null)	
					mob.session().rawPrintln("!");
				if(error.length()>0)
				{
					mob.tell("An error occurred on import: "+error);
					mob.tell("Please correct the problem and try the import again.");
					return false;
				}
				else
				{
					for(int m=0;m<mobs.size();m++)
					{
						MOB M=(MOB)mobs.elementAt(m);
						if(CMClass.DBEngine().DBUserSearch(null,M.Name()))
						{
							if(!prompt)
							{
								mob.tell("Player '"+M.Name()+"' already exists.  Skipping.");
								continue;
							}
							else
							if(!mob.session().confirm("Player: \""+M.Name()+"\" exists, obliterate first?","Y"))
								continue;
							else
								CoffeeUtensils.obliteratePlayer(CMMap.getLoadPlayer(M.Name()),false);
						}
						if(M.playerStats()!=null)
							M.playerStats().setUpdated(System.currentTimeMillis());
						CMClass.DBEngine().DBCreateCharacter(M);
						CMMap.addPlayer(M);
						Log.sysOut("Import","Imported user: "+M.Name());
						for(int s=0;s<Sessions.size();s++)
						{
							Session S=Sessions.elementAt(s);
							if((S!=null)
							&&(S.mob()!=null)
							&&((!Sense.isCloaked(M))||(CMSecurity.isASysOp(S.mob())))
							&&(Util.bset(S.mob().getBitmap(),MOB.ATT_AUTONOTIFY))
							&&(S.mob().playerStats()!=null)
							&&((S.mob().playerStats().getFriends().contains(M.Name())||S.mob().playerStats().getFriends().contains("All"))))
								S.mob().tell("^X"+M.Name()+" has just been created.^.^?");
						}
						CommonMsgs.channel("WIZINFO","",M.Name()+" has just been created.",true);
						if(M.getStartRoom()==null)
							M.setStartRoom(CMMap.getStartRoom(M));
						if(M.location()==null)
							M.setLocation(mob.location());
						if(M.playerStats().getBirthday()==null)
						{
						    M.baseCharStats().setStat(CharStats.AGE,M.playerStats().initializeBirthday((int)Math.round(Util.div(M.getAgeHours(),60.0)),M.baseCharStats().getMyRace()));
						    M.recoverCharStats();
						}
						CMClass.DBEngine().DBUpdateMOB(M);
						M.removeFromGame();
					}
					Log.sysOut("Import",mob.Name()+" imported "+areaFileName);
					mob.tell("PLAYER(s) successfully imported!");
					continue;
				}
			}
			else
			if((buf.length()>20)&&(buf.substring(0,20).indexOf("<ITEMS>")>=0))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"IMPORTITEMS"))
				{
					mob.tell("You are not allowed to import items in '"+areaFileName+"' here.");
					continue;
				}
				if(mob.session()!=null)
					mob.session().rawPrint("Unpacking items from file: '"+areaFileName+"'...");
				Vector items=new Vector();
				String error=CoffeeMaker.fillCustomVectorFromXML(buf.toString(),custom,externalFiles);
				if(error.length()==0) Import.importCustomObjects(mob,custom,customBotherChecker,!prompt);
				if(error.length()==0) Import.importCustomFiles(mob,externalFiles,customBotherChecker,!prompt);
				if(error.length()==0)
					error=CoffeeMaker.addItemsFromXML(buf.toString(),items,mob.session());
				if(mob.session()!=null)	mob.session().rawPrintln("!");
				if(error.length()>0)
				{
					mob.tell("An error occurred on import: "+error);
					mob.tell("Please correct the problem and try the import again.");
					return false;
				}
				else
				{
					for(int i=0;i<items.size();i++)
					{
						Item I=(Item)items.elementAt(i);
						mob.location().addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
					}
					mob.location().recoverRoomStats();
					Log.sysOut("Import",mob.Name()+" imported "+areaFileName);
					mob.tell("Item(s) successfully imported!");
					continue;
				}
			}
		}
		catch(Exception e)
		{
			Log.errOut("Import-",e);
			mob.tell(e.getMessage());
			return false;
		}


		Vector V=Resources.getFileLineVector(buf);

		// sort the data into general blocks, and identify area
		mob.tell("\n\rSorting data from file '"+areaFileName+"'...");
		readBlocks(V,areaData,roomData,mobData,resetData,objectData,mobProgData,objProgData,shopData,specialData,socialData);
		boolean didSocials=false;
		try
		{
			while(socialData.size()>0)
			{
				String codeLine=eatNextLine(socialData);
				if((!codeLine.startsWith("#"))&&(codeLine.trim().length()>0))
				{
					didSocials=true;
					String word=codeLine.trim().toUpperCase();
					int x=word.indexOf(" ");
					if(x>0) word=word.substring(0,x).trim();

					Social S1=Socials.FetchSocial(word,true);
					Social S2=Socials.FetchSocial(word+" <T-NAME>",true);
					Social S3=Socials.FetchSocial(word+" SELF",true);
					boolean changing=true;
					if((S1==null)||(!S1.name().toUpperCase().equals(word)))
					{
						S1=new Social();
						S1.setName(word);
						Socials.addSocial(S1);
						changing=false;
					}

					String str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S1.You_see()==null)||(!S1.You_see().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S1.name()+"' from '"+S1.You_see()+"', you see, to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S1.setYou_see(str);
					}

					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S1.Third_party_sees()==null)||(!S1.Third_party_sees().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S1.name()+"' from '"+S1.Third_party_sees()+"', others see, to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S1.setThird_party_sees(str);
					}

					changing=true;
					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;
					if(S2==null)
					{
						S2=new Social();
						S2.setName(word+" <T-NAME>");
						Socials.addSocial(S2);
						changing=false;
					}

					if((S2.You_see()==null)||(!S2.You_see().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S2.name()+"' from '"+S2.You_see()+"', you see, to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S2.setYou_see(str);
					}

					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S2.Third_party_sees()==null)||(!S2.Third_party_sees().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S2.name()+"', others see from '"+S2.Third_party_sees()+"', to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S2.setThird_party_sees(str);
					}

					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S2.Target_sees()==null)||(!S2.Target_sees().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S2.name()+"', target sees from '"+S2.Target_sees()+"', to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S2.setTarget_sees(str);
					}

					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S2.See_when_no_target()==null)||(!S2.See_when_no_target().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S2.name()+"', no target sees from '"+S2.See_when_no_target()+"', to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S2.setSee_when_no_target(str);
					}

					changing=true;
					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;
					if(S3==null)
					{
						S3=new Social();
						S3.setName(word+" SELF");
						Socials.addSocial(S3);
						changing=false;
					}

					if((S3.You_see()==null)||(!S3.You_see().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S3.name()+"', you see from '"+S3.You_see()+"', to: '"+str+"''");
						if((!changing)||(mob.session().confirm("?","Y")))
							S3.setYou_see(str);
					}

					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S3.Third_party_sees()==null)||(!S3.Third_party_sees().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S3.name()+"', others see from '"+S3.Third_party_sees()+"', to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S3.setThird_party_sees(str);
					}

				}
			}
			if(didSocials)
			{
				Log.sysOut("Import",mob.Name()+" imported socials from "+areaFileName);
				Socials.save();
			}
		}
		catch(Exception e)
		{
			Log.errOut("Import",e);
			mob.tell(e.getMessage());
			return false;
		}

		if((roomData.size()==0)||(areaData.size()==0))
		{
			if(!didSocials)
			{
				if(multiArea)
				{
					mob.tell("No data in "+areaFileName);
					if((prompt)&&(mob.session()!=null))
					{
						try{
						if(!mob.session().confirm("Would you like to continue (y/N)","N"))
							return false;
						}catch(Exception e){}
					}
					continue;
				}
				else
				{
					mob.tell("Missing data! It is very unlikely this is an .are file.");
					return false;
				}
			}
		}
		String areaName=getAreaName(areaData);
		String areaAuthor=getAreaAuthor(areaData);
		if((areaName==null)||((areaName!=null)&&(areaName.length()==0)))
		{
			if(!didSocials)
				mob.tell("#AREA tag not found!");
			if(multiArea)
				continue;
			else
				return false;
		}
		if((areaName.toUpperCase().startsWith(areaAuthor.toUpperCase()+" "))
		&&(areaName.substring(areaAuthor.length()+1).trim().length()>0))
			areaName=areaName.substring(areaAuthor.length()+1).trim();

		try
		{
			// confirm area creation/overwrite
			boolean exists=false;
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.getArea().Name().equalsIgnoreCase(areaName))
				{
					exists=true;
					break;
				}
			}
			if(exists)
			{
				if((!prompt)
				||(mob.session().confirm("Area: \""+areaName+"\" exists, obliterate first?","N")))
				{
					reLinkTable=new Vector();
					if(!localDeleteArea(mob,reLinkTable,areaName))
						return false;
				}
				else
					return false;
			}
			else
			if((prompt)&&(!mob.session().confirm("Found area: \""+areaName+"\", is this ok?","Y")))
				return false;

			mob.tell("Loading and Linking Rooms...");
			Log.sysOut("Import",mob.Name()+" imported "+areaName+" from "+areaFileName);
			// begin initial room-read
			// build first room structures, leaving rest for later.
			Room lastRoom=null;
			Hashtable petShops=new Hashtable();

			for(int r=0;r<roomData.size();r++)
			{
				Vector roomV=null;
				if(roomData.elementAt(r) instanceof Vector)
					roomV=(Vector)roomData.elementAt(r);
				else
				if(roomData.elementAt(r) instanceof String)
				{
					String s=(String)roomData.elementAt(r);
					if(!s.toUpperCase().trim().startsWith("#ROOM"))
						returnAnError(mob,"Eating immaterial line: "+roomData.elementAt(r)+", area="+areaName);
					continue;
				}
				else
					continue;

				Area A=CMMap.getArea(areaName);
				if(A==null)
				{
					A=CMClass.DBEngine().DBCreateArea(areaName,"StdArea");
					A.setAuthorID(areaAuthor);
					CMClass.DBEngine().DBUpdateArea(areaName,A);
				}
				else
					A.toggleMobility(false);
				
				Room R=CMClass.getLocale("StdRoom");
				String plainRoomID=eatNextLine(roomV);
				R.setRoomID(plainRoomID);

				if((roomV.size()>2)
				&&(((String)roomV.elementAt(0)).trim().equals("~"))
				&&(((String)roomV.elementAt(1)).trim().equals("~")))
				{
					eatLineSquiggle(roomV);
					eatLineSquiggle(roomV);
					R.setDisplayText("Emptiness...");
					R.setDescription("");
				}
				else
				{
					R.setDisplayText(CoffeeFilter.safetyFilter(eatLineSquiggle(roomV)));
					R.setDescription(CoffeeFilter.safetyFilter(eatLineSquiggle(roomV)));
				}
				R.setArea(A);
				String codeLine=eatNextLine(roomV);
				if((!R.roomID().startsWith("#"))
				||(R.displayText().length()==0)
				||(Util.numBits(codeLine)<2)
				||(Util.numBits(codeLine)>3))
				{
					returnAnError(mob,"Malformed room! Aborting this room "+R.roomID()+", display="+R.displayText()+", description="+R.description()+", numBits="+Util.numBits(codeLine)+", area="+areaName);
					continue;
				}
				else
					R.setRoomID(areaName+R.roomID());
				int codeBits=getBitMask(codeLine,0);
				int sectorType=getBitMask(codeLine,1);
				final String[][] secTypes={
				{ "inside",		"0"},
				{ "city",		"1"},
				{ "field",		"2"},
				{ "forest",		"3"},
				{ "hills",		"4"},
				{ "mountain",	"5"},
				{ "swim",		"6"},
				{ "noswim",		"7"},
				{ "unused",		"8"},
				{ "air",		"9"},
				{ "desert",		"10"}};

				if(Util.numBits(codeLine)==3)
				{
					codeBits=sectorType;
					String secType=Util.getBit(codeLine,2);
					sectorType=getBitMask(codeLine,2);
					for(int st=0;st<secTypes.length;st++)
						if(secType.equalsIgnoreCase(secTypes[st][0]))
						{
							sectorType=Util.s_int(secTypes[st][1]);
							break;
						}
				}
				if((codeBits&8)==0)
				{
					switch(sectorType)
					{
					case 0:	R=changeRoomClass(R,"CityStreet"); break;
					case 1:	R=changeRoomClass(R,"CityStreet"); break;
					case 2:	R=changeRoomClass(R,"Plains"); break;
					case 3:	R=changeRoomClass(R,"Woods"); break;
					case 4:	R=changeRoomClass(R,"Hills"); break;
					case 5:	R=changeRoomClass(R,"Mountains"); break;
					case 6:	R=changeRoomClass(R,"ShallowWater"); break;
					case 7:	R=changeRoomClass(R,"WaterSurface"); break;
					case 8:	R=changeRoomClass(R,"FrozenPlains"); break;
					case 9:	R=changeRoomClass(R,"InTheAir"); break;
					case 10: R=changeRoomClass(R,"Desert"); break;
					case 11: R=changeRoomClass(R,"FrozenPlains"); break;
					case 12: R=changeRoomClass(R,"FrozenMountains"); break;
					}
				}
				else
				{
					switch(sectorType)
					{
 					case 0:	R=changeRoomClass(R,"StoneRoom"); break;
					case 1:	R=changeRoomClass(R,"StoneRoom"); break;
					case 2:	R=changeRoomClass(R,"WoodRoom"); break;
					case 3:	R=changeRoomClass(R,"WoodRoom"); break;
					case 4:	R=changeRoomClass(R,"StoneRoom"); break;
					case 5:	R=changeRoomClass(R,"StoneRoom"); break;
					case 6:	R=changeRoomClass(R,"IndoorShallowWater"); break;
					case 7:	R=changeRoomClass(R,"IndoorWaterSurface"); break;
					case 8:	R=changeRoomClass(R,"IceRoom"); break;
					case 9:	R=changeRoomClass(R,"IndoorInTheAir"); break;
					case 10: R=changeRoomClass(R,"HotRoom"); break;
					case 11: R=changeRoomClass(R,"IceRoom"); break;
					case 12: R=changeRoomClass(R,"IceRoom"); break;
					}
					switch(sectorType)
					{
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 10:
					case 11:
					case 12:
						if((R.displayText().toUpperCase().indexOf("CAVE")>=0)
						||(R.description().toUpperCase().indexOf("CAVE")>=0))
							R=changeRoomClass(R,"CaveRoom");
						break;
					}

				}

				Ability prop_RoomCapacity=CMClass.getAbility("Prop_ReqCapacity");
				Ability prop_RoomLevels=CMClass.getAbility("Prop_ReqLevels");


				if(Util.isSet(codeBits,21)) // underwater room
					R=changeRoomClass(R,"UnderWater");

				//if(Util.isSet(codeBits,1)) //BANKS are forked up in the ROM files, who knows WHAT this is...

				if(Util.isSet(codeBits,0)) // dark room
					R.addNonUninvokableEffect(CMClass.getAbility("Prop_RoomDark"));

				if(Util.isSet(codeBits,2)) // no mobs room
					R.addNonUninvokableEffect(CMClass.getAbility("Prop_ReqNoMOB"));

				if(Util.isSet(codeBits,4)) // no summon out room
					R.addNonUninvokableEffect(CMClass.getAbility("Prop_NoSummon"));

				if(Util.isSet(codeBits,9)) // two people only room
				{
					prop_RoomCapacity.setMiscText("2");
					if(R.fetchEffect(prop_RoomCapacity.ID())==null)
						R.addNonUninvokableEffect(prop_RoomCapacity);
				}
				if(Util.isSet(codeBits,10)) // no fighting
					R.addNonUninvokableEffect(CMClass.getAbility("Prop_PeaceMaker"));

				if(Util.isSet(codeBits,11)) // solitaire room
				{
					prop_RoomCapacity.setMiscText("1");
					if(R.fetchEffect(prop_RoomCapacity.ID())==null)
						R.addNonUninvokableEffect(prop_RoomCapacity);
				}
				if(Util.isSet(codeBits,12))
					petShops.put(R,R);
				else
				if((lastRoom!=null)&&(petShops.get(lastRoom)!=null)&&(petShops.get(lastRoom)==lastRoom))
				{
					petShops.remove(lastRoom);
					petShops.put(R,lastRoom); // now ready to plop stuff!
				}

				if(Util.isSet(codeBits,13))
					R.addNonUninvokableEffect(CMClass.getAbility("Prop_NoRecall"));

				if(Util.isSet(codeBits,14))
				{
					prop_RoomLevels.setMiscText("SYSOP");
					if(R.fetchEffect(prop_RoomLevels.ID())==null)
						R.addNonUninvokableEffect(prop_RoomLevels);
				}
				if(Util.isSet(codeBits,15))
				{
					prop_RoomLevels.setMiscText(">=93");
					if(R.fetchEffect(prop_RoomLevels.ID())==null)
						R.addNonUninvokableEffect(prop_RoomLevels);
				}
				if(Util.isSet(codeBits,16))
				{
					prop_RoomLevels.setMiscText(">=91");
					if(R.fetchEffect(prop_RoomLevels.ID())==null)
						R.addNonUninvokableEffect(prop_RoomLevels);
				}
				if(Util.isSet(codeBits,17))
				{
					prop_RoomLevels.setMiscText("<=5");
					if(R.fetchEffect(prop_RoomLevels.ID())==null)
						R.addNonUninvokableEffect(prop_RoomLevels);
				}

				if(Util.isSet(codeBits,18))
				{
					R.addNonUninvokableEffect(CMClass.getAbility("Prop_NoSummon"));
					R.addNonUninvokableEffect(CMClass.getAbility("Prop_NoCharm"));
				}

				if(Util.isSet(codeBits,19))
					R.addNonUninvokableEffect(CMClass.getAbility("Prop_reqPKill"));

				if(Util.isSet(codeBits,20))
					R.addNonUninvokableEffect(CMClass.getAbility("Prop_NoTeleportOut"));

				// if(Util.isSet(codeBits,23)) No "dirt" in CoffeeMud, so this doesn't matter

				if(Util.isSet(codeBits,24))
					R.addNonUninvokableEffect(CMClass.getAbility("Prop_NoChannel"));

				roomV.insertElementAt(R.roomID(),0);
				CMMap.addRoom(R);
				newRooms.addElement(R);
				if(plainRoomID.startsWith("#"))
					doneRooms.put(plainRoomID.substring(1),R);
				else
					doneRooms.put(plainRoomID.substring(1),R);

				lastRoom=R;
			}

			// begin second pass through rooms
			// handle exits, mobs, objects, etc.
			for(int r=0;r<roomData.size();r++)
			{
				Vector roomV=null;
				if(roomData.elementAt(r) instanceof Vector)
					roomV=(Vector)roomData.elementAt(r);
				else
					continue;
				String roomID=eatLine(roomV);
				Room R=findRoomSomewhere(roomID,areaName,doneRooms);
				if(R==null)
				{
					Log.errOut("Import","Unhashed room "+roomID+"! Aborting!");
					return false;
				}

				// handle exits, and 'E' tags
				while(roomV.size()>0)
				{
					String nextLine=eatNextLine(roomV);
					if(nextLine.toUpperCase().startsWith("S"))
						continue;
					else
					if(nextLine.toUpperCase().startsWith("E"))
					{
						String nameString=CoffeeFilter.safetyFilter(eatLineSquiggle(roomV));
						String descString=CoffeeFilter.safetyFilter(eatLineSquiggle(roomV));
						Item I=null;
						if(hasReadableContent(nameString))
						{
							I=CMClass.getStdItem("GenWallpaper");
							Sense.setReadable(I,true);
							I.setReadableText(fixReadableContent(descString));
						}
						else
							I=CMClass.getStdItem("GenWallpaper");
						I.setName(nameString);
						I.setDisplayText("");
						I.setDescription(descString);
						R.addItem(I);
					}
					else
					if(nextLine.toUpperCase().startsWith("D"))
					{
						int dirCode=Util.s_int(Util.getCleanBit(nextLine,0).substring(1).trim());
						String descStr=CoffeeFilter.safetyFilter(eatLineSquiggle(roomV));
						String nameStr=CoffeeFilter.safetyFilter(eatLineSquiggle(roomV));
						String codeStr=eatLine(roomV);
						switch(dirCode)
						{
						case 0: dirCode=Directions.NORTH; break;
						case 1: dirCode=Directions.EAST; break;
						case 2: dirCode=Directions.SOUTH; break;
						case 3: dirCode=Directions.WEST; break;
						case 4: dirCode=Directions.UP; break;
						case 5: dirCode=Directions.DOWN; break;
						case 6:
						case 7:
						case 8:
						case 9:
							for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
								if(R.rawDoors()[d]==null)
								{
									dirCode=d;
									break;
								}
							break;
						case 10: dirCode=Directions.GATE; break;
						default:
								returnAnError(mob,"Room: "+R.roomID()+", Unknown direction code: "+dirCode+", aborting exit, area="+areaName);
								continue;
						}
						if((dirCode<0)||(dirCode>=Directions.NUM_DIRECTIONS))
						{
							returnAnError(mob,"Room: "+R.roomID()+", Unknown direction code: "+dirCode+", aborting exit, area="+areaName);
							continue;
						}
						if(Util.numBits(codeStr)!=3)
						{
							returnAnError(mob,"Room: "+R.roomID()+", Malformed exit codeStr "+codeStr+".  Aborting exit, area="+areaName);
							continue;
						}
						if((R.rawExits()[dirCode]!=null)||(R.rawDoors()[dirCode]!=null))
						{
							returnAnError(mob,"Room: "+R.roomID()+", Redundant exit codeStr "+nextLine+"/"+codeStr+", dircode="+dirCode+".  Aborting exit, area="+areaName);
							continue;
						}
						int exitFlag=( Util.s_int(Util.getCleanBit(codeStr,0)) & 31);
						int doorState=Util.s_int(Util.getCleanBit(codeStr,1));
						int linkRoomID=Util.s_int(Util.getCleanBit(codeStr,2));
						Exit E=CMClass.getExit("GenExit");
						Room linkRoom=findRoomSomewhere(""+linkRoomID,areaName,doneRooms);
						if(linkRoomID>=0)
						{
							boolean hasDoor=false;
							boolean hasLock=false;
							boolean defaultsClosed=false;
							boolean defaultsLocked=false;

							if((exitFlag==1)||(exitFlag==6))
							{
								hasDoor=true;
								defaultsClosed=true;
								if(exitFlag==6)
								{
									E.baseEnvStats().setLevel(100);
									E.recoverEnvStats();
								}
							}
							if(doorState<0)
								defaultsClosed=false;
							else
							if(doorState>0)
							{
								hasDoor=true;
								defaultsClosed=true;
								if(doorState>1)
								{
									hasLock=true;
									defaultsLocked=true;
									E.setKeyName(areaName+"#"+doorState);
								}
							}
							E.setDoorsNLocks(hasDoor,!defaultsClosed,defaultsClosed,
											 hasLock,defaultsLocked,defaultsLocked);
						}
						E.setDisplayText(descStr);
						String name=Util.getCleanBit(nameStr,0).trim();
						if(name.equalsIgnoreCase("SECRET"))
						{
							name="secret door";
							E.baseEnvStats().setDisposition(E.baseEnvStats().disposition()|EnvStats.IS_HIDDEN);
							E.recoverEnvStats();
						}

						if(name.length()>0)
							E.setName(Util.startWithAorAn(name));
						else
						{
							if(E.hasADoor())
							{
								E.setName("a door");
								name="door";
							}
							else
							{
								E.setName("the ground");
								name="ground";
							}
						}
						E.setExitParams(name,E.closeWord(),E.openWord(),E.Name()+", closed");
						E.setDescription(descStr);
						R.rawExits()[dirCode]=E;
						Exit opExit=null;
						if(((linkRoom==null)||(linkRoom.getArea().Name()!=R.getArea().Name()))&&(linkRoomID>=0))
						{
							for(Enumeration r2=CMMap.rooms();r2.hasMoreElements();)
							{
								Room R2=(Room)r2.nextElement();
								if((R2.roomID().endsWith("#"+linkRoomID))&&(R2!=R))
								{
									for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
									{
										Exit E3=R2.rawExits()[d];
										if((E3!=null)
										&&(E3.temporaryDoorLink().length()>0)
										&&(R.roomID().endsWith(E3.temporaryDoorLink())))
										{
											opExit=E3;
											R2.rawDoors()[d]=R;
										}
									}
									if(opExit==null)
										if((prompt)&&
										  (!mob.session().confirm(R.roomID()+" links to #"+linkRoomID+". Found "+R2.roomID()+". Link?","Y")))
											continue;
									linkRoom=R2;
									if(opExit!=null) opExit.setTemporaryDoorLink("");
									if((!doneRooms.containsValue(linkRoom))&&(!doneRooms.contains(linkRoom)))
										CMClass.DBEngine().DBUpdateExits(linkRoom);
									break;
								}
							}
							if(linkRoom==null)
								E.setTemporaryDoorLink("#"+linkRoomID);
							else
								E.setTemporaryDoorLink("");

						}
						R.rawDoors()[dirCode]=linkRoom;
						if((linkRoom==null)&&(linkRoomID>=0))
						{
							if(multiArea)
								laterLinks.put((R.roomID()+"/"+dirCode),"#"+linkRoomID);
							else
								returnAnError(mob,"Room: "+R.roomID()+" links "+Directions.getDirectionName(dirCode)+"ward to unknown room #"+linkRoomID+", area="+areaName);
						}
					}
					else
					if(nextLine.toUpperCase().startsWith("M"))
					{
						// mana heal rate
						// not important enough to generate an error from
					}
					else
					if(nextLine.toUpperCase().startsWith("H"))
					{
						// hit point heal rate
						// not important enough to generate an error from
					}
					else
					if(nextLine.startsWith(">"))
					{
						String scriptStuff="";
						if(!nextLine.substring(1).trim().toUpperCase().startsWith("IN_FILE_PROG"))
						{
							scriptStuff+=nextLine.substring(1).trim()+";";
							nextLine=nextLine(roomV);
							while(nextLine.indexOf("~")<0)
							{
								scriptStuff+=nextLine.trim()+";";
								eatLine(roomV);
								nextLine=nextLine(roomV);
							}
							nextLine=eatLineSquiggle(roomV).trim();
							scriptStuff+=nextLine+"~";
						}
						// nothing done with the script. :(
					}
					else
					if(nextLine.toUpperCase().startsWith("O"))
					{
						// ?
						// not important enough to generate an error from
					}
					else
					if(nextLine.startsWith("Rd"))
					{
						// some sort of eos thang
						// not important enough to generate an error from
					}
					else
					if(nextLine.trim().equals("|"))
					{
						// ?
						// not important enough to generate an error from
					}
					else
					if((!nextLine.equalsIgnoreCase("#0"))&&(nextLine.trim().length()>0))
						returnAnError(mob,"Unknown room code: "+nextLine+", area="+areaName);
				}
			}

			mob.session().print("Loading objects..");
			Hashtable containerHash=new Hashtable();
			MOB M=null;
			Room R=null;
			for(int nrd=0;nrd<nextResetData.size();nrd++)
				resetData.addElement(nextResetData.elementAt(nrd));
			nextResetData.clear();
			while(resetData.size()>0)
			{
				mob.session().print(".");
				String s=eatNextLine(resetData).trim();
				if((s.startsWith("#RE"))||(s.startsWith("*"))||(s.startsWith("S")))
				{
				}
				else
				if(s.startsWith("M "))
				{
					String mobID=Util.getCleanBit(s,2);
					String roomID=Util.getCleanBit(s,4);
					R=findRoomSomewhere(roomID,areaName,doneRooms);
					if(R==null)
					{
						if(multiArea)
							nextResetData.addElement(s);
						else
							returnAnError(mob,"Reset error (no room) on line: "+s+", area="+areaName);
					}
					else
					{
						M=getMOB("#"+mobID,R,mob,Util.copyVector(mobData),Util.copyVector(mobProgData),Util.copyVector(specialData),Util.copyVector(shopData),doneMOBS,areaFileName);
						if(M==null)
						{
							if(multiArea)
								nextResetData.addElement(s);
							else
								returnAnError(mob,"Reset error (no mob) on line: "+s+", area="+areaName);
						}
						else
							M.bringToLife(R,true);
					}
				}
				else
				if(s.startsWith("G "))
				{
					if(M==null)
					{
						if(multiArea)
							nextResetData.addElement(s);
						else
							returnAnError(mob,"Reset error (no mob) on line: "+s+", area="+areaName);
					}
					else
					{
						String itemID=Util.getCleanBit(s,2);
						Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData),doneItems,doneRooms);
						if(I==null)
						{
							if(multiArea) nextResetData.addElement(s);
							else
							returnAnError(mob,"Reset error (no item) on line: "+s+", area="+areaName);
						}
						else
						{
							I.recoverEnvStats();
							if(M instanceof ShopKeeper)
							{
								int num=Util.s_int(Util.getCleanBit(s,3));
								if(num<0) num=100;
								((ShopKeeper)M).addStoreInventory(I,num,-1);
								if((I instanceof Light)&&(!((ShopKeeper)M).doIHaveThisInStock("OilFlask",null)))
									((ShopKeeper)M).addStoreInventory(CMClass.getStdItem("OilFlask"),num*2,-1);
								else
								if(((I.ID().equals("GenReadable"))
								||(I instanceof com.planet_ink.coffee_mud.interfaces.Map))
								&&(!((ShopKeeper)M).doIHaveThisInStock("Parchment",null)))
								{
									((ShopKeeper)M).setWhatIsSold(ShopKeeper.DEAL_INVENTORYONLY);
									((ShopKeeper)M).addStoreInventory(CMClass.getStdItem("Parchment"),num,-1);
									Item journal1=CMClass.getStdItem("GenJournal");
									journal1.setName("the bug journal");
									journal1.setBaseValue(250);
									journal1.recoverEnvStats();
									journal1.text();
									((ShopKeeper)M).addStoreInventory(journal1,num,-1);
									Item journal2=CMClass.getStdItem("GenJournal");
									journal2.setName("the adventurers journal");
									journal2.setBaseValue(250);
									journal2.recoverEnvStats();
									journal2.text();
									((ShopKeeper)M).addStoreInventory(journal2,num,-1);
									Item journal3=CMClass.getStdItem("GenJournal");
									journal3.setName("a feature guide");
									journal3.setBaseValue(500);
									journal3.recoverEnvStats();
									journal3.text();
									((ShopKeeper)M).addStoreInventory(journal3,num,-1);
								}
								else
								if(((ShopKeeper)M).whatIsSold()==ShopKeeper.DEAL_WEAPONS)
								{
									Item arrows=CMClass.getStdItem("GenAmmunition");
									((Ammunition)arrows).setAmmunitionType("arrows");
									arrows.setName("a pack of 20 arrows");
									arrows.setUsesRemaining(20);
									arrows.setBaseValue(50);
									arrows.setDescription("They are sturdy and wooden, but probably not much use without a bow.");
									arrows.setDisplayText("Some arrows have been left here.");
									arrows.recoverEnvStats();
									arrows.text();
									((ShopKeeper)M).addStoreInventory(arrows,num,-1);
								}
							}
							else
								M.addInventory(I);
							I.recoverEnvStats();
							M.recoverCharStats();
							M.recoverEnvStats();
							M.recoverMaxState();
							M.text();
							if(I instanceof Container)
								containerHash.put(itemID,I);
						}
					}
				}
				else
				if(s.startsWith("EC "))
				{
					String roomID=Util.getCleanBit(s,1);
					String mobID=Util.getCleanBit(s,2);
					int x=roomID.lastIndexOf("#");
					if(x>=0) roomID=roomID.substring(x);
					Room R2=findRoomSomewhere(roomID,areaName,doneRooms);
					MOB M2=null;
					if(R2!=null)
						M2=R2.fetchInhabitant(mobID);
					if((R2==null)||(M2==null))
					{
						if(multiArea)
							nextResetData.addElement(s);
						else
							returnAnError(mob,"Reset error (no mob) on line: "+s+", area="+areaName);
					}
					else
					{
						String itemID=Util.getCleanBit(s,5);
						Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData),doneItems,doneRooms);
						if(I==null)
						{
							if(multiArea)
								nextResetData.addElement(s);
							else
								returnAnError(mob,"Reset error (no item) on line: "+s+", area="+areaName);
						}
						else
						{
							M2.addInventory(I);
							I.wearIfPossible(M2);
							I.recoverEnvStats();
							M2.recoverCharStats();
							M2.recoverEnvStats();
							M2.recoverMaxState();
							M2.text();
							if(I instanceof Container)
								containerHash.put(itemID,I);
						}
					}
				}
				else
				if(s.startsWith("E "))
				{
					if(M==null)
					{
						if(multiArea) nextResetData.addElement(s);
						else
						returnAnError(mob,"Reset error (no mob) on line: "+s+", area="+areaName);
					}
					else
					{
						String itemID=Util.getCleanBit(s,2);
						Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData),doneItems,doneRooms);
						if(I==null)
						{
							if(multiArea)
							{
								if(M.location()!=null)
									nextResetData.addElement("EC '"+M.location().roomID()+"' '"+M.Name()+"' "+s);
								else
									nextResetData.addElement(s);
							}
							else
								returnAnError(mob,"Reset error (no item) on line: "+s+", area="+areaName);
						}
						else
						{
							M.addInventory(I);
							I.wearIfPossible(M);
							I.recoverEnvStats();
							M.recoverCharStats();
							M.recoverEnvStats();
							M.recoverMaxState();
							M.text();
							if(I instanceof Container)
								containerHash.put(itemID,I);
						}
					}
				}
				else
				if(s.startsWith("O "))
				{
					String itemID=Util.getCleanBit(s,2);
					String roomID=Util.getCleanBit(s,4);
					R=findRoomSomewhere(roomID,areaName,doneRooms);
					if(R==null)
					{
						if(multiArea) nextResetData.addElement(s);
						else
						returnAnError(mob,"Reset error (no room) on line: "+s+"/"+roomID+"/"+roomID.length()+", area="+areaName);
					}
					else
					{
						Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData),doneItems,doneRooms);
						if(I==null)
						{
							if(multiArea) nextResetData.addElement(s);
							else
							returnAnError(mob,"Reset error (no item) on line: "+s+", area="+areaName);
						}
						else
						{
							R.addItem(I);
							if(Sense.isGettable(I))
							{
								int rejuv=(int)Math.round(Util.div((long)60000,MudHost.TICK_TIME)*4.0);
								I.baseEnvStats().setRejuv(rejuv*I.baseEnvStats().level());
							}
							I.recoverEnvStats();
							if(I instanceof Container)
							{
								containerHash.remove(itemID);
								containerHash.put(itemID,I);
							}
						}
					}
				}
				else
				if(s.startsWith("P "))
				{
					String itemID=Util.getCleanBit(s,2);
					String containerID=Util.getCleanBit(s,4);
					Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData),doneItems,doneRooms);
					Container C=(Container)containerHash.get(containerID);
					if(I==null)
					{
						if(multiArea) nextResetData.addElement(s);
						else
						returnAnError(mob,"Reset error (no item) on line: "+s+", area="+areaName);
					}
					else
					if(C==null)
					{
						if(multiArea) nextResetData.addElement(s);
						else
						returnAnError(mob,"Reset error (no container) on line: "+s+", area="+areaName);
					}
					else
					if(C.owner()==null)
					{
						if(multiArea) nextResetData.addElement(s);
						else
						returnAnError(mob,"Reset error (no container owner) on line: "+s+", area="+areaName);
					}
					else
					if(C.owner() instanceof Room)
					{
						Room RR=(Room)C.owner();
						RR.addItem(I);
						I.setContainer(C);
						if(Sense.isGettable(I))
							I.baseEnvStats().setRejuv(1000);
						I.recoverEnvStats();
						if(I instanceof Container)
							containerHash.put(itemID,I);
					}
					else
					if(C.owner() instanceof MOB)
					{
						MOB MM=(MOB)C.owner();
						MM.addInventory(I);
						I.setContainer(C);
						M.text();
						I.recoverEnvStats();
						if(I instanceof Container)
							containerHash.put(itemID,I);
					}
				}
				else
				if(s.startsWith("D "))
				{
					String roomID=Util.getCleanBit(s,2);
					int dirCode=getBitMask(s,3);
					R=findRoomSomewhere(roomID,areaName,doneRooms);
					if(R==null)
					{
						if(multiArea) nextResetData.addElement(s);
						else
						returnAnError(mob,"Reset error (no room) on line: "+s+", area="+areaName);
					}
					else
					{
						switch(dirCode)
						{
						case 0: dirCode=Directions.NORTH; break;
						case 1: dirCode=Directions.EAST; break;
						case 2: dirCode=Directions.SOUTH; break;
						case 3: dirCode=Directions.WEST; break;
						case 4: dirCode=Directions.UP; break;
						case 5: dirCode=Directions.DOWN; break;
						case 6:
						case 7:
						case 8:
						case 9:
							for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
								if((R.rawExits()[d]!=null)
								&&(!R.rawExits()[d].hasADoor()))
								{
									dirCode=d;
									break;
								}
							break;
						case 10: dirCode=Directions.GATE; break;
						default:
							returnAnError(mob,"Room: "+R.roomID()+", Unknown direction code: "+dirCode+" (not so bad at this point, it was probably aborted earlier, area="+areaName);
						}
						if(dirCode<Directions.NUM_DIRECTIONS)
						{
							Exit E=R.rawExits()[dirCode];
							if(E==null)
								returnAnError(mob,"Room: "+R.roomID()+", Unknown exit in dir: "+dirCode+" very confusing!, area="+areaName);
							else
							{
								int lockBit=getBitMask(s,4);
								boolean HasDoor=E.hasADoor();
								boolean HasLock=E.hasALock();
								boolean DefaultsClosed=E.defaultsClosed();
								boolean DefaultsLocked=E.defaultsLocked();
								boolean Open=E.isOpen();
								boolean Locked=E.isLocked();
								switch(lockBit)
								{
								case 0:
									HasDoor=true;
									Locked=false;
									DefaultsLocked=false;
									Open=true;
									DefaultsClosed=false;
									break;
								case 1:
									HasDoor=true;
									Locked=false;
									DefaultsLocked=false;
									Open=false;
									DefaultsClosed=true;
									break;
								case 2:
									HasDoor=true;
									Locked=true;
									DefaultsLocked=true;
									Open=false;
									DefaultsClosed=true;
									break;
								default:
									returnAnError(mob,"Room: "+R.roomID()+", Unknown door code: "+lockBit+", area="+areaName);
									break;
								}
								E.setDoorsNLocks(HasDoor,Open,DefaultsClosed,HasLock,Locked,DefaultsLocked);
								if(E.hasADoor()&&E.name().equals("the ground"))
								{
									E.setName("a door");
									E.setExitParams("door","close","open","a door, closed.");
								}
							}
						}
					}
				}
				else
				if(s.startsWith("R "))
				{
					// have no idea what this is, but its not important.
				}
				else
				if(s.startsWith("*"))
				{
					// usually a comment of some sort
				}
				else
				if(s.length()>0)
					returnAnError(mob,"Reset, unknown command: "+s+", area="+areaName);

			}
			// now fix the pet shops!
			for(Enumeration e=petShops.keys();e.hasMoreElements();)
			{
				Room storeRoom=(Room)e.nextElement();
				Room shopRoom=(Room)petShops.get(storeRoom);
				ShopKeeper shopKeeper=null;
				if(shopRoom==null)
					returnAnError(mob,"Unknown store room: "+storeRoom.roomID()+", area="+areaName);
				else
				for(int i=0;i<shopRoom.numInhabitants();i++)
				{
					MOB sk=shopRoom.fetchInhabitant(i);
					if((sk!=null)&&(sk instanceof ShopKeeper))
					{ shopKeeper=(ShopKeeper)sk; break;	}
				}
				if(shopKeeper==null)
					returnAnError(mob,"Unknown shopkeeper not in room: "+storeRoom.roomID()+", area="+areaName);
				else
				while(storeRoom.numInhabitants()>0)
				{
					shopKeeper.setWhatIsSold(ShopKeeper.DEAL_PETS);
					MOB pet=storeRoom.fetchInhabitant(0);
					if(pet!=null)
					{
						shopKeeper.addStoreInventory(pet,20,-1);
						pet.setFollowing(null);
						pet.destroy();
					}
				}
			}
			// now fix the smurfy wells
			for(int r=0;r<newRooms.size();r++)
			{
				Room smurfRoom=(Room)newRooms.elementAt(r);
				for(int ei=0;ei<smurfRoom.numItems();ei++)
				{
					Item lookItem=smurfRoom.fetchItem(ei);
					if((lookItem!=null)&&(lookItem.displayText().length()==0))
					{
						for(int i=0;i<smurfRoom.numItems();i++)
						{
							Item I=smurfRoom.fetchItem(i);
							if((I!=null)
							&&(I.displayText().length()>0)
							&&(I.displayText().indexOf(lookItem.Name())>=0))
							{
								String description=lookItem.description();
								smurfRoom.delItem(lookItem);

								Item testItem=CMClass.getItem(I.ID());
								if((testItem!=null)&&(testItem.description().equals(I.description())))
									I.setDescription(description);
								else
									I.setDescription(I.description()+"%0D"+description);
								ei=ei-1;
								break;
							}
						}
					}
				}
			}
			mob.session().print("\n\rResets...");

			// try to re-link olde room links
			if(reLinkTable!=null)
				for(int r=0;r<reLinkTable.size();r++)
				{
					String link=(String)reLinkTable.elementAt(r);
					String nextLink="";
					if(r<(reLinkTable.size()-1))
						nextLink=(String)reLinkTable.elementAt(r+1);
					int s1=link.indexOf("/");
					int s2=link.lastIndexOf("/");
					String sourceRoomID=link.substring(0,s1);
					int direction=Util.s_int(link.substring(s1+1,s2));
					String destRoomID=link.substring(s2+1);
					Room sourceRoom=findRoomSomewhere(sourceRoomID,areaName,doneRooms);
					Room destRoom=findRoomSomewhere(destRoomID,areaName,doneRooms);
					if((sourceRoom==null)||(destRoom==null))
						Log.errOut("Import","Relink error: "+sourceRoomID+"="+sourceRoom+"/"+destRoomID+"="+destRoom);
					else
					{
						sourceRoom.rawDoors()[direction]=destRoom;
						if(((!doneRooms.containsValue(sourceRoom)))
						&&((nextLink.length()==0)||(!nextLink.startsWith(sourceRoomID+"/"))))
							CMClass.DBEngine().DBUpdateExits(sourceRoom);
					}
				}

			if(newRooms.size()==0)
				mob.session().println("\nDone? No Room!\n\r");
			else
			if(!multiArea)
				mob.session().println("\nDone!!!!!!  A good room to look at would be "+((Room)newRooms.elementAt(0)).roomID()+"\n\r");
			else
				mob.session().println("Done!!!\n\r");
		}
		catch(Exception e)
		{
			Log.errOut("Import",e);
			mob.tell(e.getMessage());
			return false;
		}
		}

		if(nextResetData.size()>0)
		{
			StringBuffer nrf=new StringBuffer("Import bad resets:\n\r");
			for(int nrd=0;nrd<nextResetData.size();nrd++)
				nrf.append(((String)nextResetData.elementAt(nrd))+"\n\r");
			mob.session().print(nrf.toString());
			Log.errOut("Import",nrf.toString());
		}

		mob.session().print("\n\nSaving all areas imported...");
		for(Enumeration e=doneRooms.elements();e.hasMoreElements();)
		{
			Room saveRoom=(Room)e.nextElement();
			CMClass.DBEngine().DBCreateRoom(saveRoom,CMClass.className(saveRoom));
			// final exit clean-up optomization
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Exit E=saveRoom.rawExits()[d];
				if((E!=null)
				&&(E.isGeneric())
				&&(!E.hasADoor())
				&&(!E.hasALock())
				&&(E.name().equalsIgnoreCase("the ground"))
				&&(!E.isReadable())
				&&(E.numEffects()==0)
				&&(E.numBehaviors()==0)
				&&(E.temporaryDoorLink().length()==0)
				&&(E.displayText().equals(E.description())))
				{
					Exit E2=CMClass.getExit("OpenDescriptable");
					E2.setMiscText(E.displayText());
					saveRoom.rawExits()[d]=E2;
				}
			}
			CMClass.DBEngine().DBUpdateExits(saveRoom);
			CMClass.DBEngine().DBUpdateMOBs(saveRoom);
			CMClass.DBEngine().DBUpdateItems(saveRoom);
			CoffeeUtensils.clearDebriAndRestart(saveRoom,0);
			saveRoom.recoverRoomStats();
			mob.session().print(".");
		}
		mob.session().println("!");

		if(laterLinks.size()>0)
		{
			for(Enumeration e=laterLinks.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				String dcode=(String)laterLinks.get(key);
				String roomID="";
				String dirID="";
				int x=key.lastIndexOf("/");
				if(x>=0)
				{
					roomID=key.substring(0,x);
					dirID=key.substring(x+1);
				}
				else
					continue;

				Room R1=findRoomSomewhere(roomID,"NOAREA",doneRooms);
				if(R1!=null)
				{
					int dir=Util.s_int(dirID);
					Room RR=null;
					Exit RE=null;
					if(dir<Directions.NUM_DIRECTIONS)
					{
						RR=R1.rawDoors()[dir];
						RE=R1.rawExits()[dir];
					}
					Room TR=findRoomSomewhere(dcode,"NOAREA",doneRooms);
					if((RR==null)&&(TR==null))
						returnAnError(mob,"Room "+R1.roomID()+" links to unknown room "+dcode+" in direction "+Directions.getDirectionName(dir)+".");
					else
					if(RR==null)
					{
						R1.rawDoors()[dir]=TR;
						if(RE!=null) RE.setTemporaryDoorLink("");
						CMClass.DBEngine().DBUpdateExits(R1);
					}
				}
			}
		}

		for(Enumeration e=doneRooms.elements();e.hasMoreElements();)
		{
			Room saveRoom=(Room)e.nextElement();
			saveRoom.getArea().toggleMobility(true);
		}
		if(doneRooms.elements().hasMoreElements())
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
				((Area)a.nextElement()).fillInAreaRooms();
		mob.session().println("done!");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,"IMPORT");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
