package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.MiniJSON.JSONObject;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.MiniTSON.TSONObject;
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
public class MiniJSONTest extends StdTest
{
	@Override
	public String ID()
	{
		return "MiniJSONTest";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "core"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final MiniJSON json = new MiniJSON();
		try
		{
			// Test 1: parse true/false/null via parse()
			Object result = json.parse("true");
			if(!(result instanceof Boolean) || !((Boolean)result).booleanValue())
				return "Error#1.1: parse true failed";
			result = json.parse("false");
			if(!(result instanceof Boolean) || ((Boolean)result).booleanValue())
				return "Error#1.2: parse false failed";
			result = json.parse("null");
			if(result != MiniJSON.NULL)
				return "Error#1.3: parse null failed";

			// Test 2: whitespace around literals
			result = json.parse("  true  ");
			if(!(result instanceof Boolean) || !((Boolean)result).booleanValue())
				return "Error#2.1: whitespace true failed";
			result = json.parse("\t\n null \r\n");
			if(result != MiniJSON.NULL)
				return "Error#2.2: whitespace null failed";
			result = json.parse("  \"hello\"  ");
			if(!(result instanceof String) || !result.equals("hello"))
				return "Error#2.3: whitespace string failed";

			// Test 3: simple strings
			result = json.parse("\"\"");
			if(!(result instanceof String) || !((String)result).equals(""))
				return "Error#3.1: empty string failed";
			result = json.parse("\"hello\"");
			if(!(result instanceof String) || !result.equals("hello"))
				return "Error#3.2: simple string failed";
			result = json.parse("\"hello world\"");
			if(!(result instanceof String) || !result.equals("hello world"))
				return "Error#3.3: string with space failed";

			// Test 4: string escapes
			result = json.parse("\"\\\"\"");
			if(!(result instanceof String) || !result.equals("\""))
				return "Error#4.1: escaped quote failed: "+result;
			result = json.parse("\"\\\\\"");
			if(!(result instanceof String) || !result.equals("\\"))
				return "Error#4.2: escaped backslash failed";
			result = json.parse("\"\\/\"");
			if(!(result instanceof String) || !result.equals("/"))
				return "Error#4.3: escaped slash failed";
			result = json.parse("\"\\b\"");
			if(!(result instanceof String) || !result.equals("\b"))
				return "Error#4.4: escaped b failed";
			result = json.parse("\"\\f\"");
			if(!(result instanceof String) || !result.equals("\f"))
				return "Error#4.5: escaped f failed";
			result = json.parse("\"\\n\"");
			if(!(result instanceof String) || !result.equals("\n"))
				return "Error#4.6: escaped n failed";
			result = json.parse("\"\\r\"");
			if(!(result instanceof String) || !result.equals("\r"))
				return "Error#4.7: escaped r failed";
			result = json.parse("\"\\t\"");
			if(!(result instanceof String) || !result.equals("\t"))
				return "Error#4.8: escaped t failed";

			// Test 5: unicode escapes
			result = json.parse("\"\\u0041\"");
			if(!(result instanceof String) || !result.equals("A"))
				return "Error#5.1: unicode \\u0041 failed: "+result;
			result = json.parse("\"\\u0061\"");
			if(!(result instanceof String) || !result.equals("a"))
				return "Error#5.2: unicode \\u0061 failed";
			result = json.parse("\"\\u0030\"");
			if(!(result instanceof String) || !result.equals("0"))
				return "Error#5.3: unicode \\u0030 failed";
			result = json.parse("\"\\u00e9\"");
			if(!(result instanceof String) || ((String)result).charAt(0) != 0xe9)
				return "Error#5.4: unicode \\u00e9 failed: "+((String)result).charAt(0);
			result = json.parse("\"hello\\u0020world\"");
			if(!(result instanceof String) || !result.equals("hello world"))
				return "Error#5.5: unicode in string failed";

			// Test 6: complex escaped string round-trip via toJSONString
			String original = "hello\nworld\t\"quote\"\\backslash";
			String jsonStr = "\""+MiniJSON.toJSONString(original)+"\"";
			result = json.parse(jsonStr);
			if(!(result instanceof String) || !result.equals(original))
				return "Error#6.1: roundtrip escaped string failed: expected "+original+" got "+result;
			original = "\b\f\n\r\t";
			jsonStr = "\""+MiniJSON.toJSONString(original)+"\"";
			result = json.parse(jsonStr);
			if(!(result instanceof String) || !result.equals(original))
				return "Error#6.2: roundtrip control chars failed";

			// Test 7: integer numbers
			result = json.parse("0");
			if(!(result instanceof Long) || ((Long)result).longValue()!=0)
				return "Error#7.1: 0 failed";
			result = json.parse("123");
			if(!(result instanceof Long) || ((Long)result).longValue()!=123)
				return "Error#7.2: 123 failed";
			result = json.parse("-456");
			if(!(result instanceof Long) || ((Long)result).longValue()!=-456)
				return "Error#7.3: -456 failed";
			result = json.parse("9223372036854775807");
			if(!(result instanceof Long) || ((Long)result).longValue()!=Long.MAX_VALUE)
				return "Error#7.4: max long failed";
			result = json.parse("-9223372036854775808");
			if(!(result instanceof Long) || ((Long)result).longValue()!=Long.MIN_VALUE)
				return "Error#7.5: min long failed";

			// Test 8: floating point numbers
			result = json.parse("3.14");
			if(!(result instanceof Double) || Math.abs(((Double)result).doubleValue()-3.14)>0.0001)
				return "Error#8.1: 3.14 failed";
			result = json.parse("-0.5");
			if(!(result instanceof Double) || Math.abs(((Double)result).doubleValue()+0.5)>0.0001)
				return "Error#8.2: -0.5 failed";
			result = json.parse("0.0");
			if(!(result instanceof Double) || Math.abs(((Double)result).doubleValue())>0.0001)
				return "Error#8.3: 0.0 failed";
			result = json.parse("123.456");
			if(!(result instanceof Double))
				return "Error#8.4: 123.456 not double";

			// Test 9: exponent numbers
			result = json.parse("1e10");
			if(!(result instanceof Double) || Math.abs(((Double)result).doubleValue()-1e10)>1)
				return "Error#9.1: 1e10 failed";
			result = json.parse("1E10");
			if(!(result instanceof Double))
				return "Error#9.2: 1E10 failed";
			result = json.parse("1e+10");
			if(!(result instanceof Double))
				return "Error#9.3: 1e+10 failed";
			result = json.parse("1e-10");
			if(!(result instanceof Double) || Math.abs(((Double)result).doubleValue()-1e-10)>1e-15)
				return "Error#9.4: 1e-10 failed";
			result = json.parse("0e10");
			if(!(result instanceof Double) || Math.abs(((Double)result).doubleValue())>0.0001)
				return "Error#9.5: 0e10 failed";
			result = json.parse("1.5E+2");
			if(!(result instanceof Double) || Math.abs(((Double)result).doubleValue()-150.0)>0.001)
				return "Error#9.6: 1.5E+2 failed";
			result = json.parse("-1e-5");
			if(!(result instanceof Double) || Math.abs(((Double)result).doubleValue()+1e-5)>1e-10)
				return "Error#9.7: -1e-5 failed";

			// Test 10: empty array and object via parse
			result = json.parse("[]");
			if(!(result instanceof Object[]) || ((Object[])result).length!=0)
				return "Error#10.1: empty array failed";
			result = json.parse("{}");
			if(!(result instanceof JSONObject) || ((JSONObject)result).size()!=0)
				return "Error#10.2: empty object failed";

			// Test 11: simple arrays
			result = json.parse("[1,2,3]");
			if(!(result instanceof Object[]) || ((Object[])result).length!=3)
				return "Error#11.1: [1,2,3] length failed";
			Object[] arr = (Object[])result;
			if(!arr[0].equals(Long.valueOf(1)) || !arr[1].equals(Long.valueOf(2)) || !arr[2].equals(Long.valueOf(3)))
				return "Error#11.2: [1,2,3] values failed";
			result = json.parse("[\"a\",\"b\",\"c\"]");
			arr = (Object[])result;
			if(!arr[0].equals("a") || !arr[1].equals("b"))
				return "Error#11.3: [a,b,c] failed";
			result = json.parse("[true,false,null]");
			arr = (Object[])result;
			if(!arr[0].equals(Boolean.TRUE) || !arr[1].equals(Boolean.FALSE) || arr[2]!=MiniJSON.NULL)
				return "Error#11.4: [true,false,null] failed";

			// Test 12: nested arrays
			result = json.parse("[[1,2],[3,4]]");
			arr = (Object[])result;
			if(!(arr[0] instanceof Object[]) || !(arr[1] instanceof Object[]))
				return "Error#12.1: nested arrays type failed";
			Object[] inner = (Object[])arr[0];
			if(inner.length!=2 || !inner[0].equals(Long.valueOf(1)))
				return "Error#12.2: nested arrays values failed";
			result = json.parse("[{\"a\":1},{\"b\":2}]");
			arr = (Object[])result;
			if(!(arr[0] instanceof JSONObject) || !(arr[1] instanceof JSONObject))
				return "Error#12.3: array of objects failed";

			// Test 13: whitespace in arrays
			result = json.parse("  [  1 ,  2 ,  3  ]  ");
			arr = (Object[])result;
			if(arr.length!=3)
				return "Error#13.1: whitespace array failed";
			result = json.parse("[1,2,3]");
			// also test with newlines and tabs
			result = json.parse("[\n\t1,\n2\n]");
			arr = (Object[])result;
			if(arr.length!=2)
				return "Error#13.2: array with newlines failed";

			// Test 14: simple objects via parse
			result = json.parse("{\"a\":1}");
			if(!(result instanceof JSONObject))
				return "Error#14.1: {a:1} not object";
			JSONObject obj = (JSONObject)result;
			if(!obj.containsKey("a") || !obj.get("a").equals(Long.valueOf(1)))
				return "Error#14.2: {a:1} value failed";
			result = json.parse("{\"a\":1,\"b\":2,\"c\":3}");
			obj = (JSONObject)result;
			if(obj.size()!=3 || !obj.get("b").equals(Long.valueOf(2)))
				return "Error#14.3: multi-key object failed";

			// Test 15: nested objects
			result = json.parse("{\"a\":{\"b\":2}}");
			obj = (JSONObject)result;
			if(!(obj.get("a") instanceof JSONObject))
				return "Error#15.1: nested object type failed";
			JSONObject innerObj = (JSONObject)obj.get("a");
			if(!innerObj.get("b").equals(Long.valueOf(2)))
				return "Error#15.2: nested object value failed";
			result = json.parse("{\"arr\":[1,2,3],\"obj\":{\"x\":1}}");
			obj = (JSONObject)result;
			if(!(obj.get("arr") instanceof Object[]) || !(obj.get("obj") instanceof JSONObject))
				return "Error#15.3: object with array and object failed";

			// Test 16: whitespace in objects
			result = json.parse("  {  \"a\"  :  1 ,  \"b\"  :  2  }  ");
			obj = (JSONObject)result;
			if(obj.size()!=2 || !obj.get("a").equals(Long.valueOf(1)))
				return "Error#16.1: whitespace object failed";
			result = json.parse("{\n\t\"a\":\n1\n}");
			obj = (JSONObject)result;
			if(!obj.get("a").equals(Long.valueOf(1)))
				return "Error#16.2: object with newlines failed";

			// Test 17: mixed types in object
			result = json.parse("{\"s\":\"hello\",\"n\":42,\"d\":3.14,\"b\":true,\"nul\":null,\"arr\":[1,2],\"obj\":{\"x\":1}}");
			obj = (JSONObject)result;
			if(!obj.get("s").equals("hello"))
				return "Error#17.1: string value failed";
			if(!obj.get("n").equals(Long.valueOf(42)))
				return "Error#17.2: long value failed";
			if(!(obj.get("d") instanceof Double))
				return "Error#17.3: double value failed";
			if(!obj.get("b").equals(Boolean.TRUE))
				return "Error#17.4: boolean value failed";
			if(obj.get("nul")!=MiniJSON.NULL)
				return "Error#17.5: null value failed";
			if(!(obj.get("arr") instanceof Object[]))
				return "Error#17.6: array value failed";
			if(!(obj.get("obj") instanceof JSONObject))
				return "Error#17.7: object value failed";

			// Test 18: duplicate keys (last wins)
			result = json.parse("{\"a\":1, \"a\":2}");
			obj = (JSONObject)result;
			if(!obj.get("a").equals(Long.valueOf(2)))
				return "Error#18.1: duplicate keys failed";
			if(obj.size()!=1)
				return "Error#18.2: duplicate keys size failed";

			// Test 19: escaped keys and values
			result = json.parse("{\"key\\\"with\\\"quotes\":\"value\\nwith\\nnewlines\"}");
			obj = (JSONObject)result;
			if(!obj.containsKey("key\"with\"quotes"))
				return "Error#19.1: escaped key failed";
			if(!obj.get("key\"with\"quotes").equals("value\nwith\nnewlines"))
				return "Error#19.2: escaped value failed";
			result = json.parse("{\"unicode\":\"\\u0041\"}");
			obj = (JSONObject)result;
			if(!obj.get("unicode").equals("A"))
				return "Error#19.3: unicode value failed";

			// Test 20: parseObject()
			JSONObject o = json.parseObject("{}");
			if(o.size()!=0)
				return "Error#20.1: parseObject empty failed";
			o = json.parseObject("{\"x\": [1,2]}");
			if(!(o.get("x") instanceof Object[]))
				return "Error#20.2: parseObject with array failed";
			o = json.parseObject("  {\"a\":1}  ");
			if(!o.get("a").equals(Long.valueOf(1)))
				return "Error#20.3: parseObject whitespace failed";
			// parseObject should fail for non-objects
			try{ json.parseObject("[1,2]"); return "Error#20.4: parseObject should fail for array"; } catch(final MJSONException e){}
			try{ json.parseObject("\"hello\""); return "Error#20.5: parseObject should fail for string"; } catch(final MJSONException e){}
			try{ json.parseObject("123"); return "Error#20.6: parseObject should fail for number"; } catch(final MJSONException e){}

			// Test 21: parseArray()
			Object[] a = json.parseArray("[]");
			if(a.length!=0)
				return "Error#21.1: parseArray empty failed";
			a = json.parseArray("[1,2,3]");
			if(a.length!=3 || !a[0].equals(Long.valueOf(1)))
				return "Error#21.2: parseArray [1,2,3] failed";
			a = json.parseArray("[true,false,null]");
			if(a.length!=3 || a[2]!=MiniJSON.NULL)
				return "Error#21.3: parseArray with literals failed";
			a = json.parseArray("[\"a\",123,{\"b\":2}]");
			if(!(a[2] instanceof JSONObject))
				return "Error#21.4: parseArray mixed types failed";
			try{ json.parseArray("{}"); return "Error#21.5: parseArray should fail for object"; } catch(final MJSONException e){}
			try{ json.parseArray("\"hello\""); return "Error#21.6: parseArray should fail for string"; } catch(final MJSONException e){}

			// Test 22: getChecked methods
			o = json.parseObject("{\"s\":\"hello\",\"n\":42,\"d\":3.14,\"b\":true,\"arr\":[1,2],\"obj\":{\"x\":1},\"nul\":null}");
			if(!o.getCheckedString("s").equals("hello"))
				return "Error#22.1: getCheckedString failed";
			if(!o.getCheckedLong("n").equals(Long.valueOf(42)))
				return "Error#22.2: getCheckedLong failed";
			if(Math.abs(o.getCheckedDouble("d").doubleValue()-3.14)>0.001)
				return "Error#22.3: getCheckedDouble failed";
			if(!o.getCheckedBoolean("b").booleanValue())
				return "Error#22.4: getCheckedBoolean failed";
			if(o.getCheckedArray("arr").length!=2)
				return "Error#22.5: getCheckedArray failed";
			if(!o.getCheckedJSONObject("obj").get("x").equals(Long.valueOf(1)))
				return "Error#22.6: getCheckedJSONObject failed";
			if(!o.isCheckedNULL("nul"))
				return "Error#22.7: isCheckedNULL failed";
			if(Math.abs(o.getCheckedNumber("n")-42.0)>0.001)
				return "Error#22.8: getCheckedNumber long failed";
			if(Math.abs(o.getCheckedNumber("d")-3.14)>0.001)
				return "Error#22.9: getCheckedNumber double failed";
			// error cases
			try{ o.getCheckedString("missing"); return "Error#22.10: getCheckedString should fail for missing"; } catch(final MJSONException e){}
			try{ o.getCheckedString("n"); return "Error#22.11: getCheckedString should fail for wrong type"; } catch(final MJSONException e){}
			try{ o.getCheckedLong("s"); return "Error#22.12: getCheckedLong should fail for wrong type"; } catch(final MJSONException e){}
			try{ o.getCheckedDouble("n"); return "Error#22.13: getCheckedDouble should fail for wrong type"; } catch(final MJSONException e){}
			try{ o.getCheckedBoolean("s"); return "Error#22.14: getCheckedBoolean should fail for wrong type"; } catch(final MJSONException e){}
			try{ o.getCheckedArray("s"); return "Error#22.15: getCheckedArray should fail for wrong type"; } catch(final MJSONException e){}
			try{ o.getCheckedJSONObject("s"); return "Error#22.16: getCheckedJSONObject should fail for wrong type"; } catch(final MJSONException e){}
			try{ o.isCheckedNULL("missing"); return "Error#22.17: isCheckedNULL should fail for missing"; } catch(final MJSONException e){}

			// Test 23: JSONObject toString and round-trip
			o = json.parseObject("{\"a\":1,\"b\":\"hello\",\"c\":true,\"d\":null,\"e\":[1,2,3],\"f\":{\"x\":1}}");
			String s = o.toString();
			if(!s.contains("\"a\":1") || !s.contains("\"b\":\"hello\""))
				return "Error#23.1: toString content failed: "+s;
			JSONObject o2 = json.parseObject(s);
			if(!o2.get("a").equals(Long.valueOf(1)) || !o2.get("b").equals("hello"))
				return "Error#23.2: toString round-trip failed";
			if(!s.equals(o2.toString()))
				return "Error#23.3: toString stable round-trip failed";
			// empty object toString
			o = json.parseObject("{}");
			if(!o.toString().equals("{}"))
				return "Error#23.4: empty object toString failed: "+o.toString();
			// array toString via JSONObject
			o = json.parseObject("{\"arr\":[1,2,3]}");
			s = o.toString();
			if(!s.contains("[1,2,3]"))
				return "Error#23.5: array toString failed: "+s;

			// Test 24: appendJSONValue via toString (implicit)
			JSONObject tmp = new JSONObject();
			tmp.put("str","a\"b");
			s = tmp.toString();
			if(!s.contains("\\\""))
				return "Error#24.1: toString escaped quote failed: "+s;
			tmp = new JSONObject();
			tmp.put("arr", new Object[]{Long.valueOf(1), "hello", Boolean.TRUE, MiniJSON.NULL});
			s = tmp.toString();
			// parse back
			o = json.parseObject(s);
			Object[] arr2 = o.getCheckedArray("arr");
			if(arr2.length!=4 || !arr2[1].equals("hello") || arr2[3]!=MiniJSON.NULL)
				return "Error#24.2: appendJSONValue mixed array failed";

			// Test 25: copyOf and jsonDeepCopy
			JSONObject orig = json.parseObject("{\"a\":1,\"arr\":[1,2],\"obj\":{\"x\":\"hi\"}}");
			JSONObject copy = orig.copyOf();
			if(copy.size()!=orig.size() || !copy.get("a").equals(Long.valueOf(1)))
				return "Error#25.1: copyOf size/value failed";
			copy.put("a",Long.valueOf(999));
			if(orig.get("a").equals(Long.valueOf(999)))
				return "Error#25.2: copyOf shallow copy of primitive failed";
			// deep copy of array
			((Object[])copy.get("arr"))[0]=Long.valueOf(999);
			if(((Object[])orig.get("arr"))[0].equals(Long.valueOf(999)))
				return "Error#25.3: copyOf deep array should not affect original";
			// deep copy of nested object
			((JSONObject)copy.get("obj")).put("x","bye");
			if(((JSONObject)orig.get("obj")).get("x").equals("bye"))
				return "Error#25.4: copyOf deep object should not affect original";
			// jsonDeepCopy for Object[]
			Object[] srcArr = new Object[]{Long.valueOf(1), Long.valueOf(2)};
			Object[] deepCopy = (Object[])orig.jsonDeepCopy(srcArr);
			if(deepCopy==srcArr || deepCopy[0]!=srcArr[0])
				return "Error#25.5: jsonDeepCopy array failed";
			deepCopy[0]=Long.valueOf(9);
			if(srcArr[0].equals(Long.valueOf(9)))
				return "Error#25.6: jsonDeepCopy should be independent";

			// Test 26: isASCIIViewable and toJSONString
			if(!MiniJSON.isASCIIViewable('A'))
				return "Error#26.1: isASCII A failed";
			if(!MiniJSON.isASCIIViewable(' '))
				return "Error#26.2: isASCII space failed";
			if(MiniJSON.isASCIIViewable('\n'))
				return "Error#26.3: isASCII newline should be false";
			if(MiniJSON.isASCIIViewable((char)31))
				return "Error#26.4: isASCII 31 should be false";
			if(MiniJSON.isASCIIViewable((char)127))
				return "Error#26.5: isASCII 127 should be false";
			if(!MiniJSON.isASCIIViewable('~'))
				return "Error#26.6: isASCII ~ failed";
			if(!MiniJSON.toJSONString("hello").equals("hello"))
				return "Error#26.7: toJSONString plain failed";
			if(!MiniJSON.toJSONString("a\"b").equals("a\\\"b"))
				return "Error#26.8: toJSONString quote failed: "+MiniJSON.toJSONString("a\"b");
			if(!MiniJSON.toJSONString("a\\b").equals("a\\\\b"))
				return "Error#26.9: toJSONString backslash failed";
			if(!MiniJSON.toJSONString("a\nb").equals("a\\nb"))
				return "Error#26.10: toJSONString newline failed";
			if(!MiniJSON.toJSONString("a\tb").equals("a\\tb"))
				return "Error#26.11: toJSONString tab failed";

			// Test 27: toJSONString byte[] and JSONObject put byte[]
			tmp = new JSONObject();
			tmp.put("bytes","test".getBytes());
			// Actually JSONObject appendJSONValue handles byte[] as string? check
			// put byte[] and ensure toString doesn't crash
			s = tmp.toString();
			if(!s.contains("test"))
				return "Error#27.1: byte[] handling failed: "+s;

			// Test 28: error cases should throw MJSONException
			try{ json.parse(""); return "Error#28.1: empty should throw"; } catch(final MJSONException e){}
			try{ json.parse("{"); return "Error#28.2: incomplete object should throw"; } catch(final MJSONException e){}
			try{ json.parse("["); return "Error#28.3: incomplete array should throw"; } catch(final MJSONException e){}
			try{ json.parse("\"unclosed"); return "Error#28.4: unclosed string should throw"; } catch(final MJSONException e){}
			try{ json.parse("{\"a\":}"); return "Error#28.5: missing value should throw"; } catch(final MJSONException e){}
			try{ json.parse("[1,,2]"); return "Error#28.6: double comma should throw"; } catch(final MJSONException e){}
			try{ json.parse("tru"); return "Error#28.7: invalid true should throw"; } catch(final MJSONException e){}
			try{ json.parse("nul"); return "Error#28.8: invalid null should throw"; } catch(final MJSONException e){}
			try{ json.parse("{\"a\":1} extra"); return "Error#28.9: extra chars should throw"; } catch(final MJSONException e){}
			try{ json.parse("{\"a\":undefined}"); return "Error#28.10: undefined should throw"; } catch(final MJSONException e){}
			try{ json.parse("{'a':1}"); return "Error#28.11: single quotes should throw"; } catch(final MJSONException e){}

			// Test 29: invalid escapes
			try{ json.parse("\"\\z\""); return "Error#29.1: \\z should throw"; } catch(final MJSONException e){}
			try{ json.parse("\"\\u00G1\""); return "Error#29.2: invalid hex should throw"; } catch(final MJSONException e){}
			try{ json.parse("\"\\u00\""); return "Error#29.3: short unicode should throw"; } catch(final MJSONException e){}
			try{ json.parse("\"\\u\""); return "Error#29.4: bare \\u should throw"; } catch(final MJSONException e){}

			// Test 30: invalid numbers
			try{ json.parse("012"); return "Error#30.1: leading zero 012 should throw"; } catch(final MJSONException e){}
			try{ json.parse("01"); return "Error#30.2: 01 should throw"; } catch(final MJSONException e){}
			try{ json.parse("-012"); return "Error#30.3: -012 should throw"; } catch(final MJSONException e){}
			try{ json.parse("1e0"); return "Error#30.4: 1e0 should throw (MiniJSON requires non-zero exponent)"; } catch(final MJSONException e){}
			try{ json.parse("9999999999999999999"); return "Error#30.5: overflow long should throw"; } catch(final MJSONException e){}
			// valid zero and exponent zero with prefix 0
			result = json.parse("0e10");
			if(!(result instanceof Double))
				return "Error#30.6: 0e10 should be valid double";

			// Test 31: extra characters after array/object
			try{ json.parse("[1,2] trailing"); return "Error#31.1: trailing after array should throw"; } catch(final MJSONException e){}
			try{ json.parse("{\"a\":1} 123"); return "Error#31.2: trailing number after object should throw"; } catch(final MJSONException e){}

			// Test 32: complex nested structure
			String complex = "{\"name\":\"test\",\"numbers\":[1,2,3],\"nested\":{\"a\":{\"b\":2}},\"mixed\":[\"a\",123,true,null]}";
			result = json.parse(complex);
			if(!(result instanceof JSONObject))
				return "Error#32.1: complex parse failed";
			obj = (JSONObject)result;
			if(!obj.get("name").equals("test"))
				return "Error#32.2: complex name failed";
			if(!(obj.get("numbers") instanceof Object[]) || ((Object[])obj.get("numbers")).length!=3)
				return "Error#32.3: complex numbers failed";
			if(!(obj.get("nested") instanceof JSONObject))
				return "Error#32.4: complex nested failed";
			if(!(obj.get("mixed") instanceof Object[]))
				return "Error#32.5: complex mixed failed";
			Object[] mixed = (Object[])obj.get("mixed");
			if(!mixed[0].equals("a") || !mixed[2].equals(Boolean.TRUE) || mixed[3]!=MiniJSON.NULL)
				return "Error#32.6: complex mixed values failed";

			// Test 33: object with all json types
			result = json.parse("{\"str\":\"hi\",\"num\":42,\"dbl\":3.14,\"bool\":false,\"nil\":null,\"arr\":[],\"obj\":{}}");
			obj = (JSONObject)result;
			if(obj.size()!=7)
				return "Error#33.1: all types size failed";
			if(!obj.get("str").equals("hi") || !obj.get("num").equals(Long.valueOf(42)))
				return "Error#33.2: all types values failed";

			// Test 34: array with all json types
			result = json.parse("[\"hi\",42,3.14,false,null,[],{}]");
			arr = (Object[])result;
			if(arr.length!=7)
				return "Error#34.1: array all types length failed";
			if(!(arr[6] instanceof JSONObject) || !(arr[5] instanceof Object[]))
				return "Error#34.2: array all types inner failed";

			// Test 35: deeply nested (but not hitting MAX_DEPTH)
			StringBuilder deep = new StringBuilder();
			deep.append("{\"a\":");
			for(int i=0;i<10;i++) deep.append("{\"a\":");
			deep.append("1");
			for(int i=0;i<10;i++) deep.append("}");
			deep.append("}");
			result = json.parse(deep.toString());
			if(!(result instanceof JSONObject))
				return "Error#35.1: deep nested failed";

			// Test 36: MAX_DEPTH should throw for very deep structure
			try{
				StringBuilder veryDeep = new StringBuilder();
				for(int i=0;i<600;i++) veryDeep.append("{\"a\":");
				veryDeep.append("1");
				for(int i=0;i<600;i++) veryDeep.append("}");
				json.parse(veryDeep.toString());
				return "Error#36.1: max depth should throw";
			}catch(final MJSONException e){
				if(!e.getMessage().contains("Maximum depth"))
					return "Error#36.2: max depth wrong message: "+e.getMessage();
			}

			// Test 37: parse with leading/trailing whitespace of all types
			result = json.parse(" \n\t {\"a\":1} \r\n ");
			if(!(result instanceof JSONObject))
				return "Error#37.1: whitespace object failed";
			result = json.parse(" \n [1,2] \t ");
			if(!(result instanceof Object[]))
				return "Error#37.2: whitespace array failed";

			// Test 38: string with all escapes combined
			result = json.parse("\"\\b\\f\\n\\r\\t\\\"\\\\\\/\"");
			if(!(result instanceof String))
				return "Error#38.1: combined escapes not string";
			String expected = "\b\f\n\r\t\"\\/";
			if(!result.equals(expected))
				return "Error#38.2: combined escapes value failed: got length "+((String)result).length();

			// Test 39: numeric edge - zero variations
			result = json.parse("0");
			if(!result.equals(Long.valueOf(0)))
				return "Error#39.1: 0 long failed";
			result = json.parse("-0");
			if(!result.equals(Long.valueOf(0)))
				return "Error#39.2: -0 should be 0";
			result = json.parse("0.0");
			if(!(result instanceof Double))
				return "Error#39.3: 0.0 should be double";

			// Test 40: createJSONObject()
			JSONObject created = json.createJSONObject();
			created.put("test", Long.valueOf(123));
			if(!created.get("test").equals(Long.valueOf(123)))
				return "Error#40.1: createJSONObject failed";
			if(!(created instanceof JSONObject))
				return "Error#40.2: createJSONObject type failed";

			// Test 41: JSONObject copy preserves insertion order (LinkedHashMap)
			o = json.parseObject("{\"z\":1,\"a\":2,\"m\":3}");
			Iterator<String> it = o.keySet().iterator();
			if(!it.next().equals("z") || !it.next().equals("a") || !it.next().equals("m"))
				return "Error#41.1: insertion order failed";

			// Test 42: toJSONString for non-viewable ascii should produce \\u escapes
			String nonView = ""+(char)0x01 + (char)0x1F + (char)0x7F + (char)0x80;
			String escaped = MiniJSON.toJSONString(nonView);
			if(!escaped.contains("\\u"))
				return "Error#42.1: toJSONString non-viewable should escape: "+escaped;

			// Test 43: parseArray whitespace and single element
			a = json.parseArray(" [  42  ] ");
			if(a.length!=1 || !a[0].equals(Long.valueOf(42)))
				return "Error#43.1: single element array whitespace failed";
			a = json.parseArray("[\"single\"]");
			if(!a[0].equals("single"))
				return "Error#43.2: single string array failed";

			// Test 44: parseObject with array values and nested objects roundtrip
			o = json.parseObject("{\"arr\":[{\"x\":1},{\"y\":2}],\"num\":123}");
			s = o.toString();
			o2 = json.parseObject(s);
			Object[] arrOrig = o.getCheckedArray("arr");
			Object[] arrParsed = o2.getCheckedArray("arr");
			if(arrParsed.length!=arrOrig.length)
				return "Error#44.1: nested array roundtrip length failed";
			if(!((JSONObject)arrParsed[0]).get("x").equals(Long.valueOf(1)))
				return "Error#44.2: nested array roundtrip value failed";

			// Test 45: boolean and null in objects
			o = json.parseObject("{\"t\":true,\"f\":false,\"n\":null}");
			if(!o.get("t").equals(Boolean.TRUE) || !o.get("f").equals(Boolean.FALSE) || o.get("n")!=MiniJSON.NULL)
				return "Error#45.1: bool/null in object failed";
			// ensure toString preserves them
			s = o.toString();
			if(!s.contains("true") || !s.contains("false") || !s.contains("null"))
				return "Error#45.2: bool/null toString failed: "+s;

			// ===== getJSONParser() streaming tests =====

			// Test 46: simple single-value streaming
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("true");
				parser.run();
				if(results.size()!=1 || !results.get(0).equals(Boolean.TRUE))
					return "Error#46.1: streaming true failed";
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("false");
				parser.run();
				if(results.size()!=1 || !results.get(0).equals(Boolean.FALSE))
					return "Error#46.2: streaming false failed";
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("null");
				parser.run();
				if(results.size()!=1 || results.get(0)!=MiniJSON.NULL)
					return "Error#46.3: streaming null failed";
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("\"hello\"");
				parser.run();
				if(results.size()!=1 || !results.get(0).equals("hello"))
					return "Error#46.4: streaming string failed";
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("123 ");
				parser.run();
				if(results.size()!=1 || !results.get(0).equals(Long.valueOf(123)))
					return "Error#46.5: streaming int failed";
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("3.14 ");
				parser.run();
				if(results.size()!=1 || !(results.get(0) instanceof Double))
					return "Error#46.6: streaming double failed";
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("[1,2,3]");
				parser.run();
				if(results.size()!=1 || !(results.get(0) instanceof Object[]))
					return "Error#46.7: streaming array failed";
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("{\"a\":1}");
				parser.run();
				if(results.size()!=1 || !(results.get(0) instanceof JSONObject))
					return "Error#46.8: streaming object failed";
			}

			// Test 47: multiple values in one buffer in one run
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("true false null \"hello\" 123 [1,2] {\"a\":1}");
				parser.run();
				if(results.size()!=7)
					return "Error#47.1: multiple values size failed: "+results.size();
				if(!results.get(0).equals(Boolean.TRUE))
					return "Error#47.2: multi true failed";
				if(!results.get(1).equals(Boolean.FALSE))
					return "Error#47.3: multi false failed";
				if(results.get(2)!=MiniJSON.NULL)
					return "Error#47.4: multi null failed";
				if(!results.get(3).equals("hello"))
					return "Error#47.5: multi string failed";
				if(!results.get(4).equals(Long.valueOf(123)))
					return "Error#47.6: multi long failed";
				if(!(results.get(5) instanceof Object[]) || ((Object[])results.get(5)).length!=2)
					return "Error#47.7: multi array failed";
				if(!(results.get(6) instanceof JSONObject))
					return "Error#47.8: multi object failed";
			}

