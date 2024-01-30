package bachelor.chessDatabase;

import bachelor.chessDatabase.Service.FileParsingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChessDatabaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChessDatabaseApplication.class, args);
		new FileParsingService("wrap23.pgn").run();
	}

}
