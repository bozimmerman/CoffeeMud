package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class RoomData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include help, ranges, quality, target, alignment, domain, 
	// qualifyQ, auto
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("ROOM");
		if(last==null) return " @break@";

		if(last.length()>0)
		{
			Room R=CMMap.getRoom(last);
			if(R!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("NAME"))
				{
					String name=(String)httpReq.getRequestParameters().get("NAME");
					if((name==null)||(name.length()==0))
						name=R.displayText();
					str.append(name);
				}
				if(parms.containsKey("CLASSES"))
				{
					String className=(String)httpReq.getRequestParameters().get("CLASS");
					if((className==null)||(className.length()==0))
						className=CMClass.className(R);
					for(int r=0;r<CMClass.locales.size();r++)
					{
						Room cnam=(Room)CMClass.locales.elementAt(r);
						str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
						if(className.equalsIgnoreCase(CMClass.className(cnam)))
							str.append(" SELECTED");
						str.append(">"+CMClass.className(cnam));
					}
				}
				if(parms.containsKey("BEHAVIORS"))
				{
					Vector theclasses=new Vector();
					Vector theparms=new Vector();
					if(httpReq.getRequestParameters().containsKey("BEHAV1"))
					{
						int num=1;
						String behav=(String)httpReq.getRequestParameters().get("BEHAV"+num);
						String theparm=(String)httpReq.getRequestParameters().get("BDATA"+num);
						while((behav!=null)&&(theparm!=null))
						{
							if(behav.length()>0)
							{
								theclasses.addElement(behav);
								theparms.addElement(theparm);
							}
							num++;
							behav=(String)httpReq.getRequestParameters().get("BEHAV"+num);
							theparm=(String)httpReq.getRequestParameters().get("BDATA"+num);
						}
					}
					else
					for(int b=0;b<R.numBehaviors();b++)
					{
						Behavior B=R.fetchBehavior(b);
						if(B!=null)
						{
							theclasses.addElement(CMClass.className(B));
							theparms.addElement(B.getParms());
						}
					}
					str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
					for(int i=0;i<theclasses.size();i++)
					{
						String theclass=(String)theclasses.elementAt(i);
						String theparm=(String)theparms.elementAt(i);
						str.append("<TR><TD WIDTH=50%>");
						str.append("<SELECT ONCHANGE=\"EditBehavior(this);\" NAME=BEHAV"+(i+1)+">");
						str.append("<OPTION VALUE=\"\">Delete!");
						for(int b=0;b<CMClass.behaviors.size();b++)
						{
							Behavior B=(Behavior)CMClass.behaviors.elementAt(b);
							str.append("<OPTION VALUE=\""+CMClass.className(B)+"\"");
							if(CMClass.className(B).equals(theclass))
								str.append(" SELECTED");
							str.append(">"+CMClass.className(B));
						}
						str.append("</SELECT>");
						str.append("</TD><TD WIDTH=50%>");
						str.append("<INPUT TYPE=TEXT SIZE=30 NAME=BDATA"+(i+1)+" VALUE=\""+theparm+"\">");
						str.append("</TD></TR>");
					}
					str.append("<TR><TD WIDTH=50%>");
					str.append("<SELECT ONCHANGE=\"AddBehavior(this);\" NAME=BEHAV"+(theclasses.size()+1)+">");
					str.append("<OPTION SELECTED VALUE=\"\">Select a Behavior");
					for(int b=0;b<CMClass.behaviors.size();b++)
					{
						Behavior B=(Behavior)CMClass.behaviors.elementAt(b);
						str.append("<OPTION VALUE=\""+CMClass.className(B)+"\">"+CMClass.className(B));
					}
					str.append("</SELECT>");
					str.append("</TD><TD WIDTH=50%>");
					str.append("<INPUT TYPE=TEXT SIZE=30 NAME=BDATA"+(theclasses.size()+1)+" VALUE=\"\">");
					str.append("</TD></TR>");
					str.append("</TABLE>");
				}
				if(parms.containsKey("AFFECTS"))
				{
					Vector theclasses=new Vector();
					Vector theparms=new Vector();
					if(httpReq.getRequestParameters().containsKey("AFFECT1"))
					{
						int num=1;
						String behav=(String)httpReq.getRequestParameters().get("AFFECT"+num);
						String theparm=(String)httpReq.getRequestParameters().get("ADATA"+num);
						while((behav!=null)&&(theparm!=null))
						{
							if(behav.length()>0)
							{
								theclasses.addElement(behav);
								theparms.addElement(theparm);
							}
							num++;
							behav=(String)httpReq.getRequestParameters().get("AFFECT"+num);
							theparm=(String)httpReq.getRequestParameters().get("ADATA"+num);
						}
					}
					else
					for(int a=0;a<R.numAffects();a++)
					{
						Ability Able=R.fetchAffect(a);
						if((Able!=null)&&(!Able.isBorrowed(R)))
						{
							theclasses.addElement(CMClass.className(Able));
							theparms.addElement(Able.text());
						}
					}
					str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
					for(int i=0;i<theclasses.size();i++)
					{
						String theclass=(String)theclasses.elementAt(i);
						String theparm=(String)theparms.elementAt(i);
						str.append("<TR><TD WIDTH=50%>");
						str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=AFFECT"+(i+1)+">");
						str.append("<OPTION VALUE=\"\">Delete!");
						for(int b=0;b<CMClass.abilities.size();b++)
						{
							Ability B=(Ability)CMClass.abilities.elementAt(b);
							str.append("<OPTION VALUE=\""+CMClass.className(B)+"\"");
							if(CMClass.className(B).equals(theclass))
								str.append(" SELECTED");
							str.append(">"+CMClass.className(B));
						}
						str.append("</SELECT>");
						str.append("</TD><TD WIDTH=50%>");
						str.append("<INPUT TYPE=TEXT SIZE=30 NAME=ADATA"+(i+1)+" VALUE=\""+theparm+"\">");
						str.append("</TD></TR>");
					}
					str.append("<TR><TD WIDTH=50%>");
					str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=AFFECT"+(theclasses.size()+1)+">");
					str.append("<OPTION SELECTED VALUE=\"\">Select an Affect");
					for(int b=0;b<CMClass.abilities.size();b++)
					{
						Ability B=(Ability)CMClass.abilities.elementAt(b);
						str.append("<OPTION VALUE=\""+CMClass.className(B)+"\">"+CMClass.className(B));
					}
					str.append("</SELECT>");
					str.append("</TD><TD WIDTH=50%>");
					str.append("<INPUT TYPE=TEXT SIZE=30 NAME=ADATA"+(theclasses.size()+1)+" VALUE=\"\">");
					str.append("</TD></TR>");
					str.append("</TABLE>");
				}
				if(parms.containsKey("DESCRIPTION"))
				{
					String desc=(String)httpReq.getRequestParameters().get("DESCRIPTION");
					if((desc==null)||(desc.length()==0))
						desc=R.description();
					str.append(desc);
				}
									 
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return strstr;
			}
		}
		return "";
	}
}
