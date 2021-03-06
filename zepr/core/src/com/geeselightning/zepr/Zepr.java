package com.geeselightning.zepr;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.geeselightning.zepr.screens.LoadingScreen;
import com.geeselightning.zepr.screens.MenuScreen;
import com.geeselightning.zepr.screens.SelectLevelScreen;
import com.geeselightning.zepr.screens.StoryScreen;

public class Zepr extends Game {

	private MenuScreen menuScreen;
	public static AssetManager manager;

	//#changed:   Added this Location enum
	public enum Location { MENU, STORY, SELECT, TOWN, HALIFAX, CENTRALHALL, COURTYARD,
		GLASSHOUSE, CONSTANTINE, COMPLETE, MINIGAME }	

	// The progress is the integer representing the last level completed. i.e. 3 for Town
	public static Location progress;
	
	public int[] levelScores = new int[6];
	public int[] maxScores = new int[6];
	public int score = 0;
	public int maxScore = 0;
	/**
	 * Method to change the currently active screen
	 * @param screen the Location to set as active
	 * #changed:   Added more levels and new LevelConfig system to configure level values
	 */
	public void changeScreen(final Location screen) {
		LevelConfig config;
		Level level;
		switch(screen) {
			case MENU:
				if (menuScreen == null) menuScreen = new MenuScreen(this);
				    setScreen(menuScreen);
				break;
            case STORY:
				StoryScreen storyScreen = new StoryScreen(this);
                setScreen(storyScreen);
                break;
			case SELECT:
				SelectLevelScreen selectLevelScreen = new SelectLevelScreen(this);
				setScreen(selectLevelScreen);
				break;
			case TOWN:
				config = new LevelConfig() {{
					level = 0;
					mapLocation = "maps/townmap.tmx";
					playerSpawn = new Vector2(530, 600);
					powerSpawn = new Vector2(300, 300);
					backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("town.mp3"));
					zombieSpawnPoints = new ArrayList<>(
				            Arrays.asList(new Vector2(200,200), new Vector2(700,700),
				                    new Vector2(200,700), new Vector2(700,200)));
					waves = new Wave[]{ new Wave(5, Zombie.Type.ZOMBIE1),
                                        new Wave(10, Zombie.Type.ZOMBIE1),
                                        new Wave(20, Zombie.Type.ZOMBIE1)};
					location = screen;
					nonZombieSpawnPoints = new ArrayList<>(
				            Arrays.asList(new Vector2(550, 650)));
					nonZombieWaves = new Wave[]{ new Wave(5, Zombie.Type.NONZOMBIE1),
                            new Wave(1, Zombie.Type.NONZOMBIE2),
                            new Wave(2, Zombie.Type.NONZOMBIE3)};
				}};
				level = new Level(this, config);
				setScreen(level);
				break;
			case HALIFAX:
				config = new LevelConfig() {{
					level = 1;
					mapLocation = "maps/halifaxmap.tmx";
					playerSpawn = new Vector2(300, 300);
					powerSpawn = new Vector2(200, 200);
					backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("halifax.mp3")); 
					zombieSpawnPoints = new ArrayList<>(
							Arrays.asList(new Vector2(600,100), new Vector2(100,200),
				                    new Vector2(600,500), new Vector2(100,600)));
                    waves = new Wave[]{ new Wave(10, Zombie.Type.ZOMBIE1),
                                        new Wave(15, Zombie.Type.ZOMBIE1),
                                        new Wave(20, Zombie.Type.ZOMBIE2)};
					location = screen;
					nonZombieSpawnPoints = new ArrayList<>(
				            Arrays.asList(new Vector2(350, 350)));
					nonZombieWaves = new Wave[]{ new Wave(5, Zombie.Type.NONZOMBIE1),
                            new Wave(1, Zombie.Type.NONZOMBIE2),
                            new Wave(2, Zombie.Type.NONZOMBIE3)};
				}};						 
				level = new Level(this, config);
				setScreen(level);
				break;
			case CENTRALHALL:
				config = new LevelConfig() {{
					level = 2;
					mapLocation = "maps/centralhallmap.tmx";
					playerSpawn = new Vector2(50, 900);
					powerSpawn = new Vector2(250, 250);
					backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("central.mp3"));
					zombieSpawnPoints = new ArrayList<>(
							 Arrays.asList(new Vector2(120,100), new Vector2(630,600),
					                   new Vector2(630,100), new Vector2(120,500)));
                    waves = new Wave[]{ new Wave(13, Zombie.Type.ZOMBIE2),
                                        new Wave(17, Zombie.Type.ZOMBIE2),
                                        new Wave(1, Zombie.Type.BOSS1)};
					location = screen;
					nonZombieSpawnPoints = new ArrayList<>(
				            Arrays.asList(new Vector2(50, 850)));
					nonZombieWaves = new Wave[]{ new Wave(5, Zombie.Type.NONZOMBIE1),
                            new Wave(1, Zombie.Type.NONZOMBIE2),
                            new Wave(2, Zombie.Type.NONZOMBIE3)};
				}};						 
				level = new Level(this, config);
				setScreen(level);
				break;
			case COURTYARD:
				config = new LevelConfig() {{
					level = 3;
					mapLocation = "maps/courtyard.tmx";
					playerSpawn = new Vector2(300, 300);
					powerSpawn = new Vector2(150, 150);
					backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("courtyard.mp3"));
					zombieSpawnPoints = new ArrayList<>(
							 Arrays.asList(new Vector2(120,100), new Vector2(630,600),
					                   new Vector2(630,100), new Vector2(120,500)));
                    waves = new Wave[]{ new Wave(12, Zombie.Type.ZOMBIE2),
                                        new Wave(12, Zombie.Type.ZOMBIE2),
                                        new Wave(12, Zombie.Type.ZOMBIE3),
                                        new Wave(16, Zombie.Type.ZOMBIE3)};
					location = screen;
					nonZombieSpawnPoints = new ArrayList<>(
				            Arrays.asList(new Vector2(360, 350)));
					nonZombieWaves = new Wave[]{ new Wave(5, Zombie.Type.NONZOMBIE1),
                            new Wave(1, Zombie.Type.NONZOMBIE2),
                            new Wave(2, Zombie.Type.NONZOMBIE3),
                            new Wave(2, Zombie.Type.NONZOMBIE3)};
				}};
				level = new Level(this, config);
				setScreen(level);
				break;
			case GLASSHOUSE:
				config = new LevelConfig() {{
					level = 4;
					mapLocation = "maps/glasshousemap.tmx";
					playerSpawn = new Vector2(400, 70);
					powerSpawn = new Vector2(250, 250);
					backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("glasshouse.mp3"));
					zombieSpawnPoints = new ArrayList<>(
							 Arrays.asList(new Vector2(120,200), new Vector2(630,600),
					                   new Vector2(630,100), new Vector2(120,500)));
                    waves = new Wave[]{ new Wave(12, Zombie.Type.ZOMBIE3),
                                        new Wave(20, Zombie.Type.ZOMBIE3),
                                        new Wave(30, Zombie.Type.ZOMBIE3)};
					location = screen;
					nonZombieSpawnPoints = new ArrayList<>(
				            Arrays.asList(new Vector2(450, 100)));
					nonZombieWaves = new Wave[]{ new Wave(5, Zombie.Type.NONZOMBIE1),
                            new Wave(1, Zombie.Type.NONZOMBIE2),
                            new Wave(2, Zombie.Type.NONZOMBIE3)};
				}};
				level = new Level(this, config);
				setScreen(level);
				break;
			case CONSTANTINE:
				config = new LevelConfig() {{
					level = 5;
					mapLocation = "maps/constantinemap.tmx";
					playerSpawn = new Vector2(300, 300);
					powerSpawn = new Vector2(250, 250);
					backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("constantine.mp3"));
					zombieSpawnPoints = new ArrayList<>(
							 Arrays.asList(new Vector2(120,100), new Vector2(630,600),
					                   new Vector2(630,100), new Vector2(120,500)));
                    waves = new Wave[]{ new Wave(40, Zombie.Type.ZOMBIE1),
                                        new Wave(30, Zombie.Type.ZOMBIE2),
                                        new Wave(20, Zombie.Type.ZOMBIE3),
                                        new Wave(1, Zombie.Type.BOSS2)};
					location = screen;
					nonZombieSpawnPoints = new ArrayList<>(
				            Arrays.asList(new Vector2(320, 350)));
					nonZombieWaves = new Wave[]{ new Wave(5, Zombie.Type.NONZOMBIE1),
                            new Wave(1, Zombie.Type.NONZOMBIE2),
                            new Wave(2, Zombie.Type.NONZOMBIE3),
                            new Wave(2, Zombie.Type.NONZOMBIE3)};
				}};	
				level = new Level(this, config);
				setScreen(level);
				break;
			case MINIGAME:
				MiniGame minigame = new MiniGame(this);
				setScreen(minigame);
				break;
		}
	}

	/**
	 * Create event run when the class is constructed, loading save data if it exists.
	 * #changed:   Added code to load save data and to create it if not existent yet
	 */
	@Override
	public void create() {
		
		LoadingScreen loadingScreen = new LoadingScreen(this);
		setScreen(loadingScreen);

		//Attempt to load save data file
		File f = new File("saveData.txt");
		if(f.isFile()) {
			Gdx.app.log("Check file exists", f.getName() + "exists");
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(f));
				String st;
				  while ((st = br.readLine()) != null) {
				    Gdx.app.log("Player has saved progress on stage", ""+st);
				    progress = Location.TOWN;
				  }
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Gdx.app.log("Creating file", ""+f.getName());
			//Create a new save data file if one cannot be found
			try {
				boolean b = f.createNewFile();
				Gdx.app.log("File creation check", f.getName() + " has been created? ... " + b);
				FileOutputStream edit = new FileOutputStream(f);
				byte[] lvl = ("3").getBytes();
				edit.write(lvl);
				edit.close();
				progress = Location.TOWN;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * #changed:   Added this method to dispose of the sound manager
	 */
	@Override
	public void dispose() {
		manager.dispose();
	}
}