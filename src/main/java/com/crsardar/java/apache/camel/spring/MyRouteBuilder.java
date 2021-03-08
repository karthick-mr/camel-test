package com.crsardar.java.apache.camel.spring;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.Main;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

public class MyRouteBuilder extends RouteBuilder {

    /**
     * Allow this route to be run as an application
     */
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    public void configure() {
        // populate the message queue with some messages

        // 1. Route path = file (src/data)  > MQ (crsardar.TestMessageQueue) > Console
        from("file:src/data?noop=true")                 // Dependency 'artifactId>camel-file</artifactId>', 'noop=true' - do not delete file after consumption
                .to("jms:crsardar.TestMessageQueue");       // Dependency 'artifactId>activemq-broker</artifactId>'

        from("jms:crsardar.TestMessageQueue")
                .to("stream:out");                          // Dependency '<artifactId>camel-stream</artifactId>'

        /*
        from("jms:crsardar.TestMessageQueue").
                to("stream:out").
                to("file://target/test"); // It will also work - one source, multiple destinations
        */

        // 2. Rute path = Camel Timer > > MQ (crsardar.TimerMessageQueue) > Console
        from("timer://testTimer?period=2000")       // Dependency '<artifactId>camel-timer</artifactId>'
                .setBody()
                .simple("Hello World! At " + LocalDateTime.now())
                .to("jms:crsardar.TimerMessageQueue");

        from("jms:crsardar.TimerMessageQueue")
                .bean(SomeBean.class, "someMethod");            // Dependency '<artifactId>camel-bean</artifactId>'
    }

    @Component
    public static class SomeBean {

        public void someMethod(String body) {

            System.out.println("Received: " + body);
        }
    }
}
