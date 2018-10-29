package co.dporn.gmd.servlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackageClasses={DpornCoBoot.class})
@EnableAutoConfiguration
public class DpornCoBoot extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(DpornCoBoot.class);
	}
}
