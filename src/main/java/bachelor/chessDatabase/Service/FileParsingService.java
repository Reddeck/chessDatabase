package bachelor.chessDatabase.Service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileParsingService implements Runnable{
    private final GameHandlingService gameHandler = new GameHandlingService();
    private final ArrayList<String> files;

    public FileParsingService(String... files){
        this.files = new ArrayList<>(List.of(files));
    }

    @Override
    public void run() {
        if (files.isEmpty()) return;
        for (String file : files) {
            PgnHolder pgn = new PgnHolder("games/" + file);
            try {
                pgn.loadPgn();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            int count = 0;

            for(Game game : pgn.getGames()){
                count++;
                gameHandler.handleGame(game);
                System.out.println(count);
            }
        }
    }
}
