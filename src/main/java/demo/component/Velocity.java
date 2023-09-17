package demo.component;

import com.jme3.math.Vector3f;
import com.simsilica.es.EntityComponent;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Velocity implements EntityComponent {
    private final Vector3f velocity;

    public Velocity() {
        velocity = new Vector3f(0f, 0f, 0f);
    }

    public Velocity(Vector3f velocity) {
        this.velocity = new Vector3f(velocity);
    }

    public Velocity(float x, float y, float z) {
        velocity = new Vector3f(x, y, z);
    }

}
