package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Imitation extends BardSkill
{
	public String ID() { return "Skill_Imitation"; }
	public String name(){ return "Imitate";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public Environmental newInstance(){	return new Skill_Imitation();}
	private static final String[] triggerStrings = {"IMITATE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.SKILL;}
	public String lastID="";
	public int craftType(){return Ability.SPELL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public Hashtable immitations=new Hashtable();
	public String[] lastOnes=new String[2];

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((myHost==null)||(!(myHost instanceof MOB)))
		   return;
		MOB mob=(MOB)myHost;
		if(msg.tool()!=null)
		{
			if((msg.amISource(mob))
			&&((msg.tool().ID().equals("Skill_Spellcraft"))
				||(msg.tool().ID().equals("Skill_Songcraft"))
				||(msg.tool().ID().equals("Skill_Chantcraft"))
				||(msg.tool().ID().equals("Skill_Prayercraft")))
			&&(msg.tool().text().equals(lastOnes[0]))
			&&(msg.tool().text().length()>0)
			&&(!immitations.containsKey(msg.tool().text())))
			{
				Ability A=CMClass.getAbility(msg.tool().text());
				if(A!=null)	immitations.put(A.name(),lastOnes[1]);
			}
			else
			if((msg.tool() instanceof Ability)
			&&(!msg.amISource(mob))
			&&(msg.othersMessage()!=null))
			{
				lastOnes[0]=msg.tool().ID();
				lastOnes[1]=msg.othersMessage();
			}
		}

	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String cmd=(commands.size()>0)?Util.combine(commands,0).toUpperCase():"";
		StringBuffer str=new StringBuffer("");
		String found=null;
		for(Enumeration e=immitations.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if((cmd.length()>0)&&(key.toUpperCase().startsWith(cmd)))
				found=key;
			str.append(key+" ");
		}
		if((cmd.length()==0)||(found==null))
		{
			if(found!=null) mob.tell("'"+cmd+"' is not something you know how to imitate.");
			mob.tell("Spells/Skills you may imitate: "+str.toString()+".");
			return true;
		}
		Environmental target=null;
		if(commands.size()>1)
		{
			target=mob.location().fetchFromRoomFavorMOBs(null,Util.combine(commands,1),Item.WORN_REQ_ANY);
			if(target==null) target=mob.fetchInventory(null,Util.combine(commands,1));
		}
		if(target==null) target=mob.getVictim();
		if(target==null) target=mob;



		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|CMMsg.MASK_DELICATE|(auto?CMMsg.MASK_GENERAL:0),(String)immitations.get(found));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to imitate "+found+", but fail(s).");

		return success;
	}
}
