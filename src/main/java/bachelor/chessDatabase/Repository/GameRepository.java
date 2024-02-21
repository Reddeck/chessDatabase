package bachelor.chessDatabase.Repository;

import bachelor.chessDatabase.Entity.GameEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface GameRepository extends Neo4jRepository<GameEntity, Long> {

    void saveGame(GameEntity gameEntity);
}
