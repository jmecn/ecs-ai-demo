package demo;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Curve;
import com.jme3.scene.shape.Line;

import java.util.HashMap;
import java.util.Map;

public class AssetFactory {

    private final AssetManager assets;

    private final float size = 0.2f;

    private final Map<String, Material> materials = new HashMap<>();
    private final Map<String, Mesh> meshes = new HashMap<>();

    public AssetFactory(AssetManager assets ) {
        this.assets = assets;
    }

    public Material createMaterial(String name, ColorRGBA color, boolean light) {
        if (materials.containsKey(name)) {
            return materials.get(name);
        }

        Material mat;
        if (light) {
            mat = new Material(assets, Materials.LIGHTING);
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", color);
            mat.setColor("Ambient", color);
            mat.setColor("Specular", ColorRGBA.White);
        } else {
            mat = new Material(assets, Materials.UNSHADED);
            mat.setColor("Color", color);
        }

        switch (name) {
            case Constants.BLUE_TEAM: {
                mat.getAdditionalRenderState().setPolyOffset(1f, 1f);
                break;
            }
            case Constants.DEBUG_SEGMENT:
            case Constants.DEBUG_RADIUS: {
                mat.getAdditionalRenderState().setDepthTest(false);
                break;
            }
        }

        materials.put(name, mat);
        return mat;
    }

    private Mesh createMesh(String name) {
        if (meshes.containsKey(name)) {
            return meshes.get(name);
        }
        Mesh mesh;
        if (Constants.BLUE_TEAM.equals(name)) {
            AssetInfo assetInfo = assets.locateAsset(new AssetKey<Mesh>("Models/arrow.j3o"));
            mesh = (Mesh) BinaryImporter.getInstance().load(assetInfo);
        } else if (Constants.ORANGE_TEAM.equals(name)) {
            AssetInfo assetInfo = assets.locateAsset(new AssetKey<Mesh>("Models/arrow.j3o"));
            mesh = (Mesh) BinaryImporter.getInstance().load(assetInfo);
        } else if (Constants.BULLET.equals(name)) {
            mesh = new Box(0.01f, 0.01f, 0.1f);
        } else if (Constants.DEBUG_SEGMENT.equals(name)) {
            mesh = new Line(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1));
        } else if (Constants.DEBUG_RADIUS.equals(name)) {
            int samples = 36;
            Vector3f[] points = new Vector3f[samples + 1];
            float rate = FastMath.TWO_PI / samples;
            float angle = 0;
            for (int i = 0; i < samples; i++) {
                float x = FastMath.cos(angle);
                float y = FastMath.sin(angle);
                points[i] = new Vector3f(x, 0, y);
                angle += rate;
            }
            points[samples] = points[0];
            mesh = new Curve(points, 1);
        } else {
            throw new IllegalArgumentException("Unknown mesh name:" + name);
        }
        meshes.put(name, mesh);
        return mesh;
    }

    public Spatial loadModel( String name ) {
        Node model = new Node(name);
        if(Constants.BLUE_TEAM.equals(name) ) {
            Geometry geom = new Geometry(name, createMesh(name));
            geom.setMaterial(createMaterial(name, ColorRGBA.Blue, true));
            model.attachChild(geom);
        } else if(Constants.ORANGE_TEAM.equals(name) ) {
            Geometry geom = new Geometry(name, createMesh(name));
            geom.setMaterial(createMaterial(name, ColorRGBA.Orange, true));
            model.attachChild(geom);
        } else if(Constants.BULLET.equals(name) ) {
            Geometry geom = new Geometry(name, createMesh(name));
            geom.setMaterial(createMaterial(name, ColorRGBA.Yellow, true));
            model.attachChild(geom);
        } else if(Constants.DEBUG_SEGMENT.equals(name) ) {
            Geometry geom = new Geometry(name, createMesh(name));
            geom.setMaterial(createMaterial(name, ColorRGBA.Green, false));
            model.attachChild(geom);
        } else if(Constants.DEBUG_RADIUS.equals(name) ) {
            Geometry geom = new Geometry(name, createMesh(name));
            geom.setMaterial(createMaterial(name, ColorRGBA.Red, false));
            model.attachChild(geom);
        }
        
        return model;
    }
    
}
