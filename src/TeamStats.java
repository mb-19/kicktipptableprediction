package src;

class TeamStats {
    int points = 0;
    int goalDifference = 0;
    int games = 0;

    TeamStats() {
    }

    void updateStats(int goalsFor, int goalsAgainst) {
        ++this.games;
        if (goalsFor > goalsAgainst) {
            this.addWin(goalsFor - goalsAgainst);
        } else if (goalsFor < goalsAgainst) {
            this.addLoss(goalsAgainst - goalsFor);
        } else {
            this.addDraw();
        }

    }

    void addWin(int goalDiff) {
        this.points += 3;
        this.goalDifference += goalDiff;
    }

    void addLoss(int goalDiff) {
        this.goalDifference -= goalDiff;
    }

    void addDraw() {
        ++this.points;
    }
}