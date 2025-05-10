function TEXT(textParsers)
{
	this.resume = null;
	this.reset = function()
	{
		this.lastParser = null;
	};
	this.reset();
	
	this.parse = function(c)
	{
		var s;
		var lp = this.lastParser; 
		if(lp != null)
		{
			if((s=lp.process(c)) != null)
				return s;
			this.lastParser=null;
		}
		for(var x=0;x<textParsers.length;x++)
		{
			var p = textParsers[x]; 
			if((p!=lp)&&((s=p.process(c)) != null))
			{
				this.lastParser = p;
				return s;
			}
		}
		return null;
	};
	this.eolDetected = function() {
		var s;
		for(var x=0;x<textParsers.length;x++)
			if((s=textParsers[x].eolDetected()) != null)
				return s;
		return null;
	};

	this.process = function(str)
	{
		if(this.resume != null)
		{
			if(this.resume)
				str = this.resume.text + str;
			if(this.resume.call)
				this.resume.call();
			this.resume = null;
		}
		var i=0;
		var excep = null;
		while(i<str.length)
		{
			if(excep != null)
			{
				this.resume = {
					"text": str.substr(i),
					"call": excep
				}
				return str.substr(0,i);
			}
			var c = str[i];
			var s;
			try 
			{
				s = this.parse(c);
			}
			catch(e)
			{
				s = e.message;
				if(e.call !== undefined)
					excep = e.call;
			} 
			if(s != null) // should this be before the parser trig checks?
			{
				if(s.length>0)
				{
					var z = s.indexOf('\0'); // resume marker
					if(z >= 0)
					{
						str = str.substr(0,i) + s.substr(0,z) + s.substr(z+1) + str.substr(i+1);
						i += z;
					}
					else
					{
						str = str.substr(0,i) + s + str.substr(i+1);
						i += s.length;
					}
				}
				else
					str = str.substr(0,i) + str.substr(i+1);
				continue;
			}
			else
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
		if(excep != null)
		{
			this.resume = {
				"text": '',
				"call": excep
			}
		}
		return str;
	}
}