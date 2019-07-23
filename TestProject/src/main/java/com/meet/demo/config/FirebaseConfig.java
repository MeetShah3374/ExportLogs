package com.meet.demo.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * @author Meet 15-July-2019
 */
@Configuration
@ConfigurationProperties(prefix = "firebase")

public class FirebaseConfig {
	private String json;

	private String url;

	private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

	@PostConstruct
	public void init() {
		try (InputStream inputStream = new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8")))) {
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(inputStream)).setDatabaseUrl(url).build();
			FirebaseApp.initializeApp(options);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// for getting database reference
	@Bean
	public DatabaseReference firebaseDatabase() {
		DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

		return databaseReference;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
