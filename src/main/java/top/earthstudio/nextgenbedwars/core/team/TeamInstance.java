package top.earthstudio.nextgenbedwars.core.team;

import top.earthstudio.nextgenbedwars.api.team.Team;

public class TeamInstance {
    public Team team;
    public boolean isBedAlive = true;

    public TeamInstance(Team team) {
        this.team = team;
    }

    public void destroy() {
        this.team = null;
    }
}
