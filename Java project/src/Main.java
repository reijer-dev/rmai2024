import org.nlogo.headless.HeadlessWorkspace;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.lang.System.exit;

public class Main {
    static List<String> csvLines = new ArrayList<>();
    static List<HeadlessWorkspace> availableWorkspaces = new ArrayList<>();
    static ExecutorService workerThreadPool;
    static int amountOfRanSimulations = 0;
    static int amountOfSimulationsToRun = 0;
    static int[] windSpeeds = {3, 7, 23};
    static float[][] windList = {
            // South and West speeds for 0 m/s -> 1x, 0 wind.
            {0, 0},
            // 3 m/s -> 1:N NE E SE S SW W 8:NW
            {-3, 0}, {-2.121F, -2.121F}, {0, -3}, {2.121F, -2.121F}, {3, 0}, {2.121F, 2.121F}, {0, 3}, {-2.121F, 2.121F},
            // 7 m/s -> 1:N NE E SE S SW W 8:NW
            {-7, 0}, {-4.950F, -4.950F}, {0, -7}, {4.950F, -4.950F}, {7, 0}, {4.950F, 4.950F}, {0, 7}, {-4.950F, 4.950F},
            // 23 m/s -> 1:N NE E SE S SW W 8:NW
            {-23, 0}, {-16.263F, -16.263F}, {0, -23}, {16.263F, -16.263F}, {23, 0}, {16.263F, 16.263F}, {0, 23}, {-16.263F, 16.263F}
    };


    public static void main(String[] argv) {
        try {
            //Initialize workspaces
            var nlogoFile = Paths.get(System.getProperty("user.dir"), "..", "Simple Fire extension.nlogo");
            int hardwareThreads = Runtime.getRuntime().availableProcessors();
            // hardwareThreads = 6;
            workerThreadPool = Executors.newFixedThreadPool(hardwareThreads);

            //Create as many workspaces as there are threads
            for (var i = 0; i < hardwareThreads; i++) {
                var workspace = HeadlessWorkspace.newInstance();
                workspace.open(nlogoFile.toAbsolutePath().toString());
                availableWorkspaces.add(workspace);
                System.out.println("# Workspaces: " + availableWorkspaces.size());
            }

            testphase();
            // phase2();
            // phase3();
            // phase4();
            synchronized (workerThreadPool) {
                workerThreadPool.wait();
            }

            //Write CSV
            var csvFile = Paths.get(System.getProperty("user.dir"), "..", "output.csv");
            Files.write(csvFile, csvLines);
        } catch (Exception e) {
            e.printStackTrace();
        }

        exit(0);
    }

    static void testphase() {
        // In which we test, change variables etc. etc. :) enjoy
        for (var i = 1; i <= 2; i += 1) {
            String strategy = getStrategy(i);
            for (var density = 50; density <= 55; density += 5) {
                scheduleSimulation(density, 0, strategy, 0);
            }
        }
    }

    static void phase2() {
        // In which the forest density is being adjusted between 50 up until 90, in steps of 5.
        for (var i = 1; i <= 5; i += 1) {
            String strategy = getStrategy(i);
            for (var density = 50; density <= 90; density += 5) {
                scheduleSimulation(density, 0, strategy, 0);
            }
        }
    }

    static void phase3() {
        // In which the wind-speed and wind-direction will be added and adjusted, 0/3/7/23 N/W counts 8
        for (var i = 1; i <= 5; i += 1) {
            String strategy = getStrategy(i);
            for (var density = 50; density <= 90; density += 5) {
                for (int z = 0; z < windList.length; z++) {
                    scheduleSimulation(density, z, strategy, 0);
                }
            }
        }
    }

    static void phase4() {
        // In which the  previous test will be done for multiple locations. (1 / 2 / 3 / 4)
        for (var i = 1; i <= 5; i += 1) {
            String strategy = getStrategy(i);
            for (var density = 50; density <= 90; density += 5) {
                for (int z = 0; z < windList.length; z++) {
                    for (int location = 1; location <= 4; location += 1) {
                        scheduleSimulation(density, z, strategy, location);
                    }

                }
            }
        }
    }

    static String getStrategy(int i) {
        return switch (i) {
            case 2 -> "Even";
            case 3 -> "Diagonal";
            case 4 -> "Omnidirectional";
            case 5 -> "Wall";
            default -> "Nothing";
        };
    }

    static void scheduleSimulation(int forestDensity, int windListIndex, String strategy, int ignitionLocation) {
        workerThreadPool.submit(() -> {
            HeadlessWorkspace workspace = null;
            synchronized (availableWorkspaces) {
                workspace = availableWorkspaces.removeFirst();
            }
            try {
                workspace.command("setup");
                workspace.command("set density " + forestDensity);
                workspace.command("set probability-of-spread 70");
                workspace.command("set south-wind-speed " + windList[windListIndex][0]);
                workspace.command("set west-wind-speed " + windList[windListIndex][1]);
                workspace.command("set strategy \"" + strategy + "\"");
                workspace.command("random-seed 0");
                workspace.command("run-full");
                var percentageBurned = ((Double) workspace.report("percent-burned"));
                var villageDamaged = (Boolean) workspace.report("village-damaged");
                amountOfRanSimulations++;

                var windSpeed = 0;
                if (windListIndex > 0)
                    windSpeed = windSpeeds[(windListIndex - 1) / 8];

                var direction = 0;
                if (windListIndex == 0)
                    direction = 1;
                else if (windListIndex <= 8)
                    direction = windListIndex;
                else if (windListIndex <= 16)
                    direction = windListIndex - 8;
                else if (windListIndex <= 24)
                    direction = windListIndex - 16;

                String line = String.format("%d,%d,%d,%s,%.2f,%s",
                        forestDensity,
                        windSpeed,
                        direction,
                        strategy,
                        percentageBurned,
                        villageDamaged ? "True" : "False"
                );
                csvLines.add(line);
                System.out.print(amountOfRanSimulations + " / " + amountOfSimulationsToRun + "\r");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            finally {
                synchronized (availableWorkspaces) {
                    availableWorkspaces.add(workspace);
                }
            }
        });
        amountOfSimulationsToRun++;
    }


}


