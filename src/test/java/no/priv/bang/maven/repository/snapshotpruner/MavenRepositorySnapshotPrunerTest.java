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
        Path currentDirectory = setCurrentDirectoryToMockRepositoryTop();

        // Check the preconditions
        MavenRepository repository = new MavenRepository(currentDirectory);
        int totalNumberOfFilesBeforeDelete = repository.findMavenMetadataFilesWithSnapshotVersion().stream().mapToInt(p -> p.getFilesInDirectory().size()).sum();
        assertEquals(420, totalNumberOfFilesBeforeDelete);

        // Run the code to be tested, ie. the repository pruner application
        MavenRepositorySnapshotPruner.main(new String[0]);

        // Verify that the old snapshot files have actually been deleted
        int totalNumberOfFilesAfterDelete = repository.findMavenMetadataFilesWithSnapshotVersion().stream().mapToInt(p -> p.getFilesInDirectory().size()).sum();
        assertEquals(20, totalNumberOfFilesAfterDelete);
    }

    private Path setCurrentDirectoryToMockRepositoryTop() {
        Path repositoryDirectory = Paths.get(maven.getProperty("repository.top"));
        System.setProperty("user.dir", repositoryDirectory.toString());
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));
        return currentDirectory;
    }

}
