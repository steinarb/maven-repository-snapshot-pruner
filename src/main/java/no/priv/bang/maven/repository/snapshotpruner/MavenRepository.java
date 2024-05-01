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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class MavenRepository {

    private Path repositoryDirectory;
    private XPathFactory xpathFactory;
    private XPathExpression<Element> snapshotVersionXPathExpression;

    public MavenRepository(Path currentDirectory) {
        xpathFactory = XPathFactory.instance();
        snapshotVersionXPathExpression = xpathFactory.compile("/metadata/versioning/snapshotVersions/snapshotVersion/value", Filters.element());
        this.repositoryDirectory = currentDirectory;
    }

    public int pruneSnapshots() throws IOException, JDOMException {
        var numberOfDeletedFiles = 0;
        for (var metadata : findMavenMetadataFilesWithSnapshotVersion()) {
            numberOfDeletedFiles += metadata.deleteFilesNotPartOfSnapshot();
        }

        return numberOfDeletedFiles;
    }

    List<MavenMetadata> findMavenMetadataFilesWithSnapshotVersion() throws IOException, JDOMException {
        var parsedMetadataFiles = new ArrayList<MavenMetadata>();
        for (var mavenMetadataFile : findMavenMetadataFiles()) {
            var metadata = parseMavenMetdata(mavenMetadataFile);
            if (metadata.hasSnapshotVersion()) {
                parsedMetadataFiles.add(metadata);
            }
        }

        return parsedMetadataFiles;
    }

    List<Path> findMavenMetadataFiles() throws IOException {
        var mavenMetadataCollector = new FileCollector("maven-metadata.xml");
        Files.walkFileTree(repositoryDirectory, mavenMetadataCollector);
        return mavenMetadataCollector.getMatchedFilenames();
    }

    MavenMetadata parseMavenMetdata(Path mavenMetadataFile) throws JDOMException, IOException {
        var mavenMetadata = new MavenMetadata(mavenMetadataFile);
        var builder = new SAXBuilder();
        builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder.setExpandEntities(false);
        var document = builder.build(mavenMetadataFile.toFile());
        var versionElements = snapshotVersionXPathExpression.evaluate(document);
        if (!versionElements.isEmpty()) {
            var snapshotVersion = versionElements.get(0).getText();
            mavenMetadata.setSnapshotVersion(snapshotVersion);
        }

        return mavenMetadata;
    }

    static class FileCollector extends SimpleFileVisitor<Path> {
        String filenameToMatch;
        List<Path> matchedFilenames;

        public FileCollector(String filenameToMatch) {
            super();
            this.filenameToMatch = filenameToMatch;
            matchedFilenames = new ArrayList<>();
        }

        public List<Path> getMatchedFilenames() {
            return matchedFilenames;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.toString().endsWith(filenameToMatch)) {
                matchedFilenames.add(file);
            }

            return FileVisitResult.CONTINUE;
        }
    }

}
