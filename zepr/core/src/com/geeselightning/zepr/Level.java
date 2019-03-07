package com.geeselightning.zepr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.geeselightning.zepr.Zombie.Type;
import com.geeselightning.zepr.powerups.*;
import com.geeselightning.zepr.screens.TextScreen;
import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;


public class Level implements Screen {

    private Zepr parent;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private static Player player;
    private ArrayList<Zombie> aliveZombies;
    private ArrayList<Zombie> nonZombies; //Added by Shaun of the Devs
    private ZeprInputProcessor inputProcessor;
    private boolean isPaused;
    private Stage stage;
    private Table table;
    private Table tutorialTable = null;
    private Skin skin;
    private int currentWaveNumber;
    private int zombiesRemaining; // the number of zombies left to kill to complete the wave
    private int zombiesToSpawn; // the number of zombies that are left to be spawned this wave
    private int nonZombiesToSpawn; // Added by Shaun of the Devs for non zombie spawning
    private PowerUp currentPowerUp;
    //private Box2DDebugRenderer debugRenderer;
    private LevelConfig config;
    private World world;
    private int teleportCounter;
    private Label progressLabel, healthLabel, powerUpLabel, abilityLabel, tutorialLabel;
    static Texture blank;
    private Zombie originalBoss;
    public boolean toCure = false; // Added to work with cure power up
    public Float[] cureLocation = new Float[2]; // Added to work with cure power up

    /**
     * Constructor for the level
     * @param zepr the instance of the Zepr class to use
     * @param config level configuration to use
     * #changed:   Moved most of the code from show() to here
     */
    public Level(Zepr zepr, LevelConfig config) {

        //Initialise Box2D physics engine
    	this.world =  new World(new Vector2(0, 0), true);
    	
    	parent = zepr;
    	this.config = config;
        blank = new Texture("blank.png");
        
        player = new Player(new Texture("player01.png"), new Vector2(300, 300), world);
        
        skin = new Skin(Gdx.files.internal("skin/pixthulhu-ui.json"));
        aliveZombies = new ArrayList<>();
        nonZombies = new ArrayList<>();
        inputProcessor = new ZeprInputProcessor();
        
        progressLabel = new Label("", skin);
        healthLabel = new Label("", skin);
        powerUpLabel = new Label("", skin);
        abilityLabel = new Label("", skin);
         
        // Set up data for first wave of zombies
        this.zombiesRemaining = config.waves[0].numberToSpawn;
        this.zombiesToSpawn = zombiesRemaining;

        // Creating a new libgdx stage to contain the pause menu and in game UI
        this.stage = new Stage(new ScreenViewport());

        // Creating a table to hold the UI and pause menu
        this.table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        if(config.location == Zepr.Location.TOWN) {
        	tutorialTable = new Table();
        	tutorialTable.setFillParent(true);
        	stage.addActor(tutorialTable);
        	
        	tutorialLabel = new Label("", skin);
        	
        	tutorialTable.top();
        	tutorialTable.row().pad(50);
        	tutorialTable.add(tutorialLabel).top();
        
        }
        
        
        // Loads the .tmx file as map for the specified location.
        TmxMapLoader loader = new TmxMapLoader();
        map = loader.load(config.mapLocation);


        // renderer renders the .tmx map as an orthogonal (top-down) map.
        renderer = new OrthogonalTiledMapRenderer(map, Constant.WORLDSCALE);
           
        //debugRenderer = new Box2DDebugRenderer();
        
        MapBodyBuilder.buildShapes(map, Constant.PHYSICSDENSITY / Constant.WORLDSCALE, world);

        
        // It is only possible to view the render of the map through an orthographic camera.
        camera = new OrthographicCamera();

        //reset player instance
        player.respawn(config.playerSpawn);

        Gdx.input.setInputProcessor(inputProcessor);

        teleportCounter = 0;
        currentWaveNumber = 0;

        resumeGame();
    }

    public void setCurrentPowerUp(PowerUp currentPowerUp) {
        this.currentPowerUp = currentPowerUp;
    }

    public LevelConfig getConfig() {
        return config;
    }

    public static Player getPlayer() {
    	return player;
    }

    /**
     * Called when the player's health <= 0 to end the stage.
     */
    private void gameOver() {
        isPaused = true;
        parent.setScreen(new TextScreen(parent, "You died."));
    }


