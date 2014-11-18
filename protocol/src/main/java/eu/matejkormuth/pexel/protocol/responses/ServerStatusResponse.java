// @formatter:off
/*
 * Pexel Project - Minecraft minigame server platform. 
 * Copyright (C) 2014 Matej Kormuth <http://www.matejkormuth.eu>
 * 
 * This file is part of Pexel.
 * 
 * Pexel is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Pexel is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
// @formatter:on
package eu.matejkormuth.pexel.protocol.responses;

import java.nio.ByteBuffer;

import eu.matejkormuth.pexel.network.Response;

public class ServerStatusResponse extends Response {
    public long maxMem;
    public long usedMem;
    public int  slots;
    public int  playerCount;
    
    public ServerStatusResponse(final long maxMem, final long usedMem) {
        this.maxMem = maxMem;
        this.usedMem = usedMem;
    }
    
    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.allocate(2 * 8).putLong(this.maxMem).putLong(this.usedMem);
    }
    
    @Override
    public void fromByteBuffer(final ByteBuffer buffer) {
        this.maxMem = buffer.getLong();
        this.usedMem = buffer.getLong();
    }
}