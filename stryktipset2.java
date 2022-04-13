
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import org.json.*;

public class stryktipset2
{
   private static JSONArray draws = null;
   private static final int HOME = 0, DRAW = 1, AWAY = 2;
   private static String[] matches = new String[13];
   private static String[][] procents = new String[13][3];
   private static String[][] odds = new String[13][3];
   private static String[][] oddsProc2 = new String[13][3];
   private static String[][] minRad = new String[13][3];
   private static double[][] procentFolket = new double[13][3];
   private static double[][] procentOdds = new double[13][3];
   private static double[][] oddsDouble = new double[13][3];
   private static double[][] minRadOdds = new double[13][3];
   private static double[][] difference = new double[13][3];
   // ställ in lågoddsare ;
   private static final double lowOdds = 1.71;
   
   // OBS! Ändra om det ska vara Europa- eller Stryktipset här ;
   //private static String tips = "stryktipset";
   private static String tips = "europatipset";
   
   // Hur många rader ska spelas? ;
   private static int spelaRader = 50 ;
   
   // Hur stort överodds ska användas? ;
   private static int overodds = 4;

   // Hämta data från Svenska Spels API ;
   private static void updateMatches() throws IOException, JSONException
   {
       JSONArray events = draws.getJSONObject(0).getJSONArray("drawEvents");
       JSONObject currentDistr;
       JSONObject currentGame;
       JSONObject currentGameOdds;
       JSONObject oddsProc;
       //get the games 1-13 and store it in instance variable matches
       for(int i = 0; i < events.length(); i++)
       {
           currentGame = events.getJSONObject(i);
           matches[i] = currentGame.getString("eventDescription");
           currentDistr = currentGame.getJSONObject("svenskaFolket");
           procents[i][HOME] = currentDistr.getString("one");
           procents[i][DRAW] = currentDistr.getString("x");
           procents[i][AWAY] = currentDistr.getString("two");
           currentGameOdds = currentGame.getJSONObject("odds");
           //if(!currentGameOdds.isNull("home"))
           
            odds[i][HOME] = currentGameOdds.getString("one").replace(",", ".");
            odds[i][DRAW] = currentGameOdds.getString("x").replace(",", ".");
            odds[i][AWAY] = currentGameOdds.getString("two").replace(",", ".");
            
            oddsProc = currentGame.getJSONObject("favouriteOdds");
            //if(!currentGameOdds.isNull("home"))
            
             oddsProc2[i][HOME] = oddsProc.getString("one").replace(",", ".");
             oddsProc2[i][DRAW] = oddsProc.getString("x").replace(",", ".");
             oddsProc2[i][AWAY] = oddsProc.getString("two").replace(",", ".");


       }
       for (int k=0; k<13; k++)
       {
          for (int j=0; j<3; j++)
          {
             oddsDouble[k][j] = Double.parseDouble(odds[k][j]);
             procentFolket[k][j] = Double.valueOf(procents[k][j]);
             procentOdds[k][j] = Double.valueOf(oddsProc2[k][j]);
          }
       }
       

   }

   private static void retrieveJSONFromUrl(String tips) throws IOException, JSONException
   {
       JSONObject obj = null;
       /*  stryktipset can be changed to europatipset if wanted */
       String url;
       if (tips.contentEquals("stryktipset"))
       {
          url = "https://api.www.svenskaspel.se/draw/1/stryktipset/draws";
       }
       else
       {
          url = "https://api.www.svenskaspel.se/draw/1/europatipset/draws"; 
       }
       //String url = url2;
       try
       {
           obj = readJsonFromUrl(url);
       }
       catch (IOException e)
       {
           e.printStackTrace();
       }
       draws = obj.getJSONArray("draws");
   }

   private static String readAll(Reader rd) throws IOException
   {
       StringBuilder sb = new StringBuilder();
       int cp;
       while ((cp = rd.read()) != -1)
       {
           sb.append((char) cp);
       }
       return sb.toString();
   }

   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException
   {
       InputStream is = new URL(url).openStream();
       try
       {
           BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
           String jsonText = readAll(rd);
           JSONObject json = new JSONObject(jsonText);
           return json;
       }
       finally
       {
           is.close();
       }
   }

   public static String[] getMatches() {
       return matches;
   }

   public String getWeekHeader() throws IOException, JSONException
   {
       return draws.getJSONObject(0).getString("drawComment");
   }

