package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Pregnancy extends StdAbility
{
	public String ID() { return "Pregnancy"; }
	public String name(){ return "Pregnancy";}
	public String displayText()
	{
		int x=text().indexOf("/");
		if(x>0)
		{
			int y=text().indexOf("/",x+1);
			if(y<0) return "";
			long start=Util.s_long(text().substring(0,x));
			long days=((System.currentTimeMillis()-start)/Host.TICK_TIME)/Host.TICKS_PER_MUDDAY; // down to days;
			long months=days/30;
			if(days<1)
				return "(<1 day pregnant)";
			else
			if(months<1)
				return "("+days+" day(s) pregnant)";
			else
				return "("+months+" month(s) pregnant)";
		}
		return "";
	}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Pregnancy();}
	public int quality(){return Ability.INDIFFERENT;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"IMPREGNATE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean canBeUninvoked(){return false;}
	public boolean isAutoInvoked(){return true;}
	public int classificationCode(){return Ability.PROPERTY;}
	private boolean labor=false;
	private int ticksInLabor=0;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((tickID==Host.TICK_MOB)&&(affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			int x=text().indexOf("/");
			if(x>0)
			{
				int y=text().indexOf("/",x+1);
				if(y>x)
				{
					int z=text().indexOf("/",y+1);
					long end=Util.s_long(text().substring(x+1,y));
					long days=((end-System.currentTimeMillis())/Host.TICK_TIME)/Host.TICKS_PER_MUDDAY; // down to days
					long months=days/30; // down to months
					labor=false;
					if(days<7) // BIRTH!
					{
						if((Dice.rollPercentage()>50)&&(mob.charStats().getStat(CharStats.INTELLIGENCE)>5))
							mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> moan(s) and scream(s) in labor pain!!");
						labor=true;
						ticksInLabor++;
						if(ticksInLabor>45)
						{
							String name=mob.Name()+" jr.";
							Vector races=new Vector();
							races.addElement(mob.baseCharStats().getMyRace().ID());
							char gender='F';
							String sondat="daughter";
							if(Dice.rollPercentage()>50){
								gender='M';
								sondat="son";
							}
							if(mob.session()!=null)
							{
								try{
									String n=mob.session().prompt("What would you like to name your "+sondat+"? ","");
									if(n.trim().length()!=0)
										name=Util.capitalize(n);
								}
								catch(java.io.IOException e){};
							}
							String desc="The "+sondat+" of "+mob.Name();
							if(z>y)
							{
								races.addElement(text().substring(z+1));
								desc+=" and "+text().substring(y+1,z);
							}
							desc+=".";

							mob.location().show(mob,null,CMMsg.MSG_NOISE,"***** "+mob.name().toUpperCase()+" GIVE(S) BIRTH ******");
							Ability A=mob.fetchEffect(ID());
							if(A!=null) mob.delEffect(A);
							MOB babe=CMClass.getMOB("GenMOB");
							Race R=CMClass.getRace((String)races.elementAt(Dice.roll(1,races.size(),-1)));
							if(R==null) R=mob.baseCharStats().getMyRace();
							babe.setName(name);
							babe.setAlignment(1000);
							babe.setClanID(mob.getClanID());
							babe.setLeigeID(mob.getLeigeID());
							babe.setDescription(desc);
							babe.setDisplayText(name+" is here");
							babe.setMoney(0);
							babe.baseCharStats().setMyRace(R);
							babe.baseCharStats().setStat(CharStats.CHARISMA,10);
							babe.baseCharStats().setStat(CharStats.CONSTITUTION,6);
							babe.baseCharStats().setStat(CharStats.DEXTERITY,2);
							babe.baseCharStats().setStat(CharStats.GENDER,(int)gender);
							babe.baseCharStats().setStat(CharStats.INTELLIGENCE,2);
							babe.baseCharStats().setStat(CharStats.STRENGTH,1);
							babe.baseCharStats().setStat(CharStats.WISDOM,1);
							babe.baseCharStats().getMyRace().startRacing(babe,false);
							babe.baseEnvStats().setHeight(babe.baseEnvStats().height()/10);
							babe.baseEnvStats().setWeight(babe.baseEnvStats().weight()/10);
							babe.baseState().setHitPoints(1);
							babe.baseState().setMana(0);
							babe.baseState().setMovement(0);
							Ability A3=CMClass.getAbility("Age");
							A3.setMiscText(""+System.currentTimeMillis());
							babe.addNonUninvokableEffect(A3);
							babe.recoverCharStats();
							babe.recoverEnvStats();
							babe.recoverMaxState();
							babe.resetToMaxState();
							Item I=CMClass.getItem("GenCaged");
							((CagedAnimal)I).cageMe(babe);
							I.baseEnvStats().setAbility(1);
							I.addNonUninvokableEffect(A3);
							I.recoverEnvStats();
							mob.location().addItem(I);
							Behavior B=CMClass.getBehavior("Emoter");
							B.setParms("min=1 max=500 chance=10;crys.;wants its mommy.;smiles.");
							I.addBehavior(B);
							I.text();
						}
						else
							mob.tell("You are in labor!!");

					}
					else
					if((months<=1)&&(Dice.rollPercentage()<2))
						mob.tell("Ouch! -- You had a nasty labor pain.");
					else
					if((months<=3)&&(Dice.rollPercentage()==1))
						mob.tell("You had a labor pain.");
				}
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);
		long start=System.currentTimeMillis();
		long add=((10)*(30)*Host.TICKS_PER_MUDDAY*Host.TICK_TIME);
		long end=start+add;
		if(success)
		{
			setMiscText(start+"/"+end+"/"+mob.Name()+"/"+mob.charStats().getMyRace().ID());
			target.addNonUninvokableEffect(this);
		}
		return success;
	}
}
