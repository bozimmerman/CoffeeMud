package com.planet_ink.siplet.support;

public class MXPEntity implements Cloneable
{

    private String definition="";
    private boolean isFinalEntity=false;
    
    public MXPEntity(String theDefinition, boolean finalEntity)
    {
        super();
        definition=theDefinition;
        isFinalEntity=finalEntity;
    }
    public boolean isFinal(){return isFinalEntity;}
    public String getDefinition(){return definition;}
    public void setDefinition(String newDefinition){definition=newDefinition;}
}
