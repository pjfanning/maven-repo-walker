package com.github.pjfanning.mavenwalker;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PomUtilsTest {
    @Test
    void testHadoopPom() throws Exception {
        try (InputStream stream = PomUtilsTest.class.getResourceAsStream("/hadoop-common-3.3.1.pom")) {
            assertEquals("org.apache.hadoop:hadoop-common:3.3.1 (scope=compile)",
                    PomUtils.evalLog4jv1Dependency(stream));
        }
    }
}
