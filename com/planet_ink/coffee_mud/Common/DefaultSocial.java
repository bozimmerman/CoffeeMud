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
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


// requires nothing to load
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
public class DefaultSocial implements Social
{
	protected String Social_name;
	protected String You_see;
	protected String Third_party_sees;
	protected String Target_sees;
	protected String See_when_no_target;
    private String MSPfile="";
	protected int sourceCode=CMMsg.MSG_OK_ACTION;
	protected int othersCode=CMMsg.MSG_OK_ACTION;
	protected int targetCode=CMMsg.MSG_OK_ACTION;

	public String ID() { return "DefaultSocial"; }
	public String name(){ return Social_name;}
	public String Name(){return name();}
	public String baseName() {
	    int x=name().indexOf(' ');
	    if(x<0) return name();
	    return name().substring(0,x);
	}
	public void setName(String newName){Social_name=newName;}
	public String You_see(){return You_see;}
	public String Third_party_sees(){return Third_party_sees;}
	public String Target_sees(){return Target_sees;}
	public String See_when_no_target(){return See_when_no_target;}
	public int sourceCode(){return sourceCode;}
	public int othersCode(){return othersCode;}
	public int targetCode(){return targetCode;}
	public void setYou_see(String str){You_see=str;}
	public void setThird_party_sees(String str){Third_party_sees=str;}
	public void setTarget_sees(String str){Target_sees=str;}
	public void setSee_when_no_target(String str){See_when_no_target=str;}
	public void setSourceCode(int code){sourceCode=code;}
	public void setOthersCode(int code){othersCode=code;}
	public void setTargetCode(int code){targetCode=code;}
	public long getTickStatus(){return Tickable.STATUS_NOT;}
    public String MSPfile(){return MSPfile;}
    public void setMSPfile(String newFile){MSPfile=newFile;}
	public long expirationDate(){return 0;}
	public void setExpirationDate(long time){}
	public boolean targetable(Environmental E)
	{
		if(E==null)
			return name().endsWith("-NAME>");
		if(E instanceof MOB)
			return name().endsWith(" <T-NAME>");
		if((E instanceof Item)&&(((Item)E).container()==null))
		{
			Item I=(Item)E;
			if(I.owner() instanceof Room)
				return name().endsWith(" <I-NAME>");
			if(I.owner() instanceof MOB)
			{
				if(I.amWearingAt(Item.IN_INVENTORY))
					return name().endsWith(" <V-NAME>");
				else
					return name().endsWith(" <E-NAME>");
			}
		}
		return false;
	}

