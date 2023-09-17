package demo;

import com.jme3.app.*;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;
import com.simsilica.sim.common.DecaySystem;
import com.simsilica.state.GameSystemsState;
import demo.appstate.DebugAoiRadiusState;
import demo.component.*;
import demo.appstate.CheckerBoardState;
import demo.appstate.ModelViewState;
import demo.system.*;
import demo.system.aoi.Aoi;
import demo.system.aoi.AoiSystem;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/15
 */
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
                new FlyCamAppState()
        );
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLocator("assets", FileLocator.class);

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

        GameSystemsState systems = stateManager.getState(GameSystemsState.class);
        EntityData ed = systems.register(EntityData.class, new DefaultEntityData());

        systems.addSystem(new AoiSystem());
        systems.addSystem(new ChaseSystem());
        systems.addSystem(new ShotSystem());
        systems.addSystem(new AttachSystem());
        systems.addSystem(new VelocitySystem());
        systems.addSystem(new MovementSystem());
        systems.addSystem(new CrossBorderSystem());
        systems.addSystem(new DecaySystem());

        // 创建mob
        for (int i = 0; i < Constants.ORANGE_TEAM_COUNT; i++) {
            EntityId e = ed.createEntity();
            // 随机面向
            float facing = FastMath.nextRandomFloat() * FastMath.TWO_PI;
            ed.setComponents(e,
                    new Position(randomLocation()), new Rotation(facing), new Scale(),
                    new Model(Constants.ORANGE_TEAM),
                    new Aoi(Constants.ORANGE_TEAM, Constants.ORANGE_AOI_RADIUS),
                    new CoolDown(Constants.COOL_DOWN));
        }

        // 创建player
        for (int i = 0; i < Constants.BLUE_TEAM_COUNT; i++) {
            EntityId e = ed.createEntity();
            // 随机面向
            float facing = FastMath.nextRandomFloat() * FastMath.TWO_PI;
            ed.setComponents(e,
                    new Position(randomLocation()), new Rotation(facing), new Scale(),
                    new Speed(Constants.BLUE_TEAM_SPEED),
                    new Model(Constants.BLUE_TEAM),
                    new Aoi(Constants.BLUE_TEAM, Constants.BLUE_AOI_RADIUS));
        }
    }

    private Vector3f randomLocation() {
        float x = ( FastMath.nextRandomFloat() - 0.5f) * (Constants.MAP_SIZE - 1f);
        float z = ( FastMath.nextRandomFloat() - 0.5f) * (Constants.MAP_SIZE - 1f);
        return new Vector3f(x, 0, z);
    }
}