package bachelor.chessDatabase.Relationships;

import bachelor.chessDatabase.Entity.PositionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionRelationshipWithPosition {
    @Id
    @GeneratedValue
    private Long id;
    @Property
    private String move;
    @TargetNode
    private PositionEntity position;

    public PositionRelationshipWithPosition(String san, PositionEntity nextPosition) {
        this.move = san;
        this.position = nextPosition;
    }
}
