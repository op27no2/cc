package op27no2.comsta;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Created by CristMac on 12/10/17.
 */

public class WriteExcel {

    private WritableCellFormat timesBoldUnderline;
    private WritableCellFormat times;
    private String inputFile;
    private Context mContext;

    public void setOutputFile(String inputFile) {
        this.inputFile = inputFile;
    }
    public void setContext(Context context) {
        mContext = context;
    }

    public void write(String name) throws IOException, WriteException {
        //File file = new File(inputFile);
        File file = new File(mContext.getFilesDir(), name);
        System.out.println("trying to write file: " + file);

        WorkbookSettings wbSettings = new WorkbookSettings();

        wbSettings.setLocale(new Locale("en", "EN"));

        WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
        workbook.createSheet("Comsta", 0);
        WritableSheet excelSheet = workbook.getSheet(0);
        createLabel(excelSheet);
        createContent(excelSheet);

        workbook.write();
        workbook.close();
    }

    private void createLabel(WritableSheet sheet)
            throws WriteException {
        // Lets create a times font
        WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
        // Define the cell format
        times = new WritableCellFormat(times10pt);
        // Lets automatically wrap the cells
        times.setWrap(true);

        // create create a bold font with unterlines
        WritableFont times10ptBoldUnderline = new WritableFont(
                WritableFont.TIMES, 10, WritableFont.BOLD, false,
                UnderlineStyle.SINGLE);
        timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
        // Lets automatically wrap the cells
        timesBoldUnderline.setWrap(true);

        CellView cv = new CellView();
        cv.setFormat(times);
        cv.setFormat(timesBoldUnderline);
        cv.setAutosize(true);

        // Write a few headers
        addCaption(sheet, 0, 0, "Header 1");
        addCaption(sheet, 1, 0, "This is another header");


    }

    private void createContent(WritableSheet sheet) throws WriteException,
            RowsExceededException {
        // Write a few number
        for (int i = 1; i < 10; i++) {
            // First column
            addNumber(sheet, 0, i, i + 10);
            // Second column
            addNumber(sheet, 1, i, i * i);
        }
        // Lets calculate the sum of it
        StringBuffer buf = new StringBuffer();
        buf.append("SUM(A2:A10)");
        Formula f = new Formula(0, 10, buf.toString());
        sheet.addCell(f);
        buf = new StringBuffer();
        buf.append("SUM(B2:B10)");
        f = new Formula(1, 10, buf.toString());
        sheet.addCell(f);

        // now a bit of text
        for (int i = 12; i < 20; i++) {
            // First column
            addLabel(sheet, 0, i, "Boring text " + i);
            // Second column
            addLabel(sheet, 1, i, "Another text");
        }
    }

    private void addCaption(WritableSheet sheet, int column, int row, String s)
            throws RowsExceededException, WriteException {
        Label label;
        label = new Label(column, row, s, timesBoldUnderline);
        sheet.addCell(label);
    }

    private void addNumber(WritableSheet sheet, int column, int row,
                           Integer integer) throws WriteException, RowsExceededException {
        Number number;
        number = new Number(column, row, integer, times);

        sheet.addCell(number);
    }

    private void addLabel(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        Label label;
        label = new Label(column, row, s, times);
        sheet.addCell(label);
    }

/*    public static void main(String[] args) throws WriteException, IOException {
        WriteExcel test = new WriteExcel();
        //test.setOutputFile("c:/temp/lars.xls");
        test.write();
        System.out
                .println("Please check the result file under temp/lars.xls ");
    }*/




    //TUTORIAL VERSION NUMBER 2
    public void makeExcelFile(String title) {

        try {
            final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Comsta Sheets" + "/");

            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e("ALERT", "could not create the directories");
                }
            }

            File myFile = new File(dir, title+".xls");

            if (!myFile.exists()) {
                myFile.createNewFile();
            }

            WorkbookSettings wbSettings = new WorkbookSettings();

            wbSettings.setLocale(new Locale("en", "EN"));

