package jhberges.sdn;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type = "A_LINK")
public class Relation {
	@GraphId
	private Long graphId;
	@StartNode
	private BusinessObject a;
	@EndNode
	private BusinessObject b;
	private String discriminator;

	public Long getGraphId() {
		return graphId;
	}

	public void setGraphId(Long graphId) {
		this.graphId = graphId;
	}

	public BusinessObject getA() {
		return a;
	}

	public void setA(BusinessObject a) {
		this.a = a;
	}

	public BusinessObject getB() {
		return b;
	}

	public void setB(BusinessObject b) {
		this.b = b;
	}

	public String getDiscriminator() {
		return discriminator;
	}

	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

}
