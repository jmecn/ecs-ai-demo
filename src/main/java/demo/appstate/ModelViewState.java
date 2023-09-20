package demo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.es.*;
import com.simsilica.state.GameSystemsState;
import demo.Main;
import demo.AssetFactory;
import demo.component.Model;
import demo.component.Transform;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModelViewState extends BaseAppState {

    private EntityData ed;
    private AssetFactory factory;
 
    private Node modelRoot;   
    private ModelContainer models;

    public ModelViewState() {        
    }
 
    public Spatial getModel( EntityId id ) {
        ModelView view = models.getObject(id);        
        return view == null ? null : view.spatial; 
    }
    
    @Override
    protected void initialize( Application app ) {
        this.ed = getState(GameSystemsState.class).get(EntityData.class);
        
        factory =  new AssetFactory(app.getAssetManager());

        this.modelRoot = ((Main)app).getRootNode();
        this.models = new ModelContainer(ed);
    }
    
    @Override
    protected void cleanup( Application app ) {
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
 
    protected Spatial getModel( Model info ) {
        return factory.loadModel(info.getName());
    }
 
    private class ModelView {
        private Entity entity;
        private Spatial spatial;
 
        public ModelView( Entity entity ) {
            this.entity = entity;
            this.spatial = getModel(entity.get(Model.class));
            modelRoot.attachChild(spatial);
            update();
        }
        
        public void update() {
            Transform transform = entity.get(Transform.class);
            spatial.setLocalTranslation(transform.getPosition());
            spatial.setLocalRotation(transform.getRotation());
        }

        public void release() {
            spatial.removeFromParent();
        }
    }
    
    private class ModelContainer extends EntityContainer<ModelView> {
        public ModelContainer( EntityData ed ) {
            super(ed, Transform.class, Model.class);
        }
 
        @Override       
        public ModelView[] getArray() {
            return super.getArray();
        }
 
        @Override
        protected ModelView addObject( Entity e ) {
            return new ModelView(e);
        }

        @Override
        protected void updateObject( ModelView object, Entity e ) {
            object.update();
        }
        
        @Override
        protected void removeObject( ModelView object, Entity e ) {
            object.release();
        }
    }
}
