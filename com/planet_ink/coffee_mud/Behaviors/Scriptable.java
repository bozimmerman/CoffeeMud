package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.exceptions.ScriptParseException;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Scriptable extends StdBehavior implements ScriptingEngine
{
    public String ID(){return "Scriptable";}
    protected int canImproveCode(){return Behavior.CAN_MOBS|Behavior.CAN_ITEMS|Behavior.CAN_ROOMS;}
    
    protected ScriptingEngine engine = null;
    protected ScriptingEngine engine() {
        if(engine==null)
            engine=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
        return engine;
    }

    public long getTickStatus()
    {
        Tickable T=engine();
        if(T!=null) return T.getTickStatus();
        return Tickable.STATUS_NOT;
    }

    public void registerDefaultQuest(String questName){
        engine().registerDefaultQuest(questName);
    }
    
    public MOB getMakeMOB(Tickable ticking){ return engine().getMakeMOB(ticking);}
    
    public boolean endQuest(Environmental hostObj, MOB mob, String quest)
    {
        engine().endQuest(hostObj, mob, quest);
        return false;
    }

    public Vector externalFiles()
    {
        return engine().externalFiles();
    }

    public String getParms() { return engine().getScript();}
    public String[] parseEval(String evaluable) throws ScriptParseException { return engine().parseEval(evaluable);}
    public void setParms(String newParms)
    {
        engine().setScript(newParms);
        super.setParms("");
    }

    public String getVar(String context, String variable){ return engine().getVar(context, variable);}
    
    public boolean isVar(String context, String variable){ return engine().isVar(context, variable);}
    
    public void setVar(String context, String variable, String value){ engine().setVar(context, variable, value);}
    
    public String defaultQuestName() { return engine().defaultQuestName();}
    
    public void setVarScope(String scope){ engine().setVarScope(scope); }
    
    public String getVarScope() { return engine().getVarScope(); }
    
    public String getLocalVarXML(){ return engine().getLocalVarXML(); }
    
    public void setLocalVarXML(String xml){
        if(engine().getVarScope().length()>0)
            engine().setLocalVarXML(xml);
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
        return engine().eval(scripted, source, target, monster, primaryItem, secondaryItem, msg, tmp, eval, startEval);
    }

    public String getScript() { return engine().getScript();}
    
    public void setScript(String newParms){ engine().setScript(newParms);}
    
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
        return engine().execute(scripted, source, target, monster, primaryItem, secondaryItem, script, msg, tmp);
    }

    public void executeMsg(Environmental affecting, CMMsg msg)
    {
        super.executeMsg(affecting,msg);
        engine().executeMsg(affecting, msg);
    }
    
    public boolean okMessage(Environmental affecting, CMMsg msg)
    {
        if(!super.okMessage(affecting, msg))
            return false;
        return engine().okMessage(affecting, msg);
    }

    public boolean tick(Tickable ticking, int tickID)
    {
        super.tick(ticking,tickID);
        if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
            return false;
        return engine().tick(ticking, tickID);
    }

    public void dequeResponses() { engine().dequeResponses();}
}
