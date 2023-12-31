/*
 * $Id$
 *
 * Copyright (c) 2018, Simsilica, LLC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.bullet;

import com.jme3.bullet.collision.PhysicsCollisionEvent;

/**
 *  A default implementation of the CollisionFilter that will filter
 *  out ghost-&gt;ghost contacts and contacts between ghosts and their
 *  parents.
 *
 *  @author    Paul Speed
 */
public class DefaultCollisionFilter implements CollisionFilter {

    @Override
    public boolean filterCollision( EntityPhysicsObject object1,
                                    EntityPhysicsObject object2,
                                    PhysicsCollisionEvent event ) {

        //if( object1 instanceof EntityGhostObject && object2 instanceof EntityGhostObject ) {
        //    return true;
        //}
        // It depends on the masking now

        if( object1 instanceof EntityGhostObject ) {
            EntityGhostObject ghost = (EntityGhostObject)object1;
            if( ghost.getParent() == object2 ) {
                return true;
            }
            if( !ghost.canCollideWith(object2) ) {
                return true;
            }
        }

        if( object2 instanceof EntityGhostObject ) {
            EntityGhostObject ghost = (EntityGhostObject)object2;
            if( ghost.getParent() == object1 ) {
                return true;
            }
            if( !ghost.canCollideWith(object1) ) {
                return true;
            }
        }
        return false;
    }

}
