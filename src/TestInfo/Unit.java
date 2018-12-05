/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Wei Wang
 */
public class Unit {
    protected Map<String , String> basicInfoMap;
    protected Map<String , Float> resultMap;
    public Map<String , String> getBasicInfoMap(){
        return this.basicInfoMap;
    }
    public Map<String , Float> getResultMap(){
        return this.resultMap;
    }
    public void setBasicInfoMap(Map<String , String> basicInfoMap){
        this.basicInfoMap = basicInfoMap;
    }
    public void setResultMap (Map<String , Float> resultMap){
        this.resultMap = resultMap;
    }
    
}
