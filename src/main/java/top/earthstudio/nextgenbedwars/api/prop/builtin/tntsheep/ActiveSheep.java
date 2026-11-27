package top.earthstudio.nextgenbedwars.api.prop.builtin.tntsheep;

import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.TextDisplay;

import top.earthstudio.nextgenbedwars.api.particle.ParticleAnimationSequence;
import top.earthstudio.nextgenbedwars.api.team.Team;

import java.util.UUID;

public final class ActiveSheep { // ATTENTION: This was built by LLM.
    public Sheep sheep;
    public BlockDisplay tntDisplay;
    public TextDisplay textDisplay;
    public Player owner;
    public Team ownerTeam;
    public Player target;
    public int remainingTicks;
    public boolean flashState;

    public ParticleAnimationSequence fuseSequence;
    public UUID fuseParticleUuid;

    public ActiveSheep(Sheep sheep, BlockDisplay tntDisplay, TextDisplay textDisplay,
                       Player owner, Team ownerTeam, Player target, int remainingTicks,
                       ParticleAnimationSequence fuseSequence, UUID fuseParticleUuid) {
        this.sheep = sheep;
        this.tntDisplay = tntDisplay;
        this.textDisplay = textDisplay;
        this.owner = owner;
        this.ownerTeam = ownerTeam;
        this.target = target;
        this.remainingTicks = remainingTicks;
        this.fuseSequence = fuseSequence;
        this.fuseParticleUuid = fuseParticleUuid;
    }
}