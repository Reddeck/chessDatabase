package bachelor.chessDatabase.Repository;

import bachelor.chessDatabase.Entity.OpeningEntity;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpeningRepository extends Neo4jRepository<OpeningEntity, String> {

    @Query("""
            UNWIND $all as o
            MERGE (opening:Opening:Position {fen: o.fen})
            SET opening.name = o.name, opening.eco = o.eco
            """)
    void saveAllOpenings(@Param("all") List<Value> openingEntities);

}
