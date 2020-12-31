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
import com.github.benmanes.caffeine.cache.Cache;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
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

	private static final AtomicInteger PRODUCER_MEETING_MESSAGE = new AtomicInteger(0);

	@Autowired  CacheManager cacheManager;

	@Autowired(required = false)
	@Qualifier(value = "KAFKA_TEMPLATE_NOTIFICATION")
	private KafkaTemplate<Object, Object> template;

	@PostMapping(path = "/send/foo/{what}")
	public void sendFoo(@PathVariable String what) {
		this.template.send("topic1", new Foo1(what));
	}

	@GetMapping(path = "/send/meetingMessage/{number}")
	public void sendMeetingMessage(@PathVariable String number) throws InterruptedException {
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
		  	//Thread.sleep(1000);
			meetingMessage = generateMeetingMessage(meetingMessage);
			boolean isSucces = putIntoCache2(meetingMessage.getMeetingUUID());
			if(isSucces) {
				this.template.send("hmdev1_meeting_meeting_webex_notification", meetingMessage);
			} else{
				logger.error("ignore the message index " +index);
			}
			  Thread.sleep(2000);
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

	/**
	 * computeIfPresent will trigger replace and reset the timestamp , only not update will expire
	 * finally the  key , and not expire count ++
	 * @param key
	 * @return
	 */

	public boolean putIntoCache1(String key){

		CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("abnormalMessageCache");
		Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
		Object vl = nativeCache.asMap().computeIfAbsent(key, s -> 0);
		Integer sum = (Integer) nativeCache.asMap().computeIfPresent(key ,(s, num) -> (Integer) num + 1);
		nativeCache.asMap().forEach((k,v) -> logger.info(k + " : " + v));
		return sum <= 5;

	}

	/**
	 * only get ,after the first key write into cache after 12s it will be expire
	 * AtomicInteger  An {@code int} value that may be updated atomically
	 *
	 * update value not update Key will not trigger the write value expireAfterWrite logical
	 * @param key
	 * @return
	 */

	public boolean putIntoCache2(String key){

		CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("abnormalMessageCache");
		Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
		AtomicInteger atomicInteger = (AtomicInteger) nativeCache.get(key, s -> new AtomicInteger(0));
		int sum = atomicInteger.addAndGet(1);
		nativeCache.asMap().forEach((k,v) -> logger.info(k + " : " + v));
		return sum <= 5;

	}
}

