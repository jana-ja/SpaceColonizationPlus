package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class IOController {
    public static void analyzeCSV(){
        String row;
        int i =0;
        List<String> names = new ArrayList<>();
        List<String> stages = new ArrayList<>();
        try(BufferedReader csvReader = new BufferedReader(new FileReader("E:\\Users\\JanaJ\\IdeaProjects\\jana.jansen\\data\\Baumkataster2012\\Baumkataster.csv")) ){//pfad relativ angeben
            while ((row = csvReader.readLine()) != null) {
                System.out.println("line " + i);
                i++;
                String[] data = row.split(";");
                if(!names.contains(data[3])){
                    names.add(data[3]);
                }
                if(!stages.contains(data[5])){
                    stages.add(data[5]);
                }
            }
            List<String> types;
            try (PrintWriter out = new PrintWriter("tree_names.txt")) {
                types =  Arrays.asList("apfel", "ahorn", "linde", "kastanie", "weide", "pappel", "buche", "akazie", "kiefer", "birke", "erle", "esche", "kirsche", "eiche", "fichte", "dorn", "birne", "tanne", "azalee", "palme", "lebensbaum", "ulme", "kirsche", "platane");
                types.sort(String.CASE_INSENSITIVE_ORDER);
                for (String type : types) {
                    List<String> result = filterAndRemove(names, type);
                    result.forEach(out::println);
                    out.println();
                }
                out.println("Sonstiges:");
                names.forEach(out::println);
            }
            try (PrintWriter out = new PrintWriter("tree_names_short.txt")) {
                types.forEach(type -> out.println(type + "\n"));
                names.forEach(out::println);
            }
            try (PrintWriter out = new PrintWriter("tree_stages.txt")) {
                stages.forEach(out::println);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static List<String> filterAndRemove(List<String> list, String type) {
        List<String> result = list.stream()
                .filter(line -> line.toLowerCase().contains(type))
                .collect(Collectors.toList());
        list.removeAll(result);
        return result;
    }
}
