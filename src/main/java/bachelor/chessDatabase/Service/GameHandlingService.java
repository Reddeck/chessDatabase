package bachelor.chessDatabase.Service;

import bachelor.chessDatabase.Mapper.GameMapper;
import bachelor.chessDatabase.Mapper.GameMapperImpl;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;

public class GameHandlingService {
    private final GameMapperImpl gameMapper = new GameMapperImpl();

    public void handleGame(Game game){
        try{
            game.loadMoveText();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        var moves = game.getHalfMoves();
        Board board = new Board();
        for (Move move : moves){
            board.doMove(move);
            System.out.println(board.getFen());
        }
    }
}
