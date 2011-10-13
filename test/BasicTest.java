import models.Tag;
import models.Tag.Tagcloud;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class BasicTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void findTagcloud() {
		Fixtures.loadModels("testdata.yml");
		Tagcloud tagcloud = Tag.findByUsername("n-shinya");
		assertNotNull(tagcloud.tagMap);
		assertEquals(tagcloud.tagMap.size(), 2);
	}
}
