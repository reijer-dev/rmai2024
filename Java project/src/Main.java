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
            var sb = new StringBuilder("""
                    \\begin{tikzpicture}
                    \\begin{axis}
                        \\addplot+ [
                            smooth,
                        ] coordinates {
                    (0,0)\s""");
            for(var i = 50; i <= 90; i+=5) {
                workspace.command("setup");
                workspace.command("set density " + i);
                workspace.command("random-seed 0");
                workspace.command("setup");
                workspace.command("repeat 150 [ go ]");
                var value = (Double)workspace.report("percent-burned");
                sb.append("("+i+","+value.intValue()+") ");
                System.out.println("Density: " + i + " Percent burned: " + value + "%");
            }
            workspace.dispose();
            sb.append("""
                        };
                    \\end{axis}
                    \\end{tikzpicture}""");
            System.out.println(sb);

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
