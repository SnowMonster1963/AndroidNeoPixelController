package com.thomassnow.xmastree.data;

import android.util.Log;

import com.thomassnow.xmastree.network.MQTTHandler;

public class MQTTInternals
	{

		private static int mastermsgid = 0;

		MQTTInternals()
			{

			}
		
		private static String getTopicElement(String topic, int idx)
			{
				String ret = "";
				int start = 0;
				int end = topic.indexOf('/', start);
				int x = 0;
				while(x < idx && end > 0)
					{
						start = end + 1;
						end = topic.indexOf('/', start);
						x++;
					}
				
				if(x == idx)
					{
						if(end > 0)
							ret = topic.substring(start, end);
						else
							ret = topic.substring(start);
					}
				
				return ret;
			}
		
		public static boolean matches(String topic,String pattern)
			{
				boolean b = true;
				boolean ret = true;
				int idx = 0;
				while(b)
					{
						String te = getTopicElement(topic,idx);
						String pe = getTopicElement(pattern,idx++);
						if(te.compareTo(pe) != 0)
							{
								if(pe.compareTo("#") == 0)
									return true;
								int cp = pe.compareTo("+");
								if(cp != 0)
									return false;
							}
						if(te.length() == 0)
							{
								if(pe.length() != 0)
									return false;
								else
									return true;
							}
					}
				return ret;
			}
		
		private static int insertString(byte [] x,int idx,String s)
			{
				int slen = s.length();
				
				x[idx++] = (byte) ( slen >> 8);
				x[idx++] = (byte) (slen & 0xff);
				int i = 0;
				byte [] chars = s.getBytes();
				while(slen > 0)
					{
					x[idx++] = chars[i++];
					slen--;
					}
				return idx;
			}

		public static byte[] formatConnectMsg(String clientid, String username, String password, String willtopic, String willpayload,
				int willqos, boolean willretain, boolean cleansession, int keepalive)
			{
				int payloadlen = 12; // header length
				payloadlen += clientid.length() + 2;
				if (willtopic.length() > 0)
					payloadlen += willtopic.length() + 2;
				if (willpayload.length() > 0)
					payloadlen += willpayload.length() + 2;
				if (username.length() > 0)
					payloadlen += username.length() + 2;
				if (password.length() > 0)
					payloadlen += password.length() + 2;

				int len = 2;
				if(payloadlen > 127)
					len++;
				if(payloadlen > 16383)
					len++;
				if(payloadlen > 2097151)
					len++;
				

				byte[] ret = new byte[len + payloadlen];
				int idx = 0;
				
				ret[idx++] = 0x10;	// Connect Message
				do
					{
						ret[idx] = (byte) (payloadlen % 128);
						payloadlen /= 128;
						if(payloadlen > 0)
							ret[idx] |= 0x80;
						idx++;
					} while(payloadlen > 0);
				
				idx = insertString(ret,idx,"MQIsdp");
				ret[idx++] = 3;
				
				ret[idx] = 0;
				if(username.length() > 0)
					ret[idx] |= 0x80;
				
				if(password.length() > 0)
					ret[idx] |= 0x40;
				
				if(willretain)
					ret[idx] |= 0x20;
				
				ret[idx] |= (byte)((willqos & 3) << 2);
				
				if(willtopic.length() > 0)
					ret[idx] |= 0x02;

				if(cleansession)
					ret[idx] |= 0x01;
				idx++;
				
				ret[idx++] = (byte)(keepalive >> 8);
				ret[idx++] = (byte)(keepalive & 0xff);
				idx = insertString(ret,idx,clientid);
				
				if(willtopic.length() > 0)
					idx = insertString(ret,idx,willtopic);
				if(willpayload.length() > 0)
					idx = insertString(ret,idx,willpayload);
				if(username.length() > 0)
					idx = insertString(ret,idx,username);
				if(password.length() > 0)
					idx = insertString(ret,idx,password);
				return ret;

			}

		public static byte[] formatDisconnectMsg()
			{
				byte [] ret = new byte[2];
				ret[0] = (byte) 0xe0;
				ret[1] = 0;
				return ret;
			}

		public static byte[] formatPubAckMsg(int msgid)
			{
				byte [] ret = new byte[4];
				ret[0] = (byte) 0x40;
				ret[1] = 2;
				ret[2] = (byte) ((msgid >> 8)& 0xff);
				ret[3] = (byte) (msgid & 0xff);
				return ret;
			}
		
		public static byte[] formatPubRecMsg(int msgid)
			{
				byte [] ret = new byte[4];
				ret[0] = (byte) 0x50;
				ret[1] = 2;
				ret[2] = (byte) ((msgid >> 8)& 0xff);
				ret[3] = (byte) (msgid & 0xff);
				return ret;
			}
		
		public static byte[] formatPubRelMsg(int msgid)
			{
				byte [] ret = new byte[4];
				ret[0] = (byte) 0x60;
				ret[1] = 2;
				ret[2] = (byte) ((msgid >> 8)& 0xff);
				ret[3] = (byte) (msgid & 0xff);
				return ret;
			}
		
		public static byte[] formatPubCompMsg(int msgid)
			{
				byte [] ret = new byte[4];
				ret[0] = (byte) 0x70;
				ret[1] = 2;
				ret[2] = (byte) ((msgid >> 8)& 0xff);
				ret[3] = (byte) (msgid & 0xff);
				return ret;
			}
		
		public static byte[] formatPublishMsg(String topic, String payload,	int qos, boolean retain)
			{
				return formatPublishMsg(topic,payload.getBytes(),qos,retain);
			}
		
		public static byte[] formatPublishMsg(String topic, byte []payload,	int qos, boolean retain)
			{
				int msglen = 2;
				msglen += topic.length() + 2;
				msglen += payload.length;
				if(qos > 0)
					msglen += 2;
				
				int remaininglen = topic.length() + 2;
				remaininglen += payload.length;
				if(qos > 0)
					remaininglen += 2;
				

				if(remaininglen > 127)
					msglen++;
				if(remaininglen > 16383)
					msglen++;
				if(remaininglen > 2097151)
					msglen++;
				

				byte[] ret = new byte[msglen];
				int idx = 0;
				
				ret[idx++] = (byte) (0x30 | ((qos&3) << 1) | (retain ? 1 : 0));	// Connect Message
				do
					{
						ret[idx] = (byte) (remaininglen % 128);
						remaininglen /= 128;
						if(remaininglen > 0)
							ret[idx] |= 0x80;
						idx++;
					} while(remaininglen > 0);
				
				idx = insertString(ret,idx,topic);
				if(qos > 0)
					{
						nextMsgID();
						ret[idx++] = (byte)(mastermsgid>>8);
						ret[idx++] = (byte)(mastermsgid&0xff);
					}
				for(int i=0;i<payload.length;i++)
					ret[idx++] = payload[i];
				
				return ret;

			}
		
		private static void nextMsgID()
			{
				mastermsgid = (mastermsgid + 1) & 0xffff;
				if(mastermsgid == 0)
					mastermsgid++;
			}

		public static byte[] formatSubscribeMsg(MQTTSubscription [] subscriptions,boolean qos,boolean dup)
			{
				int msglen = 2;
				int remaininglen = 0;
				for(int i=0;i<subscriptions.length;i++)
					{
						remaininglen += subscriptions[i].topic.length() + 3;	// 2 for len and 1 for qos
					}

				if(remaininglen > 127)
					msglen++;
				if(remaininglen > 16383)
					msglen++;
				if(remaininglen > 2097151)
					msglen++;

				byte cmd = (byte) 0x80;
				if(dup)
					cmd |= 0x08;
				
				if(qos) // qos
					{
					cmd |= 0x02;
					remaininglen += 2; // msg id
					}

				int idx = 0;
				msglen += remaininglen;

				byte[] ret = new byte[msglen];
				

				ret[idx++] = cmd;
				do
					{
						ret[idx] = (byte) (remaininglen % 128);
						remaininglen /= 128;
						if(remaininglen > 0)
							ret[idx] |= 0x80;
						idx++;
					} while(remaininglen > 0);
				
				if(qos)
					{
						nextMsgID();
						ret[idx++] = (byte)(mastermsgid>>8);
						ret[idx++] = (byte)(mastermsgid&0xff);
					}
				
				for(int i=0;i<subscriptions.length;i++)
					{
						idx = insertString(ret,idx,subscriptions[i].topic);
						ret[idx++] = (byte)(subscriptions[i].qos & 3);
					}
				
				return ret;

			}
		
		private static String toHex1(byte b)
			{
				char x = (char) ((b &0xf) + '0');
				if(x > '9')
					x += 7;
				
				String ret = "";
				ret += x;
				
				return ret;
			}
		
		public static String toHex2(byte b)
			{
				String ret = "";
				
				byte nyb = (byte) (b >> 4);
				ret = toHex1(nyb);
				ret += toHex1(b);
				
				return ret;
			}
		
		public static String toHex4(int i)
			{
				String ret = toHex2((byte) (i >> 8));
				ret += toHex2((byte) i);
				return ret;
			}
		
		public static void dumpData(String msg,byte [] data)
			{
				String tag = "Data Dump";
				Log.d(tag,msg);
				String hex = "";
				String chars = "";
				int i = 0;
				int linelength = 8;
				while(i < data.length)
					{
						if((i%linelength == 0))
							{
								hex = toHex4(i) + ": ";
								chars = "/* ";
							}
						
						if((i%4) == 0)
							hex += " ";
						
						byte b = data[i++];
						hex += toHex2(b) + " ";
						if(b < 0x20 || b > 0x7e)
							b = '.';
						char c = (char) b;
						chars += c;
						if((i%linelength == 0))
							{
								Log.d(tag,hex + " " + chars + " */");
							}						
					}
				
				if((i%linelength) != 0)
					{
					while((i%linelength) != 0)
						{
							hex += "   ";
							chars += " ";
							if((i%4) == 0)
								hex += " ";
							i++;
						}
					Log.d(tag,hex + " " + chars);
					}
			}

	}

