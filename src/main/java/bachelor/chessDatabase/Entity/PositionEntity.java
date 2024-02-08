package bachelor.chessDatabase.Entity;

import bachelor.chessDatabase.Relationships.PositionRelationshipWithPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("Position")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionEntity {
    @Id
    @Property("fen")
    private String modifiedFen;
    @Relationship("next_move")
    private Set<PositionRelationshipWithPosition> nextPosition;

    public PositionEntity(String fen) {
        this.modifiedFen = fen;
    }

    public void setNextPosition(PositionRelationshipWithPosition nextPosition) {
        this.nextPosition = Set.of(nextPosition);
    }
}
