	window.configlisteners = {};
	window.config = localStorage.getItem('config1');
	if(window.config) 
		window.config = JSON.parse(window.config);
	else
		window.config = {};
		
	function getConfigPath(path)
	{
		path = path.split('/');
		var c = window.config;
		for(var i=0;i<path.length-1;i++)
		{
			var p = path[i];
			if(p.length > 0)
			{
				if(!(p in c))
					c[p] = {};
				c=c[p];
			}
		}
		return [c, path[path.length-1]];
	}
	
	function getConfig(path, def)
	{
		var c = getConfigPath(path);
		var p = c[1];
		c = c[0];
		if(p in c)
			return c[p];
		if(def === undefined)
			return null;
		c[p] = def;
		localStorage.setItem('config1', JSON.stringify(window.config));
		return def;
	}
	
	function setConfig(path, val)
	{
		var c = getConfigPath(path);
		var p = c[1];
		c = c[0];
		c[p] = val;
		if(path in window.configlisteners)
		{
			var lst = window.configlisteners[path];
			for(var i=0;i<lst.length;i++)
				lst[i](path, val);
		}
		localStorage.setItem('config1', JSON.stringify(window.config));
	}

	function addConfigListener(path, func)
	{
		if(!(path in window.configlisteners))
			window.configlisteners[path] = [];
		window.configlisteners[path].push(func);
	}
		
