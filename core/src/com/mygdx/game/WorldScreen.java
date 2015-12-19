package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.MipMapGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class WorldScreen implements Screen {

	private final ModelLoader.ModelParameters modelParameters;
	private final TextureLoader.TextureParameter textureParameter;

	private final PhysicsEngine engine;
	private final WorldRenderer worldRenderer;
	private final PerspectiveCamera camera;
	private final Viewport viewport;
	private final GameStage stage;
	private final ShapeRenderer shapeRenderer;
	private final AssetManager assets;

	private GameModel skybox;
	private Model sphereModel;

	public WorldScreen(int reqWidth, int reqHeight) {
		Bullet.init();
		MipMapGenerator.setUseHardwareMipMap(true);
		modelParameters = new ModelLoader.ModelParameters();
		modelParameters.textureParameter.genMipMaps = true;
		modelParameters.textureParameter.minFilter = Texture.TextureFilter.MipMap;
		modelParameters.textureParameter.magFilter = Texture.TextureFilter.Linear;

		textureParameter = new TextureLoader.TextureParameter();
		textureParameter.genMipMaps = true;
		textureParameter.minFilter = Texture.TextureFilter.MipMap;
		textureParameter.magFilter = Texture.TextureFilter.Linear;


		assets = new AssetManager();
		shapeRenderer = new ShapeRenderer();

		camera = new PerspectiveCamera(67, reqWidth, reqHeight);
		viewport = new FitViewport(reqWidth, reqHeight, camera);


		engine = new PhysicsEngine();
		engine.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_DrawWireframe);

		worldRenderer = new WorldRenderer(viewport, camera, engine);
		stage = new GameStage(viewport);

		camera.near = 1e-1f;
		camera.far = 1e3f;
		camera.position.set(-5, 3, -5);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);

		// Load assets
		assets.load("planet_Quom_2449.png", Texture.class, textureParameter);
		assets.load("skybox.g3db", Model.class, modelParameters);

		// Planets
		assets.finishLoadingAsset("planet_Quom_2449.png");
		Texture texture = assets.get("planet_Quom_2449.png");
		Material material = new Material(
				TextureAttribute.createDiffuse(texture),
				ColorAttribute.createSpecular(1, 1, 1, 1),
				FloatAttribute.createShininess(8f));
		long attributes = VertexAttributes.Usage.Position
				| VertexAttributes.Usage.Normal
				| VertexAttributes.Usage.TextureCoordinates;
		ModelBuilder modelBuilder = new ModelBuilder();
		Vector3 modelScale = new Vector3(1, 1, 1);
		int subDivs = 24;
		sphereModel = modelBuilder.createSphere(modelScale.x, modelScale.y, modelScale.z,
				subDivs, subDivs, material, attributes);

		for (int i = 1; i < 4; i++) {
			Vector3 scale = new Vector3(1, 1, 1).scl(i);
			Vector3 pos = new Vector3((i - 1) * 10, 0, 0);
			btSphereShape sphereShape = new btSphereShape(scale.x * 0.5f);
			GameModelBody planet0 = new GameModelBody(sphereModel, "planet0",
					pos, new Vector3(), scale,
					sphereShape, 1,
					PhysicsEngine.GROUND_FLAG, PhysicsEngine.ALL_FLAG,
					false, false);
			engine.addEntity(planet0);
		}

		// Skybox
		assets.finishLoadingAsset("skybox.g3db");
		Model skyboxModel = assets.get("skybox.g3db");
		Vector3 skyboxScale = new Vector3(100, 100, 100);
		skybox = new GameModel(skyboxModel, "skybox",
				new Vector3(), new Vector3(), skyboxScale);
		engine.addEntity(skybox);
	}

	@Override
	public void show() {

	}

	@Override
	public void render(float delta) {
		stage.act(delta);
		engine.update(delta);

		// Move skybox with camera so we never reach the edge of it
		skybox.modelTransform.setTranslation(camera.position);

		Gdx.gl.glClearColor(0, 0, 0, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.DARK_GRAY);
		shapeRenderer.rect(0, 0, viewport.getScreenWidth(), viewport.getScreenHeight());
		shapeRenderer.end();

		worldRenderer.update();
//		engine.debugDrawWorld(camera); // Bullet debug draw
		stage.draw();

	}

	@Override
	public void resize(int width, int height) {
		stage.resize(width, height);
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

	@Override
	public void dispose() {
		sphereModel.dispose();

		shapeRenderer.dispose();
		worldRenderer.dispose();
		engine.dispose();
		stage.dispose();
		assets.dispose();
	}
}
