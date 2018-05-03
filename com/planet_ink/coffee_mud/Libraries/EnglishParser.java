package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

	private final static String[]			ARTICLES			= { "a", "an", "all of", "some one", "a pair of", "one of", "all", "the", "some", "each" };
	public static boolean[]					PUNCTUATION_TABLE	= null;
	public final static char[]				ALL_CHRS			= "ALL".toCharArray();
	public final static String[]			fwords				= { "calf", "half", "knife", "life", "wife", "elf", "self", "shelf", "leaf", "sheaf", "thief", "loaf", "wolf" };
	public final static List<Environmental>	empty				= new ReadOnlyVector<Environmental>(1);

	@Override
	public String toEnglishStringList(final String[] V)
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
		s.append(" and ");
		s.append(V[V.length-1]);
		return s.toString();
	}

	@Override
	public String toEnglishStringList(final Class<? extends Enum<?>> enumer, boolean andOr)
	{
		Enum<?>[] V=enumer.getEnumConstants();
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
		for(Iterator<? extends Object> o=V.iterator();o.hasNext();)
		{
			if(s.length()>0) 
				s.append(", ");
			final Object O = o.next();
			if(!o.hasNext())
				s.append(" and ");
			s.append(O.toString());
		}
		return s.toString();
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
	public String makePlural(String str)
	{
		if((str==null)||(str.length()==0))
			return str;
		final boolean uppercase=Character.isUpperCase(str.charAt(str.length()-1));
		final String lowerStr=str.toLowerCase();
		if(lowerStr.endsWith("is"))
			return str.substring(0,str.length()-2)+(uppercase?"ES":"es");
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
	public String cleanArticles(String s)
	{
		final String lowStr=s.toLowerCase();
		for (final String article : ARTICLES)
		{
			if(lowStr.startsWith(article+" "))
				return s.substring(article.length()+1);
		}
		return s;
	}

	@Override
	public String properIndefiniteArticle(String str)
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
		for(SpaceObject.Distance distance : SpaceObject.DISTANCES)
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
	public String sizeDescShort(long size)
	{
		return getBestDistance(size);
	}
	
	@Override
	public String distanceDescShort(long distance)
	{
		return getBestDistance(distance);
	}
	
	@Override
	public String coordDescShort(long[] coords)
	{
		return getBestDistance(coords[0])+","+getBestDistance(coords[1])+","+getBestDistance(coords[2]);
	}
	
	@Override
	public String speedDescShort(double speed)
	{
		return getBestDistance(Math.round(speed))+"/sec";
	}
	
	@Override
	public String directionDescShort(double[] dir)
	{
		return Math.round(Math.toDegrees(dir[0])*100)/100.0+" mark "+Math.round(Math.toDegrees(dir[1])*100)/100.0;
	}
	
	@Override
	public Long parseSpaceDistance(String dist)
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
		Long value=Long.valueOf(dist.substring(0,digits+1));
		String unit=dist.substring(digits+1).trim();
		if(unit.length()==0)
			return value;
		SpaceObject.Distance distUnit=(SpaceObject.Distance)CMath.s_valueOf(SpaceObject.Distance.class, unit);
		if(distUnit==null)
		{
			for(SpaceObject.Distance d : SpaceObject.Distance.values())
			{
				if(d.abbr.equalsIgnoreCase(unit))
					distUnit=d;
			}
		}
		if(distUnit==null)
		{
			for(SpaceObject.Distance d : SpaceObject.Distance.values())
			{
				if(d.name().equalsIgnoreCase(unit))
					distUnit=d;
			}
		}
		if(distUnit==null)
		{
			for(SpaceObject.Distance d : SpaceObject.Distance.values())
			{
				if(unit.toLowerCase().startsWith(d.name().toLowerCase()))
					distUnit=d;
			}
		}
		if(distUnit==null)
			return null;
		return new Long(value.longValue() * distUnit.dm);
	}
	
	@Override
	public String getFirstWord(final String str)
	{
		int i=0;
		int start=-1;
		for(;i<str.length();i++)
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
	public boolean startsWithAnArticle(String s)
	{
		return isAnArticle(getFirstWord(s));
	}
	
	@Override
	public String insertUnColoredAdjective(String str, String adjective)
	{
		if(str.length()==0)
			return str;
		str=CMStrings.removeColors(str.trim());
		final String uppStr=str.toUpperCase();
		if((uppStr.startsWith("A "))
		||(uppStr.startsWith("AN ")))
			return properIndefiniteArticle(adjective)+" "+adjective+" "+str.substring(2).trim();
		if(uppStr.startsWith("THE "))
			return properIndefiniteArticle(adjective)+" "+adjective+" "+str.substring(3).trim();
		if(uppStr.startsWith("SOME "))
			return properIndefiniteArticle(adjective)+" "+adjective+" "+str.substring(4).trim();
		return properIndefiniteArticle(adjective)+" "+adjective+" "+str.trim();
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
	public String insertAdjectives(String paragraph, String[] adjsToChoose, int pctChance)
	{
		if((paragraph.length()==0)||(adjsToChoose==null)||(adjsToChoose.length==0))
			return paragraph;
		StringBuilder newParagraph = new StringBuilder("");
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

	@Override
	public CMObject findCommand(MOB mob, List<String> commands)
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
			return C;

		Ability A=getToEvoke(mob,new XVector<String>(commands));
		if((A!=null)
		&&(!CMSecurity.isAbilityDisabled(A.ID().toUpperCase())))
			return A;

		if(getAnEvokeWord(mob,firstWord)!=null)
			return null;

		Social social=CMLib.socials().fetchSocial(commands,true,true);
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
						final Ability A2=getToEvoke(mob,commands2);
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
			return C;

		social=CMLib.socials().fetchSocial(commands,false,true);
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
		return null;
	}

	@Override
	public boolean evokedBy(Ability thisAbility, String thisWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(thisAbility.triggerStrings()[i].equalsIgnoreCase(thisWord))
				return true;
		}
		return false;
	}

	private String collapsedName(Ability thisAbility)
	{
		final int x=thisAbility.name().indexOf(' ');
		if(x>=0)
			return CMStrings.replaceAll(thisAbility.name()," ","");
		return thisAbility.Name();
	}

	@Override
	public boolean evokedBy(Ability thisAbility, String thisWord, String secondWord)
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
	public String getAnEvokeWord(MOB mob, String word)
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
	public Ability getToEvoke(MOB mob, List<String> commands)
	{
		final String evokeWord=commands.get(0).toUpperCase();

		boolean foundMoreThanOne=false;
		Ability evokableAbility=null;
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(evokedBy(A,evokeWord)))
			{
				if((evokableAbility!=null)&&(!A.ID().equals(evokableAbility.ID())))
				{
					foundMoreThanOne=true;
					evokableAbility=null;
					break;
				}
				evokableAbility=A;
			}
		}

		if((evokableAbility!=null)&&(commands.size()>1))
		{
			final int classCode=evokableAbility.classificationCode()&Ability.ALL_ACODES;
			switch(classCode)
			{
			case Ability.ACODE_SPELL:
			case Ability.ACODE_SONG:
			case Ability.ACODE_PRAYER:
			case Ability.ACODE_CHANT:
				evokableAbility=null;
				foundMoreThanOne=true;
				break;
			default:
				break;
			}
		}

		if(evokableAbility!=null)
			commands.remove(0);
		else
		if((foundMoreThanOne)&&(commands.size()>1))
		{
			commands.remove(0);
			foundMoreThanOne=false;
			final String secondWord=commands.get(0).toUpperCase();
			for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)
				&&(evokedBy(A,evokeWord,secondWord.toUpperCase())))
				{
					if((A.name().equalsIgnoreCase(secondWord))
					||(collapsedName(A).equalsIgnoreCase(secondWord)))
					{
						evokableAbility=A;
						foundMoreThanOne=false;
						break;
					}
					else
					if((evokableAbility!=null)&&(!A.ID().equals(evokableAbility.ID())))
						foundMoreThanOne=true;
					else
						evokableAbility=A;
				}
			}
			if((evokableAbility!=null)&&(!foundMoreThanOne))
				commands.remove(0);
			else
			if((foundMoreThanOne)&&(commands.size()>1))
			{
				final String secondAndThirdWord=secondWord+" "+commands.get(1).toUpperCase();

				for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null) && (evokedBy(A,evokeWord,secondAndThirdWord.toUpperCase())))
					{
						evokableAbility=A;
						break;
					}
				}
				if(evokableAbility!=null)
				{
					commands.remove(0);
					commands.remove(0);
				}
			}
			else
			{
				for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)
					&&(evokedBy(A,evokeWord))
					&&(A.name().toUpperCase().indexOf(" "+secondWord.toUpperCase())>0))
					{
						evokableAbility=A;
						commands.remove(0);
						break;
					}
				}
			}
		}
		return evokableAbility;
	}

	@Override
	public boolean preEvoke(MOB mob, List<String> commands, int secondsElapsed, double actionsRemaining)
	{
		commands=new Vector<String>(commands);
		final Ability evokableAbility=getToEvoke(mob,commands);
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
	public void evoke(MOB mob, Vector<String> commands)
	{
		final Ability evokableAbility=getToEvoke(mob,commands);
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
				case '`': case '~': case '!': case '@': case '#': case '$': case '%':
				case '^': case '&': case '*': case '(': case ')': case '_': case '-':
				case '+': case '=': case '[': case ']': case '{': case '}': case '\\':
				case '|': case ';': case ':': case '\'': case '\"': case ',': case '<':
				case '.': case '>': case '/': case '?':
					PUNCTUATION_TEMP_TABLE[c]=true;
					break;
				default:
					PUNCTUATION_TEMP_TABLE[c]=false;
				}
			PUNCTUATION_TABLE=PUNCTUATION_TEMP_TABLE;
		}
		return PUNCTUATION_TABLE;
	}

	private boolean isPunctuation(final byte b)
	{
		if((b<0)||(b>255))
			return false;
		return PUNCTUATION_TABLE[b];
	}

	@Override
	public boolean hasPunctuation(String str)
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
	public String stripPunctuation(String str)
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
	public List<String> parseWords(final String thisStr)
	{
		if((thisStr==null)||(thisStr.length()==0))
			return new Vector<String>(1);
		return CMParms.parseSpaces(CMLib.english().stripPunctuation(thisStr), true);
	}

	public boolean equalsPunctuationless(char[] strC, char[] str2C)
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
	public boolean containsString(final String toSrchStr, final String srchStr)
	{
		if((toSrchStr==null)||(srchStr==null))
			return false;
		if((toSrchStr.length()==0)&&(srchStr.length()>0))
			return false;
		char[] srchC=srchStr.toCharArray();
		final char[] toSrchC=toSrchStr.toCharArray();
		for(int c=0;c<srchC.length;c++)
			srchC[c]=Character.toUpperCase(srchC[c]);
		for(int c=0;c<toSrchC.length;c++)
			toSrchC[c]=Character.toUpperCase(toSrchC[c]);
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
				case ';': x++;
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
	public String bumpDotNumber(String srchStr)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return srchStr;
		if(flags.allFlag)
			return srchStr;
		if(flags.occurrance==0)
			return "1."+flags.srchStr;
		return (flags.occurrance+1)+"."+flags.srchStr;
	}

	@Override
	public int getContextNumber(ItemCollection cont, Environmental E)
	{
		return getContextNumber(toCollection(cont),E);
	}
	
	@Override
	public int getContextNumber(Environmental[] list, Environmental E)
	{
		return getContextNumber(new XVector<Environmental>(list),E);
	}
	
	@Override
	public int getContextNumber(Collection<? extends Environmental> list, Environmental E)
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
	
	private Collection<? extends Environmental> toCollection(ItemCollection cont)
	{
		final LinkedList<Item> list=new LinkedList<Item>();
		for(final Enumeration<Item> i=cont.items();i.hasMoreElements();)
			list.add(i.nextElement());
		return list;
	}

	@Override
	public int getContextSameNumber(ItemCollection cont, Environmental E)
	{
		return getContextSameNumber(toCollection(cont),E);
	}
	
	@Override
	public int getContextSameNumber(Environmental[] list, Environmental E)
	{
		return getContextSameNumber(new XVector<Environmental>(list),E);
	}
	
	@Override
	public int getContextSameNumber(Collection<? extends Environmental> list, Environmental E)
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
	public String getContextName(ItemCollection cont, Environmental E)
	{
		return getContextName(toCollection(cont),E);
	}
	
	@Override
	public String getContextName(Environmental[] list, Environmental E)
	{
		return getContextName(new XVector<Environmental>(list),E);
	}
	
	@Override
	public String getContextName(Collection<? extends Environmental> list, Environmental E)
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
	public List<String> getAllContextNames(Collection<? extends Environmental> list, Filterer<Environmental> filter)
	{
		if(list==null) 
			return new ArrayList<String>();
		final List<String> flist = new Vector<String>(list.size());
		Map<String,int[]> prevFound = new HashMap<String,int[]>();
		for(Environmental E : list)
		{
			if((filter!=null)&&(!filter.passesFilter(E)))
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
	public String getContextSameName(ItemCollection cont, Environmental E)
	{
		return getContextSameName(toCollection(cont),E);
	}
	
	@Override
	public String getContextSameName(Environmental[] list, Environmental E)
	{
		return getContextSameName(new XVector<Environmental>(list),E);
	}
	
	@Override
	public String getContextSameName(Collection<? extends Environmental> list, Environmental E)
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
	public Environmental parseShopkeeper(MOB mob, List<String> commands, String error)
	{
		if(commands.isEmpty())
		{
			if(error.length()>0)
				mob.tell(error);
			return null;
		}
		commands.remove(0);

		final List<Environmental> V=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
		if(V.isEmpty())
		{
			if(error.length()>0)
				mob.tell(error);
			return null;
		}
		if(V.size()>1)
		{
			if(commands.size()<2)
			{
				if(error.length()>0)
					mob.tell(error);
				return null;
			}
			final String what=commands.get(commands.size()-1);

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
			if((shopkeeper!=null)&&(CMLib.coffeeShops().getShopKeeper(shopkeeper)!=null)&&(CMLib.flags().canBeSeenBy(shopkeeper,mob)))
				commands.remove(commands.size()-1);
			else
			{
				CMLib.commands().postCommandFail(mob,new XVector<String>(commands),
						L("You don't see anyone called '@x1' here buying or selling.",commands.get(commands.size()-1)));
				return null;
			}
			return shopkeeper;
		}
		Environmental shopkeeper=V.get(0);
		if(commands.size()>1)
		{
			final MOB M=mob.location().fetchInhabitant(commands.get(commands.size()-1));
			if((M!=null)&&(CMLib.coffeeShops().getShopKeeper(M)!=null)&&(CMLib.flags().canBeSeenBy(M,mob)))
			{
				shopkeeper=M;
				commands.remove(commands.size()-1);
			}
		}
		return shopkeeper;
	}

	@Override
	public List<Item> fetchItemList(Environmental from,
									MOB mob,
									Item container,
									List<String> commands,
									Filterer<Environmental> filter,
									boolean visionMatters)
	{
		int addendum=1;
		String addendumStr="";
		List<Item> V=new Vector<Item>();

		int maxToItem=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(CMath.s_int(commands.get(0))>0))
		{
			maxToItem=CMath.s_int(commands.get(0));
			commands.set(0,"all");
		}

		String name=CMParms.combine(commands,0);
		boolean allFlag = (!commands.isEmpty()) ? commands.get(0).equalsIgnoreCase("all") : false;
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
			final Vector<Item> V2=new Vector<Item>();
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
			final Vector<Item> V2=new Vector<Item>();
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
	public long numPossibleGold(Environmental mine, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(CMath.isInteger(itemID))
		{
			final long num=CMath.s_long(itemID);
			if(mine instanceof MOB)
			{
				List<Coins> V=CMLib.beanCounter().getStandardCurrency((MOB)mine,CMLib.beanCounter().getCurrency(mine));
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v).getNumberOfCoins()>=num)
						return num;
				}
				V=CMLib.beanCounter().getStandardCurrency((MOB)mine,null);
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v).getNumberOfCoins()>=num)
						return num;
				}
			}
			return CMath.s_long(itemID);
		}
		final Vector<String> V=CMParms.parse(itemID);
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
					final List<Coins> V2=CMLib.beanCounter().getStandardCurrency((MOB)mine,currency);
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
	public String numPossibleGoldCurrency(Environmental mine, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(CMath.isInteger(itemID))
		{
			final long num=CMath.s_long(itemID);
			if(mine instanceof MOB)
			{
				List<Coins> V=CMLib.beanCounter().getStandardCurrency((MOB)mine,CMLib.beanCounter().getCurrency(mine));
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v).getNumberOfCoins()>=num)
						return V.get(v).getCurrency();
				}
				V=CMLib.beanCounter().getStandardCurrency((MOB)mine,null);
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v).getNumberOfCoins()>=num)
						return V.get(v).getCurrency();
				}
			}
			return CMLib.beanCounter().getCurrency(mine);
		}
		final Vector<String> V=CMParms.parse(itemID);
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
	public double numPossibleGoldDenomination(Environmental mine, String currency, String moneyStr)
	{
		if(moneyStr.toUpperCase().trim().startsWith("A PILE OF "))
			moneyStr=moneyStr.substring(10);
		if(CMath.isInteger(moneyStr))
		{
			final long num=CMath.s_long(moneyStr);
			if(mine instanceof MOB)
			{
				final List<Coins> V=CMLib.beanCounter().getStandardCurrency((MOB)mine,currency);
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
	public String matchAnyCurrencySet(String moneyStr)
	{
		final List<String> V=CMLib.beanCounter().getAllCurrencies();
		List<String> V2=null;
		for(int v=0;v<V.size();v++)
		{
			V2=CMLib.beanCounter().getDenominationNameSet(V.get(v));
			for(int v2=0;v2<V2.size();v2++)
			{
				String s=V2.get(v2);
				if(s.toLowerCase().endsWith("(s)"))
					s=s.substring(0,s.length()-3)+"s";
				if(containsString(s,moneyStr))
					return V.get(v);
			}
		}
		return null;
	}

	@Override
	public double matchAnyDenomination(String currency, String moneyStr)
	{
		if(currency == null)
		{
			for(final String curr : CMLib.beanCounter().getAllCurrencies())
			{
				final MoneyLibrary.MoneyDenomination[] DV=CMLib.beanCounter().getCurrencySet(curr);
				moneyStr=moneyStr.toUpperCase();
				String s=null;
				if(DV!=null)
				{
					for (final MoneyDenomination element : DV)
					{
						s=element.name().toUpperCase();
						if(s.endsWith("(S)"))
							s=s.substring(0,s.length()-3)+"S";
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
			final MoneyLibrary.MoneyDenomination[] DV=CMLib.beanCounter().getCurrencySet(currency);
			moneyStr=moneyStr.toUpperCase();
			String s=null;
			if(DV!=null)
			{
				for (final MoneyDenomination element : DV)
				{
					s=element.name().toUpperCase();
					if(s.endsWith("(S)"))
						s=s.substring(0,s.length()-3)+"S";
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
	public Item possibleRoomGold(MOB seer, Room room, Container container, String moneyStr)
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
	public Item bestPossibleGold(MOB mob, Container container, String itemID)
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
			final List<Coins> V=CMLib.beanCounter().getStandardCurrency(mob,currency);
			for(int v=0;v<V.size();v++)
			{
				if(V.get(v).getDenomination()==denomination)
					return V.get(v);
			}
		}
		return null;
	}

	@Override
	public List<Container> possibleContainers(MOB mob, List<String> commands, Filterer<Environmental> filter, boolean withContentOnly)
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
	public Item possibleContainer(MOB mob, List<String> commands, boolean withStuff, Filterer<Environmental> filter)
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
			if(commands.get(i).equalsIgnoreCase("from") || commands.get(i).equalsIgnoreCase("in")|| commands.get(i).equalsIgnoreCase("on"))
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
	public String returnTime(long millis, long ticks)
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
	public Triad<String, Double, Long> parseMoneyStringSDL(MOB mob, String amount, String correctCurrency)
	{
		double b=0;
		String myCurrency=CMLib.beanCounter().getCurrency(mob);
		double denomination=1.0;
		if(correctCurrency==null)
			correctCurrency=myCurrency;
		if(amount.length()>0)
		{
			myCurrency=CMLib.english().numPossibleGoldCurrency(mob,amount);
			if(myCurrency!=null)
			{
				denomination=CMLib.english().numPossibleGoldDenomination(null,correctCurrency,amount);
				final long num=CMLib.english().numPossibleGold(null,amount);
				b=CMath.mul(denomination,num);
			}
			else
				myCurrency=CMLib.beanCounter().getCurrency(mob);
		}
		return new Triad<String,Double,Long>(myCurrency,Double.valueOf(denomination),Long.valueOf(Math.round(b/denomination)));
	}

	@Override
	public int calculateMaxToGive(MOB mob, List<String> commands, boolean breakPackages, Environmental checkWhat, boolean getOnly)
	{
		int maxToGive=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(CMLib.english().numPossibleGold(mob,CMParms.combine(commands,0))==0))
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
					if(checkWhat instanceof Room)
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
						final Environmental toWhat=CMLib.materials().unbundle((Item)fromWhat,maxToGive,null);
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
							mob.tell(L("Ok"));
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
				int x = CMParms.indexOfIgnoreCase(commands,"FROM");
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
						||((!CMLib.english().containsString(toWhat.name(), getName))
							&&(!CMLib.english().containsString(toWhat.displayText(), getName))))
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
	public int probabilityOfBeingEnglish(String str)
	{
		if(str.length()<100)
			return 100;
		final double[] englishFreq = new double[]{
		0.08167, 0.01492, 0.02782, 0.04253, 0.12702, 0.02228, 0.02015,  // A-G
		0.06094, 0.06966, 0.00153, 0.00772, 0.04025, 0.02406, 0.06749,  // H-N
		0.07507, 0.01929, 0.00095, 0.05987, 0.06327, 0.09056, 0.02758,  // O-U
		0.00978, 0.02360, 0.00150, 0.01974, 0.00074                     // V-Z
		};
		double punctuationCount=0;
		double wordCount=0;
		double totalLetters=0;
		double thisLetterCount=0;
		int[] lettersUsed = new int[26];
		double len=str.length();
		for(int i=0;i<str.length();i++)
		{
			char c=str.charAt(i);
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
		double pctPunctuation=punctuationCount/len;
		if(pctPunctuation > .2)
			return 0;
		double avgWordSize=totalLetters/wordCount;
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

		public FetchFlags(String ss, int oc, boolean af)
		{
			srchStr = ss;
			occurrance = oc;
			allFlag = af;
		}
	}

	public FetchFlags fetchFlags(String srchStr)
	{
		if(srchStr.length()==0)
			return null;
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equals("THE")))
			return null;

		boolean allFlag=false;
		if(srchStr.startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equals("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf('.');
		int occurrance=0;
		if(dot>0)
		{
			String sub=srchStr.substring(dot+1);
			occurrance=CMath.s_int(sub);
			if(occurrance>0)
				srchStr=srchStr.substring(0,dot);
			else
			{
				dot=srchStr.indexOf('.');
				sub=srchStr.substring(0,dot);
				occurrance=CMath.s_int(sub);
				if(occurrance>0)
					srchStr=srchStr.substring(dot+1);
				else
					occurrance=0;
			}
		}
		return new FetchFlags(srchStr,occurrance,allFlag);
	}

	protected String cleanExtraneousDollarMarkers(String srchStr)
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
	public Environmental fetchEnvironmental(Iterable<? extends Environmental> list, String srchStr, boolean exactOnly)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
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
							}
						}
					}
				}
			}
			else
			{
				myOccurrance=flags.occurrance;
				for (final Environmental E : list)
				{
					if((E!=null)
					&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
					&&((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							return E;
					}
				}
				myOccurrance=flags.occurrance;
				for (final Environmental E : list)
				{
					if((E!=null)
					&&(!(E instanceof Ability))
					&&(containsString(E.displayText(),srchStr)
						||((E instanceof MOB)&&containsString(((MOB)E).genericName(),srchStr))))
					{
						if((--myOccurrance)<=0)
							return E;
					}
				}
			}
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Exit fetchExit(Iterable<? extends Environmental> list, String srchStr, boolean exactOnly)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
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
							}
						}
					}
				}
			}
			else
			{
				myOccurrance=flags.occurrance;
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
					}
				}
				myOccurrance=flags.occurrance;
				for (final Environmental E : list)
				{
					if((E instanceof Exit)
					&&(containsString(E.displayText(),srchStr)))
					{
						if((--myOccurrance)<=0)
							return (Exit)E;
					}
				}
			}
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Environmental fetchEnvironmental(Iterator<? extends Environmental> iter, String srchStr, boolean exactOnly)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
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
							}
						}
					}
				}
			}
			else
			{
				myOccurrance=flags.occurrance;
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
					}
				}
			}
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Environmental fetchEnvironmental(Enumeration<? extends Environmental> iter, String srchStr, boolean exactOnly)
	{
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
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
							}
						}
					}
				}
			}
			else
			{
				myOccurrance=flags.occurrance;
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
					}
				}
			}
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public List<Environmental> fetchEnvironmentals(List<? extends Environmental> list, String srchStr, boolean exactOnly)
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
							}
						}
					}
				}
			}
			else
			{
				myOccurrance=flags.occurrance;
				for (final Environmental E : list)
				{
					if((E!=null)
					&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
					&&((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							matches.addElement(E);
					}
				}
				if(matches.isEmpty())
				{
					myOccurrance=flags.occurrance;
					for (final Environmental E : list)
					{
						if((E!=null)
						&&(!(E instanceof Ability))
						&&(containsString(E.displayText(),srchStr)
							||((E instanceof MOB)&&containsString(((MOB)E).genericName(),srchStr))))
						{
							if((--myOccurrance)<=0)
								matches.addElement(E);
						}
					}
				}
			}
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return matches;
	}

	@Override
	public Environmental fetchEnvironmental(Map<String, ? extends Environmental> list, String srchStr, boolean exactOnly)
	{
		if(list.isEmpty())
			return null;
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;

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
						}
					}
				}
			}
		}
		else
		{
			myOccurrance=flags.occurrance;
			for (final String string : list.keySet())
			{
				E=list.get(string);
				if((E!=null)
				&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
				&&((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0))))
				{
					if((--myOccurrance)<=0)
						return E;
				}
			}
			myOccurrance=flags.occurrance;
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
					}
				}
			}
		}
		return null;
	}

	@Override
	public Item fetchAvailableItem(List<Item> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly)
	{
		if(list.isEmpty())
			return null;
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
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
					&&(filter.passesFilter(I))
					&&(I.ID().equalsIgnoreCase(srchStr)
					  ||(I.Name().equalsIgnoreCase(srchStr))
					  ||(I.name().equalsIgnoreCase(srchStr))))
					{
						if((!allFlag)
						||((I.displayText()!=null)&&(I.displayText().length()>0)))
						{
							if((--myOccurrance)<=0)
								return I;
						}
					}
				}
			}
			catch (final java.lang.ArrayIndexOutOfBoundsException x)
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
					&&(filter.passesFilter(I))
					&&(containsString(I.name(),srchStr)||containsString(I.Name(),srchStr))
					&&((!allFlag)
						||((I.displayText()!=null)&&(I.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							return I;
					}
				}
			}
			catch (final java.lang.ArrayIndexOutOfBoundsException x)
			{
			}
			myOccurrance=flags.occurrance;
			try
			{
				for (final Item I : list)
				{
					if((I!=null)
					&&(I.container()==goodLocation)
					&&(filter.passesFilter(I))
					&&(containsString(I.displayText(),srchStr)))
					{
						if((--myOccurrance)<=0)
							return I;
					}
				}
			}
			catch (final java.lang.ArrayIndexOutOfBoundsException x)
			{
			}
		}
		return null;
	}

	@Override
	public List<Item> fetchAvailableItems(List<Item> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly)
	{
		final Vector<Item> matches=new Vector<Item>(1);
		if(list.isEmpty())
			return matches;
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return matches;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;
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
					&&(filter.passesFilter(I))
					&&(I.ID().equalsIgnoreCase(srchStr)
					   ||(I.Name().equalsIgnoreCase(srchStr))
					   ||(I.name().equalsIgnoreCase(srchStr))))
					{
						if((!allFlag)||((I.displayText()!=null)&&(I.displayText().length()>0)))
						{
							if((--myOccurrance)<=0)
								matches.addElement(I);
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
					&&(filter.passesFilter(I))
					&&(containsString(I.name(),srchStr)||containsString(I.Name(),srchStr))
					&&((!allFlag)||((I.displayText()!=null)&&(I.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							matches.addElement(I);
					}
				}
				if(matches.isEmpty())
				{
					myOccurrance=flags.occurrance;
					for (final Item I : list)
					{
						if(I==null)
							continue;
						if((I.container()==goodLocation)
						&&(filter.passesFilter(I))
						&&(containsString(I.displayText(),srchStr)))
						{
							if((--myOccurrance)<=0)
								matches.addElement(I);
						}
					}
				}
			}
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return matches;
	}

	@Override
	public Environmental fetchAvailable(Collection<? extends Environmental> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly, int[] counterSlap)
	{
		if(list.isEmpty())
			return null;
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance - counterSlap[0];
		final boolean allFlag=flags.allFlag;

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
						&&(filter.passesFilter(I))
						&&(I.ID().equalsIgnoreCase(srchStr)
						   ||(I.Name().equalsIgnoreCase(srchStr))
						   ||(I.name().equalsIgnoreCase(srchStr))))
						{
							if((!allFlag)||(E instanceof Ability)||((I.displayText()!=null)&&(I.displayText().length()>0)))
							{
								if((--myOccurrance)<=0)
									return I;
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
						&&(filter.passesFilter(I))
						&&(containsString(I.name(),srchStr)||containsString(I.Name(),srchStr))
						&&((!allFlag)||(E instanceof Ability)||((I.displayText()!=null)&&(I.displayText().length()>0))))
						{
							if((--myOccurrance)<=0)
								return I;
						}
					}
					else
					if((E!=null)
					&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
					&&((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							return E;
					}
				}

				myOccurrance=flags.occurrance - counterSlap[0];
				for (final Environmental E : list)
				{
					if(E instanceof Item)
					{
						I=(Item)E;
						if((I.container()==goodLocation)
						&&(filter.passesFilter(I))
						&&(containsString(I.displayText(),srchStr)))
						{
							if((--myOccurrance)<=0)
								return I;
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
						}
					}
				}
			}
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		counterSlap[0]+=(flags.occurrance-myOccurrance);
		return null;
	}
	
	@Override
	public Environmental fetchAvailable(Collection<? extends Environmental> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly)
	{
		if(list.isEmpty())
			return null;
		final FetchFlags flags=fetchFlags(srchStr);
		if(flags==null)
			return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		final boolean allFlag=flags.allFlag;

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
						&&(filter.passesFilter(I))
						&&(I.ID().equalsIgnoreCase(srchStr)
						   ||(I.Name().equalsIgnoreCase(srchStr))
						   ||(I.name().equalsIgnoreCase(srchStr))))
						{
							if((!allFlag)||(E instanceof Ability)||((I.displayText()!=null)&&(I.displayText().length()>0)))
							{
								if((--myOccurrance)<=0)
									return I;
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
						&&(filter.passesFilter(I))
						&&(containsString(I.name(),srchStr)||containsString(I.Name(),srchStr))
						&&((!allFlag)||(E instanceof Ability)||((I.displayText()!=null)&&(I.displayText().length()>0))))
						{
							if((--myOccurrance)<=0)
								return I;
						}
					}
					else
					if((E!=null)
					&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
					&&((!allFlag)||(E instanceof Ability)||((E.displayText()!=null)&&(E.displayText().length()>0))))
					{
						if((--myOccurrance)<=0)
							return E;
					}
				}

				myOccurrance=flags.occurrance;
				for (final Environmental E : list)
				{
					if(E instanceof Item)
					{
						I=(Item)E;
						if((I.container()==goodLocation)
						&&(filter.passesFilter(I))
						&&(containsString(I.displayText(),srchStr)))
						{
							if((--myOccurrance)<=0)
								return I;
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
						}
					}
				}
			}
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return null;
	}
}
