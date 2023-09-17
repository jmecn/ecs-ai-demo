package demo.component;

import com.jme3.math.Vector3f;
import com.simsilica.es.EntityComponent;
import lombok.Getter;
import lombok.ToString;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/16
 */
@Getter
@ToString
public class Scale implements EntityComponent {
    private final Vector3f scale;

    public Scale() {
        this.scale = new Vector3f(1, 1, 1);
    }

    public Scale(float x, float y, float z) {
        this.scale = new Vector3f(x, y, z);
    }

    public Scale(Vector3f scale) {
        this.scale = scale;
    }
}
