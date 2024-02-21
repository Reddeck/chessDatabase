package bachelor.chessDatabase.Service;

import bachelor.chessDatabase.Entity.PlayerEntity;
import bachelor.chessDatabase.Repository.PlayerRepository;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class FileParsingService{
    private final GameHandlingService gameHandler;

    public FileParsingService(GameHandlingService gameHandler){
        this.gameHandler = gameHandler;
    }

    public void parseFiles(String... files){
        for (String file : files) {
            PgnHolder pgn = new PgnHolder("games/" + file);
            try {
                pgn.loadPgn();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            int count = 0;

            /*
            for(Game game : pgn.getGames()) {
                count++;
                gameHandler.handleGame(game, count);
            }*/

            gameHandler.handleGame(pgn.getGames().get(0), 1);
            System.out.println(count);
        }
    }
}
