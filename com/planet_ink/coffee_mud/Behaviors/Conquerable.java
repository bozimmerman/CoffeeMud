package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.io.*;
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
public class Conquerable extends Arrest
{
	public String ID(){return "Conquerable";}
	protected boolean defaultModifiableNames(){return false;}
	protected String getLawParms(){ return "custom";}

	private String savedHoldingClan="";
	private String holdingClan="";
	private Vector clanItems=new Vector();
	private DVector clanControlPoints=new DVector(2);
	private DVector assaults=new DVector(2);
	private Vector noMultiFollows=new Vector();
	private int totalControlPoints=-1;
	private Area myArea=null;
	private String journalName="";
	private boolean allowLaw=false;
	private long waitToReload=0;

    private int revoltDown=REVOLTFREQ;
    private static final int REVOLTFREQ=(int)((IQCalendar.MILI_DAY*3)/MudHost.TICK_TIME);
	private int checkDown=0;
	private static final int CHECKFREQ=10;
	private int pointDown=0;
	private static final int POINTFREQ=(int)((10*60000)/MudHost.TICK_TIME);
	private int fightDown=0;
	private static final int FIGHTFREQ=2;

	// here are the codes for interacting with this behavior
	// see Law.java for info
	public boolean modifyBehavior(Environmental hostObj,
								  MOB mob,
								  Object O)
	{
		if((mob!=null)
		&&(mob.location()!=null)
		&&(hostObj!=null)
		&&(hostObj instanceof Area))
		{

			getLaws(hostObj,false);
			Integer I=null;
			Vector V=null;
			if(O instanceof Integer)
				I=(Integer)O;
			else
			if(O instanceof Vector)
			{
				V=(Vector)O;
				if(V.size()==0)
					return false;
				I=(Integer)V.firstElement();
			}
			else
				return false;
			switch(I.intValue())
			{
			case Law.MOD_RULINGCLAN:
				if(V!=null)
				{
					V.clear();
					V.addElement(holdingClan);
				}
				return true;
			case Law.MOD_CONTROLPOINTS:
				if(V!=null)
				{
					V.clear();
					if(totalControlPoints>=0)
						V.addElement(new Integer(totalControlPoints));
					else
						V.addElement(new Integer(0));
				}
				return true;
			case Law.MOD_WARINFO:
				if(V!=null)
				{
					V.clear();
					StringBuffer str=new StringBuffer("");
					if((holdingClan.length()==0)||(totalControlPoints<0))
						str.append("This area is not currently controlled by any clan.\n\r");
					else
					{
						Clan C=Clans.getClan(holdingClan);
						if(C!=null)
                        {
							str.append("This area is currently controlled by "+C.typeName()+" "+C.name()+".\n\r");
                            int pts=calcItemControlPoints((Area)hostObj);
                            int chance=calcRevoltChance((Area)hostObj);
                            str.append(C.name()+" has handed out clan items here for "+pts+" loyalty points.\n\r");
                            str.append("There is currently a "+chance+"% chance of revolt here.\n\r");
                        }
						else
						{
                            if(CMSecurity.isDebugging("CONQUEST")) Log.debugOut("Conquest",holdingClan+" has laid waste to "+myArea.name()+".");
							endClanRule();
							str.append("This area is laid waste by "+holdingClan+".\n\r");
						}
					}
					str.append("This area requires "+totalControlPoints+" points to control.\n\r");
					if(clanControlPoints.size()==0)
						str.append("There are no control points won at present by any clan.\n\r");
					synchronized(clanControlPoints)
					{
						for(int i=0;i<clanControlPoints.size();i++)
						{
							String clanID=(String)clanControlPoints.elementAt(i,1);
							int[] ic=(int[])clanControlPoints.elementAt(i,2);
							Clan C=Clans.getClan(clanID);
							if(C!=null)
								str.append(C.typeName()+" "+C.name()+" has "+ic[0]+" control points.\n\r");
						}
					}
					V.addElement(str.toString());
				}
				return true;
			default:
				break;
			}
		}
		return super.modifyBehavior(hostObj,mob,O);
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		journalName=Util.getParmStr(newParms,"JOURNAL","");
		allowLaw=Util.getParmStr(newParms,"LAW","FALSE").toUpperCase().startsWith("T");
		loadAttempt=false;
	}