    /**
     * Spawns multiple zombies and nonZombies cycling through spawnPoints until the given amount have been spawned.
     * @param spawnPoints locations where zombies should be spawned on this stage
     * @param numberToSpawn number of zombies to spawn
     * @param waveType the type of zombies/nonZombies in the wave
     */
    private void spawnZombies(int numberToSpawn, ArrayList<Vector2> spawnPoints, Type waveType) {

    	// Modified by Shaun of the Devs to allow different wave types to spawn 
        for (int i = 0; i < numberToSpawn; i++) {
            Zombie.Type type = waveType;
            Zombie zombie = new Zombie(spawnPoints.get(i % spawnPoints.size()), world, type);
            if (zombie.isZombie) {
            	aliveZombies.add(zombie);
            } else {
            	nonZombies.add(zombie);
            }
            if(type == Zombie.Type.BOSS2 && aliveZombies.size()==1)
                originalBoss = zombie;
        }
    }

    /**
     * Converts the mousePosition which is a Vector2 representing the coordinates of the mouse within the game window
     * to a Vector2 of the equivalent coordinates in the world.
     *
     * @return Vector2 of the mouse position in the world.
     */
    private Vector2 getMouseWorldCoordinates() {
        // Must first convert to 3D vector as camera.unproject() does not take 2D vectors.
        Vector3 screenCoordinates = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        Vector3 worldCoords3 = camera.unproject(screenCoordinates);

        return new Vector2(worldCoords3.x, worldCoords3.y);
    }

    @Override
    public void show() {
    }

