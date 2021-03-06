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
package eu.matejkormuth.pexel.slave.commands;

import eu.matejkormuth.pexel.commons.Player;
import eu.matejkormuth.pexel.commons.annotations.commands.CommandHandler;
import eu.matejkormuth.pexel.commons.annotations.commands.SubCommand;
import eu.matejkormuth.pexel.commons.chat.ChatChannel;
import eu.matejkormuth.pexel.commons.chat.PlayerChannelSubscriber;
import eu.matejkormuth.pexel.commons.chat.SubscribeMode;
import eu.matejkormuth.pexel.commons.text.ChatColor;

/**
 * Class that handles execution of /channel commands.
 */
@CommandHandler(name = "channel")
public class ChannelCommand {
    
    @SubCommand(description = "Displays information about channels you are in.")
    public void main(final Player sender) {
        sender.sendMessage(ChatColor.GOLD + "Your channels: ");
        //for (ChatChannel channel : ChatChannel.getChannelsByPlayer(sender)) {
        //    sender.sendMessage(channel.getName());
        //}
        sender.sendMessage(ChatColor.AQUA + "/channel help - Displays help information.");
    }
    
    @SubCommand(description = "Lists all avaiable chat channels.")
    public void list(final Player sender) {
        sender.sendMessage(ChatColor.GREEN + "Avaiable channels: ");
        for (ChatChannel channel : ChatChannel.allChannels()) {
            if (!channel.isHidden()) {
                sender.sendMessage(channel.getName());
            }
        }
    }
    
    @SubCommand(description = "Joins specified chat channel.")
    public void join(final Player sender, final String channelName) {
        if (ChatChannel.getByName(channelName) != null)
            if (!ChatChannel.getByName(channelName).isSubscribed(sender))
                
                ChatChannel.getByName(channelName).subscribe(
                        new PlayerChannelSubscriber(sender, SubscribeMode.READ));
            else
                sender.sendMessage(ChatColor.RED + "You are already in that channel!");
        else
            sender.sendMessage(ChatColor.RED + "That channel does not exists!");
    }
    
    @SubCommand(description = "Lefts specified chat channel.")
    public void leave(final Player sender, final String channelName) {
        if (ChatChannel.getByName(channelName).isSubscribed(sender))
            ChatChannel.getByName(channelName).unsubscribe(sender);
        else
            sender.sendMessage(ChatColor.RED + "You are not in that channel!");
    }
}
