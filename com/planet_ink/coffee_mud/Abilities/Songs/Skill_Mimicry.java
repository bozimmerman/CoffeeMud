package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Mimicry extends StdAbility
{
	public String ID() { return "Skill_Mimicry"; }
	public String name(){ return "Mimicry";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"MIMICRY","MIMIC"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Mimicry();}
	public MOB mimicing=null;
	private Affect lastMsg=null;
	private boolean disabled=false;
	
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		if((affecting instanceof MOB)&&(!Sense.aliveAwakeMobile((MOB)affecting,true)))
			return;
		if(disabled) return;
		if(((!(affecting instanceof MOB))||(!affect.amISource((MOB)affecting)))
		&&((affect.sourceMinor()!=Affect.TYP_EMOTE)
		||((affect.tool()!=null)&&(affect.tool().ID().equals("Social")))))
			lastMsg=affect;
	}

	public void fixSNameTo(Affect msg, MOB sMOB, Environmental ticking)
	{
		//String src=msg.sourceMessage();
		String trg=msg.targetMessage();
		String oth=msg.othersMessage();
		//if(src!=null) src=Util.replaceAll(src,"<S-NAME>",ticking.name());
		//if(src!=null) src=Util.replaceAll(src,"You ",ticking.name()+" ");
		//if(src!=null) src=Util.replaceAll(src,"Your ",ticking.name()+"`s ");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-NAME>",ticking.name());
		if(oth!=null) oth=Util.replaceAll(oth,"<S-NAME>",ticking.name());
		//if(src!=null) src=Util.replaceAll(src,"<S-HIM-HERSELF>","itself");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-HIM-HERSELF>","itself");
		if(oth!=null) oth=Util.replaceAll(oth,"<S-HIM-HERSELF>","itself");
		//if(src!=null) src=Util.replaceAll(src,"<S-HIS-HERSELF>","itself");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-HIS-HERSELF>","itself");
		if(oth!=null) oth=Util.replaceAll(oth,"<S-HIS-HERSELF>","itself");
		//if(src!=null) src=Util.replaceAll(src,"<S-HIM-HER>","it");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-HIM-HER>","it");
		if(oth!=null) oth=Util.replaceAll(oth,"<S-HIM-HER>","it");
		//if(src!=null) src=Util.replaceAll(src,"<S-HE-SHE>","it");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-HE-SHE>","it");
		if(oth!=null) oth=Util.replaceAll(oth,"<S-HE-SHE>","it");
		//if(src!=null) src=Util.replaceAll(src,"<S-HIS-HER>","its");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-HIS-HER>","its");
		if(oth!=null) oth=Util.replaceAll(oth,"<S-HIS-HER>","its");
		msg.modify(sMOB,sMOB,msg.tool(),
				   msg.sourceCode(),oth,
				   msg.targetCode(),trg,
				   msg.othersCode(),oth);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		Affect msg=lastMsg;
		if(msg==null) return true;
		lastMsg=null;
		if(((affected instanceof MOB)&&(!Sense.aliveAwakeMobile((MOB)affected,true))))
			return true;
		msg=(Affect)msg.copyOf();
		MOB sMOB=(MOB)msg.source();
		if(msg.sourceMinor()==Affect.TYP_EMOTE)
		{
			if(affected instanceof MOB)
				msg.modify((MOB)affected,msg.target(),msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			else
			{
				MOB newSMOB=CMClass.getMOB("StdMOB");
				newSMOB.baseCharStats().setStat(CharStats.GENDER,'N');
				newSMOB.setName(affected.name());
				newSMOB.recoverCharStats();
				msg.modify(newSMOB,msg.source(),msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			}
		}
		else
		if((msg.tool()!=null)&&(msg.tool().ID().equals("Social")))
		{
			MOB target=null;
			if((msg.target()!=null)&&(msg.target() instanceof MOB))
				target=(MOB)msg.source();
			if(affected instanceof MOB)
				msg.modify((MOB)affected,target,msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			else
			{
				MOB newSMOB=CMClass.getMOB("StdMOB");
				newSMOB.baseCharStats().setStat(CharStats.GENDER,'N');
				newSMOB.setName(affected.name());
				newSMOB.recoverCharStats();
				msg.modify(newSMOB,target,msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			}
		}
		else
			return true;
		disabled=true;
		if((msg!=null)
		&&(sMOB.location()!=null)
		&&(sMOB.location().okAffect(sMOB,msg)))
		{
			if(msg.source().location()==null)
			{
				msg.source().setLocation(sMOB.location());
				sMOB.location().send(msg.source(),msg);
				msg.source().setLocation(null);
			}
			else
				sMOB.location().send(msg.source(),msg);
		}
		disabled=false;
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_QUIETMOVEMENT|(auto?Affect.MASK_GENERAL:0),auto?"":"<S-NAME> begin(s) mimicing <T-NAMESELF>.");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mimicing=target;
				beneficialAffect(mob,mob,0);
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to mimic <T-NAMESELF>, but fail(s).");
		return success;
	}
}
