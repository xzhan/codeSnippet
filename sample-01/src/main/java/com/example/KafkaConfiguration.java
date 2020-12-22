package com.example;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableKafka
public class KafkaConfiguration {

	@Autowired
	KafkaProperties kafkaProperties;

	@Bean("KAFKA_TEMPLATE_NOTIFICATION")
	public KafkaTemplate<Object, Object> kafkaTemplate() {
		ProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(
				kafkaProperties.buildProducerProperties());
		KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(factory);
		return kafkaTemplate;
	}

}
