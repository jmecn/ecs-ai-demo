package demo.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityData;
import com.simsilica.state.GameSystemsState;
import demo.AssetFactory;
import demo.Constants;
import demo.component.Transform;
import demo.system.aoi.Aoi;
import demo.Main;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/17
 */
public class DebugAoiRadiusState extends BaseAppState implements ActionListener {

    public static final String TOGGLE_RADIUS = "toggle_radius";

    private EntityData ed;

    private AssetFactory factory;

    private Node rootNode;

    private Node root;

    private ModelContainer models;

    @Override
    protected void initialize(Application app) {
        GameSystemsState systems = app.getStateManager().getState(GameSystemsState.class);
        ed = systems.get(EntityData.class);
        factory =  new AssetFactory(app.getAssetManager());

        rootNode = ((Main)app).getRootNode();

        root = new Node("DebugAoiRadius");
        rootNode.attachChild(root);

        this.models = new ModelContainer(ed);
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        models.start();

        // 注册按键
        InputManager inputManager = getApplication().getInputManager();
        inputManager.addMapping(TOGGLE_RADIUS, new KeyTrigger(KeyInput.KEY_F3));
        inputManager.addListener(this, TOGGLE_RADIUS);
    }


    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
        if (name.equals(TOGGLE_RADIUS) && keyPressed) {
            toggleRadius();
        }
    }

    /**
     * 碰撞半径开/关
     */
    public void toggleRadius() {
        if (rootNode.hasChild(root)) {
            rootNode.detachChild(root);
        } else {
            rootNode.attachChild(root);
        }
    }

    @Override
    public void update( float tpf ) {
        models.update();
    }

    @Override
    protected void onDisable() {
        models.stop();
    }

    private class ModelView {
        private final Entity entity;
        private final Spatial spatial;

        public ModelView(Entity entity) {
            this.entity = entity;
            float radius = entity.get(Aoi.class).getRadius();
            this.spatial = factory.loadModel(Constants.DEBUG_RADIUS);
            spatial.scale(radius);
            root.attachChild(spatial);
            update();
        }

        public void update() {
            Transform transform = entity.get(Transform.class);
            spatial.setLocalTranslation(transform.getPosition());
        }

        public void release() {
            spatial.removeFromParent();
        }
    }

    private class ModelContainer extends EntityContainer<ModelView> {
        public ModelContainer( EntityData ed ) {
            super(ed, Transform.class, Aoi.class);
        }

        @Override
        public ModelView[] getArray() {
            return super.getArray();
        }

        @Override
        protected ModelView addObject(Entity e ) {
            return new ModelView(e);
        }

        @Override
        protected void updateObject(ModelView object, Entity e ) {
            object.update();
        }

        @Override
        protected void removeObject(ModelView object, Entity e ) {
            object.release();
        }
    }
}
