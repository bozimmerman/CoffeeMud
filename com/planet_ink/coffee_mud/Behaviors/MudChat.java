package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.ScriptParseException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2001-2018 Bo Zimmerman

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

public class MudChat extends StdBehavior implements ChattyBehavior
{
	@Override
	public String ID()
	{
		return "MudChat";
	}

	//----------------------------------------------
	// format: first group is general mob (no other
	// fit found).  All groups are chat groups.
	// each chat group includes a string describing
	// qualifying mobs followed by one or more chat
	// collections.
	protected ChattyGroup	myChatGroup			= null;
	protected String		myOldName			= "";
	protected ChattyEntry[]	addedChatEntries	= new ChattyEntry[0];
	// chat collection: first string is the pattern
	// match string
	// following strings are the proposed responses.
	//----------------------------------------------

	protected MOB		lastReactedTo		= null;
	protected MOB		lastRespondedTo		= null;
	protected String	lastThingSaid		= null;
	protected int		tickDown			= 3;
	protected int		talkDown			= 0;
	// responseQue is a qued set of commands to
	// run through the standard command processor,
	// on tick or more.
	protected SLinkedList<ChattyResponse>	responseQue	= new SLinkedList<ChattyResponse>();
	protected ScriptingEngine				scriptEngine= null;
	
	protected final static int	RESPONSE_DELAY		= 2;
	protected final static int	TALK_WAIT_DELAY		= 8;
	
	@Override
	public String accountForYourself()
	{
		if(lastThingSaid!=null)
			return "chattiness \""+lastThingSaid+"\"";
		else
			return "chattiness";
	}

	@Override
	public void setParms(String newParms)
	{
		if(newParms.startsWith("+"))
		{
			final List<String> V=CMParms.parseSemicolons(newParms.substring(1),false);
			final StringBuffer rsc=new StringBuffer("");
			for(int v=0;v<V.size();v++)
				rsc.append(V.get(v)+"\n\r");
			final ChattyGroup[] addGroups=parseChatData(rsc);
			final ArrayList<ChattyEntry> newList=new ArrayList<ChattyEntry>(addedChatEntries.length);
			for(final ChattyEntry CE : addedChatEntries)
				newList.add(CE);
			for(final ChattyGroup CG : addGroups)
			{
				for(final ChattyEntry CE : CG.entries)
					newList.add(CE);
			}
			addedChatEntries = newList.toArray(new ChattyEntry[0]);
		}
		else
		{
			super.setParms(newParms);
			addedChatEntries=new ChattyEntry[0];
		}
		responseQue=new SLinkedList<ChattyResponse>();
		myChatGroup=null;
	}

	@Override
	public String getLastThingSaid()
	{
		return lastThingSaid;
	}

	@Override
	public MOB getLastRespondedTo()
	{
		return lastRespondedTo;
	}

	protected static ChattyGroup newChattyGroup(String name)
	{
		final char[] n = name.toCharArray();
		int last=0;
		char lookFor=' ';
		final ArrayList<String> names=new ArrayList<String>();
		final ArrayList<MaskingLibrary.CompiledZMask> masks=new ArrayList<MaskingLibrary.CompiledZMask>();
		for(int i=0;i<n.length;i++)
		{
			if(n[i]==lookFor)
			{
				final String s=name.substring(last,i).trim();
				last=i;
				if(s.length()>0)
				{
					if(lookFor=='/')
						masks.add(CMLib.masking().maskCompile(s));
					else
						names.add(s.toUpperCase());
				}
				if(lookFor=='/')
					lookFor=' ';
			}
			else
			if(n[i]=='/')
			{
				lookFor='/';
				last=i;
			}
		}
		final String s=name.substring(last,name.length()).trim();
		if(s.length()>0)
		{
			if(lookFor=='/')
				masks.add(CMLib.masking().maskCompile(s));
			else
				names.add(s.toUpperCase());
		}
		if((names.size()==0)&&(masks.size()==0))
			names.add("");
		return new ChattyGroup(names.toArray(new String[0]),masks.toArray(new MaskingLibrary.CompiledZMask[0]));
	}

