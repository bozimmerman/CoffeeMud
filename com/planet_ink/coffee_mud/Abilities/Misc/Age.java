package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Age extends StdAbility
{
	public String ID() { return "Age"; }
	public String name(){ return "Age";}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int classificationCode(){return Ability.ACODE_PROPERTY;}
	public String accountForYourself(){return displayText();}
	public String displayText()
	{
		long start=CMath.s_long(text());
		if(start<Short.MAX_VALUE) return "";
		long days=((System.currentTimeMillis()-start)/Tickable.TIME_TICK)/CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY); // down to days;
		long months=days/CMLib.time().globalClock().getDaysInMonth();
		long years=months/CMLib.time().globalClock().getMonthsInYear();
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
	protected boolean norecurse=false;
    protected Race myRace=null;
    protected double divisor=0.0;
    protected long lastSoiling=0;

	public final static String happyBabyEmoter="min=1 max=500 chance=100;makes goo goo noises.;loves its mommy.;loves its daddy.;smiles.;makes a spit bubble.;wiggles its toes.;chews on their finger.;holds up a finger.;stretches its little body.";
	public final static String otherBabyEmoter="min=1 max=5 chance=10;wants its mommy.;wants its daddy.;cries.;doesnt like you.;cries for its mommy.;cries for its daddy.";
	public final static String downBabyEmoter="min=1 max=2 chance=50;wants its mommy.;wants its daddy.;cries.;cries!;cries.";

	protected Race getMyRace()
	{
	    if((myRace==null)&&(affected != null))
	    {
	    	if(affected instanceof CagedAnimal)
	    	{
		        MOB M=((CagedAnimal)affected).unCageMe();
		        if(M!=null)
		        {
			        myRace=M.baseCharStats().getMyRace();
		            M.delEffect(M.fetchEffect(ID()));
		            M.destroy();
		        }
		        else
		        {
		            Room R=CMLib.map().roomLocation(affected);
		            if(R!=null)
		                R.showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" died.");
		            ((Item)affected).destroy();
		        }
	    	}
	    	else
	    	if(affected instanceof MOB)
	    		myRace=((MOB)affected).charStats().getMyRace();
	    }
	    return myRace;
	}
	
    protected MOB getFollowing(Environmental babe)
    {
        MOB following=null;
        if(babe instanceof MOB)
            following=((MOB)babe).amFollowing();
        else
        if((babe instanceof Item)
        &&(((Item)babe).owner() instanceof MOB)
        &&(CMLib.flags().isInTheGame(((Item)babe).owner(),true)))
            following=(MOB)((Item)babe).owner();
        Room room=CMLib.map().roomLocation(babe);
        if((following!=null)&&(babe.description().toUpperCase().indexOf(following.Name().toUpperCase())<0)&&(room!=null))
        {
            MOB M=null;
            Vector choices=new Vector();
            for(int i=0;i<room.numInhabitants();i++)
            {
                M=room.fetchInhabitant(i);
                if((M!=null)
                &&(M!=babe)
                &&(M!=following)
                &&(babe.description().toUpperCase().indexOf(following.Name().toUpperCase())>=0))
                {
                    if(M.isMonster())
                        choices.addElement(M);
                    else
                    if(choices.size()==0)
                        choices.addElement(M);
                    else
                        choices.insertElementAt(M,0);
                }
            }
            if(choices.size()>0)
            {
                if(babe instanceof MOB)
                    ((MOB)babe).setFollowing((MOB)choices.firstElement());
                following=(MOB)choices.firstElement();
            }
        }
        return following;
    }


    protected void doThang()
	{
		if(affected==null) return;
		if(text().length()==0) return;
		long l=CMath.s_long(text());
		if(l==0) return;
		if(norecurse) return;
		if(l<Short.MAX_VALUE) return;
		norecurse=true;

		if(divisor==0.0)
		    divisor = (double)( CMLib.time().globalClock().getMonthsInYear() *
		                           CMLib.time().globalClock().getDaysInMonth() *
		                           CMProps.getIntVar( CMProps.SYSTEMI_TICKSPERMUDDAY ) );

		int ellapsed=(int)Math.round(Math.floor(CMath.div(CMath.div(System.currentTimeMillis()-l,Tickable.TIME_TICK),divisor)));
		if((affected instanceof Item)&&(affected instanceof CagedAnimal))
		{
            ((Item)affected).setExpirationDate(0);
            if(getMyRace()==null) return;
			if(ellapsed>=myRace.getAgingChart()[1])
			{
				Room R=CMLib.map().roomLocation(affected);
				if(R!=null)
				{
					Item I=(Item)affected;
					MOB following=getFollowing(I);
					if(following==null)
					{
						norecurse=false;
						return;
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
						babe.baseCharStats().setStat(CharStats.STAT_CHARISMA,10);
						babe.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,7);
						babe.baseCharStats().setStat(CharStats.STAT_DEXTERITY,3);
						babe.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,3);
						babe.baseCharStats().setStat(CharStats.STAT_STRENGTH,2);
						babe.baseCharStats().setStat(CharStats.STAT_WISDOM,2);
						babe.baseEnvStats().setHeight(babe.baseEnvStats().height()*2);
						babe.baseEnvStats().setWeight(babe.baseEnvStats().weight()*2);
						babe.baseState().setHitPoints(2);
						babe.baseState().setMana(10);
						babe.baseState().setMovement(20);
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
						CMLib.beanCounter().clearZeroMoney(babe,null);
						babe.setFollowing(following);
						R.show(babe,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> JUST TOOK <S-HIS-HER> FIRST STEPS!!!");
						I.destroy();
                        CMLib.database().DBReCreateData(following.Name(),"HEAVEN",following.Name()+"/HEAVEN/"+text(),babe.ID()+"/"+babe.baseEnvStats().ability()+"/"+babe.text());
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
			MOB babe=(MOB)affected;
            MOB following=getFollowing(babe);
            if(getMyRace()==null) return;
			if((babe.getLiegeID().length()==0)&&(!following.getLiegeID().equals(affected.Name())))
				babe.setLiegeID(following.Name());
			babe.setBitmap(CMath.unsetb(babe.getBitmap(),MOB.ATT_AUTOASSIST));
			if((ellapsed>=myRace.getAgingChart()[2])
			&&(babe.fetchBehavior("MudChat")==null))
			{
				Room R=CMLib.map().roomLocation(affected);
				if(R!=null)
				{
					if(babe.Name().indexOf(" ")>0)
					{
						babe.setName(CMStrings.replaceAll(babe.Name()," baby "," young "));
						babe.setDisplayText(CMStrings.replaceAll(babe.displayText()," baby "," young "));
					}
					babe.baseCharStats().setStat(CharStats.STAT_CHARISMA,10);
					babe.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,10);
					babe.baseCharStats().setStat(CharStats.STAT_DEXTERITY,5);
					babe.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,6);
					babe.baseCharStats().setStat(CharStats.STAT_STRENGTH,6);
					babe.baseCharStats().setStat(CharStats.STAT_WISDOM,6);
					if(following!=null)
						babe.copyFactions(following);
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
                    if(following!=null)
                        CMLib.database().DBReCreateData(following.Name(),"HEAVEN",following.Name()+"/HEAVEN/"+text(),babe.ID()+"/"+babe.baseEnvStats().ability()+"/"+babe.text());
				}
			}
			else
			if((ellapsed>=myRace.getAgingChart()[3])
			&&(babe.fetchBehavior("MudChat")!=null)
			&&(babe.charStats().getStat(CharStats.STAT_INTELLIGENCE)>1))
			{
				Ability A=babe.fetchEffect("Prop_SafePet");
				if(A!=null)babe.delEffect(A);
                CMLib.database().DBDeleteData(following.Name(),"HEAVEN",following.Name()+"/HEAVEN/"+text());

				Room R=CMLib.map().roomLocation(affected);
				if((R!=null)
				&&(affected.Name().indexOf(" ")<0)
				&&(!CMLib.players().playerExists(affected.Name())))
				{
					MOB liege=null;
					if(babe.getLiegeID().length()>0)
						liege=CMLib.players().getLoadPlayer(babe.getLiegeID());
					if(liege==null) liege=babe.amFollowing();
					MOB newMan=CMClass.getMOB("StdMOB");
					newMan.setAgeHours(babe.getAgeHours());
					newMan.setBaseCharStats(babe.baseCharStats());
					newMan.setBaseEnvStats(babe.baseEnvStats());
					if(liege!=null)	newMan.copyFactions(liege);
					newMan.baseEnvStats().setLevel(1);
					newMan.setBitmap(babe.getBitmap());
                    for(int t=0;t<babe.numTattoos();t++)
                        newMan.addTattoo(babe.fetchTattoo(t));
                    if(babe.getClanID().length()>0)
                        newMan.setClanID(babe.getClanID());
                    else
                    {
                        for(int t=0;t<newMan.numTattoos();t++)
                        {
                            String tattoo = newMan.fetchTattoo(t);
                            if(tattoo.startsWith("PARENT:"))
                            {
                                MOB M=CMLib.players().getLoadPlayer(tattoo);
                                if((M!=null)&&(M.getClanID().length()>0))
                                {
                                    newMan.setClanID(M.getClanID());
                                    break;
                                }
                            }
                        }
                        if((newMan.getClanID().length()==0)
                        &&(liege!=null)
                        &&(liege.getClanID().length()>0))
                            newMan.setClanID(liege.getClanID());
                    }
                    if(newMan.getClanID().length()>0)
                    {
                        newMan.setClanRole(Clan.POS_MEMBER);
	                    Clan C = CMLib.clans().findClan(newMan.getClanID());
	                    if(C!=null) C.addMember(newMan, Clan.POS_MEMBER);
                    }
					newMan.setDescription(babe.description());
					newMan.setDisplayText(babe.displayText());
					newMan.setExperience(babe.getExperience());
					newMan.setFollowing(null);
					newMan.setLiegeID(babe.getLiegeID());
					newMan.setLocation(babe.location());
					CMLib.beanCounter().setMoney(newMan,CMLib.beanCounter().getMoney(babe));
					newMan.setName(babe.Name());
					newMan.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
					newMan.setPractices(babe.getPractices());
					newMan.setQuestPoint(babe.getQuestPoint());
					newMan.setStartRoom(babe.getStartRoom());
					newMan.setTrains(babe.getTrains());
					newMan.setWimpHitPoint(babe.getWimpHitPoint());
					newMan.setWorshipCharID(babe.getWorshipCharID());
					if(liege!=null)
					{
						newMan.playerStats().setPassword(liege.playerStats().password());
						newMan.playerStats().setEmail(liege.playerStats().getEmail());
						newMan.playerStats().setAccount(liege.playerStats().getAccount());
					}
					else
						newMan.playerStats().setPassword(babe.Name());
					newMan.playerStats().setLastUpdated(System.currentTimeMillis());
					newMan.playerStats().setLastDateTime(System.currentTimeMillis());
					if(newMan.playerStats().getBirthday()==null)
					    newMan.baseCharStats().setStat(CharStats.STAT_AGE,newMan.playerStats().initializeBirthday(ellapsed*15,newMan.baseCharStats().getMyRace()));
					newMan.baseCharStats().setStat(CharStats.STAT_AGE,ellapsed);
					newMan.baseState().setHitPoints(CMProps.getIntVar(CMProps.SYSTEMI_STARTHP));
					newMan.baseState().setMana(CMProps.getIntVar(CMProps.SYSTEMI_STARTMANA));
					newMan.baseState().setMovement(CMProps.getIntVar(CMProps.SYSTEMI_STARTMOVE));
					newMan.baseCharStats().getMyRace().setHeightWeight(newMan.baseEnvStats(),(char)newMan.baseCharStats().getStat(CharStats.STAT_GENDER));
					CMLib.login().reRollStats(newMan,newMan.baseCharStats());
					newMan.baseCharStats().getMyRace().startRacing(newMan,false);
					newMan.baseCharStats().setMyClasses(";Apprentice");
					newMan.baseCharStats().setMyLevels(";1");
					newMan.baseCharStats().getCurrentClass().startCharacter(newMan,false,false);
					for(int i=0;i<babe.inventorySize();i++)
						newMan.giveItem(babe.fetchInventory(i));
					CMLib.utensils().outfit(newMan,newMan.baseCharStats().getMyRace().outfit(newMan));
					CMLib.utensils().outfit(newMan,newMan.baseCharStats().getCurrentClass().outfit(newMan));
					Vector<Integer> qualifiedStats = new Vector<Integer>();
					for(int i : CharStats.CODES.MAX())
						if(newMan.baseCharStats().getStat(i)<CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT)+7)
							qualifiedStats.addElement(Integer.valueOf(i));
					if(qualifiedStats.size()>0)
					{
						int stat=qualifiedStats.elementAt(CMLib.dice().roll(1,qualifiedStats.size(),-1)).intValue();
						newMan.baseCharStats().setStat(stat,newMan.baseCharStats().getStat(stat)+1);
					}
					for(int i : CharStats.CODES.BASE())
						if(newMan.baseCharStats().getStat(i)<CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT))
							newMan.baseCharStats().setStat(i,newMan.baseCharStats().getStat(i)+1);
					newMan.playerStats().setLastDateTime(System.currentTimeMillis());
					newMan.playerStats().setLastUpdated(System.currentTimeMillis());
					newMan.recoverCharStats();
					newMan.recoverEnvStats();
					newMan.recoverMaxState();
					newMan.resetToMaxState();
                    if(CMLib.flags().isAnimalIntelligence(newMan))
                    {
                        newMan.baseCharStats().setMyClasses(";StdCharClass");
                        newMan.recoverCharStats();
                        newMan.recoverEnvStats();
                        newMan.recoverMaxState();
                        newMan.resetToMaxState();
                    }
					CMLib.database().DBCreateCharacter(newMan);
					CMLib.players().addPlayer(newMan);

					if((liege != null) && (liege.session() != null))
						newMan.playerStats().setLastIP(liege.session().getAddress());
					Log.sysOut("Age","Created user: "+newMan.Name());
		            CMLib.login().notifyFriends(newMan,"^X"+newMan.Name()+" has just been created.^.^?");

                    Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.NEWPLAYERS);
                    for(int i=0;i<channels.size();i++)
                        CMLib.commands().postChannel((String)channels.elementAt(i),newMan.getClanID(),newMan.Name()+" has just been created.",true);

                    if(liege != null)
                    {
						if(liege!=babe.amFollowing())
							babe.amFollowing().tell(newMan.Name()+" has just grown up! "+CMStrings.capitalizeAndLower(newMan.baseCharStats().hisher())+" password is the same as "+liege.Name()+"'s.");
						liege.tell(newMan.Name()+" has just grown up! "+CMStrings.capitalizeAndLower(newMan.baseCharStats().hisher())+" password is the same as "+liege.Name()+"'s.");
                    }
					CMLib.database().DBUpdatePlayer(newMan);
					newMan.removeFromGame(false,true);
					babe.setFollowing(null);
					babe.destroy();
					MOB fol=newMan.amFollowing();
					newMan.setFollowing(null);
					CMLib.database().DBUpdateFollowers(liege);
					newMan.setFollowing(fol);
				}
				else
				{
					MOB liege=null;
					if(babe.getLiegeID().length()>0)
						liege=CMLib.players().getLoadPlayer(babe.getLiegeID());
					if(liege==null) liege=babe.amFollowing();
					if(babe.Name().indexOf(" ")>0)
					{
						babe.setName(CMStrings.replaceAll(babe.Name(),"young boy ","male "));
						babe.setName(CMStrings.replaceAll(babe.Name(),"baby boy ","male "));
						babe.setName(CMStrings.replaceAll(babe.Name(),"young girl ","female "));
						babe.setName(CMStrings.replaceAll(babe.Name(),"baby girl ","female "));
						babe.setDisplayText(babe.Name()+" stands here.");
					}
                    CMLib.database().DBDeleteData(following.Name(),"HEAVEN",following.Name()+"/HEAVEN/"+text());
					if(liege!=babe.amFollowing())
						babe.amFollowing().tell(babe.Name()+" has just grown up to be a mob.");
					liege.tell(babe.Name()+" has just grown up to be a mob.");
					A=babe.fetchEffect(ID());
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
		if((affected!=null)
		&&(!affected.amDestroyed()))
		{
            if(getMyRace()==null) return;
            if((msg.target()==affected)
            &&(msg.targetMinor()==CMMsg.TYP_EXAMINE))
            {
                if((((affected instanceof Item)&&(affected instanceof CagedAnimal))
                    ||((affected instanceof MOB)&&(!((MOB)affected).savable())))
                &&(affected.description().toUpperCase().indexOf(msg.source().name().toUpperCase())>=0))
                {
                    if(divisor==0.0)
                        divisor = (double)( CMLib.time().globalClock().getMonthsInYear() *
                                            CMLib.time().globalClock().getDaysInMonth() *
                                            CMProps.getIntVar( CMProps.SYSTEMI_TICKSPERMUDDAY ) );
                    long l=CMath.s_long(text());
                    if((l>0)&&(l<Long.MAX_VALUE))
                    {
                        int ellapsed=(int)Math.round(Math.floor(CMath.div(CMath.div(System.currentTimeMillis()-l,Tickable.TIME_TICK),divisor)));
                        if(ellapsed<=myRace.getAgingChart()[3])
                        {
                            String s=displayText();
                            if(s.startsWith("("))s=s.substring(1);
                            if(s.endsWith(")"))s=s.substring(0,s.length()-1);
                            msg.source().tell(Name()+" is "+s);
                        }
                    }
                }
            }
			if((affected instanceof Item)
			&&((msg.target()==affected)||(msg.tool()==affected))
            &&(CMLib.flags().isInTheGame(affected,true)))
			{
				Behavior B=affected.fetchBehavior("Emoter");
				Item baby=(Item)affected;
				if(B==null)
				{
					B=CMClass.getBehavior("Emoter");
					if(B!=null)
						baby.addBehavior(B);
				}
				// no else please
				if(B!=null)
				{
					if(baby.owner() instanceof Room)
					{
						if(!B.getParms().equalsIgnoreCase(downBabyEmoter))
							B.setParms(downBabyEmoter);
					}
					else
					if(baby.owner()!=null)
					{
						Environmental o=baby.owner();
						if(baby.description().toUpperCase().indexOf(o.name().toUpperCase())<0)
						{
							if(!B.getParms().equalsIgnoreCase(otherBabyEmoter))
								B.setParms(otherBabyEmoter);
						}
						else
						{
							if(!B.getParms().equalsIgnoreCase(happyBabyEmoter))
								B.setParms(happyBabyEmoter);
						}
					}
				}
			}
			if(((System.currentTimeMillis()-lastSoiling)>(TimeManager.MILI_MINUTE*30))&&(CMLib.dice().rollPercentage()<10))
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
			            if(getMyRace()==null) return;
						if(divisor==0.0)
						    divisor = (double)( CMLib.time().globalClock().getMonthsInYear() *
						                        CMLib.time().globalClock().getDaysInMonth() *
						                        CMProps.getIntVar( CMProps.SYSTEMI_TICKSPERMUDDAY ) );
						long l=CMath.s_long(text());
						if((l>0)&&(l<Long.MAX_VALUE))
						{
							int ellapsed=(int)Math.round(Math.floor(CMath.div(CMath.div(System.currentTimeMillis()-l,Tickable.TIME_TICK),divisor)));
							if(ellapsed<=myRace.getAgingChart()[2])
							    soil=true;
						}
				    }
				    if(invoker()!=null)
				        mob=invoker();
				    else
				    if((affected instanceof Item)&&(((Item)affected).owner() instanceof MOB))
				        mob=(MOB)((Item)affected).owner();
                    if((mob==null)&&(((Item)affected).owner() instanceof Room))
                        mob=((Room)((Item)affected).owner()).fetchInhabitant(0);

				    if((soil)&&(affected.fetchEffect("Soiled")==null)&&(mob!=null))
				    {
				        Ability A=CMClass.getAbility("Soiled");
				        if(A!=null) A.invoke(mob,affected,true,0);
				    }
			    }
			}
			doThang();
		}
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		doThang();
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		long l=CMath.s_long(text());
		if((l<Short.MAX_VALUE)&&(l>0))
		{
		    affected.baseCharStats().setStat(CharStats.STAT_AGE,(int)l);
		    affectableStats.setStat(CharStats.STAT_AGE,(int)l);
		}
		else
		{
			if(divisor==0.0)
			    divisor = (double)( CMLib.time().globalClock().getMonthsInYear() *
			                        CMLib.time().globalClock().getDaysInMonth() *
			                        CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY ) );
            int age=(int)Math.round(Math.floor(CMath.div(CMath.div(System.currentTimeMillis()-l,Tickable.TIME_TICK),divisor)));
            if((age>=Short.MAX_VALUE)||(age<0))
                Log.errOut("Age","Recorded, on "+affected.name()+", age of "+age+", from tick values (("+System.currentTimeMillis()+"-"+l+")/4000)/"+divisor);
            else
            {
    			affected.baseCharStats().setStat(CharStats.STAT_AGE,age);
    			affectableStats.setStat(CharStats.STAT_AGE,affected.baseCharStats().getStat(CharStats.STAT_AGE));
            }
		}
	}
}
