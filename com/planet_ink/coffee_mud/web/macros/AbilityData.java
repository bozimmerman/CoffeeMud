package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AbilityData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include help, ranges, quality, target, alignment, domain, 
	// qualifyQ, 
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("ABILITY");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Ability A=CMClass.getAbility(last);
			if(A!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuffer s=ExternalPlay.getHelpText(A.ID());
					if(s==null)
						s=ExternalPlay.getHelpText(A.name());
					if(s!=null)
					{
						int x=s.toString().indexOf("\n\r");
						while(x>=0){	s.replace(x,x+2,"<BR>"); x=s.toString().indexOf("\n\r");}
						x=s.toString().indexOf("\r\n");
						while(x>=0){	s.replace(x,x+2,"<BR>"); x=s.toString().indexOf("\r\n");}
						x=s.toString().lastIndexOf("<BR>");
						int count=0;
						int lastSpace=x+4;
						while((x>=0)&&(x<s.length()))
						{
							count++;
							if(s.charAt(x)==' ')
								lastSpace=x;
							if(count>=70)
							{
								s.replace(lastSpace,lastSpace+1,"<BR>");
								lastSpace=lastSpace+4;
								x=lastSpace+4;
								count=0;
							}
							else
								x++;
						}
						str.append(s);
					}
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
					if((!A.appropriateToMyAlignment(0))
					&&(!A.appropriateToMyAlignment(500))
					&&(A.appropriateToMyAlignment(1000)))
					   str.append("Good, ");
					else
					if((!A.appropriateToMyAlignment(0))
					&&(A.appropriateToMyAlignment(500))
					&&(!A.appropriateToMyAlignment(1000)))
					   str.append("Neutral, ");
					else
					if((A.appropriateToMyAlignment(0))
					&&(!A.appropriateToMyAlignment(500))
					&&(!A.appropriateToMyAlignment(1000)))
					   str.append("Evil, ");
					else
						str.append("Unaligned/Doesn't matter, ");
				}
				if(parms.containsKey("DOMAIN"))
				{
					String thang="";
					if((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
					{
						int domain=A.classificationCode()&Ability.ALL_DOMAINS;
						domain=domain>>5;
						thang=Ability.DOMAIN_DESCS[domain];
					}
					else
						thang=Ability.TYPE_DESCS[A.classificationCode()&Ability.ALL_CODES];
					if(thang.length()>0)
					{
						thang=Character.toUpperCase(thang.charAt(0))+thang.substring(1).toLowerCase();
						str.append(thang+", ");
					}
				}
				if(parms.containsKey("QUALIFYQ")&&(httpReq.getRequestParameters().get("CLASS")!=null))
				{
					String className=(String)httpReq.getRequestParameters().get("CLASS");
					if((className!=null)&&(className.length()>0))
					{
						boolean defaultGain=CMAble.getDefaultGain(className,A.ID());
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
