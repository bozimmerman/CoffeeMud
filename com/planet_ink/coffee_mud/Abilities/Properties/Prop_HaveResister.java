package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Prayers.Prayer;
import java.util.*;

// this ability is the very picture of the infectuous affect.
// It lobs itself onto other qualified objects, and withdraws
// again when it will.  Don't lothe the HaveResister, LOVE IT.
public class Prop_HaveResister extends Property
{
	public String ID() { return "Prop_HaveResister"; }
	public String name(){ return "Resistance due to ownership";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	private Item myItem=null;
	private MOB lastMOB=null;
	private CharStats adjCharStats=null;
	public Environmental newInstance(){	Prop_HaveResister BOB=new Prop_HaveResister();	BOB.setMiscText(text());return BOB;}
	
	public boolean isBorrowed(Environmental toMe)
	{
		if(toMe instanceof MOB)
			return true;
		return borrowed;
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		ensureStarted();
		if(affectedMOB!=null)
		{
			if(affectedMOB instanceof Item)
			{
				myItem=(Item)affectedMOB;
				if(myItem.owner() instanceof MOB)
				{
					if((lastMOB!=null)&&(myItem.owner()!=lastMOB))
					{	removeMyAffectFromLastMob(this,lastMOB); lastMOB=null;}
					if(myItem.owner() !=null)
					{
						lastMOB=(MOB)myItem.owner();
						if(!lastMOB.isMine(this))
							addMe(lastMOB,this);
					}
				}
			}
			else
			if(affectedMOB instanceof MOB)
			{
				if((myItem.owner() instanceof MOB)
				   &&(myItem.owner()==affectedMOB))
				{
					if((lastMOB!=null)&&(affectedMOB!=lastMOB))
					{	removeMyAffectFromLastMob(this,lastMOB); lastMOB=null;}
					lastMOB=(MOB)affectedMOB;
				}
				else
				if((affectedMOB!=null)&&(affectedMOB!=myItem.owner()))
				{
					removeMyAffectFromLastMob(this,(MOB)affectedMOB);
				}
			}
		}
		super.affectEnvStats(affectedMOB,affectableStats);
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		this.adjCharStats=new DefaultCharStats();
		setAdjustments(this,adjCharStats);
	}
	
	public static void removeMyAffectFromLastMob(Ability me, MOB mylastMOB)
	{
		int x=0;
		while(x<mylastMOB.numAffects())
		{
			Ability aff=mylastMOB.fetchAffect(x);
			if((aff!=null)&&(aff==me))
				mylastMOB.delAffect(aff);
			else
				x++;
		}
		mylastMOB.recoverCharStats();
	}

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}
	
