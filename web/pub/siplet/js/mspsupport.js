var MSP = function(sipwin)
{
	this.mimeTypes = { 'mp3': 'audio/mpeg', 'ogg': 'audio/ogg', 'wav': 'audio/wav' };
	
	this.reset = function()
	{
		this.tag = null;
		this.data = null;
		this.sounds={}; // permanent cache of sound info
		this.sounders={};
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

	this.flush = function() {
		var s = '';
		if(this.tag != null)
		{
			s += this.tag;
			this.tag = null;
		}
		if(this.data != null)
		{
			s += this.data;
			this.data = null;
		}
		return s;
	};

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
	
	this.LoadSound = function(key, url, tag, music)
	{
		tag = tag || '';
		tag = tag.replace(/[^a-zA-Z0-9_-]/g, '_').toLowerCase();
		var fullTag = "sounder_"+tag;
		if(!(fullTag in this.sounds))
			this.sounds[fullTag] = {};
		var lkey = key.toLowerCase().replace(/[^a-zA-Z0-9_-]/g, '_');;
		if(!(key in this.sounds[fullTag]))
		{
			var sound = {
				key: key,
				lkey: lkey,
				url: url,
				tag: tag,
				music: music,
				fullTag: fullTag
			};
			this.sounds[fullTag][lkey] = sound;
		}
		return this.sounds[fullTag][lkey];
	}
	
	this.PlaySound = function(key, url, repeats, volume, priority, music, tag)
	{
		var volPct = Number(getConfig('window/volume','100'));
		if(volPct<0.01)
			return;
		var sound = this.LoadSound(key, url, tag, music);
		var sounderName = music ? 'music' : 'sound';
		var sounder = this.sounders[sounderName];
		if(sounder)
		{
			var now=new Date();
			var elapsed=Math.abs(now.getTime() - sounder.startDate.getTime());
			if((elapsed < 1500) && (priority <= sounder.priority))
				return;
			this.StopSounder(sounderName, sounder);
		}
		sounder = {
			sound: sound,
			priority: Number(priority) || 0,
			startDate: new Date(),
			repeats: Number(repeats) || 0
		};
		sounder.player=document.createElement('audio');
		if(!sounder.player || !sounder.player.play) 
			return;
		var x = key.lastIndexOf('.');
		if(x<0)
			return;
		var ext = key.substr(x+1).toLowerCase();
		if(!ext in this.mimeTypes)
			return;
		var mimeType = this.mimeTypes[ext];
		this.sounders[sounderName] = sounder;
		sipwin.window.appendChild(sounder.player);
		if((!url.endsWith('/'))&&(!key.startsWith('/')))
			sounder.player.setAttribute('src', url+'/'+key);
		else
			sounder.player.setAttribute('src', url+key);
		sounder.player.setAttribute('type', mimeType);
		sounder.player.setAttribute('hidden','true');
		sounder.player.volume = (Number(volume) / 100.0) * (volPct/100.0);
		sounder.player.loop = false;
		if(sounder.player.addEventListener)
		{
			var me = this;
			sounder.endedHandler = function() {
				if((!sounderName in me.sounders)
				||(me.sounders[sounderName] !== sounder))
					return;
				if(sounder.repeats > 0)
					--sounder.repeats;
				if(sounder.repeats == 0)
					return me.StopSounder(sounderName, sounder);
				// repeats < 0 are infinite until stopped externally
				try {
					sounder.player.play();
				} catch(e) {
					me.StopSounder(sounderName, sounder);
				}
			};
			sounder.player.addEventListener('ended', sounder.endedHandler);
		}
		try {
			sounder.player.play();
		} catch(e) {
			me.StopSounder(sounderName, sounder);
		}
	}

	this.StopSounder = function(type, sounder)
	{
		if(sounder.player.pause)
			sounder.player.pause();
		if(sounder.player.removeEventListener && sounder.endedHandler)
			sounder.player.removeEventListener('ended', sounder.endedHandler);
		sounder.player.outerHTML = '';
		delete this.sounders[type];
	};

	this.StopSound = function(key, type)
	{
		var choices = type ? [type] : ['music', 'sound'];
		for(var i=0;i<choices.length;i++)
		{
			var sounder = this.sounders[choices[i]];
			if(sounder && ((key === undefined) || sounder.sound.key == key))
				this.StopSounder(choices[i], sounder);
		}
	};

	this.processParms = function(tag, parms)
	{
		var parmLetters = "VLPCTU";
		var parmTypes   = '####$$';
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
					parms[tagLetter] = isNumber(num) ? Number(value) : value;
					value='';
					tagLetter = ' ';
				}
				break;
			}
		}
		if(state == 2)
			parms[tagLetter] = num ? Number(value) : value;
	};

	this.processSoundEmbed = function(tag)
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
		{
			if(this.sounders['sound'])
				this.StopSound(this.sounders['sound'].key, 'sound');
		}
		else
			this.PlaySound(parms.N,parms.U,parms.L,parms.V,parms.P,false,parms.T);
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
		{
			if(this.sounders['music'])
				this.StopSound(this.sounders['music'].key, 'music');
		}
		else
			this.PlaySound(parms.N,parms.U,parms.L,parms.V,parms.P,true,parms.T);
	};
}

