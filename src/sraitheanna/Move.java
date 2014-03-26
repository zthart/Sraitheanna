package sraitheanna;

/*
*Made by Zach Hart for the 2014 Games++ Game Jam
*
*@author Zach Hart
*/

public class Move {
    
    private int startx, starty, endx, endy;
    private String toWhere;
    
    public Move(int startx, int starty, String toWhere, int endx, int endy){
        this.startx = startx;
        this.starty = starty;
        this.toWhere = toWhere;
        this.endx = endx;
        this.endy = endy;
    }
    public Move(String[] s){
        startx = Integer.parseInt(s[0]);
        starty = Integer.parseInt(s[1]);
        toWhere = s[2];
        endx = Integer.parseInt(s[3]);
        endy = Integer.parseInt(s[4]);
    }
    public int getStartX(){return startx;}
    public int getStartY(){return starty;}
    public int getEndX(){return endx;}
    public int getEndY(){return endy;}
    public String getToWhere(){return toWhere;}
}
