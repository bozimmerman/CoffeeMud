function TEXT(textParsers)
{
	this.parse = function(c) {
		var s;
		for(var x=0;x<textParsers.length;x++)
			if((s=textParsers[x].process(c))!=null)
				return s;
		return null;
	};
	this.eolDetected = function() {
		var s;
		for(var x=0;x<textParsers.length;x++)
			if((s=textParsers[x].eolDetected())!=null)
				return s;
		return null;
	};

	this.process = function(str)
	{
		var i=0;
		while(i<str.length)
		{
			var c = str[i];
			var s = this.parse(c);
			if(s != null) // should this be before the parser trig checks?
			{
				str = str.substr(0,i) + s + str.substr(i+1);
				i += s.length;
				continue;
			};
			switch(c)
			{
			case '\0':
				str = str.substr(0,i) + str.substr(i+1);
				break;
			case '&':
				str = str.substr(0,i+1)+'amp;'+str.substr(i+2);
				i += 3;
				break;
			case ' ':
				str = str.substr(0,i)+'&nbsp;'+str.substr(i+1);
				i += 4;
				break;
			case '>':
				str = str.substr(0,i)+'&gt;'+str.substr(i+1);
				i += 2;
				break;
			case '<':
				str = str.substr(0,i)+'&lt;'+str.substr(i+1);
				i += 3;
				break;
			case '\r': // should already be gone
			case '\n':
			{
				var s = this.eolDetected();
				if(s != null)
				{
					str = str.substr(0,i)+s+str.substr(i+1);
					i-=1; //TODO: wuh here?
				}
				else
				{
					str = str.substr(0,i)+'<BR>'+str.substr(i+1);
					i += 3;
				}
				break;
			}
			default:
				break;
			}
			i++;
		}
		return str;
	}
}