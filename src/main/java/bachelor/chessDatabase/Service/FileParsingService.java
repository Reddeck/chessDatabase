package bachelor.chessDatabase.Service;

import bachelor.chessDatabase.Entity.GameEntity;
import bachelor.chessDatabase.Entity.OpeningEntity;
import bachelor.chessDatabase.Repository.GameRepository;
import bachelor.chessDatabase.Repository.OpeningRepository;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class FileParsingService{
    private final GameHandlingService gameHandler;
    private final GameRepository gameRepository;
    private final OpeningHandlingService openingHandlingService;
    private final OpeningRepository openingRepository;

    public FileParsingService(GameHandlingService gameHandler, GameRepository gameRepository, OpeningHandlingService openingHandlingService , OpeningRepository openingRepository){
        this.gameHandler = gameHandler;
        this.gameRepository = gameRepository;
        this.openingHandlingService = openingHandlingService;
        this.openingRepository = openingRepository;
    }

    public void parseGameFiles(String... files){
        for (String file : files) {
            PgnHolder pgn = new PgnHolder(file);
            try {
                pgn.loadPgn();
            } catch (Exception e) {
                throw new RuntimeException("Loading PGN failed: " + e);
            }
            int count = 0;

            var list = new HashSet<GameEntity>();
            /*
            for(Game game : pgn.getGames()) {
                count++;
                gameHandler.handleGame(game, count);
            }*/

            var all = pgn.getGames();
            while (count < all.size()){
                var game = all.get(count);
                count++;
                list.add(gameHandler.handleGame(game, count));
                //if (count == 100) { // For testing purposes -> measure time to save the games
                //    break;
                //}
            }

            //As the database is not able to handle too many relationships
            //at once (too much memory allocated), we need to save them in batches
            /*
            int batchSize = 20;
            var allGames = gameHandler.mapGamesToValues(list);
            for (int i = 0; i < allGames.size(); i += batchSize) {
                int end = Math.min(allGames.size(), i + batchSize);
                List<Value> batch = allGames.subList(i, end);
                this.gameRepository.saveAllGames(batch);
                System.out.println("Saved batch of " + batch.size() + " games. Games saved so far: " + (i + batch.size()) + "/" + allGames.size() + " games.");
            }
            */


            //this.gameRepository.saveAllGames(gameHandler.mapGamesToValues(list)); PROD

            long start = System.currentTimeMillis();
            //this.gameRepository.saveAll(list);
            this.gameRepository.saveAllGames(gameHandler.mapGamesToValues(list));
            System.out.println("Time Taken to process " + count + " games " + (System.currentTimeMillis() - start) + " ms");
        }
    }

    public void parseOpeningFile(String file){
        PgnHolder pgn = new PgnHolder(file);

        try {
            pgn.loadPgn();
        } catch (Exception e) {
            throw new RuntimeException("Loading PGN failed: " + e);
        }
        int count = 0;
        long start = System.currentTimeMillis();

        var openingList = new HashSet<OpeningEntity>();
        for(Game opening : pgn.getGames()) {
            count++;
            openingList.add(openingHandlingService.handleOpening(opening));
        }

        this.openingRepository.saveAllOpenings(openingHandlingService.mapToValues(openingList));
        System.out.println("Time Taken to process " + count + " games " + (System.currentTimeMillis() - start) + " ms");

    }

}
