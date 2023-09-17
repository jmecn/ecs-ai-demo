package demo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireSphere;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;
import com.simsilica.state.GameSystemsState;
import demo.AssetFactory;
import demo.Constants;
import demo.component.Model;
import demo.component.Position;
import demo.component.Rotation;
import demo.component.Scale;
import demo.system.aoi.Aoi;
import demo.Main;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/17
 */
public class DebugAoiRadiusState extends BaseAppState {

    private EntityData ed;

    private AssetFactory factory;

    private Node rootNode;

    private Node root;

    private ModelContainer models;

    @Override
    protected void initialize(Application app) {
        GameSystemsState systems = app.getStateManager().getState(GameSystemsState.class);
        ed = systems.get(EntityData.class);
        ed.getEntities(Aoi.class, Position.class);
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
    }

    @Override
    public void update( float tpf ) {
        models.update();
    }

    @Override
    protected void onDisable() {
        models.stop();
    }


    protected Spatial getModel(Model info) {
        return factory.loadModel(info.getName());
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
            Position pos = entity.get(Position.class);
            spatial.setLocalTranslation(pos.getLocation());
        }

        public void release() {
            spatial.removeFromParent();
        }
    }

    private class ModelContainer extends EntityContainer<ModelView> {
        public ModelContainer( EntityData ed ) {
            super(ed, Position.class, Aoi.class);
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
