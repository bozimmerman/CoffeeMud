package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AbilityData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include help, ranges, quality, target, alignment, domain,
	// qualifyQ, auto
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("ABILITY");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Ability A=CMClass.getAbility(last);
			if(A!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuffer s=MUDHelp.getHelpText(A.ID(),null);
					if(s==null)
						s=MUDHelp.getHelpText(A.Name(),null);
					str.append(helpHelp(s));
				}
				if(parms.containsKey("RANGES"))
				{
					int min=A.minRange();
					int max=A.maxRange();
					if(min+max==0)
						str.append("Touch, or not applicable, ");
					else
					{
						if(min==0)
							str.append("Touch");
						else
							str.append("Range "+min);
						if(max>0)
							str.append(" - Range "+max);
						str.append(", ");
					}
				}
				if(parms.containsKey("QUALITY"))
				{
					switch(A.quality())
					{
					case Ability.MALICIOUS:
						str.append("Malicious, ");
						break;
					case Ability.BENEFICIAL_OTHERS:
					case Ability.BENEFICIAL_SELF:
						str.append("Always Beneficial, ");
						break;
					case Ability.OK_OTHERS:
					case Ability.OK_SELF:
						str.append("Sometimes Beneficial, ");
						break;
					case Ability.INDIFFERENT:
						str.append("Circumstantial, ");
						break;
					}
				}
				if(parms.containsKey("AUTO"))
				{
					if(A.isAutoInvoked())
						str.append("Automatic, ");
					else
						str.append("Requires invocation, ");
				}
				if(parms.containsKey("TARGET"))
				{
					switch(A.quality())
					{
					case Ability.INDIFFERENT:
						str.append("Item or Room, ");
						break;
					case Ability.MALICIOUS:
						str.append("Others, ");
						break;
					case Ability.BENEFICIAL_OTHERS:
					case Ability.OK_OTHERS:
						str.append("Caster or others, ");
						break;
					case Ability.BENEFICIAL_SELF:
					case Ability.OK_SELF:
						str.append("Caster only, ");
						break;
					}
				}
				if(parms.containsKey("ALIGNMENT"))
				{
					boolean notgood=(!A.appropriateToMyAlignment(1000));
					boolean notneutral=(!A.appropriateToMyAlignment(500));
					boolean notevil=(!A.appropriateToMyAlignment(0));
					boolean good=!notgood;
					boolean neutral=!notneutral;
					boolean evil=!notevil;

					if(good&&neutral&&evil)
						str.append("Unaligned/Doesn't matter, ");
					else
					if(neutral&&notgood&&notevil)
					   str.append("Neutral, ");
					else
					if(good&&notevil)
					   str.append("Good, ");
					else
					if(evil&&notgood)
					   str.append("Evil, ");
				}
				if(parms.containsKey("DOMAIN"))
				{
					StringBuffer thang=new StringBuffer("");
					if((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
					{
						int domain=A.classificationCode()&Ability.ALL_DOMAINS;
						domain=domain>>5;
						thang.append(Ability.DOMAIN_DESCS[domain].toLowerCase());
					}
					else
						thang.append(Ability.TYPE_DESCS[A.classificationCode()&Ability.ALL_CODES].toLowerCase());
					if(thang.length()>0)
					{
						thang.setCharAt(0,Character.toUpperCase(thang.charAt(0)));

						int x=thang.toString().indexOf("/");
						if(x>0) thang.setCharAt(x+1,Character.toUpperCase(thang.charAt(x+1)));
						str.append(thang.toString()+", ");
					}
				}
				if(parms.containsKey("QUALIFYQ")&&(httpReq.isRequestParameter("CLASS")))
				{
					String className=httpReq.getRequestParameter("CLASS");
					if((className!=null)&&(className.length()>0))
					{
						boolean defaultGain=CMAble.getDefaultGain(className,true,A.ID());
						if(!defaultGain)
							str.append("(Qualify), ");
					}
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return strstr;
			}
		}
		return "";
	}
}