   public static int getTurnOver() throws IOException, JSONException
   {
       return Integer.parseInt(draws.getJSONObject(0).getString("currentNetSale").split(",")[0]);
   }

   public static String[][] getProcents() {
       return procents;
   }

   public static String[][] getOdds() {
       return odds;
   }
   
   public static <T> int getLength(T[] arr)
   {
      int count = 0;
      for(T el : arr)
          if (el != null)
              ++count;
      return count;
   }

   public static void main(String[] args) throws IOException, JSONException
   {
      retrieveJSONFromUrl(tips);
      updateMatches();
     
      for (int i=0; i<13; i++)
      {
         for (int j=0;j<3; j++)
         {
            difference[i][j] = (double) Math.round((procentFolket[i][j] - procentOdds[i][j]) *10)/10;
         }
      }
      System.out.printf("%s %,d %n", "Omsättning: ", getTurnOver());
      
      for (int i=0; i<90; i++)
         System.out.printf("%s", "=");
      
      System.out.printf("%n%-5s %-15s  %-20s %-25s %-25s %n", "Rad", "SvenskaFolket", "Odds", "OddsProcent", "Differens");
            
      for (int i=0; i<13; i++)
      {
         System.out.printf("%-5s %-15s  %-20s %-25s %-25s %n", i+1, Arrays.deepToString(procents[i]), Arrays.toString(odds[i]), Arrays.toString(oddsProc2[i]), Arrays.toString(difference[i])   );
      }
      for (int i=0; i<13; i++)
      {
         for (int j=0;j<3; j++)
         {
            if (procentOdds[i][j]-procentFolket[i][j]>overodds)
               {
                  if (j==0)
                     {
                        minRad[i][j] = "1";
                        minRadOdds[i][j] = oddsDouble[i][j];
                     }
                  else if (j==1)
                  {
                     minRad[i][j] = "x";
                     minRadOdds[i][j] = oddsDouble[i][j];
                  }
                  else if (j==2)
                  {
                     minRad[i][j] = "2";
                     minRadOdds[i][j] = oddsDouble[i][j];
                  }
               }
            if (oddsDouble[i][j]<=lowOdds)
            {
               if (j==0)
                  {
                     minRad[i][j] = "1";
                     minRadOdds[i][j] = oddsDouble[i][j];
                  }
               else if (j==1)
               {
                  minRad[i][j] = "x";
                  minRadOdds[i][j] = oddsDouble[i][j];
               }
               else if (j==2)
               {
                  minRad[i][j] = "2";
                  minRadOdds[i][j] = oddsDouble[i][j];
               }
            }
         }
      }
      
      // Om några tecken inte löser ut, så sätter man 1, X, 2 ;
      String fillUp [] = {"1","x","2"};
      for (int i=0; i<13; i++)
      {
         boolean koll1 = false;
         boolean koll2 = false;
         boolean koll3 = false;
         for (int j=0; j<3; j++)
         {
            if (minRad[i][j] == null && j==0)
            {
               koll1 = true;
            }
            if (minRad[i][j] == null && j==1)
            {
               koll2 = true;
            }
            if (minRad[i][j] == null && j==2)
            {
               koll3 = true;
            }
         }
         if (koll1 == true && koll2 == true && koll3 == true)
         {
            minRad[i]=fillUp;
         }
      }

         // ta bort nullvärden ;
         String[][] minRad2 = Arrays.stream(minRad)
               .map(row -> Arrays.stream(row)
                       // filter out null elements
                       .filter(Objects::nonNull)
                       // new array
                       .toArray(String[]::new))
               .toArray(String[][]::new);
         
         for (int i=0; i<90; i++)
            System.out.printf("%s", "=");
         
         System.out.printf("%n%-5s %-15s %-20s %n", "Rad", "Min Rad", "Odds");
         for (int i=0; i<13; i++)
         {
            System.out.printf("%-5s %-15s %-20s %n", i+1, Arrays.toString(minRad2[i]), Arrays.toString(minRadOdds[i])) ;
         }
         
         // ta fram alla möjliga rader ;
         String[] combinations = Arrays.stream(minRad2)
               // pairs of a 1D arrays into a single array
               .reduce((arr1, arr2) -> Arrays.stream(arr1)
                       // concatenate pairs of strings from two arrays
                       .flatMap(str1 -> Arrays.stream(arr2)
                               .map(str2 -> str1 + str2))
                       .toArray(String[]::new))
               .orElse(new String[0]);
  
       // column-wise output
       String minaRader [] = new String [50000];
       int rows = 3;
       for (int i = 0; i < rows; i++)
       {
           for (int j = 0; j < combinations.length; j++)
           {
               if (j % rows == i)
               {
                  minaRader[j] = combinations[j];
               }
           }
              
       }
       
       // skriv ut antal rader som tagits fram ;
       int antRader = getLength(minaRader);
       for (int i=0; i<70; i++)
          System.out.printf("%s", "=");
       System.out.printf("%n%s %s", "Antal rader: ", antRader);


       // Räkna fram det sammanlagda oddset per rad ;
       double[][] minRadOdds2 = new double[antRader][14];
       
       char one = 1;
       char kryss = 'x';
       char two = 2;
       
       for (int rows2=0; rows2<antRader; rows2++)
       {
          for (int col2=0; col2<13; col2++)
          {
             char loop = minaRader[rows2].charAt(col2);

             if (loop == one);
             {
                minRadOdds2[rows2][col2] = oddsDouble[col2][0];
             }
             if (loop == kryss)
             {
                minRadOdds2[rows2][col2] = oddsDouble[col2][1];
             }
             else if (loop == two)
             {
                minRadOdds2[rows2][col2] = oddsDouble[col2][2];
             }
          }
       }
       

       
       for (int i=0; i<antRader; i++)
       {
          double summa = 1;
          for (int j=0; j<13; j++)
          {
             summa = summa * minRadOdds2[i][j];
          }
          minRadOdds2[i][13] = summa;
       }

       // försök skapa array med rad och odds ;
       String slut [][] = new String[antRader][2];
       
       for (int i=0; i<antRader; i++)
       {
          slut[i][0] = minaRader[i];
          slut[i][1] = String.valueOf(minRadOdds2[i][13]);
       }
       
       // Sortera raderna efter odds mha bubbelsortering ;
       Double slut_a []= new Double[antRader];
       String slut_b []= new String[antRader];
       
       for (int i=0; i<antRader; i++)
       {
          slut_a[i] = minRadOdds2[i][13];
          slut_b[i] = minaRader[i];
       }
       
       for (int i=0; i<antRader; i++)
       {
          for (int j=0; j<antRader-i-1; j++)
          {
             if (slut_a[j] > slut_a[j+1] )
             {
                Double temp1 = slut_a[j];
                slut_a[j] = slut_a[j+1];
                slut_a[j+1] = temp1;
                
                String temp2 = slut_b[j];
                slut_b[j] = slut_b[j+1];
                slut_b[j+1] = temp2;
             }
             
          }
       }
       
       

       // medelvärde, median osv. ;
       double medel=0, median=0, summa=0, tenProc, nineteeProc;
       int medianNr = (int) antRader/2;
       int tenProcNr = (int) antRader/10;
       int nineteeProcNr = (int) antRader/10*9;
       for (int i=0; i<antRader; i++)
       {
          summa=summa + slut_a[i];
       }
       medel=summa/antRader;
       median = slut_a[medianNr];
       tenProc = slut_a[tenProcNr];
       nineteeProc = slut_a[nineteeProcNr];


       // Skriv ut statistik till konsollen ;
       System.out.printf("%n%s %,.0f  %s %,.0f %n%s %,.0f  %s %,.0f",
             "Medel: ", medel, "Median: ", median, "10% : ", tenProc, "90% : ", nineteeProc);
       
       System.out.println();
       
       // Ta fram min och max för de rader man vill spela ;
       int spelaRaderMin = medianNr - (spelaRader/2);
       int spelaRaderMax = medianNr + (spelaRader/2);
       
       // Skriv till textfil som kan lämnas in till Svenska Spel ;
       SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
       Date date = new Date();
       FileWriter writer = new FileWriter("/home/danne/Downloads/tips/" + tips + dateFormat.format(date) + ".txt"); 
       writer.write(tips.substring(0,1).toUpperCase() + tips.substring(1).toLowerCase() + System.lineSeparator() + System.lineSeparator());
       for( int i=spelaRaderMin; i<spelaRaderMax; i++)
       {
         for (int j=0; j<13; j++)
         {
            if (j==0)
            {
               writer.write("E," + slut_b[i].charAt(j) + "," );
            }
            else if (j<12)
            {
               writer.write(slut_b[i].charAt(j) + "," );
            }
            else if (j==12)
            {
               writer.write(slut_b[i].charAt(j) );
            }
            
         }
         writer.write(System.lineSeparator());
          
       }
       writer.close();
       

   }
}