	public boolean isAnyKindOfOfficer(Law laws, MOB M)
	{
		if((M!=null)
		&&(allowLaw)
		&&(!Sense.isAnimalIntelligence(M))
		&&(M.location()!=null)
		&&((!M.isMonster())||Sense.isMobile(M))
		&&(holdingClan.length()>0)
		&&(M.getClanID().equals(holdingClan)))
			for(int i=0;i<M.inventorySize();i++)
			{
				Item I=M.fetchInventory(i);
				if((I!=null)
				&&(I instanceof ClanItem)
				&&(!I.amWearingAt(Item.INVENTORY))
				&&(((ClanItem)I).ciType()==ClanItem.CI_BANNER))
					return true;
			}
		return false;
	}

	public boolean isTheJudge(Law laws, MOB M)
	{
		if((M!=null)
		&&(allowLaw)
		&&(!Sense.isAnimalIntelligence(M))
		&&(M.location()!=null)
		&&(holdingClan.length()>0)
		&&(M.getClanID().equals(holdingClan)))
			for(int i=0;i<M.inventorySize();i++)
			{
				Item I=M.fetchInventory(i);
				if((I!=null)
				&&(I instanceof ClanItem)
				&&(!I.amWearingAt(Item.INVENTORY))
				&&(((ClanItem)I).ciType()==ClanItem.CI_GAVEL))
					return true;
			}
		return false;
	}