	public static void adjCharStats(CharStats affectedStats,
									CharStats adjCharStats)
	{
		affectedStats.setStat(CharStats.SAVE_MAGIC,affectedStats.getStat(CharStats.SAVE_MAGIC)+adjCharStats.getStat(CharStats.SAVE_MAGIC));
		affectedStats.setStat(CharStats.SAVE_GAS,affectedStats.getStat(CharStats.SAVE_GAS)+adjCharStats.getStat(CharStats.SAVE_GAS));
		affectedStats.setStat(CharStats.SAVE_FIRE,affectedStats.getStat(CharStats.SAVE_FIRE)+adjCharStats.getStat(CharStats.SAVE_FIRE));
		affectedStats.setStat(CharStats.SAVE_ELECTRIC,affectedStats.getStat(CharStats.SAVE_ELECTRIC)+adjCharStats.getStat(CharStats.SAVE_ELECTRIC));
		affectedStats.setStat(CharStats.SAVE_MIND,affectedStats.getStat(CharStats.SAVE_MIND)+adjCharStats.getStat(CharStats.SAVE_MIND));
		affectedStats.setStat(CharStats.SAVE_JUSTICE,affectedStats.getStat(CharStats.SAVE_JUSTICE)+adjCharStats.getStat(CharStats.SAVE_JUSTICE));
		affectedStats.setStat(CharStats.SAVE_COLD,affectedStats.getStat(CharStats.SAVE_COLD)+adjCharStats.getStat(CharStats.SAVE_COLD));
		affectedStats.setStat(CharStats.SAVE_ACID,affectedStats.getStat(CharStats.SAVE_ACID)+adjCharStats.getStat(CharStats.SAVE_ACID));
		affectedStats.setStat(CharStats.SAVE_WATER,affectedStats.getStat(CharStats.SAVE_WATER)+adjCharStats.getStat(CharStats.SAVE_WATER));
		affectedStats.setStat(CharStats.SAVE_UNDEAD,affectedStats.getStat(CharStats.SAVE_UNDEAD)+adjCharStats.getStat(CharStats.SAVE_UNDEAD));
		affectedStats.setStat(CharStats.SAVE_DISEASE,affectedStats.getStat(CharStats.SAVE_DISEASE)+adjCharStats.getStat(CharStats.SAVE_DISEASE));
		affectedStats.setStat(CharStats.SAVE_POISON,affectedStats.getStat(CharStats.SAVE_POISON)+adjCharStats.getStat(CharStats.SAVE_POISON));
		affectedStats.setStat(CharStats.SAVE_PARALYSIS,affectedStats.getStat(CharStats.SAVE_PARALYSIS)+adjCharStats.getStat(CharStats.SAVE_PARALYSIS));
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		if((affectedMOB!=null)
		   &&(lastMOB==affectedMOB))
			adjCharStats(affectedStats,adjCharStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}
	
	public static void addMe(MOB lastMOB, Ability me)
	{
		lastMOB.addNonUninvokableAffect(me);
		lastMOB.recoverCharStats();
	}
	public static boolean checkProtection(Ability me, String protType)
	{
		int prot=getProtection(me,protType);
		if(prot==0) return false;
		if(prot<5) prot=5;
		if(prot>95) prot=95;
		if(Dice.rollPercentage()<prot)
			return true;
		return false;
	}
	
	public static int getProtection(Ability me, String protType)
	{
		int z=me.text().toUpperCase().indexOf(protType.toUpperCase());
		if(z<0) return 0;
		int x=me.text().indexOf("%",z+protType.length());
		if(x<0)
			return 50;
		else
		{
			int mul=1;
			int tot=0;
			while((--x)>=0)
			{
				if(Character.isDigit(me.text().charAt(x)))
					tot+=Util.s_int(""+me.text().charAt(x))*mul;
				else
				{
					if(me.text().charAt(x)=='-')
						mul=mul*-1;
					x=-1;
				}
				mul=mul*10;
			}
			return tot;
		}
	}
	public static void setAdjustments(Ability me, CharStats adjCharStats)
	{
		adjCharStats.setStat(CharStats.SAVE_MAGIC,getProtection(me,"magic"));
		adjCharStats.setStat(CharStats.SAVE_GAS,getProtection(me,"gas"));
		adjCharStats.setStat(CharStats.SAVE_FIRE,getProtection(me,"fire"));
		adjCharStats.setStat(CharStats.SAVE_ELECTRIC,getProtection(me,"elec"));
		adjCharStats.setStat(CharStats.SAVE_MIND,getProtection(me,"mind"));
		adjCharStats.setStat(CharStats.SAVE_JUSTICE,getProtection(me,"justice"));
		adjCharStats.setStat(CharStats.SAVE_COLD,getProtection(me,"cold"));
		adjCharStats.setStat(CharStats.SAVE_ACID,getProtection(me,"acid"));
		adjCharStats.setStat(CharStats.SAVE_WATER,getProtection(me,"water"));
		adjCharStats.setStat(CharStats.SAVE_UNDEAD,getProtection(me,"evil"));
		adjCharStats.setStat(CharStats.SAVE_DISEASE,getProtection(me,"disease"));
		adjCharStats.setStat(CharStats.SAVE_POISON,getProtection(me,"poison"));
		adjCharStats.setStat(CharStats.SAVE_PARALYSIS,getProtection(me,"paralyze"));
	}

	public static void resistAffect(Affect affect, MOB mob, Ability me)
	{
		if(mob.location()==null) return;
		if(mob.amDead()) return;
		if(!affect.amITarget(mob)) return;

		if((Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&((affect.targetCode()-Affect.MASK_HURT)>0)
		&&(affect.tool()!=null)
		   &&(affect.tool() instanceof Weapon))
		{
			int recovery=affect.targetCode()-Affect.MASK_HURT;
			recovery=(int)Math.round(Util.mul(recovery,Math.random()));
			if(recovery<=0) recovery=0;
			if(Prop_HaveResister.checkProtection(me,"weapons"))
				mob.curState().adjHitPoints(recovery,mob.maxState());
			else
			{
				Weapon W=(Weapon)affect.tool();
				if(((W.weaponType()==Weapon.TYPE_BASHING)&&(Prop_HaveResister.checkProtection(me,"blunt")))
				 ||((W.weaponType()==Weapon.TYPE_PIERCING)&&(Prop_HaveResister.checkProtection(me,"pierce")))
				 ||((W.weaponType()==Weapon.TYPE_SLASHING)&&(Prop_HaveResister.checkProtection(me,"slash"))))
					mob.curState().adjHitPoints(recovery,mob.maxState());
			}
			return;
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