package bachelor.chessDatabase.Service;

import bachelor.chessDatabase.Entity.GameEntity;
import bachelor.chessDatabase.Entity.PlayerEntity;
import bachelor.chessDatabase.Entity.PositionEntity;
import bachelor.chessDatabase.Entity.ResultEntity;
import bachelor.chessDatabase.Enums.Color;
import bachelor.chessDatabase.Enums.Result;
import bachelor.chessDatabase.Mapper.GameMapperImpl;
import bachelor.chessDatabase.Relationships.GameRelationship;
import bachelor.chessDatabase.Relationships.PositionRelationshipWithGame;
import bachelor.chessDatabase.Relationships.PositionRelationshipWithPosition;
import bachelor.chessDatabase.Repository.GameRepository;
import bachelor.chessDatabase.Repository.PlayerRepository;
import bachelor.chessDatabase.Repository.PositionRepository;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class GameHandlingService {
    private final GameMapperImpl gameMapper = new GameMapperImpl();

    private final PlayerRepository playerRepository;
    private final PositionRepository positionRepository;
    private final GameRepository gameRepository;

    public GameHandlingService(PlayerRepository playerRepository, GameRepository gameRepository, PositionRepository positionRepository){
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.positionRepository = positionRepository;
    }

    public void handleGame(Game game){
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

        switch (game.getResult().getDescription()){
            case "1-0" -> gameEntity.setResult(new ResultEntity(Result.WHITE));
            case "0-1" -> gameEntity.setResult(new ResultEntity(Result.BLACK));
            case "1/2-1/2" -> gameEntity.setResult(new ResultEntity(Result.DRAW));
        }
        this.positionRepository.saveAll(positions);
        this.gameRepository.save(gameEntity);
        PlayerEntity white = new PlayerEntity(game.getWhitePlayer().getName(), Set.of(new GameRelationship(Color.WHITE, gameEntity)));
        PlayerEntity black = new PlayerEntity(game.getBlackPlayer().getName(), Set.of(new GameRelationship(Color.BLACK, gameEntity)));
        playerRepository.save(white);
        playerRepository.save(black);
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
}
