package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.io.*;
import java.util.*;

public class Conquerable extends Arrest
{
	public String ID(){return "Conquerable";}
	public Behavior newInstance(){ return new Conquerable();}
	protected boolean defaultModifiableNames(){return false;}
	protected String getLawParms(){ return "custom";}

	private String savedHoldingClan="";
	private String holdingClan="";
	private Vector clanItems=new Vector();
	private DVector clanControlPoints=new DVector(2);
	private DVector assaults=new DVector(2);
	private Vector noMultiFollows=new Vector();
	private int attitudePoints=-1;
	private int totalControlPoints=-1;
	private Area myArea=null;
	private String journalName="";
	private boolean allowLaw=false;

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
			Law laws=getLaws((Area)hostObj,false);
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
					if(holdingClan.length()==0)
						str.append("This area is not currently controlled by any clan.\n\r");
					else
					{
						Clan C=Clans.getClan(holdingClan);
						if(C!=null)
							str.append("This area is currently controlled by "+C.typeName()+" "+C.name()+".\n\r");
						else
						{
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
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return;

		for(int v=0;v<clanItems.size();v++)
		{
			Item I=(Item)clanItems.elementAt(v);
			if((I.owner() instanceof MOB)
			&&(I instanceof ClanItem)
			&&(((ClanItem)I).clanID().equals(holdingClan)))
			{
				MOB M=(MOB)I.owner();
				if((M.location()!=null)&&(!M.amDead()))
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
			for(Enumeration e=myArea.getMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=(MOB)R.fetchInhabitant(i);
					if((M!=null)&&(M.isMonster())&&(M.getStartRoom()!=null)
					&&(M.getStartRoom().getArea()==myArea)
					&&(M.getClanID().equals(holdingClan)))
						M.setClanID("");
				}
			}

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
		clanItems.clear();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
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
				clanItems.remove(I);
			else
				I.setDispossessionTime(0);
		}

		// calculate total control points
		// make sure all intelligent mobs belong to the clan
		if((totalControlPoints<0)&&(myArea!=null))
		{
			HashSet doneMOBs=new HashSet();
			Vector itemSet=CMClass.DBEngine().DBReadData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name());
			if((itemSet!=null)&&(itemSet.size()>0)&&(((Vector)itemSet.firstElement()).size()>3))
			{
				String data=(String)((Vector)itemSet.firstElement()).elementAt(3);
				Vector xml=XMLManager.parseAllXML(data);
				if(xml!=null)
				{
					savedHoldingClan=(String)XMLManager.getValFromPieces(xml,"CLANID");
					holdingClan=savedHoldingClan;
					Vector allData=XMLManager.getRealContentsFromPieces(xml,"ACITEMS");
					if(allData!=null)
					for(int c=0;c<allData.size();c++)
					{
						XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)allData.elementAt(c);
						if((iblk.tag.equalsIgnoreCase("ACITEM"))&&(iblk.contents!=null))
						{
							Vector roomData=iblk.contents;
							String roomID=(String)XMLManager.getValFromPieces(roomData,"ROOMID");
							String MOBname=(String)XMLManager.getValFromPieces(roomData,"MOB");
							Room R=CMMap.getRoom(roomID);
							if((R!=null)&&(R.getArea()==A))
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
										for(Enumeration e=A.getMap();e.hasMoreElements();)
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
										foundMOB.addInventory(newItem);
										newItem.wearIfPossible(foundMOB);
									}
									else
										R.addItem(newItem);
									clanItems.addElement(newItem);
								}
							}
						}
					}
				}
			}
			totalControlPoints=0;
			for(Enumeration e=A.getMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if((M!=null)
					&&(M.getStartRoom()!=null)
					&&(M.getStartRoom().getArea()==A)
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
						if((R==null)
						||(R.getArea()!=A)
						||((I instanceof Item)&&(((Item)I).amDestroyed()))
						||((I.ciType()==ClanItem.CI_FLAG)&&(!R.isContent((Item)I))))
							clanItems.removeElementAt(i);
						else
						if((I!=null)&&(I instanceof Item))
							((Item)I).setDispossessionTime(0);
					}
				}

				// make sure holding clan still holds
				if((holdingClan.length()>0)
				&&(!flagFound(A,holdingClan)))
					endClanRule();
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
		if((holdingClan.length()>0)
		&&(msg.source().getClanID().equals(holdingClan)))
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
			&&(((MOB)msg.target()).getStartRoom().getArea()==myArea))
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
			changeControlPoints(msg.source().getClanID(),-msg.target().envStats().level());
		}



		return super.okMessage(myHost,msg);
	}

	private void declareWinner(String clanID)
	{
		if(holdingClan.equals(clanID))
			return;
		Clan C=Clans.getClan(clanID);
		if(C==null) return;

		if(holdingClan.length()>0)
		   endClanRule();

		holdingClan=clanID;
		synchronized(clanControlPoints)
		{
			clanControlPoints.clear();
		}
		if(myArea!=null)
		{
			for(Enumeration e=myArea.getMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=(MOB)R.fetchInhabitant(i);
					if((M!=null)&&(M.isMonster())&&(M.getStartRoom()!=null)
					&&(M.getStartRoom().getArea()==myArea)
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

	private void registerClanItem(ClanItem I)
	{
		synchronized(clanItems)
		{
			if(!clanItems.contains(I))
				clanItems.addElement(I);
		}
	}


	private boolean flagFound(Area A, String clanID)
	{
		if(Clans.getClan(clanID)==null) return false;
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
					if((R!=null)&&((R.getArea()==A)||(A==null)))
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
					for(Enumeration e=myArea.getMap();e.hasMoreElements();)
					{
						Room R=(Room)e.nextElement();
						for(int i=0;i<R.numInhabitants();i++)
						{
							MOB M=R.fetchInhabitant(i);
							if((M!=null)
							&&(M.isMonster())
							&&(M.getStartRoom()!=null)
							&&(M.getStartRoom().getArea()==myArea)
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
		if((myHost instanceof Area)
		&&(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
		&&(totalControlPoints>0))
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
					if(msg.source().getStartRoom().getArea()==myHost)
					{ // a native was killed
						if((!killer.getClanID().equals(holdingClan))
						&&(killer.getClanID().length()>0)
						&&(flagFound((Area)myHost,killer.getClanID())))
							changeControlPoints(killer.getClanID(),msg.source().envStats().level());
					}
					else // a foreigner was killed
					if((killer.getClanID().equals(holdingClan))
					&&(msg.source().getClanID().length()>0)
					&&(flagFound((Area)myHost,msg.source().getClanID())))
						changeControlPoints(msg.source().getClanID(),-msg.source().envStats().level());
				}
			}
			else
			if((holdingClan.length()>0)
			&&(Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL)
			&&(msg.source().isMonster())
			&&(msg.source().getStartRoom()!=null)
			&&(msg.source().getStartRoom().getArea()==myHost)
			&&(!Sense.isAnimalIntelligence(msg.source()))
			&&(!msg.source().getClanID().equals(holdingClan))))
			   msg.source().setClanID(holdingClan);

			if(msg.tool() instanceof ClanItem)
				registerClanItem((ClanItem)msg.tool());
			if(msg.target() instanceof ClanItem)
				registerClanItem((ClanItem)msg.target());

			if((msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING)
			&&(msg.target() instanceof Room)
			&&(holdingClan.length()>0)
			&&(msg.source().getClanID().length()>0)
			&&(!msg.source().getClanID().equals(holdingClan))
			&&(((Room)msg.target()).numInhabitants()>0)
			&&(((Room)msg.target()).getArea()==myArea))
			{
				Clan C=Clans.getClan(holdingClan);
				if(C==null)
					endClanRule();
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
		if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
		&&(myArea!=null)
		&&((!savedHoldingClan.equals(""))||(!holdingClan.equals(""))))
		{
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
					&&(R.getArea()==myHost)
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
							&&(M.getStartRoom().getArea()==myArea))
							{
								data.append(XMLManager.convertXMLtoTag("ROOMID",CMMap.getExtendedRoomID(M.getStartRoom())));
								data.append(XMLManager.convertXMLtoTag("MOB",((MOB)((Item)I).owner()).Name()));
							}
						}
						data.append(XMLManager.convertXMLtoTag("ICLAS",CMClass.className(I)));
						data.append(XMLManager.convertXMLtoTag("IREJV",I.baseEnvStats().rejuv()));
						data.append(XMLManager.convertXMLtoTag("IUSES",((Item)I).usesRemaining()));
						data.append(XMLManager.convertXMLtoTag("ILEVL",I.baseEnvStats().level()));
						data.append(XMLManager.convertXMLtoTag("IABLE",I.baseEnvStats().ability()));
						data.append(XMLManager.convertXMLtoTag("ITEXT",CoffeeMaker.parseOutAngleBrackets(I.text())));
						data.append("</ACITEM>");
					}
				}
			}
			data.append("</ACITEMS>");
			CMClass.DBEngine().DBCreateData(myArea.name(),"CONQITEMS","CONQITEMS/"+myArea.name(),data.toString());
		}
	}

	protected boolean isAnUltimateAuthorityHere(MOB M, Law laws)
	{
		if((holdingClan.length()==0)
		||(!allowLaw)
		||(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED)))
			return false;
		Clan C=Clans.getClan(holdingClan);
		if(C==null){ endClanRule(); return false;}
		return C.allowedToDoThis(M,Clan.FUNC_CLANCANORDERCONQUERED)==1;
	}

	protected boolean theLawIsEnabled()
	{
		if((holdingClan.length()==0)
		||(!allowLaw)
		||(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED)))
			return false;
		if(flagFound(null,holdingClan))
			return true;
		else
			endClanRule();
		return false;
	}
}
