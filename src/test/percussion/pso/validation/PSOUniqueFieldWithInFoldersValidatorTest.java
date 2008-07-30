package test.percussion.pso.validation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.validation.PSOUniqueFieldWithInFoldersValidator;

public class PSOUniqueFieldWithInFoldersValidatorTest {

    PSOUniqueFieldWithInFoldersValidator validator;
    @Before
    public void setUp() throws Exception {
        validator = new PSOUniqueFieldWithInFoldersValidator();
    }
    
    @Test
    public void testGetQueryForValueInFolder() throws Exception {
        String expected = "select rx:sys_contentid, rx:filename" +
        " from nt:base " +
        "where " +
        "rx:filename = \'test\' " +
        "and " +
        "jcr:path = \'//Sites/Blah\'"; 
        String actual = validator.getQueryForValueInFolder("filename", "test", "//Sites/Blah", "nt:base");
        assertEquals(expected, actual);
    }
    
    @Test
    public void testGetQueryForValueInFolders() throws Exception {
        String expected = 
            "select rx:sys_contentid, rx:filename " +
            "from nt:base " +
            "where " +
            "rx:sys_contentid != 2000 " +
            "and " +
            "rx:filename = 'test' " +
            "and " +
            "(jcr:path = '//Sites/A' or jcr:path = '//Sites/B')";
            String actual = validator.getQueryForValueInFolders(
                    2000,"filename", "test", new String[] {"//Sites/A","//Sites/B"}, "nt:base");
        assertEquals(expected, actual);
    }

}
