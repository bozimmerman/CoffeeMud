package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/*
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public class Age extends StdAbility
{
	public String ID() { return "Age"; }
	public String name(){ return "Age";}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int classificationCode(){return Ability.PROPERTY;}
	public String accountForYourself(){return displayText();}
	public String displayText()
	{
		long start=Util.s_long(text());
		if(start<Integer.MAX_VALUE)
		    return "";
		long days=((System.currentTimeMillis()-start)/MudHost.TICK_TIME)/CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY); // down to days;
		long months=days/DefaultTimeClock.globalClock.getDaysInMonth();
		long years=months/DefaultTimeClock.globalClock.getMonthsInYear();
		if(days<1)
			return "(<1 day old)";
		else
		if(months<1)
			return "("+days+" day(s) old)";
		else
		if(years<1)
			return "("+months+" month(s) old)";
		else
			return "("+years+" year(s) old)";
	}
	private boolean norecurse=false;
	private Race myRace=null;
	private double divisor=0.0;
	private long lastSoiling=0;

	public final static String happyBabyEmoter="min=1 max=500 chance=10;makes goo goo noises.;loves its mommy.;loves its daddy.;smiles.;makes a spit bubble.;wiggles its toes.;chews on their finger.;holds up a finger.;stretches its little body.";
	public final static String otherBabyEmoter="min=1 max=5 chance=10;wants its mommy.;wants its daddy.;cries.;doesnt like you.;cries for its mommy.;cries for its daddy.";
	public final static String downBabyEmoter="min=1 max=2 chance=50;wants its mommy.;wants its daddy.;cries.;cries!;cries.";

	private void doThang()
	{
		if(affected==null) return;
		if(text().length()==0) return;
		long l=Util.s_long(text());
		if(l==0) return;
		if(norecurse) return;
		if(l<Integer.MAX_VALUE) return;
		norecurse=true;

		if(divisor==0.0)
		    divisor=new Integer(DefaultTimeClock.globalClock.getMonthsInYear()*DefaultTimeClock.globalClock.getDaysInMonth()*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)).doubleValue();
		
		int ellapsed=(int)Math.round(Math.floor(Util.div(Util.div(System.currentTimeMillis()-l,MudHost.TICK_TIME),divisor)));
		if((affected instanceof Item)&&(affected instanceof CagedAnimal))
		{
		    if(myRace==null)
		    {
		        MOB M=((CagedAnimal)affected).unCageMe();
		        if(M!=null)
			        myRace=M.baseCharStats().getMyRace();
		        else
		        {
		            Room R=CoffeeUtensils.roomLocation(affected);
		            if(R!=null)
		                R.showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" died.");
		            ((Item)affected).destroy();
		        }
	            M.destroy();
		    }
			if(ellapsed>=myRace.getAgingChart()[1])
			{
				Room R=CoffeeUtensils.roomLocation(affected);
				if(R!=null)
				{
					Item I=(Item)affected;
					MOB following=null;
					if(I.owner() instanceof MOB)
					{
						following=((MOB)I.owner());
						if(!Sense.isInTheGame(following,true))
						{
							norecurse=false;
							return;
						}
					}
							
					CagedAnimal C=(CagedAnimal)affected;
					MOB babe=C.unCageMe();
					if((babe==null)||(babe.baseCharStats()==null))
					{
						R.showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" JUST DIED OF DEFORMITIES!!");
						((Item)affected).destroy();
					}
					else
					{
						babe.baseCharStats().setStat(CharStats.CHARISMA,10);
						babe.baseCharStats().setStat(CharStats.CONSTITUTION,7);
						babe.baseCharStats().setStat(CharStats.DEXTERITY,3);
						babe.baseCharStats().setStat(CharStats.INTELLIGENCE,3);
						babe.baseCharStats().setStat(CharStats.STRENGTH,2);
						babe.baseCharStats().setStat(CharStats.WISDOM,2);
						babe.baseEnvStats().setHeight(babe.baseEnvStats().height()*2);
						babe.baseEnvStats().setWeight(babe.baseEnvStats().weight()*2);
						babe.baseState().setHitPoints(2);
						babe.baseState().setMana(10);
						babe.baseState().setMovement(20);
						if(following!=null)
							babe.setLiegeID(following.Name());
						babe.recoverCharStats();
						babe.recoverEnvStats();
						babe.recoverMaxState();
						Age A=(Age)babe.fetchEffect(ID());
						if(A!=null) A.setMiscText(text());
						Ability B=I.fetchEffect(ID());
						if(B!=null)	I.delEffect(B);
						Ability STAT=babe.fetchEffect("Prop_StatTrainer");
						if(STAT!=null)
							STAT.setMiscText("CHA=10 CON=7 DEX=3 INT=3 STR=2 WIS=2");
						babe.text();
						babe.bringToLife(R,true);
						BeanCounter.clearZeroMoney(babe,null);
						babe.setFollowing(following);
						R.show(babe,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> JUST TOOK <S-HIS-HER> FIRST STEPS!!!");
						I.destroy();
					}
				}
			}
		}
		else
		if((affected instanceof MOB)
		&&(((MOB)affected).amFollowing()!=null)
		&&(((MOB)affected).amFollowing().playerStats()!=null)
		&&(!((MOB)affected).amFollowing().isMonster())
		&&(((MOB)affected).location().isInhabitant((MOB)affected))
		&&(((MOB)affected).location().isInhabitant(((MOB)affected).amFollowing())))
		{
		    if(myRace==null) myRace=((MOB)affected).charStats().getMyRace();
			if((((MOB)affected).getLiegeID().length()==0)&&(!((MOB)affected).amFollowing().getLiegeID().equals(affected.Name())))
				((MOB)affected).setLiegeID(((MOB)affected).amFollowing().Name());
			((MOB)affected).setBitmap(Util.unsetb(((MOB)affected).getBitmap(),MOB.ATT_AUTOASSIST));
			if((ellapsed>=myRace.getAgingChart()[2])
			&&(((MOB)affected).fetchBehavior("MudChat")==null))
			{
				Room R=CoffeeUtensils.roomLocation(affected);
				if(R!=null)
				{
					MOB babe=(MOB)affected;
					if(babe.Name().indexOf(" ")>0)
					{
						babe.setName(Util.replaceAll(babe.Name()," baby "," young "));
						babe.setDisplayText(Util.replaceAll(babe.displayText()," baby "," young "));
					}
					babe.baseCharStats().setStat(CharStats.CHARISMA,10);
					babe.baseCharStats().setStat(CharStats.CONSTITUTION,10);
					babe.baseCharStats().setStat(CharStats.DEXTERITY,5);
					babe.baseCharStats().setStat(CharStats.INTELLIGENCE,6);
					babe.baseCharStats().setStat(CharStats.STRENGTH,6);
					babe.baseCharStats().setStat(CharStats.WISDOM,6);
					if(babe.amFollowing()!=null)
						babe.copyFactions(babe.amFollowing());
					babe.baseEnvStats().setHeight(babe.baseEnvStats().height()*5);
					babe.baseEnvStats().setWeight(babe.baseEnvStats().weight()*5);
					babe.baseState().setHitPoints(4);
					babe.baseState().setMana(25);
					babe.baseState().setMovement(50);
					Behavior B=CMClass.getBehavior("MudChat");
					if(B!=null)
						babe.addBehavior(B);
					else
						babe.delEffect(this);
					babe.recoverCharStats();
					babe.recoverEnvStats();
					babe.recoverMaxState();
					babe.text();
				}
			}
			else
			if((ellapsed>=myRace.getAgingChart()[3])
			&&(((MOB)affected).fetchBehavior("MudChat")!=null)
			&&(((MOB)affected).charStats().getStat(CharStats.INTELLIGENCE)>1))
			{
				Room R=CoffeeUtensils.roomLocation(affected);
				if((R!=null)&&(affected.Name().indexOf(" ")<0)&&(!CMClass.DBEngine().DBUserSearch(null,affected.Name())))
				{
					MOB babe=(MOB)affected;
					MOB liege=null;
					if(babe.getLiegeID().length()>0)
						liege=CMMap.getLoadPlayer(babe.getLiegeID());
					if(liege==null) liege=babe.amFollowing();
					MOB newMan=CMClass.getMOB("StdMOB");
					newMan.setAgeHours(babe.getAgeHours());
					newMan.setBaseCharStats(babe.baseCharStats());
					newMan.setBaseEnvStats(babe.baseEnvStats());
					if(liege!=null)	newMan.copyFactions(liege);
					newMan.baseEnvStats().setLevel(1);
					newMan.setBitmap(babe.getBitmap());
					newMan.setClanID(babe.getClanID());
					newMan.setDescription(babe.description());
					newMan.setDisplayText(babe.displayText());
					newMan.setExperience(babe.getExperience());
					newMan.setExpNextLevel(babe.getExpNextLevel());
					newMan.setFollowing(null);
					newMan.setLiegeID(babe.getLiegeID());
					newMan.setLocation(babe.location());
					BeanCounter.setMoney(newMan,BeanCounter.getMoney(babe));
					newMan.setName(babe.Name());
					newMan.setPlayerStats(new DefaultPlayerStats());
					newMan.setPractices(babe.getPractices());
					newMan.setQuestPoint(babe.getQuestPoint());
					newMan.setStartRoom(babe.getStartRoom());
					newMan.setTrains(babe.getTrains());
					newMan.setWimpHitPoint(babe.getWimpHitPoint());
					newMan.setWorshipCharID(babe.getWorshipCharID());
					newMan.playerStats().setPassword(liege.playerStats().password());
					newMan.playerStats().setEmail(liege.playerStats().getEmail());
					newMan.playerStats().setUpdated(System.currentTimeMillis());
					newMan.playerStats().setLastDateTime(System.currentTimeMillis());
					if(newMan.playerStats().getBirthday()==null)
					    newMan.baseCharStats().setStat(CharStats.AGE,newMan.playerStats().initializeBirthday(ellapsed*15,newMan.baseCharStats().getMyRace()));
					newMan.baseCharStats().setStat(CharStats.AGE,ellapsed);
					newMan.baseState().setHitPoints(CommonStrings.getIntVar(CommonStrings.SYSTEMI_STARTHP));
					newMan.baseState().setMana(CommonStrings.getIntVar(CommonStrings.SYSTEMI_STARTMANA));
					newMan.baseState().setMovement(CommonStrings.getIntVar(CommonStrings.SYSTEMI_STARTMOVE));
					newMan.baseCharStats().getMyRace().setHeightWeight(newMan.baseEnvStats(),(char)newMan.baseCharStats().getStat(CharStats.GENDER));
					newMan.baseCharStats().getMyRace().reRoll(newMan,newMan.baseCharStats());
					newMan.baseCharStats().getMyRace().startRacing(newMan,false);
					newMan.baseCharStats().setMyClasses(";Apprentice");
					newMan.baseCharStats().setMyLevels(";1");
					newMan.baseCharStats().getCurrentClass().startCharacter(newMan,false,false);
					for(int i=0;i<babe.inventorySize();i++)
						newMan.giveItem(babe.fetchInventory(i));
					CoffeeUtensils.outfit(newMan,newMan.baseCharStats().getMyRace().outfit());
					CoffeeUtensils.outfit(newMan,newMan.baseCharStats().getCurrentClass().outfit());
					for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
						newMan.baseCharStats().setStat(i,newMan.baseCharStats().getStat(i)+1);
					for(int i=CharStats.MAX_STRENGTH_ADJ;i<CharStats.MAX_STRENGTH_ADJ+CharStats.NUM_BASE_STATS;i++)
						newMan.baseCharStats().setStat(i,newMan.baseCharStats().getStat(i)+1);
					newMan.playerStats().setLastDateTime(System.currentTimeMillis());
					newMan.playerStats().setUpdated(System.currentTimeMillis());
					newMan.recoverCharStats();
					newMan.recoverEnvStats();
					newMan.recoverMaxState();
					newMan.resetToMaxState();
					CMClass.DBEngine().DBCreateCharacter(newMan);
					if(CMMap.getPlayer(newMan.Name())==null)
						CMMap.addPlayer(newMan);

					newMan.playerStats().setLastIP(liege.session().getAddress());
					Log.sysOut("Age","Created user: "+newMan.Name());
					for(int s=0;s<Sessions.size();s++)
					{
						Session S=Sessions.elementAt(s);
						if((S!=null)
						&&(S.mob()!=null)
						&&(Util.bset(S.mob().getBitmap(),MOB.ATT_AUTONOTIFY))
						&&(S.mob().playerStats()!=null)
						&&((S.mob().playerStats().getFriends().contains(newMan.Name())||S.mob().playerStats().getFriends().contains("All"))))
							S.mob().tell("^X"+newMan.Name()+" has just been created.^.^?");
					}
                    
                    Vector channels=ChannelSet.getFlaggedChannelNames("NEWPLAYERS");
                    for(int i=0;i<channels.size();i++)
                        CommonMsgs.channel((String)channels.elementAt(i),"",newMan.Name()+" has just been created.",true);
                    
					if(liege!=babe.amFollowing())
						babe.amFollowing().tell(newMan.Name()+" has just grown up! "+Util.capitalizeAndLower(newMan.baseCharStats().hisher())+" password is the same as "+liege.Name()+"'s.");
					liege.tell(newMan.Name()+" has just grown up! "+Util.capitalizeAndLower(newMan.baseCharStats().hisher())+" password is the same as "+liege.Name()+"'s.");
					CMClass.DBEngine().DBUpdatePlayer(newMan);
					newMan.removeFromGame();
					babe.setFollowing(null);
					babe.destroy();
					MOB fol=newMan.amFollowing();
					newMan.setFollowing(null);
					CMClass.DBEngine().DBUpdateFollowers(liege);
					newMan.setFollowing(fol);
				}
				else
				{
					MOB babe=(MOB)affected;
					MOB liege=null;
					if(babe.getLiegeID().length()>0)
						liege=CMMap.getLoadPlayer(babe.getLiegeID());
					if(liege==null) liege=babe.amFollowing();
					if(babe.Name().indexOf(" ")>0)
					{
						babe.setName(Util.replaceAll(babe.Name(),"young boy ","male "));
						babe.setName(Util.replaceAll(babe.Name(),"baby boy ","male "));
						babe.setName(Util.replaceAll(babe.Name(),"young girl ","female "));
						babe.setName(Util.replaceAll(babe.Name(),"baby girl ","female "));
						babe.setDisplayText(babe.Name()+" stands here.");
					}
					if(liege!=babe.amFollowing())
						babe.amFollowing().tell(babe.Name()+" has just grown up to be a mob.");
					liege.tell(babe.Name()+" has just grown up to be a mob.");
					Ability A=babe.fetchEffect(ID());
					A.setMiscText(""+ellapsed);
					babe.recoverCharStats();
					babe.recoverEnvStats();
					babe.recoverMaxState();
					babe.text();
				}
			}
		}
		norecurse=false;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof Item)&&((msg.target()==affected)||(msg.tool()==affected)))
		{
			Behavior B=affected.fetchBehavior("Emoter");
			if(B==null)
			{
				B=CMClass.getBehavior("Emoter");
				if(B!=null)
					affected.addBehavior(B);
			}
			if(((Item)affected).owner() instanceof Room)
			{ if(!B.getParms().equalsIgnoreCase(downBabyEmoter)) B.setParms(downBabyEmoter);}
			else
			{
				if(affected.description().toUpperCase().indexOf(((Item)affected).owner().name().toUpperCase())<=0)
				{ if(!B.getParms().equalsIgnoreCase(otherBabyEmoter)) B.setParms(otherBabyEmoter);}
				else
				{ if(!B.getParms().equalsIgnoreCase(happyBabyEmoter)) B.setParms(happyBabyEmoter);}
			}
		}
		if(((System.currentTimeMillis()-lastSoiling)>(IQCalendar.MILI_MINUTE*30))&&(Dice.rollPercentage()<10))
		{
		    if(lastSoiling==0)
			    lastSoiling=System.currentTimeMillis();
		    else
		    {
			    lastSoiling=System.currentTimeMillis();
			    boolean soil=(affected instanceof CagedAnimal);
			    MOB mob=null;
			    if(affected instanceof MOB)
			    {
			        mob=(MOB)affected;
				    if(myRace==null) myRace=((MOB)affected).charStats().getMyRace();
					if(divisor==0.0)
					    divisor=new Integer(DefaultTimeClock.globalClock.getMonthsInYear()*DefaultTimeClock.globalClock.getDaysInMonth()*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)).doubleValue();
					long l=Util.s_long(text());
					if((l>0)&&(l<Integer.MAX_VALUE))
					{
						int ellapsed=(int)Math.round(Math.floor(Util.div(Util.div(System.currentTimeMillis()-l,MudHost.TICK_TIME),divisor)));
						if(ellapsed>=myRace.getAgingChart()[2])
						    soil=true;
					}
			    }
			    if(invoker()!=null)
			        mob=invoker();
			    else
			    if((affected instanceof Item)&&(((Item)affected).owner() instanceof MOB))
			        mob=(MOB)((Item)affected).owner();
			    if((soil)&&(affected.fetchEffect("Soiled")==null)&&(mob!=null))
			    {
			        Ability A=CMClass.getAbility("Soiled");
			        if(A!=null) A.invoke(mob,affected,true,0);
			    }
		    }
		}
		doThang();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		doThang();
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		long l=Util.s_long(text());
		if((l<Integer.MAX_VALUE)&&(l>0))
		{
		    affected.baseCharStats().setStat(CharStats.AGE,(int)l);
		    affectableStats.setStat(CharStats.AGE,(int)l);
		}
		else
		{
			if(divisor==0.0)
			    divisor=new Integer(DefaultTimeClock.globalClock.getMonthsInYear()*DefaultTimeClock.globalClock.getDaysInMonth()*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)).doubleValue();
			affected.baseCharStats().setStat(CharStats.AGE,(int)Math.round(Math.floor(Util.div(Util.div(System.currentTimeMillis()-l,MudHost.TICK_TIME),divisor))));
			affectableStats.setStat(CharStats.AGE,affected.baseCharStats().getStat(CharStats.AGE));
		}
	}
}
