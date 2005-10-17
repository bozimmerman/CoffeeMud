package com.planet_ink.siplet.support;
import java.io.*;
import java.util.*;

import com.planet_ink.siplet.applet.Siplet;

public class TelnetFilter
{
    
    protected static final char IS = 0;
    protected static final char DO = 253;
    protected static final char SB = 250;
    protected static final char SE = 240;
    protected static final char MXP = 91;
    protected static final char IAC = 255;
    protected static final char EOR = 239;
    protected static final char WILL = 251;
    protected static final char WONT = 252;
    protected static final char DONT = 254;
    protected static final char DEAD = 65535;
    protected static final char NULL = 0;
    protected static final String CR = "\r";
    protected static final char TELOPT_EOR = 25;
    protected static final char TELOPT_ECHO = 1;
    protected static final char TELOPT_NAWS = 31;
    protected static final char TELOPT_TTYPE = 24;
    protected static final char TELOPT_TSPEED = 32;
    protected static final char MCCP_COMPRESS = 85;
    protected static final char MCCP_COMPRESS2 = 86;
    protected static final char TELOPT_NEWENVIRONMENT = 39;

    private static String defaultBackground="BK";
    private static String defaultForeground="WH";
    private static String[] colorCodes1={ // 30-37
            "BK","RD","GR","BR","BL","PU","CY","GY"};
    private static String[] colorCodes2={ 
            "DG","LR","LG","YL","LB","LP","LC","WH"};
    
    protected String lastBackground=null;
    protected String lastForeground=null;
    protected boolean blinkOn=false;
    protected boolean fontOn=false;
    protected boolean boldOn=false;
    protected boolean underlineOn=false;
    protected boolean italicsOn=false;
    private Siplet logger=null;

    
    private TelnetFilter(){};
    public TelnetFilter(Siplet log)
    {
        logger=log;
    }
    
    private String blinkOff(){ if(blinkOn){blinkOn=false; return "</BLINK>";}return ""; }
    private String underlineOff(){ if(underlineOn){underlineOn=false; return "</U>";}return ""; }
    private String fontOff()
    { 
        if(fontOn)
        {
            lastBackground=defaultBackground;
            lastForeground=defaultForeground;
            fontOn=false; 
            return "</FONT>";
        }
        return ""; 
    }
    private String italicsOff(){ if(italicsOn){italicsOn=false; return "</I>";}return ""; }
    private String allOff()
    {
        StringBuffer off=new StringBuffer("");
        off.append(blinkOff());
        off.append(underlineOff());
        off.append(fontOff());
        off.append(italicsOff());
        return off.toString();
    }
    private String escapeTranslate(String escapeString)
    {
        if(escapeString.endsWith("m"))
        {
            Vector V=Util.parseSemicolons(escapeString.substring(0,escapeString.length()-1),true);
            StringBuffer str=new StringBuffer("");
            String s=null;
            int code=0;
            String background=null;
            String foreground=null;
            for(int i=0;i<V.size();i++)
            {
                s=(String)V.elementAt(i);
                code=Util.s0_int(s);
                switch(code)
                {
                case 0: 
                        if(i==(V.size()-1))
                            str.append(allOff());
                        boldOn=false;
                        break;
                case 1: boldOn=true; break;
                case 4: 
                    {
                        if(!underlineOn)
                        {
                            underlineOn=true;
                            str.append("<U>");
                        }
                        break;
                    }
                case 5: 
                {
                    if(!blinkOn)
                    {
                        blinkOn=true;
                        str.append("<BLINK>");
                    }
                    break;
                }
                case 6: 
                {
                    if(!italicsOn)
                    {
                        italicsOn=true;
                        str.append("<I>");
                    }
                    break;
                }
                case 7: 
                {
                    // this is reverse on, and requires a wierd color reversal
                    // from whatever the previous colors were.
                    // do it later
                    break;
                }
                case 8: 
                {
                    background=defaultBackground;
                    foreground=defaultBackground;
                    break;
                }
                case 22:
                    str.append(allOff());
                    break;
                case 24:
                    str.append(underlineOff());
                    break;
                case 25:
                    str.append(blinkOff());
                    break;
                case 26:
                    str.append(italicsOff());
                    break;
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                    foreground=boldOn?colorCodes2[code-30]:colorCodes1[code-30];
                    break;
                case 39:
                    foreground=defaultForeground;
                    break;
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                    background=colorCodes1[code-40];
                    break;
                case 49:
                    background=defaultForeground;
                    break;
                }
                if((background!=null)||(foreground!=null))
                {
                    if(lastBackground==null)lastBackground=defaultBackground;
                    if(lastForeground==null)lastForeground=defaultForeground;
                    if(background==null) background=lastBackground;
                    if(foreground==null) foreground=defaultForeground;
                    if((!background.equals(lastBackground))
                    ||(!foreground.equals(lastForeground)))
                    {
                        str.append(fontOff());
                        lastBackground=background;
                        lastForeground=foreground;
                        fontOn=true;
                        str.append("<FONT CLASS="+background+foreground+">");
                    }
                }
            }
            return str.toString();
        }
        return "";
    }
    
    // filters out color codes -> <FONT>
    // CRS -> <BR>
    // SPACES -> &nbsp;
    // < -> &lt;
    // TELNET codes -> response outputstream
    public int filter(StringBuffer buf, DataOutputStream response)
    {
        int i=0;
        while(i<buf.length())
        {
            switch(buf.charAt(i))
            {
            case ' ':
                buf.setCharAt(i,'&');
                buf.insert(i+1,"nbsp;");
                i+=5;
                break;
            case '\n':
                buf.setCharAt(i,'<');
                buf.insert(i+1,"BR>");
                i+=3;
                break;
            case '\r':
                buf.deleteCharAt(i);
                i--;
                break;
            case '\033':
                {
                    int savedI=i;
                    if(i==buf.length()-1)
                        return i;
                    if(buf.charAt(++i)!='[')
                        buf.setCharAt(i,' ');
                    else
                    {
                        boolean quote=false;
                        while(((++i)<buf.length())
                        &&((quote)||(!Character.isLetter(buf.charAt(i)))))
                            if(buf.charAt(i)=='"')
                                quote=!quote;
                        if(i==buf.length())
                            return savedI;
                        String translate=escapeTranslate(buf.substring(savedI+2,i+1));
                        buf.replace(savedI,i+1,translate);
                        i=savedI+translate.length()-1;
                    }
                }
                break;
            }
            i++;
        }
        return buf.length();
    }
}
