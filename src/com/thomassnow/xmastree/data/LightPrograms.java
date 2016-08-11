package com.thomassnow.xmastree.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class LightPrograms extends ArrayList<LightProgram> implements Serializable
	{

		/**
	 * 
	 */
	private static final long serialVersionUID = -7763757028628513653L;

		public LightPrograms()
			{
				// TODO Auto-generated constructor stub
			}

		private void writeObject(java.io.ObjectOutputStream out) throws IOException
		{
//			out.write(size());
//			for(int i=0;i<size();i++)
//				out.writeObject(get(i));
		}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
		{
//			int x = in.read();
//			for(int i=0;i<x;i++)
//				{
//					LightProgram lp = (LightProgram)in.readObject();
//					add(lp);
//				}
		}
	

	}
