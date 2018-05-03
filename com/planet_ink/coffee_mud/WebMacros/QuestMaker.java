package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.QuestManager.QMCommand;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.regex.Pattern;

/*
   Copyright 2006-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class QuestMaker extends StdWebMacro
{
	@Override
	public String name()
	{
		return "QuestMaker";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	private static final Pattern keyPattern=Pattern.compile("^AT_(.+)");

	public DVector getPage(MOB mob, HTTPRequest httpReq, String template, String page, String fileToGet)
	{
		DVector pageList=(DVector)httpReq.getRequestObjects().get("QM_PAGE_LIST");
		DVector filePages=(DVector)httpReq.getRequestObjects().get("QM_FILE_PAGES");
		if(template.length()==0)
		{
			httpReq.removeUrlParameter("QM_FILE_PAGES");
			filePages=null;
			if(pageList!=null)
				return pageList;
			pageList=CMLib.quests().getQuestTemplate(mob, fileToGet);
			httpReq.getRequestObjects().put("QM_PAGE_LIST",pageList);
			return pageList;
		}

		final int pageNumber=CMath.s_int(page)-1;
		if(filePages==null)
		{
			filePages=CMLib.quests().getQuestTemplate(mob, template);
			httpReq.getRequestObjects().put("QM_FILE_PAGES",filePages);
		}
		final List<DVector> qPages=(List<DVector>)filePages.elementAt(0,4);
		if(pageNumber<=0)
			return qPages.get(0);
		if(pageNumber>=qPages.size())
			return qPages.get(qPages.size()-1);
		return qPages.get(pageNumber);
	}

	private String itemList(List<Item> itemList, Item oldItem, String oldValue)
	{
		final StringBuffer list=new StringBuffer("");
		if(oldItem==null)
			oldItem=RoomData.getItemFromCatalog(oldValue);
		for (final Item I : itemList)
		{
			list.append("<OPTION VALUE=\""+RoomData.getItemCode(itemList, I)+"\" ");
			if((oldItem!=null)&&(oldItem.sameAs(I)))
				list.append("SELECTED");
			list.append(">");
			list.append(I.Name()+RoomData.getObjIDSuffix(I));
		}
		list.append("<OPTION VALUE=\"\">------ CATALOGED -------");
		final String[] names=CMLib.catalog().getCatalogItemNames();
		for (final String name : names)
		{
			list.append("<OPTION VALUE=\"CATALOG-"+name+"\"");
			if((oldItem!=null)
			&&(CMLib.flags().isCataloged(oldItem))
			&&(oldItem.Name().equalsIgnoreCase(name)))
				list.append(" SELECTED");
			list.append(">"+name);
		}
		return list.toString();
	}

	public List<MOB> getCatalogMobsForList(Physical[] fromList)
	{
		final List<MOB> toList=new Vector<MOB>();
		for(Physical P : fromList)
		{
			P=(Physical)P.copyOf();
			CMLib.catalog().changeCatalogUsage(P,true);
			if(P instanceof MOB)
				toList.add((MOB)P);
		}
		return toList;
	}

	public List<Item> getCatalogItemsForList(Physical[] fromList)
	{
		final List<Item> toList=new Vector<Item>();
		for(Physical P : fromList)
		{
			P=(Physical)P.copyOf();
			CMLib.catalog().changeCatalogUsage(P,true);
			if(P instanceof Item)
				toList.add((Item)P);
		}
		return toList;
	}

	private String mobList(List<MOB> mobList, MOB oldMob, String oldValue)
	{
		final StringBuffer list=new StringBuffer("");
		if(oldMob==null)
			oldMob=RoomData.getMOBFromCatalog(oldValue);
		for (final MOB M2 : mobList)
		{
			list.append("<OPTION VALUE=\""+RoomData.getMOBCode(mobList, M2)+"\" ");
			if((oldMob!=null)&&(oldMob.sameAs(M2)))
				list.append("SELECTED");
			list.append(">");
			list.append(M2.Name()+RoomData.getObjIDSuffix(M2));
		}
		list.append("<OPTION VALUE=\"\">------ CATALOGED -------");
		final String[] names=CMLib.catalog().getCatalogMobNames();
		for (final String name : names)
		{
			list.append("<OPTION VALUE=\"CATALOG-"+name+"\"");
			if((oldMob!=null)
			&&(CMLib.flags().isCataloged(oldMob))
			&&(oldMob.Name().equalsIgnoreCase(name)))
				list.append(" SELECTED");
			list.append(">"+name);
		}
		return list.toString();
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		if((parms==null)||(parms.size()==0))
			return "";
		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null)
			return "[error -- no authenticated mob!]";

		String qFileToGet=null;
		if(parms.containsKey("QMFILETOGET"))
			qFileToGet=parms.get("QMFILETOGET");

		String qTemplate=httpReq.getUrlParameter("QMTEMPLATE");
		if((qTemplate==null)||(qTemplate.length()==0))
			qTemplate="";

		String qPageStr=httpReq.getUrlParameter("QMPAGE");
		if((qPageStr==null)||(qPageStr.length()==0))
			qPageStr="";

		String qPageErrors=httpReq.getUrlParameter("QMPAGEERRORS");
		if((qPageErrors==null)||(qPageErrors.length()==0))
			qPageErrors="";

		if(parms.containsKey("QMPAGETITLE"))
		{
			final DVector pageData=getPage(M,httpReq,qTemplate,qPageStr,null);
			if(pageData==null)
				return "[error -- no page selected!]";
			return (String)pageData.elementAt(0,2);
		}
		else
		if(parms.containsKey("QMPAGEINSTR"))
		{
			final DVector pageData=getPage(M,httpReq,qTemplate,qPageStr,null);
			if(pageData==null)
				return "[error -- no page selected!]";
			return (String)pageData.elementAt(0,3);
		}
		else
		if(parms.containsKey("QMPAGEFIELDS"))
		{
			final DVector pageData=getPage(M,httpReq,qTemplate,qPageStr,qFileToGet);
			if(pageData==null)
				return "[error - no page data?!]";
			String labelColor=parms.get("LABELCOLOR");
			if(labelColor==null)
				labelColor="<FONT COLOR=YELLOW><B>";
			String descColor=parms.get("DESCCOLOR");
			if(descColor==null)
				descColor="<FONT COLOR=WHITE><I>";
			final StringBuffer list=new StringBuffer("");
			if(qTemplate.length()==0)
			{
				String oldTemplate=httpReq.getUrlParameter("QMOLDTEMPLATE");
				if((oldTemplate==null)||(oldTemplate.length()==0))
					oldTemplate="";
				for(int d=0;d<pageData.size();d++)
				{
					list.append("<TR><TD VALIGN=TOP><INPUT TYPE=RADIO NAME=QMTEMPLATE VALUE=\""+htmlOutgoingFilter((String)pageData.elementAt(d,3))+"\"");
					if(pageData.elementAt(d,3).equals(oldTemplate))
						list.append(" CHECKED");
					list.append("> "+labelColor+(String)pageData.elementAt(d,1)+"</B></FONT></I></TD>");
					list.append("<TD>"+descColor+(String)pageData.elementAt(d,2)+"</B></FONT></I></TD></TR>");
					list.append("<TR><TD><BR></TD><TD><BR></TD></TR>");
				}
				return list.toString();
			}

			final List<String> V=new XVector<String>();
			for(final String str : httpReq.getUrlParameters() )
			{
				if(keyPattern.matcher(str.toUpperCase().subSequence(0, str.length())).matches())
					V.add(str.toUpperCase());
			}
			list.append("<TR><TD COLSPAN=2>");
			for(int v=0;v<V.size();v++)
			{
				final String key=V.get(v);
				if((!key.startsWith("AT_")))
					continue;
				boolean thisPage=false;
				for(int step=1;step<pageData.size();step++)
				{
					final String keyName=(String)pageData.elementAt(step,2);
					if(keyName.startsWith("$")
					&&(key.substring(3).toUpperCase().equals(keyName.substring(1))
						||(key.substring(3).toUpperCase().startsWith(keyName.substring(1)+"_")
								&&CMath.isNumber(key.substring(3+keyName.length())))))
					{
						thisPage=true;
						break;
					}
				}
				if(thisPage)
					continue;
				String oldVal=httpReq.getUrlParameter(key);
				if(oldVal==null)
					oldVal="";
				list.append("<INPUT TYPE=HIDDEN NAME="+key+" VALUE=\""+htmlOutgoingFilter(oldVal)+"\">\n\r");
			}
			list.append("</TD></TR>\n\r");

			String lastLabel=null;
			for(int step=1;step<pageData.size();step++)
			{
				final Integer stepType=(Integer)pageData.elementAt(step,1);
				final String keyName=(String)pageData.elementAt(step,2);
				final String defValue=(String)pageData.elementAt(step,3);
				String httpKeyName=keyName;
				if(httpKeyName.startsWith("$"))
					httpKeyName=httpKeyName.substring(1);
				final String keyNameFixed=CMStrings.capitalizeAndLower(httpKeyName.replace('_',' '));
				httpKeyName="AT_"+httpKeyName;
				final boolean optionalEntry=CMath.bset(stepType.intValue(),QuestManager.QM_COMMAND_OPTIONAL);
				final int inputCode=stepType.intValue()&QuestManager.QM_COMMAND_MASK;
				String oldValue=httpReq.getUrlParameter(httpKeyName);
				final QMCommand command = QMCommand.values()[inputCode];
				switch(command)
				{
				case $TITLE:
					break;
				case $LABEL:
					lastLabel = defValue;
					break;
				case $EXPRESSION:
				case $TIMEEXPRESSION:
				case $UNIQUE_QUEST_NAME:
				{
					if(oldValue==null)
						oldValue=defValue;
					list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
					list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
					list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
					list.append("<TD><INPUT TYPE=TEXT SIZE=20 NAME="+httpKeyName+" ");
					list.append(" VALUE=\""+htmlOutgoingFilter(oldValue)+"\"></TD></TR>");
					break;
				}
				case $LONG_STRING:
				{
					if(oldValue==null)
						oldValue=defValue;
					list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
					list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
					list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
					list.append("<TD><TEXTAREA ROWS=3 COLS=40 NAME="+httpKeyName+">");
					list.append(oldValue+"</TEXTAREA></TD></TR>");
					break;
				}
				case $ZAPPERMASK:
				{
					if(oldValue==null)
						oldValue=defValue;
					list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
					list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
					list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
					list.append("<TD><TEXTAREA COLS=40 ROWS=2 NAME="+httpKeyName+">");
					list.append(oldValue+"</TEXTAREA></TD></TR>");
					break;
				}
				case $STRING:
				case $ROOMID:
				case $NAME:
				case $AREA:
				{
					if(oldValue==null)
						oldValue=defValue;
					list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
					list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
					list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
					list.append("<TD><INPUT TYPE=TEXT SIZE=40 NAME="+httpKeyName+" ");
					list.append(" VALUE=\""+htmlOutgoingFilter(oldValue)+"\"></TD></TR>");
					break;
				}
				case $HIDDEN:
					break;
				case $ABILITY:
				{
					if(oldValue==null)
						oldValue=defValue;
					if(oldValue==null)
						oldValue="";
					list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
					list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
					list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
					list.append("<TD><SELECT NAME="+httpKeyName+">");
					if(optionalEntry)
						list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
					Ability A=null;
					for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
					{
						A=e.nextElement();
						if(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)&&(!CMSecurity.isASysOp(M)))
							continue;
						list.append("<OPTION VALUE=\""+A.ID()+"\" ");
						if(oldValue.equals(A.ID()))
							list.append("SELECTED");
						list.append(">");
						list.append(A.ID());
					}
					list.append("</SELECT>");
					list.append("</TD></TR>");
					break;
				}
				case $EXISTING_QUEST_NAME:
				{
					if(oldValue==null)
						oldValue=defValue;
					if(oldValue==null)
						oldValue="";
					list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
					list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
					list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
					list.append("<TD><SELECT NAME="+httpKeyName+">");
					if(optionalEntry)
						list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
					for(int q=0;q<CMLib.quests().numQuests();q++)
					{
						final Quest Q2=CMLib.quests().fetchQuest(q);
						list.append("<OPTION VALUE=\""+Q2.name()+"\" ");
						if(oldValue.equals(Q2.name()))
							list.append("SELECTED");
						list.append(">");
						list.append(Q2.name());
					}
					list.append("</SELECT>");
					list.append("</TD></TR>");
					break;
				}
				case $CHOOSE:
				{
					list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
					list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
					list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
					list.append("<TD><SELECT NAME="+httpKeyName+">");
					final List<String> options=CMParms.parseCommas(defValue.toUpperCase(),true);
					if(optionalEntry)
						options.add(0,"");
					for(int o=0;o<options.size();o++)
					{
						final String val=options.get(o);
						list.append("<OPTION VALUE=\""+val+"\" ");
						if(val.equalsIgnoreCase(oldValue))
							list.append("SELECTED");
						list.append(">");
						list.append(val);
					}
					list.append("</SELECT></TD></TR>");
					break;
				}
				case $ITEMXML:
				{
					if(oldValue==null)
						oldValue=defValue;
					if(oldValue==null)
						oldValue="";
					List<Item> itemList=new Vector<Item>();
					itemList=RoomData.contributeItems(itemList);
					final Item oldItem=RoomData.getItemFromAnywhere(itemList,oldValue);
					list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
					list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
					list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
					list.append("<TD><SELECT NAME="+httpKeyName+">");
					if(optionalEntry)
						list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
					list.append(itemList(itemList,oldItem,oldValue));
					list.append("</SELECT>");
					list.append("<INPUT TYPE=BUTTON NAME=BUTT_"+httpKeyName+" VALUE=\"NEW\" ONCLICK=\"AddNewItem();\">");
					list.append("</TD></TR>");
					break;
				}
				case $ITEMXML_ONEORMORE:
				{
					if(oldValue==null)
						oldValue=defValue;
					List<Item> itemList=new Vector<Item>();
					itemList=RoomData.contributeItems(itemList);
					final Vector<String> oldValues=new Vector<String>();
					int which=1;
					oldValue=httpReq.getUrlParameter(httpKeyName+"_"+which);
					while(oldValue!=null)
					{
						if((!oldValue.equalsIgnoreCase("DELETE"))&&(oldValue.length()>0))
							oldValues.addElement(oldValue);
						which++;
						oldValue=httpReq.getUrlParameter(httpKeyName+"_"+which);
					}
					oldValues.addElement("");
					for(int i=0;i<oldValues.size();i++)
					{
						oldValue=oldValues.elementAt(i);
						final Item oldItem=(oldValue.length()>0)?RoomData.getItemFromAnywhere(itemList,oldValue):null;
						if(i==0)
						{
							list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
							list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
						}
						list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
						list.append("<TD><SELECT NAME="+httpKeyName+"_"+(i+1)+" ONCHANGE=\"Refresh();\">");
						if(i<oldValues.size()-1)
							list.append("<OPTION VALUE=\"DELETE\">Delete!");
						if(oldValue.length()==0)
							list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
						list.append(itemList(itemList,oldItem,oldValue));
						list.append("</SELECT>");
						if(i==oldValues.size()-1)
							list.append("<INPUT TYPE=BUTTON NAME=BUTT_"+httpKeyName+" VALUE=\"NEW\" ONCLICK=\"AddNewItem();\">");
						list.append("</TD></TR>");
					}
					break;
				}
				case $MOBXML:
				{
					if(oldValue==null)
						oldValue=defValue;
					if(oldValue != null)
					{
						List<MOB> mobList=new Vector<MOB>();
						mobList=RoomData.contributeMOBs(mobList);
						final MOB oldMob=RoomData.getMOBFromCode(mobList,oldValue);
						list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
						list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
						list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
						list.append("<TD><SELECT NAME="+httpKeyName+">");
						if(optionalEntry)
							list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
						list.append(mobList(mobList,oldMob,oldValue));
						list.append("</SELECT>");
						list.append("<INPUT TYPE=BUTTON NAME=BUTT_"+httpKeyName+" VALUE=\"NEW\" ONCLICK=\"AddNewMob();\">");
						list.append("</TD></TR>");
					}
					break;
				}
				case $MOBXML_ONEORMORE:
				{
					if(oldValue==null)
						oldValue=defValue;
					final List<MOB>mobList=RoomData.contributeMOBs(new Vector<MOB>());
					final Vector<String> oldValues=new Vector<String>();
					int which=1;
					oldValue=httpReq.getUrlParameter(httpKeyName+"_"+which);
					while(oldValue!=null)
					{
						if((!oldValue.equalsIgnoreCase("DELETE"))&&(oldValue.length()>0))
							oldValues.addElement(oldValue);
						which++;
						oldValue=httpReq.getUrlParameter(httpKeyName+"_"+which);
					}
					oldValues.addElement("");
					for(int i=0;i<oldValues.size();i++)
					{
						oldValue=oldValues.elementAt(i);
						final MOB oldMob=(oldValue.length()>0)?RoomData.getMOBFromCode(mobList,oldValue):null;
						if(i==0)
						{
							list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
							list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
						}
						list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
						list.append("<TD><SELECT NAME="+httpKeyName+"_"+(i+1)+" ONCHANGE=\"Refresh();\">");
						if(i<oldValues.size()-1)
							list.append("<OPTION VALUE=\"DELETE\">Delete!");
						if(oldValue.length()==0)
							list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
						list.append(mobList(mobList,oldMob,oldValue));
						list.append("</SELECT>");
						if(i==oldValues.size()-1)
							list.append("<INPUT TYPE=BUTTON NAME=BUTT_"+httpKeyName+" VALUE=\"NEW\" ONCLICK=\"AddNewMob();\">");
						list.append("</TD></TR>");
					}
					break;
				}
				case $FACTION:
				{
					if(oldValue==null)
						oldValue=defValue;
					if(oldValue==null)
						oldValue="";
					list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
					list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
					list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
					list.append("<TD><SELECT NAME="+httpKeyName+">");
					if(optionalEntry)
						list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
					for(final Enumeration f=CMLib.factions().factions();f.hasMoreElements();)
					{
						final Faction F=(Faction)f.nextElement();
						final String fkey=F.factionID().toUpperCase().trim();
						list.append("<OPTION VALUE=\""+fkey+"\" ");
						if(oldValue.equals(fkey))
							list.append("SELECTED");
						list.append(">");
						list.append(F.name());
					}
					list.append("</SELECT>");
					list.append("</TD></TR>");
					break;
				}
				}
			}
			return list.toString();
		}
		else
		if(parms.containsKey("QMLASTPAGE"))
		{
			final DVector pageData=getPage(M,httpReq,qTemplate,qPageStr,null);
			if(pageData==null)
				return "false";
			final DVector filePages=(DVector)httpReq.getRequestObjects().get("QM_FILE_PAGES");
			if(filePages==null)
				return "false";
			return(((Vector)filePages.elementAt(0,4)).lastElement()==pageData)?"true":"false";
		}
		else
		if(parms.containsKey("QMPAGEERRORS"))
			return qPageErrors;
		else
		if(parms.containsKey("QMERRORS"))
			return qPageErrors;
		else
		if(parms.containsKey("QMTEMPLATE"))
			return qTemplate;
		else
		if(parms.containsKey("QMPAGE"))
			return qPageStr;
		else
		if(parms.containsKey("NEXT")||parms.containsKey("FINISH"))
		{
			if((qTemplate.length()>0)&&(CMath.s_int(qPageStr)<=0))
			{
				httpReq.addFakeUrlParameter("QMPAGE","1");
				httpReq.addFakeUrlParameter("QMERRORS","");
				httpReq.addFakeUrlParameter("QMPAGEERRORS","");
				return "";
			}
			if(qTemplate.length()==0)
				return "[error - no template chosen?!]";
			final DVector pageData=getPage(M,httpReq,qTemplate,qPageStr,null);
			if(pageData==null)
				return "[error - no page data?!]";
			final StringBuffer errors=new StringBuffer("");
			for(int step=1;step<pageData.size();step++)
			{
				final Integer stepType=(Integer)pageData.elementAt(step,1);
				final String keyName=(String)pageData.elementAt(step,2);
				final String defValue=(String)pageData.elementAt(step,3);
				String httpKeyName=keyName;
				if(httpKeyName.startsWith("$"))
					httpKeyName=httpKeyName.substring(1);
				final String keyNameFixed=CMStrings.capitalizeAndLower(httpKeyName.replace('_',' '));
				httpKeyName="AT_"+httpKeyName;
				final boolean optionalEntry=CMath.bset(stepType.intValue(),QuestManager.QM_COMMAND_OPTIONAL);
				final int inputCode=stepType.intValue()&QuestManager.QM_COMMAND_MASK;
				String oldValue=httpReq.getUrlParameter(httpKeyName);
				final QMCommand command = QMCommand.values()[inputCode];
				final GenericEditor.CMEval eval= CMLib.quests().getQuestCommandEval(command);
				try
				{
					switch(command)
					{
					case $TITLE:
						break;
					case $LABEL:
						break;
					case $HIDDEN:
						httpReq.addFakeUrlParameter(httpKeyName,defValue);
						break;
					case $ITEMXML_ONEORMORE:
					{
						final List<Item> rawitemlist=RoomData.contributeItems(new Vector<Item>());
						rawitemlist.addAll(getCatalogItemsForList(CMLib.catalog().getCatalogItems()));
						final Vector<String> oldValues=new Vector<String>();
						int which=1;
						oldValue=httpReq.getUrlParameter(httpKeyName+"_"+which);
						while(oldValue!=null)
						{
							if((!oldValue.equalsIgnoreCase("DELETE"))&&(oldValue.length()>0))
								oldValues.addElement(oldValue);
							which++;
							oldValue=httpReq.getUrlParameter(httpKeyName+"_"+which);
						}
						if(oldValues.size()==0)
							oldValues.addElement("");
						String newVal="";
						for(int i=0;i<oldValues.size();i++)
						{
							oldValue=oldValues.elementAt(i);
							Item I2=oldValue.length()>0?RoomData.getItemFromAnywhere(rawitemlist,oldValue):null;
							if(I2==null)
								I2=oldValue.length()>0?RoomData.getItemFromCatalog(oldValue):null;
							if(I2!=null)
							{
								if(CMLib.flags().isCataloged(I2))
									oldValue=CMLib.english().getContextSameName(rawitemlist,I2);
								else
									oldValue=CMLib.english().getContextName(rawitemlist,I2);
							}
							final Object[] choices=rawitemlist.toArray();
							final String thisVal=(String)eval.eval(oldValue,choices,optionalEntry);
							if(thisVal.length()>0)
							{
								final Item I3=(Item)CMLib.english().fetchEnvironmental(rawitemlist, thisVal, false);
								if(I3!=null)
								{
									if(CMLib.flags().isCataloged(I3))
										newVal+="CATALOG-"+I3.Name()+";";
									else
										newVal+=RoomData.getItemCode(rawitemlist, I3)+";";
								}
							}
						}
						httpReq.addFakeUrlParameter(httpKeyName,newVal);
						break;
					}
					case $ITEMXML:
					{
						final List<Item> rawitemlist=RoomData.contributeItems(new Vector<Item>());
						rawitemlist.addAll(getCatalogItemsForList(CMLib.catalog().getCatalogItems()));
						if(oldValue==null)
							oldValue="";
						Item I2=oldValue.length()>0?RoomData.getItemFromAnywhere(rawitemlist,oldValue):null;
						if(I2==null)
							I2=oldValue.length()>0?RoomData.getItemFromCatalog(oldValue):null;
						if(I2!=null)
						{
							if(CMLib.flags().isCataloged(I2))
								oldValue=CMLib.english().getContextSameName(rawitemlist,I2);
							else
								oldValue=CMLib.english().getContextName(rawitemlist,I2);
						}
						final Object[] choices=rawitemlist.toArray();
						String newVal=(String)eval.eval(oldValue,choices,optionalEntry);
						if(newVal.length()>0)
						{
							final Item I3=(Item)CMLib.english().fetchEnvironmental(rawitemlist, newVal, false);
							if(I3!=null)
							{
								if(CMLib.flags().isCataloged(I3))
									newVal="CATALOG-"+I3.Name()+";";
								else
									newVal=RoomData.getItemCode(rawitemlist, I3)+";";
							}
						}
						httpReq.addFakeUrlParameter(httpKeyName,newVal);
						break;
					}
					case $MOBXML_ONEORMORE:
					{
						final List<MOB> rawmoblist=RoomData.contributeMOBs(new Vector<MOB>());
						rawmoblist.addAll(getCatalogMobsForList(CMLib.catalog().getCatalogMobs()));
						final Vector<String> oldValues=new Vector<String>();
						int which=1;
						oldValue=httpReq.getUrlParameter(httpKeyName+"_"+which);
						while(oldValue!=null)
						{
							if((!oldValue.equalsIgnoreCase("DELETE"))&&(oldValue.length()>0))
								oldValues.addElement(oldValue);
							which++;
							oldValue=httpReq.getUrlParameter(httpKeyName+"_"+which);
						}
						if(oldValues.size()==0)
							oldValues.addElement("");
						String newVal="";
						for(int i=0;i<oldValues.size();i++)
						{
							oldValue=oldValues.elementAt(i);
							MOB M2=oldValue.length()>0?RoomData.getMOBFromCode(rawmoblist,oldValue):null;
							if(M2==null)
								M2=oldValue.length()>0?RoomData.getMOBFromCatalog(oldValue):null;
							if(M2!=null)
							{
								if(CMLib.flags().isCataloged(M2))
									oldValue=CMLib.english().getContextSameName(rawmoblist,M2);
								else
									oldValue=CMLib.english().getContextName(rawmoblist,M2);
							}
							final Object[] choices=rawmoblist.toArray();
							final String thisVal=(String)eval.eval(oldValue,choices,optionalEntry);
							if(thisVal.length()>0)
							{
								final MOB M3=(MOB)CMLib.english().fetchEnvironmental(rawmoblist, thisVal, false);
								if(M3!=null)
								{
									if(CMLib.flags().isCataloged(M3))
										newVal+="CATALOG-"+M3.Name()+";";
									else
										newVal+=RoomData.getMOBCode(rawmoblist, M3)+";";
								}
							}
						}
						httpReq.addFakeUrlParameter(httpKeyName,newVal);
						break;
					}
					case $MOBXML:
					{
						final List<MOB> rawmoblist=RoomData.contributeMOBs(new Vector<MOB>());
						rawmoblist.addAll(getCatalogMobsForList(CMLib.catalog().getCatalogMobs()));
						if(oldValue==null)
							oldValue="";
						MOB M2=oldValue.length()>0?RoomData.getMOBFromCode(rawmoblist,oldValue):null;
						if(M2==null)
							M2=oldValue.length()>0?RoomData.getMOBFromCatalog(oldValue):null;
						if(M2!=null)
						{
							if(CMLib.flags().isCataloged(M2))
								oldValue=CMLib.english().getContextSameName(rawmoblist,M2);
							else
								oldValue=CMLib.english().getContextName(rawmoblist,M2);
						}
						final Object[] choices=rawmoblist.toArray();
						String newVal=(String)eval.eval(oldValue,choices,optionalEntry);
						if(newVal.length()>0)
						{
							final MOB M3=(MOB)CMLib.english().fetchEnvironmental(rawmoblist, newVal, false);
							if(M3!=null)
							{
								if(CMLib.flags().isCataloged(M3))
									newVal="CATALOG-"+M3.Name()+";";
								else
									newVal=RoomData.getMOBCode(rawmoblist, M3)+";";
							}
						}
						httpReq.addFakeUrlParameter(httpKeyName,newVal);
						break;
					}
					case $CHOOSE:
					{
						if(oldValue==null)
							oldValue="";
						final Object[] choices=CMParms.parseCommas(defValue.toUpperCase(),true).toArray();
						final String newVal=(String)eval.eval(oldValue,choices,optionalEntry);
						httpReq.addFakeUrlParameter(httpKeyName,newVal);
						break;
					}
					default:
					{
						if(oldValue==null)
							oldValue="";
						final String newVal=(String)eval.eval(oldValue,null,optionalEntry);
						httpReq.addFakeUrlParameter(httpKeyName,newVal);
						break;
					}
					}
				}
				catch(final CMException e)
				{
					errors.append("Error in field '"+keyNameFixed+"': "+e.getMessage()+"<BR>");
				}
			}
			httpReq.addFakeUrlParameter("QMPAGEERRORS",errors.toString());
			if(errors.toString().length()>0)
				return "";
			if(parms.containsKey("FINISH"))
			{
				String name="";
				final DVector filePages=(DVector)httpReq.getRequestObjects().get("QM_FILE_PAGES");
				String script=((StringBuffer)filePages.elementAt(0,5)).toString();
				String var=null;
				String val=null;
				final List<DVector> qPages=(List<DVector>)filePages.elementAt(0,4);
				for(int page=0;page<qPages.size();page++)
				{
					final DVector pageDV=qPages.get(page);
					for(int v=0;v<pageDV.size();v++)
					{
						var=(String)pageDV.elementAt(v,2);
						String httpKeyName=var;
						if(httpKeyName.startsWith("$"))
							httpKeyName=httpKeyName.substring(1);
						else
							continue;
						httpKeyName="AT_"+httpKeyName;
						val=httpReq.getUrlParameter(httpKeyName);
						if(val==null)
							val="";
						final int codeNum = ((Integer)pageDV.elementAt(v,1)).intValue()&QuestManager.QM_COMMAND_MASK;
						final QMCommand command = QMCommand.values()[codeNum];
						switch(command)
						{
							case $UNIQUE_QUEST_NAME:
								name=val;
								break;
							case $ITEMXML:
							case $ITEMXML_ONEORMORE:
							{
								final List<String> V=CMParms.parseSemicolons(val,true);
								val="";
								for(int v1=0;v1<V.size();v1++)
								{
									Item I=RoomData.getItemFromCode(RoomData.getItemCache(),V.get(v1));
									if(I==null)
										I=RoomData.getItemFromAnywhere(RoomData.getItemCache(),V.get(v1));
									if(I==null)
										I=RoomData.getItemFromCatalog(V.get(v1));
									if(I!=null)
										val+=CMLib.coffeeMaker().getItemXML(I).toString();
								}
								break;
							}
							case $MOBXML:
							case $MOBXML_ONEORMORE:
							{
								final List<String> V=CMParms.parseSemicolons(val,true);
								val="";
								for(int v1=0;v1<V.size();v1++)
								{
									MOB M2=RoomData.getMOBFromCode(RoomData.getMOBCache(),V.get(v1));
									if(M2==null)
										M2=RoomData.getMOBFromCatalog(V.get(v1));
									if(M2!=null)
										val+=CMLib.coffeeMaker().getMobXML(M2).toString();
								}
								break;
							}
						default:
							break;
						}
						script=CMStrings.replaceAll(script,var,val);
					}
				}
				script=CMStrings.replaceAll(script,"$#AUTHOR",M.Name());
				final Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
				final CMFile newQF=new CMFile(Resources.makeFileResourceName("quests/"+name+".quest"),M,CMFile.FLAG_LOGERRORS);
				if(!newQF.saveText(script))
				{
					httpReq.addFakeUrlParameter("QMPAGEERRORS","Unable to save your quest.  Please consult the log.");
					return "";
				}
				Q.setScript("LOAD=quests/"+name+".quest",true);
				if((Q.name().trim().length()==0)||(Q.duration()<0))
				{
					httpReq.addFakeUrlParameter("QMPAGEERRORS","Unable to create your quest.  Please consult the log.");
					return "";
				}
				final Quest badQ=CMLib.quests().fetchQuest(name);
				if(badQ!=null)
				{
					httpReq.addFakeUrlParameter("QMPAGEERRORS","Unable to create your quest.  One of that name already exists!");
					return "";
				}
				Log.sysOut("QuestMgr",M.Name()+" created quest '"+Q.name()+"'");
				CMLib.quests().addQuest(Q);
				CMLib.quests().save();
			}
			httpReq.addFakeUrlParameter("QMPAGE",""+(CMath.s_int(qPageStr)+1));
			return "";
		}
		else
		if(parms.containsKey("BACK"))
		{
			final int pageNumber=CMath.s_int(qPageStr);
			if(pageNumber>1)
				httpReq.addFakeUrlParameter("QMPAGE",""+(CMath.s_int(qPageStr)-1));
			else
			{
				httpReq.addFakeUrlParameter("QMTEMPLATE","");
				httpReq.addFakeUrlParameter("QMPAGE","");
			}
		}
		return "";
	}
}
