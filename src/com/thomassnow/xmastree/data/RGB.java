package com.thomassnow.xmastree.data;

import android.util.Log;

public class RGB
	{
		private int red;
		private int green;
		private int blue;
		private int alpha;

		public RGB(int r, int g, int b,int a)
			{
				setRed(r & 0xff);
				setGreen(g & 0xff);
				setBlue(b & 0xff);
				setAlpha(a & 0xff);
			}

		public RGB()
			{
				setRed(setGreen(setBlue(setAlpha(0))));
			}
		
		public RGB(String rgb)
		{
			try
				{
					int start = 0;
					int end = rgb.indexOf(',');
					String sr = rgb.substring(0,end);
					start = end+1;
					end = rgb.indexOf(',',start);
					String sg = rgb.substring(start,end);
					String sb = rgb.substring(end+1);
					red = Integer.parseInt(sr);
					green = Integer.parseInt(sg);
					blue = Integer.parseInt(sb);
					setAlpha(0xff);
				}
			catch(NumberFormatException e)
				{
					Log.e("Exception","Number Format in RGB:  " + rgb);
				}
		}
		
		public RGB(int color)
			{
				setColor(color);
			}

		public int getRed()
			{
				return red;
			}

		public int setRed(int red)
			{
				this.red = red & 0xff;
				return this.red;
			}

		public int getGreen()
			{
				return green;
			}

		public int setGreen(int green)
			{
				this.green = green & 0xff;
				return this.green;
			}

		public int getBlue()
			{
				return blue;
			}

		public int setBlue(int blue)
			{
				this.blue = blue & 0xff;
				return this.blue;
			}

		public int getAlpha()
			{
					return alpha;
			}

		public int setAlpha(int alpha)
			{
					this.alpha = alpha & 0xff;
					return this.alpha;
			}

		public String toString()
			{
				String ret = Integer.toString(red) + "," + Integer.toString(green) + "," + Integer.toString(blue);

				return ret;
			}
		
		public int setColor(int color)
			{
				setAlpha(color >> 24);
				setRed(color >> 16);
				setGreen(color >> 8);
				setBlue(color);
				
				return color;
			}
		
		public int getColor()
			{
				int ret = 0;
				ret |= getAlpha() << 24;
				ret |= getRed() << 16;
				ret |= getGreen() << 8;
				ret |= getBlue();
				return ret;
			}

	}
