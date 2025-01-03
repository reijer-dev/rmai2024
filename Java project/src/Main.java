import org.nlogo.app.App;
import org.nlogo.headless.HeadlessWorkspace;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;

public class Main {
    public static void main(String[] argv) throws IOException {
        var nlogoFile = Paths.get(System.getProperty("user.dir"), "..", "Simple Fire extension.nlogo");
        var workspace = HeadlessWorkspace.newInstance();
        workspace.open(nlogoFile.toAbsolutePath().toString(), true);
        try {
            for(var i = 50; i <= 90; i+=5) {
                workspace.command("setup");
                workspace.command("set density " + i);
                workspace.command("set probability-of-spread 85");
                workspace.command("set south-wind-speed 0");
                workspace.command("set west-wind-speed 0");
                var strategy = "Even";
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
    }
}
