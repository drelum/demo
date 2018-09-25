package br.com.acordocerto.teste;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Configuration
class CredorNet {

	@Profile("net")
	@Bean
	public Credor getCredorNet() {
		return new Credor("Net");
	}
	
	
	@Profile("claro")
	@Bean
	public Credor getCredorClaro() {
		return new Credor("Claro");
	}
	
	@Profile("tribanco")
	@Bean
	public Credor getCredorTribanco() {
		return new Credor("Tribanco");
	}
	
	@Profile("santander")
	@Bean
	public Credor getCredorSantander() {
		return new Credor("Santander");
	}
}
