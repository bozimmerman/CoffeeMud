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
			long days=((System.currentTimeMillis()-start)/MudHost.TICK_TIME)/MudHost.TICKS_PER_MUDDAY; // down to days;
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
		if((tickID==MudHost.TICK_MOB)&&(affected!=null)&&(affected instanceof MOB))
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
					long days=((end-System.currentTimeMillis())/MudHost.TICK_TIME)/MudHost.TICKS_PER_MUDDAY; // down to days
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
							String race1=mob.baseCharStats().getMyRace().ID();
							char gender='F';
							String sondat="daughter";
							if(Dice.rollPercentage()>50){
								gender='M';
								sondat="son";
							}
							if(mob.session()!=null)
							{
								try{
									while(name.indexOf(" ")>=0)
									{
										n=mob.session().prompt("What would you like to name your "+sondat+"? ","").trim().toLowerCase();
										if(n.indexOf(" ")>=0)
											mob.tell("Spaces are not allowed in names! Please enter another one.");
										else
										if(n.length()!=0)
										{
											if(CMClass.DBEngine().DBUserSearch(null,Util.capitalize(n))!=null)
												mob.tell("That name is already taken.  Please enter a different one.");
											else
												name=Util.capitalize(n);
										}
									}
								}
								catch(java.io.IOException e){};
							}
							String desc="The "+sondat+" of "+mob.Name();
							String race2=mob.baseCharStats().getMyRace().ID();
							if(z>y)
							{
								race2=text().substring(z+1).trim();
								desc+=" and "+text().substring(y+1,z);
							}
							desc+=".";

							mob.location().show(mob,null,CMMsg.MSG_NOISE,"***** "+mob.name().toUpperCase()+" GIVE(S) BIRTH ******");
							Ability A=mob.fetchEffect(ID());
							if(A!=null) mob.delEffect(A);
							MOB babe=CMClass.getMOB("GenMOB");
							Race R=null;
							if(race1.equalsIgnoreCase(race2))
								R=CMClass.getRace(race1);
							else
							if(race1.equalsIgnoreCase("Human")||race2.equalsIgnoreCase("Human"))
							{
								String halfRace=(race1.equalsIgnoreCase("Human")?race2:race1);
								R=CMClass.getRace(halfRace);
								if((R!=null)&&(!R.ID().toUpperCase().startsWith("HALF")))
								{
									halfRace="Half"+Util.capitalize(R.ID().toLowerCase());
									Race testR=CMClass.getRace(halfRace);
									if(testR!=null)
										R=testR;
									else
									{
										Race H=CMClass.getRace("Human");
										Race GR=CMClass.getRace("GenRace").copyOf();
										GR.setRacialParms("<RACE><ID>"+halfRace+"</ID><NAME>"+"Half "+Util.capitalize(R.name())+"</NAME></RACE>");
										GR.setStat("CAT",R.racialCategory());
										GR.setStat("BWEIGHT",""+((R.lightestWeight()+H.lightestWeight())/2));
										GR.setStat("VWEIGHT",""+((R.weightVariance()+H.weightVariance())/2));
										GR.setStat("MHEIGHT",""+((R.shortestMale()+H.shortestMale())/2));
										GR.setStat("FHEIGHT",""+((R.shortestFemale()+H.shortestFemale())/2));
										GR.setStat("VHEIGHT",""+((R.heightVariance()+H.heightVariance())/2));
										GR.setStat("PLAYER","false");
										GR.setStat("LEAVE",R.leaveStr());
										GR.setStat("ARRIVE",R.arriveStr());
										GR.setStat("HEALTHRACE","Human");
										for(int i=0;i<Race.BODYPARTSTR.length;i++)
											if((R.bodyMask()[i]>0)&&(H.bodyMask()[i]>0))
												GR.bodyMask()[i]=((R.bodyMask()[i]+H.bodyMask()[i])/2);
											else
											if((R.bodyMask()[i]<0)&&(H.bodyMask()[i]>=0))
												GR.bodyMask()[i]=H.bodyMask()[i];
											else
											if((H.bodyMask()[i]<0)&&(R.bodyMask()[i]<0))
												GR.bodyMask()[i]=R.bodyMask()[i];
											else
											if((R.bodyMask()[i]==0)&&(H.bodyMask()[i]>=0))
												GR.bodyMask()[i]=H.bodyMask()[i];
											else
											if((H.bodyMask()[i]==0)&&(R.bodyMask()[i]>=0))
												GR.bodyMask()[i]=R.bodyMask()[i];
											else
												GR.bodyMask()[i]=R.bodyMask()[i];
										
										EnvStats RS=new DefaultEnvStats(0);
										R.affectEnvStats(babe,RS);
										RS.setAbility(RS.ability()/2);
										RS.setArmor(RS.armor()/2);
										RS.setAttackAdjustment(RS.attackAdjustment()/2);
										RS.setDamage(RS.damage()/2);
										RS.setHeight(RS.height()/2);
										RS.setSpeed(RS.speed()/2.0);
										RS.setWeight(RS.weight()/2);
										RS.setRejuv(0);
										GR.setStat("ESTATS",CoffeeMaker.getEnvStatsStr(RS));
										
										CharStats S1=new DefaultCharStats(0);
										CharStats S2=new DefaultCharStats(10);
										CharStats SETSTAT=new DefaultCharStats(0);
										CharStats ADJSTAT=new DefaultCharStats(0);
										R.affectCharStats(babe,S1);
										R.affectCharStats(babe,S2);
										for(int i=0;i<CharStats.NUM_STATS;i++)
										{
											if(i<CharStats.NUM_BASE_STATS)
											{
												if(S1.getStat(i)==S2.getStat(i))
													SETSTAT.setStat(i,(10+S1.getStat(i))/2);
												else
													ADJSTAT.setStat(i,S1.getStat(i)/2);
											}
											else
											if(i!=CharStats.GENDER)
												ADJSTAT.setStat(i,S1.getStat(i)/2);
										}
										GR.setStat("ASTATS",CoffeeMaker.getCharStatsStr(ADJSTAT));
										GR.setStat("CSTATS",CoffeeMaker.getCharStatsStr(SETSTAT));
										
										CharState CS=new DefaultCharState(0);
										R.affectCharState(babe,CS);
										CS.setFatigue(CS.getFatigue()/2);
										CS.setHitPoints(CS.getHitPoints()/2);
										CS.setHunger(CS.getHunger()/2);
										CS.setMana(CS.getMana()/2);
										CS.setMovement(CS.getMovement()/2);
										CS.setThirst(CS.getThirst()/2);
										GR.setStat("ASTATE",CoffeeMaker.getCharStateStr(CS));
										
										GR.setStat("NUMRSC","");
										for(int i=0;i<R.myResources().size();i++)
											GR.setStat("GETRSCID"+i,((Item)R.myResources().elementAt(i)).ID());
										for(int i=0;i<R.myResources().size();i++)
											GR.setStat("GETRSCPARM"+i,((Item)R.myResources().elementAt(i)).text());
										
										GR.setStat("NUMOFT","");
										if(R.outfit()!=null)
										{
											for(int i=0;i<R.outfit().size();i++)
												GR.setStat("GETOFTID"+i,((Item)R.outfit().elementAt(i)).ID());
											for(int i=0;i<R.outfit().size();i++)
												GR.setStat("GETOFTPARM"+i,((Item)R.outfit().elementAt(i)).text());
										}
										
										R.racialAbilities(null);
										Vector data=CMAble.getUpToLevelListings(R.ID(),Integer.MAX_VALUE,true,false);
										// kill half of them.
										for(int i=1;i<data.size();i++)
											data.removeElementAt(i);
										
										if(data.size()>0)
											GR.setStat("NUMRABLE",""+data.size());
										else
											GR.setStat("NUMRABLE","");
										for(int i=0;i<data.size();i++)
										{
											GR.setStat("GETRABLE"+i,(String)data.elementAt(i));
											GR.setStat("GETRABLELVL"+i,""+CMAble.getQualifyingLevel(R.ID(),(String)data.elementAt(i)));
											GR.setStat("GETRABLEQUAL"+i,""+CMAble.getDefaultGain(R.ID(),(String)data.elementAt(i)));
											GR.setStat("GETRABLEPROF"+i,""+CMAble.getDefaultProfficiency(R.ID(),(String)data.elementAt(i)));
										}
										
										CMClass.addRace(GR);
										CMClass.DBEngine().DBCreateRace(GR.ID(),GR.racialParms());
										R=GR;
									}
								}
							}
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
					{
						// pregnant folk get fatigued more often.
						mob.curState().adjFatigue(months*100,mob.maxState());
						if((months<=1)&&(Dice.rollPercentage()==1))
							mob.tell("Ouch! -- You had a labor pain!");
						else
						if((months<=3)&&(Dice.rollPercentage()==1)&&(Dice.rollPercentage()==1))
							mob.tell("You feel a kick in your gut.");
						else
						if((months>8)&&(mob.location()!=null)&&(mob.location().getArea().getTimeOfDay()<2)&&(Dice.rollPercentage()==1))
						{
							if(Dice.rollPercentage()>25)
								mob.tell("You feel really sick this morning.");
							else
								mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"**BLEH** <S-NAME> just threw up.");
						}
					}
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
		boolean success=profficiencyCheck(mob,0,auto);
		long start=System.currentTimeMillis();
		long add=((10)*(30)*MudHost.TICKS_PER_MUDDAY*MudHost.TICK_TIME);
		long end=start+add;
		if(success)
		{
			setMiscText(start+"/"+end+"/"+mob.Name()+"/"+mob.charStats().getMyRace().ID());
			target.addNonUninvokableEffect(this);
		}
		return success;
	}
}