	private synchronized void endClanRule()
	{
		if(holdingClan.length()==0)
			return;
		if((!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
		||(CMSecurity.isDisabled("CONQUEST")))
			return;

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
						M.setClanID("");
					I.setRawWornCode(0);
					I.setContainer(null);
					M.location().addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
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
					&&(myArea.inMetroArea(M.getStartRoom().getArea()))
					&&(M.getClanID().equals(holdingClan)))
						M.setClanID("");
				}
			}
            if(CMSecurity.isDebugging("CONQUEST")) Log.debugOut("Conquest",holdingClan+" has lost control of "+myArea.name()+".");
			CommonMsgs.channel("CLANTALK","ALL",holdingClan+" has lost control of "+myArea.name()+".",false);
			if(journalName.length()>0)
				CMClass.DBEngine().DBWriteJournal(journalName,"Conquest","ALL",holdingClan+" loses control of "+myArea.name()+".","See the subject line.",-1);
			Law laws=getLaws(myArea,false);
			if(laws.lawIsActivated())
			{
				laws.setInternalStr("ACTIVATED","FALSE");
				laws.resetLaw();
				CMClass.DBEngine().DBDeleteData(myArea.Name(),"ARREST",myArea.Name()+"/ARREST");
				CMClass.DBEngine().DBCreateData(myArea.Name(),"ARREST",myArea.Name()+"/ARREST",laws.rawLawString());
			}

		}
		holdingClan="";
        synchronized(clanItems)
        {
            try{
            for(int c=clanItems.size();c>=0;c--)
                if(((ClanItem)clanItems.elementAt(c)).ciType()!=ClanItem.CI_FLAG)
                    deRegisterClanItem(c);
            }catch(ArrayIndexOutOfBoundsException x){}
        }
	}

    public int calcItemControlPoints(Area A)
    {
        int itemControlPoints=0;
        synchronized(clanItems)
        {
            for(int i=clanItems.size()-1;i>=0;i--)
            {
                ClanItem I=(ClanItem)clanItems.elementAt(i);
                if((I instanceof Item)
                &&(!((Item)I).amDestroyed())
                &&(((Item)I).owner() instanceof MOB)
                &&(((MOB)((Item)I).owner()).isMonster())
                &&(Sense.isInTheGame(I,true))
                &&(A.inMetroArea(((MOB)((Item)I).owner()).getStartRoom().getArea()))
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
        int totalNeeded=(int)Math.round(Util.mul(0.05,totalControlPoints));
        if(totalNeeded<=0) totalNeeded=1;
        int chance=(int)Math.round(50.0-(Util.mul(50.0,Util.div(itemControlPoints,totalNeeded))));
        if(chance<=0) return 0;
        return chance;
        
    }
    
	public boolean tick(Tickable ticking, int tickID)
	{
		if((!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
		||(CMSecurity.isDisabled("CONQUEST")))
			return true;

		if(!super.tick(ticking,tickID))
			return false;
		if(tickID!=MudHost.TICK_AREA) return true;
		if(!(ticking instanceof Area)) return true;
		Area A=(Area)ticking;

		if(A!=myArea) myArea=A;

		for(int i=clanItems.size()-1;i>=0;i--)
		{
			Item I=(Item)clanItems.elementAt(i);
			if(!I.tick(this,MudHost.TICK_CLANITEM))
                deRegisterClanItem(i);
			else
				I.setDispossessionTime(0);
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
			Vector itemSet=CMClass.DBEngine().DBReadData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name());
			if((itemSet!=null)&&(itemSet.size()>0)&&(((Vector)itemSet.firstElement()).size()>3))
			{
				String data=(String)((Vector)itemSet.firstElement()).elementAt(3);
				Vector xml=XMLManager.parseAllXML(data);
				if(xml!=null)
				{
					savedHoldingClan=XMLManager.getValFromPieces(xml,"CLANID");
					holdingClan=savedHoldingClan;
					Vector allData=XMLManager.getRealContentsFromPieces(xml,"ACITEMS");
					if(allData!=null)
					for(int c=0;c<allData.size();c++)
					{
						XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)allData.elementAt(c);
						if((iblk.tag.equalsIgnoreCase("ACITEM"))&&(iblk.contents!=null))
						{
							Vector roomData=iblk.contents;
							String roomID=XMLManager.getValFromPieces(roomData,"ROOMID");
							String MOBname=XMLManager.getValFromPieces(roomData,"MOB");
							Room R=CMMap.getRoom(roomID);
							if((R!=null)&&(A.inMetroArea(R.getArea())))
							{
								String iClass=XMLManager.getValFromPieces(roomData,"ICLAS");
								Item newItem=CMClass.getItem(iClass);
								if(newItem!=null)
								{
									newItem.baseEnvStats().setLevel(XMLManager.getIntFromPieces(roomData,"ILEVL"));
									newItem.baseEnvStats().setAbility(XMLManager.getIntFromPieces(roomData,"IABLE"));
									newItem.baseEnvStats().setRejuv(XMLManager.getIntFromPieces(roomData,"IREJV"));
									newItem.setUsesRemaining(XMLManager.getIntFromPieces(roomData,"IUSES"));
									newItem.setMiscText(CoffeeMaker.restoreAngleBrackets(XMLManager.getValFromPieces(roomData,"ITEXT")));
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
    										newItem.wearIfPossible(foundMOB);
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
			totalControlPoints=0;
			for(Enumeration e=A.getMetroMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if((M!=null)
			        &&(M.isMonster())
					&&(M.getStartRoom()!=null)
					&&(A.inMetroArea(M.getStartRoom().getArea()))
					&&(!Sense.isAnimalIntelligence(M)))
					{
						if((M.getClanID().length()==0)
						&&(holdingClan.length()>0))
							M.setClanID(holdingClan);
						totalControlPoints+=M.envStats().level();
					}
				}
			}
		}
		else
		{
			if((--checkDown)<=0)
			{
				checkDown=CHECKFREQ;
				// make sure clanitems are truely in the area
				synchronized(clanItems)
				{
					for(int i=clanItems.size()-1;i>=0;i--)
					{
						ClanItem I=(ClanItem)clanItems.elementAt(i);
						Room R=CoffeeUtensils.roomLocation(I);
						if(R==null)
                            deRegisterClanItem(i);
                        else
						if(!A.inMetroArea(R.getArea()))
                            deRegisterClanItem(i);
                        else
						if((I instanceof Item)&&(((Item)I).amDestroyed()))
                            deRegisterClanItem(i);
                        else
						if((I.ciType()==ClanItem.CI_FLAG)&&(!R.isContent((Item)I)))
                            deRegisterClanItem(i);
						else
						if((I!=null)&&(I instanceof Item))
							((Item)I).setDispossessionTime(0);
					}
				}

				// make sure holding clan still holds
				if((holdingClan.length()>0)
				&&(totalControlPoints>=0)
				&&(!flagFound(A,holdingClan)))
                {
                    if(CMSecurity.isDebugging("CONQUEST")) Log.debugOut("Conquest",holdingClan+" has "+totalControlPoints+" points and flag="+flagFound(A,holdingClan));
					endClanRule();
                }
			}

            if((--revoltDown)<=0)
            {
                revoltDown=Conquerable.REVOLTFREQ;
                if(holdingClan.length()>0)
                {
                    int chance=calcRevoltChance(A);
                    if(Dice.rollPercentage()<chance)
                    {
                        if(CMSecurity.isDebugging("CONQUEST")) Log.debugOut("Conquest","The inhabitants of "+myArea.name()+" have revoluted against "+holdingClan+" with "+chance+"% chance, after "+calcItemControlPoints(myArea)+" item points of "+totalControlPoints+" control points.");
                        CommonMsgs.channel("CLANTALK","ALL","The inhabitants of "+myArea.name()+" have revoluted against "+holdingClan+".",false);
                        if(journalName.length()>0)
                            CMClass.DBEngine().DBWriteJournal(journalName,"Conquest","ALL","The inhabitants of "+myArea.name()+" have revoluted against "+holdingClan+".","See the subject line.",-1);
                        endClanRule();
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
							&&(Sense.canBeSeenBy(M2,M1)))
							{
								Vector V=new Vector();
								V.addElement("YELL");
								V.addElement(warCrys[Dice.roll(1,warCrys.length,-1)]);
								M1.doCommand(V);
								MUDFight.postAttack(M1,M2,M1.fetchWieldedItem());
							}
							assaults.removeElementAt(0);
						}
					}
			}
		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
        boolean debugging=CMSecurity.isDebugging("CONQUEST");
		if((holdingClan.length()>0)
		&&(msg.source().getClanID().equals(holdingClan))
		&&(!CMSecurity.isDisabled("CONQUEST")))
		{
			if((msg.source().isMonster())
			&&(msg.target() instanceof MOB)
			&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			&&(!((MOB)msg.target()).isInCombat())
			&&(msg.source().getVictim()!=msg.target())
			&&(((MOB)msg.target()).getClanID().equals(holdingClan)))
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
			&&(myArea.inMetroArea(((MOB)msg.target()).getStartRoom().getArea())))
				msg.setValue(0);
		}
		else // must not be equal because of else to above
		if((holdingClan.length()>0)
		&&(msg.source().getClanID().length()>0)
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).amFollowing()==msg.source())
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(!((MOB)msg.target()).isInCombat())
		&&(msg.source().getVictim()!=msg.target())
		&&(((MOB)msg.target()).getClanID().equals(holdingClan))
		&&(noMultiFollows.contains(msg.target())))
		{
			noMultiFollows.remove(msg.target());
            if(debugging) Log.debugOut("Conquest",msg.source().getClanID()+" lose "+(msg.target().envStats().level())+" points by harming "+msg.target().name());
			changeControlPoints(msg.source().getClanID(),-msg.target().envStats().level());
		}

		if((holdingClan.length()>0)
		&&(msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(!CMSecurity.isDisabled("CONQUEST"))
		&&(!msg.source().isMonster())
		&&(msg.value()>0))
		{
			Clan C=Clans.getClan(holdingClan);
			if(C.getTaxes()!=0)
			{
				int value=(int)Math.round(Util.mul(msg.value(),C.getTaxes()));
				if(value>0)
				{
					msg.setValue(msg.value()-value);
					C.setExp(C.getExp()+value);
					C.update();
				}
			}
		}

		return super.okMessage(myHost,msg);
	}

	private void declareWinner(String clanID)
	{
		if((holdingClan.equals(clanID))||(totalControlPoints<0))
			return;
		Clan C=Clans.findClan(clanID);
		if(C==null) return;

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
			for(Enumeration e=myArea.getMetroMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if((M!=null)
					&&(M.isMonster())
					&&(M.getStartRoom()!=null)
					&&(myArea.inMetroArea(M.getStartRoom().getArea()))
					&&(!Sense.isAnimalIntelligence(M))
					&&(M.getClanID().length()==0))
						M.setClanID(holdingClan);
				}
			}
			CommonMsgs.channel("CLANTALK","ALL",holdingClan+" gains control of "+myArea.name()+".",false);
			if(journalName.length()>0)
				CMClass.DBEngine().DBWriteJournal(journalName,"Conquest","ALL",holdingClan+" gains control of "+myArea.name()+".","See the subject line.",-1);
		}
	}

	private void registerClanItem(Environmental I)
	{
		synchronized(clanItems)
		{
			if(!clanItems.contains(I))
				clanItems.addElement(I);
		}
	}
    
    private void deRegisterClanItem(int i)
    {
        synchronized(clanItems)
        {
            try
            {
                clanItems.removeElementAt(i);
            }
            catch(Exception e){e.printStackTrace();}
        }
    }


	private boolean flagFound(Area A, String clanID)
	{
		if(Clans.findClan(clanID)==null) return false;
		synchronized(clanItems)
		{
			for(int i=0;i<clanItems.size();i++)
			{
				ClanItem I=(ClanItem)clanItems.elementAt(i);
				if((I.clanID().equals(clanID))
				&&(I instanceof Item)
				&&(!((Item)I).amDestroyed())
				&&(I.ciType()==ClanItem.CI_FLAG))
				{
					Room R=CoffeeUtensils.roomLocation(I);
					if((R!=null)&&((A==null)||(A.inMetroArea(R.getArea()))))
						return true;
				}
			}
		}
		return false;
	}

	private boolean changeControlPoints(String clanID, int amount)
	{
		synchronized(clanControlPoints)
		{
			int index=-1;
			for(int v=0;v<clanControlPoints.size();v++)
			{
				if(((String)clanControlPoints.elementAt(v,1)).equalsIgnoreCase(clanID))
				{ index=v; break;}
			}
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
							&&(myArea.inMetroArea(M.getStartRoom().getArea()))
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
                    Log.debugOut(clanID+" is not getting their points calculted.");
                else
                {
                    int[] i=(int[])clanControlPoints.elementAt(index,2);
                    if(i==null)
                        Log.debugOut(clanID+" is not getting their points calculted.");
                    else
                        Log.debugOut(clanID+" now has "+i[0]+" control points of "+totalControlPoints+" in "+myArea.name()+".");
                }
            }
		}
		return true;
	}


	private static final String[] warCrys={
		"INVADERS! Attack!",
		"We are under attack! To arms!",
		"Destroy the enemy!",
		"War!!!!!"
	};

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
        
        boolean debugging=CMSecurity.isDebugging("CONQUEST");
		if((myHost instanceof Area)
		&&(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED)
		&&(!CMSecurity.isDisabled("CONQUEST")))
		&&(totalControlPoints>=0))
		{
			// first look for kills and follows and register the points
			// from those events.  Protect against multi-follows using
			// a queue.
			if((((msg.sourceMinor()==CMMsg.TYP_DEATH)&&(msg.tool()!=null)&&(msg.tool() instanceof MOB))
				||((msg.sourceMinor()==CMMsg.TYP_FOLLOW)&&(msg.target()!=null)&&(msg.target() instanceof MOB)&&(!noMultiFollows.contains(msg.source()))))
			&&(msg.source().isMonster())
			&&(msg.source().getStartRoom()!=null))
			{
				MOB killer=null;
				if(msg.sourceMinor()==CMMsg.TYP_FOLLOW)
				{
					if(noMultiFollows.size()>=7)
						noMultiFollows.removeElementAt(0);
					noMultiFollows.addElement(msg.source());
					if(msg.target() instanceof MOB)
						killer=(MOB)msg.target();
				}
				else
				if(msg.tool() instanceof MOB)
					killer=(MOB)msg.tool();
				if(killer!=null)
				{
					if(((Area)myHost).inMetroArea(msg.source().getStartRoom().getArea()))
					{ // a native was killed
						if((!killer.getClanID().equals(holdingClan))
                        &&(flagFound((Area)myHost,killer.getClanID())))
                        {
    						if(killer.getClanID().length()>0)
                            {
                                if(debugging) Log.debugOut("Conquest",killer.getClanID()+" gain "+(msg.source().envStats().level())+" points by killing "+msg.source().name());
                                changeControlPoints(killer.getClanID(),msg.source().envStats().level());
                            }
                            else
                            if((killer.amFollowing()!=null)&&(killer.amFollowing().getClanID().length()>0))
                            {
                                if(debugging) Log.debugOut("Conquest",killer.amFollowing().getClanID()+" gain "+(msg.source().envStats().level())+" points by killing "+msg.source().name());
                                changeControlPoints(killer.amFollowing().getClanID(),msg.source().envStats().level());
                            }
                        }
					}
					else // a foreigner was killed
					if((killer.getClanID().equals(holdingClan))
					&&(msg.source().getClanID().length()>0)
					&&(flagFound((Area)myHost,msg.source().getClanID())))
                    {
                        if(debugging) Log.debugOut("Conquest",msg.source().getClanID()+" lose "+(msg.source().envStats().level())+" points by allowing the death of "+msg.source().name());
						changeControlPoints(msg.source().getClanID(),-msg.source().envStats().level());
                    }
				}
			}
			else
			if((holdingClan.length()>0)
			&&(Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL)
			&&(msg.source().isMonster())
			&&(msg.source().getStartRoom()!=null)
			&&(((Area)myHost).inMetroArea(msg.source().getStartRoom().getArea()))
			&&(!Sense.isAnimalIntelligence(msg.source()))
			&&(!msg.source().getClanID().equals(holdingClan))))
			   msg.source().setClanID(holdingClan);

			if(msg.tool() instanceof ClanItem)
				registerClanItem(msg.tool());
			if(msg.target() instanceof ClanItem)
				registerClanItem(msg.target());

			if((msg.targetMinor()==CMMsg.TYP_LOOK)
			&&(msg.target() instanceof Room)
			&&(holdingClan.length()>0)
			&&(msg.source().getClanID().length()>0)
			&&(!msg.source().getClanID().equals(holdingClan))
			&&(((Room)msg.target()).numInhabitants()>0)
			&&(myArea.inMetroArea(((Room)msg.target()).getArea())))
			{
				Clan C=Clans.getClan(holdingClan);
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
						&&(!M.isInCombat())
						&&(!Sense.isAnimalIntelligence(M))
						&&(Sense.aliveAwakeMobile(M,true))
						&&(Sense.canBeSeenBy(msg.source(),M))
						&&(!assaults.contains(M)))
							assaults.addElement(M,msg.source());
					}
				}
			}
		}
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(myArea!=null)
		&&(!CMSecurity.isDisabled("CONQUEST")))
		{
			waitToReload=System.currentTimeMillis()+60000;
			if((totalControlPoints>=0)
			&&((!savedHoldingClan.equals(""))||(!holdingClan.equals(""))))
			{
				totalControlPoints=-1;
				CMClass.DBEngine().DBDeleteData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name());
				StringBuffer data=new StringBuffer("");
				data.append(XMLManager.convertXMLtoTag("CLANID",holdingClan));
				data.append("<ACITEMS>");
				synchronized(clanItems)
				{
					for(int i=0;i<clanItems.size();i++)
					{
						ClanItem I=(ClanItem)clanItems.elementAt(i);
						Room R=CoffeeUtensils.roomLocation(I);
						if((R!=null)
						&&(((Area)myHost).inMetroArea(R.getArea()))
						&&(I instanceof Item)
						&&(!((Item)I).amDestroyed())
						&&((I.ciType()!=ClanItem.CI_FLAG)||(R.isContent((Item)I))))
						{
							data.append("<ACITEM>");
							if(((Item)I).owner() instanceof Room)
								data.append(XMLManager.convertXMLtoTag("ROOMID",CMMap.getExtendedRoomID(R)));
							else
							if(((Item)I).owner() instanceof MOB)
							{
								MOB M=(MOB)((Item)I).owner();
								if((M.getStartRoom()!=null)
								&&(myArea.inMetroArea(M.getStartRoom().getArea())))
								{
									data.append(XMLManager.convertXMLtoTag("ROOMID",CMMap.getExtendedRoomID(M.getStartRoom())));
									data.append(XMLManager.convertXMLtoTag("MOB",((MOB)((Item)I).owner()).Name()));
								}
							}
							((Item)I).destroy();
							data.append(XMLManager.convertXMLtoTag("ICLAS",CMClass.className(I)));
							data.append(XMLManager.convertXMLtoTag("IREJV",I.baseEnvStats().rejuv()));
							data.append(XMLManager.convertXMLtoTag("IUSES",((Item)I).usesRemaining()));
							data.append(XMLManager.convertXMLtoTag("ILEVL",I.baseEnvStats().level()));
							data.append(XMLManager.convertXMLtoTag("IABLE",I.baseEnvStats().ability()));
							data.append(XMLManager.convertXMLtoTag("ITEXT",CoffeeMaker.parseOutAngleBrackets(I.text())));
							data.append("</ACITEM>");
						}
					}
					clanItems.clear();
				}
				savedHoldingClan="";
				holdingClan="";
				clanControlPoints=new DVector(2);
				data.append("</ACITEMS>");
				CMClass.DBEngine().DBCreateData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name(),data.toString());
			}
		}
	}

	protected boolean isAnUltimateAuthorityHere(MOB M, Law laws)
	{
		if((holdingClan.length()==0)
		||(!allowLaw)
		||(totalControlPoints<0)
		||(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED)))
			return false;
		Clan C=Clans.getClan(holdingClan);
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
		||(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
		||(CMSecurity.isDisabled("ARREST")))
			return false;
		if(flagFound(null,holdingClan))
			return true;
		else
        {
            if(CMSecurity.isDebugging("CONQUEST")) Log.debugOut("Conquest",holdingClan+" has "+totalControlPoints+" points and flag="+flagFound(null,holdingClan)+" in law check.");
			endClanRule();
        }
		return false;
	}
}
