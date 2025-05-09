var MSP = function(sipwin)
{
	this.reset = function()
	{
		this.tag = null;
		this.data = null;
		this.sounders=[];
		this.soundDates=[];
		this.soundPriorities=[];
		this.skip=0;
	};
	this.reset();
	
	this.active = function() { return sipwin.MSPsupport; };
	this.eolDetected = function() { return null; };

	this.cancelProcessing = function() {
		var s = '';
		if(this.tag != null)
		{
			s += this.tag;
			if(this.data != null)
				s += this.data;
		}
		this.data = null;
		this.tag = null;
		this.skip = 1;
		return '\0' + s;
	}

	this.process = function(c) {
		if (!this.active())
			return null;
		if(this.tag == null)
		{
			if(c=='!')
			{
				if(this.skip-->0)
					return null;
				this.tag = '!';
				this.data = null;
				return '';
			}
			else
				return null;
		}
		else
		if(this.data == null)
		{
			this.tag += c;
			if((this.tag == '!!SOUND(')||(this.tag == '!!MUSIC('))
			{
				this.data = '';
				return '';
			}
			if('!!SOUND('.startsWith(this.tag)||'!!MUSIC('.startsWith(this.tag))
				return '';
			return this.cancelProcessing();
		}
		else
		switch(c)
		{
			case ')':
			{
				var which = this.tag.startsWith('!!SOUND');
				this.tag = null;
				var tagData = this.data;
				this.data = null;
				if(which == 0)
					this.processSoundEmbed(tagData);
				else
					this.processMusicEmbed(tagData);
				return '';
			}
			case '\n':
			case '\r':
				return this.cancelProcessing();
			default:
				this.data += c;
				if(this.data.length > 256)
					return this.cancelProcessing();
				return '';
		}
	};
	
	this.PlaySound = function(key,url,repeats,volume,priority)
	{
		var playerName = sipwin.windowName;
		var theSoundPlayer = this.sounders[playerName];  
		if(theSoundPlayer)
		{
			var now=new Date();
			var ellapsed=Math.abs(now.getTime() - this.soundDates[playerName].getTime());
			if((ellapsed < 1500) && (priority <= this.soundPriorities[playerName]))
				return;
			if(theSoundPlayer.stop)
				theSoundPlayer.stop();
			sipwin.window.removeChild(theSoundPlayer);
		}
		theSoundPlayer=document.createElement('audio');
		if(!theSoundPlayer || !theSoundPlayer.play) 
			theSoundPlayer=document.createElement('embed');
		if(!theSoundPlayer) 
			return;
		this.sounders[playerName]=theSoundPlayer;
		this.soundDates[playerName]=new Date();
		this.soundPriorities[playerName]=priority;
		sipwin.window.appendChild(theSoundPlayer);
		theSoundPlayer.setAttribute('src', url+key);
		var x = key.lastIndexOf('.');
		var mimeType = 'wav';
		if(x>0)
			mimeType = key.substr(x+1);
		theSoundPlayer.setAttribute('type','audio/'+mimeType);
		theSoundPlayer.setAttribute('hidden','true');
		theSoundPlayer.setAttribute('volume', volume);
		if(theSoundPlayer.play)
			theSoundPlayer.play();
	}

	this.StopSound - function(key)
	{
		var playerName = sipwin.windowName;
		var theSoundPlayer=document.getElementById(playerName);
		theSoundPlayer.src='';
		theSoundPlayer.Play();
		theSoundPlayer.innerHTML='';
	};

	this.processParms = function(tag, parms)
	{
		var parmLetters = "VLPCTU";
		var parmTypes   = '####$$';
		var last = -1;
		var state = 2;
		var tagLetter = 'N';
		var value = '';
		var num = false;
		var z;
		for(var i=0;i<tag.length;i++)
		{
			var c = tag[i];
			switch(state)
			{
			case 0:
				z=parmLetters.indexOf(c.toUpperCase()); 
				if(z>=0)
				{
					tagLetter = c.toUpperCase();
					num = parmTypes[z] == '#';
					state=1;
				}
				break;
			case 1:
				if(c=='=')
				{
					state=2;
					value='';
				}
				else
				if(c!=' ')
				{
					state=0;
					i=i-1;
				}
				break;
			case 2:
				if(c!=' ')
					value += c;
				else
				{
					// done
					state=0;
					parms[tagLetter] = num ? Number(value) : value;
					value='';
					tagLetter = ' ';
				}
				break;
			}
		}
		if(state == 2)
			parms[tagLetter] = num ? Number(value) : value;
	};

	this.processSoundEmbed = function(tag, x)
	{
		//!!SOUND(&fname; V=&v; L=&l; P=&p; T=&t; U=&u;)
		var parms = {
			N: "",
			V: 50,
			L: 0,
			P: 50,
			C: 0,
			T: "",
			U: ""
		};
		this.processParms(tag, parms);
		if((parms.U.length == 0)||(parms.N.length==0))
			return; // no internal sound files, so....
		if(parms.N.toLowerCase() == 'off')
			this.StopSound('');
		else
			this.PlaySound(parms.N,parms.U,parms.L,parms.V,parms.P);
	};

	this.processMusicEmbed = function(tag)
	{
		//!!MUSIC(&fname; V=&v; L=&l; P=&p; T=&t; U=&u;)
		var parms = {
			N: "",
			V: 50,
			L: 0,
			P: 50,
			C: 1,
			T: "",
			U: ""
		};
		this.processParms(tag, parms);
		if((parms.U.length == 0)||(parms.N.length==0))
			return; // no internal sound files, so....
		if(parms.N.toLowerCase() == 'off')
			this.StopSound('');
		else
			this.PlaySound(parms.N,parms.U,parms.L,parms.V,parms.P);
	};
}

