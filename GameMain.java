
import javax.swing.JFrame;

public class GameMain {
    public static void main(String[] args) {

        JFrame frame = new JFrame();

        frame.setSize(600,700);
        //居中
        frame.setLocationRelativeTo(null);

        frame.add(new GamePanel());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }
}
