package com.thomassnow.xmastree.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import com.thomassnow.xmastree.data.MQTTInternals;
import com.thomassnow.xmastree.data.MQTTMessage;
import com.thomassnow.xmastree.data.MQTTSettings;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class MQTTClient extends AsyncTask<MQTTSettings, MQTTMessage, String>
	{
		private Activity activity;
		private MQTTListener callback;
		private InputStream in;
		private OutputStream out;
		private Socket s;
		
		public interface MQTTListener
		{
			public void onConnected();
			public void onClose();
			public void onMessage(MQTTMessage msg);
		}

		public MQTTClient(Activity a)
			{
				callback = (MQTTListener)a;
				MQTTSettings mqtt = new MQTTSettings(a);
				this.execute(mqtt);
			}

		@Override
		protected String doInBackground(MQTTSettings... params)
			{

				try
					{
						SocketAddress sockaddr = new InetSocketAddress(params[0].getHost(), params[0].getPort());
						//SocketAddress sockaddr = new InetSocketAddress("192.168.1.126", 4321);
						s = new Socket();
						s.connect(sockaddr);
						in = s.getInputStream();
						out = s.getOutputStream();
						
						byte [] msg = MQTTInternals.formatConnectMsg(params[0].getClientID(), params[0].getUsername(), params[0].getPassword(), "Android/Active", "No", 1, true, true, 0);
						if(s.isConnected())
							{
							//String x = "Hello World!\r\n";
							//out.write(x.getBytes());
							out.write(msg);
							byte [] obuf = new byte[4];
							int len = 0;
							
							while(len < 4)
								{
									int v = in.read(obuf,len,obuf.length-len);
									if(v > 0)
										len += v;
								}
							publishProgress(new MQTTMessage("First Topic","First Message!".getBytes()));
							
							in.close();
							out.close();
							s.close();
							}

					}
				catch (UnknownHostException e)
					{
						// TODO Auto-generated catch block
						Log.e("Network", "Host not found");
						callback.onClose();
						return "Error";
					}
				catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						callback.onClose();
						return "Error";
					}
				catch (Exception e)
					{
						return "Error";
					}

				return "Done";
			}
		
		@Override
		protected void onProgressUpdate(MQTTMessage... values)
			{
				for(int i = 0;i<values.length;i++)
					callback.onMessage(values[i]);
			}

	}
