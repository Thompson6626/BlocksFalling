import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SandPanel extends JPanel implements Runnable {

    private static final int SCREEN_WIDTH = 500;
    private static final int SCREEN_HEIGHT = 700;
    private static final Dimension SCREEN_SIZE = new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT);
    private static final Color SAND_COLOR = new Color(240, 220, 130);
    private static final int UNIT_SIZE = 20;
    private static final int DELAY = 200;
    private boolean running = false;
    final static double AMOUNT_OF_TICKS = 60;

    Map<Character,int[]> map;
    final static int[][] GRAVITY = {
            {0,UNIT_SIZE/4}, // DOWN
            {UNIT_SIZE/4,0}, // RIGHT
            {0,-(UNIT_SIZE/4)}, // UP
            {-(UNIT_SIZE/4),0}  // LEFT
    };
    char gravityState='D';
    int gravityX = GRAVITY[0][0];
    int gravityY = GRAVITY[0][1];
    List<Rectangle> referenceSquares;
    List<Rectangle> squares;
    List<Rectangle> staticSquares;
    Thread thread;
    Timer sandGeneratorTimer;

    SandPanel(){
        this.setPreferredSize(SCREEN_SIZE);
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(new KeyClass());

        map = new HashMap<>();

        map.put('D', GRAVITY[0]);
        map.put('R', GRAVITY[1]);
        map.put('U', GRAVITY[2]);
        map.put('L', GRAVITY[3]);


        start();
    }

    private void start(){

        initializeReferenceSquares();

        squares = new ArrayList<>();
        staticSquares = new ArrayList<>();

        running = true;
        thread = new Thread(this);
        thread.start();

        sandGeneratorTimer = new Timer(DELAY, e -> generateSquareAccordingToMousePosition(
                MouseInfo.getPointerInfo().getLocation().x - this.getLocationOnScreen().x,
                MouseInfo.getPointerInfo().getLocation().y - this.getLocationOnScreen().y
        ));
        sandGeneratorTimer.start();
    }

    private void initializeReferenceSquares() {
        referenceSquares = new ArrayList<>();
        int rows = SCREEN_HEIGHT / UNIT_SIZE;
        int columns = SCREEN_WIDTH / UNIT_SIZE;

        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j <= columns; j++) {
                referenceSquares.add(new Rectangle(
                        j * UNIT_SIZE,
                        i * UNIT_SIZE,
                        UNIT_SIZE,
                        UNIT_SIZE
                ));
            }
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){

        Graphics2D g2D = (Graphics2D) g;

        g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


        if(running){


            g2D.setColor(SAND_COLOR);

            squares.forEach(square -> g2D.fillRect(
                    (int) square.getX(),
                    (int) square.getY(),
                    UNIT_SIZE,
                    UNIT_SIZE));

            List<Rectangle> staticSquaresCopy = new ArrayList<>(staticSquares);
            staticSquaresCopy.forEach(staticSquare -> g2D.fillRect(
                    (int) staticSquare.getX(),
                    (int) staticSquare.getY(),
                    UNIT_SIZE,
                    UNIT_SIZE));
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double ns = 1000000000 / AMOUNT_OF_TICKS;
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            if (delta >= 1) {
                moveCurrentSquares();
                checkCollisions();
                repaint();
                delta--;
            }
        }
    }

    private void moveCurrentSquares() {

        squares.forEach(square -> {
            square.x += gravityX;
            square.y += gravityY;
        });
    }

    private void checkCollisions() {
        List<Rectangle> squaresToRemove = new ArrayList<>();
        List<Rectangle> squaresToAdd = new ArrayList<>();

        for (Rectangle square : squares) {
            if (
                    (square.y >= SCREEN_HEIGHT - UNIT_SIZE && gravityState == 'D')
                            || (square.x >= SCREEN_WIDTH - UNIT_SIZE && gravityState == 'R')
                            || (square.y <= 0 && gravityState == 'U')
                            || (square.x <= 0 && gravityState == 'L')
            ) {
                square.x += (gravityX) * -1;
                square.y += (gravityY) * -1;
                squaresToRemove.add(square);
                squaresToAdd.add(square);
            }


            for (Rectangle staticSquare : staticSquares) {
                if (square.intersects(staticSquare)) {
                    square.x += (gravityX) * -1;
                    square.y += (gravityY) * -1;
                    squaresToRemove.add(square);
                    squaresToAdd.add(square);
                }
            }
        }

        squares.removeAll(squaresToRemove);
        staticSquares.addAll(squaresToAdd);
    }

    private void generateSquareAccordingToMousePosition(int x, int y){

        for(Rectangle rectangle:referenceSquares){
            if(rectangle.contains(x,y)){
                squares.add(new Rectangle(
                                rectangle.x,
                                rectangle.y,
                                UNIT_SIZE,
                                UNIT_SIZE
                        )
                );
                break;
            }
        }
    }

    private class KeyClass extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();

            if (keyCode == KeyEvent.VK_SPACE) {
                if (sandGeneratorTimer.isRunning())
                    sandGeneratorTimer.stop();
                else
                    sandGeneratorTimer.restart();
            } else if (keyCode == KeyEvent.VK_T) {
                cleanReset();
            } else if (keyCode == KeyEvent.VK_A ||
                    keyCode == KeyEvent.VK_S ||
                    keyCode == KeyEvent.VK_D ||
                    keyCode == KeyEvent.VK_W) {

                switch (keyCode) {
                    case KeyEvent.VK_A -> gravityState = 'L';
                    case KeyEvent.VK_S -> gravityState = 'D';
                    case KeyEvent.VK_D -> gravityState = 'R';
                    case KeyEvent.VK_W -> gravityState = 'U';
                }

                gravityX = map.get(gravityState)[0];
                gravityY = map.get(gravityState)[1];
                squares.addAll(staticSquares);
                staticSquares.clear();
            }
        }
    }

    private void cleanReset(){
        staticSquares = new ArrayList<>();
    }

}