	public boolean invoke(MOB mob,
						  Vector commands,
						  Environmental target,
						  boolean auto)
	{
		String targetStr="";
		if((commands.size()>1)
        &&(!((String)commands.elementAt(1)).equalsIgnoreCase("SELF"))
        &&(!((String)commands.elementAt(1)).equalsIgnoreCase("ALL")))
			targetStr=(String)commands.elementAt(1);

		Environmental Target=target;
		if(Target==null)
		{
			Target=mob.location().fetchFromMOBRoomFavorsMOBs(mob,null,targetStr,Wearable.FILTER_ANY);
			if((Target!=null)&&(!CMLib.flags().canBeSeenBy(Target,mob)))
			   Target=null;
			else
			if((Target!=null)&&(!targetable(Target)))
			{
				Social S=CMLib.socials().fetchSocial(baseName(),Target, true);
				if(S!=null) return S.invoke(mob, commands, Target, auto);
			}
		}

        String mspFile=((MSPfile!=null)&&(MSPfile.length()>0))?CMProps.msp(MSPfile,10):"";
        
		String You_see=You_see();
		if((You_see!=null)&&(You_see.trim().length()==0)) 
            You_see=null;
        
		String Third_party_sees=Third_party_sees();
		if((Third_party_sees!=null)&&(Third_party_sees.trim().length()==0)) 
            Third_party_sees=null;
        
		String Target_sees=Target_sees();
		if((Target_sees!=null)&&(Target_sees.trim().length()==0)) Target_sees=null;
        
		String See_when_no_target=See_when_no_target();
		if((See_when_no_target!=null)&&(See_when_no_target.trim().length()==0)) 
            See_when_no_target=null;
        
		if(((Target==null)&&(targetable(null)))||((Target!=null)&&(!targetable(Target))))
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,(auto?CMMsg.MASK_ALWAYS:0)|sourceCode(),See_when_no_target,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		if(Target==null)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,(auto?CMMsg.MASK_ALWAYS:0)|sourceCode(),(You_see==null)?null:You_see+mspFile,CMMsg.NO_EFFECT,null,othersCode(),(Third_party_sees==null)?null:Third_party_sees+mspFile);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,Target,this,(auto?CMMsg.MASK_ALWAYS:0)|sourceCode(),(You_see==null)?null:You_see+mspFile,targetCode(),(Target_sees==null)?null:Target_sees+mspFile,othersCode(),(Third_party_sees==null)?null:Third_party_sees+mspFile);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target instanceof MOB)
				{
					MOB tmob=(MOB)target;
					if((name().toUpperCase().startsWith("SMILE"))
					&&(mob.charStats().getStat(CharStats.STAT_CHARISMA)>=16)
					&&(mob.charStats().getMyRace().ID().equals(tmob.charStats().getMyRace().ID()))
					&&(CMLib.dice().rollPercentage()==1)
					&&(mob.charStats().getStat(CharStats.STAT_GENDER)!=('N'))
					&&(tmob.charStats().getStat(CharStats.STAT_GENDER)!=('N'))
					&&(mob.charStats().getStat(CharStats.STAT_GENDER)!=tmob.charStats().getStat(CharStats.STAT_GENDER))
					&&(!CMSecurity.isDisabled("AUTODISEASE")))
					{
						Ability A=CMClass.getAbility("Disease_Smiles");
						if((A!=null)&&(target.fetchEffect(A.ID())==null))
							A.invoke(tmob,tmob,true,0);
					}
				}
			}
		}
		return true;
	}

	public CMMsg makeChannelMsg(MOB mob,
								int channelInt,
								String channelName,
								Vector commands,
								boolean makeTarget)
	{
		String str=makeTarget?"":"^Q^<CHANNEL \""+channelName+"\"^>["+channelName+"] ";
		String end=makeTarget?"":"^</CHANNEL^>^N^.";
		return makeMessage(mob,str,end,CMMsg.MASK_CHANNEL,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),commands,channelName,makeTarget);
	}
	public CMMsg makeMessage(MOB mob,
							 String str,
							 String end,
							 int srcMask,
							 int fullCode,
							 Vector commands,
							 String I3channelName,
							 boolean makeTarget)
	{
		String targetStr="";
		if((commands.size()>1)
        &&(!((String)commands.elementAt(1)).equalsIgnoreCase("SELF"))
        &&(!((String)commands.elementAt(1)).equalsIgnoreCase("ALL")))
			targetStr=(String)commands.elementAt(1);
		Environmental target=null;
		if(targetStr.length()>0)
		{
		    String targetMud="";
		    if(targetStr.indexOf("@")>0)
		        targetMud=targetStr.substring(targetStr.indexOf("@")+1);
		    else
		    {
		    	target=CMLib.players().getPlayer(targetStr);
				if((target==null)&&(!makeTarget))
				{
					MOB possTarget=CMLib.catalog().getCatalogMob(targetStr);
					if(possTarget!=null)
					{
						CatalogLibrary.CataData data=CMLib.catalog().getCatalogData(possTarget);
						if(data!=null)
							target=data.getLiveReference();
					}
				}
		    }
			if(((target==null)&&(makeTarget))
			||((targetMud.length()>0)
				&&(I3channelName!=null)
				&&((CMLib.intermud().i3online())&&(CMLib.intermud().isI3channel(I3channelName)))))
			{
				target=CMClass.getMOB("StdMOB");
				target.setName(targetStr);
				((MOB)target).setLocation(CMLib.map().getRandomRoom());
			}
			else
			if((target!=null)&&(!CMLib.flags().isInTheGame(target, true)))
				target=null;
			if((target!=null)&&(!CMLib.flags().isSeen(target)))
				target=null;
		}

        String mspFile=((MSPfile!=null)&&(MSPfile.length()>0))?CMProps.msp(MSPfile,10):"";
        String You_see=You_see();
        if((You_see!=null)&&(You_see.trim().length()==0)) 
            You_see=null;
        
        String Third_party_sees=Third_party_sees();
        if((Third_party_sees!=null)&&(Third_party_sees.trim().length()==0)) 
            Third_party_sees=null;
        
        String Target_sees=Target_sees();
        if((Target_sees!=null)&&(Target_sees.trim().length()==0)) Target_sees=null;
        
		String See_when_no_target=See_when_no_target();
		if((See_when_no_target!=null)&&(See_when_no_target.trim().length()==0)) 
            See_when_no_target=null;
        
		CMMsg msg=null;
        if(end.length()==0) mspFile="";
		if(((target==null)&&(targetable(null)))||((target!=null)&&(!targetable(target))))
			msg=CMClass.getMsg(mob,null,this,srcMask|sourceCode(),str+See_when_no_target+end,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
		else
		if(target==null)
			msg=CMClass.getMsg(mob,null,this,srcMask|sourceCode(),str+You_see+end+mspFile,CMMsg.NO_EFFECT,null,fullCode,str+Third_party_sees+end+mspFile);
		else
			msg=CMClass.getMsg(mob,target,this,srcMask|sourceCode(),str+You_see+end+mspFile,fullCode,str+Target_sees+end+mspFile,fullCode,str+Third_party_sees+end+mspFile);
		return msg;
	}

	public String description(){return "";}
	public void setDescription(String str){}
	public String displayText(){return "";}
	public void setDisplayText(String str){}
    public EnvStats stats=null;
	public EnvStats envStats()
    {
        if(stats==null) stats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
        return stats;
    }
	public EnvStats baseEnvStats(){return envStats();}

	public void recoverEnvStats(){}
	public void setBaseEnvStats(EnvStats newBaseEnvStats){}
	public CMObject newInstance() { return new DefaultSocial();}
    public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    protected boolean amDestroyed=false;
    public void destroy(){amDestroyed=true;}
    public boolean amDestroyed(){return amDestroyed;}
    public boolean savable(){return true;}

    public int getSaveStatIndex(){return getStatCodes().length;}
	private static final String[] CODES={"CLASS","NAME"};
	public String[] getStatCodes(){return CODES;}
    public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return name();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setName(val); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof Social)) return false;
		String name=Social_name.toUpperCase().trim();
		if(!(((Social)E).name().toUpperCase().equals(name.trim())))
		   return false;
		if(((You_see == null)!=(((Social)E).You_see() == null))
		||((You_see != null)&&(!You_see.equals(((Social)E).You_see()))))
			return false;
		if(this.sourceCode != ((Social)E).sourceCode())
			return false;
		if(this.targetCode != ((Social)E).targetCode())
			return false;
		if(this.othersCode != ((Social)E).othersCode())
			return false;
		if(((Third_party_sees == null)!=(((Social)E).Third_party_sees() == null))
		||((Third_party_sees != null)&&(!Third_party_sees.equals(((Social)E).Third_party_sees()))))
			return false;
		if(((Target_sees == null)!=(((Social)E).Target_sees() == null))
		||((Target_sees != null)&&(!Target_sees.equals(((Social)E).Target_sees()))))
			return false;
		if(((See_when_no_target == null)!=(((Social)E).See_when_no_target() == null))
		||((See_when_no_target != null)&&(!See_when_no_target.equals(((Social)E).See_when_no_target()))))
			return false;
		if(((MSPfile == null)!=(((Social)E).MSPfile() == null))
		||((MSPfile != null)&&(!MSPfile.equals(((Social)E).MSPfile()))))
			return false;
		return true;
	}
    protected void cloneFix(Social E){}

	public CMObject copyOf()
	{
		try
		{
			DefaultSocial E=(DefaultSocial)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public void setMiscText(String newMiscText){}
	public String text(){return "";}
	public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)	{}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)	{}
	public void executeMsg(Environmental myHost, CMMsg msg){}
	public boolean okMessage(Environmental myHost, CMMsg msg){	return true;}
	public boolean tick(Tickable ticking, int tickID)	{ return true;	}
	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return 0;}

	public String image(){return "";}
    public String rawImage(){return "";}
	public void setImage(String newImage){}
	public void addEffect(Ability to){}
	public void addNonUninvokableEffect(Ability to){}
	public void delEffect(Ability to){}
	public int numEffects(){ return 0;}
	public Ability fetchEffect(int index){return null;}
	public Ability fetchEffect(String ID){return null;}
	public void addBehavior(Behavior to){}
	public void delBehavior(Behavior to){}
	public int numBehaviors(){return 0;}
	public Behavior fetchBehavior(int index){return null;}
	public Behavior fetchBehavior(String ID){return null;}
    public void addScript(ScriptingEngine S){}
    public void delScript(ScriptingEngine S) {}
    public int numScripts(){return 0;}
    public ScriptingEngine fetchScript(int x){ return null;}
	public boolean isGeneric(){return false;}
}
