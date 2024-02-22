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
            long start = System.currentTimeMillis();
            /*
            for(Game game : pgn.getGames()) {
                count++;
                gameHandler.handleGame(game, count);
            }*/
    /*
            var all = pgn.getGames();
            while (count < 100){
                var game = all.get(count);
                count++;
                gameHandler.handleGame(game, count);
            }*/

            gameHandler.handleGame(pgn.getGames().get(0), count);
            //System.out.println(count);
            System.out.println("Time Taken to process " + count + " games " + (System.currentTimeMillis() - start) + " ms");
        }
    }
}