	protected static synchronized ChattyGroup[] getChatGroups(String parms)
	{
		unprotectedChatGroupLoad("chat.dat");
		return unprotectedChatGroupLoad(parms);
	}

	protected static ChattyGroup[] unprotectedChatGroupLoad(String parms)
	{
		ChattyGroup[] rsc=null;
		String filename="chat.dat";
		final int x=parms.indexOf('=');
		if(x>0)
			filename=parms.substring(0,x);
		rsc=(ChattyGroup[])Resources.getResource("MUDCHAT GROUPS-"+filename.toLowerCase());
		if(rsc!=null)
			return rsc;
		synchronized(("MUDCHAT GROUPS-"+filename.toLowerCase()).intern())
		{
			rsc=(ChattyGroup[])Resources.getResource("MUDCHAT GROUPS-"+filename.toLowerCase());
			if(rsc!=null)
				return rsc;
			rsc=loadChatData(filename);
			Resources.submitResource("MUDCHAT GROUPS-"+filename.toLowerCase(),rsc);
			return rsc;
		}
	}

	@Override
	public List<String> externalFiles()
	{
		final int x=parms.indexOf('=');
		if(x>0)
		{
			final Vector<String> xmlfiles=new Vector<String>();
			final String filename=parms.substring(0,x).trim();
			if(filename.length()>0)
				xmlfiles.addElement(filename.trim());
			return xmlfiles;
		}
		return null;
	}

	protected static ChattyGroup[] parseChatData(StringBuffer rsc)
	{
		final ArrayList<ChattyGroup> chatGroups = new ArrayList<ChattyGroup>();
		ChattyGroup currentChatGroup=newChattyGroup("");
		final ArrayList<ChattyEntry> currentChatEntries = new ArrayList<ChattyEntry>();
		final ArrayList<ChattyEntry> tickyChatEntries = new ArrayList<ChattyEntry>();
		ChattyEntry currentChatEntry=null;
		final ArrayList<ChattyTestResponse> currentChatEntryResponses = new ArrayList<ChattyTestResponse>();

		ChattyGroup otherChatGroup;
		chatGroups.add(currentChatGroup);
		String str=nextLine(rsc);
		while(str!=null)
		{
			if(str.length()>0)
			{
				char c=str.charAt(0);
				switch(c)
				{
				case '"':
					Log.sysOut("MudChat",str.substring(1));
					break;
				case '*':
					if((str.length()==1)||("([{<".indexOf(str.charAt(1))<0))
						break;
					c=str.charAt(1);
				//$FALL-THROUGH$
				case '(':
				case '[':
				case '{':
				case '<':
					if(currentChatEntry!=null)
						currentChatEntry.responses = currentChatEntryResponses.toArray(new ChattyTestResponse[0]);
					currentChatEntryResponses.clear();
					currentChatEntry=new ChattyEntry(str);
					if(currentChatEntry.expression.length()>0)
					{
						if(c=='<')
							tickyChatEntries.add(currentChatEntry);
						else
							currentChatEntries.add(currentChatEntry);
					}
					else
						currentChatEntry=null;
					break;
				case '>':
					if(currentChatEntry!=null)
						currentChatEntry.responses = currentChatEntryResponses.toArray(new ChattyTestResponse[0]);
					currentChatGroup.entries = currentChatEntries.toArray(new ChattyEntry[0]);
					currentChatGroup.tickies = tickyChatEntries.toArray(new ChattyEntry[0]);
					currentChatEntries.clear();
					tickyChatEntries.clear();
					currentChatGroup=newChattyGroup(str.substring(1).trim());
					if(currentChatGroup == null)
						return null;
					chatGroups.add(currentChatGroup);
					currentChatEntry=null;
					break;
				case '@':
					{
						otherChatGroup=matchChatGroup(null,str.substring(1).trim(),chatGroups.toArray(new ChattyGroup[0]));
						if(otherChatGroup==null)
							otherChatGroup=chatGroups.get(0);
						if(otherChatGroup != currentChatGroup)
						{
							for(final ChattyEntry CE : otherChatGroup.entries)
								currentChatEntries.add(CE);
							for(final ChattyEntry CE : otherChatGroup.tickies)
								tickyChatEntries.add(CE);
						}
						break;
					}
				case '%':
					{
						final StringBuffer rsc2=new StringBuffer(Resources.getFileResource(str.substring(1).trim(),true).toString());
						if (rsc2.length() < 1)
						{
							Log.sysOut("MudChat", "Error reading resource " + str.substring(1).trim());
						}
						rsc.insert(0,rsc2.toString());
						break;
					}
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
					if(currentChatEntry!=null)
						currentChatEntryResponses.add(new ChattyTestResponse(str));
					break;
				}
			}
			str=nextLine(rsc);
		}
		if(currentChatEntry!=null)
			currentChatEntry.responses = currentChatEntryResponses.toArray(new ChattyTestResponse[0]);
		currentChatGroup.entries = currentChatEntries.toArray(new ChattyEntry[0]);
		currentChatGroup.tickies = tickyChatEntries.toArray(new ChattyEntry[0]);
		currentChatEntries.clear();
		tickyChatEntries.clear();
		return chatGroups.toArray(new ChattyGroup[0]);
	}

