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
	private static boolean resourceLoaded=false;
	// format: first group is general mob (no other
	// fit found).  All groups are chat groups.
	private static Vector chatGroups=new Vector();
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

	public static synchronized void initChat()
	{
		if(resourceLoaded) return;
		resourceLoaded=true;
		loadChatData("chat.dat");
	}

	private static synchronized void loadChatData(String resourceName)
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
				otherChatGroup=matchChatGroup(str.substring(1).trim());
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


	private static Vector matchChatGroup(String myName)
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
						String name=null;
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

	private Vector getMyChatGroup(MOB forMe)
	{
		initChat();
		if((myChatGroup!=null)&&(myOldName.equals(forMe.name())))
			return myChatGroup;
		myOldName=forMe.name();
		Vector V=matchChatGroup(myOldName.toUpperCase());
		if(V!=null) return V;
		V=matchChatGroup(forMe.description());
		if(V!=null) return V;
		V=matchChatGroup(forMe.displayText());
		if(V!=null) return V;
		V=matchChatGroup(CMClass.className(forMe));
		if(V!=null) return V;
		if(this.getParms().length()>0)
			V=matchChatGroup(this.getParms());
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
				int t=finalCommand.indexOf(" her ");
				while(t>=0)
					finalCommand=finalCommand.substring(0,t)+" <S-HISHER> "+finalCommand.substring(t+2);
			}
			else
			if(finalCommand.startsWith("!"))
				finalCommand=finalCommand.substring(1).trim();
			else
			if(finalCommand.startsWith("\""))
				finalCommand="say \""+finalCommand.substring(1).trim()+"\"";
			else
			if(target!=null)
				finalCommand="say \""+target.name()+"\" "+finalCommand.trim();

			int t=finalCommand.indexOf("$r");
			while(t>=0)
			{
				finalCommand=finalCommand.substring(0,t)+rest+finalCommand.substring(t+2);
				t=finalCommand.indexOf("$r");
			}
			t=finalCommand.indexOf("$t");
			while((t>=0)&&(target!=null))
			{
				finalCommand=finalCommand.substring(0,t)+target.name()+finalCommand.substring(t+2);
				t=finalCommand.indexOf("$t");
			}
			t=finalCommand.indexOf("$n");
			while((t>=0)&&(target!=null))
			{
				finalCommand=finalCommand.substring(0,t)+source.name()+finalCommand.substring(t+2);
				t=finalCommand.indexOf("$n");
			}
			t=finalCommand.indexOf("$$");
			while((t>=0)&&(target!=null))
			{
				finalCommand=finalCommand.substring(0,t)+"$"+finalCommand.substring(t+2);
				t=finalCommand.indexOf("$$");
			}

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
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);

		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB mob=affect.source();
		MOB monster=(MOB)affecting;
		if((!affect.amISource(monster))
		&&(!mob.isMonster())
		&&(Sense.canBeHeardBy(mob,monster))
		&&(Sense.canBeSeenBy(mob,monster))
		&&(Sense.canBeSeenBy(monster,mob)))
		{
			Vector myResponses=null;
			myChatGroup=getMyChatGroup(monster);
			String rest[]=new String[1];

			if((affect.targetMinor()==Affect.TYP_SPEAK)
			&&(affect.amITarget(monster)
			   ||((mob.location()==monster.location())
				  &&(talkDown<=0)
				  &&(mob.location().numPCInhabitants()==1)))
			&&(Sense.canBeHeardBy(mob,monster))
			&&(myChatGroup!=null)
			&&(lastReactedTo!=affect.source())
			&&(affect.sourceMessage()!=null)
			&&(affect.targetMessage()!=null))
			{
				int x=affect.sourceMessage().indexOf("'");
				int y=affect.sourceMessage().lastIndexOf("'");
				if((x>=0)&&(y>x))
				{
					String msg=" "+affect.sourceMessage().substring(x+1,y)+" ";
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
							if(match(expression.substring(1,expression.length()-1),msg,rest))
							{
								myResponses=possResponses;
								break;
							}
						}
					}
				}
			}
			else
			if((affect.sourceMinor()==Affect.TYP_SPEAK)
			&&(Sense.canBeHeardBy(mob,monster))
			&&(Sense.canBeSeenBy(mob,monster))
			&&(mob.isMonster())
			&&(affect.source()!=monster))
			   talkDown=this.TALK_WAIT_DELAY;
			else
			if((Sense.canBeHeardBy(mob,monster))
			&&(Sense.canBeSeenBy(mob,monster))
			&&(Sense.canBeSeenBy(monster,mob))
			&&(talkDown<=0)
			&&(lastReactedTo!=affect.source())
			&&(myChatGroup!=null))
			{
				String msg=null;
				char c1='[';
				char c2=']';
				if((affect.amITarget(monster)&&(affect.targetMessage()!=null)))
					msg=" "+affect.targetMessage()+" ";
				else
				if(affect.othersMessage()!=null)
				{
					c1='{';
					c2='}';
					msg=" "+affect.othersMessage()+" ";
				}
				if(msg!=null)
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
							if(match(expression.substring(1,expression.length()-1),msg,rest))
							{
								myResponses=possResponses;
								break;
							}
						}
					}
				}
			}


			if(myResponses!=null)
			{
				lastReactedTo=affect.source();
				queResponse(myResponses,monster,mob,rest[0]);
			}
		}
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((tickID==Host.MOB_TICK)&&(ticking instanceof MOB))
		{
			if(!canFreelyBehaveNormal(ticking))
			{
				responseQue.removeAllElements();
				return;
			}

			if(talkDown>0) talkDown--;

			if(tickDown>=0)
			{
				--tickDown;
				if(tickDown<0)
				{
					myChatGroup=getMyChatGroup((MOB)ticking);
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
					try
					{
						ExternalPlay.doCommand((MOB)ticking,que);
						lastReactedTo=null;
						// you've done one, so get out before doing another!
						break;
					}
					catch(Exception e)
					{
						Log.errOut("MudChat",e);
					}
				}
			}
		}
	}
}