package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenCaged extends GenItem implements CagedAnimal
{
	public String ID(){	return "GenCaged";}
	protected String	readableText="";
	public GenCaged()
	{
		super();
		setName("a caged creature");
		baseEnvStats.setWeight(150);
		setDisplayText("a caged creature sits here.");
		setDescription("");
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setMaterial(EnvResource.RESOURCE_MEAT);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenCaged();
	}
	public boolean cageMe(MOB M)
	{
		if(M==null) return false;
		if(!M.isMonster()) return false;
		name=M.Name();
		displayText=M.displayText();
		setDescription(M.description());
		baseEnvStats().setLevel(M.baseEnvStats().level());
		baseEnvStats().setWeight(M.baseEnvStats().weight());
		baseEnvStats().setHeight(M.baseEnvStats().height());
		StringBuffer itemstr=new StringBuffer("");
		itemstr.append("<MOBITEM>");
		itemstr.append(XMLManager.convertXMLtoTag("MICLASS",CMClass.className(M)));
		itemstr.append(XMLManager.convertXMLtoTag("MIDATA",CoffeeMaker.getPropertiesStr(M,true)));
		itemstr.append("</MOBITEM>");
		setCageText(itemstr.toString());
		recoverEnvStats();
		return true;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this)
		   &&(baseEnvStats().ability()==0)
		   &&(msg.targetMinor()==CMMsg.TYP_GET))
		{
			MOB M=unCageMe();
			if((M!=null)&&(msg.source().location()!=null))
				M.bringToLife(msg.source().location(),true);
			destroy();
			return;
		}
		super.executeMsg(myHost,msg);
	}
	public MOB unCageMe()
	{
		MOB M=null;
		if(cageText().length()==0) return M;
		Vector buf=XMLManager.parseAllXML(cageText());
		if(buf==null)
		{
			Log.errOut("Caged","Error parsing 'MOBITEM'.");
			return M;
		}
		XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)XMLManager.getPieceFromPieces(buf,"MOBITEM");
		if((iblk==null)||(iblk.contents==null))
		{
			Log.errOut("Caged","Error parsing 'MOBITEM'.");
			return M;
		}
		String itemi=XMLManager.getValFromPieces(iblk.contents,"MICLASS");
		Environmental newOne=CMClass.getMOB(itemi);
		Vector idat=XMLManager.getRealContentsFromPieces(iblk.contents,"MIDATA");
		if((idat==null)||(newOne==null)||(!(newOne instanceof MOB)))
		{
			Log.errOut("Caged","Error parsing 'MOBITEM' data.");
			return M;
		}
		CoffeeMaker.setPropertiesStr(newOne,idat,true);
		M=(MOB)newOne;
		M.baseEnvStats().setRejuv(0);
		M.setStartRoom(null);
		M.recoverCharStats();
		M.recoverEnvStats();
		M.resetToMaxState();
		return M;
	}
	public String cageText(){ return CoffeeMaker.restoreAngleBrackets(readableText());}
	public void setCageText(String text)
	{
		setReadableText(CoffeeMaker.parseOutAngleBrackets(text));
		Sense.setReadable(this,false);
	}
}
