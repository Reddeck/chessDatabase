package bachelor.chessDatabase.Relationships;

import bachelor.chessDatabase.Entity.PlayerEntity;
import bachelor.chessDatabase.Enums.Color;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@Data
@AllArgsConstructor
@RelationshipProperties
public class GameRelationship {
    @Id
    @GeneratedValue
    private Long id;
    @Property
    private Color color;
    @TargetNode
    private PlayerEntity player;

    public GameRelationship (Color color, PlayerEntity player){
        this.color = color;
        this.player = player;
    }
}