			// Test 48: incremental string split across chunks
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("\"hel");
				parser.run();
				if(results.size()!=0)
					return "Error#48.1: incomplete string should yield 0";
				buf.append("lo\"");
				parser.run();
				if(results.size()!=1 || !results.get(0).equals("hello"))
					return "Error#48.2: incremental string failed: "+results;
				// split escape
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("\"hello\\");
				parser.run();
				if(results.size()!=0)
					return "Error#48.3: incomplete escape should yield 0";
				buf.append("nworld\"");
				parser.run();
				if(results.size()!=1 || !results.get(0).equals("hello\nworld"))
					return "Error#48.4: incremental escape failed";
				// split unicode
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("\"\\u00");
				parser.run();
				if(results.size()!=0)
					return "Error#48.5: incomplete unicode should yield 0";
				buf.append("41\"");
				parser.run();
				if(results.size()!=1 || !results.get(0).equals("A"))
					return "Error#48.6: incremental unicode failed";
			}

			// Test 49: incremental object split at boundaries
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("{\"a\":");
				parser.run();
				if(results.size()!=0)
					return "Error#49.1: incomplete object should yield 0";
				buf.append("1}");
				parser.run();
				if(results.size()!=1 || !((JSONObject)results.get(0)).get("a").equals(Long.valueOf(1)))
					return "Error#49.2: incremental object failed";
				// split between keys
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("{\"a\":1,");
				parser.run();
				if(results.size()!=0)
					return "Error#49.3: partial object with comma should yield 0";
				buf.append("\"b\":2}");
				parser.run();
				if(results.size()!=1 || ((JSONObject)results.get(0)).size()!=2)
					return "Error#49.4: incremental second key failed";
			}

			// Test 50: incremental array split
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("[1,");
				parser.run();
				if(results.size()!=0)
					return "Error#50.1: incomplete array should yield 0";
				buf.append("2,3]");
				parser.run();
				if(results.size()!=1 || ((Object[])results.get(0)).length!=3)
					return "Error#50.2: incremental array failed";
				// nested array incremental
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("[[1");
				parser.run();
				if(results.size()!=0)
					return "Error#50.3: nested partial should yield 0";
				buf.append(",2],[3,4]]");
				parser.run();
				if(results.size()!=1 || !(results.get(0) instanceof Object[]))
					return "Error#50.4: incremental nested array failed";
			}

			// Test 51: incremental number split
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("12");
				parser.run();
				// number may be parsed only when terminated by whitespace or delimiter?
				// In streaming, number completes only when non-digit char arrives, so 0 results expected until terminator
				// Feed terminator
				buf.append("3 ");
				parser.run();
				if(results.size()!=1 || !results.get(0).equals(Long.valueOf(123)))
					return "Error#51.1: incremental integer failed: "+results;
				// exponent split
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("1e");
				parser.run();
				if(results.size()!=0)
					return "Error#51.2: incomplete exponent should yield 0";
				buf.append("10 ");
				parser.run();
				if(results.size()!=1 || !(results.get(0) instanceof Double))
					return "Error#51.3: incremental exponent failed";
				// double split
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("3.1");
				parser.run();
				if(results.size()!=0)
					return "Error#51.4: incomplete double should yield 0";
				buf.append("4 ");
				parser.run();
				if(results.size()!=1 || Math.abs(((Double)results.get(0)).doubleValue()-3.14)>0.001)
					return "Error#51.5: incremental double failed";
			}

			// Test 52: incremental nested structure split
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("{\"arr\":[");
				parser.run();
				if(results.size()!=0)
					return "Error#52.1: nested start should yield 0";
				buf.append("{\"x\":1},");
				parser.run();
				if(results.size()!=0)
					return "Error#52.2: mid nested should yield 0";
				buf.append("{\"y\":2}]}");
				parser.run();
				if(results.size()!=1 || !(results.get(0) instanceof JSONObject))
					return "Error#52.3: incremental nested object failed";
				JSONObject o5 = (JSONObject)results.get(0);
				Object[] arr5 = (Object[])o5.get("arr");
				if(arr5.length!=2)
					return "Error#52.4: nested array length failed";
			}

			// Test 53: whitespace handling streaming
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("   \n\t  ");
				parser.run();
				if(results.size()!=0)
					return "Error#53.1: whitespace only should yield 0";
				buf.append("  { \"a\" : 1 }  ");
				parser.run();
				if(results.size()!=1 || !(results.get(0) instanceof JSONObject))
					return "Error#53.2: whitespace object failed";
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("  [ 1 , 2 ]  ");
				parser.run();
				if(results.size()!=1 || ((Object[])results.get(0)).length!=2)
					return "Error#53.3: whitespace array failed";
			}

			// Test 54: results cleared between runs
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("{\"a\":1}");
				parser.run();
				if(results.size()!=1)
					return "Error#54.1: first run size failed";
				buf.append("{\"b\":2}");
				parser.run();
				if(results.size()!=1)
					return "Error#54.2: second run should have only 1 new element, got "+results.size();
				if(!((JSONObject)results.get(0)).containsKey("b"))
					return "Error#54.3: second run should be b, got "+results.get(0);
				// third run with multiple
				buf.append(" [1,2] true ");
				parser.run();
				if(results.size()!=2)
					return "Error#54.4: third run should have 2 elements, got "+results.size();
			}

			// Test 55: buffer trimming after many elements
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				for(int i=0;i<30;i++)
				{
					buf.append("{\"n\":"+i+"} ");
					parser.run();
					if(results.size()!=1)
						return "Error#55."+i+": trimming run "+i+" failed size "+results.size();
					if(!((JSONObject)results.get(0)).get("n").equals(Long.valueOf(i)))
						return "Error#55."+i+": trimming value "+i+" failed";
				}
				// after many runs, buffer should have been trimmed (len small)
				if(buf.length()>50)
					return "Error#55.30: buffer not trimmed, len="+buf.length();
				// verify next parse still works after trimming
				buf.append("{\"final\":123}");
				parser.run();
				if(results.size()!=1 || !((JSONObject)results.get(0)).get("final").equals(Long.valueOf(123)))
					return "Error#55.31: post-trim parse failed";
			}

			// Test 56: error handling - invalid JSON throws Error
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("{\"a\":}");
				try{ parser.run(); return "Error#56.1: invalid object should throw Error"; } catch(final Error e){
					if(!(e.getCause() instanceof MJSONException))
						return "Error#56.2: wrong cause for invalid object";
				}
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("[1,,2]");
				try{ parser.run(); return "Error#56.3: invalid array should throw Error"; } catch(final Error e){}
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("\"\\z\"");
				try{ parser.run(); return "Error#56.4: invalid escape should throw Error"; } catch(final Error e){}
			}

			// Test 57: incomplete JSON does not throw, waits for more data
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("{\"a\":1");
				parser.run();
				if(results.size()!=0)
					return "Error#57.1: incomplete should be 0";
				// no error thrown, now complete
				buf.append("}");
				parser.run();
				if(results.size()!=1)
					return "Error#57.2: completed after incomplete failed";
				// incomplete string
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("\"unclosed");
				parser.run();
				if(results.size()!=0)
					return "Error#57.3: unclosed string should be 0";
				buf.append("\"");
				parser.run();
				if(results.size()!=1 || !results.get(0).equals("unclosed"))
					return "Error#57.4: closed string failed";
				// incomplete array
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("[1,2");
				parser.run();
				if(results.size()!=0)
					return "Error#57.5: incomplete array should be 0";
				buf.append("]");
				parser.run();
				if(results.size()!=1)
					return "Error#57.6: completed array failed";
			}

			// Test 58: unicode escapes split across chunks via streaming
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("\"\\u004");
				parser.run();
				if(results.size()!=0)
					return "Error#58.1: partial unicode should be 0";
				buf.append("1\"");
				parser.run();
				if(results.size()!=1 || !results.get(0).equals("A"))
					return "Error#58.2: unicode across chunks failed";
				// combined
				buf = new StringBuffer();
				results = new ArrayList<Object>();
				parser = json.getJSONParser(buf, results);
				buf.append("\"hello\\u002");
				parser.run();
				if(results.size()!=0)
					return "Error#58.3: mid unicode should be 0";
				buf.append("0world\"");
				parser.run();
				if(results.size()!=1 || !results.get(0).equals("hello world"))
					return "Error#58.4: hello unicode world failed";
			}

			// Test 59: complex escaped string via streaming
			{
				String origEsc = "a\"b\\c\n\r\t";
				String jsonEsc = "\""+MiniJSON.toJSONString(origEsc)+"\"";
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				// split in middle of escape sequence
				int split = jsonEsc.length()/2;
				buf.append(jsonEsc.substring(0,split));
				parser.run();
				if(results.size()!=0)
					return "Error#59.1: half escaped should be 0";
				buf.append(jsonEsc.substring(split));
				parser.run();
				if(results.size()!=1 || !results.get(0).equals(origEsc))
					return "Error#59.2: complex escaped streaming failed: got "+results.get(0);
			}

			// Test 60: streaming with all JSON types in one buffer
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("{\"s\":\"hi\"} [\"hi\",42,3.14,false,null,[],{}] 42 3.14 true null \"test\"");
				parser.run();
				if(results.size()!=7)
					return "Error#60.1: all types streaming size failed: "+results.size();
				if(!(results.get(0) instanceof JSONObject))
					return "Error#60.2: first object failed";
				if(!(results.get(1) instanceof Object[]) || ((Object[])results.get(1)).length!=7)
					return "Error#60.3: array all types failed";
				if(!results.get(2).equals(Long.valueOf(42)))
					return "Error#60.4: long failed";
				if(!(results.get(3) instanceof Double))
					return "Error#60.5: double failed";
				if(!results.get(4).equals(Boolean.TRUE))
					return "Error#60.6: true failed";
				if(results.get(5)!=MiniJSON.NULL)
					return "Error#60.7: null failed";
				if(!results.get(6).equals("test"))
					return "Error#60.8: string failed";
			}

			// Test 61: streaming numbers with exponents
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("1e10 1E10 0e10 -1e-5 1.5E+2 ");
				parser.run();
				if(results.size()!=5)
					return "Error#61.1: exponent batch size failed: "+results.size();
				for(int i=0;i<results.size();i++) if(!(results.get(i) instanceof Double))
					return "Error#61.2: exponent "+i+" not double";
				if(Math.abs(((Double)results.get(3)).doubleValue()+1e-5)>1e-10)
					return "Error#61.3: -1e-5 value failed";
			}

			// Test 62: streaming with extra whitespace and newlines between elements
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("\n  true  \n\n  {\"a\":1}  \r\n  [1,2]  \t  \"hi\"  ");
				parser.run();
				if(results.size()!=4)
					return "Error#62.1: whitespace separated size failed: "+results.size();
				if(!results.get(0).equals(Boolean.TRUE))
					return "Error#62.2: true with whitespace failed";
				if(!(results.get(1) instanceof JSONObject))
					return "Error#62.3: object with whitespace failed";
			}

			// Test 63: object with array and nested object via streaming
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("{\"arr\":[{\"x\":1},{\"y\":2}],\"num\":123}");
				parser.run();
				if(results.size()!=1)
					return "Error#63.1: nested streaming size failed";
				JSONObject o3 = (JSONObject)results.get(0);
				Object[] arr3 = (Object[])o3.get("arr");
				if(arr3.length!=2 || !((JSONObject)arr3[0]).get("x").equals(Long.valueOf(1)))
					return "Error#63.2: nested streaming values failed";
			}

			// Test 64: buffer with incomplete at end plus complete in middle
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("{\"a\":1} {\"b\":");
				parser.run();
				if(results.size()!=1)
					return "Error#64.1: should have 1 complete, got "+results.size();
				if(!((JSONObject)results.get(0)).get("a").equals(Long.valueOf(1)))
					return "Error#64.2: first object wrong";
				buf.append("2} {\"c\":3}");
				parser.run();
				if(results.size()!=2)
					return "Error#64.3: should have 2 completions, got "+results.size();
				if(!((JSONObject)results.get(0)).get("b").equals(Long.valueOf(2)))
					return "Error#64.4: second object failed";
				if(!((JSONObject)results.get(1)).get("c").equals(Long.valueOf(3)))
					return "Error#64.5: third object failed";
			}

			// Test 65: sequential feeds simulating network chunks (char by char)
			{
				String doc = "{\"name\":\"test\",\"numbers\":[1,2,3],\"flag\":true}";
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				for(int i=0;i<doc.length();i++)
				{
					buf.append(doc.charAt(i));
					parser.run();
					if(i < doc.length()-1 && results.size()!=0)
						return "Error#65."+i+": char-by-char should not complete early at "+i;
				}
				if(results.size()!=1)
					return "Error#65.end: char-by-char final failed size "+results.size();
				if(!((JSONObject)results.get(0)).get("name").equals("test"))
					return "Error#65.end: char-by-char name failed";
			}

			// Test 66: streaming empty buffer run
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				parser.run();
				if(results.size()!=0)
					return "Error#66.1: empty buffer should yield 0";
				buf.append("   ");
				parser.run();
				if(results.size()!=0)
					return "Error#66.2: whitespace only should yield 0";
				buf.append("{}");
				parser.run();
				if(results.size()!=1)
					return "Error#66.3: object after whitespace failed";
			}

			// Test 67: streaming numbers split as 1e -> 10 with intermediate run that should still be incomplete
			{
				StringBuffer buf = new StringBuffer();
				List<Object> results = new ArrayList<Object>();
				Runnable parser = json.getJSONParser(buf, results);
				buf.append("1");
				parser.run();
				if(results.size()!=0)
					return "Error#67.1: single digit without terminator should be 0 (needs delimiter)";
				buf.append("e");
				parser.run();
				if(results.size()!=0)
					return "Error#67.2: 1e without exponent digits should be 0";
				buf.append("10 ");
				parser.run();
				if(results.size()!=1 || !(results.get(0) instanceof Double))
					return "Error#67.3: 1e10 incremental failed";
			}

		}
		catch(final MJSONException e)
		{
			return "Exception: " + e.getMessage();
		}
		catch(final Exception e)
		{
			return "Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage();
		}
		return null; // success
	}
	
}

