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
			if(httpReq.isRequestParameter("BEHAV1"))
			{
				int num=1;
				String behav=httpReq.getRequestParameter("BEHAV"+num);
				String theparm=httpReq.getRequestParameter("BDATA"+num);
				while((behav!=null)&&(theparm!=null))
				{
					if(behav.length()>0)
					{
						theclasses.addElement(behav);
						String t=theparm;
						t=Util.replaceAll(t,"\"","&quot;");
						theparms.addElement(t);
					}
					num++;
					behav=httpReq.getRequestParameter("BEHAV"+num);
					theparm=httpReq.getRequestParameter("BDATA"+num);
				}
			}
			else
			for(int b=0;b<E.numBehaviors();b++)
			{
				Behavior B=E.fetchBehavior(b);
				if(B!=null)
				{
					theclasses.addElement(CMClass.className(B));
					String t=B.getParms();
					t=Util.replaceAll(t,"\"","&quot;");
					theparms.addElement(t);
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
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=50%>");
				str.append("<INPUT TYPE=TEXT SIZE=30 NAME=BDATA"+(i+1)+" VALUE=\""+theparm+"\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=50%>");
			str.append("<SELECT ONCHANGE=\"AddBehavior(this);\" NAME=BEHAV"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Behavior");
			
			Object[] sortedB=null;
			Vector sortMeB=new Vector();
			for(Enumeration b=CMClass.behaviors();b.hasMoreElements();)
			{
				Behavior B=(Behavior)b.nextElement();
				if(B.canImprove(E))
					sortMeB.addElement(CMClass.className(B));
			}
			sortedB=(Object[])(new TreeSet(sortMeB)).toArray();
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
			if(httpReq.isRequestParameter("AFFECT1"))
			{
				int num=1;
				String behav=httpReq.getRequestParameter("AFFECT"+num);
				String theparm=httpReq.getRequestParameter("ADATA"+num);
				while((behav!=null)&&(theparm!=null))
				{
					if(behav.length()>0)
					{
						theclasses.addElement(behav);
						String t=theparm;
						t=Util.replaceAll(t,"\"","&quot;");
						theparms.addElement(t);
					}
					num++;
					behav=httpReq.getRequestParameter("AFFECT"+num);
					theparm=httpReq.getRequestParameter("ADATA"+num);
				}
			}
			else
			for(int a=0;a<E.numAffects();a++)
			{
				Ability Able=E.fetchAffect(a);
				if((Able!=null)&&(!Able.isBorrowed(E)))
				{
					theclasses.addElement(CMClass.className(Able));
					String t=Able.text();
					t=Util.replaceAll(t,"\"","&quot;");
					theparms.addElement(t);
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
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=50%>");
				str.append("<INPUT TYPE=TEXT SIZE=30 NAME=ADATA"+(i+1)+" VALUE=\""+theparm+"\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=50%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=AFFECT"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select an Affect");
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=(Ability)a.nextElement();
				if(!A.canAffect(E)) continue;
				String cnam=A.ID();
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
		String last=httpReq.getRequestParameter("AREA");
		if(last==null) return " @break@";

		if(!httpReq.getMUD().gameStatusStr().equalsIgnoreCase("OK"))
			return httpReq.getMUD().gameStatusStr();

		if(last.length()>0)
		{
			Area A=CMMap.getArea(last);
			if(A!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuffer s=ExternalPlay.getHelpText("AREA_"+A.Name(),null);
					if(s==null)	s=ExternalPlay.getHelpText(A.Name(),null);
					str.append(helpHelp(s));
				}
				if(parms.containsKey("CLIMATES"))
				{
					int climate=A.climateType();
					if(httpReq.isRequestParameter("CLIMATE"))
					{
						climate=Util.s_int(httpReq.getRequestParameter("CLIMATE"));
						for(int i=1;;i++)
							if(httpReq.isRequestParameter("CLIMATE"+(new Integer(i).toString())))
								climate=climate|Util.s_int(httpReq.getRequestParameter("CLIMATE"+(new Integer(i).toString())));
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
				if(parms.containsKey("TECHLEVEL"))
				{
					for(int i=0;i<Area.TECH_DESCS.length;i++)
					{
						str.append("<OPTION VALUE="+i);
						if(i==A.getTechLevel()) str.append(" SELECTED");
						str.append(">"+Area.TECH_DESCS[i].toLowerCase());
					}
				}
				if(parms.containsKey("NAME"))
				{
					String name=httpReq.getRequestParameter("NAME");
					if((name==null)||(name.length()==0))
						name=A.Name();
					str.append(name);
				}
				if(parms.containsKey("ARCHP"))
				{
					String path=httpReq.getRequestParameter("ARCHP");
					if((path==null)||(path.length()==0))
						path=A.getArchivePath();
					str.append(path);
				}
				if(parms.containsKey("CLASSES"))
				{
					String className=httpReq.getRequestParameter("CLASS");
					if((className==null)||(className.length()==0))
						className=CMClass.className(A);
					Object[] sortedA=(Object[])Resources.getResource("MUDGRINDER-AREAS");
					if(sortedA==null)
					{
						Vector sortMeA=new Vector();
						for(Enumeration a=CMClass.areaTypes();a.hasMoreElements();)
							sortMeA.addElement(CMClass.className(a.nextElement()));
						sortedA=(Object[])(new TreeSet(sortMeA)).toArray();
						Resources.submitResource("MUDGRINDER-AREAS",sortedA);
					}
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
					String subOps=httpReq.getRequestParameter("SUBOPS");
					if((subOps==null)||(subOps.length()==0))
						subOps=A.getSubOpList();
					else
					for(int i=1;;i++)
						if(httpReq.isRequestParameter("SUBOPS"+(new Integer(i).toString())))
							subOps+=";"+httpReq.getRequestParameter("SUBOPS"+(new Integer(i).toString()));
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
					String desc=httpReq.getRequestParameter("DESCRIPTION");
					if((desc==null)||(desc.length()==0))
						desc=A.description();
					str.append(desc);
				}

                if(parms.containsKey("PARENT"))
                {
                    String parents=httpReq.getRequestParameter("PARENT");
                    if((parents==null)||(parents.length()==0))
                        parents=A.getParentsList();
                    else
                    for(int i=1;;i++)
                        if(httpReq.isRequestParameter("PARENT"+(new Integer(i).toString())))
                            parents+=";"+httpReq.getRequestParameter("PARENT"+(new Integer(i).toString()));
                        else
                            break;
                    for(Enumeration e=CMMap.areas();e.hasMoreElements();)
                    {
                        String cnam=((Area)e.nextElement()).Name();
                        str.append("<OPTION VALUE=\""+cnam+"\"");
                        if(parents.equals(cnam)
                           ||(parents.indexOf(";"+cnam)>=0)
                           ||(parents.startsWith(cnam+";")))
                                str.append(" SELECTED");
                        str.append(">"+cnam);
                    }
                }

                if(parms.containsKey("CHILDREN"))
                {
                    String children=httpReq.getRequestParameter("CHILDREN");
                    if((children==null)||(children.length()==0))
                        children=A.getChildrenList();
                    else
                    for(int i=1;;i++)
                        if(httpReq.isRequestParameter("CHILDREN"+(new Integer(i).toString())))
                            children+=";"+httpReq.getRequestParameter("CHILDREN"+(new Integer(i).toString()));
                        else
                            break;
                    for(Enumeration e=CMMap.areas();e.hasMoreElements();)
                    {
                        String cnam=((Area)e.nextElement()).Name();
                        str.append("<OPTION VALUE=\""+cnam+"\"");
                        if(children.equals(cnam)
                           ||(children.indexOf(";"+cnam)>=0)
                           ||(children.startsWith(cnam+";")))
                                str.append(" SELECTED");
                        str.append(">"+cnam);
                    }
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
