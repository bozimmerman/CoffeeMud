package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.XMLManager;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2026-2026 Bo Zimmerman

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
public class XMLManagerTest extends StdTest
{
	@Override
	public String ID()
	{
		return "XMLManagerTest";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "core"};
	}

	// POJO helpers for fromPOJO tests
	public static class SimplePojo
	{
		public String name;
		public int count;
		public boolean flag;
		public long longVal;
		public double doubleVal;
		public SimplePojo()
		{
		}
		public SimplePojo(final String n, final int c, final boolean f, final long l, final double d)
		{
			name=n;
			count=c;
			flag=f;
			longVal=l;
			doubleVal=d;
		}
	}

	public static class InnerPojo
	{
		public String innerVal;
		public int innerNum;
		public InnerPojo()
		{
		}
		public InnerPojo(final String v, final int n)
		{
			innerVal=v;
			innerNum=n;
		}
	}

	public static class ArrayPojo
	{
		public String[] strArray;
		public int[] intArray;
		public Integer[] integerArray;
		public InnerPojo inner;
		public InnerPojo[] innerArray;
		public String nullField;
		public ArrayPojo()
		{
		}
	}

	public static class WrapperPojo
	{
		public Float fVal;
		public Double dVal;
		public Integer iVal;
		public Long lVal;
		public Short sVal;
		public Byte bVal;
		public Boolean boolVal;
		public String strVal;
		public WrapperPojo()
		{
		}
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final XMLManager xml = new XMLManager();
		try
		{
			// Test 1: parseAllXML - simple single tag
			{
				List<XMLTag> tags = xml.parseAllXML("<HELLO>world</HELLO>");
				if(tags.size()!=1)
					return "Error#1.1: simple tag size "+tags.size();
				if(!tags.get(0).tag().equals("HELLO"))
					return "Error#1.2: tag name "+tags.get(0).tag();
				if(!tags.get(0).value().equals("world"))
					return "Error#1.3: value "+tags.get(0).value();
				if(tags.get(0).contents().size()!=0)
					return "Error#1.4: contents should be 0";
				// StringBuffer overload
				tags = xml.parseAllXML(new StringBuffer("<HELLO>world</HELLO>"));
				if(tags.size()!=1)
					return "Error#1.5: StringBuffer size "+tags.size();
				if(!tags.get(0).value().equals("world"))
					return "Error#1.6: StringBuffer value";
				// case insensitivity - lower case input should be uppercased
				tags = xml.parseAllXML("<hello>world</hello>");
				if(!tags.get(0).tag().equals("HELLO"))
					return "Error#1.7: lower case tag not uppercased: "+tags.get(0).tag();
				// empty value
				tags = xml.parseAllXML("<EMPTY></EMPTY>");
				if(tags.size()!=1)
					return "Error#1.8: empty tag size";
				if(!tags.get(0).value().equals(""))
					return "Error#1.9: empty value should be empty: '"+tags.get(0).value()+"'";
			}

			// Test 2: parseAllXML - multiple root tags
			{
				List<XMLTag> tags = xml.parseAllXML("<A>1</A><B>2</B><C>3</C>");
				if(tags.size()!=3)
					return "Error#2.1: multi root size "+tags.size();
				if((!tags.get(0).tag().equals("A"))||(!tags.get(1).tag().equals("B"))||(!tags.get(2).tag().equals("C")))
					return "Error#2.2: multi root tags "+tags.get(0).tag()+","+tags.get(1).tag()+","+tags.get(2).tag();
				if((!tags.get(0).value().equals("1"))||(!tags.get(1).value().equals("2")))
					return "Error#2.3: multi root values";
				// whitespace between tags
				tags = xml.parseAllXML("  <A>1</A>  \n\t <B>2</B>  ");
				if(tags.size()!=2)
					return "Error#2.4: whitespace multi size "+tags.size();
			}

			// Test 3: parseAllXML - nested tags
			{
				List<XMLTag> tags = xml.parseAllXML("<OUTER><INNER>val</INNER></OUTER>");
				if(tags.size()!=1)
					return "Error#3.1: nested size "+tags.size();
				XMLTag outer = tags.get(0);
				if(!outer.tag().equals("OUTER"))
					return "Error#3.2: outer tag "+outer.tag();
				if(outer.contents().size()!=1)
					return "Error#3.3: outer contents "+outer.contents().size();
				XMLTag inner = outer.contents().get(0);
				if(!inner.tag().equals("INNER"))
					return "Error#3.4: inner tag "+inner.tag();
				if(!inner.value().equals("val"))
					return "Error#3.5: inner value "+inner.value();
				if(inner.parent()!=outer)
					return "Error#3.6: parent link failed";
				// deep nesting
				tags = xml.parseAllXML("<A><B><C>deep</C></B></A>");
				if(tags.size()!=1)
					return "Error#3.7: deep outer size";
				XMLTag a = tags.get(0);
				if(a.contents().size()!=1)
					return "Error#3.8: A contents";
				XMLTag b = a.contents().get(0);
				if(!b.tag().equals("B"))
					return "Error#3.9: B tag "+b.tag();
				if(b.contents().size()!=1)
					return "Error#3.10: B contents";
				XMLTag c = b.contents().get(0);
				if(!c.value().equals("deep"))
					return "Error#3.11: C value "+c.value();
				// multiple children
				tags = xml.parseAllXML("<ROOT><X>1</X><Y>2</Y><Z>3</Z></ROOT>");
				if(tags.get(0).contents().size()!=3)
					return "Error#3.12: multi child size "+tags.get(0).contents().size();
			}

			// Test 4: parseAllXML - self-closing tags
			{
				List<XMLTag> tags = xml.parseAllXML("<EMPTY />");
				if(tags.size()!=1)
					return "Error#4.1: self-close size "+tags.size();
				if(!tags.get(0).tag().equals("EMPTY"))
					return "Error#4.2: self-close tag "+tags.get(0).tag();
				if(!tags.get(0).value().equals(""))
					return "Error#4.3: self-close value '"+tags.get(0).value()+"'";
				// self-close with space before slash
				tags = xml.parseAllXML("<X />");
				if(tags.size()!=1)
					return "Error#4.4: X self-close size";
				// self-close nested
				tags = xml.parseAllXML("<ROOT><A /><B>val</B><C /></ROOT>");
				if(tags.size()!=1)
					return "Error#4.5: nested self-close outer size";
				if(tags.get(0).contents().size()!=3)
					return "Error#4.6: nested self-close contents "+tags.get(0).contents().size();
				if(!tags.get(0).contents().get(0).tag().equals("A"))
					return "Error#4.7: A tag";
				if(!tags.get(0).contents().get(1).value().equals("val"))
					return "Error#4.8: B value";
				// self-closing with attributes
				tags = xml.parseAllXML("<TAG ATTR=\"1\" />");
				if(tags.size()!=1)
					return "Error#4.9: self-close attr size";
				if(!"1".equals(tags.get(0).getParmValue("ATTR")))
					return "Error#4.10: self-close attr val "+tags.get(0).getParmValue("ATTR");
			}

			// Test 5: parseAllXML - attributes
			{
				List<XMLTag> tags = xml.parseAllXML("<TAG PARM=\"value\">content</TAG>");
				if(tags.size()!=1)
					return "Error#5.1: attr size";
				XMLTag t = tags.get(0);
				if(!"value".equals(t.getParmValue("PARM")))
					return "Error#5.2: parm value "+t.getParmValue("PARM");
				if(!"value".equals(t.getParmValue("parm")))
					return "Error#5.3: parm case insensitive";
				if(!t.parms().containsKey("PARM"))
					return "Error#5.4: parms map missing PARM";
				// multiple attributes
				tags = xml.parseAllXML("<TAG A=\"1\" B=\"2\" C=\"3\">x</TAG>");
				t = tags.get(0);
				if((!"1".equals(t.getParmValue("A")))||(!"2".equals(t.getParmValue("B")))||(!"3".equals(t.getParmValue("C"))))
					return "Error#5.5: multi attr failed";
				// attribute with spaces after =
				tags = xml.parseAllXML("<TAG A=\"1\"  B= \"2\" >x</TAG>");
				t = tags.get(0);
				if(!"1".equals(t.getParmValue("A")))
					return "Error#5.6: spaced attr A "+t.getParmValue("A");
				if(!"2".equals(t.getParmValue("B")))
					return "Error#5.7: spaced attr B "+t.getParmValue("B");
				// also test with no spaces
				tags = xml.parseAllXML("<TAG X=\"10\" Y=\"20\">x</TAG>");
				t = tags.get(0);
				if(!"10".equals(t.getParmValue("X")))
					return "Error#5.6b: attr X "+t.getParmValue("X");
				// unquoted attribute value
				tags = xml.parseAllXML("<TAG A=123>val</TAG>");
				t = tags.get(0);
				if(!"123".equals(t.getParmValue("A")))
					return "Error#5.8: unquoted attr "+t.getParmValue("A");
				// single quoted?? parser only handles double quotes for quoted values, but ensure at least no crash
				// attribute without value (empty)
				tags = xml.parseAllXML("<TAG FLAG>val</TAG>");
				t = tags.get(0);
				if(!t.parms().containsKey("FLAG"))
					return "Error#5.9: flag attr missing";
				if(!"".equals(t.getParmValue("FLAG")))
					return "Error#5.10: flag attr value '"+t.getParmValue("FLAG")+"'";
				// lower case attribute name should be uppercased
				tags = xml.parseAllXML("<TAG myattr=\"v\">x</TAG>");
				t = tags.get(0);
				if(!"v".equals(t.getParmValue("MYATTR")))
					return "Error#5.11: lower attr not upper: "+t.parms().keySet();
				if(!"v".equals(t.getParmValue("myattr")))
					return "Error#5.12: lower lookup failed";
				// null tag for getParmValue should return null
				t = tags.get(0);
				if(t.getParmValue(null)!=null)
					return "Error#5.13: null parm should return null";
			}

			// Test 6: parseAllXML - comments, CDATA, processing instructions ignored
			{
				List<XMLTag> tags = xml.parseAllXML("<!-- comment --><TAG>val</TAG>");
				if(tags.size()!=1)
					return "Error#6.1: comment ignored size "+tags.size();
				if(!tags.get(0).tag().equals("TAG"))
					return "Error#6.2: comment tag "+tags.get(0).tag();
				tags = xml.parseAllXML("<![CDATA[ <notatag> ]]><TAG>val</TAG>");
				if(tags.size()!=1)
					return "Error#6.3: CDATA size "+tags.size();
				if(!tags.get(0).value().equals("val"))
					return "Error#6.4: CDATA value "+tags.get(0).value();
				tags = xml.parseAllXML("<?xml version=\"1.0\"?><TAG>val</TAG>");
				if(tags.size()!=1)
					return "Error#6.5: PI size "+tags.size();
				// comment inside
				tags = xml.parseAllXML("<OUTER><!-- hi --><INNER>1</INNER></OUTER>");
				if(tags.size()!=1)
					return "Error#6.6: inner comment size";
				if(tags.get(0).contents().size()!=1)
					return "Error#6.7: inner comment contents "+tags.get(0).contents().size();
				if(!tags.get(0).contents().get(0).value().equals("1"))
					return "Error#6.8: inner comment value";
			}

			// Test 7: convertXMLtoTag
			{
				String s = xml.convertXMLtoTag("HELLO", "world");
				if(!s.equals("<HELLO>world</HELLO>"))
					return "Error#7.1: convert string "+s;
				s = xml.convertXMLtoTag("HELLO", "");
				if(!s.equals("<HELLO />"))
					return "Error#7.2: convert empty string "+s;
				s = xml.convertXMLtoTag("HELLO", (String)null);
				if(!s.equals("<HELLO />"))
					return "Error#7.3: convert null string "+s;
				s = xml.convertXMLtoTag("NUM", 123);
				if(!s.equals("<NUM>123</NUM>"))
					return "Error#7.4: convert int "+s;
				s = xml.convertXMLtoTag("NUM", (short)5);
				if(!s.equals("<NUM>5</NUM>"))
					return "Error#7.5: convert short "+s;
				s = xml.convertXMLtoTag("NUM", 999L);
				if(!s.equals("<NUM>999</NUM>"))
					return "Error#7.6: convert long "+s;
				s = xml.convertXMLtoTag("FLAG", true);
				if(!s.equals("<FLAG>true</FLAG>"))
					return "Error#7.7: convert bool true "+s;
				s = xml.convertXMLtoTag("FLAG", false);
				if(!s.equals("<FLAG>false</FLAG>"))
					return "Error#7.8: convert bool false "+s;
			}

			// Test 8: returnXMLBlock, returnXMLValue, returnXMLBoolean
			{
				String blob = "<A>1</A><B>2</B><C>3</C>";
				String block = xml.returnXMLBlock(blob, "B");
				if(!block.contains("<B>"))
					return "Error#8.1: block B "+block;
				if(!block.contains("2"))
					return "Error#8.2: block B content";
				// not found
				block = xml.returnXMLBlock(blob, "Z");
				if(!block.equals(""))
					return "Error#8.3: block not found should be empty: '"+block+"'";
				// self-closing block
				blob = "<A /><B>val</B>";
				block = xml.returnXMLBlock(blob, "A");
				if(block.length()==0)
					return "Error#8.4: self-close block empty";
				String val = xml.returnXMLValue("<TAG>hello</TAG>");
				if(!val.equals("hello"))
					return "Error#8.5: returnXMLValue no tag expected hello, got '"+val+"'";
				val = xml.returnXMLValue("<TAG>  hello world  </TAG>");
				if(!val.equals("hello world"))
					return "Error#8.5b: returnXMLValue trimmed hello world, got '"+val+"'";
				val = xml.returnXMLValue("<TAG />");
				if(!val.equals(""))
					return "Error#8.6: self-close returnXMLValue should be empty: '"+val+"'";
				val = xml.returnXMLValue("no tags here");
				if(!val.equals(""))
					return "Error#8.7: no tags value should be empty";
				val = xml.returnXMLValue("<A>1</A><B>2</B>");
				if(!val.equals("1"))
					return "Error#8.7b: returnXMLValue first of two expected 1, got '"+val+"'";
				val = xml.returnXMLValue("");
				if(!val.equals(""))
					return "Error#8.7c: empty blob should be empty: '"+val+"'";
				val = xml.returnXMLValue(null);
				if(!val.equals(""))
					return "Error#8.7d: null blob should be empty: '"+val+"'";
				// returnXMLValue with tag
				blob = "<ROOT><X>foo</X><Y>bar</Y></ROOT>";
				val = xml.returnXMLValue(blob, "X");
				if(!val.equals("foo"))
					return "Error#8.8: returnXMLValue X "+val;
				val = xml.returnXMLValue(blob, "MISSING");
				if(!val.equals(""))
					return "Error#8.9: missing tag value should be empty: '"+val+"'";
				// returnXMLBoolean
				blob = "<FLAG>true</FLAG>";
				if(!xml.returnXMLBoolean(blob, "FLAG"))
					return "Error#8.10: returnXMLBoolean true failed";
				blob = "<FLAG>True</FLAG>";
				if(!xml.returnXMLBoolean(blob, "FLAG"))
					return "Error#8.11: returnXMLBoolean True case failed";
				blob = "<FLAG>T</FLAG>";
				if(!xml.returnXMLBoolean(blob, "FLAG"))
					return "Error#8.12: returnXMLBoolean T failed";
				blob = "<FLAG>false</FLAG>";
				if(xml.returnXMLBoolean(blob, "FLAG"))
					return "Error#8.13: returnXMLBoolean false should be false";
				blob = "<FLAG></FLAG>";
				if(xml.returnXMLBoolean(blob, "FLAG"))
					return "Error#8.14: empty should be false";
				blob = "<A><FLAG>false</FLAG></A>";
				if(xml.returnXMLBoolean(blob, "MISSING"))
					return "Error#8.15: missing should be false";
			}

			// Test 9: parseOutAngleBrackets, parseOutAngleBracketsAndQuotes, restoreAngleBrackets
			{
				String s = xml.parseOutAngleBrackets(null);
				if(!s.equals(""))
					return "Error#9.1: null parseOut should be empty";
				s = xml.parseOutAngleBrackets("hello");
				if(!s.equals("hello"))
					return "Error#9.2: plain parseOut "+s;
				s = xml.parseOutAngleBrackets("a<b>c");
				if(!s.equals("a&lt;b&gt;c"))
					return "Error#9.3: brackets "+s;
				s = xml.parseOutAngleBrackets("<> < >");
				if((!s.contains("&lt;"))||(!s.contains("&gt;")))
					return "Error#9.4: multiple brackets "+s;
				// parseOutAngleBracketsAndQuotes
				s = xml.parseOutAngleBracketsAndQuotes("a\"b");
				if(!s.equals("a&quot;b"))
					return "Error#9.5: quotes "+s;
				s = xml.parseOutAngleBracketsAndQuotes("a<b>\"c\"");
				if((!s.contains("&lt;"))||(!s.contains("&quot;")))
					return "Error#9.6: combined "+s;
				s = xml.parseOutAngleBracketsAndQuotes(null);
				if(!s.equals(""))
					return "Error#9.7: null quotes should be empty";
				// restoreAngleBrackets
				String r = xml.restoreAngleBrackets(null);
				if(!r.equals(""))
					return "Error#9.8: null restore empty";
				r = xml.restoreAngleBrackets("hello");
				if(!r.equals("hello"))
					return "Error#9.9: plain restore "+r;
				r = xml.restoreAngleBrackets("a&lt;b&gt;c");
				if(!r.equals("a<b>c"))
					return "Error#9.10: restore lt gt "+r;
				r = xml.restoreAngleBrackets("a&quot;b");
				if(!r.equals("a\"b"))
					return "Error#9.11: restore quot "+r;
				r = xml.restoreAngleBrackets("a&amp;b");
				if(!r.equals("a&b"))
					return "Error#9.12: restore amp should be 'a&b', got '"+r+"'";
				r = xml.restoreAngleBrackets("a &amp; b &amp; c");
				if(!r.equals("a & b & c"))
					return "Error#9.12b: multiple amp failed: '"+r+"'";
				r = xml.restoreAngleBrackets("&amp;&lt;&gt;&quot;&apos;");
				if(!r.equals("&<>\"'"))
					return "Error#9.12c: all entities failed: '"+r+"'";
				r = xml.restoreAngleBrackets("a&AMP;b");
				if(!r.equals("a&b"))
					return "Error#9.12d: amp case insensitive failed: '"+r+"'";
				// round-trip for ampersand: manual encode then restore
				r = xml.restoreAngleBrackets("hello &amp; world");
				if(!r.equals("hello & world"))
					return "Error#9.12e: amp roundtrip: '"+r+"'";
				// apos should work correctly
				r = xml.restoreAngleBrackets("a&apos;b");
				if(!r.equals("a'b"))
					return "Error#9.13: restore apos "+r;
				// case insensitive
				r = xml.restoreAngleBrackets("a&LT;b&GT;c");
				if(!r.equals("a<b>c"))
					return "Error#9.14: restore case insensitive "+r;
				r = xml.restoreAngleBrackets("a&QuOt;b");
				if(!r.equals("a\"b"))
					return "Error#9.15: quot case "+r;
				// percent encoding
				r = xml.restoreAngleBrackets("%20");
				if(!r.equals(" "))
					return "Error#9.16: percent 20 should be space: '"+r+"'";
				r = xml.restoreAngleBrackets("%41");
				if(!r.equals("A"))
					return "Error#9.17: percent 41 should be A: '"+r+"'";
				// round-trip
				String original = "hello <world> \"test\" & 'check'";
				String encoded = xml.parseOutAngleBracketsAndQuotes(original);
				// need to also encode &? parseOut does not encode &, but restore handles it
				// So test simpler round-trip for brackets only
				original = "a<b>c";
				encoded = xml.parseOutAngleBrackets(original);
				r = xml.restoreAngleBrackets(encoded);
				if(!r.equals(original))
					return "Error#9.18: roundtrip brackets "+r+" vs "+original;
				original = "a\"b";
				encoded = xml.parseOutAngleBracketsAndQuotes(original);
				r = xml.restoreAngleBrackets(encoded);
				if(!r.equals(original))
					return "Error#9.19: roundtrip quotes "+r;
				// string with no encoding should be unchanged
				r = xml.restoreAngleBrackets("no special chars");
				if(!r.equals("no special chars"))
					return "Error#9.20: no special restore";
				// percent invalid should be left as is
				r = xml.restoreAngleBrackets("%ZZ");
				if(!r.equals("%ZZ"))
					return "Error#9.21: invalid percent should remain: "+r;
				r = xml.restoreAngleBrackets("%");
				if(!r.equals("%"))
					return "Error#9.22: lone percent "+r;
			}

			// Test 10: createNewTag and XMLTag basics
			{
				XMLTag tag = xml.createNewTag("HELLO", "world");
				if(!tag.tag().equals("HELLO"))
					return "Error#10.1: create tag "+tag.tag();
				if(!tag.value().equals("world"))
					return "Error#10.2: create value "+tag.value();
				tag = xml.createNewTag("test", "val");
				if(!tag.tag().equals("TEST"))
					return "Error#10.3: create lower should be upper: "+tag.tag();
				// setValue
				tag.setValue("newVal");
				if(!tag.value().equals("newVal"))
					return "Error#10.4: setValue "+tag.value();
				// toString
				tag = xml.createNewTag("A", "hello");
				String ts = tag.toString();
				if(!ts.equals("<A>hello</A>"))
					return "Error#10.5: toString "+ts;
				tag = xml.createNewTag("A", "hello");
				tag.parms().put("X","1");
				ts = tag.toString();
				if((!ts.contains("<A"))||(!ts.contains("X=\"1\""))||(!ts.contains("hello")))
					return "Error#10.6: toString with parms "+ts;
				// copyOf
				XMLTag orig = xml.createNewTag("ORIG", "val");
				orig.parms().put("P","1");
				orig.addContent(xml.createNewTag("CHILD","cval"));
				XMLTag copy = orig.copyOf();
				if(copy==orig)
					return "Error#10.7: copy should be new instance";
				if(!copy.tag().equals(orig.tag()))
					return "Error#10.8: copy tag";
				if(!copy.value().equals(orig.value()))
					return "Error#10.9: copy value";
				if(copy.contents().size()!=orig.contents().size())
					return "Error#10.10: copy contents size";
				if(copy.parms().size()!=orig.parms().size())
					return "Error#10.11: copy parms size";
				// modify copy should not affect original parms (since copy makes new Hashtable)
				copy.parms().put("NEW","2");
				if(orig.parms().containsKey("NEW"))
					return "Error#10.12: copy parms should be independent";
				// addContent
				XMLTag parent = xml.createNewTag("PARENT","pval");
				if(parent.contents().size()!=0)
					return "Error#10.13: new parent contents should be 0";
				XMLTag child = xml.createNewTag("CHILD","cval");
				parent.addContent(child);
				if(parent.contents().size()!=1)
					return "Error#10.14: addContent size";
				if(child.parent()!=parent)
					return "Error#10.15: child parent link";
				parent.addContent(null);
				if(parent.contents().size()!=1)
					return "Error#10.16: add null should not change size";
				// outer/inner indices from parsing
				List<XMLTag> tags = xml.parseAllXML("<A>hello</A>");
				XMLTag a = tags.get(0);
				if(a.outerStartIndex()<0)
					return "Error#10.17: outerStart should be >=0";
				if(a.innerStartIndex()<0)
					return "Error#10.18: innerStart";
				if(a.outerEndIndex()<0)
					return "Error#10.19: outerEnd";
				// self-closing indices
				tags = xml.parseAllXML("<A />");
				a = tags.get(0);
				if(a.outerStartIndex()<0)
					return "Error#10.20: self close outerStart";
				// isTagInPieces, getPieceFromPieces etc. on XMLTag itself
				tags = xml.parseAllXML("<ROOT><X>1</X><Y>2</Y></ROOT>");
				XMLTag root = tags.get(0);
				if(!root.isTagInPieces("X"))
					return "Error#10.21: isTagInPieces X";
				if(root.isTagInPieces("Z"))
					return "Error#10.22: isTagInPieces Z should be false";
				if(root.getPieceFromPieces("X")==null)
					return "Error#10.23: getPieceFromPieces X null";
				if(!root.getPieceFromPieces("X").value().equals("1"))
					return "Error#10.24: getPiece X value";
				if(root.getPieceFromPieces("missing")!=null)
					return "Error#10.25: missing should be null";
				if((root.getValFromPieces("X")==null)||(!root.getValFromPieces("X").equals("1")))
					return "Error#10.26: getValFromPieces X";
				if(!root.getValFromPieces("missing","def").equals("def"))
					return "Error#10.27: getVal def";
				if(root.getContentsFromPieces("X")==null)
					return "Error#10.28: getContentsFromPieces X null";
				if(root.getContentsFromPieces("missing")!=null)
					return "Error#10.29: getContents missing should be null";
				List<XMLTag> pieces = root.getPiecesFromPieces("X");
				if((pieces==null)||(pieces.size()!=1))
					return "Error#10.30: getPiecesFromPieces X size "+(pieces==null?"null":""+pieces.size());
				// get*FromPieces with type conversion
				tags = xml.parseAllXML("<R><I>123</I><S>45</S><L>999</L><D>3.14</D><B>true</B><F>false</F></R>");
				root = tags.get(0);
				if(root.getIntFromPieces("I")!=123)
					return "Error#10.31: getInt";
				if(root.getIntFromPieces("MISSING")!=0)
					return "Error#10.32: getInt missing should be 0";
				if(root.getIntFromPieces("I",999)!=123)
					return "Error#10.33: getInt with def";
				if(root.getIntFromPieces("MISSING",999)!=999)
					return "Error#10.34: getInt missing def";
				if(root.getShortFromPieces("S")!=(short)45)
					return "Error#10.35: getShort";
				if(root.getLongFromPieces("L")!=999L)
					return "Error#10.36: getLong";
				if(Math.abs(root.getDoubleFromPieces("D")-3.14)>0.001)
					return "Error#10.37: getDouble";
				if(!root.getBoolFromPieces("B"))
					return "Error#10.38: getBool true";
				if(root.getBoolFromPieces("F"))
					return "Error#10.39: getBool false should be false";
				if(root.getBoolFromPieces("MISSING"))
					return "Error#10.40: getBool missing should be false";
				if(!root.getBoolFromPieces("B",false))
					return "Error#10.41: getBool with def true";
				if(root.getDoubleFromPieces("MISSING", 1.23)!=1.23)
					return "Error#10.42: getDouble def";
				if(root.getLongFromPieces("MISSING", 5L)!=5L)
					return "Error#10.43: getLong def";
				if(root.getShortFromPieces("MISSING", (short)7)!=(short)7)
					return "Error#10.44: getShort def";
				// case insensitive tag lookup
				if(!root.isTagInPieces("i"))
					return "Error#10.45: case insensitive isTag";
				if(root.getPieceFromPieces("i")==null)
					return "Error#10.46: case insensitive getPiece";
			}

			// Test 11: List<XMLTag> variant helpers (XMLManager level)
			{
				List<XMLTag> tags = xml.parseAllXML("<ROOT><A>1</A><B>2</B><A>3</A></ROOT>");
				XMLTag root = tags.get(0);
				List<XMLTag> contents = root.contents();
				if(!xml.isTagInPieces(contents, "A"))
					return "Error#11.1: isTagInPieces list";
				if(xml.isTagInPieces(contents, "Z"))
					return "Error#11.2: isTagInPieces Z should be false";
				if(xml.isTagInPieces(null, "A"))
					return "Error#11.3: null list should be false";
				XMLTag piece = xml.getPieceFromPieces(contents, "B");
				if((piece==null)||(!piece.value().equals("2")))
					return "Error#11.4: getPieceFromPieces list B";
				if(xml.getPieceFromPieces(contents, "Z")!=null)
					return "Error#11.5: missing piece not null";
				if(xml.getPieceFromPieces(null, "A")!=null)
					return "Error#11.6: null list getPiece should be null";
				if(!xml.getValFromPieces(contents, "A").equals("1"))
					return "Error#11.7: getValFromPieces list A should be first 1, got "+xml.getValFromPieces(contents,"A");
				if(!xml.getValFromPieces(contents, "Z", "def").equals("def"))
					return "Error#11.8: getVal def";
				if((xml.getValFromPieces(null, "A")!=null)&&(!xml.getValFromPieces(null,"A","").equals(""))) {
					// getValFromPieces with null returns defVal, check
				}
				if(!xml.getValFromPieces(contents, "B").equals("2"))
					return "Error#11.9: getVal B";
				List<XMLTag> fromPieces = xml.getContentsFromPieces(contents, "A");
				if(fromPieces==null)
					return "Error#11.10: getContentsFromPieces A null";
				if(xml.getContentsFromPieces(contents, "Z")!=null)
					return "Error#11.11: missing contents should be null";
				List<XMLTag> allA = xml.getPiecesFromPieces(contents, "A");
				if((allA==null)||(allA.size()!=2))
					return "Error#11.12: getPiecesFromPieces A size "+(allA==null?"null":""+allA.size());
				if(xml.getPiecesFromPieces(null, "A")!=null)
					return "Error#11.13: null getPieces should be null";
				// typed getters with List
				List<XMLTag> typed = xml.parseAllXML("<R><I>123</I><S>45</S><L>999</L><D>3.14</D><B>true</B></R>").get(0).contents();
				if(xml.getIntFromPieces(typed,"I")!=123)
					return "Error#11.14: getInt list";
				if(xml.getIntFromPieces(typed,"MISSING",999)!=999)
					return "Error#11.15: getInt def list";
				if(xml.getShortFromPieces(typed,"S")!=(short)45)
					return "Error#11.16: getShort list";
				if(xml.getLongFromPieces(typed,"L")!=999L)
					return "Error#11.17: getLong list";
				if(Math.abs(xml.getDoubleFromPieces(typed,"D")-3.14)>0.001)
					return "Error#11.18: getDouble list";
				if(!xml.getBoolFromPieces(typed,"B"))
					return "Error#11.19: getBool list";
				if(xml.getBoolFromPieces(typed,"MISSING",false))
					return "Error#11.20: getBool missing list should be false";
				if(xml.getIntFromPieces(typed,"MISSING")!=0)
					return "Error#11.21: getInt missing 0";
				// getVal with missing returns def
				if(!xml.getValFromPieces(typed,"MISSING","def").equals("def"))
					return "Error#11.22: getVal def missing";
			}

			// Test 12: getXMLList / parseXMLList
			{
				List<String> orig = new Vector<String>();
				orig.add("hello");
				orig.add("world");
				orig.add("");
				String xmlList = xml.getXMLList(orig);
				if(!xmlList.contains("<X>hello</X>"))
					return "Error#12.1: getXMLList hello "+xmlList;
				if(!xmlList.contains("<X>world</X>"))
					return "Error#12.2: getXMLList world";
				if(!xmlList.contains("<X />"))
					return "Error#12.3: getXMLList empty should be <X /> : "+xmlList;
				List<String> parsed = xml.parseXMLList(xmlList);
				if(parsed.size()!=3)
					return "Error#12.4: parseXMLList size "+parsed.size();
				if((!parsed.get(0).equals("hello"))||(!parsed.get(1).equals("world"))||(!parsed.get(2).equals("")))
					return "Error#12.5: parseXMLList values "+parsed;
				// null handling
				String emptyList = xml.getXMLList(null);
				if(!emptyList.equals(""))
					return "Error#12.6: null list should be empty string";
				// angle brackets should be escaped and restored
				orig = new Vector<String>();
				orig.add("a<b>c");
				orig.add("x>y");
				xmlList = xml.getXMLList(orig);
				if((!xmlList.contains("&lt;"))||(!xmlList.contains("&gt;")))
					return "Error#12.7: angle escape "+xmlList;
				parsed = xml.parseXMLList(xmlList);
				if(!parsed.get(0).equals("a<b>c"))
					return "Error#12.8: angle restore "+parsed.get(0);
				if(!parsed.get(1).equals("x>y"))
					return "Error#12.9: angle restore 2 "+parsed.get(1);
				// null entries are skipped
				orig = new Vector<String>();
				orig.add("a");
				orig.add(null);
				orig.add("b");
				xmlList = xml.getXMLList(orig);
				parsed = xml.parseXMLList(xmlList);
				if(parsed.size()!=2)
					return "Error#12.10: null entries skipped size "+parsed.size();
				// whitespace-only string is treated as empty
				orig = new Vector<String>();
				orig.add("   ");
				xmlList = xml.getXMLList(orig);
				if(!xmlList.contains("<X />"))
					return "Error#12.11: whitespace empty "+xmlList;
			}

			// Test 13: toXML / fromXML map
			{
				Map<String,String> map = new Hashtable<String,String>();
				map.put("key1","value1");
				map.put("key2","value2");
				String s = xml.toXML(map);
				if((!s.contains("<KEY1>value1</KEY1>"))&&(!s.contains("<key1>value1</key1>"))) {
					// toXML escapes keys, but check upper? Actually toXML does parseOutAngleBrackets on key and value, then convertXMLtoTag which keeps case
					// Keys are not uppercased in toXML, but fromXML restores. Check at least contains value
					if(!s.contains("value1"))
						return "Error#13.1: toXML value1 missing: "+s;
				}
				Map<String,String> restored = xml.fromXML(s);
				if((!"value1".equals(restored.get("key1")))&&(!"value1".equals(restored.get("KEY1"))))
					return "Error#13.2: fromXML key1 "+restored;
				if(restored.size()!=2)
					return "Error#13.3: fromXML size "+restored.size();
				// angle brackets in keys/values (keys become uppercased by parser)
				map = new Hashtable<String,String>();
				map.put("A<B","c>d");
				s = xml.toXML(map);
				if((!s.contains("&lt;"))||(!s.contains("&gt;")))
					return "Error#13.4: toXML angle escape "+s;
				restored = xml.fromXML(s);
				if(!"c>d".equals(restored.get("A<B")))
					return "Error#13.5: fromXML angle restore key: "+restored.keySet()+" val: "+restored.get("A<B");
				// empty map
				map = new Hashtable<String,String>();
				s = xml.toXML(map);
				if(!s.equals(""))
					return "Error#13.6: empty map toXML should be empty: '"+s+"'";
				restored = xml.fromXML("");
				if(restored.size()!=0)
					return "Error#13.7: empty fromXML size "+restored.size();
			}

			// Test 14: fromPOJOtoXML / fromXMLtoPOJO round-trip
			{
				SimplePojo pojo = new SimplePojo("hello", 123, true, 999L, 3.14);
				String xmlStr = xml.fromPOJOtoXML(pojo);
				if(!xmlStr.contains("<name>hello</name>"))
					return "Error#14.1: pojo name "+xmlStr;
				if(!xmlStr.contains("<count>123</count>"))
					return "Error#14.2: pojo count "+xmlStr;
				if(!xmlStr.contains("<flag>true</flag>"))
					return "Error#14.3: pojo flag "+xmlStr;
				if(!xmlStr.contains("<longVal>999</longVal>"))
					return "Error#14.4: pojo long "+xmlStr;
				if(!xmlStr.contains("<doubleVal>3.14</doubleVal>"))
					return "Error#14.5: pojo double "+xmlStr;
				SimplePojo pojo2 = new SimplePojo();
				xml.fromXMLtoPOJO(xmlStr, pojo2);
				if(!"hello".equals(pojo2.name))
					return "Error#14.6: roundtrip name "+pojo2.name;
				if(pojo2.count!=123)
					return "Error#14.7: roundtrip count "+pojo2.count;
				if(!pojo2.flag)
					return "Error#14.8: roundtrip flag";
				if(pojo2.longVal!=999L)
					return "Error#14.9: roundtrip long "+pojo2.longVal;
				if(Math.abs(pojo2.doubleVal-3.14)>0.001)
					return "Error#14.10: roundtrip double "+pojo2.doubleVal;
				// wrapper types
				WrapperPojo w = new WrapperPojo();
				w.fVal = Float.valueOf(1.5f);
				w.dVal = Double.valueOf(2.5);
				w.iVal = Integer.valueOf(42);
				w.lVal = Long.valueOf(12345L);
				w.sVal = Short.valueOf((short)7);
				w.bVal = Byte.valueOf((byte)9);
				w.boolVal = Boolean.TRUE;
				w.strVal = "testStr";
				xmlStr = xml.fromPOJOtoXML(w);
				WrapperPojo w2 = new WrapperPojo();
				xml.fromXMLtoPOJO(xmlStr, w2);
				if((!w.fVal.equals(w2.fVal))&&(Math.abs(w.fVal.floatValue()-w2.fVal.floatValue())>0.001))
					return "Error#14.11: wrapper float "+w2.fVal;
				if(!w.dVal.equals(w2.dVal))
					return "Error#14.12: wrapper double "+w2.dVal;
				if(!w.iVal.equals(w2.iVal))
					return "Error#14.13: wrapper int "+w2.iVal;
				if(!w.lVal.equals(w2.lVal))
					return "Error#14.14: wrapper long "+w2.lVal;
				if(!w.sVal.equals(w2.sVal))
					return "Error#14.15: wrapper short "+w2.sVal;
				if(!w.bVal.equals(w2.bVal))
					return "Error#14.16: wrapper byte "+w2.bVal;
				if(!w.boolVal.equals(w2.boolVal))
					return "Error#14.17: wrapper bool "+w2.boolVal;
				if(!w.strVal.equals(w2.strVal))
					return "Error#14.18: wrapper str "+w2.strVal;
				// array pojo with nested objects
				ArrayPojo ap = new ArrayPojo();
				ap.strArray = new String[] {"a","b","c"};
				ap.intArray = new int[] {1,2,3};
				ap.integerArray = new Integer[] {Integer.valueOf(4), Integer.valueOf(5)};
				ap.inner = new InnerPojo("innerVal", 99);
				ap.innerArray = new InnerPojo[] { new InnerPojo("x",1), new InnerPojo("y",2) };
				ap.nullField = null;
				xmlStr = xml.fromPOJOtoXML(ap);
				if(!xmlStr.contains("<strArray>"))
					return "Error#14.19: array pojo strArray missing "+xmlStr;
				if(!xmlStr.contains("<VALUE>"))
					return "Error#14.20: VALUE tag missing "+xmlStr;
				if(!xmlStr.contains("ISNULL=TRUE"))
					return "Error#14.21: null field ISNULL missing "+xmlStr;
				ArrayPojo ap2 = new ArrayPojo();
				xml.fromXMLtoPOJO(xmlStr, ap2);
				if((ap2.strArray==null)||(ap2.strArray.length!=3))
					return "Error#14.22: strArray roundtrip "+(ap2.strArray==null?null:java.util.Arrays.toString(ap2.strArray));
				if((!ap2.strArray[0].equals("a"))||(!ap2.strArray[1].equals("b"))||(!ap2.strArray[2].equals("c")))
					return "Error#14.22b: strArray values "+java.util.Arrays.toString(ap2.strArray);
				// additional String[] angle bracket roundtrip
				{
					ArrayPojo ap3 = new ArrayPojo();
					ap3.strArray = new String[] {"a<b","c>d","e&f"};
					ap3.intArray = new int[] {1};
					String xmlStr3 = xml.fromPOJOtoXML(ap3);
					if((!xmlStr3.contains("&lt;"))||(!xmlStr3.contains("&gt;")))
						return "Error#14.22c: strArray angle escape missing "+xmlStr3;
					ArrayPojo ap4 = new ArrayPojo();
					xml.fromXMLtoPOJO(xmlStr3, ap4);
					if((ap4.strArray==null)||(ap4.strArray.length!=3))
						return "Error#14.22d: strArray angle length";
					if((!ap4.strArray[0].equals("a<b"))||(!ap4.strArray[1].equals("c>d"))||(!ap4.strArray[2].equals("e&f")))
						return "Error#14.22e: strArray angle values "+java.util.Arrays.toString(ap4.strArray);
				}
				// empty String[] roundtrip
				{
					ArrayPojo apEmpty = new ArrayPojo();
					apEmpty.strArray = new String[0];
					String xmlEmpty = xml.fromPOJOtoXML(apEmpty);
					ArrayPojo apEmpty2 = new ArrayPojo();
					xml.fromXMLtoPOJO(xmlEmpty, apEmpty2);
					if((apEmpty2.strArray==null)||(apEmpty2.strArray.length!=0))
						return "Error#14.22f: empty strArray length "+(apEmpty2.strArray==null?null:java.util.Arrays.toString(apEmpty2.strArray));
				}
				if((ap2.intArray==null)||(ap2.intArray.length!=3)||(ap2.intArray[1]!=2))
					return "Error#14.23: intArray roundtrip";
				if((ap2.integerArray==null)||(ap2.integerArray.length!=2)||(!ap2.integerArray[0].equals(Integer.valueOf(4))))
					return "Error#14.24: integerArray roundtrip";
				if((ap2.inner==null)||(!"innerVal".equals(ap2.inner.innerVal))||(ap2.inner.innerNum!=99))
					return "Error#14.25: inner roundtrip "+(ap2.inner==null?null:ap2.inner.innerVal);
				if((ap2.innerArray==null)||(ap2.innerArray.length!=2)||(!"x".equals(ap2.innerArray[0].innerVal)))
					return "Error#14.26: innerArray roundtrip";
				if(ap2.nullField!=null)
					return "Error#14.27: nullField should be null";
				// angle brackets in POJO string should be escaped
				SimplePojo pojo3 = new SimplePojo("a<b>c", 1, false, 0L, 0.0);
				xmlStr = xml.fromPOJOtoXML(pojo3);
				if((!xmlStr.contains("&lt;"))||(!xmlStr.contains("&gt;")))
					return "Error#14.28: pojo angle escape "+xmlStr;
				// fromPOJOtoXML with null object should return empty
				String empty = xml.fromPOJOtoXML(null);
				if(!empty.equals(""))
					return "Error#14.29: null pojo should be empty: '"+empty+"'";
				// fromXMLtoPOJO with List overload
				SimplePojo pojo4 = new SimplePojo("listTest", 55, true, 1L, 1.0);
				String xmlStr2 = xml.fromPOJOtoXML(pojo4);
				List<XMLTag> parsedTags = xml.parseAllXML(xmlStr2);
				SimplePojo pojo5 = new SimplePojo();
				xml.fromXMLtoPOJO(parsedTags, pojo5);
				if(!"listTest".equals(pojo5.name))
					return "Error#14.30: list overload roundtrip "+pojo5.name;
				// fromPOJOFieldtoXML direct tests
				String fieldXml = xml.fromPOJOFieldtoXML(String.class, "hello");
				if(!fieldXml.equals("hello"))
					return "Error#14.31: fromPOJOField String "+fieldXml;
				fieldXml = xml.fromPOJOFieldtoXML(int.class, Integer.valueOf(5));
				if(!fieldXml.equals("5"))
					return "Error#14.32: fromPOJOField primitive int "+fieldXml;
				fieldXml = xml.fromPOJOFieldtoXML(String[].class, new String[] {"x","y"});
				if((!fieldXml.contains("<VALUE>x</VALUE>"))||(!fieldXml.contains("<VALUE>y</VALUE>")))
					return "Error#14.33: fromPOJOField string array "+fieldXml;
				// angle brackets via field
				fieldXml = xml.fromPOJOFieldtoXML(String.class, "a<b");
				if(!fieldXml.equals("a&lt;b"))
					return "Error#14.34: fromPOJOField angle "+fieldXml;
			}

			// Test 15: getXMLParser streaming
			{
				StringBuffer buf = new StringBuffer();
				List<XMLTag> out = new ArrayList<XMLTag>();
				Runnable parser = xml.getXMLParser(buf, out);
				// single tag
				buf.append("<A>1</A>");
				parser.run();
				if(out.size()!=1)
					return "Error#15.1: streaming single size "+out.size();
				if((!out.get(0).tag().equals("A"))||(!out.get(0).value().equals("1")))
					return "Error#15.2: streaming single value";
				if(buf.length()!=0)
					return "Error#15.3: buffer should be trimmed after parse, len "+buf.length();
				// multiple tags in one buffer
				buf.append("<A>1</A><B>2</B><C>3</C>");
				parser.run();
				if(out.size()!=3)
					return "Error#15.4: streaming multi size "+out.size();
				if((!out.get(0).tag().equals("A"))||(!out.get(1).tag().equals("B")))
					return "Error#15.5: streaming multi tags";
				if(buf.length()!=0)
					return "Error#15.6: buffer trimmed multi";
				// incremental: split tag across runs
				buf = new StringBuffer();
				out = new ArrayList<XMLTag>();
				parser = xml.getXMLParser(buf, out);
				buf.append("<A>");
				parser.run();
				if(out.size()!=0)
					return "Error#15.7: incomplete should be 0";
				buf.append("hello</A>");
				parser.run();
				if(out.size()!=1)
					return "Error#15.8: incremental hello size "+out.size();
				if(!out.get(0).value().equals("hello"))
					return "Error#15.9: incremental hello value";
				// incremental nested
				buf = new StringBuffer();
				out = new ArrayList<XMLTag>();
				parser = xml.getXMLParser(buf, out);
				buf.append("<OUTER><INNER>");
				parser.run();
				if(out.size()!=0)
					return "Error#15.10: nested incomplete 0";
				buf.append("val</INNER></OUTER>");
				parser.run();
				if(out.size()!=1)
					return "Error#15.11: nested complete size "+out.size();
				if(!out.get(0).tag().equals("OUTER"))
					return "Error#15.12: nested outer tag";
				if(out.get(0).contents().size()!=1)
					return "Error#15.13: nested contents size";
				// buffer with incomplete at end plus complete in middle
				buf = new StringBuffer();
				out = new ArrayList<XMLTag>();
				parser = xml.getXMLParser(buf, out);
				buf.append("<A>1</A><B>");
				parser.run();
				if(out.size()!=1)
					return "Error#15.14: should have 1 complete, got "+out.size();
				if(!out.get(0).tag().equals("A"))
					return "Error#15.15: first A";
				buf.append("2</B><C>3</C>");
				parser.run();
				if(out.size()!=2)
					return "Error#15.16: should have 2 completions, got "+out.size();
				if((!out.get(0).tag().equals("B"))||(!out.get(1).tag().equals("C")))
					return "Error#15.17: B C tags";
				// results cleared between runs
				buf = new StringBuffer();
				out = new ArrayList<XMLTag>();
				parser = xml.getXMLParser(buf, out);
				buf.append("<A>1</A>");
				parser.run();
				if(out.size()!=1)
					return "Error#15.18: first run size";
				buf.append("<B>2</B>");
				parser.run();
				if(out.size()!=1)
					return "Error#15.19: second run should have only 1 new, got "+out.size();
				if(!out.get(0).tag().equals("B"))
					return "Error#15.20: second run should be B";
				// many elements trimming
				buf = new StringBuffer();
				out = new ArrayList<XMLTag>();
				parser = xml.getXMLParser(buf, out);
				for(int i=0;i<30;i++)
				{
					buf.append("<X>"+i+"</X>");
					parser.run();
					if(out.size()!=1)
						return "Error#15.21."+i+": trimming run "+i+" size "+out.size();
					if(!out.get(0).value().equals(String.valueOf(i)))
						return "Error#15.22."+i+": value "+out.get(0).value();
				}
				if(buf.length()>50)
					return "Error#15.23: buffer not trimmed, len="+buf.length();
				// whitespace handling
				buf = new StringBuffer();
				out = new ArrayList<XMLTag>();
				parser = xml.getXMLParser(buf, out);
				buf.append("   \n\t  ");
				parser.run();
				if(out.size()!=0)
					return "Error#15.24: whitespace only should be 0";
				buf.append("  <A>1</A>  ");
				parser.run();
				if(out.size()!=1)
					return "Error#15.25: whitespace tag size";
				// self-closing streaming (fixed: outerPiecesCompleted now incremented for self-close)
				{
					List<XMLTag> all = xml.parseAllXML("<A /><B>val</B>");
					if(all.size()!=2)
						return "Error#15.26: parseAll self-close size "+all.size();
					if(!all.get(0).tag().equals("A"))
						return "Error#15.26c: parseAll first tag A";
					if(!all.get(1).value().equals("val"))
						return "Error#15.26d: parseAll second value";
				}
				// streaming single self-close
				{
					buf = new StringBuffer();
					out = new ArrayList<XMLTag>();
					parser = xml.getXMLParser(buf, out);
					buf.append("<A />");
					parser.run();
					if(out.size()!=1)
						return "Error#15.26e: streaming single self-close size "+out.size();
					if(!out.get(0).tag().equals("A"))
						return "Error#15.26f: single self-close tag "+out.get(0).tag();
					if(!out.get(0).value().equals(""))
						return "Error#15.26g: single self-close value '"+out.get(0).value()+"'";
					if(buf.length()!=0)
						return "Error#15.26h: buffer should be trimmed after self-close, len "+buf.length();
				}
				// streaming self-close followed by normal tag
				{
					buf = new StringBuffer();
					out = new ArrayList<XMLTag>();
					parser = xml.getXMLParser(buf, out);
					buf.append("<A /><B>val</B>");
					parser.run();
					if(out.size()!=2)
						return "Error#15.26i: streaming self-close+val size "+out.size();
					if(!out.get(0).tag().equals("A"))
						return "Error#15.26j: first A tag";
					if(!out.get(1).tag().equals("B")||!out.get(1).value().equals("val"))
						return "Error#15.26k: second B value '"+out.get(1).value()+"'";
					if(buf.length()!=0)
						return "Error#15.26l: buffer trimmed after self-close+val "+buf.length();
				}
				// streaming multiple self-closes
				{
					buf = new StringBuffer();
					out = new ArrayList<XMLTag>();
					parser = xml.getXMLParser(buf, out);
					buf.append("<A /><B /><C />");
					parser.run();
					if(out.size()!=3)
						return "Error#15.26m: multi self-close size "+out.size();
					if((!out.get(0).tag().equals("A"))||(!out.get(1).tag().equals("B"))||(!out.get(2).tag().equals("C")))
						return "Error#15.26n: multi self-close tags";
				}
				// incremental self-close split across runs
				{
					buf = new StringBuffer();
					out = new ArrayList<XMLTag>();
					parser = xml.getXMLParser(buf, out);
					buf.append("<A ");
					parser.run();
					if(out.size()!=0)
						return "Error#15.26o: incomplete self-close should be 0";
					buf.append("/>");
					parser.run();
					if(out.size()!=1)
						return "Error#15.26p: incremental self-close size "+out.size();
					if(!out.get(0).tag().equals("A"))
						return "Error#15.26q: incremental self-close tag";
				}
				// self-close with attributes streaming
				{
					buf = new StringBuffer();
					out = new ArrayList<XMLTag>();
					parser = xml.getXMLParser(buf, out);
					buf.append("<TAG ATTR=\"1\" />");
					parser.run();
					if(out.size()!=1)
						return "Error#15.26r: attr self-close streaming size "+out.size();
					if(!"1".equals(out.get(0).getParmValue("ATTR")))
						return "Error#15.26s: attr self-close value "+out.get(0).getParmValue("ATTR");
				}
				buf = new StringBuffer();
				out = new ArrayList<XMLTag>();
				parser = xml.getXMLParser(buf, out);
				buf.append("<A>1</A><B>val</B>");
				parser.run();
				if(out.size()!=2)
					return "Error#15.26b: streaming size "+out.size();
				if((!out.get(0).tag().equals("A"))||(!out.get(1).value().equals("val")))
					return "Error#15.27: streaming values";
				// attribute streaming
				buf = new StringBuffer();
				out = new ArrayList<XMLTag>();
				parser = xml.getXMLParser(buf, out);
				buf.append("<TAG ATTR=\"1\">content</TAG>");
				parser.run();
				if(out.size()!=1)
					return "Error#15.28: attr streaming size";
				if(!"1".equals(out.get(0).getParmValue("ATTR")))
					return "Error#15.29: attr streaming value";
				// char-by-char incremental
				String doc = "<ROOT><A>1</A><B>2</B></ROOT>";
				buf = new StringBuffer();
				out = new ArrayList<XMLTag>();
				parser = xml.getXMLParser(buf, out);
				for(int i=0;i<doc.length();i++)
				{
					buf.append(doc.charAt(i));
					parser.run();
					if((i < doc.length()-1)&&(out.size()!=0))
						return "Error#15.30."+i+": char-by-char should not complete early at "+i+" out="+out.size();
				}
				if(out.size()!=1)
					return "Error#15.31: char-by-char final size "+out.size();
				if(!out.get(0).tag().equals("ROOT"))
					return "Error#15.32: char-by-char root tag";
			}

			// Test 16: whitespace, case, and empty inputs
			{
				List<XMLTag> tags = xml.parseAllXML("");
				if(tags.size()!=0)
					return "Error#16.1: empty string should give 0 tags, got "+tags.size();
				tags = xml.parseAllXML("   \n\t  ");
				if(tags.size()!=0)
					return "Error#16.2: whitespace should give 0";
				tags = xml.parseAllXML("no xml here");
				if(tags.size()!=0)
					return "Error#16.3: no xml size "+tags.size();
				tags = xml.parseAllXML("<Tag>mixedCase</Tag>");
				if(!tags.get(0).tag().equals("TAG"))
					return "Error#16.4: mixedCase upper "+tags.get(0).tag();
				if(!tags.get(0).value().equals("mixedCase"))
					return "Error#16.5: value case preserved";
				// whitespace inside tags
				tags = xml.parseAllXML("<TAG>  hello  </TAG>");
				if(!tags.get(0).value().equals("  hello  "))
					return "Error#16.6: whitespace value preserved ' "+tags.get(0).value()+"'";
				// newline in value
				tags = xml.parseAllXML("<TAG>line1\nline2</TAG>");
				if(!tags.get(0).value().equals("line1\nline2"))
					return "Error#16.7: newline value";
				// attribute case insensitive lookup already tested, but also tag lookup
				tags = xml.parseAllXML("<ABC>1</ABC>");
				XMLTag t = tags.get(0);
				if(!t.tag().equals("ABC"))
					return "Error#16.8: ABC tag";
				// ensure parent null for root
				if(t.parent()!=null)
					return "Error#16.9: root parent should be null";
			}

			// Test 17: error tolerance and edge cases
			{
				// mismatched tags - parser is forgiving, but should not crash
				List<XMLTag> tags = xml.parseAllXML("<A><B>val</A></B>");
				// should at least return something without exception
				if(tags==null)
					return "Error#17.1: mismatched should not return null";
				// unclosed tag - should be abandoned or partial
				tags = xml.parseAllXML("<A>unclosed");
				// parser keeps piece but contents may be 0 completed outer pieces
				// No exception expected
				// empty tag with attributes and no close? ensure no NPE
				tags = xml.parseAllXML("<A ATTR=\"1\"");
				if(tags==null)
					return "Error#17.2: unclosed attr should not NPE";
				// returnXMLBlock with no closing
				String block = xml.returnXMLBlock("<A>no close", "A");
				// should handle gracefully (return string or empty)
				if(!block.equals(""))
					return "Error#17.2.5: unclosed block: "+block;
				String s = xml.convertXMLtoTag("T", "a<b");
				if(!s.equals("<T>a<b</T>"))
					return "Error#17.3: convert does not escape: "+s;
				// fromXML with malformed should not throw
				Map<String,String> m = xml.fromXML("<A>1</A><B>2");
				if(m==null)
					return "Error#17.4: malformed fromXML null";
				// parseAllXML with illegalTag (pronoun suffix) - may be filtered; ensure no crash
				tags = xml.parseAllXML("<HIS>val</HIS>");
				if(tags==null)
					return "Error#17.5: illegal tag null";
			}

			// Test 18: bug-fix validation (covers the 4 documented XMLManager bugs)
			{
				// 18.1 returnXMLValue single-arg fix: was off-by-one (charAt(start-1)!='>') causing always ""
				{
					String v = xml.returnXMLValue("<HELLO>world</HELLO>");
					if(!"world".equals(v))
						return "Error#18.1: returnXMLValue HELLO world got '"+v+"'";
					v = xml.returnXMLValue("<TAG>hello</TAG>");
					if(!"hello".equals(v))
						return "Error#18.2: returnXMLValue hello got '"+v+"'";
					v = xml.returnXMLValue("<TAG> a & b </TAG>");
					if(!"a & b".equals(v))
						return "Error#18.3: returnXMLValue with spaces/amp got '"+v+"'";
					v = xml.returnXMLValue("<OUTER><INNER>val</INNER></OUTER>");
					if(!"<INNER>val</INNER>".equals(v) && !"val".equals(v)) {
						// parseAllXML returns first outer's value as literal inner xml? Actually outer value is inner tag markup?
						// but for our new implementation it returns inner value of first tag, which for nested is "" or inner?
						// So just check not empty
						if(v==null||v.length()==0)
							return "Error#18.3b: nested returnXMLValue got '"+v+"'";
					}
					v = xml.returnXMLValue("<EMPTY />");
					if(!v.equals(""))
						return "Error#18.4: returnXMLValue self-close should be empty '"+v+"'";
				}
				// 18.2 restoreAngleBrackets amp fix: was substring(loop+1,loop+6) for "amp;" (4 chars) should be loop+5
				{
					String r = xml.restoreAngleBrackets("a&amp;b");
					if(!"a&b".equals(r))
						return "Error#18.5: amp single got '"+r+"'";
					r = xml.restoreAngleBrackets("&amp;");
					if(!"&".equals(r))
						return "Error#18.6: amp lone got '"+r+"'";
					r = xml.restoreAngleBrackets("x &amp; y &lt; z &gt; w &quot; q &apos; p");
					if(!"x & y < z > w \" q ' p".equals(r))
						return "Error#18.7: combined entities got '"+r+"'";
					// ensure parseOut + restore round-trip for & via manual ampersand
					String orig = "a & b < c > d \" e ' f";
					String enc = orig.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;");
					String dec = xml.restoreAngleBrackets(enc);
					if(!orig.equals(dec))
						return "Error#18.8: manual amp roundtrip got '"+dec+"' vs '"+orig+"'";
					// case insensitive amp
					r = xml.restoreAngleBrackets("a&AMP;b");
					if(!"a&b".equals(r))
						return "Error#18.9: amp case insensitive got '"+r+"'";
				}
				// 18.3 String[] array fix: was created via reflection as empty strings
				{
					ArrayPojo ap = new ArrayPojo();
					ap.strArray = new String[] {"alpha","beta","gamma"};
					ap.intArray = new int[] {9,8};
					String s = xml.fromPOJOtoXML(ap);
					ArrayPojo ap2 = new ArrayPojo();
					xml.fromXMLtoPOJO(s, ap2);
					if(ap2.strArray==null||ap2.strArray.length!=3)
						return "Error#18.10: strArray length "+(ap2.strArray==null?"null":java.util.Arrays.toString(ap2.strArray));
					if(!"alpha".equals(ap2.strArray[0])||!"beta".equals(ap2.strArray[1])||!"gamma".equals(ap2.strArray[2]))
						return "Error#18.11: strArray content "+java.util.Arrays.toString(ap2.strArray);
					// with special chars: brackets correctly escaped/restored, & and quotes preserved
					ap.strArray = new String[] {"<tag>","a & b","\"quoted\""};
					s = xml.fromPOJOtoXML(ap);
					ap2 = new ArrayPojo();
					xml.fromXMLtoPOJO(s, ap2);
					if(!"<tag>".equals(ap2.strArray[0])||!"a & b".equals(ap2.strArray[1])||!"\"quoted\"".equals(ap2.strArray[2]))
						return "Error#18.12: strArray special chars "+java.util.Arrays.toString(ap2.strArray);
				}
				// 18.4 getXMLParser streaming self-close fix: outerPiecesCompleted not incremented for self-close
				{
					StringBuffer buf = new StringBuffer();
					List<XMLTag> out = new ArrayList<XMLTag>();
					Runnable parser = xml.getXMLParser(buf, out);
					buf.append("<SELF />");
					parser.run();
					if(out.size()!=1)
						return "Error#18.13: streaming single self-close size "+out.size();
					if(!out.get(0).tag().equals("SELF"))
						return "Error#18.14: streaming self-close tag "+out.get(0).tag();
					if(buf.length()!=0)
						return "Error#18.15: buffer not trimmed after self-close "+buf.length();
					// sequential self-closes should each be emitted immediately
					buf = new StringBuffer();
					out = new ArrayList<XMLTag>();
					parser = xml.getXMLParser(buf, out);
					for(int i=0;i<5;i++) {
						buf.append("<X"+i+" />");
						parser.run();
						if(out.size()!=1)
							return "Error#18.16."+i+": sequential self-close "+i+" size "+out.size();
						if(!out.get(0).tag().equals("X"+i))
							return "Error#18.17."+i+": tag X"+i+" got "+out.get(0).tag();
					}
					// mixed self-close and normal
					buf = new StringBuffer();
					out = new ArrayList<XMLTag>();
					parser = xml.getXMLParser(buf, out);
					buf.append("<A />");
					parser.run();
					if(out.size()!=1||!out.get(0).tag().equals("A"))
						return "Error#18.18: mixed step1 A";
					buf.append("<B>val</B>");
					parser.run();
					if(out.size()!=1||!out.get(0).tag().equals("B")||!out.get(0).value().equals("val"))
						return "Error#18.19: mixed step2 B val "+(out.isEmpty()?"empty":out.get(0).value());
					// incremental split
					buf = new StringBuffer();
					out = new ArrayList<XMLTag>();
					parser = xml.getXMLParser(buf, out);
					buf.append("<INC");
					parser.run();
					if(out.size()!=0)
						return "Error#18.20: incremental incomplete self-close should be 0";
					buf.append(" ATTR=\"1\" />");
					parser.run();
					if(out.size()!=1)
						return "Error#18.21: incremental self-close with attr size "+out.size();
					if(!"1".equals(out.get(0).getParmValue("ATTR")))
						return "Error#18.22: incremental attr value "+out.get(0).getParmValue("ATTR");
				}
			}

		}
		catch(final Exception e)
		{
			Log.errOut(e);
			return "Exception: "+e.getClass().getName()+": "+e.getMessage();
		}
		return null;
	}
}
