package sraitheanna;


/*
*Made by Zach Hart for the 2014 Games++ Game Jam
*
*@author Zach Hart
*/
import squidpony.squidcolor.SColor;

public class Cell {
    float resistance;
    char representation;
    boolean passable;
    SColor color;
    
    public Cell(float resistance, char representation, boolean passable, SColor color){
        this.resistance = resistance;
        this.representation  = representation;
        this.passable = passable;
        this.color = color;
    }
    
    public static Cell makeCell(char c){
        float res = 0f;
        SColor col;
        boolean pass = true;
        
        switch(c){
            case ',':
                col = SColor.GREEN_BAMBOO;
                break;
            case '.':
                col = SColor.DARK_SLATE_GRAY;
                break;
            case 'T':
                col = SColor.GREEN;
                res = 0.5f;
                pass = false;
                break;
            case '#':
                col = SColor.SLATE_GRAY;
                res = 1f;
                pass = false;
                break;
            case 'M':
                col = SColor.GOLDEN;
                break;
            case '>':
            case '<':
                col = SColor.PASTEL_PINK;
                break;
            case '/':
                col = SColor.BRASS;
                res = 0.75f;
                break;
            case '*':
                col = SColor.AMARANTH;
                res = 1f;
                break;
            case '?':
                col = SColor.AMARANTH;
                break;
            case 'w':
                col = SColor.AQUA;
                break;
            case 'v':
                col = SColor.ATOMIC_TANGERINE;
                break;
            case 'i':
                col = SColor.BLUE_VIOLET;
                break;
            case '+':
                col = SColor.ALOEWOOD;
                pass = false;
                break;
            case '=':
                col = SColor.CELADON;
                res = 0.2f;
                pass = false;
                break;
            default:
                col = SColor.BLACK;
                pass = false;
                res = 1f;   
        }
        return new Cell(res, c, pass, col);
    }
}
