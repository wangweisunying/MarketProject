/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.DataBaseCon;
import model.LXDataBaseCon;

/**
 *
 * @author Wei Wang
 */
public class DataProcessor {

    public static void main(String[] args) throws SQLException, Exception {
        DataProcessor dp = new DataProcessor();
        List<Integer> list = dp.getSampleList(new ArrayList(Arrays.asList(new WheatZoomer())), new ArrayList(Arrays.asList(new ENA10())));
//        System.out.println(list.size());
        dp.getRefRange(new WheatZoomer(), list);   
        
    }

    public List<Integer> getSampleList(List<TestInfo> includeList, List<TestInfo> excludeList) throws SQLException{
        List<Integer> res = new ArrayList();
        String sql = "select sample_id from vibrant_america_information.selected_test_list;";
        if (includeList.size() != 0) {
            
            sql = sql.substring(0 , sql.length() - 1) +  " where ";
            StringBuilder sbIn = new StringBuilder();
            StringBuilder sbEx = new StringBuilder();
            for (TestInfo test : includeList) {
                String[] panelList = test.getPanelList();
                sbIn.append("(");
                for (String panelName : panelList) {
                    sbIn.append("Order_").append(panelName).append(" != 0 or ");
                }
                sbIn.setLength(sbIn.length() - 3);
                sbIn.append(")");
                sbIn.append("and ");

            }
            sbIn.setLength(sbIn.length() - 4);
            sbIn.insert(0, "(");
            sbIn.append(")");
            
            if (excludeList.size() != 0) {
                for (TestInfo test : excludeList) {
                    String[] panelList = test.getPanelList();
                    sbEx.append("(");
                    for (String panelName : panelList) {
                        sbEx.append("Order_").append(panelName).append(" = 0 and ");
                    }
                    sbEx.setLength(sbEx.length() - 4);
                    sbEx.append(")");
                    sbEx.append("or ");
                }
                sbEx.setLength(sbEx.length() - 3);
                sbEx.insert(0, "(");
                sbEx.append(")");
                sql = sql + sbIn + " and " + sbEx + ";";
            } else {
                sql = sql + sbIn + ";";
            }
        }
        
        System.out.println(sql);
        DataBaseCon db = new LXDataBaseCon();
        ResultSet rs = db.read(sql);
        while(rs.next())res.add(rs.getInt(1));
        db.close();
        return res;
    }

    /*
         Map<Sample Id , Unit>
     */

