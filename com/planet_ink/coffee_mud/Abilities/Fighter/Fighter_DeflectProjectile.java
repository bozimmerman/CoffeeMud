package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_DeflectProjectile extends StdAbility
{
	public String ID() { return "Fighter_DeflectProjectile"; }
	public String name(){ return "Deflect Projectile";}
	public String displayText(){ return "";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return Ability.BENEFICIAL_SELF;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_DeflectProjectile();}
	public int classificationCode(){ return Ability.SKILL;}
	public boolean doneThisRound=false;

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(affect.amITarget(mob)
		&&(!doneThisRound)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon)
		&&((((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_RANGED)
		   ||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_THROWN))
		&&(!(affect.tool() instanceof Electronics))
		&&(mob.rangeToTarget()>0)
		&&(mob.fetchAffect("Fighter_CatchProjectile")==null)
		&&(mob.fetchAffect("Fighter_ReturnProjectile")==null)
		&&(profficiencyCheck(-85+mob.charStats().getStat(CharStats.DEXTERITY),false))
		&&(mob.fetchWornItem(Item.HELD)==null))
		{
			Item w=(Item)affect.tool();
			if((((Weapon)w).weaponClassification()==Weapon.CLASS_THROWN)
			&&(affect.source().isMine(w)))
			{
				if(!w.amWearingAt(Item.INVENTORY))
					ExternalPlay.remove(affect.source(),w,true);
				ExternalPlay.drop(affect.source(),w,true);
				if(!mob.location().isContent(w))
					return true;
			}
			FullMsg msg=new FullMsg(mob,w,Affect.MSG_GET,"<S-NAME> deflect(s) the <T-NAME> shot by "+affect.source().name()+"!");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				doneThisRound=true;
				helpProfficiency(mob);
				return false;
			}
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}
}