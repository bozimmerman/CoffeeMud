package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
public class MudChat extends StdBehavior
{
	public String ID(){return "MudChat";}
	public Behavior newInstance()
	{
		return new MudChat();
	}

	//----------------------------------------------
	// format: first group is general mob (no other
	// fit found).  All groups are chat groups.
	// each chat group includes a string describing
	// qualifying mobs followed by one or more chat
	// collections.
	private Vector myChatGroup=null;
	private String myOldName="";
	// chat collection: first string is the pattern
	// match string
	// following strings are the proposed responses.
	//----------------------------------------------

	private MOB lastReactedTo=null;
	private Vector responseQue=new Vector();
	private int tickDown=3;
	private final static int TALK_WAIT_DELAY=8;
	private int talkDown=0;
	// responseQue is a qued set of commands to
	// run through the standard command processor,
	// on tick or more.
	private final static int RESPONSE_DELAY=2;

	private static synchronized Vector getChatGroups()
	{
		Vector rsc=(Vector)Resources.getResource("MUDCHAT GROUPS");
		if(rsc==null)
		{
			rsc=loadChatData("chat.dat",new Vector());
			Resources.submitResource("MUDCHAT GROUPS",rsc);
		}
		return rsc;
	}

	private static Vector loadChatData(String resourceName, Vector chatGroups)
	{
		StringBuffer rsc=Resources.getFileResource(resourceName);
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
			case '(':
			case '[':
			case '{':
				currentChatPattern=new Vector();
				currentChatPattern.addElement(str);
				if(currentChatGroup!=null)
					currentChatGroup.addElement(currentChatPattern);
				break;
			case '>':
				currentChatGroup=new Vector();
				currentChatGroup.addElement(str.substring(1).trim());
				chatGroups.addElement(currentChatGroup);
				currentChatPattern=null;
				break;
			case '@':
				otherChatGroup=matchChatGroup(str.substring(1).trim(),chatGroups);
				if(otherChatGroup==null)
					otherChatGroup=(Vector)chatGroups.elementAt(0);
				for(int v1=1;v1<otherChatGroup.size();v1++)
					currentChatGroup.addElement(otherChatGroup.elementAt(v1));
				break;
			case '%':
				{
	  				StringBuffer rsc2=new StringBuffer(Resources.getFileResource(str.substring(1).trim()).toString());
	  				if(rsc2.length()<1) { Log.sysOut("MudChat","Error reading resource "+resourceName); }
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
		return chatGroups;
	}

	public static String nextLine(StringBuffer tsc)
	{
		String ret=null;
		if((tsc!=null)&&(tsc.length()>0))
		{
			int y=tsc.toString().indexOf("\n\r");
			if(y<0)
			{
				tsc.setLength(0);
				ret="";
			}
			else
			{
				ret=tsc.substring(0,y).trim();
				tsc.delete(0,y+2);
			}
		}
		return ret;

	}


	private static Vector matchChatGroup(String myName, Vector chatGroups)
	{
		for(int i=1;i<chatGroups.size();i++)
		{
			Vector V=(Vector)chatGroups.elementAt(i);
			Vector Names=new Vector();
			if(V.size()>0)
				if(((String)V.elementAt(0)).length()>0)
				{
					String names=((String)V.elementAt(0));
					while(names.length()>0)
					{
						int y=names.indexOf(" ");
						if(y>=0)
						{
							Names.addElement(names.substring(0,y).trim().toUpperCase());
							names=names.substring(y+1);
						}
						else
						{
							Names.addElement(names.trim().toUpperCase());
							names="";
						}
					}
					for(int j=0;j<Names.size();j++)
					{
						if(((String)Names.elementAt(j)).equalsIgnoreCase(myName))
							return V;
					}
				}
		}
		return null;
	}

	private Vector getMyChatGroup(MOB forMe, Vector chatGroups)
	{
		if((myChatGroup!=null)&&(myOldName.equals(forMe.Name())))
			return myChatGroup;
		myOldName=forMe.Name();
		Vector V=matchChatGroup(myOldName.toUpperCase(),chatGroups);
		if(V!=null) return V;
		V=matchChatGroup(forMe.description(),chatGroups);
		if(V!=null) return V;
		V=matchChatGroup(forMe.displayText(),chatGroups);
		if(V!=null) return V;
		V=matchChatGroup(CMClass.className(forMe),chatGroups);
		if(V!=null) return V;
		if(this.getParms().length()>0)
			V=matchChatGroup(this.getParms(),chatGroups);
		if(V!=null) return V;
		return (Vector)chatGroups.elementAt(0);
	}

	private void queResponse(Vector responses, MOB source, MOB target, String rest)
	{
		int total=0;
		for(int x=1;x<responses.size();x++)
			total+=Util.s_int(((String)responses.elementAt(x)).substring(0,1));

		String selection=null;
		int select=Dice.roll(1,total,0);
		for(int x=1;x<responses.size();x++)
		{
			select-=Util.s_int(((String)responses.elementAt(x)).substring(0,1));
			if(select<=0)
			{
				selection=(String)responses.elementAt(x);
				break;
			}
		}

		if(selection!=null)
		{
			String finalCommand=selection.substring(1).trim();
			if(finalCommand.trim().length()==0)
				return;
			else
			if(finalCommand.startsWith(":"))
			{
				finalCommand="emote "+finalCommand.substring(1).trim();
				if(source!=null)
					finalCommand=Util.replaceAll(finalCommand," her "," "+source.charStats().hisher()+" ");
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
				finalCommand=Util.replaceAll(finalCommand,"$r",rest);
			if((target!=null)&&(finalCommand.indexOf("$t")>=0))
				finalCommand=Util.replaceAll(finalCommand,"$t",target.name());
			if((source!=null)&&(finalCommand.indexOf("$n")>=0))
				finalCommand=Util.replaceAll(finalCommand,"$n",source.name());
			if(finalCommand.indexOf("$$")>=0)
				finalCommand=Util.replaceAll(finalCommand,"$$","$");

			Vector V=Util.parse(finalCommand);
			V.insertElementAt(new Integer(RESPONSE_DELAY),0);
			for(int f=0;f<responseQue.size();f++)
			{
				Vector V1=(Vector)responseQue.elementAt(f);
				if(Util.combine(V1,1).equalsIgnoreCase(finalCommand))
				{
					V=null;
					break;
				}
			}
			if(V!=null)
				responseQue.addElement(V);
		}
	}


	private boolean match(String expression, String message, String[] rest)
	{
		int l=expression.length();
		if(l==0) return true;
		if((expression.charAt(0)=='(')
		&&(expression.charAt(l-1)==')'))
			expression=expression.substring(1,expression.length()-1);

		int end=0;
		for(;((end<expression.length())&&(("(&|~").indexOf(expression.charAt(end))<0));end++);
		String check=null;
		if(end<expression.length())
		{
			check=expression.substring(0,end);
			expression=expression.substring(end);
		}
		else
		{
			check=expression;
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
			response=message.trim().startsWith(check.substring(1));
			if(response)
				rest[0]=message.substring(check.substring(1).trim().length());
		}
		else
		if(check.length()>0)
		{
			int x=message.toUpperCase().indexOf(check.toUpperCase());
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
					return response&match(expression.substring(1,expEnd),message,rest);
				}
				return response;
			}
			else
			if(expression.startsWith("&"))
				return response&&match(expression.substring(1),message,rest);
			else
			if(expression.startsWith("|"))
				return response||match(expression.substring(1),message,rest);
			else
			if(expression.startsWith("~"))
				return response&&(!match(expression.substring(1),message,rest));

		}
		return response;
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);

