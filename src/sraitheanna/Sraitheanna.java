package sraitheanna;

/*
*Made by Zach Hart for the 2014 Games++ Game Jam
*
*@author Zach Hart
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;
import java.awt.event.KeyListener;
import java.util.*;
import java.io.*;
import javax.swing.JFrame;
import javax.sound.sampled.*;

import squidpony.squidcolor.SColor;
import squidpony.squidcolor.SColorFactory;
import squidpony.squidgrid.gui.swing.SwingPane;
import squidpony.squidgrid.util.Direction;
import squidpony.squidgrid.fov.*;
import static squidpony.squidgrid.util.Direction.*;

public class Sraitheanna{
    
    /*
    *The Wrapper SwingPanes are the only two that get added to the frame
    *textWrapper gets added to wrapper BorderLayout.SOUTH
    *wrapper gets added to frame at BorderLayout.CENTER
    */
    private SwingPane wrapper;
    private SwingPane textWrapper;
    
    /*
    *mapDisplay gets added to wrapper at BorderLayout.NORTH
    *textDisplay gets added to textWrapper at BorderLayout.SOUTH
    *memDisplay gets added to textWrapper at BorderLayout.NORTH
    */
    private SwingPane mapDisplay;
    private SwingPane textDisplay;
    private SwingPane memDisplay;
    
    /*
    *JFrame that the wrapper Panes get added to
    */
    private JFrame frame;
    
    private String startMap = "clearing";                                   //name of start file, could be static final
    private String[] currentMap;                                            //2D String array/3D char array of the current displayed map
    private List<Move> moves = new ArrayList<>();                           //ArrayList of all map transitions possible on current map
    private Move lastSearchedMove;                                          //really crappy way to use the move without rewriting search to return move, not boolean, or SGPair
    private Cell[][] map;                                                   //current map converted into 3D of Cell
    private float[][] light;                                                //3D float array of light values for doFOV
    private float[][] resistances;                                          //3D float array of object resistances for doFOV
    private float lightForce, fovRadius;                                    //Force of light and the radius for the light to be applied over
    private SColor litNear, litFar, replaceForeground = SColor.COBALT;      //Colors for light mapping and doFOV
    private boolean lightBackground = false;                                //If the background is being lit
    private int mapWidth, mapHeight;                                        //width and height integers of current map
    private int cellWidth, cellHeight, locx, locy;                          //width and height of cells in map, current x,y location of '@' char
    
    private String[] storyText;                                             //2D String array of all story messages, stored chronologically
    private int memoriesCollected = 0;                                      //Number of memories to display in memText SwingPane
    private int memoryGet = 0;                                              //a bit redundant, memoriesCollected gets set to memoryGet when you get a new memory
    private boolean allMemories = false, canLeave = false;                  //control booleans to cause ending scene
    private int stepsToCredits;                                             //control integer to lower FOV to 1 at end
    List<String> storyLines;                                                //every line in story text, minus separators
    List<String> currentLines;                                              //current group of lines, always <=8
    List<Integer[]> visitedItems = new ArrayList<>();                       //coordinate bundles of visited '?'
    
    public Sraitheanna() throws IOException{
        currentMap = setCurrentMap(startMap + ".map");
        writeMapToVars();
        locx = 37;
        locy = 29;
        
        frame = new JFrame("Sraitheanna - Zach Hart - Games++ 2014");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        textWrapper = new SwingPane();
        wrapper = new SwingPane();
        mapDisplay = new SwingPane();
        textDisplay = new SwingPane();
        memDisplay = new SwingPane();
        
        wrapper.setLayout(new BorderLayout());
        textWrapper.setLayout(new BorderLayout());
        System.out.println(mapWidth +", "+ mapHeight );
        wrapper.initialize(mapWidth, mapHeight+10, new Font("Monospace", Font.BOLD, 14));
        mapDisplay.initialize(mapWidth, mapHeight, new Font("Monospace", Font.BOLD, 14));
        textDisplay.initialize(mapWidth, 8, new Font("Monospace", Font.BOLD, 14));
        memDisplay.initialize(mapWidth, 2, new Font("Monospace", Font.BOLD, 14));
        textWrapper.initialize(mapWidth, 10, new Font("Monospace", Font.BOLD, 14));
        
        clear();
        
        textWrapper.add(memDisplay, BorderLayout.NORTH);
        textWrapper.add(textDisplay, BorderLayout.SOUTH);
        wrapper.add(mapDisplay, BorderLayout.NORTH);
        wrapper.add(textWrapper, BorderLayout.SOUTH);
        frame.add(wrapper, BorderLayout.CENTER);
        writeMemText("Memories: " + memoriesCollected);
        
        frame.setVisible(true);
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.repaint();
        
        cellWidth = mapDisplay.getCellDimension().width;
        cellHeight = mapDisplay.getCellDimension().height;   
        
        frame.addKeyListener(new InputListener());
        doFOV(locx, locy);
        
        getStoryText();
    }
    private void writeMapToVars(){
        map = new Cell[mapHeight][mapWidth];
        resistances = new float[mapHeight][mapWidth];
        for(int y = 0; y < mapHeight; y++){
            for(int x = 0; x < mapWidth; x++){
                char c = currentMap[y].charAt(x);
                map[y][x] = Cell.makeCell(c);
                resistances[y][x] = map[y][x].resistance;
            }
        }
    } 
    private void clear(){
        for(int y = 0 ; y < mapHeight; y++)
            for(int x = 0; x < mapWidth; x++)
                mapDisplay.placeCharacter(x, y, map[y][x].representation, map[y][x].color, SColor.BLACK);
        mapDisplay.refresh();
    }
    private void getStoryText() throws IOException{
        Scanner scan = new Scanner(new File("text.story"));
        storyLines = new ArrayList();
        String line;
        while(scan.hasNext()){
            line = scan.nextLine();
            System.out.println(line);
            storyLines.add(line);
        }
    }
    private void pushStoryText()throws IOException{
        currentLines = new ArrayList<>();
        for(int y = 0; y < textDisplay.getGridHeight(); y ++)
            for(int x = 0; x < textDisplay.getGridWidth(); x++)
                textDisplay.clearCell(x, y);
        String line = storyLines.get(0);
        while(!line.equals("-")){
            currentLines.add(storyLines.remove(0));
            line = storyLines.get(0);
        }
        storyLines.remove(0);
        for(int i =0; i < currentLines.size(); i++){
            System.out.println(currentLines.get(i));
            textDisplay.placeHorizontalString(0, i, currentLines.get(i));
        }
        textDisplay.refresh();
    }
    private void writeToMessageBox(String s){
        for(int y = 0; y < textDisplay.getGridHeight(); y ++)
            for(int x = 0; x < textDisplay.getGridWidth(); x++)
                textDisplay.clearCell(x, y);
        textDisplay.placeHorizontalString(0, 1, s);
    }
    private boolean hasVisitedStoryText(int atx, int aty){
        for(int i = 0; i < visitedItems.size(); i++)
            if(visitedItems.get(i)[0] == atx && visitedItems.get(i)[1] == aty)
                return true;
        return false;
    }
    private void writeMemText(String s){
        memDisplay.placeHorizontalString(0, 0, s);
        memDisplay.refresh();
    }
    private String[] setCurrentMap(String s) throws IOException{
        Scanner scan = new Scanner(new File(s));
        List<String> newMap = new ArrayList<>();
        boolean foundSplit = false;
        String line;
        fovRadius = Float.parseFloat(scan.nextLine());
        while(scan.hasNext() && !foundSplit){
            line = scan.nextLine();
            if(!line.equals("-"))
                newMap.add(line);
            else
                foundSplit = true;
        }
        moves = new ArrayList<>();
        while(scan.hasNext())
            addMoveInstructions(scan.nextLine());
        mapWidth = newMap.get(0).length();
        mapHeight = newMap.size();
        return makeRay(newMap);
    }
    private String[] makeRay(List<String> l){
        String[] ray = new String[l.size()];
        for(int i = 0; i < l.size(); i++)
            ray[i] = l.get(i);
        return ray;
    }
    private void addMoveInstructions(String s)throws IOException{
        moves.add(new Move(s.split(" ")));
    }
    private void move(Direction dir){
        int x = locx + dir.deltaX;
        int y = locy + dir.deltaY;
        
        if(x >= 0 &&
                x < mapWidth &&
                y >= 0 &&
                y < mapHeight &&
                map[y][x].passable){
            if(map[y][x].representation != '>' &&
                map[y][x].representation != '<'){
                locx = x;
                locy = y;
                doFOV(x, y);
                if(allMemories)
                    fovRadius--;
                
                if((int)fovRadius == 0){
                    try{
                        pushStoryText();
                    }
                    catch(IOException e){}
                }
                if(map[y][x].representation == '?' &&
                        !hasVisitedStoryText(x, y))
                    try{
                        Integer[] newCoord = new Integer[2];
                        System.out.print("Message Displayed - ");
                        newCoord[0] = x;
                        newCoord[1] = y;
                        visitedItems.add(newCoord);
                        memoriesCollected = memoryGet;
                        writeMemText("Memories: " + memoriesCollected);
                        if(memoriesCollected > 0)
                            canLeave = true;
                        if(memoriesCollected == 8){
                            allMemories = true;
                            pushStoryText();
                        }
                        else{
                            memoryGet++;
                            pushStoryText();
                        }
                        
                    }
                    catch(IOException e){
                        System.err.println("RIP IN PIECES");
                    }
                System.out.println(x +", "+ y + " " + validMoveInstruction(x, y));
            } else if (validMoveInstruction(x, y) && canLeave){
                if(canLeave){
                    try{
                        wrapper.remove(mapDisplay);
                        mapDisplay = new SwingPane();

                        currentMap = setCurrentMap(lastSearchedMove.getToWhere() + ".map");
                        writeMapToVars();

                        mapDisplay.initialize(mapWidth, mapHeight, new Font("Monospace", Font.BOLD, 14));
                        clear();

                        wrapper.add(mapDisplay, BorderLayout.NORTH);

                        canLeave = false;
                        locx = lastSearchedMove.getEndX();
                        locy = lastSearchedMove.getEndY();
                        doFOV(locx, locy);
                    }
                    catch(IOException e){
                        System.err.println("FUCK");
                    }
                }
                
            }
        }
    }
    public boolean validMoveInstruction(int startx, int starty){
        for(int i = 0; i < moves.size(); i ++){
            if(moves.get(i).getStartX() == startx && moves.get(i).getStartY() == starty){
                lastSearchedMove = moves.get(i);
                return true;
            }
        }
        return false;
    }
    private void doFOV(int startx, int starty){
        lightForce = 0.5f;
        litNear = SColor.WHITE;
        litFar = SColor.BLUE_GREEN_DYE;
        light = new TranslucenceWrapperFOV().calculateFOV(resistances, starty, startx, fovRadius);
        
        SColorFactory.addPallet("light", SColorFactory.asGradient(litNear, litFar));
        for(int y = 0; y < mapHeight; y++){
            for(int x = 0; x < mapWidth; x++){
                if(light[y][x] > 0f){
                    if(lightBackground){
                        mapDisplay.placeCharacter(x, y, map[y][x].representation, replaceForeground, SColorFactory.fromPallet("light", 1-light[y][x]));    
                    } else {
                        float bright = 1- light[y][x];
                        SColor cellLight = SColorFactory.fromPallet("light", bright);
                        SColor objectLight = SColorFactory.blend(map[y][x].color, cellLight,.35f);
                        mapDisplay.placeCharacter(x, y, map[y][x].representation, objectLight);
                    }
                } else {
                    mapDisplay.clearCell(x, y);
                }
            }
        }
        
        float bright = 1 - light[starty][startx];
         SColor cellLight = SColorFactory.fromPallet("light", bright);
        SColor objectLight = SColorFactory.blend(SColor.ALICE_BLUE, cellLight, 1f);
        if(lightBackground)
            mapDisplay.placeCharacter(startx, starty, '@', replaceForeground, objectLight);
        else
            mapDisplay.placeCharacter(startx, starty, '@', objectLight);
        mapDisplay.refresh();
    }
    private class InputListener implements KeyListener{
        @Override public void keyReleased(KeyEvent e){}
        @Override public void keyTyped(KeyEvent e){}
        @Override public void keyPressed(KeyEvent e){
            int code = e.getExtendedKeyCode();
            move(getDirection(code));
        }
        
        private Direction getDirection(int code){
            switch(code) {
                case VK_LEFT:
                case VK_NUMPAD4:
                case VK_A:
                    return LEFT;
                case VK_RIGHT:
                case VK_NUMPAD6:
                case VK_D:
                    return RIGHT;
                case VK_UP:
                case VK_NUMPAD8:
                case VK_W:
                    return UP;
                case VK_DOWN:
                case VK_NUMPAD2:
                case VK_S:
                    return DOWN;
                case VK_NUMPAD1:
                    return DOWN_LEFT;
                case VK_NUMPAD3:
                    return DOWN_RIGHT;
                case VK_NUMPAD7:
                    return UP_LEFT;
                case VK_NUMPAD9:
                    return UP_RIGHT;
                default:
                    return NONE;
            }
        }
    }
    public static void main(String... args)throws IOException{
        new Sraitheanna();
        
        new Thread(new Runnable() {
            @Override
            public void run() {  
                try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("Sraitheanna.wav"));
                clip.open(inputStream);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                } catch (LineUnavailableException e) {
                    System.err.println(e.getMessage());
                } catch (UnsupportedAudioFileException e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
        
    }
}