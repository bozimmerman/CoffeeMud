package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqLevels extends Property
{
	public Prop_ReqLevels()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Level Limitations";
		canAffectCode=Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;
	}

	public Environmental newInstance()
	{
		Prop_ReqLevels newOne=new Prop_ReqLevels();
		newOne.setMiscText(text());
		return newOne;
	}

	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		   &&(affect.target()!=null)
		   &&(affect.target() instanceof Room)
		   &&(affect.targetMinor()==Affect.TYP_ENTER)
		   &&((affect.amITarget(affected))||(affect.tool()==affected)||(affected instanceof Area)))
		{
			if((text().toUpperCase().indexOf("ALL")>=0)||(text().length()==0)||(affect.source().isASysOp((Room)affect.target())))
				return super.okAffect(affect);

			if((text().toUpperCase().indexOf("SYSOP")>=0)&&(!affect.source().isASysOp((Room)affect.target())))
			{
				affect.source().tell("That way is restricted.  You are not allowed.");
				return false;
			}

			int lvl=affect.source().envStats().level();
			
			int lastPlace=0;
			int x=0;
			while(x>=0)
			{
				x=text().indexOf(">",lastPlace);
				if(x<0)	x=text().indexOf("<",lastPlace);
				if(x<0)	x=text().indexOf("=",lastPlace);
				if(x>=0)
				{
					char primaryChar=text().charAt(x);
					x++;
					boolean ok=false;
					boolean andEqual=false;
					if(text().charAt(x)=='=')
					{
						andEqual=true;
						x++;
					}
					lastPlace=x;
					
					String cmpString="";
					while((x<text().length())&&
						  (((text().charAt(x)==' ')&&(cmpString.length()==0))
						   ||(Character.isDigit(text().charAt(x)))))
					{
						if(Character.isDigit(text().charAt(x)))
							cmpString+=text().charAt(x);
						x++;
					}
					if(cmpString.length()>0)
					{
						int cmpLevel=Util.s_int(cmpString);
						if((cmpLevel==lvl)&&(andEqual))
							ok=true;
						else
						switch(primaryChar)
						{
						case '>': ok=(lvl>cmpLevel); break;
						case '<': ok=(lvl<cmpLevel); break;
						case '=': ok=(lvl==cmpLevel); break;
						}
					}
					if(!ok)
					{
						affect.source().tell("You are not allowed to go that way.");
						return false;
					}
				}
			}
		}
		return super.okAffect(affect);
	}
}