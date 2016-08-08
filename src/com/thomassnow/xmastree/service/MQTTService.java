package com.thomassnow.xmastree.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.thomassnow.xmastree.data.MQTTInternals;
import com.thomassnow.xmastree.data.MQTTMessage;
import com.thomassnow.xmastree.data.MQTTSettings;
import com.thomassnow.xmastree.network.MQTTHandler;
import com.thomassnow.xmastree.data.MQTTSubscription;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MQTTService extends Service
	{

		private class SubscriptionItem
			{
				public String topic;
				public MQTTHandler.MQTTPublish callback;
			};

		// Binder given to clients
		private final IBinder mBinder = new LocalBinder();
		// Random number generator
		private final Random mGenerator = new Random();
		private boolean mStarted;
		private Socket mqttconnection;
		private MQTTSettings mqttsettings;
		private MQTTHandler mqtthandler;
		private MQTTHandler.MQTTAction ma;
		private AsyncTask<Socket, byte[], Void> inputhandler;
		private ArrayList<SubscriptionItem> subscriptions;
		private MQTTStartedListener mqttstartedlistener;

		
		public interface MQTTStartedListener
		{
			void onMQTTStarted();
		}
		
		/**
		 * Class used for the client Binder. Because we know this service always
		 * runs in the same process as its clients, we don't need to deal with
		 * IPC.
		 */
		public class LocalBinder extends Binder
			{
				public MQTTService getService()
					{
						// Return this instance of LocalService so clients can
						// call public methods
						return MQTTService.this;
					}
			}

		public MQTTService()
			{
				// TODO Auto-generated constructor stub
			}

		@Override
		public IBinder onBind(Intent intent)
			{
				// TODO Auto-generated method stub
				return mBinder;
			}

		@Override
		public void onCreate()
			{
				mStarted = false;
			}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId)
			{
				Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
				// If we get killed, after returning from here, restart
				return START_STICKY;
			}

		private class MsgContainer
			{
				public Socket sock;
				public byte[] msg;

				public MsgContainer(Socket sock, byte[] msg)
					{
						this.sock = sock;
						this.msg = msg;
					}
			};

		private void sendMqttMsg(byte[] msg)
			{
				MQTTInternals.dumpData("To Broker", msg);
				AsyncTask<MsgContainer, Void, Void> x = new AsyncTask<MsgContainer, Void, Void>()
					{

						@Override
						protected Void doInBackground(MsgContainer... params)
							{
								// TODO Auto-generated method stub
								MsgContainer mc = params[0];
								try
									{
										mc.sock.getOutputStream().write(mc.msg);
									}
								catch (IOException e)
									{
										Log.e("SendMqttMsg", "Error on os.write");
									}
								return null;
							}
					};
					MsgContainer mc = new MsgContainer(mqttconnection, msg);
					x.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mc);
			}

		private void startMqttListener()
			{
				subscriptions = new ArrayList<SubscriptionItem>();
				inputhandler = new AsyncTask<Socket, byte[], Void>()
					{

						@Override
						protected Void doInBackground(Socket... params)
							{
								InputStream in;
								try
									{
										in = params[0].getInputStream();
									}
								catch (IOException e1)
									{
										// TODO Auto-generated catch block
										return null;
									}
								byte[] buf = new byte[2048];
								int idx = 0;
								try
									{
										while (true)
											{
												int c = in.read();
												if(c < 0)
													{
														return null;
													}

												buf[idx++] = (byte) (c & 0xff);
												int rl = 0;
												int m = 1;
												do
													{
														c = in.read();
														if(c < 0)
															{
																return null;
															}
														rl += (c & 0x7f) * m;
														m *= 128;
														buf[idx++] = (byte) (c & 0xff);

													} while ((c & 0x80) > 0);

												byte[] msg = new byte[rl + idx];
												int i = 0;
												while (i < idx)
													msg[i] = buf[i++];

												while (rl > 0)
													{
														c = in.read();
														if(c < 0)
															{
																return null;
															}
														msg[idx++] = (byte) (c & 0xff);
														rl--;
													}

												MQTTInternals.dumpData("From Broker", msg);
												publishProgress(msg);
												idx = 0;
											}
									}
								catch (IOException e)
									{
										Log.e("MQTT Socket read", "Socket Error");
									}
								return null;
							}

						@Override
						protected void onProgressUpdate(byte[]... values)
							{
								for (int i = 0; i < values.length; i++)
									processMqttMessage(values[i]);
							}
					};
				inputhandler.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mqttconnection);
			}

		protected void createMqttMsgHandler()
			{
				ma = new MQTTHandler.MQTTAction()
					{

						@Override
						public void onUnsubscribe(int msgid, String[] topics)
							{
								// TODO Auto-generated method stub

							}

						@Override
						public void onUnSubAck(int msgid)
							{
								// TODO Auto-generated method stub

							}

						@Override
						public void onSubscribe(MQTTSubscription[] subs)
							{
								// TODO Auto-generated method stub

							}

						@Override
						public void onSubAck(int msgid, byte[] payload)
							{
								// TODO Auto-generated method stub

							}

						@Override
						public void onPublish(String topic, byte[] payload, int msgid, boolean dup, int qos, boolean retain)
							{
								switch (qos)
									{
										case 1:
											sendMqttMsg(MQTTInternals.formatPubAckMsg(msgid));
											break;
										case 2:
											sendMqttMsg(MQTTInternals.formatPubRecMsg(msgid));
											break;
										default:
											break;
									}

								for (int i = 0; i < subscriptions.size(); i++)
									{
										SubscriptionItem item = subscriptions.get(i);
										if (MQTTInternals.matches(topic, item.topic))
											{
												item.callback.onPublished(topic, payload);
											}
									}
							}

						@Override
						public void onPubRel(int msgid)
							{
								sendMqttMsg(MQTTInternals.formatPubCompMsg(msgid));
							}

						@Override
						public void onPubRec(int msgid)
							{
								sendMqttMsg(MQTTInternals.formatPubRelMsg(msgid));
							}

						@Override
						public void onPubComp(int msgid)
							{
							}

						@Override
						public void onPubAck(int msgid)
							{
								// TODO Auto-generated method stub

							}

						@Override
						public void onPingResp()
							{
								// TODO Auto-generated method stub

							}

						@Override
						public void onPingReq()
							{
								// TODO Auto-generated method stub

							}

						@Override
						public void onDisconnect()
							{
								// TODO Auto-generated method stub

							}

						@Override
						public void onConnAck(int result)
							{
								if (result == 0)
									{
									}
							}
					};
			}

		protected void processMqttMessage(byte[] msg)
			{
				mqtthandler.interpretMQTT(ma, msg);
			}

		private void addSubscriptionWatcher(String topic, MQTTHandler.MQTTPublish callback)
			{
				SubscriptionItem item = new SubscriptionItem();
				item.topic = topic;
				item.callback = callback;
				subscriptions.add(item);
			}

		/** method for clients */
		public synchronized boolean startMqtt(MQTTSettings settings,MQTTStartedListener startcallback)
			{
				mqttstartedlistener = startcallback;
				if (mStarted)
					return false;
				else
					{
						mqttsettings = settings;
						mqtthandler = new MQTTHandler();
						createMqttMsgHandler();
						AsyncTask<MQTTSettings, Socket, Integer> x = new AsyncTask<MQTTSettings, Socket, Integer>()
							{

								@Override
								protected Integer doInBackground(MQTTSettings... params)
									{
										// SocketAddress sockaddr = new
										// InetSocketAddress("192.168.1.126",
										// 4321);
										SocketAddress sockaddr = new InetSocketAddress(params[0].getHost(), params[0].getPort());
										Socket s = new Socket();
										int ret = 1;
										try
											{
												s.connect(sockaddr);
												byte[] msg = MQTTInternals.formatConnectMsg(mqttsettings.getClientID(),
														mqttsettings.getUsername(), mqttsettings.getPassword(), "Android/Active", "No", 1,
														true, true, 0);
												s.getOutputStream().write(msg);
												
												msg = new byte[4];
												int n = s.getInputStream().read(msg);
												while(n<0)
													n = s.getInputStream().read(msg);
												
												if( n == 4 && msg[3] == 0)
													{
														publishProgress(s);
														ret = 0;
													}
											}
										catch (IOException e)
											{
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										return Integer.valueOf(ret);
									}

								protected void onProgressUpdate(Socket... values)
									{
										mqttconnection = values[0];
										startMqttListener();
										mStarted = true;
										byte[] msg = MQTTInternals.formatPublishMsg("Android/Active", "Yes", 1, true);
										sendMqttMsg(msg);
										mqttstartedlistener.onMQTTStarted();
									}

							};
						x.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, settings);
					}
				return true;
			}

		public synchronized boolean isConnected()
			{
				boolean ret = false;
				if (mqttconnection != null && mqttconnection.isConnected() && mStarted && inputhandler.getStatus() == AsyncTask.Status.RUNNING)
					ret = true;
				return ret;
			}

		public synchronized boolean stopMqtt()
			{
				if (mStarted)
					{
						mStarted = false;
						inputhandler.cancel(true);
						AsyncTask<Socket, Void, Void> x = new AsyncTask<Socket, Void, Void>()
							{

								@Override
								protected Void doInBackground(Socket... params)
									{
										try
											{
												params[0].getOutputStream().write(MQTTInternals.formatDisconnectMsg());
												params[0].close();
											}
										catch (IOException e)
											{
											}
										return null;
									}
							};
						x.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mqttconnection);

					}
				else
					return false;

				return true;
			}

		public synchronized boolean publishTopic(String topic, byte[] payload, int qos, boolean retain)
			{
				boolean ret = false;
				if (mStarted)
					{
						ret = true;
						byte[] msg = MQTTInternals.formatPublishMsg(topic, payload, qos, retain);
						sendMqttMsg(msg);
					}
				return ret;
			}

		public synchronized boolean publishTopic(String topic, String payload, int qos, boolean retain)
			{
				boolean ret = false;
				if (mStarted)
					{
						ret = true;
						byte[] msg = MQTTInternals.formatPublishMsg(topic, payload, qos, retain);
						sendMqttMsg(msg);
					}
				return ret;
			}

		public synchronized boolean subscribe(MQTTSubscription[] subs, MQTTHandler.MQTTPublish callback)
			{
				boolean ret = false;
				if (mStarted)
					{
						byte[] msg = MQTTInternals.formatSubscribeMsg(subs,true, false);
						for (int i = 0; i < subs.length; i++)
							addSubscriptionWatcher(subs[i].topic, callback);
						sendMqttMsg(msg);
						ret = true;
					}

				return ret;
			}

		public synchronized boolean subscribe(String topic, int qos, MQTTHandler.MQTTPublish callback)
			{
				MQTTSubscription[] x = new MQTTSubscription[1];
				x[0] = new MQTTSubscription(topic, qos);
				return subscribe(x, callback);
			}

		public synchronized boolean subscribe(String topic, MQTTHandler.MQTTPublish callback)
			{
				return subscribe(topic, 1, callback);
			}
	}