		if((!canFreelyBehaveNormal(affecting))
		||(CMSecurity.isDisabled("MUDCHAT")))
			return;
		MOB mob=msg.source();
		MOB monster=(MOB)affecting;
		if((!msg.amISource(monster))
		&&(!mob.isMonster())
		&&(Sense.canBeHeardBy(mob,monster))
		&&(Sense.canBeSeenBy(mob,monster))
		&&(Sense.canBeSeenBy(monster,mob))
		&&(!mob.isInCombat())
		&&(!monster.isInCombat()))
		{
			Vector myResponses=null;
			myChatGroup=getMyChatGroup(monster,getChatGroups());
			String rest[]=new String[1];

			if((msg.targetMinor()==CMMsg.TYP_SPEAK)
			&&(msg.amITarget(monster)
			   ||((mob.location()==monster.location())
				  &&(talkDown<=0)
				  &&(mob.location().numPCInhabitants()==1)))
			&&(Sense.canBeHeardBy(mob,monster))
			&&(myChatGroup!=null)
			&&(lastReactedTo!=msg.source())
			&&(msg.sourceMessage()!=null)
			&&(msg.targetMessage()!=null))
			{
				int x=msg.sourceMessage().indexOf("'");
				int y=msg.sourceMessage().lastIndexOf("'");
				if((x>=0)&&(y>x))
				{
					String str=" "+msg.sourceMessage().substring(x+1,y)+" ";
					int l=0;
					for(int i=1;i<myChatGroup.size();i++)
					{
						Vector possResponses=(Vector)myChatGroup.elementAt(i);
						String expression=((String)possResponses.elementAt(0)).trim();
						l=expression.length();
						if((l>0)
						&&(expression.charAt(0)=='(')
						&&(expression.charAt(l-1)==')'))
						{
							if(match(expression.substring(1,expression.length()-1),str,rest))
							{
								if(myResponses==null) myResponses=new Vector();
								myResponses.addAll(possResponses);
							}
						}
					}
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(Sense.canBeHeardBy(mob,monster))
			&&(Sense.canBeSeenBy(mob,monster))
			&&(mob.isMonster())
			&&(msg.source()!=monster))
			   talkDown=this.TALK_WAIT_DELAY;
			else
			if((Sense.canBeHeardBy(mob,monster))
			&&(Sense.canBeSeenBy(mob,monster))
			&&(Sense.canBeSeenBy(monster,mob))
			&&(talkDown<=0)
			&&(lastReactedTo!=msg.source())
			&&(myChatGroup!=null))
			{
				String str=null;
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
						l=expression.length();
						if((l>0)
						&&(expression.charAt(0)==c1)
						&&(expression.charAt(l-1)==c2))
						{
							if(match(expression.substring(1,expression.length()-1),str,rest))
							{
								if(myResponses==null) myResponses=new Vector();
								myResponses.addAll(possResponses);
							}
						}
					}
				}
			}


			if(myResponses!=null)
			{
				lastReactedTo=msg.source();
				queResponse(myResponses,monster,mob,rest[0]);
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((tickID==MudHost.TICK_MOB)
		&&(ticking instanceof MOB)
		&&(!CMSecurity.isDisabled("MUDCHAT")))
		{
			if(!canFreelyBehaveNormal(ticking))
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
					myChatGroup=getMyChatGroup((MOB)ticking,getChatGroups());
				}
			}
			for(int t=responseQue.size()-1;t>=0;t--)
			{
				Vector que=(Vector)responseQue.elementAt(t);
				Integer I=(Integer)que.elementAt(0);
				I=new Integer(I.intValue()-1);
				que.setElementAt(I,0);
				if(I.intValue()<=0)
				{
					que.removeElementAt(0);
					responseQue.removeElementAt(t);
					((MOB)ticking).doCommand(que);
					lastReactedTo=null;
					// you've done one, so get out before doing another!
					break;
				}
			}
		}
		return true;
	}
}