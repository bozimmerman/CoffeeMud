package com.planet_ink.coffee_mud.Common;

import java.net.Socket;
import java.util.Vector;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMFile;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;
import com.planet_ink.coffee_mud.core.interfaces.Tickable;

@SuppressWarnings("unchecked")
public class FakeSession implements Session
{
    CMFile theFile=null;
    MOB mob = null;
    Vector inputV = new Vector();
    
    public boolean tick(Tickable ticking, int tickID){return false;}
    public String ID(){return "FakeSession";}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new FakeSession();}}
    public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public long getTickStatus(){return 0;}
    public void initializeSession(Socket s, String introTextStr){ theFile = new CMFile(introTextStr,null,true); }
    public boolean isLockedUpWriting(){return false;}
    public void initializeClass(){}
    public void start(){}
    public String getTerminalType(){ return "Fake";}
    public void negotiateTelnetMode(int code){}
    
    public String[] clookup(){return new String[255];}
    
    public void onlyPrint(String msg, int pageBreak, boolean noCache){
        if(theFile != null) {
            synchronized(theFile)
            {
                theFile.saveText(msg,true);
            }
        }
    }
    public void onlyPrint(String msg){ onlyPrint(msg,0,false); }
    public void rawOut(String msg){ onlyPrint(msg,0,false); }
    public void rawPrintln(String msg){ onlyPrint(msg,0,false); }
    public void rawPrintln(String msg, int pageBreak){ onlyPrint(msg,0,false); }
    public void rawPrint(String msg){ onlyPrint(msg,0,false); }
    public void rawPrint(String msg, int pageBreak){ onlyPrint(msg,0,false); }
    public void stdPrint(String msg){ onlyPrint(msg,0,false); }
    public void stdPrint(Environmental Source, Environmental Target, Environmental Tool, String msg){ onlyPrint(msg,0,false); }
    public void stdPrintln(String msg){ onlyPrint(msg,0,false); }
    public void stdPrintln(Environmental Source, Environmental Target, Environmental Tool, String msg){ onlyPrint(msg,0,false); }
    public void out(char[] c){ onlyPrint(new String(c),0,false); }
    public void print(String msg){ onlyPrint(msg,0,false); }
    public void print(Environmental Source, Environmental Target, Environmental Tool, String msg){ onlyPrint(msg,0,false); }
    public void println(String msg){ onlyPrint(msg,0,false); }
    public void println(Environmental Source, Environmental Target, Environmental Tool, String msg){ onlyPrint(msg,0,false); }
    public void wraplessPrintln(String msg){ onlyPrint(msg,0,false); }
    public void wraplessPrint(String msg){ onlyPrint(msg,0,false); }
    public void colorOnlyPrintln(String msg, int pageBreak, boolean noCache){ onlyPrint(msg,0,false); }
    public void colorOnlyPrint(String msg, int pageBreak, boolean noCache){ onlyPrint(msg,0,false); }
    public void colorOnlyPrintln(String msg){ onlyPrint(msg,0,false); }
    public void colorOnlyPrint(String msg){ onlyPrint(msg,0,false); }
    public void setPromptFlag(boolean truefalse){}
    
    public char hotkey(long maxWait) {return ' ';}
    public String prompt(String Message, String Default) { 
        onlyPrint(Message,0,false);
        String msg  = readlineContinue();
        if(msg.length()==0) return Default;
        return msg;
    }
    public String prompt(String Message, String Default, long maxTime) { return prompt(Message,Default);}
    public String prompt(String Message) { return prompt(Message,"");}
    public String prompt(String Message, long maxTime) { return prompt(Message,"");}
    public boolean confirm(String Message, String Default) {
        if(Default.toUpperCase().startsWith("T")) Default="Y";
        String YN=choose(Message,"YN",Default,0);
        return(YN.equals("Y"))?true:false;
    }
    public boolean confirm(String Message, String Default, long maxTime) { return confirm(Message,Default,0);}
    public String choose(String Message, String Choices, String Default) { 
        onlyPrint(Message,0,false);
        String msg  = readlineContinue();
        if(msg.length()==0) return Default;
        if(Choices.toUpperCase().indexOf(msg.toUpperCase().trim())>=0)
            return msg.toUpperCase().trim();
        return Default;
    }
    public String choose(String Message, String Choices, String Default, long maxTime) { return choose(Message,Choices,Default);}
    public String blockingIn() { return readlineContinue();}
    public String readlineContinue() {
        synchronized(inputV) {
            if(inputV.size()==0) return "";
            String input = (String)inputV.firstElement();
            inputV.removeElementAt(0);
            return input;
        }
    }
    
    public void startBeingSnoopedBy(Session S){}
    public void stopBeingSnoopedBy(Session S){}
    public boolean amBeingSnoopedBy(Session S){return false;}
    public int snoopSuspension(int x){return 0;}
    
    public void cmdExit(MOB mob, Vector commands) throws Exception {}
    public void logoff(boolean t1, boolean t2, boolean t3){}
    public boolean killFlag(){return false;}
    
    public boolean afkFlag(){return false;}
    public void setAfkFlag(boolean truefalse){}
    public String afkMessage(){return "";}
    public void setAFKMessage(String str){}
    
    public Vector previousCMD() { return inputV;}
    public MOB mob() { return mob;}
    public void setMob(MOB newmob){ mob=newmob;}
    
    public String makeEscape(int c){return "";}
    public int getColor(char c){return ' ';}
    public int currentColor(){return ' ';}
    public int lastColor(){return ' ';}
    public int getWrap(){return 80;}
    
    public String getAddress(){return "";}
    public int getStatus(){return 0;}
    public long getTotalMillis(){return 0;}
    public long getTotalTicks(){return 0;}
    public long getIdleMillis(){return 0;}
    public long getMillisOnline(){return 0;}
    public long getLastPKFight(){return 0;}
    public void setLastPKFight(){}
    public long getLastNPCFight(){return 0;}
    public void setLastNPCFight(){}
    public long lastLoopTime(){return 0;}
    public Vector getLastMsgs(){return new Vector();}
    
    public void setServerTelnetMode(int telnetCode, boolean onOff){}
    public boolean serverTelnetMode(int telnetCode){return false;}
    public void setClientTelnetMode(int telnetCode, boolean onOff){}
    public boolean clientTelnetMode(int telnetCode){return false;}
    public void changeTelnetMode(int telnetCode, boolean onOff){}
    public void initTelnetMode(int mobbitmap){}

}
