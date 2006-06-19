package com.planet_ink.coffee_mud.Common;
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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class DefaultQuest implements Quest, Tickable, CMObject
{

    public String ID(){return "DefaultQuest";}

    protected String name="";
    protected String startDate="";
    protected boolean alreadyDateStarted=false;
    protected int duration=450; // about 30 minutes
    protected String parms="";
    protected Vector stuff=new Vector();
    protected Vector winners=new Vector();
    protected int minWait=-1;
    protected int minPlayers=-1;
    protected String playerMask="";
    protected int runLevel=-1;
    protected int maxWait=-1;
    protected int waitRemaining=-1;
    protected int ticksRemaining=-1;
    private boolean stoppingQuest=false;
    protected DVector vars=new DVector(2);

    protected Vector addons=new Vector();
    // contains a set of vectors, vectors are formatted as such:
    // 0=environmental item/mob/etc
    // 1=Ability, 2=Ability (for an ability added)
    // 1=Ability, 2=Ability, 3=String (for an ability modified)
    // 1=Effect(for an Effect added)
    // 1=Effect, 2=String (for an Effect modified)
    // 1=Behavior (for an Behavior added)
    // 1=Behavior, 2=String (for an Behavior modified)

    // the unique name of the quest
    public String name(){return name;}
    public void setName(String newName){name=newName;}
    public CMObject copyOf()
    {
        try
        {
            Object O=this.clone();
            return (CMObject)O;
        }
        catch(CloneNotSupportedException e)
        {
            return newInstance();
        }
    }
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultQuest();}}

    // the unique name of the quest
    public String startDate(){return startDate;}
    public void setStartDate(String newDate){
        int x=newDate.indexOf("-");
        if((x>0)
        &&(CMath.isInteger(newDate.substring(0,x)))
        &&(CMath.isInteger(newDate.substring(x+1))))
	    	startDate=newDate;
    }
    
    // the duration, in ticks
    public int duration(){return duration;}
    public void setDuration(int newTicks){duration=newTicks;}

	public int minPlayers(){return minPlayers;}
	public void setMinPlayers(int players){minPlayers=players;}
	public int runLevel(){return runLevel;}
	public void setRunLevel(int level){runLevel=level;}
	public String playerMask(){return playerMask;}
	public void setPlayerMask(String mask){playerMask=mask;}
	
    // the rest of the script.  This may be semicolon-separated instructions,
    // or a LOAD command followed by the quest script path.
    public void setScript(String parm){
        parms=parm;
        setVars(parseScripts(parm));
    }
    public String script(){return parms;}

    public void autostartup()
    {
        if(!setWaitRemaining())
            CMLib.threads().deleteTick(this,Tickable.TICKID_QUEST);
        else
        if(!running())
            CMLib.threads().startTickDown(this,Tickable.TICKID_QUEST,1);
    }
    
    protected void setVars(Vector script)
    {
        name="";
        startDate="";
        alreadyDateStarted=false;
        duration=-1;
        minWait=-1;
        maxWait=-1;
        minPlayers=-1;
        playerMask="";
        runLevel=-1;
        vars.clear();
        
        String line=null;
        Vector parsedLine=null;
        String cmd=null;
        String var=null;
        String val=null;
        for(int v=0;v<script.size();v++)
        {
            line=(String)script.elementAt(v);
            parsedLine=CMParms.parse(line);
            if(parsedLine.size()>0)
            {
                cmd=((String)parsedLine.elementAt(0)).toUpperCase();
                if((cmd.equals("SET"))&&(parsedLine.size()>1))
                {
                    var=((String)parsedLine.elementAt(1)).toUpperCase();
                    val=CMParms.combine(parsedLine,2);
                    setStat(var,val);
                }
            }
        }
    }

    public Vector sortSelect(Environmental E, String str,
                             Vector choices,
                             Vector choices0,
                             Vector choices1,
                             Vector choices2,
                             Vector choices3)
    {
        String mname=E.name().toUpperCase();
        String mdisp=E.displayText().toUpperCase();
        String mdesc=E.description().toUpperCase();
        if(str.equalsIgnoreCase("any"))
        {
            choices=choices0;
            choices0.addElement(E);
        }
        else
        if(mname.equalsIgnoreCase(str))
        {
            choices=choices0;
            choices0.addElement(E);
        }
        else
        if(CMLib.english().containsString(mname,str))
        {
            if((choices==null)||(choices==choices2)||(choices==choices3))
                choices=choices1;
            choices1.addElement(E);
        }
        else
        if(CMLib.english().containsString(mdisp,str))
        {
            if((choices==null)||(choices==choices3))
                choices=choices2;
            choices2.addElement(E);
        }
        else
        if(CMLib.english().containsString(mdesc,str))
        {
            if(choices==null) choices=choices3;
            choices3.addElement(E);
        }
        return choices;
    }
    
    
    // this will execute the quest script.  If the quest is running, it
    // will call stopQuest first to shut it down.
    public void startQuest()
    {
        if(running()) stopQuest();
        Vector script=parseScripts(script());
        stuff.clear();
        QuestState q=new QuestState();
        for(int v=0;v<script.size();v++)
        {
            String s=(String)script.elementAt(v);
            Vector p=CMParms.parse(s);
            boolean isQuiet=q.beQuiet;
            if(p.size()>0)
            {
                String cmd=((String)p.elementAt(0)).toUpperCase();
                if(cmd.equals("<SCRIPT>"))
                {
                    StringBuffer jscript=new StringBuffer("");
                    while(((++v)<script.size())
                    &&(!((String)script.elementAt(v)).trim().toUpperCase().startsWith("</SCRIPT>")))
                        jscript.append(((String)script.elementAt(v))+"\n");
                    if(v>=script.size())
                    {
                        Log.errOut("Quests","Quest '"+name()+"', <SCRIPT> command without </SCRIPT> found.");
                        q.error=true; 
                        break;
                    }
                    if(!CMSecurity.isApprovedJScript(jscript))
                    {
                        Log.errOut("Quests","Quest '"+name()+"', <SCRIPT> not approved.  Use MODIFY JSCRIPT to approve.");
                        q.error=true; 
                        break;
                    }
                    Context cx = Context.enter();
                    try
                    {
                        JScriptQuest scope = new JScriptQuest(this,q);
                        cx.initStandardObjects(scope);
                        scope.defineFunctionProperties(JScriptQuest.functions, 
                                                       JScriptQuest.class,
                                                       ScriptableObject.DONTENUM);
                        cx.evaluateString(scope, jscript.toString(),"<cmd>", 1, null);
                    }
                    catch(Exception e)
                    {
                        if(e!=null)
                            Log.errOut("Quests","Quest '"+name()+"', JScript q.error: "+e.getMessage()+".");
                        else
                            Log.errOut("Quests","Quest '"+name()+"', Unknown JScript q.error.");
                        q.error=true; 
                        Context.exit();
                        break;
                    }
                    Context.exit();
                    continue;
                }
                if(cmd.equals("QUIET"))
                {
                    if(p.size()<2)
                    {
                        q.beQuiet=true;
                        continue;
                    }
                    isQuiet=true;
                    p.removeElementAt(0);
                    cmd=((String)p.elementAt(0)).toUpperCase();
                }
                if(cmd.equals("RESET"))
                {
                    if((q.area==null)&&(q.room==null))
                    {
                        Log.errOut("Quests","Quest '"+name()+"', no resettable room or area set.");
                        q.error=true; 
                        break;
                    }
                    if(q.room==null)
                        CMLib.map().resetArea(q.area);
                    else
                        CMLib.map().resetRoom(q.room);
                }
                else
                if(cmd.equals("SET"))
                {
                    if(p.size()<2)
                    {
                        if(!isQuiet)
                            Log.errOut("Quests","Quest '"+name()+"', unfound variable on set.");
                        q.error=true; break;
                    }
                    cmd=((String)p.elementAt(1)).toUpperCase();
                    if(cmd.equals("AREA"))
                    {
                        q.area=null;
                        if(p.size()<3) continue;
                        Vector names=new Vector();
                        Vector areas=new Vector();
                        if((p.size()>3)&&(((String)p.elementAt(2)).equalsIgnoreCase("any")))
                            for(int ip=3;ip<p.size();ip++)
                                names.addElement(p.elementAt(ip));
                        else
                            names.addElement(CMParms.combine(p,2));
                        for(int n=0;n<names.size();n++)
                        {
                            String areaName=(String)names.elementAt(n);
                            int oldSize=areas.size();
                            if(areaName.equalsIgnoreCase("any"))
                                areas.addElement(CMLib.map().getRandomArea());
                            if(oldSize==areas.size())
                            for (Enumeration e = CMLib.map().areas(); e.hasMoreElements(); )
                            {
                                Area A2 = (Area) e.nextElement();
                                if (A2.Name().equalsIgnoreCase(areaName))
                                {
                                    areas.addElement(A2);
                                    break;
                                }
                            }
                            if(oldSize==areas.size())
                            for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
                            {
                                Area A2=(Area)e.nextElement();
                                if(CMLib.english().containsString(A2.Name(),areaName))
                                {
                                    areas.addElement(A2);
                                    break;
                                }
                            }
                        }
                        if(areas.size()>0)
                            q.area=(Area)areas.elementAt(CMLib.dice().roll(1,areas.size(),-1));
                        if(q.area==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', unknown area '"+CMParms.combine(p,2)+"'.");
                            q.error=true; break;
                        }
                    }
                    else
                    if(cmd.equals("MOBTYPE"))
                    {
                        q.mob=null;
                        if(p.size()<3) continue;
                        Vector choices=new Vector();
                        Vector mobTypes=CMParms.parse(CMParms.combine(p,2).toUpperCase());
                        for(int t=0;t<mobTypes.size();t++)
                        {
                            String mobType=(String)mobTypes.elementAt(t);
                            if(mobType.startsWith("-")) continue;
                            if(q.mobGroup==null)
                            {
                                try
                                {
                                    Enumeration e=CMLib.map().rooms();
                                    if(q.area!=null) e=q.area.getMetroMap();
                                    for(;e.hasMoreElements();)
                                    {
                                        Room R2=(Room)e.nextElement();
                                        for(int i=0;i<R2.numInhabitants();i++)
                                        {
                                            MOB M2=R2.fetchInhabitant(i);
                                            if((M2!=null)&&(M2.isMonster()))
                                            {
                                                if(mobType.equalsIgnoreCase("any"))
                                                    choices.addElement(M2);
                                                else
                                                if((CMClass.className(M2).toUpperCase().indexOf(mobType)>=0)
                                                ||(M2.charStats().getMyRace().racialCategory().toUpperCase().indexOf(mobType)>=0)
                                                ||(M2.charStats().getMyRace().name().toUpperCase().indexOf(mobType)>=0)
                                                ||(M2.charStats().getCurrentClass().name(M2.charStats().getCurrentClassLevel()).toUpperCase().indexOf(mobType)>=0))
                                                    choices.addElement(M2);
                                            }
                                        }
                                    }
                                }catch(NoSuchElementException e){}
                            }
                            else
                            {
                                try
                                {
                                    for(Enumeration e=q.mobGroup.elements();e.hasMoreElements();)
                                    {
                                        MOB M2=(MOB)e.nextElement();
                                        if((M2!=null)&&(M2.isMonster()))
                                        {
                                            if(mobType.equalsIgnoreCase("any"))
                                                choices.addElement(M2);
                                            else
                                            if((CMClass.className(M2).toUpperCase().indexOf(mobType)>=0)
                                            ||(M2.charStats().getMyRace().racialCategory().toUpperCase().indexOf(mobType)>=0)
                                            ||(M2.charStats().getMyRace().name().toUpperCase().indexOf(mobType)>=0)
                                            ||(M2.charStats().getCurrentClass().name(M2.charStats().getCurrentClassLevel()).toUpperCase().indexOf(mobType)>=0))
                                                choices.addElement(M2);
                                        }
                                    }
                                }catch(NoSuchElementException e){}
                            }
                        }
                        if(choices!=null)
                        for(int t=0;t<mobTypes.size();t++)
                        {
                            String mobType=(String)mobTypes.elementAt(t);
                            if(!mobType.startsWith("-")) continue;
                            mobType=mobType.substring(1);
                            for(int i=choices.size()-1;i>=0;i--)
                            {
                                MOB M2=(MOB)choices.elementAt(i);
                                if((M2!=null)&&(M2.isMonster()))
                                {
                                    if((CMClass.className(M2).toUpperCase().indexOf(mobType)>=0)
                                    ||(M2.charStats().getMyRace().racialCategory().toUpperCase().indexOf(mobType)>=0)
                                    ||(M2.charStats().getMyRace().name().toUpperCase().indexOf(mobType)>=0)
                                    ||(M2.charStats().getCurrentClass().name(M2.charStats().getCurrentClassLevel()).toUpperCase().indexOf(mobType)>=0)
                                    ||(M2.name().toUpperCase().indexOf(mobType)>=0)
                                    ||(M2.displayText().toUpperCase().indexOf(mobType)>=0))
                                        choices.removeElement(M2);
                                }
                            }
                        }
                        if((choices!=null)&&(choices.size()>0))
                        {
                            for(int c=choices.size()-1;c>=0;c--)
                                if(CMLib.quests().objectInUse((Environmental)choices.elementAt(c))!=null)
                                    choices.removeElementAt(c);
                            if((choices.size()==0)&&(!isQuiet))
                                Log.errOut("Quests","Quest '"+name()+"', all choices were taken: '"+p+"'.");
                        }
                        if((choices!=null)&&(choices.size()>0))
                            q.mob=(MOB)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
                        if(q.mob==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', !mob '"+p+"'.");
                            q.error=true; break;
                        }
                        if(q.room!=null)
                            q.room.bringMobHere(q.mob,false);
                        else
                            q.room=q.mob.location();
                        q.area=q.room.getArea();
                        q.envObject=q.mob;
                        runtimeRegisterObject(q.mob);
                        q.room.recoverRoomStats();
                        q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
                    }
                    else
                    if(cmd.equals("MOBGROUP"))
                    {
                        q.mobGroup=null;
                        if(p.size()<3) continue;
                        Vector choices=null;
                        Vector choices0=new Vector();
                        Vector choices1=new Vector();
                        Vector choices2=new Vector();
                        Vector choices3=new Vector();
                        String mobName=CMParms.combine(p,2).toUpperCase();
                        String mask="";
                        int x=s.lastIndexOf("MASK=");
                        if(x>=0)
                        {
                            mask=s.substring(x+5).trim();
                            mobName=CMParms.combine(CMParms.parse(s.substring(0,x).trim()),2).toUpperCase();
                        }
                        if(mobName.length()==0) mobName="ANY";
                        try
                        {
                            Enumeration e=CMLib.map().rooms();
                            if(q.area!=null) e=q.area.getMetroMap();
                            for(;e.hasMoreElements();)
                            {
                                Room R2=(Room)e.nextElement();
                                for(int i=0;i<R2.numInhabitants();i++)
                                {
                                    MOB M2=R2.fetchInhabitant(i);
                                    if((M2!=null)&&(M2.isMonster()))
                                    {
                                        if(!CMLib.masking().maskCheck(mask,M2))
                                            continue;
                                        choices=sortSelect(M2,mobName,choices,choices0,choices1,choices2,choices3);
                                    }
                                }
                            }
                        }catch(NoSuchElementException e){}
                        
                        if((choices!=null)&&(choices.size()>0))
                        {
                            for(int c=choices.size()-1;c>=0;c--)
                                if(CMLib.quests().objectInUse((Environmental)choices.elementAt(c))!=null)
                                    choices.removeElementAt(c);
                            if((choices.size()==0)&&(!isQuiet))
                                Log.errOut("Quests","Quest '"+name()+"', all choices were taken: '"+p+"'.");
                        }
                        if((choices!=null)&&(choices.size()>0))
                            q.mobGroup=choices;
                        else
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', !mobgroup '"+mobName+"'.");
                            q.error=true; break;
                        }
                    }
                    else
                    if(cmd.equals("ITEMTYPE"))
                    {
                        q.item=null;
                        if(p.size()<3) continue;
                        Vector choices=new Vector();
                        Vector itemTypes=new Vector();
                        for(int i=2;i<p.size();i++)
                            itemTypes.addElement(p.elementAt(i));
                        for(int t=0;t<itemTypes.size();t++)
                        {
                            String itemType=((String)itemTypes.elementAt(t)).toUpperCase();
                            if(itemType.startsWith("-")) continue;
                            try
                            {
                                Enumeration e=CMLib.map().rooms();
                                if(q.area!=null) e=q.area.getMetroMap();
                                for(;e.hasMoreElements();)
                                {
                                    Room R2=(Room)e.nextElement();
                                    for(int i=0;i<R2.numItems();i++)
                                    {
                                        Item I2=R2.fetchItem(i);
                                        if((I2!=null))
                                        {
                                            if(itemType.equalsIgnoreCase("any"))
                                                choices.addElement(I2);
                                            else
                                            if(CMClass.className(I2).toUpperCase().indexOf(itemType)>=0)
                                                choices.addElement(I2);
                                        }
                                    }
                                }
                            }catch(NoSuchElementException e){}
                        }
                        if(choices!=null)
                        for(int t=0;t<itemTypes.size();t++)
                        {
                            String itemType=(String)itemTypes.elementAt(t);
                            if(!itemType.startsWith("-")) continue;
                            itemType=itemType.substring(1);
                            for(int i=choices.size()-1;i>=0;i--)
                            {
                                Item I2=(Item)choices.elementAt(i);
                                if((CMClass.className(I2).toUpperCase().indexOf(itemType)>=0)
                                ||(I2.name().toUpperCase().indexOf(itemType)>=0)
                                ||(I2.displayText().toUpperCase().indexOf(itemType)>=0))
                                    choices.removeElement(I2);
                            }
                        }
                        if((choices!=null)&&(choices.size()>0))
                        {
                            for(int c=choices.size()-1;c>=0;c--)
                                if(CMLib.quests().objectInUse((Environmental)choices.elementAt(c))!=null)
                                    choices.removeElementAt(c);
                            if((choices.size()==0)&&(!isQuiet))
                                Log.errOut("Quests","Quest '"+name()+"', all choices were taken: '"+p+"'.");
                        }
                        if((choices!=null)&&(choices.size()>0))
                            q.item=(Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
                        if(q.item==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', !item '"+p+"'.");
                            q.error=true; break;
                        }
                        if(q.room!=null)
                            q.room.bringItemHere(q.item,-1,true);
                        else
                        if(q.item.owner() instanceof Room)
                            q.room=(Room)q.item.owner();
                        q.area=q.room.getArea();
                        q.envObject=q.item;
                        q.room.recoverRoomStats();
                        q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
                    }
                    else
                    if(cmd.equals("LOCALE"))
                    {
                        q.room=null;
                        if(p.size()<3) continue;
                        Vector names=new Vector();
                        if((p.size()>3)&&(((String)p.elementAt(2)).equalsIgnoreCase("any")))
                            for(int ip=3;ip<p.size();ip++)
                                names.addElement(p.elementAt(ip));
                        else
                            names.addElement(CMParms.combine(p,2));
                        Vector choices=new Vector();
                        for(int n=0;n<names.size();n++)
                        {
                            String localeName=((String)names.elementAt(n)).toUpperCase();
                            try
                            {
                                Enumeration e=CMLib.map().rooms();
                                if(q.area!=null) e=q.area.getMetroMap();
                                for(;e.hasMoreElements();)
                                {
                                    Room R2=(Room)e.nextElement();
                                    if(localeName.equalsIgnoreCase("any"))
                                        choices.addElement(R2);
                                    else
                                    if(CMClass.className(R2).toUpperCase().indexOf(localeName)>=0)
                                        choices.addElement(R2);
                                    else
                                    {
                                        int dom=R2.domainType();
                                        if((dom&Room.INDOORS)>0)
                                        {
                                            if(Room.indoorDomainDescs[dom-Room.INDOORS].indexOf(localeName)>=0)
                                                choices.addElement(R2);
                                        }
                                        else
                                        if(Room.outdoorDomainDescs[dom].indexOf(localeName)>=0)
                                            choices.addElement(R2);
                                    }
                                }
                            }catch(NoSuchElementException e){}
                        }
                        if((choices!=null)&&(choices.size()>0))
                            q.room=(Room)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
                        if(q.room==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', !locale '"+CMParms.combine(p,2)+"'.");
                            q.error=true; break;
                        }
                        q.area=q.room.getArea();
                    }
                    else
                    if(cmd.equals("ROOM"))
                    {
                        q.room=null;
                        if(p.size()<3) continue;
                        Vector choices=null;
                        Vector choices0=new Vector();
                        Vector choices1=new Vector();
                        Vector choices2=new Vector();
                        Vector choices3=new Vector();
                        Vector names=new Vector();
                        if((p.size()>3)&&(((String)p.elementAt(2)).equalsIgnoreCase("any")))
                            for(int ip=3;ip<p.size();ip++)
                                names.addElement(p.elementAt(ip));
                        else
                            names.addElement(CMParms.combine(p,2));
                        for(int n=0;n<names.size();n++)
                        {
                            String localeName=((String)names.elementAt(n)).toUpperCase();
                            try
                            {
                                Enumeration e=CMLib.map().rooms();
                                if(q.area!=null) e=q.area.getMetroMap();
                                for(;e.hasMoreElements();)
                                {
                                    Room R2=(Room)e.nextElement();
                                    String display=R2.displayText().toUpperCase();
                                    String desc=R2.description().toUpperCase();
                                    if(localeName.equalsIgnoreCase("any"))
                                    {
                                        choices=choices0;
                                        choices0.addElement(R2);
                                    }
                                    else
                                    if(CMLib.map().getExtendedRoomID(R2).equalsIgnoreCase(localeName))
                                    {
                                        choices=choices0;
                                        choices0.addElement(R2);
                                    }
                                    else
                                    if(display.equalsIgnoreCase(localeName))
                                    {
                                        if((choices==null)||(choices==choices2)||(choices==choices3))
                                            choices=choices1;
                                        choices1.addElement(R2);
                                    }
                                    else
                                    if(CMLib.english().containsString(display,localeName))
                                    {
                                        if((choices==null)||(choices==choices3))
                                            choices=choices2;
                                        choices2.addElement(R2);
                                    }
                                    else
                                    if(CMLib.english().containsString(desc,localeName))
                                    {
                                        if(choices==null) choices=choices3;
                                        choices3.addElement(R2);
                                    }
                                }
                            }catch(NoSuchElementException e){}
                        }
                        if((choices!=null)&&(choices.size()>0))
                            q.room=(Room)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
                        if(q.room==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', !locale '"+CMParms.combine(p,2)+"'.");
                            q.error=true; break;
                        }
                        q.area=q.room.getArea();
                    }
                    else
                    if(cmd.equals("MOB"))
                    {
                        q.mob=null;
                        if(p.size()<3) continue;
                        Vector choices=null;
                        Vector choices0=new Vector();
                        Vector choices1=new Vector();
                        Vector choices2=new Vector();
                        Vector choices3=new Vector();
                        String mobName=CMParms.combine(p,2).toUpperCase();
                        String mask="";
                        int x=s.lastIndexOf("MASK=");
                        if(x>=0)
                        {
                            mask=s.substring(x+5).trim();
                            mobName=CMParms.combine(CMParms.parse(s.substring(0,x).trim()),2).toUpperCase();
                        }
                        if(mobName.length()==0) mobName="ANY";
                        if(q.mobGroup!=null)
                        {
                            for(Enumeration e=q.mobGroup.elements();e.hasMoreElements();)
                            {
                                MOB M2=(MOB)e.nextElement();
                                if((M2!=null)&&(M2.isMonster()))
                                {
                                    if(!CMLib.masking().maskCheck(mask,M2))
                                        continue;
                                    choices=sortSelect(M2,mobName,choices,choices0,choices1,choices2,choices3);
                                }
                            }
                        }
                        else
                        {
                            try
                            {
                                Enumeration e=CMLib.map().rooms();
                                if(q.area!=null) e=q.area.getMetroMap();
                                for(;e.hasMoreElements();)
                                {
                                    Room R2=(Room)e.nextElement();
                                    for(int i=0;i<R2.numInhabitants();i++)
                                    {
                                        MOB M2=R2.fetchInhabitant(i);
                                        if((M2!=null)&&(M2.isMonster()))
                                        {
                                            if(!CMLib.masking().maskCheck(mask,M2))
                                                continue;
                                            choices=sortSelect(M2,mobName,choices,choices0,choices1,choices2,choices3);
                                        }
                                    }
                                }
                            }catch(NoSuchElementException e){}
                        }
                        if((choices!=null)&&(choices.size()>0))
                        {
                            for(int c=choices.size()-1;c>=0;c--)
                                if(CMLib.quests().objectInUse((Environmental)choices.elementAt(c))!=null)
                                    choices.removeElementAt(c);
                            if((choices.size()==0)&&(!isQuiet))
                                Log.errOut("Quests","Quest '"+name()+"', all choices were taken: '"+p+"'.");
                        }
                        if((choices!=null)&&(choices.size()>0))
                            q.mob=(MOB)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
                        if(q.mob==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', !mob '"+mobName+"'.");
                            q.error=true; break;
                        }
                        if(q.room!=null)
                            q.room.bringMobHere(q.mob,false);
                        else
                            q.room=q.mob.location();
                        q.area=q.room.getArea();
                        q.envObject=q.mob;
                        runtimeRegisterObject(q.mob);
                        q.room.recoverRoomStats();
                        q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
                    }
                    else
                    if(cmd.equals("ITEM"))
                    {
                        q.item=null;
                        if(p.size()<3) continue;
                        Vector choices=null;
                        Vector choices0=new Vector();
                        Vector choices1=new Vector();
                        Vector choices2=new Vector();
                        Vector choices3=new Vector();
                        String itemName=CMParms.combine(p,2).toUpperCase();
                        try
                        {
                            Enumeration e=CMLib.map().rooms();
                            if(q.area!=null) e=q.area.getMetroMap();
                            for(;e.hasMoreElements();)
                            {
                                Room R2=(Room)e.nextElement();
                                for(int i=0;i<R2.numItems();i++)
                                {
                                    Item I2=R2.fetchItem(i);
                                    if(I2!=null)
                                        choices=sortSelect(I2,itemName,choices,choices0,choices1,choices2,choices3);
                                }
                            }
                        }catch(NoSuchElementException e){}
                        if((choices!=null)&&(choices.size()>0))
                        {
                            for(int c=choices.size()-1;c>=0;c--)
                                if(CMLib.quests().objectInUse((Environmental)choices.elementAt(c))!=null)
                                    choices.removeElementAt(c);
                            if((choices.size()==0)&&(!isQuiet))
                                Log.errOut("Quests","Quest '"+name()+"', all choices were taken: '"+p+"'.");
                        }
                        if((choices!=null)&&(choices.size()>0))
                            q.item=(Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
                        if(q.item==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', !item '"+itemName+"'.");
                            q.error=true; break;
                        }
                        if(q.room!=null)
                            q.room.bringItemHere(q.item,-1,true);
                        else
                        if(q.item.owner() instanceof Room)
                            q.room=(Room)q.item.owner();
                        q.area=q.room.getArea();
                        q.envObject=q.item;
                        q.room.recoverRoomStats();
                        q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
                    }
                    else
                    if(!isStat(cmd))
                    {
                        if(!isQuiet)
                            Log.errOut("Quests","Quest '"+name()+"', unknown variable '"+cmd+"'.");
                        q.error=true; break;
                    }
                }
                else
                if(cmd.equals("IMPORT"))
                {
                    if(p.size()<2)
                    {
                        if(!isQuiet)
                            Log.errOut("Quests","Quest '"+name()+"', no IMPORT type.");
                        q.error=true; break;
                    }
                    cmd=((String)p.elementAt(1)).toUpperCase();
                    if(cmd.equals("MOBS"))
                    {
                        if(p.size()<3)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', no IMPORT MOBS file.");
                            q.error=true; break;
                        }
                        StringBuffer buf=new CMFile("resources/"+CMParms.combine(p,2),null,true).text();
                        if((buf==null)||((buf!=null)&&(buf.length()<20)))
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Unknown XML file: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
                            q.error=true; break;
                        }
                        if(buf.substring(0,20).indexOf("<MOBS>")<0)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Invalid XML file: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
                            q.error=true; break;
                        }
                        q.loadedMobs=new Vector();
                        String errorStr=CMLib.coffeeMaker().addMOBsFromXML(buf.toString(),q.loadedMobs,null);
                        if(errorStr.length()>0)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Error on import of: '"+CMParms.combine(p,2)+"' for '"+name()+"': "+errorStr+".");
                            q.error=true; break;
                        }
                        if(q.loadedMobs.size()<=0)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","No mobs loaded: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
                            q.error=true; break;
                        }
                    }
                    else
                    if(cmd.equals("ITEMS"))
                    {
                        if(p.size()<3)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', no import filename!");
                            q.error=true; break;
                        }
                        StringBuffer buf=new CMFile("resources/"+CMParms.combine(p,2),null,true).text();
                        if((buf==null)||((buf!=null)&&(buf.length()<20)))
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Unknown XML file: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
                            q.error=true; break;
                        }
                        if(buf.substring(0,20).indexOf("<ITEMS>")<0)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Invalid XML file: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
                            q.error=true; break;
                        }
                        q.loadedItems=new Vector();
                        String errorStr=CMLib.coffeeMaker().addItemsFromXML(buf.toString(),q.loadedItems,null);
                        if(errorStr.length()>0)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Error on import of: '"+CMParms.combine(p,2)+"' for '"+name()+"': "+errorStr+".");
                            q.error=true; break;
                        }
                        if(q.loadedItems.size()<=0)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","No items loaded: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
                            q.error=true; break;
                        }
                    }
                    else
                    {
                        if(!isQuiet)
                            Log.errOut("Quests","Quest '"+name()+"', unknown import type '"+cmd+"'.");
                        q.error=true; break;
                    }
                }
                else
                if(cmd.equals("LOAD"))
                {
                    if(p.size()<2)
                    {
                        if(!isQuiet)
                            Log.errOut("Quests","Quest '"+name()+"', unfound type on load.");
                        q.error=true; break;
                    }
                    cmd=((String)p.elementAt(1)).toUpperCase();
                    if(cmd.equals("MOB"))
                    {
                        if(q.loadedMobs.size()==0)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot load mob, no mobs imported.");
                            q.error=true; break;
                        }
                        if(p.size()<3)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', no mob name to load!");
                            q.error=true; break;
                        }
                        String mobName=CMParms.combine(p,2);
                        String mask="";
                        int x=s.lastIndexOf("MASK=");
                        if(x>=0)
                        {
                            mask=s.substring(x+5).trim();
                            mobName=CMParms.combine(CMParms.parse(s.substring(0,x).trim()),2).toUpperCase();
                        }
                        if(mobName.length()==0) mobName="ANY";
                        Vector choices=new Vector();
                        for(int i=0;i<q.loadedMobs.size();i++)
                        {
                            MOB M2=(MOB)q.loadedMobs.elementAt(i);
                            if(!CMLib.masking().maskCheck(mask,M2))
                                continue;
                            if((mobName.equalsIgnoreCase("any"))
                            ||(CMLib.english().containsString(M2.name(),mobName))
                            ||(CMLib.english().containsString(M2.displayText(),mobName))
                            ||(CMLib.english().containsString(M2.description(),mobName)))
                                choices.addElement(M2.copyOf());
                        }
                        if(choices.size()==0)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', no mob found to load '"+mobName+"'!");
                            q.error=true; break;
                        }
                        q.mob=(MOB)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
                        if(q.room==null)
                        {
                            if(q.area!=null)
                                q.room=q.area.getRandomMetroRoom();
                            else
                                q.room=CMLib.map().getRandomRoom();
                        }
                        if(q.room!=null)
                        {
                            q.mob.setStartRoom(null);
                            q.mob.baseEnvStats().setRejuv(0);
                            q.mob.recoverEnvStats();
                            q.mob.text();
                            q.mob.bringToLife(q.room,true);
                            q.area=q.room.getArea();
                        }
                        q.envObject=q.mob;
                        runtimeRegisterObject(q.mob);
                        q.room.recoverRoomStats();
                        q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
                    }
                    else
                    if(cmd.equals("ITEM"))
                    {
                        if(q.loadedItems.size()==0)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot load item, no items imported.");
                            q.error=true; break;
                        }
                        if(p.size()<3)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', no item name to load!");
                            q.error=true; break;
                        }
                        String itemName=CMParms.combine(p,2);
                        Vector choices=new Vector();
                        for(int i=0;i<q.loadedItems.size();i++)
                        {
                            Item I2=(Item)q.loadedItems.elementAt(i);
                            if((itemName.equalsIgnoreCase("any"))
                            ||(CMLib.english().containsString(I2.name(),itemName))
                            ||(CMLib.english().containsString(I2.displayText(),itemName))
                            ||(CMLib.english().containsString(I2.description(),itemName)))
                                choices.addElement(I2.copyOf());
                        }
                        if(choices.size()==0)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', no item found to load '"+itemName+"'!");
                            q.error=true; break;
                        }
                        q.item=(Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
                        if(q.room==null)
                        {
                            if(q.area!=null)
                                q.room=q.area.getRandomMetroRoom();
                            else
                                q.room=CMLib.map().getRandomRoom();
                        }
                        if(q.room!=null)
                        {
                            q.item.baseEnvStats().setRejuv(0);
                            q.item.recoverEnvStats();
                            q.item.text();
                            q.room.addItem(q.item);
                            q.area=q.room.getArea();
                            q.room.recoverRoomStats();
                            q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
                        }
                        q.envObject=q.item;
                        runtimeRegisterObject(q.item);
                    }
                    else
                    {
                        if(!isQuiet)
                            Log.errOut("Quests","Quest '"+name()+"', unknown load type '"+cmd+"'.");
                        q.error=true; break;
                    }

                }
                else
                if(cmd.equals("GIVE"))
                {
                    if(p.size()<2)
                    {
                        if(!isQuiet)
                            Log.errOut("Quests","Quest '"+name()+"', unfound type on give.");
                        q.error=true; break;
                    }
                    cmd=((String)p.elementAt(1)).toUpperCase();
                    if(cmd.equals("FOLLOWER"))
                    {
                        if(q.mob==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give follower, no mob set.");
                            q.error=true; break;
                        }
                        if(p.size()<3)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give follower, follower name not given.");
                            q.error=true; break;
                        }
                        String mobName=CMParms.combine(p,2);
                        Vector choices=null;
                        for(int i=stuff.size()-1;i>=0;i--)
                        {
                            Environmental E2=(Environmental)stuff.elementAt(i);
                            if((E2!=q.mob)&&(E2 instanceof MOB))
                            {
                                MOB M2=(MOB)E2;
                                if((mobName.equalsIgnoreCase("any"))
                                ||(CMLib.english().containsString(M2.name(),mobName))
                                ||(CMLib.english().containsString(M2.displayText(),mobName))
                                ||(CMLib.english().containsString(M2.description(),mobName)))
                                    choices.addElement(M2);
                            }
                        }
                        if(choices.size()==0)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give follower, no mobs called '"+mobName+"' previously set in script.");
                            q.error=true; break;
                        }
                        MOB M2=(MOB)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
                        M2.setFollowing(q.mob);
                    }
                    else
                    if(cmd.equals("ITEM"))
                    {
                        if(q.item==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give item, no item set.");
                            q.error=true; break;
                        }
                        if((q.mob==null)&&(q.mobGroup==null))
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give item, no mob set.");
                            q.error=true; break;
                        }
                        if(p.size()>2)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give item, parameter unnecessarily given: '"+CMParms.combine(p,2)+"'.");
                            q.error=true; break;
                        }
                        Vector toSet=new Vector();
                        if(q.mob!=null) 
                            toSet.addElement(q.mob);
                        else
                        if(q.mobGroup!=null) 
                            toSet=q.mobGroup;
                        for(int i=0;i<toSet.size();i++)
                        {
                            MOB M2=(MOB)toSet.elementAt(i);
                            runtimeRegisterObject(M2);
                            M2.giveItem(q.item);
                            q.item=(Item)q.item.copyOf();
                        }
                    }
                    else
                    if(cmd.equals("ABILITY"))
                    {
                        if((q.mob==null)&&(q.mobGroup==null))
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give ability, no mob set.");
                            q.error=true; break;
                        }
                        if(p.size()<3)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give ability, ability name not given.");
                            q.error=true; break;
                        }
                        Ability A3=CMClass.findAbility((String)p.elementAt(2));
                        if(A3==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give ability, ability name unknown '"+((String)p.elementAt(2))+".");
                            q.error=true; break;
                        }
                        Vector toSet=new Vector();
                        if(q.mob!=null) 
                            toSet.addElement(q.mob);
                        else
                        if(q.mobGroup!=null) 
                            toSet=q.mobGroup;
                        for(int i=0;i<toSet.size();i++)
                        {
                            MOB M2=(MOB)toSet.elementAt(i);
                            runtimeRegisterAbility(M2,A3.ID(),CMParms.combineWithQuotes(p,3));
                        }
                    }
                    else
                    if(cmd.equals("BEHAVIOR"))
                    {
                        if((q.envObject==null)
                        &&(q.mobGroup==null))
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give behavior, no mob or item set.");
                            q.error=true; break;
                        }
                        if(p.size()<3)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give behavior, behavior name not given.");
                            q.error=true; break;
                        }
                        Behavior B=CMClass.getBehavior((String)p.elementAt(2));
                        if(B==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give behavior, behavior name unknown '"+((String)p.elementAt(2))+".");
                            q.error=true; break;
                        }
                        Vector toSet=new Vector();
                        if((q.mobGroup!=null)&&(q.mob==null)) 
                            toSet=q.mobGroup;
                        else
                        if(q.envObject!=null) 
                            toSet.addElement(q.envObject);
                        else
                        if(q.mobGroup!=null) 
                            toSet=q.mobGroup;
                        for(int i=0;i<toSet.size();i++)
                        {
                            Environmental E2=(Environmental)toSet.elementAt(i);
                            runtimeRegisterBehavior(E2,B.ID(),CMParms.combineWithQuotes(p,3));
                        }
                    }
                    else
                    if(cmd.equals("AFFECT"))
                    {
                        if((q.envObject==null)&&(q.mobGroup==null))
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give Effect, no mob or item set.");
                            q.error=true; break;
                        }
                        if(p.size()<3)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give Effect, ability name not given.");
                            q.error=true; break;
                        }
                        Ability A3=CMClass.findAbility((String)p.elementAt(2));
                        if(A3==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', cannot give Effect, ability name unknown '"+((String)p.elementAt(2))+".");
                            q.error=true; break;
                        }
                        Vector toSet=new Vector();
                        if((q.mobGroup!=null)&&(q.mob==null)) 
                            toSet=q.mobGroup;
                        else
                        if(q.envObject!=null) 
                            toSet.addElement(q.envObject);
                        else
                        if(q.mobGroup!=null) 
                            toSet=q.mobGroup;
                        for(int i=0;i<toSet.size();i++)
                        {
                            Environmental E2=(Environmental)toSet.elementAt(i);
                            runtimeRegisterEffect(E2,A3.ID(),CMParms.combineWithQuotes(p,3));
                        }
                    }
                    else
                    {
                        if(!isQuiet)
                            Log.errOut("Quests","Quest '"+name()+"', unknown give type '"+cmd+"'.");
                        q.error=true; break;
                    }
                }
                else
                {
                    if(!isQuiet)
                        Log.errOut("Quests","Quest '"+name()+"', unknown command '"+cmd+"'.");
                    q.error=true; break;
                }
                q.done=true;
            }
        }
        if(q.error)
        {
            if(!q.beQuiet)
                Log.errOut("Quests","One or more errors in '"+name()+"', quest not started");
        }
        else
        if(!q.done)
            Log.errOut("Quests","Nothing parsed in '"+name()+"', quest not started");
        else
        if(duration()<0)
            Log.errOut("Quests","No duration, quest '"+name()+"' not started.");

        if((!q.error)&&(q.done)&&(duration()>=0))
        {
            waitRemaining=-1;
            ticksRemaining=duration();
            CMLib.threads().startTickDown(this,Tickable.TICKID_QUEST,1);
        }
    }
    
    // this will stop executing of the quest script.  It will clean up
    // any objects or mobs which may have been loaded, restoring map
    // mobs to their previous state.
    public void stopQuest()
    {
        if(stoppingQuest) return;
        stoppingQuest=true;
        if(stuff.size()>0)
        {
            for(int i=0;i<stuff.size();i++)
            {
                Environmental E=(Environmental)stuff.elementAt(i);
                if(E instanceof Item)
                    ((Item)E).destroy();
                else
                if(E instanceof MOB)
                {
                    MOB M=(MOB)E;
                    ScriptingEngine B=(ScriptingEngine)((MOB)E).fetchBehavior("Scriptable");
                    if(B!=null) B.endQuest(E,M,name());
                    CMLib.tracking().wanderAway(M,true,false);
                    Room R=M.getStartRoom();
                    if(R!=null)
                    {
                        if(M.location()!=null)
                            M.location().delInhabitant(M);
                        M.setLocation(null);
                        M.destroy();
                        CMLib.map().resetRoom(R);
                    }
                    else
                    {
                        if(M.location()!=null)
                            M.location().delInhabitant(M);
                        M.setLocation(null);
                        M.destroy();
                    }
                }
            }
            stuff.clear();
        }
        if(addons.size()>0)
        {
            for(int i=0;i<addons.size();i++)
            {
                Vector V=(Vector)addons.elementAt(i);
                if(V.size()<2) continue;
                Environmental E=(Environmental)V.elementAt(0);
                Object O=V.elementAt(1);
                if(O instanceof Behavior)
                {
                    Behavior B=E.fetchBehavior(((Behavior)O).ID());
                    if(B==null) continue;
                    if((E instanceof MOB)&&(B instanceof ScriptingEngine))
                        ((ScriptingEngine)B).endQuest(E,(MOB)E,name());
                    if((V.size()>2)&&(V.elementAt(2) instanceof String))
                        B.setParms((String)V.elementAt(2));
                    else
                        E.delBehavior(B);
                }
                else
                if(O instanceof Ability)
                {
                    if((V.size()>2)
                    &&(V.elementAt(2) instanceof Ability)
                    &&(E instanceof MOB))
                    {
                        Ability A=((MOB)E).fetchAbility(((Ability)O).ID());
                        if(A==null) continue;
                        if((V.size()>3)&&(V.elementAt(3) instanceof String))
                            A.setMiscText((String)V.elementAt(3));
                        else
                            ((MOB)E).delAbility(A);
                    }
                    else
                    {
                        Ability A=E.fetchEffect(((Ability)O).ID());
                        if(A==null) continue;
                        if((V.size()>2)&&(V.elementAt(2) instanceof String))
                            A.setMiscText((String)V.elementAt(2));
                        else
                        {
                            A.unInvoke();
                            E.delEffect(A);
                        }
                    }
                }
                else
                if(O instanceof Item)
                    ((Item)O).destroy();
            }
            addons.clear();
        }
        if(running())
        {
            ticksRemaining=-1;
            if(!setWaitRemaining())
                CMLib.threads().deleteTick(this,Tickable.TICKID_QUEST);
        }
        stoppingQuest=false;
    }
    
    public boolean setWaitRemaining()
    {
        if(((minWait()<0)||(maxWait<0))
        &&(startDate().trim().length()==0))
            return false;
        if(running()) return true;
        if(startDate().length()>0)
        {
            if(startDate().toUpperCase().startsWith("MUDDAY"))
            {
                String sd2=startDate().substring("MUDDAY".length()).trim();
                int x=sd2.indexOf("-");
                if(x<0) return false;
                int mudmonth=CMath.s_int(sd2.substring(0,x));
                int mudday=CMath.s_int(sd2.substring(x+1));
                TimeClock C=(TimeClock)CMClass.getCommon("DefaultTimeClock");
                TimeClock NOW=CMClass.globalClock();
                C.setMonth(mudmonth);
                C.setDayOfMonth(mudday);
                C.setTimeOfDay(0);
                if((mudmonth<NOW.getMonth())
                ||((mudmonth==NOW.getMonth())&&(mudday<NOW.getDayOfMonth())))
                    C.setYear(NOW.getYear()+1);
                else
                    C.setYear(NOW.getYear());
                long distance=C.deriveMillisAfter(NOW);
                waitRemaining=(int)(distance/Tickable.TIME_TICK);
            }
            else
            {
                int x=startDate.indexOf("-");
                if(x<0) return false;
                int month=CMath.s_int(startDate.substring(0,x));
                int day=CMath.s_int(startDate.substring(x+1));
                int year=Calendar.getInstance().get(Calendar.YEAR);
                long distance=CMLib.time().string2Millis(month+"/"+day+"/"+year+" 12:00 AM");
                while(distance<System.currentTimeMillis())
                    distance=CMLib.time().string2Millis(month+"/"+day+"/"+(++year)+" 12:00 AM");
                waitRemaining=(int)((distance-System.currentTimeMillis())/Tickable.TIME_TICK);
            }
        }
        else
            waitRemaining=minWait+(CMLib.dice().roll(1,maxWait,0));
        return true;
    }

    public int minWait(){return minWait;}
    public void setMinWait(int wait){minWait=wait;}
    public int waitInterval(){return maxWait;}
    public void setWaitInterval(int wait){maxWait=wait;}
    public int waitRemaining(){return waitRemaining;}

    // if the quest has a winner, this is him.
    public void declareWinner(String name)
    {
        name=name.trim();
        if(name.length()==0)
            return;
        if(!wasWinner(name))
        {
            getWinners().addElement(name);
            CMLib.database().DBUpdateQuest(this);
        }
    }
    public String getWinnerStr()
    {
        StringBuffer list=new StringBuffer("");
        Vector V=getWinners();
        for(int i=0;i<V.size();i++)
            list.append(((String)V.elementAt(i))+";");
        return list.toString();
    }
    public void setWinners(String list)
    {
        Vector V=getWinners();
        V.clear();
        list=list.trim();
        int x=list.indexOf(";");
        while(x>0)
        {
            String s=list.substring(0,x).trim();
            list=list.substring(x+1).trim();
            if(s.length()>0)
                V.addElement(s);
            x=list.indexOf(";");
        }
        if(list.trim().length()>0)
            V.addElement(list.trim());
    }
    // retreive the list of previous winners
    public Vector getWinners()
    {
        return winners;
    }
    // was a previous winner
    public boolean wasWinner(String name)
    {
        Vector V=getWinners();
        for(int i=0;i<V.size();i++)
        {
            if(((String)V.elementAt(i)).equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    // informational
    public boolean running(){return ticksRemaining>=0;}
    public boolean stopping(){return stoppingQuest;}
    public boolean waiting(){return waitRemaining>=0;}
    public int ticksRemaining(){return ticksRemaining;}
    public int minsRemaining(){return new Long(ticksRemaining*Tickable.TIME_TICK/60000).intValue();}
    private long tickStatus=Tickable.STATUS_NOT;
    public long getTickStatus(){return tickStatus;}
    public boolean tick(Tickable ticking, int tickID)
    {
        if(tickID!=Tickable.TICKID_QUEST)
            return false;
        if(CMSecurity.isDisabled("QUESTS")) return true;
        
        tickStatus=Tickable.STATUS_START;
        if(running())
        {
            tickStatus=Tickable.STATUS_ALIVE;
            ticksRemaining--;
            if(ticksRemaining<0)
            {
                stopQuest();
                if(!setWaitRemaining()) 
                    return false;
            }
            tickStatus=Tickable.STATUS_END;
        }
        else
        {
            tickStatus=Tickable.STATUS_DEAD;
            waitRemaining--;
            if(waitRemaining<0)
            {
                ticksRemaining=duration();
                boolean allowedToRun=true;
                if(runLevel()>=0)
            	for(int q=0;q<CMLib.quests().numQuests();q++)
            	{
            		Quest Q=CMLib.quests().fetchQuest(q);
            		if((Q!=this)&&(Q.running())&&(Q.runLevel()<=runLevel()))
            		{ allowedToRun=false; break;}
            	}
                int numElligiblePlayers=CMLib.sessions().size();
                if(playerMask.length()>0)
                {
                	numElligiblePlayers=0;
                	for(int s=CMLib.sessions().size()-1;s>=0;s--)
                	{
                		Session S=CMLib.sessions().elementAt(s);
                		if((S.mob()!=null)&&(CMLib.masking().maskCheck(playerMask,S.mob())))
                			numElligiblePlayers++;
                	}
                }
                if((allowedToRun)&&(numElligiblePlayers>=minPlayers))
	                startQuest();
            }
        }
        tickStatus=Tickable.STATUS_NOT;
        return true;
    }

    public void runtimeRegisterAbility(MOB mob, String abilityID, String parms)
    {
        if(mob==null) return;
        runtimeRegisterObject(mob);
        Vector V=new Vector();
        V.addElement(mob);
        Ability A4=mob.fetchAbility(abilityID);
        if(A4!=null)
        {
            V.addElement(A4);
            V.addElement(A4);
            V.addElement(A4.text());
            A4.setMiscText(parms);
            A4.setProfficiency(100);
        }
        else
        {
            A4=CMClass.getAbility(abilityID);
            if(A4==null) return;
            A4.setMiscText(parms);
            V.addElement(A4);
            V.addElement(A4);
            A4.setProfficiency(100);
            mob.addAbility(A4);
        }
        addons.addElement(V);
    }
    public void runtimeRegisterObject(Environmental object)
    {
        if(!stuff.contains(object))
            stuff.addElement(object);
    }
    public void runtimeRegisterEffect(Environmental affected, String abilityID, String parms)
    {
        if(affected==null) return;
        runtimeRegisterObject(affected);
        Vector V=new Vector();
        V.addElement(affected);
        Ability A4=affected.fetchEffect(abilityID);
        if(A4!=null)
        {
            V.addElement(A4);
            V.addElement(A4.text());
            A4.makeLongLasting();
            A4.setMiscText(parms);
        }
        else
        {
            A4=CMClass.getAbility(abilityID);
            if(A4==null) return;
            V.addElement(A4);
            A4.setMiscText(parms);
            if(affected instanceof MOB)
                A4.startTickDown((MOB)affected,affected,99999);
            else
                A4.startTickDown(null,affected,99999);
            A4.makeLongLasting();
        }
        addons.addElement(V);
    }
    public void runtimeRegisterBehavior(Environmental behaving, String behaviorID, String parms)
    {
        if(behaving==null) return;
        runtimeRegisterObject(behaving);
        Vector V=new Vector();
        V.addElement(behaving);
        Behavior B=behaving.fetchBehavior(behaviorID);
        if(B!=null)
        {
            V.addElement(B);
            V.addElement(B.getParms());
            B.setParms(parms);
        }
        else
        {
            B=CMClass.getBehavior(behaviorID);
            if(B==null) return;
            V.addElement(B);
            B.setParms(parms);
            behaving.addBehavior(B);
        }
        addons.addElement(V);
    }
    public int wasQuestMob(String name)
    {
        int num=1;
        for(int i=0;i<stuff.size();i++)
        {
            Environmental E=(Environmental)stuff.elementAt(i);
            if(E instanceof MOB)
            {
                if(E.name().equalsIgnoreCase(name))
                    return num;
                num++;
            }
        }
        return -1;
    }
    public int wasQuestItem(String name)
    {
        int num=1;
        for(int i=0;i<stuff.size();i++)
        {
            Environmental E=(Environmental)stuff.elementAt(i);
            if(E instanceof Item)
            {
                if(E.name().equalsIgnoreCase(name))
                    return num;
                num++;
            }
        }
        return -1;
    }
    
    public boolean isQuestObject(Environmental E)
    { return ((stuff!=null)&&(stuff.contains(E)));}
    
    public String getQuestObjectName(int i)
    {
        Environmental E=getQuestObject(i);
        if(E!=null) return E.Name();
        return "";
    }
    public Environmental getQuestObject(int i)
    {
        i=i-1; // starts counting at 1
        if((i>=0)&&(i<stuff.size()))
        {
            Environmental E=(Environmental)stuff.elementAt(i);
            return E;
        }
        return null;
    }
    public MOB getQuestMob(int i)
    {
        int num=1;
        for(int x=0;x<stuff.size();x++)
        {
            Environmental E=(Environmental)stuff.elementAt(x);
            if(E instanceof MOB)
            {
                if(num==i) return (MOB)E;
                num++;
            }
        }
        return null;
    }
    public Item getQuestItem(int i)
    {
        int num=1;
        for(int x=0;x<stuff.size();x++)
        {
            Environmental E=(Environmental)stuff.elementAt(x);
            if(E instanceof Item)
            {
                if(num==i) return (Item)E;
                num++;
            }
        }
        return null;
    }
    public String getQuestMobName(int i)
    {
        MOB M=getQuestMob(i);
        if(M!=null) return M.name();
        return "";
    }
    public String getQuestItemName(int i)
    {
        Item I=getQuestItem(i);
        if(I!=null) return I.name();
        return "";
    }
    public int wasQuestObject(String name)
    {
        for(int i=0;i<stuff.size();i++)
        {
            Environmental E=(Environmental)stuff.elementAt(i);
            if(E.name().equalsIgnoreCase(name))
                return (i+1);
        }
        return -1;
    }
    public boolean isQuestObject(String name, int i)
    {
        if((i>=0)&&(i<stuff.size()))
        {
            Environmental E=(Environmental)stuff.elementAt(i);
            if(E.name().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }
    private Vector parseScripts(String text)
    {
        if(text.toUpperCase().startsWith("LOAD="))
        {
            StringBuffer buf=new CMFile("resources/"+text.substring(5),null,true).text();
            if(buf!=null) text=buf.toString();
        }
        Vector script=new Vector();
        while(text.length()>0)
        {
            int y=-1;
            int yy=0;
            while(yy<text.length())
                if((text.charAt(yy)==';')&&((yy<=0)||(text.charAt(yy-1)!='\\'))) {y=yy;break;}
                else
                if(text.charAt(yy)=='\n'){y=yy;break;}
                else
                if(text.charAt(yy)=='\r'){y=yy;break;}
                else yy++;
            String cmd="";
            if(y<0)
            {
                cmd=text.trim();
                text="";
            }
            else
            {
                cmd=text.substring(0,y).trim();
                text=text.substring(y+1).trim();
            }
            if((cmd.length()>0)&&(!cmd.startsWith("#")))
                script.addElement(CMStrings.replaceAll(cmd,"\\;",";"));
        }
        return script;
    }
	public String[] getCodes(){
		String[] CCODES=new String[QCODES.length+MYSTERY_QCODES.length];
		for(int i=0;i<QCODES.length;i++)
			CCODES[i]=QCODES[i];
		for(int i=0;i<MYSTERY_QCODES.length;i++)
			CCODES[QCODES.length+i]=MYSTERY_QCODES[i];
		return QCODES;
	}
	protected int getCodeNum(String code)
	{
		String[] CCODES=getCodes();
		for(int i=0;i<CCODES.length;i++)
			if(code.equalsIgnoreCase(CCODES[i])) return i;
		return -1;
	}
	public boolean sameAs(DefaultQuest E){
		String[] CCODES=getCodes();
		for(int i=0;i<CCODES.length;i++)
			if(!E.getStat(CCODES[i]).equals(getStat(CCODES[i])))
			   return false;
		return true;
	}
	
	private boolean isSpecialQCode(String code)
	{
		code=code.toUpperCase().trim();
		for(int i=0;i<SPECIAL_QCODES.length;i++)
			if(code.equals(SPECIAL_QCODES[i])) return true;
		return false;
	}
	private boolean isMysteryQCode(String code)
	{
		code=code.toUpperCase().trim();
		for(int i=0;i<MYSTERY_QCODES.length;i++)
			if(code.equals(MYSTERY_QCODES[i])) return true;
		return false;
	}
	
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code)){
		case 0: break;
		case 1: setName(val); break;
		case 2: setDuration(CMath.s_int(val)); break;
		case 3: setMinWait(CMath.s_int(val)); break;
		case 4: setMinPlayers(CMath.s_int(val)); break;
		case 5: setPlayerMask(val); break;
		case 6: setRunLevel(CMath.s_int(val)); break;
		case 7: setStartDate(val); break;
		case 8: setStartDate(val); break;
		case 9: setWaitInterval(CMath.s_int(val)); break;
		default:
			if(isMysteryQCode(code))
			{
				int x=vars.indexOf(code.toUpperCase().trim());
				if(x>=0) 
					vars.setElementAt(x,2,val);
				else
					vars.addElement(code.toUpperCase().trim(),val);
			}
			break;
		}
	}
	public boolean isStat(String code)
	{
		if((getCodeNum(code)>=0)
		||(isSpecialQCode(code))
		||(isMysteryQCode(code))) 
			return true;
		return false;
	}
	public String getStat(String code)
	{
		switch(getCodeNum(code)){
		case 0: return ""+ID();
		case 1: return ""+name();
		case 2: return ""+duration();
		case 3: return ""+minWait();
		case 4: return ""+minPlayers();
		case 5: return ""+playerMask();
		case 6: return ""+runLevel();
		case 7: return ""+startDate();
		case 8: return ""+startDate();
		case 9: return ""+waitInterval();
		default: 
			int x=vars.indexOf(code.toUpperCase().trim());
			if(x>=0) return (String)vars.elementAt(x,2);
			return "";
		}
	}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    protected static class JScriptQuest extends ScriptableObject
    {
        public String getClassName(){ return "JScriptQuest";}
        static final long serialVersionUID=44;
        Quest quest=null;
        QuestState state=null;
        public Quest quest(){return quest;}
        public QuestState setupState(){return state;}
        public JScriptQuest(Quest Q, QuestState S){quest=Q; state=S;}
        public static String[] functions = { "quest", "setupState", "toJavaString"};
        public String toJavaString(Object O){return Context.toString(O);}
    }
    protected static class QuestState
    {
        public Vector loadedMobs=new Vector();
        public Vector loadedItems=new Vector();
        public Area area=null;
        public Room room=null;
        public MOB mob=null;
        public Vector mobGroup=null;
        public Item item=null;
        public Environmental envObject=null;
        public boolean error=false;
        public boolean done=false;
        public boolean beQuiet=false;
    }
}
