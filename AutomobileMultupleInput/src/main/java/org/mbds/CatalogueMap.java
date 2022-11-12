package org.mbds;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.Normalizer;

public class CatalogueMap extends Mapper<Object, Text, Text, Text> {

    private static final IntWritable ONE = new IntWritable(1);
    static private PrintStream console_log;
    static private boolean node_was_initialized = false;
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {

        //split by tab ( space between key and value from previous mapReduce
        String valueString = value.toString();
        String[] Columns = valueString.split(",");

        //key = "BMW" for exemple
        String myKey = Columns[0].replaceAll("[^a-zA-Z0-9]", "I").toUpperCase();

        // Rest of columns exemple : Bonus/Malus : 3000.00  Rejet Co2 : 15.50 Cout : 132.75
        String res = Columns[1] + ","+Columns[2]+ ","+Columns[3]+ ","+Columns[4]+ ","+Columns[5]+ ","+Columns[6]+ ","+Columns[7]+ ","+Columns[8];

        // ajouter un tag sur les valeurs pour indiquer de quelle table ils viennent
        context.write(new Text(myKey), new Text("CATALOGUE:" + res));


    }
    private static void logPrint(String line) {
        if (!node_was_initialized) {
            try {
                console_log = new PrintStream(new FileOutputStream("/tmp/my_mapred_log.txt", true));
            } catch (FileNotFoundException e) {
                return;
            }
            node_was_initialized = true;
        }
        console_log.println(line);
    }
}
