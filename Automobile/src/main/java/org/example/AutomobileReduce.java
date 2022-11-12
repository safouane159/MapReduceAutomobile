package org.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Iterator;

public class AutomobileReduce extends Reducer<Text, Text, Text, Text> {
    static private PrintStream console_log;

    private static final DecimalFormat df = new DecimalFormat("0.00");
    static private boolean node_was_initialized = false;

    // La fonction REDUCE elle-meme. Les arguments: la clef key (d'un type generique K), un Iterable de toutes les valeurs
    // qui sont associees a la clef en question, et le contexte Hadoop (un handle qui nous permet de renvoyer le resultat a Hadoop).
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {


        double totalBonusMalus = 0;
        int countBonusMalus = 0;
        int count = 0;
        double totalRejetCo2 = 0;
        double totalCout = 0;


        // recuper les moyen calculer et on garde que celui avec la valeur la plus grande
        // les keys avec la valeur AAAA sont lister les premiers
        if (key.toString().equals("AAAA")) {
            for (Text t : values) {
                String parts[] = t.toString().split(",");
                if (TotalBonusMalus.total < Double.parseDouble(parts[0])) {
                    // on stock la valeur dans un variable de class pour l'utiliser aprés
                    TotalBonusMalus.total = Double.parseDouble(parts[0]);
                    TotalBonusMalus.count = Integer.parseInt(parts[1]);
                }
            }
        } else { // le reste des keys

            for (Text t : values) {

                //séparer les valeur de chaque ligne avec ","
                String parts[] = t.toString().split(",");

                // on vas calculer le moyen de BonusMalus pour les marque qui ont au moin une valeur disponible
                if (!parts[1].equals("") && !parts[1].equals("0")) {

                    totalBonusMalus += Double.parseDouble(parts[1]);
                    countBonusMalus += 1;
                }

                // pour les maruqe qui il'ont aucune valeur disponible de bonusMalus on donne la moyenne
                if (totalBonusMalus == 0) {
                    totalBonusMalus = TotalBonusMalus.total;
                    countBonusMalus = TotalBonusMalus.count;
                }

                // on calcul le total de RejetCo2 de chaque marque
                totalRejetCo2 += Double.parseDouble(parts[2]);
                
                // on calcul le total de Cout de chaque marque
                totalCout += Double.parseDouble(parts[3].replaceAll("[^a-zA-Z0-9]", ""));
                count += 1;


            }

            // finalement écrira les moyennes calculées de chaque marque
            context.write(key, new Text(String.valueOf(df.format(totalBonusMalus / countBonusMalus)) + "," + String.valueOf(df.format(totalRejetCo2 / count)) + "," + String.valueOf(df.format(totalCout / count))));


        }


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
