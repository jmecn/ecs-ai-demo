package demo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityContainer;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.state.GameSystemsState;
import demo.AssetFactory;
import demo.Constants;
import demo.Main;
import demo.component.Follow;
import demo.component.Transform;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/17
 */
public class DebugFollowState extends BaseAppState {

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

        root = new Node("DebugFollow");
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

    private class ModelView {
        private final Entity entity;
        private final Spatial spatial;

        public ModelView(Entity entity) {
            this.entity = entity;
            this.spatial = factory.loadModel(Constants.DEBUG_SEGMENT);
            root.attachChild(spatial);
            update();
        }

        public void update() {
            Follow follow = entity.get(Follow.class);
            EntityId target = follow.getTarget();

            Transform transform = entity.get(Transform.class);
            Transform targetTransform = ed.getComponent(target, Transform.class);
            if (targetTransform == null) {
                return;
            }

            Vector3f loc1 = transform.getPosition();
            Vector3f loc2 = targetTransform.getPosition();

            // 计算Facing
            Vector3f dir = loc2.subtract(loc1);
            dir.normalizeLocal();

            float dist = FastMath.sqrt(loc2.distanceSquared(loc1));

            spatial.setLocalTranslation(loc1);
            spatial.setLocalRotation(new Quaternion().lookAt(dir, Vector3f.UNIT_Y));
            spatial.setLocalScale(dist);
        }

        public void release() {
            spatial.removeFromParent();
        }
    }

    private class ModelContainer extends EntityContainer<ModelView> {
        public ModelContainer( EntityData ed ) {
            super(ed, Transform.class, Follow.class);
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
