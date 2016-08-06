package wjtoth.cyclicstablematching;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Agent.
 */
public class AgentTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AgentTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AgentTest.class );
    }

    /**
     * Verify Agent class works
     */
    public void testAgent()
    {
        Agent testAgent = new Agent(3,0,0);
        int[] preferences = {1,2,3};
        testAgent.setPreferences(preferences);
        
        assertTrue(testAgent.getPreferences().equals(preferences));
        assertTrue(testAgent.prefers(1,0));
        assertFalse(testAgent.prefers(2, 2));
        Collection<Integer> rankedOrder = testAgent.rankedOrder();
        assertTrue(rankedOrder.toString().equals("[2, 1, 0]"));
        testAgent.setAgentPreference(0,4);
        assertFalse(testAgent.prefers(1, 0));

        Agent testAgent2 = new Agent(3,0,0);
        int[] preferences2 = {2,1,3};
        testAgent2.setPreferences(preferences2);

        assertTrue(testAgent.compareTo(testAgent2)<=0);
        assertFalse(testAgent2.compareTo(testAgent)<=0);

        assertTrue((testAgent.compareTo(testAgent.deepCopy()))<=0 &&testAgent.deepCopy().compareTo(testAgent)<=0);
    }
}
