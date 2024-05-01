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
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

/**
 * Utility class to access maven properties stored in a file.
 *
 * @author Steinar Bang
 *
 */
public class MavenProperties {
    public static Properties maven;

    static {
        maven = new Properties();
        try {
            maven.load(MavenRepositoryTest.class.getClassLoader().getResourceAsStream("maven.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyMockMavenSnapshotRepository() throws IOException {
        var repositorySourceDirectory = Paths.get(maven.getProperty("repository.source"));
        var repositoryDirectory = Paths.get(maven.getProperty("repository.top"));
        FileUtils.copyDirectory(repositorySourceDirectory.toFile(), repositoryDirectory.toFile(), true);
    }
}
