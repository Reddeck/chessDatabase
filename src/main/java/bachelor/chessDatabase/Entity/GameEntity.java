package bachelor.chessDatabase.Entity;

import bachelor.chessDatabase.Enums.Result;
import bachelor.chessDatabase.Relationships.GameRelationship;
import bachelor.chessDatabase.Relationships.PositionRelationshipWithGame;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Node("Game")
public class GameEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Property
    private int gameNumber;
    @Property
    private int moves;
    @Relationship
    private ResultEntity result;
    @Relationship(direction = Relationship.Direction.OUTGOING)
    private Set<PositionRelationshipWithGame> positions;
    @Relationship(direction = Relationship.Direction.INCOMING)
    private Set<GameRelationship> played;
}
