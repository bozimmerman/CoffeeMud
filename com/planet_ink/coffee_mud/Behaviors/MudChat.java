package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.exceptions.ScriptParseException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.MPContext;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2001-2025 Bo Zimmerman

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

	/**
	 * Enum for different match types
	 * @author Bo Zimmerman
	 *
	 */
	protected enum ChatMatchType
	{
		SAY,
		EMOTE,
		TEMOTE,
		RANDOM
	}

	/**
	 * Enum for connectors between matches/expressions
	 * @author Bo Zimmerman
	 *
	 */
	protected static enum ChatExpConn
	{
		END,
		AND,
		OR,
		ANDNOT
	}

	/**
	 * Flag for how to compare a match string with the user string
	 *
	 * @author Bo Zimmerman
	 *
	 */
	protected static enum ChatMatchFlag
	{
		EXACT,
		TOP,
		ZAPPER,
		INSTR
	}

	/**
	 * A specific string match, with modifiers
	 * @author Bo Zimmerman
	 *
	 */
	protected static class ChatMatch
	{
		public ChatMatch()
		{
		}
		public ChatMatchFlag flag = ChatMatchFlag.INSTR;
		public String str=null;
	}

	/**
	 * A match expression, composed of one or more matches and expressions
	 *
	 * @author Bo Zimmerman
	 *
	 */
	protected static class ChatExpression
	{
		public ChatExpression()
		{
		}
		public ChatMatchType	type	= null;
		public List<Pair<Object,ChatExpConn>> exp = new LinkedList<Pair<Object,ChatExpConn>>();
	}

	/**
	 * A response object representing something the chatty-one will
	 * definitely be saying soon.
	 * @author Bo Zimmerman
	 */
	protected static class ChattyResponse
	{
		public int				delay;
		public boolean			combatFlag;
		public List<String>		parsedCommand;

		public ChattyResponse(final List<String> cmd, final int responseDelay, final boolean combatFlag)
		{
			parsedCommand = cmd;
			delay = responseDelay;
			this.combatFlag=combatFlag;
		}
	}

	/**
	 * A test response is a possible response to an environmental event, such as
	 * someone speaking or acting.  It is only one possible response to one possible
	 * event, and is weighed against its neighbors for whether it is chosen.
	 * @author Bo Zimmerman
	 */
	protected static class ChattyTestResponse
	{
		public String[] responses;
		public int weight;
		public boolean combatFlag;
		public ChattyTestResponse(final String resp, final boolean combatFlag)
		{
			weight=CMath.s_int(""+resp.charAt(0));
			this.combatFlag=combatFlag;
			responses=CMParms.parseSquiggleDelimited(resp.substring(1),true).toArray(new String[0]);
		}
	}

	/**
	 * A chatty entry embodies a test for a particular environmental event, such as
	 * someone speaking or acting, and all possible responses to that event.
	 * @author Bo Zimmerman
	 */
	protected static class ChattyEntry
	{
		public ChatExpression		expression;
		public ChattyTestResponse[]	responses;
		public boolean				combatEntry	= false;

		public ChattyEntry(final ChatExpression expression, final boolean combat)
		{
			combatEntry = combat;
			this.expression = expression;
		}
	}

	/**
	 * A chatty group is a collection of particular environmental event tests, and
	 * their possible responses.  It completely embodies a particular "chat behavior"
	 * for a particular kind of chatty mob.
	 * @author Bo Zimmerman
	 */
	protected static class ChattyGroup implements Cloneable
	{
		public String[]							groupNames;
		public MaskingLibrary.CompiledZMask[]	groupMasks;
		public ChattyEntry[]					entries	= null;
		public ChattyEntry[]					tickies	= null;

		public ChattyGroup(final String[] names, final MaskingLibrary.CompiledZMask[] masks)
		{
			groupNames = names;
			groupMasks = masks;
		}

		@Override
		public ChattyGroup clone()
		{
			try
			{
				return (ChattyGroup) super.clone();
			}
			catch (final Exception e)
			{
				return this;
			}
		}
	}

	@Override
	public String accountForYourself()
	{
		if(lastThingSaid!=null)
			return "chattiness \""+lastThingSaid+"\"";
		else
			return "chattiness";
	}

	protected void addChatEntries(final String addParms)
	{
		final List<String> V=CMParms.parseSemicolons(addParms,false);
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

	@Override
	public void setParms(final String newParms)
	{
		if(newParms.startsWith("+"))
			addChatEntries(newParms.substring(1));
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

	protected static ChattyGroup newChattyGroup(final String name)
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
						masks.add(CMLib.masking().getPreCompiledMask(s));
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

	protected static synchronized ChattyGroup[] getChatGroups(final String parms)
	{
		unprotectedChatGroupLoad("chat.dat");
		return unprotectedChatGroupLoad(parms);
	}

	protected static ChattyGroup[] unprotectedChatGroupLoad(final String parms)
	{
		ChattyGroup[] rsc=null;
		String filename="chat.dat";
		final int x=parms.indexOf('=');
		if(x>0)
			filename=parms.substring(0,x);
		rsc=(ChattyGroup[])Resources.getResource("MUDCHAT GROUPS-"+filename.toLowerCase());
		if(rsc!=null)
			return rsc;
		synchronized(CMClass.getSync(("MUDCHAT GROUPS-"+filename.toLowerCase())))
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

	protected static ChattyGroup[] parseChatData(final StringBuffer rsc)
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
				final char c=str.charAt(0);
				switch(c)
				{
				case '"':
					Log.sysOut("MudChat",str.substring(1));
					break;
				case '#':
					// nothing happened, move along
					break;
				case '*':
					if((str.length()==1)||("([{<".indexOf(str.charAt(1))<0))
						break;
					str=str.substring(1);
				//$FALL-THROUGH$
				case '(':
				case '[':
				case '{':
				case '<':
					if(currentChatEntry!=null)
						currentChatEntry.responses = currentChatEntryResponses.toArray(new ChattyTestResponse[0]);
					currentChatEntryResponses.clear();
					try
					{
						final ChatExpression expression = parseExpression(str);
						currentChatEntry=new ChattyEntry(expression,c=='*');
						if(expression.type==ChatMatchType.RANDOM)
							tickyChatEntries.add(currentChatEntry);
						else
							currentChatEntries.add(currentChatEntry);
					}
					catch (final CMException e)
					{
						Log.debugOut("MudChat",e.getMessage());
						currentChatEntry=null;
					}
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
						currentChatEntryResponses.add(new ChattyTestResponse(str,currentChatEntry.combatEntry));
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

	protected static ChattyGroup[] loadChatData(final String resourceName)
	{
		final CMFile[] fileList = CMFile.getExistingExtendedFiles(Resources.makeFileResourceName(resourceName), null, 0);
		if(fileList.length>0)
		{
			final ArrayList<ChattyGroup> chatGroups = new ArrayList<ChattyGroup>();
			for(final CMFile F : fileList)
				chatGroups.addAll(Arrays.asList(parseChatData(F.text())));
			return chatGroups.toArray(new ChattyGroup[chatGroups.size()]);
		}
		else
		{
			Log.errOut("MudChat","Unable to load "+Resources.makeFileResourceName("behavior/"+resourceName)+" or "+Resources.makeFileResourceName(resourceName));
			return new ChattyGroup[0];
		}
	}

	public static String nextLine(final StringBuffer tsc)
	{
		String ret=null;
		int sr=-1;
		int se=-1;
		if((tsc!=null)&&(tsc.length()>0))
		{
			sr=-1;
			se=-1;
			for(int i=0;i<tsc.length()-1;i++)
			{
				if((tsc.charAt(i)=='\n')||(tsc.charAt(i)=='\r'))
				{
					sr=i;
					while((i<tsc.length())
					&&((tsc.charAt(i)=='\n')||(tsc.charAt(i)=='\r')))
					{
						i++;
						se=i;
					}
					break;
				}
			}
			if(sr<0)
			{
				tsc.setLength(0);
				ret="";
			}
			else
			{
				ret=tsc.substring(0,sr).trim();
				tsc.delete(0,se);
			}
		}
		return ret;

	}

	protected static ChattyGroup matchChatGroup(final MOB meM, String myName, final ChattyGroup[] chatGroups)
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

	protected ChattyGroup getMyBaseChatGroup(final MOB forMe, final ChattyGroup[] chatGroups)
	{
		if((myChatGroup!=null)&&(myOldName.equals(forMe.Name())))
			return myChatGroup;
		myOldName=forMe.Name();
		ChattyGroup matchedCG=null;
		final String parms=getParms();
		if(parms.length()>0)
		{
			String plusParms=null;
			String basicParms = parms;
			final int plusParmsX = parms.indexOf("+");
			if(plusParmsX>=0)
			{
				basicParms=parms.substring(0,plusParmsX);
				plusParms=parms.substring(plusParmsX+1);
			}
			final int x=basicParms.indexOf('=');
			if(x<0)
				matchedCG=matchChatGroup(forMe,getParms(),chatGroups);
			else
				matchedCG=matchChatGroup(forMe,getParms().substring(x+1).trim(),chatGroups);
			if(plusParms != null)
				this.addChatEntries(plusParms);
		}
		if(matchedCG!=null)
			return matchedCG;
		matchedCG=matchChatGroup(forMe,CMLib.english().removeArticleLead(CMStrings.removeColors(myOldName.toUpperCase())),chatGroups);
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

	protected ChattyGroup getMyChatGroup(final MOB forMe, final ChattyGroup[] chatGroups)
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

	protected void queResponse(final List<Pair<ChattyTestResponse,String>> responses, final MOB source, final MOB target)
	{
		int total=0;
		for(final Pair<ChattyTestResponse,String> c : responses)
			total+=c.first.weight;
		if(total == 0)
			return;
		ChattyTestResponse selection=null;
		String rest=null;
		int select=CMLib.dice().roll(1,total,0);
		for(final Pair<ChattyTestResponse,String> c : responses)
		{
			select-=c.first.weight;
			if(select<=0)
			{
				selection=c.first;
				rest=c.second;
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
					finalCommand = finalCommand.substring(1).trim();
					if(source!=null)
					{
						finalCommand=CMStrings.replaceAll(finalCommand," her "," "+source.charStats().hisher()+" ");
						finalCommand=CMStrings.replaceAll(finalCommand," his "," "+source.charStats().hisher()+" ");
					}
					finalCommand = "emote "+finalCommand;
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
					finalCommand = scriptEngine.varify(new MPContext(source, source, source, target, null, null, "", null), finalCommand);
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
					responseQue.add(new ChattyResponse(V,RESPONSE_DELAY,selection.combatFlag));
			}
		}
	}

	protected boolean isExpressionStart(final String possExpression)
	{
		if(possExpression==null)
			return false;
		String pexp=possExpression.trim();
		if(pexp.startsWith("*"))
			pexp=pexp.substring(1).trim();
		if(pexp.length()==0)
			return false;
		return "([<{".indexOf(pexp.charAt(0))>=0;
	}

	protected static Pair<ChatMatchType, Character> getTypeAndCloser(final char openChar)
	{
		switch(openChar)
		{
		case '(':
			return new Pair<ChatMatchType, Character>(ChatMatchType.SAY, Character.valueOf(')'));
		case '[':
			return new Pair<ChatMatchType, Character>(ChatMatchType.TEMOTE, Character.valueOf(']'));
		case '{':
			return new Pair<ChatMatchType, Character>(ChatMatchType.EMOTE, Character.valueOf('}'));
		case '<':
			return new Pair<ChatMatchType, Character>(ChatMatchType.RANDOM, Character.valueOf('>'));
		}
		return null;
	}

	private enum MatchState
	{
		INSIDE_PAREN,
		INSIDE_EXP,
		POST_CONN,
		POST_PAREN
	}


	protected static ChatExpression parseExpression(String expression) throws CMException
	{
		if(expression == null)
			return null;
		expression=expression.trim();
		if(expression.length()==0)
			return null;
		final ChatExpression top = new ChatExpression();
		final char openChar=expression.charAt(0);
		Pair<ChatMatchType,Character> mtype = getTypeAndCloser(openChar);
		if(mtype==null)
			return null;
		top.type=mtype.first;
		final Stack<Character> closeStack = new Stack<Character>();
		closeStack.push(mtype.second);
		final Stack<ChatExpression> stack=new Stack<ChatExpression>();
		stack.push(top);
		final StringBuilder str=new StringBuilder("");
		ChatMatch match=new ChatMatch();
		MatchState state=MatchState.INSIDE_PAREN;
		for(int i=1;i<=expression.length();i++)
		{
			final char c=(i==expression.length())?'\0':expression.charAt(i);
			switch(c)
			{
			case ')': case ']': case '}': case '>':
				if(closeStack.size()==0)
					throw new CMException("Parse error at "+i+": "+c+" not expected w/o open.");
				if(c != closeStack.peek().charValue())
				{
					if(state==MatchState.POST_PAREN)
						throw new CMException("Parse error at "+i+": "+c+" not expected w/o connector.");
					state=MatchState.INSIDE_EXP;
					str.append(c);
					break;
				}
				closeStack.pop();
			//$FALL-THROUGH$
			case '\0':
			case '&': case '|': case '~':
			{
				if(stack.size()==0)
				{
					if(c!='\0')
						throw new CMException("Parse error at "+i+": "+c+" not expected w/o connector.");
					else
						break;
				}
				final ChatExpression cur=stack.peek();
				final Pair<Object,ChatExpConn> pair;
				if(state!=MatchState.POST_PAREN)
				{
					match.str=str.toString().toLowerCase().trim();
					pair=new Pair<Object,ChatExpConn>(match,ChatExpConn.END);
					cur.exp.add(pair);
				}
				else
				if(cur.exp.size()>0)
					pair=cur.exp.get(cur.exp.size()-1);
				else
					throw new CMException("Parse error at "+i+": "+c+" encountered in post-paren w/o sibling.");
				str.setLength(0);
				switch(c)
				{
				case '&':
					pair.second=ChatExpConn.AND;
					state=MatchState.POST_CONN;
					break;
				case '|':
					pair.second=ChatExpConn.OR;
					state=MatchState.POST_CONN;
					break;
				case '~':
					pair.second=ChatExpConn.ANDNOT;
					state=MatchState.POST_CONN;
					break;
				case '\0':
					break;
				default:
					state=MatchState.POST_PAREN;
					if(stack.size()>0)
						stack.pop();
					else
						throw new CMException("Parse error at "+i+": "+c+" encountered with empty stack.");
					break;
				}
				match=new ChatMatch();
				break;
			}
			case '[': case '{': case '<':
				if(c != openChar)
				{
					if(state==MatchState.POST_PAREN)
						throw new CMException("Parse error at "+i+": "+c+" not expected w/o connector.");
					state=MatchState.INSIDE_EXP;
					str.append(c);
					break;
				}
				//$FALL-THROUGH$
			case '(':
				if((state==MatchState.INSIDE_PAREN)
				||(state==MatchState.POST_CONN))
				{
					final ChatExpression cur=stack.peek();
					mtype=getTypeAndCloser(c);
					final ChatExpression next=new ChatExpression();
					cur.exp.add(new Pair<Object,ChatExpConn>(next,ChatExpConn.END));
					next.type=mtype.first;
					closeStack.push(mtype.second);
					stack.push(next);
					str.setLength(0);
				}
				else
				if(state==MatchState.INSIDE_EXP)
					str.append(c);
				else
					throw new CMException("Parse error at "+i+": "+c+" not expected w/o connector.");
				break;
			case '^':
			case '/':
			case '=':
				if((state==MatchState.INSIDE_PAREN)
				||(state==MatchState.POST_CONN))
				{
					state=MatchState.INSIDE_EXP;
					switch(c)
					{
					case '^':
						match.flag=ChatMatchFlag.TOP;
						break;
					case '=':
						match.flag=ChatMatchFlag.EXACT;
						break;
					case '/':
						match.flag=ChatMatchFlag.ZAPPER;
						break;
					}
					str.setLength(0);
				}
				else
				if(state==MatchState.INSIDE_EXP)
					str.append(c);
				else
					throw new CMException("Parse error at "+i+": "+c+" not expected w/o connector.");
				break;
			default:
				if(!Character.isWhitespace(c))
				{
					if(state==MatchState.POST_PAREN)
						throw new CMException("Parse error at "+i+": "+c+" not expected w/o connector.");
					state=MatchState.INSIDE_EXP;
				}
				str.append(c);
				break;
			}
		}
		return top;
	}

	protected boolean match(final MOB speaker, final ChatMatch match, final String message, final String[] rest)
	{
		switch(match.flag)
		{
		case EXACT:
		{
			if(match.str.equals(message))
			{
				rest[0]="";
				return true;
			}
			return false;
		}
		case INSTR:
		{
			final int x=message.indexOf(match.str);
			if(x<0)
				return false;
			if(((x==0)||(Character.isWhitespace(message.charAt(x-1))))
			&&(((x+match.str.length())==message.length())||(Character.isWhitespace(message.charAt(x+match.str.length())))))
			{
				rest[0]=message.substring(x+match.str.length());
				return true;
			}
			return false;
		}
		case TOP:
		{
			if(message.startsWith(match.str)
			&&((message.length()==match.str.length())||(Character.isWhitespace(message.charAt(match.str.length())))))
			{
				rest[0]=message.substring(match.str.length());
				return true;
			}
			return false;
		}
		case ZAPPER:
			return CMLib.masking().maskCheck(match.str,speaker,false);
		}
		return false;
	}

	protected boolean match(final MOB speaker, final ChatExpression expression, final int val)
	{
		boolean rollingTruth=true;
		final ChatExpConn conn=ChatExpConn.AND;
		for(final Pair<Object,ChatExpConn> p : expression.exp)
		{
			boolean thisTruth;
			if(p.first instanceof ChatExpression)
				thisTruth=match(speaker,(ChatExpression)p.first,val);
			else
			if(p.first instanceof ChatMatch)
			{
				final ChatMatch cm=(ChatMatch)p.first;
				if(cm.flag==ChatMatchFlag.ZAPPER)
					thisTruth=match(speaker,(ChatMatch)p.first,"",new String[1]);
				else
					thisTruth=CMath.s_int(cm.str.trim())<=val;
			}
			else
				continue;
			switch(conn)
			{
			case AND:
				rollingTruth = rollingTruth && thisTruth;
				if(!rollingTruth)
					return false;
				break;
			case ANDNOT:
				rollingTruth = rollingTruth && !thisTruth;
				if(!rollingTruth)
					return false;
				break;
			case END:
				return rollingTruth;
			case OR:
				rollingTruth = rollingTruth || thisTruth;
				break;
			}
		}
		return rollingTruth;
	}


	protected boolean match(final MOB speaker, final ChatExpression expression, final String lowerStrNoPunc, final String[] rest)
	{
		boolean rollingTruth=true;
		ChatExpConn conn = null;
		ChatExpConn nconn = null;
		for(final Pair<Object,ChatExpConn> p : expression.exp)
		{
			boolean thisTruth;
			if(p.first instanceof ChatExpression)
			{
				thisTruth=match(speaker,(ChatExpression)p.first,lowerStrNoPunc,rest);
				nconn=p.second;
			}
			else
			if(p.first instanceof ChatMatch)
			{
				thisTruth=match(speaker,(ChatMatch)p.first,lowerStrNoPunc,rest);
				nconn=p.second;
			}
			else
				continue;
			if(conn == null)
				rollingTruth=thisTruth;
			else
			switch(conn)
			{
			case AND:
				rollingTruth = rollingTruth && thisTruth;
				if(!rollingTruth)
					return false;
				break;
			case ANDNOT:
				rollingTruth = rollingTruth && !thisTruth;
				if(!rollingTruth)
					return false;
				break;
			case END:
				return rollingTruth;
			case OR:
				rollingTruth = rollingTruth || thisTruth;
				break;
			}
			conn=nconn;
		}
		return rollingTruth;
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting,msg);

		if((!canActAtAll(affecting))
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.MUDCHAT)))
			return;
		final MOB srcMob=msg.source();
		final MOB meMob=(MOB)affecting;
		if((msg.source()==meMob)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.othersMessage()!=null))
			lastThingSaid=CMStrings.getSayFromMessage(msg.othersMessage());
		else
		if((CMLib.flags().canBeHeardSpeakingBy(srcMob,meMob))
		&&(CMLib.flags().canBeSeenBy(srcMob,meMob))
		&&(CMLib.flags().canBeSeenBy(meMob,srcMob)))
		{
			List<Pair<ChattyTestResponse,String>> myResponses=null;
			myChatGroup=getMyChatGroup(meMob,getChatGroups(getParms()));
			final String rest[]=new String[1];
			final boolean combat=((meMob.isInCombat()))||(srcMob.isInCombat());

			String str;
			if((msg.targetMinor()==CMMsg.TYP_SPEAK)
			&&(!srcMob.isMonster())
			&&(msg.amITarget(meMob)
			   ||((msg.target()==null)
				  &&(srcMob.location()==meMob.location())
				  &&(talkDown<=0)
				  &&(srcMob.location().numPCInhabitants()<3)))
			&&(CMLib.flags().canBeHeardSpeakingBy(srcMob,meMob))
			&&(myChatGroup!=null)
			&&(lastReactedTo!=msg.source())
			&&(msg.sourceMessage()!=null)
			&&(msg.targetMessage()!=null)
			&&((str=CMStrings.getSayFromMessage(msg.sourceMessage()))!=null))
			{
				str=CMLib.english().stripEnglishPunctuation(str).toLowerCase().trim();
				for(final ChattyEntry entry : myChatGroup.entries)
				{
					final ChatExpression expression=entry.expression;
					if((expression.type==ChatMatchType.SAY)
					&&(entry.combatEntry==combat))
					{
						if(match(srcMob,expression,str,rest))
						{
							myResponses=new ArrayList<Pair<ChattyTestResponse,String>>();
							for(final ChattyTestResponse c : entry.responses)
								myResponses.add(new Pair<ChattyTestResponse,String>(c,rest[0]));
							break;
						}
					}
				}
			}
			else // dont interrupt another mob
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(srcMob.isMonster())  // this is another mob (not me) talking
			&&(CMLib.flags().canBeHeardSpeakingBy(srcMob,meMob))
			&&(CMLib.flags().canBeSeenBy(srcMob,meMob)))
			   talkDown=TALK_WAIT_DELAY;
			else // dont parse unless we are done waiting
			if((CMLib.flags().canBeHeardMovingBy(srcMob,meMob))
			&&(CMLib.flags().canBeSeenBy(srcMob,meMob))
			&&(CMLib.flags().canBeSeenBy(meMob,srcMob))
			&&(!srcMob.isMonster())
			&&(talkDown<=0)
			&&(lastReactedTo!=msg.source())
			&&(myChatGroup!=null))
			{
				str=null;
				ChatMatchType matchType = ChatMatchType.EMOTE;
				if((msg.amITarget(meMob)
				&&(msg.targetMessage()!=null)))
				{
					str=CMLib.english().stripEnglishPunctuation(msg.targetMessage().toLowerCase()).trim();
					matchType = ChatMatchType.TEMOTE;
				}
				else
				if(msg.othersMessage()!=null)
				{
					matchType = ChatMatchType.EMOTE;
					str=CMLib.english().stripEnglishPunctuation(msg.othersMessage().toLowerCase()).trim();
				}
				if(str!=null)
				{
					for(final ChattyEntry entry : myChatGroup.entries)
					{
						final ChatExpression expression=entry.expression;
						if((expression.type==matchType)
						&&(entry.combatEntry==combat))
						{
							if(match(srcMob,expression,str,rest))
							{
								myResponses=new ArrayList<Pair<ChattyTestResponse,String>>();
								for(final ChattyTestResponse c : entry.responses)
									myResponses.add(new Pair<ChattyTestResponse,String>(c,rest[0]));
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
				queResponse(myResponses,meMob,srcMob);
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
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
				List<Pair<ChattyTestResponse,String>> myResponses=null;
				for(final ChattyEntry entry : myChatGroup.tickies)
				{
					if((entry.combatEntry==combat)
					&&(this.match((MOB)ticking, entry.expression, CMLib.dice().rollPercentage())))
					{
						myResponses=new ArrayList<Pair<ChattyTestResponse,String>>();
						for(final ChattyTestResponse c : entry.responses)
							myResponses.add(new Pair<ChattyTestResponse,String>(c,""));
					}
				}
				if(myResponses!=null)
				{
					queResponse(myResponses,(MOB)ticking,(MOB)ticking);
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
						if(R.combatFlag == ((MOB)ticking).isInCombat())
						{
							((MOB)ticking).doCommand(R.parsedCommand,MUDCmdProcessor.METAFLAG_FORCED);
							lastReactedTo=null;
							// you've done one, so get out before doing another!
							break;
						}
					}
				}
			}
		}
		return true;
	}
}
