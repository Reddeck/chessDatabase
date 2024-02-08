package bachelor.chessDatabase.Repository;

import bachelor.chessDatabase.Entity.PositionEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface PositionRepository extends Neo4jRepository<PositionEntity, String> {
}
