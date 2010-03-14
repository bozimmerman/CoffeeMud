package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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
public class Conquerable extends Arrest
{
	public String ID(){return "Conquerable";}
	protected boolean defaultModifiableNames(){return false;}
	protected String getLawParms(){ return "custom";}

    protected String savedHoldingClan="";
    protected String prevHoldingClan="";
    protected String holdingClan="";
    protected Vector clanItems=new Vector();
    protected DVector clanControlPoints=new DVector(2);
    protected DVector assaults=new DVector(2);
    protected Vector noMultiFollows=new Vector();
    protected int totalControlPoints=-1;
    protected Area myArea=null;
    protected String journalName="";
    protected boolean allowLaw=false;
    protected boolean REVOLTNOW=false;
    protected long waitToReload=0;
    protected long conquestDate=0;
    public boolean isFullyControlled(){
        return ((holdingClan.length()>0)&&((System.currentTimeMillis()-conquestDate)>CONTROLTIME));
    }

    protected int revoltDown=REVOLTFREQ;
    protected static final int REVOLTFREQ=(int)((TimeManager.MILI_DAY*3)/Tickable.TIME_TICK);
    protected int checkDown=0;
    protected static final int CHECKFREQ=10;
    protected int pointDown=0;
    protected static final int POINTFREQ=(int)((10*60000)/Tickable.TIME_TICK);
    protected int fightDown=0;
    protected static final int FIGHTFREQ=2;

    public String rulingOrganization()
    {
        return holdingClan;
    }
    
    public String conquestInfo(Area myArea)
    {
        StringBuffer str=new StringBuffer("");
        if((totalControlPoints<0)&&(myArea!=null))
			recalculateControlPoints(myArea);
        if((holdingClan.length()==0)||(totalControlPoints<0))
            str.append("Area '"+myArea.name()+"' is not currently controlled by any clan.\n\r");
        else
        {
            Clan C=CMLib.clans().getClan(holdingClan);
            if(C!=null)
            {
                if(isFullyControlled())
                    str.append("Area '"+myArea.name()+"' is controlled by "+C.typeName()+" "+C.name()+".\n\r");
                else
                {
                    str.append("Area '"+myArea.name()+"' is occupied by "+C.typeName()+" "+C.name()+".\n\r");
                    long remain=CONTROLTIME-(System.currentTimeMillis()-conquestDate);
                    String remainStr=myArea.getTimeObj().deriveEllapsedTimeString(remain);
                    str.append("Full control will automatically be achieved in "+remainStr+".\n\r");
                }
                
                if(C.getGovernment()!=Clan.GVT_THEOCRACY)
                {
                    int pts=calcItemControlPoints(myArea);
                    int chance=calcRevoltChance(myArea);
                    str.append(C.name()+" has handed out clan items here for "+pts+" loyalty points.\n\r");
                    str.append("There is currently a "+chance+"% chance of revolt here.\n\r");
                }
            }
            else
            {
                if(CMSecurity.isDebugging("CONQUEST")) Log.debugOut("Conquest",holdingClan+" has laid waste to "+myArea.name()+".");
                endClanRule();
                str.append("This area is laid waste by "+holdingClan+".\n\r");
            }
        }
        if((totalControlPoints<0)&&(myArea!=null))
			recalculateControlPoints(myArea);
        if(totalControlPoints<0)
            str.append("This area has not yet calculated its required control points.\n\r");
        else
	        str.append("This area requires "+totalControlPoints+" points to control.\n\r");
        if(clanControlPoints.size()==0)
            str.append("There are no control points won at present by any clan.\n\r");
        synchronized(clanControlPoints)
        {
            for(int i=0;i<clanControlPoints.size();i++)
            {
                String clanID=(String)clanControlPoints.elementAt(i,1);
                int[] ic=(int[])clanControlPoints.elementAt(i,2);
                Clan C=CMLib.clans().getClan(clanID);
                if(C!=null)
                    str.append(C.typeName()+" "+C.name()+" has "+ic[0]+" control points.\n\r");
            }
        }
        return str.toString();
    }
    public int controlPoints()
    {
        if(totalControlPoints>=0) return totalControlPoints;
        return 0;
    }
    
    public int getControlPoints(String clanID){
        if((clanID==null)||(clanID.length()==0)) return 0;
        synchronized(clanControlPoints)
        {
            for(int i=0;i<clanControlPoints.size();i++)
            {
                String clanID2=(String)clanControlPoints.elementAt(i,1);
                int[] ic=(int[])clanControlPoints.elementAt(i,2);
                if(clanID2.equalsIgnoreCase(clanID))
                {
                    Clan C=CMLib.clans().getClan(clanID);
                    if(C!=null) return ic[0];
                }
            }
        }
        return 0;
    }

