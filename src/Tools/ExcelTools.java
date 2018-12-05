/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tools;

import TestInfo.Unit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.ExcelOperation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author Wei Wang
 */
public class ExcelTools {
    
    public String generateExcel( List<ExcelUnit>list) throws IOException{
        String path = "C:\\Users\\Wei Wang\\Desktop\\tianhao\\test.xlsx";
        Workbook wb = ExcelOperation.getWriteConnection(ExcelOperation.ExcelType.SXSSF);
        
        CellStyle styleG = wb.createCellStyle();
        CellStyle styleR = wb.createCellStyle();
        styleG.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        styleG.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleR.setFillForegroundColor(IndexedColors.RED.getIndex());
        styleR.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        for(ExcelUnit excelUnit : list){
            String sheetName = excelUnit.getSheetName();
            Map<Integer , Unit> unitMap = excelUnit.getUnitMap();
            Map<Integer, Map<String, float[]>> refMap = excelUnit.getRefMap();
            Sheet sheet = wb.createSheet(sheetName);
            int rowCt = 0 , colCt = 0;
            
            Row titleRow = sheet.createRow(rowCt++);
            Unit unitTitle = (Unit)(new ArrayList(unitMap.values()).get(0));
            
            for(String str : unitTitle.getBasicInfoMap().keySet()){
                titleRow.createCell(colCt++).setCellValue(str);
            }
            for(String str : unitTitle.getResultMap().keySet()){
                titleRow.createCell(colCt++).setCellValue(str);
                titleRow.createCell(colCt++).setCellValue("refRange");
            }
            
            
            
            for(int i : unitMap.keySet()){
                colCt = 0;
                Unit unit = unitMap.get(i);
                Row curRow = sheet.createRow(rowCt++);
                for(String str : unit.getBasicInfoMap().keySet()){
                    curRow.createCell(colCt++).setCellValue(unit.getBasicInfoMap().get(str));
                }
                
                for(String str : unit.getResultMap().keySet()){
                    Cell cell = curRow.createCell(colCt++);
                    float val = unit.getResultMap().get(str);
                    float[] ref = refMap.get(i).get(str);
                    cell.setCellValue(val);
                    if(ref == null){
                        colCt++;
                        continue;
                    }
                    if(val < ref[0] || val > ref[1]){
                        cell.setCellStyle(styleR);
                    }
                    else{
                        cell.setCellStyle(styleG);
                    }
                    curRow.createCell(colCt++).setCellValue("["+ ref[0] +"," + ref[1] +"]");
                }
            }
        }
        ExcelOperation.writeExcel(path, wb);
        return path;
    }
}
