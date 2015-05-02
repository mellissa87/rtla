package com.github.b0ch3nski.logback.model;

import com.github.b0ch3nski.logback.util.RandomLogFactory;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author bochen
 */
public class SimplifiedLogTest {

    @Test
    public void shouldTestObjectsEquality() {
        SimplifiedLog test1 = RandomLogFactory.create();
        SimplifiedLog test2 = RandomLogFactory.create();

        assertThat(test1, is(test1));
        assertThat(test1, not(test2));
        assertThat(null, not(test2));
    }
}
