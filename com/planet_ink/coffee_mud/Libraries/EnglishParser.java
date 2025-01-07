package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Str;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.Room.VariationCode;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;

/*
   Copyright 2003-2024 Bo Zimmerman

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
public class EnglishParser extends StdLibrary implements EnglishParsing
{
	@Override
	public String ID()
	{
		return "EnglishParser";
	}

	private final static String[]	PREPOSITIONS	=
	{
		"aboard", "about", "above", "across", "after", "against", "along", "amid", "among", "anti",
		"around", "as", "at", "before", "behind", "below", "beneath", "beside", "besides", "between",
		"beyond", "but", "by", "concerning", "considering", "despite", "down", "during", "except",
		"excepting", "excluding", "following", "for", "from", "in", "inside", "into", "like", "minus",
		"near", "of", "off", "on", "onto", "opposite", "outside", "over", "past", "per", "plus",
		"regarding", "round", "save", "since", "than", "through", "to", "toward", "towards", "under",
		"underneath", "unlike", "until", "up", "upon", "versus", "via", "with", "within", "without"
	};
	public static boolean[]	PUNCTUATION_TABLE	= null;
	public static boolean[] NARROW_PUNCTUATION_TABLE = null;
	public final static String[]	ARTICLES	= { "a", "an", "all of", "some one", "a pair of", "a pile of", "one of", "all", "the", "some", "each" };
	public final static String[]	INDARTICLES	= { "a", "an", "some"};
	public final static char[]		ALL_CHRS	= "ALL".toCharArray();
	public final static String[]	fwords		= { "calf", "half", "knife", "life", "wife", "elf", "self", "shelf", "leaf", "sheaf", "thief", "loaf", "wolf" };
	public final static String[]	frwords		= { "calves", "halves", "knives", "lives", "wives", "elves", "selves", "shelves", "leaves", "sheaves", "thieves", "loaves", "wolves" };
	public final static String[]	fnouns		= { "bison", "buffalo", "carpcod", "deer", "fish", "moose", "pike", "salmon", "sheep", "shrimp", "squid", "trout", "ore" };
	public final static String[]	feewords1	= { "foot", "goose", "louse", "dormouse", "man", "mouse", "tooth", "woman", "ox", "child", "brother" };
	public final static String[]	feewords2	= { "feet", "geese", "lice", "dormice", "men", "mice", "teeth", "women", "oxen", "children", "brethren" };
	public final static String[]	num_words 	= {
													"zero", "one", "two", "three", "four",
													"five", "six", "seven", "eight", "nine",
													"ten", "eleven", "twelve", "thirteen", "fourteen",
													"fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
												  };
	public final static String[]	num_words_10= {
													"", "ten", "twenty", "thirty", "fourty",
													"fifty", "sixty", "seventy", "eighty", "ninety"
												  };
	public final static String[]	num_words_x	= {
													"thousand", "million", "billion", "trillion",
													"quadrillion", "quintillion", "sextillion"
												  };
	public final static String[]	numth_words	= { "", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth" };

	public final static List<Environmental>	empty	= new ReadOnlyVector<Environmental>(1);

	public static final Map<String,Filterer<Environmental>> generalItemFilterer = new Hashtable<String,Filterer<Environmental>>();

	@Override
	public String toEnglishStringList(final String[] V, final boolean andOr)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		if(V.length==1)
			return V[0];
		final StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length-1;v++)
		{
			if(v>0)
				s.append(", ");
			s.append(V[v]);
		}
		s.append(andOr?" and ":" or ");
		s.append(V[V.length-1]);
		return s.toString();
	}

	@Override
	public String toEnglishStringList(final Class<? extends Enum<?>> enumer, final boolean andOr)
	{
		final Enum<?>[] V=enumer.getEnumConstants();
		if((V==null)||(V.length==0))
		{
			return "";
		}
		if(V.length==1)
			return V[0].toString();
		final StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length-1;v++)
		{
			if(v>0)
				s.append(", ");
			s.append(V[v].toString());
		}
		if(andOr)
			s.append(" and ");
		else
			s.append(" or ");
		s.append(V[V.length-1].toString());
		return s.toString();
	}

	@Override
	public String toEnglishStringList(final Collection<? extends Object> V)
	{
		if((V==null)||(V.isEmpty()))
		{
			return "";
		}
		if(V.size()==1)
			return V.iterator().next().toString();
		final StringBuffer s=new StringBuffer("");
		for(final Iterator<? extends Object> o=V.iterator();o.hasNext();)
		{
			final Object O = o.next();
			if(!o.hasNext())
			{
				if(V.size()==2)
					s.append(" and ");
				else
					s.append(", and ");
			}
			else
			if(s.length()>0)
				s.append(", ");
			s.append(O.toString());
		}
		return s.toString();
	}

	protected String makeLowNumberWords(short num, final boolean and)
	{
		final StringBuilder str = new StringBuilder("");
		if(num > 999)
			num = (short)(num % 1000);
		if(num >= 100)
		{
			final short h = (short)Math.floor(num / 100);
			str.append(num_words[h]);
			num = (short)(num % 100);
			str.append(" hundred");
			if((num > 0)&&(and))
				str.append(" and");
			str.append(" ");
		}
		if(num >= 20)
		{
			final short h = (short)Math.floor(num / 10);
			str.append(num_words_10[h]);
			num = (short)(num % 10);
			if(num > 0)
				str.append("-");
			else
				str.append(" ");
		}
		if(num > 0)
		{
			str.append(num_words[num]);
			str.append(" ");
		}
		return str.toString().trim();
	}

	@Override
	public String makeNumberthWords(int num)
	{
		if(num < 0)
			return "";
		if(num < 10)
			return numth_words[num];
		if(num < 20)
			return makeNumberWords(num, 0)+"th";
		final String lowest=numth_words[num%10];
		num = num - (num%10);
		return makeNumberWords(num, 0)+"-"+lowest;
	}

	@Override
	public String makeNumberWords(final double num, int precision)
	{
		if(num < 0)
			return "negative " + makeNumberWords(-num, precision);
		final double point=Math.round(Math.floor(num));
		if(num == point)
		{
			long n = Math.round(num);
			if(num<1000)
				return makeLowNumberWords((short)Math.round(num),false);
			final int digCap = (int)Math.round(Math.floor(((""+n).length()-1)/3.0));
			final StringBuilder s = new StringBuilder();
			for(int dgn=digCap;dgn>=1;dgn--)
			{
				final long grpcap = Math.round(Math.pow(1000, dgn));
				final long modn = n % grpcap;
				final long grpn = Math.round((n - modn)/grpcap);
				n = modn;
				if(grpn > 0)
					s.append(makeLowNumberWords((short)grpn,false))
					.append(" ")
					.append(num_words_x[dgn-1])
					.append(" ");
			}
			s.append(makeLowNumberWords((short)n,true));
			return s.toString().trim();
		}
		else
		{
			final StringBuilder s = new StringBuilder(makeNumberWords(point, 0));
			if(precision > 0)
			{
				double remain = num - point;
				final short[] ds = new short[precision];
				for(int i=0;i<precision;i++)
				{
					remain = remain * 10.0;
					final double fl = Math.floor(remain);
					remain = remain - fl;
					ds[i] = (short)Math.round(fl);
				}
				while(precision>0 && ds[precision-1]==(short)0)
					precision--;
				if(precision > 0)
				{
					s.append(" point ");
					for(int i=0;i<precision;i++)
						s.append(num_words[ds[i]]).append(" ");
				}
			}
			return s.toString().trim();
		}
	}

	@Override
	public String makePastTense(String word, final String defaultWord)
	{
		if(word == null)
			return defaultWord;
		word = word.trim();
		if(word.length()==0)
			return defaultWord;
		final int x=word.indexOf(' ');
		if(x>0)
			word=word.substring(x+1).trim();
		if(word.endsWith("(s)"))
			word=word.substring(0, word.length()-3);
		if(word.endsWith("(es)"))
			word=word.substring(0, word.length()-4);
		if(word.endsWith("(ses)"))
			word=word.substring(0, word.length()-5);
		if(word.endsWith("(ys)"))
			word=word.substring(0, word.length()-4);
		if(CMStrings.isVowel(word.charAt(word.length()-1)))
			return word+"d";
		else
		if(!word.endsWith("ed"))
			return word+"ed";
		else
			return word;
	}

	@Override
	public boolean isAnArticle(String s)
	{
		s=s.toLowerCase();
		for (final String article : ARTICLES)
		{
			if(s.equals(article))
				return true;
		}
		return false;
	}

	@Override
	public boolean isAnIndefiniteArticle(String s)
	{
		s=s.toLowerCase();
		for (final String article : INDARTICLES)
		{
			if(s.equals(article))
				return true;
		}
		return false;
	}

	@Override
	public String makePlural(final String str)
	{
		if((str==null)||(str.length()==0))
			return str;

		if(str.indexOf("(s)")>0)
			return CMStrings.replaceAll(str, "(s)", "s");
		if(str.indexOf("(es)")>0)
			return CMStrings.replaceAll(str, "(es)", "es");
		if(str.indexOf("(ses)")>0)
			return CMStrings.replaceAll(str, "(ses)", "ses");
		if(str.indexOf("(ys)")>0)
			return CMStrings.replaceAll(str, "(ys)", "ies");
		final String lowerStr=str.toLowerCase();
		final int sp=str.indexOf(' ');
		if(sp > 0)
		{
			final int x=lowerStr.indexOf(" of ");
			if(x>0)
			{
				final int y=str.lastIndexOf(' ',x-1);
				return (y>0)?
						(str.substring(0,y+1)+makePlural(str.substring(y+1,x))+str.substring(x))
						:
						(makePlural(str.substring(0,x))+str.substring(x));
			}
		}
		final boolean uppercase=Character.isUpperCase(str.charAt(str.length()-1));
		if(CMStrings.contains(fnouns, lowerStr))
			return str;
		final int x=CMParms.indexOf(feewords1, lowerStr);
		if(x >= 0)
			return uppercase ? feewords2[x].toUpperCase() : feewords2[x];
		if(lowerStr.endsWith("is"))
			return str.substring(0,str.length()-2)+(uppercase?"ES":"es");
		if(lowerStr.endsWith("s")
		&&(lowerStr.indexOf(" pair of ")>0))
			return str;
		if(lowerStr.endsWith("ts"))
			return str;
		if(lowerStr.endsWith("s")||lowerStr.endsWith("z")||lowerStr.endsWith("x")||lowerStr.endsWith("ch")||lowerStr.endsWith("sh"))
			return str+(uppercase?"ES":"es");
		if(lowerStr.endsWith("ay")||lowerStr.endsWith("ey")||lowerStr.endsWith("iy")||lowerStr.endsWith("oy")||lowerStr.endsWith("uy"))
			return str+(uppercase?"S":"s");
		if(lowerStr.endsWith("y"))
			return str.substring(0,str.length()-1)+(uppercase?"IES":"ies");
		if(CMStrings.contains(fwords, lowerStr))
			return str.substring(0,str.length()-1)+(uppercase?"VES":"ves");
		return str+(uppercase?"S":"s");
	}

	@Override
	public String makeSingular(final String str)
	{
		if((str==null)||(str.length()==0))
			return str;
		final boolean uppercase=Character.isUpperCase(str.charAt(str.length()-1));
		final String lowerStr=str.toLowerCase();
		if(lowerStr.endsWith("ses")||lowerStr.endsWith("zes")||lowerStr.endsWith("xes")||lowerStr.endsWith("ches")||lowerStr.endsWith("shes"))
			return str.substring(0,str.length()-2);
		//if(lowerStr.endsWith("is"))
		//	return str.substring(0,str.length()-2)+(uppercase?"ES":"es");
		if(lowerStr.endsWith("ays")||lowerStr.endsWith("eys")||lowerStr.endsWith("iys")||lowerStr.endsWith("oys")||lowerStr.endsWith("uys"))
			return str.substring(0,str.length()-1);
		if(lowerStr.endsWith("ies"))
			return str.substring(0,str.length()-3)+(uppercase?"Y":"y");
		final int x=CMParms.indexOf(frwords, lowerStr);
		if(x>=0)
			return uppercase?fwords[x].toUpperCase():fwords[x];
		if(str.endsWith("s"))
			return str.substring(0,str.length()-1);
		return str;
	}

	@Override
	public String cleanPrepositions(final String s)
	{
		final String lowStr=s.toLowerCase();
		for (final String prepositino : PREPOSITIONS)
		{
			if(lowStr.startsWith(prepositino+" "))
				return s.substring(prepositino.length()+1);
		}
		return s;
	}

	@Override
	public String properIndefiniteArticle(final String str)
	{
		int i=0;
		for(;i<str.length();i++)
		{
			switch(str.charAt(i))
			{
			case '^':
			{
				i++;
				if(i<str.length())
				{
					switch(str.charAt(i))
					{
					case ColorLibrary.COLORCODE_FANSI256:
						i += 3;
						break;
					case ColorLibrary.COLORCODE_BANSI256:
						i += 3;
						break;
					case ColorLibrary.COLORCODE_BACKGROUND:
						i++;
						break;
					case '<':
						while(i<str.length()-1)
						{
							if((str.charAt(i)!='^')||(str.charAt(i+1)!='>'))
								i++;
							else
							{
								i++;
								break;
							}
						}
						break;
					case '&':
						while(i<str.length())
						{
							if(str.charAt(i)!=';')
								i++;
							else
								break;
						}
						break;
					}
				}
				break;
			}
			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '9':
			case '8':
			{
				final int x=str.indexOf(' ',i);
				if(x<=0)
					return "";
				if(str.charAt(x-1)=='#')
					return (str.charAt(i)=='8')?"an":"a";
				return "";
			}
			case 'a':
			case 'e':
			case 'i':
			case 'o':
			case 'u':
			case 'A':
			case 'E':
			case 'I':
			case 'O':
			case 'U':
				return "an";
			default:
				if(Character.isLetter(str.charAt(i)))
					return "a";
				else
					return "";
			}
		}
		return "";
	}

	protected String getBestDistance(long d)
	{
		String min=null;
		final long sign=(long)Math.signum(d);
		d=Math.abs(d);
		for(final SpaceObject.Distance distance : SpaceObject.DISTANCES)
		{
			if((distance.dm * 2) < d)
			{
				double val=(double)d/(double)distance.dm;
				if((val<0)||(val<100))
					val=Math.round(val*100.0)/100.0;
				else
					val=Math.round(val);
				if(val!=0.0)
				{
					String s=Double.toString(sign*val);
					if(s.endsWith(".0"))
						s=s.substring(0,s.length()-2);
					s+=distance.abbr;
					min = s;
					break;
					//if((min==null)||(min.length()>s.length())) min=s;
				}
			}
		}
		if(min==null)
			return (sign*d)+"dm";
		return min;
	}

	@Override
	public String sizeDescShort(final long size)
	{
		return getBestDistance(size);
	}

	@Override
	public String distanceDescShort(final long distance)
	{
		return getBestDistance(distance);
	}

	@Override
	public String coordDescShort(final long[] coords)
	{
		return getBestDistance(coords[0])+","+getBestDistance(coords[1])+","+getBestDistance(coords[2]);
	}

	@Override
	public String speedDescShort(final double speed)
	{
		return getBestDistance(Math.round(speed))+"/sec";
	}

	@Override
	public String directionDescShort(final double[] dir)
	{
		return Math.round(Math.toDegrees(dir[0])*100)/100.0+" mark "+Math.round(Math.toDegrees(dir[1])*100)/100.0;
	}

	@Override
	public String directionDescShortest(final double[] dir)
	{
		return Math.round(Math.toDegrees(dir[0])*10)/10.0+"`"+Math.round(Math.toDegrees(dir[1])*10)/10.0;
	}

	@Override
	public BigDecimal parseSpaceDistance(String dist)
	{
		if(dist==null)
			return null;
		dist=dist.trim();
		int digits=-1;
		if((dist.length()>0)&&(dist.charAt(0)=='-'))
			digits++;
		while((digits<dist.length()-1)&&(Character.isDigit(dist.charAt(digits+1))))
			digits++;
		if(digits<0)
			return null;
		final BigDecimal value=new BigDecimal(Long.valueOf(CMath.s_long(dist.substring(0,digits+1))).longValue());
		final String unit=dist.substring(digits+1).trim();
		if(unit.length()==0)
			return value;
		SpaceObject.Distance distUnit=(SpaceObject.Distance)CMath.s_valueOf(SpaceObject.Distance.class, unit);
		if(distUnit==null)
		{
			for(final SpaceObject.Distance d : SpaceObject.Distance.values())
			{
				if(d.abbr.equalsIgnoreCase(unit))
					distUnit=d;
			}
		}
		if(distUnit==null)
		{
			for(final SpaceObject.Distance d : SpaceObject.Distance.values())
			{
				if(d.name().equalsIgnoreCase(unit))
					distUnit=d;
			}
		}
		if(distUnit==null)
		{
			for(final SpaceObject.Distance d : SpaceObject.Distance.values())
			{
				if(unit.toLowerCase().startsWith(d.name().toLowerCase()))
					distUnit=d;
			}
		}
		if(distUnit==null)
			return null;
		return value.multiply(new BigDecimal(distUnit.dm));
	}

	@Override
	public BigDecimal parseSpaceSpeed(String speed)
	{
		if(speed==null)
			return null;
		speed=speed.trim();
		BigDecimal divider = BigDecimal.ONE;
		int x = speed.indexOf('/');
		if(x<0 )
			x = speed.indexOf('\\');
		if(x>0)
		{
			final String mult = speed.substring(x+1).toUpperCase().trim();
			TimeManager.TimePeriod per = (TimeManager.TimePeriod)CMath.s_valueOf(TimeManager.TimePeriod.class, mult);
			if(per == null)
				per = (TimeManager.TimePeriod)CMath.s_valueOfStartsWith(TimeManager.TimePeriod.class, mult);
			if(per == null)
				return null;
			speed=speed.substring(0,x);
			divider = BigDecimal.valueOf(per.getMillis()/1000);
		}
		final BigDecimal distance = parseSpaceDistance(speed);
		if((distance == null)||(distance.intValue()<=0))
			return null;
		return distance.divide(divider,BigCMath.SCALE,BigCMath.ROUND);
	}

	@Override
	public String getFirstWord(final String str)
	{
		int i=0;
		int start=-1;
		for(;i<str.length();i++)
		{
			switch(str.charAt(i))
			{
			case '^':
			{
				i++;
				if(i<str.length())
				{
					switch(str.charAt(i))
					{
					case ColorLibrary.COLORCODE_FANSI256:
						i += 3;
						break;
					case ColorLibrary.COLORCODE_BANSI256:
						i += 3;
						break;
					case ColorLibrary.COLORCODE_BACKGROUND:
						i++;
						break;
					case '<':
						while(i<str.length()-1)
						{
							if((str.charAt(i)!='^')||(str.charAt(i+1)!='>'))
								i++;
							else
							{
								i++;
								break;
							}
						}
						break;
					case '&':
						while(i<str.length())
						{
							if(str.charAt(i)!=';')
								i++;
							else
								break;
						}
						break;
					}
				}
				break;
			}
			case ' ':
				if(start>=0)
					return str.substring(start,i);
				break;
			default:
				if(Character.isLetter(str.charAt(i)) && (start<0))
					start=i;
				break;
			}
		}
		return str;
	}

	@Override
	public String startWithAorAn(final String str)
	{
		if((str==null)||(str.length()==0))
			return str;
		final String uppStr=getFirstWord(str).toUpperCase();
		if((!uppStr.equals("A")) &&(!uppStr.equals("AN"))
		&&(!uppStr.equals("THE")) &&(!uppStr.equals("SOME")))
			return (properIndefiniteArticle(str)+" "+str.trim()).trim();
		return str;
	}

	@Override
	public boolean startsWithAnArticle(final String s)
	{
		return isAnArticle(getFirstWord(s));
	}

	@Override
	public boolean startsWithAnIndefiniteArticle(final String s)
	{
		return isAnIndefiniteArticle(getFirstWord(s));
	}

	@Override
	public String removeArticleLead(final String s)
	{
		final String firstWord=getFirstWord(s);
		final int x=s.indexOf(firstWord);
		final String slower=(x>0) ? s.substring(x).toLowerCase() : s.toLowerCase();
		for (final String article : ARTICLES)
		{
			if(slower.startsWith(article+" "))
				return slower.substring(article.length()).trim();
		}
		return s;
	}

	@Override
	public String insertUnColoredAdjective(String str, final String adjective)
	{
		if(str.length()==0)
			return str;
		final String ostr = str;
		str=CMStrings.removeColors(removeArticleLead(str.trim()).trim()).trim();
		if(ostr.toLowerCase().startsWith("some "))
			return ostr.substring(0,5)+adjective+" "+str;
		return properIndefiniteArticle(adjective)+" "+adjective+" "+str;
	}

	protected int skipSpaces(final String paragraph, int index)
	{
		while((index<paragraph.length())&&Character.isWhitespace(paragraph.charAt(index)))
			index++;
		if(index>=paragraph.length())
			return -1;
		return index;
	}

	@Override
	public String insertAdjectives(final String paragraph, final String[] adjsToChoose, final int pctChance)
	{
		if((paragraph.length()==0)||(adjsToChoose==null)||(adjsToChoose.length==0))
			return paragraph;
		final StringBuilder newParagraph = new StringBuilder("");
		int startDex=skipSpaces(paragraph,0);
		if(startDex<0)
			return paragraph;
		newParagraph.append(paragraph.substring(0,startDex));
		int spaceDex=paragraph.indexOf(' ',startDex);
		while(spaceDex > startDex)
		{
			final String word=paragraph.substring(startDex,spaceDex).trim();
			if(isAnArticle(word) && (CMLib.dice().rollPercentage()<=pctChance))
			{
				final String adj=adjsToChoose[CMLib.dice().roll(1, adjsToChoose.length, -1)].toLowerCase();
				if(word.equalsIgnoreCase("a")||word.equalsIgnoreCase("an"))
					newParagraph.append(this.startWithAorAn(adj)).append(" ").append(adj);
				else
					newParagraph.append(word).append(" ").append(adj);
			}
			else
				newParagraph.append(paragraph.substring(startDex,spaceDex));
			startDex=skipSpaces(paragraph,spaceDex);
			if(startDex<0)
				break;
			newParagraph.append(paragraph.substring(spaceDex,startDex));
			spaceDex=paragraph.indexOf(' ',startDex);
		}
		if((spaceDex<startDex)&&(startDex>=0)&&(startDex<paragraph.length()))
			newParagraph.append(paragraph.substring(startDex));
		return newParagraph.toString();
	}

	public Social findSocial(final MOB mob, final List<String> commands, final boolean exactOnly)
	{
		if(commands.size()==0)
			return null;
		if(mob == null)
			return CMLib.socials().fetchSocial(commands, exactOnly, true);

		final String socName = commands.get(0).toUpperCase().trim();
		List<Social> socV = null;
		final Map<String,List<Social>> trigSet = mob.triggerer().getSocialSets();
		if(exactOnly)
		{
			if((trigSet != null)
			&&(!trigSet.isEmpty()))
				socV = trigSet.get(socName);
			if(socV == null)
				socV = CMLib.socials().getSocialsSet(socName);
			if(socV == null)
				return null;
		}
		else
		{
			if((trigSet != null)
			&&(!trigSet.isEmpty()))
			{
				for(final Iterator<String> s = trigSet.keySet().iterator();s.hasNext();)
				{
					final String sn = s.next();
					if(sn.startsWith(socName))
					{
						socV = trigSet.get(sn);
						break;
					}
				}
				if(socV == null)
				{
					for(final String sn : CMLib.socials().getSocialsBaseList())
					{
						if(sn.startsWith(socName))
						{
							socV = CMLib.socials().getSocialsSet(sn);
							break;
						}
					}
				}
			}
		}
		if(socV == null)
			return null;
		final String targ1 = (commands.size()>1)?commands.get(1).toUpperCase().trim():"";
		final String targn = (commands.size()>1)?CMParms.combine(commands,1).toUpperCase().trim():"";
		final String argx  = (commands.size()>2)?CMParms.combine(commands,2).toUpperCase().trim():"";
		// 1. easiest case: no target
		if(targ1.length()==0)
		{
			for(final Social S : socV)
			{
				if((S.targetName().length()==0)
				&&(S.argumentName().length()==0)
				&&(S.meetsCriteriaToUse(mob)))
					return S;
			}
			return null;
		}
		// -- here on out, an target of some sort was given

		// 2. second easiest case: named target
		for(final Social S : socV)
		{
			if(S.targetName().equalsIgnoreCase(targ1)
			&&(S.argumentName().equalsIgnoreCase(argx)
				||((!exactOnly) && S.argumentName().toUpperCase().startsWith(argx)))
			&&(S.meetsCriteriaToUse(mob)))
				return S;
			if(S.targetName().equalsIgnoreCase(targn)
			&&(S.argumentName().length()==0)
			&&(S.meetsCriteriaToUse(mob)))
				return S;
		}
		// 3. argument exists, and match is ONLY argument match.
		if(argx.length()>0)
		{
			Social social = null;
			for(final Social S : socV)
			{
				if((S.argumentName().length()>0)
				&&(S.isTargetable())
				&&(S.argumentName().equalsIgnoreCase(argx)
					||((!exactOnly) && S.argumentName().toUpperCase().startsWith(argx)))
				&&(S.meetsCriteriaToUse(mob)))
				{
					if(social != null)
					{
						social = null;
						break;
					}
					social = S;
				}
			}
			if(social != null)
				return social;
		}
		// 4. now look for best match to actual target
		final Room R=mob.location();
		if(R!=null)
		{
			Physical P = R.fetchFromMOBRoomFavorsMOBs(mob, null, targn, Wearable.FILTER_ANY);
			if(P == null)
				P = R.fetchFromMOBRoomFavorsMOBs(mob, null, targ1, Wearable.FILTER_ANY);
			if(P != null)
			{
				for(final Social S : socV)
				{
					if(S.isTargetable()
					&& S.targetable(P)
					&&(S.argumentName().equalsIgnoreCase(argx)
						||((!exactOnly) && S.argumentName().toUpperCase().startsWith(argx)))
					&&(S.meetsCriteriaToUse(mob)))
						return S;
				}
			}
			// nothing at all matched, so just return something that can Not match
			for(final Social S : socV)
			{
				if(S.targetName().equals("<T-NAME>")
				&&(S.argumentName().equalsIgnoreCase(argx)
					||((!exactOnly) && S.argumentName().toUpperCase().startsWith(argx)))
				&&(S.meetsCriteriaToUse(mob)))
					return S;
			}
			for(final Social S : socV)
			{
				if(S.isTargetable()
				&&(S.argumentName().equalsIgnoreCase(argx)
					||((!exactOnly) && S.argumentName().toUpperCase().startsWith(argx)))
				&&(S.meetsCriteriaToUse(mob)))
					return S;
			}
			for(final Social S : socV)
			{
				if((S.argumentName().equalsIgnoreCase(argx)
					||((!exactOnly) && S.argumentName().toUpperCase().startsWith(argx)))
				&&(S.meetsCriteriaToUse(mob)))
					return S;
			}
		}
		return null;
	}

	@Override
	public CMObject findCommand(final MOB mob, final List<String> commands)
	{
		if((mob==null)
		||(commands==null)
		||(mob.location()==null)
		||(commands.isEmpty()))
			return null;

		String firstWord=commands.get(0).toUpperCase();

		if((firstWord.length()>1)&&(!Character.isLetterOrDigit(firstWord.charAt(0))))
		{
			commands.add(1,commands.get(0).substring(1));
			commands.set(0,""+firstWord.charAt(0));
			firstWord=""+firstWord.charAt(0);
		}

		// first, exacting pass
		Command C=CMClass.findCommandByTrigger(firstWord,true);
		if((C!=null)
		&&(C.securityCheck(mob))
		&&(!CMSecurity.isCommandDisabled(CMClass.classID(C).toUpperCase())))
			return CMLib.leveler().deferCommandCheck(mob, C, commands);

		Ability A=getSkillToInvoke(mob,new XVector<String>(commands));
		if((A!=null)
		&&(!CMSecurity.isAbilityDisabled(A.ID().toUpperCase())))
			return A;

		if(getSkillInvokeWord(mob,firstWord)!=null)
			return null;

		Social social=findSocial(mob, commands, true);
		if(social!=null)
			return social;

		for(int c=0;c<CMLib.channels().getNumChannels();c++)
		{
			final ChannelsLibrary.CMChannel chan=CMLib.channels().getChannel(c);
			if(chan.name().equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("Channel");
				if((C!=null)&&(C.securityCheck(mob)))
					return C;
			}
			else
			if(("NO"+chan.name()).equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("NoChannel");
				if((C!=null)&&(C.securityCheck(mob)))
					return C;
			}
		}

		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(CMJ.NAME().equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("CommandJournal");
				if((C!=null)&&(C.securityCheck(mob)))
					return C;
			}
		}

		// second, inexacting pass
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			A=a.nextElement();
			final HashSet<String> tried=new HashSet<String>();
			if((A!=null)&&(A.triggerStrings()!=null))
			{
				for(int t=0;t<A.triggerStrings().length;t++)
				{
					if((A.triggerStrings()[t].toUpperCase().startsWith(firstWord))
					&&(!tried.contains(A.triggerStrings()[t])))
					{
						final Vector<String> commands2=new XVector<String>(commands);
						commands2.setElementAt(A.triggerStrings()[t],0);
						final Ability A2=getSkillToInvoke(mob,commands2);
						if((A2!=null)&&(!CMSecurity.isAbilityDisabled(A2.ID().toUpperCase())))
						{
							commands.set(0,A.triggerStrings()[t]);
							return A;
						}
					}
				}
			}
		}
		//commands comes inexactly after ables
		//because of CA, PR, etc..
		C=CMClass.findCommandByTrigger(firstWord,false);
		if((C!=null)
		&&(C.securityCheck(mob))
		&&(!CMSecurity.isCommandDisabled(CMClass.classID(C).toUpperCase())))
			return CMLib.leveler().deferCommandCheck(mob, C, commands);

		social=findSocial(mob, commands, false);
		if(social!=null)
		{
			commands.set(0,social.baseName());
			return social;
		}

		for(int c=0;c<CMLib.channels().getNumChannels();c++)
		{
			final ChannelsLibrary.CMChannel chan=CMLib.channels().getChannel(c);
			if(chan.name().startsWith(firstWord))
			{
				commands.set(0,chan.name());
				C=CMClass.getCommand("Channel");
				if((C!=null)&&(C.securityCheck(mob)))
					return C;
			}
			else
			if(("NO"+chan.name()).startsWith(firstWord))
			{
				commands.set(0,"NO"+chan.name());
				C=CMClass.getCommand("NoChannel");
				if((C!=null)&&(C.securityCheck(mob)))
					return C;
			}
		}

		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(CMJ.NAME().startsWith(firstWord))
			{
				C=CMClass.getCommand("CommandJournal");
				if((C!=null)&&(C.securityCheck(mob)))
					return C;
			}
		}
		return CMLib.leveler().deferCommandCheck(mob, null, commands);
	}

	protected boolean evokedBy(final Ability thisAbility, final String thisWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(thisAbility.triggerStrings()[i].equalsIgnoreCase(thisWord))
				return true;
		}
		return false;
	}

	protected String collapsedName(final Ability thisAbility)
	{
		final int x=thisAbility.name().indexOf(' ');
		if(x>=0)
			return CMStrings.replaceAll(thisAbility.name()," ","");
		return thisAbility.Name();
	}

	protected boolean evokedBy(final Ability thisAbility, final String thisWord, final String secondWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(thisAbility.triggerStrings()[i].equalsIgnoreCase(thisWord))
			{
				if(((thisAbility.name().toUpperCase().startsWith(secondWord)))
				||(collapsedName(thisAbility).toUpperCase().startsWith(secondWord)))
					return true;
			}
		}
		return false;
	}

	@Override
	public String getSkillInvokeWord(final MOB mob, String word)
	{
		if(mob==null)
			return null;
		Ability A=null;
		final HashSet<String[]> done=new HashSet<String[]>();
		word=word.toUpperCase().trim();
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			A=a.nextElement();
			if((A!=null)
			&&(A.triggerStrings()!=null)
			&&(!done.contains(A.triggerStrings())))
			{
				done.add(A.triggerStrings());
				for(int t=0;t<A.triggerStrings().length;t++)
				{
					if(word.equals(A.triggerStrings()[t]))
					{
						if((t>0)&&(A.triggerStrings()[0].startsWith(word)))
							return A.triggerStrings()[0];
						else
							return A.triggerStrings()[t];
					}
				}
			}
		}
		return null;
	}

	@Override
	public Ability getSkillToInvoke(final MOB mob, final List<String> commands)
	{
		final String evokeWord=commands.get(0).toUpperCase();

		boolean foundMoreThanOne=false;
		Ability evokeA=null;
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(evokedBy(A,evokeWord)))
			{
				if(evokeA != null)
				{
					if(!A.ID().equals(evokeA.ID()))
					{
						foundMoreThanOne=true;
						evokeA=null;
						break;
					}
					else
					if(A.proficiency() > evokeA.proficiency())
						evokeA = A;
				}
				else
					evokeA=A;
			}
		}

		if((evokeA!=null)&&(commands.size()>1))
		{
			final int classCode=evokeA.classificationCode()&Ability.ALL_ACODES;
			switch(classCode)
			{
			case Ability.ACODE_SPELL:
			case Ability.ACODE_SONG:
			case Ability.ACODE_PRAYER:
			case Ability.ACODE_CHANT:
				evokeA=null;
				foundMoreThanOne=true;
				break;
			default:
				break;
			}
		}

		if(evokeA!=null)
			commands.remove(0);
		else
		if((foundMoreThanOne)&&(commands.size()>1))
		{
			commands.remove(0);
			foundMoreThanOne=false;
			final String firstCastUWord=commands.get(0).toUpperCase();
			for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)
				&&(evokedBy(A,evokeWord,firstCastUWord)))
				{
					final String unameA = A.Name().toUpperCase();
					final String ucnameA = collapsedName(A).toUpperCase();
					if((unameA.equals(firstCastUWord))
					||(ucnameA.equals(firstCastUWord)))
					{
						evokeA=A;
						break;
					}
					else
					if(evokeA!=null)
					{
						if(!A.ID().equals(evokeA.ID()))
							foundMoreThanOne=true;
						if((A.proficiency() > evokeA.proficiency())
						||((unameA.startsWith(firstCastUWord)
							&& (!evokeA.Name().toUpperCase().startsWith(firstCastUWord))))
						||((ucnameA.startsWith(firstCastUWord))
							&& (!collapsedName(evokeA).toUpperCase().startsWith(firstCastUWord))))
						{
							evokeA = A;
						}
					}
					else
						evokeA=A;
				}
			}
			if((evokeA!=null)&&(!foundMoreThanOne))
				commands.remove(0);
			else
			if((foundMoreThanOne)&&(commands.size()>1))
			{
				final String secondAndThirdCastUWords=firstCastUWord+" "+commands.get(1).toUpperCase();
				for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)
					&& (evokedBy(A,evokeWord,secondAndThirdCastUWords)))
					{
						evokeA=A;
						break;
					}
				}
				if(evokeA!=null)
				{
					commands.remove(0);
					commands.remove(0);
				}
			}
			else
			if(evokeA == null)
			{
				for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)
					&&(evokedBy(A,evokeWord))
					&&(A.name().toUpperCase().indexOf(" "+firstCastUWord)>0))
					{
						evokeA=A;
						commands.remove(0);
						break;
					}
				}
			}
			else
				commands.remove(0);
		}
		return evokeA;
	}

	@Override
	public boolean preInvokeSkill(final MOB mob, List<String> commands, final int secondsElapsed, final double actionsRemaining)
	{
		commands=new Vector<String>(commands);
		final Ability evokableAbility=getSkillToInvoke(mob,commands);
		if(evokableAbility==null)
		{
			mob.tell(L("You don't know how to do that."));
			return false;
		}
		if((CMLib.ableMapper().qualifyingLevel(mob,evokableAbility)>=0)
		&&(!CMLib.ableMapper().qualifiesByLevel(mob,evokableAbility))
		&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ALLSKILLS)))
		{
			mob.tell(L("You are not high enough level to do that."));
			return false;
		}
		return evokableAbility.preInvoke(mob,commands,null,false,0,secondsElapsed,actionsRemaining);
	}

	@Override
	public void invokeSkill(final MOB mob, final List<String> commands)
	{
		final Ability evokableAbility=getSkillToInvoke(mob,commands);
		if(evokableAbility==null)
		{
			mob.tell(L("You don't know how to do that."));
			return;
		}
		if((CMLib.ableMapper().qualifyingLevel(mob,evokableAbility)>=0)
		&&(!CMLib.ableMapper().qualifiesByLevel(mob,evokableAbility))
		&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ALLSKILLS)))
		{
			mob.tell(L("You are not high enough level to do that."));
			return;
		}
		evokableAbility.invoke(mob,commands,null,false,0);
	}

	private boolean[] PUNCTUATION_TABLE()
	{
		if(PUNCTUATION_TABLE==null)
		{
			final boolean[] PUNCTUATION_TEMP_TABLE=new boolean[255];
			for(int c=0;c<255;c++)
				switch(c)
				{
				case '`':
				case '~':
				case '!':
				case '@':
				case '#':
				case '$':
				case '%':
				case '^':
				case '&':
				case '*':
				case '(':
				case ')':
				case '_':
				case '-':
				case '+':
				case '=':
				case '[':
				case ']':
				case '{':
				case '}':
				case '\\':
				case '|':
				case ';':
				case ':':
				case '\'':
				case '\"':
				case ',':
				case '<':
				case '.':
				case '>':
				case '/':
				case '?':
					PUNCTUATION_TEMP_TABLE[c]=true;
					break;
				default:
					PUNCTUATION_TEMP_TABLE[c]=false;
				}
			PUNCTUATION_TABLE=PUNCTUATION_TEMP_TABLE;
		}
		return PUNCTUATION_TABLE;
	}

	private boolean[] NARROW_PUNCTUATION_TABLE()
	{
		if(NARROW_PUNCTUATION_TABLE==null)
		{
			final boolean[] PUNCTUATION_TEMP_TABLE=new boolean[255];
			for(int c=0;c<255;c++)
				switch(c)
				{
				case '`':
				case '!':
				case ';':
				case ':':
				case '\'':
				case '\"':
				case ',':
				case '.':
				case '?':
					PUNCTUATION_TEMP_TABLE[c]=true;
					break;
				default:
					PUNCTUATION_TEMP_TABLE[c]=false;
				}
			NARROW_PUNCTUATION_TABLE=PUNCTUATION_TEMP_TABLE;
		}
		return NARROW_PUNCTUATION_TABLE;
	}

	@Override
	public boolean isPunctuation(final byte b)
	{
		if((b<0)||(b>255))
			return false;
		return PUNCTUATION_TABLE[b];
	}

	@Override
	public boolean isEnglishPunctuation(final byte b)
	{
		if((b<0)||(b>255))
			return false;
		return NARROW_PUNCTUATION_TABLE[b];
	}

	@Override
	public boolean hasEnglishPunctuation(final String str)
	{
		if((str==null)||(str.length()==0))
			return false;
		boolean puncFound=false;
		NARROW_PUNCTUATION_TABLE();
		for(int x=0;x<str.length();x++)
		{
			if(isEnglishPunctuation((byte)str.charAt(x)))
			{
				puncFound=true;
				break;
			}
		}
		return puncFound;
	}

	@Override
	public String stripEnglishPunctuation(final String str)
	{
		if(!hasEnglishPunctuation(str))
			return str;
		final char[] strc=str.toCharArray();
		final char[] str2=new char[strc.length];
		int s=0;
		for(int x=0;x<strc.length;x++)
		{
			if(!isEnglishPunctuation((byte)strc[x]))
			{
				str2[s]=strc[x];
				s++;
			}
		}
		return new String(str2,0,s);
	}

	@Override
	public boolean hasPunctuation(final String str)
	{
		if((str==null)||(str.length()==0))
			return false;
		boolean puncFound=false;
		PUNCTUATION_TABLE();
		for(int x=0;x<str.length();x++)
		{
			if(isPunctuation((byte)str.charAt(x)))
			{
				puncFound=true;
				break;
			}
		}
		return puncFound;
	}

	@Override
	public String stripPunctuation(final String str)
	{
		if(!hasPunctuation(str))
			return str;
		final char[] strc=str.toCharArray();
		final char[] str2=new char[strc.length];
		int s=0;
		for(int x=0;x<strc.length;x++)
		{
			if(!isPunctuation((byte)strc[x]))
			{
				str2[s]=strc[x];
				s++;
			}
		}
		return new String(str2,0,s);
	}

	@Override
	public String spaceOutPunctuation(final String str)
	{
		if(!hasPunctuation(str))
			return str;
		final char[] strc=str.toCharArray();
		for(int x=0;x<strc.length;x++)
		{
			if(isPunctuation((byte)strc[x]))
				strc[x]=' ';
		}
		return new String(strc);
	}

	@Override
	public List<String> parseWords(final String thisStr)
	{
		if((thisStr==null)||(thisStr.length()==0))
			return new Vector<String>(1);
		return CMParms.parseSpaces(spaceOutPunctuation(thisStr), true);
	}

	public boolean equalsPunctuationless(final char[] strC, final char[] str2C)
	{
		if((strC.length==0)&&(str2C.length==0))
			return true;
		PUNCTUATION_TABLE();
		int s1=0;
		int s2=0;
		int s1len=strC.length;
		while((s1len>0)&&(Character.isWhitespace(strC[s1len-1])||isPunctuation((byte)strC[s1len-1])))
			s1len--;
		int s2len=str2C.length;
		while((s2len>0)&&(Character.isWhitespace(str2C[s2len-1])||isPunctuation((byte)str2C[s2len-1])))
			s2len--;
		while(s1<s1len)
		{
			while((s1<s1len)&&(isPunctuation((byte)strC[s1])))
				s1++;
			while((s2<s2len)&&(isPunctuation((byte)str2C[s2])))
				s2++;
			if(s1==s1len)
			{
				if(s2==s2len)
					return true;
				return false;
			}
			if(s2==s2len)
				return false;
			if(strC[s1]!=str2C[s2])
				return false;
			s1++;
			s2++;
		}
		if(s2==s2len)
			return true;
		return false;
	}

	@Override
	public boolean containsOneOfString(final String toSrchStr, final List<String> srchForStrs)
	{
		if((toSrchStr==null)||(srchForStrs==null))
			return false;
		if((toSrchStr.length()==0)&&(srchForStrs.size()>0))
			return false;
		final char[] toSrchC=toSrchStr.toCharArray();
		for(int c=0;c<toSrchC.length;c++)
			toSrchC[c]=Character.toUpperCase(toSrchC[c]);
		for(final String strr : srchForStrs)
		{
			if(unsafeContainsString(toSrchC,strr))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsString(final String toSrchStr, final String srchForStr)
	{
		if((toSrchStr==null)||(srchForStr==null))
			return false;
		if((toSrchStr.length()==0)&&(srchForStr.length()>0))
			return false;
		final char[] toSrchC=toSrchStr.toCharArray();
		for(int c=0;c<toSrchC.length;c++)
			toSrchC[c]=Character.toUpperCase(toSrchC[c]);
		return unsafeContainsString(toSrchC,srchForStr);
	}

	protected final boolean unsafeContainsString(final char[] toSrchC, final String srchForStr)
	{
		char[] srchC=srchForStr.toCharArray();
		for(int c=0;c<srchC.length;c++)
			srchC[c]=Character.toUpperCase(srchC[c]);
		if(java.util.Arrays.equals(srchC,ALL_CHRS))
			return true;
		if(java.util.Arrays.equals(srchC,toSrchC))
			return true;
		if(equalsPunctuationless(srchC,toSrchC))
			return true;

		boolean topOnly=false;
		if((srchC.length>1)&&(srchC[0]=='$'))
		{
			srchC=new String(srchC,1,srchC.length-1).toCharArray();
			topOnly=true;
		}
		int tos=0;
		boolean found=false;
		while((!found)&&(tos<toSrchC.length))
		{
			for(int x=0;x<srchC.length;x++)
			{
				if(tos>=toSrchC.length)
				{
					if(srchC[x]=='$')
						found=true;
					break;
				}

				switch(toSrchC[tos])
				{
				case '^':
					tos++;
					if(tos<toSrchC.length)
					{
						switch(toSrchC[tos])
						{
						case ColorLibrary.COLORCODE_BACKGROUND:
							if(tos < toSrchC.length-1)
								tos+=2;
							break;
						case ColorLibrary.COLORCODE_FANSI256:
						case ColorLibrary.COLORCODE_BANSI256:
							if(tos < toSrchC.length-4)
								tos+=4;
							break;
						default:
							tos++;
							break;
						}
					}
					break;
				case ',':
				case '?':
				case '!':
				case '.':
				case ';':
					tos++;
					break;
				}
				switch(srchC[x])
				{
				case '^':
					x++;
					if(x<srchC.length)
					{
						switch(srchC[x])
						{
						case ColorLibrary.COLORCODE_BACKGROUND:
							if(x < srchC.length-1)
								x+=2;
							break;
						case ColorLibrary.COLORCODE_FANSI256:
						case ColorLibrary.COLORCODE_BANSI256:
							if(x < srchC.length-4)
								x+=4;
							break;
						default:
							x++;
							break;
						}
					}
					break;
				case ',':
				case '?':
				case '!':
				case '.':
				case ';':
					x++;
					break;
				}
				if(x<srchC.length)
				{
					if(tos<toSrchC.length)
					{
						if(srchC[x]!=toSrchC[tos])
							break;
						else
						if(x==(srchC.length-1))
							 found=true;
						else
							tos++;
					}
					else
					if(srchC[x]=='$')
						found=true;
					else
						break;
				}
				else
				{
					found=true;
					break;
				}
			}
			if((topOnly)&&(!found))
				break;
			while((!found)&&(tos<toSrchC.length)&&(Character.isLetter(toSrchC[tos])))
				tos++;
			tos++;
		}
		return found;
	}

	@Override
	public String bumpDotContextNumber(final String srchStr, final int thisMuch)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return srchStr;
		if(flags.allFlag||flags.lastFlag)
			return srchStr;
		return (flags.occurrance+thisMuch)+"."+flags.srchStr;
	}

	@Override
	public int getContextDotNumber(final String srchStr)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return 0;
		if(flags.allFlag)
			return 0;
		return flags.occurrance;
	}

	@Override
	public int getContextNumber(final ItemCollection cont, final Environmental E)
	{
		return getContextNumber(toCollection(cont),E);
	}

	@Override
	public int getContextNumber(final Environmental[] list, final Environmental E)
	{
		return getContextNumber(new XVector<Environmental>(list),E);
	}

	@Override
	public int getContextNumber(final Collection<? extends Environmental> list, final Environmental E)
	{
		if(list==null)
			return 0;
		int context=1;
		for(final Environmental O : list)
		{
			if((O.Name().equalsIgnoreCase(E.Name()))
			||(O.name().equalsIgnoreCase(E.name())))
			{
				if(O==E)
					return context<2?0:context;
				if((!(O instanceof Item))
				||(!(E instanceof Item))
				||(((Item)E).container()==((Item)O).container()))
					context++;
			}
		}
		return -1;
	}

	private Collection<? extends Environmental> toCollection(final ItemCollection cont)
	{
		final LinkedList<Item> list=new LinkedList<Item>();
		for(final Enumeration<Item> i=cont.items();i.hasMoreElements();)
			list.add(i.nextElement());
		return list;
	}

	@Override
	public int getContextSameNumber(final ItemCollection cont, final Environmental E)
	{
		return getContextSameNumber(toCollection(cont),E);
	}

	@Override
	public int getContextSameNumber(final Environmental[] list, final Environmental E)
	{
		return getContextSameNumber(new XVector<Environmental>(list),E);
	}

	@Override
	public int getContextSameNumber(final Collection<? extends Environmental> list, final Environmental E)
	{
		if(list==null)
			return 0;
		int context=1;
		for(final Object O : list)
		{
			if((((Environmental)O).Name().equalsIgnoreCase(E.Name()))
			||(((Environmental)O).name().equalsIgnoreCase(E.name())))
			{
				if(E.sameAs((Environmental)O))
					return context<2?0:context;
				if((!(O instanceof Item))
				||(!(E instanceof Item))
				||(((Item)E).container()==((Item)O).container()))
					context++;
			}
		}
		return -1;
	}

	@Override
	public String getContextName(final ItemCollection cont, final Environmental E)
	{
		return getContextName(toCollection(cont),E);
	}

	@Override
	public String getContextName(final Environmental[] list, final Environmental E)
	{
		return getContextName(new XVector<Environmental>(list),E);
	}

	@Override
	public String getContextName(final Collection<? extends Environmental> list, final Environmental E)
	{
		if(list==null)
			return E.name();
		final int number=getContextNumber(list,E);
		if(number<0)
			return null;
		if(number<2)
			return E.name();
		return E.name()+"."+number;
	}

	@Override
	public List<String> getAllContextNames(final Collection<? extends Environmental> list, final Filterer<Environmental> filter)
	{
		if(list==null)
			return new ArrayList<String>();
		final List<String> flist = new Vector<String>(list.size());
		final Map<String,int[]> prevFound = new HashMap<String,int[]>();
		for(final Environmental E : list)
		{
			if((filter!=null)
			&&(!filter.passesFilter(E)))
			{
				flist.add("");
				continue;
			}
			final String key = E.Name()+((E instanceof Item)?("/"+((Item)E).container()):"");
			if(!prevFound.containsKey(key))
			{
				final int[] ct=new int[]{1};
				prevFound.put(key, ct);
				flist.add(E.name());
			}
			else
			{
				final int[] ct=prevFound.get(key);
				ct[0]++;
				flist.add(E.name()+"."+ct[0]);
			}
		}
		return flist;
	}

	@Override
	public String getContextSameName(final ItemCollection cont, final Environmental E)
	{
		return getContextSameName(toCollection(cont),E);
	}

	@Override
	public String getContextSameName(final Environmental[] list, final Environmental E)
	{
		return getContextSameName(new XVector<Environmental>(list),E);
	}

	@Override
	public String getContextSameName(final Collection<? extends Environmental> list, final Environmental E)
	{
		if(list==null)
			return E.name();
		final int number=getContextSameNumber(list,E);
		if(number<0)
			return null;
		if(number<2)
			return E.name();
		return E.name()+"."+number;
	}

	@Override
	public Environmental parseShopkeeper(final MOB mob, final List<String> matchWords, final String fromTo, final String error)
	{
		if(matchWords.isEmpty())
		{
			if((error!=null)&&(error.length()>0))
				mob.tell(error);
			return null;
		}
		matchWords.remove(0);

		final List<Environmental> V=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
		if(V.isEmpty())
		{
			if((error!=null)&&(error.length()>0))
				mob.tell(error);
			return null;
		}
		final int fromDex = (fromTo != null) ? CMParms.indexOfIgnoreCase(matchWords, fromTo):-1;
		if((V.size()>1)
		||((fromDex > 0)&&(fromDex < matchWords.size()-1)))
		{
			if(matchWords.size()<2)
			{
				if((error!=null)&&(error.length()>0))
					mob.tell(error);
				return null;
			}
			if((fromDex > 0)
			&&(fromDex < matchWords.size()-1))
			{
				final String s=CMParms.combine(matchWords,fromDex+1);
				final Environmental shopkeeper=fetchEnvironmental(V,s,false);
				if(shopkeeper != null)
				{
					while(matchWords.size()>fromDex)
						matchWords.remove(matchWords.size()-1);
					matchWords.add(s);
				}
			}
			final String what=matchWords.get(matchWords.size()-1);
			Environmental shopkeeper=fetchEnvironmental(V,what,false);
			if((shopkeeper==null)&&(what.equals("shop")||what.equals("the shop")))
			{
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v) instanceof Area)
					{
						shopkeeper = V.get(v);
						break;
					}
				}
			}
			if((shopkeeper!=null)
			&&(CMLib.coffeeShops().getShopKeeper(shopkeeper)!=null)
			&&(CMLib.flags().canBeSeenBy(shopkeeper,mob)))
				matchWords.remove(matchWords.size()-1);
			else
			{
				CMLib.commands().postCommandFail(mob,new XVector<String>(matchWords),
						L("You don't see anyone called '@x1' here buying or selling.",matchWords.get(matchWords.size()-1)));
				return null;
			}
			return shopkeeper;
		}
		Environmental shopkeeper=V.get(0);
		if(matchWords.size()>1)
		{
			final String whom=matchWords.get(matchWords.size()-1);
			MOB M=mob.location().fetchInhabitant(whom);
			int ctr=1;
			while ((M != null)
			&& (!CMLib.flags().canBeSeenBy(M, mob))
			&&(whom.indexOf('.')<0))
				M = mob.location().fetchInhabitant(whom+"."+(++ctr));
			if((M!=null)
			&&(CMLib.coffeeShops().getShopKeeper(M)!=null)
			&&(CMLib.flags().canBeSeenBy(M,mob)))
			{
				shopkeeper=M;
				matchWords.remove(matchWords.size()-1);
			}
		}
		return shopkeeper;
	}

	@Override
	public List<Item> fetchItemList(final ItemPossessor from,
									final MOB mob,
									final Item container,
									final List<String> matchWords,
									final Filterer<Environmental> filter,
									final boolean visionMatters)
	{
		int addendum=1;
		String addendumStr="";
		List<Item> V=new Vector<Item>();

		int maxToItem=Integer.MAX_VALUE;
		if((matchWords.size()>1)
		&&(CMath.s_int(matchWords.get(0))>0))
		{
			maxToItem=CMath.s_int(matchWords.get(0));
			matchWords.set(0,"all");
		}

		String name=CMParms.combine(matchWords,0);
		boolean allFlag = (!matchWords.isEmpty()) ? matchWords.get(0).equalsIgnoreCase("all") : false;
		if (name.toUpperCase().startsWith("ALL."))
		{
			allFlag = true;
			name = "ALL " + name.substring(4);
		}
		if (name.toUpperCase().endsWith(".ALL"))
		{
			allFlag = true;
			name = "ALL " + name.substring(0, name.length() - 4);
		}
		boolean doBugFix = true;
		boolean wornOnly = true;
		boolean unwornOnly = true;
		while(doBugFix || ((allFlag)&&(addendum<=maxToItem)))
		{
			doBugFix=false;
			Environmental item=null;
			if(from instanceof MOB)
			{
				item=((MOB)from).fetchItem(container,filter,name+addendumStr);
				if((item == null)
				&&(filter == Wearable.FILTER_WORNONLY))
				{
					String locName = name.toUpperCase();
					if(locName.startsWith("ALL ")||locName.startsWith("ALL."))
						locName = locName.substring(4).trim();
					if(locName.startsWith("FROM "))
						locName = locName.substring(5).trim();
					final long code = Wearable.CODES.FIND_ignoreCase(locName);
					final List<Item> items=new ArrayList<Item>(2);
					if(code>0)
						items.addAll(((MOB)from).fetchWornItems(code,(short)-2048,(short)0));
					else
					{
						final long[] codes = Wearable.CODES.FIND_endsWiths(" "+locName);
						for(final long cd : codes)
							items.addAll(((MOB)from).fetchWornItems(cd,(short)-2048,(short)0));
					}
					if(items.size()>0)
					{
						Collections.sort(items,new Comparator<Item>() {
							@Override
							public int compare(final Item o1, final Item o2)
							{
								final int layer1 = (o1 instanceof Armor)?((Armor)o1).getClothingLayer():0;
								final int layer2 = (o2 instanceof Armor)?((Armor)o2).getClothingLayer():0;
								if(layer1>layer2)
									return -1;
								else
									return (layer1==layer2) ? 0 : 1;
							}
						});
						if((addendum>0)&&(addendum <= items.size()))
							item=items.get(addendum-1);
					}
				}
				// all this for single underlayer items with the same name.. ugh...
				if((item instanceof Armor)
				&& (!allFlag)
				&& (filter == Wearable.FILTER_WORNONLY)
				&& (addendumStr.length()==0))
				{
					int subAddendum = 0;
					Item item2=((MOB)from).fetchItem(container,filter,name+"."+(++subAddendum));
					while(item2 != null)
					{
						if((item2 instanceof Armor)
						&&(((Armor)item2).getClothingLayer() > ((Armor)item).getClothingLayer()))
							item=item2;
						item2=((MOB)from).fetchItem(container,filter,name+"."+(++subAddendum));
					}
				}
			}
			else
			if(from instanceof Room)
				item=((Room)from).fetchFromMOBRoomFavorsItems(mob,container,name+addendumStr,filter);
			if((item!=null)
			&&(item instanceof Item)
			&&((!visionMatters)||(CMLib.flags().canBeSeenBy(item,mob))||(item instanceof Light))
			&&(!V.contains(item)))
			{
				V.add((Item)item);
				if(((Item)item).amWearingAt(Wearable.IN_INVENTORY))
					wornOnly=false;
				else
					unwornOnly=false;
			}
			if(item==null)
				break;
			addendumStr="."+(++addendum);
		}

		if(wornOnly && (!V.isEmpty()))
		{
			final Vector<Item> V2=new Vector<Item>(); // return value
			short topLayer=0;
			short curLayer=0;
			int which=-1;
			while(!V.isEmpty())
			{
				Item I=V.get(0);
				topLayer=(I instanceof Armor)?((Armor)I).getClothingLayer():0;
				which=0;
				for(int v=1;v<V.size();v++)
				{
					I=V.get(v);
					curLayer=(I instanceof Armor)?((Armor)I).getClothingLayer():0;
					if(curLayer>topLayer)
					{
						which = v;
						topLayer = curLayer;
					}
				}
				V2.addElement(V.get(which));
				V.remove(which);
			}
			V=V2;
		}
		else
		if(unwornOnly && (!V.isEmpty()))
		{
			final Vector<Item> V2=new Vector<Item>(); // return value
			short topLayer=0;
			short curLayer=0;
			int which=-1;
			while(!V.isEmpty())
			{
				Item I=V.get(0);
				topLayer=(I instanceof Armor)?((Armor)I).getClothingLayer():0;
				which=0;
				for(int v=1;v<V.size();v++)
				{
					I=V.get(v);
					curLayer=(I instanceof Armor)?((Armor)I).getClothingLayer():0;
					if(curLayer<topLayer)
					{
						which=v;
						topLayer=curLayer;
					}
				}
				V2.addElement(V.get(which));
				V.remove(which);
			}
			V=V2;
		}
		return V;
	}

	@Override
	public long parseNumPossibleGold(final Environmental mine, String moneyStr)
	{
		if(moneyStr.toUpperCase().trim().startsWith("A PILE OF "))
			moneyStr=moneyStr.substring(10);
		if(CMath.isInteger(moneyStr))
		{
			final long num=CMath.s_long(moneyStr);
			if(mine instanceof MOB)
			{
				List<Coins> V=CMLib.beanCounter().getMoneyItems((MOB)mine,CMLib.beanCounter().getCurrency(mine));
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v).getNumberOfCoins()>=num)
						return num;
				}
				V=CMLib.beanCounter().getMoneyItems((MOB)mine,null);
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v).getNumberOfCoins()>=num)
						return num;
				}
			}
			return CMath.s_long(moneyStr);
		}
		final Vector<String> V=CMParms.parse(moneyStr);
		if((V.size()>1)
		&&((CMath.isInteger(V.firstElement()))
		&&(matchAnyCurrencySet(CMParms.combine(V,1))!=null)))
			return CMath.s_long(V.firstElement());
		else
		if((V.size()>1)&&(V.firstElement().equalsIgnoreCase("all")))
		{
			final String currency=matchAnyCurrencySet(CMParms.combine(V,1));
			if(currency!=null)
			{
				if(mine instanceof MOB)
				{
					final List<Coins> V2=CMLib.beanCounter().getMoneyItems((MOB)mine,currency);
					final double denomination=matchAnyDenomination(currency,CMParms.combine(V,1));
					Coins C=null;
					for(int v2=0;v2<V2.size();v2++)
					{
						C=V2.get(v2);
						if(C.getDenomination()==denomination)
							return C.getNumberOfCoins();
					}
				}
				return 1;
			}
		}
		else
		if((!V.isEmpty())&&(matchAnyCurrencySet(CMParms.combine(V,0))!=null))
			return 1;
		return 0;
	}

	@Override
	public String parseNumPossibleGoldCurrency(final Environmental mine, String moneyStr)
	{
		if(moneyStr.toUpperCase().trim().startsWith("A PILE OF "))
			moneyStr=moneyStr.substring(10);
		if(CMath.isInteger(moneyStr))
		{
			final long num=CMath.s_long(moneyStr);
			if(mine instanceof MOB)
			{
				List<Coins> V=CMLib.beanCounter().getMoneyItems((MOB)mine,CMLib.beanCounter().getCurrency(mine));
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v).getNumberOfCoins()>=num)
						return V.get(v).getCurrency();
				}
				V=CMLib.beanCounter().getMoneyItems((MOB)mine,null);
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v).getNumberOfCoins()>=num)
						return V.get(v).getCurrency();
				}
			}
			return CMLib.beanCounter().getCurrency(mine);
		}
		final Vector<String> V=CMParms.parse(moneyStr);
		if((V.size()>1)&&(CMath.isInteger(V.firstElement())))
			return matchAnyCurrencySet(CMParms.combine(V,1));
		else
		if((V.size()>1)&&(V.firstElement().equalsIgnoreCase("all")))
			return matchAnyCurrencySet(CMParms.combine(V,1));
		else
		if(!V.isEmpty())
			return matchAnyCurrencySet(CMParms.combine(V,0));
		return CMLib.beanCounter().getCurrency(mine);
	}

	@Override
	public long getMillisMultiplierByName(String timeName)
	{
		timeName=timeName.toLowerCase();
		if("ticks".startsWith(timeName))
			return CMProps.getTickMillis();
		else
		if("seconds".startsWith(timeName))
			return 1000;
		else
		if("minutes".startsWith(timeName))
			return 1000*60;
		else
		if("hours".startsWith(timeName))
			return 1000*60*60;
		else
		if("days".startsWith(timeName))
			return 1000*60*60*24;
		else
		if("weeks".startsWith(timeName))
			return 1000*60*60*24*7;
		else
			return -1;
	}

	@Override
	public double parseNumPossibleGoldDenomination(final Environmental mine, final String currency, String moneyStr)
	{
		if(moneyStr.toUpperCase().trim().startsWith("A PILE OF "))
			moneyStr=moneyStr.substring(10);
		if(CMath.isInteger(moneyStr))
		{
			final long num=CMath.s_long(moneyStr);
			if(mine instanceof MOB)
			{
				final List<Coins> V=CMLib.beanCounter().getMoneyItems((MOB)mine,currency);
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v).getNumberOfCoins()>=num)
						return V.get(v).getDenomination();
				}
			}
			return CMLib.beanCounter().getLowestDenomination(currency);
		}
		final Vector<String> V=CMParms.parse(moneyStr);
		if((V.size()>1)&&(CMath.isInteger(V.firstElement())))
			return matchAnyDenomination(currency,CMParms.combine(V,1));
		else
		if((V.size()>1)&&(V.firstElement().equalsIgnoreCase("all")))
			return matchAnyDenomination(currency,CMParms.combine(V,1));
		else
		if(!V.isEmpty())
			return matchAnyDenomination(currency,CMParms.combine(V,0));
		return 0;
	}

	@Override
	public String matchAnyCurrencySet(final String moneyStr)
	{
		final List<String> V=CMLib.beanCounter().getAllCurrencies();
		List<String> V2=null;
		for(int v=0;v<V.size();v++)
		{
			V2=CMLib.beanCounter().getDenominationNameSet(V.get(v));
			for(int v2=0;v2<V2.size();v2++)
			{
				String s=V2.get(v2);
				if(s.endsWith(")"))
				{
					final String ls=s.toLowerCase();
					if(ls.endsWith("(s)"))
					{
						s=s.substring(0,s.length()-3);
						if(containsString(s,moneyStr))
							return V.get(v);
						s=s+"s";
					}
					else
					if(ls.endsWith("(ys)"))
					{
						s=s.substring(0,s.length()-4);
						if(containsString(s+"y",moneyStr))
							return V.get(v);
						s=s+"ies";
					}
				}
				if(containsString(s,moneyStr))
					return V.get(v);
			}
		}
		return null;
	}

	@Override
	public double matchAnyDenomination(final String currency, String moneyStr)
	{
		if(currency == null)
		{
			for(final String curr : CMLib.beanCounter().getAllCurrencies())
			{
				final MoneyLibrary.MoneyDefinition def=CMLib.beanCounter().getCurrencySet(curr);
				moneyStr=moneyStr.toUpperCase();
				String s=null;
				if(def!=null)
				{
					final MoneyLibrary.MoneyDenomination[] DV=def.denominations();
					for (final MoneyDenomination element : DV)
					{
						s=element.name().toUpperCase();
						if(s.endsWith(")"))
						{
							if(s.endsWith("(S)"))
								s=s.substring(0,s.length()-3)+"S";
							else
							if(s.endsWith("(YS)"))
							{
								s=s.substring(0,s.length()-4);
								if(containsString(s+"y",moneyStr))
									return element.value();
								s=s+"ies";
							}
						}
						if(containsString(s,moneyStr))
							return element.value();
						else
						if((s.length()>0)
						&&(containsString(s,moneyStr)))
							return element.value();
					}
				}
			}
		}
		else
		{
			final MoneyLibrary.MoneyDefinition def=CMLib.beanCounter().getCurrencySet(currency);
			moneyStr=moneyStr.toUpperCase();
			String s=null;
			if(def!=null)
			{
				final MoneyLibrary.MoneyDenomination[] DV=def.denominations();
				for (final MoneyDenomination element : DV)
				{
					s=element.name().toUpperCase();
					if(s.endsWith(")"))
					{
						if(s.endsWith("(S)"))
							s=s.substring(0,s.length()-3)+"S";
						else
						if(s.endsWith("(YS)"))
						{
							s=s.substring(0,s.length()-4);
							if(containsString(s+"y",moneyStr))
								return element.value();
							s=s+"ies";
						}
					}
					if(containsString(s,moneyStr))
						return element.value();
					else
					if((s.length()>0)
					&&(containsString(s,moneyStr)))
						return element.value();
				}
			}
		}
		return 0.0;
	}

	@Override
	public Item parsePossibleRoomGold(final MOB seer, final Room room, final Container container, String moneyStr)
	{
		if(moneyStr.toUpperCase().trim().startsWith("A PILE OF "))
			moneyStr=moneyStr.substring(10);
		long gold=0;
		if(CMath.isInteger(moneyStr))
		{
			gold=CMath.s_long(moneyStr);
			moneyStr="";
		}
		else
		{
			final Vector<String> V=CMParms.parse(moneyStr);
			if((V.size()>1)&&(CMath.isInteger(V.firstElement())))
				gold=CMath.s_long(V.firstElement());
			else
				return null;
			moneyStr=CMParms.combine(V,1);
		}
		if(gold>0)
		{
			for(int i=0;i<room.numItems();i++)
			{
				final Item I=room.getItem(i);
				if((I.container()==container)
				&&(I instanceof Coins)
				&&(CMLib.flags().canBeSeenBy(I,seer))
				&&((moneyStr.length()==0)||(containsString(I.name(),moneyStr))))
				{
					if(((Coins)I).getNumberOfCoins()<=gold)
						return I;
					((Coins)I).setNumberOfCoins(((Coins)I).getNumberOfCoins()-gold);
					final Coins C=(Coins)CMClass.getItem("StdCoins");
					C.setCurrency(((Coins)I).getCurrency());
					C.setNumberOfCoins(gold);
					C.setDenomination(((Coins)I).getDenomination());
					C.setContainer(container);
					C.recoverPhyStats();
					room.addItem(C);
					C.setExpirationDate(I.expirationDate());
					return C;
				}
			}
		}
		return null;
	}

	@Override
	public Item parseBestPossibleGold(final MOB mob, final Container container, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		long gold=0;
		double denomination=0.0;
		String currency=CMLib.beanCounter().getCurrency(mob);
		if(CMath.isInteger(itemID))
		{
			gold=CMath.s_long(itemID);
			//final double totalAmount=CMLib.beanCounter().getTotalAbsoluteValue(mob,currency);
			double bestDenomination=CMLib.beanCounter().getBestDenomination(currency,(int)gold,gold);
			if(bestDenomination==0.0)
			{
				bestDenomination=CMLib.beanCounter().getBestDenomination(null,(int)gold,gold);
				if(bestDenomination>0.0)
					currency=null;
			}
			if(bestDenomination==0.0)
				return null;
			denomination=bestDenomination;
		}
		else
		{
			final Vector<String> V=CMParms.parse(itemID);
			if(V.size()<1)
				return null;
			if((!CMath.isInteger(V.firstElement()))
			&&(!V.firstElement().equalsIgnoreCase("all")))
				V.insertElementAt("1",0);
			final Item I=mob.findItem(container,CMParms.combine(V,1));
			if(I instanceof Coins)
			{
				if(V.firstElement().equalsIgnoreCase("all"))
					gold=((Coins)I).getNumberOfCoins();
				else
					gold=CMath.s_long(V.firstElement());
				currency=((Coins)I).getCurrency();
				denomination=((Coins)I).getDenomination();
			}
			else
				return null;
		}
		if(gold>0)
		{
			final double amt = CMLib.beanCounter().getTotalAbsoluteValue(mob, currency);
			if(amt>=CMath.mul(denomination,gold))
			{
				final double expectedAmt = amt - CMath.mul(denomination,gold);
				CMLib.beanCounter().subtractMoney(mob,currency,denomination,CMath.mul(denomination,gold));
				final double newAmt = CMLib.beanCounter().getTotalAbsoluteValue(mob, currency);
				if(newAmt > expectedAmt)
					CMLib.beanCounter().subtractMoney(mob,currency,(newAmt - expectedAmt));
				final Coins C=(Coins)CMClass.getItem("StdCoins");
				C.setCurrency(currency);
				C.setDenomination(denomination);
				C.setNumberOfCoins(gold);
				C.recoverPhyStats();
				mob.addItem(C);
				return C;
			}
			mob.tell(L("You don't have that much @x1.",CMLib.beanCounter().getDenominationName(currency,denomination)));
			final List<Coins> V=CMLib.beanCounter().getMoneyItems(mob,currency);
			for(int v=0;v<V.size();v++)
			{
				if(V.get(v).getDenomination()==denomination)
					return V.get(v);
			}
		}
		return null;
	}

	protected Filterer<Environmental> getGeneralItemFilter(final String upperSimpleSrchStr)
	{
		if (generalItemFilterer.size() == 0)
		{
			generalItemFilterer.put("ARMOR", new Filterer<Environmental>() {
				@Override
				public boolean passesFilter(final Environmental obj)
				{
					return obj.name().equalsIgnoreCase("armor")
							|| obj.name().equalsIgnoreCase("armors")
							|| (obj instanceof Armor);
				}
			});
			generalItemFilterer.put("ARMORS", generalItemFilterer.get("ARMOR"));
			generalItemFilterer.put("WEAPON", new Filterer<Environmental>() {
				@Override
				public boolean passesFilter(final Environmental obj)
				{
					return obj.name().equalsIgnoreCase("weapon")
							|| obj.name().equalsIgnoreCase("weapons")
							|| (obj instanceof Weapon);
				}
			});
			generalItemFilterer.put("WEAPONS", generalItemFilterer.get("WEAPON"));
			generalItemFilterer.put("COIN", new Filterer<Environmental>() {
				@Override
				public boolean passesFilter(final Environmental obj)
				{
					return obj.name().equalsIgnoreCase("coin")
							|| obj.name().equalsIgnoreCase("coins")
							|| obj.name().equalsIgnoreCase("currency")
							|| obj.name().equalsIgnoreCase("currencies")
							|| obj.name().equalsIgnoreCase("money")
							|| (obj instanceof Coins);
				}
			});
			generalItemFilterer.put("COINS", generalItemFilterer.get("COIN"));
			generalItemFilterer.put("CURRENCY", generalItemFilterer.get("COIN"));
			generalItemFilterer.put("CURRENCIES", generalItemFilterer.get("COIN"));
			generalItemFilterer.put("MONEY", generalItemFilterer.get("COIN"));
			generalItemFilterer.put("ITEM", new Filterer<Environmental>() {
				@Override
				public boolean passesFilter(final Environmental obj)
				{
					return obj.name().equalsIgnoreCase("item")
							|| obj.name().equalsIgnoreCase("items")
							|| (obj instanceof Item);
				}
			});
			generalItemFilterer.put("ITEMS", generalItemFilterer.get("ITEM"));
		}
		return generalItemFilterer.get(upperSimpleSrchStr);
	}

	@Override
	public List<Container> parsePossibleContainers(final MOB mob, final List<String> commands, final Filterer<Environmental> filter, final boolean withContentOnly)
	{
		final Vector<Container> V=new Vector<Container>(1);
		if(commands.size()==1)
			return V;

		int fromDex=-1;
		int containerDex=commands.size()-1;
		for(int i=commands.size()-2;i>0;i--)
		{
			if(commands.get(i).equalsIgnoreCase("from"))
			{
				fromDex=i;
				containerDex=i+1;
				if(((containerDex+1)<commands.size())
				&&((commands.get(containerDex).equalsIgnoreCase("all"))
					||(CMath.s_int(commands.get(containerDex))>0)))
					containerDex++;
				break;
			}
		}

		String possibleContainerID=CMParms.combine(commands,containerDex);

		boolean allFlag=false;
		String preWord="";
		if(possibleContainerID.equalsIgnoreCase("all"))
			allFlag=true;
		else
		if(containerDex>1)
			preWord=commands.get(containerDex-1);

		int maxContained=Integer.MAX_VALUE;
		if(CMath.s_int(preWord)>0)
		{
			maxContained=CMath.s_int(preWord);
			commands.set(containerDex-1,"all");
			containerDex--;
			preWord="all";
		}

		if (preWord.equalsIgnoreCase("all"))
		{
			allFlag = true;
			possibleContainerID = "ALL " + possibleContainerID;
		}
		else
		if (possibleContainerID.toUpperCase().startsWith("ALL."))
		{
			allFlag = true;
			possibleContainerID = "ALL " + possibleContainerID.substring(4);
		}
		else
		if (possibleContainerID.toUpperCase().endsWith(".ALL"))
		{
			allFlag = true;
			possibleContainerID = "ALL " + possibleContainerID.substring(0, possibleContainerID.length() - 4);
		}

		int addendum=1;
		String addendumStr="";
		boolean doBugFix = true;
		while(doBugFix || ((allFlag)&&(addendum<=maxContained)))
		{
			doBugFix=false;
			final Environmental E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID+addendumStr,filter);
			if((E!=null)
			&&(E instanceof Item)
			&&(((Item)E) instanceof Container)
			&&((!withContentOnly)||(((Container)E).hasContent()))
			&&(CMLib.flags().canBeSeenBy(E,mob)||mob.isMine(E)))
			{
				V.addElement((Container)E);
				if(V.size()==1)
				{
					while((fromDex>=0)&&(commands.size()>fromDex))
						commands.remove(fromDex);
					while(commands.size()>containerDex)
						commands.remove(containerDex);
					preWord="";
				}
			}
			if(E==null)
				return V;
			addendumStr="."+(++addendum);
		}
		return V;
	}

	@Override
	public Item parsePossibleContainer(final MOB mob, final List<String> commands, final boolean withStuff, final Filterer<Environmental> filter)
	{
		if(commands.size()==1)
			return null;

		final Room R=(mob==null)?null:mob.location();
		if(R==null)
			return null;

		int fromDex=-1;
		int containerDex=commands.size()-1;
		for(int i=commands.size()-2;i>=1;i--)
		{
			if(commands.get(i).equalsIgnoreCase("from")
			|| commands.get(i).equalsIgnoreCase("in")
			|| commands.get(i).equalsIgnoreCase("on"))
			{
				fromDex=i;
				containerDex=i+1;
				break;
			}
		}

		final String possibleContainerID=CMParms.combine(commands,containerDex);

		Environmental E=R.fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID,filter);
		if(E==null)
		{
			final CMFlagLibrary flagLib=CMLib.flags();
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if(flagLib.isOpenAccessibleContainer(I))
				{
					E=R.fetchFromMOBRoomFavorsItems(mob,I,possibleContainerID,filter);
					if(E instanceof Container)
						break;
				}
			}
		}
		if((E!=null)
		&&(E instanceof Item)
		&&(((Item)E) instanceof Container)
		&&((!withStuff)||(((Container)E).hasContent())))
		{
			while((fromDex>=0)&&(commands.size()>fromDex))
				commands.remove(fromDex);
			while(commands.size()>containerDex)
				commands.remove(containerDex);
			return (Item)E;
		}
		return null;
	}

	@Override
	public String stringifyElapsedTimeOrTicks(long millis, final long ticks)
	{
		String avg="";
		if(ticks>0)
			avg=", Average="+(millis/ticks)+"ms";
		if(millis<1000)
			return millis+"ms"+avg;
		long seconds=millis/1000;
		millis-=(seconds*1000);
		if(seconds<60)
			return seconds+"s "+millis+"ms"+avg;
		long minutes=seconds/60;
		seconds-=(minutes*60);
		if(minutes<60)
			return minutes+"m "+seconds+"s "+millis+"ms"+avg;
		long hours=minutes/60;
		minutes-=(hours*60);
		if(hours<24)
			return hours+"h "+minutes+"m "+seconds+"s "+millis+"ms"+avg;
		final long days=hours/24;
		hours-=(days*24);
		return days+"d "+hours+"h "+minutes+"m "+seconds+"s "+millis+"ms"+avg;
	}

	@Override
	public Triad<String, Double, Long> parseMoneyStringSDL(String currency, String moneyStr)
	{
		double b=0;
		double denomination=1.0;
		if(moneyStr == null)
			return null;
		moneyStr=moneyStr.trim();
		if(moneyStr.length()==0)
			return null;
		int x=0;
		while((x<moneyStr.length())&&(Character.isDigit(moneyStr.charAt(x))))
			x++;
		if(x>0)
		{
			b=CMath.s_int(moneyStr.substring(0,x));
			moneyStr=moneyStr.substring(x).trim();
		}
		if(moneyStr.length()==0)
			return new Triad<String,Double,Long>(currency,Double.valueOf(denomination),Long.valueOf(Math.round(b/denomination)));
		if(b==0)
			b=1.0;
		denomination = matchAnyDenomination(currency,moneyStr);
		if(denomination != 0.0)
			return new Triad<String,Double,Long>(currency,Double.valueOf(denomination),Long.valueOf(Math.round(b/denomination)));
		// its some other kind of money other than the given currency
		currency=matchAnyCurrencySet(moneyStr);
		if(currency == null)
			return null;
		denomination = matchAnyDenomination(currency,moneyStr);
		if(denomination != 0.0)
			return new Triad<String,Double,Long>(currency,Double.valueOf(denomination),Long.valueOf(Math.round(b/denomination)));
		return null;
	}

	@Override
	public Triad<String, Double, Long> parseMoneyStringSDL(final MOB mob, final String moneyStr, String correctCurrency)
	{
		double b=0;
		String myCurrency=CMLib.beanCounter().getCurrency(mob);
		double denomination=1.0;
		if(correctCurrency==null)
			correctCurrency=myCurrency;
		if(moneyStr.length()>0)
		{
			myCurrency=parseNumPossibleGoldCurrency(mob,moneyStr);
			if(myCurrency!=null)
			{
				denomination=parseNumPossibleGoldDenomination(null,correctCurrency,moneyStr);
				final long num=parseNumPossibleGold(null,moneyStr);
				b=CMath.mul(denomination,num);
			}
			else
				myCurrency=CMLib.beanCounter().getCurrency(mob);
		}
		if((denomination == 0)||(b == 0))
			return null;
		return new Triad<String,Double,Long>(myCurrency,Double.valueOf(denomination),Long.valueOf(Math.round(b/denomination)));
	}

	@Override
	public int parseMaxToGive(final MOB mob, final List<String> commands, final boolean breakPackages, final Environmental checkWhat, final boolean getOnly)
	{
		int maxToGive=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(parseNumPossibleGold(mob,CMParms.combine(commands,0))==0))
		{
			if(CMath.s_int(commands.get(0))>0)
			{
				maxToGive=CMath.s_int(commands.get(0));
				commands.set(0,"all");
				if(breakPackages)
				{
					boolean throwError=false;
					if((commands.size()>2)&&("FROM".startsWith(commands.get(1).toUpperCase())))
					{
						throwError=true;
						commands.remove(1);
					}
					final String packCheckName=CMParms.combine(commands,1);
					Environmental fromWhat=null;
					if(checkWhat instanceof MOB)
						fromWhat=mob.findItem(null,packCheckName);
					else
					if(checkWhat instanceof Room) //TODO: this seems to favor the mob, which might be wrong.
						fromWhat=((Room)checkWhat).fetchFromMOBRoomFavorsItems(mob,null,packCheckName,Wearable.FILTER_UNWORNONLY);
					if(fromWhat instanceof Item)
					{
						int max=mob.maxCarry();
						if(max>3000)
							max=3000;
						if(maxToGive>max)
						{
							CMLib.commands().postCommandFail(mob,new XVector<String>(commands),L("You can only handle @x1 at a time.",""+max));
							return -1;
						}
						final Environmental toWhat;
						if(fromWhat instanceof RawMaterial)
						{
							if(((RawMaterial) fromWhat).phyStats().weight()>=maxToGive)
							{
								toWhat=CMLib.materials().splitBundle((Item)fromWhat,maxToGive,null);
								if(toWhat != null)
									maxToGive = 1;
							}
							else
								toWhat=fromWhat;
						}
						else
							toWhat=CMLib.materials().unbundle((Item)fromWhat,maxToGive,null);
						if(toWhat==null)
						{
							if(throwError)
							{
								CMLib.commands().postCommandFail(mob,new XVector<String>(commands),L("You can't get anything from @x1.",fromWhat.name()));
								return -1;
							}
						}
						else
						if(getOnly&&mob.isMine(fromWhat)&&mob.isMine(toWhat))
						{
							CMLib.commands().postCommandFail(mob,new XVector<String>(commands),L("You already have that."));
							return -1;
						}
						else
						if(commands.size()==1)
							commands.add(toWhat.name());
						else
						{
							final String O=commands.get(0);
							commands.clear();
							commands.add(O);
							commands.add(toWhat.name());
						}
					}
					else
					if(throwError)
					{
						CMLib.commands().postCommandFail(mob,new XVector<String>(commands),L("You don't see '@x1' here.",packCheckName));
						return -1;
					}
				}
			}
			else
			if(!CMath.isInteger(commands.get(0)))
			{
				final int x = CMParms.indexOfIgnoreCase(commands,"FROM");
				if((x>0)&&(x<commands.size()-1))
				{
					final String packCheckName=CMParms.combine(commands,x+1);
					final String getName = CMParms.combine(commands,0,x);
					Environmental fromWhat=null;
					if(checkWhat instanceof MOB)
						fromWhat=mob.findItem(null,packCheckName);
					else
					if(checkWhat instanceof Room)
						fromWhat=((Room)checkWhat).fetchFromMOBRoomFavorsItems(mob,null,packCheckName,Wearable.FILTER_UNWORNONLY);
					if(fromWhat instanceof Item)
					{
						final Environmental toWhat=CMLib.materials().unbundle((Item)fromWhat,1,null);
						if((toWhat==null)
						||((!containsString(toWhat.name(), getName))
							&&(!containsString(toWhat.displayText(), getName))))
						{
							return maxToGive;
						}
						else
						if(getOnly&&mob.isMine(fromWhat)&&mob.isMine(toWhat))
						{
							mob.tell(L("Ok"));
							return -1;
						}
						else
						{
							maxToGive = 1;
							commands.clear();
							commands.add(toWhat.name());
						}
					}
				}
			}
		}
		return maxToGive;
	}

	@Override
	public int probabilityOfBeingEnglish(final String str)
	{
		if(str.length()<100)
			return 100;
		final double[] englishFreq = new double[]
		{
			0.08167, 0.01492, 0.02782, 0.04253, 0.12702, 0.02228, 0.02015,  // A-G
			0.06094, 0.06966, 0.00153, 0.00772, 0.04025, 0.02406, 0.06749,  // H-N
			0.07507, 0.01929, 0.00095, 0.05987, 0.06327, 0.09056, 0.02758,  // O-U
			0.00978, 0.02360, 0.00150, 0.01974, 0.00074 					// V-Z
		};
		double punctuationCount=0;
		double wordCount=0;
		double totalLetters=0;
		double thisLetterCount=0;
		final int[] lettersUsed = new int[26];
		final double len=str.length();
		for(int i=0;i<str.length();i++)
		{
			final char c=str.charAt(i);
			if(Character.isLetter(c))
			{
				thisLetterCount+=1;
				lettersUsed[Character.toLowerCase(c)-'a']++;
			}
			else
			if((c==' ')||(isPunctuation((byte)c)))
			{
				if(thisLetterCount>0)
				{
					totalLetters+=thisLetterCount;
					wordCount+=1.0;
					thisLetterCount=0;
				}
				if(c!=' ')
					punctuationCount++;
			}
			else
				punctuationCount++;
		}
		if(thisLetterCount>0)
		{
			totalLetters+=thisLetterCount;
			wordCount+=1.0;
			thisLetterCount=0;
		}
		final double pctPunctuation=punctuationCount/len;
		if(pctPunctuation > .2)
			return 0;
		final double avgWordSize=totalLetters/wordCount;
		if((avgWordSize < 2.0)||(avgWordSize > 8.0))
			return 0;
		double wordCountChi=avgWordSize-4.7;
		wordCountChi=(wordCountChi*wordCountChi)/4.7;
		double chi2=10.0*wordCountChi;
		for (int i = 0; i < 26; i++)
		{
			final double observed = lettersUsed[i];
			final double expected = totalLetters * englishFreq[i];
			final double difference = observed - expected;
			chi2 += (difference*difference) / expected / 26.0;
		}
		final int finalChance=(int)Math.round(100.0 - chi2);
		if(finalChance<0)
			return 0;
		if(finalChance>100)
			return 100;
		return finalChance;
	}

	protected static class FetchFlags
	{
		public String	srchStr;
		public int		occurrance;
		public boolean	allFlag;
		public boolean	lastFlag;
		public Filterer<Environmental> filter = null;

		public FetchFlags(final String ss, final int oc, final boolean af, final boolean lf)
		{
			srchStr = ss;
			occurrance = oc;
			allFlag = af;
			lastFlag = lf;
		}
	}

	protected FetchFlags fetchFlags(final String srchStr, final Filterer<Environmental> filter)
	{
		final FetchFlags flags = fetchFlags(srchStr);
		if(flags == null)
			return null;
		flags.filter = filter;
		if(filter != null)
		{
			final Filterer<Environmental> xtraFilter = getGeneralItemFilter(flags.srchStr);
			if(xtraFilter != null)
			{
				flags.filter = new Filterer<Environmental>() {
					@Override
					public boolean passesFilter(final Environmental obj)
					{
						return filter.passesFilter(obj) && xtraFilter.passesFilter(obj);
					}
				};
				flags.srchStr = "";
			}
		}
		return flags;
	}

	protected FetchFlags fetchFlags(String srchStr)
	{
		if(srchStr.length()==0)
			return null;
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equals("THE")))
			return null;

		boolean allFlag=false;
		boolean lastFlag=false;
		if(srchStr.startsWith("ALL"))
		{
			if(srchStr.length()>3)
			{
				if(srchStr.charAt(3)==' ')
				{
					srchStr=srchStr.substring(4);
					allFlag=true;
				}
			}
			else
				allFlag=true;
		}

		int dot=srchStr.lastIndexOf('.');
		int occurrance=0;
		if(dot>=0)
		{
			String sub=srchStr.substring(dot+1);
			if(sub.equals("LAST"))
			{
				srchStr=srchStr.substring(0,dot);
				lastFlag=true;
				occurrance=Short.MAX_VALUE;
			}
			else
			{
				occurrance=CMath.s_int(sub);
				if(occurrance>0)
					srchStr=srchStr.substring(0,dot);
				else
				{
					dot=srchStr.indexOf('.');
					sub=srchStr.substring(0,dot);
					if(sub.equals("LAST"))
					{
						srchStr=srchStr.substring(dot+1);
						lastFlag=true;
						occurrance=Short.MAX_VALUE;
					}
					else
					{
						occurrance=CMath.s_int(sub);
						if(occurrance>0)
							srchStr=srchStr.substring(dot+1);
						else
							occurrance=0;
					}
				}
			}
		}
		return new FetchFlags(srchStr,occurrance,allFlag,lastFlag);
	}

	protected String cleanExtraneousDollarMarkers(final String srchStr)
	{
		if(srchStr.startsWith("$"))
		{
			if(srchStr.endsWith("$")&&(srchStr.length()>1))
				return srchStr.substring(1,srchStr.length()-1);
			else
				return srchStr.substring(1);
		}
		else
		if(srchStr.endsWith("$"))
			return srchStr.substring(0,srchStr.length()-1);
		return srchStr;
	}

	@Override
	public Environmental fetchEnvironmental(final Iterable<? extends Environmental> list, String srchStr, final boolean exactOnly)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
		Environmental lastO = null;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for (final Environmental E : list)
				{
					if(E!=null)
					{
						if(E.ID().equalsIgnoreCase(srchStr)
						||E.name().equalsIgnoreCase(srchStr)
						||E.Name().equalsIgnoreCase(srchStr))
						{
							if((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0)))
							{
								if((--myOccurrance)<=0)
									return E;
								lastO = E;
							}
						}
					}
				}
			}
			else
			{
				for (final Environmental E : list)
				{
					if((E!=null)
					&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
					&&((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							return E;
						lastO = E;
					}
				}
				if(myOccurrance == flags.occurrance)
				{
					for (final Environmental E : list)
					{
						if((E!=null)
						&&(!(E instanceof Ability))
						&&(containsString(E.displayText(),srchStr)
							||((E instanceof MOB)&&containsString(((MOB)E).genericName(),srchStr))))
						{
							if((--myOccurrance)<=0)
								return E;
							lastO = E;
						}
					}
				}
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		if((flags.lastFlag)&&(lastO != null))
			return lastO;
		return null;
	}

	@Override
	public <T extends Object> T fetchReflective(final Iterable<T> list, String srchStr, final String methodName, final boolean exactOnly)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		T lastO = null;
		final boolean allFlag=flags.allFlag;
		try
		{
			Method M = null;
			for (final T E : list)
			{
				M=E.getClass().getMethod(methodName);
				break;
			}
			if(M==null)
				return null;
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for (final T E : list)
				{
					if(E!=null)
					{
						final String compVal = (String)M.invoke(E);
						if(compVal.equalsIgnoreCase(srchStr))
						{
							if(!allFlag)
							{
								if((--myOccurrance)<=0)
									return E;
								lastO = E;
							}
						}
					}
				}
			}
			else
			{
				for (final T E : list)
				{
					if(E!=null)
					{
						final String compVal = (String)M.invoke(E);
						if((containsString(compVal,srchStr))
						&&(!allFlag))
						{
							if((--myOccurrance)<=0)
								return E;
							lastO = E;
						}
					}
				}
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		catch (final Exception e)
		{
			Log.errOut(e);
		}
		if((flags.lastFlag)&&(lastO != null))
			return lastO;
		return null;
	}

	@Override
	public Exit fetchExit(final Iterable<? extends Environmental> list, String srchStr, final boolean exactOnly)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
		Exit lastO = null;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for (final Environmental E : list)
				{
					if(E instanceof Exit)
					{
						if(E.ID().equalsIgnoreCase(srchStr)
						||E.name().equalsIgnoreCase(srchStr)
						||E.Name().equalsIgnoreCase(srchStr)
						||((Exit)E).doorName().equalsIgnoreCase(srchStr)
						||((Exit)E).closedText().equalsIgnoreCase(srchStr))
						{
							if((!allFlag)||((E.displayText()!=null)&&(E.displayText().length()>0)))
							{
								if((--myOccurrance)<=0)
									return (Exit)E;
								lastO = (Exit)E;
							}
						}
					}
				}
			}
			else
			{
				for (final Environmental E : list)
				{
					if((E instanceof Exit)
					&&(containsString(E.name(),srchStr)
						||containsString(E.Name(),srchStr)
						||containsString(((Exit)E).doorName(),srchStr)
						||containsString(((Exit)E).closedText(),srchStr))
					&&((!allFlag)||((E.displayText()!=null)&&(E.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							return (Exit)E;
						lastO = (Exit)E;
					}
				}
				if(myOccurrance == flags.occurrance)
				{
					for (final Environmental E : list)
					{
						if((E instanceof Exit)
						&&(containsString(E.displayText(),srchStr)))
						{
							if((--myOccurrance)<=0)
								return (Exit)E;
							lastO = (Exit)E;
						}
					}
				}
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		if((flags.lastFlag)&&(lastO != null))
			return lastO;
		return null;
	}

	@Override
	public Environmental fetchEnvironmental(final Iterator<? extends Environmental> iter, String srchStr, final boolean exactOnly)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
		Environmental lastO = null;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for (;iter.hasNext();)
				{
					final Environmental E=iter.next();
					if(E!=null)
					{
						if(E.ID().equalsIgnoreCase(srchStr)
						||E.name().equalsIgnoreCase(srchStr)
						||E.Name().equalsIgnoreCase(srchStr))
						{
							if((!allFlag)
							||(E instanceof Ability)
							||((E.displayText()!=null)&&(E.displayText().length()>0)))
							{
								if((--myOccurrance)<=0)
									return E;
								lastO = E;
							}
						}
					}
				}
			}
			else
			{
				for (;iter.hasNext();)
				{
					final Environmental E=iter.next();
					if((E!=null)
					&&(containsString(E.name(),srchStr)
						||containsString(E.Name(),srchStr)
						||containsString(E.displayText(),srchStr)
						||((E instanceof MOB)&&containsString(((MOB)E).genericName(),srchStr)))
					&&((!allFlag)
						||(E instanceof Ability)
						||((E.displayText()!=null)&&(E.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							return E;
						lastO = E;
					}
				}
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		if((flags.lastFlag)&&(lastO != null))
			return lastO;
		return null;
	}

	@Override
	public Environmental fetchEnvironmental(final Enumeration<? extends Environmental> iter, String srchStr, final boolean exactOnly)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
		Environmental lastO = null;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for (;iter.hasMoreElements();)
				{
					final Environmental E=iter.nextElement();
					if(E!=null)
					{
						if(E.ID().equalsIgnoreCase(srchStr)
						||E.name().equalsIgnoreCase(srchStr)
						||E.Name().equalsIgnoreCase(srchStr))
						{
							if((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0)))
							{
								if((--myOccurrance)<=0)
									return E;
								lastO = E;
							}
						}
					}
				}
			}
			else
			{
				for (;iter.hasMoreElements();)
				{
					final Environmental E=iter.nextElement();
					if((E!=null)
					&&(containsString(E.name(),srchStr)
						||containsString(E.Name(),srchStr)
						||containsString(E.displayText(),srchStr)
						||((E instanceof MOB)&&containsString(((MOB)E).genericName(),srchStr)))
					&&((!allFlag)
						||(E instanceof Ability)
						||((E.displayText()!=null)&&(E.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							return E;
						lastO = E;
					}
				}
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		if((flags.lastFlag)&&(lastO != null))
			return lastO;
		return null;
	}

	@Override
	public List<Environmental> fetchEnvironmentals(final List<? extends Environmental> list, String srchStr, final boolean exactOnly)
	{
		final Vector<Environmental> matches=new Vector<Environmental>(1);
		if(list.isEmpty())
			return matches;
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return matches;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
		Environmental lastO = null;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for (final Environmental E : list)
				{
					if(E!=null)
					{
						if(E.ID().equalsIgnoreCase(srchStr)
						||E.name().equalsIgnoreCase(srchStr)
						||E.Name().equalsIgnoreCase(srchStr))
						{
							if((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0)))
							{
								if((--myOccurrance)<=0)
									matches.addElement(E);
								lastO = E;
							}
						}
					}
				}
			}
			else
			{
				for (final Environmental E : list)
				{
					if((E!=null)
					&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
					&&((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							matches.addElement(E);
						lastO = E;
					}
				}
				if(matches.isEmpty() && (myOccurrance == flags.occurrance))
				{
					for (final Environmental E : list)
					{
						if((E!=null)
						&&(!(E instanceof Ability))
						&&(containsString(E.displayText(),srchStr)
							||((E instanceof MOB)&&containsString(((MOB)E).genericName(),srchStr))))
						{
							if((--myOccurrance)<=0)
								matches.addElement(E);
							lastO = E;
						}
					}
				}
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		if((flags.lastFlag)&&(lastO != null)&&(matches.size()==0))
			matches.add(lastO);
		return matches;
	}

	@Override
	public Environmental fetchEnvironmental(final Map<String, ? extends Environmental> list, String srchStr, final boolean exactOnly)
	{
		if(list.isEmpty())
			return null;
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
		Environmental lastO = null;
		if(list.get(srchStr)!=null)
			return list.get(srchStr);
		Environmental E=null;
		if(exactOnly)
		{
			srchStr=cleanExtraneousDollarMarkers(srchStr);
			for (final String string : list.keySet())
			{
				E=list.get(string);
				if(E!=null)
				{
					if(E.ID().equalsIgnoreCase(srchStr)
					||E.Name().equalsIgnoreCase(srchStr)
					||E.name().equalsIgnoreCase(srchStr))
					{
						if((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0)))
						{
							if((--myOccurrance)<=0)
								return E;
							lastO = E;
						}
					}
				}
			}
		}
		else
		{
			for (final String string : list.keySet())
			{
				E=list.get(string);
				if((E!=null)
				&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
				&&((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0))))
				{
					if((--myOccurrance)<=0)
						return E;
					lastO = E;
				}
			}
			if(myOccurrance == flags.occurrance)
			{
				for (final String string : list.keySet())
				{
					E=list.get(string);
					if(E!=null)
					{
						if((containsString(E.displayText(),srchStr))
						||((E instanceof MOB) && containsString(((MOB)E).genericName(),srchStr)))
						{
							if((--myOccurrance)<=0)
								return E;
							lastO = E;
						}
					}
				}
			}
		}
		if((flags.lastFlag)&&(lastO != null))
			return lastO;
		return null;
	}

	@Override
	public Item fetchAvailableItem(final List<Item> list, String srchStr, final Item goodLocation, final Filterer<Environmental> filter, final boolean exactOnly)
	{
		if(list.isEmpty())
			return null;
		final FetchFlags flags=fetchFlags(srchStr, filter);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
		Item lastO = null;
		if(exactOnly)
		{
			try
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for (final Item I : list)
				{
					if(I==null)
						continue;
					if((I.container()==goodLocation)
					&&(flags.filter.passesFilter(I))
					&&((srchStr.length()==0)
					  ||I.ID().equalsIgnoreCase(srchStr)
					  ||(I.Name().equalsIgnoreCase(srchStr))
					  ||(I.name().equalsIgnoreCase(srchStr))))
					{
						if((!allFlag)
						||((I.displayText()!=null)&&(I.displayText().length()>0)))
						{
							if((--myOccurrance)<=0)
								return I;
							lastO = I;
						}
					}
				}
			}
			catch (final IndexOutOfBoundsException x)
			{
			}
		}
		else
		{
			try
			{
				for (final Item I : list)
				{
					if((I!=null)
					&&(I.container()==goodLocation)
					&&(flags.filter.passesFilter(I))
					&&((srchStr.length()==0)
					   ||(containsString(I.name(),srchStr)||containsString(I.Name(),srchStr)))
					&&((!allFlag)
						||((I.displayText()!=null)&&(I.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							return I;
						lastO = I;
					}
				}
			}
			catch (final IndexOutOfBoundsException x)
			{
			}
			if(myOccurrance == flags.occurrance)
			{
				try
				{
					for (final Item I : list)
					{
						if((I!=null)
						&&(I.container()==goodLocation)
						&&(flags.filter.passesFilter(I))
						&&((srchStr.length()==0)
						  ||(containsString(I.displayText(),srchStr))))
						{
							if((--myOccurrance)<=0)
								return I;
							lastO = I;
						}
					}
				}
				catch (final IndexOutOfBoundsException x)
				{
				}
			}
		}
		if((flags.lastFlag)&&(lastO != null))
			return lastO;
		return null;
	}

	@Override
	public List<Item> fetchAvailableItems(final List<Item> list, String srchStr, final Item goodLocation, final Filterer<Environmental> filter, final boolean exactOnly)
	{
		final Vector<Item> matches=new Vector<Item>(1); // return value
		if(list.isEmpty())
			return matches;
		final FetchFlags flags=fetchFlags(srchStr, filter);
		if(flags==null)
			return matches;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
		Item lastO = null;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for (final Item I : list)
				{
					if(I==null)
						continue;
					if((I.container()==goodLocation)
					&&(flags.filter.passesFilter(I))
					&&((srchStr.length()==0)
					   ||I.ID().equalsIgnoreCase(srchStr)
					   ||(I.Name().equalsIgnoreCase(srchStr))
					   ||(I.name().equalsIgnoreCase(srchStr))))
					{
						if((!allFlag)||((I.displayText()!=null)&&(I.displayText().length()>0)))
						{
							if((--myOccurrance)<=0)
								matches.addElement(I);
							lastO = I;
						}
					}
				}
			}
			else
			{
				for (final Item I : list)
				{
					if(I==null)
						continue;
					if((I.container()==goodLocation)
					&&(flags.filter.passesFilter(I))
					&&((srchStr.length()==0)
						||(containsString(I.name(),srchStr)||containsString(I.Name(),srchStr)))
					&&((!allFlag)||((I.displayText()!=null)&&(I.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							matches.addElement(I);
						lastO = I;
					}
				}
				if(matches.isEmpty() && (myOccurrance == flags.occurrance))
				{
					for (final Item I : list)
					{
						if(I==null)
							continue;
						if((I.container()==goodLocation)
						&&(flags.filter.passesFilter(I))
						&&((srchStr.length()==0)
						   ||(containsString(I.displayText(),srchStr))))
						{
							if((--myOccurrance)<=0)
								matches.addElement(I);
							lastO = I;
						}
					}
				}
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		if((flags.lastFlag)&&(lastO != null)&&(matches.size()==0))
			matches.add(lastO);
		return matches;
	}

	@Override
	public Environmental fetchAvailable(final Collection<? extends Environmental> list, String srchStr, final Item goodLocation, final Filterer<Environmental> filter, final boolean exactOnly, final int[] counterSlap)
	{
		if(list.isEmpty())
			return null;
		final FetchFlags flags=fetchFlags(srchStr, filter);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance - counterSlap[0];
		final boolean allFlag=flags.allFlag;
		Environmental lastO = null;
		Item I=null;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for (final Environmental E : list)
				{
					if(E instanceof Item)
					{
						I=(Item)E;
						if((I.container()==goodLocation)
						&&(flags.filter.passesFilter(I))
						&&((srchStr.length()==0)
						   ||(ID().equalsIgnoreCase(srchStr))
						   ||(I.Name().equalsIgnoreCase(srchStr))
						   ||(I.name().equalsIgnoreCase(srchStr))))
						{
							if((!allFlag)||(E instanceof Ability)||((I.displayText()!=null)&&(I.displayText().length()>0)))
							{
								if((--myOccurrance)<=0)
									return I;
								lastO = I;
							}
						}
					}
					else
					if(E!=null)
					{
						if(E.ID().equalsIgnoreCase(srchStr)
						||E.Name().equalsIgnoreCase(srchStr)
						||E.name().equalsIgnoreCase(srchStr))
						{
							if((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0)))
							{
								if((--myOccurrance)<=0)
									return E;
								lastO = I;
							}
						}
					}
				}
			}
			else
			{
				for (final Environmental E : list)
				{
					if(E instanceof Item)
					{
						I=(Item)E;
						if((I.container()==goodLocation)
						&&(flags.filter.passesFilter(I))
						&&((srchStr.length()==0)
							||(containsString(I.name(),srchStr)||containsString(I.Name(),srchStr)))
						&&((!allFlag)||(E instanceof Ability)||((I.displayText()!=null)&&(I.displayText().length()>0))))
						{
							if((--myOccurrance)<=0)
								return I;
							lastO = I;
						}
					}
					else
					if((E!=null)
					&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
					&&((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							return E;
						lastO = I;
					}
				}

				if(myOccurrance == flags.occurrance-counterSlap[0])
				{
					for (final Environmental E : list)
					{
						if(E instanceof Item)
						{
							I=(Item)E;
							if((I.container()==goodLocation)
							&&(flags.filter.passesFilter(I))
							&&((srchStr.length()==0)
								||(containsString(I.displayText(),srchStr))))
							{
								if((--myOccurrance)<=0)
									return I;
								lastO = I;
							}
						}
						else
						if(E!=null)
						{
							if((containsString(E.displayText(),srchStr))
							||((E instanceof MOB)
								&& containsString(((MOB)E).genericName(),srchStr)))
							{
								if((--myOccurrance)<=0)
									return E;
								lastO = I;
							}
						}
					}
				}
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		if((flags.lastFlag)&&(lastO != null))
			return lastO;
		counterSlap[0]+=(flags.occurrance-myOccurrance);
		return null;
	}

	@Override
	public Environmental fetchAvailable(final Collection<? extends Environmental> list, String srchStr, final Item goodLocation, final Filterer<Environmental> filter, final boolean exactOnly)
	{
		if(list.isEmpty())
			return null;
		final FetchFlags flags=fetchFlags(srchStr, filter);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
		Environmental lastO = null;
		Item I=null;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for (final Environmental E : list)
				{
					if(E instanceof Item)
					{
						I=(Item)E;
						if((I.container()==goodLocation)
						&&(flags.filter.passesFilter(I))
						&&((srchStr.length()==0)
							||(I.ID().equalsIgnoreCase(srchStr)
							||(I.Name().equalsIgnoreCase(srchStr))
							||(I.name().equalsIgnoreCase(srchStr)))))
						{
							if((!allFlag)||(E instanceof Ability)||((I.displayText()!=null)&&(I.displayText().length()>0)))
							{
								if((--myOccurrance)<=0)
									return I;
								lastO = I;
							}
						}
					}
					else
					if(E!=null)
					{
						if(E.ID().equalsIgnoreCase(srchStr)
						||E.Name().equalsIgnoreCase(srchStr)
						||E.name().equalsIgnoreCase(srchStr))
						{
							if((!allFlag)
							||(E instanceof Ability)
							||((E.displayText()!=null)&&(E.displayText().length()>0)))
							{
								if((--myOccurrance)<=0)
									return E;
								lastO = E;
							}
						}
					}
				}
			}
			else
			{
				for (final Environmental E : list)
				{
					if(E instanceof Item)
					{
						I=(Item)E;
						if((I.container()==goodLocation)
						&&(flags.filter.passesFilter(I))
						&&((srchStr.length()==0)
							||(containsString(I.name(),srchStr)||containsString(I.Name(),srchStr)))
						&&((!allFlag)||(E instanceof Ability)||((I.displayText()!=null)&&(I.displayText().length()>0))))
						{
							if((--myOccurrance)<=0)
								return I;
							lastO = I;
						}
					}
					else
					if((E!=null)
					&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
					&&((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							return E;
						lastO = E;
					}
				}

				if(myOccurrance == flags.occurrance)
				{
					for (final Environmental E : list)
					{
						if(E instanceof Item)
						{
							I=(Item)E;
							if((I.container()==goodLocation)
							&&(flags.filter.passesFilter(I))
							&&((srchStr.length()==0)
								||(containsString(I.displayText(),srchStr))))
							{
								if((--myOccurrance)<=0)
									return I;
								lastO = I;
							}
						}
						else
						if(E!=null)
						{
							if((containsString(E.displayText(),srchStr))
							||((E instanceof MOB)&&containsString(((MOB)E).genericName(),srchStr)))
							{
								if((--myOccurrance)<=0)
									return E;
								lastO = E;
							}
						}
					}
				}
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		if((flags.lastFlag)&&(lastO != null))
			return lastO;
		return null;
	}
}
