package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.Quests;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/* 
   Copyright 2000-2007 Bo Zimmerman

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
public interface QuestManager extends CMLibrary
{
    public Quest objectInUse(Environmental E);
    public int numQuests();
    public Quest fetchQuest(int i);
    public Quest fetchQuest(String qname);
    public void addQuest(Quest Q);
    public void shutdown();
    public void delQuest(Quest Q);
    public void save();
    public Vector parseQuestSteps(Vector script, int startLine, boolean rawLineInput);
    public Vector parseQuestCommandLines(Vector script, String cmdOnly, int startLine);
    public int getHolidayIndex(String named);
    public String getHolidayName(int index);
    public String listHolidays(Area A, String otherParms);
    public String deleteHoliday(Area A, int holidayNumber);
    public void modifyHoliday(MOB mob, int holidayNumber);
    public String createHoliday(Area A, String named);
    public Object getHolidayFile();
    public Vector getEncodedHolidayData(String dataFromStepsFile);
    public DVector getQuestTemplate(MOB mob, String fileToGet);
    public Quest questMaker(MOB mob);

    public final static int QM_COMMAND_$TITLE=0;
    public final static int QM_COMMAND_$LABEL=1;
    public final static int QM_COMMAND_$EXPRESSION=2;
    public final static int QM_COMMAND_$UNIQUE_QUEST_NAME=3;
    public final static int QM_COMMAND_$CHOOSE=4;
    public final static int QM_COMMAND_$ITEMXML=5;
    public final static int QM_COMMAND_$STRING=6;
    public final static int QM_COMMAND_$ROOMID=7;
    public final static int QM_COMMAND_$AREA=8;
    public final static int QM_COMMAND_$MOBXML=9;
    public final static int QM_COMMAND_$NAME=10;
    public final static int QM_COMMAND_$LONG_STRING=11;
    public final static int QM_COMMAND_MASK=127;
    public final static int QM_COMMAND_OPTIONAL=128;
    public final static String[] QM_COMMAND_TYPES={
        "$TITLE",
        "$LABEL",
        "$EXPRESSION",
        "$UNIQUE_QUEST_NAME",
        "$CHOOSE",
        "$ITEMXML",
        "$STRING",
        "$ROOMID",
        "$AREA",
        "$MOBXML",
        "$NAME",
        "$LONG_STRING"
    };
    public final static EnglishParsing.CMEval[] QM_COMMAND_TESTS={
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //title
            return str;
        }},
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //label
            return str;
        }},
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //expression
            if(!(str instanceof String)) throw new CMException("Bad type: "+((str==null)?"null":str.getClass().getName()));
            if((str==null)||(((String)str).trim().length()==0)){
                if(emptyOK) return "";
                throw new CMException("You must enter an expression!");
            }
            if(!CMath.isMathExpression((String)str)) 
                throw new CMException("Invalid mathematical expression.  Use numbers,+,-,*,/,(), and ? only."); 
            return str;
        }},
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //quest name
            if(!(str instanceof String)) throw new CMException("Bad type: "+((str==null)?"null":str.getClass().getName()));
            if((str==null)||(((String)str).trim().length()==0)){
                if(emptyOK) return "";
                throw new CMException("You must enter a quest name!");
            }
            for(int i=0;i<((String)str).length();i++)
                if((!Character.isLetterOrDigit(((String)str).charAt(i)))
                &&(((String)str).charAt(i)!='_'))
                    throw new CMException("Quest names may only contain letters, digits, or _ -- no spaces or special characters.");
            
            if(CMLib.quests().fetchQuest(((String)str).trim())!=null)
                throw new CMException("A quest of that name already exists.  Enter another.");
            return ((String)str).trim();
        }},
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //choose
            if((choices==null)||(choices.length==0)) throw new CMException("NO choices?!");
            if(!(str instanceof String)) throw new CMException("Bad type: "+((str==null)?"null":str.getClass().getName()));
            if((str==null)||(((String)str).trim().length()==0)){
                if(emptyOK) return "";
                throw new CMException("You must enter a value!");
            }
            int x=CMParms.indexOf(choices,((String)str).toUpperCase().trim());
            if(x<0)
                throw new CMException("That is not a valid option.  Choices include: "+CMParms.toStringList(choices));
            return choices[x];
        }},
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //itemxml
            if((choices==null)||(choices.length==0)) throw new CMException("NO choices?!");
            if(!(str instanceof String)) throw new CMException("Bad type: "+((str==null)?"null":str.getClass().getName()));
            StringBuffer choiceNames=new StringBuffer("");
            for(int c=0;c<choices.length;c++)
                choiceNames.append(((Environmental)choices[c]).Name()+", ");
            if(choiceNames.toString().endsWith(", ")) choiceNames=new StringBuffer(choiceNames.substring(0,choiceNames.length()-2));
            if((str==null)||(((String)str).trim().length()==0)){
                if(emptyOK) return "";
                throw new CMException("You must enter one of the following: "+choiceNames.toString());
            }
            Environmental[] ES=new Environmental[choices.length];
            for(int e=0;e<choices.length;e++) ES[e]=(Environmental)choices[e];
            Environmental E=CMLib.english().fetchEnvironmental(ES,(String)str,false);
            if(E==null)
                throw new CMException("'"+str+"' was not found.  You must enter one of the following: "+choiceNames.toString());
            return CMLib.english().getContextName(choices,E);
        }},
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //string
            if(!(str instanceof String)) throw new CMException("Bad type: "+((str==null)?"null":str.getClass().getName()));
            if((str==null)||(((String)str).trim().length()==0)){
                if(emptyOK) return "";
                throw new CMException("You must enter a value!");
            }
            return str;
        }},
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //roomid
            if(!(str instanceof String)) throw new CMException("Bad type: "+((str==null)?"null":str.getClass().getName()));
            if((str==null)||(((String)str).trim().length()==0)){
                if(emptyOK) return "";
                throw new CMException("You must enter an room id(s), name(s), keyword ANY, or ANY MASK=...");
            }
            if(((String)str).trim().equalsIgnoreCase("ANY")) return ((String)str).trim();
            if(((String)str).trim().toUpperCase().startsWith("ANY MASK=")) return str;
            Vector V=CMParms.parse((String)str);
            if(V.size()==0){ if(emptyOK) return ""; throw new CMException("You must enter an room id(s), name(s), keyword ANY, or ANY MASK=...");}
            String s=null;
            for(int v=0;v<V.size();v++)
            {
                s=(String)(String)V.elementAt(v);
                boolean found=false;
                Room R=CMLib.map().getRoom(s);
                if(R!=null) found=true;
                if(!found)
                for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
                {
                    R=(Room)e.nextElement();
                    if((CMLib.english().containsString(R.displayText(),s)
                    ||(s.equalsIgnoreCase(R.displayText()))
                    ||(CMLib.english().containsString(R.description(),s))))
                    { found=true; break;}
                }
                if(!found) throw new CMException("'"+((String)V.elementAt(v))+"' is not a valid room name, id, or description.");
            }
            return str;
        }},
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //area
            if(!(str instanceof String)) throw new CMException("Bad type: "+((str==null)?"null":str.getClass().getName()));
            if((str==null)||(((String)str).trim().length()==0)){
                if(emptyOK) return "";
                throw new CMException("You must enter an area name(s), keyword ANY, or ANY MASK=...");
            }
            if(((String)str).trim().equalsIgnoreCase("ANY")) return ((String)str).trim();
            if(((String)str).trim().toUpperCase().startsWith("ANY MASK=")) return str;
            Vector V=CMParms.parse((String)str);
            if(V.size()==0){ if(emptyOK) return ""; throw new CMException("You must enter an area name(s), keyword ANY, or ANY MASK=...");}
            StringBuffer returnStr=new StringBuffer("");
            for(int v=0;v<V.size();v++)
            {
                Area A=CMLib.map().findArea((String)V.elementAt(v));
                if(A==null) throw new CMException("'"+((String)V.elementAt(v))+"' is not a valid area name.");
                returnStr.append("\""+A.name()+"\" ");
            }
            return returnStr.toString().trim();
        }},
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //mobxml
            if((choices==null)||(choices.length==0)) throw new CMException("NO choices?!");
            if(!(str instanceof String)) throw new CMException("Bad type: "+((str==null)?"null":str.getClass().getName()));
            StringBuffer choiceNames=new StringBuffer("");
            for(int c=0;c<choices.length;c++)
                choiceNames.append(((Environmental)choices[c]).Name()+", ");
            if(choiceNames.toString().endsWith(", ")) choiceNames=new StringBuffer(choiceNames.substring(0,choiceNames.length()-2));
            if((str==null)||(((String)str).trim().length()==0)){
                if(emptyOK) return "";
                throw new CMException("You must enter one of the following: "+choiceNames.toString());
            }
            Environmental[] ES=new Environmental[choices.length];
            for(int e=0;e<choices.length;e++) ES[e]=(Environmental)choices[e];
            Environmental E=CMLib.english().fetchEnvironmental(ES,(String)str,false);
            if(E==null)
                throw new CMException("'"+str+"' was not found.  You must enter one of the following: "+choiceNames.toString());
            return CMLib.english().getContextName(choices,E);
        }},
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //designame
            if(!(str instanceof String)) throw new CMException("Bad type: "+((str==null)?"null":str.getClass().getName()));
            if((str==null)||(((String)str).trim().length()==0)){
                if(emptyOK) return "";
                throw new CMException("You must enter a value!");
            }
            if(((String)str).trim().equalsIgnoreCase("ANY")) return ((String)str).trim();
            if(((String)str).trim().toUpperCase().startsWith("ANY MASK=")) return str;
            if((((String)str).indexOf(' ')>0)&&(((String)str).indexOf('\"')<0))
                throw new CMException("Multiple-word names must be grouped with double-quotes.  If this represents several names, put each name in double-quotes as so: \"name1\" \"name2\" \"multi word name\".");
            return str;
        }},
        new EnglishParsing.CMEval(){ public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException { //string
            if(!(str instanceof String)) throw new CMException("Bad type: "+((str==null)?"null":str.getClass().getName()));
            if((str==null)||(((String)str).trim().length()==0)){
                if(emptyOK) return "";
                throw new CMException("You must enter a value!");
            }
            str=CMStrings.replaceAll((String)str,"\n\r"," ");
            str=CMStrings.replaceAll((String)str,"\r\n"," ");
            str=CMStrings.replaceAll((String)str,"\n"," ");
            str=CMStrings.replaceAll((String)str,"\r"," ");
            return str;
        }},
    };
    
}
