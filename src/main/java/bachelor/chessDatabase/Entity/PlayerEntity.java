package bachelor.chessDatabase.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("Player")
@AllArgsConstructor
@Data
public class PlayerEntity {
    @Id
    @Property("Name")
    private final String name;
    @Relationship("played")
    private Set<GameEntity> games;
    @Relationship("won_against")
    private Set<PlayerEntity> playersWonAgainst;
    @Relationship("lost_to")
    private Set<PlayerEntity> playersLostTo;
}
