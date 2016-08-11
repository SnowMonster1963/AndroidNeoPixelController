package com.thomassnow.xmastree.data;

import android.util.Log;

import com.thomassnow.xmastree.network.MQTTHandler.MQTTPublish;
import com.thomassnow.xmastree.service.MQTTService;

public class ControllerNode
	{
		private String name;
		private int effect;
		private int color_mode;
		private int strand_mode;
		private int density;
		private int attack;
		private int sustain;
		private int decay;
		private RGB[] colors;
		private MQTTService connection;
		private boolean payAttention;
		private NodeActivityListener listener;
		
		public interface NodeActivityListener
		{
			public void onNodeActivity(ControllerNode node);
		}
		
		private void updateListener()
			{
				if(listener != null)
					listener.onNodeActivity(this);
			}
		
		public void setNodeActivityListener(NodeActivityListener nal)
			{
				listener = nal;
			}
		
		public ControllerNode(String name, MQTTService conn)
			{
				this.name = name;
				connection = conn;
				String topic = name + "/value/+";
				colors = new RGB[16];
				for (int i = 0; i < colors.length; i++)
					colors[i] = new RGB();

				setPayAttention(true);
				connection.subscribe(topic, 1, new MQTTPublish()
					{

						@Override
						public void onPublished(String topic, byte[] payload)
							{
								String x = new String(payload);
								Log.e("Dump Data", topic + ":  " + x);

								try
									{
										if (topic.compareTo(getName() + "/value/effect") == 0)
											initEffect(Integer.parseInt(x));
										else if (topic.compareTo(getName() + "/value/cmode") == 0)
											initColorMode(Integer.parseInt(x));
										else if (topic.compareTo(getName() + "/value/smode") == 0)
											initStrandMode(Integer.parseInt(x));
										else if (topic.compareTo(getName() + "/value/density") == 0)
											initDensity(Integer.parseInt(x));
										else if (topic.compareTo(getName() + "/value/attack") == 0)
											initAttack(Integer.parseInt(x));
										else if (topic.compareTo(getName() + "/value/sustain") == 0)
											initSustain(Integer.parseInt(x));
										else if (topic.compareTo(getName() + "/value/decay") == 0)
											initDecay(Integer.parseInt(x));
										else if (topic.compareTo(getName() + "/value/pattern") == 0)
											initColors(x);
										
										updateListener();
									}
								catch (NumberFormatException e)
									{
										Log.e("Exception", "Number Format in " + topic + ":  " + x);
									}
							}
					});
			}

		private void initColors(String x)
			{
				int idx = 0;
				int i;
				for (i = 0; i < colors.length; i++)
					colors[i] = new RGB();

				i = x.indexOf(",");
				while (i >= 0)
					{
						int r, g, b;
						r = g = b = 0;
						String s = x.substring(0, i);
						r = Integer.parseInt(s);
						x = x.substring(i + 1);

						i = x.indexOf(",");
						if (i > 0)
							{
								s = x.substring(0, i);
								g = Integer.parseInt(s);
								x = x.substring(i + 1);
								i = x.indexOf(",");
							}

						if (i > 0)
							{
								s = x.substring(0, i);
								b = Integer.parseInt(s);
								x = x.substring(i + 1);
								i = x.indexOf(",");
							}
						else if (x.length() > 0)
							b = Integer.parseInt(x);

						colors[idx] = new RGB(r, g, b, 0xff);
						idx++;
					}
			}

		public String getName()
			{
				return name;
			}

		public int getEffect()
			{
				return effect;
			}

		public int getColorMode()
			{
				return color_mode;
			}

		public int getStrandMode()
			{
				return strand_mode;
			}

		public int getDensity()
			{
				return density;
			}

		public int getAttack()
			{
				return attack;
			}

		public int getSustain()
			{
				return sustain;
			}

		public int getDecay()
			{
				return decay;
			}

		public void setEffect(int effect)
			{
				this.effect = effect;
				if (isPayAttention())
					connection.publishTopic(getName() + "/set/effect", Integer.toString(getEffect()), 1, true);
			}

		public void setColorMode(int color_mode)
			{
				this.color_mode = color_mode;
				if (isPayAttention())
					connection.publishTopic(getName() + "/set/cmode", Integer.toString(getColorMode()), 1, true);
			}

		public void setStrandMode(int strand_mode)
			{
				this.strand_mode = strand_mode;
				if (isPayAttention())
					connection.publishTopic(getName() + "/set/smode", Integer.toString(getStrandMode()), 1, true);
			}

		public void setDensity(int density)
			{
				if (density > 99)
					density = 99;
				this.density = density;
				if (isPayAttention())
					connection.publishTopic(getName() + "/set/density", Integer.toString(getDensity()), 1, true);
			}

		public void setAttack(int attack)
			{
				if (attack > 255)
					attack = 255;
				this.attack = attack;
				if (isPayAttention())
					connection.publishTopic(getName() + "/set/attack", Integer.toString(getAttack()), 1, true);
			}

		public void setSustain(int sustain)
			{
				if (sustain > 255)
					sustain = 255;
				this.sustain = sustain;
				if (isPayAttention())
					connection.publishTopic(getName() + "/set/sustain", Integer.toString(getSustain()), 1, true);
			}

		public void setDecay(int decay)
			{
				if (decay > 255)
					decay = 255;
				this.decay = decay;
				if (isPayAttention())
					connection.publishTopic(getName() + "/set/decay", Integer.toString(getDecay()), 1, true);
			}

		private void initEffect(int effect)
			{
				this.effect = effect;
			}

		private void initColorMode(int color_mode)
			{
				this.color_mode = color_mode;
			}

		private void initStrandMode(int strand_mode)
			{
				this.strand_mode = strand_mode;
			}

		private void initDensity(int density)
			{
				if (density > 99)
					density = 99;
				this.density = density;
			}

		private void initAttack(int attack)
			{
				if (attack > 255)
					attack = 255;
				this.attack = attack;
			}

		private void initSustain(int sustain)
			{
				if (sustain > 255)
					sustain = 255;
				this.sustain = sustain;
			}

		private void initDecay(int decay)
			{
				if (decay > 255)
					decay = 255;
				this.decay = decay;
			}

		public String toString()
			{
				return name;
			}

		public RGB getColor(int idx)
			{
				return colors[idx];
			}

		public RGB setColor(int idx, RGB c)
			{
				colors[idx] = c;
				return colors[idx];
			}

		public void setColors(RGB[] cs)
			{
				String s = "";
				for (int i = 0; i < colors.length; i++)
					{
						if (i < cs.length && cs[i].getAlpha() != 0)
							{
								colors[i] = cs[i];
								if (i > 0)
									s += ",";

								s += Integer.toString(cs[i].getRed()) + ",";
								s += Integer.toString(cs[i].getGreen()) + ",";
								s += Integer.toString(cs[i].getBlue());
							}
						else
							colors[i] = new RGB(0);
					}
				if (s.length() == 0)
					s = "0,0,0";

				if (isPayAttention())
					connection.publishTopic(getName() + "/set/pattern", s, 1, true);

			}
		
		public RGB[] getColors()
			{
				return colors;
			}

		public boolean isPayAttention()
			{
				return payAttention;
			}

		public void setPayAttention(boolean payAttention)
			{
				this.payAttention = payAttention;
			}

	}
