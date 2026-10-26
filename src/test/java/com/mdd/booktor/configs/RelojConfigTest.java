package com.mdd.booktor.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class RelojConfigTest {

    @Test
    void elRelojUsaLaHoraDeArgentina() {
        RelojConfig config = new RelojConfig();

        assertEquals(ZoneId.of("America/Argentina/Buenos_Aires"), config.clock().getZone());
    }
}