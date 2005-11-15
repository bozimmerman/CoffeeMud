package com.planet_ink.siplet.support;
import java.util.*;

public class MXPElement implements Cloneable
{
    public static final int BIT_OPEN=1;
    public static final int BIT_COMMAND=2;
    public static final int BIT_NEEDTEXT=4;
    public static final int BIT_SPECIAL=8;
    public static final int BIT_HTML=16;
    public static final int BIT_NOTSUPPORTED=32;
    public static final int BIT_EATTEXT=64;
    public static final int BIT_DISABLED=128;
    
    private String name="";
    private String definition="";
    private String attributes="";
    private String flag="";
    private String unsupportedParms="";
    private int bitmap=0;
    private Vector parsedAttributes=null;
    private Hashtable attributeValues=null;
    private Hashtable alternativeAttributes=null;
    private Vector userParms=new Vector();
    private boolean basicElement=true;
    
    private int bufInsert=-1;
    
    public MXPElement(String newName,
                      String theDefinition,
                      String theAttributes,
                      String theFlag,
                      int theBitmap)
    {
        super();
        name=newName;
        definition=theDefinition;
        attributes=theAttributes;
        flag=theFlag;
        bitmap=theBitmap;
        if((!isCommand())
        &&(theDefinition.toUpperCase().indexOf("&TEXT;")>=0))
              bitmap=bitmap|BIT_NEEDTEXT;
    }
    
    public MXPElement(String newName,
                      String theDefinition,
                      String theAttributes,
                      String theFlag,
                      int theBitmap,
                      String unsupported)
    {
        super();
        name=newName;
        definition=theDefinition;
        attributes=theAttributes;
        flag=theFlag;
        bitmap=theBitmap;
        if((!isCommand())
        &&(theDefinition.toUpperCase().indexOf("&TEXT;")>=0))
            bitmap=bitmap|BIT_NEEDTEXT;
        unsupportedParms=unsupported;
    }
    public MXPElement copyOf()
    {
        try
        {
            MXPElement E=(MXPElement)this.clone();
            if(E.parsedAttributes!=null) E.parsedAttributes=(Vector)E.parsedAttributes.clone();
            if(E.attributeValues!=null) E.attributeValues=(Hashtable)E.attributeValues.clone();
            if(E.alternativeAttributes!=null) E.alternativeAttributes=(Hashtable)E.alternativeAttributes.clone();
            if(E.userParms!=null) E.userParms=(Vector)E.userParms.clone();
            return E;
        }
        catch(Exception e){};
        return this;
    }

    public String name(){return name;}
    public void setName(String newName){name=newName;}
    public boolean isCommand(){return Util.bset(bitmap,BIT_COMMAND);}
    public boolean isOpen(){return Util.bset(bitmap,BIT_OPEN);}
    public boolean isHTML(){return Util.bset(bitmap,BIT_HTML);}
    public boolean isSpecialProcessor(){return Util.bset(bitmap,BIT_SPECIAL);}
    public boolean isDisabled(){return Util.bset(bitmap,BIT_DISABLED);}
    public boolean isTextEater(){return Util.bset(bitmap,BIT_EATTEXT);}
    public String getDefinition(){return definition;}
    public void setDefinition(String defi){definition=defi;}
    public String getAttributes(){return attributes;}
    public boolean needsText(){return Util.bset(bitmap,BIT_NEEDTEXT);}
    public void setNotBasicElement(){basicElement=false;}
    public boolean isBasicElement(){return basicElement;}
    public boolean isGenerallySupported(){return !Util.bset(bitmap,BIT_NOTSUPPORTED);}
    public void setBitmap(int newBitmap){bitmap=newBitmap;}
    public int getBitmap(){return bitmap;}
    public Vector getUnsupportedParms()
    {
        if((unsupportedParms==null)||(unsupportedParms.trim().length()==0))
            return new Vector();
        return Util.parseSpaces(unsupportedParms,true);
    }
    public void setAttributes(String newAttributes)
    { 
        attributes=newAttributes;
        parsedAttributes=null;
        attributeValues=null;
        alternativeAttributes=null;
    }
    public String getAttributeValue(String tag)
    {
        getParsedAttributes();
        return (String)attributeValues.get(tag.toUpperCase().trim());
    }
    public void setAttributeValue(String tag, String value)
    {
        getParsedAttributes();
        attributeValues.remove(tag);
        attributeValues.put(tag,value);
    }
    
