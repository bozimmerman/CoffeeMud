package com.planet_ink.coffee_mud.Commands.base;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

// requires nothing to load
public class Social implements Environmental
{
	private String Social_name;
	private String You_see;
	private String Third_party_sees;
	private String Target_sees;
	private String See_when_no_target;
	private int sourceCode=Affect.MSG_OK_ACTION;
	private int othersCode=Affect.MSG_OK_ACTION;
	private int targetCode=Affect.MSG_OK_ACTION;

	public String ID() { return "Social"; }
	public String name(){ return Social_name;}
	public void setName(String newName){Social_name=newName;}
	public String You_see(){return You_see;};
	public String Third_party_sees(){return Third_party_sees;};
	public String Target_sees(){return Target_sees;};
	public String See_when_no_target(){return See_when_no_target;};
	public int sourceCode(){return sourceCode;}
	public int othersCode(){return othersCode;}
	public int targetCode(){return targetCode;}
	public void setYou_see(String str){You_see=str;};
	public void setThird_party_sees(String str){Third_party_sees=str;};
	public void setTarget_sees(String str){Target_sees=str;};
	public void setSee_when_no_target(String str){See_when_no_target=str;};
	public void setSourceCode(int code){sourceCode=code;}
	public void setOthersCode(int code){othersCode=code;}
	public void setTargetCode(int code){targetCode=code;}


	public boolean invoke(MOB mob,
						  Vector commands,
						  Environmental target,
						  boolean auto)
	{
		String targetStr="";
		if((commands.size()>1)&&(!((String)commands.elementAt(1)).equalsIgnoreCase("SELF")))
			targetStr=(String)commands.elementAt(1);

		Environmental Target=target;
		if(Target==null)
			Target=mob.location().fetchFromRoomFavorMOBs(null,targetStr,Item.WORN_REQ_ANY);
		if((Target!=null)&&(!Sense.canBeSeenBy(Target,mob)))
		   Target=null;

		String You_see=You_see();
		if((You_see!=null)&&(You_see.trim().length()==0)) You_see=null;
		String Third_party_sees=Third_party_sees();
		if((Third_party_sees!=null)&&(Third_party_sees.trim().length()==0)) Third_party_sees=null;
		String Target_sees=Target_sees();
		if((Target_sees!=null)&&(Target_sees.trim().length()==0)) Target_sees=null;
		String See_when_no_target=See_when_no_target();
		if((See_when_no_target!=null)&&(See_when_no_target.trim().length()==0)) See_when_no_target=null;
		if((Target==null)&&(targetStr.equals("")))
		{
			FullMsg msg=new FullMsg(mob,null,this,(auto?Affect.MASK_GENERAL:0)|sourceCode(),You_see,Affect.NO_EFFECT,null,othersCode(),Third_party_sees);
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		if((Target==null)&&(!targetStr.equals("")))
		{
			FullMsg msg=new FullMsg(mob,null,this,(auto?Affect.MASK_GENERAL:0)|sourceCode(),See_when_no_target,Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			FullMsg msg=new FullMsg(mob,Target,this,(auto?Affect.MASK_GENERAL:0)|sourceCode(),You_see,targetCode(),Target_sees,othersCode(),Third_party_sees);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target instanceof MOB)
				{
					MOB tmob=(MOB)target;
					if((name().toUpperCase().startsWith("SMILE"))
					&&(mob.charStats().getStat(CharStats.CHARISMA)>=16)
					&&(mob.charStats().getMyRace().ID().equals(tmob.charStats().getMyRace().ID()))
					&&(Dice.rollPercentage()==1)
					&&(mob.charStats().getStat(CharStats.GENDER)!=((int)'N'))
					&&(tmob.charStats().getStat(CharStats.GENDER)!=((int)'N'))
					&&(mob.charStats().getStat(CharStats.GENDER)!=tmob.charStats().getStat(CharStats.GENDER)))
					{
						Ability A=CMClass.getAbility("Disease_Smiles");
						if(A!=null) A.invoke(tmob,tmob,true);
					}
				}
			}
		}
		return true;
	}

	public String description(){return "";}
	public void setDescription(String str){}
	public String displayText(){return "";}
	public void setDisplayText(String str){}
	protected static final EnvStats envStats=new DefaultEnvStats();
	public EnvStats envStats(){return envStats;}
	public EnvStats baseEnvStats(){return envStats;}

	public void recoverEnvStats(){}
	public void setBaseEnvStats(EnvStats newBaseEnvStats){}
	public Environmental newInstance()
	{ return new Social();}

	private static final String[] CODES={"CLASS","NAME"};
	public String[] getStatCodes(){return CODES;}
	private int getCodeNum(String code){
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
		if(name.indexOf(" ")>=0)
			name=name.substring(0,name.indexOf(" ")+1);
		if(((Social)E).Social_name.toUpperCase().startsWith(name))
			return true;
		if((((Social)E).Social_name.toUpperCase().equals(name.trim())))
		   return true;
		return false;
	}
	private void cloneFix(Social E){}

	public Environmental copyOf()
	{
		try
		{
			Social E=(Social)this.clone();
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

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)	{}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)	{}
	public void affect(Environmental myHost, Affect affect){}
	public boolean okAffect(Environmental myHost, Affect affect){	return true;}
	public boolean tick(Tickable ticking, int tickID)	{ return true;	}
	public int maxRange(){return -1;}
	public int minRange(){return -1;}

	public void addAffect(Ability to){}
	public void addNonUninvokableAffect(Ability to){}
	public void delAffect(Ability to){}
	public int numAffects(){ return 0;}
	public Ability fetchAffect(int index){return null;}
	public Ability fetchAffect(String ID){return null;}
	public void addBehavior(Behavior to){}
	public void delBehavior(Behavior to){}
	public int numBehaviors(){return 0;}
	public Behavior fetchBehavior(int index){return null;}
	public Behavior fetchBehavior(String ID){return null;}
	public boolean isGeneric(){return false;}
}
