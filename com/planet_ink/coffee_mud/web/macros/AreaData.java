package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AreaData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	
	
	public static StringBuffer affectsNBehaves(Environmental E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		StringBuffer str=new StringBuffer("");
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
			for(int b=0;b<E.numBehaviors();b++)
			{
				Behavior B=E.fetchBehavior(b);
				if(B!=null)
				{
					theclasses.addElement(CMClass.className(B));
					theparms.addElement(B.getParms());
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			Vector sortMeB=new Vector();
			for(int r=0;r<CMClass.behaviors.size();r++)
			{
				Behavior B=(Behavior)CMClass.behaviors.elementAt(r);
				if(B.canImprove(E))
					sortMeB.addElement(CMClass.className(B));
			}
			Object[] sortedB=(Object[])(new TreeSet(sortMeB)).toArray();
			for(int i=0;i<theclasses.size();i++)
			{
				String theclass=(String)theclasses.elementAt(i);
				String theparm=(String)theparms.elementAt(i);
				str.append("<TR><TD WIDTH=50%>");
				str.append("<SELECT ONCHANGE=\"EditBehavior(this);\" NAME=BEHAV"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				for(int r=0;r<sortedB.length;r++)
				{
					String cnam=(String)sortedB[r];
					str.append("<OPTION VALUE=\""+cnam+"\"");
					if(theclass.equals(cnam))
						str.append(" SELECTED");
					str.append(">"+cnam);
				}
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=50%>");
				str.append("<INPUT TYPE=TEXT SIZE=30 NAME=BDATA"+(i+1)+" VALUE=\""+theparm+"\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=50%>");
			str.append("<SELECT ONCHANGE=\"AddBehavior(this);\" NAME=BEHAV"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Behavior");
			for(int r=0;r<sortedB.length;r++)
			{
				String cnam=(String)sortedB[r];
				str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
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
			for(int a=0;a<E.numAffects();a++)
			{
				Ability Able=E.fetchAffect(a);
				if((Able!=null)&&(!Able.isBorrowed(E)))
				{
					theclasses.addElement(CMClass.className(Able));
					theparms.addElement(Able.text());
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			Vector sortMeA=new Vector();
			for(int r=0;r<CMClass.abilities.size();r++)
			{
				Ability A=(Ability)CMClass.abilities.elementAt(r);
				if(A.canAffect(E))
					sortMeA.addElement(CMClass.className(A));
			}
			Object[] sortedA=(Object[])(new TreeSet(sortMeA)).toArray();
			for(int i=0;i<theclasses.size();i++)
			{
				String theclass=(String)theclasses.elementAt(i);
				String theparm=(String)theparms.elementAt(i);
				str.append("<TR><TD WIDTH=50%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=AFFECT"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				for(int r=0;r<sortedA.length;r++)
				{
					String cnam=(String)sortedA[r];
					str.append("<OPTION VALUE=\""+cnam+"\"");
					if(theclass.equals(cnam))
						str.append(" SELECTED");
					str.append(">"+cnam);
				}
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=50%>");
				str.append("<INPUT TYPE=TEXT SIZE=30 NAME=ADATA"+(i+1)+" VALUE=\""+theparm+"\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=50%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=AFFECT"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select an Affect");
			for(int r=0;r<sortedA.length;r++)
			{
				String cnam=(String)sortedA[r];
				str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
			}
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=50%>");
			str.append("<INPUT TYPE=TEXT SIZE=30 NAME=ADATA"+(theclasses.size()+1)+" VALUE=\"\">");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("AREA");
		if(last==null) return " @break@";

		if(last.length()>0)
		{
			Area A=CMMap.getArea(last);
			if(A!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuffer s=ExternalPlay.getHelpText("AREA_"+A.name());
					if(s==null)	s=ExternalPlay.getHelpText(A.name());
					str.append(helpHelp(s));
				}
				if(parms.containsKey("CLIMATES"))
				{
					int climate=A.climateType();
					if(httpReq.getRequestParameters().containsKey("CLIMATE"))
					{
						climate=Util.s_int((String)httpReq.getRequestParameters().get("CLIMATE"));
						for(int i=1;;i++)
							if(httpReq.getRequestParameters().containsKey("CLIMATE"+(new Integer(i).toString())))
								climate=climate|Util.s_int((String)httpReq.getRequestParameters().get("CLIMATE"+(new Integer(i).toString())));
							else
								break;
					}
					for(int i=1;i<Area.NUM_CLIMATES;i++)
					{
						String climstr=Area.CLIMATE_DESCS[i];
						int mask=Util.pow(2,i-1);
						str.append("<OPTION VALUE="+mask);
						if((climate&mask)>0) str.append(" SELECTED");
						str.append(">"+climstr);
					}
				}
				if(parms.containsKey("NAME"))
				{
					String name=(String)httpReq.getRequestParameters().get("NAME");
					if((name==null)||(name.length()==0))
						name=A.name();
					str.append(name);
				}
				if(parms.containsKey("CLASSES"))
				{
					String className=(String)httpReq.getRequestParameters().get("CLASS");
					if((className==null)||(className.length()==0))
						className=CMClass.className(A);
					Vector sortMeA=new Vector();
					for(int r=0;r<CMClass.areaTypes.size();r++)
						sortMeA.addElement(CMClass.className(CMClass.areaTypes.elementAt(r)));
					Object[] sortedA=(Object[])(new TreeSet(sortMeA)).toArray();
					for(int r=0;r<sortedA.length;r++)
					{
						String cnam=(String)sortedA[r];
						str.append("<OPTION VALUE=\""+cnam+"\"");
						if(className.equals(cnam))
							str.append(" SELECTED");
						str.append(">"+cnam);
					}
				}
				if(parms.containsKey("TESTSTUFF"))
					str.append(A.text());
				
				str.append(AreaData.affectsNBehaves(A,httpReq,parms));
				
				if(parms.containsKey("SUBOPS"))
				{
					String subOps=(String)httpReq.getRequestParameters().get("SUBOPS");
					if((subOps==null)||(subOps.length()==0))
						subOps=A.getSubOpList();
					else
					for(int i=1;;i++)
						if(httpReq.getRequestParameters().containsKey("SUBOPS"+(new Integer(i).toString())))
							subOps+=";"+(String)httpReq.getRequestParameters().get("SUBOPS"+(new Integer(i).toString()));
						else
							break;
					Vector V=ExternalPlay.userList();
					for(int v=0;v<V.size();v++)
					{
						String cnam=(String)V.elementAt(v);
						str.append("<OPTION VALUE=\""+cnam+"\"");
						if(subOps.equals(cnam)
						   ||(subOps.indexOf(";"+cnam)>=0)
						   ||(subOps.startsWith(cnam+";")))
							str.append(" SELECTED");
						str.append(">"+cnam);
					}
				}
				if(parms.containsKey("DESCRIPTION"))
				{
					String desc=(String)httpReq.getRequestParameters().get("DESCRIPTION");
					if((desc==null)||(desc.length()==0))
						desc=A.description();
					str.append(desc);
				}
									 
				if(parms.containsKey("SEASON"))
					str.append(Area.SEASON_DESCS[A.getSeasonCode()]+", ");
				if(parms.containsKey("TODCODE"))
					str.append(Area.TOD_DESC[A.getTODCode()]+", ");
				if(parms.containsKey("WEATHER"))
					str.append(A.getWeatherDescription()+", ");
				if(parms.containsKey("MOON"))
					str.append(Area.MOON_PHASES[A.getMoonPhase()]+", ");
				if(parms.containsKey("STATS"))
					str.append(A.getAreaStats()+", ");
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return strstr;
			}
		}
		return "";
	}
}
