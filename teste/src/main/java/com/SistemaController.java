package com;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class SistemaController {
	private final AtomicLong counter = new AtomicLong();
	private final AtomicBoolean ligado = new AtomicBoolean(true);
	
	@GetMapping("/hello-world")
    @ResponseBody
    public ResponseEntity<String> sayHello(@RequestParam(name="name", required=false, defaultValue="Stranger") String name) {
        return new ResponseEntity<String>(counter.incrementAndGet() +  " oi " + name, HttpStatus.ACCEPTED) ;
    }
	
	@GetMapping("/shudown")
    @ResponseBody
    public ResponseEntity<String> shudown() {
		ligado.set(false);
        return new ResponseEntity<String>(ligado.get()+"", HttpStatus.ACCEPTED) ;
    }
	
	@GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> health() throws Exception {

		if (ligado.get())
	        return new ResponseEntity<String>("OK", HttpStatus.ACCEPTED) ;
		
		return new ResponseEntity<String>("NOK", HttpStatus.SERVICE_UNAVAILABLE) ;
    }
	
}
