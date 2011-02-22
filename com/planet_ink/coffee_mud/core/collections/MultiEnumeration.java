package com.planet_ink.coffee_mud.core.collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
/*
Copyright 2000-2011 Bo Zimmerman

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
public class MultiEnumeration<K> implements Enumeration<K>
{
	private final Vector<Enumeration<K>> enums=new Vector<Enumeration<K>>(4);
	private int dex=0;
    
	@SuppressWarnings("unchecked")
	public MultiEnumeration(Enumeration<K>[] esets) 
    {
        if((esets==null)||(esets.length==0))
        	enums.add((Enumeration<K>)EmptyEnumeration.INSTANCE);
        else
        for(Enumeration<K> E : esets)
        	if(E!=null) enums.add(E);
        hasMoreElements();
    }
    
	public MultiEnumeration(Enumeration<K> eset) 
    {
    	enums.add(eset);
        hasMoreElements();
    }
    
	public void addEnumeration(Enumeration<K> set)
	{
		if(set != null)
			enums.add(set);
	}
	
    public boolean hasMoreElements() 
    { 
    	while((dex<enums.size())&&(!enums.get(dex).hasMoreElements()))
    		dex++;
    	if(dex>=enums.size())
    		return false;
    	return true;
    }
    
    public K nextElement() 
    {
    	if(!hasMoreElements())
    		throw new NoSuchElementException();
    	return enums.get(dex).nextElement();
    }
}
