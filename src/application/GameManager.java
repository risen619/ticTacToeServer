package application;

public class GameManager
{
    private Player[] players;
    private String turn;
    private boolean started;
    private String[][] field;
    private static GameManager instance = null;

    private GameManager()
    {
        players = new Player[]{ new Player(), new Player() };
        turn = "";
        started = false;
        field = new String[3][3];
        reset();
    }

    public static GameManager getInstance()
    {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public boolean getStarted() { return started; }
    public String getTurn() { return turn; }
    public void toggleTurn() { turn = turn.equals("X") ? "O" : "X"; }

    public boolean playersReady()
    {
        boolean result = true;
        for(Player p : players)
        	result &= p.ready;
        return result;
    }

    public void prepare()
    {
        int seed = (int)Math.random() * 10;
        players[0].sign = seed < 5 ? "X" : "O";
        players[1].sign = seed >= 5 ? "X" : "O";
        turn = seed >= 4 && seed <= 8 ? players[0].sign : players[1].sign;
    }

    public void start() { started = true; }

    public Player getPlayer(int index) { return players[index]; }

    public void reset()
    {
        for(int i=0; i<3; i++)
        	for(int j=0; j<3; j++)
        		field[i][j] = "";
        for(Player p : players)
        	p.ready = false;
        started = false;
    }

    public void addReady()
    {
    	int i;
        for(i = 0; i<players.length; i++)
        	if(!players[i].ready) break;
        players[i].ready = true;
    }

    private boolean _win(String sign)
    {
        int rowCount = 0;
        int colCount = 0;
        for(int i=0; i<3; i++)
        {
            rowCount = 0;
            colCount = 0;
            for(int j=0; j<3; j++)
            {
                if (field[i][j].equals(sign)) ++rowCount;
                if (field[j][i].equals(sign)) ++colCount;
            }
            if (rowCount == 3 || colCount == 3) return true;
        }
        rowCount = 0;
        colCount = 0;
        for(int i=0; i<3; i++)
        {
            if (field[i][i].equals(sign)) ++rowCount;
            if (field[2 - i][i].equals(sign)) ++colCount;
        }
        if (rowCount == 3 || colCount == 3) return true;
        return false;
    }

    public boolean filled()
    {
    	int c=0;
        for(int i=0; i<3; i++)
            for(int j=0; j<3; j++)
                if (!field[i][j].isEmpty()) ++c;
        System.out.println("Filled cells: " + c);
        if (c >= 9) return true;
        return false;
    }

    public String anybodyWin()
    {
        if (_win("O")) return "O";
        if (_win("X")) return "X";
        if (filled()) return "Draw";
        return "";
    }

    public void setCell(int id, String sign) { this.field[id / 3][id % 3] = sign; }

    class Player
    {
        boolean ready;
        String sign;

        Player()
        {
            ready = false;
            sign = "";
        }
    }
}