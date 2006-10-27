package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.GenLiquidResource;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;
import java.io.IOException;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class CommonMsgs extends StdLibrary implements CommonCommands
{
    public String ID(){return "CommonMsgs";}
    
    protected final static int LOOK_LONG=0;
    protected final static int LOOK_NORMAL=1;
    protected final static int LOOK_BRIEFOK=2;
    protected String unknownCommand(){return "Huh?";}

	public boolean handleUnknownCommand(MOB mob, Vector command)
	{
		if(mob==null) return false;
		Room R=mob.location();
		if(R==null){ mob.tell(unknownCommand()); return false;}
		CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_HUH,unknownCommand(),CMParms.combine(command,0),null);
		if(!R.okMessage(mob,msg)) return false;
		R.send(mob,msg);
		return true;
	}
	
	public boolean doStandardCommand(MOB mob, String command, Vector parms)
	{
		try
		{
			Command C=CMClass.getCommand(command);
			if(C!=null)
				return C.execute(mob,parms);
		}
		catch(IOException e)
		{
			Log.errOut("CommonMsgs",e);
		}
		return false;
	}

	public StringBuffer getScore(MOB mob)
	{
		Vector V=new Vector();
		doStandardCommand(mob,"Score",V);
		if((V.size()==1)&&(V.firstElement() instanceof StringBuffer))
			return (StringBuffer)V.firstElement();
		return new StringBuffer("");
	}
	public StringBuffer getEquipment(MOB viewer, MOB mob)
	{
		Vector V=new Vector();
		V.addElement(viewer);
		doStandardCommand(mob,"Equipment",V);
		if((V.size()>1)&&(V.elementAt(1) instanceof StringBuffer))
			return (StringBuffer)V.elementAt(1);
		return new StringBuffer("");
	}
	public StringBuffer getInventory(MOB viewer, MOB mob)
	{
		Vector V=new Vector();
		V.addElement(viewer);
		doStandardCommand(mob,"Inventory",V);
		if((V.size()>1)&&(V.elementAt(1) instanceof StringBuffer))
			return (StringBuffer)V.elementAt(1);
		return new StringBuffer("");
	}
	
	public void postChannel(MOB mob,
						    String channelName, 
						    String message, 
						    boolean systemMsg)
	{
		doStandardCommand(mob,"Channel",
						  CMParms.makeVector(new Boolean(systemMsg),channelName,message));
	}
	
	public void postChannel(String channelName,
						    String clanID, 
						    String message, 
						    boolean systemMsg)
	{
        MOB talker=CMClass.getMOB("StdMOB");
		talker.setName("^?");
		talker.setLocation(CMLib.map().getRandomRoom());
		talker.baseEnvStats().setDisposition(EnvStats.IS_GOLEM);
        talker.envStats().setDisposition(EnvStats.IS_GOLEM);
		talker.setClanID(clanID);
		postChannel(talker,channelName,message,systemMsg);
        talker.destroy();
	}

	public boolean postDrop(MOB mob, Environmental dropThis, boolean quiet, boolean optimized)
	{
		return doStandardCommand(mob,"Drop",CMParms.makeVector(dropThis,new Boolean(quiet),new Boolean(optimized)));
	}
    public boolean postDrop(MOB mob, Environmental dropThis, Room dropRoom, boolean quiet, boolean optimized)
    {
        return doStandardCommand(mob,"Drop",CMParms.makeVector(dropThis,new Boolean(quiet),new Boolean(optimized),dropRoom));
    }
	public boolean postGet(MOB mob, Item container, Item getThis, boolean quiet)
	{
		if(container==null)
			return doStandardCommand(mob,"Get",CMParms.makeVector(getThis,new Boolean(quiet)));
		return doStandardCommand(mob,"Get",CMParms.makeVector(getThis,container,new Boolean(quiet)));
	}
	
	public boolean postRemove(MOB mob, Item item, boolean quiet)
	{
		if(quiet)
			return doStandardCommand(mob,"Remove",CMParms.makeVector("REMOVE",item,"QUIETLY"));
		return doStandardCommand(mob,"Remove",CMParms.makeVector("REMOVE",item));
	}
	
	public void postLook(MOB mob, boolean quiet)
	{
		if(quiet)
			doStandardCommand(mob,"Look",CMParms.makeVector("LOOK","UNOBTRUSIVELY"));
		else
			doStandardCommand(mob,"Look",CMParms.makeVector("LOOK"));
	}

	public void postFlee(MOB mob, String whereTo)
	{
		doStandardCommand(mob,"Flee",CMParms.makeVector("FLEE",whereTo));
	}

	public void postSheath(MOB mob, boolean ifPossible)
	{
		if(ifPossible)
			doStandardCommand(mob,"Sheath",CMParms.makeVector("SHEATH","IFPOSSIBLE"));
		else
			doStandardCommand(mob,"Sheath",CMParms.makeVector("SHEATH"));
	}
	
	public void postDraw(MOB mob, boolean doHold, boolean ifNecessary)
	{
		if(ifNecessary)
		{
			if(doHold)
				doStandardCommand(mob,"Draw",CMParms.makeVector("DRAW","HELD","IFNECESSARY"));
			else
				doStandardCommand(mob,"Draw",CMParms.makeVector("DRAW","IFNECESSARY"));
		}
		else
			doStandardCommand(mob,"Draw",CMParms.makeVector("DRAW"));
	}
	
	public void postStand(MOB mob, boolean ifNecessary)
	{
		if(ifNecessary)
			doStandardCommand(mob,"Stand",CMParms.makeVector("STAND","IFNECESSARY"));
		else
			doStandardCommand(mob,"Stand",CMParms.makeVector("STAND"));
	}

	public void postFollow(MOB follower, MOB leader, boolean quiet)
	{
		if(leader!=null)
		{
			if(quiet)
				doStandardCommand(follower,"Follow",CMParms.makeVector("FOLLOW",leader,"UNOBTRUSIVELY"));
			else
				doStandardCommand(follower,"Follow",CMParms.makeVector("FOLLOW",leader));
		}
		else
		{
			if(quiet)
				doStandardCommand(follower,"Follow",CMParms.makeVector("FOLLOW","SELF","UNOBTRUSIVELY"));
			else
				doStandardCommand(follower,"Follow",CMParms.makeVector("FOLLOW","SELF"));
		}
	}

    public void postSay(MOB mob, MOB target,String text){ postSay(mob,target,text,false,false);}
    public void postSay(MOB mob, String text){ postSay(mob,null,text,false,false);}
	public void postSay(MOB mob,
    				    MOB target,
    				    String text,
    				    boolean isPrivate,
    				    boolean tellFlag)
	{
		Room location=mob.location();
		text=CMProps.applyINIFilter(text,CMProps.SYSTEM_SAYFILTER);
		if(target!=null)
			location=target.location();
		if(location==null) return;
		if((isPrivate)&&(target!=null))
		{
			if(tellFlag)
			{
				String targetName=target.name();
				if(targetName.indexOf("@")>=0)
				{
					String mudName=targetName.substring(targetName.indexOf("@")+1);
					targetName=targetName.substring(0,targetName.indexOf("@"));
					if((!(CMLib.intermud().i3online()))&&(!(CMLib.intermud().imc2online())))
						mob.tell("Intermud is unavailable.");
					else
						CMLib.intermud().i3tell(mob,targetName,mudName,text);
				}
				else
				{
					boolean ignore=((target.playerStats()!=null)&&(target.playerStats().getIgnored().contains(mob.Name())));
					CMMsg msg=null;
					if((!CMLib.flags().isSeen(mob))||(!CMLib.flags().isSeen(target)))
						msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_TELL,"^t^<TELL \""+target.name()+"\"^>You tell <T-NAME> '"+text+"'^</TELL^>^?^.",CMMsg.MSG_TELL,"^t^<TELL \""+mob.Name()+"\"^><S-NAME> tell(s) you '"+text+"'^</TELL^>^?^.",CMMsg.NO_EFFECT,null);
					else
						msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_TELL,"^t^<TELL \""+target.name()+"\"^>You tell "+target.name()+" '"+text+"'^</TELL^>^?^.",CMMsg.MSG_TELL,"^t^<TELL \""+mob.Name()+"\"^>"+mob.Name()+" tell(s) you '"+text+"'^</TELL^>^?^.",CMMsg.NO_EFFECT,null);
					if((mob.location().okMessage(mob,msg))
					&&((ignore)||(target.okMessage(target,msg))))
					{
						mob.executeMsg(mob,msg);
						if((mob!=target)&&(!ignore))
						{
							target.executeMsg(target,msg);
							if(msg.trailerMsgs()!=null)
							{
								for(int i=0;i<msg.trailerMsgs().size();i++)
								{
									CMMsg msg2=(CMMsg)msg.trailerMsgs().elementAt(i);
									if((msg!=msg2)&&(target.okMessage(target,msg2)))
										target.executeMsg(target,msg2);
								}
								msg.trailerMsgs().clear();
							}
							if(mob.playerStats()!=null)
							{
								mob.playerStats().setReplyTo(target);
								mob.playerStats().addTellStack(CMLib.coffeeFilter().fullOutFilter(mob.session(),mob,mob,target,null,CMStrings.removeColors(msg.sourceMessage()),false));
							}
							if(target.playerStats()!=null)
							{
								target.playerStats().setReplyTo(mob);
								target.playerStats().addTellStack(CMLib.coffeeFilter().fullOutFilter(target.session(),target,mob,target,null,CMStrings.removeColors(msg.targetMessage()),false));
							}
						}
					}
				}
			}
			else
			{
				CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T^<SAY \""+((target==null)?mob.name():target.name())+"\"^><S-NAME> say(s) '"+text+"'"+((target==null)?"^</SAY^>":" to <T-NAMESELF>.^</SAY^>^?"),CMMsg.MSG_SPEAK,"^T^<SAY \""+mob.name()+"\"^><S-NAME> say(s) '"+text+"'"+((target==null)?"^</SAY^>":" to <T-NAMESELF>.^</SAY^>^?"),CMMsg.NO_EFFECT,null);
				if(location.okMessage(mob,msg))
					location.send(mob,msg);
			}
		}
		else
		if(!isPrivate)
		{
		    String str="<S-NAME> say(s) '"+text+"'"+((target==null)?"^</SAY^>":" to <T-NAMESELF>.^</SAY^>^?");
			CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T^<SAY \""+((target==null)?mob.name():target.name())+"\"^>"+str,"^T^<SAY \""+mob.name()+"\"^>"+str,"^T^<SAY \""+mob.name()+"\"^>"+str);
			if(location.okMessage(mob,msg))
				location.send(mob,msg);
		}
	}
    
    public void handleBeingSniffed(CMMsg msg)
    {
        if(msg.target() instanceof Room)
            handleBeingRoomSniffed(msg);
        else
        if(msg.target() instanceof Item)
            handleBeingItemSniffed(msg);
        else
        if(msg.target() instanceof MOB)
            handleBeingMobSniffed(msg);
    }
    
    public void handleBeingMobSniffed(CMMsg msg)
    {
        if(!(msg.target() instanceof MOB)) return;
        MOB sniffingmob=msg.source();
        MOB sniffedmob=(MOB)msg.target();
        if((sniffedmob.playerStats()!=null)
        &&(sniffedmob.soulMate()==null)
        &&(sniffedmob.playerStats().getHygiene()>=PlayerStats.HYGIENE_DELIMIT))
        {
            int x=(int)(sniffedmob.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT);
            if(x<=1) 
                sniffingmob.tell(sniffedmob.displayName(sniffingmob)+" has a slight aroma about "+sniffedmob.charStats().himher()+"."); 
            else
            if(x<=3) 
                sniffingmob.tell(sniffedmob.displayName(sniffingmob)+" smells pretty sweaty."); 
            else
            if(x<=7) 
                sniffingmob.tell(sniffedmob.displayName(sniffingmob)+" stinks pretty bad.");
            else
            if(x<15) 
                sniffingmob.tell(sniffedmob.displayName(sniffingmob)+" smells most foul.");
            else 
                sniffingmob.tell(sniffedmob.displayName(sniffingmob)+" reeks of noxious odors.");
        }
    }
    
    public void handleSit(CMMsg msg)
    {
        MOB sittingmob=msg.source();
        int oldDisposition=sittingmob.baseEnvStats().disposition();
        oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
        sittingmob.baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SITTING);
        sittingmob.recoverEnvStats();
        sittingmob.recoverCharStats();
        sittingmob.recoverMaxState();
        sittingmob.tell(sittingmob,msg.target(),msg.tool(),msg.sourceMessage());
    }
    public void handleSleep(CMMsg msg)
    {
        MOB sleepingmob=msg.source();
        int oldDisposition=sleepingmob.baseEnvStats().disposition();
        oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
        sleepingmob.baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SLEEPING);
        sleepingmob.recoverEnvStats();
        sleepingmob.recoverCharStats();
        sleepingmob.recoverMaxState();
        sleepingmob.tell(sleepingmob,msg.target(),msg.tool(),msg.sourceMessage());
    }
    public void handleStand(CMMsg msg)
    {
        MOB standingmob=msg.source();
        int oldDisposition=standingmob.baseEnvStats().disposition();
        oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
        standingmob.baseEnvStats().setDisposition(oldDisposition);
        standingmob.recoverEnvStats();
        standingmob.recoverCharStats();
        standingmob.recoverMaxState();
        standingmob.tell(standingmob,msg.target(),msg.tool(),msg.sourceMessage());
    }
    
    public void handleRecall(CMMsg msg)
    {
        MOB recallingmob=msg.source();
        if((msg.target()!=null) 
        &&(msg.target() instanceof Room)
        &&(recallingmob.location() != msg.target()))
        {
            recallingmob.tell(msg.source(),null,msg.tool(),msg.targetMessage());
            recallingmob.location().delInhabitant(recallingmob);
            ((Room)msg.target()).addInhabitant(recallingmob);
            ((Room)msg.target()).showOthers(recallingmob,null,CMMsg.MSG_ENTER,"<S-NAME> appears out of the Java Plain.");
    
            recallingmob.setLocation(((Room)msg.target()));
            if((recallingmob.riding()!=null)&&(recallingmob.location()!=CMLib.map().roomLocation(recallingmob.riding())))
            {
            	if(recallingmob.riding().mobileRideBasis())
                {
                    if(recallingmob.riding() instanceof Item)
                        recallingmob.location().bringItemHere((Item)recallingmob.riding(),-1,true);
                    else
                    if(recallingmob.riding() instanceof MOB)
                        recallingmob.location().bringMobHere((MOB)recallingmob.riding(),true);
                }
                else
                    recallingmob.setRiding(null);
            }
            recallingmob.recoverEnvStats();
            recallingmob.recoverCharStats();
            recallingmob.recoverMaxState();
            postLook(recallingmob,true);
        }
    }

    public int tickManaConsumption(MOB mob, int manaConsumeCounter)
    {
        if((CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMETIME)>0)
        &&(CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMEAMT)>0)
        &&((--manaConsumeCounter)<=0))
        {
            Vector expenseAffects=new Vector();
            manaConsumeCounter=CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMETIME);
            for(int a=0;a<mob.numAllEffects();a++)
            {
                Ability A=mob.fetchEffect(a);
                if(A!=null)
                {
                    if((!A.isAutoInvoked())
                    &&(A.canBeUninvoked())
                    &&(A.displayText().length()>0)
                    &&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
                        ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
                        ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
                        ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
                    &&(A.usageCost(mob)[0]>0))
                        expenseAffects.addElement(A);
                }
            }
            if((expenseAffects!=null)&&(expenseAffects.size()>0))
            {
                int basePrice=1;
                switch(CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMEAMT))
                {
                case -100: basePrice=basePrice*mob.envStats().level(); break;
                case -200:
                    {
                        int total=0;
                        for(int a1=0;a1<expenseAffects.size();a1++)
                        {
                            int lql=CMLib.ableMapper().lowestQualifyingLevel(((Ability)expenseAffects.elementAt(a1)).ID());
                            if(lql>0)
                                total+=lql;
                            else
                                total+=1;
                        }
                        basePrice=basePrice*(total/expenseAffects.size());
                    }
                    break;
                default:
                    basePrice=basePrice*CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMEAMT);
                    break;
                }

                if(expenseAffects!=null)
                {
                    // 1 per tick per level per msg.  +1 to the affects so that way it's about
                    // 3 cost = 1 regen... :)
                    int reallyEat=basePrice*(expenseAffects.size()+1);
                    while(mob.curState().getMana()<reallyEat)
                    {
                        mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> strength of will begins to crumble.");
                        //pick one and kill it
                        Ability A=(Ability)expenseAffects.elementAt(CMLib.dice().roll(1,expenseAffects.size(),-1));
                        A.unInvoke();
                        expenseAffects.remove(A);
                        reallyEat=basePrice*expenseAffects.size();
                    }
                    if(reallyEat>0)
                        mob.curState().adjMana( -reallyEat, mob.maxState());
                }
            }
        }
        return manaConsumeCounter;
    }
    
    public void tickAging(MOB mob)
    {
        mob.setAgeHours(mob.getAgeHours()+1); // this is really minutes
        if((mob.baseCharStats().getStat(CharStats.STAT_AGE)>0)
        &&(mob.playerStats()!=null)
        &&(mob.playerStats().getBirthday()!=null)
        &&((mob.getAgeHours()%20)==0))
        {
            int tage=mob.baseCharStats().getMyRace().getAgingChart()[Race.AGE_YOUNGADULT]
                    +CMClass.globalClock().getYear()
                    -mob.playerStats().getBirthday()[2];
            int month=CMClass.globalClock().getMonth();
            int day=CMClass.globalClock().getDayOfMonth();
            int bday=mob.playerStats().getBirthday()[0];
            int bmonth=mob.playerStats().getBirthday()[1];
            while((tage>mob.baseCharStats().getStat(CharStats.STAT_AGE))
            &&((month>bmonth)||((month==bmonth)&&(day>=bday))))
            {
                if(!CMSecurity.isAllowed(mob,mob.location(),"IMMORT"))
                {
                    if((month==bmonth)&&(day==bday))
                        mob.tell("Happy Birthday!");
                    mob.baseCharStats().setStat(CharStats.STAT_AGE,mob.baseCharStats().getStat(CharStats.STAT_AGE)+1);
                    mob.recoverCharStats();
                    mob.recoverEnvStats();
                    mob.recoverMaxState();
                }
                else
                {
                    mob.playerStats().getBirthday()[2]++;
                    tage--;
                }
            }
            if(!CMSecurity.isAllowed(mob,mob.location(),"IMMORT"))
            {
                if((mob.baseCharStats().ageCategory()>=Race.AGE_VENERABLE)
                &&(CMLib.dice().rollPercentage()==1)
                &&(CMLib.dice().rollPercentage()==1))
                {
                    Ability A=CMClass.getAbility("Disease_Cancer");
                    if((A!=null)&&(mob.fetchEffect(A.ID())==null))
                        A.invoke(mob,mob,true,0);
                }
                else
                if((mob.baseCharStats().ageCategory()>=Race.AGE_ANCIENT)
                &&(CMLib.dice().rollPercentage()==1))
                {
                    Ability A=CMClass.getAbility("Disease_Arthritis");
                    if((A!=null)&&(mob.fetchEffect(A.ID())==null))
                        A.invoke(mob,mob,true,0);
                }
                else
                if((mob.baseCharStats().ageCategory()>=Race.AGE_ANCIENT)
                &&(CMLib.dice().rollPercentage()==1))
                {
                    Ability A=CMClass.getAbility("Disease_Alzheimers");
                    if((A!=null)&&(mob.fetchEffect(A.ID())==null))
                        A.invoke(mob,mob,true,0);
                }
                else
                if(CMLib.dice().rollPercentage()<10)
                {
                    int max=CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
                    for(int i=CharStats.STAT_MAX_STRENGTH_ADJ;i<CharStats.STAT_MAX_STRENGTH_ADJ+CharStats.NUM_BASE_STATS;i++)
                        if((max+mob.charStats().getStat(i))<=0)
                        {
                            mob.tell("Your max "+CharStats.STAT_DESCS[i].toLowerCase()+" has fallen below 1!");
                            CMLib.combat().postDeath(null,mob,null);
                            break;
                        }
                }
            }
        }
    }
    
    protected String relativeCharStatTest(CharStats C, 
                                          MOB mob, 
                                          String weakword, 
                                          String strongword, 
                                          int stat)
    {
        double d=CMath.div(C.getStat(stat),mob.charStats().getStat(stat));
        String prepend="";
        if((d<=0.5)||(d>=3.0)) prepend="much ";
        if(d>=1.6) return mob.charStats().HeShe()+" appears "+prepend+weakword+" than the average "+mob.charStats().raceName()+".\n\r";
        if(d<=0.67) return mob.charStats().HeShe()+" appears "+prepend+strongword+" than the average "+mob.charStats().raceName()+".\n\r";
        return "";
    }
    
    public void handleBeingLookedAt(CMMsg msg)
    {
        if(msg.target() instanceof Room)
            handleBeingRoomLookedAt(msg);
        else
        if(msg.target() instanceof Item)
            handleBeingItemLookedAt(msg);
        else
        if(msg.target() instanceof MOB)
            handleBeingMobLookedAt(msg);
        else
        if(msg.target() instanceof Exit)
            handleBeingExitLookedAt(msg);
    }

    public String examineItemString(MOB mob, Item item)
    {
        StringBuffer response=new StringBuffer("");
        String level=null;
        if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
        {
            int l=(int)Math.round(Math.floor(CMath.div(item.envStats().level(),10.0)));
            level=(l*10)+"-"+((l*10)+9);
        }
        else
        if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<18))
        {
            int l=(int)Math.round(Math.floor(CMath.div(item.envStats().level(),5.0)));
            level=(l*5)+"-"+((l*5)+4);
        }
        else
            level=""+item.envStats().level();
        double divider=100.0;
        if(item.envStats().weight()<10)
            divider=4.0;
        else
        if(item.envStats().weight()<50)
            divider=10.0;
        else
        if(item.envStats().weight()<150)
            divider=20.0;
        String weight=null;
        if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
        {
            double l=Math.floor(CMath.div(item.envStats().level(),divider));
            weight=(int)Math.round(CMath.mul(l,divider))+"-"+(int)Math.round(CMath.mul(l,divider)+(divider-1.0));
        }
        else
        if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<18))
        {
            divider=divider/2.0;
            double l=Math.floor(CMath.div(item.envStats().level(),divider));
            weight=(int)Math.round(CMath.mul(l,divider))+"-"+(int)Math.round(CMath.mul(l,divider)+(divider-1.0));
        }
        else
            weight=""+item.envStats().weight();
        if(item instanceof CagedAnimal)
        {
	        MOB M=((CagedAnimal)item).unCageMe();
	        if(M==null)
	            response.append("\n\rLooks like some sort of lifeless thing.\n\r");
	        else
	        {
	            if(M.envStats().height()>0)
	            	response.append("\n\r"+CMStrings.capitalizeFirstLetter(item.name())+" is "+M.envStats().height()+" inches tall and weighs "+weight+" pounds.\n\r");
	            if(!mob.isMonster())
	            	response.append(CMProps.mxpImage(M," ALIGN=RIGHT H=70 W=70"));
	            response.append(M.healthText(mob)+"\n\r\n\r");
	            if(!M.description().equalsIgnoreCase(item.description()))
		            response.append(M.description()+"\n\r\n\r");
	        }
        }
        else
        {
	        response.append("\n\r"+CMStrings.capitalizeFirstLetter(item.name())+" is a level "+level+" item, and weighs "+weight+" pounds.  ");
	        if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
	            response.append("It is mostly made of a kind of "+RawMaterial.MATERIAL_NOUNDESCS[(item.material()&RawMaterial.MATERIAL_MASK)>>8].toLowerCase()+".  ");
	        else
	            response.append("It is mostly made of "+RawMaterial.RESOURCE_DESCS[(item.material()&RawMaterial.RESOURCE_MASK)].toLowerCase()+".  ");
	        if((item instanceof Weapon)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10))
	            response.append("It is a "+CMStrings.capitalizeAndLower(Weapon.classifictionDescription[((Weapon)item).weaponClassification()])+" class weapon that does "+CMStrings.capitalizeAndLower(Weapon.typeDescription[((Weapon)item).weaponType()])+" damage.  ");
	        else
	        if((item instanceof Armor)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10))
	        {
	            if(item.envStats().height()>0)
	                response.append(" It is a size "+item.envStats().height()+", and is ");
	            else
	                response.append(" It is your size, and is ");
	            response.append(((item.rawProperLocationBitmap()==Item.WORN_HELD)||(item.rawProperLocationBitmap()==(Item.WORN_HELD|Item.WORN_WIELD)))
	            					 ?new StringBuffer("")
	            					 :new StringBuffer("worn on the "));
	            for(int l=0;l<Item.WORN_CODES.length;l++)
	            {
	                int wornCode=1<<l;
	                if(CMath.bset(item.rawProperLocationBitmap(),wornCode))
	                {
	                	String wornString=CMLib.flags().wornLocation(wornCode);
		                if(wornString.length()>0)
	 	                {
		                	response.append(CMStrings.capitalizeAndLower(wornString)+" ");
	                        if(item.rawLogicalAnd())
	                        	response.append("and ");
	                        else
	                        	response.append("or ");
	                    }
	                }
	            }
	            if(response.toString().endsWith(" and "))
	                response.delete(response.length()-5,response.length());
	            else
	            if(response.toString().endsWith(" or "))
	                response.delete(response.length()-4,response.length());
	            response.append(".  ");
	        }
        }
        return response.toString();
    }
    
    protected String dispossessionTimeLeftString(Item item)
    {
        if(item.expirationDate()==0)
            return "N/A";
        return ""+(item.expirationDate()-System.currentTimeMillis());
    }

    
    protected void handleBeingItemLookedAt(CMMsg msg)
    {
        MOB mob=msg.source();
        Item item=(Item)msg.target();
        if(!CMLib.flags().canBeSeenBy(item,mob)) 
        {
            mob.tell("You can't see that!");
            return;
        }
        
        StringBuffer buf=new StringBuffer("");
        if(CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
            buf.append(item.ID()+"\n\rRejuv :"+item.baseEnvStats().rejuv()
		                    +"\n\rType  :"+item.ID()
                            +"\n\rUses  :"+item.usesRemaining()
                            +"\n\rHeight:"+item.baseEnvStats().height()
                            +"\n\rAbilty:"+item.baseEnvStats().ability()
                            +"\n\rLevel :"+item.baseEnvStats().level()
                            +"\n\rTime  : "+dispossessionTimeLeftString(item)
                            +"\n\rMisc  :'"+item.text());
        if(item.description().length()==0)
            buf.append("You don't see anything special about "+item.name());
        else
            buf.append(item.description());
        if(msg.targetMinor()==CMMsg.TYP_EXAMINE)
            buf.append(examineItemString(mob,item));
        if(item instanceof Container)
        {
            buf.append("\n\r");
            Container contitem=(Container)item;
            if((contitem.isOpen())&&((contitem.capacity()>0)||(contitem.getContents().size()>0)))
            {
                buf.append(item.name()+" contains:^<!ENTITY container \""+item.name()+"\"^>"+(CMath.bset(mob.getBitmap(),MOB.ATT_COMPRESS)?" ":"\n\r"));
                Vector newItems=new Vector();
                if((item instanceof Drink)&&(((Drink)item).liquidRemaining()>0))
                {
                    GenLiquidResource l=new GenLiquidResource();
                    int myResource=((Drink)item).liquidType();
                    l.setMaterial(myResource);
                    ((Drink)l).setLiquidType(myResource);
                    l.setBaseValue(RawMaterial.RESOURCE_DATA[myResource&RawMaterial.RESOURCE_MASK][1]);
                    l.baseEnvStats().setWeight(1);
                    String name=RawMaterial.RESOURCE_DESCS[myResource&RawMaterial.RESOURCE_MASK].toLowerCase();
                    l.setName("some "+name);
                    l.setDisplayText("some "+name+" sits here.");
                    l.setDescription("");
                    l.recoverEnvStats();
                    newItems.addElement(l);
                }
            
                if(item.owner() instanceof MOB)
                {
                    MOB M=(MOB)item.owner();
                    for(int i=0;i<M.inventorySize();i++)
                    {
                        Item item2=M.fetchInventory(i);
                        if((item2!=null)&&(item2.container()==item))
                            newItems.addElement(item2);
                    }
                    buf.append(CMLib.lister().lister(mob,newItems,true,"CMItem","",false,CMath.bset(mob.getBitmap(),MOB.ATT_COMPRESS)));
                }
                else
                if(item.owner() instanceof Room)
                {
                    Room room=(Room)item.owner();
                    if(room!=null)
                    for(int i=0;i<room.numItems();i++)
                    {
                        Item item2=room.fetchItem(i);
                        if((item2!=null)&&(item2.container()==item))
                            newItems.addElement(item2);
                    }
                    buf.append(CMLib.lister().lister(mob,newItems,true,"CRItem","",false,CMath.bset(mob.getBitmap(),MOB.ATT_COMPRESS)));
                }
            }
            else
            if((contitem.hasALid())&&((contitem.capacity()>0)||(contitem.getContents().size()>0)))
                buf.append(item.name()+" is closed.");
        }
        if(!msg.source().isMonster())
            buf.append(CMProps.mxpImage(item," ALIGN=RIGHT H=70 W=70"));
        mob.tell(buf.toString());
    }
    
    protected void handleBeingItemSniffed(CMMsg msg)
    {
        String s=null;
        Item item=(Item)msg.target();
        if(CMLib.flags().canSmell(msg.source()))
            s=RawMaterial.RESOURCE_SMELLS[item.material()&RawMaterial.RESOURCE_MASK].toLowerCase();
        if((s!=null)&&(s.length()>0))
            msg.source().tell(msg.source(),item,null,"<T-NAME> has a "+s+" smell.");
    }
    
    public void handleIntroductions(MOB speaker, MOB me, String msg)
    {
        if((me.playerStats()!=null)
        &&(speaker!=me)
        &&(speaker.playerStats()!=null)
        &&(msg!=null)
        &&(!me.playerStats().isIntroducedTo(speaker.Name()))
        &&(CMLib.english().containsString(msg,speaker.Name())))
            me.playerStats().introduceTo(speaker.Name());
    }
    
    protected void handleBeingRoomSniffed(CMMsg msg)
    {
        Room room=(Room)msg.target();
        StringBuffer smell=new StringBuffer("");
        switch(room.domainType())
        {
        case Room.DOMAIN_INDOORS_UNDERWATER:
        case Room.DOMAIN_INDOORS_WATERSURFACE:
        case Room.DOMAIN_OUTDOORS_UNDERWATER:
        case Room.DOMAIN_OUTDOORS_WATERSURFACE:
            smell.append("It smells very WET here. ");
            break;
        case Room.DOMAIN_INDOORS_CAVE:
            smell.append("It smells very dank and mildewy here. ");
            break;
        case Room.DOMAIN_OUTDOORS_HILLS:
        case Room.DOMAIN_OUTDOORS_PLAINS:
            switch(room.getArea().getTimeObj().getSeasonCode())
            {
            case TimeClock.SEASON_FALL:
            case TimeClock.SEASON_WINTER:
                smell.append("There is a faint grassy smell here. ");
                break;
            case TimeClock.SEASON_SPRING:
            case TimeClock.SEASON_SUMMER:
                smell.append("There is a floral grassy smell here. ");
                break;
            }
            break;
        case Room.DOMAIN_OUTDOORS_WOODS:
            switch(room.getArea().getTimeObj().getSeasonCode())
            {
            case TimeClock.SEASON_FALL:
            case TimeClock.SEASON_WINTER:
                smell.append("There is a faint woodsy smell here. ");
                break;
            case TimeClock.SEASON_SPRING:
            case TimeClock.SEASON_SUMMER:
                smell.append("There is a rich woodsy smell here. ");
                break;
            }
            break;
        case Room.DOMAIN_OUTDOORS_JUNGLE:
            smell.append("There is a rich floral and plant aroma here. ");
            break;
        case Room.DOMAIN_OUTDOORS_MOUNTAINS:
        case Room.DOMAIN_OUTDOORS_ROCKS:
            switch(room.getArea().getTimeObj().getSeasonCode())
            {
            case TimeClock.SEASON_FALL:
            case TimeClock.SEASON_WINTER:
            case TimeClock.SEASON_SUMMER:
                smell.append("It smells musty and rocky here. ");
                break;
            case TimeClock.SEASON_SPRING:
                smell.append("It smells musty, rocky, and a bit grassy here. ");
                break;
            }
            break;
        case Room.DOMAIN_OUTDOORS_SWAMP:
            smell.append("It smells stinky and gassy here. ");
            break;
        }
        if(smell.length()>0)
            msg.source().tell(smell.toString());
    }
    
    protected void handleBeingRoomLookedAt(CMMsg msg)
    {
        
        MOB mob=msg.source();
        if(mob.session()==null) return; // no need for monsters to build all this data
        
        Room room=(Room)msg.target();
        int lookCode=LOOK_LONG;
        if(msg.targetMinor()!=CMMsg.TYP_EXAMINE)
            lookCode=(msg.sourceMessage()==null)?LOOK_BRIEFOK:LOOK_NORMAL;
        
        StringBuffer Say=new StringBuffer("");
        boolean compress=CMath.bset(mob.getBitmap(),MOB.ATT_COMPRESS);
        if(CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
        {
            if(!CMSecurity.isAllowed(mob,room,"SYSMSGS"))
                mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_SYSOPMSGS));
            else
            {
                if(room.getArea()!=null)
                    Say.append("^!Area  :^N("+room.getArea().Name()+")"+"\n\r");
                Say.append("^!Locale:^N("+room.ID()+")"+"\n\r");
                Say.append("^H("+CMLib.map().getExtendedRoomID(room)+")^N ");
            }
        }
        if(CMLib.flags().canBeSeenBy(room,mob))
        {
            Say.append("^O^<RName^>" + room.roomTitle()+"^</RName^>"+CMLib.flags().colorCodes(room,mob)+"^L\n\r");
            if((lookCode!=LOOK_BRIEFOK)||(!CMath.bset(mob.getBitmap(),MOB.ATT_BRIEF)))
            {
                if(lookCode==LOOK_LONG)
                {
                    String roomDesc=room.roomDescription();
                    Vector keyWords=null;
                    String word=null;
                    int x=0;
                    for(int c=0;c<room.numItems();c++)
                    {
                        Item item=room.fetchItem(c);
                        if(item==null) continue;
                        if((item.container()==null)
                        &&(item.displayText().length()==0)
                        &&(CMLib.flags().canBeSeenBy(item,mob)))
                        {
                            keyWords=CMParms.parse(item.name().toUpperCase());
                            for(int k=0;k<keyWords.size();k++)
                            {
                                word=(String)keyWords.elementAt(k);
                                x=roomDesc.toUpperCase().indexOf(word);
                                while(x>=0)
                                {
                                    if(((x<=0)||((!Character.isLetterOrDigit(roomDesc.charAt(x-1)))&&(roomDesc.charAt(x-1)!='>')))
                                    &&(((x+word.length())>=(roomDesc.length()-1))||((!Character.isLetterOrDigit(roomDesc.charAt((x+word.length()))))&&(roomDesc.charAt(x+word.length())!='^'))))
                                    {
                                        int brackCheck=roomDesc.substring(x).indexOf("^>");
                                        int brackCheck2=roomDesc.substring(x).indexOf("^<");
                                        if((brackCheck<0)||(brackCheck2<brackCheck))
                                        {
                                            int start=x;
                                            while((start>=0)&&(!Character.isWhitespace(roomDesc.charAt(start))))
                                                start--;
                                            start++;
                                            int end=(x+word.length());
                                            while((end<roomDesc.length())&&(!Character.isWhitespace(roomDesc.charAt(end))))
                                                end++;
                                            int l=roomDesc.length();
                                            roomDesc=roomDesc.substring(0,start)+"^H^<WItem \""+item.name()+"\"^>"+roomDesc.substring(start,end)+"^</WItem^>^?"+roomDesc.substring(end);
                                            x=x+(roomDesc.length()-l);
                                        }
                                    }
                                    x=roomDesc.toUpperCase().indexOf(word,x+1);
                                }
                            }
                        }
                    }
                    Say.append("^L^<RDesc^>"+roomDesc+"^</RDesc^>");
                }
                else
                    Say.append("^L^<RDesc^>" + room.roomDescription()+"^</RDesc^>");
                if((!mob.isMonster())&&(mob.session().clientTelnetMode(Session.TELNET_MXP)))
                    Say.append(CMProps.mxpImage(room," ALIGN=RIGHT H=70 W=70"));
                if(compress)
                    Say.append("^N  ");
                else
                    Say.append("^N\n\r\n\r");
            }
        }

        Vector viewItems=new Vector();
        int itemsInTheDarkness=0;
        for(int c=0;c<room.numItems();c++)
        {
            Item item=room.fetchItem(c);
            if(item==null) continue;
            
            if(item.container()==null)
            {
                if(CMLib.flags().canBarelyBeSeenBy(item,mob))
                    itemsInTheDarkness++;
                viewItems.addElement(item);
            }
        }
        
        StringBuffer itemStr=CMLib.lister().lister(mob,viewItems,false,"RItem"," \"*\"",lookCode==LOOK_LONG,compress);
        if(itemStr.length()>0)
            Say.append(itemStr);

        int mobsInTheDarkness=0;
        for(int i=0;i<room.numInhabitants();i++)
        {
            MOB mob2=room.fetchInhabitant(i);
            if((mob2!=null)&&(mob2!=mob))
            {
               if((mob2.displayText(mob).length()>0)
               ||(CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))
               {
                   if(CMLib.flags().canBeSeenBy(mob2,mob))
                   {
                        if(CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
                            Say.append("^H("+CMClass.className(mob2)+")^N ");
    
                        if((!compress)&&(!mob.isMonster())&&(mob.session().clientTelnetMode(Session.TELNET_MXP)))
                            Say.append(CMProps.mxpImage(mob2," H=10 W=10",""," "));
                        Say.append("^M^<RMob \""+mob2.name()+"\"^>");
                        if(compress) Say.append(CMLib.flags().colorCodes(mob2,mob)+"^M ");
                        if(mob2.displayText(mob).length()>0)
                            Say.append(CMStrings.endWithAPeriod(CMStrings.capitalizeFirstLetter(mob2.displayText(mob))));
                        else
                            Say.append(CMStrings.endWithAPeriod(CMStrings.capitalizeFirstLetter(mob2.name())));
                        Say.append("^</RMob^>");
                        if(!compress)
                            Say.append(CMLib.flags().colorCodes(mob2,mob)+"^N\n\r");
                        else
                            Say.append("^N");
                   }
                   else
                   if(CMLib.flags().canBarelyBeSeenBy(mob2,mob))
                       mobsInTheDarkness++;
               }
            }
        }

        if(Say.length()==0)
            mob.tell("You can't see anything!");
        else
        {
            if(compress) Say.append("\n\r");
            mob.tell(Say.toString());
            if(itemsInTheDarkness>0)
                mob.tell("      ^IThere is something here, but it's too dark to make out.^?\n\r");
            if(mobsInTheDarkness>1)
                mob.tell("^MThe darkness conceals several others.^?\n\r");
            else
            if(mobsInTheDarkness>0)
                mob.tell("^MYou are not alone, but it's too dark to tell.^?\n\r");
        }
    }

    
    private static boolean isAClearExitView(MOB mob, Room room, Exit exit)
    {
        if((room!=null)
        &&(exit!=null)
        &&(exit.isOpen())
        &&(CMLib.flags().canBeSeenBy(room,mob))
        &&(CMLib.flags().canBeSeenBy(exit,mob)))
        {
            int domain=room.domainType();
            switch(domain)
            {
            case Room.DOMAIN_INDOORS_AIR:
            case Room.DOMAIN_INDOORS_UNDERWATER:
            case Room.DOMAIN_OUTDOORS_AIR:
            case Room.DOMAIN_OUTDOORS_UNDERWATER:
            {
                int weather=room.getArea().getClimateObj().weatherType(room);
                if((weather!=Climate.WEATHER_BLIZZARD)&&(weather!=Climate.WEATHER_DUSTSTORM))
                    return true;
                break;
            }
            }
        }
        return false;
    }
    
    protected void handleBeingExitLookedAt(CMMsg msg)
    {
        Exit exit=(Exit)msg.target();
        MOB mob=msg.source();
        if(CMLib.flags().canBeSeenBy(exit,mob))
        {
            if(exit.description().trim().length()>0)
                mob.tell(exit.description());
            else
            if(mob.location()!=null)
            {
                Room room=null;
                int direction=-1;
                if(msg.tool() instanceof Room)
                    room=(Room)msg.tool();
                else
                for(int r=0;r<Directions.NUM_DIRECTIONS;r++)
                    if(mob.location().getExitInDir(r)==exit)
                    {
                        room=mob.location().getRoomInDir(r);
                        break;
                    }
                if(room!=null)
                for(int r=0;r<Directions.NUM_DIRECTIONS;r++)
                    if((mob.location().getRoomInDir(r)==room)
                    &&((mob.location().getExitInDir(r)==exit)))
                        direction=r;
                mob.tell(exit.viewableText(mob,room).toString());
                if(isAClearExitView(mob,room,exit)&&(direction>=0))
                {
                    Vector view=null;
                    Vector items=new Vector();
                    if(room.getGridParent()!=null)
                        view=room.getGridParent().getAllRooms();
                    else
                    {
                        view=new Vector();
                        view.addElement(room);
                        for(int i=0;i<5;i++)
                        {
                            room=room.getRoomInDir(direction);
                            if(room==null) break;
                            Exit E=room.getExitInDir(direction);
                            if((isAClearExitView(mob,room,E)))
                                view.addElement(room);
                        }
                    }
                    Environmental E=null;
                    for(int r=0;r<view.size();r++)
                    {
                        room=(Room)view.elementAt(r);
                        for(int i=0;i<room.numItems();i++)
                        {
                            E=room.fetchItem(i);
                            if(E!=null) items.addElement(E);
                        }
                        for(int i=0;i<room.numInhabitants();i++)
                        {
                            E=room.fetchInhabitant(i);
                            if(E!=null) items.addElement(E);
                        }
                    }
                    StringBuffer seenThatWay=CMLib.lister().lister(msg.source(),items,true,"","",false,true);
                    if(seenThatWay.length()>0)
                        mob.tell("Yonder, you can also see: "+seenThatWay.toString());
                }
            }
            else
                mob.tell("You don't see anything special.");
            if(CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
            {
                mob.tell("Type  : "+exit.ID());
                mob.tell("Misc   : "+exit.text());
            }
            String image=CMProps.mxpImage(exit," ALIGN=RIGHT H=70 W=70");
            if((image!=null)&&(image.length()>0)) mob.tell(image);
        }
        else
            mob.tell("You can't see that way!");
    }
    
    protected void handleBeingMobLookedAt(CMMsg msg)
    {
        MOB viewermob=msg.source();
        MOB viewedmob=(MOB)msg.target();
        boolean longlook=msg.targetMinor()==CMMsg.TYP_EXAMINE;
        StringBuffer myDescription=new StringBuffer("");
        if(CMLib.flags().canBeSeenBy(viewedmob,viewermob))
        {
            if(CMath.bset(viewermob.getBitmap(),MOB.ATT_SYSOPMSGS))
                myDescription.append("\n\rType :"+viewedmob.ID()
                                    +"\n\rRejuv:"+viewedmob.baseEnvStats().rejuv()
                                    +"\n\rAbile:"+viewedmob.baseEnvStats().ability()
                                    +"\n\rLevel:"+viewedmob.baseEnvStats().level()
                                    +"\n\rDesc : "+viewedmob.description()
                                    +"\n\rRoom :'"+((viewedmob.getStartRoom()==null)?"null":viewedmob.getStartRoom().roomID())
                                    +"\n\rMisc : "+viewedmob.text()
                                    +"\n\r");
            if(!viewedmob.isMonster())
            {
                String levelStr=null;
                if((!CMSecurity.isDisabled("CLASSES"))
                &&(!viewedmob.charStats().getMyRace().classless())
                &&(!viewedmob.charStats().getCurrentClass().leveless())
                &&(!viewedmob.charStats().getMyRace().leveless())
                &&(!CMSecurity.isDisabled("LEVELS")))
                    levelStr=CMStrings.startWithAorAn(viewedmob.charStats().displayClassLevel(viewedmob,false));
                else
                if((!CMSecurity.isDisabled("LEVELS"))
                &&(!viewedmob.charStats().getCurrentClass().leveless())
                &&(!viewedmob.charStats().getMyRace().leveless()))
                    levelStr="level "+viewedmob.charStats().displayClassLevelOnly(viewedmob);
                else
                if((!CMSecurity.isDisabled("CLASSES"))
                &&(!viewedmob.charStats().getMyRace().classless()))
                    levelStr=CMStrings.startWithAorAn(viewedmob.charStats().displayClassName());
                if((!CMSecurity.isDisabled("RACES"))
                &&(!viewedmob.charStats().getCurrentClass().raceless()))
                {
                    myDescription.append(viewedmob.displayName(viewermob)+" the ");
                    if(viewedmob.charStats().getStat(CharStats.STAT_AGE)>0)
                        myDescription.append(viewedmob.charStats().ageName().toLowerCase()+" ");
                    myDescription.append(viewedmob.charStats().raceName());
                }
                else
                    myDescription.append(viewedmob.displayName(viewermob)+" ");
                if(levelStr!=null)
                    myDescription.append(" is "+levelStr+".\n\r");
                else
                    myDescription.append("is here.\n\r");
            }
            if(viewedmob.envStats().height()>0)
                myDescription.append(viewedmob.charStats().HeShe()+" is "+viewedmob.envStats().height()+" inches tall and weighs "+viewedmob.baseEnvStats().weight()+" pounds.\n\r");
            if((longlook)&&(viewermob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>12))
            {
                CharStats C=(CharStats)CMClass.getCommon("DefaultCharStats");
                MOB testMOB=CMClass.getMOB("StdMOB");
                viewedmob.charStats().getMyRace().affectCharStats(testMOB,C);
                myDescription.append(relativeCharStatTest(C,viewedmob,"weaker","stronger",CharStats.STAT_STRENGTH));
                myDescription.append(relativeCharStatTest(C,viewedmob,"clumsier","more nimble",CharStats.STAT_DEXTERITY));
                myDescription.append(relativeCharStatTest(C,viewedmob,"more sickly","healthier",CharStats.STAT_CONSTITUTION));
                myDescription.append(relativeCharStatTest(C,viewedmob,"more repulsive","more attractive",CharStats.STAT_CHARISMA));
                myDescription.append(relativeCharStatTest(C,viewedmob,"more naive","wiser",CharStats.STAT_WISDOM));
                myDescription.append(relativeCharStatTest(C,viewedmob,"dumber","smarter",CharStats.STAT_INTELLIGENCE));
                testMOB.destroy();
            }
            if(!viewermob.isMonster())
                myDescription.append(CMProps.mxpImage(viewedmob," ALIGN=RIGHT H=70 W=70"));
            myDescription.append(viewedmob.healthText(viewermob)+"\n\r\n\r");
            myDescription.append(viewedmob.description()+"\n\r\n\r");
            
            StringBuffer eq=CMLib.commands().getEquipment(viewermob,viewedmob);
            if(eq.length() > 0)
            {
                if((CMProps.getIntVar(CMProps.SYSTEMI_EQVIEW)>1)
                ||((viewermob!=viewedmob)&&(CMProps.getIntVar(CMProps.SYSTEMI_EQVIEW)>0)))
                    myDescription.append(viewedmob.charStats().HeShe()+" is wearing "+eq.toString());
                else
                    myDescription.append(viewedmob.charStats().HeShe()+" is wearing:\n\r"+eq.toString());
            }
            viewermob.tell(myDescription.toString());
            if(longlook)
            {
                Command C=CMClass.getCommand("Consider");
                try{if(C!=null)C.execute(viewermob,CMParms.makeVector(viewedmob));}catch(java.io.IOException e){}
            }
        }
    }
    
    public void handleBeingGivenTo(CMMsg msg)
    {
        if(!(msg.target() instanceof MOB)) return;
        MOB givermob=msg.source();
        MOB giveemob=(MOB)msg.target();
        if(giveemob.location()!=null)
        {
            CMMsg msg2=CMClass.getMsg(givermob,msg.tool(),null,CMMsg.MSG_DROP,null,CMMsg.MSG_DROP,"GIVE",CMMsg.MSG_DROP,null);
            giveemob.location().send(givermob,msg2);
            msg2=CMClass.getMsg((MOB)msg.target(),msg.tool(),null,CMMsg.MSG_GET,null,CMMsg.MSG_GET,"GIVE",CMMsg.MSG_GET,null);
            giveemob.location().send(giveemob,msg2);
        }
    }

    public void handleBeingRead(CMMsg msg)
    {
        if((msg.targetMessage()==null)||(!msg.targetMessage().equals("CANCEL")))
        {
            MOB mob=msg.source();
            if(CMLib.flags().canBeSeenBy(msg.target(),mob))
            {
            	String text=null;
            	if((msg.target() instanceof Exit)&&(((Exit)msg.target()).isReadable()))
            		text=((Exit)msg.target()).readableText();
            	else
            	if((msg.target() instanceof Item)&&(CMLib.flags().isReadable((Item)msg.target())))
            		text=((Item)msg.target()).readableText();
            	if((text!=null)
                &&(text.length()>0))
                {
                    if(text.toUpperCase().startsWith("FILE="))
                    {
                        StringBuffer buf=Resources.getFileResource(text.substring(5),true);
                        if((buf!=null)&&(buf.length()>0))
                            mob.tell("It says '"+buf.toString()+"'.");
                        else
                            mob.tell("There is nothing written on "+msg.target().name()+".");
                    }
                    else
                        mob.tell("It says '"+text+"'.");
                }
                else
                    mob.tell("There is nothing written on "+msg.target().name()+".");
            }
            else
                mob.tell("You can't see that!");
        }
    }
    
    public void handleBeingGetted(CMMsg msg)
    {
        if(!(msg.target() instanceof Item)) return;
        Item item=(Item)msg.target();
        MOB mob=msg.source();
        if(item instanceof Container)
        {
            if((msg.tool()!=null)
            &&(msg.tool() instanceof Item))
            {
                Item newitem=(Item)msg.tool();
                if(newitem.container()==item)
                {
                    newitem.setContainer(null);
                    newitem.unWear();
                }
            }
            else
            if(!mob.isMine(item))
            {
                item.setContainer(null);
                mob.giveItem(item);
                if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
                    mob.location().recoverRoomStats();
                else
                    mob.envStats().setWeight(mob.envStats().weight()+item.recursiveWeight());
            }
            else
            {
                item.setContainer(null);
                item.unWear();
                if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
                    mob.location().recoverRoomStats();
            }
        }
        else
        {
            item.setContainer(null);
            if(CMLib.flags().isHidden(item))
                item.baseEnvStats().setDisposition(item.baseEnvStats().disposition()&((int)EnvStats.ALLMASK-EnvStats.IS_HIDDEN));
            if(mob.location().isContent(item))
                mob.location().delItem(item);
            if(!mob.isMine(item))
            {
                mob.addInventory(item);
                if(CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
                    mob.envStats().setWeight(mob.envStats().weight()+item.envStats().weight());
            }
            item.unWear();
            if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
                mob.location().recoverRoomStats();
            if(item instanceof Coins)
                ((Coins)item).putCoinsBack();
    		if(item instanceof RawMaterial)
    			((RawMaterial)item).rebundle();
        }
    }
    public void handleBeingDropped(CMMsg msg)
    {
        if(!(msg.target() instanceof Item)) return;
        Item item=(Item)msg.target();
        MOB mob=msg.source();
        if(mob.isMine(item)&&(item instanceof Container))
        {
            item.setContainer(null);
            CMLib.utensils().recursiveDropMOB(mob,mob.location(),item,item instanceof DeadBody);
            if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
                mob.location().recoverRoomStats();
        }
        if(mob.isMine(item))
        {
            mob.delInventory(item);
            if(!mob.location().isContent(item))
                mob.location().addItemRefuse(item,Item.REFUSE_PLAYER_DROP);
            if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
                mob.location().recoverRoomStats();
        }
        item.unWear();
        item.setContainer(null);
        if((msg.targetMessage()==null)||(!msg.targetMessage().equals("GIVE")))
        {
            if(item instanceof Coins)
            	((Coins)item).putCoinsBack();
            if(item instanceof RawMaterial)
            	((RawMaterial)item).rebundle();
        }
    }
    public void handleBeingRemoved(CMMsg msg)
    {
        if(!(msg.target() instanceof Item)) return;
        Item item=(Item)msg.target();
        MOB mob=msg.source();
        if(item instanceof Container)
        {
            handleBeingGetted(msg);
            return;
        }
        item.unWear();
        if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
            mob.location().recoverRoomStats();
    }
    public void handleBeingWorn(CMMsg msg)
    {
        if(!(msg.target() instanceof Item)) return;
        Item item=(Item)msg.target();
        MOB mob=msg.source();
        if(item.canWear(mob,0))
        {
            item.wearIfPossible(mob);
            mob.recoverCharStats();
            mob.recoverEnvStats();
            mob.recoverMaxState();
        }
    }
    public void handleBeingWielded(CMMsg msg)
    {
        if(!(msg.target() instanceof Item)) return;
        Item item=(Item)msg.target();
        MOB mob=msg.source();
        if((item.canWear(mob,Item.WORN_WIELD))&&(item.fitsOn(Item.WORN_WIELD)))
        {
            item.wearAt(Item.WORN_WIELD);
            mob.recoverCharStats();
            mob.recoverEnvStats();
            mob.recoverMaxState();
        }
    }
    public void handleBeingHeld(CMMsg msg)
    {
        if(!(msg.target() instanceof Item)) return;
        Item item=(Item)msg.target();
        MOB mob=msg.source();
        if((item.canWear(mob,Item.WORN_HELD))&&(item.fitsOn(Item.WORN_HELD)))
        {
            item.wearAt(Item.WORN_HELD);
            mob.recoverCharStats();
            mob.recoverEnvStats();
            mob.recoverMaxState();
        }
    }
    
    public void lookAtExits(Room room, MOB mob)
    {
        if((mob==null)||(room==null)||(mob.isMonster())) return;
        if(!CMLib.flags().canSee(mob))
        {
            mob.tell("You can't see anything!");
            return;
        }

        StringBuffer buf=new StringBuffer("^DObvious exits:^.^N\n\r");
        String Dir=null;
        for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
        {
            Exit exit=room.getExitInDir(i);
            Room room2=room.getRoomInDir(i);
            StringBuffer Say=new StringBuffer("");
            if(exit!=null)
                Say=exit.viewableText(mob, room2);
            else
            if((room2!=null)&&(CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))
                Say.append(room2.roomID()+" via NULL");
            if(Say.length()>0)
            {
                Dir=CMStrings.padRightPreserve(Directions.getDirectionName(i),5);
                if((mob!=null)
                &&(mob.playerStats()!=null)
                &&(room2!=null)
                &&(!mob.playerStats().hasVisited(room2)))
                    buf.append("^U^<EX^>" + Dir+"^</EX^>:^.^N ^u"+Say+"^.^N\n\r");
                else
                    buf.append("^D^<EX^>" + Dir+"^</EX^>:^.^N ^d"+Say+"^.^N\n\r");
            }
        }
        Item I=null;
        for(int i=0;i<room.numItems();i++)
        {
            I=room.fetchItem(i);
            if((I instanceof Exit)&&(((Exit)I).doorName().length()>0))
            {
                StringBuffer Say=((Exit)I).viewableText(mob, room);
                if(Say.length()>5)
                    buf.append("^D^<MEX^>" + ((Exit)I).doorName()+"^</MEX^>:^.^N ^d"+Say+"^.^N\n\r");
                else
                if(Say.length()>0)
                    buf.append("^D^<MEX^>" + CMStrings.padRight(((Exit)I).doorName(),5)+"^</MEX^>:^.^N ^d"+Say+"^.^N\n\r");
            }
        }
        mob.tell(buf.toString());
    }
    
    public void lookAtExitsShort(Room room, MOB mob)
    {
        if((mob==null)||(room==null)||(mob.isMonster())) return;
        if(!CMLib.flags().canSee(mob)) return;
        
        StringBuffer buf=new StringBuffer("^D[Exits: ");
        for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
        {
            Exit exit=room.getExitInDir(i);
            if((exit!=null)&&(exit.viewableText(mob, room.getRoomInDir(i)).length()>0))
                buf.append("^<EX^>"+Directions.getDirectionName(i)+"^</EX^> ");
        }
        Item I=null;
        for(int i=0;i<room.numItems();i++)
        {
            I=room.fetchItem(i);
            if((I instanceof Exit)&&(((Exit)I).viewableText(mob, room).length()>0))
                buf.append("^<MEX^>"+((Exit)I).doorName()+"^</MEX^> ");
        }
        mob.tell(buf.toString().trim()+"]^.^N");
    }

}
