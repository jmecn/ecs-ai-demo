package demo;

import com.jme3.app.*;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.input.KeyInput;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.simsilica.bullet.*;
import com.simsilica.bullet.debug.ContactDebugState;
import com.simsilica.bullet.debug.DebugPhysicsListener;
import com.simsilica.bullet.debug.PhysicsDebugState;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;
import com.simsilica.es.common.Decay;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.sim.common.DecaySystem;
import com.simsilica.state.GameSystemsState;
import demo.appstate.DebugAoiRadiusState;
import demo.appstate.DebugFollowState;
import demo.component.*;
import demo.appstate.CheckerBoardState;
import demo.appstate.ModelViewState;
import demo.system.*;
import demo.system.ai.CollisionSystem;
import demo.system.aoi.Aoi;
import demo.system.aoi.AoiSystem;
import demo.system.bullet.PositionAdapterImpl;
import demo.system.bullet.PositionPublisher;
import lombok.extern.slf4j.Slf4j;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/15
 */
@Slf4j
public class Main extends SimpleApplication {
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setVSync(true);
        settings.setSamples(4);

        Main app = new Main();
        app.setSettings(settings);
        app.start();
    }

    public Main() {
        super(new StatsAppState(), new DebugKeysAppState(),
                new CheckerBoardState(Constants.MAP_SIZE, Constants.CELL_SIZE),
                new DetailedProfilerState(),
                new GameSystemsState(),
                new ModelViewState(),
                new DebugAoiRadiusState(),
                new DebugFollowState(),
                new FlyCamAppState()
        );
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLocator("assets", FileLocator.class);

        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        GuiGlobals.getInstance().setCursorEventsEnabled(false);

        if( flyCam != null ) {
            flyCam.setMoveSpeed(4.5f);
            flyCam.setDragToRotate(true);
        }

        cam.setLocation(new Vector3f(1.0556282f, 10.3794775f, 22.42032f));
        cam.setRotation(new Quaternion(-0.018781494f, 0.9545543f, -0.29099423f, -0.06160951f));

        initLight();
        setupGameSystems();
    }

    private void initLight() {
        AmbientLight ambientLight = new AmbientLight(ColorRGBA.White.mult(0.5f));
        DirectionalLight dl = new DirectionalLight(
                new Vector3f(-3, -5, -4).normalizeLocal(),
                ColorRGBA.White.mult(0.5f)
        );
        rootNode.addLight(ambientLight);
        rootNode.addLight(dl);
    }

    protected void setupGameSystems() {

        DefaultEntityData ed = new DefaultEntityData();

        GameSystemsState systems = stateManager.getState(GameSystemsState.class);
        systems.setUpdateRate(1000000000L / 30);
        systems.register(EntityData.class, ed);
        AoiSystem aoiSystem = systems.register(AoiSystem.class, new AoiSystem());

        // initBulletSystem(systems, ed);

        systems.addSystem(aoiSystem);
        systems.addSystem(new VelocitySystem());
        systems.addSystem(new MovementSystem());
        systems.addSystem(new CrossBorderSystem());
        systems.addSystem(new CollisionSystem());
        systems.addSystem(new ChaseSystem());
        systems.addSystem(new ShotSystem());
        systems.addSystem(new DecaySystem());

        generatePlayGround(ed);
        generateOrangeTeam(ed);
        generateBlueTeam(ed);
    }

    private void initBulletSystem(GameSystemsState systems, EntityData ed) {
        CollisionShapes shapes = systems.register(CollisionShapes.class,
                new DefaultCollisionShapes(ed));

        BulletSystem bullet = new BulletSystem();
        bullet.addPhysicsObjectListener(new PositionPublisher(ed));
        bullet.addPhysicsObjectListener(new DebugPhysicsListener(ed));
        bullet.addEntityCollisionListener(new DefaultContactPublisher(ed) {
            /**
             *  Overridden to give some extra contact decay time so the
             *  debug visualization always has a chance to see them.
             */
            @Override
            protected EntityId createEntity( Contact c ) {
                EntityId result = ed.createEntity();
                ed.setComponents(result, c,
                        Decay.duration(systems.getStepTime().getTime(),
                                systems.getStepTime().toSimTime(0.1))
                );
                return result;
            }
        });
        systems.register(BulletSystem.class, bullet);

        // Add a state we can use to visualize debugging information
        stateManager.attach(new PhysicsDebugState(ed, shapes, new PositionAdapterImpl()));
        stateManager.attach(new ContactDebugState(ed));

        // Register some collision shapes we'll use
        shapes.register(ShapeInfo.create("floor", ed),
                new BoxCollisionShape(new Vector3f(Constants.MAP_SIZE_HALF, 0.25f, Constants.MAP_SIZE_HALF)));
        shapes.register(ShapeInfo.create("box", ed),
                new BoxCollisionShape(new Vector3f(1, 1, 1)));
        shapes.register(ShapeInfo.create("sphere", ed),
                new SphereCollisionShape(1));
        shapes.register(ShapeInfo.create("wall", ed),
                new BoxCollisionShape(new Vector3f(10, 1, 0.25f)));
        shapes.register(ShapeInfo.create("bigSphere", ed),
                new SphereCollisionShape(3));
        shapes.register(ShapeInfo.create("avatar", ed),
                new CapsuleCollisionShape(0.3f, 1.4f));
        shapes.register(ShapeInfo.create("avatarPerception", ed),
                new CapsuleCollisionShape(0.7f, 0.6f));

        FunctionId bulletDebug = new FunctionId("Toggle Physics Debug");
        GuiGlobals.getInstance().getInputMapper().map(bulletDebug, KeyInput.KEY_F7);
        GuiGlobals.getInstance().getInputMapper().addDelegate(bulletDebug, stateManager.getState(PhysicsDebugState.class),
                "toggleEnabled");

        FunctionId contactDebug = new FunctionId("Toggle Contact Debug");
        GuiGlobals.getInstance().getInputMapper().map(contactDebug, KeyInput.KEY_F7, KeyInput.KEY_LSHIFT);
        GuiGlobals.getInstance().getInputMapper().map(contactDebug, KeyInput.KEY_F7, KeyInput.KEY_RSHIFT);
        GuiGlobals.getInstance().getInputMapper().addDelegate(contactDebug,
                stateManager.getState(ContactDebugState.class),
                "toggleEnabled");
    }

    private Vector3f randomLocation() {
        float x = ( FastMath.nextRandomFloat() - 0.5f) * (Constants.MAP_SIZE - 1f);
        float z = ( FastMath.nextRandomFloat() - 0.5f) * (Constants.MAP_SIZE - 1f);
        return new Vector3f(x, 0, z);
    }

    private void generatePlayGround(EntityData ed) {
        // Create a floor
        EntityId floor = ed.createEntity();
        ed.setComponents(floor,
                ShapeInfo.create("floor", ed),
                new SpawnPosition(0, -0.25, 0),
                new Mass(0));

        // Create a wall or two
        EntityId wall = ed.createEntity();
        ed.setComponents(wall,
                ShapeInfo.create("wall", ed),
                new SpawnPosition(10, 1, -20),
                new Mass(0));

        wall = ed.createEntity();
        ed.setComponents(wall,
                ShapeInfo.create("wall", ed),
                new SpawnPosition(-10, 1, -20),
                new Mass(0));

        wall = ed.createEntity();
        ed.setComponents(wall,
                ShapeInfo.create("wall", ed),
                new SpawnPosition(new Vector3f(20, 1, -10), FastMath.HALF_PI),
                new Mass(0));

        wall = ed.createEntity();
        ed.setComponents(wall,
                ShapeInfo.create("wall", ed),
                new SpawnPosition(new Vector3f(20, 1, 10), FastMath.HALF_PI),
                new Mass(0));

        wall = ed.createEntity();
        ed.setComponents(wall,
                ShapeInfo.create("wall", ed),
                new SpawnPosition(10, 1, 20),
                new Mass(0));

        wall = ed.createEntity();
        ed.setComponents(wall,
                ShapeInfo.create("wall", ed),
                new SpawnPosition(-10, 1, 20),
                new Mass(0));

        wall = ed.createEntity();
        ed.setComponents(wall,
                ShapeInfo.create("wall", ed),
                new SpawnPosition(new Vector3f(-20, 1, -10), FastMath.HALF_PI),
                new Mass(0));

        wall = ed.createEntity();
        ed.setComponents(wall,
                ShapeInfo.create("wall", ed),
                new SpawnPosition(new Vector3f(-20, 1, 10), FastMath.HALF_PI),
                new Mass(0));
    }

    private void generateBlueTeam(EntityData ed) {
        // 创建player
        for (int i = 0; i < Constants.BLUE_TEAM_COUNT; i++) {
            EntityId e = ed.createEntity();
            // 随机位置
            Vector3f location = randomLocation();
            // 随机面向
            float facing = FastMath.nextRandomFloat() * FastMath.TWO_PI;
            Quaternion rotation = new Quaternion().fromAngles(0, facing, 0);

            ed.setComponents(e,
                    new Transform(location, rotation),
                    new Speed(Constants.BLUE_TEAM_SPEED),
                    new Model(Constants.BLUE_TEAM),
                    new Aoi(Constants.BLUE_TEAM, Constants.BLUE_AOI_RADIUS),
                    new SpawnPosition(location, rotation),
                    new Mass(30), ShapeInfo.create("avatar", ed)
            );
        }
    }

    private void generateOrangeTeam(EntityData ed) {
        for (int i = 0; i < Constants.ORANGE_TEAM_COUNT; i++) {
            EntityId e = ed.createEntity();
            // 随机位置
            Vector3f location = randomLocation();
            // 随机面向
            float facing = FastMath.nextRandomFloat() * FastMath.TWO_PI;
            Quaternion rotation = new Quaternion().fromAngles(0, facing, 0);

            ed.setComponents(e,
                    new Transform(location, rotation),
                    new Model(Constants.ORANGE_TEAM),
                    new Aoi(Constants.ORANGE_TEAM, Constants.ORANGE_AOI_RADIUS),
                    new CoolDown(Constants.COOL_DOWN),
                    new SpawnPosition(location, rotation),
                    new Mass(30), ShapeInfo.create("avatar", ed)
            );
        }
    }

}