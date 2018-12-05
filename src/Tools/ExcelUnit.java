/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tools;

import TestInfo.Unit;
import java.util.Map;

/**
 *
 * @author Wei Wang
 */
public class ExcelUnit{
        private String sheetName;
        private Map<Integer , Unit> unitMap;
        private Map<Integer, Map<String, float[]>> refMap;
        public ExcelUnit(String sheetName , Map<Integer , Unit> unitMap  , Map<Integer, Map<String, float[]>> refMap){
            this.sheetName = sheetName;
            this.unitMap = unitMap;
            this.refMap = refMap;
        }
        public String getSheetName(){
            return this.sheetName;
        }
        public Map<Integer , Unit> getUnitMap(){
            return this.unitMap;
        }
        public Map<Integer, Map<String, float[]>> getRefMap(){
            return this.refMap;
        }
    }
