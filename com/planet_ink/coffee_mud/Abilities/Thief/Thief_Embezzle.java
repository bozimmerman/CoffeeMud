package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Embezzle extends ThiefSkill
{
	public String ID() { return "Thief_Embezzle"; }
	public String name(){ return "Embezzle";}
	public String displayText(){return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"EMBEZZLE"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Embezzle();}
	public Vector mobs=new Vector();

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Embezzle money from whose accounts?");
			return false;
		}
		MOB target=mob.location().fetchInhabitant(Util.combine(commands,0));
		if((target==null)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) 
			target=(MOB)givenTarget;
		if((target==null)||(target.amDead())||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+Util.combine(commands,1)+"' here.");
			return false;
		}
		if(!(target instanceof Banker))
		{
			mob.tell("You can't embezzle from "+target.name()+"'s accounts.");
			return false;
		}
		Banker bank=(Banker)target;
		Ability A=target.fetchAffect(ID());
		if(A!=null)
		{
			mob.tell(target.name()+" is watching "+target.charStats().hisher()+" books too closely.");
			return false;
		}
		int levelDiff=target.envStats().level()-mob.envStats().level();

		
		if(((!target.mayIFight(mob))&&(levelDiff<10)))
		{
			mob.tell("You cannot embezzle from "+target.charStats().himher()+".");
			return false;
		}
		
		Item myCoins=bank.findDepositInventory(mob.name(),"1");
		if((myCoins==null)||(!(myCoins instanceof Coins)))
		{
			mob.tell("You don't have your own account with "+target.name()+".");
			return false;
		}
		Vector accounts=bank.getAccountNames();
		String victim="";
		Vector choices=new Vector();
		for(int i=0;i<accounts.size();i++)
		{
			String name=(String)accounts.elementAt(i);
			Item coins=bank.findDepositInventory(name,"1");
			if((coins!=null)&&(coins instanceof Coins)&&((((Coins)coins).numberOfCoins()/50)>0))
				choices.addElement(name);
		}
		if(choices.size()==0)
		{
			mob.tell(target.name()+" doesn't seem to maintain any accounts worth embezzling from.");
			return false;
		}
		victim=(String)choices.elementAt(Dice.roll(1,choices.size(),-1));
		Coins coins=(Coins)bank.findDepositInventory(victim,"1");
		int amount=coins.numberOfCoins()/50;
		int classLevel=mob.charStats().getClassLevel("Burglar");
		if((classLevel>0)
		&&(amount>(1000*classLevel)))
		   amount=(1000*classLevel);
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(-(levelDiff),auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,(auto?Affect.MASK_GENERAL:0)|Affect.MSG_THIEF_ACT,"<S-NAME> embezzle(s) "+amount+" gold from the "+victim+" account maintained by <T-NAME>.");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,((Host.TIME_TICK_DELAY*Area.A_FULL_DAY)/Host.TICK_TIME));
				bank.delDepositInventory(victim,coins);
				coins.setNumberOfCoins(coins.numberOfCoins()-amount);
				bank.addDepositInventory(victim,coins);
				bank.delDepositInventory(mob.name(),coins);
				((Coins)myCoins).setNumberOfCoins(((Coins)myCoins).numberOfCoins()+amount);
				bank.addDepositInventory(mob.name(),coins);
			}
		}
		else
			maliciousFizzle(mob,target,"<T-NAME> catch(es) <S-NAME> trying to embezzle money!");
		return success;
	}

}
