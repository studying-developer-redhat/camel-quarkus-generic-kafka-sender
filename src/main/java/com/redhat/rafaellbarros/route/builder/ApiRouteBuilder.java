package com.redhat.rafaellbarros.route.builder;

import com.redhat.rafaellbarros.model.Message;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.model.rest.RestBindingMode;

public class ApiRouteBuilder extends RouteBuilder {

    protected String KAFKA_TOPIC = "{{quarkus.openshift.env.vars.kafka-topic}}";
    protected String KAFKA_BOOTSTRAP_SERVERS = "{{quarkus.openshift.env.vars.kafka-bootstrap-servers}}";

    @Override
    public void configure() {

        // REST and Open API configuration
        restConfiguration().bindingMode(RestBindingMode.auto)
                .component("platform-http")
                .dataFormatProperty("prettyPrint", "true")
                .contextPath("/").port(8080)
                .apiContextPath("/openapi")
                .apiProperty("api.title", "Camel Quarkus Generic Kafka API Send Demo")
                .apiProperty("api.version", "1.0.0-SNAPSHOT")
                .apiProperty("cors", "true");

        // REST methods configuration
        rest("/rest").tag("API Send Demo using Camel and Quarkus")
                .produces("application/json")
                .post("/messages")
                .consumes("application/json")
                .type(Message.class)
                .description("Send payload to kafka")
                .route().routeId("setPayloadRest")
                .convertBodyTo(Message.class)
                .to("direct:sendToKafka")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .endRest();

        //Route that sends message to kafka topic
        from("direct:sendToKafka")
                .routeId("setPayloadKafka")
                .marshal().json()
                .setHeader(KafkaConstants.KEY, constant("Camel")) // Key of the message
                .to("kafka:"+ KAFKA_TOPIC + "?brokers=" + KAFKA_BOOTSTRAP_SERVERS)
                .log("Response Body: ${body}")
                .setBody(simple("${body}"));
    }
}