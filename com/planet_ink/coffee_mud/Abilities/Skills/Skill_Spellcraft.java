package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Spellcraft extends StdAbility
{
	public String ID() { return "Skill_Spellcraft"; }
	public String name(){ return "Spellcraft";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public Environmental newInstance(){	return new Skill_Spellcraft();}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.SKILL;}
	public String lastID="";
	public int craftType(){return Ability.SPELL;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected==null)||(!(affected instanceof MOB)))
		   return;
		MOB mob=(MOB)affected;
		if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(!msg.amISource(mob))
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==craftType())
		&&(!lastID.equalsIgnoreCase(msg.tool().ID()))
		&&(mob.location()!=null)
		&&(mob.location().isInhabitant(msg.source()))
		&&(Sense.canBeSeenBy(msg.source(),mob))
		&&(msg.source().fetchAbility(msg.tool().ID())!=null)
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(0,false)))
		{
			Ability A=(Ability)copyOf();
			A.setMiscText(msg.tool().ID());
			lastID=msg.tool().ID();
			msg.addTrailerMsg(new FullMsg(mob,msg.source(),A,CMMsg.MSG_OK_VISUAL,"<T-NAME> cast '"+msg.tool().name()+"'.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			helpProfficiency(mob);
		}
	}
}
