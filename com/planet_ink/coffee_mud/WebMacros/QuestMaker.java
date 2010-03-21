package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class QuestMaker extends StdWebMacro
{
    public String name()    {return "QuestMaker";}

    public DVector getPage(MOB mob, ExternalHTTPRequests httpReq, String template, String page, String fileToGet)
    {
    	DVector pageList=(DVector)httpReq.getRequestObjects().get("QM_PAGE_LIST");
    	DVector filePages=(DVector)httpReq.getRequestObjects().get("QM_FILE_PAGES");
    	if(template.length()==0)
    	{ 
    		httpReq.removeRequestParameter("QM_FILE_PAGES"); 
    		filePages=null;
    		if(pageList!=null) return pageList;
    		pageList=CMLib.quests().getQuestTemplate(mob, fileToGet);
    		httpReq.getRequestObjects().put("QM_PAGE_LIST",pageList);
    		return pageList;
    	}
        
		int pageNumber=CMath.s_int(page)-1;
		if(filePages==null)
		{
			filePages=CMLib.quests().getQuestTemplate(mob, template);
    		httpReq.getRequestObjects().put("QM_FILE_PAGES",filePages);
		}
        Vector qPages=(Vector)filePages.elementAt(0,4);
        if(pageNumber<=0) return (DVector)qPages.firstElement();
        if(pageNumber>=qPages.size()) return (DVector)qPages.lastElement();
        return (DVector)qPages.elementAt(pageNumber);
    }

    private String itemList(Vector itemList, Item oldItem, String oldValue)
    {
        StringBuffer list=new StringBuffer("");
        if(oldItem==null) oldItem=RoomData.getItemFromCatalog(oldValue);
        for(int o=0;o<itemList.size();o++)
        {
            Item I=(Item)itemList.elementAt(o);
            list.append("<OPTION VALUE=\""+RoomData.getItemCode(itemList, I)+"\" ");
            if((oldItem!=null)&&(oldItem.sameAs(I)))
                list.append("SELECTED");
            list.append(">");
            list.append(I.Name()+" ("+I.ID()+")");
        }
        list.append("<OPTION VALUE=\"\">------ CATALOGED -------");
        String[] names=CMLib.catalog().getCatalogItemNames();
        for(int i=0;i<names.length;i++)
        {
            list.append("<OPTION VALUE=\"CATALOG-"+names[i]+"\"");
            if((oldItem!=null)
            &&(CMLib.flags().isCataloged(oldItem))
            &&(oldItem.Name().equalsIgnoreCase(names[i])))
                list.append(" SELECTED");
            list.append(">"+names[i]);
        }
        return list.toString();
    }
    
    public void addCatalogList(Vector toList, Environmental[] fromList)
    {
        for(int m=0;m<fromList.length;m++)
        {
            Environmental E=(Environmental)fromList[m];
            E=(Environmental)E.copyOf();
            CMLib.catalog().changeCatalogUsage(E,true);
            toList.addElement(E);
        }
    }
    
    private String mobList(Vector mobList, MOB oldMob, String oldValue)
    {
        StringBuffer list=new StringBuffer("");
        if(oldMob==null) oldMob=RoomData.getMOBFromCatalog(oldValue);
        for(int o=0;o<mobList.size();o++)
        {
            MOB M2=(MOB)mobList.elementAt(o);
            list.append("<OPTION VALUE=\""+RoomData.getMOBCode(mobList, M2)+"\" ");
            if((oldMob!=null)&&(oldMob.sameAs(M2)))
                list.append("SELECTED");
            list.append(">");
            list.append(M2.Name()+" ("+M2.ID()+")");
        }
        list.append("<OPTION VALUE=\"\">------ CATALOGED -------");
        String[] names=CMLib.catalog().getCatalogMobNames();
        for(int m=0;m<names.length;m++)
        {
            list.append("<OPTION VALUE=\"CATALOG-"+names[m]+"\"");
            if((oldMob!=null)
            &&(CMLib.flags().isCataloged(oldMob))
            &&(oldMob.Name().equalsIgnoreCase(names[m])))
                list.append(" SELECTED");
            list.append(">"+names[m]);
        }
        return list.toString();
    }
    
    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        if((parms==null)||(parms.size()==0)) return "";
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null) return "[error -- no authenticated mob!]";
		
        String qFileToGet=null;
        if(parms.containsKey("QMFILETOGET"))
            qFileToGet=(String)parms.get("QMFILETOGET");
        
        String qTemplate=httpReq.getRequestParameter("QMTEMPLATE");
        if((qTemplate==null)||(qTemplate.length()==0)) qTemplate="";
        
        String qPageStr=httpReq.getRequestParameter("QMPAGE");
        if((qPageStr==null)||(qPageStr.length()==0)) qPageStr="";
        
        String qPageErrors=httpReq.getRequestParameter("QMPAGEERRORS");
        if((qPageErrors==null)||(qPageErrors.length()==0)) qPageErrors="";
        
        if(parms.containsKey("QMPAGETITLE"))
        {
        	DVector pageData=getPage(M,httpReq,qTemplate,qPageStr,null);
        	if(pageData==null) return "[error -- no page selected!]";
            return (String)pageData.elementAt(0,2);
        }
        else
        if(parms.containsKey("QMPAGEINSTR"))
        {
        	DVector pageData=getPage(M,httpReq,qTemplate,qPageStr,null);
        	if(pageData==null) return "[error -- no page selected!]";
            return (String)pageData.elementAt(0,3);
        }
        else
        if(parms.containsKey("QMPAGEFIELDS"))
        {
        	DVector pageData=getPage(M,httpReq,qTemplate,qPageStr,qFileToGet);
        	if(pageData==null) return "[error - no page data?!]";
    		String labelColor=(String)parms.get("LABELCOLOR");
    		if(labelColor==null) labelColor="<FONT COLOR=YELLOW><B>";
    		String descColor=(String)parms.get("DESCCOLOR");
    		if(descColor==null) descColor="<FONT COLOR=WHITE><I>";
    		StringBuffer list=new StringBuffer("");
        	if(qTemplate.length()==0)
        	{
        		String oldTemplate=(String)httpReq.getRequestParameter("QMOLDTEMPLATE");
                if((oldTemplate==null)||(oldTemplate.length()==0)) oldTemplate="";
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

            Vector V=httpReq.getAllRequestParameterKeys("^AT_(.+)");
			list.append("<TR><TD COLSPAN=2>");
            for(int v=0;v<V.size();v++)
            {
                String key=(String)V.elementAt(v);
                if((!key.startsWith("AT_")))
                    continue;
                boolean thisPage=false;
                for(int step=1;step<pageData.size();step++)
                {
                    String keyName=(String)pageData.elementAt(step,2);
                    if(keyName.startsWith("$")
                    &&(key.substring(3).toUpperCase().equals(keyName.substring(1))
                		||(key.substring(3).toUpperCase().startsWith(keyName.substring(1)+"_")
                				&&CMath.isNumber(key.substring(3+keyName.length())))))
                    { thisPage=true; break;}
                }
                if(thisPage) continue;
                String oldVal=(String)httpReq.getRequestParameter(key);
                if(oldVal==null) oldVal="";
                list.append("<INPUT TYPE=HIDDEN NAME="+key+" VALUE=\""+htmlOutgoingFilter(oldVal)+"\">\n\r");
            }
            list.append("</TD></TR>\n\r");
            
            String lastLabel=null;
            for(int step=1;step<pageData.size();step++)
            {
                Integer stepType=(Integer)pageData.elementAt(step,1);
                String keyName=(String)pageData.elementAt(step,2);
                String defValue=(String)pageData.elementAt(step,3);
                String httpKeyName=keyName;
                if(httpKeyName.startsWith("$")) httpKeyName=httpKeyName.substring(1);
                String keyNameFixed=CMStrings.capitalizeAndLower(httpKeyName.replace('_',' '));
                httpKeyName="AT_"+httpKeyName;
                boolean optionalEntry=CMath.bset(stepType.intValue(),QuestManager.QM_COMMAND_OPTIONAL);
                int inputCode=stepType.intValue()&QuestManager.QM_COMMAND_MASK;
    			String oldValue=(String)httpReq.getRequestParameter(httpKeyName);
                switch(inputCode)
                {
                case QuestManager.QM_COMMAND_$TITLE: break;
                case QuestManager.QM_COMMAND_$LABEL: lastLabel=defValue; break;
                case QuestManager.QM_COMMAND_$EXPRESSION:
                case QuestManager.QM_COMMAND_$TIMEEXPRESSION:
                case QuestManager.QM_COMMAND_$UNIQUE_QUEST_NAME:
                {
        			if(oldValue==null) oldValue=defValue;
        			list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
        			list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
        			list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
        			list.append("<TD><INPUT TYPE=TEXT SIZE=20 NAME="+httpKeyName+" ");
        			list.append(" VALUE=\""+htmlOutgoingFilter(oldValue)+"\"></TD></TR>");
                	break;
                }
                case QuestManager.QM_COMMAND_$LONG_STRING:
                {
        			if(oldValue==null) oldValue=defValue;
        			list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
        			list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
        			list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
        			list.append("<TD><TEXTAREA ROWS=3 COLS=40 NAME="+httpKeyName+">");
        			list.append(oldValue+"</TEXTAREA></TD></TR>");
                	break;
                }
                case QuestManager.QM_COMMAND_$ZAPPERMASK:
                {
                    if(oldValue==null) oldValue=defValue;
                    list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
                    list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
                    list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
                    list.append("<TD><TEXTAREA COLS=40 ROWS=2 NAME="+httpKeyName+">");
                    list.append(oldValue+"</TEXTAREA></TD></TR>");
                    break;
                }
                case QuestManager.QM_COMMAND_$STRING:
                case QuestManager.QM_COMMAND_$ROOMID:
                case QuestManager.QM_COMMAND_$NAME:
                case QuestManager.QM_COMMAND_$AREA:
                {
        			if(oldValue==null) oldValue=defValue;
        			list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
        			list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
        			list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
        			list.append("<TD><INPUT TYPE=TEXT SIZE=40 NAME="+httpKeyName+" ");
        			list.append(" VALUE=\""+htmlOutgoingFilter(oldValue)+"\"></TD></TR>");
                	break;
                }
                case QuestManager.QM_COMMAND_$HIDDEN:
                	break;
                case QuestManager.QM_COMMAND_$ABILITY:
                {
        			if(oldValue==null) oldValue=defValue;
        			list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
        			list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
        			list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
        			list.append("<TD><SELECT NAME="+httpKeyName+">");
        			if(optionalEntry) list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
        			Ability A=null;
        			for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
        			{
        				A=(Ability)e.nextElement();
            			list.append("<OPTION VALUE=\""+A.ID()+"\" ");
        				if(oldValue.equals(A.ID())) list.append("SELECTED");
        				list.append(">");
            			list.append(A.ID());
        			}
        			list.append("</SELECT>");
        			list.append("</TD></TR>");
                	break;
                }
                case QuestManager.QM_COMMAND_$EXISTING_QUEST_NAME:
                {
        			if(oldValue==null) oldValue=defValue;
        			list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
        			list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
        			list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
        			list.append("<TD><SELECT NAME="+httpKeyName+">");
        			if(optionalEntry) list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
        			for(int q=0;q<CMLib.quests().numQuests();q++)
        			{
        				Quest Q2=CMLib.quests().fetchQuest(q);
            			list.append("<OPTION VALUE=\""+Q2.name()+"\" ");
        				if(oldValue.equals(Q2.name())) list.append("SELECTED");
        				list.append(">");
            			list.append(Q2.name());
        			}
        			list.append("</SELECT>");
        			list.append("</TD></TR>");
                	break;
                }
                case QuestManager.QM_COMMAND_$CHOOSE:
                {
        			list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
        			list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
        			list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
        			list.append("<TD><SELECT NAME="+httpKeyName+">");
        			Vector <String>options=CMParms.parseCommas(defValue.toUpperCase(),true);
        			if(optionalEntry) options.insertElementAt("",0);
        			for(int o=0;o<options.size();o++)
        			{
        				String val=(String)options.elementAt(o);
            			list.append("<OPTION VALUE=\""+val+"\" ");
        				if(val.equalsIgnoreCase(oldValue)) list.append("SELECTED");
            			list.append(">");
            			list.append(val);
        			}
        			list.append("</SELECT></TD></TR>");
                	break;
                }
                case QuestManager.QM_COMMAND_$ITEMXML:
                {
                    if(oldValue==null) oldValue=defValue;
                    Vector itemList=new Vector();
    				itemList=RoomData.contributeItems((Vector)itemList.clone());
                    Item oldItem=RoomData.getItemFromAnywhere(itemList,oldValue);
        			list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
        			list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
        			list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
        			list.append("<TD><SELECT NAME="+httpKeyName+">");
        			if(optionalEntry) list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
        			list.append(itemList(itemList,oldItem,oldValue));
        			list.append("</SELECT>");
        			list.append("<INPUT TYPE=BUTTON NAME=BUTT_"+httpKeyName+" VALUE=\"NEW\" ONCLICK=\"AddNewItem();\">");
        			list.append("</TD></TR>");
                	break;
                }
                case QuestManager.QM_COMMAND_$ITEMXML_ONEORMORE:
                {
                    if(oldValue==null) oldValue=defValue;
                    Vector itemList=new Vector();
    				itemList=RoomData.contributeItems((Vector)itemList.clone());
                    Vector oldValues=new Vector();
                    int which=1;
                    oldValue=(String)httpReq.getRequestParameter(httpKeyName+"_"+which);
                    while(oldValue!=null)
                    {
                    	if((!oldValue.equalsIgnoreCase("DELETE"))&&(oldValue.length()>0))
                        	oldValues.addElement(oldValue);
                    	which++;
                        oldValue=(String)httpReq.getRequestParameter(httpKeyName+"_"+which);
                    }
                    oldValues.addElement("");
                    for(int i=0;i<oldValues.size();i++)
                    {
                    	oldValue=(String)oldValues.elementAt(i);
	                    Item oldItem=(oldValue.length()>0)?RoomData.getItemFromAnywhere(itemList,oldValue):null;
	                    if(i==0)
	                    {
		        			list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
		        			list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
	                    }
	        			list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
	        			list.append("<TD><SELECT NAME="+httpKeyName+"_"+(i+1)+" ONCHANGE=\"Refresh();\">");
                        if(i<oldValues.size()-1)  list.append("<OPTION VALUE=\"DELETE\">Delete!");
                        if(oldValue.length()==0) list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
                        list.append(itemList(itemList,oldItem,oldValue));
	        			list.append("</SELECT>");
	        			if(i==oldValues.size()-1)
		        			list.append("<INPUT TYPE=BUTTON NAME=BUTT_"+httpKeyName+" VALUE=\"NEW\" ONCLICK=\"AddNewItem();\">");
	        			list.append("</TD></TR>");
                    }
                	break;
                }
                case QuestManager.QM_COMMAND_$MOBXML:
                {
                    if(oldValue==null) oldValue=defValue;
                    Vector mobList=new Vector();
                    mobList=RoomData.contributeMOBs((Vector)mobList.clone());
                    MOB oldMob=RoomData.getMOBFromCode(mobList,oldValue);
                    list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
                    list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
                    list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
                    list.append("<TD><SELECT NAME="+httpKeyName+">");
                    if(optionalEntry) list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
                    list.append(mobList(mobList,oldMob,oldValue));
                    list.append("</SELECT>");
                    list.append("<INPUT TYPE=BUTTON NAME=BUTT_"+httpKeyName+" VALUE=\"NEW\" ONCLICK=\"AddNewMob();\">");
                    list.append("</TD></TR>");
                	break;
                }
                case QuestManager.QM_COMMAND_$MOBXML_ONEORMORE:
                {
                    if(oldValue==null) oldValue=defValue;
                    Vector mobList=new Vector();
                    mobList=RoomData.contributeMOBs((Vector)mobList.clone());
                    Vector oldValues=new Vector();
                    int which=1;
                    oldValue=(String)httpReq.getRequestParameter(httpKeyName+"_"+which);
                    while(oldValue!=null)
                    {
                    	if((!oldValue.equalsIgnoreCase("DELETE"))&&(oldValue.length()>0))
                        	oldValues.addElement(oldValue);
                    	which++;
                        oldValue=(String)httpReq.getRequestParameter(httpKeyName+"_"+which);
                    }
                    oldValues.addElement("");
                    for(int i=0;i<oldValues.size();i++)
                    {
                    	oldValue=(String)oldValues.elementAt(i);
	                    MOB oldMob=(oldValue.length()>0)?RoomData.getMOBFromCode(mobList,oldValue):null;
	                    if(i==0)
	                    {
		                    list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
	                    	list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
	                    }
	                    list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
	                    list.append("<TD><SELECT NAME="+httpKeyName+"_"+(i+1)+" ONCHANGE=\"Refresh();\">");
	                    if(i<oldValues.size()-1)  list.append("<OPTION VALUE=\"DELETE\">Delete!");
	                    if(oldValue.length()==0) list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
	                    list.append(mobList(mobList,oldMob,oldValue));
	                    list.append("</SELECT>");
	                    if(i==oldValues.size()-1)
		                    list.append("<INPUT TYPE=BUTTON NAME=BUTT_"+httpKeyName+" VALUE=\"NEW\" ONCLICK=\"AddNewMob();\">");
	                    list.append("</TD></TR>");
                    }
                	break;
                }
                case QuestManager.QM_COMMAND_$FACTION:
                {
                    if(oldValue==null) oldValue=defValue;
                    list.append("<TR><TD COLSPAN=2><BR></TD></TR>\n\r");
                    list.append("<TR><TD COLSPAN=2>"+descColor+lastLabel+"</B></FONT></I></TD></TR>\n\r");
                    list.append("<TR><TD>"+labelColor+keyNameFixed+"</B></FONT></I></TD>");
                    list.append("<TD><SELECT NAME="+httpKeyName+">");
                    if(optionalEntry) list.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
                    for(Enumeration f=CMLib.factions().factions();f.hasMoreElements();)
                    {
                        Faction F=(Faction)f.nextElement();
                        String fkey=F.factionID().toUpperCase().trim();
                        list.append("<OPTION VALUE=\""+fkey+"\" ");
                        if(oldValue.equals(fkey)) list.append("SELECTED");
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
        	DVector pageData=getPage(M,httpReq,qTemplate,qPageStr,null);
        	if(pageData==null) return "false";
        	DVector filePages=(DVector)httpReq.getRequestObjects().get("QM_FILE_PAGES");
            if(filePages==null) return "false";
        	return(((Vector)filePages.elementAt(0,4)).lastElement()==pageData)?"true":"false";
        }
        else
        if(parms.containsKey("QMPAGEERRORS")) return qPageErrors;
        else
        if(parms.containsKey("QMERRORS")) return qPageErrors;
        else
        if(parms.containsKey("QMTEMPLATE")) return qTemplate;
        else
        if(parms.containsKey("QMPAGE")) return qPageStr;
        else
        if(parms.containsKey("NEXT")||parms.containsKey("FINISH"))
        {
        	if((qTemplate.length()>0)&&(CMath.s_int(qPageStr)<=0))
        	{
        		httpReq.addRequestParameters("QMPAGE","1");
        		httpReq.addRequestParameters("QMERRORS","");
        		httpReq.addRequestParameters("QMPAGEERRORS","");
        		return "";
        	}
        	if(qTemplate.length()==0) return "[error - no template chosen?!]";
        	DVector pageData=getPage(M,httpReq,qTemplate,qPageStr,null);
        	if(pageData==null) return "[error - no page data?!]";
            StringBuffer errors=new StringBuffer("");
            for(int step=1;step<pageData.size();step++)
            {
                Integer stepType=(Integer)pageData.elementAt(step,1);
                String keyName=(String)pageData.elementAt(step,2);
                String defValue=(String)pageData.elementAt(step,3);
                String httpKeyName=keyName;
                if(httpKeyName.startsWith("$")) httpKeyName=httpKeyName.substring(1);
                String keyNameFixed=CMStrings.capitalizeAndLower(httpKeyName.replace('_',' '));
                httpKeyName="AT_"+httpKeyName;
                boolean optionalEntry=CMath.bset(stepType.intValue(),QuestManager.QM_COMMAND_OPTIONAL);
                int inputCode=stepType.intValue()&QuestManager.QM_COMMAND_MASK;
                String oldValue=(String)httpReq.getRequestParameter(httpKeyName);
                GenericEditor.CMEval eval= QuestManager.QM_COMMAND_TESTS[inputCode];
                try
                {
                    switch(inputCode)
                    {
                    case QuestManager.QM_COMMAND_$TITLE: break;
                    case QuestManager.QM_COMMAND_$LABEL: break;
                    case QuestManager.QM_COMMAND_$HIDDEN: 
                        httpReq.addRequestParameters(httpKeyName,defValue);
                    	break;
                    case QuestManager.QM_COMMAND_$ITEMXML_ONEORMORE: 
                    {
                        Vector rawitemlist=RoomData.contributeItems(new Vector());
                        addCatalogList(rawitemlist,CMLib.catalog().getCatalogItems());
                        Vector oldValues=new Vector();
                        int which=1;
                        oldValue=(String)httpReq.getRequestParameter(httpKeyName+"_"+which);
                        while(oldValue!=null)
                        {
                        	if((!oldValue.equalsIgnoreCase("DELETE"))&&(oldValue.length()>0))
	                        	oldValues.addElement(oldValue);
                        	which++;
                            oldValue=(String)httpReq.getRequestParameter(httpKeyName+"_"+which);
                        }
                        if(oldValues.size()==0) oldValues.addElement("");
                        String newVal="";
                        for(int i=0;i<oldValues.size();i++)
                        {
                        	oldValue=(String)oldValues.elementAt(i);
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
	                        Object[] choices=rawitemlist.toArray();
	                        String thisVal=(String)eval.eval(oldValue,choices,optionalEntry);
	                        if(thisVal.length()>0)
	                        {
	                        	Item I3=(Item)CMLib.english().fetchEnvironmental(rawitemlist, thisVal, false);
	                        	if(I3!=null)
	                        	{
	                        	    if(CMLib.flags().isCataloged(I3))
                                        newVal+="CATALOG-"+I3.Name()+";";
	                        	    else
    	                        	    newVal+=RoomData.getItemCode(rawitemlist, I3)+";";
	                        	}
	                        }
                        }
                        httpReq.addRequestParameters(httpKeyName,newVal);
                        break;
                    }
                    case QuestManager.QM_COMMAND_$ITEMXML: 
                    {
                        Vector rawitemlist=RoomData.contributeItems(new Vector());
                        addCatalogList(rawitemlist,CMLib.catalog().getCatalogItems());
                        if(oldValue==null) oldValue="";
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
                        Object[] choices=rawitemlist.toArray();
                        String newVal=(String)eval.eval(oldValue,choices,optionalEntry);
                        if(newVal.length()>0)
                        {
                        	Item I3=(Item)CMLib.english().fetchEnvironmental(rawitemlist, newVal, false);
                            if(I3!=null)
                            {
                                if(CMLib.flags().isCataloged(I3))
                                    newVal="CATALOG-"+I3.Name()+";";
                                else
                                    newVal=RoomData.getItemCode(rawitemlist, I3)+";";
                            }
                        }
                        httpReq.addRequestParameters(httpKeyName,newVal);
                        break;
                    }
                    case QuestManager.QM_COMMAND_$MOBXML_ONEORMORE:
                    {
                        Vector rawmoblist=RoomData.contributeMOBs(new Vector());
                        addCatalogList(rawmoblist,CMLib.catalog().getCatalogMobs());
                        Vector oldValues=new Vector();
                        int which=1;
                        oldValue=(String)httpReq.getRequestParameter(httpKeyName+"_"+which);
                        while(oldValue!=null)
                        {
                        	if((!oldValue.equalsIgnoreCase("DELETE"))&&(oldValue.length()>0))
	                        	oldValues.addElement(oldValue);
                        	which++;
                            oldValue=(String)httpReq.getRequestParameter(httpKeyName+"_"+which);
                        }
                        if(oldValues.size()==0) oldValues.addElement("");
                        String newVal="";
                        for(int i=0;i<oldValues.size();i++)
                        {
                        	oldValue=(String)oldValues.elementAt(i);
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
	                        Object[] choices=rawmoblist.toArray();
	                        String thisVal=(String)eval.eval(oldValue,choices,optionalEntry);
	                        if(thisVal.length()>0)
	                        {
	                        	MOB M3=(MOB)CMLib.english().fetchEnvironmental(rawmoblist, thisVal, false);
	                        	if(M3!=null)
	                        	{
                                    if(CMLib.flags().isCataloged(M3))
                                        newVal+="CATALOG-"+M3.Name()+";";
                                    else
    	                        	    newVal+=RoomData.getMOBCode(rawmoblist, M3)+";";
	                        	}
	                        }
                        }
                        httpReq.addRequestParameters(httpKeyName,newVal);
                        break;
                    }
                    case QuestManager.QM_COMMAND_$MOBXML: 
                    {
                        Vector rawmoblist=RoomData.contributeMOBs(new Vector());
                        addCatalogList(rawmoblist,CMLib.catalog().getCatalogMobs());
                        if(oldValue==null) oldValue="";
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
                        Object[] choices=rawmoblist.toArray();
                        String newVal=(String)eval.eval(oldValue,choices,optionalEntry);
                        if(newVal.length()>0)
                        {
                        	MOB M3=(MOB)CMLib.english().fetchEnvironmental(rawmoblist, newVal, false);
                            if(M3!=null)
                            {
                                if(CMLib.flags().isCataloged(M3))
                                    newVal="CATALOG-"+M3.Name()+";";
                                else
                                    newVal=RoomData.getMOBCode(rawmoblist, M3)+";";
                            }
                        }
                        httpReq.addRequestParameters(httpKeyName,newVal);
                        break;
                    }
                    case QuestManager.QM_COMMAND_$CHOOSE:
                    {
                        if(oldValue==null) oldValue="";
                        Object[] choices=CMParms.parseCommas(defValue.toUpperCase(),true).toArray();
                        String newVal=(String)eval.eval(oldValue,choices,optionalEntry);
                        httpReq.addRequestParameters(httpKeyName,newVal);
                        break;
                    }
                    default:
                    {
                        if(oldValue==null) oldValue="";
                        String newVal=(String)eval.eval(oldValue,null,optionalEntry);
                        httpReq.addRequestParameters(httpKeyName,newVal);
                        break;
                    }
                    }
                }
                catch(CMException e)
                {
                    errors.append("Error in field '"+keyNameFixed+"': "+e.getMessage()+"<BR>");
                }
            }
            httpReq.addRequestParameters("QMPAGEERRORS",errors.toString());
            if(errors.toString().length()>0) return "";
            if(parms.containsKey("FINISH"))
            {
                String name="";
            	DVector filePages=(DVector)httpReq.getRequestObjects().get("QM_FILE_PAGES");
                String script=((StringBuffer)filePages.elementAt(0,5)).toString();
                String var=null;
                String val=null;
                Vector qPages=(Vector)filePages.elementAt(0,4);
                for(int page=0;page<qPages.size();page++)
                {
                    DVector pageDV=(DVector)qPages.elementAt(page);
                    for(int v=0;v<pageDV.size();v++)
                    {
                        var=(String)pageDV.elementAt(v,2);
                        String httpKeyName=var;
                        if(httpKeyName.startsWith("$")) 
                            httpKeyName=httpKeyName.substring(1);
                        else
                            continue;
                        httpKeyName="AT_"+httpKeyName;
                        val=(String)httpReq.getRequestParameter(httpKeyName);
                        if(val==null) val="";
                        switch(((Integer)pageDV.elementAt(v,1)).intValue()&QuestManager.QM_COMMAND_MASK)
                        {
                        	case QuestManager.QM_COMMAND_$UNIQUE_QUEST_NAME:
                                name=val;
                        		break;
                            case QuestManager.QM_COMMAND_$ITEMXML:
                        	case QuestManager.QM_COMMAND_$ITEMXML_ONEORMORE:
                        	{
                        		Vector V=CMParms.parseSemicolons(val,true);
                        		val="";
                        		for(int v1=0;v1<V.size();v1++)
                        		{
                                    Item I=RoomData.getItemFromCode(RoomData.items,(String)V.elementAt(v1));
                                    if(I==null) I=RoomData.getItemFromAnywhere(RoomData.items,(String)V.elementAt(v1));
                                    if(I==null) I=RoomData.getItemFromCatalog((String)V.elementAt(v1));
                                    if(I!=null)
                                        val+=CMLib.coffeeMaker().getItemXML(I).toString();
                        		}
                        		break;
                        	}
                            case QuestManager.QM_COMMAND_$MOBXML:
                        	case QuestManager.QM_COMMAND_$MOBXML_ONEORMORE:
                        	{
                        		Vector V=CMParms.parseSemicolons(val,true);
                        		val="";
                        		for(int v1=0;v1<V.size();v1++)
                        		{
                                    MOB M2=RoomData.getMOBFromCode(RoomData.mobs,(String)V.elementAt(v1));
                                    if(M2==null) M2=RoomData.getMOBFromCatalog((String)V.elementAt(v1));
                                    if(M2!=null)
                                        val+=CMLib.coffeeMaker().getMobXML(M2).toString();
                        		}
                        		break;
                        	}
                        }
                        script=CMStrings.replaceAll(script,var,val);
                    }
                }
                script=CMStrings.replaceAll(script,"$#AUTHOR",M.Name());
                Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
                CMFile newQF=new CMFile(Resources.makeFileResourceName("quests/"+name+".quest"),M,true,false);
                newQF.saveText(script);
                Q.setScript("LOAD=quests/"+name+".quest");
                if((Q.name().trim().length()==0)||(Q.duration()<0))
                {
                    httpReq.addRequestParameters("QMPAGEERRORS","Unable to create your quest.  Please consult the log.");
                    return "";
                }
                Quest badQ=CMLib.quests().fetchQuest(name);
                if(badQ!=null)
                {
                    httpReq.addRequestParameters("QMPAGEERRORS","Unable to create your quest.  One of that name already exists!");
                    return "";
                }
                Log.sysOut("QuestMgr",M.Name()+" created quest '"+Q.name()+"'");
                CMLib.quests().addQuest(Q);
                CMLib.quests().save();
            }
    		httpReq.addRequestParameters("QMPAGE",""+(CMath.s_int(qPageStr)+1));
        	return "";
        }
        else
        if(parms.containsKey("BACK"))
        {
            int pageNumber=CMath.s_int(qPageStr);
            if(pageNumber>1)
                httpReq.addRequestParameters("QMPAGE",""+(CMath.s_int(qPageStr)-1));
            else
            {
                httpReq.addRequestParameters("QMTEMPLATE","");
                httpReq.addRequestParameters("QMPAGE","");
            }
        }
        return "";
    }
}
