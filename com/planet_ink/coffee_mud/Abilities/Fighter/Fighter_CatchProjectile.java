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
	protected int canAffectCode(){return Ability.BENEFICIAL_SELF;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_CatchProjectile();}
	public int classificationCode(){ return Ability.SKILL;}
	public boolean doneThisRound=false;

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
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
		&&(mob.fetchAffect("Fighter_ReturnProjectile")==null)
		&&(profficiencyCheck(-85+mob.charStats().getStat(CharStats.DEXTERITY),false)))
		{
			Item w=(Item)affect.tool();
			if((((Weapon)w).weaponClassification()==Weapon.CLASS_THROWN)
			&&(affect.source().isMine(w)))
			{
				if(!w.amWearingAt(Item.INVENTORY))
					ExternalPlay.remove(affect.source(),w,true);
				ExternalPlay.drop(affect.source(),w,true);
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
				FullMsg msg=new FullMsg(mob,w,Affect.MSG_GET,"<S-NAME> catch(es) the <T-NAME> shot by "+affect.source().name()+"!");
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					doneThisRound=true;
					helpProfficiency(mob);
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
			doneThisRound=false;
		return super.tick(tickID);
	}
}