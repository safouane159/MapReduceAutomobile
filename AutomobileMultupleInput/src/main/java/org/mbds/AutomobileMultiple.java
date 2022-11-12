package org.mbds;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
public class AutomobileMultiple {

    public static void main(String[] args) throws Exception
    {
        // Cr���� un object de configuration Hadoop.
        Configuration conf=new Configuration();





        Job job=Job.getInstance(conf, "Cars1.0");

        // D��fini les classes driver, map et reduce.
        job.setJarByClass(AutomobileMultiple.class);
        job.setReducerClass(AutomobileMultipleReduce.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // D��fini types clefs/valeurs de notre programme Hadoop.
        MultipleInputs.addInputPath(job, new Path(args[0]),TextInputFormat.class, CatalogueMap.class);
        MultipleInputs.addInputPath(job, new Path(args[1]),TextInputFormat.class, CO2Map.class);
        Path outputPath = new Path(args[2]);
        FileOutputFormat.setOutputPath(job, outputPath);






        // On lance la t��che Hadoop. Si elle s'est effectu��e correctement, on renvoie 0. Sinon, on renvoie -1.
        if(job.waitForCompletion(true))
            System.exit(0);
        System.exit(-1);
    }



}

