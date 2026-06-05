package com.blemberg;

import com.blemberg.marketdata.application.MarketDataProperties;
import com.blemberg.providers.fred.FredProperties;
import com.blemberg.providers.twelvedata.TwelveDataProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({
    MarketDataProperties.class,
    TwelveDataProperties.class,
    FredProperties.class
})
public class BlembergApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlembergApplication.class, args);
    }
}
