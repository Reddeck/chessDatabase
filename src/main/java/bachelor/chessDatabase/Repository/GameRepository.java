package bachelor.chessDatabase.Repository;

import bachelor.chessDatabase.Entity.GameEntity;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRepository extends Neo4jRepository<GameEntity, Long> {

    @Query("""
            UNWIND $allGames as g
            CALL {
                WITH g
                MERGE (white:Player {Name: g.player_white})
                MERGE (black:Player {Name: g.player_black})
                MERGE (game:Game {gameNumber: g.gameNumber, moves: g.moves})
                MERGE (white)-[:played {colour: "white"}]->(game)
                MERGE (black)-[:played {colour: "black"}]->(game)
                MERGE (result:Result {fen: g.result})
                MERGE (game)-[:result_of_game]->(result)
                WITH g, game
                UNWIND g.positions as p
                MERGE (start:Position {fen: p.fen})
                MERGE (game)-[:position {move: p.move, ply: p.ply}]->(start)
                WITH start, p
                UNWIND p.next_move as next
                MERGE (end:Position {fen: next.next_position})
                MERGE (start)-[:next_move {move: next.move}]->(end)
            } IN TRANSACTIONS OF 30 ROWS
            """)
    void saveAllGames(@Param("allGames") List<Value> gameEntities);


    //void saveAllOpenings(@Param("allOpenings") List<Value> openingEntities);
}
