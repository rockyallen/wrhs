/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nom.rockyallen.wrhswebsite;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rocky
 */
public class BuilderTest {
    
    public BuilderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

//    @Test
//    public void testMain() throws Exception {
//        System.out.println("main");
//        String[] args = null;
//        Builder.main(args);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testBuild() throws Exception {
//        System.out.println("build");
//        Builder instance = new Builder();
//        instance.build();
//        fail("The test case is a prototype.");
//    }

    @Test
    public void testSanitise() {
        System.out.println("sanitise");
        String s = "Weed & feed, 3 litre (bottle)";
        String expResult = "Weed_and_feed_3_litre_bottle";
        String result = Builder.sanitise(s);
        assertEquals(expResult, result);
    }

//    @Test
//    public void testUrl() {
//        System.out.println("url");
//        String s = "";
//        String expResult = "";
//        String result = Builder.url(s);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
    
}
