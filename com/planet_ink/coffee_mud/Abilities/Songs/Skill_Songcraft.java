package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Songcraft extends StdAbility
{
	public String ID() { return "Skill_Songcraft"; }
	public String name(){ return "Songcraft";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public Environmental newInstance(){	return new Skill_Songcraft();}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.SKILL;}
	public String lastID="";
	public int craftType(){return Ability.SONG;}
	
	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if((affected==null)||(!(affected instanceof MOB)))
		   return;
		MOB mob=(MOB)affected;
		if((msg.sourceMinor()==Affect.TYP_CAST_SPELL)
		&&(!msg.amISource(mob))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==craftType())
		&&(!lastID.equalsIgnoreCase(msg.tool().ID()))
		&&(mob.location()!=null)
		&&(mob.location().isInhabitant(msg.source()))
		&&(profficiencyCheck(0,false))
		&&(Sense.canBeSeenBy(msg.source(),mob)))
		{
			Ability A=(Ability)copyOf();
			A.setMiscText(msg.tool().ID());
			lastID=msg.tool().ID();
			msg.addTrailerMsg(new FullMsg(mob,msg.source(),A,Affect.MSG_OK_VISUAL,"<T-NAME> cast '"+msg.tool().name()+"'.",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null));
			helpProfficiency(mob);
		}
	}
}