package bachelor.chessDatabase.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Node("GameEntity")
public class GameEntity {
    @Id
    private Long id;
    private PlayerEntity white;
    private PlayerEntity black;

}
