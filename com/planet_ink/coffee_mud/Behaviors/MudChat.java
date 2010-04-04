package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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


import java.util.*;
/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class MudChat extends StdBehavior implements ChattyBehavior
{
	public String ID(){return "MudChat";}


	//----------------------------------------------
	// format: first group is general mob (no other
	// fit found).  All groups are chat groups.
	// each chat group includes a string describing
	// qualifying mobs followed by one or more chat
	// collections.
	protected Vector myChatGroup=null;
	protected String myOldName="";
    protected Vector addedChatData=new Vector();
	// chat collection: first string is the pattern
	// match string
	// following strings are the proposed responses.
	//----------------------------------------------

	protected MOB lastReactedTo=null;
	protected MOB lastRespondedTo=null;
	protected String lastThingSaid=null;
	protected Vector responseQue=new Vector();
	protected int tickDown=3;
	protected final static int TALK_WAIT_DELAY=8;
	protected int talkDown=0;
	// responseQue is a qued set of commands to
	// run through the standard command processor,
	// on tick or more.
	protected final static int RESPONSE_DELAY=2;

    public void setParms(String newParms)
    {
        if(newParms.startsWith("+"))
        {
            Vector V=CMParms.parseSemicolons(newParms.substring(1),false);
            StringBuffer rsc=new StringBuffer("");
            for(int v=0;v<V.size();v++)
                rsc.append(((String)V.elementAt(v))+"\n\r");
            Vector chatV=parseChatData(rsc,new Vector());
            for(int v=0;v<chatV.size();v++)
            {
                Vector chatV2=(Vector)chatV.elementAt(v);
                for(int v2=1;v2<chatV2.size();v2++)
                    addedChatData.addElement(chatV2.elementAt(v2));
            }
        }
        else
        {
            super.setParms(newParms);
            addedChatData.clear();
        }
        responseQue=new Vector();
        myChatGroup=null;
    }

	public String getLastThingSaid(){ return lastThingSaid;}
	public MOB getLastRespondedTo(){return lastRespondedTo;}
    
	protected static synchronized Vector getChatGroups(String parms)
	{
		unprotectedChatGroupLoad("chat.dat");
		return unprotectedChatGroupLoad(parms);
	}

	protected static Vector unprotectedChatGroupLoad(String parms)
	{
		Vector rsc=null;
		String filename="chat.dat";
		int x=parms.indexOf("=");
		if(x>0)	filename=parms.substring(0,x);
		rsc=(Vector)Resources.getResource("MUDCHAT GROUPS-"+filename.toLowerCase());
		if(rsc!=null) return rsc;
		synchronized(("MUDCHAT GROUPS-"+filename.toLowerCase()).intern())
		{
			rsc=(Vector)Resources.getResource("MUDCHAT GROUPS-"+filename.toLowerCase());
			if(rsc!=null) return rsc;
			rsc=loadChatData(filename,new Vector());
			Resources.submitResource("MUDCHAT GROUPS-"+filename.toLowerCase(),rsc);
			return rsc;
		}
	}
	
	public Vector externalFiles()
	{
		int x=parms.indexOf("=");
		if(x>0)
		{
		    Vector xmlfiles=new Vector();
			String filename=parms.substring(0,x).trim();
			if(filename.length()>0)
			    xmlfiles.addElement(filename.trim());
			return xmlfiles;
		}
		return null;
	}

	protected static Vector parseChatData(StringBuffer rsc, Vector chatGroups)
	{
		Vector currentChatGroup=new Vector();
		Vector otherChatGroup;
		currentChatGroup.addElement("");
		chatGroups.addElement(currentChatGroup);
		String str=nextLine(rsc);
		Vector currentChatPattern=null;
		while(str!=null)
		{
			if(str.length()>0)
			switch(str.charAt(0))
			{
			case '"':
				Log.sysOut("MudChat",str.substring(1));
				break;
			case '*':
				if((str.length()==1)||("([{".indexOf(str.charAt(1))<0))
					break;
			case '(':
			case '[':
			case '{':
				if(currentChatPattern!=null)currentChatPattern.trimToSize();
				currentChatPattern=new Vector();
				currentChatPattern.addElement(str);
				if(currentChatGroup!=null)
					currentChatGroup.addElement(currentChatPattern);
				break;
			case '>':
				if(currentChatGroup!=null)currentChatGroup.trimToSize();
				currentChatGroup=new Vector();
				currentChatGroup.addElement(str.substring(1).trim());
				chatGroups.addElement(currentChatGroup);
				currentChatPattern=null;
				break;
			case '@':
				if(currentChatGroup!=null)
				{
					otherChatGroup=matchChatGroup(null,str.substring(1).trim(),chatGroups);
					if(otherChatGroup==null)
						otherChatGroup=(Vector)chatGroups.elementAt(0);
					if(otherChatGroup != currentChatGroup)
						for(int v1=1;v1<otherChatGroup.size();v1++)
							currentChatGroup.addElement(otherChatGroup.elementAt(v1));
				}
				break;
			case '%':
				{
	  				StringBuffer rsc2=new StringBuffer(Resources.getFileResource(str.substring(1).trim(),true).toString());
	  				if(rsc2.length()<1) { Log.sysOut("MudChat","Error reading resource "+str.substring(1).trim()); }
	  				rsc.insert(0,rsc2.toString());
				}
				break;
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
				if(currentChatPattern!=null)
					currentChatPattern.addElement(str);
				break;
			}
			str=nextLine(rsc);
		}
		if(currentChatGroup!=null)currentChatGroup.trimToSize();
		if(currentChatPattern!=null)currentChatPattern.trimToSize();
		for(int v=0;v<chatGroups.size();v++)
			((Vector)chatGroups.elementAt(v)).trimToSize();
		chatGroups.trimToSize();
		return chatGroups;
	}

	protected static Vector loadChatData(String resourceName, Vector chatGroups)
	{
		StringBuffer rsc=new CMFile(Resources.makeFileResourceName(resourceName),null,true).text();
		chatGroups=parseChatData(rsc,chatGroups);
		return chatGroups;
	}

	public static String nextLine(StringBuffer tsc)
	{
		String ret=null;
		if((tsc!=null)&&(tsc.length()>0))
		{
			int y=tsc.toString().indexOf("\n\r");
			if(y<0) y=tsc.toString().indexOf("\r\n");
			if(y<0)
			{
				y=tsc.toString().indexOf("\n");
				if(y<0) y=tsc.toString().indexOf("\r");
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


	protected static Vector matchChatGroup(MOB meM, String myName, Vector chatGroups)
	{
		for(int i=1;i<chatGroups.size();i++)
		{
			Vector V=(Vector)chatGroups.elementAt(i);
			if(V.size()>0)
				if(((String)V.elementAt(0)).length()>0)
				{
					String names=((String)V.elementAt(0));
                    int lastDex=0;
                    for(int s=0;s<=names.length();s++)
					{
                        if((s>=names.length())||(names.charAt(s)==' '))
                        {
                            if(names.substring(lastDex,s).equalsIgnoreCase(myName))
                                return V;
                        }
                        else
                        if(names.charAt(s)=='/')
                        {
                            int nextSlash=names.indexOf('/',s+1);
                            if(nextSlash<0) nextSlash=names.length();
                            String mask=names.substring(s+1,nextSlash);
                            if((meM!=null)&&(!CMLib.masking().maskCheck(mask,meM,true)))
                                break;
                            s=nextSlash+1;
                        }
                        else
                            continue;
                        
                        lastDex=s;
                        while(lastDex<names.length()&&Character.isWhitespace(names.charAt(lastDex)))
                        {
                            lastDex++;
                            s=lastDex-1;
                        }
					}
				}
		}
		return null;
	}

	protected Vector getMyBaseChatGroup(MOB forMe, Vector chatGroups)
	{
		if((myChatGroup!=null)&&(myOldName.equals(forMe.Name())))
			return myChatGroup;
		myOldName=forMe.Name();
		Vector V=null;
        if(getParms().length()>0)
        {
            int x=getParms().indexOf("=");
            if(x<0)
                V=matchChatGroup(forMe,getParms(),chatGroups);
            else
            if(getParms().substring(x+1).trim().length()>0)
                V=matchChatGroup(forMe,getParms().substring(x+1),chatGroups);
        }
        if(V!=null) return V;
        V=matchChatGroup(forMe,CMLib.english().cleanArticles(CMStrings.removeColors(myOldName.toUpperCase())),chatGroups);
		if(V!=null) return V;
		V=matchChatGroup(forMe,forMe.charStats().raceName(),chatGroups);
        if(V!=null) return V;
        V=matchChatGroup(forMe,forMe.charStats().getCurrentClass().name(),chatGroups);
		if(V!=null) return V;
		return (Vector)chatGroups.elementAt(0);
	}

    protected Vector getMyChatGroup(MOB forMe, Vector chatGroups)
    {
        if((myChatGroup!=null)&&(myOldName.equals(forMe.Name())))
            return myChatGroup;
        Vector chatGrp=getMyBaseChatGroup(forMe,chatGroups);
        if((addedChatData==null)||(addedChatData.size()==0)) return chatGrp;
        chatGrp=(Vector)chatGrp.clone();
        for(int v=0;v<addedChatData.size();v++)
            if(chatGrp.size()==(v+1))
                chatGrp.addElement(addedChatData.elementAt(v));
            else
                chatGrp.insertElementAt(addedChatData.elementAt(v),v+1);
        chatGrp.trimToSize();
        return chatGrp;
    }


	protected void queResponse(Vector responses, MOB source, MOB target, String rest)
	{
		int total=0;
		for(int x=1;x<responses.size();x++)
			total+=CMath.s_int(((String)responses.elementAt(x)).substring(0,1));

		String selection=null;
		int select=CMLib.dice().roll(1,total,0);
		for(int x=1;x<responses.size();x++)
		{
			select-=CMath.s_int(((String)responses.elementAt(x)).substring(0,1));
			if(select<=0)
			{
				selection=(String)responses.elementAt(x);
				break;
			}
		}

		if(selection!=null)
		{
			Vector selections=CMParms.parseSquiggleDelimited(selection.substring(1).trim(),true);
			for(int v=0;v<selections.size();v++)
			{
				String finalCommand=(String)selections.elementAt(v);
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

				if(finalCommand.indexOf("$r")>=0)
					finalCommand=CMStrings.replaceAll(finalCommand,"$r",rest);
				if((target!=null)&&(finalCommand.indexOf("$t")>=0))
					finalCommand=CMStrings.replaceAll(finalCommand,"$t",target.name());
				if((source!=null)&&(finalCommand.indexOf("$n")>=0))
					finalCommand=CMStrings.replaceAll(finalCommand,"$n",source.name());
				if(finalCommand.indexOf("$$")>=0)
					finalCommand=CMStrings.replaceAll(finalCommand,"$$","$");

				Vector V=CMParms.parse(finalCommand);
				V.insertElementAt(Integer.valueOf(RESPONSE_DELAY),0);
				for(int f=0;f<responseQue.size();f++)
				{
					Vector V1=(Vector)responseQue.elementAt(f);
					if(CMParms.combine(V1,1).equalsIgnoreCase(finalCommand))
					{
						V=null;
						break;
					}
				}
				if(V!=null)
					responseQue.addElement(V);
			}
		}
	}


	protected boolean match(MOB speaker, String expression, String message, String[] rest)
	{
		int l=expression.length();
		if(l==0) return true;
		if((expression.charAt(0)=='(')
		&&(expression.charAt(l-1)==')'))
			expression=expression.substring(1,expression.length()-1).trim();

		int end=0;
		for(;((end<expression.length())&&(("(&|~").indexOf(expression.charAt(end))<0));end++){/*loop*/}
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
                if(check.charAt(expEnd)=='/')
                    break;
            response=CMLib.masking().maskCheck(check.substring(1,expEnd).trim(),speaker,false);
        }
        else
		if(check.length()>0)
		{
			int x=message.toUpperCase().indexOf(check.toUpperCase().trim());
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
					if(expression.charAt(expEnd)=='(')
						parenCount++;
					else
					if(expression.charAt(expEnd)==')')
					{
						parenCount--;
						if(parenCount<=0) break;
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

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);

		if((!canActAtAll(affecting))
		||(CMSecurity.isDisabled("MUDCHAT")))
			return;
		MOB mob=msg.source();
		MOB monster=(MOB)affecting;
		if((msg.source()==monster)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.othersMessage()!=null))
			lastThingSaid=CMStrings.getSayFromMessage(msg.othersMessage());
		else
		if((!mob.isMonster())
		&&(CMLib.flags().canBeHeardBy(mob,monster))
		&&(CMLib.flags().canBeSeenBy(mob,monster))
		&&(CMLib.flags().canBeSeenBy(monster,mob)))
		{
			Vector myResponses=null;
			myChatGroup=getMyChatGroup(monster,getChatGroups(getParms()));
			String rest[]=new String[1];
			boolean combat=((monster.isInCombat()))||(mob.isInCombat());

			String str;
			if((msg.targetMinor()==CMMsg.TYP_SPEAK)
			&&(msg.amITarget(monster)
			   ||((msg.target()==null)
			      &&(mob.location()==monster.location())
				  &&(talkDown<=0)
				  &&(mob.location().numPCInhabitants()<3)))
			&&(CMLib.flags().canBeHeardBy(mob,monster))
			&&(myChatGroup!=null)
			&&(lastReactedTo!=msg.source())
			&&(msg.sourceMessage()!=null)
			&&(msg.targetMessage()!=null)
			&&((str=CMStrings.getSayFromMessage(msg.sourceMessage()))!=null))
			{
				str=" "+CMLib.english().stripPunctuation(str)+" ";
				int l=0;
				for(int i=1;i<myChatGroup.size();i++)
				{
					Vector possResponses=(Vector)myChatGroup.elementAt(i);
					String expression=((String)possResponses.elementAt(0)).trim();
					if(expression.startsWith("*"))
					{
						if(!combat) continue;
						expression=expression.substring(1);
					}
					else
					if(combat) continue;

					l=expression.length();
					if((l>0)
					&&(expression.charAt(0)=='(')
					&&(expression.charAt(l-1)==')'))
					{
						if(match(mob,expression.substring(1,expression.length()-1),str,rest))
						{
							myResponses=new Vector();
							myResponses.addAll(possResponses);
                            break;
						}
					}
				}
			}
			else // dont interrupt another mob
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK) 
			&&(mob.isMonster())  // this is another mob (not me) talking
			&&(CMLib.flags().canBeHeardBy(mob,monster))
			&&(CMLib.flags().canBeSeenBy(mob,monster)))
			   talkDown=TALK_WAIT_DELAY;
			else // dont parse unless we are done waiting
			if((CMLib.flags().canBeHeardBy(mob,monster))
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
					int l=0;
					for(int i=1;i<myChatGroup.size();i++)
					{
						Vector possResponses=(Vector)myChatGroup.elementAt(i);
						String expression=((String)possResponses.elementAt(0)).trim();
						if(expression.startsWith("*"))
						{
							if(!combat) continue;
							expression=expression.substring(1);
						}
						else
						if(combat) continue;
						l=expression.length();
						if((l>0)
						&&(expression.charAt(0)==c1)
						&&(expression.charAt(l-1)==c2))
						{
							if(match(mob,expression.substring(1,expression.length()-1),str,rest))
							{
								myResponses=new Vector();
								myResponses.addAll(possResponses);
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

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((tickID==Tickable.TICKID_MOB)
		&&(ticking instanceof MOB)
		&&(!CMSecurity.isDisabled("MUDCHAT")))
		{
			if(!canActAtAll(ticking))
			{
				responseQue.removeAllElements();
				return true;
			}

			if(talkDown>0) talkDown--;

			if(tickDown>=0)
			{
				--tickDown;
				if(tickDown<0)
				{
					myChatGroup=getMyChatGroup((MOB)ticking,getChatGroups(getParms()));
				}
			}
			if(responseQue.size()==0)
				lastReactedTo=null;
			else
			for(int t=responseQue.size()-1;t>=0;t--)
			{
				Vector que=(Vector)responseQue.elementAt(t);
				Integer I=(Integer)que.elementAt(0);
				I=Integer.valueOf(I.intValue()-1);
				que.setElementAt(I,0);
				if(I.intValue()<=0)
				{
					que.removeElementAt(0);
					responseQue.removeElementAt(t);
					((MOB)ticking).doCommand(que,Command.METAFLAG_FORCED);
					lastReactedTo=null;
					// you've done one, so get out before doing another!
					break;
				}
			}
		}
		return true;
	}
}
