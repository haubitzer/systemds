package org.apache.sysds.test.functions.builtin;

import org.apache.sysds.common.Types;
import org.apache.sysds.lops.LopProperties;
import org.apache.sysds.runtime.matrix.data.MatrixValue;
import org.apache.sysds.test.TestConfiguration;
import org.apache.sysds.test.TestUtils;

import org.apache.sysds.test.AutomatedTestBase;
import org.apache.sysds.test.TestConfiguration;
import org.apache.sysds.test.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class BuiltinStatsNATest extends AutomatedTestBase {
    private final static String TEST_NAME = "statsNATest";
    private final static String TEST_DIR = "functions/builtin/";
    private final static String TEST_CLASS_DIR = TEST_DIR + BuiltinSplitTest.class.getSimpleName() + "/";
    private final static int rows = 10;
    private final static int cols = 1;
    private final static double eps = 1e-10;

    @Override
    public void setUp() {
        TestUtils.clearAssertionInformation();
        addTestConfiguration(TEST_NAME, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME, new String[] {"B",}));
    }

    //@Test
    //public void testStatsNA() {runStatsNA(1, true);}

    @Test
    public void testStatsNA2() {runStatsNA(4, true);}

    /*@Test
    public void testStatsNA3() {runStatsNA(10, true);}

    @Test
    public void testStatsNA4() {runStatsNA(100, true);}

    @Test
    public void testStatsNAList() {runStatsNA(1, false);}
    @Test
    public void testStatsNA2List() {runStatsNA(4, false);}
    @Test
    public void testStatsNA3List() {runStatsNA(10, false);}
    @Test
    public void testStatsNA4List() {runStatsNA(100, false);}

     */

    private void runStatsNA(int bins, boolean prints_only)
    {
        Types.ExecMode patformOld = setExecMode(Types.ExecMode.SPARK);

        //try {
            loadTestConfiguration(getTestConfiguration(TEST_NAME));
            String HOME = SCRIPT_DIR + TEST_DIR;
            fullDMLScriptName = HOME + TEST_NAME + ".dml";
            programArgs = new String[]{"-nvargs", "X=" + input("A"), "bins=" + bins,
                    "O1="+ output("O1"),"O2="+ output("O2"),"O3="+ output("O3"),"O4="+ output("O4"),
                    "O5="+ output("O5"),"O6="+ output("O6"),"O7="+ output("O7"),"O8="+ output("O8")};

            double[][] A = getRandomMatrix(rows, cols, -10, 10, 0.6, 7);
            writeInputMatrixWithMTD("A", A, true);

            fullRScriptName = HOME + TEST_NAME + ".R";
            rCmd = getRCmd(inputDir(),Integer.toString(bins), expectedDir());

            runTest(true, false, null, -1);
            runRScript(true);
            //compare matrices
            HashMap<MatrixValue.CellIndex, Double> dmlfileO1 = readDMLScalarFromOutputDir("O1");
            HashMap<MatrixValue.CellIndex, Double> dmlfileO2 = readDMLScalarFromOutputDir("O2");
            HashMap<MatrixValue.CellIndex, Double> dmlfileO3 = readDMLScalarFromOutputDir("O3");
            HashMap<MatrixValue.CellIndex, Double> dmlfileO4 = readDMLScalarFromOutputDir("O4");
            HashMap<MatrixValue.CellIndex, Double> dmlfileO5 = readDMLScalarFromOutputDir("O5");
            HashMap<MatrixValue.CellIndex, Double> dmlfileO6 = readDMLScalarFromOutputDir("O6");
            HashMap<MatrixValue.CellIndex, Double> dmlfileO7 = readDMLScalarFromOutputDir("O7");
            HashMap<MatrixValue.CellIndex, Double> dmlfileO8 = readDMLScalarFromOutputDir("O8");

            HashMap<MatrixValue.CellIndex, Double> rfileO1 = readRScalarFromExpectedDir("O1");
            HashMap<MatrixValue.CellIndex, Double> rfileO2 = readRScalarFromExpectedDir("O2");
            HashMap<MatrixValue.CellIndex, Double> rfileO3 = readRScalarFromExpectedDir("O3");
            HashMap<MatrixValue.CellIndex, Double> rfileO4 = readRScalarFromExpectedDir("O4");
            HashMap<MatrixValue.CellIndex, Double> rfileO5 = readRScalarFromExpectedDir("O5");
            HashMap<MatrixValue.CellIndex, Double> rfileO6 = readRScalarFromExpectedDir("O6");
            HashMap<MatrixValue.CellIndex, Double> rfileO7 = readRScalarFromExpectedDir("O7");
            HashMap<MatrixValue.CellIndex, Double> rfileO8 = readRScalarFromExpectedDir("O8");

            TestUtils.compareMatrices(dmlfileO1, rfileO1, eps, "Stat-DML", "Stat-R");
            TestUtils.compareMatrices(dmlfileO2, rfileO2, eps, "Stat-DML", "Stat-R");
            TestUtils.compareMatrices(dmlfileO3, rfileO3, eps, "Stat-DML", "Stat-R");
            TestUtils.compareMatrices(dmlfileO4, rfileO4, eps, "Stat-DML", "Stat-R");
            TestUtils.compareMatrices(dmlfileO5, rfileO5, eps, "Stat-DML", "Stat-R");
            TestUtils.compareMatrices(dmlfileO6, rfileO6, eps, "Stat-DML", "Stat-R");
            TestUtils.compareMatrices(dmlfileO7, rfileO7, eps, "Stat-DML", "Stat-R");
            TestUtils.compareMatrices(dmlfileO8, rfileO8, eps, "Stat-DML", "Stat-R");
        /*}
        finally {
            rtplatform = platformOld;
        }*/


    }
}
