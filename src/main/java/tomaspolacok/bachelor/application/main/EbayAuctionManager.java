package tomaspolacok.bachelor.application.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


@ComponentScan({"tomaspolacok.bachelor.application.repositories",
	"tomaspolacok.bachelor.application.entities",
	"tomaspolacok.bachelor.application.config",
	"tomaspolacok.bachelor.application.services",
	"tomaspolacok.bachelor.application.controllers",
	"tomaspolacok.bachelor.application.util"})
@EnableJpaRepositories(basePackages = { "tomaspolacok.bachelor.application.repositories"})
@EntityScan("tomaspolacok.bachelor.application.entities")
@EnableScheduling
@EnableCaching
@SpringBootApplication
public class EbayAuctionManager {

	public static void main(String[] args) {
		SpringApplication.run(EbayAuctionManager.class, args);
	}
}