    public synchronized Vector getParsedAttributes()
    {
        if(parsedAttributes!=null) return parsedAttributes;
        parsedAttributes=new Vector();
        attributeValues=new Hashtable();
        alternativeAttributes=new Hashtable();
        StringBuffer buf=new StringBuffer(attributes.trim());
        StringBuffer bit=new StringBuffer("");
        Vector quotes=new Vector();
        int i=-1;
        char lastC=' ';
        boolean firstEqual=false;
        while((bit!=null)&&((++i)<buf.length()))
        {
            switch(buf.charAt(i))
            {
            case '=':
                if((!firstEqual)&&(bit.length()>0))
                {
                    String tag=bit.toString().toUpperCase().trim();
                    bit=new StringBuffer("");
                    parsedAttributes.addElement(tag);
                    attributeValues.put(tag,bit);
                }
                else
                    bit.append(buf.charAt(i));
                firstEqual=true;
                break;
            case '\n':
            case '\r':
            case ' ':
            case '\t':
                if(quotes.size()==0)
                {
                    if((!firstEqual)&&(bit.length()>0))
                        parsedAttributes.addElement(bit.toString().toUpperCase().trim());
                    bit=new StringBuffer("");
                    firstEqual=false;
                }
                else
                    bit.append(buf.charAt(i));
                break;
            case '"':
            case '\'':
                if((lastC=='=')
                ||(quotes.size()>0)
                ||((quotes.size()==0)&&((lastC==' ')||(lastC=='\t'))))
                {
                    if((quotes.size()>0)&&(((Character)quotes.lastElement()).charValue()==buf.charAt(i)))
                    {
                        quotes.removeElementAt(quotes.size()-1);
                        if(quotes.size()==0)
                        {
                            if((!firstEqual)&&(bit.length()>0))
                                parsedAttributes.addElement(bit.toString().toUpperCase().trim());
                            bit=new StringBuffer("");
                            firstEqual=false;
                        }
                        else
                            bit.append(buf.charAt(i));
                    }
                    else
                    {
                        if(quotes.size()>0)
                            bit.append(buf.charAt(i));
                        quotes.addElement(new Character(buf.charAt(i)));
                    }
                }
                else
                    bit.append(buf.charAt(i));
                break;
            default:
                bit.append(buf.charAt(i));
                break;
            }
            lastC=buf.charAt(i);
        }
        if((!firstEqual)&&(bit.length()>0))
            parsedAttributes.addElement(bit.toString().toUpperCase().trim());
        for(int p=parsedAttributes.size()-1;p>=0;p--)
        {
            String PA=(String)parsedAttributes.elementAt(p);
            StringBuffer VAL=(StringBuffer)attributeValues.get(PA);
            if((VAL!=null)&&(parsedAttributes.contains(VAL.toString())))
            {
                parsedAttributes.removeElementAt(p);
                attributeValues.remove(PA);
                alternativeAttributes.put(PA,VAL.toString());
            }
        }
        return parsedAttributes;
    }
    public String getFlag(){return flag;}
    public Vector getUserParms(){return userParms;}
    public void saveSettings(int insertPoint, Vector theUserParms)
    {
        bufInsert=insertPoint;
        userParms=theUserParms;
    }
    public int getBufInsert(){return bufInsert;}
    public void deleteAttribute(String name)
    {
        getParsedAttributes();
        attributeValues.remove(name.toUpperCase().trim());
    }
    
    public Vector getEndableTags(String desc)
    {
        StringBuffer buf=new StringBuffer(desc);
        Vector tags=new Vector();
        StringBuffer bit=null;
        Vector quotes=new Vector();
        int i=-1;
        while((++i)<buf.length())
        {
            switch(buf.charAt(i))
            {
            case '<':
                if(quotes.size()>0)
                {
                    bit=null;
                    break;
                }
                else
                if(bit!=null)
                    return tags;
                else
                    bit=new StringBuffer("");
                break;
            case '>':
                if((quotes.size()==0)&&(bit!=null)&&(bit.toString().trim().length()>0))
                {
                    tags.add(bit.toString().toUpperCase().trim());
                }
                bit=null;
                break;
            case ' ':
            case '\t':
                if((quotes.size()==0)&&(bit!=null)&&(bit.toString().trim().length()>0))
                    tags.add(bit.toString().toUpperCase().trim());
                bit=null;
                break;
            case '"':
            case '\'':
                if((quotes.size()>0)&&(((Character)quotes.lastElement()).charValue()==buf.charAt(i)))
                    quotes.removeElementAt(quotes.size()-1);
                else
                    quotes.addElement(new Character(buf.charAt(i)));
                bit=null;
                break;
            default:
                if((bit!=null)&&(Character.isLetterOrDigit(buf.charAt(i))))
                    bit.append(buf.charAt(i));
                else
                    bit=null;
                break;
            }
        }
        return tags;
    }
    
    public String getFoldedDefinition(String text)
    {
        Vector aV=getParsedAttributes();
        attributeValues.remove("TEXT");
        attributeValues.put("TEXT",text);
        if((userParms!=null)&&(userParms.size()>0))
        {
            int position=-1;
            String avParm=null;
            String userParm=null;
            for(int u=0;u<userParms.size();u++)
            {
                userParm=((String)userParms.elementAt(u)).toUpperCase().trim();
                int xx=userParm.indexOf("=");
                if((xx>0)&&(alternativeAttributes.containsKey(userParm.substring(0,xx).trim())))
                {
                    String newKey=(String)alternativeAttributes.get(userParm.substring(0,xx).trim());
                    String uu=(String)userParms.elementAt(u);
                    xx=uu.indexOf("=");
                    userParms.setElementAt(newKey+uu.substring(xx),u);
                    userParm=((String)userParms.elementAt(u)).toUpperCase().trim();
                }
                boolean found=false;
                for(int a=0;a<aV.size();a++)
                {
                    avParm=(String)aV.elementAt(a);
                    if((userParm.startsWith(avParm+"="))||(avParm.equals(userParm)))
                    {
                        found=true;
                        if(a>position) position=a;
                        attributeValues.remove(avParm);
                        String val=(avParm.equals(userParm))?"":((String)userParms.elementAt(u)).trim().substring(avParm.length()+1);
                        attributeValues.put(avParm,val);
                        break;
                    }
                }
                if((!found)&&(position<(aV.size()-1)))
                {
                    position++;
                    avParm=(String)aV.elementAt(position);
                    attributeValues.remove(avParm);
                    attributeValues.put(avParm,((String)userParms.elementAt(u)).trim());
                }
            }
        }
        return definition;
    }
}
