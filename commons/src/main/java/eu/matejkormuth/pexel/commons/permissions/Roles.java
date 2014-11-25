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
package eu.matejkormuth.pexel.commons.permissions;

/**
 * All basic roles in Pexel.
 */
public abstract class Roles {
    /**
     * Default player role that have permission to chat.
     */
    public static final Role DEFAULT = new Role("pexel.default", Permissions.CHAT);
    /**
     * Default admin role that have permissions to kick, ban, broadcast and basically moderate server.
     */
    public static final Role ADMIN   = new Role("pexel.admin", Roles.DEFAULT,
                                             Permissions.KICK, Permissions.BAN_IP,
                                             Permissions.BAN_PARDON,
                                             Permissions.BAN_PERMANENT,
                                             Permissions.BAN_TEMPORARY,
                                             Permissions.BROADCAST);
    /**
     * Default owner role that have all permissions.
     */
    public static final Role OWNER   = new Role("pexel.owner", Roles.ADMIN,
                                             Permissions.SPECIAL_ALL);
}
