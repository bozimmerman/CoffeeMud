package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.Prayers.Prayer;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Ranger_AnimalFrenzy extends StdAbility
{
	public String ID() { return "Ranger_AnimalFrenzy"; }
	public String name(){ return "Animal Frenzy";}
	public String displayText(){return "";}
	public int quality(){return Ability.OK_OTHERS;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	protected Vector rangersGroup=null;
	public Environmental newInstance(){	return new Ranger_AnimalFrenzy();}
	public int classificationCode(){ return Ability.SKILL;}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affect.source();
		if(rangersGroup.contains(affect.source())
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(profficiencyCheck(-90+invoker.charStats().getStat(CharStats.STRENGTH),false)))
		{
			double pctRecovery=(Util.div(profficiency(),400.0)*Math.random());
			int bonus=(int)Math.round(Util.mul((affect.targetCode()-Affect.MASK_HURT),pctRecovery));
			affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()+bonus,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}
	
	public boolean tick(int tickID)
	{
		if(!super.tick(tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return false;
		if(invoker==null) invoker=(MOB)affected;
		if(rangersGroup!=null)
		{
			Hashtable h=((MOB)affected).getGroupMembers(new Hashtable());
			for(Enumeration e=h.elements();e.hasMoreElements();)
			{
				MOB mob=(MOB)e.nextElement();
				if((!rangersGroup.contains(mob))
				   &&(mob.charStats().getStat(CharStats.INTELLIGENCE)<2))
					rangersGroup.addElement(mob);
			}
			for(int i=rangersGroup.size()-1;i>=0;i--)
			{
				try
				{
					MOB mob=(MOB)rangersGroup.elementAt(i);
					if((!h.contains(mob))
					||(mob.location()!=invoker.location()))
						rangersGroup.removeElement(mob);
				}
				catch(java.lang.ArrayIndexOutOfBoundsException e)
				{
				}
			}
		}
		if(Dice.rollPercentage()==1) 
			helpProfficiency(invoker);
		return true;
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(rangersGroup!=null)
		for(int i=rangersGroup.size()-1;i>=0;i--)
		{
			try
			{
				MOB mob=(MOB)rangersGroup.elementAt(i);
				mob.envStats().setAttackAdjustment(
					(int)Math.round(Util.mul(Util.div(profficiency(),100.0),affectableStats.attackAdjustment())));
			}
			catch(java.lang.ArrayIndexOutOfBoundsException e)
			{
			}
		}
	}
}