package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Poison_Alcohol extends Poison
{
	public String ID() { return "Poison_Alcohol"; }
	public String name(){ return "Alcohol";}
	private static final String[] triggerStrings = {"POISONALCOHOL"};
	public String displayText(){ return "(Drunk)";}
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Poison_Alcohol();}
	
	protected int POISON_TICKS(){return 35;}
	protected int POISON_DELAY(){return 1;}
	protected String POISON_DONE(){return "You feel sober again.";}
	protected String POISON_START(){return "^G<S-NAME> burp(s)!^?";}
	protected String POISON_AFFECT(){return "";}
	protected String POISON_CAST(){return "^F<S-NAME> inebriate(s) <T-NAMESELF>!^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to inebriate <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return 0;}
	protected boolean disableHappiness=false;
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof MOB)
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(((MOB)affected).envStats().level()));
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(affectableStats.getStat(CharStats.DEXTERITY)-3));
		if(affectableStats.getStat(CharStats.DEXTERITY)<=0)
			affectableStats.setStat(CharStats.DEXTERITY,1);
	}
	
	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((Dice.rollPercentage()==1)&&(!((MOB)affected).isMonster()))
			{
				Ability A=CMClass.getAbility("Disease_Migraines");
				if((A!=null)&&(mob.fetchAffect(A.ID())==null))
					A.invoke(mob,mob,true);
			}
			ExternalPlay.standIfNecessary(mob);
		}
		super.unInvoke();
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		
		if(disableHappiness){disableHappiness=false; return true;}
		
		MOB mob=(MOB)affected;
		if(mob==null) return true;

		Room room=mob.location();
		if((Dice.rollPercentage()<20)&&(Sense.aliveAwakeMobile(mob,true))&&(room!=null))
		{
			if(mob.getAlignment()<350)
			switch(Dice.roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around making ugly faces.");
				break;
			case 1:
				room.show(mob,null,this,Affect.MSG_NOISE,"<S-NAME> belch(es) grotesquely.");
				break;
			case 2:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> spin(s) <S-HIS-HER> head around.");
				break;
			case 3:
				room.show(mob,null,this,Affect.MSG_NOISE,"<S-NAME> can't stop snarling.");
				break;
			case 4:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> just fell over!");
				break;
			case 5:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 6:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 7:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> <S-IS-ARE> definitely sh** faced!");
				break;
			case 8:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly at the ground.");
				break;
			}
			else
			if(mob.getAlignment()<650)
			switch(Dice.roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around aimlessly.");
				break;
			case 1:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> burp(s) noncommitally.");
				break;
			case 2:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 3:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 4:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> almost fell over.");
				break;
			case 5:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> hiccup(s) and almost smile(s).");
				break;
			case 6:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> belch(es)!");
				break;
			case 7:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> <S-IS-ARE> definitely drunk!");
				break;
			case 8:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly ahead.");
				break;
			}
			else
			switch(Dice.roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around trying to hug everyone.");
				break;
			case 1:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> hiccup(s) and smile(s).");
				break;
			case 2:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> bob(s) <S-HIS-HER> head back and forth.");
				break;
			case 3:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> can't stop smiling.");
				break;
			case 4:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> lean(s) slightly to one side.");
				break;
			case 5:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 6:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 7:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> <S-IS-ARE> definitely a bit tipsy!");
				break;
			case 8:
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly at <S-HIS-HER> eyelids.");
				break;
			}

		}
		return true;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;
		if(affected instanceof MOB)
		{
			if(affect.source()!=affected)
				return true;
			if(affect.source().location()==null)
				return true;
			
			if((affect.amISource((MOB)affected))
			&&(affect.sourceMessage()!=null)
			&&(affect.tool()==null)
			&&((affect.sourceMinor()==Affect.TYP_SPEAK)
			   ||(affect.sourceMinor()==Affect.TYP_TELL)
			   ||(Util.bset(affect.sourceCode(),Affect.MASK_CHANNEL))))
			{
				Ability A=CMClass.getAbility("Drunken");
				if(A!=null)
				{
					A.setProfficiency(100);
					A.invoke(affect.source(),null,true);
					A.setAffectedOne(affect.source());
					if(!A.okAffect(myHost,affect))
						return false;
				}
			}
			else
			if((!Util.bset(affect.targetMajor(),Affect.MASK_GENERAL))
			&&(affect.targetMajor()>0))
			{
				if((affect.target() !=null)
					&&(affect.target() instanceof MOB))
						affect.modify(affect.source(),affect.source().location().fetchInhabitant(Dice.roll(1,affect.source().location().numInhabitants(),0)-1),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode(),affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			}
		}
		else
		{
			
		}
		return true;
	}
}
