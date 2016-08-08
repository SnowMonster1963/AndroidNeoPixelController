package com.thomassnow.xmastree.data;

public class MQTTSubscription
	{
		public String topic;
		public int qos;

		public MQTTSubscription(String topic,int qos)
			{
				this.topic = topic;
				this.qos = qos;
			}

	}
