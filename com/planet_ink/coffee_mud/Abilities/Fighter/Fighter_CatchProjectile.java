package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_CatchProjectile extends StdAbility
{
	public String ID() { return "Fighter_CatchProjectile"; }
	public String name(){ return "Catch Projectile";}
	public String displayText(){ return "";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_CatchProjectile();}
	public int classificationCode(){ return Ability.SKILL;}
	public boolean doneThisRound=false;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(!doneThisRound)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)
		   ||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN))
		&&(!(msg.tool() instanceof Electronics))
		&&(mob.rangeToTarget()>0)
		&&(mob.fetchEffect("Fighter_ReturnProjectile")==null)
		&&(mob.charStats().getBodyPart(Race.BODY_HAND)>0)
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(-85+mob.charStats().getStat(CharStats.DEXTERITY),false))
		&&(mob.freeWearPositions(Item.HELD)>0))
		{
			Item w=(Item)msg.tool();
			if((((Weapon)w).weaponClassification()==Weapon.CLASS_THROWN)
			&&(msg.source().isMine(w)))
			{
				if(!w.amWearingAt(Item.INVENTORY))
					ExternalPlay.remove(msg.source(),w,true);
				ExternalPlay.drop(msg.source(),w,true,false);
			}
			else
			if(((Weapon)w).requiresAmmunition())
			{
				Item neww=CMClass.getItem("GenItem");
				String ammo=((Weapon)w).ammunitionType();
				if(ammo.length()==0) return true;
				if(ammo.endsWith("s"))
					ammo=ammo.substring(0,ammo.length()-1);
				if(("aeiouAEIOU").indexOf(ammo.charAt(0))>=0)
					ammo="an "+ammo;
				else
					ammo="a "+ammo;
				neww.setName(ammo);
				neww.setDisplayText(ammo+" sits here.");
				neww.setSecretIdentity(ammo);
				neww.setUsesRemaining(1);
				neww.baseEnvStats().setWeight(1);
				neww.setBaseValue(0);
				neww.recoverEnvStats();
				w=neww;
				mob.location().addItemRefuse(neww,Item.REFUSE_PLAYER_DROP);
			}
			if(mob.location().isContent(w))
			{
				FullMsg msg2=new FullMsg(mob,w,msg.source(),CMMsg.MSG_GET,"<S-NAME> catch(es) the <T-NAME> shot by <O-NAME>!");
				if(mob.location().okMessage(mob,msg2))
				{
					mob.location().send(mob,msg2);
					doneThisRound=true;
					helpProfficiency(mob);
					return false;
				}
			}
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}
}