package connector;

import connector.containers.LdesServerContainer;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ServerSeeder {
	private final String exampleContent = """
			@prefix data:   <http://data.linkedmdb.org/resource/data/> .
			@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
			@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
			@prefix ex:    <http://example.org/sample/> .
			@prefix terms: <http://purl.org/dc/terms/> .
			   
			<http://example.org/resource/data/{id}>
				  a 	  ex:Data;
				  terms:isVersionOf <http://example.org/resource/data> ;
			      rdfs:label "sample" ;
			      data:timestamp "{timestamp}" .
			""";

	private final AtomicInteger counter;
	private final LdesServerContainer server;

	public ServerSeeder(LdesServerContainer server) {
		this.server = server;
		this.counter = new AtomicInteger();
	}

	public void sendData(String eventStream, int times) {
		IntStream.range(0, times)
				.forEach((i) -> server.postMember(eventStream, exampleContent
						.replace("{id}", String.valueOf(counter.incrementAndGet()))
						.replace("{timestamp}", LocalDateTime.now().toString())));
	}


}
