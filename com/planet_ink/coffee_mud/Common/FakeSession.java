package com.planet_ink.coffee_mud.Common;

import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.util.Vector;

import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMFile;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;
import com.planet_ink.coffee_mud.core.interfaces.Tickable;
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
public class FakeSession implements Session
{
    CMFile theFile=null;
    ByteArrayOutputStream bout=null;
    MOB mob = null;
    Vector inputV = new Vector();
    
    public boolean tick(Tickable ticking, int tickID){return false;}
    public String ID(){return "FakeSession";}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new FakeSession();}}
    public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public long getTickStatus(){return 0;}
    public void initializeSession(Socket s, String introTextStr)
    {
    	if(introTextStr.equalsIgnoreCase("MEMORY"))
    		bout=new ByteArrayOutputStream();
    	else
	    	theFile = new CMFile(introTextStr,null,true); 
    }
    public boolean isLockedUpWriting(){return false;}
    public void initializeClass(){}
    public void start(){}
	public void logout(boolean b1){}
    public String getTerminalType(){ return "Fake";}
    public void negotiateTelnetMode(int code){}
	public boolean isFake() { return true;}
    
    public String[] clookup(){return new String[255];}
    
    public void onlyPrint(String msg, boolean noCache){
        if(theFile != null) {
            synchronized(theFile)
            {
                theFile.saveText(msg,true);
            }
        }
        if(bout != null) {
        	synchronized(bout)
        	{
        		try {
	        		bout.write(msg.getBytes());
        		} catch(Exception e) {
        			Log.errOut("FakeSession",e);
        		}
        	}
        }
    }
    public void onlyPrint(String msg){ onlyPrint(msg,false); }
    public void rawOut(String msg){ onlyPrint(msg,false); }
    public void rawPrintln(String msg){ onlyPrint(msg+"\n",false); }
    public void rawPrintln(String msg, int pageBreak){ onlyPrint(msg+"\n",false); }
    public void rawPrint(String msg){ onlyPrint(msg,false); }
    public void rawPrint(String msg, int pageBreak){ onlyPrint(msg,false); }
    public void stdPrint(String msg){ onlyPrint(msg,false); }
    public void stdPrint(Environmental Source, Environmental Target, Environmental Tool, String msg){ onlyPrint(msg,false); }
    public void stdPrintln(String msg){ onlyPrint(msg+"\n",false); }
    public void stdPrintln(Environmental Source, Environmental Target, Environmental Tool, String msg){ onlyPrint(msg+"\n",false); }
    public void out(char[] c){ onlyPrint(new String(c),false); }
    public void print(String msg){ onlyPrint(msg,false); }
    public void print(Environmental Source, Environmental Target, Environmental Tool, String msg){ onlyPrint(msg,false); }
    public void println(String msg){ onlyPrint(msg+"\n",false); }
    public void println(Environmental Source, Environmental Target, Environmental Tool, String msg){ onlyPrint(msg+"\n",false); }
    public void wraplessPrintln(String msg){ onlyPrint(msg+"\n",false); }
    public void wraplessPrint(String msg){ onlyPrint(msg,false); }
    public void colorOnlyPrintln(String msg, boolean noCache){ onlyPrint(msg+"\n",false); }
    public void colorOnlyPrint(String msg, boolean noCache){ onlyPrint(msg,false); }
    public void colorOnlyPrintln(String msg){ onlyPrint(msg+"\n",false); }
    public void colorOnlyPrint(String msg){ onlyPrint(msg,false); }
    public void setPromptFlag(boolean truefalse){}
    
    public char hotkey(long maxWait) {return ' ';}
    public String prompt(String Message, String Default) { 
        onlyPrint(Message,false);
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
        onlyPrint(Message,false);
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
    public boolean amBeingSnoopedBy(Session S){return S==this;}
    public int snoopSuspension(int x){return 0;}
    
    public void cmdExit(MOB mob, Vector commands) throws Exception {}
    public void kill(boolean t1, boolean t2, boolean t3){}
    public boolean killFlag(){return false;}
    
    public boolean afkFlag(){return false;}
    public void setAfkFlag(boolean truefalse){}
    public String afkMessage(){
    	if(bout!=null)
    		return new String(bout.toByteArray());
    	return "";
    }
    public void setAFKMessage(String str){}
    
    public Vector previousCMD() { return inputV;}
    public MOB mob() { return mob;}
    public void setMob(MOB newmob){ mob=newmob;}
	public void setAccount(PlayerAccount account){}
    
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
	public void updateLoopTime(){}
    public Vector getLastMsgs(){return new Vector();}
    
    public void setServerTelnetMode(int telnetCode, boolean onOff){}
    public boolean serverTelnetMode(int telnetCode){return false;}
    public void setClientTelnetMode(int telnetCode, boolean onOff){}
    public boolean clientTelnetMode(int telnetCode){return false;}
    public void changeTelnetMode(int telnetCode, boolean onOff){}
    public void initTelnetMode(int mobbitmap){}

}
