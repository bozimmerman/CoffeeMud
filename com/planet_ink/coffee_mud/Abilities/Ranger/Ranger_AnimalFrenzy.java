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

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return false;
		if(invoker==null)
		{
			invoker=(MOB)affected;
			rangersGroup=new Vector();
		}
		if(invoker!=affected) return true;
		
		if(rangersGroup!=null)
		{
			Hashtable h=invoker.getGroupMembers(new Hashtable());
			for(Enumeration e=h.elements();e.hasMoreElements();)
			{
				MOB mob=(MOB)e.nextElement();
				if((!rangersGroup.contains(mob))
				   &&(mob!=invoker)
				   &&(mob.location()==invoker.location())
				   &&(mob.charStats().getStat(CharStats.INTELLIGENCE)<2))
				{
					rangersGroup.addElement(mob);
					mob.addNonUninvokableAffect((Ability)this.copyOf());
				}
			}
			for(int i=rangersGroup.size()-1;i>=0;i--)
			{
				try
				{
					MOB mob=(MOB)rangersGroup.elementAt(i);
					if((!h.contains(mob))
					||(mob.location()!=invoker.location()))
					{
						Ability A=mob.fetchAffect(this.ID());
						if((A!=null)&&(A.invoker()==invoker))
							mob.delAffect(A);
						rangersGroup.removeElement(mob);
					}
				}
				catch(java.lang.ArrayIndexOutOfBoundsException e)
				{
				}
			}
		}
		if((Dice.rollPercentage()==1) 
		   &&(invoker.isInCombat())
		   &&(rangersGroup.size()>0))
			helpProfficiency(invoker);
		return true;
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((invoker!=null)&&(affected!=invoker)&&(invoker.isInCombat()))
		{
			int invoAtt=(int)Math.round(Util.mul(Util.div(profficiency(),100.0),invoker.envStats().attackAdjustment()));
			int damBonus=(int)Math.round(Util.mul(affectableStats.damage(),(Util.div(profficiency(),100.0)*4.0)));
			affectableStats.setDamage(affectableStats.damage()+damBonus);
			if(affectableStats.attackAdjustment()<invoAtt)
				affectableStats.setAttackAdjustment(invoAtt);
		}
	}
}