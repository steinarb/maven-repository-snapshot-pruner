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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static no.priv.bang.maven.repository.snapshotpruner.MavenProperties.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.jdom2.JDOMException;
import org.junit.jupiter.api.Test;

class MavenRepositoryTest {

    @Test
    void testFindMavenMetadataFiles() throws IOException {
        var repositoryDirectory = Paths.get(maven.getProperty("repository.top"));
        var repository = new MavenRepository(repositoryDirectory);
        var metadataFiles = repository.findMavenMetadataFiles();
        assertThat(metadataFiles).hasSize(4);
    }

    @Test
    void testParseMavenMetadataFile() throws IOException, JDOMException {
        var repositoryDirectory = Paths.get(maven.getProperty("repository.top"));
        var repository = new MavenRepository(repositoryDirectory);

        var mavenMetadataFileWithSnapshotVersion = Paths.get(repositoryDirectory.toString(), "no/priv/bang/ukelonn/ukelonn.api/1.0.0-SNAPSHOT/maven-metadata.xml");
        var mavenMetadataWithSnapshotVersion = repository.parseMavenMetdata(mavenMetadataFileWithSnapshotVersion);
        assertTrue(mavenMetadataWithSnapshotVersion.hasSnapshotVersion());
        assertEquals("1.0.0-20170922.181212-25", mavenMetadataWithSnapshotVersion.getSnapshotVersion());
        assertEquals(mavenMetadataFileWithSnapshotVersion, mavenMetadataWithSnapshotVersion.getPath());

        var mavenMetadataFileWithoutSnapshotVersion = Paths.get(repositoryDirectory.toString(), "no/priv/bang/ukelonn/ukelonn.api/maven-metadata.xml");
        var mavenMetadataWithoutSnapshotVersion = repository.parseMavenMetdata(mavenMetadataFileWithoutSnapshotVersion);
        assertFalse(mavenMetadataWithoutSnapshotVersion.hasSnapshotVersion());
    }

    @Test
    void testFindMavenMetadataFilesWithSnapshotVersion() throws IOException, JDOMException {
        var repositoryDirectory = Paths.get(maven.getProperty("repository.top"));
        var repository = new MavenRepository(repositoryDirectory);
        List<MavenMetadata> metadataFiles = repository.findMavenMetadataFilesWithSnapshotVersion();
        assertThat(metadataFiles).hasSize(2);
    }

    @Test
    void testPruneSnapshotsInRepository() throws IOException, JDOMException {
        copyMockMavenSnapshotRepository();
        var repositoryDirectory = Paths.get(maven.getProperty("repository.top"));
        var repository = new MavenRepository(repositoryDirectory);

        var totalNumberOfDeletedFiles = repository.pruneSnapshots();
        assertEquals(396, totalNumberOfDeletedFiles);
    }

}
