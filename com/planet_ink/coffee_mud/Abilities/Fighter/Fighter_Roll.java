package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Roll extends StdAbility
{
	int regain=-1;
	public String ID() { return "Fighter_Roll"; }
	public String name(){ return "Roll With Blows";}
	public String displayText(){ return "";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return Ability.BENEFICIAL_SELF;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_Roll();}
	public int classificationCode(){ return Ability.SKILL;}
	public boolean doneThisRound=false;

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		regain=-1;
		if(!super.okAffect(myHost,affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(affect.amITarget(mob)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&((affect.targetCode()-Affect.MASK_HURT)>0)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon)
		&&(mob.rangeToTarget()==0)
		&&(!doneThisRound)
		&&(profficiencyCheck(-85+mob.charStats().getStat(CharStats.DEXTERITY),false)))
		{
			doneThisRound=true;
			double pctRecovery=(Util.div(profficiency(),100.0)*Math.random());
			regain=(int)Math.round(Util.mul((affect.targetCode()-Affect.MASK_HURT),pctRecovery));
			affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()-regain,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}
	
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(regain>0))
		{
			affect.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> roll(s) with the hit."));
			helpProfficiency(mob);
			regain=-1;
		}
	}
}