	protected static ChattyGroup[] loadChatData(String resourceName)
	{
		CMFile //F=new CMFile(Resources.makeFileResourceName("behavior/"+resourceName),null,0);
		//if((!F.exists()) || (!F.canRead()))
			F=new CMFile(Resources.makeFileResourceName(resourceName),null,0);
		if(F.exists() && F.canRead())
		{
			final StringBuffer rsc=F.text();
			return parseChatData(rsc);
		}
		else
		{
			Log.errOut("MudChat","Unable to load "+Resources.makeFileResourceName("behavior/"+resourceName)+" or "+Resources.makeFileResourceName(resourceName));
			return new ChattyGroup[0];
		}
	}

	public static String nextLine(StringBuffer tsc)
	{
		String ret=null;
		if((tsc!=null)&&(tsc.length()>0))
		{
			int y=tsc.toString().indexOf("\n\r");
			if(y<0)
				y=tsc.toString().indexOf("\r\n");
			if(y<0)
			{
				y=tsc.toString().indexOf("\n");
				if(y<0)
					y=tsc.toString().indexOf("\r");
				if(y<0)
				{
					tsc.setLength(0);
					ret="";
				}
				else
				{
					ret=tsc.substring(0,y).trim();
					tsc.delete(0,y+1);
				}
			}
			else
			{
				ret=tsc.substring(0,y).trim();
				tsc.delete(0,y+2);
			}
		}
		return ret;

	}

	protected static ChattyGroup matchChatGroup(MOB meM, String myName, ChattyGroup[] chatGroups)
	{
		myName=myName.toUpperCase();
		if(myName.equals("DEFAULT"))
			return chatGroups[0];
		for(final ChattyGroup CG : chatGroups)
		{
			if(CG.entries!=null)
			{
				for(final String name : CG.groupNames)
				{
					if(name.equals(myName))
						return CG;
				}
				if(meM != null)
				{
					for(final MaskingLibrary.CompiledZMask mask : CG.groupMasks)
					{
						if(CMLib.masking().maskCheck(mask, meM, true))
							return CG;
					}
				}
			}
		}
		return null;
	}

	protected ChattyGroup getMyBaseChatGroup(MOB forMe, ChattyGroup[] chatGroups)
	{
		if((myChatGroup!=null)&&(myOldName.equals(forMe.Name())))
			return myChatGroup;
		myOldName=forMe.Name();
		ChattyGroup matchedCG=null;
		if(getParms().length()>0)
		{
			final int x=getParms().indexOf('=');
			if(x<0)
				matchedCG=matchChatGroup(forMe,getParms(),chatGroups);
			else
				matchedCG=matchChatGroup(forMe,getParms().substring(x+1).trim(),chatGroups);
		}
		if(matchedCG!=null)
			return matchedCG;
		matchedCG=matchChatGroup(forMe,CMLib.english().cleanArticles(CMStrings.removeColors(myOldName.toUpperCase())),chatGroups);
		if(matchedCG!=null)
			return matchedCG;
		matchedCG=matchChatGroup(forMe,forMe.charStats().raceName(),chatGroups);
		if(matchedCG!=null)
			return matchedCG;
		matchedCG=matchChatGroup(forMe,forMe.charStats().getCurrentClass().name(),chatGroups);
		if(matchedCG!=null)
			return matchedCG;
		return chatGroups[0];
	}

