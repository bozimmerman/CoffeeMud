package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Mime extends ActiveTicker
{
	public String ID(){return "Mime";}
	public boolean grantsMobility(){return true;}
	public Mime()
	{
		super();
		minTicks=1; maxTicks=1; chance=100;
		tickReset();
	}
	protected int canImproveCode(){return Behavior.CAN_MOBS
										  |Behavior.CAN_EXITS
										  |Behavior.CAN_ITEMS
									      |Behavior.CAN_ROOMS;}
	private boolean disabled=false;
	private Affect lastMsg=null;

	public Behavior newInstance()
	{
		return new Mime();
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		if((affecting instanceof MOB)&&(!canFreelyBehaveNormal(affecting)))
			return;
		if(disabled) return;
		if(((!(affecting instanceof MOB))||(!affect.amISource((MOB)affecting)))
		&&(affect.sourceMinor()==Affect.TYP_EMOTE)
		||((affect.tool()!=null)&&(affect.tool().ID().equals("Social"))))
			lastMsg=affect;
	}

	public void fixSNameTo(Affect msg, MOB sMOB, Environmental ticking)
	{
		//String src=msg.sourceMessage();
		String trg=msg.targetMessage();
		String oth=msg.othersMessage();
		//if(src!=null) src=Util.replaceAll(src,"<S-NAME>",ticking.displayName());
		//if(src!=null) src=Util.replaceAll(src,"You ",ticking.displayName()+" ");
		//if(src!=null) src=Util.replaceAll(src,"Your ",ticking.displayName()+"`s ");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-NAME>",ticking.displayName());
		if(oth!=null) oth=Util.replaceAll(oth,"<S-NAME>",ticking.displayName());
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
		if(((ticking instanceof MOB)&&(!canFreelyBehaveNormal(ticking)))
		||(!canAct(ticking,tickID)))
			return true;
		msg=(Affect)msg.copyOf();
		MOB sMOB=(MOB)msg.source();
		if(msg.sourceMinor()==Affect.TYP_EMOTE)
		{
			if(ticking instanceof MOB)
				msg.modify((MOB)ticking,msg.target(),msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			else
			{
				MOB newSMOB=CMClass.getMOB("StdMOB");
				newSMOB.baseCharStats().setStat(CharStats.GENDER,'N');
				newSMOB.setName(ticking.name());
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
			if(ticking instanceof MOB)
				msg.modify((MOB)ticking,target,msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			else
			{
				MOB newSMOB=CMClass.getMOB("StdMOB");
				newSMOB.baseCharStats().setStat(CharStats.GENDER,'N');
				newSMOB.setName(ticking.name());
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
}
