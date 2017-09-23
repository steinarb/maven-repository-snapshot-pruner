/*
 * Copyright 2017 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.maven.repository.snapshotpruner;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MavenMetadata {

    private String snapshotVersion;
    private Path path;

    public MavenMetadata(Path mavenMetadataFile) {
        path = mavenMetadataFile;
    }

    public boolean hasSnapshotVersion() {
        return snapshotVersion != null;
    }

    public void setSnapshotVersion(String snapshotVersion) {
        this.snapshotVersion = snapshotVersion;
    }

    public String getSnapshotVersion() {
        return snapshotVersion;
    }

    public Path getPath() {
        return path;
    }

    public List<File> getFilesInDirectory() {
        return Arrays.asList(path.getParent().toFile().listFiles());
    }

    public List<File> getCurrentSnapshotFilesInDirectory() {
        FilenameFilter snapshotFilter = new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.contains(snapshotVersion) || name.equals("maven-metadata.xml");
                }
            };
        File directory = path.getParent().toFile();
        File[] snapshotFiles = directory.listFiles(snapshotFilter);
        return Arrays.asList(snapshotFiles);
    }

    public int deleteFilesNotPartOfSnapshot() {
        int numberOfDeletedFiles = 0;
        Set<File> filesToDelete = new HashSet<File>(getFilesInDirectory());
        filesToDelete.removeAll(getCurrentSnapshotFilesInDirectory());
        for (File file : filesToDelete) {
            file.delete();
            numberOfDeletedFiles++;
        }

        return numberOfDeletedFiles;
    }

}
