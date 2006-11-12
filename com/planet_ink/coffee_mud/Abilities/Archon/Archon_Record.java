package com.planet_ink.coffee_mud.Abilities.Archon;
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


import java.io.IOException;
import java.net.Socket;
import java.util.*;

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

public class Archon_Record extends ArchonSkill
{
	boolean doneTicking=false;
	public String ID() { return "Archon_Record"; }
	public String name(){ return "Record";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"RECORD"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
	public int maxRange(){return adjustedMaxInvokerRange(1);}
	public int usageType(){return USAGE_MOVEMENT;}
	FakeSession sess=null;

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if((sess!=null)&&(mob.session()!=null))
				mob.session().stopBeingSnoopedBy(sess);
			sess=null;
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(sess==null) return false;
		if((affected instanceof MOB)
		&&(((MOB)affected).session()!=null)
		&&(!(((MOB)affected).session().amBeingSnoopedBy(sess))))
			((MOB)affected).session().startBeingSnoopedBy(sess);
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=CMLib.map().getLoadPlayer(CMParms.combine(commands,0));
		if(target==null) target=getTargetAnywhere(mob,commands,givenTarget,false,true,false);
		if(target==null) return false;

		Archon_Record A=(Archon_Record)target.fetchEffect(ID());
		if(A!=null)
		{
			target.delEffect(A);
			if(target.playerStats()!=null) target.playerStats().setUpdated(0);
			mob.tell(target.Name()+" will no longer be recorded.");
			return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),"^F<S-NAME> begin(s) recording <T-NAMESELF>.^?");
            CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String filename=mob.Name()+System.currentTimeMillis()+".log";
				CMFile file=new CMFile("/"+target.Name()+System.currentTimeMillis()+".log",null,true);
				if(!file.canWrite())
	                Log.sysOut("Record",mob.name()+" failed to start recording "+target.name()+".");
				else
				{
	                Log.sysOut("Record",mob.name()+" started recording "+target.name()+" to /"+filename+".");
					Archon_Record A2=(Archon_Record)copyOf();
					FakeSession F=new FakeSession(file);
					A2.sess=F;
	                target.addNonUninvokableEffect(A2);
	                mob.tell("Enter RECORD "+mob.Name()+" again to stop recording.");
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to hush <T-NAMESELF>, but fail(s).");
		return success;
	}
	private class FakeSession implements Session
	{
		CMFile theFile=null;
		public FakeSession(CMFile F){
			theFile=F;
		}
        public boolean tick(Tickable ticking, int tickID){return false;}
        public String ID(){return "FakeSession";}
        public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new FakeSession(theFile);}}
        public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
        public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
        public long getTickStatus(){return 0;}
	    public void initializeSession(Socket s, String introTextStr){}
	    public boolean isLockedUpWriting(){return false;}
        public void initializeClass(){}
	    public void start(){}
	    
		public String[] clookup(){return new String[255];}
		
		public void onlyPrint(String msg, int pageBreak, boolean noCache){
			synchronized(theFile)
			{
				theFile.saveText(msg,true);
			}
		}
		public void onlyPrint(String msg){}
		
		public void rawPrintln(String msg){}
		public void rawPrintln(String msg, int pageBreak){}
		public void rawPrint(String msg){}
		public void rawPrint(String msg, int pageBreak){}
		
		public void stdPrint(String msg){}
		public void stdPrint(Environmental Source,
							 Environmental Target,
							 Environmental Tool,
							 String msg){}
		public void stdPrintln(String msg){}
		public void stdPrintln(Environmental Source,
							   Environmental Target,
							   Environmental Tool,
							   String msg){}
		
		public void out(char[] c){}
		
		public void print(String msg){}
		public void print(Environmental Source,
						  Environmental Target,
						  Environmental Tool,
						  String msg){}
		public void println(String msg){}
		public void println(Environmental Source,
							Environmental Target,
							Environmental Tool,
							String msg){}
		
		public void setPromptFlag(boolean truefalse){}
		
		
		public void wraplessPrintln(String msg){}
		public void wraplessPrint(String msg){}
		
		public void colorOnlyPrintln(String msg, int pageBreak){}
		public void colorOnlyPrint(String msg, int pageBreak){}
		public void colorOnlyPrintln(String msg){}
		public void colorOnlyPrint(String msg){}
		
	    public char hotkey(long maxWait){return ' ';}
		public String prompt(String Message, String Default)
		{ return "";}
		public String prompt(String Message, String Default, long maxTime)
		{ return "";}
		public String prompt(String Message)
		{ return "";}
		public String prompt(String Message, long maxTime)
		{ return "";}
		public boolean confirm(String Message, String Default)
		{ return false;}
		public boolean confirm(String Message, String Default, long maxTime)
		{ return false;}
		public String choose(String Message, String Choices, String Default)
		{ return "";}
		public String choose(String Message, String Choices, String Default, long maxTime)
		{ return "";}
		
		public void startBeingSnoopedBy(Session S){}
		public void stopBeingSnoopedBy(Session S){}
		public boolean amBeingSnoopedBy(Session S){return false;}
		
		public void cmdExit(MOB mob, Vector commands)
			throws Exception{}
		public void logoff(boolean tf){}
		public boolean killFlag(){return false;}
		public void setKillFlag(boolean truefalse){}
		
		public boolean afkFlag(){return false;}
		public void setAfkFlag(boolean truefalse){}
	    public String afkMessage(){return "";}
	    public void setAFKMessage(String str){}
		
		public String blockingIn()
		{ return "";}
		public String readlineContinue()
		{ return "";}
		
		public Vector previousCMD()
		{ return new Vector();}
		
		public MOB mob()
		{ return invoker();}
		public void setMob(MOB newmob){}
		
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
}