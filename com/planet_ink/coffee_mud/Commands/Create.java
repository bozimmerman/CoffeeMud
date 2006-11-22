package com.planet_ink.coffee_mud.Commands;
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
public class Create extends BaseGenerics
{
	public Create(){}

	private String[] access={getScr("Create","cmd1")};
	public String[] getAccessWords(){return access;}

	public void exits(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell(getScr("Create","notingridchild"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}
		if(commands.size()<4)
		{
			mob.tell(getScr("Create","badcreateexit"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell(getScr("Create","baddir")+Directions.DIRECTIONS_DESC+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}

		String Locale=(String)commands.elementAt(3);
		Exit thisExit=CMClass.getExit(Locale);
		if(thisExit==null)
		{
			mob.tell(getScr("Create","badexittype")+Locale+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}

		Exit opExit=mob.location().rawExits()[direction];
		Room opRoom=mob.location().rawDoors()[direction];

		Exit reverseExit=null;
		if(opRoom!=null)
			reverseExit=opRoom.rawExits()[Directions.getOpDirectionCode(direction)];
		if(reverseExit!=null)
		{
			if((thisExit.isGeneric())&&(reverseExit.isGeneric()))
			{
				thisExit=(Exit)reverseExit.copyOf();
				modifyGenExit(mob,thisExit);
			}
		}


		mob.location().rawExits()[direction]=thisExit;
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Create","portalopens")+Directions.getInDirectionName(direction)+".\n\r");
		CMLib.database().DBUpdateExits(mob.location());
		if((reverseExit!=null)&&(opExit!=null)&&(opRoom!=null))
		{
			int revDirCode=Directions.getOpDirectionCode(direction);
			if(opRoom.rawExits()[revDirCode]==reverseExit)
			{
				opRoom.rawExits()[revDirCode]=(Exit)thisExit.copyOf();
				CMLib.database().DBUpdateExits(opRoom);
			}
		}
		else
		if((reverseExit==null)&&(opExit==null)&&(opRoom!=null))
		{
			int revDirCode=Directions.getOpDirectionCode(direction);
			if((opRoom.rawExits()[revDirCode]==null)&&(opRoom.rawDoors()[revDirCode]==mob.location()))
			{
				opRoom.rawExits()[revDirCode]=(Exit)thisExit.copyOf();
				CMLib.database().DBUpdateExits(opRoom);
			}
		}
		mob.location().getArea().fillInAreaRoom(mob.location());
		if(opRoom!=null) opRoom.getArea().fillInAreaRoom(opRoom);
		Log.sysOut("Exits",mob.location().roomID()+getScr("Create","exitschanged")+mob.Name()+".");
	}

    public void polls(MOB mob, Vector commands)
    {
    }
    
	public void items(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Create","badcreateitem"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}

		String itemID=CMParms.combine(commands,2);
		Environmental dest=mob.location();
		int x=itemID.indexOf("@");
		if(x>0)
		{
			String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if((!rest.equalsIgnoreCase(getScr("Create","room")))
			&&(rest.length()>0))
			{
				MOB M=mob.location().fetchInhabitant(rest);
				if(M==null)
				{
					mob.tell(getScr("Create","mobnotfound",rest));
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
					return;
				}
				dest=M;
			}
		}
		Item newItem=CMClass.getItem(itemID);
		if((newItem==null)&&(CMLib.english().numPossibleGold(null,itemID)>0))
		{
		    long numCoins=CMLib.english().numPossibleGold(null,itemID);
		    String currency=CMLib.english().numPossibleGoldCurrency(mob,itemID);
		    double denom=CMLib.english().numPossibleGoldDenomination(mob,currency,itemID);
		    if((numCoins>0)&&(denom>0.0))
			    newItem=CMLib.beanCounter().makeCurrency(currency,denom,numCoins);
		}

		if(newItem==null)
		{
			mob.tell(getScr("Create","nosuchthing")+itemID+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}

		if((newItem instanceof ArchonOnly)
		&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell(getScr("Create","noreally"));
			return;
		}

		if(newItem.subjectToWearAndTear())
			newItem.setUsesRemaining(100);
		if(dest instanceof Room)
		{
			((Room)dest).addItem(newItem);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Create","dropsfromsky",newItem.name()));
		}
		else
		if(dest instanceof MOB)
		{
			((MOB)dest).addInventory(newItem);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Create","dropsintoarms",newItem.name(),dest.name()));
		}

		if(newItem.isGeneric())
			genMiscSet(mob,newItem);
		mob.location().recoverRoomStats();
		Log.sysOut("Items",mob.Name()+getScr("Create","createitem")+newItem.ID()+".");
	}

	public void players(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Create","badcreateuser"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}
	
		String mobID=CMParms.combine(commands,2);
		MOB M=CMLib.map().getLoadPlayer(mobID);
		if(M!=null)
		{
			mob.tell(getScr("Create","alreadyplayer")+M.Name()+"'!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}
		if(!CMLib.login().isOkName(mobID))
		{
			mob.tell("'"+mobID+getScr("Create","badname"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}
		mobID=CMStrings.capitalizeAndLower(mobID);
		M=CMClass.getMOB("StdMOB");
		M.setName(mobID);
		CMLib.login().createCharacter(M,mobID,mob.session());
		M=CMLib.map().getLoadPlayer(mobID);
		if(M!=null)
		{
			if(CMLib.flags().isInTheGame(M,true))
				M.removeFromGame(false);
			modifyPlayer(mob,M);
			Log.sysOut("Mobs",mob.Name()+getScr("Create","createdplayer")+M.Name()+".");
		}
	}

	public void rooms(MOB mob, Vector commands)
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell(getScr("Create","notingridchild"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}
		if(commands.size()<4)
		{
			mob.tell(getScr("Create","badcreateroom"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell(getScr("Create","baddir")+Directions.DIRECTIONS_DESC+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}

		Room thisRoom=null;
		String Locale=(String)commands.elementAt(3);
		thisRoom=CMClass.getLocale(Locale);
		if(thisRoom==null)
		{
			mob.tell(getScr("Create","badcreateroomagain",Locale));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}
		Room room=mob.location();
		thisRoom.setRoomID(room.getArea().getNewRoomID(room,direction));
		thisRoom.setArea(room.getArea());
		if(thisRoom.roomID().length()==0)
		{
			mob.tell(getScr("Create","badroomdir"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}
		thisRoom.setDisplayText(CMClass.classID(thisRoom)+"-"+thisRoom.roomID());
		thisRoom.setDescription("");
		CMLib.database().DBCreateRoom(thisRoom,Locale);

		if(thisRoom==null)
		{
			mob.tell(getScr("Create","badcreateroomyetagain"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}

		CMLib.map().createNewExit(mob.location(),thisRoom,direction);

		mob.location().recoverRoomStats();
		thisRoom.recoverRoomStats();
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().getArea().fillInAreaRoom(thisRoom);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Create","earthfalls"));
		Log.sysOut("Rooms",mob.Name()+getScr("Create","createdroom")+thisRoom.roomID()+".");
	}

	public void mobs(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Create","badcreatemob"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}

		String mobID=((String)commands.elementAt(2));
		MOB newMOB=CMClass.getMOB(mobID);

		if(newMOB==null)
		{
			mob.tell(getScr("Create","nosuchthing")+mobID+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}

		if(newMOB.Name().length()==0)
			newMOB.setName(getScr("Create","stanmob"));
		newMOB.setStartRoom(mob.location());
		newMOB.setLocation(mob.location());
        long rejuv=Tickable.TICKS_PER_RLMIN+Tickable.TICKS_PER_RLMIN+(Tickable.TICKS_PER_RLMIN/2);
        if(rejuv>(Tickable.TICKS_PER_RLMIN*20)) rejuv=(Tickable.TICKS_PER_RLMIN*20);
		newMOB.envStats().setRejuv((int)rejuv);
		newMOB.baseCharStats().getMyRace().setHeightWeight(newMOB.baseEnvStats(),(char)newMOB.baseCharStats().getStat(CharStats.STAT_GENDER));
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(mob.location(),true);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","instantiated",newMOB.name()));
		if(newMOB.isGeneric())
			genMiscSet(mob,newMOB);
		Log.sysOut("Mobs",mob.Name()+getScr("Create","createdmob")+newMOB.Name()+".");
	}

	public void races(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Create","badcreaterace"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		String raceID=CMParms.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if((R!=null)&&(R.isGeneric()))
		{
			mob.tell(getScr("Create","racealreadyexists",R.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		if(raceID.indexOf(" ")>=0)
		{
			mob.tell("'"+raceID+getScr("Create","badraceid"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		Race GR=(Race)CMClass.getRace("GenRace").copyOf();
		GR.setRacialParms("<RACE><ID>"+CMStrings.capitalizeAndLower(raceID)+"</ID><NAME>"+CMStrings.capitalizeAndLower(raceID)+"</NAME></RACE>");
		CMClass.addRace(GR);
		modifyGenRace(mob,GR);
		CMLib.database().DBCreateRace(GR.ID(),GR.racialParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Create","diversityup"));
	}

	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Create","badcreatearea"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		String areaName=CMParms.combine(commands,2);
		Area A=CMLib.map().getArea(areaName);
		if(A!=null)
		{
			mob.tell(getScr("Create","areaexists",A.name()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		String areaType="";
		int tries=0;
		while((areaType.length()==0)&&((++tries)<10))
		{
			areaType=mob.session().prompt(getScr("Create","enterareatype"),"StdArea");
			if(CMClass.getAreaType(areaType)==null)
			{
				mob.session().println(getScr("Create","badareatype"));
				mob.session().println(CMLib.lister().reallyList(CMClass.areaTypes(),-1,null).toString());
				areaType="";
			}
		}
		if(areaType.length()==0) areaType="StdArea";
		A=CMLib.database().DBCreateArea(areaName,areaType);
		A.setName(areaName);
		Room R=CMClass.getLocale("StdRoom");
		R.setRoomID(A.getNewRoomID(R,-1));
		R.setArea(A);
		R.setDisplayText(CMClass.classID(R)+"-"+R.roomID());
		R.setDescription("");
		CMLib.database().DBCreateRoom(R,R.ID());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Create","sizeincreased"));
		mob.tell(getScr("Create","nowat")+R.roomID()+".");
		R.bringMobHere(mob,true);
        CMLib.coffeeMaker().addAutoPropsToAreaIfNecessary(A);
	}

	public void components(MOB mob, Vector commands)
    throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Create","badcreatecomponent"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		String skillID=CMParms.combine(commands,2);
		Ability A=CMClass.getAbility(skillID);
		if(A==null)
		{
			mob.tell("'"+skillID+getScr("Create","badskillid"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
        skillID=A.ID();
		if(CMLib.ableMapper().getAbilityComponentMap().get(A.ID().toUpperCase())!=null)
		{
			mob.tell("'"+A.ID()+getScr("Create","compexists"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
        DVector DV=new DVector(6);
        CMLib.ableMapper().getAbilityComponentMap().put(skillID.toUpperCase().trim(),DV);
        CMLib.ableMapper().addBlankAbilityComponent(DV);
        super.modifyComponents(mob,skillID);
        String parms=CMLib.ableMapper().getAbilityComponentCodedString(skillID);
		String error=CMLib.ableMapper().addAbilityComponent(parms,CMLib.ableMapper().getAbilityComponentMap());
		if(error!=null)
		{
			mob.tell(error);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		CMFile F=new CMFile(Resources.makeFileResourceName("skills/components.txt"),null,true);
        StringBuffer crtestbuf=F.textUnformatted();
        boolean addCR=false;
        for(int c=crtestbuf.length()-1;c>=0;c--)
            if((crtestbuf.charAt(c)=='\n')||(crtestbuf.charAt(c)=='\r'))
                break;
            else
            if(Character.isWhitespace(crtestbuf.charAt(c)))
                continue;
            else
            { addCR=true; break;}
		F.saveText((addCR?"\n":"")+parms,true);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Create","complicationup"));
	}
    
    public void expertises(MOB mob, Vector commands)
    {
        if((commands.size()<3)||(CMParms.combine(commands,1).indexOf("=")<0))
        {
            mob.tell(getScr("Create","badcreateexpertise"));
            StringBuffer buf=new CMFile(Resources.makeFileResourceName("skills/expertises.txt"),null,true).text();
            StringBuffer inst=new StringBuffer("");
            Vector V=new Vector();
            if(buf!=null) V=Resources.getFileLineVector(buf);
            for(int v=0;v<V.size();v++)
                if(((String)V.elementAt(v)).startsWith("#"))
                    inst.append(((String)V.elementAt(v)).substring(1)+"\n\r");
                else
                if(((String)V.elementAt(v)).length()>0) 
                    break;
            if(mob.session()!=null) mob.session().wraplessPrintln(inst.toString());
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
            return;
        }
        String parms=CMParms.combineWithQuotes(commands,2);
        String skillID=parms.substring(0,parms.indexOf("="));
        if(skillID.indexOf(" ")>=0)
        {
            mob.tell(getScr("Create","badexperspace"));
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
            return;
        }
        String WKID=CMStrings.replaceAll(skillID.toUpperCase(),"@X1","");
        WKID=CMStrings.replaceAll(WKID,"@X2","").trim();
        if(CMLib.expertises().getStages(WKID)>0)
        {
            mob.tell("'"+WKID+getScr("Create","compexists"));
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
            return;
        }
        String error=CMLib.expertises().confirmExpertiseLine(parms,null,false);
        if(error!=null)
        {
            mob.tell(error);
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
            return;
        }
        CMFile F=new CMFile(Resources.makeFileResourceName("skills/expertises.txt"),null,true);
        F.saveText("\n"+parms,true);
        Resources.removeResource("skills/expertises.txt");
        CMLib.expertises().recompileExpertises();
        mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Create","skilluseup"));
    }
    
    public void titles(MOB mob, Vector commands)
    {
        if((commands.size()<3)||(CMParms.combine(commands,1).indexOf("=")<0))
        {
            mob.tell(getScr("Create","badcreatetitle"));
            StringBuffer buf=new CMFile(Resources.makeFileResourceName("titles.txt"),null,true).text();
            StringBuffer inst=new StringBuffer("");
            Vector V=new Vector();
            if(buf!=null) V=Resources.getFileLineVector(buf);
            for(int v=0;v<V.size();v++)
                if(((String)V.elementAt(v)).startsWith("#"))
                    inst.append(((String)V.elementAt(v)).substring(1)+"\n\r");
                else
                if(((String)V.elementAt(v)).length()>0) 
                    break;
            if(mob.session()!=null) mob.session().wraplessPrintln(inst.toString());
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
            return;
        }
        String parms=CMParms.combineWithQuotes(commands,2);
        String skillID=parms.substring(0,parms.indexOf("="));
        if(CMLib.login().isExistingAutoTitle(skillID))
        {
            mob.tell("'"+skillID+getScr("Create","compexists"));
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
            return;
        }
        String error=CMLib.login().evaluateAutoTitle(parms,false);
        if(error!=null)
        {
            mob.tell(error);
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
            return;
        }
        CMFile F=new CMFile(Resources.makeFileResourceName("titles.txt"),null,true);
        F.saveText("\n"+parms,true);
        Resources.removeResource("titles.txt");
        CMLib.login().reloadAutoTitles();
        mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Create","presigeup"));
    }
    
	public void abilities(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Create","badcreateability"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		String classD=CMParms.combine(commands,2);
		Ability A=CMClass.getAbility(classD);
		if((A!=null)&&(A.isGeneric()))
		{
			mob.tell(getScr("Create","genableexists",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		if(classD.indexOf(" ")>=0)
		{
			mob.tell("'"+classD+getScr("Create","badableid"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		Ability CR=(Ability)CMClass.getAbility("GenAbility").copyOf();
		CR.setStat("CLASS",classD);
		modifyGenAbility(mob,CR);
		CMLib.database().DBCreateAbility(CR.ID(),CR.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Create","skillup"));
	}

	public void classes(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Create","badcreateclass"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		String classD=CMParms.combine(commands,2);
		CharClass C=CMClass.getCharClass(classD);
		if((C!=null)&&(C.isGeneric()))
		{
			mob.tell(getScr("Create","classexists",C.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		if(classD.indexOf(" ")>=0)
		{
			mob.tell("'"+classD+getScr("Create","badclassid"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubsspell"));
			return;
		}
		CharClass CR=(CharClass)CMClass.getCharClass("GenCharClass").copyOf();
		CR.setClassParms("<CCLASS><ID>"+CMStrings.capitalizeAndLower(classD)+"</ID><NAME>"+CMStrings.capitalizeAndLower(classD)+"</NAME></CCLASS>");
		CMClass.addCharClass(CR);
		modifyGenClass(mob,CR);
		CMLib.database().DBCreateClass(CR.ID(),CR.classParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Create","employmentup"));
	}

	public void socials(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;
		
		if(commands.size()<3)
		{
			mob.tell(getScr("Create","badcreatesocial"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
			return;
		}
        String stuff=CMParms.combine(commands,2);
        if(CMLib.socials().FetchSocial(stuff,false)!=null)
        {
            mob.tell(getScr("Create","socialexists",stuff));
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Create","flubspowspell"));
            return;
        }
		CMLib.socials().modifySocialInterface(mob,stuff);
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell(getScr("Create","notallowed"));
		return false;
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();

		if(commandType.equals(getScr("Create","cmdexit")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			exits(mob,commands);
		}
		else
		if(commandType.equals(getScr("Create","cmdrace")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDRACES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			races(mob,commands);
		}
		else
		if(commandType.equals(getScr("Create","cmdclass")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLASSES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			classes(mob,commands);
		}
		else
		if(commandType.equals(getScr("Create","cmdability")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDABILITIES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			abilities(mob,commands);
		}
		else
		if(commandType.equals(getScr("Create","cmdcomponent")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"COMPONENTS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			components(mob,commands);
		}
        else
        if(commandType.equals(getScr("Create","cmdexpertise")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"EXPERTISES")) return errorOut(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
            expertises(mob,commands);
        }
        else
        if(commandType.equals(getScr("Create","cmdtitle")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"TITLES")) return errorOut(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
            titles(mob,commands);
        }
		else
		if(commandType.equals(getScr("Create","cmdarea")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDAREAS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			areas(mob,commands);
		}
		else
		if(commandType.equals(getScr("Create","cmditem")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			items(mob,commands);
		}
		else
		if(commandType.equals(getScr("Create","cmdroom")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			rooms(mob,commands);
		}
		else
		if(commandType.equals(getScr("Create","cmdsocial")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDSOCIALS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			socials(mob,commands);
		}
		else
        if(commandType.equals(getScr("Create","cmdfaction")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"CMDFACTIONS")) return errorOut(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
            if((commands.size()<3)||(CMParms.combine(commands,2).indexOf(".")<0))
                mob.tell(getScr("Create","whichfaction"));
            else
            {
                String name=CMParms.combine(commands,2);
                if((name.indexOf(" ")>=0)||(name.length()==0))
                {
                	mob.tell(getScr("Create","nospaces"));
                	return false;
                }
                Faction F=CMLib.factions().getFaction(name);
                if(F==null) F=CMLib.factions().getFactionByName(name);
                if(F!=null)
                    mob.tell(getScr("Create","facexists",name));
                else
                if((!mob.isMonster())&&(mob.session().confirm(getScr("Create","newfaction",name),"N")))
                {
                	//name=Resources.buildResourcePath("")+name;
                    StringBuffer template=new CMFile(Resources.buildResourcePath("examples")+"factiontemplate.ini",null,true).text();
                    if((template==null)||(template.length()==0))
                    {
                        mob.tell(getScr("Create","factmpnotfound"));
                        return false;
                    }
                    Resources.submitResource(name,template);
                    Resources.saveFileResource(name,null,template);
                    F=(Faction)CMClass.getCommon("DefaultFaction");
                    F.initializeFaction(template,name);
                    modifyFaction(mob,F);
                    Log.sysOut("CreateEdit",mob.Name()+getScr("Create","createdfaction")+F.name()+" ("+F.factionID()+").");
                }
            }
        }
        else
		if(commandType.equals(getScr("Create","cmduser")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			players(mob,commands);
		}
		else
		if(commandType.equals(getScr("Create","cmdmob")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			mobs(mob,commands);
		}
        else
        if(commandType.equals(getScr("Create","cmdpoll")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"POLLS")) return errorOut(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
            Poll P=(Poll)CMClass.getCommon("DefaultPoll");
            while(CMLib.polls().getPoll(P.getName())!=null)
                P.setName(P.getName()+"!");
            P.setFlags(Poll.FLAG_ACTIVE);
            P.dbcreate();
            P.modifyVote(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","uncertainup"));
            Log.sysOut("CreateEdit",mob.Name()+getScr("Create","createdpoll")+P.getName()+".");
        }
		else
		if(commandType.equals(getScr("Create","cmdquest")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDQUESTS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			if(commands.size()<3)
				mob.tell(getScr("Create","badqueststring"));
			else
			{
				String script=CMParms.combine(commands,2);
				Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
				Q.setScript(script);
				if((Q.name().trim().length()==0)||(Q.duration()<0))
					mob.tell(getScr("Create","badqueststring2"));
				else
				if((CMLib.quests().fetchQuest(Q.name())!=null)
                &&((mob.isMonster())
                    ||(!mob.session().confirm(getScr("Create","questloaded"),"N"))))
                        return false;
				else
				{
					mob.tell(getScr("Create","qadded",Q.name()));
					CMLib.quests().addQuest(Q);
				}
			}
		}
		else
		if(commandType.equals(getScr("Create","cmdclan")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLANS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Create","wavesarms"));
			if(commands.size()<3)
				mob.tell(getScr("Create","badclanname"));
			else
			{
				String name=CMParms.combine(commands,2);
				Clan C=CMLib.clans().getClanType(Clan.TYPE_CLAN);
				C.setName(name);
				if(C.name().trim().length()==0)
					mob.tell(getScr("Create","invalidclanname"));
				else
				if(CMLib.clans().getClan(C.name())!=null)
					mob.tell(getScr("Create","clanexists"));
				else
				{
					mob.tell(getScr("Create","clancreated",C.name()));
					C.setStatus(Clan.CLANSTATUS_ACTIVE);
					C.create();
					if(CMLib.clans().getClan(C.ID())==null)
						CMLib.clans().addClan(C);
				}
			}
		}
		else
		{
			String allWord=CMParms.combine(commands,1);
			String lastWord=null;
			if(commands.size()>2)
				lastWord=(String)commands.lastElement();
			Environmental E=null;
			E=CMClass.getItem(allWord);
			if(((E!=null)&&(E instanceof Item))||(CMLib.english().numPossibleGold(null,allWord)>0))
			{
				commands.insertElementAt(getScr("Create","cmditem"),1);
				execute(mob,commands);
			}
			else
			{
				E=CMClass.getMOB(allWord);
				if((E!=null)&&(E instanceof MOB))
				{
					commands.insertElementAt(getScr("Create","cmdmob"),1);
					execute(mob,commands);
				}
				else
				if((lastWord!=null)&&(Directions.getGoodDirectionCode(lastWord)>=0))
				{
					commands.removeElementAt(commands.size()-1);
					allWord=CMParms.combine(commands,1);

					E=CMClass.getLocale(allWord);
					if(E==null)
						E=CMClass.getExit(allWord);
					if(E==null)
						E=CMClass.getAreaType(allWord);
					if((E!=null)&&(E instanceof Room))
					{
						commands=new Vector();
						commands.addElement(getScr("Create","cmd1"));
						commands.addElement(getScr("Create","cmdroom"));
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands);
					}
					else
					if((E!=null)&&(E instanceof Exit))
					{
						commands=new Vector();
						commands.addElement(getScr("Create","cmd1"));
						commands.addElement(getScr("Create","cmdexit"));
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands);
					}
					else
					if((E!=null)&&(E instanceof Area))
					{
						commands=new Vector();
						commands.addElement(getScr("Create","cmd1"));
						commands.addElement(getScr("Create","cmdarea"));
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands);
					}
					else
						mob.tell(getScr("Create","createinstr",commandType));
				}
				else
					mob.tell(getScr("Create","createinstr2",commandType));
			}
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMD");}

	
}
