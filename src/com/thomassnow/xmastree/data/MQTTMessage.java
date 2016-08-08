package com.thomassnow.xmastree.data;

public class MQTTMessage
	{
		private String topic;
		private byte [] payload;
		
		public MQTTMessage(String topic,byte [] payload)
		{
			this.setTopic(topic);
			this.setPayload(payload);
		}

		public String getTopic()
			{
					return topic;
			}

		public void setTopic(String topic)
			{
					this.topic = topic;
			}

		public byte [] getPayload()
			{
					return payload;
			}

		public void setPayload(byte [] payload)
			{
					this.payload = payload;
			}

	}
