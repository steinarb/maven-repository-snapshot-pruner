package no.priv.bang.maven.repository.snapshotpruner;


import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static no.priv.bang.maven.repository.snapshotpruner.MavenProperties.*;

import org.jdom2.JDOMException;
import org.junit.jupiter.api.Test;

class MavenRepositorySnapshotPrunerTest {

    @Test
    void testRunSnapshotPrunerApplication() throws IOException, JDOMException {
        copyMockMavenSnapshotRepository();
        var currentDirectory = setCurrentDirectoryToMockRepositoryTop();

        // Check the preconditions
        var repository = new MavenRepository(currentDirectory);
        var totalNumberOfFilesBeforeDelete = repository.findMavenMetadataFilesWithSnapshotVersion().stream().mapToInt(p -> p.getFilesInDirectory().size()).sum();
        assertEquals(420, totalNumberOfFilesBeforeDelete);

        // Run the code to be tested, ie. the repository pruner application
        MavenRepositorySnapshotPruner.main(new String[0]);

        // Verify that the old snapshot files have actually been deleted
        var totalNumberOfFilesAfterDelete = repository.findMavenMetadataFilesWithSnapshotVersion().stream().mapToInt(p -> p.getFilesInDirectory().size()).sum();
        assertEquals(24, totalNumberOfFilesAfterDelete);
    }

    private Path setCurrentDirectoryToMockRepositoryTop() {
        var repositoryDirectory = Paths.get(maven.getProperty("repository.top"));
        System.setProperty("user.dir", repositoryDirectory.toString());
        var currentDirectory = Paths.get(System.getProperty("user.dir"));
        return currentDirectory;
    }

}
