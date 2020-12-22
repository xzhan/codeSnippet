/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;


import com.alibaba.fastjson.JSONObject;
import com.common.Foo1;
import com.common.message.meeting.MeetingMessage;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Gary Russell
 * @since 2.2.1
 */
@RestController
public class Controller {

	private final Logger logger = LoggerFactory.getLogger(Controller.class);

	private static final AtomicInteger PRODUCER_MEETING_MESSAGE = new AtomicInteger(1500);

	@Autowired(required = false)
	@Qualifier(value = "KAFKA_TEMPLATE_NOTIFICATION")
	private KafkaTemplate<Object, Object> template;

	@PostMapping(path = "/send/foo/{what}")
	public void sendFoo(@PathVariable String what) {
		this.template.send("topic1", new Foo1(what));
	}

	@GetMapping(path = "/send/meetingMessage/{number}")
	public void sendMeetingMessage(@PathVariable String number) {
		int index = Integer.valueOf(number);
		MeetingMessage meetingMessage = new MeetingMessage();
		File file = new File("/Users/xzhan/spring-project/spring-kafka/samples/sample-01/src/main/resources/smallMeetingMessage.json");
		try {
			String message = FileUtils.readFileToString(file);
			 meetingMessage = JSONObject.parseObject(message, MeetingMessage.class);
		} catch (IOException e){
			logger.error(e.getMessage());
		}

		  while(index >0){
			this.template.send("hmdev1_meeting_meeting_webex_notification", generateMeetingMessage(meetingMessage));
		  index --;
		}
	}

	private MeetingMessage generateMeetingMessage(MeetingMessage meetingMessage) {

			long timestamp = System.currentTimeMillis();
			int clue = PRODUCER_MEETING_MESSAGE.getAndIncrement();
			meetingMessage.setTimestamp(timestamp);
			meetingMessage.setMessageUUID("MeetingMessage - " +clue);
			String index = " generate meetingMessage index :  "  + clue;
			logger.info(index + " timestamp: " +timestamp);
			return  meetingMessage;

	}


}

