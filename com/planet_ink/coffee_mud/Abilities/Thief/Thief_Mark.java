package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Mark extends ThiefSkill
{
	public String ID() { return "Thief_Mark"; }
	public String name(){ return "Mark";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"MARK"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Thief_Mark();}
	public MOB mark=null;
	public int ticks=0;
	
	public String displayText(){
		if(mark!=null)
			return "(Marked: "+mark.name()+", "+ticks+" ticks)";
		else
			return "";
	}

	public void affect(Environmental myHost, Affect msg)
	{
		if(msg.amISource(mark)&&(msg.sourceMinor()==Affect.TYP_DEATH))
		{
			mark=null;
			ticks=0;
			setMiscText("");
		}
		super.affect(myHost,msg);
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected!=null)&&(affected instanceof MOB)&&(((MOB)affected).getVictim()==mark))
		{
			affectableStats.setDamage(affectableStats.damage()+(ticks/20));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(ticks/2));
		}
	}

	public boolean tick(Tickable me, int tickID)
	{
		if((text().length()==0)
		||((affected==null)||(!(affected instanceof MOB))))
		   return super.tick(me,tickID);
		MOB mob=(MOB)affected;
		if(mob.location()!=null)
		{
			if(mark==null)
			{
				int x=text().indexOf("/");
				if(x<0) return super.tick(me,tickID);
				MOB M=mob.location().fetchInhabitant(text().substring(0,x));
				if(M!=null)
					mark=M;
				ticks=Util.s_int(text().substring(x+1));
			}
			else
			if(mob.location().isInhabitant(mark)
			   &&(Sense.canBeSeenBy(mark,mob))
			   &&(!Sense.canBeSeenBy(mob,mark)))
			{
				ticks++;
				setMiscText(mark.name()+"/"+ticks);
			}
		}
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("Who would you like to mark?");
			return false;
		}
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob)
		{
			mob.tell("You cannot mark yourself!");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>0) levelDiff=0;
		boolean success=profficiencyCheck(levelDiff,auto);

		FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_DELICATE_HANDS_ACT,"<S-NAME> mark(s) <T-NAMESELF>.",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			Ability A=mob.fetchAffect(ID());
			if(A==null)
			{
				A=(Ability)copyOf();
				mob.addAffect(A);
				A.makeNonUninvokable();
			}
			((Thief_Mark)A).mark=target;
			((Thief_Mark)A).ticks=0;
			A.setMiscText(target.name()+"/0");
		}
		return success;
	}

}