	protected ChattyGroup getMyChatGroup(MOB forMe, ChattyGroup[] chatGroups)
	{
		if((myChatGroup!=null)&&(myOldName.equals(forMe.Name())))
			return myChatGroup;
		ChattyGroup chatGrp=getMyBaseChatGroup(forMe,chatGroups);
		if((addedChatEntries==null)||(addedChatEntries.length==0))
			return chatGrp;
		final List<ChattyEntry> newEntries = new ArrayList<ChattyEntry>();
		newEntries.addAll(Arrays.asList(addedChatEntries));
		newEntries.addAll(Arrays.asList(chatGrp.entries));
		chatGrp=chatGrp.clone();
		chatGrp.entries = newEntries.toArray(new ChattyEntry[0]);
		return chatGrp;
	}

	protected void queResponse(ArrayList<ChattyTestResponse> responses, MOB source, MOB target, String rest)
	{
		int total=0;
		for(final ChattyTestResponse CR : responses)
			total+=CR.weight;
		if(total == 0)
			return;
		ChattyTestResponse selection=null;
		int select=CMLib.dice().roll(1,total,0);
		for(final ChattyTestResponse CR : responses)
		{
			select-=CR.weight;
			if(select<=0)
			{
				selection=CR;
				break;
			}
		}

		if(selection!=null)
		{
			for(String finalCommand : selection.responses)
			{
				if(finalCommand.trim().length()==0)
					return;
				else
				if(finalCommand.startsWith(":"))
				{
					finalCommand="emote "+finalCommand.substring(1).trim();
					if(source!=null)
						finalCommand=CMStrings.replaceAll(finalCommand," her "," "+source.charStats().hisher()+" ");
				}
				else
				if(finalCommand.startsWith("!"))
					finalCommand=finalCommand.substring(1).trim();
				else
				if(finalCommand.startsWith("\""))
					finalCommand="say \""+finalCommand.substring(1).trim()+"\"";
				else
				if(target!=null)
					finalCommand="sayto \""+target.name()+"\" "+finalCommand.trim();

				finalCommand=CMStrings.replaceAll(finalCommand,"$r",rest);
				if(target!=null)
					finalCommand=CMStrings.replaceAll(finalCommand,"$t",target.name());
				if(source!=null)
					finalCommand=CMStrings.replaceAll(finalCommand,"$n",source.name());
				if(finalCommand.indexOf("$%")>=0)
				{
					if(scriptEngine == null)
					{
						scriptEngine=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
						scriptEngine.setSavable(false);
						scriptEngine.setVarScope("*");
					}
					final Object[] tmp = new Object[ScriptingEngine.SPECIAL_NUM_OBJECTS];
					finalCommand = scriptEngine.varify(source, target, source, source, null, null, "", tmp, finalCommand);
				}
				finalCommand=CMStrings.replaceAll(finalCommand,"$$","$");
				Vector<String> V=CMParms.parse(finalCommand);
				for(final ChattyResponse R : responseQue)
				{
					if(CMParms.combine(R.parsedCommand,1).equalsIgnoreCase(finalCommand))
					{
						V=null;
						break;
					}
				}
				if(V!=null)
					responseQue.add(new ChattyResponse(V,RESPONSE_DELAY));
			}
		}
	}

