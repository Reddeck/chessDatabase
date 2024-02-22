package bachelor.chessDatabase.Service;

import bachelor.chessDatabase.Entity.GameEntity;
import bachelor.chessDatabase.Entity.PlayerEntity;
import bachelor.chessDatabase.Entity.PositionEntity;
import bachelor.chessDatabase.Entity.ResultEntity;
import bachelor.chessDatabase.Enums.Color;
import bachelor.chessDatabase.Enums.Result;
import bachelor.chessDatabase.Relationships.GameRelationship;
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
        gameEntity.setGameNumber(number);
        PlayerEntity white = new PlayerEntity(game.getWhitePlayer().getName());
        PlayerEntity black = new PlayerEntity(game.getBlackPlayer().getName());
        gameEntity.setPlayed(Set.of(new GameRelationship(Color.WHITE, white), new GameRelationship(Color.BLACK, black)));
        try{
            game.loadMoveText();
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        Board board = new Board();
        HashSet <PositionRelationshipWithGame> positionRelationshipsWithGames = new HashSet<>();
        HashSet <PositionEntity> positions = new HashSet<>();
        handleMoves(positions, positionRelationshipsWithGames, board, game);
        if(board.getSideToMove().toString().equals("WHITE")){
            gameEntity.setMoves(board.getMoveCounter() - 1);
        } else{
            gameEntity.setMoves(board.getMoveCounter());
        }
        gameEntity.setPositions(positionRelationshipsWithGames);

        switch (game.getResult().getDescription()){
            case "1-0" -> gameEntity.setResult(new ResultEntity(Result.WHITE));
            case "0-1" -> gameEntity.setResult(new ResultEntity(Result.BLACK));
            case "1/2-1/2" -> gameEntity.setResult(new ResultEntity(Result.DRAW));
        }


        this.gameRepository.saveAllGames(mapGamesToValues(Set.of(gameEntity)));
        //this.positionRepository.saveAllPositions(mapPositionsToValues(positions));
        /*
        //this.positionRepository.saveAll(positions);
        //this.gameRepository.save(gameEntity);
        //this.gameRepository.saveGame(gameEntity);
                //if Players are saved everything in them is also saved
        PlayerEntity white = new PlayerEntity(game.getWhitePlayer().getName(), Set.of(new GameRelationship(Color.WHITE, gameEntity)));
        PlayerEntity black = new PlayerEntity(game.getBlackPlayer().getName(), Set.of(new GameRelationship(Color.BLACK, gameEntity)));
        playerRepository.save(white);
        playerRepository.save(black);*/
    }

    private void handleMoves(HashSet <PositionEntity> positions, HashSet<PositionRelationshipWithGame> positionRelationshipWithGames, Board board, Game game){
        int plyNumber = 0;
        PositionEntity tempPosition;
        PositionEntity nextPosition = new PositionEntity(board.getFen(false));
        positionRelationshipWithGames.add(new PositionRelationshipWithGame(0, plyNumber, nextPosition));

        for(Move move : game.getHalfMoves()){
            tempPosition = nextPosition;
            int moveCounter = board.getMoveCounter();
            board.doMove(move);
            plyNumber++;
            nextPosition = new PositionEntity(board.getFen(false));
            tempPosition.setNextPosition(new PositionRelationshipWithPosition(move.getSan(), nextPosition));
            positionRelationshipWithGames.add(new PositionRelationshipWithGame(moveCounter, plyNumber, nextPosition));
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

    private List<Value> mapGamesToValues(Set<GameEntity> games){
        List<Value> valuesAllGames = new ArrayList<>();
        for(GameEntity game : games){
            Map<String, Object> map = new HashMap<>();
            map.put("gameNumber", game.getGameNumber());
            map.put("moves", game.getMoves());
            List<Object> posList = new ArrayList<>();
            for(PositionRelationshipWithGame pos : game.getPositions()){
                Map<String, Object> tempMap = new HashMap<>();
                tempMap.put("move", pos.getMoveNumber());
                tempMap.put("ply", pos.getPlyNumber());
                var position = pos.getPosition();
                tempMap.put("fen", position.getModifiedFen());
                if (position.getNextPosition() != null) {
                    List<Map<String, Object>> relationships = new ArrayList<>();
                    for (PositionRelationshipWithPosition relationship : position.getNextPosition()) {
                        Map<String, Object> relationshipMap = new HashMap<>();
                        relationshipMap.put("move", relationship.getMove());
                        relationshipMap.put("next_position", relationship.getPosition().getModifiedFen());
                        relationships.add(relationshipMap);
                    }
                    tempMap.put("next_move", relationships);
                }
                posList.add(tempMap);
            }
            map.put("positions", posList);
            map.put("result", game.getResult().getModifiedFen());

            map.put("player_white", game.getPlayed().stream().filter(p -> p.getColor().toString().equals("WHITE")).findFirst().get().getPlayer().getName());
            map.put("player_black", game.getPlayed().stream().filter(p -> p.getColor().toString().equals("BLACK")).findFirst().get().getPlayer().getName());
            valuesAllGames.add(Values.value(map));
        }
        return valuesAllGames;
    }
}
