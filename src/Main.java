package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        String selectedDirectory = promptUserForDirectory();
        List<String> csvFiles = getCsvFiles(selectedDirectory);
        Map<String, Map<String, TeamStats>> tippersStatsMap = new HashMap();
        Map<String, Integer> missedTipsMap = new HashMap();
        Map<String, Integer> specificScoreMap = new HashMap();
        Map<String, Integer> totalPredictionsMap = new HashMap();
        Map<String, Integer> drawPredictionsMap = new HashMap();

        for(String csvFile : csvFiles) {
            processCsvFile(csvFile, tippersStatsMap, missedTipsMap, specificScoreMap, totalPredictionsMap, drawPredictionsMap);
        }

        for(Map.Entry<String, Map<String, TeamStats>> tipperEntry : tippersStatsMap.entrySet()) {
            String tipperName = (String)tipperEntry.getKey();
            Map<String, TeamStats> teamStatsMap = (Map)tipperEntry.getValue();
            System.out.println("Tipper: " + tipperName);
            System.out.println("Team\tPunkte\tTordifferenz\tSpiele");

            for(Map.Entry<String, TeamStats> entry : sortTeamStats(teamStatsMap)) {
                PrintStream var10000 = System.out;
                String var10001 = (String)entry.getKey();
                var10000.println(var10001 + "\t\t" + ((TeamStats)entry.getValue()).points + "\t\t" + ((TeamStats)entry.getValue()).goalDifference + "\t\t\t\t" + ((TeamStats)entry.getValue()).games);
            }

            PrintStream var20 = System.out;
            Object var21 = missedTipsMap.getOrDefault(tipperName, 0);
            var20.println("Vergessene Tipps: " + String.valueOf(var21));
            int specificScoreCount = (Integer)specificScoreMap.getOrDefault(tipperName, 0);
            int totalPredictions = (Integer)totalPredictionsMap.getOrDefault(tipperName, 0);
            int drawPredictions = (Integer)drawPredictionsMap.getOrDefault(tipperName, 0);
            System.out.println("Sicherheitstipps (2:1,1:2): " + specificScoreCount + "/" + totalPredictions);
            System.out.println("Unentschieden Tipps: " + drawPredictions + "/" + totalPredictions);
            System.out.println();
        }

    }

    private static String promptUserForDirectory() {
        try {
            List<Path> directories = (List)Files.list(Paths.get("./data")).filter((x$0) -> Files.isDirectory(x$0, new LinkOption[0])).collect(Collectors.toList());
            if (directories.isEmpty()) {
                System.out.println("No directories found in ./data");
                System.exit(1);
            }

            System.out.println("Select a directory:");

            for(int i = 0; i < directories.size(); ++i) {
                System.out.println(i + 1 + ": " + String.valueOf(((Path)directories.get(i)).getFileName()));
            }

            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            if (choice < 1 || choice > directories.size()) {
                System.out.println("Invalid choice");
                System.exit(1);
            }

            return ((Path)directories.get(choice - 1)).toString();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private static List<String> getCsvFiles(String directory) {
        List<String> csvFiles = new ArrayList();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directory), "*.csv")) {
            for(Path entry : stream) {
                csvFiles.add(entry.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return csvFiles;
    }

    private static void processCsvFile(String csvFile, Map<String, Map<String, TeamStats>> tippersStatsMap, Map<String, Integer> missedTipsMap, Map<String, Integer> specificScoreMap, Map<String, Integer> totalPredictionsMap, Map<String, Integer> drawPredictionsMap) {
        String csvSplitBy = ";";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String[] headers = br.readLine().split(csvSplitBy);

            String line;
            while((line = br.readLine()) != null) {
                String[] row = line.split(csvSplitBy);
                String tipperName = row[0].trim();
                tippersStatsMap.putIfAbsent(tipperName, new HashMap());
                missedTipsMap.putIfAbsent(tipperName, 0);
                specificScoreMap.putIfAbsent(tipperName, 0);
                totalPredictionsMap.putIfAbsent(tipperName, 0);
                drawPredictionsMap.putIfAbsent(tipperName, 0);

                for(int i = 2; i < row.length; ++i) {
                    String match = headers[i].trim();
                    String prediction = row[i].trim();
                    String[] teams = match.split(" - ");
                    String[] scores = prediction.split(":");
                    if (scores.length != 2) {
                        System.err.println("Invalid prediction format: " + prediction);
                        missedTipsMap.put(tipperName, (Integer)missedTipsMap.get(tipperName) + 1);
                    } else {
                        String team1 = teams[0].replace("\"", "").trim();
                        String team2 = teams[1].replace("\"", "").trim();

                        int score1;
                        int score2;
                        try {
                            score1 = Integer.parseInt(scores[0].replace("\"", "").trim());
                            score2 = Integer.parseInt(scores[1].replace("\"", "").trim());
                        } catch (NumberFormatException var23) {
                            System.err.println("Invalid score format: " + prediction);
                            missedTipsMap.put(tipperName, (Integer)missedTipsMap.get(tipperName) + 1);
                            continue;
                        }

                        totalPredictionsMap.put(tipperName, (Integer)totalPredictionsMap.get(tipperName) + 1);
                        if (score1 == 2 && score2 == 1 || score1 == 1 && score2 == 2) {
                            specificScoreMap.put(tipperName, (Integer)specificScoreMap.get(tipperName) + 1);
                        }

                        if (score1 == score2) {
                            drawPredictionsMap.put(tipperName, (Integer)drawPredictionsMap.get(tipperName) + 1);
                        }

                        Map<String, TeamStats> teamStatsMap = (Map)tippersStatsMap.get(tipperName);
                        teamStatsMap.putIfAbsent(team1, new TeamStats());
                        teamStatsMap.putIfAbsent(team2, new TeamStats());
                        ((TeamStats)teamStatsMap.get(team1)).updateStats(score1, score2);
                        ((TeamStats)teamStatsMap.get(team2)).updateStats(score2, score1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static List<Map.Entry<String, TeamStats>> sortTeamStats(Map<String, TeamStats> teamStatsMap) {
        List<Map.Entry<String, TeamStats>> sortedList = new ArrayList(teamStatsMap.entrySet());
        sortedList.sort((e1, e2) -> {
            int pointsComparison = Integer.compare(((TeamStats)e2.getValue()).points, ((TeamStats)e1.getValue()).points);
            return pointsComparison != 0 ? pointsComparison : Integer.compare(((TeamStats)e2.getValue()).goalDifference, ((TeamStats)e1.getValue()).goalDifference);
        });
        return sortedList;
    }
}
