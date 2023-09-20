package demo.component;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.simsilica.es.EntityComponent;
import lombok.Getter;
import lombok.ToString;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/20
 */
@Getter
@ToString
public class Transform implements EntityComponent {
    private final Vector3f position;
    private final Quaternion rotation;

    public Transform(Vector3f position, Quaternion rotation) {
        this.position = position;
        this.rotation = rotation;
    }
}