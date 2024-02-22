package bachelor.chessDatabase;

import bachelor.chessDatabase.Service.FileParsingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ChessDatabaseApplication implements CommandLineRunner {
	private final FileParsingService fileParsingService;

	public ChessDatabaseApplication(FileParsingService service){
		this.fileParsingService = service;
	}


	public static void main(String[] args) {
		SpringApplication.run(ChessDatabaseApplication.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("LETS GO");
		fileParsingService.parseFiles("test.pgn");
	}
}