            WritableWorkbook workbook = Workbook.createWorkbook(myFile, wbSettings);
            workbook.createSheet("testExcel", 0);
            WritableSheet workSheet = workbook.getSheet(0);

            // Lets create a times font
            WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
            // Define the cell format
            WritableCellFormat times = new WritableCellFormat(times10pt);
            // Lets automatically wrap the cells
            times.setWrap(true);

            Util mUtil = new Util(mContext);
            BigListModel mObject = mUtil.getSavedObjectFromPreference(mContext, title, BigListModel.class);
            ArrayList<ArrayList<String>> mData = mObject.getData();

            for(int i =0; i<mData.size(); i++){
                for(int j=0; j<mData.get(i).size(); j++){
                    addCaption2(workSheet, i, j, mData.get(i).get(j));
                }
            }


            workbook.write();
            workbook.close();

            Toast.makeText(mContext, "File saved to /Comsta Sheets/"+title, Toast.LENGTH_LONG).show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void addCaption2(WritableSheet workSheet, int column, int row, String s)
            throws WriteException {
        // create create a bold font with unterlines
        WritableFont times12ptBoldUnderline = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD, false,
                UnderlineStyle.NO_UNDERLINE);
        WritableCellFormat timesBoldUnderline = new WritableCellFormat(times12ptBoldUnderline);
        // Lets automatically wrap the cells
        timesBoldUnderline.setWrap(true);


        Label label;
        label = new Label(column, row, s, timesBoldUnderline);
        workSheet.addCell(label);
    }

/*    public void writeToSheet(String value) throws WriteException,
            IOException, BiffException {

        Workbook workbook = Workbook.getWorkbook(myFile);
        WritableWorkbook copy = Workbook.createWorkbook(myFile, workbook);
        WritableSheet workSheet = copy.getSheet(0);
        int currentRowIndex = workSheet.getRows();

        addValueToSheet(workSheet, 0, currentRowIndex, value);

        copy.write();
        copy.close();
    }*/

    private void addValueToSheet(WritableSheet workSheet, int column, int row, String s)
            throws WriteException {
        Label label;
        label = new Label(column, row, s, times);
        workSheet.addCell(label);
    }

    //TUTORIAL VERSION NUMBER 2
    public void addToList(String title, File mFile, String addToTitle) {

        try {
            final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Comsta Sheets" + "/");

            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e("ALERT", "could not create the directories");
                }
            }

            File myFile = new File(dir, title);

            WorkbookSettings wbSettings = new WorkbookSettings();

            wbSettings.setLocale(new Locale("en", "EN"));
            Workbook workbook = Workbook.getWorkbook(myFile);
            Sheet workSheet = workbook.getSheet(0);

            WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
            WritableCellFormat times = new WritableCellFormat(times10pt);
            times.setWrap(true);


            Util mUtil = new Util(mContext);
            BigListModel mObject = mUtil.getSavedObjectFromPreference(mContext, addToTitle, BigListModel.class);
            ArrayList<ArrayList<String>> mData = mObject.getData();

            for(int i=mData.size(); i<workSheet.getColumns(); i++){
                mData.add(new ArrayList<String>());
            }

            for(int i=0; i< workSheet.getColumns(); i++) {
                System.out.println("Working on Column: "+i);
                //TODO IF MDATA NOT BIF ENOUGH ADD THE ARRAYS;

                if(workSheet.getCell(i,0).getContents().equals("")){
                    System.out.println("Breaking at Column: "+i);
                    break;
                }
                for(int j = 0; j< workSheet.getRows(); j++){
                    if(workSheet.getCell(i,j).getContents().equals("")) {
                        System.out.println("Breaking at row: "+j);
                        break;
                    }
                    mData.get(i).add(workSheet.getCell(i,j).getContents());
                    System.out.println("Adding to List  "+workSheet.getCell(i,j).getContents());

                }
            }

            BigListModel mList = new BigListModel(addToTitle, mData);
            mUtil.saveObjectToSharedPreference(mContext, addToTitle, mList);

            Toast.makeText(mContext, "Excel Rows Added", Toast.LENGTH_LONG).show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
