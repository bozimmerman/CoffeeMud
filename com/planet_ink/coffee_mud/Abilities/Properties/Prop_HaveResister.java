package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Prayers.Prayer;
import java.util.*;

public class Prop_HaveResister extends Property
{
	private Item myItem=null;
	private MOB lastMOB=null;

	public Prop_HaveResister()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Resistance due to ownership";
	}

	public Environmental newInstance()
	{
		Prop_HaveResister BOB=new Prop_HaveResister();
		BOB.setMiscText(text());
		return BOB;
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(affectedMOB!=null)
		{
			if(affectedMOB instanceof Item)
			{
				myItem=(Item)affectedMOB;
				if((myItem.myOwner()!=null)&&(myItem.myOwner() instanceof MOB))
					lastMOB=(MOB)myItem.myOwner();
				else
					lastMOB=null;
			}
			else
				lastMOB=null;
		}
		else
			lastMOB=null;
		super.affectEnvStats(affectedMOB,affectableStats);
	}

	public static boolean checkProtection(Ability me, String protType)
	{
		int z=me.text().toUpperCase().indexOf(protType.toUpperCase());
		if(z<0) return false;
		int x=me.text().indexOf("%",z+protType.length());
		if(x<0)
		{
			if(Dice.rollPercentage()<50)
				return true;
			else
				return false;
		}
		else
		{
			int mul=1;
			int tot=0;
			while((--x)>=0)
			{
				if(Character.isDigit(me.text().charAt(x)))
					tot+=Util.s_int(""+me.text().charAt(x))*mul;
				else
					x=-1;
				mul=mul*10;
			}
			if(tot>100) tot=95;
			if(tot<5) tot=5;
			if(Dice.rollPercentage()<tot)
				return true;
			else
				return false;
		}
	}

	public static void resistAffect(Affect affect, MOB mob, Ability me)
	{
		if(mob.location()==null) return;
		if(mob.amDead()) return;
		if(!affect.amITarget(mob)) return;

		if(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		{
			int recovery=affect.targetCode()-Affect.MASK_HURT;
			recovery=(int)Math.round(Util.mul(recovery,Math.random()));
			if(recovery<=0) recovery=0;
			if(Prop_HaveResister.checkProtection(me,"weapons"))
				mob.curState().adjHitPoints(recovery,mob.maxState());
			else
			if(affect.tool()!=null)
			{
				if(affect.tool() instanceof Weapon)
				{
					Weapon W=(Weapon)affect.tool();
					if(((W.weaponType()==Weapon.TYPE_BASHING)&&(Prop_HaveResister.checkProtection(me,"blunt")))
					 ||((W.weaponType()==Weapon.TYPE_PIERCING)&&(Prop_HaveResister.checkProtection(me,"pierce")))
					 ||((W.weaponType()==Weapon.TYPE_SLASHING)&&(Prop_HaveResister.checkProtection(me,"slash"))))
						mob.curState().adjHitPoints(recovery,mob.maxState());
				}
			}
			return;
		}


		if(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
			switch(affect.targetMinor())
			{
			case Affect.TYP_CAST_SPELL:
				if(Prop_HaveResister.checkProtection(me,"magic"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			case Affect.TYP_GAS:
				if(Prop_HaveResister.checkProtection(me,"gas"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			case Affect.TYP_FIRE:
				if(Prop_HaveResister.checkProtection(me,"fire"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			case Affect.TYP_ELECTRIC:
				if(Prop_HaveResister.checkProtection(me,"elec"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			case Affect.TYP_MIND:
				if(Prop_HaveResister.checkProtection(me,"mind"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			case Affect.TYP_JUSTICE:
				if(Prop_HaveResister.checkProtection(me,"justice"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			case Affect.TYP_COLD:
				if(Prop_HaveResister.checkProtection(me,"cold"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			case Affect.TYP_ACID:
				if(Prop_HaveResister.checkProtection(me,"acid"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			case Affect.TYP_WATER:
				if(Prop_HaveResister.checkProtection(me,"water"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			case Affect.TYP_UNDEAD:
				if(Prop_HaveResister.checkProtection(me,"evil"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			case Affect.TYP_POISON:
				if(Prop_HaveResister.checkProtection(me,"poison"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			case Affect.TYP_PARALYZE:
				if(Prop_HaveResister.checkProtection(me,"paralyze"))
					ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
				break;
			default:
				break;
			}
	}

	public String accountForYourself()
	{
		String id="The owner gains resistances: "+text();
		return id;
	}

	public static boolean isOk(Affect affect, Ability me, MOB mob)
	{
		if(!Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
			return true;

		if(Util.bset(affect.targetCode(),Affect.MASK_MAGIC))
		{
			if(affect.tool() instanceof Ability)
			{
				Ability A=(Ability)affect.tool();
				if((A.ID().equals("Spell_Summon"))||(A.ID().equals("Spell_Gate")))
				{
					if(Prop_HaveResister.checkProtection(me,"teleport"))
					{
						affect.source().tell("You can't seem to fixate on '"+mob.name()+"'.");
						return false;
					}
				}
				else
				if((A instanceof Prayer)&&(((Prayer)A).holyQuality()==Prayer.HOLY_EVIL))
				{
					if(Prop_HaveResister.checkProtection(me,"evil"))
					{
						mob.location().show(affect.source(),mob,Affect.MSG_OK_VISUAL,"The unholy energies from <S-NAME> repell from <T-NAME>.");
						return false;
					}
				}
				else
				if((A instanceof Prayer)&&(((Prayer)A).holyQuality()==Prayer.HOLY_GOOD))
				{
					if(Prop_HaveResister.checkProtection(me,"holy"))
					{
						mob.location().show(affect.source(),mob,Affect.MSG_OK_VISUAL,"Holy energies from <S-NAME> are repelled from <T-NAME>.");
						return false;
					}
				}
				else
				if(A.ID().equals("Prayer_Plague"))
				{
					if(Prop_HaveResister.checkProtection(me,"disease"))
					{
						mob.location().show(affect.source(),mob,Affect.MSG_OK_VISUAL,"<T-NAME> resist(s) the disease attack from <S-NAME>.");
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(lastMOB==null) return true;
		MOB mob=lastMOB;
		if((affect.amITarget(mob))&&(!affect.wasModified())&&(mob.location()!=null))
		{
			if(!Prop_HaveResister.isOk(affect,this,mob))
				return false;
			Prop_HaveResister.resistAffect(affect,mob,this);
		}
		return true;
	}
}