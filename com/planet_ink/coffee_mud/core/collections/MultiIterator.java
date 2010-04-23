package com.planet_ink.coffee_mud.core.collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Vector;
/*
Copyright 2000-2010 Bo Zimmerman

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
public class MultiIterator<K> implements Iterator<K>
{
    Vector<Iterator<K>> iters=new Vector<Iterator<K>>();
    int dex=0;
    
	public MultiIterator(Iterator<K>[] esets) 
    {
        if((esets==null)||(esets.length==0)) 
        	iters.add(new EmptyIterator<K>());
        else
        for(Iterator<K> I : esets)
        	iters.add(I);
        hasNext();
    }
    
    public boolean hasNext() 
    { 
    	while((dex<iters.size())&&(!iters.get(dex).hasNext()))
    		dex++;
    	if(dex>=iters.size())
    		return false;
    	return true;
    }
    
    public K next() 
    {
    	if(!hasNext())
    		throw new NoSuchElementException();
    	return iters.get(dex).next();
    }
    
    public void remove() 
    {
    	if(dex>=iters.size())
    		throw new NoSuchElementException();
    	iters.get(dex).remove();
    }
}
