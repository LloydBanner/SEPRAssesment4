/**
 * Added by Shaun of the Devs to meet the requirement of having a cure in the game
 */

package com.geeselightning.zepr.powerups;

import com.badlogic.gdx.graphics.Texture;
import com.geeselightning.zepr.Constant;
import com.geeselightning.zepr.Level;
import com.geeselightning.zepr.Player;

public class PowerUpCure extends PowerUp {

	private Level level;
	
    /**
     * Constructor for the healing power up
     * @param currentLevel level to spawn the power up in
     * @param player player to monitor for pick up event and to apply the effect to
     */
    public PowerUpCure(Level currentLevel, Player player) {
        super(new Texture("cure.png"), currentLevel, player, 0, "Cure PowerUp Collected");
        level = currentLevel;
    }

    /**
     * Cure zombies in a short radius
     */
    @Override
    public void activate() {
        super.activate();

        //Done in level class
        level.toCure = true;
        level.cureLocation[0] = getX();
        level.cureLocation[1] = getY();
    }
    
}