    public int revoltChance()
    {
    	if(myArea==null) return 100;
        Clan C=CMLib.clans().getClan(holdingClan);
        if((C==null)||(C.getGovernment()!=Clan.GVT_THEOCRACY))
	    	return calcRevoltChance(myArea);
        return 0;
    }

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		journalName=CMParms.getParmStr(newParms,"JOURNAL","");
		allowLaw=CMParms.getParmStr(newParms,"LAW","FALSE").toUpperCase().startsWith("T");
		loadAttempt=false;
        clanItems=new Vector();
        clanControlPoints=new DVector(2);
        assaults=new DVector(2);
        noMultiFollows=new Vector();
	}

	public void startBehavior(Environmental E)
	{
		super.startBehavior(E);
		CMLib.map().addGlobalHandler(this, CMMsg.TYP_CLANEVENT);
	}
	
	public boolean isAnyKindOfOfficer(Law laws, MOB M)
	{
		if((M!=null)
		&&(allowLaw)
		&&(!CMLib.flags().isAnimalIntelligence(M))
		&&(M.location()!=null)
		&&((!M.isMonster())||CMLib.flags().isMobile(M))
		&&(holdingClan.length()>0)
		&&(M.getClanID().equals(holdingClan)))
			for(int i=0;i<M.inventorySize();i++)
			{
				Item I=M.fetchInventory(i);
				if((I!=null)
				&&(I instanceof ClanItem)
				&&(!I.amWearingAt(Wearable.IN_INVENTORY))
				&&(((ClanItem)I).ciType()==ClanItem.CI_BANNER))
					return true;
			}
		return false;
	}

	public boolean isTheJudge(Law laws, MOB M)
	{
		if((M!=null)
		&&(allowLaw)
		&&(!CMLib.flags().isAnimalIntelligence(M))
		&&(M.location()!=null)
		&&(holdingClan.length()>0)
		&&(M.getClanID().equals(holdingClan)))
			for(int i=0;i<M.inventorySize();i++)
			{
				Item I=M.fetchInventory(i);
				if((I!=null)
				&&(I instanceof ClanItem)
				&&(!I.amWearingAt(Wearable.IN_INVENTORY))
				&&(((ClanItem)I).ciType()==ClanItem.CI_GAVEL))
					return true;
			}
		return false;
	}

    protected synchronized void endClanRule()
	{
		if(holdingClan.length()==0)
			return;
		if((!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
		||(CMSecurity.isDisabled("CONQUEST")))
			return;
		Clan C=CMLib.clans().getClan(holdingClan);
        String worship=getManadatoryWorshipID();
        prevHoldingClan=holdingClan;
		for(int v=0;v<clanItems.size();v++)
		{
			Item I=(Item)clanItems.elementAt(v);
			if((I.owner() instanceof MOB)
			&&(I instanceof ClanItem)
			&&(((ClanItem)I).clanID().equals(holdingClan)))
			{
				MOB M=(MOB)I.owner();
				if((M.location()!=null)&&(!M.amDead())&&(M.isMonster()))
				{
					M.delInventory(I);
					if(M.getClanID().equals(holdingClan))
                    {
						M.setClanID("");
                        if((worship!=null)&&(M.getWorshipCharID().equals(worship))) 
                            M.setWorshipCharID("");
                    }
					I.setRawWornCode(0);
					I.setContainer(null);
					M.location().addItemRefuse(I,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
				}
			}
		}

		if(myArea!=null)
		{
			for(Enumeration e=myArea.getMetroMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if((M!=null)
					&&(M.isMonster())
					&&(M.getStartRoom()!=null)
					&&(myArea.inMyMetroArea(M.getStartRoom().getArea()))
					&&(M.getClanID().equals(holdingClan)))
                    {
						M.setClanID("");
                        if((worship!=null)&&(M.getWorshipCharID().equals(worship))) 
                            M.setWorshipCharID("");
                    }
				}
			}
			if(holdingClan.length()>0)
			{
	            if(CMSecurity.isDebugging("CONQUEST")) Log.debugOut("Conquest",holdingClan+" has lost control of "+myArea.name()+".");
	            Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS);
	            for(int i=0;i<channels.size();i++)
	                CMLib.commands().postChannel((String)channels.elementAt(i),"ALL",holdingClan+" has lost control of "+myArea.name()+".",false);
				if(journalName.length()>0)
					CMLib.database().DBWriteJournal(journalName,"Conquest","ALL",holdingClan+" loses control of "+myArea.name()+".","See the subject line.");
			}
			Law laws=getLaws(myArea,false);
			if(laws.lawIsActivated())
			{
				laws.setInternalStr("ACTIVATED","FALSE");
				laws.resetLaw();
				CMLib.database().DBReCreateData(myArea.Name(),"ARREST",myArea.Name()+"/ARREST",laws.rawLawString());
			}
		}
        synchronized(clanItems)
        {
            try{
	            for(int c=clanItems.size()-1;c>=0;c--)
	            {
	            	if((C==null)&&(((ClanItem)clanItems.elementAt(c)).clanID().equalsIgnoreCase(holdingClan)))
	            	{
	            		((ClanItem)clanItems.elementAt(c)).destroy();
	            		clanItems.removeElementAt(c);
	            	}
	            	else
	                if(((ClanItem)clanItems.elementAt(c)).ciType()!=ClanItem.CI_FLAG)
	                    deRegisterClanItem((ClanItem)clanItems.elementAt(c));
	            }
            }catch(ArrayIndexOutOfBoundsException x){}
            if((C==null)&&(clanItems.size()==0)&&(myArea!=null))
				CMLib.database().DBDeleteData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name());
        }
		holdingClan="";
        conquestDate=0;
	}

    public int calcItemControlPoints(Area A)
    {
        int itemControlPoints=0;
        synchronized(clanItems)
        {
            for(int i=clanItems.size()-1;i>=0;i--)
            {
                ClanItem I=(ClanItem)clanItems.elementAt(i);
                if((!I.amDestroyed())
                &&(I.owner() instanceof MOB)
                &&(((MOB)I.owner()).isMonster())
                &&(CMLib.flags().isInTheGame((MOB)I.owner(),true))
                &&(A.inMyMetroArea(((MOB)I.owner()).getStartRoom().getArea()))
                &&((holdingClan.length()==0)||(I.clanID().equals(holdingClan)))
                &&(I.ciType()!=ClanItem.CI_PROPAGANDA))
                    itemControlPoints+=((MOB)((Item)I).owner()).envStats().level();
            }
        }
        return itemControlPoints;
    }
    
    public int calcRevoltChance(Area A)
    {
        if(totalControlPoints<=0) return 0;
        int itemControlPoints=calcItemControlPoints(A);
        int totalNeeded=(int)Math.round(CMath.mul(0.05,totalControlPoints));
        if(totalNeeded<=0) totalNeeded=1;
        int chance=(int)Math.round(10.0-(CMath.mul(10.0,CMath.div(itemControlPoints,totalNeeded))));
        if(chance<=0) return 0;
        return chance;
        
    }
    
    protected void announceToArea(Area area, String clanID, int amount)
    {
        Session S=null;
        for(int s=CMLib.sessions().size()-1;s>=0;s--)
        {
            S=CMLib.sessions().elementAt(s);
            if(S==null) continue;
            if((S.mob()!=null)
            &&(S.mob().location()!=null)
            &&(area.inMyMetroArea(S.mob().location().getArea())))
                S.println(clanID+" "+(amount<0?"loses "+(-amount):"gains "+amount)+" control points.");
        }
    }
    
	public boolean tick(Tickable ticking, int tickID)
	{
		if((!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
		||(CMSecurity.isDisabled("CONQUEST")))
			return true;

		if(!super.tick(ticking,tickID))
			return false;
		if(tickID!=Tickable.TICKID_AREA) return true;
		if(!(ticking instanceof Area)) return true;
		Area A=(Area)ticking;

		if(A!=myArea) myArea=A;

		for(int i=clanItems.size()-1;i>=0;i--)
		{
			Item I=(Item)clanItems.elementAt(i);
			if(!I.tick(this,Tickable.TICKID_CLANITEM))
                deRegisterClanItem(I);
			else
            {
				I.setExpirationDate(0);
                if((I.owner() instanceof Room)&&(I.container()!=null))
                    I.setContainer(null);
            }
		}

		// calculate total control points
		// make sure all intelligent mobs belong to the clan
		if((totalControlPoints<0)
		&&((waitToReload<=0)||(System.currentTimeMillis()>waitToReload))
		&&(myArea!=null))
		{
			HashSet doneMOBs=new HashSet();
            HashSet doneRooms=new HashSet();
            clanItems.clear();
			Vector itemSet=CMLib.database().DBReadData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name());
			if((itemSet!=null)&&(itemSet.size()>0))
			{
				String data=((DatabaseEngine.PlayerData)itemSet.firstElement()).xml;
				Vector xml=CMLib.xml().parseAllXML(data);
				if(xml!=null)
				{
					savedHoldingClan=CMLib.xml().getValFromPieces(xml,"CLANID");
                    prevHoldingClan=CMLib.xml().getValFromPieces(xml,"OLDCLANID");
                    conquestDate=CMLib.xml().getLongFromPieces(xml,"CLANDATE");
					holdingClan=savedHoldingClan;
					Vector allData=CMLib.xml().getRealContentsFromPieces(xml,"ACITEMS");
					if(allData!=null)
					for(int c=0;c<allData.size();c++)
					{
						XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)allData.elementAt(c);
						if((iblk.tag.equalsIgnoreCase("ACITEM"))&&(iblk.contents!=null))
						{
							Vector roomData=iblk.contents;
							String roomID=CMLib.xml().getValFromPieces(roomData,"ROOMID");
							String MOBname=CMLib.xml().getValFromPieces(roomData,"MOB");
							Room R=CMLib.map().getRoom(roomID);
							if((R!=null)&&(A.inMyMetroArea(R.getArea())))
							{
								String iClass=CMLib.xml().getValFromPieces(roomData,"ICLAS");
								Item newItem=CMClass.getItem(iClass);
								if(newItem!=null)
								{
									newItem.baseEnvStats().setLevel(CMLib.xml().getIntFromPieces(roomData,"ILEVL"));
									newItem.baseEnvStats().setAbility(CMLib.xml().getIntFromPieces(roomData,"IABLE"));
									newItem.baseEnvStats().setRejuv(CMLib.xml().getIntFromPieces(roomData,"IREJV"));
									newItem.setUsesRemaining(CMLib.xml().getIntFromPieces(roomData,"IUSES"));
									newItem.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(roomData,"ITEXT")));
									newItem.recoverEnvStats();
									MOB foundMOB=null;
									if(MOBname.length()>0)
										for(int i=0;i<R.numInhabitants();i++)
										{
											MOB M=R.fetchInhabitant(i);
											if((M!=null)
											&&(M.isMonster())
											&&(M.name().equals(MOBname))
											&&(M.getStartRoom()==R)
											&&(!doneMOBs.contains(M)))
											{ foundMOB=M; break;}
										}
									if((foundMOB==null)&&(MOBname.length()>0))
										for(Enumeration e=A.getMetroMap();e.hasMoreElements();)
										{
											Room R2=(Room)e.nextElement();
											for(int i=0;i<R2.numInhabitants();i++)
											{
												MOB M=R2.fetchInhabitant(i);
												if((M!=null)
												&&(M.isMonster())
												&&(M.name().equals(MOBname))
												&&(M.getStartRoom()==R)
												&&(!doneMOBs.contains(M)))
												{ foundMOB=M; break;}
											}
										}
									if(foundMOB!=null)
									{
                                        boolean found=false;
                                        for(int i=0;i<foundMOB.inventorySize();i++)
                                            if(newItem.sameAs(foundMOB.fetchInventory(i)))
                                                found=true;
                                        if(!found)
                                        {
    										foundMOB.addInventory(newItem);
    										newItem.wearAt(newItem.rawProperLocationBitmap());
                                        }
									}
									else
                                    {
                                        if(!doneRooms.contains(R))
                                        {
                                            doneRooms.add(R);
                                            for(int i=R.numItems()-1;i>=0;i--)
                                            {
                                                Item I=R.fetchItem(i);
                                                if(I instanceof ClanItem)
                                                    I.destroy();
                                            }
                                        }
										R.addItem(newItem);
                                    }
                                    registerClanItem(newItem);
								}
							}
						}
					}
				}
			}
			recalculateControlPoints(A);
		}
		else
		{
			if((--checkDown)<=0)
			{
				checkDown=CHECKFREQ;
				// make sure clanitems are truly in the area
				synchronized(clanItems)
				{
					for(int i=clanItems.size()-1;i>=0;i--)
					{
						ClanItem I=(ClanItem)clanItems.elementAt(i);
						Room R=CMLib.map().roomLocation(I);
						if(R==null)
                            deRegisterClanItem(I);
                        else
						if(!A.inMyMetroArea(R.getArea()))
                            deRegisterClanItem(I);
                        else
						if(I.amDestroyed())
                            deRegisterClanItem(I);
                        else
						if((I.ciType()==ClanItem.CI_FLAG)&&(!R.isContent(I)))
                            deRegisterClanItem(I);
						else
						if(I!=null)
                        {
							I.setExpirationDate(0);
                            if((I.owner() instanceof Room)&&(I.container()!=null))
                                I.setContainer(null);
                        }
					}
				}

				// make sure holding clan still holds
				if((holdingClan.length()>0)
				&&(totalControlPoints>=0)
				&&(!flagFound(A,holdingClan)))
                {
                    if(CMSecurity.isDebugging("CONQUEST")) Log.debugOut("Conquest",holdingClan+" has "+totalControlPoints+" points and flag="+flagFound(A,holdingClan));
                    if((prevHoldingClan.length()>0)
                    &&(!holdingClan.equalsIgnoreCase(prevHoldingClan))
                    &&(CMLib.clans().getClan(prevHoldingClan)!=null)
                    &&(flagFound(A,prevHoldingClan)))
                        declareWinner(prevHoldingClan);
                    else
    					endClanRule();
                }
			}

            if((--revoltDown)<=0)
            {
                revoltDown=Conquerable.REVOLTFREQ;
                if(holdingClan.length()>0)
                {
                    Clan C=CMLib.clans().getClan(holdingClan);
                    if((C==null)||(C.getGovernment()!=Clan.GVT_THEOCRACY))
                    {
                        int chance=calcRevoltChance(A);
                    	if((REVOLTNOW)&&(chance<100))
                    	{
                        	Log.sysOut("Conquerable",A.Name()+" revolted against "+holdingClan+" with "+chance+"% chance");
                            if(CMSecurity.isDebugging("CONQUEST")) Log.debugOut("Conquest","The inhabitants of "+myArea.name()+" have revolted against "+holdingClan+" with "+chance+"% chance, after "+calcItemControlPoints(myArea)+" item points of "+totalControlPoints+" control points.");
                            Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS);
                            for(int i=0;i<channels.size();i++)
                                CMLib.commands().postChannel((String)channels.elementAt(i),"ALL","The inhabitants of "+myArea.name()+" have revolted against "+holdingClan+".",false);
                            if(journalName.length()>0)
                                CMLib.database().DBWriteJournal(journalName,"Conquest","ALL","The inhabitants of "+myArea.name()+" have revolted against "+holdingClan+".","See the subject line.");
                            if((prevHoldingClan.length()>0)
                            &&(!holdingClan.equalsIgnoreCase(prevHoldingClan))
                            &&(CMLib.clans().getClan(prevHoldingClan)!=null)
                            &&(flagFound(A,prevHoldingClan)))
                                declareWinner(prevHoldingClan);
                            else
                                endClanRule();
                    	}
                    	else
                    	{
    	                    if(CMLib.dice().rollPercentage()<chance)
    	                    {
    	                        Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS);
    	                        for(int i=0;i<channels.size();i++)
    	                            CMLib.commands().postChannel((String)channels.elementAt(i),"ALL","There are the rumblings of revolt in "+myArea.name()+".",false);
    	                    }
                    	}
                    }
                }
            }
            
			if((--pointDown)<=0)
			{
				pointDown=POINTFREQ;
				// slowly decrease control points over time
				synchronized(clanControlPoints)
				{
					for(int v=clanControlPoints.size()-1;v>=0;v--)
					{
						int[] pts=(int[])clanControlPoints.elementAt(v,2);
						if(pts[0]<=1)
							clanControlPoints.removeElementAt(v);
						else
							pts[0]--;
					}
				}
			}

			if((--fightDown)<=0)
			{
				fightDown=FIGHTFREQ;
				if(assaults.size()>0)
					synchronized(assaults)
					{
						while(assaults.size()>0)
						{
							MOB M1=(MOB)assaults.elementAt(0,1);
							MOB M2=(MOB)assaults.elementAt(0,2);
							if((M1!=M2)
							&&(M1.location()==M2.location())
							&&(!M1.isInCombat())
							&&(holdingClan.length()>0)
							&&(M1.getClanID().equals(holdingClan))
							&&(!M2.getClanID().equals(holdingClan))
							&&(CMLib.flags().canBeSeenBy(M2,M1)))
							{
								Vector V=new Vector();
								V.addElement("YELL");
								V.addElement(warCrys()[CMLib.dice().roll(1,warCrys().length,-1)]);
								M1.doCommand(V,Command.METAFLAG_FORCED);
								CMLib.combat().postAttack(M1,M2,M1.fetchWieldedItem());
							}
							assaults.removeElementAt(0);
						}
					}
			}
		}
		return true;
	}

    protected String getManadatoryWorshipID()
    {
        if(holdingClan.length()==0) return null; 
        Clan C=CMLib.clans().getClan(holdingClan);
        if(C==null) return null;
        if(C.getGovernment()==Clan.GVT_THEOCRACY)
        {
            MOB M=C.getResponsibleMember();
            if((M!=null)&&(M.getWorshipCharID().length()>0))
                return M.getWorshipCharID();
        }
        return null;
    }
    
	public void recalculateControlPoints(Area A)
	{
		totalControlPoints=0;
        String worship=getManadatoryWorshipID();
		for(Enumeration e=A.getMetroMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if((M!=null)
		        &&(M.isMonster())
				&&(M.getStartRoom()!=null)
				&&(A.inMyMetroArea(M.getStartRoom().getArea()))
				&&(!CMLib.flags().isAnimalIntelligence(M)))
				{
					if((M.getClanID().length()==0)
					&&(holdingClan.length()>0))
                    {
						M.setClanID(holdingClan);
                        if(worship!=null) M.setWorshipCharID(worship);
                    }
					totalControlPoints+=M.envStats().level();
				}
			}
		}
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
        boolean debugging=CMSecurity.isDebugging("CONQUEST");
		if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
		&&(msg.target() instanceof Room)
		&&(myArea!=null)
		&&(!CMSecurity.isDisabled("CONQUEST"))
		&&(totalControlPoints>=0)
		&&((!savedHoldingClan.equals(""))||(!holdingClan.equals(""))))
		{
			synchronized(clanItems)
			{
				for(int i=0;i<clanItems.size();i++)
				{
					ClanItem I=(ClanItem)clanItems.elementAt(i);
					Room R=CMLib.map().roomLocation(I);
					if((R==msg.target())
					&&(!((Item)I).amDestroyed())
					&&((I.ciType()!=ClanItem.CI_FLAG)||(R.isContent(I))))
						return false;
				}
			}
		}
		if((holdingClan.length()>0)
		&&(msg.source().getClanID().equals(holdingClan))
		&&(!CMSecurity.isDisabled("CONQUEST")))
		{
			if((msg.source().isMonster())
			&&(msg.target() instanceof MOB)
			&&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			&&(!((MOB)msg.target()).isInCombat())
			&&(msg.source().getVictim()!=msg.target())
			&&(((MOB)msg.target()).getClanID().equals(holdingClan))
            &&(!CMLib.flags().isAnimalIntelligence(msg.source())))
			{
				MOB target=(MOB)msg.target();
				msg.source().tell(target.name()+" is a fellow "+holdingClan+" member, and you must respect "+target.charStats().himher()+".");
				if(target.getVictim()==msg.source())
				{
					target.makePeace();
					target.setVictim(null);
				}
				return false;
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
			&&(msg.target() instanceof MOB)
			&&(myArea!=null)
			&&(((MOB)msg.target()).getStartRoom()!=null)
			&&(myArea.inMyMetroArea(((MOB)msg.target()).getStartRoom().getArea())))
				msg.setValue(0);
			else
			if((msg.targetMinor()==CMMsg.TYP_ORDER)
			&&(!msg.source().isMonster())
			&&(msg.target() instanceof MOB)
			&&(((MOB)msg.target()).isMonster()))
			{
				Item badge=null;
				Item I=null;
				ClanItem CI=null;
				for(int i=msg.source().inventorySize()-1;i>=0;i--)
				{
					I=msg.source().fetchInventory(i);
					if(I instanceof ClanItem)
					{
						CI=(ClanItem)I;
						if(CI.ciType()==ClanItem.CI_LEGALBADGE)
						{ badge=CI; break;}
					}
				}
				if(badge==null)
				{
					
				}
			}
		}
		else // must not be equal because of else to above
		if((holdingClan.length()>0)
		&&(msg.source().getClanID().length()>0)
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).amFollowing()==msg.source())
		&&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(!((MOB)msg.target()).isInCombat())
		&&(msg.source().getVictim()!=msg.target())
		&&(((MOB)msg.target()).getClanID().equals(holdingClan))
		&&(noMultiFollows.contains(msg.target()))
        &&(flagFound((Area)myHost,msg.source().getClanID())))
		{
			noMultiFollows.remove(msg.target());
            if(debugging) Log.debugOut("Conquest",msg.source().getClanID()+" lose "+(msg.target().envStats().level())+" points by harming "+msg.target().name());
			changeControlPoints(msg.source().getClanID(),-msg.target().envStats().level(),msg.source().location());
		}

        if((holdingClan.length()>0)
        &&(!CMSecurity.isDisabled("CONQUEST")))
        {
            if((msg.target() instanceof Room)
            &&(msg.tool() instanceof Ability)
            &&(msg.tool().ID().startsWith("Prayer_Infuse")))
            {
                if((!msg.source().getClanID().equals(holdingClan))
                ||(CMLib.clans().getClan(holdingClan)==null)
                ||(CMLib.clans().getClan(holdingClan).getGovernment()!=Clan.GVT_THEOCRACY))
                {
                    msg.source().tell("Only a member of a conquering theocracy can pray for that here.");
                    return false;
                }
            }
        
    		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
    		&&((msg.source().getStartRoom()==null)||(!myArea.inMyMetroArea(msg.source().getStartRoom().getArea())))
    		&&(msg.value()>0))
    		{
    			Clan C=CMLib.clans().getClan(holdingClan);
    			if(C.getTaxes()!=0)
    			{
                    MOB target=(msg.target() instanceof MOB)?(MOB)msg.target():null;
                    int lossAmt=(int)Math.round(CMath.mul(msg.value(),C.getTaxes()));
    				int clanAmt=(int)Math.round(CMath.mul(CMLib.leveler().adjustedExperience(msg.source(),target,msg.value()),C.getTaxes()));
    				if(lossAmt>0)
    				{
    					msg.setValue(msg.value()-lossAmt);
                        C.adjExp(clanAmt);
    					C.update();
    				}
    			}
    		}
        }
		return super.okMessage(myHost,msg);
	}

    protected void declareWinner(String clanID)
	{
		if((holdingClan.equals(clanID))||(totalControlPoints<0))
			return;
		Clan C=CMLib.clans().findClan(clanID);
		if(C==null) return;

        MOB mob=CMLib.map().mobCreated();
        mob.setName(clanID);
        if(myArea!=null)
            for(Enumeration e=myArea.getMetroMap();e.hasMoreElements();)
                if(!((Room)e.nextElement()).show(mob,myArea,null,CMMsg.MSG_AREAAFFECT,null,CMMsg.MSG_AREAAFFECT,"CONQUEST",CMMsg.MSG_AREAAFFECT,null))
                {
                    Log.errOut("Conquest","Conquest was stopped in "+myArea.name()+" for "+clanID+".");
                    return;
                }
        mob.destroy();
        if(CMSecurity.isDebugging("CONQUEST")) 
            Log.debugOut("Conquest","The inhabitants of "+myArea.name()+" are conquered by "+clanID+", vanquishing '"+holdingClan+"'.");
		if(holdingClan.length()>0)
		   endClanRule();

        revoltDown=REVOLTFREQ;
		holdingClan=clanID;
		synchronized(clanControlPoints)
		{
			clanControlPoints.clear();
		}
		if(myArea!=null)
		{
            String worship=getManadatoryWorshipID();
			for(Enumeration e=myArea.getMetroMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if((M!=null)
					&&(M.isMonster())
					&&(M.getStartRoom()!=null)
					&&(myArea.inMyMetroArea(M.getStartRoom().getArea()))
					&&(!CMLib.flags().isAnimalIntelligence(M))
					&&(M.getClanID().length()==0))
                    {
						M.setClanID(holdingClan);
                        if(worship!=null)
                            M.setWorshipCharID(worship);
                    }
				}
			}
            Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONQUESTS);
            for(int i=0;i<channels.size();i++)
                CMLib.commands().postChannel((String)channels.elementAt(i),"ALL",holdingClan+" gains control of "+myArea.name()+".",false);
			if(journalName.length()>0)
				CMLib.database().DBWriteJournal(journalName,"Conquest","ALL",holdingClan+" gains control of "+myArea.name()+".","See the subject line.");
            conquestDate=System.currentTimeMillis();
		}
	}

    protected void registerClanItem(Item I)
	{
		synchronized(clanItems)
		{
			if(!clanItems.contains(I))
				clanItems.addElement(I);
            if((I.owner() instanceof Room)
            &&(I.container()!=null))
                I.setContainer(null);
			I.setExpirationDate(0);
		}
	}
    
    protected void deRegisterClanItem(Item I)
    {
        synchronized(clanItems)
        {
            try
            {
                clanItems.removeElement(I);
            }
            catch(Exception e){e.printStackTrace();}
        }
    }


    protected boolean flagFound(Area A, String clanID)
	{
		if(CMLib.clans().findClan(clanID)==null) return false;
		synchronized(clanItems)
		{
			for(int i=0;i<clanItems.size();i++)
			{
				ClanItem I=(ClanItem)clanItems.elementAt(i);
				if((I.clanID().equals(clanID))
				&&(!I.amDestroyed())
				&&(I.ciType()==ClanItem.CI_FLAG))
				{
					Room R=CMLib.map().roomLocation(I);
					if((R!=null)&&((A==null)||(A.inMyMetroArea(R.getArea()))))
						return true;
				}
			}
		}
		if((holdingClan.length()>0)&&(holdingClan.equalsIgnoreCase(clanID))&&(myArea!=null))
		{
			// make a desperation check if we are talking about the holding clan.
			Room R=null;
			Item I=null;
			for(Enumeration e=myArea.getCompleteMap();e.hasMoreElements();)
			{
				R=(Room)e.nextElement();
				for(int i=0;i<R.numItems();i++)
				{
					I=R.fetchItem(i);
					if((I instanceof ClanItem)
					&&(((ClanItem)I).ciType()==ClanItem.CI_FLAG)
					&&(((ClanItem)I).clanID().equals(clanID))
					&&(!I.amDestroyed()))
					{
						registerClanItem(I);
						return true;
					}
				}
			}
		}
		return false;
	}

	public void setControlPoints(String clanID, int newControlPoints)
	{
		synchronized(clanControlPoints)
		{
			int index=-1;
			for(int v=0;v<clanControlPoints.size();v++)
			{
				if(((String)clanControlPoints.elementAt(v,1)).equalsIgnoreCase(clanID))
				{ index=v; break;}
			}
			if(index>=0)
				clanControlPoints.setElementAt(index,2,new int[]{newControlPoints});
			else
				clanControlPoints.addElement(clanID,new int[]{newControlPoints});
            if(newControlPoints>=totalControlPoints)
                declareWinner(clanID);
		}
	}
	
    protected boolean changeControlPoints(String clanID, int amount, Room notifyRoom)
	{
		synchronized(clanControlPoints)
		{
			int index=-1;
			for(int v=0;v<clanControlPoints.size();v++)
			{
				if(((String)clanControlPoints.elementAt(v,1)).equalsIgnoreCase(clanID))
				{ index=v; break;}
			}
            if((holdingClan.length()>0)
            &&(!clanID.equals(holdingClan))
            &&(CMLib.clans().getClanRelations(clanID,holdingClan)!=Clan.REL_WAR)
            &&(CMLib.clans().getClanRelations(holdingClan,clanID)!=Clan.REL_WAR))
                return false;
            announceToArea(myArea,clanID,amount);
			if(index<0)
			{
				if((holdingClan.length()>0)
				&&(!clanID.equals(holdingClan))
				&&(myArea!=null))
				{
					for(Enumeration e=myArea.getMetroMap();e.hasMoreElements();)
					{
						Room R=(Room)e.nextElement();
						for(int i=0;i<R.numInhabitants();i++)
						{
							MOB M=R.fetchInhabitant(i);
							if((M!=null)
							&&(M.isMonster())
							&&(M.getStartRoom()!=null)
							&&(myArea.inMyMetroArea(M.getStartRoom().getArea()))
							&&(M.getClanID().equals(clanID)))
								amount+=M.envStats().level();
						}
					}
				}
				if(amount>0)
				{
					int[] i=new int[1];
					i[0]+=amount;
					clanControlPoints.addElement(clanID,i);
                    if(i[0]>=totalControlPoints)
                        declareWinner(clanID);
				}
			}
			else
			{
				int[] i=(int[])clanControlPoints.elementAt(index,2);
				i[0]+=amount;
				if(i[0]<=0)
					clanControlPoints.removeElementAt(index);
				else
				if(i[0]>=totalControlPoints)
					declareWinner((String)clanControlPoints.elementAt(index,1));
			}
            if(CMSecurity.isDebugging("CONQUEST"))
            {
                index=clanControlPoints.indexOf(clanID);
                if(index<0)
                    Log.debugOut(clanID+" is not getting their points calculated.");
                else
                {
                    int[] i=(int[])clanControlPoints.elementAt(index,2);
                    if(i==null)
                        Log.debugOut(clanID+" is not getting their points calculated.");
                    else
                        Log.debugOut(clanID+" now has "+i[0]+" control points of "+totalControlPoints+" in "+myArea.name()+".");
                }
            }
		}
		return true;
	}


    protected static String[] warCrys()
    {
        return DEFAULT_WAR_CRYS;
    }
	protected static final String[] DEFAULT_WAR_CRYS={
		"INVADERS! Attack!",
		"We are under attack! To arms!",
		"Destroy the enemy!",
		"War!!!!!"
	};

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
        
        boolean debugging=CMSecurity.isDebugging("CONQUEST");
        if((msg.sourceMinor()==CMMsg.TYP_CLANEVENT)
        &&(msg.sourceMessage().startsWith("-")))
        {
        	if((holdingClan!=null)&&(holdingClan.equalsIgnoreCase(msg.sourceMessage().substring(1))))
        	{
                if(debugging) Log.debugOut("Conquest",holdingClan+" no longer exists.");
        		endClanRule();
        	}
        }
        else
		if((myHost instanceof Area)
		&&(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)
		&&(!CMSecurity.isDisabled("CONQUEST")))
		&&(totalControlPoints>=0))
		{
			// first look for kills and follows and register the points
			// from those events.  Protect against multi-follows using
			// a queue.
			if((((msg.sourceMinor()==CMMsg.TYP_DEATH)&&(msg.tool()!=null)&&(msg.tool() instanceof MOB))
				||((msg.sourceMinor()==CMMsg.TYP_FOLLOW)
						&&(msg.target()!=null)&&(msg.target() instanceof MOB)
						&&(!noMultiFollows.contains(msg.source()))))
			&&(msg.source().isMonster())
			&&(msg.source().getStartRoom()!=null))
			{
				Room R=msg.source().location();
				MOB killer=null;
				if(msg.sourceMinor()==CMMsg.TYP_FOLLOW)
				{
					if(noMultiFollows.size()>=70)
						noMultiFollows.removeElementAt(0);
					noMultiFollows.addElement(msg.source());
					if(msg.target() instanceof MOB)
						killer=(MOB)msg.target();
				}
				else
				if(msg.tool() instanceof MOB)
					killer=(MOB)msg.tool();
				if((killer!=null)&&(R!=null))
				{
					// make sure followers are picked up
                    HashSet killersSeen=new HashSet();
					while((killer.getClanID().length()==0)
					&&(killer.amFollowing()!=null)
					&&(R.isInhabitant(killer.amFollowing()))
                    &&(!killersSeen.contains(killer)))
                    {
                        killersSeen.add(killer);
						killer=killer.amFollowing();
                    }
						
					if(((Area)myHost).inMyMetroArea(msg.source().getStartRoom().getArea()))
					{ // a native was killed
						if((!killer.getClanID().equals(holdingClan))
                        &&(flagFound((Area)myHost,killer.getClanID())))
                        {
    						if(killer.getClanID().length()>0)
                            {
    							Clan C=CMLib.clans().getClan(killer.getClanID());
                                int level=msg.source().envStats().level();
    							if((C!=null)&&(C.getGovernment()==Clan.GVT_THEOCRACY)
    							&&(killer.getWorshipCharID().equals(msg.source().getWorshipCharID())))
    								level=(level>1)?level/2:level;
                                if(debugging) Log.debugOut("Conquest",killer.getClanID()+" gain "+level+" points by killing "+msg.source().name());
                                changeControlPoints(killer.getClanID(),level,killer.location());
                            }
                            else
                            if((killer.amFollowing()!=null)&&(killer.amFollowing().getClanID().length()>0))
                            {
    							Clan C=CMLib.clans().getClan(killer.amFollowing().getClanID());
                                int level=msg.source().envStats().level();
    							if((C!=null)&&(C.getGovernment()==Clan.GVT_THEOCRACY)
    							&&(killer.amFollowing().getWorshipCharID().equals(msg.source().getWorshipCharID())))
    								level=(level>1)?level/2:level;
                                if(debugging) Log.debugOut("Conquest",killer.amFollowing().getClanID()+" gain "+level+" points by killing "+msg.source().name());
                                changeControlPoints(killer.amFollowing().getClanID(),level,killer.location());
                            }
                        }
					}
					else // a foreigner was killed
					if((killer.getClanID().equals(holdingClan)) // killer is from holding clan
                    &&(holdingClan.length()>0)
					&&(msg.source().getClanID().length()>0)     // killed is a conquesting one
					&&(!msg.source().getClanID().equals(holdingClan))
					&&(flagFound((Area)myHost,msg.source().getClanID())))
                    {
                        if(debugging) Log.debugOut("Conquest",msg.source().getClanID()+" lose "+(msg.source().envStats().level())+" points by allowing the death of "+msg.source().name());
						changeControlPoints(msg.source().getClanID(),-msg.source().envStats().level(),killer.location());
                    }
				}
			}
			else
            if(((msg.tool() instanceof Ability)
            &&(msg.tool().ID().equals("Skill_Convert"))
            &&(msg.target() instanceof MOB)
            &&(msg.source().getClanID().length()>0)
            &&(((MOB)msg.target()).isMonster())
            &&(((MOB)msg.target()).getStartRoom()!=null)))
            {
                Clan C=CMLib.clans().getClan(msg.source().getClanID());
                if((C!=null)
                &&(C.getGovernment()==Clan.GVT_THEOCRACY)
                &&(((Area)myHost).inMyMetroArea(((MOB)msg.target()).getStartRoom().getArea()))
                &&(!msg.source().getClanID().equals(holdingClan))
                &&(flagFound((Area)myHost,msg.source().getClanID())))
                {
                    if(debugging) Log.debugOut("Conquest",msg.source().getClanID()+" gain "+(msg.source().envStats().level())+" points by converting "+msg.source().name());
                    changeControlPoints(msg.source().getClanID(),msg.target().envStats().level(),msg.source().location());
                }
            }
            else
			if((holdingClan.length()>0)
			&&(msg.source().isMonster())
            &&(msg.sourceMinor()==CMMsg.TYP_LIFE)
			&&(msg.source().getStartRoom()!=null)
			&&(((Area)myHost).inMyMetroArea(msg.source().getStartRoom().getArea()))
			&&(!CMLib.flags().isAnimalIntelligence(msg.source()))
			&&(!msg.source().getClanID().equals(holdingClan)))
            {
                String worship=getManadatoryWorshipID();
			    msg.source().setClanID(holdingClan);
                if(worship!=null) msg.source().setWorshipCharID(worship);
            }

			if(msg.tool() instanceof ClanItem)
				registerClanItem((ClanItem)msg.tool());
			if(msg.target() instanceof ClanItem)
				registerClanItem((ClanItem)msg.target());

			if(((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
			&&(msg.target() instanceof Room)
			&&(holdingClan.length()>0)
			&&(msg.source().getClanID().length()>0)
			&&(!msg.source().getClanID().equals(holdingClan))
			&&(((Room)msg.target()).numInhabitants()>0)
			&&(myArea.inMyMetroArea(((Room)msg.target()).getArea())))
			{
				Clan C=CMLib.clans().getClan(holdingClan);
				if(C==null)
                {
                    if(debugging) Log.debugOut("Conquest",holdingClan+" no longer exists.");
					endClanRule();
                }
				else
				if(C.getClanRelations(msg.source().getClanID())==Clan.REL_WAR)
				{
					Room R=(Room)msg.target();
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M=R.fetchInhabitant(i);
						if((M!=null)
						&&(M.isMonster())
						&&(M.getClanID().equals(holdingClan))
						&&(!M.getClanID().equals(msg.source().getClanID()))
						&&(!M.isInCombat())
						&&(!CMLib.flags().isAnimalIntelligence(M))
						&&(CMLib.flags().aliveAwakeMobileUnbound(M,true))
						&&(CMLib.flags().canBeSeenBy(msg.source(),M))
						&&(!assaults.contains(M)))
							assaults.addElement(M,msg.source());
					}
				}
			}
		}
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(myArea!=null)
		&&(!CMSecurity.isDisabled("CONQUEST")))
		{
			
			if(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
				waitToReload=0;
			else
				waitToReload=System.currentTimeMillis()+60000;
			if((totalControlPoints>=0)
			&&((!savedHoldingClan.equals(""))||(!holdingClan.equals(""))))
			{
				totalControlPoints=-1;
				StringBuffer data=new StringBuffer("");
				data.append(CMLib.xml().convertXMLtoTag("CLANID",holdingClan));
                data.append(CMLib.xml().convertXMLtoTag("OLDCLANID",prevHoldingClan));
                data.append(CMLib.xml().convertXMLtoTag("CLANDATE",conquestDate));
				data.append("<ACITEMS>");
				synchronized(clanItems)
				{
					for(int i=0;i<clanItems.size();i++)
					{
						ClanItem I=(ClanItem)clanItems.elementAt(i);
						Room R=CMLib.map().roomLocation(I);
						if((R!=null)
						&&(((Area)myHost).inMyMetroArea(R.getArea()))
						&&(!((Item)I).amDestroyed())
						&&((I.ciType()!=ClanItem.CI_FLAG)||(R.isContent(I))))
						{
							data.append("<ACITEM>");
							if(((Item)I).owner() instanceof Room)
								data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(R)));
							else
							if(((Item)I).owner() instanceof MOB)
							{
								MOB M=(MOB)((Item)I).owner();
								if((M.getStartRoom()!=null)
								&&(myArea.inMyMetroArea(M.getStartRoom().getArea())))
								{
									data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(M.getStartRoom())));
									data.append(CMLib.xml().convertXMLtoTag("MOB",((MOB)((Item)I).owner()).Name()));
								}
							}
							data.append(CMLib.xml().convertXMLtoTag("ICLAS",CMClass.classID(I)));
							data.append(CMLib.xml().convertXMLtoTag("IREJV",I.baseEnvStats().rejuv()));
							data.append(CMLib.xml().convertXMLtoTag("IUSES",((Item)I).usesRemaining()));
							data.append(CMLib.xml().convertXMLtoTag("ILEVL",I.baseEnvStats().level()));
							data.append(CMLib.xml().convertXMLtoTag("IABLE",I.baseEnvStats().ability()));
							data.append(CMLib.xml().convertXMLtoTag("ITEXT",CMLib.xml().parseOutAngleBrackets(I.text())));
							data.append("</ACITEM>");
                            ((Item)I).destroy();
						}
					}
					clanItems.clear();
				}
				savedHoldingClan="";
				holdingClan="";
                prevHoldingClan="";
				clanControlPoints=new DVector(2);
				data.append("</ACITEMS>");
				CMLib.database().DBReCreateData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name(),data.toString());
			}
		}
	}

	protected boolean isAnUltimateAuthorityHere(MOB M, Law laws)
	{
		if((holdingClan.length()==0)
		||(!allowLaw)
		||(totalControlPoints<0)
		||(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)))
			return false;
		Clan C=CMLib.clans().getClan(holdingClan);
		if(C==null)
        { 
            if(CMSecurity.isDebugging("CONQUEST")) Log.debugOut("Conquest",holdingClan+" no longer exists.");
            endClanRule(); 
            return false;
        }
		return C.allowedToDoThis(M,Clan.FUNC_CLANCANORDERCONQUERED)==1;
	}

	protected boolean theLawIsEnabled()
	{
		if((holdingClan.length()==0)
		||(!allowLaw)
		||(totalControlPoints<0)
		||(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
		||(CMSecurity.isDisabled("ARREST")))
			return false;
		if(flagFound(null,holdingClan))
			return true;
        if(CMSecurity.isDebugging("CONQUEST")) Log.debugOut("Conquest",holdingClan+" has "+totalControlPoints+" points and flag="+flagFound(null,holdingClan)+" in law check.");
		endClanRule();
		return false;
	}
}
