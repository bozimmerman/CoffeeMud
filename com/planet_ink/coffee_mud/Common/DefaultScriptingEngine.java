package com.planet_ink.coffee_mud.Common;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import org.mozilla.javascript.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class DefaultScriptingEngine implements ScriptingEngine
{
    public String ID(){return "DefaultScriptingEngine";}
    public String name(){return "Default Scripting Engine";}
    protected static final Hashtable funcH=new Hashtable();
    protected static final Hashtable methH=new Hashtable();
    protected static final Hashtable progH=new Hashtable();
    protected static final Hashtable connH=new Hashtable();
    protected static final Hashtable gstatH=new Hashtable();
    protected static final Hashtable signH=new Hashtable();
    
    protected static Hashtable patterns=new Hashtable();
    protected boolean noDelay=CMSecurity.isDisabled("SCRIPTABLEDELAY");
    
    protected String scope="";

    protected long tickStatus=Tickable.STATUS_NOT;
    protected boolean isSavable=true;

    protected MOB lastToHurtMe=null;
    protected Room lastKnownLocation=null;
    protected Tickable altStatusTickable=null;
    protected Vector que=new Vector();
    protected Vector oncesDone=new Vector();
    protected Hashtable delayTargetTimes=new Hashtable();
    protected Hashtable delayProgCounters=new Hashtable();
    protected Hashtable lastTimeProgsDone=new Hashtable();
    protected Hashtable lastDayProgsDone=new Hashtable();
    protected HashSet registeredSpecialEvents=new HashSet();
    protected Hashtable noTrigger=new Hashtable();
    protected MOB backupMOB=null;
    protected CMMsg lastMsg=null;
    protected Resources resources=Resources.instance();
    protected Environmental lastLoaded=null;
    protected String myScript="";
    protected String defaultQuestName="";

    public DefaultScriptingEngine()
    {
        super();
        CMClass.bumpCounter(this,CMClass.OBJECT_COMMON);
    }

    public boolean isSavable(){ return isSavable;}
    public void setSavable(boolean truefalse){isSavable=truefalse;}

    public String defaultQuestName(){ return defaultQuestName;}

    protected Quest defaultQuest() {
        if(defaultQuestName.length()==0)
            return null;
        return CMLib.quests().fetchQuest(defaultQuestName);
    }

    public void setVarScope(String newScope){
        if((newScope==null)||(newScope.trim().length()==0))
        {
            scope="";
            resources=Resources.instance();
        }
        else
	        scope=newScope.toUpperCase().trim();
        if(scope.equalsIgnoreCase("*"))
            resources = Resources.newResources();
        else
        {
            resources=(Resources)Resources.getResource("VARSCOPE-"+scope);
            if(resources==null)
            {
                resources = Resources.newResources();
                Resources.submitResource("VARSCOPE-"+scope,resources);
            }
        }
    }

    public String getVarScope() { return scope;}

    protected Object[] newObjs() { return new Object[ScriptingEngine.SPECIAL_NUM_OBJECTS];}

    public String getLocalVarXML()
    {
        if((scope==null)||(scope.length()==0)) return "";
        StringBuffer str=new StringBuffer("");
        Vector V=resources._findResourceKeys("SCRIPTVAR-");
        for(int v=0;v<V.size();v++)
        {
            String key=(String)V.elementAt(v);
            if(key.startsWith("SCRIPTVAR-"))
            {
                str.append("<"+key.substring(10)+">");
                Hashtable H=(Hashtable)resources._getResource(key);
                for(Enumeration e=H.keys();e.hasMoreElements();)
                {
                    String vn=(String)e.nextElement();
                    String val=(String)H.get(vn);
                    str.append("<"+vn+">"+CMLib.xml().parseOutAngleBrackets(val)+"</"+vn+">");
                }
                str.append("</"+key.substring(10)+">");
            }
        }
        return str.toString();
    }

    public void setLocalVarXML(String xml)
    {
        Vector V=resources._findResourceKeys("SCRIPTVAR-");
        for(int v=0;v<V.size();v++)
        {
            String key=(String)V.elementAt(v);
            if(key.startsWith("SCRIPTVAR-"))
                resources._removeResource(key);
        }
        V=CMLib.xml().parseAllXML(xml);
        for(int v=0;v<V.size();v++)
        {
            XMLLibrary.XMLpiece piece=(XMLLibrary.XMLpiece)V.elementAt(v);
            if((piece.contents!=null)&&(piece.contents.size()>0))
            {
                String kkey="SCRIPTVAR-"+piece.tag;
                Hashtable H=new Hashtable();
                for(int c=0;c<piece.contents.size();c++)
                {
                    XMLLibrary.XMLpiece piece2=(XMLLibrary.XMLpiece)piece.contents.elementAt(c);
                    H.put(piece2.tag,piece2.value);
                }
                resources._submitResource(kkey,H);
            }
        }
    }

    private Quest getQuest(String named)
    {
        if((defaultQuestName.length()>0)&&(named.equals("*")||named.equalsIgnoreCase(defaultQuestName)))
            return defaultQuest();

        Quest Q=null;
        for(int i=0;i<CMLib.quests().numQuests();i++)
        {
            try{Q=CMLib.quests().fetchQuest(i);}catch(Exception e){}
            if(Q!=null)
            {
                if(Q.name().equalsIgnoreCase(named))
                    if(Q.running()) return Q;
            }
        }
        return CMLib.quests().fetchQuest(named);
    }

    public long getTickStatus()
    {
        Tickable T=altStatusTickable;
        if(T!=null) return T.getTickStatus();
        return tickStatus;
    }

    public void registerDefaultQuest(String qName){
        if((qName==null)||(qName.trim().length()==0))
            defaultQuestName="";
        else
            defaultQuestName=qName.trim();
    }

    public CMObject newInstance()
    {
        try
        {
            return (ScriptingEngine)this.getClass().newInstance();
        }
        catch(Exception e)
        {
            Log.errOut(ID(),e);
        }
        return new DefaultScriptingEngine();
    }

    public CMObject copyOf()
    {
        try
        {
            ScriptingEngine S=(ScriptingEngine)this.clone();
            CMClass.bumpCounter(S,CMClass.OBJECT_COMMON);
            S.setScript(getScript());
            return S;
        }
        catch(CloneNotSupportedException e)
        {
            return new DefaultScriptingEngine();
        }
    }
    protected void finalize(){CMClass.unbumpCounter(this,CMClass.OBJECT_COMMON);}

    /**
     * c=clean bit, r=pastbitclean, p=pastbit, s=remaining clean bits, t=trigger
     */
    protected String[] parseBits(DVector script, int row, String instructions)
    {
        String line=(String)script.elementAt(row,1);
        String[] newLine=parseBits(line,instructions);
        script.setElementAt(row,2,newLine);
        return newLine;
    }

    protected String[] parseSpecial3PartEval(String[][] eval, int t)
    {
        String[] tt=eval[0];
        String funcParms=tt[t];
        String[] tryTT=parseBits(funcParms,"ccr");
        if(signH.containsKey(tryTT[1]))
            tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
        else
        {
            String[] parsed=null;
            if(CMParms.cleanBit(funcParms).equals(funcParms))
                parsed=parseBits("'"+funcParms+"' . .","cr");
            else
                parsed=parseBits(funcParms+" . .","cr");
            tt=insertStringArray(tt,parsed,t);
            eval[0]=tt;
        }
        return tt;
    }
    
    /**
     * c=clean bit, r=pastbitclean, p=pastbit, s=remaining clean bits, t=trigger
     */
    protected String[] parseBits(String line, String instructions)
    {
        String[] newLine=new String[instructions.length()];
        for(int i=0;i<instructions.length();i++)
            switch(instructions.charAt(i))
            {
            case 'c': newLine[i]=CMParms.getCleanBit(line,i); break;
            case 'C': newLine[i]=CMParms.getCleanBit(line,i).toUpperCase().trim(); break;
            case 'r': newLine[i]=CMParms.getPastBitClean(line,i-1); break;
            case 'R': newLine[i]=CMParms.getPastBitClean(line,i-1).toUpperCase().trim(); break;
            case 'p': newLine[i]=CMParms.getPastBit(line,i-1); break;
            case 'P': newLine[i]=CMParms.getPastBit(line,i-1).toUpperCase().trim(); break;
            case 'S': line=line.toUpperCase();
            case 's': 
            {
                String s=CMParms.getPastBit(line,i-1);
                int numBits=CMParms.numBits(s);
                String[] newNewLine=new String[newLine.length-1+numBits];
                for(int x=0;x<i;x++) 
                    newNewLine[x]=newLine[x];
                for(int x=0;x<numBits;x++)
                    newNewLine[i+x]=CMParms.getCleanBit(s,i-1);
                newLine=newNewLine;
                i=instructions.length();
                break;
            }
            case 'T': line=line.toUpperCase();
            case 't': 
            {
                String s=CMParms.getPastBit(line,i-1);
                String[] newNewLine=null;
                if(CMParms.getCleanBit(s,0).equalsIgnoreCase("P"))
                {
                    newNewLine=new String[newLine.length+1];
                    for(int x=0;x<i;x++) 
                        newNewLine[x]=newLine[x];
                    newNewLine[i]="P";
                    newNewLine[i+1]=CMParms.getPastBitClean(s,0);
                }
                else
                {
                    
                    int numNewBits=(s.trim().length()==0)?1:CMParms.numBits(s);
                    newNewLine=new String[newLine.length-1+numNewBits];
                    for(int x=0;x<i;x++) 
                        newNewLine[x]=newLine[x];
                    for(int x=0;x<numNewBits;x++)
                        newNewLine[i+x]=CMParms.getCleanBit(s,x);
                }
                newLine=newNewLine;
                i=instructions.length();
                break;
            }
            }
        return newLine;
    }
    
    protected String[] insertStringArray(String[] oldS, String[] inS, int where)
    {
        String[] newLine=new String[oldS.length+inS.length-1];
        for(int i=0;i<where;i++)
            newLine[i]=oldS[i];
        for(int i=0;i<inS.length;i++)
            newLine[where+i]=inS[i];
        for(int i=where+1;i<oldS.length;i++)
            newLine[inS.length+i-1]=oldS[i];
        return newLine;
    }
    
    /**
     * c=clean bit, r=pastbitclean, p=pastbit, s=remaining clean bits, t=trigger
     */
    protected String[] parseBits(String[][] oldBits, int start, String instructions)
    {
        String[] tt=(String[])oldBits[0];
        String parseMe=tt[start];
        String[] parsed=parseBits(parseMe,instructions);
        if(parsed.length==1)
        {
            tt[start]=parsed[0];
            return tt;
        }
        String[] newLine=insertStringArray(tt,parsed,start);
        oldBits[0]=newLine;
        return newLine;
        
    }
    public boolean endQuest(Environmental hostObj, MOB mob, String quest)
    {
        if(mob!=null)
        {
            Vector scripts=getScripts();
            if(!mob.amDead()) lastKnownLocation=mob.location();
            String trigger="";
            String[] tt=null;
            for(int v=0;v<scripts.size();v++)
            {
                DVector script=(DVector)scripts.elementAt(v);
                if(script.size()>0)
                {
                    trigger=((String)script.elementAt(0,1)).toUpperCase().trim();
                    tt=(String[])script.elementAt(0,2);
                    if((getTriggerCode(trigger,tt)==13) //questtimeprog
                    &&(!oncesDone.contains(script)))
                    {
                        if(tt==null) tt=parseBits(script,0,"CCC");
                        if((tt[1].equals(quest)||(tt[1].equals("*")))
                        &&(CMath.s_int(tt[2])<0))
                        {
                            oncesDone.addElement(script);
                            execute(hostObj,mob,mob,mob,null,null,script,null,newObjs());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public Vector externalFiles()
    {
        Vector xmlfiles=new Vector();
        parseLoads(getScript(), 0, xmlfiles, null);
        return xmlfiles;
    }

    protected String getVarHost(Environmental E,
                             String rawHost,
                             MOB source,
                             Environmental target,
                             Environmental scripted,
                             MOB monster,
                             Item primaryItem,
                             Item secondaryItem,
                             String msg,
                             Object[] tmp)
    {
        if(!rawHost.equals("*"))
        {
            if(E==null)
                rawHost=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,rawHost);
            else
            if(E instanceof Room)
                rawHost=CMLib.map().getExtendedRoomID((Room)E);
            else
                rawHost=E.Name();
        }
        return rawHost;
    }

    public boolean isVar(String host, String var) {
        if(host.equalsIgnoreCase("*"))
        {
            Vector V=resources._findResourceKeys("SCRIPTVAR-");
            String val=null;
            Hashtable H=null;
            String key=null;
            var=var.toUpperCase();
            for(int v=0;v<V.size();v++)
            {
                key=(String)V.elementAt(v);
                if(key.startsWith("SCRIPTVAR-"))
                {
                    H=(Hashtable)resources._getResource(key);
                    val=(String)H.get(var);
                    if(val!=null) return true;
                }
            }
            return false;
        }
        Hashtable H=(Hashtable)resources._getResource("SCRIPTVAR-"+host);
        String val=null;
        if(H!=null)
            val=(String)H.get(var.toUpperCase());
        return (val!=null);
    }

    public String getVar(Environmental E, String rawHost, String var, MOB source, Environmental target,
                         Environmental scripted, MOB monster, Item primaryItem, Item secondaryItem, String msg,
                         Object[] tmp)
    { return getVar(getVarHost(E,rawHost,source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp),var); }

    public String getVar(String host, String var)
    {
        if(host.equalsIgnoreCase("*"))
        {
        	if(var.equals("COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT"))
        	{
        		StringBuffer str=new StringBuffer("");
        		parseLoads(getScript(),0,null,str);
        		return str.toString();
        	}
            Vector V=resources._findResourceKeys("SCRIPTVAR-");
            String val=null;
            Hashtable H=null;
            String key=null;
            var=var.toUpperCase();
            for(int v=0;v<V.size();v++)
            {
                key=(String)V.elementAt(v);
                if(key.startsWith("SCRIPTVAR-"))
                {
                    H=(Hashtable)resources._getResource(key);
                    val=(String)H.get(var);
                    if(val!=null) return val;
                }
            }
            return "";
        }
        Hashtable H=(Hashtable)resources._getResource("SCRIPTVAR-"+host);
        String val=null;
        if(H!=null)
            val=(String)H.get(var.toUpperCase());
        else
        if((defaultQuestName!=null)&&(defaultQuestName.length()>0))
        {
            MOB M=CMLib.players().getPlayer(host);
            if(M!=null)
                for(int s=0;s<M.numScripts();s++)
                {
                    ScriptingEngine E=M.fetchScript(s);
                    if((E!=null)
                    &&(E!=this)
                    &&(defaultQuestName.equalsIgnoreCase(E.defaultQuestName()))
                    &&(E.isVar(host,var)))
                        return E.getVar(host,var);
                }
        }
        if(val==null) return "";
        return val;
    }

    private StringBuffer getResourceFileData(String named)
    {
        if(getQuest("*")!=null) return getQuest("*").getResourceFileData(named);
        return new CMFile(Resources.makeFileResourceName(named),null,true).text();
    }

    public String getScript(){ return myScript;}

    public void setScript(String newParms)
    {
        newParms=CMStrings.replaceAll(newParms,"'","`");
        if(newParms.startsWith("+"))
        {
            String superParms=getScript();
            if(superParms.length()>100)
                Resources.removeResource("PARSEDPRG: "+superParms.substring(0,100)+superParms.length()+superParms.hashCode());
            else
                Resources.removeResource("PARSEDPRG: "+superParms);
            newParms=superParms+";"+newParms.substring(1);
        }
        que=new Vector();
        oncesDone=new Vector();
        delayTargetTimes=new Hashtable();
        delayProgCounters=new Hashtable();
        lastTimeProgsDone=new Hashtable();
        lastDayProgsDone=new Hashtable();
        registeredSpecialEvents=new HashSet();
        noTrigger=new Hashtable();
        myScript=newParms;
        if(oncesDone.size()>0)
            oncesDone.clear();
    }

    public boolean canActAtAll(Tickable affecting)
    { return CMLib.flags().canActAtAll(affecting);}

    public boolean canFreelyBehaveNormal(Tickable affecting)
    { return CMLib.flags().canFreelyBehaveNormal(affecting);}

    protected String parseLoads(String text, int depth, Vector filenames, StringBuffer nonFilenameScript)
    {
        StringBuffer results=new StringBuffer("");
        String parse=text;
        if(depth>10) return "";  // no including off to infinity
        String p=null;
        while(parse.length()>0)
        {
            int y=parse.toUpperCase().indexOf("LOAD=");
            if(y>=0)
            {
                p=parse.substring(0,y).trim();
                if((!p.endsWith(";"))
                &&(!p.endsWith("\n"))
                &&(!p.endsWith("~"))
                &&(!p.endsWith("\r"))
                &&(p.length()>0))
                {
                    if(nonFilenameScript!=null) 
                    	nonFilenameScript.append(parse.substring(0,y+1));
                    results.append(parse.substring(0,y+1));
                    parse=parse.substring(y+1);
                    continue;
                }
                results.append(p+"\n");
                int z=parse.indexOf("~",y);
                while((z>0)&&(parse.charAt(z-1)=='\\'))
                    z=parse.indexOf("~",z+1);
                if(z>0)
                {
                    String filename=parse.substring(y+5,z).trim();
                    parse=parse.substring(z+1);
                    if((filenames!=null)&&(!filenames.contains(filename))) 
                    	filenames.addElement(filename);
                    results.append(parseLoads(getResourceFileData(filename).toString(),depth+1,filenames,null));
                }
                else
                {
                    String filename=parse.substring(y+5).trim();
                    if((filenames!=null)&&(!filenames.contains(filename))) 
                    	filenames.addElement(filename);
                    results.append(parseLoads(getResourceFileData(filename).toString(),depth+1,filenames,null));
                    break;
                }
            }
            else
            {
                if(nonFilenameScript!=null) 
                	nonFilenameScript.append(parse);
                results.append(parse);
                break;
            }
        }
        return results.toString();
    }

    protected Vector parseScripts(String text)
    {
        synchronized(funcH)
        {
            if(funcH.size()==0)
            {
                for(int i=0;i<funcs.length;i++)
                    funcH.put(funcs[i],Integer.valueOf(i+1));
                for(int i=0;i<methods.length;i++)
                    methH.put(methods[i],Integer.valueOf(i+1));
                for(int i=0;i<progs.length;i++)
                    progH.put(progs[i],Integer.valueOf(i+1));
                for(int i=0;i<CONNECTORS.length;i++)
                    connH.put(CONNECTORS[i],Integer.valueOf(i));
                for(int i=0;i<GSTATCODES_ADDITIONAL.length;i++)
                    gstatH.put(GSTATCODES_ADDITIONAL[i],Integer.valueOf(i));
                for(int i=0;i<SIGNS.length;i++)
                   signH.put(SIGNS[i],Integer.valueOf(i));
            }
        }
        Vector V=new Vector();
        text=parseLoads(text,0,null,null);
        int y=0;
        while((text!=null)&&(text.length()>0))
        {
            y=text.indexOf("~");
            while((y>0)&&(text.charAt(y-1)=='\\'))
                y=text.indexOf("~",y+1);
            String script="";
            if(y<0)
            {
                script=text.trim();
                text="";
            }
            else
            {
                script=text.substring(0,y).trim();
                text=text.substring(y+1).trim();
            }
            if(script.length()>0)
                V.addElement(script);
        }
        for(int v=0;v<V.size();v++)
        {
            String s=(String)V.elementAt(v);
            DVector script=new DVector(2);
            while(s.length()>0)
            {
                y=-1;
                int yy=0;
                while(yy<s.length())
                    if((s.charAt(yy)==';')&&((yy<=0)||(s.charAt(yy-1)!='\\'))) {y=yy;break;}
                    else
                    if(s.charAt(yy)=='\n'){y=yy;break;}
                    else
                    if(s.charAt(yy)=='\r'){y=yy;break;}
                    else yy++;
                String cmd="";
                if(y<0)
                {
                    cmd=s.trim();
                    s="";
                }
                else
                {
                    cmd=s.substring(0,y).trim();
                    s=s.substring(y+1).trim();
                }
                if((cmd.length()>0)&&(!cmd.startsWith("#")))
                {
                    cmd=CMStrings.replaceAll(cmd,"\\~","~");
                    cmd=CMStrings.replaceAll(cmd,"\\=","=");
                    script.addElement(CMStrings.replaceAll(cmd,"\\;",";"),null);
                }
            }
            V.setElementAt(script,v);
        }
        V.trimToSize();
        return V;
    }

    protected Room getRoom(String thisName, Room imHere)
    {
        if(thisName.length()==0) return null;
        if((imHere!=null)&&(imHere.roomID().equalsIgnoreCase(thisName)))
            return imHere;
        Room room=CMLib.map().getRoom(thisName);
        if((room!=null)&&(room.roomID().equalsIgnoreCase(thisName)))
            return room;
    	Vector rooms=new Vector(1);
    	if((imHere!=null)&&(imHere.getArea()!=null))
    		rooms=CMLib.map().findAreaRoomsLiberally(null, imHere.getArea(), thisName, "RIEPM",100);
    	if(rooms.size()==0)
    		rooms=CMLib.map().findWorldRoomsLiberally(null,thisName, "RIEPM",100,10);
        if(rooms.size()>0) return (Room)rooms.elementAt(CMLib.dice().roll(1,rooms.size(),-1));
        return room;
    }


    protected void logError(Environmental scripted, String cmdName, String errType, String errMsg)
    {
        if(scripted!=null)
        {
            Room R=CMLib.map().roomLocation(scripted);
            Log.errOut("Scripting",scripted.name()+"/"+CMLib.map().getExtendedRoomID(R)+"/"+ cmdName+"/"+errType+"/"+errMsg);
            if(R!=null) R.showHappens(CMMsg.MSG_OK_VISUAL,"Scripting Error: "+scripted.name()+"/"+CMLib.map().getExtendedRoomID(R)+"/"+CMParms.toStringList(externalFiles())+"/"+ cmdName+"/"+errType+"/"+errMsg);
        }
        else
            Log.errOut("Scripting","*/*/"+CMParms.toStringList(externalFiles())+"/"+cmdName+"/"+errType+"/"+errMsg);

    }

    protected boolean simpleEvalStr(Environmental scripted,
                                    String arg1,
                                    String arg2,
                                    String cmp,
                                    String cmdName)
    {
        int x=arg1.compareToIgnoreCase(arg2);
        Integer SIGN=(Integer)signH.get(cmp);
        if(SIGN==null)
        {
            logError(scripted,cmdName,"Syntax",arg1+" "+cmp+" "+arg2);
            return false;
        }
        switch(SIGN.intValue())
        {
        case SIGN_EQUL: return (x==0);
        case SIGN_EQGT:
        case SIGN_GTEQ: return (x==0)||(x>0);
        case SIGN_EQLT:
        case SIGN_LTEQ: return (x==0)||(x<0);
        case SIGN_GRAT: return (x>0);
        case SIGN_LEST: return (x<0);
        case SIGN_NTEQ: return (x!=0);
        default: return (x==0);
        }
    }


    protected boolean simpleEval(Environmental scripted, String arg1, String arg2, String cmp, String cmdName)
    {
        long val1=CMath.s_long(arg1.trim());
        long val2=CMath.s_long(arg2.trim());
        Integer SIGN=(Integer)signH.get(cmp);
        if(SIGN==null)
        {
            logError(scripted,cmdName,"Syntax",val1+" "+cmp+" "+val2);
            return false;
        }
        switch(SIGN.intValue())
        {
        case SIGN_EQUL: return (val1==val2);
        case SIGN_EQGT:
        case SIGN_GTEQ: return val1>=val2;
        case SIGN_EQLT:
        case SIGN_LTEQ: return val1<=val2;
        case SIGN_GRAT: return (val1>val2);
        case SIGN_LEST: return (val1<val2);
        case SIGN_NTEQ: return (val1!=val2);
        default: return (val1==val2);
        }
    }

    protected boolean simpleExpressionEval(Environmental scripted, String arg1, String arg2, String cmp, String cmdName)
    {
        double val1=CMath.s_parseMathExpression(arg1.trim());
        double val2=CMath.s_parseMathExpression(arg2.trim());
        Integer SIGN=(Integer)signH.get(cmp);
        if(SIGN==null)
        {
            logError(scripted,cmdName,"Syntax",val1+" "+cmp+" "+val2);
            return false;
        }
        switch(SIGN.intValue())
        {
        case SIGN_EQUL: return (val1==val2);
        case SIGN_EQGT:
        case SIGN_GTEQ: return val1>=val2;
        case SIGN_EQLT:
        case SIGN_LTEQ: return val1<=val2;
        case SIGN_GRAT: return (val1>val2);
        case SIGN_LEST: return (val1<val2);
        case SIGN_NTEQ: return (val1!=val2);
        default: return (val1==val2);
        }
    }

    protected Vector loadMobsFromFile(Environmental scripted, String filename)
    {
        filename=filename.trim();
        Vector monsters=(Vector)Resources.getResource("RANDOMMONSTERS-"+filename);
        if(monsters!=null) return monsters;
        StringBuffer buf=getResourceFileData(filename);
        String thangName="null";
        Room R=CMLib.map().roomLocation(scripted);
        if(R!=null)
            thangName=scripted.name()+" at "+CMLib.map().getExtendedRoomID((Room)scripted);
        else
        if(scripted!=null)
            thangName=scripted.name();
        if((buf==null)||(buf.length()<20))
        {
            logError(scripted,"XMLLOAD","?","Unknown XML file: '"+filename+"' in "+thangName);
            return null;
        }
        if(buf.substring(0,20).indexOf("<MOBS>")<0)
        {
            logError(scripted,"XMLLOAD","?","Invalid XML file: '"+filename+"' in "+thangName);
            return null;
        }
        monsters=new Vector();
        String error=CMLib.coffeeMaker().addMOBsFromXML(buf.toString(),monsters,null);
        if(error.length()>0)
        {
            logError(scripted,"XMLLOAD","?","Error in XML file: '"+filename+"'");
            return null;
        }
        if(monsters.size()<=0)
        {
            logError(scripted,"XMLLOAD","?","Empty XML file: '"+filename+"'");
            return null;
        }
        Resources.submitResource("RANDOMMONSTERS-"+filename,monsters);
        return monsters;
    }

    protected Vector loadItemsFromFile(Environmental scripted, String filename)
    {
        filename=filename.trim();
        Vector items=(Vector)Resources.getResource("RANDOMITEMS-"+filename);
        if(items!=null) return items;
        StringBuffer buf=getResourceFileData(filename);
        String thangName="null";
        Room R=CMLib.map().roomLocation(scripted);
        if(R!=null)
            thangName=scripted.name()+" at "+CMLib.map().getExtendedRoomID((Room)scripted);
        else
        if(scripted!=null)
            thangName=scripted.name();
        if((buf==null)||(buf.length()<20))
        {
            logError(scripted,"XMLLOAD","?","Unknown XML file: '"+filename+"' in "+thangName);
            return null;
        }
        if(buf.substring(0,20).indexOf("<ITEMS>")<0)
        {
            logError(scripted,"XMLLOAD","?","Invalid XML file: '"+filename+"' in "+thangName);
            return null;
        }
        items=new Vector();
        String error=CMLib.coffeeMaker().addItemsFromXML(buf.toString(),items,null);
        if(error.length()>0)
        {
            logError(scripted,"XMLLOAD","?","Error in XML file: '"+filename+"'");
            return null;
        }
        if(items.size()<=0)
        {
            logError(scripted,"XMLLOAD","?","Empty XML file: '"+filename+"'");
            return null;
        }
        Resources.submitResource("RANDOMITEMS-"+filename,items);
        return items;
    }

    protected Environmental findSomethingCalledThis(String thisName, MOB meMOB, Room imHere, Vector OBJS, boolean mob)
    {
        if(thisName.length()==0) return null;
        Environmental thing=null;
        Environmental areaThing=null;
        if(thisName.toUpperCase().trim().startsWith("FROMFILE "))
        {
            try{
                Vector V=null;
                if(mob)
                    V=loadMobsFromFile(null,CMParms.getCleanBit(thisName,1));
                else
                    V=loadItemsFromFile(null,CMParms.getCleanBit(thisName,1));
                if(V!=null)
                {
                    String name=CMParms.getPastBitClean(thisName,1);
                    if(name.equalsIgnoreCase("ALL"))
                        OBJS=V;
                    else
                    if(name.equalsIgnoreCase("ANY"))
                    {
                        if(V.size()>0)
                            areaThing=(Environmental)V.elementAt(CMLib.dice().roll(1,V.size(),-1));
                    }
                    else
                    {
                        areaThing=CMLib.english().fetchEnvironmental(V,name,true);
                        if(areaThing==null)
                            areaThing=CMLib.english().fetchEnvironmental(V,name,false);
                    }
                }
            }
            catch(Exception e){}
        }
        else
        {
            if(!mob)
                areaThing=(meMOB!=null)?meMOB.fetchInventory(thisName):null;
            try
            {
                if(areaThing==null)
                {
            		Area A=imHere.getArea();
                	Vector all=new Vector();
                	if(mob)
                	{
                		all.addAll(CMLib.map().findInhabitants(A.getProperMap(),null,thisName,100));
	            		if(all.size()==0)
	            			all.addAll(CMLib.map().findShopStock(A.getProperMap(), null, thisName,100));
	            		for(int a=all.size()-1;a>=0;a--)
	            			if(!(all.elementAt(a) instanceof MOB))
	            				all.removeElementAt(a);
	            		if(all.size()>0) 
	            			areaThing=(Environmental)all.elementAt(CMLib.dice().roll(1,all.size(),-1));
	            		else
	            		{
	                		all.addAll(CMLib.map().findInhabitants(CMLib.map().rooms(),null,thisName,100));
		            		if(all.size()==0)
		            			all.addAll(CMLib.map().findShopStock(CMLib.map().rooms(), null, thisName,100));
		            		for(int a=all.size()-1;a>=0;a--)
		            			if(!(all.elementAt(a) instanceof MOB))
		            				all.removeElementAt(a);
		            		if(all.size()>0) 
		            			thing=(Environmental)all.elementAt(CMLib.dice().roll(1,all.size(),-1));
	            		}
                	}
                	if(all.size()==0)
                	{
	                	all.addAll(CMLib.map().findRoomItems(A.getProperMap(), null,thisName,true,100));
	            		if(all.size()==0)
	                    	all.addAll(CMLib.map().findInventory(A.getProperMap(), null,thisName,100));
	            		if(all.size()==0)
	                    	all.addAll(CMLib.map().findShopStock(A.getProperMap(), null,thisName,100));
	            		if(all.size()>0) 
	            			areaThing=(Environmental)all.elementAt(CMLib.dice().roll(1,all.size(),-1));
	            		else
	            		{
		                	all.addAll(CMLib.map().findRoomItems(CMLib.map().rooms(), null,thisName,true,100));
		            		if(all.size()==0)
		                    	all.addAll(CMLib.map().findInventory(CMLib.map().rooms(), null,thisName,100));
		            		if(all.size()==0)
		                    	all.addAll(CMLib.map().findShopStock(CMLib.map().rooms(), null,thisName,100));
		            		if(all.size()>0) 
		            			thing=(Environmental)all.elementAt(CMLib.dice().roll(1,all.size(),-1));
	            		}
                	}
                }
            }catch(NoSuchElementException nse){}
        }
        if(areaThing!=null)
            OBJS.addElement(areaThing);
        else
        if(thing!=null)
            OBJS.addElement(thing);
        if(OBJS.size()>0)
            return (Environmental)OBJS.firstElement();
        return null;
    }

    protected Environmental getArgumentMOB(String str,
                                        MOB source,
                                        MOB monster,
                                        Environmental target,
                                        Item primaryItem,
                                        Item secondaryItem,
                                        String msg,
                                        Object[] tmp)
    {
        return getArgumentItem(str,source,monster,monster,target,primaryItem,secondaryItem,msg,tmp);
    }

    protected Environmental getArgumentItem(String str,
                                            MOB source,
                                            MOB monster,
                                            Environmental scripted,
                                            Environmental target,
                                            Item primaryItem,
                                            Item secondaryItem,
                                            String msg,
                                            Object[] tmp)
    {
        if(str.length()<2) return null;
        if(str.charAt(0)=='$')
        {
            if(Character.isDigit(str.charAt(1)))
            {
                Object O=tmp[CMath.s_int(Character.toString(str.charAt(1)))];
                if(O instanceof Environmental)
                    return (Environmental)O;
                else
                if((O instanceof Vector)&&(str.length()>3)&&(str.charAt(2)=='.'))
                {
                    Vector V=(Vector)O;
                    String back=str.substring(2);
                    if(back.charAt(1)=='$')
                        back=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,back);
                    if((back.length()>1)&&Character.isDigit(back.charAt(1)))
                    {
                        int x=1;
                        while((x<back.length())&&(Character.isDigit(back.charAt(x)))) x++;
                        int y=CMath.s_int(back.substring(1,x).trim());
                        if((V.size()>0)&&(y>=0))
                        {
                            if(y>=V.size()) return null;
                            O=V.elementAt(y);
                            if(O instanceof Environmental) return (Environmental)O;
                        }
                        str=O.toString(); // will fall through
                    }
                }
                else
                if(O!=null)
                    str=O.toString(); // will fall through
                else
                    return null;
            }
            else
            switch(str.charAt(1))
            {
            case 'a': return (lastKnownLocation!=null)?lastKnownLocation.getArea():null;
            case 'B':
            case 'b': return lastLoaded;
            case 'N':
            case 'n': return ((source==backupMOB)&&(backupMOB!=null)&&(monster!=scripted))?scripted:source;
            case 'I':
            case 'i': return scripted;
            case 'T':
            case 't': return ((target==backupMOB)&&(backupMOB!=null)&&(monster!=scripted))?scripted:target;
            case 'O':
            case 'o': return primaryItem;
            case 'P':
            case 'p': return secondaryItem;
            case 'd':
            case 'D': return lastKnownLocation;
            case 'F':
            case 'f': if((monster!=null)&&(monster.amFollowing()!=null))
                        return monster.amFollowing();
                      return null;
            case 'r':
            case 'R': return getRandPC(monster,tmp,lastKnownLocation);
            case 'c':
            case 'C': return getRandAnyone(monster,tmp,lastKnownLocation);
            case 'w': return primaryItem!=null?primaryItem.owner():null;
            case 'W': return secondaryItem!=null?secondaryItem.owner():null;
            case 'x':
            case 'X':
                if(lastKnownLocation!=null)
                {
                    if((str.length()>2)&&(Directions.getGoodDirectionCode(""+str.charAt(2))>=0))
                        return lastKnownLocation.getExitInDir(Directions.getGoodDirectionCode(""+str.charAt(2)));
                    int i=0;
                    Exit E=null;
                    while(((++i)<100)||(E!=null))
                        E=lastKnownLocation.getExitInDir(CMLib.dice().roll(1,Directions.NUM_DIRECTIONS(),-1));
                    return E;
                }
                return null;
            case '[':
                {
                    int x=str.substring(2).indexOf("]");
                    if(x>=0)
                    {
                        String mid=str.substring(2).substring(0,x);
                        int y=mid.indexOf(" ");
                        if(y>0)
                        {
                            int num=CMath.s_int(mid.substring(0,y).trim());
                            mid=mid.substring(y+1).trim();
                            Quest Q=getQuest(mid);
                            if(Q!=null) return Q.getQuestItem(num);
                        }
                    }
                }
            break;
            case '{':
                {
                    int x=str.substring(2).indexOf("}");
                    if(x>=0)
                    {
                        String mid=str.substring(2).substring(0,x).trim();
                        int y=mid.indexOf(" ");
                        if(y>0)
                        {
                            int num=CMath.s_int(mid.substring(0,y).trim());
                            mid=mid.substring(y+1).trim();
                            Quest Q=getQuest(mid);
                            if(Q!=null) return Q.getQuestMob(num);
                        }
                    }
                }
            break;
            }
        }
        if(lastKnownLocation!=null)
        {
            str=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,str);
            Environmental E=lastKnownLocation.fetchFromRoomFavorMOBs(null,str,Wearable.FILTER_ANY);
            if(E==null) E=lastKnownLocation.fetchFromMOBRoomFavorsItems(monster,null,str,Wearable.FILTER_ANY);
            if(E==null) E=lastKnownLocation.fetchAnyItem(str);
            if((E==null)&&(monster!=null)) E=monster.fetchInventory(str);
            if(E==null) E=CMLib.players().getPlayer(str);
            return E;
        }
        return null;
    }

    private String makeNamedString(Object O)
    {
        if(O instanceof Vector)
            return makeParsableString((Vector)O);
        else
        if(O instanceof Room)
            return ((Room)O).roomTitle(null);
        else
        if(O instanceof Environmental)
            return ((Environmental)O).Name();
        else
        if(O!=null)
            return O.toString();
        return "";
    }

    private String makeParsableString(Vector V)
    {
        if((V==null)||(V.size()==0)) return "";
        if(V.firstElement() instanceof String) return CMParms.combineWithQuotes(V,0);
        StringBuffer ret=new StringBuffer("");
        String S=null;
        for(int v=0;v<V.size();v++)
        {
            S=makeNamedString(V.elementAt(v)).trim();
            if(S.length()==0)
                ret.append("? ");
            else
            if(S.indexOf(" ")>=0)
                ret.append("\""+S+"\" ");
            else
                ret.append(S+" ");
        }
        return ret.toString();
    }

    protected String varify(MOB source,
                            Environmental target,
                            Environmental scripted,
                            MOB monster,
                            Item primaryItem,
                            Item secondaryItem,
                            String msg,
                            Object[] tmp,
                            String varifyable)
    {
        int t=varifyable.indexOf("$");
        if((monster!=null)&&(monster.location()!=null))
            lastKnownLocation=monster.location();
        if(lastKnownLocation==null) lastKnownLocation=source.location();
        MOB randMOB=null;
        while((t>=0)&&(t<varifyable.length()-1))
        {
            char c=varifyable.charAt(t+1);
            String middle="";
            String front=varifyable.substring(0,t);
            String back=varifyable.substring(t+2);
            if(Character.isDigit(c))
                middle=makeNamedString(tmp[CMath.s_int(Character.toString(c))]);
            else
            switch(c)
            {
            case '@':
                if((t<varifyable.length()-2)&&Character.isLetter(varifyable.charAt(t+2)))
                {
                    Environmental E=getArgumentItem("$"+varifyable.charAt(t+2),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    middle=(E==null)?"null":""+E;
                }
                break;
            case 'a':
                if(lastKnownLocation!=null)
                    middle=lastKnownLocation.getArea().name();
                break;
                //case 'a':
            case 'A':
                // unnecessary, since, in coffeemud, this is part of the name
                break;
            case 'b': middle=lastLoaded!=null?lastLoaded.name():""; break;
            case 'B': middle=lastLoaded!=null?lastLoaded.displayText():""; break;
            case 'c':
            case 'C':
                randMOB=getRandAnyone(monster,tmp,lastKnownLocation);
                if(randMOB!=null)
                    middle=randMOB.name();
                break;
            case 'd': middle=(lastKnownLocation!=null)?lastKnownLocation.roomTitle(monster):""; break;
            case 'D': middle=(lastKnownLocation!=null)?lastKnownLocation.roomDescription(monster):""; break;
            case 'e':
                if(source!=null)
                    middle=source.charStats().heshe();
                break;
            case 'E':
                if((target!=null)&&(target instanceof MOB))
                    middle=((MOB)target).charStats().heshe();
                break;
            case 'f':
                if((monster!=null)&&(monster.amFollowing()!=null))
                    middle=monster.amFollowing().name();
                break;
            case 'F':
                if((monster!=null)&&(monster.amFollowing()!=null))
                    middle=monster.amFollowing().charStats().heshe();
                break;
            case 'g': middle=((msg==null)?"":msg.toLowerCase()); break;
            case 'G': middle=((msg==null)?"":msg); break;
            case 'h':
                if(monster!=null)
                    middle=monster.charStats().himher();
                break;
            case 'H':
                randMOB=getRandPC(monster,tmp,lastKnownLocation);
                if(randMOB!=null)
                    middle=randMOB.charStats().himher();
                break;
            case 'i':
                if(monster!=null)
                    middle=monster.name();
                break;
            case 'I':
                if(monster!=null)
                    middle=monster.displayText();
                break;
            case 'j':
                if(monster!=null)
                    middle=monster.charStats().heshe();
                break;
            case 'J':
                randMOB=getRandPC(monster,tmp,lastKnownLocation);
                if(randMOB!=null)
                    middle=randMOB.charStats().heshe();
                break;
            case 'k':
                if(monster!=null)
                    middle=monster.charStats().hisher();
                break;
            case 'K':
                randMOB=getRandPC(monster,tmp,lastKnownLocation);
                if(randMOB!=null)
                    middle=randMOB.charStats().hisher();
                break;
            case 'l':
                if(lastKnownLocation!=null)
                {
                    StringBuffer str=new StringBuffer("");
                    for(int i=0;i<lastKnownLocation.numInhabitants();i++)
                    {
                        MOB M=lastKnownLocation.fetchInhabitant(i);
                        if((M!=null)&&(M!=monster)&&(CMLib.flags().canBeSeenBy(M,monster)))
                           str.append("\""+M.name()+"\" ");
                    }
                    middle=str.toString();
                }
                break;
            case 'L':
                if(lastKnownLocation!=null)
                {
                    StringBuffer str=new StringBuffer("");
                    for(int i=0;i<lastKnownLocation.numItems();i++)
                    {
                        Item I=lastKnownLocation.fetchItem(i);
                        if((I!=null)&&(I.container()==null)&&(CMLib.flags().canBeSeenBy(I,monster)))
                           str.append("\""+I.name()+"\" ");
                    }
                    middle=str.toString();
                }
                break;
            case 'm':
                if(source!=null)
                    middle=source.charStats().hisher();
                break;
            case 'M':
                if((target!=null)&&(target instanceof MOB))
                    middle=((MOB)target).charStats().hisher();
                break;
            case 'n':
            case 'N':
                if(source!=null)
                    middle=source.name();
                break;
            case 'o':
            case 'O':
                if(primaryItem!=null)
                    middle=primaryItem.name();
                break;
            case 'p':
            case 'P':
                if(secondaryItem!=null)
                    middle=secondaryItem.name();
                break;
            case 'r':
            case 'R':
                randMOB=getRandPC(monster,tmp,lastKnownLocation);
                if(randMOB!=null)
                    middle=randMOB.name();
                break;
            case 's':
                if(source!=null)
                    middle=source.charStats().himher();
                break;
            case 'S':
                if((target!=null)&&(target instanceof MOB))
                    middle=((MOB)target).charStats().himher();
                break;
            case 't':
            case 'T':
                if(target!=null)
                    middle=target.name();
                break;
            case 'w':
                middle=primaryItem!=null?primaryItem.owner().Name():middle;
                break;
            case 'W':
                middle=secondaryItem!=null?secondaryItem.owner().Name():middle;
                break;
            case 'x':
            case 'X':
                if(lastKnownLocation!=null)
                {
                    middle="";
                    Exit E=null;
                    int dir=-1;
                    if((t<varifyable.length()-2)&&(Directions.getGoodDirectionCode(""+varifyable.charAt(t+2))>=0))
                    {
                        dir=Directions.getGoodDirectionCode(""+varifyable.charAt(t+2));
                        E=lastKnownLocation.getExitInDir(dir);
                    }
                    else
                    {
                        int i=0;
                        while(((++i)<100)||(E!=null))
                        {
                            dir=CMLib.dice().roll(1,Directions.NUM_DIRECTIONS(),-1);
                            E=lastKnownLocation.getExitInDir(dir);
                        }
                    }
                    if((dir>=0)&&(E!=null))
                    {
                        if(c=='x')
                            middle=Directions.getDirectionName(dir);
                        else
                            middle=E.name();
                    }
                }
                break;
            case 'y':
                if(source!=null)
                    middle=source.charStats().sirmadam();
                break;
            case 'Y':
                if((target!=null)&&(target instanceof MOB))
                    middle=((MOB)target).charStats().sirmadam();
                break;
            case '<':
                {
                    int x=back.indexOf(">");
                    if(x>=0)
                    {
                        String mid=back.substring(0,x);
                        int y=mid.indexOf(" ");
                        Environmental E=null;
                        String arg1="";
                        if(y>=0)
                        {
                            arg1=mid.substring(0,y).trim();
                            E=getArgumentItem(arg1,source,monster,monster,target,primaryItem,secondaryItem,msg,tmp);
                            mid=mid.substring(y+1).trim();
                        }
                        if(arg1.length()>0)
                            middle=getVar(E,arg1,mid,source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp);
                        back=back.substring(x+1);
                    }
                }
                break;
            case '[':
                {
                    middle="";
                    int x=back.indexOf("]");
                    if(x>=0)
                    {
                        String mid=back.substring(0,x);
                        int y=mid.indexOf(" ");
                        if(y>0)
                        {
                            int num=CMath.s_int(mid.substring(0,y).trim());
                            mid=mid.substring(y+1).trim();
                            Quest Q=getQuest(mid);
                            if(Q!=null) middle=Q.getQuestItemName(num);
                        }
                        back=back.substring(x+1);
                    }
                }
                break;
            case '{':
                {
                    middle="";
                    int x=back.indexOf("}");
                    if(x>=0)
                    {
                        String mid=back.substring(0,x).trim();
                        int y=mid.indexOf(" ");
                        if(y>0)
                        {
                            int num=CMath.s_int(mid.substring(0,y).trim());
                            mid=mid.substring(y+1).trim();
                            Quest Q=getQuest(mid);
                            if(Q!=null) middle=Q.getQuestMobName(num);
                        }
                        back=back.substring(x+1);
                    }
                }
                break;
            case '%':
                {
                    middle="";
                    int x=back.indexOf("%");
                    if(x>=0)
                    {
                        middle=functify(monster,source,target,monster,primaryItem,secondaryItem,msg,tmp,back.substring(0,x).trim());
                        back=back.substring(x+1);
                    }
                }
                break;
            }
            if((back.startsWith("."))
            &&(back.length()>1))
            {
                if(back.charAt(1)=='$')
                    back=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,back);
                if(back.equalsIgnoreCase(".LENGTH#"))
                {
                    middle=""+CMParms.parse(middle).size();
                    back="";
                }
                else
                if((back.length()>1)&&Character.isDigit(back.charAt(1)))
                {
                    int x=1;
                    while((x<back.length())
                    &&(Character.isDigit(back.charAt(x))))
                        x++;
                    int y=CMath.s_int(back.substring(1,x).trim());
                    back=back.substring(x);
                    boolean rest=back.startsWith("..");
                    if(rest) back=back.substring(2);
                    Vector V=CMParms.parse(middle);
                    if((V.size()>0)&&(y>=0))
                    {
                        if(y>=V.size())
                            middle="";
                        else
                        if(rest)
                            middle=CMParms.combine(V,y);
                        else
                            middle=(String)V.elementAt(y);
                    }
                }
            }
            varifyable=front+middle+back;
            t=varifyable.indexOf("$");
        }
        return varifyable;
    }

    protected DVector getScriptVarSet(String mobname, String varname)
    {
        DVector set=new DVector(2);
        if(mobname.equals("*"))
        {
            Vector V=resources._findResourceKeys("SCRIPTVAR-");
            for(int v=0;v<V.size();v++)
            {
                String key=(String)V.elementAt(v);
                if(key.startsWith("SCRIPTVAR-"))
                {
                    Hashtable H=(Hashtable)resources._getResource(key);
                    if(varname.equals("*"))
                    {
                        for(Enumeration e=H.keys();e.hasMoreElements();)
                        {
                            String vn=(String)e.nextElement();
                            set.addElement(key.substring(10),vn);
                        }
                    }
                    else
                        set.addElement(key.substring(10),varname);
                }
            }
        }
        else
        {
            Hashtable H=(Hashtable)resources._getResource("SCRIPTVAR-"+mobname);
            if(varname.equals("*"))
            {
                for(Enumeration e=H.keys();e.hasMoreElements();)
                {
                    String vn=(String)e.nextElement();
                    set.addElement(mobname,vn);
                }
            }
            else
                set.addElement(mobname,varname);
        }
        return set;
    }

    protected String getStatValue(Environmental E, String arg2)
    {
        boolean found=false;
        String val="";
        for(int i=0;i<E.getStatCodes().length;i++)
        {
            if(E.getStatCodes()[i].equalsIgnoreCase(arg2))
            {
                val=E.getStat(arg2);
                found=true;
                break;
            }
        }
        if((!found)&&(E instanceof MOB))
        {
            MOB M=(MOB)E;
            for(int i : CharStats.CODES.ALL())
                if(CharStats.CODES.NAME(i).equalsIgnoreCase(arg2)||CharStats.CODES.DESC(i).equalsIgnoreCase(arg2))
                {
                    val=""+M.charStats().getStat(CharStats.CODES.NAME(i)); //yes, this is right
                    found=true;
                    break;
                }
            if(!found)
            for(int i=0;i<M.curState().getStatCodes().length;i++)
                if(M.curState().getStatCodes()[i].equalsIgnoreCase(arg2))
                {
                    val=M.curState().getStat(M.curState().getStatCodes()[i]);
                    found=true;
                    break;
                }
            if(!found)
            for(int i=0;i<M.envStats().getStatCodes().length;i++)
                if(M.envStats().getStatCodes()[i].equalsIgnoreCase(arg2))
                {
                    val=M.envStats().getStat(M.envStats().getStatCodes()[i]);
                    found=true;
                    break;
                }
            if((!found)&&(M.playerStats()!=null))
                for(int i=0;i<M.playerStats().getStatCodes().length;i++)
                    if(M.playerStats().getStatCodes()[i].equalsIgnoreCase(arg2))
                    {
                        val=M.playerStats().getStat(M.playerStats().getStatCodes()[i]);
                        found=true;
                        break;
                    }
            if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
                for(int i=0;i<M.baseState().getStatCodes().length;i++)
                    if(M.baseState().getStatCodes()[i].equalsIgnoreCase(arg2.substring(4)))
                    {
                        val=M.baseState().getStat(M.baseState().getStatCodes()[i]);
                        found=true;
                        break;
                    }
            if((!found)&&(gstatH.containsKey(arg2.toUpperCase()))) {
                found=true;
                switch(((Integer)gstatH.get(arg2.toUpperCase())).intValue()) {
                case GSTATADD_DEITY: val=M.getWorshipCharID(); break;
                case GSTATADD_CLAN: val=M.getClanID(); break;
                case GSTATADD_CLANROLE: val=""+M.getClanRole(); break;
                }
            }
        }
        if(!found)return null;
        return val;
    }
    protected String getGStatValue(Environmental E, String arg2)
    {
        if(E==null) return null;
        boolean found=false;
        String val="";
        for(int i=0;i<E.getStatCodes().length;i++)
        {
            if(E.getStatCodes()[i].equalsIgnoreCase(arg2))
            {
                val=E.getStat(arg2);
                found=true; break;
            }
        }
        if(!found)
        if(E instanceof MOB)
        {
            for(int i=0;i<GenericBuilder.GENMOBCODES.length;i++)
            {
                if(GenericBuilder.GENMOBCODES[i].equalsIgnoreCase(arg2))
                {
                    val=CMLib.coffeeMaker().getGenMobStat((MOB)E,GenericBuilder.GENMOBCODES[i]);
                    found=true; break;
                }
            }
            if(!found)
            {
                MOB M=(MOB)E;
                for(int i : CharStats.CODES.ALL())
                    if(CharStats.CODES.NAME(i).equalsIgnoreCase(arg2)||CharStats.CODES.DESC(i).equalsIgnoreCase(arg2))
                    {
                        val=""+M.charStats().getStat(CharStats.CODES.NAME(i));
                        found=true;
                        break;
                    }
                if(!found)
                for(int i=0;i<M.curState().getStatCodes().length;i++)
                    if(M.curState().getStatCodes()[i].equalsIgnoreCase(arg2))
                    {
                        val=M.curState().getStat(M.curState().getStatCodes()[i]);
                        found=true;
                        break;
                    }
                if(!found)
                for(int i=0;i<M.envStats().getStatCodes().length;i++)
                    if(M.envStats().getStatCodes()[i].equalsIgnoreCase(arg2))
                    {
                        val=M.envStats().getStat(M.envStats().getStatCodes()[i]);
                        found=true;
                        break;
                    }
                if((!found)&&(M.playerStats()!=null))
                for(int i=0;i<M.playerStats().getStatCodes().length;i++)
                    if(M.playerStats().getStatCodes()[i].equalsIgnoreCase(arg2))
                    {
                        val=M.playerStats().getStat(M.playerStats().getStatCodes()[i]);
                        found=true;
                        break;
                    }
                if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
                    for(int i=0;i<M.baseState().getStatCodes().length;i++)
                        if(M.baseState().getStatCodes()[i].equalsIgnoreCase(arg2.substring(4)))
                        {
                            val=M.baseState().getStat(M.baseState().getStatCodes()[i]);
                            found=true;
                            break;
                        }
                if((!found)&&(gstatH.containsKey(arg2.toUpperCase()))) {
                    found=true;
                    switch(((Integer)gstatH.get(arg2.toUpperCase())).intValue()) {
                    case GSTATADD_DEITY: val=M.getWorshipCharID(); break;
                    case GSTATADD_CLAN: val=M.getClanID(); break;
                    case GSTATADD_CLANROLE: val=""+M.getClanRole(); break;
                    }
                }
            }
        }
        else
        if(E instanceof Item)
        {
            for(int i=0;i<GenericBuilder.GENITEMCODES.length;i++)
            {
                if(GenericBuilder.GENITEMCODES[i].equalsIgnoreCase(arg2))
                {
                    val=CMLib.coffeeMaker().getGenItemStat((Item)E,GenericBuilder.GENITEMCODES[i]);
                    found=true; break;
                }
            }
        }
        if(found) return val;
        return null;
    }


    public void setVar(String name, String key, String val)
    {
        DVector V=getScriptVarSet(name,key);
        for(int v=0;v<V.size();v++)
        {
            name=(String)V.elementAt(v,1);
            key=((String)V.elementAt(v,2)).toUpperCase();
            Hashtable H=(Hashtable)resources._getResource("SCRIPTVAR-"+name);
            if((H==null)&&(defaultQuestName!=null)&&(defaultQuestName.length()>0))
            {
                MOB M=CMLib.players().getPlayer(name);
                if(M!=null)
                    for(int s=0;s<M.numScripts();s++)
                    {
                        ScriptingEngine E=M.fetchScript(s);
                        if((E!=null)
                        &&(E!=this)
                        &&(defaultQuestName.equalsIgnoreCase(E.defaultQuestName()))
                        &&(E.isVar(name,key)))
                        {
                            E.setVar(name,key,val);
                            return;
                        }
                    }
            }
            if(H==null)
            {
                if(val.length()==0)
                    continue;

                H=new Hashtable();
                resources._submitResource("SCRIPTVAR-"+name,H);
            }
            if(val.equals("++"))
            {
                String num=(String)H.get(key);
                if(num==null) num="0";
                val=Integer.toString(CMath.s_int(num.trim())+1);
            }
            else
            if(val.equals("--"))
            {
                String num=(String)H.get(key);
                if(num==null) num="0";
                val=Integer.toString(CMath.s_int(num.trim())-1);
            }
            else
            if(val.startsWith("+"))
            {
                // add via +number form
                val=val.substring(1);
                int amount=CMath.s_int(val.trim());
                String num=(String)H.get(key);
                if(num==null) num="0";
                val=Integer.toString(CMath.s_int(num.trim())+amount);
            }
            else
            if(val.startsWith("-"))
            {
                // subtract -number form
                val=val.substring(1);
                int amount=CMath.s_int(val.trim());
                String num=(String)H.get(key);
                if(num==null) num="0";
                val=Integer.toString(CMath.s_int(num.trim())-amount);
            }
            else
            if(val.startsWith("*"))
            {
                // multiply via *number form
                val=val.substring(1);
                int amount=CMath.s_int(val.trim());
                String num=(String)H.get(key);
                if(num==null) num="0";
                val=Integer.toString(CMath.s_int(num.trim())*amount);
            }
            else
            if(val.startsWith("/"))
            {
                // divide /number form
                val=val.substring(1);
                int amount=CMath.s_int(val.trim());
                String num=(String)H.get(key);
                if(num==null) num="0";
                val=Integer.toString(CMath.s_int(num.trim())/amount);
            }
            if(H.containsKey(key))
                H.remove(key);
            if(val.trim().length()>0)
                H.put(key,val);
            if(H.size()==0)
                resources._removeResource("SCRIPTVAR-"+name);
        }
    }

    public String[] parseEval(String evaluable) throws ScriptParseException
    {
        final int STATE_MAIN=0;
        final int STATE_INFUNCTION=1;
        final int STATE_INFUNCQUOTE=2;
        final int STATE_POSTFUNCTION=3;
        final int STATE_POSTFUNCEVAL=4;
        final int STATE_POSTFUNCQUOTE=5;
        final int STATE_MAYFUNCTION=6;
        
        Vector V=new Vector();
        if((evaluable==null)||(evaluable.trim().length()==0))
            return new String[]{};
        char[] evalC=evaluable.toCharArray();
        int state=0;
        int dex=0;
        char lastQuote='\0';
        String s=null;
        int depth=0;
        for(int c=0;c<evalC.length;c++)
            switch(state) {
            case STATE_MAIN:
            {
                if(Character.isWhitespace(evalC[c]))
                {
                    s=new String(evalC,dex,c-dex).trim();
                    if(s.length()>0)
                    {
                        s=s.toUpperCase();
                        V.addElement(s);
                        dex=c+1;
                        if(funcH.containsKey(s))
                            state=STATE_MAYFUNCTION;
                        else
                        if(!connH.containsKey(s))
                            throw new ScriptParseException("Unknown keyword: "+s);
                    }
                }
                else
                if(Character.isLetter(evalC[c]))
                { /* move along */ }
                else
                switch(evalC[c])
                {
                case '!':
                {
                    if(c==evalC.length-1)
                        throw new ScriptParseException("Bad Syntax on last !");
                    V.addElement("NOT");
                    dex=c+1;
                    break;
                }
                case '(':
                {
                    s=new String(evalC,dex,c-dex).trim();
                    if(s.length()>0)
                    {
                        s=s.toUpperCase();
                        V.addElement(s);
                        V.addElement("(");
                        dex=c+1;
                        if(funcH.containsKey(s))
                            state=STATE_INFUNCTION;
                        else
                        if(connH.containsKey(s))
                            state=STATE_MAIN;
                        else
                            throw new ScriptParseException("Unknown keyword: "+s);
                    }
                    else
                    {
                        V.addElement("(");
                        depth++;
                        dex=c+1;
                    }
                    break;
                }
                case ')':
                    s=new String(evalC,dex,c-dex).trim();
                    if(s.length()>0)
                        throw new ScriptParseException("Bad syntax before ) at: "+s);
                    if(depth==0)
                        throw new ScriptParseException("Unmatched ) character");
                    V.addElement(")");
                    depth--;
                    dex=c+1;
                    break;
                default:
                    throw new ScriptParseException("Unknown character at: "+new String(evalC,dex,c-dex+1).trim()+": "+evaluable);
                }
                break;
            }
            case STATE_MAYFUNCTION:
            {
                if(evalC[c]=='(')
                {
                    V.addElement("(");
                    dex=c+1;
                    state=STATE_INFUNCTION;
                }
                else
                if(!Character.isWhitespace(evalC[c]))
                    throw new ScriptParseException("Expected ( at "+evalC[c]+": "+evaluable);
                break;
            }
            case STATE_POSTFUNCTION:
            {
                if(!Character.isWhitespace(evalC[c]))
                    switch(evalC[c])
                    {
                    case '=': case '>': case '<': case '!':
                    {
                        if(c==evalC.length-1)
                            throw new ScriptParseException("Bad Syntax on last "+evalC[c]);
                        if(!Character.isWhitespace(evalC[c+1]))
                        {
                            s=new String(evalC,c,2);
                            if((!signH.containsKey(s))&&(evalC[c]!='!'))
                                s=""+evalC[c];
                        }
                        else
                            s=""+evalC[c];
                        if(!signH.containsKey(s))
                        {
                            c=dex-1;
                            state=STATE_MAIN;
                            break;
                        }
                        V.addElement(s);
                        dex=c+(s.length());
                        c=c+(s.length()-1);
                        state=STATE_POSTFUNCEVAL;
                        break;
                    }
                    default:
                        c=dex-1;
                        state=STATE_MAIN;
                        break;
                    }
                break;
            }
            case STATE_INFUNCTION:
            {
                if(evalC[c]==')')
                {
                    V.addElement(new String(evalC,dex,c-dex));
                    V.addElement(")");
                    dex=c+1;
                    state=STATE_POSTFUNCTION;
                }
                else
                if((evalC[c]=='\'')||(evalC[c]=='`'))
                {
                    lastQuote=evalC[c];
                    state=STATE_INFUNCQUOTE;
                }
                break;
            }
            case STATE_INFUNCQUOTE:
            {
                if(evalC[c]==lastQuote)
                    state=STATE_INFUNCTION;
                break;
            }
            case STATE_POSTFUNCQUOTE:
            {
                if(evalC[c]==lastQuote)
                {
                    if((V.size()>2)
                    &&(signH.containsKey((String)V.lastElement()))
                    &&(((String)V.elementAt(V.size()-2)).equals(")")))
                    {
                        String sign=(String)V.lastElement();
                        V.removeElementAt(V.size()-1);
                        V.removeElementAt(V.size()-1);
                        String prev=(String)V.lastElement();
                        if(prev.equals("("))
                            s=sign+" "+new String(evalC,dex+1,c-dex);
                        else
                        {
                            V.removeElementAt(V.size()-1);
                            s=prev+" "+sign+" "+new String(evalC,dex+1,c-dex);
                        }
                        V.addElement(s);
                        V.addElement(")");
                        dex=c+1;
                        state=STATE_MAIN;
                    }
                    else
                        throw new ScriptParseException("Bad postfunc Eval somewhere");
                }
                break;
            }
            case STATE_POSTFUNCEVAL:
            {
                if(Character.isWhitespace(evalC[c]))
                {
                    s=new String(evalC,dex,c-dex).trim();
                    if(s.length()>0)
                    {
                        if((V.size()>1)
                        &&(signH.containsKey((String)V.lastElement()))
                        &&(((String)V.elementAt(V.size()-2)).equals(")")))
                        {
                            String sign=(String)V.lastElement();
                            V.removeElementAt(V.size()-1);
                            V.removeElementAt(V.size()-1);
                            String prev=(String)V.lastElement();
                            if(prev.equals("("))
                                s=sign+" "+new String(evalC,dex+1,c-dex);
                            else
                            {
                                V.removeElementAt(V.size()-1);
                                s=prev+" "+sign+" "+new String(evalC,dex+1,c-dex);
                            }
                            V.addElement(s);
                            V.addElement(")");
                            dex=c+1;
                            state=STATE_MAIN;
                        }
                        else
                            throw new ScriptParseException("Bad postfunc Eval somewhere");
                    }
                }
                else
                if(Character.isLetterOrDigit(evalC[c]))
                { /* move along */ }
                else
                if((evalC[c]=='\'')||(evalC[c]=='`'))
                {
                    s=new String(evalC,dex,c-dex).trim();
                    if(s.length()==0)
                    {
                        lastQuote=evalC[c];
                        state=STATE_POSTFUNCQUOTE;
                    }
                }
                break;
            }
            }
        if((state==STATE_POSTFUNCQUOTE)
        ||(state==STATE_INFUNCQUOTE))
            throw new ScriptParseException("Unclosed "+lastQuote+" somewhere");
        if(depth>0)
            throw new ScriptParseException("Unclosed ( somewhere");
        return CMParms.toStringArray(V);
    }

    public void pushEvalBoolean(Vector stack, boolean trueFalse)
    {
        if(stack.size()>0)
        {
            Object O=stack.elementAt(stack.size()-1);
            if(O instanceof Integer)
            {
                int connector=((Integer)O).intValue();
                stack.removeElementAt(stack.size()-1);
                if((stack.size()>0)
                &&((stack.elementAt(stack.size()-1) instanceof Boolean)))
                {
                    boolean preTrueFalse=((Boolean)stack.elementAt(stack.size()-1)).booleanValue();
                    stack.removeElementAt(stack.size()-1);
                    switch(connector)
                    {
                    case CONNECTOR_AND: trueFalse=preTrueFalse&&trueFalse; break;
                    case CONNECTOR_OR: trueFalse=preTrueFalse||trueFalse; break;
                    case CONNECTOR_ANDNOT: trueFalse=preTrueFalse&&(!trueFalse); break;
                    case CONNECTOR_NOT: 
                    case CONNECTOR_ORNOT: trueFalse=preTrueFalse||(!trueFalse); break;
                    }
                }
                else
                switch(connector)
                {
                case CONNECTOR_ANDNOT:
                case CONNECTOR_NOT: 
                case CONNECTOR_ORNOT: trueFalse=!trueFalse; break;
                default: break;
                }
            }
            else
            if(O instanceof Boolean)
            {
                boolean preTrueFalse=((Boolean)stack.elementAt(stack.size()-1)).booleanValue();
                stack.removeElementAt(stack.size()-1);
                trueFalse=preTrueFalse&&trueFalse;
            }
        }
        stack.addElement(trueFalse?Boolean.TRUE:Boolean.FALSE);
    }
    
    public boolean eval(Environmental scripted,
                        MOB source,
                        Environmental target,
                        MOB monster,
                        Item primaryItem,
                        Item secondaryItem,
                        String msg,
                        Object[] tmp,
                        String[][] eval,
                        int startEval)
    {
        String[] tt=(String[])eval[0];
        if(tmp == null) tmp = newObjs();
        Vector stack=new Vector();
        for(int t=startEval;t<tt.length;t++)
        if(tt[t].equals("("))
            stack.addElement(tt[t]);
        else
        if(tt[t].equals(")"))
        {
            if((!(stack.lastElement() instanceof Boolean))
            ||(stack.size()==1)
            ||(!(stack.elementAt(stack.size()-2)).equals("(")))
            {
                logError(scripted,"EVAL","SYNTAX",") Format error: "+CMParms.toStringList(tt));
                return false;
            }
            boolean b=((Boolean)stack.lastElement()).booleanValue();
            stack.removeElementAt(stack.size()-1);
            stack.removeElementAt(stack.size()-1);
            pushEvalBoolean(stack,b);
        }
        else
        if(connH.containsKey(tt[t]))
        {
            Integer curr=(Integer)connH.get(tt[t]);
            if((stack.size()>0)&&(stack.lastElement() instanceof Integer))
            {
                int old=((Integer)stack.lastElement()).intValue();
                stack.removeElementAt(stack.size()-1);
                curr=Integer.valueOf(CONNECTOR_MAP[old][curr.intValue()]);
            }
            stack.addElement(curr);
        }
        else
        if(funcH.containsKey(tt[t]))
        {
            Integer funcCode=(Integer)funcH.get(tt[t]);
            if((t==tt.length-1)
            ||(!tt[t+1].equals("(")))
            {
                logError(scripted,"EVAL","SYNTAX","No ( for fuction "+tt[t]+": "+CMParms.toStringList(tt));
                return false;
            }
            t+=2;
            int tlen=0;
            while(((t+tlen)<tt.length)&&(!tt[t+tlen].equals(")")))
                tlen++;
            if((t+tlen)==tt.length)
            {
                logError(scripted,"EVAL","SYNTAX","No ) for fuction "+tt[t-1]+": "+CMParms.toStringList(tt));
                return false;
            }
            tickStatus=Tickable.STATUS_MISC+funcCode.intValue();
            String funcParms=tt[t];
            boolean returnable=false;
            switch(funcCode.intValue())
            {
            case 1: // rand
            {
                String num=funcParms;
                if(num.endsWith("%")) num=num.substring(0,num.length()-1);
                int arg=CMath.s_int(num);
                if(CMLib.dice().rollPercentage()<arg)
                    returnable=true;
                else
                    returnable=false;
                break;
            }
            case 2: // has
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(arg2.length()==0)
                {
                    logError(scripted,"HAS","Syntax",funcParms);
                    return returnable;
                }
                Environmental E2=getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                if(E instanceof MOB)
                {
                    if(E2!=null)
                        returnable=((MOB)E).isMine(E2);
                    else
                        returnable=(((MOB)E).fetchInventory(arg2)!=null);
                }
                else
                if(E instanceof Item)
                    returnable=CMLib.english().containsString(E.name(),arg2);
                else
                if(E instanceof Room)
                {
                    if(E2 instanceof Item)
                        returnable=((Room)E).isContent((Item)E2);
                    else
                        returnable=(((Room)E).fetchItem(null,arg2)!=null);
                }
                else
                    returnable=false;
                break;
            }
            case 74: // hasnum
            {
                if(tlen==1) tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String item=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                String cmp=tt[t+2];
                String value=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((value.length()==0)||(item.length()==0)||(cmp.length()==0))
                {
                    logError(scripted,"HASNUM","Syntax",funcParms);
                    return returnable;
                }
                Item I=null;
                int num=0;
                if(E==null)
                    returnable=false;
                else
                if(E instanceof MOB)
                {
                    MOB M=(MOB)E;
                    for(int i=0;i<M.inventorySize();i++)
                    {
                        I=M.fetchInventory(i);
                        if(I==null) break;
                        if((item.equalsIgnoreCase("all"))
                        ||(CMLib.english().containsString(I.Name(),item)))
                            num++;
                    }
                    returnable=simpleEval(scripted,""+num,value,cmp,"HASNUM");
                }
                else
                if(E instanceof Item)
                {
                    num=CMLib.english().containsString(E.name(),item)?1:0;
                    returnable=simpleEval(scripted,""+num,value,cmp,"HASNUM");
                }
                else
                if(E instanceof Room)
                {
                    Room R=(Room)E;
                    for(int i=0;i<R.numItems();i++)
                    {
                        I=R.fetchItem(i);
                        if(I==null) break;
                        if((item.equalsIgnoreCase("all"))
                        ||(CMLib.english().containsString(I.Name(),item)))
                            num++;
                    }
                    returnable=simpleEval(scripted,""+num,value,cmp,"HASNUM");
                }
                else
                    returnable=false;
                break;
            }
            case 67: // hastitle
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(arg2.length()==0)
                {
                    logError(scripted,"HASTITLE","Syntax",funcParms);
                    return returnable;
                }
                if(E instanceof MOB)
                {
                    MOB M=(MOB)E;
                    returnable=(M.playerStats()!=null)&&(M.playerStats().getTitles().contains(arg2));
                }
                else
                    returnable=false;
                break;
            }
            case 3: // worn
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(arg2.length()==0)
                {
                    logError(scripted,"WORN","Syntax",funcParms);
                    return returnable;
                }
                if(E==null)
                    returnable=false;
                else
                if(E instanceof MOB)
                    returnable=(((MOB)E).fetchWornItem(arg2)!=null);
                else
                if(E instanceof Item)
                    returnable=(CMLib.english().containsString(E.name(),arg2)&&(!((Item)E).amWearingAt(Wearable.IN_INVENTORY)));
                else
                    returnable=false;
                break;
            }
            case 4: // isnpc
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                    returnable=((MOB)E).isMonster();
                break;
            }
            case 87: // isbirthday
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    MOB mob=(MOB)E;
                    if(mob.playerStats()==null)
                         returnable=false;
                    else
                    {
                        int tage=mob.baseCharStats().getMyRace().getAgingChart()[Race.AGE_YOUNGADULT]
                                                                                 +CMLib.time().globalClock().getYear()
                                                                                 -mob.playerStats().getBirthday()[2];
                         int month=CMLib.time().globalClock().getMonth();
                         int day=CMLib.time().globalClock().getDayOfMonth();
                         int bday=mob.playerStats().getBirthday()[0];
                         int bmonth=mob.playerStats().getBirthday()[1];
                         if((tage>mob.baseCharStats().getStat(CharStats.STAT_AGE))
                         &&((month==bmonth)&&(day==bday)))
                             returnable=true;
                         else
                             returnable=false;
                    }
                }
                break;
            }
            case 5: // ispc
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                    returnable=!((MOB)E).isMonster();
                break;
            }
            case 6: // isgood
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                    returnable=CMLib.flags().isGood(E);
                break;
            }
            case 8: // isevil
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                    returnable=CMLib.flags().isEvil(E);
                break;
            }
            case 9: // isneutral
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                    returnable=CMLib.flags().isNeutral(E);
                break;
            }
            case 54: // isalive
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
                    returnable=true;
                else
                    returnable=false;
                break;
            }
            case 58: // isable
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
                {
                    ExpertiseLibrary X=(ExpertiseLibrary)CMLib.expertises().findDefinition(arg2,true);
                    if(X!=null)
                        returnable=((MOB)E).fetchExpertise(X.ID())!=null;
                    else
                        returnable=((MOB)E).findAbility(arg2)!=null;
                }
                else
                    returnable=false;
                break;
            }
            case 59: // isopen
            {
                String arg1=CMParms.cleanBit(funcParms);
                int dir=Directions.getGoodDirectionCode(arg1);
                returnable=false;
                if(dir<0)
                {
                    Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    if((E!=null)&&(E instanceof Container))
                        returnable=((Container)E).isOpen();
                    else
                    if((E!=null)&&(E instanceof Exit))
                        returnable=((Exit)E).isOpen();
                }
                else
                if(lastKnownLocation!=null)
                {
                    Exit E=lastKnownLocation.getExitInDir(dir);
                    if(E!=null) returnable= E.isOpen();
                }
                break;
            }
            case 60: // islocked
            {
                String arg1=CMParms.cleanBit(funcParms);
                int dir=Directions.getGoodDirectionCode(arg1);
                returnable=false;
                if(dir<0)
                {
                    Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    if((E!=null)&&(E instanceof Container))
                        returnable=((Container)E).isLocked();
                    else
                    if((E!=null)&&(E instanceof Exit))
                        returnable=((Exit)E).isLocked();
                }
                else
                if(lastKnownLocation!=null)
                {
                    Exit E=lastKnownLocation.getExitInDir(dir);
                    if(E!=null) returnable= E.isLocked();
                }
                break;
            }
            case 10: // isfight
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                    returnable=((MOB)E).isInCombat();
                break;
            }
            case 11: // isimmort
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                    returnable=CMSecurity.isAllowed(((MOB)E),lastKnownLocation,"IMMORT");
                break;
            }
            case 12: // ischarmed
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                    returnable=CMLib.flags().flaggedAffects(E,Ability.FLAG_CHARMING).size()>0;
                break;
            }
            case 15: // isfollow
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                if(((MOB)E).amFollowing()==null)
                    returnable=false;
                else
                if(((MOB)E).amFollowing().location()!=lastKnownLocation)
                    returnable=false;
                else
                    returnable=true;
                break;
            }
            case 73: // isservant
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB))||(lastKnownLocation==null))
                    returnable=false;
                else
                if((((MOB)E).getLiegeID()==null)||(((MOB)E).getLiegeID().length()==0))
                    returnable=false;
                else
                if(lastKnownLocation.fetchInhabitant("$"+((MOB)E).getLiegeID()+"$")==null)
                    returnable=false;
                else
                    returnable=true;
                break;
            }
            case 55: // ispkill
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                if(CMath.bset(((MOB)E).getBitmap(),MOB.ATT_PLAYERKILL))
                    returnable=true;
                else
                    returnable=false;
                break;
            }
            case 7: // isname
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                    returnable=CMLib.english().containsString(E.name(),arg2);
                break;
            }
            case 56: // name
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                    returnable=simpleEvalStr(scripted,E.Name(),arg3,arg2,"NAME");
                break;
            }
            case 75: // currency
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                    returnable=simpleEvalStr(scripted,CMLib.beanCounter().getCurrency(E),arg3,arg2,"CURRENCY");
                break;
            }
            case 61: // strin
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                Vector V=CMParms.parse(arg1.toUpperCase());
                returnable=V.contains(arg2.toUpperCase());
                break;
            }
            case 62: // callfunc
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                String found=null;
                boolean validFunc=false;
                Vector scripts=getScripts();
                String trigger=null;
                String[] ttrigger=null;
                for(int v=0;v<scripts.size();v++)
                {
                    DVector script2=(DVector)scripts.elementAt(v);
                    if(script2.size()<1) continue;
                    trigger=((String)script2.elementAt(0,1)).toUpperCase().trim();
                    ttrigger=(String[])script2.elementAt(0,2);
                    if(getTriggerCode(trigger,ttrigger)==17)
                    {
                        String fnamed=
                            (ttrigger!=null)
                            ?ttrigger[1]
                            :CMParms.getCleanBit(trigger,1);
                        if(fnamed.equalsIgnoreCase(arg1))
                        {
                            validFunc=true;
                            found=
                            execute(scripted,
                                    source,
                                    target,
                                    monster,
                                    primaryItem,
                                    secondaryItem,
                                    script2,
                                    varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg2),
                                    tmp);
                            if(found==null) found="";
                            break;
                        }
                    }
                }
                if(!validFunc)
                    logError(scripted,"CALLFUNC","Unknown","Function: "+arg1);
                else
                if(found!=null)
                    returnable=!(found.trim().length()==0);
                else
                	returnable=false;
                break;
            }
            case 14: // affected
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                {
                    Ability A=CMClass.findAbility(arg2);
                    if(A!=null) arg2=A.ID();
                    returnable=(E.fetchEffect(arg2)!=null);
                }
                break;
            }
            case 69: // isbehave
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                {
                    Behavior B=CMClass.findBehavior(arg2);
                    if(B!=null) arg2=B.ID();
                    returnable=(E.fetchBehavior(arg2)!=null);
                }
                break;
            }
            case 70: // ipaddress
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB))||(((MOB)E).isMonster()))
                    returnable=false;
                else
                    returnable=simpleEvalStr(scripted,((MOB)E).session().getAddress(),arg3,arg2,"ADDRESS");
                break;
            }
            case 28: // questwinner
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=tt[t+1];
                Environmental E=getArgumentMOB(tt[t+0],source,monster,target,primaryItem,secondaryItem,msg,tmp);
                Quest Q=getQuest(arg2);
                if(Q==null)
                    returnable=false;
                else
                {
                    if(E!=null) arg1=E.Name();
                    returnable=Q.wasWinner(arg1);
                }
                break;
            }
            case 93: // questscripted
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                Environmental E=getArgumentMOB(tt[t+0],source,monster,target,primaryItem,secondaryItem,msg,tmp);
                String arg2=tt[t+1];
                Quest Q=getQuest(arg2);
                returnable=false;
                if((Q!=null)&&(E!=null))
                {
                    for(int i=0;i<E.numScripts();i++)
                    {
                        ScriptingEngine S=E.fetchScript(i);
                        if((S!=null)&&(S.defaultQuestName().equalsIgnoreCase(Q.name())))
                            returnable=true;
                    }
                }
                break;
            }
            case 94: // questroom
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=tt[t+1];
                Quest Q=getQuest(arg2);
                if(Q==null)
                    returnable=false;
                else
                    returnable=(Q.getQuestRoomIndex(arg1)>=0);
                break;
            }
            case 29: // questmob
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=tt[t+1];
                Quest Q=getQuest(arg2);
                if(Q==null)
                    returnable=false;
                else
                    returnable=(Q.getQuestMobIndex(arg1)>=0);
                break;
            }
            case 31: // isquestmobalive
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=tt[t+1];
                Quest Q=getQuest(arg2);
                if(Q==null)
                    returnable=false;
                else
                {
                    MOB M=null;
                    if(CMath.s_int(arg1.trim())>0)
                        M=Q.getQuestMob(CMath.s_int(arg1.trim()));
                    else
                        M=Q.getQuestMob(Q.getQuestMobIndex(arg1));
                    if(M==null) returnable=false;
                    else returnable=!M.amDead();
                }
                break;
            }
            case 32: // nummobsinarea
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase();
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                int num=0;
                Vector MASK=null;
                if((arg3.toUpperCase().startsWith("MASK")&&(arg3.substring(4).trim().startsWith("="))))
                {
                    arg3=arg3.substring(4).trim();
                    arg3=arg3.substring(1).trim();
                    MASK=CMLib.masking().maskCompile(arg3);
                }
                for(Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
                {
                    Room R=(Room)e.nextElement();
                    for(int m=0;m<R.numInhabitants();m++)
                    {
                        MOB M=R.fetchInhabitant(m);
                        if(M==null) continue;
                        if(MASK!=null)
                        {
                            if(CMLib.masking().maskCheck(MASK,M,true))
                                num++;
                        }
                        else
                        if(CMLib.english().containsString(M.name(),arg1))
                            num++;
                    }
                }
                returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMMOBSINAREA");
                break;
            }
            case 33: // nummobs
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase();
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                int num=0;
                Vector MASK=null;
                if((arg3.toUpperCase().startsWith("MASK")&&(arg3.substring(4).trim().startsWith("="))))
                {
                    arg3=arg3.substring(4).trim();
                    arg3=arg3.substring(1).trim();
                    MASK=CMLib.masking().maskCompile(arg3);
                }
                try
                {
                    for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
                    {
                        Room R=(Room)e.nextElement();
                        for(int m=0;m<R.numInhabitants();m++)
                        {
                            MOB M=R.fetchInhabitant(m);
                            if(M==null) continue;
                            if(MASK!=null)
                            {
                                if(CMLib.masking().maskCheck(MASK,M,true))
                                    num++;
                            }
                            else
                            if(CMLib.english().containsString(M.name(),arg1))
                                num++;
                        }
                    }
                }catch(NoSuchElementException nse){}
                returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMMOBS");
                break;
            }
            case 34: // numracesinarea
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase();
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                int num=0;
                Room R=null;
                MOB M=null;
                for(Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
                {
                    R=(Room)e.nextElement();
                    for(int m=0;m<R.numInhabitants();m++)
                    {
                        M=R.fetchInhabitant(m);
                        if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
                            num++;
                    }
                }
                returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMRACESINAREA");
                break;
            }
            case 35: // numraces
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase();
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                int num=0;
                try
                {
                    for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
                    {
                        Room R=(Room)e.nextElement();
                        for(int m=0;m<R.numInhabitants();m++)
                        {
                            MOB M=R.fetchInhabitant(m);
                            if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
                                num++;
                        }
                    }
                }catch(NoSuchElementException nse){}
                returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMRACES");
                break;
            }
            case 30: // questobj
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=tt[t+1];
                Quest Q=getQuest(arg2);
                if(Q==null)
                    returnable=false;
                else
                    returnable=(Q.getQuestItemIndex(arg1)>=0);
                break;
            }
            case 85: // islike
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=tt[t+1];
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                    returnable=CMLib.masking().maskCheck(arg2, E,false);
                break;
            }
            case 86: // strcontains
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                returnable=CMParms.stringContains(arg1,arg2)>=0;
                break;
            }
            case 92: // isodd
            {
                String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).trim();
                boolean isodd = false;
                if( CMath.isLong( val ) )
                {
                    isodd = (CMath.s_long(val) %2 == 1);
                }
                returnable = isodd;
                break;
            }
            case 16: // hitprcnt
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"HITPRCNT","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    double hitPctD=CMath.div(((MOB)E).curState().getHitPoints(),((MOB)E).maxState().getHitPoints());
                    int val1=(int)Math.round(hitPctD*100.0);
                    returnable=simpleEval(scripted,""+val1,arg3,arg2,"HITPRCNT");
                }
                break;
            }
            case 50: // isseason
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                returnable=false;
                if(monster.location()!=null)
                for(int a=0;a<TimeClock.SEASON_DESCS.length;a++)
                    if((TimeClock.SEASON_DESCS[a]).startsWith(arg1.toUpperCase())
                    &&(monster.location().getArea().getTimeObj().getSeasonCode()==a))
                    {returnable=true; break;}
                break;
            }
            case 51: // isweather
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                returnable=false;
                if(monster.location()!=null)
                for(int a=0;a<Climate.WEATHER_DESCS.length;a++)
                    if((Climate.WEATHER_DESCS[a]).startsWith(arg1.toUpperCase())
                    &&(monster.location().getArea().getClimateObj().weatherType(monster.location())==a))
                    {returnable=true; break;}
                break;
            }
            case 57: // ismoon
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                returnable=false;
                if(monster.location()!=null)
                {
                    if(arg1.length()==0)
                        returnable=monster.location().getArea().getClimateObj().canSeeTheStars(monster.location());
                    else
                    for(int a=0;a<TimeClock.PHASE_DESC.length;a++)
                        if((TimeClock.PHASE_DESC[a]).startsWith(arg1.toUpperCase())
                        &&(monster.location().getArea().getTimeObj().getMoonPhase()==a))
                        {
                            returnable=true;
                            break;
                        }
                }
                break;
            }
            case 38: // istime
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).toLowerCase().trim();
                if(monster.location()==null)
                    returnable=false;
                else
                if(("daytime").startsWith(arg1)
                &&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TIME_DAY))
                    returnable=true;
                else
                if(("dawn").startsWith(arg1)
                &&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TIME_DAWN))
                    returnable=true;
                else
                if(("dusk").startsWith(arg1)
                &&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TIME_DUSK))
                    returnable=true;
                else
                if(("nighttime").startsWith(arg1)
                &&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TIME_NIGHT))
                    returnable=true;
                else
                if((monster.location().getArea().getTimeObj().getTimeOfDay()==CMath.s_int(arg1)))
                    returnable=true;
                else
                    returnable=false;
                break;
            }
            case 39: // isday
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                if((monster.location()!=null)&&(monster.location().getArea().getTimeObj().getDayOfMonth()==CMath.s_int(arg1.trim())))
                    returnable=true;
                else
                    returnable=false;
                break;
            }
            case 45: // nummobsroom
            {
                if(tlen==1)
                {
                    if(CMParms.numBits(funcParms)>2)
                        tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                    else
                        tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                }
                int num=0;
                int startbit=0;
                if(lastKnownLocation!=null)
                {
                    num=lastKnownLocation.numInhabitants();
                    if(signH.containsKey(tt[t+1]))
                    {
                        String name=tt[t+0];
                        startbit++;
                        if(!name.equalsIgnoreCase("*"))
                        {
                            num=0;
                            Vector MASK=null;
                            if((name.toUpperCase().startsWith("MASK")&&(name.substring(4).trim().startsWith("="))))
                            {
                                name=name.substring(4).trim();
                                name=name.substring(1).trim();
                                MASK=CMLib.masking().maskCompile(name);
                            }
                            for(int i=0;i<lastKnownLocation.numInhabitants();i++)
                            {
                                MOB M=lastKnownLocation.fetchInhabitant(i);
                                if(M==null) continue;
                                if(MASK!=null)
                                {
                                    if(CMLib.masking().maskCheck(MASK,M,true))
                                        num++;
                                }
                                else
                                if(CMLib.english().containsString(M.Name(),name)
                                ||CMLib.english().containsString(M.displayText(),name))
                                    num++;
                            }
                        }
                    }
                }
                else
                if(!signH.containsKey(tt[t+0]))
                {
                    logError(scripted,"NUMMOBSROOM","Syntax","No SIGN found: "+funcParms);
                    return returnable;
                }
                
                String comp=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+startbit]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+startbit+1]);
                if(lastKnownLocation!=null)
                    returnable=simpleEval(scripted,""+num,arg2,comp,"NUMMOBSROOM");
                break;
            }
            case 63: // numpcsroom
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                if(lastKnownLocation!=null)
                    returnable=simpleEval(scripted,""+lastKnownLocation.numPCInhabitants(),arg2,arg1,"NUMPCSROOM");
                break;
            }
            case 79: // numpcsarea
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                if(lastKnownLocation!=null)
                {
                    int num=0;
                    for(int s=0;s<CMLib.sessions().size();s++)
                    {
                        Session S=CMLib.sessions().elementAt(s);
                        if((S!=null)&&(S.mob()!=null)&&(S.mob().location()!=null)&&(S.mob().location().getArea()==lastKnownLocation.getArea()))
                            num++;
                    }
                    returnable=simpleEval(scripted,""+num,arg2,arg1,"NUMPCSAREA");
                }
                break;
            }
            case 77: // explored
            {
                if(tlen==1) tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
                String whom=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String where=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                String cmp=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
                Environmental E=getArgumentMOB(whom,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB)))
                {
                    logError(scripted,"EXPLORED","Unknown Code",whom);
                    return returnable;
                }
                Area A=null;
                if(!where.equalsIgnoreCase("world"))
                {
                	A=CMLib.map().getArea(where);
                	if(A==null)
                	{
	                    Environmental E2=getArgumentItem(where,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
	                    if(E2 != null)
	                    	A=CMLib.map().areaLocation(E2);
                	}
                    if(A==null)
                    {
                        logError(scripted,"EXPLORED","Unknown Area",where);
                        return returnable;
                    }
                }
                if(lastKnownLocation!=null)
                {
                    int pct=0;
                    MOB M=(MOB)E;
                    if(M.playerStats()!=null)
                        pct=M.playerStats().percentVisited(M,A);
                    returnable=simpleEval(scripted,""+pct,arg2,cmp,"EXPLORED");
                }
                break;
            }
            case 72: // faction
            {
                if(tlen==1) tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
                String whom=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                String cmp=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
                Environmental E=getArgumentMOB(whom,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                Faction F=CMLib.factions().getFaction(arg1);
                if((E==null)||(!(E instanceof MOB)))
                {
                    logError(scripted,"FACTION","Unknown Code",whom);
                    return returnable;
                }
                if(F==null)
                {
                    logError(scripted,"FACTION","Unknown Faction",arg1);
                    return returnable;
                }
                MOB M=(MOB)E;
                String value=null;
                if(!M.hasFaction(F.factionID()))
                    value="";
                else
                {
                    int myfac=M.fetchFaction(F.factionID());
                    if(CMath.isNumber(arg2.trim()))
                        value=Integer.toString(myfac);
                    else
                    {
                        Faction.FactionRange FR=CMLib.factions().getRange(F.factionID(),myfac);
                        if(FR==null)
                            value="";
                        else
                            value=FR.name();
                    }
                }
                if(lastKnownLocation!=null)
                    returnable=simpleEval(scripted,value,arg2,cmp,"FACTION");
                break;
            }
            case 46: // numitemsroom
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                int ct=0;
                if(lastKnownLocation!=null)
                for(int i=0;i<lastKnownLocation.numItems();i++)
                {
                    Item I=lastKnownLocation.fetchItem(i);
                    if((I!=null)&&(I.container()==null))
                        ct++;
                }
                returnable=simpleEval(scripted,""+ct,arg2,arg1,"NUMITEMSROOM");
                break;
            }
            case 47: //mobitem
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                MOB M=null;
                if(lastKnownLocation!=null)
                    M=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
                Item which=null;
                int ct=1;
                if(M!=null)
                for(int i=0;i<M.inventorySize();i++)
                {
                    Item I=M.fetchInventory(i);
                    if((I!=null)&&(I.container()==null))
                    {
                        if(ct==CMath.s_int(arg2.trim()))
                        { which=I; break;}
                        ct++;
                    }
                }
                if(which==null)
                    returnable=false;
                else
                    returnable=(CMLib.english().containsString(which.name(),arg3)
                                ||CMLib.english().containsString(which.Name(),arg3)
                                ||CMLib.english().containsString(which.displayText(),arg3));
                break;
            }
            case 49: // hastattoo
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(arg2.length()==0)
                {
                    logError(scripted,"HASTATTOO","Syntax",funcParms);
                    break;
                }
                else
                if((E!=null)&&(E instanceof MOB))
                    returnable=(((MOB)E).fetchTattoo(arg2)!=null);
                else
                    returnable=false;
                break;
            }
            case 48: // numitemsmob
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                MOB which=null;
                if(lastKnownLocation!=null)
                    which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
                int ct=1;
                if(which!=null)
                for(int i=0;i<which.inventorySize();i++)
                {
                    Item I=which.fetchInventory(i);
                    if((I!=null)&&(I.container()==null))
                        ct++;
                }
                returnable=simpleEval(scripted,""+ct,arg3,arg2,"NUMITEMSMOB");
                break;
            }
            case 43: // roommob
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                Environmental which=null;
                if(lastKnownLocation!=null)
                    which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
                if(which==null)
                    returnable=false;
                else
                    returnable=(CMLib.english().containsString(which.name(),arg2)
                                ||CMLib.english().containsString(which.Name(),arg2)
                                ||CMLib.english().containsString(which.displayText(),arg2));
                break;
            }
            case 44: // roomitem
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                Environmental which=null;
                int ct=1;
                if(lastKnownLocation!=null)
                for(int i=0;i<lastKnownLocation.numItems();i++)
                {
                    Item I=lastKnownLocation.fetchItem(i);
                    if((I!=null)&&(I.container()==null))
                    {
                        if(ct==CMath.s_int(arg1.trim()))
                        { which=I; break;}
                        ct++;
                    }
                }
                if(which==null)
                    returnable=false;
                else
                    returnable=(CMLib.english().containsString(which.name(),arg2)
                                ||CMLib.english().containsString(which.Name(),arg2)
                                ||CMLib.english().containsString(which.displayText(),arg2));
                break;
            }
            case 36: // ishere
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                if(lastKnownLocation!=null)
                    returnable=((lastKnownLocation.fetchAnyItem(arg1)!=null)||(lastKnownLocation.fetchInhabitant(arg1)!=null));
                else
                    returnable=false;
                break;
            }
            case 17: // inroom
            {
                if(tlen==1) tt=parseSpecial3PartEval(eval,t);
                String comp="==";
                Environmental E=monster;
                String arg2;
                if(signH.containsKey(tt[t+1]))
                {
                    E=getArgumentItem(tt[t+0],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    comp=tt[t+1];
                    arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                }
                else
                    arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                Room R=null;
                if(arg2.startsWith("$"))
                    R=CMLib.map().roomLocation(this.getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
                if(R==null)
                    R=getRoom(arg2,lastKnownLocation);
                if(E==null)
                    returnable=false;
                else
                {
                    Room R2=CMLib.map().roomLocation(E);
                    if((R==null)&&((arg2.length()==0)||(R2==null)))
                        returnable=true;
                    else
                    if((R==null)||(R2==null))
                        returnable=false;
                    else
                        returnable=simpleEvalStr(scripted,CMLib.map().getExtendedRoomID(R2),CMLib.map().getExtendedRoomID(R),comp,"INROOM");
                }
                break;
            }
            case 90: // inarea
            {
                if(tlen==1) tt=parseSpecial3PartEval(eval,t);
                String comp="==";
                Environmental E=monster;
                String arg2;
                if(signH.containsKey(tt[t+1]))
                {
                    E=getArgumentItem(tt[t+0],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    comp=tt[t+1];
                    arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                }
                else
                    arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                Room R=null;
                if(arg2.startsWith("$"))
                    R=CMLib.map().roomLocation(this.getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
                if(R==null)
                try
                {
                    if((lastKnownLocation!=null)&&(lastKnownLocation.getArea().Name().equalsIgnoreCase(arg2)))
                        R=lastKnownLocation;
                    if(R==null)
                    for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
                    {
                        Area A=(Area)a.nextElement();
                        if((A!=null)&&(A.Name().equalsIgnoreCase(arg2)))
                        {
                            if((lastKnownLocation!=null)&&(lastKnownLocation.getArea().Name().equals(A.Name())))
                                R=lastKnownLocation;
                            else
                                R=A.getRandomProperRoom();
                        }
                    }
                    for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
                    {
                        Area A=(Area)a.nextElement();
                        if((A!=null)&&(CMLib.english().containsString(A.Name(),arg2)))
                        {
                            if((lastKnownLocation!=null)&&(lastKnownLocation.getArea().Name().equals(A.Name())))
                                R=lastKnownLocation;
                            else
                                R=A.getRandomProperRoom();
                        }
                    }
                }catch(NoSuchElementException nse){}
                if(R==null)
                    R=getRoom(arg2,lastKnownLocation);
                if(E==null)
                    returnable=false;
                else
                {
                    Room R2=CMLib.map().roomLocation(E);
                    if((R==null)&&((arg2.length()==0)||(R2==null)))
                        returnable=true;
                    else
                    if((R==null)||(R2==null))
                        returnable=false;
                    else
                        returnable=simpleEvalStr(scripted,R2.getArea().Name(),R.getArea().Name(),comp,"INAREA");
                }
                break;
            }
            case 89: // isrecall
            {
                if(tlen==1) tt=parseSpecial3PartEval(eval,t);
                String comp="==";
                Environmental E=monster;
                String arg2;
                if(signH.containsKey(tt[t+1]))
                {
                    E=getArgumentItem(tt[t+0],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    comp=tt[t+1];
                    arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                }
                else
                    arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                Room R=null;
                if(arg2.startsWith("$"))
                    R=CMLib.map().getStartRoom(this.getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
                if(R==null)
                    R=getRoom(arg2,lastKnownLocation);
                if(E==null)
                    returnable=false;
                else
                {
                    Room R2=CMLib.map().getStartRoom(E);
                    if((R==null)&&((arg2.length()==0)||(R2==null)))
                        returnable=true;
                    else
                    if((R==null)||(R2==null))
                        returnable=false;
                    else
                        returnable=simpleEvalStr(scripted,CMLib.map().getExtendedRoomID(R2),CMLib.map().getExtendedRoomID(R),comp,"ISRECALL");
                }
                break;
            }
            case 37: // inlocale
            {
                if(tlen==1)
                {
                    if(CMParms.numBits(funcParms)>1)
                        tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                    else
                    {
                        int numBits=2;
                        String[] parsed=null;
                        if(CMParms.cleanBit(funcParms).equals(funcParms))
                            parsed=parseBits("'"+funcParms+"'"+CMStrings.repeat(" .",numBits-1),"cr");
                        else
                            parsed=parseBits(funcParms+CMStrings.repeat(" .",numBits-1),"cr");
                        tt=insertStringArray(tt,parsed,t);
                        eval[0]=tt;
                    }
                }
                String arg2=null;
                Environmental E=monster;
                if(tt[t+1].equals("."))
                    arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                else
                {
                    E=getArgumentItem(tt[t+0],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                }
                if(E==null)
                    returnable=false;
                else
                if(arg2.length()==0)
                    returnable=true;
                else
                {
                    Room R=CMLib.map().roomLocation(E);
                    if(R==null)
                        returnable=false;
                    else
                    if(CMClass.classID(R).toUpperCase().indexOf(arg2.toUpperCase())>=0)
                        returnable=true;
                    else
                        returnable=false;
                }
                break;
            }
            case 18: // sex
            {
                if(tlen==1) tt=parseBits(eval,t,"CcR"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=tt[t+1];
                String arg3=tt[t+2];
                if(CMath.isNumber(arg3.trim()))
                    switch(CMath.s_int(arg3.trim()))
                    {
                    case 0: arg3="NEUTER"; break;
                    case 1: arg3="MALE"; break;
                    case 2: arg3="FEMALE"; break;
                    }
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"SEX","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    String sex=(""+((char)((MOB)E).charStats().getStat(CharStats.STAT_GENDER))).toUpperCase();
                    if(arg2.equals("=="))
                        returnable=arg3.startsWith(sex);
                    else
                    if(arg2.equals("!="))
                        returnable=!arg3.startsWith(sex);
                    else
                    {
                        logError(scripted,"SEX","Syntax",funcParms);
                        return returnable;
                    }
                }
                break;
            }
            case 91: // datetime
            {
                if(tlen==1) tt=parseBits(eval,t,"Ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=tt[t+2];
                int index=CMParms.indexOf(ScriptingEngine.DATETIME_ARGS,arg1.trim());
                if(index<0)
                    logError(scripted,"DATETIME","Syntax","Unknown arg: "+arg1+" for "+scripted.name());
                else
                if(CMLib.map().areaLocation(scripted)!=null)
                {
                    String val=null;
                    switch(index)
                    {
                    case 2: val=""+CMLib.map().areaLocation(scripted).getTimeObj().getDayOfMonth(); break;
                    case 3: val=""+CMLib.map().areaLocation(scripted).getTimeObj().getDayOfMonth(); break;
                    case 4: val=""+CMLib.map().areaLocation(scripted).getTimeObj().getMonth(); break;
                    case 5: val=""+CMLib.map().areaLocation(scripted).getTimeObj().getYear(); break;
                    default:
                        val=""+CMLib.map().areaLocation(scripted).getTimeObj().getTimeOfDay(); break;
                    }
                    returnable=simpleEval(scripted,val,arg3,arg2,"DATETIME");
                }
                break;
            }
            case 13: // stat
            {
                if(tlen==1) tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=tt[t+2];
                String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"STAT","Syntax",funcParms);
                    break;
                }
                if(E==null)
                    returnable=false;
                else
                {
                    String val=getStatValue(E,arg2);
                    if(val==null)
                    {
                        logError(scripted,"STAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
                        break;
                    }

                    if(arg3.equals("=="))
                        returnable=val.equalsIgnoreCase(arg4);
                    else
                    if(arg3.equals("!="))
                        returnable=!val.equalsIgnoreCase(arg4);
                    else
                        returnable=simpleEval(scripted,val,arg4,arg3,"STAT");
                }
                break;
            }
            case 52: // gstat
            {
                if(tlen==1) tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=tt[t+2];
                String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"GSTAT","Syntax",funcParms);
                    break;
                }
                if(E==null)
                    returnable=false;
                else
                {
                    String val=getGStatValue(E,arg2);
                    if(val==null)
                    {
                        logError(scripted,"GSTAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
                        break;
                    }

                    if(arg3.equals("=="))
                        returnable=val.equalsIgnoreCase(arg4);
                    else
                    if(arg3.equals("!="))
                        returnable=!val.equalsIgnoreCase(arg4);
                    else
                        returnable=simpleEval(scripted,val,arg4,arg3,"GSTAT");
                }
                break;
            }
            case 19: // position
            {
                if(tlen==1) tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=tt[t+2];
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"POSITION","Syntax",funcParms);
                    return returnable;
                }
                if(E==null)
                    returnable=false;
                else
                {
                    String sex="STANDING";
                    if(CMLib.flags().isSleeping(E))
                        sex="SLEEPING";
                    else
                    if(CMLib.flags().isSitting(E))
                        sex="SITTING";
                    if(arg2.equals("=="))
                        returnable=sex.startsWith(arg3);
                    else
                    if(arg2.equals("!="))
                        returnable=!sex.startsWith(arg3);
                    else
                    {
                        logError(scripted,"POSITION","Syntax",funcParms);
                        return returnable;
                    }
                }
                break;
            }
            case 20: // level
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"LEVEL","Syntax",funcParms);
                    return returnable;
                }
                if(E==null)
                    returnable=false;
                else
                {
                    int val1=E.envStats().level();
                    returnable=simpleEval(scripted,""+val1,arg3,arg2,"LEVEL");
                }
                break;
            }
            case 80: // questpoints
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"QUESTPOINTS","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    int val1=((MOB)E).getQuestPoint();
                    returnable=simpleEval(scripted,""+val1,arg3,arg2,"QUESTPOINTS");
                }
                break;
            }
            case 83: // qvar
            {
                if(tlen==1) tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                String arg3=tt[t+2];
                String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
                Quest Q=getQuest(arg1);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"QVAR","Syntax",funcParms);
                    return returnable;
                }
                if(Q==null)
                    returnable=false;
                else
                    returnable=simpleEvalStr(scripted,Q.getStat(arg2),arg4,arg3,"QVAR");
                break;
            }
            case 84: // math
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                if(!CMath.isMathExpression(arg1))
                {
                    logError(scripted,"MATH","Syntax",funcParms);
                    return returnable;
                }
                if(!CMath.isMathExpression(arg3))
                {
                    logError(scripted,"MATH","Syntax",funcParms);
                    return returnable;
                }
                returnable=simpleExpressionEval(scripted,arg1,arg3,arg2,"MATH");
                break;
            }
            case 81: // trains
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"TRAINS","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    int val1=((MOB)E).getTrains();
                    returnable=simpleEval(scripted,""+val1,arg3,arg2,"TRAINS");
                }
                break;
            }
            case 82: // pracs
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"PRACS","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    int val1=((MOB)E).getPractices();
                    returnable=simpleEval(scripted,""+val1,arg3,arg2,"PRACS");
                }
                break;
            }
            case 66: // clanrank
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"CLANRANK","Syntax",funcParms);
                    return returnable;
                }
                if(E==null)
                    returnable=false;
                else
                {
                    int val1=(E instanceof MOB)?((MOB)E).getClanRole():-1;
                    returnable=simpleEval(scripted,""+val1,arg3,arg2,"CLANRANK");
                }
                break;
            }
            case 64: // deity
            {
                if(tlen==1) tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(arg2.length()==0)
                {
                    logError(scripted,"DEITY","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    String sex=((MOB)E).getWorshipCharID();
                    if(arg2.equals("=="))
                        returnable=sex.equalsIgnoreCase(arg3);
                    else
                    if(arg2.equals("!="))
                        returnable=!sex.equalsIgnoreCase(arg3);
                    else
                    {
                        logError(scripted,"DEITY","Syntax",funcParms);
                        return returnable;
                    }
                }
                break;
            }
            case 68: // clandata
            {
                if(tlen==1) tt=parseBits(eval,t,"cccR"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=tt[t+2];
                String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"CLANDATA","Syntax",funcParms);
                    return returnable;
                }
                String clanID=null;
                if((E!=null)&&(E instanceof MOB))
                    clanID=((MOB)E).getClanID();
                else
                    clanID=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg1);
                Clan C=CMLib.clans().findClan(clanID);
                if(C!=null)
                {
                    if(!C.isStat(arg2))
                        logError(scripted,"CLANDATA","RunTime",arg2+" is not a valid clan variable.");
                    else
                    {
                        String whichVal=C.getStat(arg2).trim();
                        if(CMath.isNumber(whichVal)&&CMath.isNumber(arg4.trim()))
                            returnable=simpleEval(scripted,whichVal,arg4,arg3,"CLANDATA");
                        else
                            returnable=simpleEvalStr(scripted,whichVal,arg4,arg3,"CLANDATA");
                    }
                }
                break;
            }
            case 65: // clan
            {
                if(tlen==1) tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(arg2.length()==0)
                {
                    logError(scripted,"CLAN","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    String sex=((MOB)E).getClanID();
                    if(arg2.equals("=="))
                        returnable=sex.equalsIgnoreCase(arg3);
                    else
                    if(arg2.equals("!="))
                        returnable=!sex.equalsIgnoreCase(arg3);
                    else
                    {
                        logError(scripted,"CLAN","Syntax",funcParms);
                        return returnable;
                    }
                }
                break;
            }
            case 88: // mood
            {
                if(tlen==1) tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(arg2.length()==0)
                {
                    logError(scripted,"MOOD","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                if(E.fetchEffect("Mood")!=null)
                {
                    String sex=E.fetchEffect("Mood").text();
                    if(arg2.equals("=="))
                        returnable=sex.equalsIgnoreCase(arg3);
                    else
                    if(arg2.equals("!="))
                        returnable=!sex.equalsIgnoreCase(arg3);
                    else
                    {
                        logError(scripted,"MOOD","Syntax",funcParms);
                        return returnable;
                    }
                }
                break;
            }
            case 21: // class
            {
                if(tlen==1) tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"CLASS","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    String sex=((MOB)E).charStats().displayClassName().toUpperCase();
                    if(arg2.equals("=="))
                        returnable=sex.startsWith(arg3);
                    else
                    if(arg2.equals("!="))
                        returnable=!sex.startsWith(arg3);
                    else
                    {
                        logError(scripted,"CLASS","Syntax",funcParms);
                        return returnable;
                    }
                }
                break;
            }
            case 22: // baseclass
            {
                if(tlen==1) tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"CLASS","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    String sex=((MOB)E).charStats().getCurrentClass().baseClass().toUpperCase();
                    if(arg2.equals("=="))
                        returnable=sex.startsWith(arg3);
                    else
                    if(arg2.equals("!="))
                        returnable=!sex.startsWith(arg3);
                    else
                    {
                        logError(scripted,"CLASS","Syntax",funcParms);
                        return returnable;
                    }
                }
                break;
            }
            case 23: // race
            {
                if(tlen==1) tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"RACE","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    String sex=((MOB)E).charStats().raceName().toUpperCase();
                    if(arg2.equals("=="))
                        returnable=sex.startsWith(arg3);
                    else
                    if(arg2.equals("!="))
                        returnable=!sex.startsWith(arg3);
                    else
                    {
                        logError(scripted,"RACE","Syntax",funcParms);
                        return returnable;
                    }
                }
                break;
            }
            case 24: //racecat
            {
                if(tlen==1) tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"RACECAT","Syntax",funcParms);
                    return returnable;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    String sex=((MOB)E).charStats().getMyRace().racialCategory().toUpperCase();
                    if(arg2.equals("=="))
                        returnable=sex.startsWith(arg3);
                    else
                    if(arg2.equals("!="))
                        returnable=!sex.startsWith(arg3);
                    else
                    {
                        logError(scripted,"RACECAT","Syntax",funcParms);
                        return returnable;
                    }
                }
                break;
            }
            case 25: // goldamt
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"GOLDAMT","Syntax",funcParms);
                    break;
                }
                if(E==null)
                    returnable=false;
                else
                {
                    int val1=0;
                    if(E instanceof MOB)
                        val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,CMLib.beanCounter().getCurrency(scripted)));
                    else
                    if(E instanceof Coins)
                        val1=(int)Math.round(((Coins)E).getTotalValue());
                    else
                    if(E instanceof Item)
                        val1=((Item)E).value();
                    else
                    {
                        logError(scripted,"GOLDAMT","Syntax",funcParms);
                        return returnable;
                    }

                    returnable=simpleEval(scripted,""+val1,arg3,arg2,"GOLDAMT");
                }
                break;
            }
            case 78: // exp
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"EXP","Syntax",funcParms);
                    break;
                }
                if((E==null)||(!(E instanceof MOB)))
                    returnable=false;
                else
                {
                    int val1=((MOB)E).getExperience();
                    returnable=simpleEval(scripted,""+val1,arg3,arg2,"EXP");
                }
                break;
            }
            case 76: // value
            {
                if(tlen==1) tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
                String arg1=tt[t+0];
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
                String arg3=tt[t+2];
                String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
                if((arg2.length()==0)||(arg3.length()==0)||(arg4.length()==0))
                {
                    logError(scripted,"VALUE","Syntax",funcParms);
                    break;
                }
                if(!CMLib.beanCounter().getAllCurrencies().contains(arg2.toUpperCase()))
                {
                    logError(scripted,"VALUE","Syntax",arg2+" is not a valid designated currency.");
                    break;
                }
                if(E==null)
                    returnable=false;
                else
                {
                    int val1=0;
                    if(E instanceof MOB)
                        val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,arg2.toUpperCase()));
                    else
                    if(E instanceof Coins)
                    {
                        if(((Coins)E).getCurrency().equalsIgnoreCase(arg2))
                            val1=(int)Math.round(((Coins)E).getTotalValue());
                    }
                    else
                    if(E instanceof Item)
                        val1=((Item)E).value();
                    else
                    {
                        logError(scripted,"VALUE","Syntax",funcParms);
                        return returnable;
                    }

                    returnable=simpleEval(scripted,""+val1,arg4,arg3,"GOLDAMT");
                }
                break;
            }
            case 26: // objtype
            {
                if(tlen==1) tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"OBJTYPE","Syntax",funcParms);
                    return returnable;
                }
                if(E==null)
                    returnable=false;
                else
                {
                    String sex=CMClass.classID(E).toUpperCase();
                    if(arg2.equals("=="))
                        returnable=sex.indexOf(arg3)>=0;
                    else
                    if(arg2.equals("!="))
                        returnable=sex.indexOf(arg3)<0;
                    else
                    {
                        logError(scripted,"OBJTYPE","Syntax",funcParms);
                        return returnable;
                    }
                }
                break;
            }
            case 27: // var
            {
                if(tlen==1) tt=parseBits(eval,t,"cCcr"); /* tt[t+0] */
                String arg1=tt[t+0];
                String arg2=tt[t+1];
                String arg3=tt[t+2];
                String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()==0)||(arg3.length()==0))
                {
                    logError(scripted,"VAR","Syntax",funcParms);
                    return returnable;
                }
                String val=getVar(E,arg1,arg2,source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp);
                if(arg3.equals("=="))
                    returnable=val.equals(arg4);
                else
                if(arg3.equals("!="))
                    returnable=!val.equals(arg4);
                else
                if(arg3.equals(">"))
                    returnable=CMath.s_int(val.trim())>CMath.s_int(arg4.trim());
                else
                if(arg3.equals("<"))
                    returnable=CMath.s_int(val.trim())<CMath.s_int(arg4.trim());
                else
                if(arg3.equals(">="))
                    returnable=CMath.s_int(val.trim())>=CMath.s_int(arg4.trim());
                else
                if(arg3.equals("<="))
                    returnable=CMath.s_int(val.trim())<=CMath.s_int(arg4.trim());
                else
                {
                    logError(scripted,"VAR","Syntax",funcParms);
                    return returnable;
                }
                break;
            }
            case 41: // eval
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
                String arg3=tt[t+1];
                String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
                if(arg3.length()==0)
                {
                    logError(scripted,"EVAL","Syntax",funcParms);
                    return returnable;
                }
                if(arg3.equals("=="))
                    returnable=val.equals(arg4);
                else
                if(arg3.equals("!="))
                    returnable=!val.equals(arg4);
                else
                if(arg3.equals(">"))
                    returnable=CMath.s_int(val.trim())>CMath.s_int(arg4.trim());
                else
                if(arg3.equals("<"))
                    returnable=CMath.s_int(val.trim())<CMath.s_int(arg4.trim());
                else
                if(arg3.equals(">="))
                    returnable=CMath.s_int(val.trim())>=CMath.s_int(arg4.trim());
                else
                if(arg3.equals("<="))
                    returnable=CMath.s_int(val.trim())<=CMath.s_int(arg4.trim());
                else
                {
                    logError(scripted,"EVAL","Syntax",funcParms);
                    return returnable;
                }
                break;
            }
            case 40: // number
            {
                String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).trim();
                boolean isnumber=(val.length()>0);
                for(int i=0;i<val.length();i++)
                    if(!Character.isDigit(val.charAt(i)))
                    { isnumber=false; break;}
                returnable=isnumber;
                break;
            }
            case 42: // randnum
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1s=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase().trim();
                int arg1=0;
                if(CMath.isMathExpression(arg1s.trim()))
                    arg1=CMath.s_parseIntExpression(arg1s.trim());
                else
                    arg1=CMParms.parse(arg1s.trim()).size();
                String arg2=tt[t+1];
                String arg3s=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]).trim();
                int arg3=0;
                if(CMath.isMathExpression(arg3s.trim()))
                    arg3=CMath.s_parseIntExpression(arg3s.trim());
                else
                    arg3=CMParms.parse(arg3s.trim()).size();
                arg3=CMLib.dice().roll(1,arg3,0);
                returnable=simpleEval(scripted,""+arg1,""+arg3,arg2,"RANDNUM");
                break;
            }
            case 71: // rand0num
            {
                if(tlen==1) tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
                String arg1s=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase().trim();
                int arg1=0;
                if(CMath.isMathExpression(arg1s))
                    arg1=CMath.s_parseIntExpression(arg1s);
                else
                    arg1=CMParms.parse(arg1s).size();
                String arg2=tt[t+1];
                String arg3s=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]).trim();
                int arg3=0;
                if(CMath.isMathExpression(arg3s))
                    arg3=CMath.s_parseIntExpression(arg3s);
                else
                    arg3=CMParms.parse(arg3s).size();
                arg3=CMLib.dice().roll(1,arg3,-1);
                returnable=simpleEval(scripted,""+arg1,""+arg3,arg2,"RAND0NUM");
                break;
            }
            case 53: // incontainer
            {
                if(tlen==1) tt=parseBits(eval,t,"cr"); /* tt[t+0] */
                String arg1=tt[t+0];
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String arg2=tt[t+1];
                Environmental E2=getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                if(E instanceof MOB)
                {
                    if(arg2.length()==0)
                        returnable=(((MOB)E).riding()==null);
                    else
                    if(E2!=null)
                        returnable=(((MOB)E).riding()==E2);
                    else
                        returnable=false;
                }
                else
                if(E instanceof Item)
                {
                    if(arg2.length()==0)
                        returnable=(((Item)E).container()==null);
                    else
                    if(E2!=null)
                        returnable=(((Item)E).container()==E2);
                    else
                        returnable=false;
                }
                else
                    returnable=false;
                break;
            }
            default:
                logError(scripted,"EVAL","UNKNOWN",CMParms.toStringList(tt));
                return false;
            }
            pushEvalBoolean(stack,returnable);
            while((t<tt.length)&&(!tt[t].equals(")")))
                t++;
        }
        else
        {
            logError(scripted,"EVAL","SYNTAX","BAD CONJUCTOR "+tt[t]+": "+CMParms.toStringList(tt));
            return false;
        }
        
        if((stack.size()!=1)||(!(stack.firstElement() instanceof Boolean)))
        {
            logError(scripted,"EVAL","SYNTAX","Unmatched (: "+CMParms.toStringList(tt));
            return false;
        }
        return ((Boolean)stack.firstElement()).booleanValue();
    }

    protected String functify(Environmental scripted,
                              MOB source,
                              Environmental target,
                              MOB monster,
                              Item primaryItem,
                              Item secondaryItem,
                              String msg,
                              Object[] tmp,
                              String evaluable)
    {
        String uevaluable=evaluable.toUpperCase().trim();
        StringBuffer results = new StringBuffer("");
        while(evaluable.length()>0)
        {
            int y=evaluable.indexOf("(");
            int z=evaluable.indexOf(")");
            String preFab=(y>=0)?uevaluable.substring(0,y).trim():"";
            Integer funcCode=(Integer)funcH.get(preFab);
            if(funcCode==null) funcCode=Integer.valueOf(0);
            if(y==0)
            {
                int depth=0;
                int i=0;
                while((++i)<evaluable.length())
                {
                    char c=evaluable.charAt(i);
                    if((c==')')&&(depth==0))
                    {
                        String expr=evaluable.substring(1,i);
                        evaluable=evaluable.substring(i+1);
                        uevaluable=uevaluable.substring(i+1);
                        results.append(functify(scripted,source,target,monster,primaryItem,secondaryItem,msg,tmp,expr));
                        break;
                    }
                    else
                    if(c=='(') depth++;
                    else
                    if(c==')') depth--;
                }
                z=evaluable.indexOf(")");
            }
            else
            if((y<0)||(z<y))
            {
                logError(scripted,"()","Syntax",evaluable);
                break;
            }
            else
            {
            tickStatus=Tickable.STATUS_MISC2+funcCode.intValue();
            String funcParms=evaluable.substring(y+1,z).trim();
            switch(funcCode.intValue())
            {
            case 1: // rand
            {
                results.append(CMLib.dice().rollPercentage());
                break;
            }
            case 2: // has
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                Vector choices=new Vector();
                if(E==null)
                    choices=new Vector();
                else
                if(E instanceof MOB)
                {
                    for(int i=0;i<((MOB)E).inventorySize();i++)
                    {
                        Item I=((MOB)E).fetchInventory(i);
                        if((I!=null)&&(I.amWearingAt(Wearable.IN_INVENTORY))&&(I.container()==null))
                            choices.addElement(I);
                    }
                }
                else
                if(E instanceof Item)
                {
                    choices.addElement(E);
                    if(E instanceof Container)
                        choices=((Container)E).getContents();
                }
                else
                if(E instanceof Room)
                {
                    for(int i=0;i<((Room)E).numItems();i++)
                    {
                        Item I=((Room)E).fetchItem(i);
                        if((I!=null)&&(I.container()==null))
                            choices.addElement(I);
                    }
                }
                if(choices.size()>0)
                    results.append(((Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1))).name());
                break;
            }
            case 74: // hasnum
            {
                String arg1=CMParms.getCleanBit(funcParms,0);
                String item=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,1));
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((item.length()==0)||(E==null))
                    logError(scripted,"HASNUM","Syntax",funcParms);
                else
                {
                    Item I=null;
                    int num=0;
                    if(E instanceof MOB)
                    {
                        MOB M=(MOB)E;
                        for(int i=0;i<M.inventorySize();i++)
                        {
                            I=M.fetchInventory(i);
                            if(I==null) break;
                            if((item.equalsIgnoreCase("all"))
                            ||(CMLib.english().containsString(I.Name(),item)))
                                num++;
                        }
                        results.append(""+num);
                    }
                    else
                    if(E instanceof Item)
                    {
                        num=CMLib.english().containsString(E.name(),item)?1:0;
                        results.append(""+num);
                    }
                    else
                    if(E instanceof Room)
                    {
                        Room R=(Room)E;
                        for(int i=0;i<R.numItems();i++)
                        {
                            I=R.fetchItem(i);
                            if(I==null) break;
                            if((item.equalsIgnoreCase("all"))
                            ||(CMLib.english().containsString(I.Name(),item)))
                                num++;
                        }
                        results.append(""+num);
                    }
                }
                break;
            }
            case 3: // worn
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                Vector choices=new Vector();
                if(E==null)
                    choices=new Vector();
                else
                if(E instanceof MOB)
                {
                    for(int i=0;i<((MOB)E).inventorySize();i++)
                    {
                        Item I=((MOB)E).fetchInventory(i);
                        if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY))&&(I.container()==null))
                            choices.addElement(I);
                    }
                }
                else
                if((E instanceof Item)&&(!(((Item)E).amWearingAt(Wearable.IN_INVENTORY))))
                {
                    choices.addElement(E);
                    if(E instanceof Container)
                        choices=((Container)E).getContents();
                }
                if(choices.size()>0)
                    results.append(((Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1))).name());
                break;
            }
            case 4: // isnpc
            case 5: // ispc
                results.append("[unimplemented function]");
                break;
            case 87: // isbirthday
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB)&&(((MOB)E).playerStats()!=null)&&(((MOB)E).playerStats().getBirthday()!=null))
                {
                    MOB mob=(MOB)E;
                    TimeClock C=CMLib.time().globalClock();
                    int day=C.getDayOfMonth();
                    int month=C.getMonth();
                    int year=C.getYear();
                    int bday=mob.playerStats().getBirthday()[0];
                    int bmonth=mob.playerStats().getBirthday()[1];
                    if((month>bmonth)||((month==bmonth)&&(day>bday)))
                        year++;

                    StringBuffer timeDesc=new StringBuffer("");
                    if(C.getDaysInWeek()>0)
                    {
                        long x=((long)year)*((long)C.getMonthsInYear())*C.getDaysInMonth();
                        x=x+((long)(bmonth-1))*((long)C.getDaysInMonth());
                        x=x+bmonth;
                        timeDesc.append(C.getWeekNames()[(int)(x%C.getDaysInWeek())]+", ");
                    }
                    timeDesc.append("the "+bday+CMath.numAppendage(bday));
                    timeDesc.append(" day of "+C.getMonthNames()[bmonth-1]);
                    if(C.getYearNames().length>0)
                        timeDesc.append(", "+CMStrings.replaceAll(C.getYearNames()[year%C.getYearNames().length],"#",""+year));
                    results.append(timeDesc.toString());
                }
                break;
            }
            case 6: // isgood
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&((E instanceof MOB)))
                {
                    Faction.FactionRange FR=CMLib.factions().getRange(CMLib.factions().AlignID(),((MOB)E).fetchFaction(CMLib.factions().AlignID()));
                    if(FR!=null)
                        results.append(FR.name());
                    else
                        results.append(((MOB)E).fetchFaction(CMLib.factions().AlignID()));
                }
                break;
            }
            case 8: // isevil
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&((E instanceof MOB)))
                    results.append(CMStrings.capitalizeAndLower(CMLib.flags().getAlignmentName(E)).toLowerCase());
                break;
            }
            case 9: // isneutral
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&((E instanceof MOB)))
                    results.append(((MOB)E).fetchFaction(CMLib.factions().AlignID()));
                break;
            }
            case 11: // isimmort
                results.append("[unimplemented function]");
                break;
            case 54: // isalive
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
                    results.append(((MOB)E).healthText(null));
                else
                if(E!=null)
                    results.append(E.name()+" is dead.");
                break;
            }
            case 58: // isable
            {
                String arg1=CMParms.getCleanBit(funcParms,0);
                String arg2=CMParms.getPastBitClean(funcParms,0);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
                {
                    ExpertiseLibrary X=(ExpertiseLibrary)CMLib.expertises().findDefinition(arg2,true);
                    if(X!=null)
                    {
                        String s=((MOB)E).fetchExpertise(X.ID());
                        if(s!=null) results.append(s);
                    }
                    else
                    {
                        Ability A=((MOB)E).findAbility(arg2);
                        if(A!=null) results.append(""+A.proficiency());
                    }
                }
                break;
            }
            case 59: // isopen
            {
                String arg1=CMParms.cleanBit(funcParms);
                int dir=Directions.getGoodDirectionCode(arg1);
                boolean returnable=false;
                if(dir<0)
                {
                    Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    if((E!=null)&&(E instanceof Container))
                        returnable=((Container)E).isOpen();
                    else
                    if((E!=null)&&(E instanceof Exit))
                        returnable=((Exit)E).isOpen();
                }
                else
                if(lastKnownLocation!=null)
                {
                    Exit E=lastKnownLocation.getExitInDir(dir);
                    if(E!=null) returnable= E.isOpen();
                }
                results.append(""+returnable);
                break;
            }
            case 60: // islocked
            {
                String arg1=CMParms.cleanBit(funcParms);
                int dir=Directions.getGoodDirectionCode(arg1);
                if(dir<0)
                {
                    Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    if((E!=null)&&(E instanceof Container))
                        results.append(((Container)E).keyName());
                    else
                    if((E!=null)&&(E instanceof Exit))
                        results.append(((Exit)E).keyName());
                }
                else
                if(lastKnownLocation!=null)
                {
                    Exit E=lastKnownLocation.getExitInDir(dir);
                    if(E!=null)
                        results.append(E.keyName());
                }
                break;
            }
            case 62: // callfunc
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,0));
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
                String found=null;
                boolean validFunc=false;
                Vector scripts=getScripts();
                String trigger=null;
                String[] ttrigger=null;
                for(int v=0;v<scripts.size();v++)
                {
                    DVector script2=(DVector)scripts.elementAt(v);
                    if(script2.size()<1) continue;
                    trigger=((String)script2.elementAt(0,1)).toUpperCase().trim();
                    ttrigger=(String[])script2.elementAt(0,2);
                    if(getTriggerCode(trigger,ttrigger)==17)
                    {
                        String fnamed=CMParms.getCleanBit(trigger,1);
                        if(fnamed.equalsIgnoreCase(arg1))
                        {
                            validFunc=true;
                            found=
                            execute(scripted,
                                    source,
                                    target,
                                    monster,
                                    primaryItem,
                                    secondaryItem,
                                    script2,
                                    varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg2),
                                    tmp);
                            if(found==null) found="";
                            break;
                        }
                    }
                }
                if(!validFunc)
                    logError(scripted,"CALLFUNC","Unknown","Function: "+arg1);
                else
                    results.append(found);
                break;
            }
            case 61: // strin
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,0));
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
                Vector V=CMParms.parse(arg1.toUpperCase());
                results.append(V.indexOf(arg2.toUpperCase()));
                break;
            }
            case 55: // ispkill
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB)))
                    results.append("false");
                else
                if(CMath.bset(((MOB)E).getBitmap(),MOB.ATT_PLAYERKILL))
                    results.append("true");
                else
                    results.append("false");
                break;
            }
            case 10: // isfight
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&((E instanceof MOB))&&(((MOB)E).isInCombat()))
                    results.append(((MOB)E).getVictim().name());
                break;
            }
            case 12: // ischarmed
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null)
                {
                    Vector V=CMLib.flags().flaggedAffects(E,Ability.FLAG_CHARMING);
                    for(int v=0;v<V.size();v++)
                        results.append((((Ability)V.elementAt(v)).name())+" ");
                }
                break;
            }
            case 15: // isfollow
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB)&&(((MOB)E).amFollowing()!=null)
                &&(((MOB)E).amFollowing().location()==lastKnownLocation))
                    results.append(((MOB)E).amFollowing().name());
                break;
            }
            case 73: // isservant
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB)&&(((MOB)E).getLiegeID()!=null)&&(((MOB)E).getLiegeID().length()>0))
                    results.append(((MOB)E).getLiegeID());
                break;
            }
            case 56: // name
            case 7: // isname
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null) results.append(E.name());
                break;
            }
            case 75: // currency
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null)results.append(CMLib.beanCounter().getCurrency(E));
                break;
            }
            case 14: // affected
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E instanceof MOB)
                {
                    if(((MOB)E).numAllEffects()>0)
                        results.append(E.fetchEffect(CMLib.dice().roll(1,((MOB)E).numAllEffects(),-1)).name());
                }
                else
                if((E!=null)&&(E.numEffects()>0))
                    results.append(E.fetchEffect(CMLib.dice().roll(1,E.numEffects(),-1)).name());
                break;
            }
            case 69: // isbehave
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null)
                for(int i=0;i<E.numBehaviors();i++)
                    results.append(E.fetchBehavior(i).ID()+" ");
                break;
            }
            case 70: // ipaddress
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB)&&(!((MOB)E).isMonster()))
                    results.append(((MOB)E).session().getAddress());
                break;
            }
            case 28: // questwinner
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB)&&(!((MOB)E).isMonster()))
                    for(int q=0;q<CMLib.quests().numQuests();q++)
                    {
                        Quest Q=CMLib.quests().fetchQuest(q);
                        if((Q!=null)&&(Q.wasWinner(E.Name())))
                            results.append(Q.name()+" ");
                    }
                break;
            }
            case 93: // questscripted
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB)&&(!((MOB)E).isMonster()))
                {
                    for(int s=0;s<E.numScripts();s++)
                    {
                        ScriptingEngine S=E.fetchScript(s);
                        if((S!=null)&&(S.defaultQuestName()!=null)&&(S.defaultQuestName().length()>0))
                        {
                            Quest Q=CMLib.quests().fetchQuest(S.defaultQuestName());
                            if(Q!=null)
                                results.append(Q.name()+" ");
                            else
                                results.append(S.defaultQuestName()+" ");
                        }
                    }
                }
                break;
            }
            case 30: // questobj
            {
                String questName=CMParms.cleanBit(funcParms);
                questName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,questName);
                Quest Q=getQuest(questName);
                if(Q==null)
                {
                    logError(scripted,"QUESTOBJ","Unknown","Quest: "+questName);
                    break;
                }
                StringBuffer list=new StringBuffer("");
                int num=1;
                Environmental E=Q.getQuestItem(num);
                while(E!=null)
                {
                    if(E.Name().indexOf(' ')>=0)
                        list.append("\""+E.Name()+"\" ");
                    else
                        list.append(E.Name()+" ");
                    num++;
                    E=Q.getQuestItem(num);
                }
                results.append(list.toString().trim());
                break;
            }
            case 94: // questroom
            {
                String questName=CMParms.cleanBit(funcParms);
                questName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,questName);
                Quest Q=getQuest(questName);
                if(Q==null)
                {
                    logError(scripted,"QUESTOBJ","Unknown","Quest: "+questName);
                    break;
                }
                StringBuffer list=new StringBuffer("");
                int num=1;
                Environmental E=Q.getQuestRoom(num);
                while(E!=null)
                {
                    String roomID=CMLib.map().getExtendedRoomID((Room)E);
                    if(roomID.indexOf(' ')>=0)
                        list.append("\""+roomID+"\" ");
                    else
                        list.append(roomID+" ");
                    num++;
                    E=Q.getQuestRoom(num);
                }
                results.append(list.toString().trim());
            }
            case 29: // questmob
            {
                String questName=CMParms.cleanBit(funcParms);
                questName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,questName);
                Quest Q=getQuest(questName);
                if(Q==null)
                {
                    logError(scripted,"QUESTOBJ","Unknown","Quest: "+questName);
                    break;
                }
                StringBuffer list=new StringBuffer("");
                int num=1;
                Environmental E=Q.getQuestMob(num);
                while(E!=null)
                {
                    if(E.Name().indexOf(' ')>=0)
                        list.append("\""+E.Name()+"\" ");
                    else
                        list.append(E.Name()+" ");
                    num++;
                    E=Q.getQuestMob(num);
                }
                results.append(list.toString().trim());
                break;
            }
            case 31: // isquestmobalive
            {
                String questName=CMParms.cleanBit(funcParms);
                questName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,questName);
                Quest Q=getQuest(questName);
                if(Q==null)
                {
                    logError(scripted,"QUESTOBJ","Unknown","Quest: "+questName);
                    break;
                }
                StringBuffer list=new StringBuffer("");
                int num=1;
                Environmental E=Q.getQuestMob(num);
                while(E!=null)
                {
                    if(CMLib.flags().isInTheGame(E,true))
                    {
                        if(E.Name().indexOf(' ')>=0)
                            list.append("\""+E.Name()+"\" ");
                        else
                            list.append(E.Name()+" ");
                    }
                    num++;
                    E=Q.getQuestMob(num);
                }
                results.append(list.toString().trim());
                break;
            }
            case 32: // nummobsinarea
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                int num=0;
                Vector MASK=null;
                if((arg1.toUpperCase().startsWith("MASK")&&(arg1.substring(4).trim().startsWith("="))))
                {
                    arg1=arg1.substring(4).trim();
                    arg1=arg1.substring(1).trim();
                    MASK=CMLib.masking().maskCompile(arg1);
                }
                for(Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
                {
                    Room R=(Room)e.nextElement();
                    for(int m=0;m<R.numInhabitants();m++)
                    {
                        MOB M=R.fetchInhabitant(m);
                        if(M==null) continue;
                        if(MASK!=null)
                        {
                            if(CMLib.masking().maskCheck(MASK,M,true))
                                num++;
                        }
                        else
                        if(CMLib.english().containsString(M.name(),arg1))
                            num++;
                    }
                }
                results.append(num);
                break;
            }
            case 33: // nummobs
            {
                int num=0;
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                Vector MASK=null;
                if((arg1.toUpperCase().startsWith("MASK")&&(arg1.substring(4).trim().startsWith("="))))
                {
                    arg1=arg1.substring(4).trim();
                    arg1=arg1.substring(1).trim();
                    MASK=CMLib.masking().maskCompile(arg1);
                }
                try
                {
                    for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
                    {
                        Room R=(Room)e.nextElement();
                        for(int m=0;m<R.numInhabitants();m++)
                        {
                            MOB M=R.fetchInhabitant(m);
                            if(M==null) continue;
                            if(MASK!=null)
                            {
                                if(CMLib.masking().maskCheck(MASK,M,true))
                                    num++;
                            }
                            else
                            if(CMLib.english().containsString(M.name(),arg1))
                                num++;
                        }
                    }
                }catch(NoSuchElementException nse){}
                results.append(num);
                break;
            }
            case 34: // numracesinarea
            {
                int num=0;
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                Room R=null;
                MOB M=null;
                for(Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
                {
                    R=(Room)e.nextElement();
                    for(int m=0;m<R.numInhabitants();m++)
                    {
                        M=R.fetchInhabitant(m);
                        if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
                            num++;
                    }
                }
                results.append(num);
                break;
            }
            case 35: // numraces
            {
                int num=0;
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                Room R=null;
                MOB M=null;
                try
                {
                    for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
                    {
                        R=(Room)e.nextElement();
                        for(int m=0;m<R.numInhabitants();m++)
                        {
                            M=R.fetchInhabitant(m);
                            if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
                                num++;
                        }
                    }
                }catch(NoSuchElementException nse){}
                results.append(num);
                break;
            }
            case 16: // hitprcnt
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB))
                {
                    double hitPctD=CMath.div(((MOB)E).curState().getHitPoints(),((MOB)E).maxState().getHitPoints());
                    int val1=(int)Math.round(hitPctD*100.0);
                    results.append(val1);
                }
                break;
            }
            case 50: // isseason
            {
                if(monster.location()!=null)
                    results.append(TimeClock.SEASON_DESCS[monster.location().getArea().getTimeObj().getSeasonCode()]);
                break;
            }
            case 51: // isweather
            {
                if(monster.location()!=null)
                    results.append(Climate.WEATHER_DESCS[monster.location().getArea().getClimateObj().weatherType(monster.location())]);
                break;
            }
            case 57: // ismoon
            {
                if(monster.location()!=null)
                    results.append(TimeClock.PHASE_DESC[monster.location().getArea().getTimeObj().getMoonPhase()]);
                break;
            }
            case 38: // istime
            {
                if(lastKnownLocation!=null)
                    results.append(TimeClock.TOD_DESC[lastKnownLocation.getArea().getTimeObj().getTODCode()].toLowerCase());
                break;
            }
            case 39: // isday
            {
                if(lastKnownLocation!=null)
                    results.append(""+lastKnownLocation.getArea().getTimeObj().getDayOfMonth());
                break;
            }
            case 43: // roommob
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                Environmental which=null;
                if(lastKnownLocation!=null)
                {
                    which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
                    if(which!=null)
                    {
                        Vector list=new Vector();
                        for(int i=0;i<lastKnownLocation.numInhabitants();i++)
                        {
                            MOB M=lastKnownLocation.fetchInhabitant(i);
                            if(M!=null) list.addElement(M);
                        }
                        results.append(CMLib.english().getContextName(list,which));
                    }
                }
                break;
            }
            case 44: // roomitem
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                Environmental which=null;
                int ct=1;
                if(lastKnownLocation!=null)
                {
                    Vector list=new Vector();
                    for(int i=0;i<lastKnownLocation.numItems();i++)
                    {
                        Item I=lastKnownLocation.fetchItem(i);
                        if((I!=null)&&(I.container()==null))
                        {
                            list.addElement(I);
                            if(ct==CMath.s_int(arg1.trim()))
                            { which=I; break;}
                            ct++;
                        }
                    }
                    if(which!=null)
                        results.append(CMLib.english().getContextName(list,which));
                }
                break;
            }
            case 45: // nummobsroom
            {
                int num=0;
                if(lastKnownLocation!=null)
                {
                    num=lastKnownLocation.numInhabitants();
                    String name=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                    if((name.length()>0)&&(!name.equalsIgnoreCase("*")))
                    {
                        num=0;
                        Vector MASK=null;
                        if((name.toUpperCase().startsWith("MASK")&&(name.substring(4).trim().startsWith("="))))
                        {
                            name=name.substring(4).trim();
                            name=name.substring(1).trim();
                            MASK=CMLib.masking().maskCompile(name);
                        }
                        for(int i=0;i<lastKnownLocation.numInhabitants();i++)
                        {
                            MOB M=lastKnownLocation.fetchInhabitant(i);
                            if(M==null) continue;
                            if(MASK!=null)
                            {
                                if(CMLib.masking().maskCheck(MASK,M,true))
                                    num++;
                            }
                            else
                            if(CMLib.english().containsString(M.Name(),name)
                            ||CMLib.english().containsString(M.displayText(),name))
                                num++;
                        }
                    }
                }
                results.append(""+num);
                break;
            }
            case 63: // numpcsroom
            {
                if(lastKnownLocation!=null)
                    results.append(""+lastKnownLocation.numPCInhabitants());
                break;
            }
            case 79: // numpcsarea
            {
                if(lastKnownLocation!=null)
                {
                    int num=0;
                    for(int s=0;s<CMLib.sessions().size();s++)
                    {
                        Session S=CMLib.sessions().elementAt(s);
                        if((S!=null)&&(S.mob()!=null)&&(S.mob().location()!=null)&&(S.mob().location().getArea()==lastKnownLocation.getArea()))
                            num++;
                    }
                    results.append(""+num);
                }
                break;
            }
            case 77: // explored
            {
                String whom=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,0));
                String where=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,1));
                Environmental E=getArgumentMOB(whom,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(E instanceof MOB)
                {
                    Area A=null;
                    if(!where.equalsIgnoreCase("world"))
                    {
                    	A=CMLib.map().getArea(where);
                    	if(A==null)
                    	{
	                        Environmental E2=getArgumentItem(where,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
	                        if(E2!=null)
	                            A=CMLib.map().areaLocation(E2);
                    	}
                    }
                    if((lastKnownLocation!=null)
                    &&((A!=null)||(where.equalsIgnoreCase("world"))))
                    {
                        int pct=0;
                        MOB M=(MOB)E;
                        if(M.playerStats()!=null)
                            pct=M.playerStats().percentVisited(M,A);
                        results.append(""+pct);
                    }
                }
                break;
            }
            case 72: // faction
            {
                String arg1=CMParms.getCleanBit(funcParms,0);
                String arg2=CMParms.getPastBit(funcParms,0);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                Faction F=CMLib.factions().getFaction(arg2);
                if(F==null)
                    logError(scripted,"FACTION","Unknown Faction",arg1);
                else
                if((E!=null)&&(E instanceof MOB)&&(((MOB)E).hasFaction(F.factionID())))
                {
                    int value=((MOB)E).fetchFaction(F.factionID());
                    Faction.FactionRange FR=CMLib.factions().getRange(F.factionID(),value);
                    if(FR!=null)
                        results.append(FR.name());
                }
                break;
            }
            case 46: // numitemsroom
            {
                int ct=0;
                if(lastKnownLocation!=null)
                for(int i=0;i<lastKnownLocation.numItems();i++)
                {
                    Item I=lastKnownLocation.fetchItem(i);
                    if((I!=null)&&(I.container()==null))
                        ct++;
                }
                results.append(""+ct);
                break;
            }
            case 47: //mobitem
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,0));
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
                MOB M=null;
                if(lastKnownLocation!=null)
                    M=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
                Item which=null;
                int ct=1;
                if(M!=null)
                for(int i=0;i<M.inventorySize();i++)
                {
                    Item I=M.fetchInventory(i);
                    if((I!=null)&&(I.container()==null))
                    {
                        if(ct==CMath.s_int(arg2.trim()))
                        { which=I; break;}
                        ct++;
                    }
                }
                if(which!=null)
                    results.append(which.name());
                break;
            }
            case 48: // numitemsmob
            {
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                MOB which=null;
                if(lastKnownLocation!=null)
                    which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
                int ct=1;
                if(which!=null)
                for(int i=0;i<which.inventorySize();i++)
                {
                    Item I=which.fetchInventory(i);
                    if((I!=null)&&(I.container()==null))
                        ct++;
                }
                results.append(""+ct);
                break;
            }
            case 36: // ishere
            {
                if(lastKnownLocation!=null)
                    results.append(lastKnownLocation.getArea().name());
                break;
            }
            case 17: // inroom
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||arg1.length()==0)
                    results.append(CMLib.map().getExtendedRoomID(lastKnownLocation));
                else
                    results.append(CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(E)));
                break;
            }
            case 90: // inarea
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||arg1.length()==0)
                    results.append(lastKnownLocation==null?"Nowhere":lastKnownLocation.getArea().Name());
                else
                {
                    Room R=CMLib.map().roomLocation(E);
                    results.append(R==null?"Nowhere":R.getArea().Name());
                }
                break;
            }
            case 89: // isrecall
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null) results.append(CMLib.map().getExtendedRoomID(CMLib.map().getStartRoom(E)));
                break;
            }
            case 37: // inlocale
            {
                String parms=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
                if(parms.trim().length()==0)
                {
                    if(lastKnownLocation!=null)
                        results.append(lastKnownLocation.name());
                }
                else
                {
                    Environmental E=getArgumentItem(parms,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    if(E!=null)
                    {
                        Room R=CMLib.map().roomLocation(E);
                        if(R!=null)
                            results.append(R.name());
                    }
                }
                break;
            }
            case 18: // sex
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB))
                    results.append(((MOB)E).charStats().genderName());
                break;
            }
            case 91: // datetime
            {
                String arg1=CMParms.getCleanBit(funcParms,0);
                int index=CMParms.indexOf(ScriptingEngine.DATETIME_ARGS,arg1.toUpperCase().trim());
                if(index<0)
                    logError(scripted,"DATETIME","Syntax","Unknown arg: "+arg1+" for "+scripted.name());
                else
                if(CMLib.map().areaLocation(scripted)!=null)
                switch(index)
                {
                case 2: results.append(CMLib.map().areaLocation(scripted).getTimeObj().getDayOfMonth()); break;
                case 3: results.append(CMLib.map().areaLocation(scripted).getTimeObj().getDayOfMonth()); break;
                case 4: results.append(CMLib.map().areaLocation(scripted).getTimeObj().getMonth()); break;
                case 5: results.append(CMLib.map().areaLocation(scripted).getTimeObj().getYear()); break;
                default:
                    results.append(CMLib.map().areaLocation(scripted).getTimeObj().getTimeOfDay()); break;
                }
                break;
            }
            case 13: // stat
            {
                String arg1=CMParms.getCleanBit(funcParms,0);
                String arg2=CMParms.getPastBitClean(funcParms,0);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null)
                {
                    String val=getStatValue(E,arg2);
                    if(val==null)
                    {
                        logError(scripted,"STAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
                        break;
                    }

                    results.append(val);
                    break;
                }
                break;
            }
            case 52: // gstat
            {
                String arg1=CMParms.getCleanBit(funcParms,0);
                String arg2=CMParms.getPastBitClean(funcParms,0);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null)
                {
                    String val=getGStatValue(E,arg2);
                    if(val==null)
                    {
                        logError(scripted,"GSTAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
                        break;
                    }

                    results.append(val);
                    break;
                }
                break;
            }
            case 19: // position
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null)
                {
                    String sex="STANDING";
                    if(CMLib.flags().isSleeping(E))
                        sex="SLEEPING";
                    else
                    if(CMLib.flags().isSitting(E))
                        sex="SITTING";
                    results.append(sex);
                    break;
                }
                break;
            }
            case 20: // level
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null)
                    results.append(E.envStats().level());
                break;
            }
            case 80: // questpoints
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(E instanceof MOB)
                    results.append(((MOB)E).getQuestPoint());
                break;
            }
            case 83: // qvar
            {
                String arg1=CMParms.getCleanBit(funcParms,0);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
                if((arg1.length()!=0)&&(arg2.length()!=0))
                {
                    Quest Q=getQuest(arg1);
                    if(Q!=null) results.append(Q.getStat(arg2));
                }
                break;
            }
            case 84: // math
            {
                String arg1=CMParms.cleanBit(funcParms);
                results.append(""+Math.round(CMath.s_parseMathExpression(arg1)));
                break;
            }
            case 85: // islike
            {
                String arg1=CMParms.cleanBit(funcParms);
                results.append(CMLib.masking().maskDesc(arg1));
                break;
            }
            case 86: // strcontains
            {
                results.append("[unimplemented function]");
                break;
            }
            case 81: // trains
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(E instanceof MOB)
                    results.append(((MOB)E).getTrains());
                break;
            }
            case 92: // isodd
            {
                String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp ,CMParms.cleanBit(funcParms)).trim();
                boolean isodd = false;
                if( CMath.isLong( val ) )
                {
                    isodd = (CMath.s_long(val) %2 == 1);
                }
                if( isodd )
                {
                    results.append( CMath.s_long( val.trim() ) );
                }
                break;
            }
            case 82: // pracs
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(E instanceof MOB)
                    results.append(((MOB)E).getPractices());
                break;
            }
            case 68: // clandata
            {
                String arg1=CMParms.getCleanBit(funcParms,0);
                String arg2=CMParms.getPastBitClean(funcParms,0);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String clanID=null;
                if((E!=null)&&(E instanceof MOB))
                    clanID=((MOB)E).getClanID();
                else
                    clanID=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg1);
                Clan C=CMLib.clans().findClan(clanID);
                if(C!=null)
                {
                    if(!C.isStat(arg2))
                        logError(scripted,"CLANDATA","RunTime",arg2+" is not a valid clan variable.");
                    else
                        results.append(C.getStat(arg2));
                }
                break;
            }
            case 67: // hastitle
            {
                String arg1=CMParms.getCleanBit(funcParms,0);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((arg2.length()>0)&&(E instanceof MOB)&&(((MOB)E).playerStats()!=null))
                {
                    MOB M=(MOB)E;
                    results.append(M.playerStats().getActiveTitle());
                }
                break;
            }
            case 66: // clanrank
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB))
                    results.append(((MOB)E).getClanRole()+"");
                break;
            }
            case 21: // class
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB))
                    results.append(((MOB)E).charStats().displayClassName());
                break;
            }
            case 64: // deity
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB))
                {
                    String sex=((MOB)E).getWorshipCharID();
                    results.append(sex);
                }
                break;
            }
            case 65: // clan
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB))
                {
                    String sex=((MOB)E).getClanID();
                    results.append(sex);
                }
                break;
            }
            case 88: // mood
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB)&&(E.fetchEffect("Mood")!=null))
                    results.append(CMStrings.capitalizeAndLower(E.fetchEffect("Mood").text()));
                break;
            }
            case 22: // baseclass
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB))
                    results.append(((MOB)E).charStats().getCurrentClass().baseClass());
                break;
            }
            case 23: // race
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB))
                    results.append(((MOB)E).charStats().raceName());
                break;
            }
            case 24: //racecat
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB))
                    results.append(((MOB)E).charStats().getMyRace().racialCategory());
                break;
            }
            case 25: // goldamt
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    results.append(false);
                else
                {
                    int val1=0;
                    if(E instanceof MOB)
                        val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,CMLib.beanCounter().getCurrency(scripted)));
                    else
                    if(E instanceof Coins)
                        val1=(int)Math.round(((Coins)E).getTotalValue());
                    else
                    if(E instanceof Item)
                        val1=((Item)E).value();
                    else
                    {
                        logError(scripted,"GOLDAMT","Syntax",funcParms);
                        return results.toString();
                    }
                    results.append(val1);
                }
                break;
            }
            case 78: // exp
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    results.append(false);
                else
                {
                    int val1=0;
                    if(E instanceof MOB)
                        val1=((MOB)E).getExperience();
                    results.append(val1);
                }
                break;
            }
            case 76: // value
            {
                String arg1=CMParms.getCleanBit(funcParms,0);
                String arg2=CMParms.getPastBitClean(funcParms,0);
                if(!CMLib.beanCounter().getAllCurrencies().contains(arg2.toUpperCase()))
                {
                    logError(scripted,"VALUE","Syntax",arg2+" is not a valid designated currency.");
                    return results.toString();
                }
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    results.append(false);
                else
                {
                    int val1=0;
                    if(E instanceof MOB)
                        val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,arg2));
                    else
                    if(E instanceof Coins)
                    {
                        if(((Coins)E).getCurrency().equalsIgnoreCase(arg2))
                            val1=(int)Math.round(((Coins)E).getTotalValue());
                    }
                    else
                    if(E instanceof Item)
                        val1=((Item)E).value();
                    else
                    {
                        logError(scripted,"GOLDAMT","Syntax",funcParms);
                        return results.toString();
                    }
                    results.append(val1);
                }
                break;
            }
            case 26: // objtype
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null)
                {
                    String sex=CMClass.classID(E).toLowerCase();
                    results.append(sex);
                }
                break;
            }
            case 53: // incontainer
            {
                String arg1=CMParms.cleanBit(funcParms);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null)
                {
                    if(E instanceof MOB)
                    {
                        if(((MOB)E).riding()!=null)
                            results.append(((MOB)E).riding().Name());
                    }
                    else
                    if(E instanceof Item)
                    {
                        if(((Item)E).riding()!=null)
                            results.append(((Item)E).riding().Name());
                        else
                        if(((Item)E).container()!=null)
                            results.append(((Item)E).container().Name());
                        else
                        if(E instanceof Container)
                        {
                            Vector V=((Container)E).getContents();
                            for(int v=0;v<V.size();v++)
                                results.append("\""+((Item)V.elementAt(v)).Name()+"\" ");
                        }
                    }
                }
                break;
            }
            case 27: // var
            {
                String arg1=CMParms.getCleanBit(funcParms,0);
                String arg2=CMParms.getPastBitClean(funcParms,0).toUpperCase();
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String val=getVar(E,arg1,arg2,source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp);
                results.append(val);
                break;
            }
            case 41: // eval
                results.append("[unimplemented function]");
                break;
            case 40: // number
            {
                String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).trim();
                boolean isnumber=(val.length()>0);
                for(int i=0;i<val.length();i++)
                    if(!Character.isDigit(val.charAt(i)))
                    { isnumber=false; break;}
                if(isnumber)
                    results.append(CMath.s_long(val.trim()));
                break;
            }
            case 42: // randnum
            {
                String arg1String=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).toUpperCase();
                int arg1=0;
                if(CMath.isMathExpression(arg1String))
                    arg1=CMath.s_parseIntExpression(arg1String.trim());
                else
                    arg1=CMParms.parse(arg1String.trim()).size();
                results.append(CMLib.dice().roll(1,arg1,0));
                break;
            }
            case 71: // rand0num
            {
                String arg1String=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).toUpperCase();
                int arg1=0;
                if(CMath.isMathExpression(arg1String))
                    arg1=CMath.s_parseIntExpression(arg1String.trim());
                else
                    arg1=CMParms.parse(arg1String.trim()).size();
                results.append(CMLib.dice().roll(1,arg1,-1));
                break;
            }
            default:
                logError(scripted,"Unknown Val",preFab,evaluable);
                return results.toString();
            }
            }
            if((z>=0)&&(z<=evaluable.length()))
            {
                evaluable=evaluable.substring(z+1).trim();
                uevaluable=uevaluable.substring(z+1).trim();
            }
        }
        return results.toString();
    }

    protected MOB getRandPC(MOB monster, Object[] tmp, Room room)
    {
        if((tmp[SPECIAL_RANDPC]==null)||(tmp[SPECIAL_RANDPC]==monster)) {
            MOB M=null;
            if(room!=null) {
                Vector choices = new Vector();
                for(int p=0;p<room.numInhabitants();p++)
                {
                    M=room.fetchInhabitant(p);
                    if((!M.isMonster())&&(M!=monster))
                    {
                        HashSet seen=new HashSet();
                        while((M.amFollowing()!=null)&&(!M.amFollowing().isMonster())&&(!seen.contains(M)))
                        {
                            seen.add(M);
                            M=M.amFollowing();
                        }
                        choices.addElement(M);
                    }
                }
                if(choices.size() > 0)
                    tmp[SPECIAL_RANDPC] = choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
            }
        }
        return (MOB)tmp[SPECIAL_RANDPC];
    }
    protected MOB getRandAnyone(MOB monster, Object[] tmp, Room room)
    {
        if((tmp[SPECIAL_RANDANYONE]==null)||(tmp[SPECIAL_RANDANYONE]==monster)) {
            MOB M=null;
            if(room!=null) {
                Vector choices = new Vector();
                for(int p=0;p<room.numInhabitants();p++)
                {
                    M=room.fetchInhabitant(p);
                    if(M!=monster)
                    {
                        HashSet seen=new HashSet();
                        while((M.amFollowing()!=null)&&(!M.amFollowing().isMonster())&&(!seen.contains(M)))
                        {
                            seen.add(M);
                            M=M.amFollowing();
                        }
                        choices.addElement(M);
                    }
                }
                if(choices.size() > 0)
                    tmp[SPECIAL_RANDANYONE] = choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
            }
        }
        return (MOB)tmp[SPECIAL_RANDANYONE];
    }

    public String execute(Environmental scripted,
                          MOB source,
                          Environmental target,
                          MOB monster,
                          Item primaryItem,
                          Item secondaryItem,
                          DVector script,
                          String msg,
                          Object[] tmp)
    {
        tickStatus=Tickable.STATUS_START;
        String s=null;
        String[] tt=null;
        String cmd=null;
        for(int si=1;si<script.size();si++)
        {
            s=((String)script.elementAt(si,1)).trim();
            tt=(String[])script.elementAt(si,2);
            if(tt!=null)
                cmd=tt[0];
            else
                cmd=CMParms.getCleanBit(s,0).toUpperCase();
            if(cmd.length()==0) continue;

            Integer methCode=(Integer)methH.get(cmd);
            if((methCode==null)&&(cmd.startsWith("MP")))
                for(int i=0;i<methods.length;i++)
                    if(methods[i].startsWith(cmd))
                        methCode=Integer.valueOf(i);
            if(methCode==null) methCode=Integer.valueOf(0);
            tickStatus=Tickable.STATUS_MISC3+methCode.intValue();
            switch(methCode.intValue())
            {
            case 57: // <SCRIPT>
            {
                if(tt==null) tt=parseBits(script,si,"C");
                StringBuffer jscript=new StringBuffer("");
                while((++si)<script.size())
                {
                    s=((String)script.elementAt(si,1)).trim();
                    tt=(String[])script.elementAt(si,2);
                    if(tt!=null)
                        cmd=tt[0];
                    else
                        cmd=CMParms.getCleanBit(s,0).toUpperCase();
                    if(cmd.equals("</SCRIPT>"))
                    {
                        if(tt==null) tt=parseBits(script,si,"C");
                        break;
                    }
                    jscript.append(s+"\n");
                }
                if(CMSecurity.isApprovedJScript(jscript))
                {
                    Context cx = Context.enter();
                    try
                    {
                        JScriptEvent scope = new JScriptEvent(this,scripted,source,target,monster,primaryItem,secondaryItem,msg);
                        cx.initStandardObjects(scope);
                        String[] names = { "host", "source", "target", "monster", "item", "item2", "message" ,"getVar", "setVar", "toJavaString"};
                        scope.defineFunctionProperties(names, JScriptEvent.class,
                                                       ScriptableObject.DONTENUM);
                        cx.evaluateString(scope, jscript.toString(),"<cmd>", 1, null);
                    }
                    catch(Exception e)
                    {
                        Log.errOut("Scripting",scripted.name()+"/"+CMLib.map().getExtendedRoomID(lastKnownLocation)+"/JSCRIPT Error: "+e.getMessage());
                    }
                    Context.exit();
                }
                else
                if(CMProps.getIntVar(CMProps.SYSTEMI_JSCRIPTS)==1)
                {
                    if(lastKnownLocation!=null)
                        lastKnownLocation.showHappens(CMMsg.MSG_OK_ACTION,"A Javascript was not authorized.  Contact an Admin to use MODIFY JSCRIPT to authorize this script.");
                }
                break;
            }
            case 19: // if
            {
                if(tt==null){
                    try {
                        String[] ttParms=parseEval(s.substring(2));
                        tt=new String[ttParms.length+1];
                        tt[0]="IF";
                        for(int i=0;i<ttParms.length;i++)
                            tt[i+1]=ttParms[i];
                        script.setElementAt(si,2,tt);
                    } catch(Exception e) {
                        logError(scripted,"IF","Syntax",e.getMessage());
                        tickStatus=Tickable.STATUS_END;
                        return null;
                    }
                }
                String[][] EVAL={tt};
                boolean condition=eval(scripted,source,target,monster,primaryItem,secondaryItem,msg,tmp,EVAL,1);
                if(EVAL[0]!=tt)
                {
                    tt=EVAL[0];
                    script.setElementAt(si,2,tt);
                }
                DVector V=new DVector(2);
                V.addElement("",null);
                int depth=0;
                boolean foundendif=false;
                boolean ignoreUntilEndScript=false;
                si++;
                while(si<script.size())
                {
                    s=((String)script.elementAt(si,1)).trim();
                    tt=(String[])script.elementAt(si,2);
                    if(tt!=null)
                        cmd=tt[0];
                    else
                        cmd=CMParms.getCleanBit(s,0).toUpperCase();
                    if(cmd.equals("<SCRIPT>"))
                    {
                        if(tt==null) tt=parseBits(script,si,"C");
                        ignoreUntilEndScript=true;
                    }
                    else
                    if(cmd.equals("</SCRIPT>"))
                    {
                        if(tt==null) tt=parseBits(script,si,"C");
                        ignoreUntilEndScript=false;
                    }
                    else
                    if(ignoreUntilEndScript){}
                    else
                    if(cmd.equals("ENDIF")&&(depth==0))
                    {
                        if(tt==null) tt=parseBits(script,si,"C");
                        foundendif=true;
                        break;
                    }
                    else
                    if(cmd.equals("ELSE")&&(depth==0))
                    {
                        condition=!condition;
                        if(s.substring(4).trim().length()>0)
                        {
                            script.setElementAt(si,1,"ELSE");
                            script.setElementAt(si,2,new String[]{"ELSE"});
                            script.insertElementAt(si+1,s.substring(4).trim(),null);
                        }
                        else
                        if(tt==null) 
                            tt=parseBits(script,si,"C");
                    }
                    else
                    {
                        if(cmd.equals("IF"))
                            depth++;
                        else
                        if(cmd.equals("ENDIF"))
                        {
                            if(tt==null) 
                                tt=parseBits(script,si,"C");
                            depth--;
                        }
                        if(condition)
                            V.addSharedElements(script.elementsAt(si));
                    }
                    si++;
                }
                if(!foundendif)
                {
                    logError(scripted,"IF","Syntax"," Without ENDIF!");
                    tickStatus=Tickable.STATUS_END;
                    return null;
                }
                if(V.size()>1)
                {
                    //source.tell("Starting "+conditionStr);
                    //for(int v=0;v<V.size();v++)
                    //  source.tell("Statement "+((String)V.elementAt(v)));
                    String response=execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
                    if(response!=null)
                    {
                        tickStatus=Tickable.STATUS_END;
                        return response;
                    }
                    //source.tell("Stopping "+conditionStr);
                }
                break;
            }
            case 70: // switch
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                String var=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]).trim();
                DVector V=new DVector(2);
                V.addElement("",null);
                int depth=0;
                boolean foundendif=false;
                boolean ignoreUntilEndScript=false;
                boolean inCase=false;
                boolean matchedCase=false;
                si++;
                String s2=null;
                while(si<script.size())
                {
                    s=((String)script.elementAt(si,1)).trim();
                    tt=(String[])script.elementAt(si,2);
                    if(tt!=null)
                        cmd=tt[0];
                    else
                        cmd=CMParms.getCleanBit(s,0).toUpperCase();
                    if(cmd.equals("<SCRIPT>"))
                    {
                        if(tt==null) tt=parseBits(script,si,"C");
                        ignoreUntilEndScript=true;
                    }
                    else
                    if(cmd.equals("</SCRIPT>"))
                    {
                        if(tt==null) tt=parseBits(script,si,"C");
                        ignoreUntilEndScript=false;
                    }
                    else
                    if(ignoreUntilEndScript){}
                    else
                    if(cmd.equals("ENDSWITCH")&&(depth==0))
                    {
                        if(tt==null) tt=parseBits(script,si,"C");
                        foundendif=true;
                        break;
                    }
                    else
                    if(cmd.equals("CASE")&&(depth==0))
                    {
                        if(tt==null){
                        	tt=parseBits(script,si,"Ccr");
                        	if(tt==null) return null;
                        }
                        s2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]).trim();
                        inCase=var.equalsIgnoreCase(s2);
                        matchedCase=matchedCase||inCase;
                    }
                    else
                    if(cmd.equals("DEFAULT")&&(depth==0))
                    {
                        inCase=!matchedCase;
                    }
                    else
                    {
                        if(inCase)
                            V.addElement(s,tt);
                        if(cmd.equals("SWITCH"))
                        {
                            if(tt==null) tt=parseBits(script,si,"Cr");
                            depth++;
                        }
                        else
                        if(cmd.equals("ENDSWITCH"))
                        {
                            if(tt==null) tt=parseBits(script,si,"C");
                            depth--;
                        }
                    }
                    si++;
                }
                if(!foundendif)
                {
                    logError(scripted,"SWITCH","Syntax"," Without ENDSWITCH!");
                    tickStatus=Tickable.STATUS_END;
                    return null;
                }
                if(V.size()>1)
                {
                    String response=execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
                    if(response!=null)
                    {
                        tickStatus=Tickable.STATUS_END;
                        return response;
                    }
                }
                break;
            }
            case 62: // for x = 1 to 100
            {
                if(tt==null)
                {
            		tt=parseBits(script,si,"CcccCr");
                	if(tt==null) return null;
                }
                if(tt[5].length()==0)
                {
                    logError(scripted,"FOR","Syntax","5 parms required!");
                    tickStatus=Tickable.STATUS_END;
                    return null;
                }
                String varStr=tt[1];
                if((varStr.length()!=2)||(varStr.charAt(0)!='$')||(!Character.isDigit(varStr.charAt(1))))
                {
                    logError(scripted,"FOR","Syntax","'"+varStr+"' is not a tmp var $1, $2..");
                    tickStatus=Tickable.STATUS_END;
                    return null;
                }
                int whichVar=CMath.s_int(Character.toString(varStr.charAt(1)));
                if((tmp[whichVar] instanceof String)
                &&(((String)tmp[whichVar]).length()>0)
                &&(CMath.isInteger(((String)tmp[whichVar]).trim())))
                {
                    logError(scripted,"FOR","Syntax","'"+whichVar+"' is already in use! Use a different one!");
                    tickStatus=Tickable.STATUS_END;
                    return null;
                }
                if(!tt[2].equals("="))
                {
                    logError(scripted,"FOR","Syntax","'"+s+"' is illegal for syntax!");
                    tickStatus=Tickable.STATUS_END;
                    return null;
                }

                int toAdd=0;
                if(tt[4].equals("TO<"))
                    toAdd=-1;
                else
                if(tt[4].equals("TO>"))
                    toAdd=1;
                else
                if(!tt[4].equals("TO"))
                {
                    logError(scripted,"FOR","Syntax","'"+s+"' is illegal for syntax!");
                    tickStatus=Tickable.STATUS_END;
                    return null;
                }
                String from=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]).trim();
                String to=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[5]).trim();
                if((!CMath.isInteger(from))||(!CMath.isInteger(to)))
                {
                    logError(scripted,"FOR","Syntax","'"+from+"-"+to+"' is illegal range!");
                    tickStatus=Tickable.STATUS_END;
                    return null;
                }
                DVector V=new DVector(2);
                V.addElement("",null);
                int depth=0;
                boolean foundnext=false;
                boolean ignoreUntilEndScript=false;
                si++;
                while(si<script.size())
                {
                    s=((String)script.elementAt(si,1)).trim();
                    tt=(String[])script.elementAt(si,2);
                    if(tt!=null)
                        cmd=tt[0];
                    else
                        cmd=CMParms.getCleanBit(s,0).toUpperCase();
                    if(cmd.equals("<SCRIPT>"))
                    {
                        if(tt==null) tt=parseBits(script,si,"C");
                        ignoreUntilEndScript=true;
                    }
                    else
                    if(cmd.equals("</SCRIPT>"))
                    {
                        if(tt==null) tt=parseBits(script,si,"C");
                        ignoreUntilEndScript=false;
                    }
                    else
                    if(ignoreUntilEndScript){}
                    else
                    if(cmd.equals("NEXT")&&(depth==0))
                    {
                        if(tt==null) tt=parseBits(script,si,"C");
                        foundnext=true;
                        break;
                    }
                    else
                    {
                        if(cmd.equals("FOR"))
                        {
                            if(tt==null) tt=parseBits(script,si,"CcccCr");
                            depth++;
                        }
                        else
                        if(cmd.equals("NEXT"))
                        {
                            if(tt==null) tt=parseBits(script,si,"C");
                            depth--;
                        }
                        V.addSharedElements(script.elementsAt(si));
                    }
                    si++;
                }
                if(!foundnext)
                {
                    logError(scripted,"FOR","Syntax"," Without NEXT!");
                    tickStatus=Tickable.STATUS_END;
                    return null;
                }
                if(V.size()>1)
                {
                    //source.tell("Starting "+conditionStr);
                    //for(int v=0;v<V.size();v++)
                    //  source.tell("Statement "+((String)V.elementAt(v)));
                    int fromInt=CMath.s_int(from);
                    int toInt=CMath.s_int(to);
                    int increment=(toInt>=fromInt)?1:-1;
                    String response=null;
                    if(((increment>0)&&(fromInt<=(toInt+toAdd)))
                    ||((increment<0)&&(fromInt>=(toInt+toAdd))))
                    {
                        toInt+=toAdd;
                        long tm=System.currentTimeMillis()+(10 * 1000);
                        for(int forLoop=fromInt;forLoop!=toInt;forLoop+=increment)
                        {
                            tmp[whichVar]=""+forLoop;
                            response=execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
                            if(response!=null) break;
                            if(System.currentTimeMillis()>tm)
                            {
                                logError(scripted,"FOR","Runtime","For loop violates 10 second rule: " +s);
                                break;
                            }
                        }
                        tmp[whichVar]=""+toInt;
                        response=execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
                    }
                    if(response!=null)
                    {
                        tickStatus=Tickable.STATUS_END;
                        return response;
                    }
                    tmp[whichVar]=null;
                    //source.tell("Stopping "+conditionStr);
                }
                break;
            }
            case 50: // break;
                if(tt==null) tt=parseBits(script,si,"C");
                tickStatus=Tickable.STATUS_END;
                return null;
            case 1: // mpasound
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cp");
                	if(tt==null) return null;
                }
                String echo=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                //lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,echo);
                for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
                {
                    Room R2=lastKnownLocation.getRoomInDir(d);
                    Exit E2=lastKnownLocation.getExitInDir(d);
                    if((R2!=null)&&(E2!=null)&&(E2.isOpen()))
                        R2.showOthers(monster,null,null,CMMsg.MSG_OK_ACTION,echo);
                }
                break;
            }
            case 4: // mpjunk
            {
                if(tt==null){
                	tt=parseBits(script,si,"CR");
                	if(tt==null) return null;
                }
                if(tt[1].equals("ALL") && (monster!=null))
                {
                    while(monster.inventorySize()>0)
                    {
                        Item I=monster.fetchInventory(0);
                        if(I!=null)
                        {
                            if(I.owner()==null) I.setOwner(monster);
                            I.destroy();
                        }
                        else
                            break;
                    }
                }
                else
                {
                    Environmental E=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    Item I=null;
                    if(E instanceof Item)
                        I=(Item)E;
                    if((I==null)&&(monster!=null))
                        I=monster.fetchInventory(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]));
                    if((I==null)&&(scripted instanceof Room))
                        I=((Room)scripted).fetchAnyItem(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]));
                    if(I!=null)
                        I.destroy();
                }
                break;
            }
            case 2: // mpecho
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cp");
                	if(tt==null) return null;
                }
                if(lastKnownLocation!=null)
                    lastKnownLocation.show(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]));
                break;
            }
            case 13: // mpunaffect
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String which=tt[2];
                if(newTarget!=null)
                if(which.equalsIgnoreCase("all")||(which.length()==0))
                {
                    for(int a=newTarget.numEffects()-1;a>=0;a--)
                    {
                        Ability A=newTarget.fetchEffect(a);
                        if(A!=null)
                            A.unInvoke();
                    }
                }
                else
                {
                    Ability A2=CMClass.findAbility(which);
                    if(A2!=null) which=A2.ID();
                    Ability A=newTarget.fetchEffect(which);
                    if(A!=null)
                    {
                        if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
                            Log.sysOut("Scripting",newTarget.Name()+" was MPUNAFFECTED by "+A.Name());
                        A.unInvoke();
                        if(newTarget.fetchEffect(which)==A)
                            newTarget.delEffect(A);
                    }
                }
                break;
            }
            case 3: // mpslay
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget!=null)&&(newTarget instanceof MOB))
                    CMLib.combat().postDeath(monster,(MOB)newTarget,null);
                break;
            }
            case 73: // mpsetinternal
            {
                if(tt==null){
                	tt=parseBits(script,si,"CCr");
                	if(tt==null) return null;
                }
                String arg2=tt[1];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                if(arg2.equals("SCOPE"))
                    setVarScope(arg3);
                else
                if(arg2.equals("NODELAY"))
                    noDelay=CMath.s_bool(arg3);
                else
                if(arg2.equals("DEFAULTQUEST"))
                    registerDefaultQuest(arg3);
                else
                if(arg2.equals("SAVABLE"))
                    setSavable(CMath.s_bool(arg3));
                else
                    logError(scripted,"MPSETINTERNAL","Syntax","Unknown stat: "+arg2);
                break;
            }
            case 74: // mpprompt
            {
                if(tt==null){
                	tt=parseBits(script,si,"CCCr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String var=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                String promptStr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                if((newTarget!=null)&&(newTarget instanceof MOB)&&((((MOB)newTarget).session()!=null)))
                {
                    try {
                        String value=((MOB)newTarget).session().prompt(promptStr);
                        setVar(newTarget.Name(),var,value);
                    } catch(Exception e) { return "";}
                }
                break;
            }
            case 75: // mpconfirm
            {
                if(tt==null){
                	tt=parseBits(script,si,"CCCCr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String var=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                String defaultVal=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                String promptStr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[4]);
                if((newTarget!=null)&&(newTarget instanceof MOB)&&((((MOB)newTarget).session()!=null)))
                {
                    try {
                        String value=((MOB)newTarget).session().confirm(promptStr,defaultVal)?"Y":"N";
                        setVar(newTarget.Name(),var,value);
                    } catch(Exception e) { return "";}
                }
                break;
            }
            case 76: // mpchoose
            {
                if(tt==null){
                	tt=parseBits(script,si,"CCCCCr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String var=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                String choices=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                String defaultVal=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[4]);
                String promptStr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[5]);
                if((newTarget!=null)&&(newTarget instanceof MOB)&&((((MOB)newTarget).session()!=null)))
                {
                    try {
                        String value=((MOB)newTarget).session().choose(promptStr,choices,defaultVal);
                        setVar(newTarget.Name(),var,value);
                    } catch(Exception e) { return "";}
                }
                break;
            }
            case 16: // mpset
            {
                if(tt==null){
                	tt=parseBits(script,si,"CCcr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String arg2=tt[2];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                if(newTarget!=null)
                {
                    if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
                        Log.sysOut("Scripting",newTarget.Name()+" has "+arg2+" MPSETTED to "+arg3);
                    boolean found=false;
                    for(int i=0;i<newTarget.getStatCodes().length;i++)
                    {
                        if(newTarget.getStatCodes()[i].equalsIgnoreCase(arg2))
                        {
                            if(arg3.equals("++")) arg3=""+(CMath.s_int(newTarget.getStat(newTarget.getStatCodes()[i]))+1);
                            if(arg3.equals("--")) arg3=""+(CMath.s_int(newTarget.getStat(newTarget.getStatCodes()[i]))-1);
                            newTarget.setStat(arg2,arg3);
                            found=true;
                            break;
                        }
                    }
                    if((!found)&&(newTarget instanceof MOB))
                    {
                        MOB M=(MOB)newTarget;
                        for(int i : CharStats.CODES.ALL())
                            if(CharStats.CODES.NAME(i).equalsIgnoreCase(arg2)||CharStats.CODES.DESC(i).equalsIgnoreCase(arg2))
                            {
                                if(arg3.equals("++")) arg3=""+(M.baseCharStats().getStat(i)+1);
                                if(arg3.equals("--")) arg3=""+(M.baseCharStats().getStat(i)-1);
                                M.baseCharStats().setStat(i,CMath.s_int(arg3.trim()));
                                M.recoverCharStats();
                                if(arg2.equalsIgnoreCase("RACE"))
                                    M.charStats().getMyRace().startRacing(M,false);
                                found=true;
                                break;
                            }
                        if(!found)
                        for(int i=0;i<M.curState().getStatCodes().length;i++)
                            if(M.curState().getStatCodes()[i].equalsIgnoreCase(arg2))
                            {
                                if(arg3.equals("++")) arg3=""+(CMath.s_int(M.curState().getStat(M.curState().getStatCodes()[i]))+1);
                                if(arg3.equals("--")) arg3=""+(CMath.s_int(M.curState().getStat(M.curState().getStatCodes()[i]))-1);
                                M.curState().setStat(arg2,arg3);
                                found=true;
                                break;
                            }
                        if(!found)
                        for(int i=0;i<M.baseEnvStats().getStatCodes().length;i++)
                            if(M.baseEnvStats().getStatCodes()[i].equalsIgnoreCase(arg2))
                            {
                                if(arg3.equals("++")) arg3=""+(CMath.s_int(M.baseEnvStats().getStat(M.baseEnvStats().getStatCodes()[i]))+1);
                                if(arg3.equals("--")) arg3=""+(CMath.s_int(M.baseEnvStats().getStat(M.baseEnvStats().getStatCodes()[i]))-1);
                                M.baseEnvStats().setStat(arg2,arg3);
                                found=true;
                                break;
                            }
                        if((!found)&&(M.playerStats()!=null))
                        for(int i=0;i<M.playerStats().getStatCodes().length;i++)
                            if(M.playerStats().getStatCodes()[i].equalsIgnoreCase(arg2))
                            {
                                if(arg3.equals("++")) arg3=""+(CMath.s_int(M.playerStats().getStat(M.playerStats().getStatCodes()[i]))+1);
                                if(arg3.equals("--")) arg3=""+(CMath.s_int(M.playerStats().getStat(M.playerStats().getStatCodes()[i]))-1);
                                M.playerStats().setStat(arg2,arg3);
                                found=true;
                                break;
                            }
                        if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
                            for(int i=0;i<M.baseState().getStatCodes().length;i++)
                                if(M.baseState().getStatCodes()[i].equalsIgnoreCase(arg2.substring(4)))
                                {
                                    if(arg3.equals("++")) arg3=""+(CMath.s_int(M.baseState().getStat(M.baseState().getStatCodes()[i]))+1);
                                    if(arg3.equals("--")) arg3=""+(CMath.s_int(M.baseState().getStat(M.baseState().getStatCodes()[i]))-1);
                                    M.baseState().setStat(arg2.substring(4),arg3);
                                    found=true;
                                    break;
                                }
                        if((!found)&&(gstatH.containsKey(arg2.toUpperCase()))) {
                            found=true;
                            switch(((Integer)gstatH.get(arg2.toUpperCase())).intValue()) {
                            case GSTATADD_DEITY: M.setWorshipCharID(arg3); break;
                            case GSTATADD_CLAN: M.setClanID(arg3); break;
                            case GSTATADD_CLANROLE: M.setClanRole(CMath.s_int(arg3)); break;
                            }
                        }
                    }

                    if(!found)
                    {
                        logError(scripted,"MPSET","Syntax","Unknown stat: "+arg2+" for "+newTarget.Name());
                        break;
                    }
                    if(newTarget instanceof MOB)
                        ((MOB)newTarget).recoverCharStats();
                    newTarget.recoverEnvStats();
                    if(newTarget instanceof MOB)
                    {
                        ((MOB)newTarget).recoverMaxState();
                        if(arg2.equalsIgnoreCase("LEVEL"))
                        {
                            CMLib.leveler().fillOutMOB(((MOB)newTarget),((MOB)newTarget).baseEnvStats().level());
                            ((MOB)newTarget).recoverMaxState();
                            ((MOB)newTarget).recoverCharStats();
                            ((MOB)newTarget).recoverEnvStats();
                            ((MOB)newTarget).resetToMaxState();
                        }
                    }
                }
                break;
            }
            case 63: // mpargset
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                String arg1=tt[1];
                String arg2=tt[2];
                if((arg1.length()!=2)||(!arg1.startsWith("$")))
                {
                    logError(scripted,"MPARGSET","Syntax","Mangled argument var: "+arg1+" for "+scripted.Name());
                    break;
                }
                Object O=getArgumentMOB(arg2,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if(O==null) O=getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((O==null)
                &&((!arg2.trim().startsWith("$"))
                    ||(arg2.length()<2)
                    ||((!Character.isDigit(arg2.charAt(1)))
                        &&(!Character.isLetter(arg2.charAt(1))))))
                    O=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg2);
                char c=arg1.charAt(1);
                if(Character.isDigit(c))
                {
                    if((O instanceof String)&&(((String)O).equalsIgnoreCase("null")))
                        O=null;
                    tmp[CMath.s_int(Character.toString(c))]=O;
                }
                else
                switch(arg1.charAt(1))
                {
                case 'N':
                case 'n': if(O instanceof MOB) source=(MOB)O; break;
                case 'B':
                case 'b': if(O instanceof Environmental)
                                lastLoaded=(Environmental)O;
                          break;
                case 'I':
                case 'i': if(O instanceof Environmental) scripted=(Environmental)O;
                          if(O instanceof MOB) monster=(MOB)O;
                          break;
                case 'T':
                case 't': if(O instanceof Environmental) target=(Environmental)O; break;
                case 'O':
                case 'o': if(O instanceof Item) primaryItem=(Item)O; break;
                case 'P':
                case 'p': if(O instanceof Item) secondaryItem=(Item)O; break;
                case 'd':
                case 'D': if(O instanceof Room) lastKnownLocation=(Room)O; break;
                case 'g':
                case 'G': if(O instanceof String) msg=(String)O; break;
                default:
                    logError(scripted,"MPARGSET","Syntax","Invalid argument var: "+arg1+" for "+scripted.Name());
                    break;
                }
                break;
            }
            case 35: // mpgset
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String arg2=tt[2];
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                if(newTarget!=null)
                {
                    if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
                        Log.sysOut("Scripting",newTarget.Name()+" has "+arg2+" MPGSETTED to "+arg3);
                    boolean found=false;
                    for(int i=0;i<newTarget.getStatCodes().length;i++)
                    {
                        if(newTarget.getStatCodes()[i].equalsIgnoreCase(arg2))
                        {
                            if(arg3.equals("++")) arg3=""+(CMath.s_int(newTarget.getStat(newTarget.getStatCodes()[i]))+1);
                            if(arg3.equals("--")) arg3=""+(CMath.s_int(newTarget.getStat(newTarget.getStatCodes()[i]))-1);
                            newTarget.setStat(newTarget.getStatCodes()[i],arg3);
                            found=true; break;
                        }
                    }
                    if(!found)
                    if(newTarget instanceof MOB)
                    {
                        for(int i=0;i<GenericBuilder.GENMOBCODES.length;i++)
                        {
                            if(GenericBuilder.GENMOBCODES[i].equalsIgnoreCase(arg2))
                            {
                                if(arg3.equals("++")) arg3=""+(CMath.s_int(CMLib.coffeeMaker().getGenMobStat((MOB)newTarget,GenericBuilder.GENMOBCODES[i]))+1);
                                if(arg3.equals("--")) arg3=""+(CMath.s_int(CMLib.coffeeMaker().getGenMobStat((MOB)newTarget,GenericBuilder.GENMOBCODES[i]))-1);
                                CMLib.coffeeMaker().setGenMobStat((MOB)newTarget,GenericBuilder.GENMOBCODES[i],arg3);
                                found=true;
                                break;
                            }
                        }
                        if(!found)
                        {
                            MOB M=(MOB)newTarget;
                            for(int i : CharStats.CODES.ALL())
                            {
                                if(CharStats.CODES.NAME(i).equalsIgnoreCase(arg2)||CharStats.CODES.DESC(i).equalsIgnoreCase(arg2))
                                {
                                    if(arg3.equals("++")) arg3=""+(M.baseCharStats().getStat(i)+1);
                                    if(arg3.equals("--")) arg3=""+(M.baseCharStats().getStat(i)-1);
                                    if((arg3.length()==1)&&(Character.isLetter(arg3.charAt(0))))
                                        M.baseCharStats().setStat(i,arg3.charAt(0));
                                    else
                                        M.baseCharStats().setStat(i,CMath.s_int(arg3.trim()));
                                    M.recoverCharStats();
                                    if(arg2.equalsIgnoreCase("RACE"))
                                        M.charStats().getMyRace().startRacing(M,false);
                                    found=true;
                                    break;
                                }
                            }
                            if(!found)
                            for(int i=0;i<M.curState().getStatCodes().length;i++)
                            {
                                if(M.curState().getStatCodes()[i].equalsIgnoreCase(arg2))
                                {
                                    if(arg3.equals("++")) arg3=""+(CMath.s_int(M.curState().getStat(M.curState().getStatCodes()[i]))+1);
                                    if(arg3.equals("--")) arg3=""+(CMath.s_int(M.curState().getStat(M.curState().getStatCodes()[i]))-1);
                                    M.curState().setStat(arg2,arg3);
                                    found=true;
                                    break;
                                }
                            }
                            if(!found)
                            for(int i=0;i<M.baseEnvStats().getStatCodes().length;i++)
                            {
                                if(M.baseEnvStats().getStatCodes()[i].equalsIgnoreCase(arg2))
                                {
                                    if(arg3.equals("++")) arg3=""+(CMath.s_int(M.baseEnvStats().getStat(M.baseEnvStats().getStatCodes()[i]))+1);
                                    if(arg3.equals("--")) arg3=""+(CMath.s_int(M.baseEnvStats().getStat(M.baseEnvStats().getStatCodes()[i]))-1);
                                    M.baseEnvStats().setStat(arg2,arg3);
                                    found=true;
                                    break;
                                }
                            }
                            if((!found)&&(M.playerStats()!=null))
                            for(int i=0;i<M.playerStats().getStatCodes().length;i++)
                                if(M.playerStats().getStatCodes()[i].equalsIgnoreCase(arg2))
                                {
                                    if(arg3.equals("++")) arg3=""+(CMath.s_int(M.playerStats().getStat(M.playerStats().getStatCodes()[i]))+1);
                                    if(arg3.equals("--")) arg3=""+(CMath.s_int(M.playerStats().getStat(M.playerStats().getStatCodes()[i]))-1);
                                    M.playerStats().setStat(arg2,arg3);
                                    found=true;
                                    break;
                                }
                            if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
                                for(int i=0;i<M.baseState().getStatCodes().length;i++)
                                    if(M.baseState().getStatCodes()[i].equalsIgnoreCase(arg2.substring(4)))
                                    {
                                        if(arg3.equals("++")) arg3=""+(CMath.s_int(M.baseState().getStat(M.baseState().getStatCodes()[i]))+1);
                                        if(arg3.equals("--")) arg3=""+(CMath.s_int(M.baseState().getStat(M.baseState().getStatCodes()[i]))-1);
                                        M.baseState().setStat(arg2.substring(4),arg3);
                                        found=true;
                                        break;
                                    }
                            if((!found)&&(gstatH.containsKey(arg2.toUpperCase()))) {
                                found=true;
                                switch(((Integer)gstatH.get(arg2.toUpperCase())).intValue()) {
                                case GSTATADD_DEITY: M.setWorshipCharID(arg3); break;
                                case GSTATADD_CLAN: M.setClanID(arg3); break;
                                case GSTATADD_CLANROLE: M.setClanRole(CMath.s_int(arg3)); break;
                                }
                            }
                        }
                    }
                    else
                    if(newTarget instanceof Item)
                    {
                        for(int i=0;i<GenericBuilder.GENITEMCODES.length;i++)
                        {
                            if(GenericBuilder.GENITEMCODES[i].equalsIgnoreCase(arg2))
                            {
                                if(arg3.equals("++")) arg3=""+(CMath.s_int(CMLib.coffeeMaker().getGenItemStat((Item)newTarget,GenericBuilder.GENITEMCODES[i]))+1);
                                if(arg3.equals("--")) arg3=""+(CMath.s_int(CMLib.coffeeMaker().getGenItemStat((Item)newTarget,GenericBuilder.GENITEMCODES[i]))-1);
                                CMLib.coffeeMaker().setGenItemStat((Item)newTarget,GenericBuilder.GENITEMCODES[i],arg3);
                                found=true;
                                break;
                            }
                        }
                    }

                    if(!found)
                    {
                        logError(scripted,"MPGSET","Syntax","Unknown stat: "+arg2+" for "+newTarget.Name());
                        break;
                    }
                    if(newTarget instanceof MOB)
                        ((MOB)newTarget).recoverCharStats();
                    newTarget.recoverEnvStats();
                    if(newTarget instanceof MOB)
                    {
                        ((MOB)newTarget).recoverMaxState();
                        if(arg2.equalsIgnoreCase("LEVEL"))
                        {
                            CMLib.leveler().fillOutMOB(((MOB)newTarget),((MOB)newTarget).baseEnvStats().level());
                            ((MOB)newTarget).recoverMaxState();
                            ((MOB)newTarget).recoverCharStats();
                            ((MOB)newTarget).recoverEnvStats();
                            ((MOB)newTarget).resetToMaxState();
                        }
                    }
                }
                break;
            }
            case 11: // mpexp
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String amtStr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]).trim();
                int t=CMath.s_int(amtStr);
                if((newTarget!=null)&&(newTarget instanceof MOB))
                {
                    if((amtStr.endsWith("%"))
                    &&(((MOB)newTarget).getExpNeededLevel()<Integer.MAX_VALUE))
                    {
                        int baseLevel=newTarget.baseEnvStats().level();
                        int lastLevelExpNeeded=(baseLevel<=1)?0:CMLib.leveler().getLevelExperience(baseLevel-1);
                        int thisLevelExpNeeded=CMLib.leveler().getLevelExperience(baseLevel);
                        t=(int)Math.round(CMath.mul(thisLevelExpNeeded-lastLevelExpNeeded,
                                            CMath.div(CMath.s_int(amtStr.substring(0,amtStr.length()-1)),100.0)));
                    }
                    if(t!=0) CMLib.leveler().postExperience((MOB)newTarget,null,null,t,false);
                }
                break;
            }
            case 77: // mpmoney
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentMOB(tt[1],source,monster,scripted,primaryItem,secondaryItem,msg,tmp);
                if(newTarget==null) newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String amtStr=tt[2];
                amtStr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,amtStr).trim();
                boolean plus=!amtStr.startsWith("-");
                if(amtStr.startsWith("+")||amtStr.startsWith("-"))
                	amtStr=amtStr.substring(1).trim();
                String currency = CMLib.english().numPossibleGoldCurrency(source, amtStr);
                long amt = CMLib.english().numPossibleGold(source, amtStr);
                double denomination = CMLib.english().numPossibleGoldDenomination(source, currency, amtStr);
                Item container = null;
                if(newTarget instanceof Item)
                {
                	container = (newTarget instanceof Container)?(Item)newTarget:null;
                	newTarget = ((Item)newTarget).owner();
                }
                if(newTarget instanceof MOB)
                {
                	if(plus)
	                	CMLib.beanCounter().giveSomeoneMoney((MOB)newTarget, currency, amt * denomination);
                	else
	                	CMLib.beanCounter().subtractMoney((MOB)newTarget, currency, amt * denomination);
                }
                else
                {
                	if(!(newTarget instanceof Room))
                		newTarget=lastKnownLocation;
                	if(plus)
	                	CMLib.beanCounter().dropMoney((Room)newTarget, container, currency, amt * denomination);
                	else
	                	CMLib.beanCounter().removeMoney((Room)newTarget, container, currency, amt * denomination);
                }
                break;
            }
            case 59: // mpquestpoints
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]).trim();
                if(newTarget instanceof MOB)
                {
                    if(CMath.isNumber(val))
                        ((MOB)newTarget).setQuestPoint(CMath.s_int(val));
                    else
                    if(val.startsWith("++")&&(CMath.isNumber(val.substring(2).trim())))
                        ((MOB)newTarget).setQuestPoint(((MOB)newTarget).getQuestPoint()+CMath.s_int(val.substring(2).trim()));
                    else
                    if(val.startsWith("--")&&(CMath.isNumber(val.substring(2).trim())))
                        ((MOB)newTarget).setQuestPoint(((MOB)newTarget).getQuestPoint()-CMath.s_int(val.substring(2).trim()));
                    else
                        logError(scripted,"QUESTPOINTS","Syntax","Bad syntax "+val+" for "+scripted.Name());
                }
                break;
            }
            case 65: // MPQSET
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cccr");
                	if(tt==null) return null;
                }
                String qstr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                String var=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                Environmental obj=getArgumentItem(tt[3],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                Quest Q=getQuest(qstr);
                if(Q==null)
                    logError(scripted,"MPQSET","Syntax","Unknown quest "+qstr+" for "+scripted.Name());
                else
                if(var.equalsIgnoreCase("QUESTOBJ"))
                {
                    if(obj==null)
                        logError(scripted,"MPQSET","Syntax","Unknown object "+tt[3]+" for "+scripted.Name());
                    else
                    {
                        obj.baseEnvStats().setDisposition(obj.baseEnvStats().disposition()|EnvStats.IS_UNSAVABLE);
                        obj.recoverEnvStats();
                        Q.runtimeRegisterObject(obj);
                    }
                }
                else
                if(var.equalsIgnoreCase("STATISTICS")&&(val.equalsIgnoreCase("ACCEPTED")))
                	CMLib.coffeeTables().bump(Q,CoffeeTableRow.STAT_QUESTACCEPTED);
                else
                if(var.equalsIgnoreCase("STATISTICS")&&(val.equalsIgnoreCase("SUCCESS")||val.equalsIgnoreCase("WON")))
                	CMLib.coffeeTables().bump(Q,CoffeeTableRow.STAT_QUESTSUCCESS);
                else
                if(var.equalsIgnoreCase("STATISTICS")&&(val.equalsIgnoreCase("FAILED")))
                	CMLib.coffeeTables().bump(Q,CoffeeTableRow.STAT_QUESTFAILED);
                else
                {
                    if(val.equals("++")) val=""+(CMath.s_int(Q.getStat(var))+1);
                    if(val.equals("--")) val=""+(CMath.s_int(Q.getStat(var))-1);
                    Q.setStat(var,val);
                }
                break;
            }
            case 66: // MPLOG
            {
                if(tt==null){
                	tt=parseBits(script,si,"CCcr");
                	if(tt==null) return null;
                }
                String type=tt[1];
                String head=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                if(type.startsWith("E")) Log.errOut("Script","["+head+"] "+val);
                else
                if(type.startsWith("I")||type.startsWith("S")) Log.infoOut("Script","["+head+"] "+val);
                else
                if(type.startsWith("D")) Log.debugOut("Script","["+head+"] "+val);
                else
                    logError(scripted,"MPLOG","Syntax","Unknown log type "+type+" for "+scripted.Name());
                break;
            }
            case 67: // MPCHANNEL
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                String channel=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                boolean sysmsg=channel.startsWith("!");
                if(sysmsg) channel=channel.substring(1);
                String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                if(CMLib.channels().getChannelCodeNumber(channel)<0)
                    logError(scripted,"MPCHANNEL","Syntax","Unknown channel "+channel+" for "+scripted.Name());
                else
                    CMLib.commands().postChannel(monster,channel,val,sysmsg);
                break;
            }
            case 68: // unload
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cc");
                	if(tt==null) return null;
                }
                String scriptname=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                if(!new CMFile(Resources.makeFileResourceName(scriptname),null,false,true).exists())
                    logError(scripted,"MPUNLOADSCRIPT","Runtime","File does not exist: "+Resources.makeFileResourceName(scriptname));
                else
                {
                    Vector delThese=new Vector();
                    boolean foundKey=false;
                    scriptname=scriptname.toUpperCase().trim();
                    String parmname=scriptname;
                    Vector V=Resources.findResourceKeys(parmname);
                    for(Enumeration e=V.elements();e.hasMoreElements();)
                    {
                        String key=(String)e.nextElement();
                        if(key.startsWith("PARSEDPRG: ")&&(key.toUpperCase().endsWith(parmname)))
                        { foundKey=true; delThese.addElement(key);}
                    }
                    if(foundKey)
                        for(int i=0;i<delThese.size();i++)
                            Resources.removeResource((String)delThese.elementAt(i));
                }

                break;
            }
            case 60: // trains
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]).trim();
                if(newTarget instanceof MOB)
                {
                    if(CMath.isNumber(val))
                        ((MOB)newTarget).setTrains(CMath.s_int(val));
                    else
                    if(val.startsWith("++")&&(CMath.isNumber(val.substring(2).trim())))
                        ((MOB)newTarget).setTrains(((MOB)newTarget).getTrains()+CMath.s_int(val.substring(2).trim()));
                    else
                    if(val.startsWith("--")&&(CMath.isNumber(val.substring(2).trim())))
                        ((MOB)newTarget).setTrains(((MOB)newTarget).getTrains()-CMath.s_int(val.substring(2).trim()));
                    else
                        logError(scripted,"TRAINS","Syntax","Bad syntax "+val+" for "+scripted.Name());
                }
                break;
            }
            case 61: // pracs
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]).trim();
                if(newTarget instanceof MOB)
                {
                    if(CMath.isNumber(val))
                        ((MOB)newTarget).setPractices(CMath.s_int(val));
                    else
                    if(val.startsWith("++")&&(CMath.isNumber(val.substring(2).trim())))
                        ((MOB)newTarget).setPractices(((MOB)newTarget).getPractices()+CMath.s_int(val.substring(2).trim()));
                    else
                    if(val.startsWith("--")&&(CMath.isNumber(val.substring(2).trim())))
                        ((MOB)newTarget).setPractices(((MOB)newTarget).getPractices()-CMath.s_int(val.substring(2).trim()));
                    else
                        logError(scripted,"PRACS","Syntax","Bad syntax "+val+" for "+scripted.Name());
                }
                break;
            }
            case 5: // mpmload
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                String name=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                Vector Ms=new Vector();
                MOB m=CMClass.getMOB(name);
                if(m!=null) Ms.addElement(m);
                if(lastKnownLocation!=null)
                {
                    if(Ms.size()==0)
                        findSomethingCalledThis(name,monster,lastKnownLocation,Ms,true);
                    for(int i=0;i<Ms.size();i++)
                    {
                        if(Ms.elementAt(i) instanceof MOB)
                        {
                            m=(MOB)((MOB)Ms.elementAt(i)).copyOf();
                            m.text();
                            m.recoverEnvStats();
                            m.recoverCharStats();
                            m.resetToMaxState();
                            m.bringToLife(lastKnownLocation,true);
                            lastLoaded=m;
                        }
                    }
                }
                break;
            }
            case 6: // mpoload
            {
                // if not mob
                if((scripted instanceof MOB)&&(monster != null))
                {
                    if(tt==null){
                    	tt=parseBits(script,si,"Cr");
                    	if(tt==null) return null;
                    }
                    String name=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                    int containerIndex=name.toUpperCase().indexOf(" INTO ");
                    Container container=null;
                    if(containerIndex>=0)
                    {
                        Vector containers=new Vector();
                        findSomethingCalledThis(name.substring(containerIndex+6).trim(),monster,lastKnownLocation,containers,false);
                        for(int c=0;c<containers.size();c++)
                            if((containers.elementAt(c) instanceof Container)
                            &&(((Container)containers.elementAt(c)).capacity()>0))
                            {
                                container=(Container)containers.elementAt(c);
                                name=name.substring(0,containerIndex).trim();
                                break;
                            }
                    }
                    long coins=CMLib.english().numPossibleGold(null,name);
                    if(coins>0)
                    {
                        String currency=CMLib.english().numPossibleGoldCurrency(scripted,name);
                        double denom=CMLib.english().numPossibleGoldDenomination(scripted,currency,name);
                        Coins C=CMLib.beanCounter().makeCurrency(currency,denom,coins);
                        monster.addInventory(C);
                        C.putCoinsBack();
                    }
                    else
                    if(lastKnownLocation!=null)
                    {
                        Vector Is=new Vector();
                        Item m=CMClass.getItem(name);
                        if(m!=null)
                            Is.addElement(m);
                        else
                            findSomethingCalledThis(name,(MOB)scripted,lastKnownLocation,Is,false);
                        for(int i=0;i<Is.size();i++)
                        {
                            if(Is.elementAt(i) instanceof Item)
                            {
                                m=(Item)Is.elementAt(i);
                                if((m!=null)&&(!(m instanceof ArchonOnly)))
                                {
                                    m=(Item)m.copyOf();
                                    m.recoverEnvStats();
                                    m.setContainer(container);
                                    if(container instanceof MOB)
                                        ((MOB)container.owner()).addInventory(m);
                                    else
                                    if(container instanceof Room)
                                        ((Room)container.owner()).addItemRefuse(m,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
                                    else
                                        monster.addInventory(m);
                                    lastLoaded=m;
                                }
                            }
                        }
                        lastKnownLocation.recoverRoomStats();
                        monster.recoverCharStats();
                        monster.recoverEnvStats();
                        monster.recoverMaxState();
                    }
                }
                break;
            }
            case 41: // mpoloadroom
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                String name=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                if(lastKnownLocation!=null)
                {
                    Vector Is=new Vector();
                    int containerIndex=name.toUpperCase().indexOf(" INTO ");
                    Container container=null;
                    if(containerIndex>=0)
                    {
                        Vector containers=new Vector();
                        findSomethingCalledThis(name.substring(containerIndex+6).trim(),null,lastKnownLocation,containers,false);
                        for(int c=0;c<containers.size();c++)
                            if((containers.elementAt(c) instanceof Container)
                            &&(((Container)containers.elementAt(c)).capacity()>0))
                            {
                                container=(Container)containers.elementAt(c);
                                name=name.substring(0,containerIndex).trim();
                                break;
                            }
                    }
                    long coins=CMLib.english().numPossibleGold(null,name);
                    if(coins>0)
                    {
                        String currency=CMLib.english().numPossibleGoldCurrency(monster,name);
                        double denom=CMLib.english().numPossibleGoldDenomination(monster,currency,name);
                        Coins C=CMLib.beanCounter().makeCurrency(currency,denom,coins);
                        Is.addElement(C);
                    }
                    else
                    {
                        Item I=CMClass.getItem(name);
                        if(I!=null)
                            Is.addElement(I);
                        else
                            findSomethingCalledThis(name,monster,lastKnownLocation,Is,false);
                    }
                    for(int i=0;i<Is.size();i++)
                    {
                        if(Is.elementAt(i) instanceof Item)
                        {
                            Item I=(Item)Is.elementAt(i);
                            if((I!=null)&&(!(I instanceof ArchonOnly)))
                            {
                                I=(Item)I.copyOf();
                                I.recoverEnvStats();
                                lastKnownLocation.addItemRefuse(I,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ));
                                I.setContainer(container);
                                if(I instanceof Coins)
                                    ((Coins)I).putCoinsBack();
                                if(I instanceof RawMaterial)
                                    ((RawMaterial)I).rebundle();
                                lastLoaded=I;
                            }
                        }
                    }
                    lastKnownLocation.recoverRoomStats();
                }
                break;
            }
            case 42: // mphide
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(newTarget!=null)
                {
                    newTarget.baseEnvStats().setDisposition(newTarget.baseEnvStats().disposition()|EnvStats.IS_NOT_SEEN);
                    newTarget.recoverEnvStats();
                    if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                }
                break;
            }
            case 58: // mpreset
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                String arg=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                if(arg.equalsIgnoreCase("area"))
                {
                    if(lastKnownLocation!=null)
                        CMLib.map().resetArea(lastKnownLocation.getArea());
                }
                else
                if(arg.equalsIgnoreCase("room"))
                {
                    if(lastKnownLocation!=null)
                        CMLib.map().resetRoom(lastKnownLocation, true);
                }
                else
                {
                    Room R=CMLib.map().getRoom(arg);
                    if(R!=null)
                        CMLib.map().resetRoom(R, true);
                    else
                    {
                        Area A=CMLib.map().findArea(arg);
                        if(A!=null)
                            CMLib.map().resetArea(A);
                        else
                            logError(scripted,"MPRESET","Syntax","Unknown location: "+arg+" for "+scripted.Name());
                    }
                }
                break;
            }
            case 71: // mprejuv
            {
                if(tt==null)
                {
                    String rest=CMParms.getPastBitClean(s,1);
                    if(rest.equals("item")||rest.equals("items"))
                        tt=parseBits(script,si,"Ccr");
                    else
                    if(rest.equals("mob")||rest.equals("mobs"))
                        tt=parseBits(script,si,"Ccr");
                    else
                        tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                String next=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                String rest=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                int tickID=0;
                if(rest.equalsIgnoreCase("item")||rest.equalsIgnoreCase("items"))
                    tickID=Tickable.TICKID_ROOM_ITEM_REJUV;
                else
                if(rest.equalsIgnoreCase("mob")||rest.equalsIgnoreCase("mobs"))
                    tickID=Tickable.TICKID_MOB;
                if(next.equalsIgnoreCase("area"))
                {
                    if(lastKnownLocation!=null)
                        for(Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
                            CMLib.threads().rejuv((Room)e.nextElement(),tickID);
                }
                else
                if(next.equalsIgnoreCase("room"))
                {
                    if(lastKnownLocation!=null)
                        CMLib.threads().rejuv(lastKnownLocation,tickID);
                }
                else
                {
                    Room R=CMLib.map().getRoom(next);
                    if(R!=null)
                        CMLib.threads().rejuv(R,tickID);
                    else
                    {
                        Area A=CMLib.map().findArea(next);
                        if(A!=null)
                            for(Enumeration e=A.getProperMap();e.hasMoreElements();)
                                CMLib.threads().rejuv((Room)e.nextElement(),tickID);
                        else
                            logError(scripted,"MPREJUV","Syntax","Unknown location: "+next+" for "+scripted.Name());
                    }
                }
                break;
            }
            case 56: // mpstop
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                Vector V=new Vector();
                String who=tt[1];
                if(who.equalsIgnoreCase("all"))
                {
                    for(int i=0;i<lastKnownLocation.numInhabitants();i++)
                        V.addElement(lastKnownLocation.fetchInhabitant(i));
                }
                else
                {
                    Environmental newTarget=getArgumentItem(who,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    if(newTarget instanceof MOB)
                        V.addElement(newTarget);
                }
                for(int v=0;v<V.size();v++)
                {
                    Environmental newTarget=(Environmental)V.elementAt(v);
                    if(newTarget instanceof MOB)
                    {
                        MOB mob=(MOB)newTarget;
                        Ability A=null;
                        for(int a=mob.numEffects()-1;a>=0;a--)
                        {
                            A=mob.fetchEffect(a);
                            if((A!=null)
                            &&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
                            &&(A.canBeUninvoked())
                            &&(!A.isAutoInvoked()))
                                A.unInvoke();
                        }
                        mob.makePeace();
                        if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                    }
                }
                break;
            }
            case 43: // mpunhide
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget!=null)&&(CMath.bset(newTarget.baseEnvStats().disposition(),EnvStats.IS_NOT_SEEN)))
                {
                    newTarget.baseEnvStats().setDisposition(newTarget.baseEnvStats().disposition()-EnvStats.IS_NOT_SEEN);
                    newTarget.recoverEnvStats();
                    if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                }
                break;
            }
            case 44: // mpopen
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget instanceof Exit)&&(((Exit)newTarget).hasADoor()))
                {
                    Exit E=(Exit)newTarget;
                    E.setDoorsNLocks(E.hasADoor(),true,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
                    if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                }
                else
                if((newTarget instanceof Container)&&(((Container)newTarget).hasALid()))
                {
                    Container E=(Container)newTarget;
                    E.setLidsNLocks(E.hasALid(),true,E.hasALock(),false);
                    if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                }
                break;
            }
            case 45: // mpclose
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget instanceof Exit)&&(((Exit)newTarget).hasADoor())&&(((Exit)newTarget).isOpen()))
                {
                    Exit E=(Exit)newTarget;
                    E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
                    if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                }
                else
                if((newTarget instanceof Container)&&(((Container)newTarget).hasALid())&&(((Container)newTarget).isOpen()))
                {
                    Container E=(Container)newTarget;
                    E.setLidsNLocks(E.hasALid(),false,E.hasALock(),false);
                    if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                }
                break;
            }
            case 46: // mplock
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget instanceof Exit)&&(((Exit)newTarget).hasALock()))
                {
                    Exit E=(Exit)newTarget;
                    E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),true,E.defaultsLocked());
                    if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                }
                else
                if((newTarget instanceof Container)&&(((Container)newTarget).hasALock()))
                {
                    Container E=(Container)newTarget;
                    E.setLidsNLocks(E.hasALid(),false,E.hasALock(),true);
                    if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                }
                break;
            }
            case 47: // mpunlock
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget instanceof Exit)&&(((Exit)newTarget).isLocked()))
                {
                    Exit E=(Exit)newTarget;
                    E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
                    if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                }
                else
                if((newTarget instanceof Container)&&(((Container)newTarget).isLocked()))
                {
                    Container E=(Container)newTarget;
                    E.setLidsNLocks(E.hasALid(),false,E.hasALock(),false);
                    if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                }
                break;
            }
            case 48: // return
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                tickStatus=Tickable.STATUS_END;
                return varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
            case 7: // mpechoat
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccp");
                	if(tt==null) return null;
                }
                String parm=tt[1];
                Environmental newTarget=getArgumentMOB(parm,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget!=null)&&(newTarget instanceof MOB)&&(lastKnownLocation!=null))
                {
                    if(newTarget==monster)
                        lastKnownLocation.showSource(monster,null,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
                    else
                        lastKnownLocation.show(monster,newTarget,null,CMMsg.MSG_OK_ACTION,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]),CMMsg.NO_EFFECT,null);
                }
                else
                if(parm.equalsIgnoreCase("world"))
                {
                    lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
                    for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
                    {
                        Room R=(Room)e.nextElement();
                        if(R.numInhabitants()>0)
                            R.showOthers(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
                    }
                }
                else
                if(parm.equalsIgnoreCase("area")&&(lastKnownLocation!=null))
                {
                    lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
                    for(Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
                    {
                        Room R=(Room)e.nextElement();
                        if(R.numInhabitants()>0)
                            R.showOthers(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
                    }
                }
                else
                if(CMLib.map().getRoom(parm)!=null)
                    CMLib.map().getRoom(parm).show(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
                else
                if(CMLib.map().findArea(parm)!=null)
                {
                    lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
                    for(Enumeration e=CMLib.map().findArea(parm).getMetroMap();e.hasMoreElements();)
                    {
                        Room R=(Room)e.nextElement();
                        if(R.numInhabitants()>0)
                            R.showOthers(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
                    }
                }
                break;
            }
            case 8: // mpechoaround
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccp");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget!=null)&&(newTarget instanceof MOB)&&(lastKnownLocation!=null))
                {
                    lastKnownLocation.showOthers((MOB)newTarget,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
                }
                break;
            }
            case 9: // mpcast
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                Environmental newTarget=getArgumentItem(tt[2],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                Ability A=null;
                if(cast!=null) A=CMClass.findAbility(cast);
                if((newTarget!=null)&&(A!=null))
                {
                    A.setProficiency(100);
                    A.invoke(monster,newTarget,false,0);
                }
                break;
            }
            case 30: // mpaffect
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cccp");
                	if(tt==null) return null;
                }
                String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                Environmental newTarget=getArgumentItem(tt[2],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String m2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                Ability A=null;
                if(cast!=null) A=CMClass.findAbility(cast);
                if((newTarget!=null)&&(A!=null))
                {
                    if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
                        Log.sysOut("Scripting",newTarget.Name()+" was MPAFFECTED by "+A.Name());
                    A.setMiscText(m2);
                    if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY)
                        newTarget.addNonUninvokableEffect(A);
                    else
                        A.invoke(monster,CMParms.parse(m2),newTarget,true,0);
                }
                break;
            }
            case 31: // mpbehave
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cccp");
                	if(tt==null) return null;
                }
                String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                Environmental newTarget=getArgumentItem(tt[2],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String m2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                Behavior B=null;
                Behavior B2=(cast==null)?null:CMClass.findBehavior(cast);
                if(B2!=null) cast=B2.ID();
                if((cast!=null)&&(newTarget!=null))
                {
                    B=newTarget.fetchBehavior(cast);
                    if(B==null) B=CMClass.getBehavior(cast);
                }
                if((newTarget!=null)&&(B!=null))
                {
                    if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
                        Log.sysOut("Scripting",newTarget.Name()+" was MPBEHAVED with "+B.name());
                    B.setParms(m2);
                    if(newTarget.fetchBehavior(B.ID())==null)
                    {
                        newTarget.addBehavior(B);
                        if((defaultQuestName()!=null)&&(defaultQuestName().length()>0))
                        B.registerDefaultQuest(defaultQuestName());
                    }
                }
                break;
            }
            case 72: // mpscript
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccp");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String m2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                boolean proceed=true;
                boolean savable=false;
                boolean execute=false;
                String scope=getVarScope();
                while(proceed)
                {
                    proceed=false;
                    if(m2.toUpperCase().startsWith("SAVABLE"))
                    {
                        savable=true;
                        m2=m2.substring(8).trim();
                        proceed=true;
                    }
                    else
                    if(m2.toUpperCase().startsWith("EXECUTE"))
                    {
                        execute=true;
                        m2=m2.substring(8).trim();
                        proceed=true;
                    }
                    else
                    if(m2.toUpperCase().startsWith("GLOBAL"))
                    {
                        scope="";
                        proceed=true;
                        m2=m2.substring(6).trim();
                    }
                    else
                    if(m2.toUpperCase().startsWith("INDIVIDUAL"))
                    {
                        scope="*";
                        proceed=true;
                        m2=m2.substring(10).trim();
                    }
                }
                if((newTarget!=null)&&(m2.length()>0))
                {
                    if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
                        Log.sysOut("Scripting",newTarget.Name()+" was MPSCRIPTED: "+defaultQuestName);
                    ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
                    S.setSavable(savable);
                    S.setVarScope(scope);
                    S.setScript(m2);
                    if((defaultQuestName()!=null)&&(defaultQuestName().length()>0))
                        S.registerDefaultQuest(defaultQuestName());
                    newTarget.addScript(S);
                    if(execute)
                    {
                        S.tick(newTarget,Tickable.TICKID_MOB);
                        for(int i=0;i<5;i++)
                            S.dequeResponses();
                        newTarget.delScript(S);
                    }
                }
                break;
            }
            case 32: // mpunbehave
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                Environmental newTarget=getArgumentItem(tt[2],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget!=null)&&(cast!=null))
                {
                    Behavior B=CMClass.findBehavior(cast);
                    if(B!=null) cast=B.ID();
                    B=newTarget.fetchBehavior(cast);
                    if(B!=null) newTarget.delBehavior(B);
                    if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster())&&(B!=null))
                        Log.sysOut("Scripting",newTarget.Name()+" was MPUNBEHAVED with "+B.name());
                }
                break;
            }
            case 33: // mptattoo
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String tattooName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                if((newTarget!=null)&&(tattooName.length()>0)&&(newTarget instanceof MOB))
                {
                    MOB themob=(MOB)newTarget;
                    boolean tattooMinus=tattooName.startsWith("-");
                    if(tattooMinus) tattooName=tattooName.substring(1);
                    String tattoo=tattooName;
                    if((tattoo.length()>0)
                    &&(Character.isDigit(tattoo.charAt(0)))
                    &&(tattoo.indexOf(" ")>0)
                    &&(CMath.isNumber(tattoo.substring(0,tattoo.indexOf(" ")).trim())))
                        tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
                    if(themob.fetchTattoo(tattoo)!=null)
                    {
                        if(tattooMinus)
                            themob.delTattoo(tattooName);
                    }
                    else
                    if(!tattooMinus)
                        themob.addTattoo(tattooName);
                }
                break;
            }
            case 55: // mpnotrigger
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                String trigger=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                String time=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                int triggerCode=-1;
                for(int i=0;i<progs.length;i++)
                    if(trigger.equalsIgnoreCase(progs[i]))
                        triggerCode=i;
                if(triggerCode<0)
                    logError(scripted,"MPNOTRIGGER","RunTime",trigger+" is not a valid trigger name.");
                else
                if(!CMath.isInteger(time.trim()))
                    logError(scripted,"MPNOTRIGGER","RunTime",time+" is not a valid milisecond time.");
                else
                {
                    noTrigger.remove(Integer.valueOf(triggerCode));
                    noTrigger.put(Integer.valueOf(triggerCode),Long.valueOf(System.currentTimeMillis()+CMath.s_long(time.trim())));
                }
                break;
            }
            case 54: // mpfaction
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
                String faction=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                String range=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]).trim();
                Faction F=CMLib.factions().getFaction(faction);
                if((newTarget!=null)&&(F!=null)&&(newTarget instanceof MOB))
                {
                    MOB themob=(MOB)newTarget;
                    if((range.startsWith("--"))&&(CMath.isInteger(range.substring(2).trim())))
                    {
                        int amt=CMath.s_int(range.substring(2).trim());
                        themob.tell("You lose "+amt+" faction with "+F.name()+".");
                        range=""+(themob.fetchFaction(faction)-amt);
                    }
                    else
                    if((range.startsWith("+"))&&(CMath.isInteger(range.substring(1).trim())))
                    {
                        int amt=CMath.s_int(range.substring(1).trim());
                        themob.tell("You gain "+amt+" faction with "+F.name()+".");
                        range=""+(themob.fetchFaction(faction)+amt);
                    }
                    else
                    if(CMath.isInteger(range))
                        themob.tell("Your faction with "+F.name()+" is now "+CMath.s_int(range.trim())+".");
                    if(CMath.isInteger(range))
                        themob.addFaction(F.factionID(),CMath.s_int(range.trim()));
                    else
                    {
                        Faction.FactionRange FR=null;
                        Enumeration e=CMLib.factions().getRanges(CMLib.factions().AlignID());
                        if(e!=null)
                        for(;e.hasMoreElements();)
                        {
                            Faction.FactionRange FR2=(Faction.FactionRange)e.nextElement();
                            if(FR2.name().equalsIgnoreCase(range))
                            { FR=FR2; break;}
                        }
                        if(FR==null)
                            logError(scripted,"MPFACTION","RunTime",range+" is not a valid range for "+F.name()+".");
                        else
                        {
                            themob.tell("Your faction with "+F.name()+" is now "+FR.name()+".");
                            themob.addFaction(F.factionID(),FR.low()+((FR.high()-FR.low())/2));
                        }
                    }
                }
                break;
            }
            case 49: // mptitle
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String tattooName=tt[2];
                if((newTarget!=null)&&(tattooName.length()>0)&&(newTarget instanceof MOB))
                {
                    MOB themob=(MOB)newTarget;
                    boolean tattooMinus=tattooName.startsWith("-");
                    if(tattooMinus) tattooName=tattooName.substring(1);
                    String tattoo=tattooName;
                    if((tattoo.length()>0)
                    &&(Character.isDigit(tattoo.charAt(0)))
                    &&(tattoo.indexOf(" ")>0)
                    &&(CMath.isNumber(tattoo.substring(0,tattoo.indexOf(" ")).trim())))
                        tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
                    if(themob.playerStats()!=null)
                    {
                        if(themob.playerStats().getTitles().contains(tattoo))
                        {
                            if(tattooMinus)
                                themob.playerStats().getTitles().removeElement(tattooName);
                        }
                        else
                        if(!tattooMinus)
                            themob.playerStats().getTitles().insertElementAt(tattooName,0);
                    }
                }
                break;
            }
            case 10: // mpkill
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget!=null)&&(newTarget instanceof MOB)&&(monster!=null))
                    monster.setVictim((MOB)newTarget);
                break;
            }
            case 51: // mpsetclandata
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String clanID=null;
                if((newTarget!=null)&&(newTarget instanceof MOB))
                    clanID=((MOB)newTarget).getClanID();
                else
                    clanID=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                String clanvar=tt[2];
                String clanval=tt[3];
                Clan C=CMLib.clans().getClan(clanID);
                if(C!=null)
                {
                    if(!C.isStat(clanvar))
                        logError(scripted,"MPSETCLANDATA","RunTime",clanvar+" is not a valid clan variable.");
                    else
                    {
                        C.setStat(clanvar,clanval.trim());
                        if(C.getStat(clanvar).equalsIgnoreCase(clanval))
                            C.update();
                    }
                }
                break;
            }
            case 52: // mpplayerclass
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget!=null)&&(newTarget instanceof MOB))
                {
                    Vector V=CMParms.parse(tt[2]);
                    for(int i=0;i<V.size();i++)
                    {
                        if(CMath.isInteger(((String)V.elementAt(i)).trim()))
                            ((MOB)newTarget).baseCharStats().setClassLevel(((MOB)newTarget).baseCharStats().getCurrentClass(),CMath.s_int(((String)V.elementAt(i)).trim()));
                        else
                        {
                            CharClass C=CMClass.findCharClass((String)V.elementAt(i));
                            if(C!=null)
                                ((MOB)newTarget).baseCharStats().setCurrentClass(C);
                        }
                    }
                    ((MOB)newTarget).recoverCharStats();
                }
                break;
            }
            case 12: // mppurge
            {
                if(lastKnownLocation!=null)
                {
                    int flag=0;
                    if(tt==null)
                    {
                        String s2=CMParms.getCleanBit(s,1).toLowerCase();
                        if(s2.equals("room"))
                            tt=parseBits(script,si,"Ccr");
                        else
                        if(s2.equals("my"))
                            tt=parseBits(script,si,"Ccr");
                        else
                            tt=parseBits(script,si,"Cr");
                    	if(tt==null) return null;
                    }
                    String s2=tt[1];
                    if(s2.equalsIgnoreCase("room"))
                    {
                        flag=1;
                        s2=tt[2];
                    }
                    else
                    if(s2.equalsIgnoreCase("my"))
                    {
                        flag=2;
                        s2=tt[2];
                    }
                    Environmental E=null;
                    if(s2.equalsIgnoreCase("self")||s2.equalsIgnoreCase("me"))
                        E=scripted;
                    else
                    if(flag==1)
                    {
                        s2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,s2);
                        E=lastKnownLocation.fetchFromRoomFavorItems(null,s2,Wearable.FILTER_ANY);
                    }
                    else
                    if(flag==2)
                    {
                        s2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,s2);
                        if(monster!=null)
                            E=monster.fetchInventory(s2);
                    }
                    else
                        E=getArgumentItem(s2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    if(E!=null)
                    {
                        if(E instanceof MOB)
                        {
                            if(!((MOB)E).isMonster())
                            {
                                if(((MOB)E).getStartRoom()!=null)
                                    ((MOB)E).getStartRoom().bringMobHere((MOB)E,false);
                                ((MOB)E).session().kill(false,false,false);
                            }
                            else
                            if(((MOB)E).getStartRoom()!=null)
                                ((MOB)E).killMeDead(false);
                            else
                                ((MOB)E).destroy();
                        }
                        else
                        if(E instanceof Item)
                        {
                            Environmental oE=((Item)E).owner();
                            ((Item)E).destroy();
                            if(oE!=null) oE.recoverEnvStats();
                        }
                    }
                    lastKnownLocation.recoverRoomStats();
                }
                break;
            }
            case 14: // mpgoto
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                String roomID=tt[1].trim();
                if((roomID.length()>0)&&(lastKnownLocation!=null))
                {
                    Room goHere=null;
                    if(roomID.startsWith("$"))
                        goHere=CMLib.map().roomLocation(this.getArgumentItem(roomID,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
                    if(goHere==null)
                        goHere=getRoom(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,roomID),lastKnownLocation);
                    if(goHere!=null)
                    {
                        if(scripted instanceof MOB)
                            goHere.bringMobHere((MOB)scripted,true);
                        else
                        if(scripted instanceof Item)
                            goHere.bringItemHere((Item)scripted,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP),true);
                        else
                        {
                            goHere.bringMobHere(monster,true);
                            if(!(scripted instanceof MOB))
                                goHere.delInhabitant(monster);
                        }
                        if(CMLib.map().roomLocation(scripted)==goHere)
                            lastKnownLocation=goHere;
                    }
                }
                break;
            }
            case 15: // mpat
            if(lastKnownLocation!=null)
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccp");
                	if(tt==null) return null;
                }
                Room lastPlace=lastKnownLocation;
                String roomName=tt[1];
                if(roomName.length()>0)
                {
                    String doWhat=tt[2].trim();
                    Room goHere=null;
                    if(roomName.startsWith("$"))
                        goHere=CMLib.map().roomLocation(this.getArgumentItem(roomName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
                    if(goHere==null)
                        goHere=getRoom(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,roomName),lastKnownLocation);
                    if(goHere!=null)
                    {
                        goHere.bringMobHere(monster,true);
                        DVector DV=new DVector(2);
                        DV.addElement("",null);
                        DV.addElement(doWhat,null);
                        lastKnownLocation=goHere;
                        execute(scripted,source,target,monster,primaryItem,secondaryItem,DV,msg,tmp);
                        lastKnownLocation=lastPlace;
                        lastPlace.bringMobHere(monster,true);
                        if(!(scripted instanceof MOB))
                        {
                            goHere.delInhabitant(monster);
                            lastPlace.delInhabitant(monster);
                        }
                    }
                }
            }
            break;
            case 17: // mptransfer
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                String mobName=tt[1];
                String roomName=tt[2].trim();
                Room newRoom=null;
                if(roomName.startsWith("$"))
                    newRoom=CMLib.map().roomLocation(this.getArgumentItem(roomName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
                if((roomName.length()==0)&&(lastKnownLocation!=null))
                    roomName=lastKnownLocation.roomID();
                if(roomName.length()>0)
                {
                    if(newRoom==null)
                        newRoom=getRoom(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,roomName),lastKnownLocation);
                    if(newRoom!=null)
                    {
                        Vector V=new Vector();
                        if(mobName.startsWith("$"))
                        {
                            Environmental E=getArgumentItem(mobName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                            if(E!=null) V.addElement(E);
                        }
                        if(V.size()==0)
                        {
                            mobName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,mobName);
                            if(mobName.equalsIgnoreCase("all"))
                            {
                                if(lastKnownLocation!=null)
                                {
                                    for(int x=0;x<lastKnownLocation.numInhabitants();x++)
                                    {
                                        MOB m=lastKnownLocation.fetchInhabitant(x);
                                        if((m!=null)&&(m!=monster)&&(!V.contains(m)))
                                            V.addElement(m);
                                    }
                                }
                            }
                            else
                            {
                                MOB findOne=null;
                                Area A=null;
                                if(lastKnownLocation!=null)
                                {
                                    findOne=lastKnownLocation.fetchInhabitant(mobName);
                                    A=lastKnownLocation.getArea();
                                    if((findOne!=null)&&(findOne!=monster))
                                        V.addElement(findOne);
                                }
                                if(findOne==null)
                                {
                                    findOne=CMLib.players().getPlayer(mobName);
                                    if((findOne!=null)&&(!CMLib.flags().isInTheGame(findOne,true)))
                                        findOne=null;
                                    if((findOne!=null)&&(findOne!=monster))
                                        V.addElement(findOne);
                                }
                                if((findOne==null)&&(A!=null))
                                    for(Enumeration r=A.getProperMap();r.hasMoreElements();)
                                    {
                                        Room R=(Room)r.nextElement();
                                        findOne=R.fetchInhabitant(mobName);
                                        if((findOne!=null)&&(findOne!=monster))
                                            V.addElement(findOne);
                                    }
                            }
                        }
                        for(int v=0;v<V.size();v++)
                        {
                            if(V.elementAt(v) instanceof MOB)
                            {
                                MOB mob=(MOB)V.elementAt(v);
                                HashSet H=mob.getGroupMembers(new HashSet());
                                for(Iterator e=H.iterator();e.hasNext();)
                                {
                                    MOB M=(MOB)e.next();
                                    if((!V.contains(M))&&(M.location()==mob.location()))
                                       V.addElement(M);
                                }
                            }
                        }
                        for(int v=0;v<V.size();v++)
                        {
                            if(V.elementAt(v) instanceof MOB)
                            {
                                MOB follower=(MOB)V.elementAt(v);
                                Room thisRoom=follower.location();
                                // scripting guide calls for NO text -- empty is probably req tho
                                CMMsg enterMsg=CMClass.getMsg(follower,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER," "+CMProps.msp("appear.wav",10));
                                CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,null,CMMsg.MSG_LEAVE," ");
                                if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
                                {
                                    if(follower.isInCombat())
                                    {
                                        CMLib.commands().postFlee(follower,("NOWHERE"));
                                        follower.makePeace();
                                    }
                                    thisRoom.send(follower,leaveMsg);
                                    newRoom.bringMobHere(follower,false);
                                    newRoom.send(follower,enterMsg);
                                    follower.tell("\n\r\n\r");
                                    CMLib.commands().postLook(follower,true);
                                }
                            }
                            else
                            if((V.elementAt(v) instanceof Item)
                            &&(newRoom!=CMLib.map().roomLocation((Environmental)V.elementAt(v))))
                                newRoom.bringItemHere((Item)V.elementAt(v),CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP),true);
                            if(V.elementAt(v)==scripted)
                                lastKnownLocation=newRoom;
                        }
                    }
                }
                break;
            }
            case 25: // mpbeacon
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                String roomName=tt[1];
                Room newRoom=null;
                if((roomName.length()>0)&&(lastKnownLocation!=null))
                {
                    String beacon=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                    if(roomName.startsWith("$"))
                        newRoom=CMLib.map().roomLocation(this.getArgumentItem(roomName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
                    if(newRoom==null)
                        newRoom=getRoom(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,roomName),lastKnownLocation);
                    if((newRoom!=null)&&(lastKnownLocation!=null))
                    {
                        Vector V=new Vector();
                        if(beacon.equalsIgnoreCase("all"))
                        {
                            for(int x=0;x<lastKnownLocation.numInhabitants();x++)
                            {
                                MOB m=lastKnownLocation.fetchInhabitant(x);
                                if((m!=null)&&(m!=monster)&&(!m.isMonster())&&(!V.contains(m)))
                                    V.addElement(m);
                            }
                        }
                        else
                        {
                            MOB findOne=lastKnownLocation.fetchInhabitant(beacon);
                            if((findOne!=null)&&(findOne!=monster)&&(!findOne.isMonster()))
                                V.addElement(findOne);
                        }
                        for(int v=0;v<V.size();v++)
                        {
                            MOB follower=(MOB)V.elementAt(v);
                            if(!follower.isMonster())
                                follower.setStartRoom(newRoom);
                        }
                    }
                }
                break;
            }
            case 18: // mpforce
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccp");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
                String force=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]).trim();
                if(newTarget!=null)
                {
                    DVector vscript=new DVector(2);
                    vscript.addElement("FUNCTION_PROG MPFORCE_"+System.currentTimeMillis()+Math.random(),null);
                    vscript.addElement(force,null);
                    // this can not be permanently parsed because it is variable
                    execute(newTarget, source, target, getMakeMOB(newTarget), primaryItem, secondaryItem, vscript, msg, tmp);
                }
                break;
            }
            case 20: // mpsetvar
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cccr");
                	if(tt==null) return null;
                }
                String which=tt[1];
                Environmental E=getArgumentItem(which,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                if(!which.equals("*"))
                {
                    if(E==null)
                        which=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,which);
                    else
                    if(E instanceof Room)
                        which=CMLib.map().getExtendedRoomID((Room)E);
                    else
                        which=E.Name();
                }
                if((which.length()>0)&&(arg2.length()>0))
                    setVar(which,arg2,arg3);
                break;
            }
            case 36: // mpsavevar
            {
                if(tt==null){
                	tt=parseBits(script,si,"CcR");
                	if(tt==null) return null;
                }
                String which=tt[1];
                String arg2=tt[2];
                Environmental E=getArgumentItem(which,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                which=getVarHost(E,which,source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp);
                if((which.length()>0)&&(arg2.length()>0))
                {
                    DVector V=getScriptVarSet(which,arg2);
                    for(int v=0;v<V.size();v++)
                    {
                        which=(String)V.elementAt(0,1);
                        arg2=((String)V.elementAt(0,2)).toUpperCase();
                        Hashtable H=(Hashtable)resources._getResource("SCRIPTVAR-"+which);
                        String val="";
                        if(H!=null)
                        {
                            val=(String)H.get(arg2);
                            if(val==null) val="";
                        }
                        if(val.length()>0)
                            CMLib.database().DBReCreateData(which,"SCRIPTABLEVARS",which.toUpperCase()+"_SCRIPTABLEVARS_"+arg2,val);
                        else
                            CMLib.database().DBDeleteData(which,"SCRIPTABLEVARS",which.toUpperCase()+"_SCRIPTABLEVARS_"+arg2);
                    }
                }
                break;
            }
            case 39: // mploadvar
            {
                if(tt==null){
                	tt=parseBits(script,si,"CcR");
                	if(tt==null) return null;
                }
                String which=tt[1];
                String arg2=tt[2];
                Environmental E=getArgumentItem(which,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(arg2.length()>0)
                {
                    Vector V=null;
                    which=getVarHost(E,which,source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp);
                    if(arg2.equals("*"))
                        V=CMLib.database().DBReadData(which,"SCRIPTABLEVARS");
                    else
                        V=CMLib.database().DBReadData(which,"SCRIPTABLEVARS",which.toUpperCase()+"_SCRIPTABLEVARS_"+arg2);
                    if((V!=null)&&(V.size()>0))
                    for(int v=0;v<V.size();v++)
                    {
                    	DatabaseEngine.PlayerData VAR=(DatabaseEngine.PlayerData)V.elementAt(v);
                        String varName=VAR.key;
                        if(varName.startsWith(which.toUpperCase()+"_SCRIPTABLEVARS_"))
                            varName=varName.substring((which+"_SCRIPTABLEVARS_").length());
                        setVar(which,varName,VAR.xml);
                    }
                }
                break;
            }
            case 40: // MPM2I2M
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cccr");
                	if(tt==null) return null;
                }
                String arg1=tt[1];
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E instanceof MOB)
                {
                    String arg2=tt[2];
                    String arg3=tt[3];
                    CagedAnimal caged=(CagedAnimal)CMClass.getItem("GenCaged");
                    if(caged!=null)
                    {
                        ((Item)caged).baseEnvStats().setAbility(1);
                        ((Item)caged).recoverEnvStats();
                    }
                    if((caged!=null)&&caged.cageMe((MOB)E)&&(lastKnownLocation!=null))
                    {
                        if(arg2.length()>0) ((Item)caged).setName(arg2);
                        if(arg3.length()>0) ((Item)caged).setDisplayText(arg3);
                        lastKnownLocation.addItemRefuse((Item)caged,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
                        ((MOB)E).killMeDead(false);
                    }
                }
                else
                if(E instanceof CagedAnimal)
                {
                    MOB M=((CagedAnimal)E).unCageMe();
                    if((M!=null)&&(lastKnownLocation!=null))
                    {
                        M.bringToLife(lastKnownLocation,true);
                        ((Item)E).destroy();
                    }
                }
                else
                    logError(scripted,"MPM2I2M","RunTime",arg1+" is not a mob or a caged item.");
                break;
            }
            case 28: // mpdamage
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[4]);
                if((newTarget!=null)&&(arg2.length()>0))
                {
                    if(newTarget instanceof MOB)
                    {
                        MOB deadM=(MOB)newTarget;
                        MOB killerM=(MOB)newTarget;
                        if(arg4.equalsIgnoreCase("MEKILL")||arg4.equalsIgnoreCase("ME"))
                            killerM=monster;
                        int min=CMath.s_int(arg2.trim());
                        int max=CMath.s_int(arg3.trim());
                        if(max<min) max=min;
                        if(min>0)
                        {
                            int dmg=(max==min)?min:CMLib.dice().roll(1,max-min,min);
                            if((dmg>=deadM.curState().getHitPoints())
                            &&(!arg4.equalsIgnoreCase("KILL"))
                            &&(!arg4.equalsIgnoreCase("MEKILL")))
                                dmg=deadM.curState().getHitPoints()-1;
                            if(dmg>0)
                                CMLib.combat().postDamage(killerM,deadM,null,dmg,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,-1,null);
                        }
                    }
                    else
                    if(newTarget instanceof Item)
                    {
                        Item E=(Item)newTarget;
                        int min=CMath.s_int(arg2.trim());
                        int max=CMath.s_int(arg3.trim());
                        if(max<min) max=min;
                        if(min>0)
                        {
                            int dmg=(max==min)?min:CMLib.dice().roll(1,max-min,min);
                            boolean destroy=false;
                            if(E.subjectToWearAndTear())
                            {
                                if((dmg>=E.usesRemaining())&&(!arg4.equalsIgnoreCase("kill")))
                                    dmg=E.usesRemaining()-1;
                                if(dmg>0)
                                    E.setUsesRemaining(E.usesRemaining()-dmg);
                                if(E.usesRemaining()<=0) destroy=true;
                            }
                            else
                            if(arg4.equalsIgnoreCase("kill"))
                                destroy=true;
                            if(destroy)
                            {
                                if(lastKnownLocation!=null)
                                    lastKnownLocation.showHappens(CMMsg.MSG_OK_VISUAL,E.name()+" is destroyed!");
                                E.destroy();
                            }
                        }
                    }
                }
                break;
            }
            case 78: // mpheal
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                if((newTarget!=null)&&(arg2.length()>0))
                {
                    if(newTarget instanceof MOB)
                    {
                        MOB healedM=(MOB)newTarget;
                        MOB healerM=(MOB)newTarget;
                        int min=CMath.s_int(arg2.trim());
                        int max=CMath.s_int(arg3.trim());
                        if(max<min) max=min;
                        if(min>0)
                        {
                            int amt=(max==min)?min:CMLib.dice().roll(1,max-min,min);
                            if(amt>0)
                                CMLib.combat().postHealing(healerM,healedM,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,amt,null);
                        }
                    }
                    else
                    if(newTarget instanceof Item)
                    {
                        Item E=(Item)newTarget;
                        int min=CMath.s_int(arg2.trim());
                        int max=CMath.s_int(arg3.trim());
                        if(max<min) max=min;
                        if(min>0)
                        {
                            int amt=(max==min)?min:CMLib.dice().roll(1,max-min,min);
                            if(E.subjectToWearAndTear())
                            {
                                E.setUsesRemaining(E.usesRemaining()+amt);
                                if(E.usesRemaining()>100) E.setUsesRemaining(100);
                            }
                        }
                    }
                }
                break;
            }
            case 29: // mptrackto
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                Ability A=CMClass.getAbility("Skill_Track");
                if(A!=null)
                {
                    altStatusTickable=A;
                    A.invoke(monster,CMParms.parse(arg1),null,true,0);
                    altStatusTickable=null;
                }
                break;
            }
            case 53: // mpwalkto
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                Ability A=CMClass.getAbility("Skill_Track");
                if(A!=null)
                {
                    altStatusTickable=A;
                    A.invoke(monster,CMParms.parse(arg1+" LANDONLY"),null,true,0);
                    altStatusTickable=null;
                }
                break;
            }
            case 21: //MPENDQUEST
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                String q=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1].trim());
                Quest Q=getQuest(q);
                if(Q!=null)
                    Q.stopQuest();
                else
                if((tt[1].length()>0)&&(defaultQuestName!=null)&&(defaultQuestName.length()>0))
                {
                    Environmental newTarget=getArgumentMOB(tt[1].trim(),source,monster,target,primaryItem,secondaryItem,msg,tmp);
                    if(newTarget==null)
                        logError(scripted,"MPENDQUEST","Unknown","Quest or MOB: "+s);
                    else
                    {
                        for(int i=newTarget.numScripts()-1;i>=0;i--)
                        {
                            ScriptingEngine S=newTarget.fetchScript(i);
                            if((S!=null)
                            &&(S.defaultQuestName()!=null)
                            &&(S.defaultQuestName().equalsIgnoreCase(defaultQuestName)))
                                newTarget.delScript(S);
                        }
                    }
                }
                else
                    logError(scripted,"MPENDQUEST","Unknown","Quest: "+s);
                break;
            }
            case 69: // MPSTEPQUEST
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                String qName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1].trim());
                Quest Q=getQuest(qName);
                if(Q!=null) Q.stepQuest();
                else
                    logError(scripted,"MPSTEPQUEST","Unknown","Quest: "+s);
                break;
            }
            case 23: //MPSTARTQUEST
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cr");
                	if(tt==null) return null;
                }
                String qName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1].trim());
                Quest Q=getQuest(qName);
                if(Q!=null) Q.startQuest();
                else
                    logError(scripted,"MPSTARTQUEST","Unknown","Quest: "+s);
                break;
            }
            case 64: //MPLOADQUESTOBJ
            {
                if(tt==null){
                	tt=parseBits(script,si,"Cccr");
                	if(tt==null) return null;
                }
                String questName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1].trim());
                Quest Q=getQuest(questName);
                if(Q==null)
                {
                    logError(scripted,"MPLOADQUESTOBJ","Unknown","Quest: "+questName);
                    break;
                }
                Object O=Q.getDesignatedObject(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
                if(O==null)
                {
                    logError(scripted,"MPLOADQUESTOBJ","Unknown","Unknown var "+tt[2]+" for Quest: "+questName);
                    break;
                }
                String varArg=tt[3];
                if((varArg.length()!=2)||(!varArg.startsWith("$")))
                {
                    logError(scripted,"MPLOADQUESTOBJ","Syntax","Invalid argument var: "+varArg+" for "+scripted.Name());
                    break;
                }

                char c=varArg.charAt(1);
                if(Character.isDigit(c))
                    tmp[CMath.s_int(Character.toString(c))]=O;
                else
                switch(c)
                {
                case 'N':
                case 'n': if(O instanceof MOB) source=(MOB)O; break;
                case 'I':
                case 'i': if(O instanceof Environmental) scripted=(Environmental)O;
                          if(O instanceof MOB) monster=(MOB)O;
                          break;
                case 'B':
                case 'b': if(O instanceof Environmental)
                                lastLoaded=(Environmental)O;
                          break;
                case 'T':
                case 't': if(O instanceof Environmental) target=(Environmental)O; break;
                case 'O':
                case 'o': if(O instanceof Item) primaryItem=(Item)O; break;
                case 'P':
                case 'p': if(O instanceof Item) secondaryItem=(Item)O; break;
                case 'd':
                case 'D': if(O instanceof Room) lastKnownLocation=(Room)O; break;
                default:
                    logError(scripted,"MPLOADQUESTOBJ","Syntax","Invalid argument var: "+varArg+" for "+scripted.Name());
                    break;
                }
                break;
            }
            case 22: //MPQUESTWIN
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                String whoName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                MOB M=null;
                if(lastKnownLocation!=null)
                    M=lastKnownLocation.fetchInhabitant(whoName);
                if(M==null) M=CMLib.players().getPlayer(whoName);
                if(M!=null) whoName=M.Name();
                if(whoName.length()>0)
                {
                    Quest Q=getQuest(tt[2]);
                    if(Q!=null)
                        Q.declareWinner(whoName);
                    else
                        logError(scripted,"MPQUESTWIN","Unknown","Quest: "+s);
                }
                break;
            }
            case 24: // MPCALLFUNC
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                String named=tt[1];
                String parms=tt[2].trim();
                boolean found=false;
                Vector scripts=getScripts();
                for(int v=0;v<scripts.size();v++)
                {
                    DVector script2=(DVector)scripts.elementAt(v);
                    if(script2.size()<1) continue;
                    String trigger=((String)script2.elementAt(0,1)).toUpperCase().trim();
                    String[] ttrigger=(String[])script2.elementAt(0,2);
                    if(getTriggerCode(trigger,ttrigger)==17)
                    {
                        String fnamed=CMParms.getCleanBit(trigger,1);
                        if(fnamed.equalsIgnoreCase(named))
                        {
                            found=true;
                            execute(scripted,
                                    source,
                                    target,
                                    monster,
                                    primaryItem,
                                    secondaryItem,
                                    script2,
                                    varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,parms),
                                    tmp);
                            break;
                        }
                    }
                }
                if(!found)
                    logError(scripted,"MPCALLFUNC","Unknown","Function: "+named);
                break;
            }
            case 27: // MPWHILE
            {
                if(tt==null)
                {
                    Vector V=new Vector();
                    V.addElement("MPWHILE");
                    String conditionStr=(s.substring(7).trim());
                    if(!conditionStr.startsWith("("))
                    {
                        logError(scripted,"MPWHILE","Syntax"," NO Starting (: "+s);
                        break;
                    }
                    conditionStr=conditionStr.substring(1).trim();
                    int x=-1;
                    int depth=0;
                    for(int i=0;i<conditionStr.length();i++)
                        if(conditionStr.charAt(i)=='(')
                            depth++;
                        else
                        if((conditionStr.charAt(i)==')')&&((--depth)<0))
                        {
                            x=i;
                            break;
                        }
                    if(x<0)
                    {
                        logError(scripted,"MPWHILE","Syntax"," no closing ')': "+s);
                        break;
                    }
                    String DO=conditionStr.substring(x+1).trim();
                    conditionStr=conditionStr.substring(0,x);
                    try {
                        String[] EVAL=parseEval(conditionStr);
                        V.addElement("(");
                        Vector V2=CMParms.makeVector(EVAL);
                        V.addAll(V2);
                        V.addElement(")");
                        V.addElement(DO);
                        tt=CMParms.toStringArray(V);
                        script.setElementAt(si,2,tt);
                    } catch(Exception e) {
                        logError(scripted,"MPWHILE","Syntax",e.getMessage());
                        break;
                    }
                	if(tt==null) return null;
                }
                int evalEnd=2;
                int depth=0;
                while((evalEnd<tt.length)&&((!tt[evalEnd].equals(")"))||(depth>0)))
                {
                    if(tt[evalEnd].equals("("))
                        depth++;
                    else
                    if(tt[evalEnd].equals(")"))
                        depth--;
                    evalEnd++;
                }
                if(evalEnd==tt.length)
                {
                    logError(scripted,"MPWHILE","Syntax"," no closing ')': "+s);
                    break;
                }
                String[] EVAL=new String[evalEnd-2];
                for(int y=2;y<evalEnd;y++)
                    EVAL[y-2]=tt[y];
                String DO=tt[evalEnd+1];
                String[] DOT=null;
                int doLen=(tt.length-evalEnd)-1;
                if(doLen>1)
                {
                    DOT=new String[doLen];
                    for(int y=0;y<DOT.length;y++)
                    {
                        DOT[y]=tt[evalEnd+y+1];
                        if(y>0) DO+=" "+tt[evalEnd+y+1];
                    }
                }
                String[][] EVALO={EVAL};
                DVector vscript=new DVector(2);
                vscript.addElement("FUNCTION_PROG MPWHILE_"+Math.random(),null);
                vscript.addElement(DO,DOT);
                long time=System.currentTimeMillis();
                while((eval(scripted,source,target,monster,primaryItem,secondaryItem,msg,tmp,EVALO,0))&&((System.currentTimeMillis()-time)<4000))
                    execute(scripted,source,target,monster,primaryItem,secondaryItem,vscript,msg,tmp);
                if(vscript.elementAt(1,2)!=DOT)
                {
                    int oldDotLen=(DOT==null)?1:DOT.length;
                    String[] newDOT=(String[])vscript.elementAt(1,2);
                    String[] newTT=new String[tt.length-oldDotLen+newDOT.length];
                    int end=0;
                    for(end=0;end<tt.length-oldDotLen;end++)
                        newTT[end]=tt[end];
                    for(int y=0;y<newDOT.length;y++)
                        newTT[end+y]=newDOT[y];
                    tt=newTT;
                    script.setElementAt(si,2,tt);
                }
                if(EVALO[0]!=EVAL)
                {
                    Vector lazyV=new Vector();
                    lazyV.addElement("MPWHILE");
                    lazyV.addElement("(");
                    String[] newEVAL=EVALO[0];
                    for(int y=0;y<newEVAL.length;y++)
                        lazyV.addElement(newEVAL[y]);
                    for(int i=evalEnd;i<tt.length;i++)
                        lazyV.addElement(tt[i]);
                    tt=CMParms.toStringArray(lazyV);
                    script.setElementAt(si,2,tt);
                }
                if((System.currentTimeMillis()-time)>=4000)
                {
                    logError(scripted,"MPWHILE","RunTime","4 second limit exceeded: "+s);
                    break;
                }
                break;
            }
            case 26: // MPALARM
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccp");
                	if(tt==null) return null;
                }
                String time=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
                String parms=tt[2].trim();
                if(CMath.s_int(time.trim())<=0)
                {
                    logError(scripted,"MPALARM","Syntax","Bad time "+time);
                    break;
                }
                if(parms.length()==0)
                {
                    logError(scripted,"MPALARM","Syntax","No command!");
                    break;
                }
                DVector vscript=new DVector(2);
                vscript.addElement("FUNCTION_PROG ALARM_"+time+Math.random(),null);
                vscript.addElement(parms,null);
                prequeResponse(scripted,source,target,monster,primaryItem,secondaryItem,vscript,CMath.s_int(time.trim()),msg);
                break;
            }
            case 37: // mpenable
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccccp");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
                String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                String p2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
                String m2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[4]);
                Ability A=null;
                if(cast!=null)
                {
                    if(newTarget instanceof MOB) A=((MOB)newTarget).fetchAbility(cast);
                    if(A==null) A=CMClass.getAbility(cast);
                    if(A==null)
                    {
                        ExpertiseLibrary.ExpertiseDefinition D=CMLib.expertises().findDefinition(cast,false);
                        if(D==null)
                            logError(scripted,"MPENABLE","Syntax","Unknown skill/expertise: "+cast);
                        else
                        if((newTarget!=null)&&(newTarget instanceof MOB))
                            ((MOB)newTarget).addExpertise(D.ID);
                    }
                }
                if((newTarget!=null)
                &&(A!=null)
                &&(newTarget instanceof MOB))
                {
                    if(!((MOB)newTarget).isMonster())
                        Log.sysOut("Scripting",newTarget.Name()+" was MPENABLED with "+A.Name());
                    if(p2.trim().startsWith("++"))
                        p2=""+(CMath.s_int(p2.trim().substring(2))+A.proficiency());
                    else
                    if(p2.trim().startsWith("--"))
                        p2=""+(A.proficiency()-CMath.s_int(p2.trim().substring(2)));
                    A.setProficiency(CMath.s_int(p2.trim()));
                    A.setMiscText(m2);
                    if(((MOB)newTarget).fetchAbility(A.ID())==null)
                        ((MOB)newTarget).addAbility(A);
                }
                break;
            }
            case 38: // mpdisable
            {
                if(tt==null){
                	tt=parseBits(script,si,"Ccr");
                	if(tt==null) return null;
                }
                Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
                String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
                if((newTarget!=null)&&(newTarget instanceof MOB))
                {
                    Ability A=((MOB)newTarget).findAbility(cast);
                    if(A!=null)((MOB)newTarget).delAbility(A);
                    if((!((MOB)newTarget).isMonster())&&(A!=null))
                        Log.sysOut("Scripting",newTarget.Name()+" was MPDISABLED with "+A.Name());
                    ExpertiseLibrary.ExpertiseDefinition D=CMLib.expertises().findDefinition(cast,false);
                    if((newTarget instanceof MOB)&&(D!=null))
                        ((MOB)newTarget).delExpertise(D.ID);
                }
                break;
            }
            default:
                if(cmd.length()>0)
                {
                    Vector V=CMParms.parse(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,s));
                    if((V.size()>0)&&(monster!=null))
                        monster.doCommand(V,Command.METAFLAG_MPFORCED);
                }
                break;
            }
        }
        tickStatus=Tickable.STATUS_END;
        return null;
    }

    protected static final Vector empty=new Vector();

    protected Vector getScripts()
    {
        if(CMSecurity.isDisabled("SCRIPTABLE")||CMSecurity.isDisabled("SCRIPTING"))
            return empty;
        Vector scripts=null;
        if(getScript().length()>100)
            scripts=(Vector)Resources.getResource("PARSEDPRG: "+getScript().substring(0,100)+getScript().length()+getScript().hashCode());
        else
            scripts=(Vector)Resources.getResource("PARSEDPRG: "+getScript());
        if(scripts==null)
        {
            String scr=getScript();
            scr=CMStrings.replaceAll(scr,"`","'");
            scripts=parseScripts(scr);
            if(getScript().length()>100)
                Resources.submitResource("PARSEDPRG: "+getScript().substring(0,100)+getScript().length()+getScript().hashCode(),scripts);
            else
                Resources.submitResource("PARSEDPRG: "+getScript(),scripts);
        }
        return scripts;
    }

    protected boolean match(String str, String patt)
    {
        if(patt.trim().equalsIgnoreCase("ALL"))
            return true;
        if(patt.length()==0)
            return true;
        if(str.length()==0)
            return false;
        if(str.equalsIgnoreCase(patt))
            return true;
        return false;
    }

    private Item makeCheapItem(Environmental E)
    {
        Item product=null;
        if(E instanceof Item)
            product=(Item)E;
        else
        {
            product=CMClass.getItem("StdItem");
            product.setName(E.Name());
            product.setDisplayText(E.displayText());
            product.setDescription(E.description());
            product.setBaseEnvStats((EnvStats)E.baseEnvStats().copyOf());
            product.recoverEnvStats();
        }
        return product;
    }

    public boolean okMessage(Environmental affecting, CMMsg msg)
    {
        if((affecting==null)||(msg.source()==null))
            return true;

        Vector scripts=getScripts();
        DVector script=null;
        boolean tryIt=false;
        String trigger=null;
        String[] t=null;
        int triggerCode=0;
        String str=null;
        for(int v=0;v<scripts.size();v++)
        {
            tryIt=false;
            script=(DVector)scripts.elementAt(v);
            if(script.size()<1) continue;

            trigger=((String)script.elementAt(0,1)).toUpperCase().trim();
            t=(String[])script.elementAt(0,2);
            triggerCode=getTriggerCode(trigger,t);
            switch(triggerCode)
            {
            case 42: // cnclmsg_prog
                if(canTrigger(42))
                {
                    if(t==null) t=parseBits(script,0,"CCT");
                    String command=t[1];
                    boolean chk=false;
                    int x=command.indexOf('=');
                    if(x>0)
                    {
                        chk=true;
                        for(int i=0;i<x;i++)
                            switch(command.charAt(i)) {
                                case 'S': chk=chk&&msg.isSource(command.substring(x+1)); break;
                                case 'T': chk=chk&&msg.isTarget(command.substring(x+1)); break;
                                case 'O': chk=chk&&msg.isOthers(command.substring(x+1)); break;
                                default: chk=false; break;
                            }
                    }
                    else
                        chk=msg.isSource(command)||msg.isTarget(command)||msg.isOthers(command);
                    if(chk)
                    {
                        str="";
                        if((msg.source().session()!=null)&&(msg.source().session().previousCMD()!=null))
                            str=" "+CMParms.combine(msg.source().session().previousCMD(),0).toUpperCase()+" ";
                        if((t[2].length()==0)||(t[2].equals("ALL")))
                            tryIt=true;
                        else
                        if((t[2].equals("P"))&&(t.length>3))
                        {
                            if(match(str.trim(),t[3]))
                                tryIt=true;
                        }
                        else
                        for(int i=2;i<t.length;i++)
                        {
                            if(str.indexOf(" "+t[i]+" ")>=0)
                            {
                                str=(t[i].trim()+" "+str.trim()).trim();
                                tryIt=true;
                                break;
                            }
                        }
                    }
                }
                break;
            }
            if(tryIt)
            {
                MOB monster=getMakeMOB(affecting);
                if(lastKnownLocation==null) lastKnownLocation=msg.source().location();
                if((monster==null)||(monster.amDead())||(lastKnownLocation==null)) return true;
                Item defaultItem=(affecting instanceof Item)?(Item)affecting:null;
                Item Tool=null;
                if(msg.tool() instanceof Item)
                    Tool=(Item)msg.tool();
                if(Tool==null) Tool=defaultItem;
                String resp=null;
                if(msg.target() instanceof MOB)
                    resp=execute(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,str,newObjs());
                else
                if(msg.target() instanceof Item)
                    resp=execute(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,str,newObjs());
                else
                    resp=execute(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,str,newObjs());
                if((resp!=null)&&(resp.equalsIgnoreCase("CANCEL")))
                    return false;
            }
        }
        return true;
    }
    
    protected String standardTriggerCheck(DVector script, String[] t, Environmental E) {
        if(E==null) return null;
        if(t==null) t=parseBits(script,0,"CT");
        String NAME=E.Name().toUpperCase();
        if((t[1].length()==0)
        ||(t[1].equals("ALL"))
        ||(t[1].equals("P")
            &&(t.length>2)
            &&((t[2].indexOf(NAME)>=0)
                ||(E.ID().equalsIgnoreCase(t[2]))
                ||(t[2].equalsIgnoreCase("ALL")))))
            return t[1];
        for(int i=1;i<t.length;i++)
        {
            if(((" "+NAME+" ").indexOf(" "+t[i]+" ")>=0)
            ||(E.ID().equalsIgnoreCase(t[i]))
            ||(t[i].equalsIgnoreCase("ALL")))
                return t[i];
        }
        return null;
        
    }

    public void executeMsg(Environmental affecting, CMMsg msg)
    {
        if((affecting==null)||(msg.source()==null))
            return;

        MOB monster=getMakeMOB(affecting);

        if(lastKnownLocation==null) lastKnownLocation=msg.source().location();
        if((monster==null)||(monster.amDead())||(lastKnownLocation==null)) return;

        Item defaultItem=(affecting instanceof Item)?(Item)affecting:null;
        MOB eventMob=monster;
        if((defaultItem!=null)&&(defaultItem.owner() instanceof MOB))
            eventMob=(MOB)defaultItem.owner();

        Vector scripts=getScripts();

        if(msg.amITarget(eventMob)
        &&(!msg.amISource(monster))
        &&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
        &&(msg.source()!=monster))
            lastToHurtMe=msg.source();
        DVector script=null;
        String trigger=null;
        String[] t=null;
        for(int v=0;v<scripts.size();v++)
        {
            script=(DVector)scripts.elementAt(v);
            if(script.size()<1) continue;

            trigger=((String)script.elementAt(0,1)).toUpperCase().trim();
            t=(String[])script.elementAt(0,2);
            int triggerCode=getTriggerCode(trigger,t);
            int targetMinorTrigger=-1;
            switch(triggerCode)
            {
            case 1: // greet_prog
                if((msg.targetMinor()==CMMsg.TYP_ENTER)
                &&(msg.amITarget(lastKnownLocation))
                &&(!msg.amISource(eventMob))
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB)))
                &&canTrigger(1)
                &&((!(affecting instanceof MOB))||CMLib.flags().canSenseMoving(msg.source(),(MOB)affecting)))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    int prcnt=CMath.s_int(t[1]);
                    if(CMLib.dice().rollPercentage()<prcnt)
                    {
                        enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null);
                        return;
                    }
                }
                break;
            case 2: // all_greet_prog
                if((msg.targetMinor()==CMMsg.TYP_ENTER)&&canTrigger(2)
                &&(msg.amITarget(lastKnownLocation))
                &&(!msg.amISource(eventMob))
                &&(canActAtAll(monster)))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    int prcnt=CMath.s_int(t[1]);
                    if(CMLib.dice().rollPercentage()<prcnt)
                    {
                        enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null);
                        return;
                    }
                }
                break;
            case 3: // speech_prog
                if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)||(msg.targetMinor()==CMMsg.TYP_SPEAK))&&canTrigger(3)
                &&(!msg.amISource(monster))
                &&(!CMath.bset(msg.othersMajor(),CMMsg.MASK_CHANNEL))
                &&(((msg.othersMessage()!=null)&&((msg.tool()==null)||(!(msg.tool() instanceof Ability))||((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_LANGUAGE)))
                   ||((msg.target()==monster)&&(msg.targetMessage()!=null)&&(msg.tool()==null)))
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    if(t==null) t=parseBits(script,0,"CT");
                    String str=null;
                    if(msg.othersMessage()!=null)
                        str=CMStrings.replaceAll(CMStrings.getSayFromMessage(msg.othersMessage().toUpperCase()),"`","'");
                    else
                        str=CMStrings.replaceAll(CMStrings.getSayFromMessage(msg.targetMessage().toUpperCase()),"`","'");
                    str=(" "+str+" ").toUpperCase();
                    str=CMStrings.removeColors(str);
                    str=CMStrings.replaceAll(str,"\n\r"," ");
                    if((t[1].length()==0)||(t[1].equals("ALL")))
                    {
                        enqueResponse(affecting,msg.source(),msg.target(),monster,defaultItem,null,script,1,str);
                        return;
                    }
                    else
                    if((t[1].equals("P"))&&(t.length>2))
                    {
                        if(match(str.trim(),t[2]))
                        {
                            enqueResponse(affecting,msg.source(),msg.target(),monster,defaultItem,null,script,1,str);
                            return;
                        }
                    }
                    else
                    for(int i=1;i<t.length;i++)
                    {
                        int x=str.indexOf(" "+t[i]+" ");
                        if(x>=0)
                        {
                            enqueResponse(affecting,msg.source(),msg.target(),monster,defaultItem,null,script,1,str.substring(x).trim());
                            return;
                        }
                    }
                }
                break;
            case 4: // give_prog
                if((msg.targetMinor()==CMMsg.TYP_GIVE)
                &&canTrigger(4)
                &&((msg.amITarget(monster))
                        ||(msg.tool()==affecting)
                        ||(affecting instanceof Room)
                        ||(affecting instanceof Area))
                &&(!msg.amISource(monster))
                &&(msg.tool() instanceof Item)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    String check=standardTriggerCheck(script,t,msg.tool());
                    if(check!=null)
                    {
                        if(lastMsg==msg) break;
                        lastMsg=msg;
                        enqueResponse(affecting,msg.source(),monster,monster,(Item)msg.tool(),defaultItem,script,1,check);
                        return;
                    }
                }
                break;
            case 40: // llook_prog
                if((msg.targetMinor()==CMMsg.TYP_EXAMINE)&&canTrigger(40)
                &&(!msg.amISource(monster))
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    String check=standardTriggerCheck(script,t,msg.target());
                    if(check!=null)
                    {
                        enqueResponse(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),defaultItem,script,1,check);
                        return;
                    }
                }
                break;
            case 41: // execmsg_prog
                if(canTrigger(41))
                {
                    if(t==null) t=parseBits(script,0,"CCT");
                    String command=t[1];
                    boolean chk=false;
                    int x=command.indexOf('=');
                    if(x>0)
                    {
                        chk=true;
                        for(int i=0;i<x;i++)
                            switch(command.charAt(i)) {
                                case 'S': chk=chk&&msg.isSource(command.substring(x+1)); break;
                                case 'T': chk=chk&&msg.isTarget(command.substring(x+1)); break;
                                case 'O': chk=chk&&msg.isOthers(command.substring(x+1)); break;
                                default: chk=false; break;
                            }
                    }
                    else
                        chk=msg.isSource(command)||msg.isTarget(command)||msg.isOthers(command);
                    if(chk)
                    {
                        String str="";
                        if((msg.source().session()!=null)&&(msg.source().session().previousCMD()!=null))
                            str=" "+CMParms.combine(msg.source().session().previousCMD(),0).toUpperCase()+" ";
                        boolean doIt=false;
                        if((t[2].length()==0)||(t[2].equals("ALL")))
                            doIt=true;
                        else
                        if((t[2].equals("P"))&&(t.length>3))
                        {
                            if(match(str.trim(),t[3]))
                                doIt=true;
                        }
                        else
                        for(int i=2;i<t.length;i++)
                        {
                            if(str.indexOf(" "+t[i]+" ")>=0)
                            {
                                str=(t[i].trim()+" "+str.trim()).trim();
                                doIt=true;
                                break;
                            }
                        }
                        if(doIt)
                        {
                            Item Tool=null;
                            if(msg.tool() instanceof Item)
                                Tool=(Item)msg.tool();
                            if(Tool==null) Tool=defaultItem;
                            if(msg.target() instanceof MOB)
                                enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
                            else
                            if(msg.target() instanceof Item)
                                enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,1,str);
                            else
                                enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
                            return;
                        }
                    }
                }
                break;
            case 39: // look_prog
                if((msg.targetMinor()==CMMsg.TYP_LOOK)&&canTrigger(39)
                &&(!msg.amISource(monster))
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    String check=standardTriggerCheck(script,t,msg.target());
                    if(check!=null)
                    {
                        enqueResponse(affecting,msg.source(),msg.target(),monster,defaultItem,defaultItem,script,1,check);
                        return;
                    }
                }
                break;
            case 20: // get_prog
                if((msg.targetMinor()==CMMsg.TYP_GET)&&canTrigger(20)
                &&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
                &&(!msg.amISource(monster))
                &&(msg.target() instanceof Item)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    String check=standardTriggerCheck(script,t,msg.target());
                    if(check!=null)
                    {
                        if(lastMsg==msg) break;
                        lastMsg=msg;
                        enqueResponse(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),defaultItem,script,1,check);
                        return;
                    }
                }
                break;
            case 22: // drop_prog
                if((msg.targetMinor()==CMMsg.TYP_DROP)&&canTrigger(22)
                &&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
                &&(!msg.amISource(monster))
                &&(msg.target() instanceof Item)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    String check=standardTriggerCheck(script,t,msg.target());
                    if(check!=null)
                    {
                        if(lastMsg==msg) break;
                        lastMsg=msg;
                        if(msg.target() instanceof Coins)
                            execute(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),(Item)((Item)msg.target()).copyOf(),script,check,newObjs());
                        else
                            enqueResponse(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),defaultItem,script,1,check);
                        return;
                    }
                }
                break;
            case 24: // remove_prog
                if((msg.targetMinor()==CMMsg.TYP_REMOVE)&&canTrigger(24)
                &&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
                &&(!msg.amISource(monster))
                &&(msg.target() instanceof Item)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    String check=standardTriggerCheck(script,t,msg.target());
                    if(check!=null)
                    {
                        enqueResponse(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),defaultItem,script,1,check);
                        return;
                    }
                }
                break;
            case 34: // open_prog
                if(targetMinorTrigger<0) targetMinorTrigger=CMMsg.TYP_OPEN;
            case 35: // close_prog
                if(targetMinorTrigger<0) targetMinorTrigger=CMMsg.TYP_CLOSE;
            case 36: // lock_prog
                if(targetMinorTrigger<0) targetMinorTrigger=CMMsg.TYP_LOCK;
            case 37: // unlock_prog
            {
                if(targetMinorTrigger<0) targetMinorTrigger=CMMsg.TYP_UNLOCK;
                if((msg.targetMinor()==targetMinorTrigger)&&canTrigger(triggerCode)
                &&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
                &&(!msg.amISource(monster))
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    Item I=(msg.target() instanceof Item)?(Item)msg.target():defaultItem;
                    String check=standardTriggerCheck(script,t,msg.target());
                    if(check!=null)
                    {
                        enqueResponse(affecting,msg.source(),msg.target(),monster,I,defaultItem,script,1,check);
                        return;
                    }
                }
                break;
            }
            case 25: // consume_prog
                if(((msg.targetMinor()==CMMsg.TYP_EAT)||(msg.targetMinor()==CMMsg.TYP_DRINK))
                &&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
                &&(!msg.amISource(monster))&&canTrigger(25)
                &&(msg.target() instanceof Item)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    String check=standardTriggerCheck(script,t,msg.target());
                    if(check!=null)
                    {
                    	if((msg.target() == affecting)
                    	&&(affecting instanceof Food))
                            execute(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),(Item)((Item)msg.target()).copyOf(),script,check,newObjs());
                    	else
	                        enqueResponse(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),defaultItem,script,1,check);
                        return;
                    }
                }
                break;
            case 21: // put_prog
                if((msg.targetMinor()==CMMsg.TYP_PUT)&&canTrigger(21)
                &&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
                &&(msg.tool() instanceof Item)
                &&(!msg.amISource(monster))
                &&(msg.target() instanceof Item)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    String check=standardTriggerCheck(script,t,msg.target());
                    if(check!=null)
                    {
                        if(lastMsg==msg) break;
                        lastMsg=msg;
                        if((msg.tool() instanceof Coins)&&(((Item)msg.target()).owner() instanceof Room))
                            execute(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),(Item)((Item)msg.target()).copyOf(),script,check,newObjs());
                        else
                            enqueResponse(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),(Item)msg.tool(),script,1,check);
                        return;
                    }
                }
                break;
            case 27: // buy_prog
                if((msg.targetMinor()==CMMsg.TYP_BUY)&&canTrigger(27)
                &&((!(affecting instanceof ShopKeeper))
                    ||msg.amITarget(affecting))
                &&(!msg.amISource(monster))
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    String check=standardTriggerCheck(script,t,msg.tool());
                    if(check!=null)
                    {
                        Item product=makeCheapItem(msg.tool());
                        if((product instanceof Coins)
                        &&(product.owner() instanceof Room))
                            execute(affecting,msg.source(),monster,monster,product,(Item)product.copyOf(),script,check,newObjs());
                        else
                            enqueResponse(affecting,msg.source(),monster,monster,product,product,script,1,check);
                        return;
                    }
                }
                break;
            case 28: // sell_prog
                if((msg.targetMinor()==CMMsg.TYP_SELL)&&canTrigger(28)
                &&((msg.amITarget(affecting))||(!(affecting instanceof ShopKeeper)))
                &&(!msg.amISource(monster))
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    String check=standardTriggerCheck(script,t,msg.tool());
                    if(check!=null)
                    {
                        Item product=makeCheapItem(msg.tool());
                        if((product instanceof Coins)
                        &&(product.owner() instanceof Room))
                            execute(affecting,msg.source(),monster,monster,product,(Item)product.copyOf(),script,null,newObjs());
                        else
                            enqueResponse(affecting,msg.source(),monster,monster,product,product,script,1,check);
                        return;
                    }
                }
                break;
            case 23: // wear_prog
                if(((msg.targetMinor()==CMMsg.TYP_WEAR)
                    ||(msg.targetMinor()==CMMsg.TYP_HOLD)
                    ||(msg.targetMinor()==CMMsg.TYP_WIELD))
                &&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
                &&(!msg.amISource(monster))&&canTrigger(23)
                &&(msg.target() instanceof Item)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    String check=standardTriggerCheck(script,t,msg.target());
                    if(check!=null)
                    {
                        enqueResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,check);
                        return;
                    }
                }
                break;
            case 19: // bribe_prog
                if((msg.targetMinor()==CMMsg.TYP_GIVE)
                &&(msg.amITarget(eventMob)||(!(affecting instanceof MOB)))
                &&(!msg.amISource(monster))&&canTrigger(19)
                &&(msg.tool() instanceof Coins)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    if(t[1].startsWith("ANY")||t[1].startsWith("ALL"))
                        t[1]=t[1].trim();
                    else
                    if(!((Coins)msg.tool()).getCurrency().equals(CMLib.beanCounter().getCurrency(monster)))
                        break;
                    double d=0.0;
                    if(CMath.isDouble(t[1]))
                        d=CMath.s_double(t[1]);
                    else
                        d=(double)CMath.s_int(t[1]);
                    if((((Coins)msg.tool()).getTotalValue()>=d)
                    ||(t[1].equals("ALL"))
                    ||(t[1].equals("ANY")))
                    {
                        enqueResponse(affecting,msg.source(),monster,monster,(Item)msg.tool(),defaultItem,script,1,null);
                        return;
                    }
                }
                break;
            case 8: // entry_prog
                if((msg.targetMinor()==CMMsg.TYP_ENTER)&&canTrigger(8)
                &&(msg.amISource(eventMob)
                    ||(msg.target()==affecting)
                    ||(msg.tool()==affecting)
                    ||(affecting instanceof Item))
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    int prcnt=CMath.s_int(t[1]);
                    if(CMLib.dice().rollPercentage()<prcnt)
                    {
                        Vector V=(Vector)que.clone();
                        ScriptableResponse SB=null;
                        String roomID=null;
                        if(msg.target()!=null)
                            roomID=CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(msg.target()));
                        for(int q=0;q<V.size();q++)
                        {
                            SB=(ScriptableResponse)V.elementAt(q);
                            if((SB.scr==script)&&(SB.s==msg.source()))
                            {
                                if(que.removeElement(SB))
                                    execute(SB.h,SB.s,SB.t,SB.m,SB.pi,SB.si,SB.scr,SB.message,newObjs());
                                break;
                            }
                        }
                        enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,roomID);
                        return;
                    }
                }
                break;
            case 9: // exit prog
                if((msg.targetMinor()==CMMsg.TYP_LEAVE)&&canTrigger(9)
                &&(msg.amITarget(lastKnownLocation))
                &&(!msg.amISource(eventMob))
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    int prcnt=CMath.s_int(t[1]);
                    if(CMLib.dice().rollPercentage()<prcnt)
                    {
                        Vector V=(Vector)que.clone();
                        ScriptableResponse SB=null;
                        String roomID=null;
                        if(msg.target()!=null)
                            roomID=CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(msg.target()));
                        for(int q=0;q<V.size();q++)
                        {
                            SB=(ScriptableResponse)V.elementAt(q);
                            if((SB.scr==script)&&(SB.s==msg.source()))
                            {
                                if(que.removeElement(SB))
                                    execute(SB.h,SB.s,SB.t,SB.m,SB.pi,SB.si,SB.scr,SB.message,newObjs());
                                break;
                            }
                        }
                        enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,roomID);
                        return;
                    }
                }
                break;
            case 10: // death prog
                if((msg.sourceMinor()==CMMsg.TYP_DEATH)&&canTrigger(10)
                &&(msg.amISource(eventMob)||(!(affecting instanceof MOB))))
                {
                    if(t==null) t=parseBits(script,0,"C");
                    MOB ded=msg.source();
                    MOB src=lastToHurtMe;
                    if(msg.tool() instanceof MOB)
                        src=(MOB)msg.tool();
                    if((src==null)||(src.location()!=monster.location()))
                       src=ded;
                    execute(affecting,src,ded,ded,defaultItem,null,script,null,newObjs());
                    return;
                }
                break;
            case 44: // kill prog
                if((msg.sourceMinor()==CMMsg.TYP_DEATH)&&canTrigger(44)
                &&((msg.tool()==affecting)||(!(affecting instanceof MOB))))
                {
                    if(t==null) t=parseBits(script,0,"C");
                    MOB ded=msg.source();
                    MOB src=lastToHurtMe;
                    if(msg.tool() instanceof MOB)
                        src=(MOB)msg.tool();
                    if((src==null)||(src.location()!=monster.location()))
                       src=ded;
                    execute(affecting,src,ded,ded,defaultItem,null,script,null,newObjs());
                    return;
                }
                break;
            case 26: // damage prog
                if((msg.targetMinor()==CMMsg.TYP_DAMAGE)&&canTrigger(26)
                &&(msg.amITarget(eventMob)||(msg.tool()==affecting)))
                {
                    if(t==null) t=parseBits(script,0,"C");
                    Item I=null;
                    if(msg.tool() instanceof Item)
                        I=(Item)msg.tool();
                    execute(affecting,msg.source(),msg.target(),eventMob,defaultItem,I,script,""+msg.value(),newObjs());
                    return;
                }
                break;
            case 29: // login_prog
                if(!registeredSpecialEvents.contains(Integer.valueOf(CMMsg.TYP_LOGIN)))
                {
                    CMLib.map().addGlobalHandler(affecting,CMMsg.TYP_LOGIN);
                    registeredSpecialEvents.add(Integer.valueOf(CMMsg.TYP_LOGIN));
                }
                if((msg.sourceMinor()==CMMsg.TYP_LOGIN)&&canTrigger(29)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB)))
                &&(!CMLib.flags().isCloaked(msg.source())))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    int prcnt=CMath.s_int(t[1]);
                    if(CMLib.dice().rollPercentage()<prcnt)
                    {
                        enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null);
                        return;
                    }
                }
                break;
            case 32: // level_prog
                if(!registeredSpecialEvents.contains(Integer.valueOf(CMMsg.TYP_LEVEL)))
                {
                    CMLib.map().addGlobalHandler(affecting,CMMsg.TYP_LEVEL);
                    registeredSpecialEvents.add(Integer.valueOf(CMMsg.TYP_LEVEL));
                }
                if((msg.sourceMinor()==CMMsg.TYP_LEVEL)&&canTrigger(32)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB)))
                &&(!CMLib.flags().isCloaked(msg.source())))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    int prcnt=CMath.s_int(t[1]);
                    if(CMLib.dice().rollPercentage()<prcnt)
                    {
                        enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null);
                        return;
                    }
                }
                break;
            case 30: // logoff_prog
                if((msg.sourceMinor()==CMMsg.TYP_QUIT)&&canTrigger(30)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB)))
                &&(!CMLib.flags().isCloaked(msg.source())))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    int prcnt=CMath.s_int(t[1]);
                    if(CMLib.dice().rollPercentage()<prcnt)
                    {
                        enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null);
                        return;
                    }
                }
                break;
            case 12: // mask_prog
                if(!canTrigger(12))
                    break;
            case 18: // act_prog
                if((msg.amISource(monster))
                ||((triggerCode==18)&&(!canTrigger(18))))
                    break;
            case 43: // imask_prog
                if((triggerCode!=43)||(msg.amISource(monster)&&canTrigger(43)))
                {
                    if(t==null)
                    {
                    	t=parseBits(script,0,"CT");
                    	for(int i=0;i<t.length;i++)
                    		t[i]=CMLib.english().stripPunctuation(CMStrings.removeColors(t[i]));
                    }
                    boolean doIt=false;
                    String str=msg.othersMessage();
                    if(str==null) str=msg.targetMessage();
                    if(str==null) str=msg.sourceMessage();
                    if(str==null) break;
                    str=CMLib.coffeeFilter().fullOutFilter(null,monster,msg.source(),msg.target(),msg.tool(),str,false);
                    str=CMLib.english().stripPunctuation(CMStrings.removeColors(str));
                    str=" "+CMStrings.replaceAll(str,"\n\r"," ").toUpperCase().trim()+" ";
                    if((t[1].length()==0)||(t[1].equals("ALL")))
                        doIt=true;
                    else
                    if((t[1].equals("P"))&&(t.length>2))
                    {
                        if(match(str.trim(),t[2]))
                            doIt=true;
                    }
                    else
                    for(int i=1;i<t.length;i++)
                        if(str.indexOf(" "+t[i]+" ")>=0)
                        {
                            str=t[i];
                            doIt=true;
                            break;
                        }
                    if(doIt)
                    {
                        Item Tool=null;
                        if(msg.tool() instanceof Item)
                            Tool=(Item)msg.tool();
                        if(Tool==null) Tool=defaultItem;
                        if(msg.target() instanceof MOB)
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
                        else
                        if(msg.target() instanceof Item)
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,1,str);
                        else
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
                        return;
                    }
                }
                break;
            case 38: // social prog
                if(!msg.amISource(monster)
                &&canTrigger(38)
                &&(msg.tool() instanceof Social))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    if(((Social)msg.tool()).Name().toUpperCase().startsWith(t[1]))
                    {
                        Item Tool=defaultItem;
                        if(msg.target() instanceof MOB)
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,msg.tool().Name());
                        else
                        if(msg.target() instanceof Item)
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,1,msg.tool().Name());
                        else
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,msg.tool().Name());
                        return;
                    }
                }
                break;
            case 33: // channel prog
                if(!registeredSpecialEvents.contains(Integer.valueOf(CMMsg.TYP_CHANNEL)))
                {
                    CMLib.map().addGlobalHandler(affecting,CMMsg.TYP_CHANNEL);
                    registeredSpecialEvents.add(Integer.valueOf(CMMsg.TYP_CHANNEL));
                }
                if(!msg.amISource(monster)
                &&(CMath.bset(msg.othersMajor(),CMMsg.MASK_CHANNEL))
                &&canTrigger(33))
                {
                    if(t==null) t=parseBits(script,0,"CCT");
                    boolean doIt=false;
                    String channel=t[1];
                    int channelInt=msg.othersMinor()-CMMsg.TYP_CHANNEL;
                    String str=null;
                    if(channel.equalsIgnoreCase(CMLib.channels().getChannelName(channelInt)))
                    {
                        str=msg.sourceMessage();
                        if(str==null) str=msg.othersMessage();
                        if(str==null) str=msg.targetMessage();
                        if(str==null) break;
                        str=CMLib.coffeeFilter().fullOutFilter(null,monster,msg.source(),msg.target(),msg.tool(),str,false).toUpperCase().trim();
                        int dex=str.indexOf("["+channel+"]");
                        if(dex>0)
                            str=str.substring(dex+2+channel.length()).trim();
                        else
                        {
                            dex=str.indexOf("'");
                            int edex=str.lastIndexOf("'");
                            if(edex>dex) str=str.substring(dex+1,edex);
                        }
                        str=" "+CMStrings.removeColors(str)+" ";
                        str=CMStrings.replaceAll(str,"\n\r"," ");
                        if((t[2].length()==0)||(t[2].equals("ALL")))
                            doIt=true;
                        else
                        if(t[2].equals("P")&&(t.length>2))
                        {
                            if(match(str.trim(),t[3]))
                                doIt=true;
                        }
                        else
                        for(int i=2;i<t.length;i++)
                            if(str.indexOf(" "+t[i]+" ")>=0)
                            {
                                str=t[i];
                                doIt=true;
                                break;
                            }
                    }
                    if(doIt)
                    {
                        Item Tool=null;
                        if(msg.tool() instanceof Item)
                            Tool=(Item)msg.tool();
                        if(Tool==null) Tool=defaultItem;
                        if(msg.target() instanceof MOB)
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
                        else
                        if(msg.target() instanceof Item)
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,1,str);
                        else
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
                        return;
                    }
                }
                break;
            case 31: // regmask prog
                if(!msg.amISource(monster)&&canTrigger(31))
                {
                    boolean doIt=false;
                    String str=msg.othersMessage();
                    if(str==null) str=msg.targetMessage();
                    if(str==null) str=msg.sourceMessage();
                    if(str==null) break;
                    str=CMLib.coffeeFilter().fullOutFilter(null,monster,msg.source(),msg.target(),msg.tool(),str,false);
                    if(t==null) t=parseBits(script,0,"Cp");
                    if(CMParms.getCleanBit(t[1],0).equalsIgnoreCase("p"))
                        doIt=str.trim().equals(t[1].substring(1).trim());
                    else
                    {
                        Pattern P=(Pattern)patterns.get(t[1]);
                        if(P==null)
                        {
                            P=Pattern.compile(t[1], Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                            patterns.put(t[1],P);
                        }
                        Matcher M=P.matcher(str);
                        doIt=M.find();
                        if(doIt) str=str.substring(M.start()).trim();
                    }
                    if(doIt)
                    {
                        Item Tool=null;
                        if(msg.tool() instanceof Item)
                            Tool=(Item)msg.tool();
                        if(Tool==null) Tool=defaultItem;
                        if(msg.target() instanceof MOB)
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
                        else
                        if(msg.target() instanceof Item)
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,1,str);
                        else
                            enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
                        return;
                    }
                }
                break;
            }
        }
    }

    protected int getTriggerCode(String trigger, String[] ttrigger)
    {
        Integer I=null;
        if((ttrigger!=null)&&(ttrigger.length>0))
            I=(Integer)progH.get(ttrigger[0]);
        else
        {
            int x=trigger.indexOf(" ");
            if(x<0)
                I=(Integer)progH.get(trigger.toUpperCase().trim());
            else
                I=(Integer)progH.get(trigger.substring(0,x).toUpperCase().trim());
        }
        if(I==null) return 0;
        return I.intValue();
    }
    
    public MOB getMakeMOB(Tickable ticking)
    {
        MOB mob=null;
        if(ticking instanceof MOB)
        {
            mob=(MOB)ticking;
            if(!mob.amDead())
                lastKnownLocation=mob.location();
        }
        else
        if(ticking instanceof Environmental)
        {
            Room R=CMLib.map().roomLocation((Environmental)ticking);
            if(R!=null) lastKnownLocation=R;

            if((backupMOB==null)
            ||(backupMOB.amDestroyed())
            ||(backupMOB.amDead()))
            {
                backupMOB=CMClass.getMOB("StdMOB");
                if(backupMOB!=null)
                {
    	            backupMOB.setName(ticking.name());
    	            backupMOB.setDisplayText(ticking.name()+" is here.");
                    backupMOB.setDescription("");
                    backupMOB.setAgeHours(-1);
                    mob=backupMOB;
                    if(backupMOB.location()!=lastKnownLocation)
                        backupMOB.setLocation(lastKnownLocation);
                }
            }
            else
            {
                backupMOB.setAgeHours(-1);
                mob=backupMOB;
                if(backupMOB.location()!=lastKnownLocation)
                {
                    backupMOB.setLocation(lastKnownLocation);
    	            backupMOB.setName(ticking.name());
    	            backupMOB.setDisplayText(ticking.name()+" is here.");
                }
            }
        }
        return mob;
    }

    protected boolean canTrigger(int triggerCode)
    {
        Long L=(Long)noTrigger.get(Integer.valueOf(triggerCode));
        if(L==null) return true;
        if(System.currentTimeMillis()<L.longValue())
            return false;
        noTrigger.remove(Integer.valueOf(triggerCode));
        return true;
    }

    public boolean tick(Tickable ticking, int tickID)
    {
        MOB mob=getMakeMOB(ticking);
        Item defaultItem=(ticking instanceof Item)?(Item)ticking:null;

        if((mob==null)||(lastKnownLocation==null))
        {
            altStatusTickable=null;
            return true;
        }

        Environmental affecting=(ticking instanceof Environmental)?((Environmental)ticking):null;

        Vector scripts=getScripts();

        int triggerCode=-1;
        String trigger="";
        String[] t=null;
        for(int thisScriptIndex=0;thisScriptIndex<scripts.size();thisScriptIndex++)
        {
            DVector script=(DVector)scripts.elementAt(thisScriptIndex);
            if(script.size()<2) continue;
            trigger=((String)script.elementAt(0,1)).toUpperCase().trim();
            t=(String[])script.elementAt(0,2);
            triggerCode=getTriggerCode(trigger,t);
            tickStatus=Tickable.STATUS_SCRIPT+triggerCode;
            switch(triggerCode)
            {
            case 5: // rand_Prog
                if((!mob.amDead())&&canTrigger(5))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    int prcnt=CMath.s_int(t[1]);
                    if(CMLib.dice().rollPercentage()<prcnt)
                        execute(affecting,mob,mob,mob,defaultItem,null,script,null,newObjs());
                }
                break;
            case 16: // delay_prog
                if((!mob.amDead())&&canTrigger(16))
                {
                    int targetTick=-1;
                    if(delayTargetTimes.containsKey(Integer.valueOf(thisScriptIndex)))
                        targetTick=((Integer)delayTargetTimes.get(Integer.valueOf(thisScriptIndex))).intValue();
                    else
                    {
                        if(t==null) t=parseBits(script,0,"CCR");
                        int low=CMath.s_int(t[1]);
                        int high=CMath.s_int(t[2]);
                        if(high<low) high=low;
                        targetTick=CMLib.dice().roll(1,high-low+1,low-1);
                        delayTargetTimes.put(Integer.valueOf(thisScriptIndex),Integer.valueOf(targetTick));
                    }
                    int delayProgCounter=0;
                    if(delayProgCounters.containsKey(Integer.valueOf(thisScriptIndex)))
                        delayProgCounter=((Integer)delayProgCounters.get(Integer.valueOf(thisScriptIndex))).intValue();
                    else
                        delayProgCounters.put(Integer.valueOf(thisScriptIndex),Integer.valueOf(0));
                    if(delayProgCounter==targetTick)
                    {
                        execute(affecting,mob,mob,mob,defaultItem,null,script,null,newObjs());
                        delayProgCounter=-1;
                    }
                    delayProgCounters.remove(Integer.valueOf(thisScriptIndex));
                    delayProgCounters.put(Integer.valueOf(thisScriptIndex),Integer.valueOf(delayProgCounter+1));
                }
                break;
            case 7: // fightProg
                if((mob.isInCombat())&&(!mob.amDead())&&canTrigger(7))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    int prcnt=CMath.s_int(t[1]);
                    if(CMLib.dice().rollPercentage()<prcnt)
                        execute(affecting,mob.getVictim(),mob,mob,defaultItem,null,script,null,newObjs());
                }
                else
                if((ticking instanceof Item)
                &&canTrigger(7)
                &&(((Item)ticking).owner() instanceof MOB)
                &&(((MOB)((Item)ticking).owner()).isInCombat()))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    int prcnt=CMath.s_int(t[1]);
                    if(CMLib.dice().rollPercentage()<prcnt)
                    {
                        MOB M=(MOB)((Item)ticking).owner();
                        if(!M.amDead())
                            execute(affecting,M,mob.getVictim(),mob,defaultItem,null,script,null,newObjs());
                    }
                }
                break;
            case 11: // hitprcnt
                if((mob.isInCombat())&&(!mob.amDead())&&canTrigger(11))
                {
                    if(t==null) t=parseBits(script,0,"CR");
                    int prcnt=CMath.s_int(t[1]);
                    int floor=(int)Math.round(CMath.mul(CMath.div(prcnt,100.0),mob.maxState().getHitPoints()));
                    if(mob.curState().getHitPoints()<=floor)
                        execute(affecting,mob.getVictim(),mob,mob,defaultItem,null,script,null,newObjs());
                }
                else
                if((ticking instanceof Item)
                &&canTrigger(11)
                &&(((Item)ticking).owner() instanceof MOB)
                &&(((MOB)((Item)ticking).owner()).isInCombat()))
                {
                    MOB M=(MOB)((Item)ticking).owner();
                    if(!M.amDead())
                    {
                        if(t==null) t=parseBits(script,0,"CR");
                        int prcnt=CMath.s_int(t[1]);
                        int floor=(int)Math.round(CMath.mul(CMath.div(prcnt,100.0),M.maxState().getHitPoints()));
                        if(M.curState().getHitPoints()<=floor)
                            execute(affecting,M,mob.getVictim(),mob,defaultItem,null,script,null,newObjs());
                    }
                }
                break;
            case 6: // once_prog
                if(!oncesDone.contains(script)&&canTrigger(6))
                {
                    if(t==null) t=parseBits(script,0,"C");
                    oncesDone.addElement(script);
                    execute(affecting,mob,mob,mob,defaultItem,null,script,null,newObjs());
                }
                break;
            case 14: // time_prog
                if((mob.location()!=null)
                &&canTrigger(14)
                &&(!mob.amDead()))
                {
                    if(t==null) t=parseBits(script,0,"CT");
                    int lastTimeProgDone=-1;
                    if(lastTimeProgsDone.containsKey(Integer.valueOf(thisScriptIndex)))
                        lastTimeProgDone=((Integer)lastTimeProgsDone.get(Integer.valueOf(thisScriptIndex))).intValue();
                    int time=mob.location().getArea().getTimeObj().getTimeOfDay();
                    if(lastTimeProgDone!=time)
                    {
                        boolean done=false;
                        for(int i=1;i<t.length;i++)
                        {
                            if(time==CMath.s_int(t[i]))
                            {
                                done=true;
                                execute(affecting,mob,mob,mob,defaultItem,null,script,""+time,newObjs());
                                lastTimeProgsDone.remove(Integer.valueOf(thisScriptIndex));
                                lastTimeProgsDone.put(Integer.valueOf(thisScriptIndex),Integer.valueOf(time));
                                break;
                            }
                        }
                        if(!done)
                            lastTimeProgsDone.remove(Integer.valueOf(thisScriptIndex));
                    }
                }
                break;
            case 15: // day_prog
                if((mob.location()!=null)&&canTrigger(15)
                &&(!mob.amDead()))
                {
                    if(t==null) t=parseBits(script,0,"CT");
                    int lastDayProgDone=-1;
                    if(lastDayProgsDone.containsKey(Integer.valueOf(thisScriptIndex)))
                        lastDayProgDone=((Integer)lastDayProgsDone.get(Integer.valueOf(thisScriptIndex))).intValue();
                    int day=mob.location().getArea().getTimeObj().getDayOfMonth();
                    if(lastDayProgDone!=day)
                    {
                        boolean done=false;
                        for(int i=1;i<t.length;i++)
                        {
                            if(day==CMath.s_int(t[i]))
                            {
                                done=true;
                                execute(affecting,mob,mob,mob,defaultItem,null,script,null,newObjs());
                                lastDayProgsDone.remove(Integer.valueOf(thisScriptIndex));
                                lastDayProgsDone.put(Integer.valueOf(thisScriptIndex),Integer.valueOf(day));
                                break;
                            }
                        }
                        if(!done)
                            lastDayProgsDone.remove(Integer.valueOf(thisScriptIndex));
                    }
                }
                break;
            case 13: // quest time prog
                if(!oncesDone.contains(script)&&canTrigger(13))
                {
                    if(t==null) t=parseBits(script,0,"CCC");
                    Quest Q=getQuest(t[1]);
                    if((Q!=null)&&(Q.running())&&(!Q.stopping()))
                    {
                        int time=CMath.s_int(t[2]);
                        if(time>=Q.minsRemaining())
                        {
                            oncesDone.addElement(script);
                            execute(affecting,mob,mob,mob,defaultItem,null,script,null,newObjs());
                        }
                    }
                }
                break;
            default:
                break;
            }
        }
        tickStatus=Tickable.STATUS_SCRIPT+100;
        dequeResponses();
        altStatusTickable=null;
        return true;
    }

    public void initializeClass(){};
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

    public void enqueResponse(Environmental host,
                              MOB source,
                              Environmental target,
                              MOB monster,
                              Item primaryItem,
                              Item secondaryItem,
                              DVector script,
                              int ticks,
                              String msg)
    {
        if(noDelay)
            execute(host,source,target,monster,primaryItem,secondaryItem,script,msg,newObjs());
        else
            que.addElement(new ScriptableResponse(host,source,target,monster,primaryItem,secondaryItem,script,ticks,msg));
    }
    
    public void prequeResponse(Environmental host,
                               MOB source,
                               Environmental target,
                               MOB monster,
                               Item primaryItem,
                               Item secondaryItem,
                               DVector script,
                               int ticks,
                               String msg)
    {
        que.insertElementAt(new ScriptableResponse(host,source,target,monster,primaryItem,secondaryItem,script,ticks,msg),0);
    }
    
    public void dequeResponses()
    {
        try{
            tickStatus=Tickable.STATUS_SCRIPT+100;
            for(int q=que.size()-1;q>=0;q--)
            {
                ScriptableResponse SB=null;
                try{SB=(ScriptableResponse)que.elementAt(q);}catch(ArrayIndexOutOfBoundsException x){continue;}
                if(SB.checkTimeToExecute())
                {
                    execute(SB.h,SB.s,SB.t,SB.m,SB.pi,SB.si,SB.scr,SB.message,newObjs());
                    que.removeElement(SB);
                }
            }
        }catch(Exception e){Log.errOut("DefaultScriptingEngine",e);}
    }

    protected static class JScriptEvent extends ScriptableObject
    {
        public String getClassName(){ return "JScriptEvent";}
        static final long serialVersionUID=43;
        Environmental h=null;
        MOB s=null;
        Environmental t=null;
        MOB m=null;
        Item pi=null;
        Item si=null;
        Vector scr;
        String message=null;
        DefaultScriptingEngine c=null;
        public Environmental host(){return h;}
        public MOB source(){return s;}
        public Environmental target(){return t;}
        public MOB monster(){return m;}
        public Item item(){return pi;}
        public Item item2(){return si;}
        public String message(){return message;}
        public void setVar(String host, String var, String value)
        {
            c.setVar(host,var.toUpperCase(),value);
        }
        public String getVar(String host, String var)
        { return c.getVar(host,var);}
        public String toJavaString(Object O){return Context.toString(O);}

        public JScriptEvent(DefaultScriptingEngine scrpt,
                            Environmental host,
                            MOB source,
                            Environmental target,
                            MOB monster,
                            Item primaryItem,
                            Item secondaryItem,
                            String msg)
        {
            c=scrpt;
            h=host;
            s=source;
            t=target;
            m=monster;
            pi=primaryItem;
            si=secondaryItem;
            message=msg;
        }
    }
}
