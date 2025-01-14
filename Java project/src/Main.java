import org.nlogo.headless.HeadlessWorkspace;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {

    static float[][] windList = {
            // South and West speeds for 0 m/s -> 1x want, 0 wind.
            {0, 0},
            // North      North-East       East     South-East        South     South-West      West       North-West    Speeds for 3 m/s -> 1:N NE E SE S SW W 8:NW
            {-3, 0}, {-2.121F, -2.121F}, {0, -3}, {2.121F, -2.121F}, {3, 0}, {2.121F, 2.121F}, {0, 3}, {-2.121F, 2.121F},
            // North      North-East       East     South-East        South     South-West      West       North-West    Speeds for 7 m/s -> 1:N NE E SE S SW W 8:NW
            {-7, 0}, {-4.950F, -4.950F}, {0, -7}, {4.950F, -4.950F}, {7, 0}, {4.950F, 4.950F}, {0, 7}, {-4.950F, 4.950F},
            // North      North-East           East       South-East         South      South-West        West       North-West    Speeds for 23 m/s -> 1:N NE E SE S SW W 8:NW
            {-23, 0}, {-16.263F, -16.263F}, {0, -23}, {16.263F, -16.263F}, {23, 0}, {16.263F, 16.263F}, {0, 23}, {-16.263F, 16.263F}
    };


    public static void main(String[] argv) throws IOException {
        // phase2();
        phase3();
    }

    static void phase2() {
        // In which the forest density is being adjusted between 50 up until 90, in steps of 5.
        for (var i = 1; i <=5; i += 1) {
            String strategy = getStrategy(i);
            for (var density = 50; density <= 90; density += 5) {
                startWorkspace(density, 0, strategy, 0);
            }
        }
    }

    static void phase3() {
        // In which the wind-speed and wind-direction will be added and adjusted, 0/3/7/23 N/W counts 8
        for (var i = 1; i <=5; i += 1) {
            String strategy = getStrategy(i);
            for (var density=50; density<=90; density+=5) {
                for (int z = 0; z < windList.length; z++) {
                    startWorkspace(density, z, strategy, 0);
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


    static void startWorkspace(int forestDensity, int windListIndex, String strategy, int ignitionLocation) {
        HeadlessWorkspace workspace = HeadlessWorkspace.newInstance();
        try {
            var nlogoFile = Paths.get(System.getProperty("user.dir"), "..", "Simple Fire extension.nlogo");
            workspace.open(nlogoFile.toAbsolutePath().toString());
            workspace.command("setup");
            workspace.command("set density " + forestDensity);
            workspace.command("set probability-of-spread 70");
            workspace.command("set south-wind-speed " + windList[windListIndex][0]);
            workspace.command("set west-wind-speed " + windList[windListIndex][1]);
            workspace.command("set strategy \"" + strategy + "\"");
            workspace.command("random-seed 0");
            workspace.command("run-full");
            var percentageBurned = ((Double)workspace.report("percent-burned")).intValue();
            var villageDamaged = (Boolean)workspace.report("village-damaged");

            System.out.println("Strategy: " + strategy +
                    ", Density: " + forestDensity +
                    ", SouthWindSpeed: " + windList[windListIndex][0] +
                    ", WestWindSpeed: " + windList[windListIndex][1] +
                    ", Percent burned: " + percentageBurned +
                    "%, Village damaged: " +villageDamaged);
            workspace.dispose();
        }

        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}







/*
import org.nlogo.app.App;
import org.nlogo.headless.HeadlessWorkspace;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] argv) throws IOException {
        var nlogoFile = Paths.get(System.getProperty("user.dir"), "..", "Simple Fire extension.nlogo");

        int hardwareThreads = Runtime.getRuntime().availableProcessors();
        var workerThreadPool = Executors.newFixedThreadPool(hardwareThreads);

        for(var prob = 50; prob <= 90; prob+=5) {
            int finalProb = prob;
            Runnable task1 =  () -> {
                try {
                    System.out.println("test");
                    var workspace = HeadlessWorkspace.newInstance();
                    workspace.open(nlogoFile.toAbsolutePath().toString(), true);
                    for(var i = 50; i <= 90; i+=5) {
                        workspace.command("setup");
                        workspace.command("set density " + i);
                        workspace.command("set probability-of-spread " + finalProb);
                        workspace.command("set south-wind-speed 0");
                        workspace.command("set west-wind-speed 0");
                        var strategy = "Omnidirectional";
                        workspace.command("set strategy \"" + strategy + "\"");
                        workspace.command("random-seed 0");
                        workspace.command("run-full");
                        var percentageBurned = ((Double)workspace.report("percent-burned")).intValue();
                        var villageDamaged = (Boolean)workspace.report("village-damaged");

                        System.out.println("Density: " + i + " Percent burned: " + percentageBurned + "% Village damaged: " +villageDamaged);
                    }
                    workspace.dispose();
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            };

            workerThreadPool.submit(task1);
        }



        try {
            workerThreadPool.wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        exit(0);
    }

}
*/