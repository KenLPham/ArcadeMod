package superhb.arcademod.client.tileentity;

public class ArcadeLeaderboard {
    private String playerName;
    private int score;
    private String difficulty;

    public ArcadeLeaderboard (String playerName, int score, String difficulty) {
        this.playerName = playerName;
        this.score = score;
        this.difficulty = difficulty;
    }

    public ArcadeLeaderboard (String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    public String getPlayerName () {
        return playerName;
    }

    public void setPlayerName (String playerName) {
        this.playerName = playerName;
    }

    public int getScore () {
        return score;
    }

    public void setScore (int score) {
        this.score = score;
    }

    public String getDifficulty () {
        return difficulty;
    }

    public void setDifficulty (String difficulty) {
        this.difficulty = difficulty;
    }
}