    public Map<Integer, Unit> getUnitData(TestInfo test , List<Integer> sampleIdList) throws SQLException, Exception {
        Map<Integer, Unit> UnitMap = new HashMap();
        String[] panelList = test.getPanelList();
        StringBuilder sb = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        StringBuilder sbSampleSql = getSampleSql(sampleIdList);
        sb.append("SELECT\n"
                + "    sd.sample_id,\n"
                + "    cd.customer_id,\n"
                + "    pd.patient_id,\n"
                + "    patient_firstname,\n"
                + "    patient_lastname,\n"
                + "    patient_gender, patient_birthdate, \n"
                + "    date_format(now() , '%Y') - date_format(patient_birthdate , '%Y') as Age,\n"
                + "    patient_zipcode,\n"
                + "    customer_practice_name,\n"
                + "    customer_zipcode,");
        for (String panel : panelList) {
            sb.append("vibrant_america_test_result.result_").append(panel).append(".* ,");
        }
        sb.setLength(sb.length() - 1);
        sb.append("FROM\n"
                + "    vibrant_america_information.sample_data as sd\n"
                + "        JOIN\n"
                + "    vibrant_america_information.patient_details as pd  ON sd.patient_id = pd.patient_id\n"
                + "        JOIN\n"
                + "    vibrant_america_information.customer_details as cd ON pd.customer_id = cd.customer_id\n");
        for (String panel : panelList) {
            sb.append(" join vibrant_america_test_result.result_").append(panel).append(" ON vibrant_america_test_result.result_").append(panel).append(".sample_id = sd.sample_id ");
        }
        sb.append("where sd.customer_id < 900000 and sd.sample_id in (" + sbSampleSql.toString()+");");

        DataBaseCon db = new LXDataBaseCon();
        ResultSet rs = db.read(sb.toString());
        int colCt = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            Unit curUnit = new Unit();
            Map<String, String> basicInfoMap = new LinkedHashMap();
            Map<String, Float> resultMap = new LinkedHashMap();
            boolean flag = false;
            for (int i = 2; i <= colCt; i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                if (columnName.equals("sample_id")) {
                    flag = true;
                    continue;
                }
                if (flag) {
                    resultMap.put(columnName, Float.parseFloat(decimalFormat.format(rs.getFloat(i))));
                } else {
                    basicInfoMap.put(columnName, rs.getString(i));
                }
            }
            curUnit.setBasicInfoMap(basicInfoMap);
            curUnit.setResultMap(resultMap);
            UnitMap.put(rs.getInt(1), curUnit);
        }
        db.close();

//        for (Unit x : UnitMap.values()) {
//            System.out.println(x.getResultMap());
//        }
        return UnitMap;
    }

    /*
    Map<Integer,Unit>  sample_id , patiendUnit;
     */
    public Map<Integer, int[]> getSampleId2BinaryOrderMap(TestInfo test, List<Integer> sampleIdList) throws SQLException, Exception {
        String[] panelList = test.getPanelList();
        Map<Integer, int[]> sampleId2BinaryOrderMap = new HashMap();
        StringBuilder sb = new StringBuilder();
        for (String panelName : panelList) {
            sb.append("Order_").append(panelName).append(",");
        }
        sb.setLength(sb.length() - 1);
        StringBuilder sbSampleId = getSampleSql(sampleIdList);
        String sql = "select sample_id ," + sb.toString() + " from vibrant_america_information.selected_test_list where sample_id in ( " + sbSampleId.toString() + " ) ;";
        System.out.println(sql);
        DataBaseCon db = new LXDataBaseCon();
        ResultSet rs = db.read(sql);
        while (rs.next()) {
            int[] binaryArr = new int[panelList.length];
            for (int i = 1; i <= panelList.length; i++) {
                binaryArr[i - 1] = rs.getInt(i + 1);
            }
            sampleId2BinaryOrderMap.put(rs.getInt(1), binaryArr);
        }

//        for (int x : sampleId2BinaryOrderMap.keySet()) {
//            System.out.println(x + " " + Arrays.toString(sampleId2BinaryOrderMap.get(x)));
//        }

        db.close();
        return sampleId2BinaryOrderMap;
    }

    /*
        Map<Integer , Map<String , float[]>>   <sampleId , <testCode>
     */
    public Map<Integer, Map<String, float[]>> getRefRange(TestInfo test , List<Integer> sampleIdList ) throws SQLException, Exception {
        Map<Integer, Map<String, float[]>> refRangeMap = new HashMap();
        DataBaseCon db = new LXDataBaseCon();
        // get ref range
        Map<Integer, float[]> trackingId2Ref = new HashMap();
        String trackingQuery = "SELECT \n"
                + "    tracking_id,\n"
                + "    normal_min,\n"
                + "    normal_max,\n"
                + "    moderate_min,\n"
                + "    moderate_max,\n"
                + "    abnormal_min_start,\n"
                + "    abnormal_min_end,\n"
                + "    abnormal_max_start,\n"
                + "    abnormal_max_end\n"
                + "FROM\n"
                + "    vibrant_america_information.report_master_list_tracking;";
        ResultSet rsTracking = db.read(trackingQuery);
        while (rsTracking.next()) {
            float[] refArr = new float[8];
            for (int i = 0; i < refArr.length; i++) {
                refArr[i] = rsTracking.getFloat(i + 2);
            }
            trackingId2Ref.put(rsTracking.getInt(1), refArr);
        }
        StringBuilder sbSampleId = getSampleSql(sampleIdList);
        String[] panelList = test.getPanelList();
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("select * from vibrant_america_test_result_ml.master_list_").append(panelList[0]);
        String tmp = "vibrant_america_test_result_ml.master_list_" + panelList[0] + ".sample_id";
        for (int i = 1; i < panelList.length; i++) {
            String cur = "vibrant_america_test_result_ml.master_list_" + panelList[i];
            sbSql.append(" join ").append(cur).append(" on ").append(tmp).append(" = ").append(cur).append(".sample_id ");
        }
        sbSql.append(" where " + tmp + " in( "+sbSampleId.toString() +" );");
        System.out.println(sbSql.toString());

        ResultSet rs = db.read(sbSql.toString());
        int colCt = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            Map<String, float[]> testRefMap = new HashMap();
            refRangeMap.put(rs.getInt(1), testRefMap);
            for (int i = 1; i <= colCt; i++) {
                String testCode = rs.getMetaData().getColumnLabel(i);
                if (testCode.equals("sample_id")) {
                    continue;
                }
                testRefMap.put(testCode, trackingId2Ref.get(rs.getInt(i)));
            }
        }
//        for (int key : refRangeMap.keySet()) {
//            System.out.println(key);
//            for (String testCode : refRangeMap.get(key).keySet()) {
//                System.out.println(testCode + Arrays.toString(refRangeMap.get(key).get(testCode)));
//            }
//        }
        db.close();
        return refRangeMap;
    }
    
    private StringBuilder getSampleSql(List<Integer> sampleIdList) throws Exception{
        StringBuilder sbSampleId = new StringBuilder();
        if(sampleIdList == null || sampleIdList.size() == 0){
            throw new Exception("sampleIdList can not be empty!");
        }
        for (int sampleId : sampleIdList) {
            sbSampleId.append(sampleId).append(",");
        }
        sbSampleId.setLength(sbSampleId.length() - 1);
        return sbSampleId;
    }

}
