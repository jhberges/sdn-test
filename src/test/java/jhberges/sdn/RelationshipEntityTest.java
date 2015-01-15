package jhberges.sdn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.data.neo4j.repository.GraphRepositoryFactory;
import org.springframework.data.neo4j.rest.SpringCypherRestGraphDatabase;
import org.springframework.data.neo4j.support.Neo4jTemplate;

public class RelationshipEntityTest {
	private static final String REL_DISCRIMINATOR = "DISCRIMINATOR";
	private SpringCypherRestGraphDatabase db;
	private Neo4jTemplate template;
	private BusinessObjectRepository repo;

	@Before
	public void before() {
		// Rest-remoted DB doesn't work (save doesn't persist property on RelationshipEntity)
		db = new SpringCypherRestGraphDatabase("http://localhost:7474/db/data");
		System.err.println("JRE : " + System.getProperty("java.vendor"));
		System.err.println("    : " + System.getProperty("java.version"));
		System.err.println(" OS : " + System.getProperty("os.name"));
		System.err.println("    : " + System.getProperty("os.arch"));
		// Works fine with the embedded DB: (except from "assertNull" below which fails due to properties having been saved...) 
//		db = new GraphDatabaseFactory().newEmbeddedDatabase("target/dbs" + System.currentTimeMillis());
		
		template = new Neo4jTemplate((GraphDatabase) db);
		GraphRepositoryFactory graphRepoFactory = 
				new GraphRepositoryFactory(template, template.getInfrastructure().getMappingContext());
		repo = graphRepoFactory.getRepository(BusinessObjectRepository.class);
	}

	@After
	public void after() {
		Optional.ofNullable(db).ifPresent(database -> database.shutdown());
	}

	@Test
	public void testModelSave() throws Exception {
		BusinessObject obj = createModel();
		
		BusinessObject result = inTx(() -> save(obj));
		Relation rel = result.getRelations().iterator().next();
		assertEquals(REL_DISCRIMINATOR, rel.getDiscriminator());
		Result<Map<String, Object>> queryResult = template.query("MATCH (a)-[r:A_LINK {discriminator: {d}]-(b)", 
				Collections.singletonMap("discriminator", REL_DISCRIMINATOR));
		assertTrue(queryResult.iterator().hasNext());
		assertEquals(REL_DISCRIMINATOR, template.findOne(rel.getGraphId(), Relation.class).getDiscriminator());
	}

	protected <T> T inTx(Supplier<T> consume) {
		Transaction tx = template.getGraphDatabase().beginTx();
		try {
			T result = consume.get();
			tx.success();
			return result;
		} finally {
			tx.close();
		}
	}

	private BusinessObject createModel() {
		BusinessObject root = new BusinessObject();
		root.setName("A");

		BusinessObject far = new BusinessObject();
		far.setName("B");
		
		Relation rel = new Relation();
		rel.setA(root);
		rel.setB(far);
		rel.setDiscriminator(REL_DISCRIMINATOR);
		root.getRelations().add(rel);
		
		return root;
	}

	private BusinessObject save(BusinessObject obj) {
		for (Relation rel : obj.getRelations()) {
			repo.save(rel.getB());
		}
		
		BusinessObject result = repo.save(obj);
		
		// Added now - automagic save worked in pre-3.3.0 (also seemed to work in embedded mode?)
		// A bit simplistic but you get the gist.
		Relation rel = result.getRelations().iterator().next();
		// The following assert fails when using emdedded graphdb
//		assertNull(result.getRelations().iterator().next().getDiscriminator());
		
		rel.setDiscriminator(obj.getRelations().iterator().next().getDiscriminator());
		Relation updatedRel = template.save(rel);
		System.err.println("ID = " + rel.getGraphId());
		System.err.println("To-Save Relation has discriminator: " + rel.getDiscriminator());
		System.err.println("Updated Relation has discriminator: " + updatedRel.getDiscriminator());
		
		assertEquals(rel.getDiscriminator(), result.getRelations().iterator().next().getDiscriminator());
		// Assert below: assert that the DB is updated.
		assertEquals(rel.getDiscriminator(), updatedRel.getDiscriminator());
		
		
		return result;
	}
}
