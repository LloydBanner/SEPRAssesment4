package com.geeselightning.zepr;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class Zombie extends Character {

    private int hitRange;
    public boolean isZombie;
    public Character closestAttackable = Level.getPlayer();
    public enum Type { ZOMBIE1, ZOMBIE2, ZOMBIE3, NONZOMBIE1, NONZOMBIE2, NONZOMBIE3, BOSS1, BOSS2 }
    private Type currentType;
    private Texture normalTexture;
    private Texture attackTexture;

    /**
     * Constructor for the Zombie class
     * @param zombieSpawn the coordinates to spawn the zombie at
     * @param world the Box2D world to add the zombie to
     * @param type the type of zombie to spawn
     * #changed:  Added assignment of different values for different zombie types.
     *            Hitrange now scales with sprite size. Box2D body code added.
     */
    public Zombie(Vector2 zombieSpawn, World world, Type type) {
        super(world);

        speed = Constant.ZOMBIESPEED;
        attackDamage = Constant.ZOMBIEDMG;
        maxhealth = Constant.ZOMBIEMAXHP;
        currentType = type;
        
        hitRefresh = (float) Math.random(); // Added to prevent all zombies from attacking at the same time

		set(new Sprite(new Texture("zombie01.png")));
        setType();

        body.setFixedRotation(true);
        body.setLinearDamping(50f);
        // Added by Shaun of the Devs to allow speed change
        setMaxLinearSpeed(speed);
        setCharacterPosition(zombieSpawn);

        hitRange = (int) (Constant.ZOMBIERANGE*getScaleX()*getWidth()/25 - getWidth()*getHealth()/1200);
    }
    
    // Moved to method by Shaun of the Devs to make type changing easier
    public void setType() {
    	switch(currentType) {
    		case ZOMBIE1:
    			speed *= 1;
    			attackDamage *= 1;
    			maxhealth *= 1;
    			isZombie = true;
    			normalTexture = new Texture("zombie01.png");
    			attackTexture = new Texture("zombie01_attack.png");
    			break;
    		case ZOMBIE2:
    			speed *= 1.2f;
    			attackDamage *= 2;
    			maxhealth *= 2;
    			isZombie = true;
    			normalTexture = new Texture("zombie02.png");
    			attackTexture = new Texture("zombie02_attack.png");
    			break;
    		case ZOMBIE3:
    			speed *= 2;
    			attackDamage *= 3;
    			maxhealth *= 1;
    			isZombie = true;
    			normalTexture = new Texture("zombie03.png");
    			attackTexture = new Texture("zombie03_attack.png");
    			break;
    		case NONZOMBIE1:
    			speed *= 1;
    			attackDamage *= 1;
    			maxhealth *= 0.5;
    			isZombie = false;
    			normalTexture = new Texture("player01.png");
    			attackTexture = new Texture("player01.png");
        		break;
    		case NONZOMBIE2:
    			speed *= 1.2f;
    			attackDamage *= 2;
    			maxhealth *= 1;
    			isZombie = false;
    			normalTexture = new Texture("player02.png");
    			attackTexture = new Texture("player02.png");
    			break;
    		case NONZOMBIE3:
    			speed *= 2;
    			attackDamage *= 3;
    			maxhealth *= 0.5;
    			isZombie = false;
    			normalTexture = new Texture("player02.png");
    			attackTexture = new Texture("player02.png");
    			break;
            case BOSS1:
                speed *= 100;
                attackDamage *= 2;
                maxhealth *= 5;
                isZombie = true;
                normalTexture = new Texture("GeeseLightningBoss.png");
                attackTexture = new Texture("GeeseLightningBoss.png");
                setScale(2);
                break;
            case BOSS2:
                speed *= 60;
                attackDamage *= 1;
                maxhealth *= 5;
                isZombie = true;
                normalTexture = new Texture("JJBossZombie.png");
                attackTexture = new Texture("JJBossZombie_attack.png");
                setScale(2);
                break;
    	}

        health = maxhealth;
        setMaxLinearSpeed(speed);
    }
    
    // Added by Shaun of the Devs
    // Allows zombies to switch between zombie and nonZombie
    public void switchType() {
    	switch(currentType) {
    		case ZOMBIE1:
    			currentType = Type.NONZOMBIE1;
    			break;
    		case ZOMBIE2:
    			currentType = Type.NONZOMBIE2;
    			break;
    		case ZOMBIE3:
    			currentType = Type.NONZOMBIE3;
    			break;
    		case NONZOMBIE1:
    			currentType = Type.ZOMBIE1;
    			break;
    		case NONZOMBIE2:
    			currentType = Type.ZOMBIE2;
    			break;
    		case NONZOMBIE3:
    			currentType = Type.ZOMBIE3;
    			break;
    	}
    	setType();
    }
    
    /**
     * Added by Shaun of the Devs to stop type switching on bosses
     * @return if zombies is a boss
     */
    public boolean isBoss() {
    	switch(currentType) {
    		case BOSS1:
    			return true;
    		case BOSS2:
    			return true;
    	}
		return false;
    }

    /**
     * Attack and damage the player if in range and hit counter refreshed
     * @param player instance of Player class to attack
     * @param delta the time between the start of the previous call and now
     */
    public void attack(Character character, float delta) {
        if (canHitGlobal(character, hitRange) && hitRefresh > Constant.ZOMBIEHITCOOLDOWN) {
            character.takeDamage(attackDamage);
            hitRefresh = 0;
        } else
            hitRefresh += delta;
    }

    /**
     * Method to update positional and action behavior
     * @param delta the time between the start of the previous call and now
     * #changed:  Code to remove from aliveZombies list when dead now moved to Level
     *            Added LibGDX AI steering behaviour and wandering when player undetected.
     */
    @Override
    public void update(float delta) {
        //move according to velocity
        super.update(delta);
        
        // Added by Shaun of the Devs for attack period, gives player more feedback on attacks
        if (hitRefresh > Constant.ZOMBIEHITCOOLDOWN) {
        	setTexture(attackTexture);
        } else {
        	setTexture(normalTexture);
        }
        
        if (hitRefresh > Constant.ZOMBIECOOLDOWNRESET) {
        	hitRefresh = 0;
        }

        if ((closestAttackable != null) && isZombie) {
            // seek out player using gdx-ai seek functionality
            this.steeringBehavior = SteeringPresets.getSeek(this, closestAttackable);
            this.currentMode = SteeringState.SEEK;
            // update direction to face the player
            direction = getDirectionTo(closestAttackable.getCenter());
        } else if((closestAttackable != null) && !isZombie){
            this.steeringBehavior = SteeringPresets.getEvade(this, closestAttackable);
            this.currentMode = SteeringState.EVADE;
            direction = -(this.vectorToAngle(this.getLinearVelocity()));
        }else { //player cannot be seen, so wander randomly
            this.steeringBehavior = SteeringPresets.getWander(this);
            this.currentMode = SteeringState.WANDER;
            // update direction to face direction of travel
            direction = -(this.vectorToAngle(this.getLinearVelocity()));
        }
    }
}
