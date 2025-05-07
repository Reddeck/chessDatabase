package bachelor.chessDatabase.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.Set;

@Node("Opening")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpeningEntity extends PositionEntity{
    @Property("name")
    private String name;
    @Property("eco")
    private String eco;

    public OpeningEntity(String fen, String name, String eco) {
        super(fen);
        this.name = name;
        this.eco = eco;
    }
}