    /**
     * Run this procedure once to set the game to pause mode
     */
    private void pauseGame() {
        isPaused = true;
        // Input processor has to be changed back once unpaused.
        Gdx.input.setInputProcessor(stage);

        TextButton resume = new TextButton("Resume", skin);
        TextButton exit = new TextButton("Exit", skin);
        
        if(tutorialTable != null)
        	tutorialTable.clear();
        
        table.clear();
        table.center();
        table.add(resume).pad(10);
        table.row();
        table.add(exit);
        // Defining actions for the resume button.
        resume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Change input processor back
                Gdx.input.setInputProcessor(inputProcessor);
                resumeGame();
            }
        });

        // Defining actions for the exit button.
        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	saveGame();
            }
        });
    }

    /**
     * Save the current progress in the game to a text file
     */
    private void saveGame() {
        File f = new File("saveData.txt");
        FileOutputStream edit;
        try {
            edit = new FileOutputStream(f);
            byte[] lvl = (Integer.toString(Zepr.progress.ordinal())).getBytes();
            edit.write(lvl);
            edit.close();
            Gdx.app.log("Save status", "Saved!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        parent.changeScreen(Zepr.Location.SELECT);
    }

    /**
     *  Run this procedure once to resume the game after pausing or upon level loading
     *  Sets up GUI labels
     */
    private void resumeGame() {
        isPaused = false;
        table.clear();
        table.top().left();
        table.add(progressLabel).pad(10).left();
        table.row().pad(10);
        table.add(healthLabel).pad(10).left();
        table.row();
        table.add(powerUpLabel).pad(10).left();
        table.row();
        table.add(abilityLabel).pad(10).left();
        
        if(tutorialTable != null && currentWaveNumber == 1) {
        	tutorialTable.top();
        	tutorialTable.row().pad(50);
        	tutorialTable.add(tutorialLabel).top();
        }
    }
    
    // Added by Shaun of the Devs to determine who each character should attack
    public Character getClosestAttackable(boolean isZombie, Character attacker) {
        ArrayList<Character> attackable = new ArrayList<>();
    	if (isZombie) {
            for (Zombie nonZombie : nonZombies) {
            	attackable.add(nonZombie);
            }
            if (player.isVisible()) {
            	attackable.add(player);
            }
    	} else {
            for (Zombie zombie : aliveZombies) {
            	attackable.add(zombie);
            }
    	}
    	float closestDistance = 100000000; 
    	Character closestChar = null;
    	for (Character character : attackable) {
    		 float xDistance = attacker.getPosition().x - character.getPosition().x;
    		 float yDistance = attacker.getPosition().y - character.getPosition().y;
    		 float distance = (float) Math.sqrt(Math.pow(yDistance, 2) + Math.pow(xDistance, 2));
    		 if (closestDistance > distance) {
    			 closestDistance = distance;
    			 closestChar = character;
    		 }
    	}
    	return closestChar;
    }

    /**
     * Render the level and its contents to the screen
     * @param delta the time between the start of the previous call and now
     * #changed:   Moved most of the code from here to update(). Moved render code for
     *             zombies and players into their own classes to increase encapsulation
     */
    @Override
    public void render(float delta) {
    	
    	 // Clears the screen to black.
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!isPaused){

            update(delta);

            if (!isPaused) {

                // Keep the player central in the screen.
                camera.position.set(player.getCenter().x, player.getCenter().y, 0);
                camera.update();

                renderer.setView(camera);
                renderer.render();

                Batch batch = renderer.getBatch();
                batch.begin();

                player.draw(batch);

                // Draw zombies
                for (Zombie zombie : aliveZombies)
                    zombie.draw(batch);
                
                // Added by Shaun of the Devs to draw nonZombies
                for (Zombie nonZombie : nonZombies)
                    nonZombie.draw(batch);

                if (currentPowerUp != null) {
                    // Activate the powerup up if the player moves over it and it's not already active
                    // Only render the powerup if it is not active, otherwise it disappears
                    if (!currentPowerUp.isActive()) {
                        if (currentPowerUp.overlapsPlayer())
                            currentPowerUp.activate();
                        currentPowerUp.draw(batch);
                    }
                    currentPowerUp.update(delta);
                }

                batch.end();

                //debugRenderer.render(world, camera.combined.scl(Constant.PHYSICSDENSITY));
            }
        }
        
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        if (Gdx.input.isKeyPressed(Keys.ESCAPE))
            pauseGame();
    }

    /**
     * Update everything in the level
     * @param delta the time between the start of the previous call and now
     * #changed:   Added this method, most of the code here was in render().
     *             Optimised a lot of the original code and increased encapsulation
     */
    public void update(float delta) {
        world.step(1/60f, 6, 2);

        player.update(delta);
        player.look(getMouseWorldCoordinates());

        //#changed:   Added tutorial text code
        if(tutorialTable != null && currentWaveNumber > 1) {
            tutorialTable.clear();
        }

        // When you die, end the level.
        if (player.health <= 0)
            gameOver();
        
        //#changed:   Moved this zombie removal code here from the Zombie class
        for(int i = 0; i < aliveZombies.size(); i++) {
        	
            Zombie zomb = aliveZombies.get(i);
            zomb.update(delta);
            
            // Added by Shaun of the Devs to cure zombies with power up
            // Cures zombies in a small area around the cure power up 
            if (toCure) {
            	if (!zomb.isBoss()) {
            		if (Math.abs(zomb.getX() - cureLocation[0]) < 100) {
            			if (Math.abs(zomb.getY() - cureLocation[1]) < 100) {
            					zomb.switchType();
            					zombiesRemaining--;
            					aliveZombies.remove(zomb);
            					nonZombies.add(zomb);
            			}
            		}
            	}
            }
            
            if (zomb.getHealth() <= 0) {
                zombiesRemaining--;
                aliveZombies.remove(zomb);
                zomb.dispose();
            }
        }
		toCure = false;

        zombiesRemaining = aliveZombies.size();

        // Resolve all possible attacks
        for (Zombie zombie : aliveZombies) {
            // Zombies will only attack if they are in range, the attack has cooled down, and they are
            // facing a player or nonZombie.
            // Player will only attack in the reverse situation but player.attack must also be true. This is
            //controlled by the ZeprInputProcessor. So the player will only attack when the user clicks.

        	//Changed by Shaun of the Devs to accomodate nonZombies
            zombie.closestAttackable = getClosestAttackable(true, zombie);
        		
            if (player.isAttackReady())
                player.attack(zombie, delta);
            if(zombie.closestAttackable != null)
            	zombie.attack(zombie.closestAttackable, delta);
        }
        
        // Added by Shaun of the Devs for nonZombies
        for(int i = 0; i < nonZombies.size(); i++) {
            Zombie zomb = nonZombies.get(i);
            zomb.update(delta);
            
            if (zomb.getHealth() <= 0) {
            	zomb.health = zomb.maxhealth;
            	zomb.switchType();
                nonZombies.remove(zomb);
                aliveZombies.add(zomb);
            }
        }
        
        // Added by Shaun of the Devs for nonZombies
        for (Zombie zombie : nonZombies) {
            // Zombies will only attack if they are in range, the attack has cooled down, and they are
            // facing a player.
            // Player will only attack in the reverse situation but player.attack must also be true. This is
            //controlled by the ZeprInputProcessor. So the player will only attack when the user clicks.
            zombie.closestAttackable = getClosestAttackable(false, zombie);
        	
            if(zombie.closestAttackable != null)
            	zombie.attack(zombie.closestAttackable, delta);
        }

        if (zombiesRemaining == 0) {

            // Spawn a power up and the end of a wave, if there isn't already a powerUp on the level
            //#changed:   Added code for the new power ups here
            if (currentPowerUp == null) {

                int random = (int)(Math.random() * 6 + 1);
                switch(random) {
                    case 1:
                        currentPowerUp = new PowerUpHeal(this, player);
                        break;
                    case 2:
                        currentPowerUp = new PowerUpSpeed(this, player);
                        break;
                    case 3:
                        currentPowerUp = new PowerUpImmunity(this, player);
                        break;
                    case 4:
                        currentPowerUp = new PowerUpInstaKill(this, player);
                        break;
                    case 5:
                        currentPowerUp = new PowerUpInvisibility(this, player);
                        break;
                    case 6:
                        currentPowerUp = new PowerUpCure(this, player);
                        break;
                }
            }


            if (currentWaveNumber > config.waves.length) {
                // Level completed, back to select screen and complete stage.
                isPaused = true;
                
                for (Zombie nonZombie : nonZombies) {
                	parent.score += 1;
                }

                if (config.location == Zepr.Location.CONSTANTINE)
                    parent.setScreen(new TextScreen(parent, "Game completed."));
                else {
                    parent.setScreen(new TextScreen(parent, "Level completed."));
                    if(Zepr.progress == config.location) {
                        Zepr.progress = Zepr.Location.values()[Zepr.progress.ordinal() + 1];
                        saveGame();
                    }
                }
            } else {
                if (currentWaveNumber < config.waves.length) {
                    // Update zombiesRemaining with the number of zombies of the new wave
                    zombiesRemaining = config.waves[currentWaveNumber].numberToSpawn;
                    nonZombiesToSpawn = config.nonZombieWaves[currentWaveNumber].numberToSpawn;
                } else
                    zombiesRemaining = 0;
                	

                // Wave complete, increment wave number
                currentWaveNumber++;
            }

            zombiesToSpawn = zombiesRemaining;

            // Changed by Shaun of the Devs to spawn nonZombies separately from Zombies
            // Spawn all zombies in the stage
            if (config.waves.length >= currentWaveNumber) {
            	spawnZombies(zombiesToSpawn, config.zombieSpawnPoints, config.waves[currentWaveNumber-1].zombieType);
            	spawnZombies(nonZombiesToSpawn, config.nonZombieSpawnPoints, config.nonZombieWaves[currentWaveNumber-1].zombieType);
            }
        }

        //Teleporting and minon spawning behavior for boss2
        teleportCounter++;
        if (currentWaveNumber <= config.waves.length && config.waves[currentWaveNumber-1].zombieType == Zombie.Type.BOSS2 && teleportCounter > 100) {
            teleportCounter = 0;
            if (originalBoss.getHealth() < 250 && Math.random() < 0.1)
                aliveZombies.add(new Zombie(new Vector2(200,200), world, Zombie.Type.BOSS2));
            for (Zombie boss : aliveZombies) {
                Vector2 start = boss.getPhysicsPosition();
                Vector2 end =  player.getPhysicsPosition();
                Vector2 position = new Vector2((start.x + end.x)/2, (start.y + end.y)/2);
                boss.setCharacterPosition(position);
            }
        }


        String progressString = ("Wave " + currentWaveNumber + ", " + zombiesRemaining + " zombies remaining.");
        String healthString = ("Health: " + player.health + "HP");
        String abilityString;
        String powerUpString = PowerUp.activePowerUp;

        if(player.ability)
            abilityString = ("Press E to trigger special ability");
        else if(player.abilityUsed)
            abilityString = player.abilityString;
        else
            abilityString = ("Special ability used");

        progressLabel.setText(progressString);
        powerUpLabel.setText(powerUpString);
        healthLabel.setText(healthString);
        abilityLabel.setText(abilityString);

        if(tutorialTable != null && currentWaveNumber == 1)
            tutorialLabel.setText("TUTORIAL WAVE \n\n Up: W \n Left: A \n Down: S \n Right: D \n Attack: Left Click \n Look: Mouse \n Special Ability: E");
    }

    /**
     * Resize method, called when the game window is resized
     * @param width the new window width
     * @param height the new window height
     */
    @Override
    public void resize(int width, int height) {
    	// Resize the camera depending the size of the window.
        camera.viewportHeight = height;
        camera.viewportWidth = width;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    /**
     * Dispose of the level, clearing the memory
     * #changed:   Added code to dispose of Box2D elements
     */
    @Override
    public void dispose() {
        skin.dispose();
        stage.dispose();
        map.dispose();
        renderer.dispose();
        //debugRenderer.dispose();
        if (currentPowerUp != null)
            currentPowerUp.getTexture().dispose();
        for (Zombie zombie : aliveZombies)
            zombie.dispose();
        player.dispose();
        
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        for(Body body : bodies)
        	world.destroyBody(body);
    }
}
