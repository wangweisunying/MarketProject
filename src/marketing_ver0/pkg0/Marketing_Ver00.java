/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marketing_ver0.pkg0;

import TestInfo.DataProcessor;
import TestInfo.ENA10;
import TestInfo.TestInfo;
import TestInfo.Thyroid;
import TestInfo.Unit;
import TestInfo.WheatZoomer;
import Tools.ExcelTools;
import Tools.ExcelUnit;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.EmailAndText;

/**
 *
 * @author Wei Wang
 */
public class Marketing_Ver00 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Marketing_Ver00 mk = new Marketing_Ver00();
        List<ExcelUnit> list = new ArrayList();
        list.add(mk.orderPosNotOrder(new ArrayList(Arrays.asList(new WheatZoomer())), new ArrayList(Arrays.asList(new ENA10())), new WheatZoomer()));
        list.add(mk.orderPosNotOrder(new ArrayList(Arrays.asList(new Thyroid())), new ArrayList(Arrays.asList(new ENA10())), new Thyroid()));
        ExcelTools excelTools = new ExcelTools();
        String path = excelTools.generateExcel(list);
//        EmailAndText.sendEmail("wei_vg@vibrantgenomics.com", "vibrant@2014", "hari@vibrantsci.com", new String[]{}, "marketing file", "autoMail please do not reply", path);
    }

    public ExcelUnit orderPosNotOrder(List<TestInfo> orderList, List<TestInfo> notOrderList, TestInfo filterTest) throws SQLException, Exception {
        String sheetName = "";

        Map<Integer, Unit> res = new HashMap();
        DataProcessor dp = new DataProcessor();
        List<Integer> list = dp.getSampleList(orderList, notOrderList);
        Map<Integer, Unit> rawUnitMap = dp.getUnitData(filterTest, list);
        Map<Integer, Map<String, float[]>> fiterMap = dp.getRefRange(filterTest, list);
        for (int sampleId : rawUnitMap.keySet()) {
            if (!fiterMap.containsKey(sampleId)) {
                continue;
            }
            Unit unit = rawUnitMap.get(sampleId);
            for (String testCode : unit.getResultMap().keySet()) {              
                float[] ref = fiterMap.get(sampleId).get(testCode);
                if(ref == null){
                    continue;
                }
                float unitRaw = unit.getResultMap().get(testCode);
//                System.out.println(unitRaw);
//                System.out.println(Arrays.toString(ref));
                if (unitRaw < ref[0] || unitRaw > ref[1]) {
                    res.put(sampleId, unit);
                    break;
                }
            }
        }
        sheetName += " in ";
        for (TestInfo test : orderList) {
            sheetName += test.getTestName() + "&&";
        }
        sheetName = sheetName.substring(0, sheetName.length() - 2) + "  not in";
        for (TestInfo test : notOrderList) {
            sheetName += test.getTestName() + "||";
        }
        sheetName = sheetName.substring(0, sheetName.length() - 2);
//        for (Unit x : res.values()) {
//            System.out.println(x.getResultMap());
//        }
        return new ExcelUnit(sheetName, res, fiterMap);
    }

}
