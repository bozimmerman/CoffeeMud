package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.Misc.Amputation;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_CalledStrike extends StdAbility
{
	public String ID() { return "Fighter_CalledStrike"; }
	public String name(){ return "Called Strike";}
	private static final String[] triggerStrings = {"CALLEDSTRIKE"};
	public int quality(){return Ability.MALICIOUS;}
	public String displayText(){return "";}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Fighter_CalledStrike();}
	public int classificationCode(){ return Ability.SKILL;}
	
	protected String gone="";
	protected long code=0;
	protected long bit=0;
	protected MOB target=null;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-100);
	}
	
	protected boolean amputate()
	{
		MOB mob=(MOB)affected;
		if(mob==null) return false;
		Amputation A=(Amputation)target.fetchAffect("Amputation");
		boolean newOne=false;
		if(A==null){
			A=new Amputation();
			newOne=true;
		}
		if((Amputation.AMPUTATE_BOTHEYES&bit)>0)
			target.location().show(target,null,Affect.MSG_OK_VISUAL,"^G<S-YOUPOSS> "+gone+" is destroyed!^?");
		else
		{
			target.location().show(target,null,Affect.MSG_OK_VISUAL,"^G<S-YOUPOSS> "+gone+" falls off!^?");
			Item limb=CMClass.getItem("GenItem");
			limb.setName("a "+gone);
			limb.setDisplayText("a bloody "+gone+" is sitting here.");
			limb.setSecretIdentity(target.name()+"`s bloody "+gone+".");
			limb.baseEnvStats().setLevel(1);
			limb.baseEnvStats().setWeight(5);
			limb.recoverEnvStats();
			target.location().addItemRefuse(limb,Item.REFUSE_PLAYER_DROP);
		}
		if(newOne==true)
			target.addNonUninvokableAffect(A);
		A.setMiscText(""+(A.missingLimbList()|code));
		mob.confirmWearability();
		return true;
	}
	
	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if((affected==null)||(!(affected instanceof MOB))||(target==null))
		   return super.okAffect(myHost,msg);
		MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		&&(msg.amITarget(target))
		&&(Util.bset(msg.targetCode(),Affect.MASK_HURT)))
		{
			int hurtAmount=msg.targetCode()-Affect.MASK_HURT;
			if(hurtAmount>=(target.baseState().getHitPoints()/9))
			{
				hurtAmount=(target.baseState().getHitPoints()/9);
				msg.modify(msg.source(),msg.target(),msg.tool(),
						   msg.sourceCode(),
						   msg.sourceMessage(),
						   Affect.MASK_HURT+hurtAmount,
						   msg.targetMessage(),
						   msg.othersCode(),
						   msg.othersMessage());
				amputate();
			}
			else
				mob.tell(mob,target,null,"You failed to cut off <T-YOUPOSS> '"+gone+"'.");
			unInvoke();
		}
		return super.okAffect(myHost,msg);
	}

	protected boolean prereqs(MOB mob)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to perform a called strike!");
			return false;
		}
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to call a strike!");
			return false;
		}
		
		Item w=mob.fetchWieldedItem();
		if((w==null)||(!(w instanceof Weapon)))
		{
			mob.tell("You need a weapon to perform a called strike!");
			return false;
		}
		Weapon wp=(Weapon)w;
		if(wp.weaponType()!=Weapon.TYPE_SLASHING)
		{
			mob.tell("You cannot amputate with "+wp.name()+"!");
			return false;
		}
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!prereqs(mob)) return false;
		
		gone="";
		code=0;
		bit=0;

		target=mob.getVictim();
		if(target==null) return false;

		Amputation A=(Amputation)target.fetchAffect("Amputation");
		boolean newOne=false;
		if(A==null){
			A=new Amputation();
			newOne=true;
		}

		long missingLimbList=A.missingLimbList();
		Vector V=new Vector();
		for(int i=0;i<Amputation.AMPUTATE_BITS;i++)
		{
			if(!Util.isSet((int)missingLimbList,i))
				V.addElement(new Integer(i));
		}
		if(V.size()==0)
		{
			if(!auto)
				mob.tell("There is nothing left on "+target.name()+" to cut off!");
			return false;
		}
		if(mob.isMonster())
		{
			bit=((Integer)V.elementAt(Dice.roll(1,V.size(),-1))).longValue();
			gone=Amputation.AMPUTATE_DESCS[(int)bit];
			code=Amputation.AMPUTATE_CODES[(int)bit];
		}
		else
		if(commands.size()<=0)
		{
			mob.tell("You must specify a body part to cut off.");
			StringBuffer str=new StringBuffer("Parts include: ");
			for(int i=0;i<V.size();i++)
				str.append(Amputation.AMPUTATE_DESCS[(int)((Integer)V.elementAt(i)).longValue()]+", ");
			mob.tell(str.toString().substring(0,str.length()-2)+".");
			return false;
		}
		else
		{
			for(int i=0;i<V.size();i++)
				if(Amputation.AMPUTATE_DESCS[(int)((Integer)V.elementAt(i)).longValue()].toLowerCase().startsWith(Util.combine(commands,0).toLowerCase()))
				{
					gone=Amputation.AMPUTATE_DESCS[(int)((Integer)V.elementAt(i)).longValue()];
					code=Amputation.AMPUTATE_CODES[(int)((Integer)V.elementAt(i)).longValue()];
					break;
				}
			if(gone.length()==0)
			{
				mob.tell("'"+Util.combine(commands,0)+"' is not a valid body part.");
				StringBuffer str=new StringBuffer("Parts include: ");
				for(int i=0;i<V.size();i++)
					str.append(Amputation.AMPUTATE_DESCS[(int)((Integer)V.elementAt(i)).longValue()]+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
				return false;
			}
		}
		
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if((success)&&(gone.length()>0))
		{
			if(mob.location().show(mob,target,this,(auto?Affect.MASK_GENERAL:0)|Affect.MASK_MALICIOUS|Affect.MSG_NOISYMOVEMENT,"^F<S-NAME> call(s) '"+gone+"'!^?"))
			{
				invoker=mob;
				beneficialAffect(mob,mob,2);
				mob.recoverEnvStats();
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> call(s) '"+gone+"', but fail(s) <S-HIS-HER> attack.");

		// return whether it worked
		return success;
	}
}
