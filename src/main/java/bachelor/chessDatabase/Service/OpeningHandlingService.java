package bachelor.chessDatabase.Service;

import bachelor.chessDatabase.Entity.GameEntity;
import bachelor.chessDatabase.Entity.OpeningEntity;
import bachelor.chessDatabase.Entity.PositionEntity;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;

@Service
public class OpeningHandlingService {
    public OpeningHandlingService(){}
    public OpeningEntity handleOpening(Game opening) {
        //System.out.println("Eco: " + opening.get());
        String openingName = opening.getWhitePlayer().getName();
        String variation =opening.getBlackPlayer().getName();
        if (!variation.isEmpty()){
            openingName = openingName + ": " + variation;
        }
        try{
            opening.setBoard(new Board());
            opening.loadMoveText();
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        // go to the last position of the opening pgn
        opening.gotoLast(opening.getHalfMoves());
        String fen = opening.getBoard().getFen(false);
        String eco = opening.getEco();
        return new OpeningEntity(fen, openingName, eco);
    }

    public List<Value> mapToValues(Set<OpeningEntity> openingEntities) {
        List<Value> valuesList = new ArrayList<>();
        for (OpeningEntity opening : openingEntities) {
            Map<String, String> map = new HashMap<>();
            System.out.println("GO");
            map.put("eco", opening.getEco());
            map.put("name", opening.getName());
            map.put("fen", opening.getModifiedFen());
            System.out.println(map);
            valuesList.add(Values.value(map));
        }
        return valuesList;
    }
}
