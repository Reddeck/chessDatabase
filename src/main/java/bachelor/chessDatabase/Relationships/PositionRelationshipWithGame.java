package bachelor.chessDatabase.Relationships;

import bachelor.chessDatabase.Entity.PositionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Data
@AllArgsConstructor
public class PositionRelationshipWithGame {
    @Id
    @GeneratedValue
    private Long id;
    @Property
    private int moveNumber;
    @Property
    private int plyNumber;
    @TargetNode
    private PositionEntity position;

    public PositionRelationshipWithGame(int moveNumber, int plyNumber, PositionEntity position){
        this.moveNumber = moveNumber;
        this.plyNumber = plyNumber;
        this.position = position;
    }
}
