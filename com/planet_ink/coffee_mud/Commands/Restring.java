package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.IOException;

public class Restring extends BaseGenerics
{
	public Restring(){}

	private String[] access={"RESTRING"};
	public String[] getAccessWords(){return access;}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String allWord=Util.combine(commands,1);
		int x=allWord.indexOf("@");
		MOB srchMob=mob;
		Room srchRoom=mob.location();
		if(x>0)
		{
			String rest=allWord.substring(x+1).trim();
			allWord=allWord.substring(0,x).trim();
			if(rest.equalsIgnoreCase("room"))
				srchMob=null;
			else
			if(rest.length()>0)
			{
				MOB M=srchRoom.fetchInhabitant(rest);
				if(M==null)
				{
					mob.tell("MOB '"+rest+"' not found.");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
					return false;
				}
				srchMob=M;
				srchRoom=null;
			}
		}
		Environmental thang=null;
		if((srchMob!=null)&&(srchRoom!=null))
			thang=srchRoom.fetchFromMOBRoomFavorsItems(srchMob,null,allWord,Item.WORN_REQ_ANY);
		else
		if(srchMob!=null)
			thang=srchMob.fetchInventory(allWord);
		else
		if(srchRoom!=null)
			thang=srchRoom.fetchFromRoomFavorItems(null,allWord,Item.WORN_REQ_ANY);
		if((thang!=null)&&(thang instanceof Item))
		{
			if(!thang.isGeneric())
				mob.tell(thang.name()+" can not be restrung.");
			else
			{
				int showFlag=-1;
				if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
					showFlag=-999;
				boolean ok=false;
				while(!ok)
				{
					int showNumber=0;
					genName(mob,thang,++showNumber,showFlag);
					genDisplayText(mob,thang,++showNumber,showFlag);
					genDescription(mob,thang,++showNumber,showFlag);
					if(showFlag<-900){ ok=true; break;}
					if(showFlag>0){ showFlag=-1; continue;}
					showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
					if(showFlag<=0)
					{
						showFlag=-1;
						ok=true;
					}
				}
			}
			thang.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
		}
		else
			mob.tell("'"+allWord+"' can not be restrung.");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMD");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