	protected boolean match(MOB speaker, String expression, String message, String[] rest)
	{
		final int l=expression.length();
		if(l==0)
			return true;
		if((expression.charAt(0)=='(')
		&&(expression.charAt(l-1)==')'))
			expression=expression.substring(1,expression.length()-1).trim();

		int end=0;
		for (; ((end < expression.length()) && (("(&|~").indexOf(expression.charAt(end)) < 0)); end++)
		{/* loop */
		}
		String check=null;
		if(end<expression.length())
		{
			check=expression.substring(0,end).trim();
			expression=expression.substring(end).trim();
		}
		else
		{
			check=expression.trim();
			expression="";
		}
		boolean response=true;
		if(check.startsWith("="))
		{
			response=check.substring(1).trim().equalsIgnoreCase(message.trim());
			if(response)
				rest[0]="";
		}
		else
		if(check.startsWith("^"))
		{
			response=message.trim().startsWith(check.substring(1).trim());
			if(response)
				rest[0]=message.substring(check.substring(1).trim().length());
		}
		else
		if(check.startsWith("/"))
		{
			int expEnd=0;
			while((++expEnd)<check.length())
			{
				if(check.charAt(expEnd)=='/')
					break;
			}
			response=CMLib.masking().maskCheck(check.substring(1,expEnd).trim(),speaker,false);
		}
		else
		if(check.length()>0)
		{
			final int x=message.toUpperCase().indexOf(check.toUpperCase().trim());
			response=(x>=0);
			if(response)
				rest[0]=message.substring(x+check.length());
		}
		else
		{
			response=true;
			rest[0]=message;
		}

		if(expression.length()>0)
		{
			if(expression.startsWith("("))
			{
				int expEnd=0;
				int parenCount=1;
				while(((++expEnd)<expression.length())&&(parenCount>0))
				{
					if(expression.charAt(expEnd)=='(')
						parenCount++;
					else
					if(expression.charAt(expEnd)==')')
					{
						parenCount--;
						if(parenCount<=0)
							break;
					}
				}
				if(expEnd<expression.length()&&(parenCount<=0))
				{
					return response && match(speaker,expression.substring(1,expEnd).trim(),message,rest);
				}
				return response;
			}
			else
			if(expression.startsWith("&"))
				return response&&match(speaker,expression.substring(1).trim(),message,rest);
			else
			if(expression.startsWith("|"))
				return response||match(speaker,expression.substring(1).trim(),message,rest);
			else
			if(expression.startsWith("~"))
				return response&&(!match(speaker,expression.substring(1).trim(),message,rest));
		}
		return response;
	}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);

		if((!canActAtAll(affecting))
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.MUDCHAT)))
			return;
		final MOB mob=msg.source();
		final MOB monster=(MOB)affecting;
		if((msg.source()==monster)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.othersMessage()!=null))
			lastThingSaid=CMStrings.getSayFromMessage(msg.othersMessage());
		else
		if((!mob.isMonster())
		&&(CMLib.flags().canBeHeardSpeakingBy(mob,monster))
		&&(CMLib.flags().canBeSeenBy(mob,monster))
		&&(CMLib.flags().canBeSeenBy(monster,mob)))
		{
			ArrayList<ChattyTestResponse> myResponses=null;
			myChatGroup=getMyChatGroup(monster,getChatGroups(getParms()));
			final String rest[]=new String[1];
			final boolean combat=((monster.isInCombat()))||(mob.isInCombat());

			String str;
			if((msg.targetMinor()==CMMsg.TYP_SPEAK)
			&&(msg.amITarget(monster)
			   ||((msg.target()==null)
				  &&(mob.location()==monster.location())
				  &&(talkDown<=0)
				  &&(mob.location().numPCInhabitants()<3)))
			&&(CMLib.flags().canBeHeardSpeakingBy(mob,monster))
			&&(myChatGroup!=null)
			&&(lastReactedTo!=msg.source())
			&&(msg.sourceMessage()!=null)
			&&(msg.targetMessage()!=null)
			&&((str=CMStrings.getSayFromMessage(msg.sourceMessage()))!=null))
			{
				str=" "+CMLib.english().stripPunctuation(str)+" ";
				for(final ChattyEntry entry : myChatGroup.entries)
				{
					final String expression=entry.expression;
					if(entry.combatEntry)
					{
						if(!combat)
							continue;
					}
					else
					if(combat)
						continue;

					if((expression.charAt(0)=='(')
					&&(expression.charAt(expression.length()-1)==')'))
					{
						if(match(mob,expression.substring(1,expression.length()-1),str,rest))
						{
							myResponses=new ArrayList<ChattyTestResponse>();
							myResponses.addAll(Arrays.asList(entry.responses));
							break;
						}
					}
				}
			}
			else // dont interrupt another mob
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(mob.isMonster())  // this is another mob (not me) talking
			&&(CMLib.flags().canBeHeardSpeakingBy(mob,monster))
			&&(CMLib.flags().canBeSeenBy(mob,monster)))
			   talkDown=TALK_WAIT_DELAY;
			else // dont parse unless we are done waiting
			if((CMLib.flags().canBeHeardMovingBy(mob,monster))
			&&(CMLib.flags().canBeSeenBy(mob,monster))
			&&(CMLib.flags().canBeSeenBy(monster,mob))
			&&(talkDown<=0)
			&&(lastReactedTo!=msg.source())
			&&(myChatGroup!=null))
			{
				str=null;
				char c1='[';
				char c2=']';
				if((msg.amITarget(monster)&&(msg.targetMessage()!=null)))
					str=" "+msg.targetMessage()+" ";
				else
				if(msg.othersMessage()!=null)
				{
					c1='{';
					c2='}';
					str=" "+msg.othersMessage()+" ";
				}
				if(str!=null)
				{
					for(final ChattyEntry entry : myChatGroup.entries)
					{
						final String expression=entry.expression;
						if(entry.combatEntry)
						{
							if(!combat)
								continue;
						}
						else
						if(combat)
							continue;
						if((expression.charAt(0)==c1)
						&&(expression.charAt(expression.length()-1)==c2))
						{
							if(match(mob,expression.substring(1,expression.length()-1),str,rest))
							{
								myResponses=new ArrayList<ChattyTestResponse>();
								myResponses.addAll(Arrays.asList(entry.responses));
								break;
							}
						}
					}
				}
			}

			if(myResponses!=null)
			{
				lastReactedTo=msg.source();
				lastRespondedTo=msg.source();
				queResponse(myResponses,monster,mob,rest[0]);
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((tickID==Tickable.TICKID_MOB)
		&&(ticking instanceof MOB)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MUDCHAT)))
		{
			if(talkDown>0)
				talkDown--;

			if(tickDown>=0)
			{
				--tickDown;
				if(tickDown<0)
				{
					myChatGroup=getMyChatGroup((MOB)ticking,getChatGroups(getParms()));
				}
			}
			if((myChatGroup!=null)&&(myChatGroup.tickies.length>0) && canActAtAll(ticking))
			{
				final boolean combat = ((MOB)ticking).isInCombat();
				ArrayList<ChattyTestResponse> myResponses=null;
				for(ChattyEntry entry : myChatGroup.tickies)
				{
					if(entry.combatEntry)
					{
						if(!combat)
							continue;
					}
					else
					if(combat)
						continue;
					if(entry.expression.length()>2)
					{
						int val=CMath.s_int(entry.expression.substring(1,entry.expression.length()-1).trim());
						if((val==0)||(CMLib.dice().rollPercentage()>val))
							continue;
					}
					if(myResponses==null)
						myResponses=new ArrayList<ChattyTestResponse>();
					myResponses.addAll(Arrays.asList(entry.responses));
				}
				if(myResponses!=null)
				{
					queResponse(myResponses,(MOB)ticking,(MOB)ticking,"");
				}
			}
			if(responseQue.size()==0)
				lastReactedTo=null;
			else
			if(!canActAtAll(ticking))
			{
				responseQue.clear();
				return true;
			}
			else
			{
				for(final Iterator<ChattyResponse> riter= responseQue.descendingIterator();riter.hasNext();)
				{
					final ChattyResponse R = riter.next();
					R.delay--;
					if(R.delay<=0)
					{
						responseQue.remove(R);
						((MOB)ticking).doCommand(R.parsedCommand,MUDCmdProcessor.METAFLAG_FORCED);
						lastReactedTo=null;
						// you've done one, so get out before doing another!
						break;
					}
				}
			}
		}
		return true;
	}
}
