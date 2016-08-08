package com.thomassnow.xmastree.data;
import com.thomassnow.xmastree.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class MQTTSettings
	{
		String  m_clientid;
		String  m_host;
		int		m_port;
		String	m_username;
		String	m_password;
		Activity m_activity;
		
		public MQTTSettings(Activity a)
			{
				m_activity = a;
				SharedPreferences sharedPref = a.getSharedPreferences("mqtt_prefs",Context.MODE_PRIVATE);

				String default_client = a.getString(R.string.default_mqtt_clientid);
				String default_host = a.getString(R.string.default_mqtt_host);
				int default_port = Integer.parseInt(a.getString(R.string.default_mqtt_port));


				m_clientid = sharedPref.getString(a.getString(R.string.key_mqtt_clientid), default_client);
				m_host = sharedPref.getString(a.getString(R.string.key_mqtt_host), default_host);
				m_port = sharedPref.getInt(a.getString(R.string.key_mqtt_port), default_port);
				m_username = sharedPref.getString(a.getString(R.string.key_mqtt_username), "");
				m_password = sharedPref.getString(a.getString(R.string.key_mqtt_password), "");
				
			}
		
		public String getClientID(){return m_clientid;}
		public String getHost()
			{
				return m_host;
			}
		
		public int getPort(){return m_port;}
		public String getUsername(){return m_username;}
		public String getPassword(){return m_password;}
		
		public boolean save()
			{
				SharedPreferences sharedPref = m_activity.getSharedPreferences("mqtt_prefs",Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();

				editor.putString(m_activity.getString(R.string.key_mqtt_clientid), m_clientid);
				editor.putString(m_activity.getString(R.string.key_mqtt_host), m_host);
				editor.putInt(m_activity.getString(R.string.key_mqtt_port), m_port);
				editor.putString(m_activity.getString(R.string.key_mqtt_username), m_username);
				editor.putString(m_activity.getString(R.string.key_mqtt_password), m_password);
				return editor.commit();
				
			}

		public void setClientID(String clientid)
			{
				m_clientid = clientid;
			}

		public void setHost(String host)
			{
				m_host = host;
			}

		public void setPort(int port)
			{
				m_port = port;
			}

		public void setUsername(String username)
			{
				m_username = username;
			}

		public void setPassword(String password)
			{
				m_password = password;
			}

	}
