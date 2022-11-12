package org.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class AutomobileMap extends Mapper<Object, Text, Text, Text> {


    static private PrintStream console_log;
    static private boolean node_was_initialized = false;

    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {

        //séparer les valeur de chaque ligne avec ","
        String valueString = value.toString();
        String[] Columns = valueString.split(",");

        //recuperer la marque de voiture dans chaque ligne et la netoyer.
        String myKey = Columns[1].split(" ")[0].replaceAll("[^a-zA-Z0-9]", "");

        // recuperer le premier char dans la colonne 'bonusMalus'
        Character c2 = Columns[2].charAt(0);

        /*
         * tout les ligne contient une - au debut de la calonoe Columns[2] 'bonusMalus'
         * sauf la pluspart des ligne de mercedes et 3 ligne de Nissan
         */
        if (c2.equals('-') || myKey.equals("MERCEDES") || myKey.equals("NISSAN")) {

            // recupere le bonusMalus et le nettoyer
            String bonusMalus = Columns[2].replaceAll("[^a-zA-Z0-9]", "");


            if (bonusMalus.equals("")) {
                bonusMalus = "0";
            } //remplacer les valeur manquant dans la colone BonusMalus avec des 0


            //recuperer le numero de la ligne
            int id = Integer.parseInt(Columns[0].replaceAll("[^a-zA-Z0-9]", ""));


            //supprimer le 1 qui apparettre dans la colonne bonnusmalus dans les ligne 1 jusqu'à 57
            if (id < 57) {
                bonusMalus = bonusMalus.replaceAll("1", "");
            }

            // calculer le total de BonnusMalus on accumulent tout ses valeurs
            // cela nous permetra de calculer le moyen de cette colonne afin de la affecter
            // au maeques qui on aucune valeur disponible
            if (!bonusMalus.equals("0")) {
                TotalBonusMalus.total += Double.parseDouble(bonusMalus);
                TotalBonusMalus.count += 1;


                logPrint(" tBonusMalus " + TotalBonusMalus.total + "count " + TotalBonusMalus.count);

            }


            //construire la valeur qui sera passer au reducer
            String myValue = Columns[1].split(" ")[1].replaceAll("[^a-zA-Z0-9]", "") + "," + bonusMalus + "," + Columns[3] + "," + Columns[4];

            //construire la valeur pour le total des BonnusMalus
            String myValue1 = String.valueOf(TotalBonusMalus.total) + "," + String.valueOf(TotalBonusMalus.count);

            //ecrire les valeurs obtenu de total des BonnusMalus
            context.write(new Text("AAAA"), new Text(myValue1));
            // ecrire la valaur construite dans cette format : key = marque , value le reste de la ligne
            context.write(new Text(myKey), new Text(myValue));


            // on dupliqe la valeur avec la cle fix ""foralla"
            context.write(new Text("forAll"), new Text(myValue));


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
