/*
 * Copyright 2017-2024 Steinar Bang
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
import static org.assertj.core.api.Assertions.assertThat;
import static no.priv.bang.maven.repository.snapshotpruner.MavenProperties.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.jdom2.JDOMException;
import org.junit.jupiter.api.Test;

class MavenMetadataTest {

    @Test
    void testDeleteSnapshotFiles() throws JDOMException, IOException {
        copyMockMavenSnapshotRepository();
        var repositoryDirectory = Paths.get(maven.getProperty("repository.top"));
        var repository = new MavenRepository(repositoryDirectory);

        var mavenMetadataFileWithSnapshotVersion = Paths.get(repositoryDirectory.toString(), "no/priv/bang/ukelonn/ukelonn.api/1.0.0-SNAPSHOT/maven-metadata.xml");
        var mavenMetadataWithSnapshotVersion = repository.parseMavenMetdata(mavenMetadataFileWithSnapshotVersion);
        assertHasSha1AndMd5Checksums(findMavenMetadataFiles(mavenMetadataWithSnapshotVersion.getFilesInDirectory()));
        assertTrue(mavenMetadataWithSnapshotVersion.hasSnapshotVersion());
        assertEquals("1.0.0-20170922.181212-25", mavenMetadataWithSnapshotVersion.getSnapshotVersion());
        assertEquals(mavenMetadataFileWithSnapshotVersion, mavenMetadataWithSnapshotVersion.getPath());
        assertEquals(378, mavenMetadataWithSnapshotVersion.getFilesInDirectory().size());
        assertEquals(18, mavenMetadataWithSnapshotVersion.getCurrentSnapshotFilesInDirectory().size());
        var numberOfDeletedFiles = mavenMetadataWithSnapshotVersion.deleteFilesNotPartOfSnapshot();
        assertEquals(360, numberOfDeletedFiles);
        assertEquals(18, mavenMetadataWithSnapshotVersion.getFilesInDirectory().size());
        assertHasSha1AndMd5Checksums(findMavenMetadataFiles(mavenMetadataWithSnapshotVersion.getFilesInDirectory()));
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
        var repositoryDirectory = Paths.get(maven.getProperty("repository.top"));
        var repository = new MavenRepository(repositoryDirectory);

        var mavenMetadataFileWithSnapshotVersion = Paths.get(repositoryDirectory.toString(), "no/priv/bang/ukelonn/ukelonn.api/1.0.0-SNAPSHOT/maven-metadata.xml");
        var mavenMetadataWithSnapshotVersion = repository.parseMavenMetdata(mavenMetadataFileWithSnapshotVersion);
        var originalNumberOfFiles = mavenMetadataWithSnapshotVersion.getFilesInDirectory().size();
        var numberOfFilesThatShouldNotBeDeleted = mavenMetadataWithSnapshotVersion.getCurrentSnapshotFilesInDirectory().size();
        var filesToDelete = new HashSet<File>(mavenMetadataWithSnapshotVersion.getFilesInDirectory());
        filesToDelete.removeAll(mavenMetadataWithSnapshotVersion.getCurrentSnapshotFilesInDirectory());
        deleteAFileInAdvanceOfTheTest(filesToDelete);
        var numberOfDeletedFiles = mavenMetadataWithSnapshotVersion.doDeleteFilesNotPartOfSnapshot(filesToDelete, Collections.emptyList());
        assertEquals(originalNumberOfFiles - numberOfFilesThatShouldNotBeDeleted - 1, numberOfDeletedFiles);
    }


    void deleteAFileInAdvanceOfTheTest(Set<File> filesToDelete) {
        var fileToDeleteInAdvance = filesToDelete.iterator().next();
        fileToDeleteInAdvance.delete();
    }


    private List<String> findMavenMetadataFiles(List<File> filesInDirectory) {
        return filesInDirectory
            .stream()
            .map(File::getAbsolutePath)
            .filter(n -> n.contains("maven-metadata"))
            .collect(Collectors.toList());
    }

    private void assertHasSha1AndMd5Checksums(List<String> mavenMetadataFiles) {
        var sha1Checksum = new Condition<String>(filename -> filename.endsWith("sha1"), "sha1 checksum file");
        var md5Checksum = new Condition<String>(filename -> filename.endsWith("md5"), "md5 checksum file");
        assertThat(mavenMetadataFiles).as("sha1 and/or md5 file missing").haveAtLeastOne(sha1Checksum).haveAtLeastOne(md5Checksum);
    }
}
