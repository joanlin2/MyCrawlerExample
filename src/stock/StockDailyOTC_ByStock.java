package stock;

import com.github.abola.crawler.CrawlerPack;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 擷取證交所提供的盤後資訊
 *
 * 重點
 * 1. Json取得上櫃每日交易資料
 *
 * @author Joan lin
 * 2017/09/05
 *
 */
public class StockDailyOTC_ByStock {

    public static void main(String[] args) {
        String YYYY ="", MM ="" , sockId ="", uri = "", strYYYYMM="";
        String taskID = "dlytrans";
        String[] arrLine = null;  //股票清單陣列
        String strLine ="";   //單行股票info
        String data;
        String csvString = "" , csvTitle ="";
        FileWriter fw1 = null;
        csvTitle = "股票代號,日期,成交千股,成交千元,開盤,最高,最低,收盤,漲跌,筆數";
        String[] YYYYMMDD = null;
        Integer cnt =0;

        //起迄日期設定
        Calendar initDate = new GregorianCalendar(2017, Calendar.AUGUST, 1);
        Calendar startDate = null;
        int mons = getMonthNum(initDate); //total month
        System.out.println("total mons ="+ mons);

        try {
            //原始檔案編碼
            BufferedReader br1 = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream("D:\\workspace\\output\\stocklist.csv"), "Big5"));

            while ((data = br1.readLine()) != null) {//股票迴圈
                strLine = new String(data.getBytes( "Big5"),"Big5"); //讀跟顯示時的編碼
                arrLine = strLine.split(",");
                sockId = arrLine[0] ;  //股票代號
                System.out.println("---------------------------");
                System.out.println("sockId="+ sockId);
                //重設初始日期
                startDate = new GregorianCalendar(initDate.get(Calendar.YEAR), initDate.get(Calendar.MONTH), 1);

//                System.out.println("initDate="+initDate.get(Calendar.YEAR)+"-"+ (initDate.get(Calendar.MONTH)+1));
                for(int k =0; k <= mons; k++) { //月份遞減
                    startDate.add(GregorianCalendar.MONTH, k); //月份遞增
                    YYYY = String.format("%04d",startDate.get(Calendar.YEAR));
                    MM = String.format("%02d",(startDate.get(Calendar.MONTH)+1 ) );
                    strYYYYMM = YYYY + MM ;//reset
                    System.out.println("strYYYYMM="+strYYYYMM);

                    //個股日成交資訊
                    if (arrLine[2].equals("上櫃") ){
                        uri = "http://www.tpex.org.tw/web/stock/aftertrading/daily_trading_info/st43_result.php?l=zh-tw&d="
                                + (Integer.valueOf(YYYY) - 1911 )+"/"+ MM +"&stkno="+sockId;

//                        System.out.println(uri);
                        Document jsoupDoc = CrawlerPack.start()
                                .setRemoteEncoding("big5")
                                .getFromJson(uri);
                        csvString = csvTitle;
                        cnt = jsoupDoc.select("aadata").size();
                        for (Element elem : jsoupDoc.select("aadata")) {
                            File file = new File("D:\\workspace\\output\\OTC-"+ taskID + "-" + sockId + "-"+ strYYYYMM + ".csv");
                            if (file.exists()) {
                                file.delete();
                            }
                            fw1 = new FileWriter(file,true);
//                            fw1 = new FileWriter("D:\\workspace\\output\\OTC-"+ taskID + "-" + sockId + "-"+ strYYYYMM + ".csv",true);
                            if(file.length()==0){
                                fw1.write (csvString+"\r\n");  //標題
                            }
                            YYYYMMDD = elem.child(0).html().replace(",", "").split("/");
                            csvString = sockId
                                    + "," + (Integer.valueOf(YYYYMMDD[0]) + 1911) + YYYYMMDD[1] + YYYYMMDD[2]
                                    + "," + elem.child(1).html().replace(",", "")
                                    + "," + elem.child(2).html().replace(",", "")
                                    + "," + elem.child(3).html().replace(",", "")
                                    + "," + elem.child(4).html().replace(",", "")
                                    + "," + elem.child(5).html().replace(",", "")
                                    + "," + elem.child(6).html().replace(",", "")
                                    + "," + elem.child(7).html().replace(",", "")
                                    + "," + elem.child(8).html().replace(",", "")
                            ;
                            fw1.write (csvString+"\r\n");
                            fw1.flush();
//                          System.out.println(csvString);
                        }
                        System.out.println("done:" + cnt);
                    }
                    if(fw1 != null){
                        fw1.close();

                    }
                }//月份迴圈
            }//股票迴圈
            br1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getMonthNum(Calendar initDate){
        Calendar now = Calendar.getInstance() ;
        int year = now.get(Calendar.YEAR) - initDate.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) - initDate.get(Calendar.MONTH);
        if (month < 0) {
                month = 12 - initDate.get(Calendar.MONTH) + now.get(Calendar.MONTH);
                year -= 1;
            }
        return year*12 + month ;
    }


}
