package com.thomassnow.xmastree.network;

import com.thomassnow.xmastree.data.MQTTSubscription;

public class MQTTHandler
	{
		private MQTTAction listener;
		private int curmsgtype;
		private int curmsgid;
		private boolean curdup;
		private int curqos;
		private boolean curretain;
		private int idx;
		private int remaininglen;
		
		
		public interface MQTTPublish
		{
			void onPublished(String topic,byte [] payload);
		}

		public interface MQTTAction
		{
			void onConnAck(int result);
			void onPublish(String topic,byte [] payload,int msgid,boolean dup, int qos, boolean retain);
			void onPubAck(int msgid);
			void onPubRec(int msgid);
			void onPubRel(int msgid);
			void onPubComp(int msgid);
			void onSubscribe(MQTTSubscription[] subs);
			void onSubAck(int msgid,byte[] payload);
			void onUnsubscribe(int msgid,String [] topics);
			void onUnSubAck(int msgid);
			void onPingReq();
			void onPingResp();
			void onDisconnect();
		}
		
		public MQTTHandler()
			{
			}
		
		private void parsePayloadLength(byte [] msg)
			{
				idx = 1;
				int multiplier = 1;
				remaininglen = 0;
				do
					{
						remaininglen += (msg[idx] & 0x7f) * multiplier;
						multiplier *= 128;
					} while((msg[idx++] & 0x80) > 0);
				
			}
		private void parseHeader(byte [] msg)
			{
				curmsgtype = msg[0] >> 4;
				curdup = (msg[0] & 0x08) > 0;
				curqos = (msg[0] >> 1) & 3;
				curretain = (msg[0] & 1) > 0;
				parsePayloadLength(msg);
			}
		
		private int parseInt(byte [] msg)
			{
				int ret = msg[idx++] * 256;
				ret += msg[idx++];
				return ret;
			}
		
		private String parseString(byte [] msg)
			{
				int topiclen = parseInt(msg);
				byte [] str = new byte[topiclen];
				int x = 0;
				while(topiclen > 0)
					{
						str[x++] = msg[idx++];
						topiclen--;
					}
				return new String(str);
			}
		
		private void parseMessageID(byte [] msg)
			{
				curmsgid = parseInt(msg);
			}
		
		private byte [] parseRemaining(byte [] msg)
			{
				byte [] ret = new byte[remaininglen];
				int i = 0;
				while(remaininglen > 0)
					{
						ret[i++] = msg[idx++];
						remaininglen--;
					}
				
				return ret;
			}
		
		private void parsePublish(byte [] msg)
			{
				String topic = parseString(msg);
				if(curqos > 0)
					{
					parseMessageID(msg);
					remaininglen -= 2;
					}
				remaininglen -= topic.length() + 2;
				byte [] payload = parseRemaining(msg);
				listener.onPublish(topic, payload, curmsgid, curdup, curqos, curretain);
			}
		
		private void parseMessage(byte [] msg)
			{
				parseHeader(msg);
				switch(curmsgtype)
				{
					case 0: // invalid
						break;
					case 1: // connect - we're not a broker
						break;
					case 2:	// connack
						listener.onConnAck(msg[3]);
						break;
					case 3: // publish
						parsePublish(msg);
						break;
					case 4: // puback;
						parseMessageID(msg);
						listener.onPubAck(curmsgid);
						break;
					case 5:	// pubrec
						parseMessageID(msg);
						listener.onPubRec(curmsgid);
						break;
					case 6:	// pubrel
						parseMessageID(msg);
						listener.onPubRel(curmsgid);
						break;
					case 7: // pubcomp
						parseMessageID(msg);
						listener.onPubComp(curmsgid);
						break;
					case 8:	// subscribe
						break;	// not a broker
					case 9:	// suback
						parseMessageID(msg);
						remaininglen -= 2;
						byte [] payload = parseRemaining(msg);
						listener.onSubAck(curmsgid, payload);
						break;
					case 10:	// unsubscribe
						break;	// not a broker
					case 11:	// unsuback
						parseMessageID(msg);
						listener.onUnSubAck(curmsgid);
						break;
					case 12:	// pingreq
						listener.onPingReq();
						break;
					case 13:	// pingresp
						listener.onPingResp();
						break;
					case 14:	// disconnect
						listener.onDisconnect();
						break;
					case 15:	// reserved
					default:
						break;
				}
			}
		
		public void interpretMQTT(MQTTAction owner,byte [] msg)
			{
				listener = owner;
				parseMessage(msg);
			}

	}
