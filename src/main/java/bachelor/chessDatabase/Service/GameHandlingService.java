package bachelor.chessDatabase.Service;

import bachelor.chessDatabase.Entity.GameEntity;
import bachelor.chessDatabase.Entity.PositionEntity;
import bachelor.chessDatabase.Entity.ResultEntity;
import bachelor.chessDatabase.Enums.Result;
import bachelor.chessDatabase.Relationships.PositionRelationshipWithGame;
import bachelor.chessDatabase.Relationships.PositionRelationshipWithPosition;
import bachelor.chessDatabase.Repository.GameRepository;
import bachelor.chessDatabase.Repository.PlayerRepository;
import bachelor.chessDatabase.Repository.PositionRepository;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import jakarta.annotation.PostConstruct;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GameHandlingService {

    private final PlayerRepository playerRepository;
    private final PositionRepository positionRepository;
    private final GameRepository gameRepository;
    private final Driver driver;

    public GameHandlingService(PlayerRepository playerRepository, GameRepository gameRepository, PositionRepository positionRepository, Driver driver){
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.positionRepository = positionRepository;
        this.driver = driver;
    }

    @PostConstruct
    private void init(){
        var list = new HashSet<ResultEntity>();
        list.add(new ResultEntity(Result.WHITE));
        list.add(new ResultEntity(Result.BLACK));
        list.add(new ResultEntity(Result.DRAW));
        this.positionRepository.saveAll(list);
    }

    public void handleGame(Game game, int number){
        GameEntity gameEntity = new GameEntity();
        try{
            game.loadMoveText();
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        Board board = new Board();
        HashSet <PositionRelationshipWithGame> positionRelationshipsWithGames = new HashSet<>();
        HashSet <PositionEntity> positions = new HashSet<>();
        handleMoves(positions, positionRelationshipsWithGames, board, game);
        gameEntity.setMoves(board.getMoveCounter());
        gameEntity.setPositions(positionRelationshipsWithGames);
        gameEntity.setGameNumber(number);

        switch (game.getResult().getDescription()){
            case "1-0" -> gameEntity.setResult(new ResultEntity(Result.WHITE));
            case "0-1" -> gameEntity.setResult(new ResultEntity(Result.BLACK));
            case "1/2-1/2" -> gameEntity.setResult(new ResultEntity(Result.DRAW));
        }

        //this.positionRepository.saveAllPositions(mapPositionsToValues(positions));
        this.positionRepository.saveAll(positions);
        this.gameRepository.saveGame(gameEntity);
        /*
        this.positionRepository.saveAll(positions);
        this.gameRepository.save(gameEntity);
        PlayerEntity white = new PlayerEntity(game.getWhitePlayer().getName(), Set.of(new GameRelationship(Color.WHITE, gameEntity)));
        PlayerEntity black = new PlayerEntity(game.getBlackPlayer().getName(), Set.of(new GameRelationship(Color.BLACK, gameEntity)));
        playerRepository.save(white);
        playerRepository.save(black);*/
    }

    private void handleMoves(HashSet <PositionEntity> positions, HashSet<PositionRelationshipWithGame> positionRelationshipWithGames, Board board, Game game){
        int plyNumber = 0;
        PositionEntity tempPosition;
        PositionEntity nextPosition = new PositionEntity(board.getFen(false));
        positionRelationshipWithGames.add(new PositionRelationshipWithGame(board.getMoveCounter(), plyNumber, nextPosition));

        for(Move move : game.getHalfMoves()){
            tempPosition = nextPosition;
            board.doMove(move);
            plyNumber++;
            nextPosition = new PositionEntity(board.getFen(false));
            tempPosition.setNextPosition(new PositionRelationshipWithPosition(move.getSan(), nextPosition));
            positionRelationshipWithGames.add(new PositionRelationshipWithGame(board.getMoveCounter(), plyNumber, nextPosition));
            positions.add(tempPosition);
        }

        tempPosition = nextPosition;
        switch (game.getResult().getDescription()){
            case "1-0" -> nextPosition = new ResultEntity(Result.WHITE);
            case "0-1" -> nextPosition = new ResultEntity(Result.BLACK);
            case "1/2-1/2" -> nextPosition = new ResultEntity(Result.DRAW);
        }

        tempPosition.setNextPosition(new PositionRelationshipWithPosition("finished", nextPosition));
        positions.add(tempPosition);
        positions.add(nextPosition);
    }

    private List<Value> mapPositionsToValues(Set<PositionEntity> positions){
        List<Value> valuesAllPos = new ArrayList<>();
        for(PositionEntity position : positions){
            Map<String, Object> map = new HashMap<>();
            map.put("fen", position.getModifiedFen());

            if (position.getNextPosition() != null) {
                List<Map<String, Object>> relationships = new ArrayList<>();
                for (PositionRelationshipWithPosition relationship : position.getNextPosition()) {
                    Map<String, Object> relationshipMap = new HashMap<>();
                    relationshipMap.put("move", relationship.getMove());
                    relationshipMap.put("next_position", relationship.getPosition().getModifiedFen());
                    relationships.add(relationshipMap);
                }
                map.put("next_move", relationships);
            }
            valuesAllPos.add(Values.value(map));
        }
        return valuesAllPos;
    }
}
