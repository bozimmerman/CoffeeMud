package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_RogueLimb extends Spell
{
	public String ID() { return "Spell_RogueLimb"; }
	public String name(){return "Rogue Limb";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canTargetCode(){return CAN_MOBS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_RogueLimb();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public MOB rogueLimb=null;
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((rogueLimb!=null)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			if(rogueLimb.location()!=((MOB)affected).location())
			{
				((MOB)affected).location().bringMobHere(rogueLimb,false);
				rogueLimb.setVictim((MOB)affected);
			}
			if((rogueLimb.amFollowing()!=null)
			||(rogueLimb.getVictim()!=affected)
			||(!Sense.aliveAwakeMobile(rogueLimb,true))
			||(!Sense.aliveAwakeMobile((MOB)affected,true))
			||(!((MOB)affected).location().isInhabitant((MOB)affected))
			||(!rogueLimb.location().isInhabitant(rogueLimb)))
				unInvoke();
		}
		else
			unInvoke();
		return super.tick(ticking,tickID);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;
		if((affected==null)
		   ||(!(affected instanceof MOB))
		   ||(rogueLimb==null))
			return true;
		if(affect.amITarget(rogueLimb)
		&&(Sense.aliveAwakeMobile(rogueLimb,true))
		&&(Sense.aliveAwakeMobile((MOB)affected,true))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT)))
			ExternalPlay.postDamage(rogueLimb,(MOB)affected,affect.tool(),affect.targetCode()-Affect.MASK_HURT,Affect.MASK_GENERAL|affect.sourceCode(),Weapon.TYPE_NATURAL,null);
		if(affect.amISource(rogueLimb)
		&&(affect.sourceMinor()==Affect.TYP_DEATH))
		{
			unInvoke();
			return false;
		}
		return true;
	}

	public void unInvoke()
	{
		if((affected!=null)
		&&(affected instanceof MOB))
			((MOB)affected).location().show(((MOB)affected),rogueLimb,null,Affect.MSG_OK_ACTION,"<S-NAME> gain(s) control of <T-NAMESELF>.");
		if(rogueLimb!=null)
		{
			rogueLimb.destroy();
			rogueLimb=null;
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> lose(s) control of <T-HIS-HER> limb!":"^S<S-NAME> invoke(s) a powerful spell upon <T-NAMESELF>!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					Vector limbs=new Vector();
					Race theRace=target.charStats().getMyRace();
					for(int i=0;i<Race.BODY_PARTS;i++)
					{
						if((theRace.bodyMask()[i]>0)
						&&(i!=Race.BODY_TORSO))
							limbs.addElement(new Integer(i));
					}
					String limb=null;
					if(limbs.size()==0) 
						limb="body part";
					else 
						limb=((String)Race.BODYPARTSTR[((Integer)limbs.elementAt(Dice.roll(1,limbs.size(),-1))).intValue()]).toLowerCase();
					rogueLimb=CMClass.getMOB("GenMob");
					rogueLimb.setName(target.name()+"'s "+limb);
					rogueLimb.setDisplayText(rogueLimb.name()+" is misbehaving here.");
					rogueLimb.baseEnvStats().setAttackAdjustment((-target.adjustedArmor())+50);
					rogueLimb.baseEnvStats().setArmor(100-target.adjustedAttackBonus());
					rogueLimb.baseCharStats().setMyRace(theRace);
					int hp=100; 
					if(hp>(target.baseState().getHitPoints()/3)) 
						hp=(target.baseState().getHitPoints()/3);
					rogueLimb.baseEnvStats().setDamage(1);
					rogueLimb.baseState().setHitPoints(100);
					rogueLimb.baseState().setMana(0);
					rogueLimb.baseState().setMovement(100);
					rogueLimb.baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK);
					rogueLimb.setVictim(target);
					rogueLimb.recoverMaxState();
					rogueLimb.recoverCharStats();
					rogueLimb.recoverEnvStats();
					rogueLimb.resetToMaxState();
					rogueLimb.bringToLife(mob.location(),true);
					rogueLimb.setVictim(target);
					maliciousAffect(mob,target,0,-1);
					rogueLimb.setVictim(target);
				}
			}
		}
		else
			mob.location().show(mob,target,Affect.MSG_OK_ACTION,"^S<S-NAME> invoke(s) at <T-NAMESELF>, causing <T-NAME> to twitch, and nothing more.");


		// return whether it worked
		return success;
	}
}