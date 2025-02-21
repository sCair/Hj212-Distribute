package com.sctech.hj212distribute;

import com.sctech.hj212distribute.socket.StringClient;
import com.sctech.hj212distribute.socket.StringServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Hj212DistributeApplication {

	public static void main(String[] args) {
		//SpringApplication.run(Hj212DistributeApplication.class, args);
		try {
			StringServer.startServer();
			StringClient.startClient();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

}
