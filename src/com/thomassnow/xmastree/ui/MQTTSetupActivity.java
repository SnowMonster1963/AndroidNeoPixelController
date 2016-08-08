package com.thomassnow.xmastree.ui;

import com.thomassnow.xmastree.R;
import com.thomassnow.xmastree.data.MQTTMessage;
import com.thomassnow.xmastree.data.MQTTSettings;
import com.thomassnow.xmastree.network.MQTTClient;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MQTTSetupActivity extends Activity implements MQTTClient.MQTTListener
	{
		private MQTTClient client;

		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_mqttsetup);
				client = new MQTTClient(this);
				if (savedInstanceState == null)
					{
						getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
					}
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				// Inflate the menu; this adds items to the action bar if it is
				// present.
				getMenuInflater().inflate(R.menu.mqttsetup, menu);
				return true;
			}

		@Override
		public boolean onOptionsItemSelected(MenuItem item)
			{
				// Handle action bar item clicks here. The action bar will
				// automatically handle clicks on the Home/Up button, so long
				// as you specify a parent activity in AndroidManifest.xml.
				int id = item.getItemId();
				if (id == R.id.action_settings)
					{
						return true;
					}
				return super.onOptionsItemSelected(item);
			}

		/**
		 * A placeholder fragment containing a simple view.
		 */
		public static class PlaceholderFragment extends Fragment
			{

				public PlaceholderFragment()
					{
					}

				@Override
				public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
					{
						View rootView = inflater.inflate(R.layout.fragment_mqttsetup, container, false);
						
						Activity a = getActivity();
						a.setResult(RESULT_CANCELED);
						try
							{
								MQTTSettings x = new MQTTSettings(a);

								TextView tv = (TextView) rootView.findViewById(R.id.editMQTTHost);
								tv.setText(x.getHost());

								tv = (TextView) rootView.findViewById(R.id.editMQTTPort);
								tv.setText(Integer.toString(x.getPort(), 10));

								tv = (TextView) rootView.findViewById(R.id.editMQTTUserName);
								tv.setText(x.getUsername());

								tv = (TextView) rootView.findViewById(R.id.editMQTTPassword);
								tv.setText(x.getPassword());
							}
						catch (Exception e)
							{
							}

						final Button button = (Button) rootView.findViewById(com.thomassnow.xmastree.R.id.buttonMQTTSave);
						button.setOnClickListener(new View.OnClickListener()
							{
								public void onClick(View v)
									{
										try
											{
												Activity a = getActivity();
												
												MQTTSettings x = new MQTTSettings(a);

												TextView tv = (TextView) getView().findViewById(R.id.editMQTTHost);
												x.setHost(tv.getText().toString());

												tv = (TextView) getView().findViewById(R.id.editMQTTPort);
												String s = tv.getText().toString();
												x.setPort(Integer.parseInt(s));

												tv = (TextView) getView().findViewById(R.id.editMQTTUserName);
												s = tv.getText().toString();
												x.setUsername(s);

												tv = (TextView) getView().findViewById(R.id.editMQTTPassword);
												s = tv.getText().toString();
												x.setPassword(s);
												x.save();
												a.setResult(RESULT_OK);
												a.finish();
											}
										catch (Exception e)
											{

											}
									}
							});
						return rootView;
					}


			}

		@Override
		public synchronized void onConnected()
			{
			Context context = getApplicationContext();
			CharSequence text = "Connected!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();			
			}

		@Override
		public void onClose()
			{
				Context context = getApplicationContext();
				CharSequence text = "Closed!";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();			
			}

		@Override
		public void onMessage(MQTTMessage msg)
			{
				Context context = getApplicationContext();
				String pl = new String(msg.getPayload());
				CharSequence text = "Topic:  " + msg.getTopic() + "\r\nMessage:  " + pl;
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();			
			}
	}
