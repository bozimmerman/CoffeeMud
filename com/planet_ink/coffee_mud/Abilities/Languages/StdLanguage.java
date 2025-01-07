package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CommonSkill;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.CostDef;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2008-2024 Bo Zimmerman

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
public class StdLanguage extends StdAbility implements Language
{
	@Override
	public String ID()
	{
		return "StdLanguage";
	}

	private final static String	localizedName	= CMLib.lang().L("Languages");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String writtenName()
	{
		return name();
	}

	private static final String[]	triggerStrings	= I(new String[] { "SPEAK" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	protected CostDef getRawTrainingCost()
	{
		return CMProps.getLangSkillGainCost(ID());
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_LANGUAGE;
	}

	protected static final String		CANCEL_WORD	= "CANCEL";
	private static Map<String, String>	emptyHash	= new Hashtable<String, String>();
	private static List<String[]>		emptyVector	= new Vector<String[]>();
	protected boolean					spoken		= false;
	protected boolean					alwaysSpoken= false;
	private final static String			consonants	= "bcdfghjklmnpqrstvwxz";
	private final static String			vowels		= "aeiouy";

	private final static Map<String,Set<String>> allLangIDsCache = new Hashtable<String,Set<String>>();

	@Override
	public boolean beingSpoken(final String language)
	{
		return alwaysSpoken || spoken;
	}

	@Override
	public void setBeingSpoken(final String language, final boolean beingSpoken)
	{
		spoken = alwaysSpoken || beingSpoken;
	}

	@Override
	public Map<String, String> translationHash(final String language)
	{
		return emptyHash;
	}

	@Override
	public List<String[]> translationLists(final String language)
	{
		return emptyVector;
	}

	@Override
	public boolean isANaturalLanguage()
	{
		return true;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		if(newMiscText.length()>0)
		{
			alwaysSpoken = CMParms.getParmBool(newMiscText,"ALWAYS", false);
			spoken = CMParms.getParmBool(newMiscText,"SPOKEN", spoken);
		}
		super.setMiscText(newMiscText);
	}

	@Override
	public Set<String> languagesSupported()
	{
		if(!allLangIDsCache.containsKey(ID()))
			allLangIDsCache.put(ID(), new XHashSet<String>(ID()));
		return allLangIDsCache.get(ID());
	}

	@Override
	public boolean translatesLanguage(final String language, final String words)
	{
		return ID().equalsIgnoreCase(language);
	}

	@Override
	public int getProficiency(final String language)
	{
		if (ID().equalsIgnoreCase(language))
			return proficiency();
		return 0;
	}

	@Override
	public String displayText()
	{
		if(beingSpoken(ID()))
			return "(Speaking "+name()+")";
		return "";
	}

	protected String fixCase(final String like,final String make)
	{
		final StringBuffer s=new StringBuffer(make);
		char lastLike=' ';
		for(int x=0;x<make.length();x++)
		{
			if(x<like.length())
				lastLike=like.charAt(x);
			s.setCharAt(x,fixCase(lastLike,make.charAt(x)));
		}
		return s.toString();
	}

	protected char fixCase(final char like,final char make)
	{
		if(Character.isUpperCase(like))
			return Character.toUpperCase(make);
		return Character.toLowerCase(make);
	}

	@Override
	public String translate(final String language, final String word)
	{
		if(translationHash(language).containsKey(word.toUpperCase()))
			return fixCase(word,translationHash(language).get(word.toUpperCase()));
		final MOB M=CMLib.players().getPlayerAllHosts(word);
		if(M!=null)
			return word;
		final List<String[]> translationVector=translationLists(language);
		if((translationVector!=null)&&(translationVector.size()>0))
		{
			String[] choices=null;
			try
			{
				choices = translationVector.get(word.length() - 1);
			}
			catch (final Exception e)
			{
			}
			if(choices==null)
				choices=translationVector.get(translationVector.size()-1);
			if(choices.length==0)
				return word;
			return choices[CMath.abs(word.toLowerCase().hashCode()) % choices.length];
		}
		return word;
	}

	protected int numChars(final String words)
	{
		int num=0;
		final boolean[] nos=CMStrings.markMarkups(words);
		for(int i=0;i<words.length();i++)
		{
			if((!nos[i])
			&& Character.isLetter(words.charAt(i)))
				num++;
		}
		return num;
	}

	public String messChars(final String language, final String words, int numToMess)
	{
		numToMess=numToMess/2;
		if(numToMess==0)
			return words;
		final StringBuffer w=new StringBuffer(words);
		final boolean[] nos=CMStrings.markMarkups(words);
		int attempts=words.length() * 100;
		while((numToMess>0) && (--attempts>0))
		{
			final int x=CMLib.dice().roll(1,words.length(),-1);
			if(!nos[x])
			{
				final char c=words.charAt(x);
				if(Character.isLetter(c))
				{
					if(vowels.indexOf(c)>=0)
						w.setCharAt(x,fixCase(c,vowels.charAt(CMLib.dice().roll(1,vowels.length(),-1))));
					else
						w.setCharAt(x,fixCase(c,consonants.charAt(CMLib.dice().roll(1,consonants.length(),-1))));
					numToMess--;
					nos[x]=true; // prevent the same letter change twice
				}
			}
		}
		return w.toString();
	}

	@Override
	public String getVerb()
	{
		return "";
	}

	@Override
	public String getTranslationVerb()
	{
		return "";
	}

	public String scrambleAll(final String language, final String str, final int numToMess)
	{
		final StringBuffer newStr=new StringBuffer("");
		final boolean[] nos=CMStrings.markMarkups(str);
		final StringBuilder cs=new StringBuilder(str);
		int start=0;
		int end=0;
		int state=-1;
		while(start<=cs.length())
		{
			char c='\0';
			if(end>=cs.length())
				c=' ';
			else
				c=cs.charAt(end);
			switch(state)
			{
			case -1:
				if((end < cs.length()) && nos[end])
				{
					newStr.append(c);
					end++;
					start = end;
				}
				else
				if(Character.isLetter(c))
				{
					state = 0;
					end++;
				}
				else
				{
					newStr.append(c);
					end++;
					start = end;
				}
				break;
			case 0:
				if((end < cs.length()) && nos[end])
				{
					newStr.append(translate(language, cs.substring(start, end)));
					newStr.append(c);
					end++;
					start = end;
					state = -1;
				}
				else
				if(Character.isLetter(c))
					end++;
				else
				if(Character.isDigit(c))
				{
					newStr.append(str.substring(start, end + 1));
					end++;
					start = end;
					state = 1;
				}
				else
				{
					newStr.append(translate(language, cs.substring(start, end)) + c);
					end++;
					start = end;
					state = -1;
				}
				break;
			case 1:
				if((end < cs.length()) && nos[end])
				{
					newStr.append(c);
					end++;
					start = end;
					state = -1;
				}
				else
				if(Character.isLetterOrDigit(c))
				{
					newStr.append(c);
					end++;
					start = end;
				}
				else
				{
					newStr.append(c);
					end++;
					start = end;
					state = -1;
				}
				break;
			}
		}
		return newStr.toString();
	}

	protected Language getMyTranslator(final String id, final Physical P, Language winner, final String words)
	{
		if(P==null)
			return winner;
		for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A instanceof Language)
			&& ((Language)A).translatesLanguage(id, words)
			&& ((winner==null)
				||((Language)A).getProficiency(id) > winner.getProficiency(id)))
			{
				winner = (Language)A;
			}
		}
		return winner;
	}

	protected Language getAnyTranslator(final String id, final MOB mob, final String words)
	{
		Language winner = null;
		winner = getMyTranslator(id, mob, winner, words);
		if(winner == null)
			winner = getMyTranslator(id, mob.location(), winner, words);
		if(winner == null)
		{
			for(int i=0;i<mob.numItems();i++)
			{
				winner=getMyTranslator(id, mob.getItem(i), winner, words);
				if(winner != null)
					break;
			}
		}
		if((winner == null)
		&&(languagesSupported().contains(id)))
			return this;
		return winner;
	}

	protected String fixSayVerb(final String fullSay, final String verb)
	{
		if((verb.length() > 0)
		&&(fullSay != null))
		{
			final int x=fullSay.indexOf('\'');
			if(x > 0)
			{
				final String word = L("say(s)");
				final int y = fullSay.lastIndexOf(word, x);
				if((y > 0) && (y < x))
					return fullSay.substring(0,y) + verb + fullSay.substring(y+word.length());
			}
		}
		return fullSay;
	}

	protected boolean processSourceMessage(final CMMsg msg, final String str, final int numToMess)
	{
		String smsg=CMStrings.getSayFromMessage(msg.sourceMessage());
		if(smsg != null)
		{
			final String sayMessage = fixSayVerb(msg.sourceMessage(), getTranslationVerb());
			if(numToMess>0)
				smsg=messChars(ID(),smsg,numToMess);
			msg.modify(msg.source(),
					   msg.target(),
					   this,
					   msg.sourceCode(),
					   CMStrings.substituteSayInMessage(sayMessage,smsg),
					   msg.targetCode(),
					   msg.targetMessage(),
					   msg.othersCode(),
					   msg.othersMessage());
		}
		return true;
	}

	protected boolean processNonSourceMessages(final CMMsg msg, String str, final int numToMess)
	{
		str=scrambleAll(ID(),str,numToMess);
		final String targetMessage = fixSayVerb(msg.targetMessage(), getVerb());
		final String othersMessage = fixSayVerb(msg.othersMessage(), getVerb());
		msg.modify(msg.source(),
				   msg.target(),
				   this,
				   msg.sourceCode(),
				   msg.sourceMessage(),
				   msg.targetCode(),
				   CMStrings.substituteSayInMessage(targetMessage,str),
				   msg.othersCode(),
				   CMStrings.substituteSayInMessage(othersMessage,str));
		return true;
	}

	protected boolean tryLinguisticWriting(final CMMsg msg)
	{
		if(msg.target() instanceof Physical)
		{
			final Physical P = (Physical)msg.target();
			for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A instanceof Language)&&(!A.ID().equals(ID())))
				{
					msg.source().tell(L("@x1 is already written in @x2 and can not have @x3 writing added.",P.name(msg.source()),A.name(),writtenName()));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(beingSpoken(ID())))
		{
			if((msg.source()==affected)
			&&(msg.sourceMessage()!=null)
			&&(msg.tool()==null)
			&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))))
			{
				String str=CMStrings.getSayFromMessage(msg.othersMessage());
				if(str==null)
					str=CMStrings.getSayFromMessage(msg.targetMessage());
				if(str!=null)
				{
					final int numToMess=(int)Math.round(CMath.mul(numChars(str),CMath.div(100-getProficiency(ID()),100)));
					if(!processSourceMessage(msg, str, numToMess))
						return false;
					if(!processNonSourceMessages(msg, str, numToMess))
						return false;
					if(CMLib.flags().isAliveAwakeMobile((MOB)affected,true))
						helpProficiency((MOB)affected, 0);
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_WRITE)
			&&(msg.source()==affected)
			&&(msg.target() instanceof Item)
			&&(((Item)msg.target()).isReadable())
			&&(msg.targetMessage()!=null)
			&&(msg.targetMessage().length()>0))
			{
				if(!tryLinguisticWriting(msg))
					return false;
			}
			else
			if((msg.target()==affected)
			&&(msg.source()!=affected)
			&&(msg.sourceMinor()!=CMMsg.NO_EFFECT))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_ORDER:
				case CMMsg.TYP_BUY:
				case CMMsg.TYP_BID:
				case CMMsg.TYP_SELL:
				case CMMsg.TYP_LIST:
				case CMMsg.TYP_VIEW:
				case CMMsg.TYP_WITHDRAW:
				case CMMsg.TYP_DEPOSIT:
				{
					// yes, this means that a mob speaking Common to a marketing player will get failed,
					// however, remember that the LISTer language doesn't matter, only the responding (this) language.
					// also, think about muds where there is no Common (an interesting mud!)
					if((!CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.ORDER))
					&&(!CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.CMDMOBS)||(!((MOB)msg.target()).isMonster()))
					&&(!CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.CMDROOMS)||(!((MOB)msg.target()).isMonster())))
					{
						Language spokenL; // this is the language being spoken
						if(msg.tool() instanceof Language)
							spokenL=(Language)msg.tool();
						else
						if((affected instanceof MOB)
						&&(((MOB)affected).isMonster()))
							spokenL=CMLib.utensils().getLanguageSpoken(msg.source());
						else
							break;
						if(spokenL==null)
						{
							spokenL=(Language)msg.source().fetchAbility("Common");
							if(spokenL==null)
							{
								spokenL=(Language)CMClass.getAbility("Common");
								spokenL.setProficiency(100);
							}
						}

						final String heardMsg;
						if(msg.targetMinor()==CMMsg.TYP_ORDER)
							heardMsg = CMStrings.getSayFromMessage(msg.sourceMessage());
						else
							heardMsg = CMMsg.TYPE_DESCS[msg.targetMinor()];
						final Language heardL; // this is the language as heard
						if(spokenL.ID().equals(ID()))
							heardL=this;
						else
						if(affected instanceof MOB)
							heardL=getAnyTranslator(spokenL.ID(), (MOB)affected, heardMsg);
						else
							heardL=getAnyTranslator(spokenL.ID(), msg.source(), heardMsg);
						if((heardL==null)
						||((CMLib.dice().rollPercentage()*2)>(spokenL.getProficiency(spokenL.ID())+heardL.getProficiency(heardL.ID()))))
						{
							String reply=null;
							if(heardL==null)
							{
								if(msg.targetMinor()==CMMsg.TYP_ORDER)
								{
									final Object[][] cmds = CMProps.getListFileStringChoices(CMProps.ListFile.ANIMAL_ORDER_LIST);
									final String order = CMStrings.getSayFromMessage(msg.targetMessage());
									if((order != null) && (order.length()>0) && (cmds.length>0))
									{
										final String word = CMStrings.getFirstWord(order.trim()).toUpperCase();
										Object[] found=null;
										for(final Object[] cmd : cmds)
										{
											final String cmdStr = (String)cmd[0];
											if((CMStrings.getFirstWord(cmdStr).toUpperCase().startsWith(word)))
											{
												final int switchDex = cmdStr.indexOf('(');
												final String cmdMatch = (switchDex<0)?cmdStr:cmdStr.substring(0,switchDex);
												if(cmdMatch.endsWith("*") || (order.indexOf(' ')<0))
												{
													found=cmd;
													break;
												}
											}
										}
										if(found != null)
										{
											final int switchDex = found[0].toString().indexOf('(');
											if((switchDex >= 0)
											&&(found[0].toString().endsWith(")")))
											{
												msg.setTargetMessage(CMStrings.replaceSayInMessage(msg.targetMessage(),
														found[0].toString().substring(switchDex+1,found[0].toString().length()-1)));
											}
											break;
										}
									}
								}
								reply="<S-NAME> do(es) not speak "+spokenL.name()+" and would not understand <T-HIM-HER>.";
							}
							else
								reply="<T-NAME> <T-IS-ARE> having trouble understanding <T-YOUPOSS> pronunciation.";
							msg.setTargetCode(CMMsg.TYP_SPEAK);
							msg.setSourceCode(CMMsg.TYP_SPEAK);
							msg.setOthersCode(CMMsg.TYP_SPEAK);
							msg.addTrailerMsg(CMClass.getMsg((MOB)msg.target(),msg.source(),null,CMMsg.MSG_OK_VISUAL,reply));
						}
					}
					break;
				}
				default:
					break;
				}
			}
		}
		if((affected instanceof Item)
		&&(!canBeUninvoked())
		&&(msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_WASREAD)
		&&(msg.othersCode()==CMMsg.NO_EFFECT)
		&&((msg.othersMessage()==null)||(!msg.othersMessage().equals(CANCEL_WORD)))
		&&(!(affected instanceof LandTitle))
		&&(CMLib.flags().canBeSeenBy(this,msg.source()))
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0)
		&&(((Item)affected).isReadable())
		)
		{
			String str;
			if(msg.targetMessage().startsWith("::")
			&&(msg.targetMessage().indexOf("::",2)>0))
				str=msg.targetMessage().substring(msg.targetMessage().indexOf("::",2)+2);
			else
				str=msg.targetMessage();
			int numToMess=numChars(str);
			if(numToMess>0)
			{
				final Language L=(Language)msg.source().fetchEffect(ID());
				if(L!=null)
					numToMess=(int)Math.round(CMath.mul(numChars(str),CMath.div(100-L.getProficiency(ID()),100)));
				final String original=messChars(ID(),str,numToMess);
				str=scrambleAll(ID(),str,numToMess);
				msg.setSourceMessage(L("It says '@x1'.  ",str.trim()));
				if((L!=null)&&(!original.equals(str)))
				{
					msg.setSourceMessage(msg.sourceMessage()+(L("\n\rIt says '@x1' (translated from @x2).",original,L.writtenName())));
					msg.setTargetMessage(original);
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean canBeLearnedBy(final MOB teacher, final MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null)
			return true;
		final AbilityComponents.AbilityLimits remainders = CMLib.ableComponents().getSpecialSkillRemainder(student, this);
		if(remainders.languageSkills()<=0)
		{
			if(teacher != null)
				teacher.tell(L("@x1 can not learn any more languages.",student.name(teacher)));
			student.tell(L("You have learned the maximum @x1 languages, and may not learn any more.",""+remainders.maxLanguageSkills()));
			return false;
		}
		return true;
	}

	@Override
	public void teach(final MOB teacher, final MOB student)
	{
		super.teach(teacher, student);
		if((student!=null)&&(student.fetchAbility(ID())!=null))
		{
			final AbilityComponents.AbilityLimits remainders = CMLib.ableComponents().getSpecialSkillRemainder(student, this);
			if(remainders.languageSkills()<=0)
				student.tell(L("@x1 may not learn any more languages.",student.name()));
			else
			if(remainders.languageSkills()<=Integer.MAX_VALUE/2)
				student.tell(L("@x1 may learn @x2 more languages.",student.name(),""+remainders.languageSkills()));
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!auto)
		{
			boolean alreadySpeaking=false;
			boolean found=false;
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A instanceof Language))
				{
					if(mob.isMonster())
						A.setProficiency(100);
					if(A.ID().equals(ID()))
					{
						found=true;
						alreadySpeaking = ((Language)A).beingSpoken(A.ID());
						((Language)A).setBeingSpoken(ID(),true);
					}
					else
						((Language)A).setBeingSpoken(ID(),false);
				}
			}
			isAnAutoEffect=false;
			if(found)
			{
				if(alreadySpeaking)
					mob.tell(L("You were already speaking @x1.",name()));
				else
					mob.tell(L("You are now speaking @x1.",name()));
			}
			else
				mob.tell(L("You are now speaking Common."));
		}
		else
			setBeingSpoken(ID(),true);
		return true;
	}

	protected boolean translateOthersMessage(final CMMsg msg, final String sourceWords)
	{
		if((msg.othersMessage()!=null)&&(msg.othersMessage().indexOf('\'')>0))
		{
			String otherMes=fixSayVerb(msg.othersMessage(), getTranslationVerb());
			if(msg.target()!=null)
				otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,null,msg.othersCode(),
					L("@x1 (translated from @x2)",CMStrings.substituteSayInMessage(otherMes,sourceWords),name()),CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}

	protected boolean translateTargetMessage(final CMMsg msg, final String sourceWords)
	{
		if(msg.amITarget(affected)&&(msg.targetMessage()!=null))
		{
			String targetMes=fixSayVerb(msg.targetMessage(), getTranslationVerb());
			if(msg.target()!=null)
				targetMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.source(),msg.target(),msg.tool(),targetMes,false);
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,null,msg.targetCode(),
					L("@x1 (translated from @x2)",CMStrings.substituteSayInMessage(targetMes,sourceWords),name()),CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}

	protected boolean translateChannelMessage(final CMMsg msg, final String sourceWords)
	{
		if(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)&&(msg.othersMessage()!=null))
		{
			final ChannelsLibrary.CMChannel C = CMLib.channels().getChannelFromMsg(msg);
			if((C==null)||(!C.flags().contains(ChannelsLibrary.ChannelFlag.NOLANGUAGE)))
			{
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,msg.othersCode(),
						L("@x1 (translated from @x2)",CMStrings.substituteSayInMessage(msg.othersMessage(),sourceWords),name())));
				return true;
			}
		}
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected instanceof MOB)
		&&(!msg.amISource((MOB)affected))
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
		&&(msg.sourceMessage()!=null)
		&&(msg.tool() instanceof Language)
		&&(msg.tool().ID().equals(ID())))
		{
			String str=CMStrings.getSayFromMessage(msg.sourceMessage());
			if(str!=null)
			{
				final int numToMess=(int)Math.round(CMath.mul(numChars(str),CMath.div(100-getProficiency(ID()),100)));
				if(numToMess>0)
					str=messChars(ID(),str,numToMess);
				if(!translateChannelMessage(msg,str))
				{
					if(!translateTargetMessage(msg,str))
						translateOthersMessage(msg, str);
				}
			}
		}
		else
		if((affected instanceof MOB)
		&&(msg.source()==affected)
		&&(beingSpoken(ID()))
		&&(msg.target() instanceof Item)
		&&(msg.sourceMinor()==CMMsg.TYP_WRITE)
		&&(((Item)msg.target()).isReadable())
		&&(msg.targetMessage()!=null)
		&&((msg.targetMessage().length()>0)||(msg.target().ID().endsWith("Book"))))
		{
			final Item I = (Item)msg.target();
			Ability L=null;
			for(int i=I.numEffects()-1;i>=0;i--) // reverse enumeration
			{
				L=I.fetchEffect(i);
				if(L instanceof Language)
				{
					I.delEffect(L);
					break;
				}
			}
			I.addNonUninvokableEffect((Ability)this.copyOf());
		}
	}
}
