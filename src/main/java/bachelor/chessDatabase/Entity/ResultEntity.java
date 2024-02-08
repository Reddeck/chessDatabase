package bachelor.chessDatabase.Entity;

import bachelor.chessDatabase.Enums.Result;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Result")
public class ResultEntity extends PositionEntity{
    public ResultEntity(Result result) {
         super(result.toString());
    }
}
