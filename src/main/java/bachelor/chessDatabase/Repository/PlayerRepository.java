package bachelor.chessDatabase.Repository;

import bachelor.chessDatabase.Entity.PlayerEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface PlayerRepository extends Neo4jRepository<PlayerEntity, String> {
}
