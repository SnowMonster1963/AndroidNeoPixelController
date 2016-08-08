package com.thomassnow.xmastree.data;

import android.graphics.Color;

public class LightString
	{
		private String	name;
		private int		effect;
		private RGB		pcolor;
		private RGB		scolor;
		private int		cmode;
		private int		smode;
		private int		density;
		private	int		delay;
		
		
		public LightString(String name)
			{
				this.setName(name);
			}


		public String getName()
			{
					return name;
			}


		public void setName(String name)
			{
					this.name = name;
			}


		public int getEffect()
			{
					return effect;
			}


		public void setEffect(int effect)
			{
					this.effect = effect;
			}


		public RGB getPrimaryColor()
			{
					return pcolor;
			}


		public void setPrimaryColor(RGB pcolor)
			{
					this.pcolor = pcolor;
			}


		public RGB getSecondaryColor()
			{
					return scolor;
			}


		public void setSecondaryColor(RGB scolor)
			{
					this.scolor = scolor;
			}


		public int getColorMode()
			{
					return cmode;
			}


		public void setColorMode(int cmode)
			{
					this.cmode = cmode;
			}


		public int getStrandMode()
			{
					return smode;
			}


		public void setStrandMode(int smode)
			{
					this.smode = smode;
			}


		public int getDensity()
			{
					return density;
			}


		public void setDensity(int density)
			{
					this.density = density;
			}


		public int getDelay()
			{
					return delay;
			}


		public void setDelay(int delay)
			{
					this.delay = delay;
			}

	}
