import javax.swing.*;
import java.awt.*;

public class Frame extends JFrame {

    SandPanel panel;

    Frame() {
        panel = new SandPanel();

        this.add(panel);
        this.setTitle("Sand falling");
        this.setResizable(false);
        this.setBackground(Color.BLACK);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);

    }
}
