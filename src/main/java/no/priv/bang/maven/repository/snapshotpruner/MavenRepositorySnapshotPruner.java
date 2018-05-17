package no.priv.bang.maven.repository.snapshotpruner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom2.JDOMException;

public class MavenRepositorySnapshotPruner {

    public static void main(String[] args) throws IOException, JDOMException {
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));
        MavenRepository repository = new MavenRepository(currentDirectory);
        System.out.println("Starting snapshot pruning in maven repository under \"" + currentDirectory + "\""); // NOSONAR This is feedback to the user from a command line program, so not using a log is quite OK
        int numberOfDeletedFiles = repository.pruneSnapshots();
        int numberOfDirectoriesWithSnapshotVersions = repository.findMavenMetadataFilesWithSnapshotVersion().size();
        System.out.println("Deleted " + numberOfDeletedFiles + " snapshot files in " + numberOfDirectoriesWithSnapshotVersions + " directories with snapshots"); // NOSONAR This is feedback to the user from a command line program, so not using a log is quite OK
    }

}
