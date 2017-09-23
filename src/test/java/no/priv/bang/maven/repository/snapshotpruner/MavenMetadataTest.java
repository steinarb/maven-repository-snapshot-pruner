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

import static org.junit.Assert.*;
import static no.priv.bang.maven.repository.snapshotpruner.MavenProperties.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom2.JDOMException;
import org.junit.Test;

public class MavenMetadataTest {

    @Test
    public void testDeleteSnapshotFiles() throws JDOMException, IOException {
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

}
