/*
 * Copyright 2017-2021 Steinar Bang
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

import static org.junit.jupiter.api.Assertions.*;
import static no.priv.bang.maven.repository.snapshotpruner.MavenProperties.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jdom2.JDOMException;
import org.junit.jupiter.api.Test;

class MavenMetadataTest {

    @Test
    void testDeleteSnapshotFiles() throws JDOMException, IOException {
        copyMockMavenSnapshotRepository();
        Path repositoryDirectory = Paths.get(maven.getProperty("repository.top"));
        MavenRepository repository = new MavenRepository(repositoryDirectory);

        Path mavenMetadataFileWithSnapshotVersion = Paths.get(repositoryDirectory.toString(), "no/priv/bang/ukelonn/ukelonn.api/1.0.0-SNAPSHOT/maven-metadata.xml");
        MavenMetadata mavenMetadataWithSnapshotVersion = repository.parseMavenMetdata(mavenMetadataFileWithSnapshotVersion);
        assertTrue(mavenMetadataWithSnapshotVersion.hasSnapshotVersion());
        assertEquals("1.0.0-20170922.181212-25", mavenMetadataWithSnapshotVersion.getSnapshotVersion());
        assertEquals(mavenMetadataFileWithSnapshotVersion, mavenMetadataWithSnapshotVersion.getPath());
        assertEquals(378, mavenMetadataWithSnapshotVersion.getFilesInDirectory().size());
        assertEquals(16, mavenMetadataWithSnapshotVersion.getCurrentSnapshotFilesInDirectory().size());
        int numberOfDeletedFiles = mavenMetadataWithSnapshotVersion.deleteFilesNotPartOfSnapshot();
        assertEquals(362, numberOfDeletedFiles);
        assertEquals(16, mavenMetadataWithSnapshotVersion.getFilesInDirectory().size());
    }


    /**
     * Corner case test: Verifies that trying to delete non-existing file
     * will not cause any problems and the failed file will not be counted
     * as deleted.
     *
     * @throws Exception
     */
    @Test
    void testDeleteSnapshotFilesWhenErrorOccurs() throws Exception {
        copyMockMavenSnapshotRepository();
        Path repositoryDirectory = Paths.get(maven.getProperty("repository.top"));
        MavenRepository repository = new MavenRepository(repositoryDirectory);

        Path mavenMetadataFileWithSnapshotVersion = Paths.get(repositoryDirectory.toString(), "no/priv/bang/ukelonn/ukelonn.api/1.0.0-SNAPSHOT/maven-metadata.xml");
        MavenMetadata mavenMetadataWithSnapshotVersion = repository.parseMavenMetdata(mavenMetadataFileWithSnapshotVersion);
        int originalNumberOfFiles = mavenMetadataWithSnapshotVersion.getFilesInDirectory().size();
        int numberOfFilesThatShouldNotBeDeleted = mavenMetadataWithSnapshotVersion.getCurrentSnapshotFilesInDirectory().size();
        Set<File> filesToDelete = new HashSet<>(mavenMetadataWithSnapshotVersion.getFilesInDirectory());
        filesToDelete.removeAll(mavenMetadataWithSnapshotVersion.getCurrentSnapshotFilesInDirectory());
        deleteAFileInAdvanceOfTheTest(filesToDelete);
        int numberOfDeletedFiles = mavenMetadataWithSnapshotVersion.doDeleteFilesNotPartOfSnapshot(filesToDelete, Collections.emptyList());
        assertEquals(originalNumberOfFiles - numberOfFilesThatShouldNotBeDeleted - 1, numberOfDeletedFiles);
    }


    void deleteAFileInAdvanceOfTheTest(Set<File> filesToDelete) {
        File fileToDeleteInAdvance = filesToDelete.iterator().next();
        fileToDeleteInAdvance.delete();
    }
}
