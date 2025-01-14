import org.nlogo.headless.HeadlessWorkspace;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    List<String> csvLines = new ArrayList<>();

    public static void main(String[] argv) throws IOException {
        var main = new Main();
        main.phase2();
        // phase3();
    }

    void phase2() {
        // In which the forest density is being adjusted between 50 up until 90, in steps of 5.
        for (var i = 1; i <=5; i += 1) {
            String strategy = getStrategy(i);
            for (var density = 50; density <= 60; density += 5) {
                startWorkspace(density, 0, 0, strategy, 0);
            }
        }
    }

    void phase3() {
        // In which the wind-speed and wind-direction will be added and adjusted, 0/3/7/23 N/W counts 8
        for (var i = 1; i <=5; i += 1) {
            String strategy = getStrategy(i);
            for (var density=50; density<=60; density+=5) {
                List<Integer> speedList = Arrays.asList(0, 3, 7 ,23);
                for (int speed : speedList) {
                    System.out.println(speed);
                    for (var dir=1; dir<=8; dir+=1) {  // 1:N NE E SE S SW W 8:NW
                        var speeds = calculateWind(speed, dir);
                        startWorkspace(density, (int)speeds[0], (int) speeds[1], strategy, 0);
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


    static float[] calculateWind(int speed, int dir) {
        // TODO !! Calculate actual wind speed for south, and west, based on desired wind-speed, and direction.
        float[] speeds = new float[2];
        speeds[0] = speed;  // SouthSpeed
        speeds[1] = dir;  // WestSpeed
        return speeds;
    }

    void startWorkspace(int forestDensity, int southWindSpeed, int westWindSpeed, String strategy, int ignitionLocation) {
        HeadlessWorkspace workspace = HeadlessWorkspace.newInstance();
        try {
            var nlogoFile = Paths.get(System.getProperty("user.dir"), "..", "Simple Fire extension.nlogo");
            workspace.open(nlogoFile.toAbsolutePath().toString());
            workspace.command("setup");
            workspace.command("set density " + forestDensity);
            workspace.command("set probability-of-spread 70");
            workspace.command("set south-wind-speed " + southWindSpeed);
            workspace.command("set west-wind-speed " + westWindSpeed);
            workspace.command("set strategy \"" + strategy + "\"");
            workspace.command("random-seed 0");
            workspace.command("run-full");
            var percentageBurned = ((Double)workspace.report("percent-burned")).intValue();
            var villageDamaged = (Boolean)workspace.report("village-damaged");

            csvLines.add(String.format("%i,%i,%i,%s,%.2f,%s",
                    forestDensity,
                    1,
                    1,
                    strategy,
                    percentageBurned,
                    villageDamaged ? "True" : "False"
                    ));
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