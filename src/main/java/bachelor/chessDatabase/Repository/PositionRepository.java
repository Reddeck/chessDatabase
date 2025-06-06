package bachelor.chessDatabase.Repository;

import bachelor.chessDatabase.Entity.PositionEntity;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PositionRepository extends Neo4jRepository<PositionEntity, String> {

    @Query("""
            UNWIND $all as p
            MERGE (start:Position {fen: p.fen})
            WITH start, p
            UNWIND p.next_move as next
            MERGE (end:Position {fen: next.next_position})
            MERGE (start)-[:next_move {move: next.move}]->(end)
            """)
    void saveAllPositions(@Param("all") List<Value> positionEntity);
    
}
