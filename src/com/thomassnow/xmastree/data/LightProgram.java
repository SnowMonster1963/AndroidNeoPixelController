package com.thomassnow.xmastree.data;

import java.io.IOException;
import java.io.Serializable;

public class LightProgram implements Serializable
	{
		private static final long serialVersionUID = 5227284938460391624L;
		private String name;
		private String description;
		private int effect;
		private int density;
		private int attack;
		private int sustain;
		private int decay;
		private RGB[] colors;

		private void init()
			{
				colors = new RGB[16];
				colors[0] = new RGB(255, 255, 255, 255);
				for (int i = 1; i < 16; i++)
					colors[i] = new RGB(0);
				effect = 0;
				density = 50;
				attack = decay = 0;
				sustain = 10;
				name = "New Program";
				description = "Need to name this";
			}
		
		public LightProgram()
			{
				init();
			}
		
		public LightProgram(String name,String description)
			{
				init();
				this.name = name;
				this.description = description;
			}

		public String getName()
			{
					return name;
			}

		public void setName(String name)
			{
					this.name = name;
			}

		public String getDescription()
			{
					return description;
			}

		public void setDescription(String description)
			{
					this.description = description;
			}

		public int getEffect()
			{
				return effect;
			}

		public void setEffect(int effect)
			{
				this.effect = effect;
			}

		public int getDensity()
			{
				return density;
			}

		public void setDensity(int density)
			{
				this.density = density;
			}

		public int getAttack()
			{
				return attack;
			}

		public void setAttack(int attack)
			{
				this.attack = attack;
			}

		public int getSustain()
			{
				return sustain;
			}

		public void setSustain(int sustain)
			{
				this.sustain = sustain;
			}

		public int getDecay()
			{
				return decay;
			}

		public void setDecay(int decay)
			{
				this.decay = decay;
			}

		public RGB[] getColors()
			{
				return colors;
			}

		public void setColors(RGB[] colors)
			{
				for (int i = 0; i < this.colors.length; i++)
					{
						if (i < colors.length)
							this.colors[i].setColor(colors[i].getColor());
						else
							this.colors[i].setColor(0);
					}
			}

		public void copyNode(ControllerNode cn)
			{
				setAttack(cn.getAttack());
				setColors(cn.getColors());
				setEffect(cn.getEffect());
				setAttack(cn.getAttack());
				setSustain(cn.getSustain());
				setDecay(cn.getDecay());
				setDensity(cn.getDensity());
			}

		private void writeObject(java.io.ObjectOutputStream out) throws IOException
			{
				out.writeObject(name);
				out.writeObject(description);
				out.writeObject(Integer.valueOf(effect));
				out.writeObject(Integer.valueOf(density));
				out.writeObject(Integer.valueOf(attack));
				out.writeObject(Integer.valueOf(sustain));
				out.writeObject(Integer.valueOf(decay));
				for(int i=0;i<16;i++)
					out.writeObject(Integer.valueOf(colors[i].getColor()));
			}

		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
			{
				name = (String)in.readObject();
				description = (String)in.readObject();
				effect = ((Integer)in.readObject()).intValue();
				density = ((Integer)in.readObject()).intValue();
				attack = ((Integer)in.readObject()).intValue();
				sustain = ((Integer)in.readObject()).intValue();
				decay = ((Integer)in.readObject()).intValue();
				colors = new RGB[16];
				for(int i=0;i<16;i++)
					colors[i] = new RGB(((Integer)in.readObject()).intValue());
			}
		
		public String toString()
			{
				return getName();
			}

	}
