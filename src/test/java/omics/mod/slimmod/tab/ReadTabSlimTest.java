package omics.mod.slimmod.tab;

import omics.mod.io.slimmod.model.SlimModCollection;
import omics.mod.io.slimmod.tab.ReadTabSlim;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: local_admin
 * Date: 20/07/11
 * Time: 16:42
 * To change this template use File | Settings | File Templates.
 */
public class ReadTabSlimTest {
    @Test
    public void testParseSlimModification() throws Exception {
        String filename = ReadTabSlimTest.class.getClassLoader().getResource("slimFile.txt").getPath();
        SlimModCollection slimModCollection = ReadTabSlim.parseSlimModification(filename);
    }
}
