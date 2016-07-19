package org.devside.logviewer;

import junit.framework.TestCase;

public class SequenceGeneratorTest extends TestCase {
    public void testSequenceGenerator(){
        SequenceGenerator sg = SequenceGenerator.getInstance();
        int original = sg.next();
        assertEquals(original+1,sg.next());
    }
}
