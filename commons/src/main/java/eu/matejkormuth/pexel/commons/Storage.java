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
package eu.matejkormuth.pexel.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;

import eu.matejkormuth.pexel.commons.storage.MapDescriptor;
import eu.matejkormuth.pexel.commons.storage.MinigameDescriptor;
import eu.matejkormuth.pexel.network.ServerSide;

/**
 * Class that represents storage.
 */
public class Storage extends Component {
    protected ServerSide              side;
    protected File                    rootFolder;
    
    protected ConfigurationSection    config;
    
    protected Set<MinigameDescriptor> minigames    = new HashSet<MinigameDescriptor>();
    protected Set<MapDescriptor>      maps         = new HashSet<MapDescriptor>();
    protected Set<String>             tags         = new HashSet<String>();
    protected Set<URI>                trustedSites = new HashSet<URI>();
    
    private final ReentrantLock       lock         = new ReentrantLock();
    
    /**
     * Creates new Storage wrapper object on spcified folder.
     * 
     * @param storageFolder
     */
    public Storage(final File storageFolder, final ConfigurationSection config) {
        Preconditions.checkNotNull(storageFolder);
        Preconditions.checkArgument(storageFolder.exists(), "storageFolder must exist");
        Preconditions.checkArgument(storageFolder.isDirectory(),
                "storageFolder must be directory");
        
        this.config = config;
        this.rootFolder = storageFolder;
    }
    
    @Override
    public void onEnable() {
        this.expandStructure();
        this.loadIndex();
        this.updateIndex();
    }
    
    // Method that returns File object based from relative path.
    private File getFile(final String relative) {
        return new File(this.rootFolder.getAbsolutePath() + "/" + relative);
    }
    
    private InputStream getRemoteFile(final String url) {
        try {
            return new URL(url).openStream();
        } catch (IOException e) {
            // Catch silently.
            return null;
        }
    }
    
    // Method that expands folder structure.
    private void expandStructure() {
        boolean expanded = false;
        expanded |= this.getFile("maps").mkdir();
        expanded |= this.getFile("minigames").mkdir();
        expanded |= this.getFile(".downloads").mkdir();
        
        if (expanded) {
            this.logger.info("Directory structure expanded!");
        }
    }
    
    private void loadIndex() {
        File index = this.getFile("index.xml");
        if (index.exists()) {
            //TODO: this.readIndex();
        }
        else {
            this.logger.info("Index not found!");
        }
    }
    
    private void updateIndex() {
        this.logger.info("Updating index...");
        
        // Async scan.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Storage.this.scanAsync();
                Storage.this.checkForUpdates();
            }
        }, "Storage-IndexUpdater/FSScanner").start();
    }
    
    // Called from another thread.
    private void scanAsync() {
        this.logger.info("Starting async file structure scan...");
        // Lock object.
        this.lock.lock();
        
        // Maps
        File maps = this.getFile("maps");
        for (String folder : maps.list()) {
            if (new File(maps, folder).isDirectory()) {
                this.scanMapDir(new File(maps, folder));
            }
        }
        
        // Plugins
        File plugins = this.getFile("minigames");
        for (String folder : plugins.list()) {
            if (new File(plugins, folder).isDirectory()) {
                this.scanPluginDir(new File(plugins, folder));
            }
        }
        
        // Unlock object.
        this.lock.unlock();
        
        // Log a message.
        this.logger.info("File structure scan finished!");
        this.logger.info("Found " + this.minigames.size() + " plugins, "
                + this.maps.size() + " maps, " + this.tags.size() + " tags!");
    }
    
    // Called from other thread.
    protected void checkForUpdates() {
        this.logger.info("Checking for updates for plugins...");
        for (MinigameDescriptor desc : this.minigames) {
            // If auto updates enabled.
            if (this.config.get(Configuration.KEY_STORAGE_AUTOUPDATES, true).asBoolean()) {
                this.update(desc);
            }
        }
    }
    
    // Update method that checks if plugin comes from safe location.
    private void update(final MinigameDescriptor desc) {
        // Check if source is from trusted domain.
        try {
            URI uri = new URI(desc.getSourceUrl());
            // Trust check.
            boolean trusted = false;
            for (URI trusteduri : this.trustedSites) {
                if (trusteduri.getHost().equals(uri.getHost())
                        && trusteduri.getPort() == uri.getPort()) {
                    trusted = true;
                }
            }
            
            // If this plugin comes from trusted location, then download update.
            if (trusted
                    && this.config.get(Configuration.KEY_STORAGE_ONLYTRUSTED, true)
                            .asBoolean()) {
                this.uncheckedUpdate(desc);
            }
            else {
                this.logger.warn("Plugin " + desc.getName()
                        + " comes from untrusted source! Not updating.");
            }
            
        } catch (URISyntaxException e) {
            this.logger.error("Plugin " + desc.getName() + " has invalid sourceUrl!");
        }
    }
    
    // Method that downloads 
    private void uncheckedUpdate(final MinigameDescriptor localDescriptor) {
        // Get remote descriptor.
        InputStream remoteDescriptorFile = this.getRemoteFile(localDescriptor.getSourceUrl()
                + "/description.xml");
        // Stupid null check...
        if (remoteDescriptorFile == null) {
            this.logger.error("Can't load remote desccriptor of plugin "
                    + localDescriptor.getName() + "!");
            return;
        }
        
        MinigameDescriptor remoteDescriptor = MinigameDescriptor.load(remoteDescriptorFile);
        
        // If bigger version on remote.
        if (remoteDescriptor.getRevision().getNumeric() > localDescriptor.getRevision()
                .getNumeric()) {
            // Create update directory.
            this.getFile(
                    ".downloads/" + localDescriptor.getName() + "/"
                            + remoteDescriptor.getRevision()).mkdirs();
            // Download jar.
            InputStream remoteJar = this.getRemoteFile(localDescriptor.getSourceUrl()
                    + "/" + remoteDescriptor.getName() + ".jar");
            try {
                // Save jar to .downloads folder. 
                Files.copy(remoteJar,
                        Paths.get(this.getFile(
                                "minigames/" + remoteDescriptor.getName() + "/"
                                        + remoteDescriptor.getName() + ".jar")
                                .getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                this.logger.error("Error occured (" + e.getMessage()
                        + ") while saving update "
                        + remoteDescriptor.getRevision().getName() + " of "
                        + remoteDescriptor.getName());
            }
        }
        else {
            // Alredy last version.
        }
    }
    
    // Method used to scan ./minigames/{minigame}/ folder
    private void scanPluginDir(final File folder) {
        File jar = new File(folder, folder.getName() + ".jar");
        File desc = new File(folder, "description.xml");
        
        if (jar.exists() && desc.exists()) {
            try {
                MinigameDescriptor descriptor = MinigameDescriptor.load(desc);
                
                // Add minigame plugin.
                this.minigames.add(descriptor);
                
                // Add tags.
                this.tags.addAll(Arrays.asList(descriptor.getTags()));
                
            } catch (Exception ex) {
                this.logger.error("File " + desc.getAbsolutePath()
                        + " is not valid descriptor!");
            }
            
        }
        else {
            this.logger.error("Folder " + folder.getAbsolutePath()
                    + " is not valid plugin package!");
        }
    }
    
    private void scanMapDir(final File folder) {
        // TODO: Not implemented.
    }
    
    public Collection<MinigameDescriptor> getAvaiablePlugins() {
        return Collections.unmodifiableSet(this.minigames);
    }
    
    public Collection<MapDescriptor> getAvaiableMaps() {
        return Collections.unmodifiableSet(this.maps);
    }
}