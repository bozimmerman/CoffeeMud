package com.planet_ink.siplet.support;

public class MXPElement implements Cloneable
{
    private String definition="";
    private String attributes="";
    private String defAttribute="";
    private String flag="";
    private int tag=-1;
    private boolean open=false;
    private boolean command=false;
    private boolean isFinalElement=false;
    private boolean specialProcessing=false;
    
    
    public MXPElement(String theDefinition,
                      String theAttributes,
                      String theFlag,
                      String defaultAttribute,
                      int theTag,
                      boolean isOpen,
                      boolean isCommand,
                      boolean isFinal,
                      boolean isSpecialProcessor)
    {
        super();
        definition=theDefinition;
        attributes=theAttributes;
        flag=theFlag;
        tag=theTag;
        open=isOpen;
        command=isCommand;
        defAttribute=defaultAttribute;
        isFinalElement=isFinal;
        specialProcessing=isSpecialProcessor;
    }
    
    public static MXPElement MXPFinalCommand(String theDefinition)
    {
        return new MXPElement(theDefinition,"","","",-1,false,true,true,false);
    }
    public static MXPElement MXPFinalElement(String theDefinition, String defaultAttribute)
    {
        return new MXPElement(theDefinition,"","",defaultAttribute,-1,false,false,true,false);
    }
    
    public boolean isCommand(){return command;}
    public boolean isFinal(){return isFinalElement;}
    public boolean isOpen(){return open;}
    public boolean isSpecialProcessor(){return specialProcessing;}
    public String getDefinition(){return definition;}
    public String getAttributes(){return attributes;}
    public String getFlag(){return flag;}
    public String getDefaultAttribute(){return defAttribute;}
    public int getTag(){return tag;}
}
