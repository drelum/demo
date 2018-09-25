package br.com.acordocerto.teste;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.inject.Inject;

@Controller
@Configuration
public class SistemaController {
	private final AtomicLong counter = new AtomicLong();
	private final AtomicBoolean ligado = new AtomicBoolean(true);
	
	private String versao = "1";
	
	@Autowired
	private Credor credor;
	
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
	
	@GetMapping("/ip")
    @ResponseBody
    public ResponseEntity<String> ip() throws SocketException {
		
		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		String host = "";
		while(e.hasMoreElements())  {
			NetworkInterface n = (NetworkInterface) e.nextElement();
		    Enumeration<InetAddress> ee = n.getInetAddresses();
		    while (ee.hasMoreElements())
		    {
		    	InetAddress inetAddress = ee.nextElement();
		    	
		    	
		    	
		    	if (inetAddress instanceof Inet6Address ||  inetAddress.isLoopbackAddress())
		    		continue;
		    	
		    	host = credor.getCredor() + " v " + versao + ": " + inetAddress.getHostAddress();
		    	System.out.println(host);
		    	
		    
		    	
		    }
		}
		
        return new ResponseEntity<String>(counter.incrementAndGet() +  " - Host: " + host, HttpStatus.OK) ;
    }
	
	@GetMapping("/")
    @ResponseBody
    public ResponseEntity<String> index() {
		if (ligado.get())
			return new ResponseEntity<String>("<html>FUNCIONANDO</html>", HttpStatus.OK);
		else
			return new ResponseEntity<String>("<html>QUEBRADO!!</html>", HttpStatus.SERVICE_UNAVAILABLE);
    }
	
	@GetMapping("/shutdown")
    @ResponseBody
    public ResponseEntity<String> shudown() {
		ligado.set(false);
        return new ResponseEntity<String>(ligado.get()+"", HttpStatus.OK) ;
    }
	
	@GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> health() throws Exception {

		if (ligado.get())
	        return new ResponseEntity<String>("OK", HttpStatus.OK) ;
		
		return new ResponseEntity<String>("NOK", HttpStatus.SERVICE_UNAVAILABLE) ;
    }
	
}
